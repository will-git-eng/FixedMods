package thaumcraft.common.lib.events;

import ru.will.git.eventhelper.util.FastUtils;
import ru.will.git.thaumcraft.EventConfig;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.entities.EntitySpecialItem;
import thaumcraft.common.golems.seals.SealHandler;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockBamf;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.devices.TileArcaneEar;
import thaumcraft.common.world.ThaumcraftWorldGenerator;
import thaumcraft.common.world.aura.AuraHandler;
import thaumcraft.common.world.aura.AuraThread;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

@EventBusSubscriber
public class ServerEvents
{
	long lastcheck = 0L;
	static HashMap<Integer, Integer> serverTicks = new HashMap<>();
	public static ConcurrentHashMap<Integer, AuraThread> auraThreads = new ConcurrentHashMap<>();
	DecimalFormat myFormatter = new DecimalFormat("#######.##");
	public static HashMap<Integer, LinkedBlockingQueue<ServerEvents.BreakData>> breakList = new HashMap<>();
	public static HashMap<Integer, LinkedBlockingQueue<ServerEvents.VirtualSwapper>> swapList = new HashMap<>();
	public static HashMap<Integer, ArrayList<ChunkPos>> chunksToGenerate = new HashMap<>();
	public static final Predicate<ServerEvents.SwapperPredicate> DEFAULT_PREDICATE = new Predicate<ServerEvents.SwapperPredicate>()
	{
		@Override
		public boolean apply(@Nullable ServerEvents.SwapperPredicate pred)
		{
			return true;
		}
	};
	private static HashMap<Integer, LinkedBlockingQueue<ServerEvents.RunnableEntry>> serverRunList = new HashMap<>();
	private static LinkedBlockingQueue<ServerEvents.RunnableEntry> clientRunList = new LinkedBlockingQueue<>();

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void clientWorldTick(ClientTickEvent event)
	{
		if (event.side != Side.SERVER)
			if (event.phase == Phase.END && !clientRunList.isEmpty())
			{
				LinkedBlockingQueue<RunnableEntry> temp = new LinkedBlockingQueue<>();

				while (!clientRunList.isEmpty())
				{
					RunnableEntry current = clientRunList.poll();
					if (current != null)
						if (current.delay > 0)
						{
							--current.delay;
							temp.offer(current);
						}
						else
							try
							{
								current.runnable.run();
							}
							catch (Exception ignored)
							{
							}
				}

				while (!temp.isEmpty())
				{
					clientRunList.offer(temp.poll());
				}
			}
	}

	@SubscribeEvent
	public static void worldTick(WorldTickEvent event)
	{
		if (event.side != Side.CLIENT)
		{
			int dim = event.world.provider.getDimension();
			if (event.phase == Phase.START)
			{
				if (!auraThreads.containsKey(dim) && AuraHandler.getAuraWorld(dim) != null)
				{
					AuraThread at = new AuraThread(dim);
					Thread thread = new Thread(at);
					thread.start();
					auraThreads.put(dim, at);
				}
			}
			else
			{
				if (!serverTicks.containsKey(dim))
					serverTicks.put(dim, 0);

				LinkedBlockingQueue<ServerEvents.RunnableEntry> rlist = serverRunList.get(dim);
				if (rlist == null)
					serverRunList.put(dim, new LinkedBlockingQueue<>());
				else if (!rlist.isEmpty())
				{
					LinkedBlockingQueue<ServerEvents.RunnableEntry> temp = new LinkedBlockingQueue<>();

					while (!rlist.isEmpty())
					{
						ServerEvents.RunnableEntry current = rlist.poll();
						if (current != null)
							if (current.delay > 0)
							{
								--current.delay;
								temp.offer(current);
							}
							else
								try
								{
									current.runnable.run();
								}
								catch (Exception ignored)
								{
								}
					}

					while (!temp.isEmpty())
					{
						rlist.offer(temp.poll());
					}
				}

				int ticks = serverTicks.get(dim);
				tickChunkRegeneration(event);
				tickBlockSwap(event.world);
				tickBlockBreak(event.world);
				ArrayList<Integer[]> nbe = TileArcaneEar.noteBlockEvents.get(dim);
				if (nbe != null)
					nbe.clear();

				if (ticks % 20 == 0)
				{
					CopyOnWriteArrayList<ChunkPos> dc = AuraHandler.dirtyChunks.get(dim);
					if (dc != null && dc.size() > 0)
					{
						for (ChunkPos pos : dc)
						{
							event.world.markChunkDirty(pos.getBlock(5, 5, 5), null);
						}

						dc.clear();
					}

					if (AuraHandler.riftTrigger.containsKey(dim))
					{
						if (!ModConfig.CONFIG_MISC.wussMode)
							EntityFluxRift.createRift(event.world, AuraHandler.riftTrigger.get(dim));

						AuraHandler.riftTrigger.remove(dim);
					}

					TaskHandler.clearSuspendedOrExpiredTasks(event.world);
				}

				SealHandler.tickSealEntities(event.world);
				serverTicks.put(dim, ticks + 1);
			}

		}
	}

	public static void tickChunkRegeneration(WorldTickEvent event)
	{
		int dim = event.world.provider.getDimension();
		int count = 0;
		ArrayList<ChunkPos> chunks = chunksToGenerate.get(dim);
		if (chunks != null && chunks.size() > 0)
			for (int a = 0; a < 10; ++a)
			{
				chunks = chunksToGenerate.get(dim);
				if (chunks == null || chunks.size() <= 0)
					break;

				++count;
				ChunkPos loc = chunks.get(0);
				long worldSeed = event.world.getSeed();
				Random fmlRandom = new Random(worldSeed);
				long xSeed = fmlRandom.nextLong() >> 3;
				long zSeed = fmlRandom.nextLong() >> 3;
				fmlRandom.setSeed(xSeed * (long) loc.x + zSeed * (long) loc.z ^ worldSeed);
				ThaumcraftWorldGenerator.INSTANCE.worldGeneration(fmlRandom, loc.x, loc.z, event.world, false);
				chunks.remove(0);
				chunksToGenerate.put(dim, chunks);
			}

		if (count > 0)
			FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, "[Thaumcraft] Regenerated " + count + " chunks. " + Math.max(0, chunks.size()) + " chunks left");

	}

	private static void tickBlockSwap(World world)
	{
		int dim = world.provider.getDimension();
		LinkedBlockingQueue<ServerEvents.VirtualSwapper> queue = swapList.get(dim);
		LinkedBlockingQueue<ServerEvents.VirtualSwapper> queue2 = new LinkedBlockingQueue<>();
		if (queue != null)
		{
			while (!queue.isEmpty())
			{
				ServerEvents.VirtualSwapper vs = queue.poll();
				if (vs != null)
				{
					
					if (!FastUtils.isValidRealPlayer(vs.player))
						continue;
					

					IBlockState bs = world.getBlockState(vs.pos);
					boolean allow = bs.getBlockHardness(world, vs.pos) >= 0.0F;
					if (vs.source != null && vs.source instanceof IBlockState && vs.source != bs || vs.source != null && vs.source instanceof Material && vs.source != bs.getMaterial())
						allow = false;

					if (vs.visCost > 0.0F && AuraHelper.getVis(world, vs.pos) < vs.visCost)
						allow = false;

					if (world.canMineBlockBody(vs.player, vs.pos) && allow && (vs.target == null || vs.target.isEmpty() || !vs.target.isItemEqual(new ItemStack(bs.getBlock(), 1, bs.getBlock().getMetaFromState(bs)))) && !ForgeEventFactory.onPlayerBlockPlace(vs.player, new BlockSnapshot(world, vs.pos, bs), EnumFacing.UP, EnumHand.MAIN_HAND).isCanceled() && vs.allowSwap.apply(new ServerEvents.SwapperPredicate(world, vs.player, vs.pos)))
					{
						int slot = -1;
						if (vs.consumeTarget && vs.target != null && !vs.target.isEmpty())
							slot = InventoryUtils.getPlayerSlotFor(vs.player, vs.target);
						else
							slot = 1;

						if (vs.player.capabilities.isCreativeMode)
							slot = 1;

						boolean matches = false;
						if (vs.source instanceof Material)
							matches = bs.getMaterial() == vs.source;

						if (vs.source instanceof IBlockState)
							matches = bs == vs.source;

						if ((vs.source == null || matches) && slot >= 0)
						{
							if (!vs.player.capabilities.isCreativeMode)
							{
								if (vs.consumeTarget)
									vs.player.inventory.decrStackSize(slot, 1);

								if (vs.pickup)
								{
									List<ItemStack> ret = new ArrayList<>();
									if (vs.silk && bs.getBlock().canSilkHarvest(world, vs.pos, bs, vs.player))
									{
										ItemStack itemstack = BlockUtils.getSilkTouchDrop(bs);
										if (itemstack != null && !itemstack.isEmpty())
											ret.add(itemstack);
									}
									else
										ret = bs.getBlock().getDrops(world, vs.pos, bs, vs.fortune);

									if (ret.size() > 0)
										for (ItemStack is : ret)
										{
											if (!vs.player.inventory.addItemStackToInventory(is))
												world.spawnEntity(new EntityItem(world, (double) vs.pos.getX() + 0.5D, (double) vs.pos.getY() + 0.5D, (double) vs.pos.getZ() + 0.5D, is));
										}
								}

								if (vs.visCost > 0.0F)
									ThaumcraftApi.internalMethods.drainVis(world, vs.pos, vs.visCost, false);
							}

							if (vs.target != null && !vs.target.isEmpty())
							{
								Block tb = Block.getBlockFromItem(vs.target.getItem());
								if (tb != null && tb != Blocks.AIR)
									world.setBlockState(vs.pos, tb.getStateFromMeta(vs.target.getItemDamage()), 3);
								else
								{
									world.setBlockToAir(vs.pos);
									EntitySpecialItem entityItem = new EntitySpecialItem(world, (double) vs.pos.getX() + 0.5D, (double) vs.pos.getY() + 0.1D, (double) vs.pos.getZ() + 0.5D, vs.target.copy());
									entityItem.motionY = 0.0D;
									entityItem.motionX = 0.0D;
									entityItem.motionZ = 0.0D;
									world.spawnEntity(entityItem);
								}
							}
							else
								world.setBlockToAir(vs.pos);

							if (vs.fx)
								PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockBamf(vs.pos, vs.color, true, vs.fancy, null), new TargetPoint(world.provider.getDimension(), (double) vs.pos.getX(), (double) vs.pos.getY(), (double) vs.pos.getZ(), 32.0D));

							if (vs.lifespan > 0)
								for (int xx = -1; xx <= 1; ++xx)
								{
									for (int yy = -1; yy <= 1; ++yy)
									{
										for (int zz = -1; zz <= 1; ++zz)
										{
											matches = false;
											if (vs.source instanceof Material)
											{
												IBlockState bb = world.getBlockState(vs.pos.add(xx, yy, zz));
												matches = bb.getBlock().getMaterial(bb) == vs.source;
											}

											if (vs.source instanceof IBlockState)
												matches = world.getBlockState(vs.pos.add(xx, yy, zz)) == vs.source;

											if ((xx != 0 || yy != 0 || zz != 0) && matches && BlockUtils.isBlockExposed(world, vs.pos.add(xx, yy, zz)))
												queue2.offer(new VirtualSwapper(vs.pos.add(xx, yy, zz), vs.source, vs.target, vs.consumeTarget, vs.lifespan - 1, vs.player, vs.fx, vs.fancy, vs.color, vs.pickup, vs.silk, vs.fortune, vs.allowSwap, vs.visCost));
										}
									}
								}
						}
					}
				}
			}

			swapList.put(dim, queue2);
		}

	}

	private static void tickBlockBreak(World world)
	{
		int dim = world.provider.getDimension();
		LinkedBlockingQueue<ServerEvents.BreakData> queue = breakList.get(dim);
		LinkedBlockingQueue<ServerEvents.BreakData> queue2 = new LinkedBlockingQueue<>();
		if (queue != null)
		{
			while (!queue.isEmpty())
			{
				ServerEvents.BreakData vs = queue.poll();
				if (vs != null)
				{
					IBlockState bs = world.getBlockState(vs.pos);

					
					if (bs == vs.source && !EventConfig.focusBreakBlackList.contains(bs))
					{
						
						if (!FastUtils.isValidRealPlayer(vs.player))
							continue;
						

						if ((vs.visCost <= 0.0F || AuraHelper.getVis(world, vs.pos) >= vs.visCost) && world.canMineBlockBody(vs.player, vs.pos) && bs.getBlockHardness(world, vs.pos) >= 0.0F)
						{
							if (vs.fx)
								world.sendBlockBreakProgress(vs.pos.hashCode(), vs.pos, (int) ((1.0F - vs.durabilityCurrent / vs.durabilityMax) * 10.0F));

							vs.durabilityCurrent -= vs.strength;
							if (vs.durabilityCurrent <= 0.0F)
							{
								BlockUtils.harvestBlock(world, vs.player, vs.pos, true, vs.silk, vs.fortune, false);
								if (vs.fx)
									world.sendBlockBreakProgress(vs.pos.hashCode(), vs.pos, -1);

								if (vs.visCost > 0.0F)
									ThaumcraftApi.internalMethods.drainVis(world, vs.pos, vs.visCost, false);
							}
							else
								queue2.offer(new BreakData(vs.strength, vs.durabilityCurrent, vs.durabilityMax, vs.pos, vs.source, vs.player, vs.fx, vs.silk, vs.fortune, vs.visCost));
						}
					}
					else if (vs.fx)
						world.sendBlockBreakProgress(vs.pos.hashCode(), vs.pos, -1);
				}
			}

			breakList.put(dim, queue2);
		}

	}

	public static void addSwapper(World world, BlockPos pos, Object source, ItemStack target, boolean consumeTarget, int life, EntityPlayer player, boolean fx, boolean fancy, int color, boolean pickup, boolean silk, int fortune, Predicate<ServerEvents.SwapperPredicate> allowSwap, float visCost)
	{
		int dim = world.provider.getDimension();
		LinkedBlockingQueue<ServerEvents.VirtualSwapper> queue = swapList.get(dim);
		if (queue == null)
		{
			swapList.put(dim, new LinkedBlockingQueue<>());
			queue = swapList.get(dim);
		}

		queue.offer(new ServerEvents.VirtualSwapper(pos, source, target, consumeTarget, life, player, fx, fancy, color, pickup, silk, fortune, allowSwap, visCost));
		swapList.put(dim, queue);
	}

	public static void addBreaker(final World world, final BlockPos pos, final IBlockState source, final EntityPlayer player, final boolean fx, final boolean silk, final int fortune, final float str, final float durabilityCurrent, final float durabilityMax, int delay, final float vis, final Runnable run)
	{
		int dim = world.provider.getDimension();
		if (delay > 0)
			addRunnableServer(world, new Runnable()
			{
				@Override
				public void run()
				{
					ServerEvents.addBreaker(world, pos, source, player, fx, silk, fortune, str, durabilityCurrent, durabilityMax, 0, vis, run);
				}
			}, delay);
		else
		{
			LinkedBlockingQueue<ServerEvents.BreakData> queue = breakList.get(dim);
			if (queue == null)
			{
				breakList.put(dim, new LinkedBlockingQueue<>());
				queue = breakList.get(dim);
			}

			queue.offer(new ServerEvents.BreakData(str, durabilityCurrent, durabilityMax, pos, source, player, fx, silk, fortune, vis));
			breakList.put(dim, queue);
			if (run != null)
				run.run();
		}

	}

	public static void addRunnableServer(World world, Runnable runnable, int delay)
	{
		if (!world.isRemote)
		{
			LinkedBlockingQueue<ServerEvents.RunnableEntry> rlist = serverRunList.get(world.provider.getDimension());
			if (rlist == null)
				serverRunList.put(world.provider.getDimension(), rlist = new LinkedBlockingQueue<>());

			rlist.add(new ServerEvents.RunnableEntry(runnable, delay));
		}
	}

	public static void addRunnableClient(World world, Runnable runnable, int delay)
	{
		if (world.isRemote)
			clientRunList.add(new RunnableEntry(runnable, delay));
	}

	public static class BreakData
	{
		float strength = 0.0F;
		float durabilityCurrent = 1.0F;
		float durabilityMax = 1.0F;
		IBlockState source;
		BlockPos pos;
		EntityPlayer player = null;
		boolean fx;
		boolean silk;
		int fortune;
		float visCost;

		public BreakData(float strength, float durabilityCurrent, float durabilityMax, BlockPos pos, IBlockState source, EntityPlayer player, boolean fx, boolean silk, int fortune, float vis)
		{
			this.strength = strength;
			this.source = source;
			this.pos = pos;
			this.player = player;
			this.fx = fx;
			this.silk = silk;
			this.fortune = fortune;
			this.durabilityCurrent = durabilityCurrent;
			this.durabilityMax = durabilityMax;
			this.visCost = vis;
		}
	}

	public static class RunnableEntry
	{
		Runnable runnable;
		int delay;

		public RunnableEntry(Runnable runnable, int delay)
		{
			this.runnable = runnable;
			this.delay = delay;
		}
	}

	public static class SwapperPredicate
	{
		public World world;
		public EntityPlayer player;
		public BlockPos pos;

		public SwapperPredicate(World world, EntityPlayer player, BlockPos pos)
		{
			this.world = world;
			this.player = player;
			this.pos = pos;
		}
	}

	public static class VirtualSwapper
	{
		int color;
		boolean fancy;
		Predicate<ServerEvents.SwapperPredicate> allowSwap;
		int lifespan = 0;
		BlockPos pos;
		Object source;
		ItemStack target;
		EntityPlayer player = null;
		boolean fx;
		boolean silk;
		boolean pickup;
		boolean consumeTarget;
		int fortune;
		float visCost;

		VirtualSwapper(BlockPos pos, Object source, ItemStack t, boolean consumeTarget, int life, EntityPlayer p, boolean fx, boolean fancy, int color, boolean pickup, boolean silk, int fortune, Predicate<ServerEvents.SwapperPredicate> allowSwap, float cost)
		{
			this.pos = pos;
			this.source = source;
			this.target = t;
			this.lifespan = life;
			this.player = p;
			this.consumeTarget = consumeTarget;
			this.fx = fx;
			this.fancy = fancy;
			this.allowSwap = allowSwap;
			this.silk = silk;
			this.fortune = fortune;
			this.pickup = pickup;
			this.color = color;
			this.visCost = cost;
		}
	}
}
