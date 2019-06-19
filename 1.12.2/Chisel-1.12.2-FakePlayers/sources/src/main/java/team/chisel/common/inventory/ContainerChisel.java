package team.chisel.common.inventory;

import ru.will.git.eventhelper.util.ItemInventoryValidator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import team.chisel.Chisel;
import team.chisel.api.IChiselItem;
import team.chisel.api.carving.ICarvingRegistry;
import team.chisel.common.carving.Carving;
import team.chisel.common.util.NBTUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ContainerChisel extends Container
{
	protected final InventoryChiselSelection inventoryChisel;
	protected final InventoryPlayer inventoryPlayer;
	protected final EnumHand hand;
	protected final int chiselSlot;
	protected final ItemStack chisel;
	protected final ICarvingRegistry carving;
	ClickType currentClickType;

	    
	public final ItemInventoryValidator validator;
	    

	public ContainerChisel(InventoryPlayer inventoryplayer, InventoryChiselSelection inv, EnumHand hand)
	{
		this.inventoryChisel = inv;
		this.inventoryPlayer = inventoryplayer;
		this.hand = hand;
		this.chiselSlot = hand == EnumHand.MAIN_HAND ? inventoryplayer.currentItem : inventoryplayer.getSizeInventory() - 1;
		this.chisel = inventoryplayer.getStackInSlot(this.chiselSlot);
		this.carving = Carving.chisel;
		inv.container = this;

		    
		this.validator = new ItemInventoryValidator(this.chisel, IChiselItem.class::isInstance);
		this.validator.setSlotIndex(this.chiselSlot, true);
		    

		this.addSlots();
		if (!this.chisel.isEmpty() && this.chisel.getTagCompound() != null)
		{
			ItemStack stack = NBTUtil.getChiselTarget(this.chisel);
			this.inventoryChisel.setInventorySlotContents(this.getInventoryChisel().size, stack);
		}

		this.inventoryChisel.updateItems();
	}

	protected void addSlots()
	{
		int top = 8;
		int left = 62;

		for (int i = 0; i < this.getInventoryChisel().size; ++i)
		{
			this.addSlotToContainer(new SlotChiselSelection(this, this.inventoryChisel, this.inventoryChisel, i, left + i % 10 * 18, top + i / 10 * 18));
		}

		this.addSlotToContainer(new SlotChiselInput(this, this.inventoryChisel, this.getInventoryChisel().size, 24, 24));
		top = top + 112;
		left = left + 9;

		for (int i = 0; i < 27; ++i)
		{
			this.addSlotToContainer(new Slot(this.inventoryPlayer, i + 9, left + i % 9 * 18, top + i / 9 * 18));
		}

		top = top + 58;

		for (int i = 0; i < 9; ++i)
		{
			Slot slot = this.addSlotToContainer(new Slot(this.inventoryPlayer, i, left + i % 9 * 18, top + i / 9 * 18));

			    
			this.validator.tryGetSlotIdFromPlayerSlot(slot);
			    
		}
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
	{
		    
		if (!this.validator.canSlotClick(slotId, dragType, clickTypeIn, player))
			return ItemStack.EMPTY;
		    

		if (clickTypeIn != ClickType.QUICK_CRAFT && slotId >= 0)
		{
			int clickedSlot = slotId - this.inventoryChisel.getSizeInventory() - 27;
			Chisel.debug("Slot clicked is " + slotId + " and slot length is " + this.inventorySlots.size());

			try
			{
				Slot slot = this.inventorySlots.get(slotId);
				Chisel.debug("Slot is " + slot);
			}
			catch (Exception var7)
			{
				Chisel.debug("Exception getting slot");
				var7.printStackTrace();
			}

			if (clickedSlot == this.chiselSlot || clickTypeIn == ClickType.SWAP)
				return ItemStack.EMPTY;
		}

		this.currentClickType = clickTypeIn;
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		this.inventoryChisel.clearItems();
		super.onContainerClosed(entityplayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return this.inventoryChisel.isUsableByPlayer(player) && this.validator.canInteractWith(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIdx)
	{
		    
		if (this.validator.getSlotId() == slotIdx)
			return ItemStack.EMPTY;
		    

		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotIdx);
		if (slot != null && slot.getHasStack())
		{
			    
			if (!this.canInteractWith(player))
				return ItemStack.EMPTY;
			    

			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (slotIdx > this.getInventoryChisel().size)
			{
				if (!this.mergeItemStack(itemstack1, this.getInventoryChisel().size, this.getInventoryChisel().size + 1, false))
					return ItemStack.EMPTY;
			}
			else if (slotIdx < this.getInventoryChisel().size && !itemstack1.isEmpty())
			{
				SlotChiselSelection selectslot = (SlotChiselSelection) slot;
				ItemStack check = selectslot.craft(player, itemstack1, true);
				if (check.isEmpty())
					return ItemStack.EMPTY;

				if (!this.mergeItemStack(check, this.getInventoryChisel().size + 1, this.getInventoryChisel().size + 1 + 36, true))
					return ItemStack.EMPTY;

				itemstack1 = selectslot.craft(player, itemstack1, false);
			}
			else if (!this.mergeItemStack(itemstack1, this.getInventoryChisel().size + 1, this.getInventoryChisel().size + 1 + 36, true))
				return ItemStack.EMPTY;

			boolean clearSlot = slotIdx >= this.getInventoryChisel().size || this.getInventoryChisel().getStackInSpecialSlot().isEmpty();
			slot.onSlotChange(itemstack1, itemstack);
			if (itemstack1.isEmpty())
			{
				if (clearSlot)
					slot.putStack(ItemStack.EMPTY);
			}
			else
				slot.onSlotChanged();

			this.getInventoryChisel().updateItems();
			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;
			if (slotIdx >= this.getInventoryChisel().size)
				slot.onTake(player, itemstack1);

			if (itemstack1.isEmpty())
			{
				if (clearSlot)
					slot.putStack(ItemStack.EMPTY);

				return ItemStack.EMPTY;
			}
			slot.putStack(itemstack1);
			return itemstack1;
		}
		return itemstack;
	}

	public void onChiselSlotChanged()
	{
		NBTUtil.setChiselTarget(this.chisel, this.inventoryChisel.getStackInSpecialSlot());
	}

	public void onChiselBroken()
	{
		if (!this.getInventoryPlayer().player.world.isRemote)
			this.getInventoryPlayer().player.dropItem(this.inventoryChisel.getStackInSpecialSlot(), false);

	}

	public InventoryChiselSelection getInventoryChisel()
	{
		return this.inventoryChisel;
	}

	public InventoryPlayer getInventoryPlayer()
	{
		return this.inventoryPlayer;
	}

	public EnumHand getHand()
	{
		return this.hand;
	}

	public int getChiselSlot()
	{
		return this.chiselSlot;
	}

	public ItemStack getChisel()
	{
		return this.chisel;
	}

	public ICarvingRegistry getCarving()
	{
		return this.carving;
	}

	public ClickType getCurrentClickType()
	{
		return this.currentClickType;
	}
}
