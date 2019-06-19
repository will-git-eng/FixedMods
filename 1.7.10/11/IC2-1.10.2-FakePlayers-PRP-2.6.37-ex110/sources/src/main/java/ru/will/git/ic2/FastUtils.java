package ru.will.git.ic2;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;

public final class FastUtils
{
	public static final boolean isOnline(EntityPlayer player)
	{
		if (player instanceof FakePlayer)
			return true;

		for (EntityPlayer playerOnline : getServer().getPlayerList().getPlayerList())
			if (playerOnline.equals(player))
				return true;

		return false;
	}

	public static final FakePlayer getFake(World world, FakePlayer fake)
	{
		fake.worldObj = world == null ? getEntityWorld() : world;
		return fake;
	}

	public static final FakePlayer getFake(World world, GameProfile profile)
	{
		return getFake(world, FakePlayerFactory.get((WorldServer) (world == null ? getEntityWorld() : world), profile));
	}

	public static final EntityPlayer getLivingPlayer(EntityLivingBase entity, FakePlayer modFake)
	{
		return entity instanceof EntityPlayer ? (EntityPlayer) entity : getFake(entity == null ? null : entity.worldObj, modFake);
	}

	public static final EntityPlayer getLivingPlayer(EntityLivingBase entity, GameProfile modFakeProfile)
	{
		return entity instanceof EntityPlayer ? (EntityPlayer) entity : getFake(entity == null ? null : entity.worldObj, modFakeProfile);
	}

	public static final EntityPlayer getThrowerPlayer(EntityThrowable entity, FakePlayer modFake)
	{
		return getLivingPlayer(entity.getThrower(), modFake);
	}

	public static final EntityPlayer getThrowerPlayer(EntityThrowable entity, GameProfile modFakeProfile)
	{
		return getLivingPlayer(entity.getThrower(), modFakeProfile);
	}

	public static final EntityLivingBase getThrower(EntityThrowable entity, FakePlayer modFake)
	{
		EntityLivingBase thrower = entity.getThrower();
		return thrower != null ? thrower : getFake(entity == null ? null : entity.worldObj, modFake);
	}

	public static final EntityLivingBase getThrower(EntityThrowable entity, GameProfile modFakeProfile)
	{
		EntityLivingBase thrower = entity.getThrower();
		return thrower != null ? thrower : getFake(entity == null ? null : entity.worldObj, modFakeProfile);
	}

	private static final MinecraftServer getServer()
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}

	private static final World getEntityWorld()
	{
		return getServer().getEntityWorld();
	}
}