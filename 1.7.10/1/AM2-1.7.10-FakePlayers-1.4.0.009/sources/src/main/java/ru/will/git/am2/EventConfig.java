package ru.will.git.am2;

import java.util.Set;

import ru.will.git.reflectionmedic.util.FastUtils;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static final Set<String> appropriationBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> soulboundBlackList = Sets.newHashSet("ThaumicTinkerer:ichorPouch");
	public static boolean denyStackSpell = true;
	public static boolean enableEarthArmorKnockback = false;
	public static boolean enableSoulbound = false;

	static
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("AM2");
			readStringSet(cfg, "appropriationBlackList", "blacklists", "Чёрный список предметов для Appropriation", appropriationBlackList);
			readStringSet(cfg, "soulboundBlackList", "blacklists", "Чёрный список предметов для зачарования Soulbound", soulboundBlackList);
			denyStackSpell = cfg.getBoolean("denyStackSpell", "general", denyStackSpell, "Запретить стакать свитки заклинаний (защита от дюпа)");
			enableEarthArmorKnockback = cfg.getBoolean("enableEarthArmorKnockback", "general", enableEarthArmorKnockback, "Включить отбрасывание Доспехом земли");
			enableSoulbound = cfg.getBoolean("enableSoulbound", "general", enableSoulbound, "Включить зачарование Soulbound");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static final boolean inList(Set<String> list, ItemStack stack)
	{
		return stack != null && inList(list, stack.getItem(), stack.getItemDamage());
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
}
