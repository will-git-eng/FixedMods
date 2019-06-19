package ru.will.git.ic2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ITerraformingBPFakePlayer
{
	public abstract boolean terraform(EntityPlayer player, ItemStack item, World world, int x, int z, int yCoord);
}
