package ic2.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import ru.will.git.ic2.FakePlayerContainer;
import ru.will.git.ic2.FakePlayerContainerWorld;
import ru.will.git.ic2.ModUtils;

import ic2.api.event.ExplosionEvent;
import ic2.api.tile.ExplosionWhitelist;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ExplosionIC2 extends Explosion
{
	private final World worldObj;
	private final Entity exploder;
	private final double explosionX;
	private final double explosionY;
	private final double explosionZ;
	private final int mapHeight;
	private final float power;
	private final float explosionDropRate;
	private final ExplosionIC2.Type type;
	private final int radiationRange;
	private final EntityLivingBase igniter;
	private final Random rng;
	private final double maxDistance;
	private final int areaSize;
	private final int areaX;
	private final int areaZ;
	private final DamageSource damageSource;
	private final List<ExplosionIC2.EntityDamage> entitiesInRange;
	private final long[][] destroyedBlockPositions;
	private ChunkCache chunkCache;
	private static final double dropPowerLimit = 8.0D;
	private static final double damageAtDropPowerLimit = 32.0D;
	private static final double accelerationAtDropPowerLimit = 0.7D;
	private static final double motionLimit = 60.0D;
	private static final int secondaryRayCount = 5;
    
	public final FakePlayerContainer fake;

	private static final void onBlockExploded(Block block, World world, BlockPos pos, Explosion explosion)
	{
		if (block != null)
			block.onBlockExploded(world, pos, explosion);
    

	public ExplosionIC2(World world, Entity entity, double x, double y, double z, float power, float drop)
	{
		this(world, entity, x, y, z, power, drop, ExplosionIC2.Type.Normal);
	}

	public ExplosionIC2(World world, Entity entity, double x, double y, double z, float power, float drop, ExplosionIC2.Type type)
	{
		this(world, entity, x, y, z, power, drop, type, (EntityLivingBase) null, 0);
	}

	public ExplosionIC2(World world, Entity entity, BlockPos pos, float power, float drop, ExplosionIC2.Type type)
	{
		this(world, entity, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, power, drop, type);
	}

	public ExplosionIC2(World world, Entity entity, double x, double y, double z, float power, float drop, ExplosionIC2.Type type, EntityLivingBase igniter, int radiationRange)
	{
		super(world, entity, x, y, z, power, false, false);
		this.rng = new Random();
		this.entitiesInRange = new ArrayList();
		this.worldObj = world;
		this.exploder = entity;
		this.explosionX = x;
		this.explosionY = y;
		this.explosionZ = z;
		this.mapHeight = IC2.getWorldHeight(world);
		this.power = power;
		this.explosionDropRate = drop;
		this.type = type;
		this.igniter = igniter;
		this.radiationRange = radiationRange;
		this.maxDistance = this.power / 0.4D;
		int maxDistanceInt = (int) Math.ceil(this.maxDistance);
		this.areaSize = maxDistanceInt * 2;
		this.areaX = Util.roundToNegInf(x) - maxDistanceInt;
		this.areaZ = Util.roundToNegInf(z) - maxDistanceInt;
		if (this.isNuclear())
			this.damageSource = IC2DamageSource.getNukeSource(this);
		else
			this.damageSource = DamageSource.causeExplosionDamage(this);

    
		this.fake = new FakePlayerContainerWorld(ModUtils.profile, this.worldObj);
		if (entity instanceof EntityPlayer)
			this.fake.setRealPlayer((EntityPlayer) entity);
		else if (igniter instanceof EntityPlayer)
    
	}

	public ExplosionIC2(World world, Entity entity, BlockPos pos, int power, float drop, ExplosionIC2.Type type)
	{
		this(world, entity, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, power, drop, type);
	}

	public void doExplosion()
	{
		if (this.power > 0.0F)
		{
			ExplosionEvent event = new ExplosionEvent(this.worldObj, this.exploder, this.getPosition(), this.power, this.igniter, this.radiationRange, this.maxDistance);
			if (!MinecraftForge.EVENT_BUS.post(event))
			{
				int range = this.areaSize / 2;
				BlockPos pos = new BlockPos(this.getPosition());
				BlockPos start = pos.add(-range, -range, -range);
				BlockPos end = pos.add(range, range, range);
				this.chunkCache = new ChunkCache(this.worldObj, start, end, 0);

				for (Entity entity : this.worldObj.getEntitiesWithinAABBExcludingEntity((Entity) null, new AxisAlignedBB(start, end)))
					if (entity instanceof EntityLivingBase || entity instanceof EntityItem)
    
						if (this.fake.cantDamage(entity))
    

						int distance = (int) (Util.square(entity.posX - this.explosionX) + Util.square(entity.posY - this.explosionY) + Util.square(entity.posZ - this.explosionZ));
						double health = getEntityHealth(entity);
						this.entitiesInRange.add(new ExplosionIC2.EntityDamage(entity, distance, health));
					}

				boolean entitiesAreInRange = !this.entitiesInRange.isEmpty();
				if (entitiesAreInRange)
					Collections.sort(this.entitiesInRange, new Comparator<ExplosionIC2.EntityDamage>()
					{
						@Override
						public int compare(ExplosionIC2.EntityDamage a, ExplosionIC2.EntityDamage b)
						{
							return a.distance - b.distance;
						}
					});

				int steps = (int) Math.ceil(3.141592653589793D / Math.atan(1.0D / this.maxDistance));
				MutableBlockPos tmpPos = new MutableBlockPos();

				for (int phi_n = 0; phi_n < 2 * steps; ++phi_n)
					for (int theta_n = 0; theta_n < steps; ++theta_n)
					{
						double phi = 6.283185307179586D / steps * phi_n;
						double theta = 3.141592653589793D / steps * theta_n;
						this.shootRay(this.explosionX, this.explosionY, this.explosionZ, phi, theta, this.power, entitiesAreInRange && phi_n % 8 == 0 && theta_n % 8 == 0, tmpPos);
					}

				for (ExplosionIC2.EntityDamage entry : this.entitiesInRange)
				{
					Entity entity = entry.entity;
					entity.attackEntityFrom(this.damageSource, (float) entry.damage);
					if (entity instanceof EntityPlayer)
					{
						EntityPlayer player = (EntityPlayer) entity;
						if (this.isNuclear() && this.igniter != null && player == this.igniter && player.getHealth() <= 0.0F)
							IC2.achievements.issueAchievement(player, "dieFromOwnNuke");
					}

					double motionSq = Util.square(entry.motionX) + Util.square(entity.motionY) + Util.square(entity.motionZ);
					double reduction = motionSq > 3600.0D ? Math.sqrt(3600.0D / motionSq) : 1.0D;
					entity.motionX += entry.motionX * reduction;
					entity.motionY += entry.motionY * reduction;
					entity.motionZ += entry.motionZ * reduction;
				}

				if (this.isNuclear() && this.radiationRange >= 1)
					for (EntityLiving entity : this.worldObj.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.explosionX - this.radiationRange, this.explosionY - this.radiationRange, this.explosionZ - this.radiationRange, this.explosionX + this.radiationRange, this.explosionY + this.radiationRange, this.explosionZ + this.radiationRange)))
						if (!ItemArmorHazmat.hasCompleteHazmat(entity))
    
							if (this.fake.cantDamage(entity))
    

							double distance = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ);
							int hungerLength = (int) (120.0D * (this.radiationRange - distance));
							int poisonLength = (int) (80.0D * (this.radiationRange / 3 - distance));
							if (hungerLength >= 0)
								entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, hungerLength, 0));

							if (poisonLength >= 0)
								IC2Potion.radiation.applyTo(entity, poisonLength, 0);
						}

				IC2.network.get(true).initiateExplosionEffect(this.worldObj, this.getPosition());
				Random rng = this.worldObj.rand;
				boolean doDrops = this.worldObj.getGameRules().getBoolean("doTileDrops");
				Map<ExplosionIC2.XZposition, Map<ItemComparableItemStack, ExplosionIC2.DropData>> blocksToDrop = new HashMap();
				this.worldObj.playSound((EntityPlayer) null, this.explosionX, this.explosionY, this.explosionZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (rng.nextFloat() - rng.nextFloat()) * 0.2F) * 0.7F);

				for (int y = 0; y < this.destroyedBlockPositions.length; ++y)
				{
					long[] bitSet = this.destroyedBlockPositions[y];
					Block block;
    
						for (int index = -2; (index = nextSetIndex(index + 2, bitSet, 2)) != -1; onBlockExploded(block, this.worldObj, tmpPos, this))
						{
						int realIndex = index / 2;
						int z = realIndex / this.areaSize;
						int x = realIndex - z * this.areaSize;
						x = x + this.areaX;
						z = z + this.areaZ;
    
						if (this.fake.cantBreak(this.worldObj, tmpPos))
						{
						block = null;
						continue;
    

						IBlockState state = this.chunkCache.getBlockState(tmpPos);
						block = state.getBlock();
						if (this.power < 20.0F)
						;

						if (doDrops && block.canDropFromExplosion(this) && getAtIndex(index, bitSet, 2) == 1)
						for (ItemStack stack : StackUtil.getDrops(this.worldObj, tmpPos, state, 0))
						if (rng.nextFloat() <= this.explosionDropRate)
						{
						ExplosionIC2.XZposition xZposition = new ExplosionIC2.XZposition(x / 2, z / 2);
						Map<ItemComparableItemStack, ExplosionIC2.DropData> map = blocksToDrop.get(xZposition);
						if (map == null)
						{
						map = new HashMap();
						blocksToDrop.put(xZposition, map);
						}

						ItemComparableItemStack isw = new ItemComparableItemStack(stack, false);
						ExplosionIC2.DropData data = map.get(isw);
						if (data == null)
						{
						data = new ExplosionIC2.DropData(stack.stackSize, y);
						map.put(isw.copy(), data);
						}
						else
						data.add(stack.stackSize, y);
						}
						}
				}

				for (Entry<ExplosionIC2.XZposition, Map<ItemComparableItemStack, ExplosionIC2.DropData>> entry : blocksToDrop.entrySet())
				{
					ExplosionIC2.XZposition xZposition = entry.getKey();

					for (Entry<ItemComparableItemStack, ExplosionIC2.DropData> entry2 : entry.getValue().entrySet())
					{
						ItemComparableItemStack isw = entry2.getKey();

						int stackSize;
						for (int count = entry2.getValue().n; count > 0; count -= stackSize)
						{
							stackSize = Math.min(count, 64);
							EntityItem entityitem = new EntityItem(this.worldObj, (xZposition.x + this.worldObj.rand.nextFloat()) * 2.0F, entry2.getValue().maxY + 0.5D, (xZposition.z + this.worldObj.rand.nextFloat()) * 2.0F, isw.toStack(stackSize));
							entityitem.setDefaultPickupDelay();
							this.worldObj.spawnEntityInWorld(entityitem);
						}
					}
				}

			}
		}
	}

	public void destroy(int x, int y, int z, boolean noDrop)
	{
		this.destroyUnchecked(x, y, z, noDrop);
	}

	private void destroyUnchecked(int x, int y, int z, boolean noDrop)
	{
		int index = (z - this.areaZ) * this.areaSize + x - this.areaX;
		index = index * 2;
		long[] array = this.destroyedBlockPositions[y];
		if (array == null)
		{
			array = makeArray(Util.square(this.areaSize), 2);
			this.destroyedBlockPositions[y] = array;
		}

		if (noDrop)
			setAtIndex(index, array, 3);
		else
			setAtIndex(index, array, 1);
	}

	private void shootRay(double x, double y, double z, double phi, double theta, double power1, boolean killEntities, MutableBlockPos tmpPos)
	{
		double deltaX = Math.sin(theta) * Math.cos(phi);
		double deltaY = Math.cos(theta);
		double deltaZ = Math.sin(theta) * Math.sin(phi);
		int step = 0;

		while (true)
		{
			int blockY = Util.roundToNegInf(y);
			if (blockY < 0 || blockY >= this.mapHeight)
				break;

			int blockX = Util.roundToNegInf(x);
			int blockZ = Util.roundToNegInf(z);
			tmpPos.setPos(blockX, blockY, blockZ);
			IBlockState state = this.chunkCache.getBlockState(tmpPos);
			Block block = state.getBlock();
			double absorption = this.getAbsorption(block, tmpPos);
			if (absorption < 0.0D)
				break;

			if (absorption > 1000.0D && !ExplosionWhitelist.isBlockWhitelisted(block))
				absorption = 0.5D;
			else
			{
				if (absorption > power1)
					break;

				if (block == Blocks.STONE || block != Blocks.AIR && !block.isAir(state, this.worldObj, tmpPos))
					this.destroyUnchecked(blockX, blockY, blockZ, power1 > 8.0D);
			}

			if (killEntities && (step + 4) % 8 == 0 && !this.entitiesInRange.isEmpty() && power1 >= 0.25D)
				this.damageEntities(x, y, z, step, power1);

			if (absorption > 10.0D)
				for (int i = 0; i < 5; ++i)
					this.shootRay(x, y, z, this.rng.nextDouble() * 2.0D * 3.141592653589793D, this.rng.nextDouble() * 3.141592653589793D, absorption * 0.4D, false, tmpPos);

			power1 -= absorption;
			x += deltaX;
			y += deltaY;
			z += deltaZ;
			++step;
		}

	}

	private double getAbsorption(Block block, BlockPos pos)
	{
		double ret = 0.5D;
		if (block != Blocks.AIR && !block.isAir(block.getDefaultState(), this.worldObj, pos))
		{
			if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && this.type != ExplosionIC2.Type.Normal)
				++ret;
			else
			{
				float resistance = block.getExplosionResistance(this.worldObj, pos, this.exploder, this);
				if (resistance < 0.0F)
					return resistance;

				double extra = (resistance + 4.0F) * 0.3D;
				if (this.type != ExplosionIC2.Type.Heat)
					ret += extra;
				else
					ret += extra * 6.0D;
			}

			return ret;
		}
		else
			return ret;
	}

	private void damageEntities(double x, double y, double z, int step, double power)
	{
		int index;
		if (step != 4)
		{
			int distanceMin = Util.square(step - 5);
			int indexStart = 0;
			int indexEnd = this.entitiesInRange.size() - 1;

			while (true)
			{
				index = (indexStart + indexEnd) / 2;
				int distance = this.entitiesInRange.get(index).distance;
				if (distance < distanceMin)
					indexStart = index + 1;
				else if (distance > distanceMin)
					indexEnd = index - 1;
				else
					indexEnd = index;

				if (indexStart >= indexEnd)
					break;
			}
		}
		else
			index = 0;

		int distanceMax = Util.square(step + 5);

		for (int i = index; i < this.entitiesInRange.size(); ++i)
		{
			ExplosionIC2.EntityDamage entry = this.entitiesInRange.get(i);
			if (entry.distance >= distanceMax)
				break;

			Entity entity = entry.entity;
			if (Util.square(entity.posX - x) + Util.square(entity.posY - y) + Util.square(entity.posZ - z) <= 25.0D)
			{
				double damage = 4.0D * power;
				entry.damage += damage;
				entry.health -= damage;
				double dx = entity.posX - this.explosionX;
				double dy = entity.posY - this.explosionY;
				double dz = entity.posZ - this.explosionZ;
				double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
				entry.motionX += dx / distance * 0.0875D * power;
				entry.motionY += dy / distance * 0.0875D * power;
				entry.motionZ += dz / distance * 0.0875D * power;
				if (entry.health <= 0.0D)
				{
					entity.attackEntityFrom(this.damageSource, (float) entry.damage);
					if (!entity.isEntityAlive())
					{
						this.entitiesInRange.remove(i);
						--i;
					}
				}
			}
		}

	}

	@Override
	public EntityLivingBase getExplosivePlacedBy()
	{
		return this.igniter;
	}

	private boolean isNuclear()
	{
		return this.type == ExplosionIC2.Type.Nuclear;
	}

	private static double getEntityHealth(Entity entity)
	{
		return entity instanceof EntityItem ? 5.0D : Double.POSITIVE_INFINITY;
	}

	private static long[] makeArray(int size, int step)
	{
		return new long[(size * step + 8 - step) / 8];
	}

	private static int nextSetIndex(int start, long[] array, int step)
	{
		int offset = start % 8;

		for (int i = start / 8; i < array.length; ++i)
		{
			long aval = array[i];

			for (int j = offset; j < 8; j += step)
			{
				int val = (int) (aval >> j & (1 << step) - 1);
				if (val != 0)
					return i * 8 + j;
			}

			offset = 0;
		}

		return -1;
	}

	private static int getAtIndex(int index, long[] array, int step)
	{
		return (int) (array[index / 8] >>> index % 8 & (1 << step) - 1);
	}

	private static void setAtIndex(int index, long[] array, int value)
	{
		array[index / 8] |= value << index % 8;
	}

	private static class DropData
	{
		int n;
		int maxY;

		DropData(int n1, int y)
		{
			this.n = n1;
			this.maxY = y;
		}

		public ExplosionIC2.DropData add(int n1, int y)
		{
			this.n += n1;
			if (y > this.maxY)
				this.maxY = y;

			return this;
		}
	}

	private static class EntityDamage
	{
		final Entity entity;
		final int distance;
		double health;
		double damage;
		double motionX;
		double motionY;
		double motionZ;

		EntityDamage(Entity entity, int distance, double health)
		{
			this.entity = entity;
			this.distance = distance;
			this.health = health;
		}
	}

	public static enum Type
	{
		Normal,
		Heat,
		Nuclear;
	}

	private static class XZposition
	{
		int x;
		int z;

		XZposition(int x1, int z1)
		{
			this.x = x1;
			this.z = z1;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof ExplosionIC2.XZposition))
				return false;
			else
			{
				ExplosionIC2.XZposition xZposition = (ExplosionIC2.XZposition) obj;
				return xZposition.x == this.x && xZposition.z == this.z;
			}
		}

		@Override
		public int hashCode()
		{
			return this.x * 31 ^ this.z;
		}
	}
}
