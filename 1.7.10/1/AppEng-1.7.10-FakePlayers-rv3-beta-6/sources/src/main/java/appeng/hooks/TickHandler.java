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

package appeng.hooks;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.CableRenderMode;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.CommonHelper;
import appeng.core.sync.packets.PacketPaintedEntity;
import appeng.crafting.CraftingJob;
import appeng.entity.EntityFloatingItem;
import appeng.me.Grid;
import appeng.me.NetworkList;
import appeng.parts.AEBasePart;
import appeng.parts.automation.PartExportBus;
import appeng.parts.automation.PartImportBus;
import appeng.parts.automation.PartSharedItemBus;
import appeng.parts.misc.PartStorageBus;
import appeng.tile.AEBaseTile;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import ru.will.git.ae.BusUtils;
import ru.will.git.ae.EventConfig;
import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.Type;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TickHandler
{

	public static final TickHandler INSTANCE = new TickHandler();
	private final Queue<IWorldCallable<?>> serverQueue = new LinkedList<IWorldCallable<?>>();
	private final Multimap<World, CraftingJob> craftingJobs = LinkedListMultimap.create();
	private final WeakHashMap<World, Queue<IWorldCallable<?>>> callQueue = new WeakHashMap<World, Queue<IWorldCallable<?>>>();
	private final HandlerRep server = new HandlerRep();
	private final HandlerRep client = new HandlerRep();
	private final HashMap<Integer, PlayerColor> cliPlayerColors = new HashMap<Integer, PlayerColor>();
	private final HashMap<Integer, PlayerColor> srvPlayerColors = new HashMap<Integer, PlayerColor>();
	private CableRenderMode crm = CableRenderMode.Standard;

	public HashMap<Integer, PlayerColor> getPlayerColors()
	{
		if (Platform.isServer())
		{
			return this.srvPlayerColors;
		}
		return this.cliPlayerColors;
	}

	public void addCallable(final World w, final IWorldCallable<?> c)
	{
		if (w == null)
		{
			this.serverQueue.add(c);
		}
		else
		{
			Queue<IWorldCallable<?>> queue = this.callQueue.get(w);

			if (queue == null)
			{
				queue = new LinkedList<IWorldCallable<?>>();
				this.callQueue.put(w, queue);
			}

			queue.add(c);
		}
	}

	public void addInit(final AEBaseTile tile)
	{
		if (Platform.isServer()) // for no there is no reason to care about this on the client...
		{
			this.getRepo().tiles.add(tile);
		}
	}

	private HandlerRep getRepo()
	{
		if (Platform.isServer())
		{
			return this.server;
		}
		return this.client;
	}

	public void addNetwork(final Grid grid)
	{
		if (Platform.isServer()) // for no there is no reason to care about this on the client...
		{
			this.getRepo().networks.add(grid);
		}
	}

	public void removeNetwork(final Grid grid)
	{
		if (Platform.isServer()) // for no there is no reason to care about this on the client...
		{
			this.getRepo().networks.remove(grid);
		}
	}

	public Iterable<Grid> getGridList()
	{
		return this.getRepo().networks;
	}

	public void shutdown()
	{
		this.getRepo().clear();
	}

	@SubscribeEvent
	public void unloadWorld(final WorldEvent.Unload ev)
	{
		if (Platform.isServer()) // for no there is no reason to care about this on the client...
		{
			final LinkedList<IGridNode> toDestroy = new LinkedList<IGridNode>();

			for (final Grid g : this.getRepo().networks)
			{
				for (final IGridNode n : g.getNodes())
				{
					if (n.getWorld() == ev.world)
					{
						toDestroy.add(n);
					}
				}
			}

			for (final IGridNode n : toDestroy)
			{
				n.destroy();
			}
		}
	}

	@SubscribeEvent
	public void onChunkLoad(final ChunkEvent.Load load)
	{
		for (final Object te : load.getChunk().chunkTileEntityMap.values())
		{
			if (te instanceof AEBaseTile)
			{
				((AEBaseTile) te).onChunkLoad();
			}
		}
	}

	    
	private static final Class[] CLASSES_IMPORT_EXPORT_BUS = { PartImportBus.class, PartExportBus.class };

	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event)
	{
		    
		if (!EventConfig.experimentalChunkDupeFix)
			return;
		    

		Chunk chunk = event.getChunk();
		int chunkX = chunk.xPosition;
		int chunkZ = chunk.zPosition;
		for (Grid grid : this.getRepo().networks)
		{
			checkStorageBus(chunkX, chunkZ, grid);
			checkImportExportBus(chunkX, chunkZ, grid);
		}
	}

	private static void checkStorageBus(int chunkX, int chunkZ, Grid grid)
	{
		IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
		if (storageGrid != null)
			for (IGridNode node : getMachines(grid, PartStorageBus.class))
			{
				IGridHost host = node.getMachine();
				if (host instanceof PartStorageBus)
				{
					PartStorageBus bus = (PartStorageBus) host;
					if (needUnregister(bus, chunkX, chunkZ))
						storageGrid.unregisterCellProvider(bus);
				}
			}
	}

	private static void checkImportExportBus(int chunkX, int chunkZ, Grid grid)
	{
		for (IGridNode node : getMachines(grid, (Class<? extends IGridHost>[]) CLASSES_IMPORT_EXPORT_BUS))
		{
			IGridHost host = node.getMachine();
			if (host instanceof PartSharedItemBus)
			{
				PartSharedItemBus bus = (PartSharedItemBus) host;
				if (needUnregister(bus, chunkX, chunkZ))
					bus.resetCache();
			}
		}
	}

	private static Collection<IGridNode> getMachines(IGrid grid, Class<? extends IGridHost> clazz)
	{
		return grid instanceof Grid ? ((Grid) grid).getMachinesCopy(clazz) : Lists.newArrayList(grid.getMachines(clazz));
	}

	private static Collection<IGridNode> getMachines(IGrid grid, Class<? extends IGridHost>... classes)
	{
		List<IGridNode> list = new ArrayList<IGridNode>();
		boolean isGrid = grid instanceof Grid;
		for (Class<? extends IGridHost> clazz : classes)
		{
			if (isGrid)
				((Grid) grid).addMachinesTo(clazz, list);
			else
				for (IGridNode node : grid.getMachines(clazz))
				{
					list.add(node);
				}
		}
		return list;
	}

	private static boolean needUnregister(AEBasePart part, int chunkX, int chunkZ)
	{
		TileEntity tile = part.getTile();
		if (tile != null)
		{
			int busChunkX = tile.xCoord >> 4;
			int busChunkZ = tile.zCoord >> 4;

			if (busChunkX == chunkX && busChunkZ == chunkZ)
				return true;
			else if (Math.abs(busChunkX - chunkX) + Math.abs(busChunkZ - chunkZ) == 1)
			{
				ForgeDirection side = part.getSide();
				int targetX = tile.xCoord + side.offsetX;
				int targetZ = tile.zCoord + side.offsetZ;
				int targetChunkX = targetX >> 4;
				int targetChunkZ = targetZ >> 4;
				if (targetChunkX == chunkX && targetChunkZ == chunkZ)
					return true;
				else if (tile.getWorldObj().blockExists(targetX, tile.yCoord + side.offsetY, targetZ))
				{
					TileEntity target = tile.getWorldObj().getTileEntity(targetX, tile.yCoord + side.offsetY, targetZ);
					if (target != null)
					{
						for (TileEntity secondTarget : BusUtils.getSecondTiles(target))
						{
							if (secondTarget != null)
							{
								int secondTargetChunkX = secondTarget.xCoord >> 4;
								int secondTargetChunkZ = secondTarget.zCoord >> 4;
								if (secondTargetChunkX == chunkX && secondTargetChunkZ == chunkZ)
									return true;
							}
						}
					}
				}
			}
		}

		return false;
	}
	    

	@SubscribeEvent
	public void onTick(final TickEvent ev)
	{
		if (ev.type == Type.CLIENT && ev.phase == Phase.START)
		{
			this.tickColors(this.cliPlayerColors);
			EntityFloatingItem.ageStatic = (EntityFloatingItem.ageStatic + 1) % 60000;
			final CableRenderMode currentMode = AEApi.instance().partHelper().getCableRenderMode();
			if (currentMode != this.crm)
			{
				this.crm = currentMode;
				CommonHelper.proxy.triggerUpdates();
			}
		}

		if (ev.type == Type.WORLD && ev.phase == Phase.END)
		{
			final WorldTickEvent wte = (WorldTickEvent) ev;
			synchronized (this.craftingJobs)
			{
				final Collection<CraftingJob> jobSet = this.craftingJobs.get(wte.world);
				if (!jobSet.isEmpty())
				{
					final int simTime = Math.max(1, AEConfig.instance.craftingCalculationTimePerTick / jobSet.size());
					final Iterator<CraftingJob> i = jobSet.iterator();
					while (i.hasNext())
					{
						final CraftingJob cj = i.next();
						if (!cj.simulateFor(simTime))
						{
							i.remove();
						}
					}
				}
			}
		}

		// for no there is no reason to care about this on the client...
		else if (ev.type == Type.SERVER && ev.phase == Phase.END)
		{
			this.tickColors(this.srvPlayerColors);
			// ready tiles.
			final HandlerRep repo = this.getRepo();
			while (!repo.tiles.isEmpty())
			{
				final AEBaseTile bt = repo.tiles.poll();
				if (!bt.isInvalid())
				{
					bt.onReady();
				}
			}

			// tick networks.
			for (final Grid g : this.getRepo().networks)
			{
				g.update();
			}

			// cross world queue.
			this.processQueue(this.serverQueue, null);
		}

		// world synced queue(s)
		if (ev.type == Type.WORLD && ev.phase == Phase.START)
		{
			final World world = ((WorldTickEvent) ev).world;
			final Queue<IWorldCallable<?>> queue = this.callQueue.get(world);
			this.processQueue(queue, world);
		}
	}

	private void tickColors(final HashMap<Integer, PlayerColor> playerSet)
	{
		final Iterator<PlayerColor> i = playerSet.values().iterator();
		while (i.hasNext())
		{
			final PlayerColor pc = i.next();
			if (pc.ticksLeft <= 0)
			{
				i.remove();
			}
			pc.ticksLeft--;
		}
	}

	private void processQueue(final Queue<IWorldCallable<?>> queue, final World world)
	{
		if (queue == null)
		{
			return;
		}

		final Stopwatch sw = Stopwatch.createStarted();

		IWorldCallable<?> c = null;
		while ((c = queue.poll()) != null)
		{
			try
			{
				c.call(world);

				if (sw.elapsed(TimeUnit.MILLISECONDS) > 50)
				{
					break;
				}
			}
			catch (final Exception e)
			{
				AELog.debug(e);
			}
		}

		// long time = sw.elapsed( TimeUnit.MILLISECONDS );
		// if ( time > 0 )
		// AELog.info( "processQueue Time: " + time + "ms" );
	}

	public void registerCraftingSimulation(final World world, final CraftingJob craftingJob)
	{
		synchronized (this.craftingJobs)
		{
			this.craftingJobs.put(world, craftingJob);
		}
	}

	private static class HandlerRep
	{

		private Queue<AEBaseTile> tiles = new LinkedList<AEBaseTile>();

		private Collection<Grid> networks = new NetworkList();

		private void clear()
		{
			this.tiles = new LinkedList<AEBaseTile>();
			this.networks = new NetworkList();
		}
	}

	public static class PlayerColor
	{

		public final AEColor myColor;
		private final int myEntity;
		private int ticksLeft;

		public PlayerColor(final int id, final AEColor col, final int ticks)
		{
			this.myEntity = id;
			this.myColor = col;
			this.ticksLeft = ticks;
		}

		public PacketPaintedEntity getPacket()
		{
			return new PacketPaintedEntity(this.myEntity, this.myColor, this.ticksLeft);
		}
	}
}
