package chylex.hee.mechanics.misc;

import chylex.hee.entity.mob.EntityMobHomelandEnderman;
import chylex.hee.entity.technical.EntityTechnicalBiomeInteraction;
import chylex.hee.system.util.ColorUtil;
import chylex.hee.world.structure.island.biome.interaction.BiomeInteractionEnchantedIsland;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class HomelandEndermen
{
	public static boolean isOvertakeHappening(EntityMobHomelandEnderman source)
	{
		return getOvertakeGroup(source) != -1L;
	}

	public static long getOvertakeGroup(EntityMobHomelandEnderman source)
	{
		AxisAlignedBB aabb = source.boundingBox.expand(260.0D, 128.0D, 260.0D);

		EntityTechnicalBiomeInteraction entity = EntityTechnicalBiomeInteraction.getEntity(source.worldObj, aabb, BiomeInteractionEnchantedIsland.InteractionOvertake.class, 1);
		if (entity != null)
			return ((BiomeInteractionEnchantedIsland.InteractionOvertake) entity.getInteraction()).groupId;
		    

		return -1L;
	}

	public static List<EntityMobHomelandEnderman> getAll(EntityMobHomelandEnderman source)
	{
		List<EntityMobHomelandEnderman> all = source.worldObj.getEntitiesWithinAABB(EntityMobHomelandEnderman.class, source.boundingBox.expand(260.0D, 128.0D, 260.0D));
		return all;
	}

	public static List<EntityMobHomelandEnderman> getByHomelandRole(EntityMobHomelandEnderman source, HomelandEndermen.HomelandRole role)
	{
		List<EntityMobHomelandEnderman> all = getAll(source);
		List<EntityMobHomelandEnderman> filtered = new ArrayList();

		for (EntityMobHomelandEnderman enderman : all)
		{
			if (enderman.getHomelandRole() == role)
			{
				filtered.add(enderman);
			}
		}

		return filtered;
	}

	public static List<EntityMobHomelandEnderman> getInSameGroup(EntityMobHomelandEnderman source)
	{
		List<EntityMobHomelandEnderman> all = getAll(source);
		List<EntityMobHomelandEnderman> filtered = new ArrayList();

		for (EntityMobHomelandEnderman enderman : all)
		{
			if (enderman.isInSameGroup(source))
			{
				filtered.add(enderman);
			}
		}

		return filtered;
	}

	public static List<EntityMobHomelandEnderman> getByGroupRole(EntityMobHomelandEnderman source, HomelandEndermen.OvertakeGroupRole role)
	{
		List<EntityMobHomelandEnderman> all = getAll(source);
		List<EntityMobHomelandEnderman> filtered = new ArrayList();

		for (EntityMobHomelandEnderman enderman : all)
		{
			if (enderman.isInSameGroup(source) && enderman.getGroupRole() == role)
			{
				filtered.add(enderman);
			}
		}

		return filtered;
	}

	public enum EndermanTask
	{
		NONE,
		RECRUIT_TO_GROUP,
		LISTEN_TO_RECRUITER,
		STROLL,
		WALK,
		COMMUNICATE,
		WAIT,
		GET_TNT
	}

	public enum HomelandRole
	{
		WORKER(227),
		ISLAND_LEADERS(58),
		GUARD(0),
		COLLECTOR(176),
		OVERWORLD_EXPLORER(141),
		BUSINESSMAN(335),
		INTELLIGENCE(275);

		public static final HomelandEndermen.HomelandRole[] values = values();
		public final float red;
		public final float green;
		public final float blue;

		public static HomelandEndermen.HomelandRole getRandomRole(Random rand)
		{
			HomelandEndermen.HomelandRole role = WORKER;
			if (rand.nextInt(10) == 0)
			{
				role = OVERWORLD_EXPLORER;
			}
			else if (rand.nextInt(7) == 0)
			{
				role = BUSINESSMAN;
			}
			else if (rand.nextInt(6) == 0)
			{
				role = COLLECTOR;
			}
			else if (rand.nextInt(5) == 0)
			{
				role = INTELLIGENCE;
			}
			else if (rand.nextInt(7) <= 2)
			{
				role = GUARD;
			}

			return role;
		}

		HomelandRole(int hue)
		{
			float[] col = ColorUtil.hsvToRgb((float) hue / 359.0F, 0.78F, 0.78F);
			this.red = col[0];
			this.green = col[1];
			this.blue = col[2];
		}
	}

	public enum OvertakeGroupRole
	{
		LEADER,
		CHAOSMAKER,
		FIGHTER,
		TELEPORTER;

		public static final HomelandEndermen.OvertakeGroupRole[] values = values();

		public static HomelandEndermen.OvertakeGroupRole getRandomMember(Random rand)
		{
			int r = rand.nextInt(20);
			return r < 12 ? FIGHTER : r < 17 ? TELEPORTER : CHAOSMAKER;
		}
	}
}
