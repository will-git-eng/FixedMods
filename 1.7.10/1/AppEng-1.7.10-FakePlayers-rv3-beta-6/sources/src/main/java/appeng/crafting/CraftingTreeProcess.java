/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.crafting;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.Platform;
import ru.will.git.ae.EventConfig;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CraftingTreeProcess
{

	private final CraftingTreeNode parent;
	final ICraftingPatternDetails details;
	private final CraftingJob job;
	private final Map<CraftingTreeNode, Long> nodes = new HashMap<CraftingTreeNode, Long>();
	private final int depth;
	boolean possible = true;
	private World world;
	private long crafts = 0;
	private boolean containerItems;
	private boolean limitQty;
	private boolean fullSimulation;
	private long bytes = 0;

	    
	private static void firePlayerCraftingEvent(EntityPlayer player, ItemStack crafted, IInventory craftMatrix)
	{
		if (player != null && crafted != null && craftMatrix != null)
			FMLCommonHandler.instance().firePlayerCraftingEvent(player, crafted, craftMatrix);
	}
	    

	public CraftingTreeProcess(final ICraftingGrid cc, final CraftingJob job, final ICraftingPatternDetails details, final CraftingTreeNode craftingTreeNode, final int depth)
	{
		this.parent = craftingTreeNode;
		this.details = details;
		this.job = job;
		this.depth = depth;
		final World world = job.getWorld();

		if (details.isCraftable())
		{
			final IAEItemStack[] list = details.getInputs();

			final InventoryCrafting ic = new InventoryCrafting(new ContainerNull(), 3, 3);
			final IAEItemStack[] is = details.getInputs();
			for (int x = 0; x < ic.getSizeInventory(); x++)
			{
				ic.setInventorySlotContents(x, is[x] == null ? null : is[x].getItemStack());
			}

			firePlayerCraftingEvent(Platform.getPlayer((WorldServer) world), details.getOutput(ic, world), ic);
			    

			for (int x = 0; x < ic.getSizeInventory(); x++)
			{
				final ItemStack g = ic.getStackInSlot(x);
				if (g != null && g.stackSize > 1)
					this.fullSimulation = true;
			}

			for (final IAEItemStack part : details.getCondensedInputs())
			{
				final ItemStack g = part.getItemStack();

				boolean isAnInput = false;
				for (final IAEItemStack a : details.getCondensedOutputs())
				{
					if (g != null && a != null && a.equals(g))
						isAnInput = true;
				}

				if (isAnInput)
					this.limitQty = true;

				if (g.getItem().hasContainerItem(g))
				{
					    
					if (!EventConfig.inList(EventConfig.autoCraftForceCheckList, g))
						    
						this.containerItems = true;

					this.limitQty = true;
				}
			}

			final boolean complicated = false;

			// this is minor different then below, this slot uses the pattern, but kinda fudges it.
			if (this.containerItems || complicated)
				for (int x = 0; x < list.length; x++)
				{
					final IAEItemStack part = list[x];
					if (part != null)
						this.nodes.put(new CraftingTreeNode(cc, job, part.copy(), this, x, depth + 1), part.getStackSize());
				}
			else
				for (final IAEItemStack part : details.getCondensedInputs())
				{
					for (int x = 0; x < list.length; x++)
					{
						final IAEItemStack comparePart = list[x];
						if (part != null && part.equals(comparePart))
						{
							// use the first slot...
							this.nodes.put(new CraftingTreeNode(cc, job, part.copy(), this, x, depth + 1), part.getStackSize());
							break;
						}
					}
				}
		}
		else
		{
			for (final IAEItemStack part : details.getCondensedInputs())
			{
				final ItemStack g = part.getItemStack();

				boolean isAnInput = false;
				for (final IAEItemStack a : details.getCondensedOutputs())
				{
					if (g != null && a != null && a.equals(g))
						isAnInput = true;
				}

				if (isAnInput)
					this.limitQty = true;
			}

			for (final IAEItemStack part : details.getCondensedInputs())
			{
				this.nodes.put(new CraftingTreeNode(cc, job, part.copy(), this, -1, depth + 1), part.getStackSize());
			}
		}
	}

	boolean notRecursive(final ICraftingPatternDetails details)
	{
		return this.parent == null || this.parent.notRecursive(details);
	}

	long getTimes(final long remaining, final long stackSize)
	{
		if (this.limitQty || this.fullSimulation)
			return 1;
		return remaining / stackSize + (remaining % stackSize != 0 ? 1 : 0);
	}

	void request(final MECraftingInventory inv, final long i, final BaseActionSource src)
			throws CraftBranchFailure, InterruptedException
	{
		this.job.handlePausing();

		// request and remove inputs...
		if (this.fullSimulation)
		{
			final InventoryCrafting ic = new InventoryCrafting(new ContainerNull(), 3, 3);

			for (final Entry<CraftingTreeNode, Long> entry : this.nodes.entrySet())
			{
				final IAEItemStack item = entry.getKey().getStack(entry.getValue());
				final IAEItemStack stack = entry.getKey().request(inv, item.getStackSize(), src);

				ic.setInventorySlotContents(entry.getKey().getSlot(), stack.getItemStack());
			}

			firePlayerCraftingEvent(Platform.getPlayer((WorldServer) this.world), this.details.getOutput(ic, this.world), ic);
			    

			for (int x = 0; x < ic.getSizeInventory(); x++)
			{
				ItemStack is = ic.getStackInSlot(x);
				is = Platform.getContainerItem(is);

				final IAEItemStack o = AEApi.instance().storage().createItemStack(is);
				if (o != null)
				{
					this.bytes++;
					inv.injectItems(o, Actionable.MODULATE, src);
				}
			}
		}
		else
			for (final Entry<CraftingTreeNode, Long> entry : this.nodes.entrySet())
			{
				final IAEItemStack item = entry.getKey().getStack(entry.getValue());
				final IAEItemStack stack = entry.getKey().request(inv, item.getStackSize() * i, src);

				if (this.containerItems)
				{
					final ItemStack is = Platform.getContainerItem(stack.getItemStack());
					final IAEItemStack o = AEApi.instance().storage().createItemStack(is);
					if (o != null)
					{
						this.bytes++;
						inv.injectItems(o, Actionable.MODULATE, src);
					}
				}
			}

		// assume its possible.

		// add crafting results..
		for (final IAEItemStack out : this.details.getCondensedOutputs())
		{
			final IAEItemStack o = out.copy();
			o.setStackSize(o.getStackSize() * i);
			inv.injectItems(o, Actionable.MODULATE, src);
		}

		this.crafts += i;
	}

	void dive(final CraftingJob job)
	{
		job.addTask(this.getAmountCrafted(this.parent.getStack(1)), this.crafts, this.details, this.depth);
		for (final CraftingTreeNode pro : this.nodes.keySet())
		{
			pro.dive(job);
		}

		job.addBytes(8 + this.crafts + this.bytes);
	}

	IAEItemStack getAmountCrafted(IAEItemStack what2)
	{
		for (final IAEItemStack is : this.details.getCondensedOutputs())
		{
			if (is.equals(what2))
			{
				what2 = what2.copy();
				what2.setStackSize(is.getStackSize());
				return what2;
			}
		}

		// more fuzzy!
		for (final IAEItemStack is : this.details.getCondensedOutputs())
		{
			if (is.getItem() == what2.getItem() && (is.getItem().isDamageable() || is.getItemDamage() == what2.getItemDamage()))
			{
				what2 = is.copy();
				what2.setStackSize(is.getStackSize());
				return what2;
			}
		}

		throw new IllegalStateException("Crafting Tree construction failed.");
	}

	void setSimulate()
	{
		this.crafts = 0;
		this.bytes = 0;

		for (final CraftingTreeNode pro : this.nodes.keySet())
		{
			pro.setSimulate();
		}
	}

	void setJob(final MECraftingInventory storage, final CraftingCPUCluster craftingCPUCluster, final BaseActionSource src)
			throws CraftBranchFailure
	{
		craftingCPUCluster.addCrafting(this.details, this.crafts);

		for (final CraftingTreeNode pro : this.nodes.keySet())
		{
			pro.setJob(storage, craftingCPUCluster, src);
		}
	}

	void getPlan(final IItemList<IAEItemStack> plan)
	{
		for (IAEItemStack i : this.details.getOutputs())
		{
			i = i.copy();
			i.setCountRequestable(i.getStackSize() * this.crafts);
			plan.addRequestable(i);
		}

		for (final CraftingTreeNode pro : this.nodes.keySet())
		{
			pro.getPlan(plan);
		}
	}
}
