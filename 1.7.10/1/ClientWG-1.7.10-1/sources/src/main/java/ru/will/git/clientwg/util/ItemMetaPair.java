package ru.will.git.clientwg.util;

import com.google.common.base.Objects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class ItemMetaPair
{
	public static final int ALL_META = -1;
	public final Item item;
	public final int meta;

	public ItemMetaPair(Item item, int meta)
	{
		this.item = item;
		this.meta = meta;
	}

	public boolean isMatch(Item item, int meta)
	{
		return this.item == item && (this.meta == ALL_META || this.meta == meta);
	}

	public boolean isMatch(ItemStack stack)
	{
		return stack != null && stack.getItem() != null && this.isMatch(stack.getItem(), stack.getItemDamage());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		ItemMetaPair that = (ItemMetaPair) o;
		return this.meta == that.meta && Objects.equal(this.item, that.item);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.item, this.meta);
	}
}
