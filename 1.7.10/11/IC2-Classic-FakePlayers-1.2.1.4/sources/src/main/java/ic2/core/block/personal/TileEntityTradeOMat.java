package ic2.core.block.personal;

import java.util.Random;
import java.util.UUID;

import ic2.api.Direction;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.ContainerIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.PositionSpec;
import ic2.core.block.inventory.filter.BasicItemFilter;
import ic2.core.block.machine.tileentity.TileEntityMachine;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;

public class TileEntityTradeOMat extends TileEntityMachine implements IPersonalBlock, IHasGui, IPersonalInventory, ISidedInventory, INetworkTileEntityEventListener
{
	private static Direction[] directions = Direction.values();
	public static Random randomizer = new Random();
	private UUID owner;
	public int totalTradeCount = 0;
	public int stock = 0;
	private static int EventTrade = 0;
	private PersonalInventory inv = new PersonalInventory(this, "Trade-O-Mat", 2);

	public TileEntityTradeOMat()
	{
		super(2);
		this.addNetworkFields(new String[] { "owner" });
		this.addGuiFields(new String[] { "totalTradeCount", "stock" });
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("PlayerOwner"))
		{
			NBTTagCompound nbt = nbttagcompound.getCompoundTag("PlayerOwner");
			this.owner = new UUID(nbt.getLong("UUIDMost"), nbt.getLong("UUIDLeast"));
		}
		else
			this.owner = null;

		this.totalTradeCount = nbttagcompound.getInteger("totalTradeCount");
		this.inv.readFromNBT(nbttagcompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		if (this.owner != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setLong("UUIDMost", this.owner.getMostSignificantBits());
			nbt.setLong("UUIDLeast", this.owner.getLeastSignificantBits());
			nbttagcompound.setTag("PlayerOwner", nbt);
		}

		nbttagcompound.setInteger("totalTradeCount", this.totalTradeCount);
		this.inv.writeToNBT(nbttagcompound);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		ItemStack stack0 = this.inv.getStackInSlot(0);
		ItemStack stack1 = this.inv.getStackInSlot(1);
		ItemStack stackA = this.inventory[0];
    
		if (stack0 != null && stack1 != null && stackA != null && StackUtil.isStackEqual(stack0, stackA) && ItemStack.areItemStackTagsEqual(stack0, stackA) && stackA.stackSize >= stack0.stackSize && (stackB == null || StackUtil.isStackEqual(stack1, stackB) && ItemStack.areItemStackTagsEqual(stack1, stackB) && stackB.stackSize + stack1.stackSize <= stackB.getMaxStackSize()))
		{
			boolean tradePerformed = false;

			for (Direction direction : directions)
			{
				TileEntity target = direction.applyToTileEntity(this);
				if (target instanceof IInventory && (!(target instanceof TileEntityPersonalChest) || ((TileEntityPersonalChest) target).owner.equals(this.owner)))
				{
					IInventory targetInventory = (IInventory) target;
					if (target instanceof TileEntityChest)
						targetInventory = Blocks.chest.func_149951_m(target.getWorldObj(), target.xCoord, target.yCoord, target.zCoord);

					if (targetInventory != null && targetInventory.getSizeInventory() >= 18)
					{
						int inputSpace = 0;
						int outputAvailable = 0;

						for (int i = 0; i < targetInventory.getSizeInventory(); ++i)
						{
							ItemStack stack = targetInventory.getStackInSlot(i);
							if (stack == null)
								inputSpace += this.inv.getStackInSlot(0).getMaxStackSize();
							else
							{
    
								if (StackUtil.isStackEqual(stack, stack0_) && ItemStack.areItemStackTagsEqual(stack, stack0_))
									inputSpace += stack.getMaxStackSize() - stack.stackSize;

    
								if (StackUtil.isStackEqual(stack, stack1_) && ItemStack.areItemStackTagsEqual(stack, stack1_))
									outputAvailable += stack.stackSize;
							}
						}

						int outputSpace = this.inventory[1] == null ? this.inv.getStackInSlot(1).getMaxStackSize() : this.inventory[1].getMaxStackSize() - this.inventory[1].stackSize;
						int tradeCount = Math.min(Math.min(Math.min(this.inventory[0].stackSize / this.inv.getStackInSlot(0).stackSize, inputSpace / this.inv.getStackInSlot(0).stackSize), outputSpace / this.inv.getStackInSlot(1).stackSize), outputAvailable / this.inv.getStackInSlot(1).stackSize);
						if (tradeCount > 0)
						{
							int inputCount = this.inv.getStackInSlot(0).stackSize * tradeCount;
							int outputCount = this.inv.getStackInSlot(1).stackSize * tradeCount;
							ItemStack itemStack2 = this.inventory[0];
							itemStack2.stackSize -= inputCount;
							if (this.inventory[0].stackSize == 0)
								this.inventory[0] = null;

							ItemStack gs = StackUtil.getFromInventory(targetInventory, direction, this.owner, new BasicItemFilter(this.inv.getStackInSlot(1).copy()), outputCount, true);
							if (gs != null)
								if (this.inventory[1] == null)
									this.inventory[1] = gs;
								else
								{
									ItemStack itemStack3 = this.inventory[1];
									itemStack3.stackSize += gs.stackSize;
								}

							ItemStack item = this.inv.getStackInSlot(0).copy();
							item.stackSize = inputCount;
							StackUtil.putInInventory(targetInventory, direction, this.owner, item);
							this.totalTradeCount += tradeCount;
							tradePerformed = true;
							this.getNetwork().initiateTileEntityEvent(this, 0, true);
							this.markDirty();
							break;
						}
					}
				}
			}

			if (tradePerformed)
				this.updateStock();
		}

	}

	@Override
	public void onLoaded()
	{
		super.onLoaded();
		if (this.isSimulating())
			this.updateStock();

	}

	public void updateStock()
	{
		this.stock = 0;

		for (Direction direction : directions)
		{
			TileEntity target = direction.applyToTileEntity(this);
			if (target instanceof IInventory && (!(target instanceof TileEntityPersonalChest) || ((TileEntityPersonalChest) target).owner.equals(this.owner)))
			{
				IInventory targetInventory = (IInventory) target;
				if (target instanceof TileEntityChest)
					targetInventory = Blocks.chest.func_149951_m(target.getWorldObj(), target.xCoord, target.yCoord, target.zCoord);

				if (targetInventory.getSizeInventory() >= 18)
					for (int i = 0; i < targetInventory.getSizeInventory(); ++i)
					{
						ItemStack stack = targetInventory.getStackInSlot(i);
						if (StackUtil.isStackEqual(this.inv.getStackInSlot(1), stack))
							this.stock += stack.stackSize;
					}
			}
		}

		this.getNetwork().updateTileGuiField(this, "stock");
		this.getNetwork().updateTileGuiField(this, "totalTradeCount");
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer)
	{
		return this.canAccess(entityPlayer);
	}

	@Override
	public boolean canAccess(EntityPlayer player)
	{
		if (this.owner == null)
		{
			this.owner = player.getGameProfile().getId();
			this.getNetwork().updateTileGuiField(this, "owner");
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
	public String getInventoryName()
	{
		return "Trade-O-Mat";
	}

	@Override
	public ContainerIC2 getGuiContainer(EntityPlayer entityPlayer)
	{
		return this.canAccess(entityPlayer) ? new ContainerTradeOMatOpen(entityPlayer, this) : new ContainerTradeOMatClosed(entityPlayer, this);
	}

	@Override
	public String getGuiClassName(EntityPlayer entityPlayer)
	{
		return this.canAccess(entityPlayer) ? "block.personal.GuiTradeOMatOpen" : "block.personal.GuiTradeOMatClosed";
	}

	@Override
	public void onGuiClosed(EntityPlayer entityPlayer)
	{
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
    
    
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return true;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return true;
	}

	@Override
	public void onNetworkEvent(int event)
	{
		switch (event)
		{
			case 0:
				IC2.audioManager.playOnce(this, PositionSpec.Center, "Machines/o-mat.ogg", true, IC2.audioManager.defaultVolume);
				break;
			default:
				IC2.platform.displayError("An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID " + event + ", tile entity below)\n" + "T: " + this + " (" + this.xCoord + "," + this.yCoord + "," + this.zCoord + ")");
		}

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
