package ru.will.git.extrautilities;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class ItemStackWrapper
{
	private final ItemStack stack;

	public ItemStackWrapper(ItemStack stack)
	{
		this.stack = stack;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null || this.getClass() != obj.getClass())
			return false;

		ItemStackWrapper other = (ItemStackWrapper) obj;
		return ItemStack.areItemStacksEqual(this.stack, other.stack);
	}

	@Override
	public int hashCode()
	{
		if (this.stack == null)
			return 31;

		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.stack.getItem());
		builder.append(this.stack.getItemDamage());
		builder.append(this.stack.getTagCompound());
		builder.append(this.stack.stackSize);
		return builder.toHashCode();
	}
}
