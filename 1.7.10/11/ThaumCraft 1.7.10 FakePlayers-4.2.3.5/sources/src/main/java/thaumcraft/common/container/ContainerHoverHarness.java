package thaumcraft.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.common.items.armor.ItemHoverHarness;

import java.util.UUID;

public class ContainerHoverHarness extends Container
{
	private World worldObj;
	private int posX;
	private int posY;
	private int posZ;
	public IInventory input = new InventoryHoverHarness(this);
	ItemStack armor = null;
	EntityPlayer player = null;
    
	private static final String NBT_KEY_UID = "UID";
	private int blockedSlot;

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
		return baseUID != null && baseUID.equals(comparisonUID);
    

	public ContainerHoverHarness(InventoryPlayer iinventory, World par2World, int par3, int par4, int par5)
	{
		this.worldObj = par2World;
		this.posX = par3;
		this.posY = par4;
		this.posZ = par5;
		this.player = iinventory.player;
		this.armor = iinventory.getCurrentItem();
    
		if (this.armor != null && this.armor.getItem() instanceof ItemHoverHarness)
		{
			if (!this.armor.hasTagCompound())
				this.armor.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = this.armor.getTagCompound();
			if (!nbt.hasKey(NBT_KEY_UID))
				nbt.setString(NBT_KEY_UID, UUID.randomUUID().toString());
    

		this.addSlotToContainer(new Slot(this.input, 0, 80, 32));
		this.bindPlayerInventory(iinventory);
		if (!par2World.isRemote)
			try
			{
				ItemStack jar = ItemStack.loadItemStackFromNBT(this.armor.stackTagCompound.getCompoundTag("jar"));
				this.input.setInventorySlotContents(0, jar);
			}
			catch (Exception ignored)
			{
			}

		this.onCraftMatrixChanged(this.input);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
    
    

		for (int i = 0; i < 9; ++i)
		{
    
			if (slot.getSlotIndex() == currentSlot)
    
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		if (slot == this.blockSlot)
			return null;
		else
    
			if (slot == this.blockedSlot)
				return null;
			if (!this.canInteractWith(player))
    

			ItemStack stack = null;
			Slot slotObject = (Slot) this.inventorySlots.get(slot);
			if (slotObject != null && slotObject.getHasStack())
			{
				ItemStack stackInSlot = slotObject.getStack();
				stack = stackInSlot.copy();
				if (slot == 0)
				{
					if (!this.input.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 1, this.inventorySlots.size(), true, 64))
						return null;
				}
				else if (!this.input.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 0, 1, false, 1))
					return null;

				if (stackInSlot.stackSize == 0)
					slotObject.putStack(null);
				else
					slotObject.onSlotChanged();
			}

			return stack;
		}
	}

	@Override
	public ItemStack slotClick(int slot, int button, int buttonType, EntityPlayer player)
	{
		if (slot == this.blockSlot)
			return null;
		else
    
			if (slot == this.blockedSlot)
				return null;
			if (buttonType == 2 && button == this.blockedSlot)
				return null;
			if (!this.canInteractWith(player))
    

			InventoryPlayer inventoryplayer = player.inventory;
			return slot == 0 && !this.input.isItemValidForSlot(slot, inventoryplayer.getItemStack()) && (slot != 0 || inventoryplayer.getItemStack() != null) ? null : super.slotClick(slot, button, buttonType, player);
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
    
    
	}

	@Override
	public void putStackInSlot(int par1, ItemStack par2ItemStack)
	{
		if (this.input.isItemValidForSlot(par1, par2ItemStack))
			super.putStackInSlot(par1, par2ItemStack);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		if (!this.worldObj.isRemote)
		{
			ItemStack var3 = this.input.getStackInSlotOnClosing(0);
			if (var3 != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var3.writeToNBT(var4);
				this.armor.setTagInfo("jar", var4);
			}
			else
				this.armor.setTagInfo("jar", new NBTTagCompound());

			if (this.player == null)
				return;

			if (this.player.getHeldItem() != null && this.player.getHeldItem().isItemEqual(this.armor))
				this.player.setCurrentItemOrArmor(0, this.armor);

			this.player.inventory.markDirty();
		}

	}

	protected boolean mergeItemStack(ItemStack par1ItemStack, int par2, int par3, boolean par4, int limit)
	{
		boolean var5 = false;
		int var6 = par2;
		if (par4)
			var6 = par3 - 1;

		if (par1ItemStack.isStackable())
			while (par1ItemStack.stackSize > 0 && (par4 ? var6 >= par2 : var6 < par3))
			{
				Slot var7 = (Slot) this.inventorySlots.get(var6);
				ItemStack var8 = var7.getStack();
				if (var8 != null && var8.getItem() == par1ItemStack.getItem() && (!par1ItemStack.getHasSubtypes() || par1ItemStack.getItemDamage() == var8.getItemDamage()) && ItemStack.areItemStackTagsEqual(par1ItemStack, var8))
				{
					int var9 = var8.stackSize + par1ItemStack.stackSize;
					if (var9 <= Math.min(par1ItemStack.getMaxStackSize(), limit))
					{
						par1ItemStack.stackSize = 0;
						var8.stackSize = var9;
						var7.onSlotChanged();
						var5 = true;
					}
					else if (var8.stackSize < Math.min(par1ItemStack.getMaxStackSize(), limit))
					{
						par1ItemStack.stackSize -= Math.min(par1ItemStack.getMaxStackSize(), limit) - var8.stackSize;
						var8.stackSize = Math.min(par1ItemStack.getMaxStackSize(), limit);
						var7.onSlotChanged();
						var5 = true;
					}
				}

				if (par4)
					--var6;
				else
					++var6;
			}

		if (par1ItemStack.stackSize > 0)
		{
			if (par4)
				var6 = par3 - 1;
			else
				var6 = par2;

			while (par4 ? var6 >= par2 : var6 < par3)
			{
				Slot var7 = (Slot) this.inventorySlots.get(var6);
				ItemStack var8 = var7.getStack();
				if (var8 == null)
				{
					ItemStack res = par1ItemStack.copy();
					res.stackSize = Math.min(res.stackSize, limit);
					var7.putStack(res);
					var7.onSlotChanged();
					par1ItemStack.stackSize -= res.stackSize;
					var5 = true;
					break;
				}

				if (par4)
					--var6;
				else
					++var6;
			}
		}

		return var5;
	}
}
