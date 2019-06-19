package ic2.core.item.tool;

import java.util.Random;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.core.ContainerIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.ITickCallback;
import ic2.core.Ic2Items;
import ic2.core.item.ItemCropSeed;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class HandHeldCropnalyzer implements IHasGui, ITickCallback, IInventory
{
	private ItemStack itemStack;
	private ItemStack[] inventory = new ItemStack[3];

	public HandHeldCropnalyzer(EntityPlayer entityPlayer, ItemStack itemStack)
	{
		this.itemStack = itemStack;
		if (IC2.platform.isSimulating())
		{
			NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(itemStack);
			nbtTagCompound.setInteger("uid", new Random().nextInt());
			NBTTagList nbtTagList = nbtTagCompound.getTagList("Items", 10);

			for (int i = 0; i < nbtTagList.tagCount(); ++i)
			{
				NBTTagCompound nbtTagCompoundSlot = nbtTagList.getCompoundTagAt(i);
				int slot = nbtTagCompoundSlot.getByte("Slot");
				if (slot >= 0 && slot < this.inventory.length)
					this.inventory[slot] = ItemStack.loadItemStackFromNBT(nbtTagCompoundSlot);
			}

			IC2.addContinuousTickCallback(entityPlayer.worldObj, this);
		}
	}

	@Override
	public int getSizeInventory()
	{
		return this.inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.inventory[i];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if (this.inventory[slot] == null)
			return null;
		else if (this.inventory[slot].stackSize <= amount)
		{
			ItemStack itemstack = this.inventory[slot];
			this.inventory[slot] = null;
			return itemstack;
		}
		else
		{
			ItemStack ret = this.inventory[slot].splitStack(amount);
			if (this.inventory[slot].stackSize == 0)
				this.inventory[slot] = null;

			return ret;
		}
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack)
	{
		this.inventory[slot] = itemStack;
		if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit())
			itemStack.stackSize = this.getInventoryStackLimit();

	}

	@Override
	public String getInventoryName()
	{
		return "Cropnalyzer";
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer)
    
		InventoryPlayer inventory = entityPlayer.inventory;
		for (ItemStack stack : inventory.armorInventory)
			if (stack != null && ItemStack.areItemStacksEqual(stack, this.itemStack))
				return true;
		for (ItemStack stack : inventory.mainInventory)
			if (stack != null && ItemStack.areItemStacksEqual(stack, this.itemStack))
				return true;

    
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return null;
	}

	@Override
	public ContainerIC2 getGuiContainer(EntityPlayer entityPlayer)
	{
		return new ContainerCropnalyzer(entityPlayer, this);
	}

	@Override
	public String getGuiClassName(EntityPlayer entityPlayer)
	{
		return "item.tool.GuiCropnalyzer";
	}

	@Override
	public void onGuiClosed(EntityPlayer entityPlayer)
	{
		if (IC2.platform.isSimulating())
		{
			IC2.removeContinuousTickCallback(entityPlayer.worldObj, this);
			NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(this.itemStack);
			boolean dropItself = false;

			for (int i = 0; i < this.getSizeInventory(); ++i)
				if (this.inventory[i] != null)
				{
					NBTTagCompound nbtTagCompoundSlot = StackUtil.getOrCreateNbtData(this.inventory[i]);
					if (nbtTagCompound.getInteger("uid") == nbtTagCompoundSlot.getInteger("uid"))
					{
						this.itemStack.stackSize = 1;
						this.inventory[i] = null;
						dropItself = true;
						break;
					}
				}

			NBTTagList nbtTagList = new NBTTagList();

			for (int j = 0; j < this.inventory.length; ++j)
				if (this.inventory[j] != null)
				{
					NBTTagCompound nbtTagCompoundSlot = new NBTTagCompound();
					nbtTagCompoundSlot.setByte("Slot", (byte) j);
					this.inventory[j].writeToNBT(nbtTagCompoundSlot);
					nbtTagList.appendTag(nbtTagCompoundSlot);
				}

			nbtTagCompound.setTag("Items", nbtTagList);
			if (dropItself)
				StackUtil.dropAsEntity(entityPlayer.worldObj, (int) entityPlayer.posX, (int) entityPlayer.posY, (int) entityPlayer.posZ, this.itemStack);
			else
				for (int j = -1; j < entityPlayer.inventory.getSizeInventory(); ++j)
				{
					ItemStack itemStackSlot;
					if (j == -1)
						itemStackSlot = entityPlayer.inventory.getItemStack();
					else
						itemStackSlot = entityPlayer.inventory.getStackInSlot(j);

					if (itemStackSlot != null)
					{
						NBTTagCompound nbtTagCompoundSlot2 = itemStackSlot.getTagCompound();
						if (nbtTagCompoundSlot2 != null && nbtTagCompound.getInteger("uid") == nbtTagCompoundSlot2.getInteger("uid"))
						{
							this.itemStack.stackSize = 1;
							if (j == -1)
								entityPlayer.inventory.setItemStack(this.itemStack);
							else
								entityPlayer.inventory.setInventorySlotContents(j, this.itemStack);
							break;
						}
					}
				}
		}

	}

	@Override
	public void tickCallback(World world)
	{
		if (this.inventory[1] == null && this.inventory[0] != null && this.inventory[0].getItem() == Ic2Items.cropSeed.getItem())
		{
			int level = ItemCropSeed.getScannedFromStack(this.inventory[0]);
			if (level == 4)
			{
				this.inventory[1] = this.inventory[0];
				this.inventory[0] = null;
				return;
			}

			if (this.inventory[2] == null || !(this.inventory[2].getItem() instanceof IElectricItem))
				return;

			int ned = this.energyForLevel(level);
			int got = (int) ElectricItem.manager.discharge(this.inventory[2], ned, 2, true, true, false);
			if (got < ned)
				return;

			ItemCropSeed.incrementScannedOfStack(this.inventory[0]);
			this.inventory[1] = this.inventory[0];
			this.inventory[0] = null;
		}

	}

	public int energyForLevel(int i)
	{
		switch (i)
		{
			case 1:
				return 90;
			case 2:
				return 900;
			case 3:
				return 9000;
			default:
				return 10;
		}
	}

	public CropCard crop()
	{
		return Crops.instance.getCropList()[ItemCropSeed.getIdFromStack(this.inventory[1])];
	}

	public int getScannedLevel()
	{
		return this.inventory[1] != null && this.inventory[1].getItem() == Ic2Items.cropSeed.getItem() ? ItemCropSeed.getScannedFromStack(this.inventory[1]) : -1;
	}

	public String getSeedName()
	{
		return this.crop().displayName();
	}

	public String getSeedTier()
	{
		switch (this.crop().tier())
		{
			case 1:
				return "I";
			case 2:
				return "II";
			case 3:
				return "III";
			case 4:
				return "IV";
			case 5:
				return "V";
			case 6:
				return "VI";
			case 7:
				return "VII";
			case 8:
				return "VIII";
			case 9:
				return "IX";
			case 10:
				return "X";
			case 11:
				return "XI";
			case 12:
				return "XII";
			case 13:
				return "XIII";
			case 14:
				return "XIV";
			case 15:
				return "XV";
			case 16:
				return "XVI";
			default:
				return "0";
		}
	}

	public String getSeedDiscovered()
	{
		return this.crop().discoveredBy();
	}

	public String getSeedDesc(int i)
	{
		return this.crop().desc(i);
	}

	public int getSeedGrowth()
	{
		return ItemCropSeed.getGrowthFromStack(this.inventory[1]);
	}

	public int getSeedGain()
	{
		return ItemCropSeed.getGainFromStack(this.inventory[1]);
	}

	public int getSeedResistence()
	{
		return ItemCropSeed.getResistanceFromStack(this.inventory[1]);
	}

	public boolean matchesUid(int uid)
	{
		NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(this.itemStack);
		return nbtTagCompound.getInteger("uid") == uid;
	}

	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}
}
