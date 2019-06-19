package tconstruct.smeltery.inventory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.smeltery.gui.SmelteryGui;
import tconstruct.smeltery.logic.SmelteryLogic;

public class SmelteryContainer extends ActiveContainer
{
	public SmelteryLogic logic;
	public InventoryPlayer playerInv;
	public int fuel = 0;
	private int slotRow;
	public int columns;
    
    

	public SmelteryContainer(InventoryPlayer inventoryplayer, SmelteryLogic smeltery)
	{
		this.logic = smeltery;
		this.playerInv = inventoryplayer;
		this.slotRow = 0;
		this.columns = smeltery.getBlocksPerLayer() >= 16 ? 4 : 3;
    
		int totalSlots = smeltery.getBlockCapacity();
		int y = 0;

		for (int i = 0; i < totalSlots; i++)
		{
			int x = i % this.columns;
			this.addDualSlotToContainer(new ActiveSlot(smeltery, x + y * this.columns, 2 + x * 22, 8 + y * 18, y < 8));
			if (x == this.columns - 1)
				y++;
		}

		int baseX = 90 + (this.columns - 3) * 22;

    
		for (int row = 0; row < 3; row++)
		{
			for (int column = 0; column < 9; column++)
			{
				this.addSlotToContainer(new Slot(inventoryplayer, column + row * 9 + 9, baseX + column * 18, 84 + row * 18));
			}
		}

		for (int column = 0; column < 9; column++)
		{
			this.addSlotToContainer(new Slot(inventoryplayer, column, baseX + column * 18, 142));
    
    
	}

	public int updateRows(int invRow)
	{
		if (invRow != this.slotRow)
		{
    
			int basePos = invRow * this.columns;
			for (int iter = 0; iter < this.activeInventorySlots.size(); iter++)
			{
				ActiveSlot slot = this.activeInventorySlots.get(iter);
				if (slot.activeSlotNumber >= basePos && slot.activeSlotNumber < basePos + this.columns * SmelteryGui.maxRows)
					slot.setActive(true);
				else
					slot.setActive(false);

				int xPos = (iter - basePos) % this.columns;
				int yPos = (iter - basePos) / this.columns;
				slot.xDisplayPosition = 2 + 22 * xPos;
				slot.yDisplayPosition = 8 + 18 * yPos;
			}
			return this.slotRow;
		}
		return -1;
	}

	public int scrollTo(float scrollPos)
	{
		int slots = SmelteryGui.maxRows * this.columns;
		float total = (this.logic.getSizeInventory() - slots) / this.columns;
		if ((this.logic.getSizeInventory() - slots) % this.columns != 0)
			total++;
		int rowPos = Math.round(total * scrollPos);
		return this.updateRows(rowPos);
	}

	@Override
    
    
		if (this.smelterySize == this.inventorySlots.size())
			super.detectAndSendChanges();
    
	}

	@Override
	public void updateProgressBar(int id, int value)
	{
		if (id == 0)
			this.logic.fuelGague = value;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
    
		if (this.lastChange != this.logic.lastChange)
    

		Block block = this.logic.getWorldObj().getBlock(this.logic.xCoord, this.logic.yCoord, this.logic.zCoord);
		if (block != TinkerSmeltery.smeltery && block != TinkerSmeltery.smelteryNether)
			return false;
		return this.logic.isUseableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		ItemStack stack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotID);

		if (slot != null && slot.getHasStack())
		{
			ItemStack slotStack = slot.getStack();
			stack = slotStack.copy();

			if (slotID < this.smelterySize)
			{
				if (!this.mergeItemStack(slotStack, this.logic.getSizeInventory(), this.inventorySlots.size(), true))
					return null;
			}
			else if (!this.mergeItemStack(slotStack, 0, this.smelterySize, false))
				return null;

			if (slotStack.stackSize == 0)
				slot.putStack(null);
			else
				slot.onSlotChanged();
		}

		return stack;
	}

	@Override
	protected boolean mergeItemStack(ItemStack inputStack, int startSlot, int endSlot, boolean flag)
    
		boolean merged = false;
		int slotPos = startSlot;

		if (flag)
			slotPos = endSlot - 1;

		Slot slot;
		ItemStack slotStack;

    

		if (inputStack.isStackable() && startSlot >= this.logic.getSizeInventory())
			while (inputStack.stackSize > 0 && (flag ? slotPos >= startSlot : slotPos < endSlot))
			{
				slot = (Slot) this.inventorySlots.get(slotPos);
				slotStack = slot.getStack();

				if (slotStack != null && slotStack.isItemEqual(inputStack) && ItemStack.areItemStackTagsEqual(slotStack, inputStack))
				{
					int l = slotStack.stackSize + inputStack.stackSize;

					if (l <= inputStack.getMaxStackSize())
					{
						inputStack.stackSize = 0;
						slotStack.stackSize = l;
						slot.onSlotChanged();
						merged = true;
					}
					else if (slotStack.stackSize < inputStack.getMaxStackSize())
					{
						inputStack.stackSize -= inputStack.getMaxStackSize() - slotStack.stackSize;
						slotStack.stackSize = inputStack.getMaxStackSize();
						slot.onSlotChanged();
						merged = true;
					}
				}

				if (flag)
					--slotPos;
				else
					++slotPos;
			}

		if (inputStack.stackSize > 0)
		{
			if (flag)
				slotPos = endSlot - 1;
			else
				slotPos = startSlot;

			while (flag ? slotPos >= startSlot : slotPos < endSlot)
			{
				slot = (Slot) this.inventorySlots.get(slotPos);
				slotStack = slot.getStack();

				if (slotStack == null)
				{
					slot.putStack(inputStack.copy());
					slot.onSlotChanged();
					inputStack.stackSize -= 1;
					merged = true;
					break;
				}

				if (flag)
					--slotPos;
				else
					++slotPos;
			}
		}

		return merged;
	}
}
