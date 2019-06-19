package ru.will.git.draconicevolution;

import ru.will.git.reflectionmedic.util.FastUtils;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;

import java.util.Set;

public final class EventConfig
{
	public static final Set<Class<? extends Entity>> teleporterBlackList = Sets.newHashSet();

	public static boolean enableReactorExplosion = false;
	public static boolean enableDraconicBowExplosion = false;
	public static boolean enableSpawnerWitcherSkeleton = true;
	public static boolean enablePlaceItemInWorld = false;
	public static boolean enableSpawnerUpgrades = true;
	public static boolean enableSpawner = true;
	public static boolean enableMagnetXp = true;
	public static boolean enableDraconiumBlockStackCharge = true;

	public static boolean extendedDragonHeartActivation = false;
	public static boolean energyPylonOptimize = true;

	public static int staffMaxRange = 11;
	public static int pickaxeMaxRange = 11;

	public static String placeItemInWorldPermission = "draconicevolution.placeitem";
	public static boolean teleporterSelfOnly = false;
	public static boolean teleporterCheckRegion = true;

	public static void load()
	{
		try
		{
			try
			{
				teleporterBlackList.add((Class<? extends Entity>) Class.forName("noppes.npcs.entity.EntityNPCInterface"));
			}
			catch (Throwable ignored)
			{
			}

			Configuration cfg = FastUtils.getConfig("DraconicEvolution");
			String c = Configuration.CATEGORY_GENERAL;
			readClassSet(cfg, "teleporterBlackList", c, "Чёрный список сущностей для Charm of Dislocation", teleporterBlackList, Entity.class);

			enableReactorExplosion = cfg.getBoolean("enableReactorExplosion", c, enableReactorExplosion, "Включить взрыв реактора");
			enableDraconicBowExplosion = cfg.getBoolean("enableDraconicBowExplosion", c, enableDraconicBowExplosion, "Включить взрыв стрел DraconicBow");
			enableSpawnerWitcherSkeleton = cfg.getBoolean("enableSpawnerWitcherSkeleton", c, enableSpawnerWitcherSkeleton, "Включить спавн скелетов-иссушителей в спавнере");
			enablePlaceItemInWorld = cfg.getBoolean("enablePlaceItemInWorld", c, enablePlaceItemInWorld, "Включить функцию \"Place item in world\"");
			enableSpawnerUpgrades = cfg.getBoolean("enableSpawnerUpgrades", c, enableSpawnerUpgrades, "Включить улучшение спавнера");
			enableSpawner = cfg.getBoolean("enableSpawner", c, enableSpawner, "Включить спавнер");
			enableMagnetXp = cfg.getBoolean("enableMagnetXp", c, enableMagnetXp, "Включить притягивание опыта магнитом");
			enableDraconiumBlockStackCharge = cfg.getBoolean("enableDraconiumBlockStackCharge", c, enableDraconiumBlockStackCharge, "Включить возможность заряда стаков Draconium Block");

			extendedDragonHeartActivation = cfg.getBoolean("extendedDragonHeartActivation", c, extendedDragonHeartActivation, "Расширенная проверка наличия взрыва для Dragon Heart");
			energyPylonOptimize = cfg.getBoolean("energyPylonOptimize", c, energyPylonOptimize, "Оптимизация Energy Pylon");

			staffMaxRange = cfg.getInt("staffMaxRange", c, staffMaxRange, 0, Integer.MAX_VALUE, "Максимальный радиус разрушения посохом");
			pickaxeMaxRange = cfg.getInt("pickaxeMaxRange", c, pickaxeMaxRange, 0, Integer.MAX_VALUE, "Максимальный радиус разрушения киркой");

			placeItemInWorldPermission = cfg.getString("placeItemInWorldPermission", c, placeItemInWorldPermission, "Разрешение ставить предметы в виде блока");
			teleporterSelfOnly = cfg.getBoolean("teleporterSelfOnly", c, teleporterSelfOnly, "Charm of Dislocation может перемещать только игрока");
			teleporterCheckRegion = cfg.getBoolean("teleporterCheckRegion", c, teleporterCheckRegion, "Проверка приватов при телепортации");

			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static boolean inList(Set<String> list, Item item, int meta)
	{
		if (item instanceof ItemBlock)
			return inList(list, ((ItemBlock) item).field_150939_a, meta);

		return inList(list, getId(item), meta);
	}

	public static boolean inList(Set<String> list, Block block, int meta)
	{
		return inList(list, getId(block), meta);
	}

	private static boolean inList(Set<String> list, String id, int meta)
	{
		return id != null && (list.contains(id) || list.contains(id + ':' + meta));
	}

	public static <T> boolean inList(Set<Class<? extends T>> blackList, Class<? extends T> clazz)
	{
		if (clazz == null)
			return false;
		for (Class<? extends T> classInList : blackList)
		{
			if (classInList.isAssignableFrom(clazz))
				return true;
		}
		return false;
	}

	private static <T> void readClassSet(Configuration cfg, String name, String category, String comment, Set<Class<? extends T>> def, Class<? extends T> rootClass)
	{
		Set<String> names = Sets.newHashSet();
		for (Class<?> clazz : def)
		{
			names.add(clazz.getName());
		}
		readStringSet(cfg, name, category, comment, names);

		def.clear();
		for (String className : names)
		{
			try
			{
				Class<?> clazz = Class.forName(className);
				if (rootClass.isAssignableFrom(clazz))
					def.add((Class<T>) clazz);
			}
			catch (ClassNotFoundException ignored)
			{
			}
		}
	}

	private static void readStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static Set<String> getStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[def.size()]));
	}

	private static Set<String> getStringSet(Configuration cfg, String name, String category, String comment, String... def)
	{
		return Sets.newHashSet(cfg.getStringList(name, category, def, comment));
	}

	private static String getId(Item item)
	{
		return GameData.getItemRegistry().getNameForObject(item);
	}

	private static String getId(Block block)
	{
		return GameData.getBlockRegistry().getNameForObject(block);
	}

	static
	{
		load();
	}
}
