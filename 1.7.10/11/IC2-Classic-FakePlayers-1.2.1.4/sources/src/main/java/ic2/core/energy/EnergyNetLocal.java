package ic2.core.energy;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.math.DoubleMath;

import ic2.api.Direction;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.PacketStat;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.block.TileEntityBlock;
import ic2.core.util.FilteredList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EnergyNetLocal
{
	public static double minConductionLoss = 1.0E-4D;
	private static Direction[] directions = Direction.values();
	public static EnergyTransferList list;
	private World world;
	private EnergyNetLocal.EnergyPathMap energySourceToEnergyPathMap = new EnergyNetLocal.EnergyPathMap();
	private Map<EntityLivingBase, Integer> entityLivingToShockEnergyMap = new HashMap();
	private Map<ChunkCoordinates, IEnergyTile> registeredTiles = new HashMap();
	private Map<ChunkCoordinates, IEnergySource> sources = new HashMap();
	private EnergyNetLocal.WaitingList waitingList = new EnergyNetLocal.WaitingList();
	private EnergyNetLocal.UnloadingList unloading = new EnergyNetLocal.UnloadingList();

	EnergyNetLocal(World world)
	{
		this.world = world;
	}

	public void addTile(TileEntity par1)
	{
		if (par1 instanceof IMetaDelegate)
		{
			List<TileEntity> tiles = ((IMetaDelegate) par1).getSubTiles();

			for (TileEntity tile : tiles)
				this.addTileEntity(coords(tile), par1);

			if (par1 instanceof IEnergySource)
				this.sources.put(coords(tiles.get(0)), (IEnergySource) par1);
		}
		else
			this.addTileEntity(coords(par1), par1);

	}

	public void addTileEntity(ChunkCoordinates coords, TileEntity tile)
	{
		if (tile instanceof IEnergyTile && (!this.registeredTiles.containsKey(coords) || this.unloading.contains(coords)))
		{
			this.registeredTiles.put(coords, (IEnergyTile) tile);
			this.update(coords.posX, coords.posY, coords.posZ);
			if (tile instanceof IEnergyAcceptor)
			{
				this.waitingList.onTileEntityAdded(this.getValidReceivers(tile, true), tile);
				this.unloading.onLoaded(coords);
			}

			if (tile instanceof IEnergySource && !(tile instanceof IMetaDelegate))
				this.sources.put(coords, (IEnergySource) tile);

		}
	}

	public void removeTile(TileEntity par1)
	{
		if (par1 instanceof IMetaDelegate)
			for (TileEntity tile : ((IMetaDelegate) par1).getSubTiles())
				this.removeTileEntity(coords(tile), par1);
		else
			this.removeTileEntity(coords(par1), par1);

	}

	public void removeTileEntity(ChunkCoordinates coords, TileEntity tile)
	{
		if (tile instanceof IEnergyTile && this.registeredTiles.containsKey(coords))
		{
			if (tile instanceof IEnergyAcceptor)
			{
				this.waitingList.onTileEntityRemoved(tile);
				this.unloading.onUnloaded(this.getValidReceivers(tile, true), coords, tile);
			}

			if (tile instanceof IEnergySource)
			{
				this.sources.remove(coords);
				this.energySourceToEnergyPathMap.remove(tile);
				if (!(tile instanceof IEnergyAcceptor))
				{
					this.registeredTiles.remove(coords);
					this.update(coords.posX, coords.posY, coords.posZ);
				}
			}

		}
		else
		{
			boolean alreadyRemoved = !this.registeredTiles.containsKey(coords);
			if (!alreadyRemoved)
				IC2.log.warn("removing " + tile + " from the EnergyNet failed, already removed: " + alreadyRemoved);

		}
	}

	public List<PacketStat> getSendedPackets(TileEntity tileEntity)
	{
		Map<Integer, EnergyNetLocal.EnergyPacket> totalPackets = new LinkedHashMap();
		if (tileEntity instanceof IEnergyConductor || tileEntity instanceof IEnergySink)
			for (EnergyNetLocal.EnergyPath energyPath : this.energySourceToEnergyPathMap.getPaths((IEnergyAcceptor) tileEntity))
			{
				if (energyPath.backupEnergyPackets.isEmpty() && !energyPath.mesuredEnergyPackets.isEmpty())
				{
					energyPath.backupEnergyPackets.putAll(energyPath.mesuredEnergyPackets);
					energyPath.mesuredEnergyPackets.clear();
				}

				this.addPackets(totalPackets, energyPath.backupEnergyPackets);
			}

		if (tileEntity instanceof IEnergySource && this.energySourceToEnergyPathMap.containsKey(tileEntity))
			for (EnergyNetLocal.EnergyPath energyPath : this.energySourceToEnergyPathMap.get(tileEntity))
			{
				if (energyPath.backupEnergyPackets.isEmpty() && !energyPath.mesuredEnergyPackets.isEmpty())
				{
					energyPath.backupEnergyPackets.putAll(energyPath.mesuredEnergyPackets);
					energyPath.mesuredEnergyPackets.clear();
				}

				this.addPackets(totalPackets, energyPath.backupEnergyPackets);
			}

		List<PacketStat> packets = new ArrayList();

		for (Entry<Integer, EnergyNetLocal.EnergyPacket> entry : totalPackets.entrySet())
			packets.add(new PacketStat(entry.getKey().intValue(), entry.getValue().getAmount()));

		Collections.sort(packets);
		return packets;
	}

	public List<PacketStat> getTotalSendedPackets(TileEntity tileEntity)
	{
		Map<Integer, EnergyNetLocal.EnergyPacket> totalPackets = new LinkedHashMap();
		if (tileEntity instanceof IEnergyConductor || tileEntity instanceof IEnergySink)
			for (EnergyNetLocal.EnergyPath energyPath : this.energySourceToEnergyPathMap.getPaths((IEnergyAcceptor) tileEntity))
				this.addPackets(totalPackets, energyPath.totalEnergyPackets);

		if (tileEntity instanceof IEnergySource && this.energySourceToEnergyPathMap.containsKey(tileEntity))
			for (EnergyNetLocal.EnergyPath energyPath : this.energySourceToEnergyPathMap.get(tileEntity))
				this.addPackets(totalPackets, energyPath.totalEnergyPackets);

		List<PacketStat> packets = new ArrayList();

		for (Entry<Integer, EnergyNetLocal.EnergyPacket> entry : totalPackets.entrySet())
			packets.add(new PacketStat(entry.getKey().intValue(), entry.getValue().getAmount()));

		Collections.sort(packets);
		return packets;
	}

	public double getTotalEnergyEmitted(TileEntity tileEntity)
	{
		double ret = 0.0D;
		if (tileEntity instanceof IEnergyConductor)
			for (EnergyNetLocal.EnergyPath energyPath : this.energySourceToEnergyPathMap.getPaths((IEnergyAcceptor) tileEntity))
				ret += energyPath.totalEnergyConducted;

		if (tileEntity instanceof IEnergySource && this.energySourceToEnergyPathMap.containsKey(tileEntity))
			for (EnergyNetLocal.EnergyPath energyPath2 : this.energySourceToEnergyPathMap.get(tileEntity))
				ret += energyPath2.totalEnergyConducted;

		return ret;
	}

	public double getTotalEnergySunken(TileEntity tileEntity)
	{
		double ret = 0.0D;
		if (tileEntity instanceof IEnergyConductor || tileEntity instanceof IEnergySink)
			for (EnergyNetLocal.EnergyPath energyPath : this.energySourceToEnergyPathMap.getPaths((IEnergyAcceptor) tileEntity))
				ret += energyPath.totalEnergyConducted;

		return ret;
	}

	public int emitEnergyFrom(ChunkCoordinates coords, IEnergySource energySource, int amount)
	{
		if (!this.registeredTiles.containsKey(coords))
		{
			IC2.log.warn("EnergyNet.emitEnergyFrom: " + energySource + " is not added to the enet");
			return amount;
		}
		else
		{
			if (!this.energySourceToEnergyPathMap.containsKey(energySource))
			{
				TileEntity var10003 = (TileEntity) energySource;
				EnergyTransferList var10005 = list;
				this.energySourceToEnergyPathMap.put(energySource, this.discover(var10003, false, EnergyTransferList.getMaxEnergy(energySource, amount)));
			}

			List<EnergyNetLocal.EnergyPath> paths = this.energySourceToEnergyPathMap.get(energySource);
			if (paths.isEmpty())
				return amount;
			else
			{
				double totalInvLoss = 0.0D;
				List<EnergyNetLocal.EnergyPath> activeEnergyPaths = new ArrayList(paths.size());

				for (EnergyNetLocal.EnergyPath energyPath : paths)
				{
					assert energyPath.target instanceof IEnergySink;

					IEnergySink energySink = (IEnergySink) energyPath.target;
					if (energySink.getDemandedEnergy() > 0.0D && energyPath.loss < amount && (!IC2.enableIC2EasyMode || !this.conductorToWeak(energyPath, amount)))
					{
						totalInvLoss += 1.0D / energyPath.loss;
						activeEnergyPaths.add(energyPath);
					}
				}

				if (activeEnergyPaths.isEmpty())
					return amount;
				else
				{
					Collections.shuffle(activeEnergyPaths);

					for (int i = activeEnergyPaths.size() - amount; i > 0; --i)
					{
						EnergyNetLocal.EnergyPath removedEnergyPath = activeEnergyPaths.remove(activeEnergyPaths.size() - 1);
						totalInvLoss -= 1.0D / removedEnergyPath.loss;
					}

					double source = EnergyNet.instance.getPowerFromTier(energySource.getSourceTier());

					int energyConsumed;
					Map<EnergyNetLocal.EnergyPath, Integer> suppliedEnergyPaths;
					for (suppliedEnergyPaths = new LinkedHashMap(); !((List) activeEnergyPaths).isEmpty() && amount > 0; amount -= energyConsumed)
					{
						energyConsumed = 0;
						double newTotalInvLoss = 0.0D;
						List<EnergyNetLocal.EnergyPath> currentActiveEnergyPaths = activeEnergyPaths;
						activeEnergyPaths = new ArrayList(activeEnergyPaths.size());

						for (EnergyNetLocal.EnergyPath energyPath2 : currentActiveEnergyPaths)
						{
							IEnergySink energySink2 = (IEnergySink) energyPath2.target;
							int energyProvided = (int) Math.floor(Math.round(amount / totalInvLoss / energyPath2.loss * 100000.0D) / 100000.0D);
							int energyLoss = (int) Math.floor(energyPath2.loss);
							if (energyProvided > energyLoss)
							{
								double providing = energyProvided - energyLoss;
								double adding = Math.min(providing, energySink2.getDemandedEnergy());
								if (adding <= 0.0D && EnergyTransferList.hasOverrideInput(energySink2))
									adding = EnergyTransferList.getOverrideInput(energySink2);

								if (adding > 0.0D)
								{
									int accepting = (int) EnergyNet.instance.getPowerFromTier(energySink2.getSinkTier());
									if (accepting <= 0)
										accepting = Integer.MAX_VALUE;

									if (providing > accepting)
									{
										if (!IC2.enableIC2EasyMode)
											this.explodeTiles(energySink2);
									}
									else
									{
										double energyReturned = energySink2.injectEnergy(energyPath2.targetDirection.toForgeDirection(), adding, source);
										if (energyReturned == 0.0D)
										{
											if (energySink2.getDemandedEnergy() >= 1.0D)
											{
												activeEnergyPaths.add(energyPath2);
												newTotalInvLoss += 1.0D / energyPath2.loss;
											}
										}
										else if (energyReturned >= energyProvided - energyLoss)
										{
											energyReturned = energyProvided - energyLoss;
											IC2.log.warn("API ERROR: " + energySink2 + " didn\'t implement demandsEnergy() properly, no energy from injectEnergy accepted although demandsEnergy() returned true.");
										}

										energyConsumed = (int) (energyConsumed + adding - energyReturned + energyLoss);
										int energyInjected = (int) (adding - energyReturned);
										if (!suppliedEnergyPaths.containsKey(energyPath2))
											suppliedEnergyPaths.put(energyPath2, Integer.valueOf(energyInjected));
										else
											suppliedEnergyPaths.put(energyPath2, Integer.valueOf(energyInjected + suppliedEnergyPaths.get(energyPath2).intValue()));
									}
								}
							}
							else
							{
								activeEnergyPaths.add(energyPath2);
								newTotalInvLoss += 1.0D / energyPath2.loss;
							}
						}

						if (energyConsumed == 0 && !activeEnergyPaths.isEmpty())
						{
							EnergyNetLocal.EnergyPath removedEnergyPath2 = activeEnergyPaths.remove(activeEnergyPaths.size() - 1);
							newTotalInvLoss -= 1.0D / removedEnergyPath2.loss;
						}

						totalInvLoss = newTotalInvLoss;
					}

					for (Entry<EnergyNetLocal.EnergyPath, Integer> entry : suppliedEnergyPaths.entrySet())
					{
						EnergyNetLocal.EnergyPath energyPath3 = entry.getKey();
						int energyInjected2 = entry.getValue().intValue();
						energyPath3.totalEnergyConducted += energyInjected2;
						this.addPacket(energyPath3, energyInjected2);
						if (energyInjected2 > energyPath3.minInsulationEnergyAbsorption)
						{
							Map<EntityLivingBase, Integer> shocks = new HashMap();
							List<EntityLivingBase> entitiesNearEnergyPath = this.world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(energyPath3.minX - 1, energyPath3.minY - 1, energyPath3.minZ - 1, energyPath3.maxX + 2, energyPath3.maxY + 2, energyPath3.maxZ + 2));

							for (IEnergyConductor energyConductor : energyPath3.conductors)
							{
								int shockAbsorbing = (int) energyConductor.getInsulationEnergyAbsorption();
								if (shockAbsorbing < energyInjected2)
								{
									ChunkCoordinates coord = coords((TileEntity) energyConductor);
									AxisAlignedBB box = AxisAlignedBB.getBoundingBox(coords.posX - 1, coords.posY - 1, coords.posZ - 1, coords.posX + 2, coords.posY + 2, coords.posZ + 2);

									for (EntityLivingBase entity : entitiesNearEnergyPath)
										if (entity.boundingBox.intersectsWith(box))
										{
											int shockEnergy = energyInjected2 - shockAbsorbing;
											if (shockEnergy >= 0)
												if (shocks.containsKey(entity))
												{
													int oldValue = shocks.get(entity).intValue();
													if (shockEnergy > oldValue)
														shocks.put(entity, Integer.valueOf(shockEnergy));
												}
												else
													shocks.put(entity, Integer.valueOf(shockEnergy));
										}
								}
							}

							if (shocks.size() > 0)
								for (Entry<EntityLivingBase, Integer> shockEntry : shocks.entrySet())
								{
									EntityLivingBase base = shockEntry.getKey();
									if (this.entityLivingToShockEnergyMap.containsKey(base))
										this.entityLivingToShockEnergyMap.put(base, Integer.valueOf(shockEntry.getValue().intValue() + this.entityLivingToShockEnergyMap.get(base).intValue()));
									else
										this.entityLivingToShockEnergyMap.put(base, shockEntry.getValue());
								}

							if (energyInjected2 >= energyPath3.minInsulationBreakdownEnergy)
								for (IEnergyConductor energyConductor2 : energyPath3.conductors)
									if (energyInjected2 >= energyConductor2.getInsulationBreakdownEnergy())
									{
										energyConductor2.removeInsulation();
										if (energyConductor2.getInsulationEnergyAbsorption() < energyPath3.minInsulationEnergyAbsorption)
											energyPath3.minInsulationEnergyAbsorption = (int) energyConductor2.getInsulationEnergyAbsorption();
									}
						}

						if (energyInjected2 >= energyPath3.minConductorBreakdownEnergy && !IC2.enableIC2EasyMode)
							for (IEnergyConductor energyConductor3 : energyPath3.conductors)
								if (energyInjected2 >= energyConductor3.getConductorBreakdownEnergy())
									energyConductor3.removeConductor();
					}

					return amount;
				}
			}
		}
	}

	private FilteredList<EnergyNetLocal.EnergyPath> discover(TileEntity emitter, boolean reverse, int lossLimit)
	{
		Map<TileEntity, EnergyNetLocal.EnergyBlockLink> reachedTileEntities = new HashMap();
		LinkedList<TileEntity> tileEntitiesToCheck = new LinkedList();
		tileEntitiesToCheck.add(emitter);
		int totalSinks = 0;

		label36: while (!tileEntitiesToCheck.isEmpty())
		{
			TileEntity currentTileEntity = tileEntitiesToCheck.remove();
			if (!currentTileEntity.isInvalid())
			{
				double currentLoss = 0.0D;
				if (this.registeredTiles.get(coords(currentTileEntity)) != null && this.registeredTiles.get(coords(currentTileEntity)) != emitter && reachedTileEntities.containsKey(currentTileEntity))
					currentLoss = reachedTileEntities.get(currentTileEntity).loss;

				List<EnergyNetLocal.EnergyTarget> validReceivers = this.getValidReceivers(currentTileEntity, reverse);
				Iterator energyBlockLink = validReceivers.iterator();

				while (true)
				{
					EnergyNetLocal.EnergyTarget validReceiver;
					double additionalLoss;
					while (true)
					{
						if (!energyBlockLink.hasNext())
							continue label36;

						validReceiver = (EnergyNetLocal.EnergyTarget) energyBlockLink.next();
						if (validReceiver.tileEntity != emitter)
						{
							additionalLoss = 0.0D;
							if (!(validReceiver.tileEntity instanceof IEnergyConductor))
								break;

							additionalLoss = ((IEnergyConductor) validReceiver.tileEntity).getConductionLoss();
							if (additionalLoss < 1.0E-4D)
								additionalLoss = 1.0E-4D;

							if (currentLoss + additionalLoss < lossLimit)
								break;
						}
					}

					if (!reachedTileEntities.containsKey(validReceiver.tileEntity) || reachedTileEntities.get(validReceiver.tileEntity).loss > currentLoss + additionalLoss)
					{
						reachedTileEntities.put(validReceiver.tileEntity, new EnergyNetLocal.EnergyBlockLink(validReceiver.direction, currentLoss + additionalLoss));
						if (validReceiver.tileEntity instanceof IEnergySink)
							++totalSinks;

						if (validReceiver.tileEntity instanceof IEnergyConductor)
						{
							tileEntitiesToCheck.remove(validReceiver.tileEntity);
							tileEntitiesToCheck.add(validReceiver.tileEntity);
						}
					}
				}
			}
		}

		if (totalSinks < 10)
			totalSinks = 10;

		FilteredList<EnergyNetLocal.EnergyPath> energyPaths = new FilteredList(totalSinks);

		for (Entry<TileEntity, EnergyNetLocal.EnergyBlockLink> entry : reachedTileEntities.entrySet())
		{
			TileEntity tileEntity = entry.getKey();
			if (!reverse && tileEntity instanceof IEnergySink || reverse && tileEntity instanceof IEnergySource)
			{
				EnergyNetLocal.EnergyBlockLink energyBlockLink = entry.getValue();
				EnergyNetLocal.EnergyPath energyPath = new EnergyNetLocal.EnergyPath();
				if (energyBlockLink.loss > 0.1D)
					energyPath.loss = energyBlockLink.loss;
				else
					energyPath.loss = 0.1D;

				energyPath.target = tileEntity;
				energyPath.targetDirection = energyBlockLink.direction;
				if (!reverse && emitter instanceof IEnergySource)
					while (true)
					{
						tileEntity = EnergyNet.instance.getNeighbor(tileEntity, energyBlockLink.direction.toForgeDirection());
						if (tileEntity == emitter || !(tileEntity instanceof IEnergyConductor))
							break;

						IEnergyConductor energyConductor = (IEnergyConductor) tileEntity;
						if (tileEntity.xCoord < energyPath.minX)
							energyPath.minX = tileEntity.xCoord;

						if (tileEntity.yCoord < energyPath.minY)
							energyPath.minY = tileEntity.yCoord;

						if (tileEntity.zCoord < energyPath.minZ)
							energyPath.minZ = tileEntity.zCoord;

						if (tileEntity.xCoord > energyPath.maxX)
							energyPath.maxX = tileEntity.xCoord;

						if (tileEntity.yCoord > energyPath.maxY)
							energyPath.maxY = tileEntity.yCoord;

						if (tileEntity.zCoord > energyPath.maxZ)
							energyPath.maxZ = tileEntity.zCoord;

						energyPath.conductors.add(energyConductor);
						if (energyConductor.getInsulationEnergyAbsorption() < energyPath.minInsulationEnergyAbsorption)
							energyPath.minInsulationEnergyAbsorption = (int) energyConductor.getInsulationEnergyAbsorption();

						if (energyConductor.getInsulationBreakdownEnergy() < energyPath.minInsulationBreakdownEnergy)
							energyPath.minInsulationBreakdownEnergy = (int) energyConductor.getInsulationBreakdownEnergy();

						if (energyConductor.getConductorBreakdownEnergy() < energyPath.minConductorBreakdownEnergy)
							energyPath.minConductorBreakdownEnergy = (int) energyConductor.getConductorBreakdownEnergy();

						energyBlockLink = reachedTileEntities.get(tileEntity);
						if (energyBlockLink == null)
							IC2.platform.displayError("An energy network pathfinding entry is corrupted.\nThis could happen due to incorrect Minecraft behavior or a bug.\n\n(Technical information: energyBlockLink, tile entities below)\nE: " + emitter + " (" + emitter.xCoord + "," + emitter.yCoord + "," + emitter.zCoord + ")\n" + "C: " + tileEntity + " (" + tileEntity.xCoord + "," + tileEntity.yCoord + "," + tileEntity.zCoord + ")\n" + "R: " + energyPath.target + " (" + energyPath.target.xCoord + "," + energyPath.target.yCoord + "," + energyPath.target.zCoord + ")");
					}

				energyPaths.add(energyPath);
			}
		}

		return energyPaths;
	}

	public List<TileEntity> discoverTargets(TileEntity emitter, boolean reverse, int lossLimit)
	{
		List<EnergyNetLocal.EnergyPath> paths = this.discover(emitter, reverse, lossLimit);
		List<TileEntity> targets = new ArrayList();

		for (EnergyNetLocal.EnergyPath path : paths)
			targets.add(path.target);

		return targets;
	}

	private List<EnergyNetLocal.EnergyTarget> getValidReceivers(TileEntity emitter, boolean reverse)
	{
		List<EnergyNetLocal.EnergyTarget> validReceivers = new ArrayList();

		for (Direction direction : directions)
			if (emitter instanceof IMetaDelegate)
			{
				IMetaDelegate meta = (IMetaDelegate) emitter;

				for (TileEntity tile : meta.getSubTiles())
				{
					TileEntity target = EnergyNet.instance.getNeighbor(tile, direction.toForgeDirection());
					if (target != emitter && target instanceof IEnergyTile && this.registeredTiles.containsKey(coords(target)))
					{
						Direction inverseDirection = direction.getInverse();
						if (reverse)
						{
							if (emitter instanceof IEnergyAcceptor && target instanceof IEnergyEmitter)
							{
								IEnergyEmitter sender = (IEnergyEmitter) target;
								IEnergyAcceptor receiver = (IEnergyAcceptor) emitter;
								if (sender.emitsEnergyTo(emitter, inverseDirection.toForgeDirection()) && receiver.acceptsEnergyFrom(target, direction.toForgeDirection()))
									validReceivers.add(new EnergyNetLocal.EnergyTarget(target, inverseDirection));
							}
						}
						else if (emitter instanceof IEnergyEmitter && target instanceof IEnergyAcceptor)
						{
							IEnergyEmitter sender = (IEnergyEmitter) emitter;
							IEnergyAcceptor receiver = (IEnergyAcceptor) target;
							if (sender.emitsEnergyTo(target, direction.toForgeDirection()) && receiver.acceptsEnergyFrom(emitter, inverseDirection.toForgeDirection()))
								validReceivers.add(new EnergyNetLocal.EnergyTarget(target, inverseDirection));
						}
					}
				}
			}
			else
			{
				TileEntity target = EnergyNet.instance.getNeighbor(emitter, direction.toForgeDirection());
				if (target instanceof IEnergyTile && this.registeredTiles.containsKey(coords(target)))
				{
					Direction inverseDirection = direction.getInverse();
					if (reverse)
					{
						if (emitter instanceof IEnergyAcceptor && target instanceof IEnergyEmitter)
						{
							IEnergyEmitter sender = (IEnergyEmitter) target;
							IEnergyAcceptor receiver = (IEnergyAcceptor) emitter;
							if (sender.emitsEnergyTo(emitter, inverseDirection.toForgeDirection()) && receiver.acceptsEnergyFrom(target, direction.toForgeDirection()))
								validReceivers.add(new EnergyNetLocal.EnergyTarget(target, inverseDirection));
						}
					}
					else if (emitter instanceof IEnergyEmitter && target instanceof IEnergyAcceptor)
					{
						IEnergyEmitter sender = (IEnergyEmitter) emitter;
						IEnergyAcceptor receiver = (IEnergyAcceptor) target;
						if (sender.emitsEnergyTo(target, direction.toForgeDirection()) && receiver.acceptsEnergyFrom(emitter, inverseDirection.toForgeDirection()))
							validReceivers.add(new EnergyNetLocal.EnergyTarget(target, inverseDirection));
					}
				}
			}

		return validReceivers;
	}

	private boolean conductorToWeak(EnergyNetLocal.EnergyPath path, int energyToSend)
	{
		if (path.minConductorBreakdownEnergy > energyToSend)
			return false;
		else
		{
			boolean flag = false;

			for (IEnergyConductor cond : path.conductors)
				if (cond.getConductorBreakdownEnergy() <= energyToSend)
				{
					flag = true;
					break;
				}

			return flag;
		}
	}

	public List<IEnergySource> discoverFirstPathOrSources(TileEntity par1)
	{
		Set<TileEntity> reached = new HashSet();
		List<IEnergySource> result = new ArrayList();
		LinkedList<TileEntity> workList = new LinkedList();
		workList.add(par1);

		while (workList.size() > 0)
		{
			TileEntity tile = workList.remove();
			if (!tile.isInvalid())
			{
				List<EnergyNetLocal.EnergyTarget> targets = this.getValidReceivers(tile, true);

				for (int i = 0; i < targets.size(); ++i)
				{
					TileEntity target = targets.get(i).tileEntity;
					if (target != par1 && !reached.contains(target))
					{
						reached.add(target);
						if (target instanceof IEnergySource)
							result.add((IEnergySource) target);

						if (target instanceof IEnergyConductor)
							workList.add(target);
					}
				}
			}
		}

		return result;
	}

	public static ChunkCoordinates coords(TileEntity par1)
	{
		return par1 == null ? null : new ChunkCoordinates(par1.xCoord, par1.yCoord, par1.zCoord);
	}

	void addPacket(EnergyNetLocal.EnergyPath link, int energy)
	{
		if (link.backupEnergyPackets.size() > 0)
			link.backupEnergyPackets = new HashMap();

		EnergyNetLocal.EnergyPacket totalPackets = link.totalEnergyPackets.get(Integer.valueOf(energy));
		if (totalPackets == null)
		{
			totalPackets = new EnergyNetLocal.EnergyPacket();
			link.totalEnergyPackets.put(Integer.valueOf(energy), totalPackets);
		}

		totalPackets.add();
		EnergyNetLocal.EnergyPacket mesurePackets = link.mesuredEnergyPackets.get(Integer.valueOf(energy));
		if (mesurePackets == null)
		{
			mesurePackets = new EnergyNetLocal.EnergyPacket();
			link.totalEnergyPackets.put(Integer.valueOf(energy), mesurePackets);
		}

		mesurePackets.add();
	}

	private void addPackets(Map<Integer, EnergyNetLocal.EnergyPacket> input, Map<Integer, EnergyNetLocal.EnergyPacket> ref)
	{
		for (Entry<Integer, EnergyNetLocal.EnergyPacket> entry : ref.entrySet())
		{
			int key = entry.getKey().intValue();
			EnergyNetLocal.EnergyPacket result = input.get(Integer.valueOf(key));
			if (result == null)
			{
				result = new EnergyNetLocal.EnergyPacket();
				input.put(Integer.valueOf(key), result);
			}

			result.combine(entry.getValue());
		}

	}

	public void onTickStart()
	{
		for (Entry<EntityLivingBase, Integer> entry : this.entityLivingToShockEnergyMap.entrySet())
		{
			EntityLivingBase target = entry.getKey();
			int damage = (entry.getValue().intValue() + 63) / 64;
			if (target.isEntityAlive() && damage > 0)
				target.attackEntityFrom(IC2DamageSource.electricity, damage);
		}

		this.entityLivingToShockEnergyMap.clear();
	}

	public void onTickEnd()
	{
		if (this.unloading.hasWork())
		{
			this.energySourceToEnergyPathMap.clearSources(this.unloading.getWork());

			for (ChunkCoordinates coord : this.unloading.getWorkEnd())
			{
				this.registeredTiles.remove(coord);
				this.update(coord.posX, coord.posY, coord.posZ);
			}

			this.unloading.clear();
		}

		if (this.waitingList.hasWork())
		{
			for (TileEntity tile : this.waitingList.getPathTiles())
			{
				List<IEnergySource> sources = this.discoverFirstPathOrSources(tile);
				if (sources.size() > 0)
					this.energySourceToEnergyPathMap.removeAll(sources);
			}

			this.waitingList.clear();
		}

		Iterator<Entry<ChunkCoordinates, IEnergySource>> iter = new LinkedHashMap(this.sources).entrySet().iterator();

		for (int z = 0; iter.hasNext(); ++z)
		{
			Entry<ChunkCoordinates, IEnergySource> entry = iter.next();
			if (entry != null && this.sources.containsKey(entry.getKey()))
			{
				IEnergySource source = entry.getValue();
				if (source != null)
				{
					int offer = DoubleMath.roundToInt(source.getOfferedEnergy(), RoundingMode.DOWN);
					if (offer >= 1)
						if (source instanceof IMultiEnergySource && ((IMultiEnergySource) source).sendMultibleEnergyPackets())
						{
							IMultiEnergySource multi = (IMultiEnergySource) source;

							for (int i = 0; i < multi.getMultibleEnergyPacketAmount(); ++i)
							{
								offer = DoubleMath.roundToInt(source.getOfferedEnergy(), RoundingMode.DOWN);
								if (offer < 1)
									break;

								int removed = offer - this.emitEnergyFrom(entry.getKey(), source, offer);
								if (removed <= 0)
									break;

								source.drawEnergy(removed);
							}
						}
						else
						{
							int removed = offer - this.emitEnergyFrom(entry.getKey(), source, offer);
							if (removed > 0)
								source.drawEnergy(removed);
						}
				}
			}
		}

	}

	public void explodeTiles(IEnergySink sink)
	{
		this.removeTile((TileEntity) sink);
		if (sink instanceof IMetaDelegate)
		{
			IMetaDelegate meta = (IMetaDelegate) sink;

    
				this.explodeMachineAt(tile, tile.xCoord, tile.yCoord, tile.zCoord);
		}
    
			this.explodeMachineAt((TileEntity) sink, ((TileEntity) sink).xCoord, ((TileEntity) sink).yCoord, ((TileEntity) sink).zCoord);

	}

	public TileEntity getTileEntity(int x, int y, int z)
	{
		ChunkCoordinates coords = new ChunkCoordinates(x, y, z);
		return this.registeredTiles.containsKey(coords) ? (TileEntity) this.registeredTiles.get(coords) : null;
	}

	public NodeStats getNodeStats(TileEntity tile)
	{
		double emitted = this.getTotalEnergyEmitted(tile);
		double received = this.getTotalEnergySunken(tile);
		double volt = Math.max(EnergyNet.instance.getTierFromPower(emitted), EnergyNet.instance.getTierFromPower(received));
		return new NodeStats(received, emitted, volt);
    
	void explodeMachineAt(int x, int y, int z)
	{
		this.explodeMachineAt(null, x, y, z);
    
    
	void explodeMachineAt(TileEntity tile, int x, int y, int z)
	{
		this.world.setBlockToAir(x, y, z);
    
		if (tile instanceof TileEntityBlock)
    

		explosion.doExplosion();
	}

	void update(int x, int y, int z)
	{
		for (ForgeDirection dir : ForgeDirection.values())
			if (this.world.blockExists(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ))
				this.world.notifyBlockOfNeighborChange(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, Blocks.air);

	}

	public void onUnload()
	{
		this.energySourceToEnergyPathMap.clear();
		this.registeredTiles.clear();
		this.sources.clear();
		this.entityLivingToShockEnergyMap.clear();
		this.unloading.clear();
		this.waitingList.clear();
	}

	public List<AxisAlignedBB> getBoxes()
	{
		List<EnergyNetLocal.BoundingBox> boxes = this.energySourceToEnergyPathMap.getBoxes();
		Set<AxisAlignedBB> result = new HashSet();

		for (EnergyNetLocal.BoundingBox box : boxes)
			result.add(box.toAxis());

		return new ArrayList(result);
	}

	static class BoundingBox
	{
		int minX;
		int minY;
		int minZ;
		int maxX;
		int maxY;
		int maxZ;
		Set<IEnergyConductor> conductors = new HashSet();
		Set<IEnergySink> sink = new HashSet();

		public BoundingBox(EnergyNetLocal.EnergyPath par1)
		{
			this.minX = par1.minX;
			this.minY = par1.minY;
			this.minZ = par1.minZ;
			this.maxX = par1.maxX;
			this.maxY = par1.maxY;
			this.maxZ = par1.maxZ;
		}

		public BoundingBox(EnergyNetLocal.EnergyPath par1, IEnergySource par2)
		{
			this.minX = par1.minX;
			this.minY = par1.minY;
			this.minZ = par1.minZ;
			this.maxX = par1.maxX;
			this.maxY = par1.maxY;
			this.maxZ = par1.maxZ;
			TileEntity tile = (TileEntity) par2;
			if (tile.xCoord < this.minX)
				this.minX = tile.xCoord;

			if (tile.yCoord < this.minY)
				this.minY = tile.yCoord;

			if (tile.zCoord < this.minZ)
				this.minZ = tile.zCoord;

			if (tile.xCoord > this.maxX)
				this.maxX = tile.xCoord;

			if (tile.yCoord > this.maxY)
				this.maxY = tile.yCoord;

			if (tile.zCoord > this.maxZ)
				this.maxZ = tile.zCoord;

			tile = par1.target;
			if (tile.xCoord < this.minX)
				this.minX = tile.xCoord;

			if (tile.yCoord < this.minY)
				this.minY = tile.yCoord;

			if (tile.zCoord < this.minZ)
				this.minZ = tile.zCoord;

			if (tile.xCoord > this.maxX)
				this.maxX = tile.xCoord;

			if (tile.yCoord > this.maxY)
				this.maxY = tile.yCoord;

			if (tile.zCoord > this.maxZ)
				this.maxZ = tile.zCoord;

		}

		public BoundingBox(List<ChunkCoordinates> coords, EnergyNetLocal local)
		{
			this.minX = Integer.MAX_VALUE;
			this.minY = Integer.MAX_VALUE;
			this.minZ = Integer.MAX_VALUE;
			this.maxX = Integer.MIN_VALUE;
			this.maxY = Integer.MIN_VALUE;
			this.maxZ = Integer.MIN_VALUE;

			for (ChunkCoordinates coord : coords)
			{
				if (this.minX > coord.posX)
					this.minX = coord.posX;

				if (this.minY > coord.posY)
					this.minY = coord.posY;

				if (this.minZ > coord.posZ)
					this.minZ = coord.posZ;

				if (this.maxX < coord.posX)
					this.maxX = coord.posX;

				if (this.maxY < coord.posY)
					this.maxY = coord.posY;

				if (this.maxZ < coord.posZ)
					this.maxZ = coord.posZ;

				TileEntity tile = local.getTileEntity(coord.posX, coord.posY, coord.posZ);
				if (tile instanceof IEnergySink)
					this.sink.add((IEnergySink) tile);

				if (tile instanceof IEnergyConductor)
					this.conductors.add((IEnergyConductor) tile);
			}

		}

		public boolean intersectsWith(EnergyNetLocal.BoundingBox par1)
		{
			return (par1.maxX >= this.minX || par1.minX <= this.maxX) && (par1.maxY >= this.minY || par1.minY <= this.maxY) && (par1.maxZ >= this.minZ || par1.minZ <= this.maxZ);
		}

		public boolean intersectsWith(TileEntity tile)
		{
			return (tile.xCoord >= this.minX || tile.xCoord <= this.maxX) && (tile.yCoord >= this.minY || tile.yCoord <= this.maxY) && (tile.zCoord >= this.minZ || tile.zCoord <= this.maxZ);
		}

		public AxisAlignedBB toAxis()
		{
			return AxisAlignedBB.getBoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ).addCoord(1.0D, 1.0D, 1.0D);
		}

		@Override
		public String toString()
		{
			return "Min: " + this.minX + ":" + this.minY + ":" + this.minZ + " Max: " + this.maxX + ":" + this.maxY + ":" + this.maxZ;
		}
	}

	static class EnergyBlockLink
	{
		Direction direction;
		double loss;

		EnergyBlockLink(Direction direction, double loss)
		{
			this.direction = direction;
			this.loss = loss;
		}
	}

	static class EnergyPacket
	{
		long count;

		public void clear()
		{
			this.count = 0L;
		}

		public void add()
		{
			this.add(1L);
		}

		public void add(long amount)
		{
			this.count += amount;
		}

		public void combine(EnergyNetLocal.EnergyPacket other)
		{
			this.count += other.count;
		}

		public long getAmount()
		{
			return this.count;
		}
	}

	static class EnergyPath
	{
		TileEntity target = null;
		Direction targetDirection;
		List<IEnergyConductor> conductors = new FilteredList();
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		double loss = 0.0D;
		int minInsulationEnergyAbsorption = Integer.MAX_VALUE;
		int minInsulationBreakdownEnergy = Integer.MAX_VALUE;
		int minConductorBreakdownEnergy = Integer.MAX_VALUE;
		long totalEnergyConducted = 0L;
		HashMap<Integer, EnergyNetLocal.EnergyPacket> totalEnergyPackets = new HashMap();
		HashMap<Integer, EnergyNetLocal.EnergyPacket> mesuredEnergyPackets = new HashMap();
		HashMap<Integer, EnergyNetLocal.EnergyPacket> backupEnergyPackets = new HashMap();
	}

	static class EnergyPathMap
	{
		Map<IEnergySource, FilteredList<EnergyNetLocal.EnergyPath>> senderPath = new HashMap();
		Map<EnergyNetLocal.EnergyPath, IEnergySource> pathToSender = new HashMap();

		public void put(IEnergySource par1, FilteredList<EnergyNetLocal.EnergyPath> par2)
		{
			this.senderPath.put(par1, par2);

			for (int i = 0; i < par2.size(); ++i)
				this.pathToSender.put(par2.get(i), par1);

		}

		public boolean containsKey(Object par1)
		{
			return this.senderPath.containsKey(par1);
		}

		public List<EnergyNetLocal.EnergyPath> get(Object par1)
		{
			return this.senderPath.get(par1);
		}

		public void remove(Object par1)
		{
			List<EnergyNetLocal.EnergyPath> paths = this.senderPath.remove(par1);
			if (paths != null)
				for (int i = 0; i < paths.size(); ++i)
					this.pathToSender.remove(paths.get(i));

		}

		public void removeAll(List<IEnergySource> par1)
		{
			for (int i = 0; i < par1.size(); ++i)
				this.remove(par1.get(i));

		}

		public void clearSources(List<EnergyNetLocal.BoundingBox> boxes)
		{
			List<IEnergySource> sources = new FilteredList();

			for (Entry<EnergyNetLocal.EnergyPath, IEnergySource> entry : this.pathToSender.entrySet())
				if (!sources.contains(entry.getValue()))
				{
					EnergyNetLocal.EnergyPath path = entry.getKey();
					EnergyNetLocal.BoundingBox pathBox = new EnergyNetLocal.BoundingBox(path, entry.getValue());

					for (EnergyNetLocal.BoundingBox box : boxes)
						if (pathBox.intersectsWith(box))
						{
							if (box.sink.contains(path.target))
							{
								sources.add(entry.getValue());
								break;
							}

							boolean found = false;

							for (IEnergyConductor con : box.conductors)
								if (path.conductors.contains(con))
								{
									found = true;
									break;
								}

							if (found)
							{
								sources.add(entry.getValue());
								break;
							}
						}
				}

			this.removeAll(sources);
		}

		public List<EnergyNetLocal.EnergyPath> getPaths(IEnergyAcceptor par1)
		{
			List<EnergyNetLocal.EnergyPath> paths = new FilteredList();

			for (IEnergySource source : this.getSources(par1))
				if (this.containsKey(source))
					paths.addAll(this.get(source));

			return paths;
		}

		public List<IEnergySource> getSources(IEnergyAcceptor par1)
		{
			List<IEnergySource> source = new FilteredList();

			for (Entry<EnergyNetLocal.EnergyPath, IEnergySource> entry : this.pathToSender.entrySet())
				if (!source.contains(entry.getValue()))
				{
					EnergyNetLocal.EnergyPath path = entry.getKey();
					EnergyNetLocal.BoundingBox box = new EnergyNetLocal.BoundingBox(path, entry.getValue());
					if (box.intersectsWith((TileEntity) par1) && (par1 instanceof IEnergyConductor && path.conductors.contains(par1) || par1 instanceof IEnergySink && path.target == par1))
						source.add(entry.getValue());
				}

			return source;
		}

		public void clear()
		{
			this.senderPath.clear();
			this.pathToSender.clear();
		}

		public List<EnergyNetLocal.BoundingBox> getBoxes()
		{
			Set<EnergyNetLocal.BoundingBox> boxes = new HashSet();

			for (Entry<EnergyNetLocal.EnergyPath, IEnergySource> path : this.pathToSender.entrySet())
				boxes.add(new EnergyNetLocal.BoundingBox(path.getKey(), path.getValue()));

			return new ArrayList(boxes);
		}
	}

	static class EnergyTarget
	{
		TileEntity tileEntity;
		Direction direction;

		EnergyTarget(TileEntity tileEntity, Direction direction)
		{
			this.tileEntity = tileEntity;
			this.direction = direction;
		}
	}

	static class PathLogic
	{
		List<TileEntity> tiles = new FilteredList();

		public boolean contains(TileEntity par1)
		{
			return this.tiles.contains(par1);
		}

		public void add(TileEntity par1)
		{
			this.tiles.add(par1);
		}

		public void remove(TileEntity par1)
		{
			this.tiles.remove(par1);
		}

		public void clear()
		{
			this.tiles.clear();
		}

		public TileEntity getRepresentingTile()
		{
			return this.tiles.isEmpty() ? null : (TileEntity) this.tiles.get(0);
		}
	}

	class UnloadLogic
	{
		List<ChunkCoordinates> coords = new FilteredList();

		public boolean contains(ChunkCoordinates par1)
		{
			return this.coords.contains(par1);
		}

		public void add(ChunkCoordinates par1)
		{
			this.coords.add(par1);
		}

		public void remove(ChunkCoordinates par1)
		{
			this.coords.remove(par1);
		}

		public void clear()
		{
			this.coords.clear();
		}

		public List<ChunkCoordinates> getAll()
		{
			return this.coords;
		}
	}

	class UnloadingList
	{
		List<EnergyNetLocal.UnloadLogic> logics = new ArrayList();

		public void onLoaded(ChunkCoordinates coords)
		{
			if (!this.logics.isEmpty())
			{
				List<ChunkCoordinates> toRecalculate = new ArrayList();

				for (int i = 0; i < this.logics.size(); ++i)
				{
					EnergyNetLocal.UnloadLogic logic = this.logics.get(i);
					if (logic.contains(coords))
					{
						logic.remove(coords);
						toRecalculate.addAll(logic.getAll());
						this.logics.remove(i--);
					}
				}

				for (int i = 0; i < ((List) toRecalculate).size(); ++i)
				{
					ChunkCoordinates coord = toRecalculate.get(i);
					TileEntity tile = EnergyNetLocal.this.getTileEntity(coord.posX, coord.posY, coord.posZ);
					if (tile != null)
						this.onUnloaded(EnergyNetLocal.this.getValidReceivers(tile, true), coord, tile);
				}

			}
		}

		public void onUnloaded(List<EnergyNetLocal.EnergyTarget> around, ChunkCoordinates coords, TileEntity tile)
		{
			if (!around.isEmpty() && !this.logics.isEmpty())
			{
				boolean found = false;
				List<EnergyNetLocal.UnloadLogic> combine = new ArrayList();

				for (int i = 0; i < this.logics.size(); ++i)
				{
					EnergyNetLocal.UnloadLogic logic = this.logics.get(i);
					if (logic.contains(coords))
					{
						found = true;
						if (tile instanceof IEnergyConductor)
							combine.add(logic);
					}
					else
						for (EnergyNetLocal.EnergyTarget target : around)
						{
							if (logic.contains(coords))
							{
								found = true;
								if (tile instanceof IEnergyConductor)
									combine.add(logic);
								break;
							}

							if (logic.contains(EnergyNetLocal.coords(target.tileEntity)))
							{
								found = true;
								logic.add(coords);
								if (target.tileEntity instanceof IEnergyConductor)
									combine.add(logic);
								break;
							}
						}
				}

				if (combine.size() > 1 && tile instanceof IEnergyConductor)
				{
					EnergyNetLocal.UnloadLogic newLogic = EnergyNetLocal.this.new UnloadLogic();

					for (EnergyNetLocal.UnloadLogic logic : combine)
					{
						this.logics.remove(logic);

						for (ChunkCoordinates toMove : logic.getAll())
							if (!newLogic.contains(toMove))
								newLogic.add(toMove);

						logic.clear();
					}

					this.logics.add(newLogic);
				}

				if (!found)
					this.createNewPath(coords);

			}
			else
				this.createNewPath(coords);
		}

		public void createNewPath(ChunkCoordinates coords)
		{
			EnergyNetLocal.UnloadLogic logic = EnergyNetLocal.this.new UnloadLogic();
			logic.add(coords);
			this.logics.add(logic);
		}

		public boolean contains(ChunkCoordinates coords)
		{
			if (this.logics.isEmpty())
				return false;
			else
			{
				for (EnergyNetLocal.UnloadLogic logic : this.logics)
					if (logic.contains(coords))
						return true;

				return false;
			}
		}

		public void clear()
		{
			if (!this.logics.isEmpty())
			{
				for (int i = 0; i < this.logics.size(); ++i)
					this.logics.get(i).clear();

				this.logics.clear();
			}
		}

		public List<EnergyNetLocal.BoundingBox> getWork()
		{
			List<EnergyNetLocal.BoundingBox> boxes = new ArrayList(this.logics.size());

			for (EnergyNetLocal.UnloadLogic logic : this.logics)
				boxes.add(new EnergyNetLocal.BoundingBox(logic.getAll(), EnergyNetLocal.this));

			return boxes;
		}

		public List<ChunkCoordinates> getWorkEnd()
		{
			Set<ChunkCoordinates> coords = new HashSet();

			for (EnergyNetLocal.UnloadLogic logic : this.logics)
				coords.addAll(logic.getAll());

			return new ArrayList(coords);
		}

		public boolean hasWork()
		{
			return this.logics.size() > 0;
		}
	}

	class WaitingList
	{
		List<EnergyNetLocal.PathLogic> paths = new ArrayList();

		public void onTileEntityAdded(List<EnergyNetLocal.EnergyTarget> around, TileEntity tile)
		{
			if (!around.isEmpty() && !this.paths.isEmpty())
			{
				boolean found = false;
				List<EnergyNetLocal.PathLogic> logics = new ArrayList();

				for (int i = 0; i < this.paths.size(); ++i)
				{
					EnergyNetLocal.PathLogic logic = this.paths.get(i);
					if (logic.contains(tile))
					{
						found = true;
						if (tile instanceof IEnergyConductor)
							logics.add(logic);
					}
					else
						for (EnergyNetLocal.EnergyTarget target : around)
						{
							if (logic.contains(tile))
							{
								found = true;
								if (tile instanceof IEnergyConductor)
									logics.add(logic);
								break;
							}

							if (logic.contains(target.tileEntity))
							{
								found = true;
								logic.add(tile);
								if (target.tileEntity instanceof IEnergyConductor)
									logics.add(logic);
								break;
							}
						}
				}

				if (logics.size() > 1 && tile instanceof IEnergyConductor)
				{
					EnergyNetLocal.PathLogic newLogic = new EnergyNetLocal.PathLogic();

					for (EnergyNetLocal.PathLogic logic : logics)
					{
						this.paths.remove(logic);

						for (TileEntity toMove : logic.tiles)
							if (!newLogic.contains(toMove))
								newLogic.add(toMove);

						logic.clear();
					}

					this.paths.add(newLogic);
				}

				if (!found)
					this.createNewPath(tile);

			}
			else
				this.createNewPath(tile);
		}

		public void onTileEntityRemoved(TileEntity par1)
		{
			if (!this.paths.isEmpty())
			{
				List<TileEntity> toRecalculate = new ArrayList();

				for (int i = 0; i < this.paths.size(); ++i)
				{
					EnergyNetLocal.PathLogic logic = this.paths.get(i);
					if (logic.contains(par1))
					{
						logic.remove(par1);
						toRecalculate.addAll(logic.tiles);
						this.paths.remove(i--);
					}
				}

				for (int i = 0; i < ((List) toRecalculate).size(); ++i)
				{
					TileEntity tile = toRecalculate.get(i);
					this.onTileEntityAdded(EnergyNetLocal.this.getValidReceivers(tile, true), tile);
				}

			}
		}

		public void createNewPath(TileEntity par1)
		{
			EnergyNetLocal.PathLogic logic = new EnergyNetLocal.PathLogic();
			logic.add(par1);
			this.paths.add(logic);
		}

		public void clear()
		{
			if (!this.paths.isEmpty())
			{
				for (int i = 0; i < this.paths.size(); ++i)
					this.paths.get(i).clear();

				this.paths.clear();
			}
		}

		public boolean hasWork()
		{
			return this.paths.size() > 0;
		}

		public List<TileEntity> getPathTiles()
		{
			List<TileEntity> tiles = new ArrayList();

			for (int i = 0; i < this.paths.size(); ++i)
			{
				TileEntity tile = this.paths.get(i).getRepresentingTile();
				if (tile != null)
					tiles.add(tile);
			}

			return tiles;
		}
	}
}
