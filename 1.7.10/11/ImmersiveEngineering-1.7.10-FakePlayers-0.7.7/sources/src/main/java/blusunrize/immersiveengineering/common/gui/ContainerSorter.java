package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSorter extends Container
{
	TileEntityConveyorSorter tile;
    
	public TileEntityConveyorSorter getTile()
	{
		return this.tile;
    

	public ContainerSorter(InventoryPlayer inventoryPlayer, TileEntityConveyorSorter tile)
	{
		this.tile = tile;
		for (int side = 0; side < 6; side++)
		{
			for (int i = 0; i < TileEntityConveyorSorter.filterSlotsPerSide; i++)
			{
				int x = 4 + (side / 2) * 58 + (i < 3 ? i * 18 : i > 4 ? (i - 5) * 18 : i == 3 ? 0 : 36);
				int y = 22 + side % 2 * 76 + (i < 3 ? 0 : i > 4 ? 36 : 18);
				int id = side * TileEntityConveyorSorter.filterSlotsPerSide + i;
				this.addSlotToContainer(new IESlot.Ghost(this, tile.filter, id, x, y));
			}
		}
		this.slotCount = 6 * TileEntityConveyorSorter.filterSlotsPerSide;

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 163 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 221));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
	{
		return this.tile.isUseableByPlayer(p_75145_1_);
	}

	@Override
	public ItemStack slotClick(int id, int button, int modifier, EntityPlayer player)
	{
		Slot slot = id < 0 ? null : (Slot) this.inventorySlots.get(id);
		if (!(slot instanceof IESlot.Ghost))
			return super.slotClick(id, button, modifier, player);

		ItemStack stack = null;
		ItemStack stackSlot = slot.getStack();
		if (stackSlot != null)
			stack = stackSlot.copy();

		if (button == 2)
			slot.putStack(null);
		else if (button == 0 || button == 1)
		{
			InventoryPlayer playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if (stackSlot == null)
			{
				if (stackHeld != null && slot.isItemValid(stackHeld))
					slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
			}
			else if (stackHeld == null)
				slot.putStack(null);
			else if (slot.isItemValid(stackHeld))
				slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
		}
		else if (button == 5)
		{
			InventoryPlayer playerInv = player.inventory;
			ItemStack stackHeld = playerInv.getItemStack();
			if (!slot.getHasStack())
				slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
		}
		return stack;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	}
}