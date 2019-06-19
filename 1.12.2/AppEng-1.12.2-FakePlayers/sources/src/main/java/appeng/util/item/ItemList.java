/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.util.item;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AESharedItemStack.Bounds;
import ru.will.git.ae.EventConfig;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class ItemList implements IItemList<IAEItemStack>
{
	private final NavigableMap<AESharedItemStack, IAEItemStack> records = new ConcurrentSkipListMap<>();

	    
	private final Map<AESharedItemStack, IAEItemStack> unorderedRecords = EventConfig.useHybridItemList ? new ConcurrentHashMap<>() : this.records;
	    

	@Override
	public void add(final IAEItemStack option)
	{
		if (option == null)
			return;

		final IAEItemStack st = this.unorderedRecords.get(((AEItemStack) option).getSharedStack());
		    

		if (st != null)
		{
			st.add(option);
			return;
		}

		final IAEItemStack opt = option.copy();

		this.putItemRecord(opt);
	}

	@Override
	public IAEItemStack findPrecise(final IAEItemStack itemStack)
	{
		if (itemStack == null)
			return null;

		return this.unorderedRecords.get(((AEItemStack) itemStack).getSharedStack());
		    
	}

	@Override
	public Collection<IAEItemStack> findFuzzy(final IAEItemStack filter, final FuzzyMode fuzzy)
	{
		if (filter == null)
			return Collections.emptyList();

		final AEItemStack ais = (AEItemStack) filter;

		return ais.getOre().map(or -> {
			if (or.getAEEquivalents().size() == 1)
			{
				final IAEItemStack is = or.getAEEquivalents().get(0);
				return this.findFuzzyDamage(is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE);
			}
			else
			{
				final Collection<IAEItemStack> output = new ArrayList<>();

				for (final IAEItemStack is : or.getAEEquivalents())
				{
					output.addAll(this.findFuzzyDamage(is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE));
				}

				return output;
			}
		}).orElse(this.findFuzzyDamage(ais, fuzzy, false));
	}

	@Override
	public boolean isEmpty()
	{
		return !this.iterator().hasNext();
	}

	@Override
	public void addStorage(final IAEItemStack option)
	{
		if (option == null)
			return;


		final IAEItemStack st = this.unorderedRecords.get(((AEItemStack) option).getSharedStack());
		    

		if (st != null)
		{
			st.incStackSize(option.getStackSize());
			return;
		}

		final IAEItemStack opt = option.copy();

		this.putItemRecord(opt);
	}


	@Override
	public void addCrafting(final IAEItemStack option)
	{
		if (option == null)
			return;

		final IAEItemStack st = this.unorderedRecords.get(((AEItemStack) option).getSharedStack());
		    

		if (st != null)
		{
			st.setCraftable(true);
			return;
		}

		final IAEItemStack opt = option.copy();
		opt.setStackSize(0);
		opt.setCraftable(true);

		this.putItemRecord(opt);
	}

	@Override
	public void addRequestable(final IAEItemStack option)
	{
		if (option == null)
			return;

		final IAEItemStack st = this.unorderedRecords.get(((AEItemStack) option).getSharedStack());
		    

		if (st != null)
		{
			st.setCountRequestable(st.getCountRequestable() + option.getCountRequestable());
			return;
		}

		final IAEItemStack opt = option.copy();
		opt.setStackSize(0);
		opt.setCraftable(false);
		opt.setCountRequestable(option.getCountRequestable());

		this.putItemRecord(opt);
	}

	@Override
	public IAEItemStack getFirstItem()
	{
		for (final IAEItemStack stackType : this)
		{
			return stackType;
		}

		return null;
	}

	@Override
	public int size()
	{
		return this.records.size();
	}

	@Override
	public Iterator<IAEItemStack> iterator()
	{
		    
		if (this.unorderedRecords != this.records)
			return new MeaningfulItemHybridIterator<>(this.records, this.unorderedRecords);
		    

		return new MeaningfulItemIterator<>(this.records.values().iterator());
	}

	@Override
	public void resetStatus()
	{
		for (final IAEItemStack i : this)
		{
			i.reset();
		}
	}

	private IAEItemStack putItemRecord(final IAEItemStack itemStack)
	{
		AESharedItemStack sharedStack = ((AEItemStack) itemStack).getSharedStack();

		    
		if (this.unorderedRecords != this.records)
			this.unorderedRecords.put(sharedStack, itemStack);
		    

		return this.records.put(sharedStack, itemStack);
	}

	private Collection<IAEItemStack> findFuzzyDamage(final IAEItemStack filter, final FuzzyMode fuzzy, final boolean ignoreMeta)
	{
		final AEItemStack itemStack = (AEItemStack) filter;
		final Bounds bounds = itemStack.getSharedStack().getBounds(fuzzy, ignoreMeta);
		Collection<IAEItemStack> values = this.records.subMap(bounds.lower(), true, bounds.upper(), true).descendingMap().values();

		    
		if (this.unorderedRecords != this.records)
			return Collections.unmodifiableCollection(values);
		    

		return values;
	}

	    
	private static final class MeaningfulItemHybridIterator<T extends IAEItemStack> implements Iterator<T>
	{

		private final Iterator<Map.Entry<AESharedItemStack, T>> parentPrimaryIterator;
		private final Map<AESharedItemStack, T> parentSecondary;
		private Map.Entry<AESharedItemStack, T> next;

		public MeaningfulItemHybridIterator(Map<AESharedItemStack, T> parentPrimary, Map<AESharedItemStack, T> parentSecondary)
		{

			this.parentPrimaryIterator = parentPrimary.entrySet().iterator();
			this.parentSecondary = parentSecondary;
		}

		@Override
		public boolean hasNext()
		{
			while (this.parentPrimaryIterator.hasNext())
			{
				this.next = this.parentPrimaryIterator.next();

				T primaryValue = this.next.getValue();
				if (primaryValue.isMeaningful())
					return true;

				this.parentPrimaryIterator.remove(); 
				T secondaryValue = this.parentSecondary.remove(this.next.getKey());

				if (primaryValue != secondaryValue)
					throw new IllegalStateException("ItemList collections has been desynchronized");
			}

			this.next = null;
			return false;
		}

		@Override
		public T next()
		{
			if (this.next == null)
				throw new NoSuchElementException();
			return this.next.getValue();
		}

		@Override
		public void remove()
		{
			if (this.next == null)
				throw new IllegalStateException();

			T primaryValue = this.next.getValue();
			this.parentPrimaryIterator.remove();
			T secondaryValue = this.parentSecondary.remove(this.next.getKey());

			if (primaryValue != secondaryValue)
				throw new IllegalStateException("ItemList collections has been desynchronized");
		}
	}
	    
}
