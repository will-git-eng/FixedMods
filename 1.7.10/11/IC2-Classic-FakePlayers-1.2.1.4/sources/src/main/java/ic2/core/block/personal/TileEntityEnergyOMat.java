package ic2.core.block.personal;

import java.util.UUID;

import ic2.api.Direction;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySourceInfo;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerIC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Items;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityEnergyOMat extends TileEntityMachine implements IPersonalBlock, IHasGui, IPersonalInventory, ISidedInventory, IEnergySink, IEnergySourceInfo, INetworkClientTileEntityEventListener
{
	private static Direction[] directions = Direction.values();
	public int euOffer = 1000;
	private UUID owner;
	private boolean addedToEnergyNet = false;
	public int paidFor = 0;
	public int euBuffer = 0;
	private int euBufferMax = 10000;
	private int maxOutputRate = 32;
	private PersonalInventory inv = new PersonalInventory(this, "Energy-O-Mat", 2);

	public TileEntityEnergyOMat()
	{
		super(1);
		this.addNetworkFields(new String[] { "owner" });
		this.addGuiFields(new String[] { "payedFor", "euBuffer" });
	}

	@Override
	public String getInventoryName()
	{
		return "Energy-O-Mat";
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.euOffer = nbttagcompound.getInteger("euOffer");
		this.paidFor = nbttagcompound.getInteger("paidFor");
		this.euBuffer = nbttagcompound.getInteger("euBuffer");
		if (nbttagcompound.hasKey("PlayerOwner"))
		{
			NBTTagCompound nbt = nbttagcompound.getCompoundTag("PlayerOwner");
			this.owner = new UUID(nbt.getLong("UUIDMost"), nbt.getLong("UUIDLeast"));
		}
		else
			this.owner = null;

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("euOffer", this.euOffer);
		nbttagcompound.setInteger("paidFor", this.paidFor);
		nbttagcompound.setInteger("euBuffer", this.euBuffer);
		if (this.owner != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setLong("UUIDMost", this.owner.getMostSignificantBits());
			nbt.setLong("UUIDLeast", this.owner.getLeastSignificantBits());
			nbttagcompound.setTag("PlayerOwner", nbt);
		}

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
	public void onLoaded()
	{
		super.onLoaded();
		if (this.isSimulating())
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			this.addedToEnergyNet = true;
		}

	}

	@Override
	public void onUnloaded()
	{
		if (this.isSimulating() && this.addedToEnergyNet)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			this.addedToEnergyNet = false;
		}

		super.onUnloaded();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (this.isSimulating())
		{
			this.euBufferMax = 10000;
			this.maxOutputRate = 32;
			if (this.inv.getStackInSlot(1) != null)
				if (this.inv.getStackInSlot(1).isItemEqual(Ic2Items.energyStorageUpgrade))
					this.euBufferMax = 10000 * (this.inv.getStackInSlot(1).stackSize + 1);
				else if (this.inv.getStackInSlot(1).isItemEqual(Ic2Items.transformerUpgrade))
					this.maxOutputRate = 32 * (int) Math.pow(4.0D, Math.min(4, this.inv.getStackInSlot(1).stackSize));

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
					this.paidFor += this.euOffer / this.inventory[0].stackSize * numPaymentMoved;
					this.getNetwork().updateTileGuiField(this, "paidFor");
					if (this.inventory[0].stackSize == 0)
						this.inventory[0] = null;

					this.markDirty();
				}
			}

			if (this.euBuffer > this.euBufferMax)
			{
				this.euBuffer = this.euBufferMax;
				this.getNetwork().updateTileGuiField(this, "euBuffer");
			}
		}

	}

	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return this.getFacing() != side && this.canAccess(entityPlayer);
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return this.facingMatchesDirection(direction);
	}

	public boolean facingMatchesDirection(ForgeDirection direction)
	{
		return direction.ordinal() == this.getFacing();
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return !this.facingMatchesDirection(direction);
	}

	@Override
	public double getOfferedEnergy()
	{
		return Math.min(this.maxOutputRate, this.euBuffer);
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.euBuffer = (int) (this.euBuffer - amount);
		this.getNetwork().updateTileGuiField(this, "euBuffer");
	}

	@Override
	public double getDemandedEnergy()
	{
		return Math.min(this.paidFor, this.euBufferMax - this.euBuffer);
	}

	@Override
	public double injectEnergy(ForgeDirection directionFrom, double amount, double volt)
	{
		int toAdd = (int) Math.min(Math.min(amount, this.paidFor), this.euBufferMax - this.euBuffer);
		this.paidFor -= toAdd;
		this.euBuffer += toAdd;
		this.getNetwork().updateTileGuiField(this, "paidFor");
		this.getNetwork().updateTileGuiField(this, "euBuffer");
		return amount - toAdd;
	}

	@Override
	public int getSinkTier()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public ContainerIC2 getGuiContainer(EntityPlayer entityPlayer)
	{
		return this.canAccess(entityPlayer) ? new ContainerEnergyOMatOpen(entityPlayer, this) : new ContainerEnergyOMatClosed(entityPlayer, this);
	}

	@Override
	public String getGuiClassName(EntityPlayer entityPlayer)
	{
		return this.canAccess(entityPlayer) ? "block.personal.GuiEnergyOMatOpen" : "block.personal.GuiEnergyOMatClosed";
	}

	@Override
	public void onGuiClosed(EntityPlayer entityPlayer)
	{
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
			}
	}

	private void attemptSet(int amount)
	{
		if (this.euOffer + amount <= 0)
			amount = 0;

		this.euOffer += amount;
		this.getNetwork().updateTileGuiField(this, "euOffer");
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[] { 0 };
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
	public int getSourceTier()
	{
		return EnergyNet.instance.getTierFromPower(this.maxOutputRate);
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

	@Override
	public int getMaxEnergyAmount()
	{
		return 8192;
	}
}
