package ru.will.git.clientwg.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;

public final class Utils
{
	public static boolean isHostile(Entity entity)
	{
		return entity instanceof EntityMob || entity instanceof EntitySlime || entity instanceof EntityFlying || entity instanceof EntityDragon || entity instanceof EntityDragonPart;
	}

	public static boolean isNonHostile(Entity entity)
	{
		return !isHostile(entity) && entity instanceof EntityCreature;
	}
}
