package chylex.hee.entity.item;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.entity.fx.FXType;
import chylex.hee.mechanics.orb.OrbAcquirableItems;
import chylex.hee.mechanics.orb.OrbSpawnableMobs;
import chylex.hee.mechanics.orb.WeightedItem;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C21EffectEntity;
import chylex.hee.system.logging.Log;
import chylex.hee.system.util.BlockPosM;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerWorld;
import ru.will.git.hee.EventConfig;
import ru.will.git.hee.ModUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class EntityItemInstabilityOrb extends EntityItem
{
	    
	public final FakePlayerContainer fake = new FakePlayerContainerEntity(ModUtils.profile, this);

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		this.fake.writeToNBT(nbt);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.fake.readFromNBT(nbt);
	}

	@Override
	public void func_145799_b(String playerName)
	{
		super.func_145799_b(playerName);
		if (this.worldObj != null)
		{
			EntityPlayer player = this.worldObj.getPlayerEntityByName(playerName);
			if (player != null)
				this.fake.setProfile(player.getGameProfile());
		}
	}
	    

	public EntityItemInstabilityOrb(World world)
	{
		super(world);
	}

	public EntityItemInstabilityOrb(World world, double x, double y, double z, ItemStack is)
	{
		super(world, x, y, z, is);

		for (int a = 0; a < is.stackSize - 1; a++)
		{
			ItemStack newIS = is.copy();
			newIS.stackSize = 1;
			EntityItem item = new EntityItemInstabilityOrb(world, x, y, z, newIS);
			item.delayBeforeCanPickup = 40;
			world.spawnEntityInWorld(item);
		}

		is.stackSize = 1;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (!this.worldObj.isRemote)
		{
			this.age += 1 + this.rand.nextInt(2 + this.rand.nextInt(4)) * this.rand.nextInt(3);
			if (this.age >= 666)
				this.detonate();
		}
		else
		{
			int chance = this.age / 17 - 7;
			for (int a = 0; a < Math.min(7, chance <= 0 ? this.rand.nextInt(Math.abs(chance) + 1) == 0 ? 1 : 0 : chance); a++)
			{
				HardcoreEnderExpansion.fx.instability(this);
			}

			if (this.rand.nextInt(3 + Math.max(0, (int) ((680F - this.age) / 70F))) == 0)
				this.worldObj.playSound(this.posX, this.posY, this.posZ, "random.pop", 0.15F + this.rand.nextFloat() * 0.1F, 0.7F + this.rand.nextFloat() * 0.6F, false);
		}
	}

	private void detonate()
	{
		if (this.rand.nextInt(6) == 0)
		{
			    
			if (!this.worldObj.isRemote)
			    
			{
				ExplosionOrb explosion = new ExplosionOrb(this.worldObj, this, this.posX, this.posY, this.posZ, 2.8F + this.rand.nextFloat() * 0.8F);
				explosion.doExplosionA();
				explosion.doExplosionB(true);
				PacketPipeline.sendToAllAround(this, 64D, new C21EffectEntity(FXType.Entity.ORB_EXPLOSION, this.posX, this.posY, this.posZ, 0F, explosion.explosionSize));
			}
		}

		else if (EventConfig.instabilityOrbSpawnMobs && this.rand.nextInt(6) == 0)
		{
			Class<?> cls = null;
			int ele = this.rand.nextInt(OrbSpawnableMobs.classList.size());

			for (Class<?> aClassList : OrbSpawnableMobs.classList)
			{
				cls = aClassList;
				if (--ele < 0)
					break;
			}

			try
			{
				Entity e = (Entity) cls.getConstructor(World.class).newInstance(this.worldObj);
				e.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rand.nextFloat() * 360F - 180F, 0F);
				this.worldObj.spawnEntityInWorld(e);

				PacketPipeline.sendToAllAround(this, 64D, new C21EffectEntity(FXType.Entity.ORB_TRANSFORMATION, e));
			}
			catch (Exception ex)
			{
				Log.throwable(ex, "Error spawning entity $0 in EntityItemInstabilityOrb", cls == null ? "<null>" : cls.getSimpleName());
			}
		}

		else if (EventConfig.instabilityOrbSpawnItems)
		{
			WeightedItem item = OrbAcquirableItems.getRandomItem(this.worldObj, this.rand);

			if (item == null)
			{ // list is empty
				String[] list = { ChestGenHooks.DUNGEON_CHEST, ChestGenHooks.BONUS_CHEST, ChestGenHooks.MINESHAFT_CORRIDOR, ChestGenHooks.VILLAGE_BLACKSMITH, ChestGenHooks.PYRAMID_DESERT_CHEST, ChestGenHooks.PYRAMID_JUNGLE_CHEST, ChestGenHooks.STRONGHOLD_LIBRARY, ChestGenHooks.STRONGHOLD_CORRIDOR };

				WeightedRandomChestContent[] content = ChestGenHooks.getItems(list[this.rand.nextInt(list.length)], this.rand);
				if (content.length == 0)
					return;

				ItemStack is = content[this.rand.nextInt(content.length)].theItemId;
				item = new WeightedItem(is.getItem(), is.getItemDamage(), 1);
			}

			int meta = item.getDamageValues()[this.rand.nextInt(item.getDamageValues().length)];
			if (meta == 32767)
				meta = 0;

			EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, new ItemStack(item.getItem(), 1, meta));
			entityitem.motionX = entityitem.motionY = entityitem.motionZ = 0D;
			entityitem.delayBeforeCanPickup = 10;
			this.worldObj.spawnEntityInWorld(entityitem);

			PacketPipeline.sendToAllAround(this, 64D, new C21EffectEntity(FXType.Entity.ORB_TRANSFORMATION, this.posX, this.posY, this.posZ, 0.25F, 0.4F));
		}

		this.setDead();
	}

	@Override
	public boolean combineItems(EntityItem item)
	{
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if (source.isExplosion())
			if (this.rand.nextInt(6) != 0)
			{
				this.age -= 10 - this.rand.nextInt(80);
				return false;
			}

		return super.attackEntityFrom(source, amount);
	}

	public static final class ExplosionOrb extends Explosion
	{
		private final World worldObj;
		private final int dist = 16;
		private final Map<EntityPlayer, Vec3> hurtPlayers = new HashMap<EntityPlayer, Vec3>();

		    
		public final FakePlayerContainer fake;
		    

		public ExplosionOrb(World world, Entity sourceEntity, double x, double y, double z, float power)
		{
			super(world, sourceEntity, x, y, z, power);
			this.worldObj = world;
			this.isSmoking = true;
			this.isFlaming = false;

			    
			this.fake = new FakePlayerContainerWorld(ModUtils.profile, world);
			if (sourceEntity instanceof EntityItemInstabilityOrb)
				this.fake.setParent(((EntityItemInstabilityOrb) sourceEntity).fake);
			    
		}

		@Override
		public void doExplosionA()
		{
			float explosionSizeBackup = this.explosionSize;

			HashSet<ChunkPosition> affectedBlocks = new HashSet<ChunkPosition>();
			double tempX, tempY, tempZ, distX, distY, distZ, totalDist;
			BlockPosM testPos = new BlockPosM();

			    
			if (EventConfig.instabilityOrbGrief)
				    
				for (int x = 0; x < this.dist; ++x)
				{
					for (int y = 0; y < this.dist; ++y)
					{
						for (int z = 0; z < this.dist; ++z)
						{
							if (x == 0 || x == this.dist - 1 || y == 0 || y == this.dist - 1 || z == 0 || z == this.dist - 1)
							{
								distX = x / (this.dist - 1F) * 2F - 1F;
								distY = y / (this.dist - 1F) * 2F - 1F;
								distZ = z / (this.dist - 1F) * 2F - 1F;
								totalDist = Math.sqrt(distX * distX + distY * distY + distZ * distZ);

								distX /= totalDist;
								distY /= totalDist;
								distZ /= totalDist;

								float affectedDistance = this.explosionSize * (0.7F + this.worldObj.rand.nextFloat() * 0.6F);
								tempX = this.explosionX;
								tempY = this.explosionY;
								tempZ = this.explosionZ;

								for (float mp = 0.3F; affectedDistance > 0F; affectedDistance -= mp * 0.75F)
								{
									testPos.set(tempX, tempY, tempZ);
									Block block = testPos.getBlock(this.worldObj);

									if (block.getMaterial() != Material.air)
									{
										float resistance = this.exploder != null ? this.exploder.func_145772_a(this, this.worldObj, testPos.x, testPos.y, testPos.z, block) : block.getExplosionResistance(this.exploder, this.worldObj, testPos.x, testPos.y, testPos.z, this.explosionX, this.explosionY, this.explosionZ);
										affectedDistance -= (resistance + 0.3F) * mp;
									}

									if (affectedDistance > 0F && (this.exploder == null || this.exploder.func_145774_a(this, this.worldObj, testPos.x, testPos.y, testPos.z, block, affectedDistance)))
										    
										if (!this.fake.cantBreak(testPos.x, testPos.y, testPos.z))
											    
											affectedBlocks.add(new ChunkPosition(testPos.x, testPos.y, testPos.z));

									tempX += distX * mp;
									tempY += distY * mp;
									tempZ += distZ * mp;
								}
							}
						}
					}
				}

			this.affectedBlockPositions.addAll(affectedBlocks);
			this.explosionSize *= 2F;

			int minX = MathHelper.floor_double(this.explosionX - this.explosionSize - 1D), maxX = MathHelper.floor_double(this.explosionX + this.explosionSize + 1D);
			int minY = MathHelper.floor_double(this.explosionY - this.explosionSize - 1D), maxY = MathHelper.floor_double(this.explosionY + this.explosionSize + 1D);
			int minZ = MathHelper.floor_double(this.explosionZ - this.explosionSize - 1D), maxZ = MathHelper.floor_double(this.explosionZ + this.explosionSize + 1D);

			List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this.exploder, AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
			Vec3 locationVec = Vec3.createVectorHelper(this.explosionX, this.explosionY, this.explosionZ);

			for (int a = 0; a < entities.size(); ++a)
			{
				Entity entity = entities.get(a);
				double entityDist = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / this.explosionSize;

				if (entityDist <= 1D)
				{
					tempX = entity.posX - this.explosionX;
					tempY = entity.posY + entity.getEyeHeight() - this.explosionY;
					tempZ = entity.posZ - this.explosionZ;
					totalDist = MathHelper.sqrt_double(tempX * tempX + tempY * tempY + tempZ * tempZ);

					if (totalDist != 0D)
					{
						    
						if (this.fake.cantDamage(entity))
							continue;
						    

						tempX /= totalDist;
						tempY /= totalDist;
						tempZ /= totalDist;

						double blastPower = (1D - entityDist) * this.worldObj.getBlockDensity(locationVec, entity.boundingBox);

						if (this.canDamageEntity(entity))
							entity.attackEntityFrom(DamageSource.setExplosionSource(this), (int) ((blastPower * blastPower + blastPower) / 2D * 8D * this.explosionSize + 1D));
						else
							continue;

						double knockbackMp = EnchantmentProtection.func_92092_a(entity, blastPower);
						entity.motionX += tempX * knockbackMp;
						entity.motionY += tempY * knockbackMp;
						entity.motionZ += tempZ * knockbackMp;

						if (entity instanceof EntityPlayer)
							this.hurtPlayers.put((EntityPlayer) entity, Vec3.createVectorHelper(tempX * blastPower, tempY * blastPower, tempZ * blastPower));
					}
				}
			}

			this.explosionSize = explosionSizeBackup;
		}

		    
		@Override
		public void doExplosionB(boolean smoking)
		{
			if (!EventConfig.instabilityOrbGrief)
				this.affectedBlockPositions.clear();
			super.doExplosionB(smoking);
		}
		    

		@Override
		public Map func_77277_b()
		{
			return this.hurtPlayers;
		}

		private boolean canDamageEntity(Entity entity)
		{
			return !(entity instanceof EntityLiving);
		}
	}
}
