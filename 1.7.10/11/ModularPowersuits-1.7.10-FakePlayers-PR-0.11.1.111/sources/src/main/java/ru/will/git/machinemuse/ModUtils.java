package ru.will.git.machinemuse;

import java.util.UUID;

import ru.will.git.reflectionmedic.util.FastUtils;
import com.mojang.authlib.GameProfile;

import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public final class ModUtils
{
	public static final GameProfile profile = new GameProfile(UUID.fromString("ea46161a-469a-4036-9869-aba90315824a"), "[MachineMuse]");
	private static FakePlayer player = null;

	public static final FakePlayer getModFake(World world)
	{
		if (player == null)
			player = FastUtils.getFake(world, profile);
		else
			player.worldObj = world;

		return player;
	}
}
