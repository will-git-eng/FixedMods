package ru.will.git.thaumcraft;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public final class ItemBlockList
{
	private static final char SEPARATOR = '@';
	private static final int ALL_META = -1;

	private final Set<String> rawSet = new HashSet<>();
	private final Map<Item, TIntSet> items = new HashMap<>();
	private final Map<Block, TIntSet> blocks = new HashMap<>();
	private boolean loaded = false;

	public boolean isEmpty()
	{
		return this.items.isEmpty() && this.blocks.isEmpty();
	}

	public void addRaw(Collection<String> strings)
	{
		this.loaded = false;
		this.rawSet.addAll(strings);
		this.items.clear();
		this.blocks.clear();
	}

	public boolean contains(ItemStack stack)
	{
		return stack != null && this.contains(stack.getItem(), stack.getItemDamage());
	}

	public boolean contains(Item item, int meta)
	{
		this.load();

		if (item instanceof ItemBlock && this.contains(((ItemBlock) item).field_150939_a, meta))
			return true;

		return contains(this.items, item, meta);
	}

	public boolean contains(Block block, int meta)
	{
		this.load();
		return contains(this.blocks, block, meta);
	}

	private void load()
	{
		if (!this.loaded)
		{
			this.loaded = true;

			FMLControlledNamespacedRegistry<Item> itemRegistry = GameData.getItemRegistry();
			FMLControlledNamespacedRegistry<Block> blockRegistry = GameData.getBlockRegistry();

			for (String s : this.rawSet)
			{
				s = s.trim();
				if (!s.isEmpty())
				{
					String[] parts = StringUtils.split(s, SEPARATOR);
					String name = parts[0];
					int meta = parts.length > 1 ? safeParseInt(parts[1]) : ALL_META;

					Item item = itemRegistry.getObject(name);
					if (item != null)
						put(this.items, item, meta);
					Block block = blockRegistry.getObject(name);
					if (block != null && block != Blocks.air)
						put(this.blocks, block, meta);
				}
			}
		}
	}

	private static <K> boolean put(Map<K, TIntSet> map, K key, int value)
	{
		TIntSet set = map.get(key);
		if (set == null)
			map.put(key, set = new TIntHashSet());
		return set.add(value);
	}

	private static <K> boolean contains(Map<K, TIntSet> map, K key, int value)
	{
		TIntSet set = map.get(key);
		return set != null && (set.contains(value) || set.contains(ALL_META));
	}

	private static int safeParseInt(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (Throwable throwable)
		{
			return ALL_META;
		}
	}
}
