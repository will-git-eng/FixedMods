package chylex.hee.system.util;

import chylex.hee.system.logging.Stopwatch;
import ru.will.git.hee.ModUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public final class DragonUtil
{
	public static int portalEffectX;
	public static int portalEffectZ;
	public static final DecimalFormat formatOnePlace = new DecimalFormat("0.0");
	public static final DecimalFormat formatTwoPlaces = new DecimalFormat("0.00");
	private static final Pattern regexChatFormatting = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-OR]");
	private static final double[] rayX = { -1.0D, -1.0D, 1.0D, 1.0D };
	private static final double[] rayZ = { -1.0D, 1.0D, -1.0D, 1.0D };

	public static boolean canEntitySeePoint(EntityLivingBase entity, double x, double y, double z, double pointScale)
	{
		return canEntitySeePoint(entity, x, y, z, pointScale, false);
	}

	public static boolean canEntitySeePoint(EntityLivingBase entity, double x, double y, double z, double pointScale, boolean fromCenter)
	{
		Stopwatch.timeAverage("DragonUtil - canEntitySeePoint", 800);
		double px = entity.posX;
		double py = entity.posY;
		double pz = entity.posZ;
		double ry = (double) -entity.rotationYaw;
		double rp = (double) -entity.rotationPitch;
		if (!fromCenter)
		{
			px -= MathUtil.lendirx(1.5D, ry);
			pz -= MathUtil.lendiry(1.5D, ry);
		}

		int t1x = (int) (px + MathUtil.lendirx(600.0D, ry - 66.0D));
		int t1z = (int) (pz + MathUtil.lendiry(600.0D, ry - 66.0D));
		int t2x = (int) (px + MathUtil.lendirx(600.0D, ry + 66.0D));
		int t2z = (int) (pz + MathUtil.lendiry(600.0D, ry + 66.0D));
		boolean isXZInYaw = MathUtil.triangle((int) x, (int) z, (int) px, (int) pz, t1x, t1z, t2x, t2z);
		boolean isYInPitch;
		if ((Math.abs(rp) >= 70.0D || Math.abs(y - py) > 2.0D) && Math.abs(y - py) <= 2.0D)
		{
			int t3y = (int) (py + MathUtil.lendirx(28.0D, rp - 60.0D));
			int t4y = (int) (py + MathUtil.lendirx(28.0D, rp + 60.0D));
			isYInPitch = y >= (double) t3y && y <= (double) t4y;
		}
		else
			isYInPitch = true;

		boolean[] isBlockedArr = new boolean[8];

		for (int a = 0; a < 4; ++a)
		{
			for (int b = 0; b < 2; ++b)
			{
				MovingObjectPosition mop = entity.worldObj.rayTraceBlocks(Vec3.createVectorHelper(px, py + 0.11999999731779099D, pz), Vec3.createVectorHelper(x + rayX[a] * pointScale, y + (b == 0 ? -1.0D : 1.0D) * pointScale, z + rayZ[a] * pointScale));
				if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK)
				{
					isBlockedArr[a * 2 + b] = MathUtil.distance((double) mop.blockX + 0.5D - x, (double) mop.blockY + 0.5D - y, (double) mop.blockZ + 0.5D - z) > 0.1D && MathUtil.distance((double) mop.blockX + 0.5D - px, (double) mop.blockY + 0.62D - py, (double) mop.blockZ - pz) > 2.0D;
					if (isBlockedArr[a * 2 + b] && !entity.worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ).isOpaqueCube())
						isBlockedArr[a * 2 + b] = false;
				}
			}
		}

		boolean isBlocked = isBlockedArr[0] && isBlockedArr[1] && isBlockedArr[2] && isBlockedArr[3] && isBlockedArr[4] && isBlockedArr[5] && isBlockedArr[6] && isBlockedArr[7];
		Stopwatch.finish("DragonUtil - canEntitySeePoint");
		if (isBlocked)
			return false;
		else
			return isXZInYaw && isYInPitch;
	}

	public static float rotateSmoothly(float sourceAngle, float targetAngle, float amount)
	{
		while (sourceAngle < 0.0F)
		{
			sourceAngle += 360.0F;
		}

		while (sourceAngle >= 360.0F)
		{
			sourceAngle -= 360.0F;
		}

		float angleDifference = 180.0F - Math.abs(Math.abs(sourceAngle - targetAngle) - 180.0F);
		if (angleDifference <= amount)
			return targetAngle;
		else
		{
			float d = sourceAngle < 180.0F ? sourceAngle + 180.0F : sourceAngle - 180.0F;
			sourceAngle = sourceAngle + amount * (float) ((targetAngle <= d || targetAngle >= sourceAngle) && (sourceAngle >= 180.0F || targetAngle <= d && targetAngle >= sourceAngle) ? 1 : -1);
			return sourceAngle;
		}
	}

	public static <T extends Entity> T getClosestEntity(Entity source, List<? extends T> list)
	{
		double closestDist = Double.MAX_VALUE;
		T closestEntity = null;

		for (T entity : list)
		{
			double currentDist;
			if (!entity.isDead && entity != source && (currentDist = source.getDistanceSqToEntity(entity)) < closestDist)
			{
				closestDist = currentDist;
				closestEntity = entity;
			}
		}

		return closestEntity;
	}

	public static <T extends Entity> List<T> getClosestEntities(int maxAmount, Entity source, List<? extends T> list)
	{
		List<T> closestEntities = new ArrayList();
		Map<T, Float> entities = new HashMap();
		if (maxAmount <= 0)
			return closestEntities;
		else
		{
			for (T entity : list)
			{
				if (!entity.isDead && entity != source)
					entities.put(entity, source.getDistanceToEntity(entity));
			}

			for (Entry<T, Float> entry : CollectionUtil.sortMapByValueAsc(entities))
			{
				closestEntities.add(entry.getKey());
				--maxAmount;
				if (maxAmount <= 0)
					break;
			}

			return closestEntities;
		}
	}

	public static double[] getNormalizedVector(double vecX, double vecZ)
	{
		double len = Math.sqrt(vecX * vecX + vecZ * vecZ);
		return len == 0.0D ? new double[] { 0.0D, 0.0D } : new double[] { vecX / len, vecZ / len };
	}

	public static Vec3 getRandomVector(Random rand)
	{
		return Vec3.createVectorHelper(rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D, rand.nextDouble() - 0.5D).normalize();
	}

	public static <T> T[] getNonNullValues(T[] array)
	{
		if (array.length == 0)
			return array;
		else
		{
			int nonNull = 0;
			int cnt = 0;

			for (T t : array)
			{
				if (t != null)
					++nonNull;
			}

			T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), nonNull);

			for (T t : array)
			{
				if (t != null)
					newArray[cnt++] = t;
			}

			return newArray;
		}
	}

	public static int getTopBlockY(World world, Block block, int x, int z, int startY)
	{
		for (BlockPosM pos = new BlockPosM(x, startY, z); pos.y >= 0; --pos.y)
		{
			if (pos.getBlock(world) == block)
				return pos.y;
		}

		return -1;
	}

	public static int getTopBlockY(World world, Block block, int x, int z)
	{
		return getTopBlockY(world, block, x, z, 255);
	}

	public static void spawnXP(Entity entity, int amount)
	{
		int a = amount;

		while (a > 0)
		{
			int split = EntityXPOrb.getXPSplit(a);
			a -= split;
			entity.worldObj.spawnEntityInWorld(new EntityXPOrb(entity.worldObj, entity.posX, entity.posY, entity.posZ, a));
		}

	}

	public static void teleportToOverworld(EntityPlayerMP player)
	{
		player.mcServer.getConfigurationManager().transferPlayerToDimension(player, 0);


		player.playerNetServerHandler.playerEntity = ModUtils.respawnPlayer(player, 0, true);
		    
	}

	public static void createMobExplosion(Entity entity, double x, double y, double z, float strength, boolean fire)
	{
		entity.worldObj.newExplosion(entity, x, y, z, strength, fire, entity.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"));
	}

	public static void createMobExplosion(World world, double x, double y, double z, float strength, boolean fire)
	{
		world.newExplosion(null, x, y, z, strength, fire, world.getGameRules().getGameRuleBooleanValue("mobGriefing"));
	}

	public static String stripChatFormatting(String str)
	{
		return regexChatFormatting.matcher(str).replaceAll("");
	}

	public static int tryParse(String str, int def)
	{
		try
		{
			return Integer.parseInt(str);
		}
		catch (NumberFormatException var3)
		{
			return def;
		}
	}

	public static int getDayDifference(Calendar cal1, Calendar cal2)
	{
		return MathUtil.floor((double) Math.abs(cal1.getTimeInMillis() - cal2.getTimeInMillis()) / 8.64E7D);
	}

	public static boolean canAddOneItemTo(ItemStack is, ItemStack itemToAdd)
	{
		return is.isStackable() && is.getItem() == itemToAdd.getItem() && (!is.getHasSubtypes() || is.getItemDamage() == itemToAdd.getItemDamage()) && ItemStack.areItemStackTagsEqual(is, itemToAdd) && is.stackSize + 1 <= is.getMaxStackSize();
	}

	public static boolean checkSystemProperty(String key)
	{
		String str = System.getProperty("hee");
		return str != null && ArrayUtils.contains(str.split(","), key);
	}
}
