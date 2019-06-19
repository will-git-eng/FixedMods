package am2.containers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import am2.blocks.tileentities.TileEntityMagiciansWorkbench;
import am2.containers.slots.AM2Container;
import am2.containers.slots.SlotGhostRune;
import am2.containers.slots.SlotMagiciansWorkbenchCrafting;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;

public class ContainerMagiciansWorkbench extends AM2Container
{
	private final TileEntityMagiciansWorkbench workbenchInventory;
	public InventoryCrafting firstCraftMatrix;
	public InventoryCrafting secondCraftMatrix;
	private boolean initializing;
	private final World world;
	private int INVENTORY_STORAGE_START = 20;
	private int PLAYER_INVENTORY_START = 47;
	private int PLAYER_ACTION_BAR_START = 74;
	private int PLAYER_ACTION_BAR_END = 83;

	    
	private static boolean sendingChanges;
	private final EntityPlayer player;
	    

	public ContainerMagiciansWorkbench(InventoryPlayer pInv, TileEntityMagiciansWorkbench tile)
	{
		this.workbenchInventory = tile;
		this.workbenchInventory.openInventory();
		this.world = pInv.player.worldObj;
		this.INVENTORY_STORAGE_START = tile.getStorageStart() - 3;
		if (tile.getUpgradeStatus((byte) 1))
			this.INVENTORY_STORAGE_START += 5;

		this.PLAYER_INVENTORY_START = this.INVENTORY_STORAGE_START + tile.getStorageSize();
		this.PLAYER_ACTION_BAR_START = this.PLAYER_INVENTORY_START + 27;
		this.PLAYER_ACTION_BAR_END = this.PLAYER_ACTION_BAR_START + 9;
		this.firstCraftMatrix = new InventoryCrafting(this, 3, 3);
		this.secondCraftMatrix = tile.getUpgradeStatus((byte) 1) ? new InventoryCrafting(this, 3, 3) : new InventoryCrafting(this, 2, 2);
		this.updateCraftingMatrices();
		int index = 0;

		for (int i = 0; i < 3; ++i)
			for (int k = 0; k < 3; ++k)
				this.addSlotToContainer(new Slot(this.firstCraftMatrix, index++, 19 + k * 18, 29 + i * 18));

		this.addSlotToContainer(new SlotMagiciansWorkbenchCrafting(pInv.player, this.firstCraftMatrix, tile.firstCraftResult, this, 0, 37, 89));
		index = 0;
		if (tile.getUpgradeStatus((byte) 1))
			for (int i = 0; i < 3; ++i)
				for (int k = 0; k < 3; ++k)
					this.addSlotToContainer(new Slot(this.secondCraftMatrix, index++, 93 + k * 18, 29 + i * 18));
		else
			for (int i = 0; i < 2; ++i)
				for (int k = 0; k < 2; ++k)
					this.addSlotToContainer(new Slot(this.secondCraftMatrix, index++, 102 + k * 18, 38 + i * 18));

		this.addSlotToContainer(new SlotMagiciansWorkbenchCrafting(pInv.player, this.secondCraftMatrix, tile.secondCraftResult, this, 0, 111, 89));
		index = 18;

		for (int i = 0; i < 9; ++i)
			for (int k = 0; k < 3; ++k)
				this.addSlotToContainer(new Slot(tile, index++, 167 + k * 18, 1 + i * 18));

		for (int i = 0; i < 3; ++i)
			for (int k = 0; k < 9; ++k)
				this.addSlotToContainer(new Slot(pInv, k + i * 9 + 9, 20 + k * 18, 168 + i * 18));

		for (int j1 = 0; j1 < 9; ++j1)
			this.addSlotToContainer(new Slot(pInv, j1, 20 + j1 * 18, 226));

		this.addSlotToContainer(new SlotGhostRune(tile, 45, 194, 177));
		this.addSlotToContainer(new SlotGhostRune(tile, 46, 194, 195));
		this.addSlotToContainer(new SlotGhostRune(tile, 47, 194, 213));

		    
		tile.containers.put(this.player = pInv.player, this);
		    
	}

	public void updateCraftingMatrices()
	{
		this.initializing = true;

		for (int i = 0; i < 9; ++i)
			this.firstCraftMatrix.setInventorySlotContents(i, this.workbenchInventory.getStackInSlot(i));

		if (this.workbenchInventory.getUpgradeStatus((byte) 1))
			for (int i = 9; i < 18; ++i)
				this.secondCraftMatrix.setInventorySlotContents(i - 9, this.workbenchInventory.getStackInSlot(i));
		else
			for (int i = 9; i < 13; ++i)
				this.secondCraftMatrix.setInventorySlotContents(i - 9, this.workbenchInventory.getStackInSlot(i));

		this.initializing = false;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv)
	{
		this.workbenchInventory.firstCraftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.firstCraftMatrix, this.world));
		if (!this.initializing)
			for (int i = 0; i < 9; ++i)
				this.workbenchInventory.setInventorySlotContents(i, this.firstCraftMatrix.getStackInSlot(i));

		this.workbenchInventory.secondCraftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.secondCraftMatrix, this.world));
		if (!this.initializing)
			for (int i = 0; i < 9; ++i)
				this.workbenchInventory.setInventorySlotContents(i + 9, this.secondCraftMatrix.getStackInSlot(i));

		    
		if (!sendingChanges)
		{
			sendingChanges = true;
			try
			{
				for (Map.Entry<EntityPlayer, ContainerMagiciansWorkbench> entry : this.workbenchInventory.containers.entrySet())
					if (entry.getKey() != this.player)
					{
						entry.getValue().updateCraftingMatrices();
						entry.getValue().detectAndSendChanges();
					}
			}
			finally
			{
				sendingChanges = false;
			}
		}
		    
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return this.workbenchInventory.isUseableByPlayer(player);
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		    
		this.workbenchInventory.containers.remove(this.player);
		    

		this.workbenchInventory.closeInventory();
		super.onContainerClosed(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int i)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) super.inventorySlots.get(i);
		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (slot instanceof SlotMagiciansWorkbenchCrafting)
			{
				if (!this.mergeItemStack(itemstack1, this.INVENTORY_STORAGE_START, this.PLAYER_ACTION_BAR_END, true))
					return null;
			}
			else if (i < this.INVENTORY_STORAGE_START)
			{
				if (!this.mergeItemStack(itemstack1, this.INVENTORY_STORAGE_START, this.PLAYER_ACTION_BAR_END, true))
					return null;
			}
			else if (i >= this.INVENTORY_STORAGE_START && i < this.PLAYER_INVENTORY_START)
			{
				if (!this.mergeItemStack(itemstack1, this.PLAYER_INVENTORY_START, this.PLAYER_ACTION_BAR_END, false))
					return null;
			}
			else if (i >= this.PLAYER_INVENTORY_START && i < this.PLAYER_ACTION_BAR_START)
			{
				if (!this.mergeItemStack(itemstack1, this.INVENTORY_STORAGE_START, this.PLAYER_INVENTORY_START, false) && !this.mergeItemStack(itemstack1, this.PLAYER_ACTION_BAR_START, this.PLAYER_ACTION_BAR_END, false))
					return null;
			}
			else if (i >= this.PLAYER_ACTION_BAR_START && i < this.PLAYER_ACTION_BAR_END)
			{
				if (!this.mergeItemStack(itemstack1, this.INVENTORY_STORAGE_START, this.PLAYER_ACTION_BAR_START - 1, false))
					return null;
			}
			else if (!this.mergeItemStack(itemstack1, this.PLAYER_INVENTORY_START, this.PLAYER_ACTION_BAR_END, false))
				return null;

			if (itemstack1.stackSize == 0)
				slot.putStack((ItemStack) null);
			else
				slot.onSlotChanged();

			if (itemstack1.stackSize == itemstack.stackSize)
				return null;

			slot.onPickupFromSlot(player, itemstack1);
		}

		return itemstack;
	}

	public TileEntityMagiciansWorkbench getWorkbench()
	{
		return this.workbenchInventory;
	}

	public HashMap<ImmutablePair<Item, Integer>, Integer> getComponentCount(int recipeIndex)
	{
		HashMap<ImmutablePair<Item, Integer>, Integer> componentCount = new HashMap();
		TileEntityMagiciansWorkbench.RememberedRecipe recipe = this.workbenchInventory.getRememberedRecipeItems().get(recipeIndex);

		for (ItemStack stack : recipe.components)
			if (stack != null)
			{
				ImmutablePair<Item, Integer> pair = new ImmutablePair(stack.getItem(), Integer.valueOf(stack.getItemDamage()));
				if (componentCount.containsKey(pair))
				{
					int amt = componentCount.get(pair).intValue();
					++amt;
					componentCount.put(pair, Integer.valueOf(amt));
				}
				else
					componentCount.put(pair, Integer.valueOf(1));
			}

		return componentCount;
	}

	public boolean hasComponents(int recipeIndex)
	{
		HashMap<ImmutablePair<Item, Integer>, Integer> componentCount = this.getComponentCount(recipeIndex);
		boolean allComponentsPresent = true;

		for (ImmutablePair<Item, Integer> pair : componentCount.keySet())
		{
			Integer qty = componentCount.get(pair);
			if (qty == null)
				return false;

			allComponentsPresent &= this.hasComponent(new ItemStack(pair.left, 1, pair.right.intValue()), qty.intValue());
		}

		return allComponentsPresent;
	}

	private boolean hasComponent(ItemStack component, int qty)
	{
		int matchedQty = 0;

		for (int i = this.getWorkbench().getStorageStart() - 3; i < this.getWorkbench().getStorageStart() - 3 + this.getWorkbench().getStorageSize(); ++i)
		{
			ItemStack stack = this.getWorkbench().getStackInSlot(i);
			if (stack != null && stack.isItemEqual(component))
			{
				    
				if (!ItemStack.areItemStackTagsEqual(stack, component))
					continue;
				    

				matchedQty += stack.stackSize;
			}

			if (matchedQty >= qty)
				return true;
		}

		return false;
	}

	private void decrementStoredComponents(int recipeIndex)
	{
		HashMap<ImmutablePair<Item, Integer>, Integer> componentCount = this.getComponentCount(recipeIndex);

		for (ImmutablePair<Item, Integer> pair : componentCount.keySet())
		{
			Integer qty = componentCount.get(pair);
			if (qty == null)
				return;

			this.decrementStoredComponent(new ItemStack(pair.left, 1, pair.right.intValue()), qty.intValue());
		}

	}

	private void decrementStoredComponent(ItemStack component, int qty)
	{
		int qtyLeft = qty;

		for (int i = this.INVENTORY_STORAGE_START; i < this.PLAYER_INVENTORY_START - 1; ++i)
		{
			Slot slot = (Slot) super.inventorySlots.get(i);
			ItemStack stack = slot.getStack();
			if (stack != null && stack.isItemEqual(component))
			{
				    
				if (!ItemStack.areItemStackTagsEqual(stack, component))
					continue;
				    

				if (stack.stackSize > qtyLeft)
				{
					stack.stackSize -= qtyLeft;
					slot.putStack(stack);
					slot.onSlotChanged();
					return;
				}

				qtyLeft -= stack.stackSize;
				slot.putStack((ItemStack) null);
				slot.onSlotChanged();
			}
		}

	}

	private void setRecipeItemsToGrid(int recipeIndex)
	{
		TileEntityMagiciansWorkbench.RememberedRecipe recipe;
		int count;
		label0:
		{
			recipe = this.workbenchInventory.getRememberedRecipeItems().get(recipeIndex);
			count = 0;
			if (!recipe.is2x2)
			{
				TileEntityMagiciansWorkbench var10000 = this.getWorkbench();
				this.getWorkbench();
				if (!var10000.getUpgradeStatus((byte) 1))
					break label0;
			}

			if (this.craftingGridIsEmpty(true))
			{
				for (ItemStack stack : recipe.components)
				{
					Slot slot = this.getSlot(10 + count);
					if (stack != null)
						slot.putStack(new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
					else
						slot.putStack((ItemStack) null);

					slot.onSlotChanged();
					++count;
					if (recipe.is2x2 && count == 2)
						++count;
				}

				return;
			}
		}

		if (this.craftingGridIsEmpty(false))
			for (ItemStack stack : recipe.components)
			{
				Slot slot = this.getSlot(count);
				if (stack != null)
					slot.putStack(new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
				else
					slot.putStack((ItemStack) null);

				slot.onSlotChanged();
				++count;
				if (recipe.is2x2 && count == 2)
					++count;
			}

	}

	public boolean gridIsFreeFor(int recipeIndex)
	{
		TileEntityMagiciansWorkbench.RememberedRecipe recipe = this.workbenchInventory.getRememberedRecipeItems().get(recipeIndex);
		if (recipe.components.length > 4)
		{
			TileEntityMagiciansWorkbench var10001 = this.workbenchInventory;
			if (!this.workbenchInventory.getUpgradeStatus((byte) 1))
				return this.craftingGridIsEmpty(false);
		}

		return this.craftingGridIsEmpty(false) || this.craftingGridIsEmpty(true);
	}

	private boolean craftingGridIsEmpty(boolean second)
	{
		if (!second)
		{
			for (int i = 0; i < 9; ++i)
				if (this.getWorkbench().getStackInSlot(i) != null)
					return false;
		}
		else
			for (int i = 0; i < 9; ++i)
				if (this.getWorkbench().getStackInSlot(i + 9) != null)
					return false;

		return true;
	}

	public void moveRecipeToCraftingGrid(int recipeIndex)
	{
		if (this.gridIsFreeFor(recipeIndex) && !this.isRecipeAlreadyInGrid(recipeIndex))
			if (this.world.isRemote)
			{
				AMDataWriter writer = new AMDataWriter();
				writer.add(recipeIndex);
				AMNetHandler.INSTANCE.sendPacketToServer((byte) 45, writer.generate());
			}
			else if (this.hasComponents(recipeIndex))
			{
				this.decrementStoredComponents(recipeIndex);
				this.setRecipeItemsToGrid(recipeIndex);
				this.updateCraftingMatrices();
				this.onCraftMatrixChanged(this.workbenchInventory);
				this.detectAndSendChanges();
			}
	}

	public boolean isRecipeAlreadyInGrid(int recipeIndex)
	{
		TileEntityMagiciansWorkbench.RememberedRecipe recipe = this.workbenchInventory.getRememberedRecipeItems().get(recipeIndex);
		ItemStack stack1 = this.getWorkbench().firstCraftResult.getStackInSlot(0);
		ItemStack stack2 = this.getWorkbench().secondCraftResult.getStackInSlot(0);

		    
		return isItemEqual(stack1, recipe.output) || isItemEqual(stack2, recipe.output);
		    
	}

	    
	private static boolean isItemEqual(ItemStack stack1, ItemStack stack2)
	{
		if (stack1 == null || stack2 == null)
			return false;
		if (stack1 == stack2)
			return true;
		return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}
	    
}
