package am2.containers;

import java.util.UUID;

import am2.api.spell.ItemSpellBase;
import am2.containers.slots.SlotOneItemClassOnly;
import am2.items.ItemSpellBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ContainerSpellBook extends Container
{
	private ItemStack bookStack;
	private InventorySpellBook spellBookStack;
	private int bookSlot;
	public int specialSlotIndex;

	    
	private static final String NBT_KEY_UID = "UID";
	private final EntityPlayer player;
	    

	public ContainerSpellBook(InventoryPlayer invPlayer, ItemStack bookStack, InventorySpellBook invBook)
	{
		this.spellBookStack = invBook;
		this.bookStack = bookStack;
		this.bookSlot = invPlayer.currentItem;
		int slotIndex = 0;

		    
		this.player = invPlayer.player;
		invBook.container = this;

		if (bookStack != null && bookStack.getItem() instanceof ItemSpellBook)
		{
			if (!bookStack.hasTagCompound())
				bookStack.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = bookStack.getTagCompound();
			if (!nbt.hasKey(NBT_KEY_UID))
				nbt.setString(NBT_KEY_UID, UUID.randomUUID().toString());
		}
		    

		for (int i = 0; i < 8; ++i)
			this.addSlotToContainer(new SlotOneItemClassOnly(this.spellBookStack, slotIndex++, 18, 5 + i * 18, ItemSpellBase.class, 1));

		for (int i = 0; i < 4; ++i)
			for (int k = 0; k < 8; ++k)
				this.addSlotToContainer(new SlotOneItemClassOnly(this.spellBookStack, slotIndex++, 138 + i * 26, 5 + k * 18, ItemSpellBase.class, 1));

		for (int i = 0; i < 3; ++i)
			for (int k = 0; k < 9; ++k)
				this.addSlotToContainer(new Slot(invPlayer, k + i * 9 + 9, 48 + k * 18, 171 + i * 18));

		for (int j1 = 0; j1 < 9; ++j1)
			if (invPlayer.getStackInSlot(j1) == bookStack)
				this.specialSlotIndex = j1 + 67;
			else
				this.addSlotToContainer(new Slot(invPlayer, j1, 48 + j1 * 18, 229));
	}

	public ItemStack[] GetActiveSpells()
	{
		ItemStack[] itemStack = new ItemStack[7];

		for (int i = 0; i < 7; ++i)
			itemStack[i] = this.spellBookStack.getStackInSlot(i);

		return itemStack;
	}

	public ItemStack[] GetFullInventory()
	{
		ItemStack[] stack = new ItemStack[40];

		for (int i = 0; i < 40; ++i)
			stack[i] = ((Slot) super.inventorySlots.get(i)).getStack();

		return stack;
	}

	    
	public void save()
	{
		if (!this.player.worldObj.isRemote)
		{
			ItemStack spellBookItemStack = this.bookStack;
			ItemSpellBook spellBook = (ItemSpellBook) spellBookItemStack.getItem();
			ItemStack[] items = this.GetFullInventory();
			spellBook.UpdateStackTagCompound(spellBookItemStack, items);
			this.player.inventory.setInventorySlotContents(this.player.inventory.currentItem, spellBookItemStack);
		}
	}

	@Override
	public ItemStack slotClick(int slot, int button, int buttonType, EntityPlayer player)
	{
		if (slot == this.specialSlotIndex)
			return null;
		if (buttonType == 2 && button == this.specialSlotIndex)
			return null;
		if (!isSameItemInventory(player.getCurrentEquippedItem(), this.bookStack))
			return null;

		return super.slotClick(slot, button, buttonType, player);
	}

	private static boolean isSameItemInventory(ItemStack base, ItemStack comparison)
	{
		if (base == null || comparison == null)
			return false;

		if (base.getItem() != comparison.getItem())
			return false;

		if (!base.hasTagCompound() || !comparison.hasTagCompound())
			return false;

		String baseUID = base.getTagCompound().getString(NBT_KEY_UID);
		String comparisonUID = comparison.getTagCompound().getString(NBT_KEY_UID);
		return baseUID != null && comparisonUID != null && baseUID.equals(comparisonUID);
	}
	    

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		World world = player.worldObj;
		if (!world.isRemote)
		{
			ItemStack spellBookItemStack = this.bookStack;
			ItemSpellBook spellBook = (ItemSpellBook) spellBookItemStack.getItem();
			ItemStack[] items = this.GetFullInventory();
			spellBook.UpdateStackTagCompound(spellBookItemStack, items);
			player.inventory.setInventorySlotContents(player.inventory.currentItem, spellBookItemStack);
		}

		super.onContainerClosed(player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		    
		if (!isSameItemInventory(this.player.getCurrentEquippedItem(), this.bookStack))
			return false;
		    

		return this.spellBookStack.isUseableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) super.inventorySlots.get(i);
		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (i < 40)
			{
				if (!this.mergeItemStack(itemstack1, 40, 75, true))
					return null;
			}
			else if (i >= 40 && i < 67)
			{
				if (itemstack.getItem() instanceof ItemSpellBase)
					for (int n = 0; n < 40; ++n)
					{
						Slot scrollSlot = (Slot) super.inventorySlots.get(n);
						if (!scrollSlot.getHasStack())
						{
							ItemStack newStack = new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage());
							newStack.setTagCompound(itemstack1.getTagCompound());
							scrollSlot.putStack(newStack);
							scrollSlot.onSlotChanged();
							--itemstack1.stackSize;
							if (itemstack1.stackSize == 0)
							{
								slot.putStack((ItemStack) null);
								slot.onSlotChanged();
							}

							return null;
						}
					}

				if (!this.mergeItemStack(itemstack1, 67, 75, false))
					return null;
			}
			else if (i >= 67 && i < 76)
			{
				if (itemstack.getItem() instanceof ItemSpellBase)
					for (int n = 0; n < 40; ++n)
					{
						Slot scrollSlot = (Slot) super.inventorySlots.get(n);
						if (!scrollSlot.getHasStack())
						{
							ItemStack newStack = new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage());
							newStack.setTagCompound(itemstack1.getTagCompound());
							scrollSlot.putStack(newStack);
							scrollSlot.onSlotChanged();
							--itemstack1.stackSize;
							if (itemstack1.stackSize == 0)
							{
								slot.putStack((ItemStack) null);
								slot.onSlotChanged();
							}

							return null;
						}
					}

				if (!this.mergeItemStack(itemstack1, 40, 67, false))
					return null;
			}
			else if (!this.mergeItemStack(itemstack1, 40, 75, false))
				return null;

			if (itemstack1.stackSize == 0)
				slot.putStack((ItemStack) null);
			else
				slot.onSlotChanged();

			if (itemstack1.stackSize == itemstack.stackSize)
				return null;

			slot.onSlotChange(itemstack1, itemstack);
		}

		return itemstack;
	}
}
