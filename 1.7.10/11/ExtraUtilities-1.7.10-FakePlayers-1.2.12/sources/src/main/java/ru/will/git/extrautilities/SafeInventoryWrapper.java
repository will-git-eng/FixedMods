package ru.will.git.extrautilities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public final class SafeInventoryWrapper implements IInventory
{
	private final IInventory inv;
	private final SafeChecker safeChecker;

	public SafeInventoryWrapper(IInventory inv, SafeChecker safeChecker)
	{
		this.inv = inv;
		this.safeChecker = safeChecker;
	}

	@Override
	public int getSizeInventory()
	{
		return this.inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int p_70301_1_)
	{
		return this.inv.getStackInSlot(p_70301_1_);
	}

	@Override
	public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_)
	{
		return this.inv.decrStackSize(p_70298_1_, p_70298_2_);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int p_70304_1_)
	{
		return this.inv.getStackInSlotOnClosing(p_70304_1_);
	}

	@Override
	public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_)
	{
		this.inv.setInventorySlotContents(p_70299_1_, p_70299_2_);
	}

	@Override
	public String getInventoryName()
	{
		return this.inv.getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return this.inv.hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.inv.getInventoryStackLimit();
	}

	@Override
	public void markDirty()
	{
		this.inv.markDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return this.inv.isUseableByPlayer(player) && this.safeChecker.isUseableByPlayer0(player);
	}

	@Override
	public void openInventory()
	{
		this.inv.openInventory();
	}

	@Override
	public void closeInventory()
	{
		this.inv.closeInventory();
	}

	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_)
	{
		return this.inv.isItemValidForSlot(p_94041_1_, p_94041_2_);
	}

	public interface SafeChecker
	{
		boolean isUseableByPlayer0(EntityPlayer player);
	}
}
