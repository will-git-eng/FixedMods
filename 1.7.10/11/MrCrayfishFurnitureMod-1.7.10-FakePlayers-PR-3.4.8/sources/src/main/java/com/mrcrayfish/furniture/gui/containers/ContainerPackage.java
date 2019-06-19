package com.mrcrayfish.furniture.gui.containers;

import com.mrcrayfish.furniture.gui.slots.SlotPackage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPackage extends Container
{
    
	private final IInventory inventory1;
    

	public ContainerPackage(IInventory par1IInventory, IInventory par2IInventory)
	{
		par2IInventory.openInventory();
		int var3 = (this.numRows - 4) * 18;
		this.addSlotToContainer(new SlotPackage(par2IInventory, 0, 62, 15));
		this.addSlotToContainer(new SlotPackage(par2IInventory, 1, 62, 33));
		this.addSlotToContainer(new SlotPackage(par2IInventory, 2, 80, 15));
		this.addSlotToContainer(new SlotPackage(par2IInventory, 3, 80, 33));
		this.addSlotToContainer(new SlotPackage(par2IInventory, 4, 98, 15));
		this.addSlotToContainer(new SlotPackage(par2IInventory, 5, 98, 33));

		for (int var4 = 0; var4 < 3; ++var4)
			for (int var5 = 0; var5 < 9; ++var5)
				this.addSlotToContainer(new Slot(par1IInventory, var5 + var4 * 9 + 9, 8 + var5 * 18, 103 + var4 * 18 + var3 + 53));

		for (int var4 = 0; var4 < 9; ++var4)
    
		this.inventory1 = par1IInventory;
    
	}

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    
    
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
		ItemStack var3 = null;
		Slot var4 = (Slot) super.inventorySlots.get(par2);
		if (var4 != null && var4.getHasStack() && var4 instanceof SlotPackage)
		{
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();
			if (par2 < 6)
			{
				if (!this.mergeItemStack(var5, 6, super.inventorySlots.size(), true))
					return null;
			}
			else if (!this.mergeItemStack(var5, 0, 6, false))
				return null;

			if (var5.stackSize == 0)
				var4.putStack((ItemStack) null);
			else
				var4.onSlotChanged();
		}

		return var3;
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
	}
}
