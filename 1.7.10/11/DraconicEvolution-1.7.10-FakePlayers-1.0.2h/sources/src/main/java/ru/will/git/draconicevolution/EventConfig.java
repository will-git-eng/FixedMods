package ru.will.git.draconicevolution;

import ru.will.git.reflectionmedic.util.FastUtils;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;

import java.util.Set;

public final class EventConfig
{
	public static boolean enableReactorExplosion = false;
	public static boolean enableDraconicBowExplosion = false;
	public static boolean enableSpawnerWitcherSkeleton = false;
	public static boolean enablePlaceItemInWorld = false;
	public static boolean enableSpawnerUpgrades = true;
	public static boolean enableSpawner = true;
	public static boolean enableMagnetXp = true;

	public static int staffMaxRange = 11;
	public static int pickaxeMaxRange = 11;

	public static String placeItemInWorldPermission = "draconicevolution.placeitem";

	public static void load()
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("DraconicEvolution");
			String c = Configuration.CATEGORY_GENERAL;

			enableReactorExplosion = cfg.getBoolean("enableReactorExplosion", c, enableReactorExplosion, "Включить взрыв реактора");
			enableDraconicBowExplosion = cfg.getBoolean("enableDraconicBowExplosion", c, enableDraconicBowExplosion, "Включить взрыв стрел DraconicBow");
			enableSpawnerWitcherSkeleton = cfg.getBoolean("enableSpawnerWitcherSkeleton", c, enableSpawnerWitcherSkeleton, "Включить спавн скелетов-иссушителей в спавнере");
			enablePlaceItemInWorld = cfg.getBoolean("enablePlaceItemInWorld", c, enablePlaceItemInWorld, "Включить функцию \"Place item in world\"");
			enableSpawnerUpgrades = cfg.getBoolean("enableSpawnerUpgrades", c, enableSpawnerUpgrades, "Включить улучшение спавнера");
			enableSpawner = cfg.getBoolean("enableSpawner", c, enableSpawner, "Включить спавнер");
			enableMagnetXp = cfg.getBoolean("enableMagnetXp", c, enableMagnetXp, "Включить притягивание опыта магнитом");

			staffMaxRange = cfg.getInt("staffMaxRange", c, staffMaxRange, 0, Integer.MAX_VALUE, "Максимальный радиус разрушения посохом");
			pickaxeMaxRange = cfg.getInt("pickaxeMaxRange", c, pickaxeMaxRange, 0, Integer.MAX_VALUE, "Максимальный радиус разрушения киркой");

			placeItemInWorldPermission = cfg.getString("placeItemInWorldPermission", c, placeItemInWorldPermission, "Разрешение ставить предметы в виде блока");

			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static final boolean inList(Set<String> list, Item item, int meta)
	{
		if (item instanceof ItemBlock)
			return inList(list, ((ItemBlock) item).field_150939_a, meta);

		return inList(list, getId(item), meta);
	}

	public static final boolean inList(Set<String> list, Block block, int meta)
	{
		return inList(list, getId(block), meta);
	}

	private static final boolean inList(Set<String> list, String id, int meta)
	{
		return id != null && (list.contains(id) || list.contains(id + ':' + meta));
	}

	private static final void readStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static final Set<String> getStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[def.size()]));
	}

	private static final Set<String> getStringSet(Configuration cfg, String name, String category, String comment, String... def)
	{
		return Sets.newHashSet(cfg.getStringList(name, category, def, comment));
	}

	private static final String getId(Item item)
	{
		return GameData.getItemRegistry().getNameForObject(item);
	}

	private static final String getId(Block block)
	{
		return GameData.getBlockRegistry().getNameForObject(block);
	}

	static
	{
		load();
	}
}
