package blusunrize.immersiveengineering.api.energy;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.common.IESaveData;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static blusunrize.immersiveengineering.api.ApiUtils.*;
    
public class ImmersiveNetHandler
{
	public static ImmersiveNetHandler INSTANCE;
	public ConcurrentHashMap<Integer, ConcurrentHashMap<ChunkCoordinates, Set<Connection>>> directConnections = new ConcurrentHashMap<>();
	public ConcurrentHashMap<ChunkCoordinates, Set<AbstractConnection>> indirectConnections = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Integer, ConcurrentHashMap<Connection, Integer>> transferPerTick = new ConcurrentHashMap<>();
    
	static
	{
		INSTANCE = new ImmersiveNetHandler();
    

	private ConcurrentHashMap<ChunkCoordinates, Set<Connection>> getMultimap(int dimension)
	{
		return this.directConnections.computeIfAbsent(dimension, ignored -> new ConcurrentHashMap<>());
	}

	public Map<Connection, Integer> getTransferedRates(int dimension)
	{
		return this.transferPerTick.computeIfAbsent(dimension, ignored -> new ConcurrentHashMap<>());
	}

	public void addConnection(World world, ChunkCoordinates node, ChunkCoordinates connection, int distance, WireType cableType)
	{
		int dimensionId = world.provider.dimensionId;
		ConcurrentHashMap<ChunkCoordinates, Set<Connection>> multimap = this.getMultimap(dimensionId);
		multimap.computeIfAbsent(node, ignored -> newSetFromMap(new ConcurrentHashMap<>())).add(new Connection(node, connection, cableType, distance));
		multimap.computeIfAbsent(connection, ignored -> newSetFromMap(new ConcurrentHashMap<>())).add(new Connection(connection, node, cableType, distance));
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.indirectConnections.clear();
		if (world.blockExists(node.posX, node.posY, node.posZ))
			world.addBlockEvent(node.posX, node.posY, node.posZ, world.getBlock(node.posX, node.posY, node.posZ), -1, 0);
		if (world.blockExists(connection.posX, connection.posY, connection.posZ))
			world.addBlockEvent(connection.posX, connection.posY, connection.posZ, world.getBlock(connection.posX, connection.posY, connection.posZ), -1, 0);
		IESaveData.setDirty(dimensionId);
	}

	public void addConnection(World world, ChunkCoordinates node, Connection con)
	{
		int dimensionId = world.provider.dimensionId;
		ConcurrentHashMap<ChunkCoordinates, Set<Connection>> multimap = this.getMultimap(dimensionId);
		multimap.computeIfAbsent(node, ignored -> newSetFromMap(new ConcurrentHashMap<>())).add(con);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.indirectConnections.clear();
		IESaveData.setDirty(dimensionId);
	}

	public void addConnection(int world, ChunkCoordinates node, Connection con)
	{
		ConcurrentHashMap<ChunkCoordinates, Set<Connection>> multimap = this.getMultimap(world);
		multimap.computeIfAbsent(node, ignored -> newSetFromMap(new ConcurrentHashMap<>())).add(con);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.indirectConnections.clear();
		IESaveData.setDirty(world);
	}

	public void setProxy(DimensionBlockPos position, IICProxy proxy)
	{
		if (proxy != null)
			this.proxies.put(position, proxy);
		else
			this.proxies.remove(position);
	}

	public void addProxy(IICProxy p)
	{
		if (p == null)
			return;
		ChunkCoordinates pos = p.getPos();
		this.setProxy(new DimensionBlockPos(p.getDimension(), pos.posX, pos.posY, pos.posZ), p);
	}

	public void removeConnection(World world, Connection con)
	{
		if (con == null || world == null)
			return;
		for (Set<Connection> conl : this.getMultimap(world.provider.dimensionId).values())
		{
			Iterator<Connection> it = conl.iterator();
			while (it.hasNext())
			{
				Connection itCon = it.next();
				if (con.hasSameConnectors(itCon))
				{
					it.remove();

					this.remove(itCon.end, world, itCon);
					this.remove(itCon.start, world, itCon);

					if (world.blockExists(itCon.start.posX, itCon.start.posY, itCon.start.posZ))
						world.addBlockEvent(itCon.start.posX, itCon.start.posY, itCon.start.posZ, world.getBlock(itCon.start.posX, itCon.start.posY, itCon.start.posZ), -1, 0);
					if (world.blockExists(itCon.end.posX, itCon.end.posY, itCon.end.posZ))
						world.addBlockEvent(itCon.end.posX, itCon.end.posY, itCon.end.posZ, world.getBlock(itCon.end.posX, itCon.end.posY, itCon.end.posZ), -1, 0);
				}
			}
		}
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.indirectConnections.clear();
		IESaveData.setDirty(world.provider.dimensionId);
	}

	private void remove(ChunkCoordinates cc, World w, Connection c)
	{
		IImmersiveConnectable iic = toIIC(cc, w);
		if (iic != null)
			iic.removeCable(c);
		else
		{
			DimensionBlockPos pos = new DimensionBlockPos(w.provider.dimensionId, cc.posX, cc.posY, cc.posZ);
			IICProxy iicProxy = this.proxies.get(pos);
			if (iicProxy != null)
				iicProxy.removeCable(c);
		}
	}

	public Set<Integer> getRelevantDimensions()
	{
    
	}

    
	public Collection<Connection> getAllConnections(World world)
	{
		return this.getAllConnections(world.provider.dimensionId);
	}

	public Collection<Connection> getAllConnections(int dimensionId)
	{
		Set<Connection> ret = newSetFromMap(new ConcurrentHashMap<>());
		for (Set<Connection> conlist : this.getMultimap(dimensionId).values())
		{
			ret.addAll(conlist);
		}
		return ret;
    

	public synchronized Set<Connection> getConnections(World world, ChunkCoordinates node)
	{
		return this.getMultimap(world.provider.dimensionId).get(node);
	}

	public void clearAllConnections(World world)
	{
		this.getMultimap(world.provider.dimensionId).clear();
	}

	public void clearAllConnections(int world)
	{
		this.getMultimap(world).clear();
	}

	public void clearConnectionsOriginatingFrom(ChunkCoordinates node, World world)
	{
		int dimensionId = world.provider.dimensionId;
		ConcurrentHashMap<ChunkCoordinates, Set<Connection>> multimap = this.getMultimap(dimensionId);
		Set<Connection> connections = multimap.get(node);
		if (connections != null)
			connections.clear();
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.indirectConnections.clear();
	}

	public void resetCachedIndirectConnections()
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.indirectConnections.clear();
	}

    
	public void clearAllConnectionsFor(ChunkCoordinates node, World world, boolean doDrops)
	{
		int dimensionId = world.provider.dimensionId;
		ConcurrentHashMap<ChunkCoordinates, Set<Connection>> multimap = this.getMultimap(dimensionId);
		Set<Connection> connections = multimap.get(node);
		if (connections != null)
			connections.clear();
    
    
    
    

		for (Set<Connection> conl : multimap.values())
		{
			Iterator<Connection> it = conl.iterator();

			while (it.hasNext())
			{
				Connection con = it.next();
				if (node.equals(con.start) || node.equals(con.end))
				{
					it.remove();
					this.remove(con.end, world, con);
					this.remove(con.start, world, con);
					if (node.equals(con.end))
					{
						double dx = node.posX + .5 + Math.signum(con.start.posX - con.end.posX);
						double dy = node.posY + .5 + Math.signum(con.start.posY - con.end.posY);
						double dz = node.posZ + .5 + Math.signum(con.start.posZ - con.end.posZ);
						if (doDrops && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
							world.spawnEntityInWorld(new EntityItem(world, dx, dy, dz, con.cableType.getWireCoil()));
						if (world.blockExists(con.start.posX, con.start.posY, con.start.posZ))
							world.addBlockEvent(con.start.posX, con.start.posY, con.start.posZ, world.getBlock(con.start.posX, con.start.posY, con.start.posZ), -1, 0);
					}
					else if (world.blockExists(con.end.posX, con.end.posY, con.end.posZ))
						world.addBlockEvent(con.end.posX, con.end.posY, con.end.posZ, world.getBlock(con.end.posX, con.end.posY, con.end.posZ), -1, 0);
				}
			}
		}
		if (world.blockExists(node.posX, node.posY, node.posZ))
			world.addBlockEvent(node.posX, node.posY, node.posZ, world.getBlock(node.posX, node.posY, node.posZ), -1, 0);
		IESaveData.setDirty(dimensionId);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.indirectConnections.clear();
	}

    
	public void clearAllConnectionsFor(ChunkCoordinates node, World world, TargetingInfo target)
	{
		IImmersiveConnectable iic = toIIC(node, world);
		WireType type = target == null ? null : iic.getCableLimiter(target);
		if (type == null)
			return;
		for (Set<Connection> conl : this.getMultimap(world.provider.dimensionId).values())
		{
			Iterator<Connection> it = conl.iterator();
			while (it.hasNext())
			{
				Connection con = it.next();
				if (con.cableType == type)
					if (node.equals(con.start) || node.equals(con.end))
					{
						it.remove();
						this.remove(con.end, world, con);
						this.remove(con.start, world, con);
						if (node.equals(con.end))
						{
							double dx = node.posX + .5 + Math.signum(con.start.posX - con.end.posX);
							double dy = node.posY + .5 + Math.signum(con.start.posY - con.end.posY);
							double dz = node.posZ + .5 + Math.signum(con.start.posZ - con.end.posZ);
							if (world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
								world.spawnEntityInWorld(new EntityItem(world, dx, dy, dz, con.cableType.getWireCoil()));
							if (world.blockExists(con.start.posX, con.start.posY, con.start.posZ))
								world.addBlockEvent(con.start.posX, con.start.posY, con.start.posZ, world.getBlock(con.start.posX, con.start.posY, con.start.posZ), -1, 0);
						}
						else if (world.blockExists(con.end.posX, con.end.posY, con.end.posZ))
							world.addBlockEvent(con.end.posX, con.end.posY, con.end.posZ, world.getBlock(con.end.posX, con.end.posY, con.end.posZ), -1, 0);
					}
			}
		}
		if (world.blockExists(node.posX, node.posY, node.posZ))
			world.addBlockEvent(node.posX, node.posY, node.posZ, world.getBlock(node.posX, node.posY, node.posZ), -1, 0);

		IESaveData.setDirty(world.provider.dimensionId);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.indirectConnections.clear();
	}

    
	public Set<AbstractConnection> getIndirectEnergyConnections(ChunkCoordinates node, World world)
	{
		Set<AbstractConnection> connections = this.indirectConnections.get(node);
		if (connections != null)
			return connections;

		List<IImmersiveConnectable> openList = new ArrayList<>();
		Set<AbstractConnection> closedList = newSetFromMap(new ConcurrentHashMap<>());
		List<ChunkCoordinates> checked = new ArrayList<>();
		HashMap<ChunkCoordinates, ChunkCoordinates> backtracker = new HashMap<>();

		checked.add(node);
		Set<Connection> conL = this.getConnections(world, node);
		if (conL != null)
			for (Connection con : conL)
			{
				IImmersiveConnectable end = toIIC(con.end, world);
				if (end == null)
				{
					DimensionBlockPos p = new DimensionBlockPos(world.provider.dimensionId, con.end.posX, con.end.posY, con.end.posZ);
					end = this.proxies.get(p);
				}
				if (end != null)
				{
					openList.add(end);
					backtracker.put(con.end, node);
				}
			}

		IImmersiveConnectable next = null;
		final int closedListMax = 1200;

		while (closedList.size() < closedListMax && !openList.isEmpty())
		{
			next = openList.get(0);
			if (!checked.contains(toCC(next)))
			{
				if (next.isEnergyOutput())
				{
					ChunkCoordinates last = toCC(next);
					WireType averageType = null;
					int distance = 0;
					List<Connection> connectionParts = new ArrayList<>();
					while (last != null)
					{
						ChunkCoordinates prev = last;
						last = backtracker.get(last);
						if (last != null)
						{

							Set<Connection> conLB = this.getConnections(world, prev);
							if (conLB != null)
								for (Connection conB : conLB)
								{
									if (conB.end.equals(last))
									{
										connectionParts.add(conB);
										distance += conB.length;
										if (averageType == null || conB.cableType.getTransferRate() < averageType.getTransferRate())
											averageType = conB.cableType;
										break;
									}
								}
						}
					}
					closedList.add(new AbstractConnection(toCC(node), toCC(next), averageType, distance, connectionParts.toArray(new Connection[0])));
				}

				Set<Connection> conLN = this.getConnections(world, toCC(next));
				if (conLN != null)
					for (Connection con : conLN)
					{
						if (next.allowEnergyToPass(con))
						{
							IImmersiveConnectable end = toIIC(con.end, world);
							if (end == null)
							{
								DimensionBlockPos p = new DimensionBlockPos(world.provider.dimensionId, con.end.posX, con.end.posY, con.end.posZ);
								end = this.proxies.get(p);
							}
							if (end != null && !checked.contains(con.end) && !openList.contains(end))
							{
								openList.add(end);
								backtracker.put(con.end, toCC(next));
							}
						}
					}
				checked.add(toCC(next));
			}
			openList.remove(0);
		}
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.indirectConnections.computeIfAbsent(node, ignored -> newSetFromMap(new ConcurrentHashMap<>())).addAll(closedList);
		return closedList;
	}

	public static class Connection implements Comparable<Connection>
	{
		public ChunkCoordinates start;
		public ChunkCoordinates end;
		public WireType cableType;
		public int length;
		public Vec3[] catenaryVertices;

		public Connection(ChunkCoordinates start, ChunkCoordinates end, WireType cableType, int length)
		{
			this.start = start;
			this.end = end;
			this.cableType = cableType;
			this.length = length;
		}

		public boolean hasSameConnectors(Connection con)
		{
			if (con == null)
				return false;
			boolean n0 = this.start.equals(con.start) && this.end.equals(con.end);
			boolean n1 = this.start.equals(con.end) && this.end.equals(con.start);
			return n0 || n1;
		}

		public Vec3[] getSubVertices(World world)
		{
			if (this.catenaryVertices == null)
			{
				Vec3 vStart = Vec3.createVectorHelper(this.start.posX, this.start.posY, this.start.posZ);
				Vec3 vEnd = Vec3.createVectorHelper(this.end.posX, this.end.posY, this.end.posZ);
				IImmersiveConnectable iicStart = toIIC(this.start, world);
				IImmersiveConnectable iicEnd = toIIC(this.end, world);
				if (iicStart != null)
					vStart = addVectors(vStart, iicStart.getConnectionOffset(this));
				if (iicEnd != null)
					vEnd = addVectors(vEnd, iicEnd.getConnectionOffset(this));
				this.catenaryVertices = getConnectionCatenary(this, vStart, vEnd);
			}
			return this.catenaryVertices;
		}

		public NBTTagCompound writeToNBT()
		{
			NBTTagCompound tag = new NBTTagCompound();
			if (this.start != null)
				tag.setIntArray("start", new int[] { this.start.posX, this.start.posY, this.start.posZ });
			if (this.end != null)
				tag.setIntArray("end", new int[] { this.end.posX, this.end.posY, this.end.posZ });
			tag.setString("cableType", this.cableType.getUniqueName());
			tag.setInteger("length", this.length);
			return tag;
		}

		public static Connection readFromNBT(NBTTagCompound tag)
		{
			if (tag == null)
				return null;
			int[] iStart = tag.getIntArray("start");
			ChunkCoordinates start = new ChunkCoordinates(iStart[0], iStart[1], iStart[2]);

			int[] iEnd = tag.getIntArray("end");
			ChunkCoordinates end = new ChunkCoordinates(iEnd[0], iEnd[1], iEnd[2]);

			WireType type = ApiUtils.getWireTypeFromNBT(tag, "cableType");

			if (start != null && end != null && type != null)
				return new Connection(start, end, type, tag.getInteger("length"));
			return null;
		}

		@Override
		public int compareTo(Connection o)
		{
			if (this.equals(o))
				return 0;
			int distComp = Integer.compare(this.length, o.length);
			int cableComp = -1 * Integer.compare(this.cableType.getTransferRate(), o.cableType.getTransferRate());
			if (cableComp != 0)
				return cableComp;
			if (distComp != 0)
				return distComp;
			if (this.start.posX != o.start.posX)
				return this.start.posX > o.start.posX ? 1 : -1;
			if (this.start.posY != o.start.posY)
				return this.start.posY > o.start.posY ? 1 : -1;
			if (this.start.posZ != o.start.posZ)
				return this.start.posZ > o.start.posZ ? 1 : -1;
			if (this.end.posX != o.end.posX)
				return this.end.posX > o.end.posX ? 1 : -1;
			if (this.end.posY != o.end.posY)
				return this.end.posY > o.end.posY ? 1 : -1;
			if (this.end.posZ != o.end.posZ)
				return this.end.posZ > o.end.posZ ? 1 : -1;
			return 0;
		}
	}

	public static class AbstractConnection extends Connection
	{
		public Connection[] subConnections;

		public AbstractConnection(ChunkCoordinates start, ChunkCoordinates end, WireType cableType, int length, Connection... subConnections)
		{
			super(start, end, cableType, length);
			this.subConnections = subConnections;
		}

		public float getPreciseLossRate(int energyInput, int connectorMaxInput)
		{
			float f = 0;
			for (Connection c : this.subConnections)
			{
				float length = c.length / (float) c.cableType.getMaxLength();
				float baseLoss = (float) c.cableType.getLossRatio();
				float mod = ((connectorMaxInput - energyInput) / (float) connectorMaxInput) * .4f;
				f += length * (baseLoss + baseLoss * mod);
			}
			return Math.min(f, 1);
		}

		public float getAverageLossRate()
		{
			float f = 0;
			for (Connection c : this.subConnections)
			{
				float length = c.length / (float) c.cableType.getMaxLength();
				float baseLoss = (float) c.cableType.getLossRatio();
				f += length * baseLoss;
			}
			return Math.min(f, 1);
		}
	}
}