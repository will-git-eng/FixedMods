package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAssembler extends Container
{
	TileEntityAssembler tile;
    
	public TileEntityAssembler getTile()
	{
		return this.tile;
    

	public ContainerAssembler(InventoryPlayer inventoryPlayer, TileEntityAssembler tile)
	{
		this.tile = tile;
		for (int i = 0; i < tile.patterns.length; i++)
		{
			this.tile.patterns[i].recalculateOutput();
			for (int j = 0; j < 9; j++)
			{
				int x = 9 + i * 58 + j % 3 * 18;
				int y = 7 + (j / 3) * 18;
				this.addSlotToContainer(new IESlot.Ghost(this, tile.patterns[i], j, x, y));
			}
			this.addSlotToContainer(new IESlot.Output(this, tile, 18 + i, 27 + i * 58, 64));
		}
		for (int i = 0; i < 18; i++)
		{
			this.addSlotToContainer(new Slot(tile, i, 13 + i % 9 * 18, 87 + (i / 9) * 18));
		}
		this.slotCount = 21;

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 13 + j * 18, 137 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 13 + i * 18, 195));
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
		ItemStack stack = null;
		Slot slotObject = (Slot) this.inventorySlots.get(slot);

		if (slotObject != null && slotObject.getHasStack() && !(slotObject instanceof IESlot.Ghost))
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();
			if (slot < 48)
			{
				if (!this.mergeItemStack(stackInSlot, 48, 48 + 36, true))
					return null;
			}
			else if (!this.mergeItemStack(stackInSlot, 30, 48, false))
				return null;

			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if (stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
}