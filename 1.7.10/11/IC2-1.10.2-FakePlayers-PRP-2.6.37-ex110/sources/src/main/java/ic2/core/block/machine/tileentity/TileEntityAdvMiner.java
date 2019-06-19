package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ic2.api.item.ElectricItem;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.block.machine.gui.GuiAdvMiner;
import ic2.core.init.MainConfig;
import ic2.core.init.OreValues;
import ic2.core.item.tool.ItemScanner;
import ic2.core.item.tool.ItemScannerAdv;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.upgrade.IUpgradableBlock;
import ic2.core.upgrade.UpgradableProperty;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityAdvMiner extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener, IUpgradableBlock
{
	private int blockScanCount;
	private int maxBlockScanCount;
	public final int defaultTier = 3;
	public final int workTick = 20;
	public boolean blacklist = true;
	public boolean silkTouch = false;
	public boolean redstonePowered = false;
	public int energyConsume = 512;
	private BlockPos mineTarget;
	private short ticker = 0;
	public final InvSlotConsumableId scannerSlot = new InvSlotConsumableId(this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, new Item[] { ItemName.scanner.getInstance(), ItemName.advanced_scanner.getInstance() });
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	public final InvSlot filterSlot = new InvSlot(this, "list", (InvSlot.Access) null, 15);
	protected final Redstone redstone = this.addComponent(new Redstone(this));

	public TileEntityAdvMiner()
	{
		super(4000000, 3);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.worldObj.isRemote)
			this.setUpgradestat();

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("mineTargetX"))
			this.mineTarget = new BlockPos(nbt.getInteger("mineTargetX"), nbt.getInteger("mineTargetY"), nbt.getInteger("mineTargetZ"));

		this.blacklist = nbt.getBoolean("blacklist");
		this.silkTouch = nbt.getBoolean("silkTouch");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (this.mineTarget != null)
		{
			nbt.setInteger("mineTargetX", this.mineTarget.getX());
			nbt.setInteger("mineTargetY", this.mineTarget.getY());
			nbt.setInteger("mineTargetZ", this.mineTarget.getZ());
		}

		nbt.setBoolean("blacklist", this.blacklist);
		nbt.setBoolean("silkTouch", this.silkTouch);
		return nbt;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.chargeTool();
		this.setUpgradestat();
		if (this.work())
		{
			this.markDirty();
			if (!this.getActive())
				this.setActive(true);
		}
		else if (this.getActive())
			this.setActive(false);

	}

	private boolean work()
	{
		if (this.energy.getEnergy() < this.energyConsume)
			return false;
		else if (this.redstone.hasRedstoneInput())
			return false;
		else if (this.mineTarget != null && this.mineTarget.getY() < 0)
			return false;
		else if (this.scannerSlot.isEmpty())
			return false;
		else if (this.scannerSlot.get().getItem() instanceof ItemScanner && !((ItemScanner) this.scannerSlot.get().getItem()).haveChargeforScan(this.scannerSlot.get()))
			return false;
		else if (++this.ticker != this.workTick)
			return true;
		else
		{
			this.ticker = 0;
			int range;
			if (this.scannerSlot.get().getItem() instanceof ItemScannerAdv)
				range = 32;
			else if (this.scannerSlot.get().getItem() instanceof ItemScanner)
				range = 16;
			else
				range = 0;

			if (this.mineTarget == null)
			{
				this.mineTarget = new BlockPos(this.pos.getX() - range - 1, this.pos.getY() - 1, this.pos.getZ() - range);
				if (this.mineTarget.getY() < 0)
					return false;
			}

			this.blockScanCount = this.maxBlockScanCount;

			for (MutableBlockPos scanPos = new MutableBlockPos(this.mineTarget.getX(), this.mineTarget.getY(), this.mineTarget.getZ()); this.blockScanCount > 0; --this.blockScanCount)
			{
				if (scanPos.getX() < this.pos.getX() + range)
					scanPos = new MutableBlockPos(scanPos.getX() + 1, scanPos.getY(), scanPos.getZ());
				else if (scanPos.getZ() < this.pos.getZ() + range)
					scanPos = new MutableBlockPos(this.pos.getX() - range, scanPos.getY(), scanPos.getZ() + 1);
				else
				{
					scanPos = new MutableBlockPos(this.pos.getX() - range, scanPos.getY() - 1, this.pos.getZ() - range);
					if (scanPos.getY() < 0)
					{
						this.mineTarget = new BlockPos(scanPos);
						return true;
					}
				}

				if (!this.scannerSlot.isEmpty())
					ElectricItem.manager.discharge(this.scannerSlot.get(), 64.0D, Integer.MAX_VALUE, true, true, false);

				IBlockState state = this.worldObj.getBlockState(scanPos);
				Block block = state.getBlock();
				if (!block.isAir(state, this.worldObj, scanPos) && this.canMine(scanPos, block, state))
				{
					this.mineTarget = new BlockPos(scanPos);
					this.doMine(this.mineTarget, block, state);
					break;
				}

				this.mineTarget = new BlockPos(scanPos);
			}

			return true;
		}
	}

	private void chargeTool()
	{
		if (!this.scannerSlot.isEmpty())
			this.energy.useEnergy(ElectricItem.manager.charge(this.scannerSlot.get(), this.energy.getEnergy(), 2, false, false));

	}

	public void doMine(BlockPos pos, Block block, IBlockState state)
    
		if (this.fake.cantBreak(this.worldObj, pos))
    

		StackUtil.distributeDrops(this, StackUtil.getDrops(this.worldObj, pos, state, (EntityPlayer) null, 0, this.silkTouch));
		this.worldObj.setBlockToAir(pos);
		this.energy.useEnergy(this.energyConsume);
	}

	public boolean canMine(BlockPos pos, Block block, IBlockState state)
	{
		if (!(block instanceof IFluidBlock) && !(block instanceof BlockFluidClassic) && !(block instanceof BlockStaticLiquid) && !(block instanceof BlockDynamicLiquid))
		{
			if (state.getBlockHardness(this.worldObj, pos) < 0.0F)
				return false;
			else
			{
				List<ItemStack> drops = StackUtil.getDrops(this.worldObj, pos, state, (EntityPlayer) null, 0, this.silkTouch);
				if (drops.isEmpty())
					return false;
				else if (block.hasTileEntity(state) && OreValues.get(drops) <= 0)
					return false;
				else if (this.blacklist)
				{
					label121: for (ItemStack drop : drops)
					{
						Iterator var11 = this.filterSlot.iterator();

						while (true)
						{
							if (!var11.hasNext())
								continue label121;

							ItemStack filter = (ItemStack) var11.next();
							if (StackUtil.checkItemEquality(drop, filter))
								break;
						}

						return false;
					}

					return true;
				}
				else
				{
					label203: for (ItemStack drop : drops)
					{
						Iterator var7 = this.filterSlot.iterator();

						while (true)
						{
							if (!var7.hasNext())
								continue label203;

							ItemStack filter = (ItemStack) var7.next();
							if (StackUtil.checkItemEquality(drop, filter))
								break;
						}

						return true;
					}

					return false;
				}
			}
		}
		else
			return false;
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		switch (event)
		{
			case 0:
				if (!this.getActive())
					this.mineTarget = null;
				break;
			case 1:
				if (!this.getActive())
					this.blacklist = !this.blacklist;
				break;
			case 2:
				if (!this.getActive())
					this.silkTouch = !this.silkTouch;
		}

	}

	public void setUpgradestat()
	{
		this.upgradeSlot.onChanged();
		this.energy.setSinkTier(applyModifier(this.defaultTier, this.upgradeSlot.extraTier, 1.0D));
		this.maxBlockScanCount = 5 * (this.upgradeSlot.augmentation + 1);
	}

	private static int applyModifier(int base, int extra, double multiplier)
	{
		double ret = Math.round(((double) base + (double) extra) * multiplier);
		return ret > 2.147483647E9D ? Integer.MAX_VALUE : (int) ret;
	}

	@Override
	public ContainerBase<TileEntityAdvMiner> getGuiContainer(EntityPlayer player)
	{
		return new ContainerAdvMiner(player, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiAdvMiner(new ContainerAdvMiner(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
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

	public BlockPos getMineTarget()
	{
		return this.mineTarget;
	}

	@Override
	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.worldObj.isRemote)
		{
			NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
			this.energy.addEnergy(nbt.getDouble("energy"));
		}

	}

	@Override
	protected ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		drop = super.adjustDrop(drop, wrench);
		if (wrench || this.teBlock.defaultDrop == TeBlock.DefaultDrop.Self)
		{
			double retainedRatio = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
			if (retainedRatio > 0.0D)
			{
				NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
				nbt.setDouble("energy", this.energy.getEnergy() * retainedRatio);
			}
		}

		return drop;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Augmentable, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer);
	}
}
