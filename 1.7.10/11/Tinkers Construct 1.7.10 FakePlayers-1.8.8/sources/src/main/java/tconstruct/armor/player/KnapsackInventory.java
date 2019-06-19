package tconstruct.armor.player;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.lang.ref.WeakReference;

public class KnapsackInventory implements IInventory
{
	public ItemStack[] inventory = new ItemStack[27];
	public WeakReference<EntityPlayer> parent;

	public void init(EntityPlayer player)
	{
		this.parent = new WeakReference<EntityPlayer>(player);
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return this.inventory[slot];
	}

	public boolean isStackInSlot(int slot)
	{
		return this.inventory[slot] != null;
	}

	@Override
	public int getSizeInventory()
	{
		return this.inventory.length;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	public boolean canDropInventorySlot(int slot)
	{
		return true;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		this.inventory[slot] = itemstack;
		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
			itemstack.stackSize = this.getInventoryStackLimit();
	}

	@Override
	public ItemStack decrStackSize(int slot, int quantity)
	{
		if (this.inventory[slot] != null)
		{
			if (this.inventory[slot].stackSize <= quantity)
			{
				ItemStack stack = this.inventory[slot];
				this.inventory[slot] = null;
				return stack;
			}
			ItemStack split = this.inventory[slot].splitStack(quantity);
			if (this.inventory[slot].stackSize == 0)
				this.inventory[slot] = null;
			return split;
		}
		else
			return null;
	}

	@Override
	public String getInventoryName()
	{
		return "tconstruct.knapsack";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
	}

	public void openChest()
	{
	}

	public void closeChest()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

    
	public void saveToNBT(NBTTagCompound tagCompound)
	{
		NBTTagList tagList = new NBTTagList();
		NBTTagCompound invSlot;

		for (int i = 0; i < this.inventory.length; ++i)
		{
			if (this.inventory[i] != null)
			{
				invSlot = new NBTTagCompound();
				invSlot.setByte("Slot", (byte) i);
				this.inventory[i].writeToNBT(invSlot);
				tagList.appendTag(invSlot);
			}
		}

		tagCompound.setTag("Knapsack", tagList);
	}

	public void readFromNBT(NBTTagCompound tagCompound)
    
		if (tagCompound == null || !tagCompound.hasKey("Knapsack"))
			return;

		NBTTagList tagList = tagCompound.getTagList("Knapsack", 10);
		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;
			ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

			if (itemstack != null)
				this.inventory[j] = itemstack;
		}
	}

	public void dropItems()
	{
    
		if (player == null)
    

		for (int i = 0; i < this.inventory.length; ++i)
		{
			if (this.inventory[i] != null)
			{
				player.func_146097_a(this.inventory[i], true, false);
				this.inventory[i] = null;
			}
		}
	}

	public void unequipItems()
	{
    
		if (player == null)
    

		for (int i = 0; i < this.inventory.length; ++i)
		{
			if (this.inventory[i] != null)
			{
				this.dropItemEntity(player, this.inventory[i]);
				this.inventory[i] = null;
			}
		}
	}

	void dropItemEntity(Entity dropper, ItemStack dropStack)
	{
		EntityItem entityitem = new EntityItem(dropper.worldObj, dropper.posX, dropper.posY, dropper.posZ, dropStack);
		dropper.worldObj.spawnEntityInWorld(entityitem);
	}

	@Override
	public void markDirty()
	{
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	public void writeInventoryToStream(ByteBuf os)
	{
		for (int i = 0; i < 27; i++)
		{
			ByteBufUtils.writeItemStack(os, this.inventory[i]);
		}
	}

	public void readInventoryFromStream(ByteBuf is)
	{
		for (int i = 0; i < 27; i++)
		{
			this.inventory[i] = ByteBufUtils.readItemStack(is);
		}
	}

}
