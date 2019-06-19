package com.rwtema.extrautils.tileentity;

import com.rwtema.extrautils.helper.XUHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class TileEntityFilingCabinet extends TileEntity implements IInventory
{
	public List<ItemStack> itemSlots = new ArrayList();
	public List<ItemStack> inputSlots = new ArrayList();
	private boolean needsUpdate = false;

	public static boolean areCloseEnoughForBasic(ItemStack a, ItemStack b)
	{
		if (a != null && b != null)
		{
			int[] da = OreDictionary.getOreIDs(a);
			int[] db = OreDictionary.getOreIDs(b);
			return da.length <= 0 && db.length <= 0 ? a.getItem() == b.getItem() : arrayContain(da, db);
		}
		else
			return false;
	}

	public static boolean arrayContain(int[] a, int[] b)
	{
		if (a.length != 0 && b.length != 0)
		{
			for (int element : a)
			{
				for (int j = 0; j < b.length; ++j)
				{
					if (element == a[j])
						return true;
				}
			}

			return false;
		}
		else
			return false;
	}

	public int getMaxSlots()
	{
		return this.getBlockMetadata() < 6 ? 1728 : 1728;
	}

	@Override
	public void updateEntity()
	{
		this.handleInput();
	}

	public void handleInput()
	{
		if (this.needsUpdate)
		{
			for (int i = 0; i < this.itemSlots.size(); ++i)
			{
				if (this.itemSlots.get(i) == null)
				{
					this.itemSlots.remove(i);
					--i;
				}
			}

			for (; this.inputSlots.size() > 0; this.inputSlots.remove(0))
			{
				boolean added = false;

    
				{
					if (XUHelper.canItemsStack(itemSlot, this.inputSlots.get(0), false, false))
					{
						itemSlot.stackSize += this.inputSlots.get(0).stackSize;
						added = true;
						break;
					}
				}

				if (!added)
					this.itemSlots.add(this.inputSlots.get(0));
			}
		}

	}

	@Override
	public void markDirty()
	{
		this.needsUpdate = true;
		super.markDirty();
	}

	@Override
	public int getSizeInventory()
	{
		return this.itemSlots.size() + this.inputSlots.size() + 1;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return i < this.itemSlots.size() ? this.itemSlots.get(i) : i - this.itemSlots.size() < this.inputSlots.size() ? this.inputSlots.get(i - this.itemSlots.size()) : null;
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2)
	{
		if (par1 < this.itemSlots.size() && this.itemSlots.get(par1) != null)
		{
			if (par2 > this.itemSlots.get(par1).getMaxStackSize())
				par2 = this.itemSlots.get(par1).getMaxStackSize();

			if (this.itemSlots.get(par1).stackSize <= par2)
			{
				ItemStack itemstack = this.itemSlots.get(par1);
				this.itemSlots.set(par1, null);
				this.markDirty();
				return itemstack;
			}
			else
			{
				ItemStack itemstack = this.itemSlots.get(par1).splitStack(par2);
				if (this.itemSlots.get(par1).stackSize == 0)
					this.itemSlots.set(par1, null);

				this.markDirty();
				return itemstack;
			}
		}
		else
			return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return i < this.itemSlots.size() ? this.itemSlots.get(i) : null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if (i < this.itemSlots.size())
			this.itemSlots.set(i, itemstack);
		else if (i - this.itemSlots.size() < this.inputSlots.size())
			this.inputSlots.set(i - this.itemSlots.size(), itemstack);
		else if (i == this.itemSlots.size() + this.inputSlots.size() && itemstack != null)
			this.inputSlots.add(itemstack);

		this.needsUpdate = true;
	}

	@Override
	public String getInventoryName()
	{
		return "extrautils:filing.cabinet";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		if (this.getBlockMetadata() >= 6)
			return 1;
		else
		{
			int n = 0;

			for (int j = 0; j < this.itemSlots.size() && n <= this.getMaxSlots(); ++j)
			{
				if (this.itemSlots.get(j) != null)
					n += this.itemSlots.get(j).stackSize;
			}

			for (ItemStack inputSlot : this.inputSlots)
			{
				if (inputSlot != null)
					n += inputSlot.stackSize;
			}

			return Math.max(1, this.getMaxSlots() - n);
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
    
    
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if (itemstack == null)
			return false;
		else if (i != this.itemSlots.size() + this.inputSlots.size())
			return false;
		else
		{
			boolean basic = this.getBlockMetadata() < 6;
			if (!basic && itemstack.getMaxStackSize() != 1)
				return false;
			else
			{
				int n = 0;

				for (int j = 0; j < this.itemSlots.size() && n < this.getMaxSlots(); ++j)
				{
					if (this.itemSlots.get(j) != null)
					{
						if (basic && !areCloseEnoughForBasic(this.itemSlots.get(j), itemstack))
							return false;

						n += this.itemSlots.get(j).stackSize;
					}
				}

				for (int j = 0; j < this.inputSlots.size() && n < this.getMaxSlots(); ++j)
				{
					if (this.inputSlots.get(j) != null)
						n += this.inputSlots.get(j).stackSize;
				}

				return n < this.getMaxSlots();
			}
		}
	}

	public void readInvFromTags(NBTTagCompound tags)
	{
		int n = 0;
		if (tags.hasKey("item_no"))
			n = tags.getInteger("item_no");

		this.itemSlots.clear();
		this.inputSlots.clear();

		for (int i = 0; i < n; ++i)
		{
			ItemStack item = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("item_" + i));
			if (item != null)
			{
				item.stackSize = tags.getCompoundTag("item_" + i).getInteger("Size");
				if (item.stackSize > 0)
					this.itemSlots.add(item);
			}
		}

	}

	public void writeInvToTags(NBTTagCompound tags)
	{
		this.handleInput();
		if (this.itemSlots.size() > 0)
		{
			tags.setInteger("item_no", this.itemSlots.size());

			for (int i = 0; i < this.itemSlots.size(); ++i)
			{
				NBTTagCompound t = new NBTTagCompound();
				this.itemSlots.get(i).writeToNBT(t);
				t.setInteger("Size", this.itemSlots.get(i).stackSize);
				tags.setTag("item_" + i, t);
			}
		}

	}

	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		super.readFromNBT(tags);
		this.readInvFromTags(tags);
	}

	@Override
	public void writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);
		this.writeInvToTags(tags);
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}
}
