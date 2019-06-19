package ru.will.git.ic2;

import java.util.UUID;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.reflectionmedic.util.FastUtils;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public final class ModUtils
{
	public static final GameProfile profile = new GameProfile(UUID.fromString("6c788982-d6ca-11e4-b9d6-1681e6b88ec1"), "[IC2]");
	private static FakePlayer player = null;

	public static final FakePlayer getModFake(World world)
	{
		if (player == null)
			player = FastUtils.getFake(world, profile);
		else
			player.worldObj = world;

		return player;
	}

	public static final boolean setBlock(EntityPlayer player, World world, int x, int y, int z, Block block)
	{
		return !EventUtils.cantBreak(player, x, y, z) && world.setBlock(x, y, z, block);
	}

	public static final boolean setBlock(EntityPlayer player, World world, int x, int y, int z, Block block, int meta, int flags)
	{
		return !EventUtils.cantBreak(player, x, y, z) && world.setBlock(x, y, z, block, meta, flags);
	}
}
