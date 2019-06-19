package ru.will.git.immersiveengineering;

import ru.will.git.reflectionmedic.util.FastUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public final class ModUtils
{
	public static final GameProfile profile = new GameProfile(UUID.fromString("a61ca9cc-9d82-4b18-b04d-cf6302910ea5"), "[ImmersiveEngineering]");
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