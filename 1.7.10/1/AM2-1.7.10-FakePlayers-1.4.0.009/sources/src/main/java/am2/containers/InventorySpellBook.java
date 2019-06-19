package am2.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventorySpellBook implements IInventory
{
	public static int inventorySize = 40;
	public static int activeInventorySize = 8;
	private ItemStack[] inventoryItems;

	    
	public ContainerSpellBook container;

	private void save()
	{
		if (this.container != null)
			this.container.save();
	}
	    

	public InventorySpellBook()
	{
		this.inventoryItems = new ItemStack[inventorySize];
	}

	public void SetInventoryContents(ItemStack[] inventoryContents)
	{
		int loops = Math.min(inventorySize, inventoryContents.length);

		for (int i = 0; i < loops; ++i)
			this.inventoryItems[i] = inventoryContents[i];
	}

	@Override
	public int getSizeInventory()
	{
		return inventorySize;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return i >= 0 && i <= this.inventoryItems.length - 1 ? this.inventoryItems[i] : null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.inventoryItems[i] != null)
		{
			if (this.inventoryItems[i].stackSize <= j)
			{
				ItemStack stack = this.inventoryItems[i];
				this.inventoryItems[i] = null;

				    
				this.save();
				    

				return stack;
			}
			else
			{
				ItemStack stack = this.inventoryItems[i].splitStack(j);
				if (this.inventoryItems[i].stackSize == 0)
					this.inventoryItems[i] = null;

				    
				this.save();
				    

				return stack;
			}
		}
		else
			return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.inventoryItems[i] = itemstack;

		    
		this.save();
		    
	}

	@Override
	public String getInventoryName()
	{
		return "Spell Book";
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
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

	public ItemStack[] GetInventoryContents()
	{
		return this.inventoryItems;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		if (this.inventoryItems[i] != null)
		{
			ItemStack itemstack = this.inventoryItems[i];
			this.inventoryItems[i] = null;

			    
			this.save();
			    

			return itemstack;
		}
		else
			return null;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return false;
	}

	@Override
	public void markDirty()
	{
	}
}
