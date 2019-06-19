package ic2.core.block.kineticgenerator.tileentity;

import java.util.EnumSet;
import java.util.Set;

import ic2.api.energy.tile.IKineticSource;
import ic2.core.ContainerBase;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiSteamKineticGenerator;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.IUpgradeItem;
import ic2.core.upgrade.UpgradableProperty;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySteamKineticGenerator extends TileEntityInventory implements IKineticSource, IFluidHandler, IHasGui, IUpgradableBlock
{
	protected final FluidTank steamTank = new FluidTank(21000);
	protected final FluidTank distilledWaterTank = new FluidTank(1000);
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
	public final InvSlotConsumable turbineSlot = new InvSlotConsumableItemStack(this, "Turbineslot", 1, new ItemStack[] { ItemName.crafting.getItemStack(CraftingItemType.steam_turbine) });
	private static final float outputModifier = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/steam");
	private int kUoutput;
	private boolean isTurbineFilledWithWater = false;
	private float condensationProgress = 0.0F;
	private int updateTicker = IC2.random.nextInt(this.getTickRate());

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.distilledWaterTank.getCapacity() - this.distilledWaterTank.getFluidAmount() >= 1 && this.isTurbineFilledWithWater)
			this.isTurbineFilledWithWater = false;

		if (this.steamTank.getFluidAmount() > 10 && !this.isTurbineFilledWithWater && !this.turbineSlot.isEmpty())
		{
			if (!this.getActive())
			{
				this.setActive(true);
				needsInvUpdate = true;
			}

			boolean turbineDoneWork = this.turbineDoWork();
			if (this.updateTicker++ >= this.getTickRate())
			{
				if (turbineDoneWork)
					this.turbineSlot.damage(this.isHotSteam() ? 1 : 2, false);

				this.updateTicker = 0;
			}
		}
		else if (this.getActive())
		{
			this.setActive(false);
			needsInvUpdate = true;
			this.kUoutput = 0;
		}

		for (int slot = 0; slot < this.upgradeSlot.size(); ++slot)
		{
			ItemStack stack = this.upgradeSlot.get(slot);
			if (stack != null && stack.getItem() instanceof IUpgradeItem)
				needsInvUpdate = ((IUpgradeItem) stack.getItem()).onTick(stack, this) || needsInvUpdate;
		}

		if (needsInvUpdate)
			super.markDirty();

	}

	private float handleSteam(int amount)
	{
		this.steamTank.drain(amount, true);
		float KUWorkbuffer = amount * 2 * (this.isHotSteam() ? 2.0F : 1.0F);
		if (this.isHotSteam())
			this.Steamoutput(amount);
		else
		{
			this.condensationProgress += amount / 100.0F * 10.0F;
			this.Steamoutput(amount / 100.0F * 90.0F);
		}

		return KUWorkbuffer;
	}

	private boolean turbineDoWork()
	{
		float KUWorkbuffer = 0.0F;
		int steamAmount = this.steamTank.getFluidAmount();
		if (steamAmount > 18000)
			KUWorkbuffer = this.handleSteam(1000);
		else if (steamAmount > 16000)
			KUWorkbuffer = this.handleSteam(800);
		else if (steamAmount > 12000)
			KUWorkbuffer = this.handleSteam(600);
		else if (steamAmount > 8000)
			KUWorkbuffer = this.handleSteam(400);
		else if (steamAmount > 4000)
			KUWorkbuffer = this.handleSteam(200);
		else if (steamAmount > 2000)
			KUWorkbuffer = this.handleSteam(100);
		else if (steamAmount > 1000)
			KUWorkbuffer = this.handleSteam(50);
		else if (steamAmount > 800)
			KUWorkbuffer = this.handleSteam(40);
		else if (steamAmount > 600)
			KUWorkbuffer = this.handleSteam(30);
		else if (steamAmount > 400)
			KUWorkbuffer = this.handleSteam(20);
		else if (steamAmount > 10)
			KUWorkbuffer = this.handleSteam(10);

		if (this.condensationProgress >= 100.0F)
			if (this.distilledWaterTank.fill(new FluidStack(FluidName.distilled_water.getInstance(), 1), false) == 1)
			{
				this.condensationProgress -= 100.0F;
				this.distilledWaterTank.fill(new FluidStack(FluidName.distilled_water.getInstance(), 1), true);
			}
			else
				this.isTurbineFilledWithWater = true;

		this.kUoutput = (int) (KUWorkbuffer * (100.0F - (float) this.distilledWaterTank.getFluidAmount() / (float) this.distilledWaterTank.getCapacity() * 100.0F) / 100.0F * outputModifier);
		return KUWorkbuffer > 0.0F;
	}

	private void Steamoutput(float amount)
	{
		for (EnumFacing dir : EnumFacing.VALUES)
		{
			TileEntity te = this.worldObj.getTileEntity(this.pos.offset(dir));
			if (this.isHotSteam())
			{
				if (!(te instanceof TileEntityCondenser) && !(te instanceof TileEntitySteamKineticGenerator))
					continue;
			}
			else if (!(te instanceof TileEntityCondenser))
				continue;

			IFluidHandler target = (IFluidHandler) te;
			int transAmount = target.fill(dir.getOpposite(), new FluidStack(FluidName.steam.getInstance(), (int) amount), false);
			if (transAmount > 0)
			{
				if (amount > transAmount)
				{
					target.fill(dir.getOpposite(), new FluidStack(FluidName.steam.getInstance(), (int) amount), true);
					amount -= transAmount;
				}
				else
				{
					target.fill(dir.getOpposite(), new FluidStack(FluidName.steam.getInstance(), (int) amount), true);
					amount = 0.0F;
				}

				if (amount == 0.0F)
					break;
			}
		}

		if (amount > 0.0F && this.worldObj.rand.nextInt(10) == 0)
    
			ExplosionIC2 explosion = new ExplosionIC2(this.worldObj, null, this.pos, 1, 1.0F, ExplosionIC2.Type.Heat);
			explosion.fake.setParent(this.fake);
    
		}
	}

	public int getKUoutput()
	{
		return this.kUoutput;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.condensationProgress = nbttagcompound.getFloat("condensationprogress");
		this.distilledWaterTank.readFromNBT(nbttagcompound.getCompoundTag("distilledwaterTank"));
		this.steamTank.readFromNBT(nbttagcompound.getCompoundTag("SteamTank"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setFloat("condensationprogress", this.condensationProgress);
		nbt.setTag("distilledwaterTank", this.distilledWaterTank.writeToNBT(new NBTTagCompound()));
		nbt.setTag("SteamTank", this.steamTank.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	@Override
	public ContainerBase<TileEntitySteamKineticGenerator> getGuiContainer(EntityPlayer player)
	{
		return new ContainerSteamKineticGenerator(player, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiSteamKineticGenerator(new ContainerSteamKineticGenerator(player, this));
	}

	@Override
	public int maxrequestkineticenergyTick(EnumFacing directionFrom)
	{
		return directionFrom == this.getFacing() ? this.kUoutput : 0;
	}

	@Override
	public int requestkineticenergy(EnumFacing directionFrom, int requestkineticenergy)
	{
		return directionFrom == this.getFacing() ? this.kUoutput : 0;
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if (resource != null && this.canFill(from, resource.getFluid()))
		{
			if (resource.getFluid() != FluidName.steam.getInstance() && resource.getFluid() != FluidName.superheated_steam.getInstance())
			{
				if (resource.getFluid() != FluidRegistry.WATER && resource.getFluid() != FluidName.distilled_water.getInstance())
					return 0;
				else
				{
					if (this.distilledWaterTank.getFluid() != null && this.distilledWaterTank.getFluid().getFluid() != resource.getFluid())
						this.distilledWaterTank.drain(this.distilledWaterTank.getFluidAmount(), true);

					return this.distilledWaterTank.fill(resource, doFill);
				}
			}
			else
			{
				if (this.steamTank.getFluid() != null && this.steamTank.getFluid().getFluid() != resource.getFluid())
					this.steamTank.drain(this.steamTank.getFluidAmount(), true);

				return this.steamTank.fill(resource, doFill);
			}
		}
		else
			return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		return resource != null && resource.isFluidEqual(this.distilledWaterTank.getFluid()) && this.canDrain(from, resource.getFluid()) ? this.distilledWaterTank.drain(resource.amount, doDrain) : null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		return this.distilledWaterTank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return from == this.getFacing() ? false : fluid != FluidName.steam.getInstance() && fluid != FluidName.superheated_steam.getInstance() ? fluid != FluidRegistry.WATER && fluid != FluidName.distilled_water.getInstance() ? false : this.distilledWaterTank.getFluidAmount() < this.distilledWaterTank.getCapacity() : this.steamTank.getFluidAmount() < this.steamTank.getCapacity();
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		if (from.getOpposite() != this.getFacing() && from != this.getFacing())
		{
			FluidStack fs = this.distilledWaterTank.getFluid();
			return fs != null && fs.getFluid() == fluid;
		}
		else
			return false;
	}

	public int gaugeLiquidScaled(int i, int tank)
	{
		return tank == 0 && this.distilledWaterTank.getFluidAmount() > 0 ? this.distilledWaterTank.getFluidAmount() * i / this.distilledWaterTank.getCapacity() : 0;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return new FluidTankInfo[] { this.distilledWaterTank.getInfo(), this.steamTank.getInfo() };
	}

	@Override
	public double getEnergy()
	{
		return 0.0D;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return false;
	}

	public int getDistilledWaterTankFill()
	{
		return this.distilledWaterTank.getFluidAmount();
	}

	public FluidTank getDistilledWaterTank()
	{
		return this.distilledWaterTank;
	}

	public boolean isHotSteam()
	{
		return this.steamTank.getFluid() != null && this.steamTank.getFluid().getFluid() == FluidName.superheated_steam.getInstance();
	}

	public boolean hasTurbine()
	{
		return !this.turbineSlot.isEmpty();
	}

	public boolean isTurbineBlockedByWater()
	{
		return this.isTurbineFilledWithWater;
	}

	public int getTickRate()
	{
		return 20;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
	}
}
