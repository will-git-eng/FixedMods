package ru.will.git.thaumicenergistics;

import net.minecraft.item.ItemStack;

public final class ModUtils
{
	public static final boolean isMatch(ItemStack s1, ItemStack s2)
	{
		if (s1 == s2)
			return true;
		if (s1 == null || s2 == null)
			return false;
		return s1.isItemEqual(s2) && ItemStack.areItemStackTagsEqual(s1, s2);
	}
}
