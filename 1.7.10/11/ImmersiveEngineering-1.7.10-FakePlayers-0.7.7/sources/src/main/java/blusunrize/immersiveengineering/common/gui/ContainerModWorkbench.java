package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerModWorkbench extends Container
{
	public int slotCount;
	public InventoryStorageItem toolInv;
	public TileEntityModWorkbench tile;
    
    

	public ContainerModWorkbench(InventoryPlayer inventoryPlayer, TileEntityModWorkbench tile)
	{
		this.inventoryPlayer = inventoryPlayer;
		this.tile = tile;
    
		if (tile != null)
		{
			this.needClose = this.tile.isOpened;
			tile.isOpened = true;
		}
		else
    
    
	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		if (this.tile != null && !this.needClose)
			this.tile.isOpened = false;
		super.onContainerClosed(player);
    

	private void bindPlayerInv(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 87 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 145));
		}
	}

	public void rebindSlots()
	{
		this.inventorySlots.clear();
		this.addSlotToContainer(new IESlot.UpgradeableItem(this, this.tile, 0, 24, 22, 1));
		this.slotCount = 1;

		ItemStack tool = this.getSlot(0).getStack();
		if (tool != null && tool.getItem() instanceof IUpgradeableTool)
		{
			if (tool.getItem() instanceof ItemEngineersBlueprint)
				((ItemEngineersBlueprint) tool.getItem()).updateOutputs(tool);

			this.toolInv = new InventoryStorageItem(this, tool);
			Slot[] slots = ((IUpgradeableTool) tool.getItem()).getWorkbenchSlots(this, tool, this.toolInv);
			if (slots != null)
				for (Slot s : slots)
				{
					this.addSlotToContainer(s);
					this.slotCount++;
				}

			ItemStack[] cont = ((IUpgradeableTool) tool.getItem()).getContainedItems(tool);
			this.toolInv.stackList = cont;
		}

		this.bindPlayerInv(this.inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
    
		if (this.needClose)
    

		return this.tile.isUseableByPlayer(p_75145_1_);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) this.inventorySlots.get(slot);

		if (slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if (slot < this.slotCount)
			{
				if (!this.mergeItemStack(stackInSlot, this.slotCount, this.slotCount + 36, true))
					return null;
			}
			else if (stackInSlot != null)
				if (stackInSlot.getItem() instanceof IUpgradeableTool && ((IUpgradeableTool) stackInSlot.getItem()).canModify(stackInSlot))
				{
					if (!this.mergeItemStack(stackInSlot, 0, 1, true))
						return null;
				}
				else if (this.slotCount > 1)
				{
					boolean b = true;
					for (int i = 1; i < this.slotCount; i++)
					{
						Slot s = (Slot) this.inventorySlots.get(i);
						if (s != null && s.isItemValid(stackInSlot))
							if (this.mergeItemStack(stackInSlot, i, i + 1, true))
							{
								b = false;
								break;
							}
							else
								continue;
					}
					if (b)
						return null;
				}

			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if (stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(player, stack);
		}
		return stack;
	}

	@Override
	public void onCraftMatrixChanged(IInventory p_75130_1_)
	{
		super.onCraftMatrixChanged(p_75130_1_);
		this.tile.getWorldObj().markBlockForUpdate(this.tile.xCoord, this.tile.yCoord, this.tile.zCoord);
	}
}