package ru.will.git.draconicevolution;

import ru.will.git.reflectionmedic.util.ConvertUtils;
import ru.will.git.reflectionmedic.util.FastUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public final class ModUtils
{
	public static final GameProfile profile = new GameProfile(UUID.fromString("8680d4b1-e359-43d3-99d5-683785ec89b5"), "[DraconicEvolution]");
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
