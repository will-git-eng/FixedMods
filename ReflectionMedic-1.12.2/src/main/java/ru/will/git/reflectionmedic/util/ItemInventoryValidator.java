package ru.will.git.reflectionmedic.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ItemInventoryValidator
{
	public static final String NBT_KEY_ID = "UID";
	private static final Random RANDOM = new Random();

	@Nullable
	private final ItemStack stack;
	@Nonnull
	private final String nbtIdKey;
	@Nullable
	private final Predicate<Item> itemValidator;
	@Nullable
	private final Function<EntityPlayer, ItemStack> stackGetter;

	private boolean itemInHotbar = true;
	private int slotIndex = -1;
	private int slotId = -1;

	public ItemInventoryValidator(@Nullable ItemStack stack)
	{
		this(stack, null, true);
	}

	public ItemInventoryValidator(@Nullable ItemStack stack, @Nullable Predicate<Item> itemValidator)
	{
		this(stack, null, true, itemValidator);
	}

	public ItemInventoryValidator(@Nullable ItemStack stack, @Nullable Function<EntityPlayer, ItemStack> stackGetter)
	{
		this(stack, null, true, stackGetter);
	}

	public ItemInventoryValidator(
			@Nullable ItemStack stack,
			@Nullable Predicate<Item> itemValidator, @Nullable Function<EntityPlayer, ItemStack> stackGetter)
	{
		this(stack, null, true, itemValidator, stackGetter);
	}

	public ItemInventoryValidator(@Nullable ItemStack stack, @Nullable String nbtKeyId, boolean generateIdIfAbsent)
	{
		this(stack, nbtKeyId, generateIdIfAbsent, null, null);
	}

	public ItemInventoryValidator(
			@Nullable ItemStack stack,
			@Nullable String nbtKeyId, boolean generateIdIfAbsent, @Nullable Predicate<Item> itemValidator)
	{
		this(stack, nbtKeyId, generateIdIfAbsent, itemValidator, null);
	}

	public ItemInventoryValidator(
			@Nullable ItemStack stack,
			@Nullable String nbtKeyId, boolean generateIdIfAbsent,
			@Nullable Function<EntityPlayer, ItemStack> stackGetter)
	{
		this(stack, nbtKeyId, generateIdIfAbsent, null, stackGetter);
	}

	public ItemInventoryValidator(
			@Nullable ItemStack stack,
			@Nullable String nbtKeyId, boolean generateIdIfAbsent,
			@Nullable Predicate<Item> itemValidator, @Nullable Function<EntityPlayer, ItemStack> stackGetter)
	{
		this.stack = stack;
		this.nbtIdKey = nbtKeyId = StringUtils.defaultIfBlank(nbtKeyId, NBT_KEY_ID);
		this.itemValidator = itemValidator;
		this.stackGetter = stackGetter;

		if (generateIdIfAbsent && stack != null && !stack.isEmpty() && (itemValidator == null || itemValidator.test(stack.getItem())))
		{
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null)
			{
				nbt = new NBTTagCompound();
				stack.setTagCompound(nbt);
			}
			if (!nbt.hasKey(nbtKeyId))
				nbt.setInteger(nbtKeyId, RANDOM.nextInt());
		}
	}

	public boolean isItemInHotbar()
	{
		return this.itemInHotbar;
	}

	public void setItemInHotbar(boolean itemInHotbar)
	{
		this.itemInHotbar = itemInHotbar;
	}

	public int getSlotIndex()
	{
		return this.slotIndex;
	}

	public void setSlotIndex(int slotIndex)
	{
		this.slotIndex = slotIndex;
	}

	public void setSlotIndex(int slotIndex, boolean itemInHotbar)
	{
		this.setSlotIndex(slotIndex);
		this.setItemInHotbar(itemInHotbar);
	}

	public int getSlotId()
	{
		return this.slotId;
	}

	public void setSlotId(int slotId)
	{
		this.slotId = slotId;
	}

	public boolean tryGetSlotIdFromPlayerSlot(@Nonnull Slot slot)
	{
		if (this.slotIndex >= 0 && slot.inventory instanceof InventoryPlayer && slot.getSlotIndex() == this.slotIndex)
		{
			this.setSlotId(slot.slotNumber);
			return true;
		}
		return false;
	}

	public boolean canInteractWith(@Nonnull EntityPlayer player)
	{
		if (this.itemInHotbar && this.slotIndex >= 0 && this.slotIndex != player.inventory.currentItem)
			return false;
		if (this.stack != null && !this.stack.isEmpty())
		{
			ItemStack stackToCheck;
			if (this.stackGetter == null)
			{
				if (this.slotIndex < 0)
					return true;
				stackToCheck = player.inventory.getStackInSlot(this.slotIndex);
			}
			else
				stackToCheck = this.stackGetter.apply(player);
			return stackToCheck != null && (this.itemValidator == null || this.itemValidator.test(stackToCheck.getItem())) && this.isSameItemInventory(this.stack, stackToCheck);
		}
		return true;
	}

	public boolean canSlotClick(int slotId, int dragType, @Nonnull ClickType clickType, @Nonnull EntityPlayer player)
	{
		if (this.slotId >= 0 && slotId == this.slotId)
			return false;
		if (clickType == ClickType.SWAP && this.itemInHotbar && this.slotIndex >= 0 && dragType == this.slotIndex)
			return false;
		return this.canInteractWith(player);
	}

	private boolean isSameItemInventory(@Nullable ItemStack base, @Nullable ItemStack comparison)
	{
		if (base == null || comparison == null || base.isEmpty() || comparison.isEmpty())
			return false;

		if (base.getItem() != comparison.getItem())
			return false;

		if (!base.hasTagCompound() || !comparison.hasTagCompound())
			return false;

		String baseUID = base.getTagCompound().getString(this.nbtIdKey);
		String comparisonUID = comparison.getTagCompound().getString(this.nbtIdKey);
		return baseUID.equals(comparisonUID);
	}
}
