package ru.will.git.minefactoryreloaded;

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
	public static boolean safariNet = true;
	public static boolean plasticTankManually = false;
	public static boolean enableRocket = false;
	public static boolean fixInfiniteRocket = true;
	public static boolean networkFix = true;
	public static boolean enableBigSaplings = true;
	public static final Set<String> blockBreaker = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> blockPlacer = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");

	public static void init()
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("MineFactoryReloaded");
			String c = "general";
			safariNet = cfg.getBoolean("safariNet", "event", safariNet, "Ивенты для Сафари-сети");
			plasticTankManually = cfg.getBoolean("plasticTankManually", c, plasticTankManually, "Разрешить вручную заполнять Пластиковый резервуар.");
			enableRocket = cfg.getBoolean("enableRocket", c, enableRocket, "Разрешить использование ракет");
			fixInfiniteRocket = cfg.getBoolean("fixInfiniteRocket", c, fixInfiniteRocket, "Исправить возможность использования ракет без их расходования");
			networkFix = cfg.getBoolean("networkFix", c, networkFix, "Исправление уязвимостей с пакетами");
			enableBigSaplings = cfg.getBoolean("enableBigSaplings", c, enableBigSaplings, "Включить саженцы больших деревьев");
			readStringSet(cfg, "blockBreaker", c, "Чёрный список блоков для Разрушителя блоков", blockBreaker);
			readStringSet(cfg, "blockPlacer", c, "Чёрный список блоков для Установщика блоков", blockPlacer);
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static boolean inList(Set<String> blackList, Item item, int meta)
	{
		if (item instanceof ItemBlock)
			return inList(blackList, ((ItemBlock) item).field_150939_a, meta);

		return inList(blackList, getId(item), meta);
	}

	public static boolean inList(Set<String> blackList, Block block, int meta)
	{
		return inList(blackList, getId(block), meta);
	}

	private static boolean inList(Set<String> blackList, String id, int meta)
	{
		return id != null && (blackList.contains(id) || blackList.contains(id + ':' + meta));
	}

	private static void readStringSet(final Configuration cfg, final String name, final String category, final String comment, final Set<String> def)
	{
		final Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static Set<String> getStringSet(final Configuration cfg, final String name, final String category, final String comment, final Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[def.size()]));
	}

	private static Set<String> getStringSet(final Configuration cfg, final String name, final String category, final String comment, final String... def)
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
		init();
	}
}