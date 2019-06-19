package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableObject;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.TileEntityLiquidTankElectricMachine;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradableProperty;
import ic2.core.util.LiquidUtil;
import ic2.core.util.PumpUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityPump extends TileEntityLiquidTankElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider
{
	public final int defaultTier;
	public int energyConsume;
	public int operationsPerTick;
	public final int defaultEnergyStorage;
	public final int defaultEnergyConsume;
	public final int defaultOperationLength;
	private AudioSource audioSource;
	private TileEntityMiner miner = null;
	public boolean redstonePowered = false;
	public final InvSlotCharge chargeSlot = new InvSlotCharge(this, 1);
	public final InvSlotConsumableLiquid containerSlot = new InvSlotConsumableLiquid(this, "input", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill);
	public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	public short progress = 0;
	public int operationLength;
	@GuiSynced
	public float guiProgress;

	public TileEntityPump()
	{
		super(20, 1, 8);
		this.defaultEnergyConsume = this.energyConsume = 1;
		this.defaultOperationLength = this.operationLength = 20;
		this.defaultTier = 1;
		this.defaultEnergyStorage = 1 * this.operationLength;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.worldObj.isRemote)
			this.setUpgradestat();

	}

	@Override
	protected void onUnloaded()
	{
		if (IC2.platform.isRendering() && this.audioSource != null)
		{
			IC2.audioManager.removeSources(this);
			this.audioSource = null;
		}

		this.miner = null;
		super.onUnloaded();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.progress = nbt.getShort("progress");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setShort("progress", this.progress);
		return nbt;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.canoperate() && this.energy.getEnergy() >= this.energyConsume * this.operationLength)
			if (this.progress < this.operationLength)
			{
				++this.progress;
				this.energy.useEnergy(this.energyConsume);
			}
			else
			{
				this.progress = 0;
				this.operate(false);
			}

		MutableObject<ItemStack> output = new MutableObject();
		if (this.containerSlot.transferFromTank(this.fluidTank, output, true) && (output.getValue() == null || this.outputSlot.canAdd(output.getValue())))
		{
			this.containerSlot.transferFromTank(this.fluidTank, output, false);
			if (output.getValue() != null)
				this.outputSlot.add(output.getValue());
		}

		for (int i = 0; i < this.upgradeSlot.size(); ++i)
		{
			ItemStack stack = this.upgradeSlot.get(i);
			if (stack != null && stack.getItem() instanceof IUpgradeItem && ((IUpgradeItem) stack.getItem()).onTick(stack, this))
				needsInvUpdate = true;
		}

		this.guiProgress = (float) this.progress / (float) this.operationLength;
		if (needsInvUpdate)
			super.markDirty();

	}

	public boolean canoperate()
	{
		return this.operate(true);
	}

	public boolean operate(boolean sim)
	{
		if (this.miner == null || this.miner.isInvalid())
		{
			this.miner = null;

			for (EnumFacing dir : EnumFacing.VALUES)
				if (dir != EnumFacing.UP)
				{
					TileEntity te = this.worldObj.getTileEntity(this.pos.offset(dir));
					if (te instanceof TileEntityMiner)
					{
						this.miner = (TileEntityMiner) te;
						break;
					}
				}
		}

		FluidStack liquid = null;
		if (this.miner != null)
		{
			if (this.miner.canProvideLiquid)
				liquid = this.pump(this.miner.liquidPos, sim, this.miner);
		}
		else
		{
			EnumFacing dir = this.getFacing();
			liquid = this.pump(this.pos.offset(dir), sim, this.miner);
		}

		if (liquid != null && this.getFluidTank().fill(liquid, false) > 0)
		{
			if (!sim)
				this.getFluidTank().fill(liquid, true);

			return true;
		}
		else
			return false;
	}

	public FluidStack pump(BlockPos startPos, boolean sim, TileEntity miner)
	{
		int freeSpace = this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
		if (miner == null && freeSpace > 0)
		{
			TileEntity te = this.worldObj.getTileEntity(startPos);
			EnumFacing side = this.getFacing().getOpposite();
			if (te != null && LiquidUtil.isFluidTile(te, side))
    
				if (this.fake.cantBreak(this.worldObj, startPos))
    

				if (freeSpace > 1000)
					freeSpace = 1000;

				return LiquidUtil.drainTile(te, side, freeSpace, sim);
			}
		}

		if (freeSpace >= 1000)
		{
			BlockPos cPos = PumpUtil.searchFluidSource(this.worldObj, startPos);
			if (cPos != null)
    
				if (this.fake.cantBreak(this.worldObj, cPos))
    

				return LiquidUtil.drainBlock(this.worldObj, cPos, sim);
			}
		}

		return null;
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (IC2.platform.isSimulating())
			this.setUpgradestat();

	}

	public void setUpgradestat()
	{
		int extraProcessTime = 0;
		double processTimeMultiplier = 1.0D;
		int extraEnergyDemand = 0;
		double energyDemandMultiplier = 1.0D;
		int extraEnergyStorage = 0;
		double energyStorageMultiplier = 1.0D;
		int extraTier = 0;

		for (int i = 0; i < this.upgradeSlot.size(); ++i)
		{
			ItemStack stack = this.upgradeSlot.get(i);
			if (stack != null && stack.getItem() instanceof IUpgradeItem)
			{
				IUpgradeItem upgrade = (IUpgradeItem) stack.getItem();
				extraProcessTime += upgrade.getExtraProcessTime(stack, this) * stack.stackSize;
				processTimeMultiplier *= Math.pow(upgrade.getProcessTimeMultiplier(stack, this), stack.stackSize);
				extraEnergyDemand += upgrade.getExtraEnergyDemand(stack, this) * stack.stackSize;
				energyDemandMultiplier *= Math.pow(upgrade.getEnergyDemandMultiplier(stack, this), stack.stackSize);
				extraEnergyStorage += upgrade.getExtraEnergyStorage(stack, this) * stack.stackSize;
				energyStorageMultiplier *= Math.pow(upgrade.getEnergyStorageMultiplier(stack, this), stack.stackSize);
				extraTier += upgrade.getExtraTier(stack, this) * stack.stackSize;
			}
		}

		double previousProgress = (double) this.progress / (double) this.operationLength;
		double stackOpLen = ((double) this.defaultOperationLength + (double) extraProcessTime) * 64.0D * processTimeMultiplier;
		this.operationsPerTick = (int) Math.min(Math.ceil(64.0D / stackOpLen), 2.147483647E9D);
		this.operationLength = (int) Math.round(stackOpLen * this.operationsPerTick / 64.0D);
		this.energyConsume = applyModifier(this.defaultEnergyConsume, extraEnergyDemand, energyDemandMultiplier);
		this.energy.setSinkTier(applyModifier(this.defaultTier, extraTier, 1.0D));
		this.energy.setCapacity(applyModifier(this.defaultEnergyStorage, extraEnergyStorage + this.operationLength * this.energyConsume, energyStorageMultiplier));
		if (this.operationLength < 1)
			this.operationLength = 1;

		this.progress = (short) (int) Math.floor(previousProgress * this.operationLength + 0.1D);
	}

	private static int applyModifier(int base, int extra, double multiplier)
	{
		double ret = Math.round(((double) base + (double) extra) * multiplier);
		return ret > 2.147483647E9D ? Integer.MAX_VALUE : (int) ret;
	}

	@Override
	public double getGuiValue(String name)
	{
		if (name.equals("progress"))
			return this.guiProgress;
		else
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + name);
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public ContainerBase<TileEntityPump> getGuiContainer(EntityPlayer player)
	{
		return DynamicContainer.<TileEntityPump>create(this, player, GuiParser.parse(this.teBlock));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("active"))
		{
			if (this.audioSource == null)
				this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/PumpOp.ogg", true, false, IC2.audioManager.getDefaultVolume());

			if (this.getActive())
			{
				if (this.audioSource != null)
					this.audioSource.play();
			}
			else if (this.audioSource != null)
				this.audioSource.stop();
		}

		super.onNetworkUpdate(field);
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return false;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return true;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Processing, new UpgradableProperty[] { UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing });
	}
}
