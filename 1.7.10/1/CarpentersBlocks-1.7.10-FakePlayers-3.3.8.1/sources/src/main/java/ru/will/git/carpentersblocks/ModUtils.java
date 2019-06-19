package ru.will.git.carpentersblocks;

import java.util.UUID;

import ru.will.git.reflectionmedic.util.ConvertUtils;
import ru.will.git.reflectionmedic.util.FastUtils;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public final class ModUtils
{
	public static final GameProfile profile = new GameProfile(UUID.fromString("ff7a14e5-322e-4d19-82c9-0a68225236ea"), "[CarpentersBlocks]");
	private static FakePlayer player = null;

	public static final FakePlayer getModFake(World world)
	{
		if (player == null)
			player = FastUtils.getFake(world, profile);
		else
			player.worldObj = world;

		return player;
	}

	public static final boolean hasPermission(EntityPlayer player, String permission)
	{
		try
		{
			return ConvertUtils.toBukkitEntity(player).hasPermission(permission);
		}
		catch (Exception e)
		{
			return false;
		}
	}
}