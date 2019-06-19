package ru.will.git.tconstruct;

import ru.will.git.reflectionmedic.util.FastUtils;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import java.util.Set;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

public final class EventConfig
{
	public static final Set<String> travelBeltBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> pickaxeRMBBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static boolean enableCraftingStationDoubleChest = true;
	public static boolean enableRapierPierceArmor = true;
	public static boolean enableBeheading = false;
	public static int maxWeaponMods = -1;
	public static int maxCreativeMods = -1;

	public static boolean smelteryLogicUpdateInSameTick = true;
	public static boolean smelteryInstantDeath = false;

	public static void init()
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("TConstruct");
			String c = CATEGORY_GENERAL;
			readStringSet(cfg, "travelBeltBlackList", c, "Чёрный список предметов для Пояса путешественника", travelBeltBlackList);
			readStringSet(cfg, "pickaxeRMBBlackList", c, "Чёрный список предметов для Кирки", pickaxeRMBBlackList);
			enableCraftingStationDoubleChest = cfg.getBoolean("enableCraftingStationDoubleChest", c, enableCraftingStationDoubleChest, "Включить поддержку двойных сундуков для Crafting Station");
			enableRapierPierceArmor = cfg.getBoolean("enableRapierPierceArmor", c, enableRapierPierceArmor, "Включить игнорирование брони Рапирами");
			enableBeheading = cfg.getBoolean("enableBeheading", c, enableBeheading, "Включить модификатор Усекновение");
			maxWeaponMods = cfg.getInt("maxWeaponMods", c, maxWeaponMods, -1, Integer.MAX_VALUE, "Максимальное количество модификаторов для оружия (-1 - неограничено)");
			maxCreativeMods = cfg.getInt("maxCreativeMods", c, maxCreativeMods, -1, Integer.MAX_VALUE, "Максимальное количество Творческих модификаторов для инструментов и оружия (-1 - неограничено)");
			smelteryLogicUpdateInSameTick = cfg.getBoolean("smelteryLogicUpdateInSameTick", c, smelteryLogicUpdateInSameTick, "Разрешить обновлять плавильню несколько раз в тик");
			smelteryInstantDeath = cfg.getBoolean("smelteryInstantDeath", c, smelteryInstantDeath, "Мгновенная смерть мобов в плавильне (защита от дюпа с лечением)");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static boolean inList(Set<String> list, ItemStack stack)
	{
		return stack != null && inList(list, stack.getItem(), stack.getItemDamage());
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

	private static void readStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static Set<String> getStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[0]));
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
		init();
	}
}
