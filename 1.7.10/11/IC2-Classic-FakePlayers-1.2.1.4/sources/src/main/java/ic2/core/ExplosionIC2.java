package ic2.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerWorld;
import ru.will.git.ic2.ModUtils;

import ic2.api.tile.ExplosionWhitelist;
import ic2.core.item.armor.ItemArmorHazmat;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class ExplosionIC2
{
	private Random ExplosionRNG;
	private World worldObj;
	private int mapHeight;
	public double explosionX;
	public double explosionY;
	public double explosionZ;
	public Entity exploder;
	public float power;
	public float explosionDropRate;
	public float explosionDamage;
	public DamageSource damageSource;
	public String igniter;
	public List<EntityLivingBase> entitiesInRange;
	public Map vecMap;
	public Map<ChunkPosition, Boolean> destroyedBlockPositions;
	private double dropPowerLimit;
	private int secondaryRayCount;
    
    

	public ExplosionIC2(World world, Entity entity, double d, double d1, double d2, float power, float drop, float entitydamage, DamageSource damagesource)
	{
		this.dropPowerLimit = 8.0D;
		this.secondaryRayCount = 5;
		this.ExplosionRNG = new Random();
		this.vecMap = new HashMap();
		this.destroyedBlockPositions = new HashMap();
		this.worldObj = world;
		this.mapHeight = IC2.getWorldHeight(world);
		this.exploder = entity;
		this.power = power;
		this.explosionDropRate = drop;
		this.explosionDamage = entitydamage;
		this.explosionX = d;
		this.explosionY = d1;
		this.explosionZ = d2;
		this.damageSource = damagesource;
		this.fakeExplosion = new Explosion(world, entity, d2, d1, d2, power);
		if (damagesource == null)
    
		this.fake = new FakePlayerContainerWorld(ModUtils.profile, this.worldObj);
		if (entity instanceof EntityPlayer)
    

	}

	public ExplosionIC2(World world, Entity entity, double d, double d1, double d2, float power, float drop, float entitydamage)
	{
		this(world, entity, d, d1, d2, power, drop, entitydamage, (DamageSource) null);
	}

	public ExplosionIC2(World world, Entity entity, double d, double d1, double d2, float power, float drop, float entitydamage, DamageSource damagesource, String igniter)
	{
		this(world, entity, d, d1, d2, power, drop, entitydamage, damagesource);
		this.igniter = igniter;
	}

	public void doExplosion()
	{
		if (this.power > 0.0F)
		{
			double maxDistance = this.power / 0.4D;
			this.entitiesInRange = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(this.explosionX - maxDistance, this.explosionY - maxDistance, this.explosionZ - maxDistance, this.explosionX + maxDistance, this.explosionY + maxDistance, this.explosionZ + maxDistance));
			int steps = (int) Math.ceil(3.141592653589793D / Math.atan(1.0D / maxDistance));

			for (int phi_n = 0; phi_n < 2 * steps; ++phi_n)
				for (int theta_n = 0; theta_n < steps; ++theta_n)
				{
					double phi = 6.283185307179586D / steps * phi_n;
					double theta = 3.141592653589793D / steps * theta_n;
					this.shootRay(this.explosionX, this.explosionY, this.explosionZ, phi, theta, this.power, phi_n % 8 == 0 && theta_n % 8 == 0);
				}

			if (this.damageSource == IC2DamageSource.nuke)
				for (EntityLivingBase entity : (Iterable<EntityLivingBase>) this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(this.explosionX - 100.0D, this.explosionY - 100.0D, this.explosionZ - 100.0D, this.explosionX + 100.0D, this.explosionY + 100.0D, this.explosionZ + 100.0D)))
					if (!ItemArmorHazmat.hasCompleteHazmat(entity))
    
						if (this.fake.cantDamage(entity))
    

						double distance = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ);
						int hungerLength = (int) (120.0D * (100.0D - distance));
						int poisonLength = (int) (80.0D * (30.0D - distance));
						if (hungerLength >= 0)
							entity.addPotionEffect(new PotionEffect(Potion.hunger.id, hungerLength, 0));

						if (poisonLength >= 0)
							entity.addPotionEffect(new PotionEffect(IC2Potion.radiation.id, poisonLength, 0));
					}

			IC2.network.get().initiateExplosionEffect(this.worldObj, this.explosionX, this.explosionY, this.explosionZ, this.damageSource == IC2DamageSource.nuke);
			Map<XZposition, Map<ItemWithMeta, DropData>> blocksToDrop = new HashMap();

			for (Entry<ChunkPosition, Boolean> entry : this.destroyedBlockPositions.entrySet())
			{
				int x = entry.getKey().chunkPosX;
				int y = entry.getKey().chunkPosY;
				int z = entry.getKey().chunkPosZ;
				Block blockId = this.worldObj.getBlock(x, y, z);
				if (blockId != null)
    
					if (this.fake.cantBreak(x, y, z))
    

					if (entry.getValue().booleanValue())
					{
						double effectX = x + this.worldObj.rand.nextFloat();
						double effectY = y + this.worldObj.rand.nextFloat();
						double effectZ = z + this.worldObj.rand.nextFloat();
						double var10000 = effectX - this.explosionX;
						double d2 = effectY - this.explosionY;
						double d3 = effectZ - this.explosionZ;
						double effectDistance = MathHelper.sqrt_double(d3 * d3 + d2 * d2 + d3 * d3);
						d3 = d3 / effectDistance;
						d2 = d2 / effectDistance;
						d3 = d3 / effectDistance;
						double d4 = 0.5D / (effectDistance / this.power + 0.1D);
						d4 = d4 * (this.worldObj.rand.nextFloat() * this.worldObj.rand.nextFloat() + 0.3F);
						d3 = d3 * d4;
						d2 = d2 * d4;
						d3 = d3 * d4;
						this.worldObj.spawnParticle("explode", (effectX + this.explosionX) / 2.0D, (effectY + this.explosionY) / 2.0D, (effectZ + this.explosionZ) / 2.0D, d3, d2, d3);
						this.worldObj.spawnParticle("smoke", effectX, effectY, effectZ, d3, d2, d3);
						if (this.worldObj.rand.nextFloat() <= this.explosionDropRate)
						{
							int meta = this.worldObj.getBlockMetadata(x, y, z);

							for (ItemStack itemStack : blockId.getDrops(this.worldObj, x, y, z, meta, 0))
							{
								XZposition xZposition = new XZposition(x / 2, z / 2);
								if (!blocksToDrop.containsKey(xZposition))
									blocksToDrop.put(xZposition, new HashMap());

								Map<ItemWithMeta, DropData> map = blocksToDrop.get(xZposition);
								ItemWithMeta itemWithMeta = new ItemWithMeta(itemStack.getItem(), itemStack.getItemDamage());
								if (!map.containsKey(itemWithMeta))
									map.put(itemWithMeta, new DropData(itemStack.stackSize, y));
								else
									map.put(itemWithMeta, map.get(itemWithMeta).add(itemStack.stackSize, y));
							}
						}
					}

					this.worldObj.setBlockToAir(x, y, z);
					blockId.onBlockDestroyedByExplosion(this.worldObj, x, y, z, this.fakeExplosion);
				}
			}

			for (Entry<XZposition, Map<ItemWithMeta, DropData>> entry2 : blocksToDrop.entrySet())
			{
				XZposition xZposition2 = entry2.getKey();

				for (Entry entry3 : entry2.getValue().entrySet())
				{
					ItemWithMeta itemWithMeta2 = (ItemWithMeta) entry3.getKey();

					int stackSize;
					for (int count = ((DropData) entry3.getValue()).n; count > 0; count -= stackSize)
					{
						stackSize = Math.min(count, 64);
						EntityItem entityitem = new EntityItem(this.worldObj, (xZposition2.x + this.worldObj.rand.nextFloat()) * 2.0D, ((DropData) entry3.getValue()).maxY + 0.5D, (xZposition2.z + this.worldObj.rand.nextFloat()) * 2.0D, new ItemStack(itemWithMeta2.itemId, stackSize, itemWithMeta2.metaData));
						entityitem.delayBeforeCanPickup = 10;
						this.worldObj.spawnEntityInWorld(entityitem);
					}
				}
			}

		}
	}

	private void shootRay(double x, double y, double z, double phi, double theta, double power, boolean killEntities)
	{
		double deltaX = Math.sin(theta) * Math.cos(phi);
		double deltaY = Math.cos(theta);
		double deltaZ = Math.sin(theta) * Math.sin(phi);

		while (true)
		{
			boolean isAir = this.worldObj.isAirBlock((int) x, (int) y, (int) z);
			Block blockId = this.worldObj.getBlock((int) x, (int) y, (int) z);
			double absorption = 0.5D;
			if (!isAir)
				absorption += (blockId.getExplosionResistance(this.exploder, this.worldObj, (int) x, (int) y, (int) z, this.explosionX, this.explosionY, this.explosionZ) + 4.0D) * 0.3D;

			if (absorption > 1000.0D && !ExplosionWhitelist.isBlockWhitelisted(blockId))
				absorption = 0.5D;
			else
			{
				if (absorption > power)
					break;

				if (!isAir)
				{
					ChunkPosition position = new ChunkPosition((int) x, (int) y, (int) z);
					if (!this.destroyedBlockPositions.containsKey(position) || power > 8.0D && this.destroyedBlockPositions.get(position).booleanValue())
						this.destroyedBlockPositions.put(position, Boolean.valueOf(power <= 8.0D));
				}
			}

			if (killEntities)
			{
				Iterator<EntityLivingBase> it = this.entitiesInRange.iterator();

				while (it.hasNext())
				{
					EntityLivingBase entity = it.next();
					if ((entity.posX - x) * (entity.posX - x) + (entity.posY - y) * (entity.posY - y) + (entity.posZ - z) * (entity.posZ - z) <= 25.0D)
    
						if (this.fake.cantDamage(entity))
    

						double dx = entity.posX - this.explosionX;
						double dy = entity.posY - this.explosionY;
						double dz = entity.posZ - this.explosionZ;
						double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
						double distanceFactor = power / 2.0D / (Math.pow(distance, 0.8D) + 1.0D);
						entity.attackEntityFrom(this.damageSource, (int) Math.pow(distanceFactor * 3.0D, 2.0D));
						if (this.damageSource == IC2DamageSource.nuke && entity instanceof EntityPlayer && this.igniter != null && ((EntityPlayer) entity).getGameProfile().getName().equals(this.igniter) && entity.getHealth() <= 0.0F)
							IC2.achievements.issueAchievement((EntityPlayer) entity, "dieFromOwnNuke");

						dx = dx / distance;
						dy = dy / distance;
						dz = dz / distance;
						entity.motionX += dx * distanceFactor;
						entity.motionY += dy * distanceFactor;
						entity.motionZ += dz * distanceFactor;
						it.remove();
					}
				}
			}

			if (absorption > 10.0D)
				for (int i = 0; i < 5; ++i)
					this.shootRay(x, y, z, this.ExplosionRNG.nextDouble() * 2.0D * 3.141592653589793D, this.ExplosionRNG.nextDouble() * 3.141592653589793D, absorption * 0.4D, false);

			power -= absorption;
			x += deltaX;
			y += deltaY;
			z += deltaZ;
			if (y <= 0.0D || y >= this.mapHeight)
				break;
		}

	}

	static class DropData
	{
		int n;
		int maxY;

		DropData(int n, int y)
		{
			this.n = n;
			this.maxY = y;
		}

		public DropData add(int n, int y)
		{
			this.n += n;
			if (y > this.maxY)
				this.maxY = y;

			return this;
		}
	}

	static class ItemWithMeta
	{
		Item itemId;
		int metaData;

		ItemWithMeta(Item itemId, int metaData)
		{
			this.itemId = itemId;
			this.metaData = metaData;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof ItemWithMeta))
				return false;
			else
			{
				ItemWithMeta itemWithMeta = (ItemWithMeta) obj;
				return itemWithMeta.itemId == this.itemId && itemWithMeta.metaData == this.metaData;
			}
		}

		@Override
		public int hashCode()
		{
			return this.itemId.hashCode() * 31 ^ this.metaData;
		}
	}

	static class XZposition
	{
		int x;
		int z;

		XZposition(int x, int z)
		{
			this.x = x;
			this.z = z;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof XZposition))
				return false;
			else
			{
				XZposition xZposition = (XZposition) obj;
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
