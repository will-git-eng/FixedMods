/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.mail.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import forestry.api.core.IErrorSource;
import forestry.api.core.IErrorState;
import forestry.api.mail.ILetter;
import forestry.core.errors.EnumErrorCode;
import forestry.core.inventory.ItemInventory;
import forestry.core.items.ItemWithGui;
import forestry.core.utils.SlotUtil;
import forestry.mail.Letter;
import forestry.mail.LetterProperties;
import forestry.mail.items.ItemStamps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemInventoryLetter extends ItemInventory implements IErrorSource
{
	private final ILetter letter;

	public ItemInventoryLetter(EntityPlayer player, ItemStack itemstack)
	{
		super(player, 0, itemstack);
		NBTTagCompound tagCompound = itemstack.getTagCompound();
		Preconditions.checkNotNull(tagCompound);
		this.letter = new Letter(tagCompound);
	}

	public ILetter getLetter()
	{
		return this.letter;
	}

	public void onLetterClosed()
	{
		ItemStack parent = this.getParent();
		LetterProperties.closeLetter(parent, this.letter);
	}

	public void onLetterOpened()
	{
		ItemStack parent = this.getParent();
		LetterProperties.openLetter(parent);
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		ItemStack result = this.letter.decrStackSize(index, count);
		NBTTagCompound tagCompound = this.getParent().getTagCompound();
		Preconditions.checkNotNull(tagCompound);
		this.letter.writeToNBT(tagCompound);
		return result;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack itemstack)
	{
		this.letter.setInventorySlotContents(index, itemstack);
		NBTTagCompound tagCompound = this.getParent().getTagCompound();
		Preconditions.checkNotNull(tagCompound);
		this.letter.writeToNBT(tagCompound);
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.letter.getStackInSlot(i);
	}

	@Override
	public int getSizeInventory()
	{
		return this.letter.getSizeInventory();
	}

	@Override
	public String getName()
	{
		return this.letter.getName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.letter.getInventoryStackLimit();
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player)
	{

		return this.letter.isUsableByPlayer(player) && super.isUsableByPlayer(player);
	}

	@Override
	public ItemStack removeStackFromSlot(int slot)
	{
		return this.letter.removeStackFromSlot(slot);
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack itemStack)
	{
		if (this.letter.isProcessed())
			return false;
		if (SlotUtil.isSlotInRange(slotIndex, Letter.SLOT_POSTAGE_1, Letter.SLOT_POSTAGE_COUNT))
		{
			Item item = itemStack.getItem();
			return item instanceof ItemStamps;
		}
		if (SlotUtil.isSlotInRange(slotIndex, Letter.SLOT_ATTACHMENT_1, Letter.SLOT_ATTACHMENT_COUNT))
			return !(itemStack.getItem() instanceof ItemWithGui);
		return false;
	}

	/* IErrorSource */
	@Override
	public ImmutableSet<IErrorState> getErrorStates()
	{

		ImmutableSet.Builder<IErrorState> errorStates = ImmutableSet.builder();

		if (!this.letter.hasRecipient())
			errorStates.add(EnumErrorCode.NO_RECIPIENT);

		if (!this.letter.isProcessed() && !this.letter.isPostPaid())
			errorStates.add(EnumErrorCode.NOT_POST_PAID);

		return errorStates.build();
	}
}
