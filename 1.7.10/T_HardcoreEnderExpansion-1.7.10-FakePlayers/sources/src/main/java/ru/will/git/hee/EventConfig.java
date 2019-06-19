package ru.will.git.hee;

import ru.will.git.reflectionmedic.util.FastUtils;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import java.util.Set;

public final class EventConfig
{
	public static boolean transferenceGemSelfOnly = false;
	public static boolean instabilityOrbGrief = true;
	public static boolean instabilityOrbSpawnMobs = true;
	public static boolean instabilityOrbSpawnItems = true;
	public static boolean energyClusterBlockGrief = true;
	public static boolean babyEndermanTheft = true;
	public static boolean mobGrief = true;

	static
	{
		init();
	}

	public static void init()
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("HEE");
			String c = Configuration.CATEGORY_GENERAL;

			transferenceGemSelfOnly = cfg.getBoolean("transferenceGemSelfOnly", c, transferenceGemSelfOnly, "Самоцвет переноса может переносить только игрока");
			instabilityOrbGrief = cfg.getBoolean("instabilityOrbGrief", c, instabilityOrbGrief, "Разрушение блоков Нестабильной сферой");
			instabilityOrbSpawnMobs = cfg.getBoolean("instabilityOrbSpawnMobs", c, instabilityOrbSpawnMobs, "Спавн мобов Нестабильной сферой");
			instabilityOrbSpawnItems = cfg.getBoolean("instabilityOrbSpawnItems", c, instabilityOrbSpawnItems, "Спавн предметов Нестабильной сферой");
			energyClusterBlockGrief = cfg.getBoolean("energyClusterBlockGrief", c, energyClusterBlockGrief, "Разрушение блоков Скоплением энергии");
			babyEndermanTheft = cfg.getBoolean("babyEndermanTheft", c, babyEndermanTheft, "Воровство предметов Маленьким странником Края");
			mobGrief = cfg.getBoolean("mobGrief", c, mobGrief, "Гриферство мобов HEE");

			cfg.save();
		}
		catch (final Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static boolean inList(Set<String> list, ItemStack stack)
	{
		return inList(list, stack.getItem(), stack.getItemDamage());
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

	private static void readStringSet(final Configuration cfg, final String name, final String category, final String comment, final Set<String> def)
	{
		final Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static Set<String> getStringSet(final Configuration cfg, final String name, final String category, final String comment, final Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[0]));
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
}
