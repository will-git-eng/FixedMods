package ic2.core.block.personal;

import java.util.UUID;

import ic2.api.Direction;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.machine.tileentity.TileEntityMachine;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityFluidOMat extends TileEntityMachine implements IHasGui, IFluidHandler, IPersonalInventory, IPersonalBlock, INetworkClientTileEntityEventListener
{
	private static Direction[] directions = Direction.values();
	private UUID owner;
	public FluidTank fluid = new FluidTank(32000);
	public int transferlimit = 20;
	public int paidFor;
	public int fluidOffer = 1000;
	private PersonalInventory inv = new PersonalInventory(this, "Fluid-O-Mat", 2);

	public TileEntityFluidOMat()
	{
		super(1);
		this.addNetworkFields(new String[] { "owner" });
		this.addNetworkFields(new String[] { "fluid", "paidFor", "fluidOffer" });
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("PlayerOwner"))
		{
			NBTTagCompound nbtTag = nbt.getCompoundTag("PlayerOwner");
			this.owner = new UUID(nbtTag.getLong("UUIDMost"), nbtTag.getLong("UUIDLeast"));
		}
		else
			this.owner = null;

		this.paidFor = nbt.getInteger("Paid");
		this.fluidOffer = nbt.getInteger("Offer");
		if (nbt.hasKey("Tank"))
			this.fluid = this.fluid.readFromNBT(nbt.getCompoundTag("Tank"));

		this.inv.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("Paid", this.paidFor);
		nbt.setInteger("Offer", this.fluidOffer);
		NBTTagCompound data = new NBTTagCompound();
		this.fluid.writeToNBT(data);
		nbt.setTag("Tank", data);
		if (this.owner != null)
		{
			NBTTagCompound NBTTag = new NBTTagCompound();
			NBTTag.setLong("UUIDMost", this.owner.getMostSignificantBits());
			NBTTag.setLong("UUIDLeast", this.owner.getLeastSignificantBits());
			nbt.setTag("PlayerOwner", NBTTag);
		}

		this.inv.writeToNBT(nbt);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return true;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[] { 1 };
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (doFill)
			this.getNetwork().updateTileGuiField(this, "fluid");

		return this.fluid.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return this.drain(from, resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		int amount = Math.min(this.paidFor, maxDrain);
		if (amount <= 0)
			return null;
		else
		{
			if (doDrain)
			{
				this.paidFor -= amount;
				this.getNetwork().updateTileGuiField(this, "paidFor");
				this.getNetwork().updateTileGuiField(this, "fluid");
			}

			return this.fluid.drain(amount, doDrain);
		}
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return this.getFacing() != from.ordinal();
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return this.getFacing() == from.ordinal();
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { this.fluid.getInfo() };
	}

	@Override
	public ContainerIC2 getGuiContainer(EntityPlayer p0)
	{
		return this.canAccess(p0) ? new ContainerFluidOMatOpen(p0, this) : new ContainerFluidOMatClosed(p0, this);
	}

	@Override
	public String getGuiClassName(EntityPlayer p0)
	{
		return this.canAccess(p0) ? "block.personal.GuiFluidOMatOpen" : "block.personal.GuiFluidOMatClosed";
	}

	@Override
	public void onGuiClosed(EntityPlayer p0)
	{
	}

	@Override
	public String getInventoryName()
	{
		return "Fluid-O-Mat";
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (this.isSimulating())
		{
			if (this.paidFor > 0)
			{
				int mB = Math.min(this.paidFor, this.transferlimit);
				if (mB > 0)
				{
					Direction dir = this.getDirection(this.getFacing());
					ForgeDirection fDir = dir.toForgeDirection().getOpposite();
					TileEntity tile = dir.applyToTileEntity(this);
					if (tile != null && tile instanceof IFluidHandler)
					{
						IFluidHandler fluid = (IFluidHandler) tile;
						FluidStack stack = this.drain(fDir, mB, false);
						if (stack != null && fluid.canFill(fDir, stack.getFluid()))
						{
							int filled = fluid.fill(fDir, stack, true);
							if (filled > 0)
								this.drain(fDir, filled, true);
						}
					}
				}
			}

			ItemStack stack1 = this.inventory[0];
    
			if (stack1 != null && stack2 != null && stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2))
			{
				int originalStackSize = this.inventory[0].stackSize;

				for (Direction direction : directions)
				{
					TileEntity target = direction.applyToTileEntity(this);
					if (target instanceof IInventory && !(target instanceof IPersonalBlock) || target instanceof TileEntityPersonalChest && ((TileEntityPersonalChest) target).canAccess(this.owner))
					{
						IInventory targetInventory = (IInventory) target;
						if (target instanceof TileEntityChest)
							targetInventory = Blocks.chest.func_149951_m(target.getWorldObj(), target.xCoord, target.yCoord, target.zCoord);

						ItemStack stack = this.inventory[0].copy();
						int amount = StackUtil.putInInventory(targetInventory, direction, this.owner, stack);
						stack.stackSize -= amount;
						if (stack.stackSize <= 0)
							stack = null;

						this.inventory[0] = stack;
					}
				}

				int numPaymentMoved = originalStackSize - this.inventory[1].stackSize;
				if (numPaymentMoved > 0)
				{
					this.paidFor += this.fluidOffer / this.inventory[0].stackSize * numPaymentMoved;
					IC2.network.get().updateTileGuiField(this, "paidFor");
					if (this.inventory[0].stackSize == 0)
						this.inventory[0] = null;

					this.markDirty();
				}
			}
		}

	}

	private Direction getDirection(int dir)
	{
		for (Direction dirs : directions)
			if (dirs.toSideValue() == dir)
				return dirs;

		return null;
	}

	@Override
	public boolean canAccess(EntityPlayer player)
	{
		if (this.owner == null)
		{
			this.owner = player.getGameProfile().getId();
			this.getNetwork().updateTileEntityField(this, "owner");
			return true;
		}
		else
			return this.canAccess(player.getGameProfile().getId());
	}

	@Override
	public boolean canAccess(UUID player)
	{
		return this.owner == null ? true : this.owner.equals(player);
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		if (this.canAccess(player))
			switch (event)
			{
				case 0:
					this.attemptSet(-1000);
					break;
				case 1:
					this.attemptSet(-100);
					break;
				case 2:
					this.attemptSet(1000);
					break;
				case 3:
					this.attemptSet(100);
					break;
				case 4:
					this.attemptSet(-10);
					break;
				case 5:
					this.attemptSet(10);
			}
	}

	private void attemptSet(int amount)
	{
		if (this.fluidOffer + amount <= 0)
			amount = 0;

		this.fluidOffer += amount;
		this.getNetwork().updateTileGuiField(this, "fluidOffer");
	}

	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return this.getFacing() != side && this.canAccess(entityPlayer);
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer)
	{
		return this.canAccess(entityPlayer);
	}

	@Override
	public IPersonalInventory getInventory(EntityPlayer player)
	{
		return !this.canAccess(player) ? this : this.getInventory(player.getGameProfile().getId());
	}

	@Override
	public IPersonalInventory getInventory(UUID player)
	{
		return !this.canAccess(player) ? this : this.inv;
	}

	UUID getOwner()
	{
		return this.owner;
	}
}
