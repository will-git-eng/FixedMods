package chylex.hee.gui;

import chylex.hee.gui.slots.SlotCharmPouchItem;
import chylex.hee.gui.slots.SlotCharmPouchRune;
import chylex.hee.gui.slots.SlotCharmPouchRuneResult;
import chylex.hee.init.ItemList;
import chylex.hee.item.ItemCharmPouch;
import chylex.hee.mechanics.charms.CharmPouchInfo;
import chylex.hee.mechanics.charms.CharmRecipe;
import chylex.hee.mechanics.charms.CharmType;
import chylex.hee.mechanics.charms.RuneType;
import chylex.hee.mechanics.charms.handler.CharmPouchHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ContainerCharmPouch extends Container
{
	private final EntityPlayer player;
	private final IInventory charmInv = new InventoryBasic("container.charmPouch", false, 3);
	private final IInventory runeInv = new InventoryBasic("container.runeCrafting", false, 5);
	private final IInventory runeResultInv = new InventoryBasic("container.runeCrafting", false, 1);
	private final long pouchID;

	public ContainerCharmPouch(EntityPlayer player)
	{
		this.player = player;
		this.pouchID = ItemCharmPouch.getPouchID(player.getHeldItem());

		for (int a = 0; a < 3; ++a)
		{
			this.addSlotToContainer(new SlotCharmPouchItem(this.charmInv, this, a, 39, 20 + a * 20));
		}

		ItemStack[] charms = ItemCharmPouch.getPouchCharms(player.getHeldItem());

		for (int a = 0; a < Math.min(this.charmInv.getSizeInventory(), charms.length); ++a)
		{
			this.charmInv.setInventorySlotContents(a, charms[a]);
		}

		this.addSlotToContainer(new SlotCharmPouchRune(this.runeInv, this, 0, 122, 18, ItemList.rune, 16));
		this.addSlotToContainer(new SlotCharmPouchRune(this.runeInv, this, 1, 98, 38, ItemList.rune, 16));
		this.addSlotToContainer(new SlotCharmPouchRune(this.runeInv, this, 2, 146, 38, ItemList.rune, 16));
		this.addSlotToContainer(new SlotCharmPouchRune(this.runeInv, this, 3, 109, 63, ItemList.rune, 16));
		this.addSlotToContainer(new SlotCharmPouchRune(this.runeInv, this, 4, 135, 63, ItemList.rune, 16));
		this.addSlotToContainer(new SlotCharmPouchRuneResult(this.runeResultInv, this.runeInv, this, 0, 122, 41));

		for (int a = 0; a < 3; ++a)
		{
			for (int b = 0; b < 9; ++b)
			{
				this.addSlotToContainer(new Slot(player.inventory, b + a * 9 + 9, 8 + b * 18, 99 + a * 18));
			}
		}

		for (int a = 0; a < 9; ++a)
		{
			this.addSlotToContainer(new Slot(player.inventory, a, 8 + a * 18, 157));
		}

	}

	private boolean isHoldingPouch()
	{
		ItemStack is = this.player.getHeldItem();
		return is != null && is.getItem() == ItemList.charm_pouch;
	}

	public void saveCharmPouch()
	{
		if (!this.player.worldObj.isRemote && this.isHoldingPouch())
			ItemCharmPouch.setPouchCharms(this.player.getHeldItem(), new ItemStack[] { this.charmInv.getStackInSlot(0), this.charmInv.getStackInSlot(1), this.charmInv.getStackInSlot(2) });

	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		if (!this.player.worldObj.isRemote && !this.isHoldingPouch())
			this.player.closeScreen();

	}

	@Override
	public void onCraftMatrixChanged(IInventory inventory)
	{
		if (inventory == this.runeInv)
		{
			this.runeResultInv.setInventorySlotContents(0, null);
			List<RuneType> runes = new ArrayList<RuneType>(5);

			for (int a = 0; a < 5; ++a)
			{
				ItemStack rune = this.runeInv.getStackInSlot(a);
				if (rune != null)
				{
					int damage = rune.getItemDamage();
					if (damage >= 0 && damage < RuneType.values.length)
						runes.add(RuneType.values[damage]);
				}
			}

			if (runes.size() >= 3)
			{
				Pair<CharmType, CharmRecipe> charm = CharmType.findRecipe(runes.toArray(new RuneType[0]));
				if (charm.getRight() != null)
					this.runeResultInv.setInventorySlotContents(0, new ItemStack(ItemList.charm, 1, charm.getRight().id));
			}
		}

	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed(player);

		for (int a = 0; a < 5; ++a)
		{
			ItemStack is = this.runeInv.getStackInSlot(a);
			if (is != null)
				player.dropPlayerItemWithRandomChoice(is, false);
		}

		this.runeResultInv.setInventorySlotContents(0, null);
		CharmPouchInfo activePouch = CharmPouchHandler.getActivePouch(player);
		if (activePouch != null && activePouch.pouchID == this.pouchID)
			CharmPouchHandler.setActivePouch(player, player.getHeldItem());

	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotId)
	{
		ItemStack is = null;
		Slot slot = (Slot) this.inventorySlots.get(slotId);
		if (slot != null && slot.getHasStack())
		{
			ItemStack is2 = slot.getStack();
			is = is2.copy();
			if (slotId < 9)
			{
				if (!this.mergeItemStack(is2, 9, this.inventorySlots.size(), true))
					return null;
			}
			else if (is2.getItem() == ItemList.charm)
			{
				if (!this.mergeItemStack(is2, 0, 3, false))
					return null;
			}
			else
			{
				if (is2.getItem() != ItemList.rune)
					return null;

				if (!this.mergeItemStack(is2, 3, 8, false))
					return null;
			}

			if (is2.stackSize == 0)
				slot.putStack(null);
			else
				slot.onSlotChanged();

			    
			if (is2.stackSize == is.stackSize)
				return null;
			slot.onPickupFromSlot(player, is2);
			    
		}

		return is;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return true;
	}
}
