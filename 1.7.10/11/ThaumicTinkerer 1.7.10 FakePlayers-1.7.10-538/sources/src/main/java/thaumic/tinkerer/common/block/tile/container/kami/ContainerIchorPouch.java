    
package thaumic.tinkerer.common.block.tile.container.kami;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.common.container.InventoryFocusPouch;
import thaumcraft.common.items.wands.ItemFocusPouch;
import thaumic.tinkerer.common.block.tile.container.ContainerPlayerInv;
import thaumic.tinkerer.common.block.tile.container.slot.kami.SlotNoPouches;

import java.util.UUID;

public class ContainerIchorPouch extends ContainerPlayerInv
{

	public IInventory inv = new InventoryIchorPouch(this);
	EntityPlayer player;
	ItemStack pouch;
	int blockSlot;

	public ContainerIchorPouch(EntityPlayer player)
	{
		super(player.inventory);

		this.player = player;
		this.pouch = player.getCurrentEquippedItem();
    
		if (this.pouch != null)
		{
			if (!this.pouch.hasTagCompound())
				this.pouch.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = this.pouch.getTagCompound();
			if (!nbt.hasKey(NBT_KEY_UID))
				nbt.setString(NBT_KEY_UID, UUID.randomUUID().toString());
    

		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 13; x++)
			{
				this.addSlotToContainer(new SlotNoPouches(this.inv, y * 13 + x, 12 + x * 18, 8 + y * 18));
			}
		}
		this.initPlayerInv();

		if (!player.worldObj.isRemote)
			try
			{
				((InventoryIchorPouch) this.inv).stackList = ((ItemFocusPouch) this.pouch.getItem()).getInventory(this.pouch);
			}
			catch (Exception e)
			{
			}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot)
	{
		if (slot == this.blockSlot)
			return null;

		ItemStack stack = null;
		Slot slotObject = (Slot) this.inventorySlots.get(slot);
		if (slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();
			if (slot < 13 * 9)
			{
				if (!this.inv.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 13 * 9, this.inventorySlots.size(), true))
					return null;
			}
			else if (!this.inv.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 0, 13 * 9, false))
				return null;
			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();
		}

		return stack;
	}

	@Override
	public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer par4EntityPlayer)
	{
		if (par1 == this.blockSlot)
			return null;
		return super.slotClick(par1, par2, par3, par4EntityPlayer);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		if (!this.player.worldObj.isRemote)
		{
    
			if (this.player == null || !this.canInteractWith(this.player))
				return;
			if (this.player.getHeldItem() != null && this.player.getHeldItem().isItemEqual(this.pouch))
				this.player.setCurrentItemOrArmor(0, this.pouch);

			this.player.inventory.markDirty();
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
    
    
	}

	@Override
	public int getInvXStart()
	{
		return 48;
	}

	@Override
	public int getInvYStart()
	{
		return 177;
    
	private static final String NBT_KEY_UID = "UID";

	@Override
	public void onCraftMatrixChanged(IInventory inventory)
	{
		((ItemFocusPouch) this.pouch.getItem()).setInventory(this.pouch, ((InventoryIchorPouch) this.inv).stackList);
		if (this.player != null && this.canInteractWith(this.player) && this.player.getHeldItem() != null && this.player.getHeldItem().isItemEqual(this.pouch))
			this.player.setCurrentItemOrArmor(0, this.pouch);
		super.onCraftMatrixChanged(inventory);
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
    

	private static class InventoryIchorPouch extends InventoryFocusPouch
	{

		public InventoryIchorPouch(Container par1Container)
		{
			super(par1Container);
			this.stackList = new ItemStack[13 * 9];
		}

		@Override
		public int getInventoryStackLimit()
		{
			return 64;
		}

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return itemstack != null && !(itemstack.getItem() instanceof ItemFocusPouch);
		}
	}
}