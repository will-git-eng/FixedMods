package ru.will.git.thaumcraft;

import ru.will.git.reflectionmedic.util.ConvertUtils;
import ru.will.git.reflectionmedic.util.FastUtils;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public final class ModUtils
{
	public static final Logger LOGGER = LogManager.getLogger("ThaumCraft");
	public static final GameProfile profile = new GameProfile(UUID.fromString("745dd166-13e9-41db-999d-6af5bacba7fd"), "[ThaumCraft]");
	private static FakePlayer player = null;

	public static FakePlayer getModFake(World world)
	{
		if (player == null)
			player = FastUtils.getFake(world, profile);
		else
			player.worldObj = world;

		return player;
	}

	public static void init()
	{
		EventConfig.init();
		FixHandler.init();
	}

	public static void killEntity(Entity entity)
	{
		World world = entity.worldObj;
		world.removeEntity(entity);

		IChunkProvider provider = world.getChunkProvider();
		int chunkX = entity.chunkCoordX;
		int chunkZ = entity.chunkCoordZ;
		if (provider.chunkExists(chunkX, chunkZ))
		{
			Chunk chunk = provider.provideChunk(chunkX, chunkZ);
			if (chunk != null)
				chunk.removeEntity(entity);
		}
	}

	public static void stopPotionEffect(EntityLivingBase entity, Potion potion)
	{
		stopPotionEffect(entity.getActivePotionEffect(potion));
	}

	public static void stopPotionEffect(PotionEffect potionEffect)
	{
		if (potionEffect != null)
			ReflectionHelper.setPrivateValue(PotionEffect.class, potionEffect, 0, "field_76460_b", "duration");
	}

	public static boolean hasPermission(EntityPlayer player, String permission)
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

	public static boolean isValidStack(ItemStack stack)
	{
		return stack != null && stack.stackSize > 0;
	}
}
