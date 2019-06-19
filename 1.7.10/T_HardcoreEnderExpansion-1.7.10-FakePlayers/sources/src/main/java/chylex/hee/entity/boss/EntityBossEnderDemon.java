package chylex.hee.entity.boss;

import chylex.hee.entity.GlobalMobData;
import chylex.hee.entity.mob.EntityMobAngryEnderman;
import chylex.hee.entity.mob.util.DamageSourceMobUnscaled;
import chylex.hee.entity.weather.EntityWeatherLightningBoltDemon;
import chylex.hee.init.BlockList;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C05CustomWeather;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.util.DragonUtil;
import chylex.hee.system.util.MathUtil;
import ru.will.git.hee.ModUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class EntityBossEnderDemon extends EntityFlying implements IBossDisplayData, GlobalMobData.IIgnoreEnderGoo
{
	private static final PotionEffect endermanStrength;
	private byte healthRegenTimer;
	private byte lightningStartCounter;
	private byte lightningCounter;
	private byte endermanSpawnTimer;
	private byte obsidianSpawnTimer;
	private EntityPlayerMP lastAttacker;
	private EntityPlayer lightningTarget;

	public EntityBossEnderDemon(World world)
	{
		super(world);
		this.healthRegenTimer = 10;
		this.lightningStartCounter = 30;
		this.endermanSpawnTimer = 25;
		this.obsidianSpawnTimer = 69;
		this.setSize(2.0F, 5.0F);
		this.experienceValue = 70;
		this.scoreValue = 100;
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.rotationPitch = -90.0F;
	}

	public EntityBossEnderDemon(World world, double x, double y, double z)
	{
		this(world);
		this.setPosition(x, y, z);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(400.0D);
	}

	@Override
	protected void updateEntityActionState()
	{
		if (this.lastAttacker != null && (this.lastAttacker.isDead || !this.lastAttacker.playerNetServerHandler.func_147362_b().isChannelOpen()))
			this.lastAttacker = null;

		float health = this.getHealth();
		if (health > 0.0F)
		{
			if (this.lightningTarget == null && --this.healthRegenTimer < 0 && health < this.getMaxHealth())
				this.setHealth(health + 1.0F);

			if (this.healthRegenTimer < 0)
				this.healthRegenTimer = 9;

			if (this.lightningStartCounter <= 0)
			{
				this.lightningStartCounter = 40;
				this.lightningCounter = 0;
				this.lightningTarget = this.lastAttacker == null ? this.worldObj.getClosestPlayerToEntity(this, 512.0D) : this.lastAttacker;
			}

			if (this.lightningTarget != null)
			{
				if (this.ticksExisted % 18 == 0)
				{
					double xx = this.lightningTarget.posX + (this.rand.nextDouble() - 0.5D) * 1.5D;
					double yy = this.lightningTarget.posY;
					double zz = this.lightningTarget.posZ + (this.rand.nextDouble() - 0.5D) * 1.5D;
					this.lightningTarget.attackEntityFrom(new DamageSourceMobUnscaled(this), DamageSourceMobUnscaled.getDamage(ModCommonProxy.opMobs ? 7.0F : 4.0F, this.worldObj.difficultySetting));
					EntityWeatherEffect bolt = new EntityWeatherLightningBoltDemon(this.worldObj, xx, yy, zz, this, false);
					this.worldObj.weatherEffects.add(bolt);
					PacketPipeline.sendToAllAround(bolt, 512.0D, new C05CustomWeather(bolt, (byte) 0));
					if (++this.lightningCounter >= 6)
						this.lightningTarget = null;
				}
			}
			else
			{
				if (--this.endermanSpawnTimer < -100)
				{
					this.endermanSpawnTimer = (byte) (125 - this.rand.nextInt(40));
					if (this.obsidianSpawnTimer < -105)
						this.obsidianSpawnTimer = (byte) (this.obsidianSpawnTimer + 20);

					for (EntityPlayer player : (Iterable<? extends EntityPlayer>) this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(128.0D, 64.0D, 128.0D)))
					{
						for (int attempt = 0; attempt < 40; ++attempt)
						{
							double ang = this.rand.nextDouble() * 3.141592653589793D * 2.0D;
							double len = 3.5D + this.rand.nextDouble() * 2.0D;
							int ix = MathUtil.floor(player.posX + Math.cos(ang) * len);
							int iz = MathUtil.floor(player.posZ + Math.sin(ang) * len);

							for (int iy = MathUtil.floor(player.posY) - 2; (double) iy < player.posY + 3.0D; ++iy)
							{
								if (this.worldObj.isAirBlock(ix, iy, iz) && this.worldObj.isAirBlock(ix, iy + 1, iz) && this.worldObj.isAirBlock(ix, iy + 2, iz))
								{
									for (int a = 0; a < (ModCommonProxy.opMobs ? 4 : 3); ++a)
									{
										EntityMobAngryEnderman enderman = new EntityMobAngryEnderman(this.worldObj, (double) ix + this.rand.nextDouble(), (double) iy, (double) iz + this.rand.nextDouble());
										enderman.rotationYaw = this.rand.nextFloat() * 360.0F;
										enderman.setTarget(player);
										enderman.addPotionEffect(endermanStrength);
										this.worldObj.spawnEntityInWorld(enderman);
										attempt = 999;
									}

									EntityWeatherEffect bolt = new EntityWeatherLightningBoltDemon(this.worldObj, (double) ix + 0.5D, (double) iy, (double) iz + 0.5D, this, false);
									this.worldObj.addWeatherEffect(bolt);
									PacketPipeline.sendToAllAround(bolt, 512.0D, new C05CustomWeather(bolt, (byte) 0));
									break;
								}
							}
						}
					}
				}

				if (--this.obsidianSpawnTimer < -120)
				{
					this.obsidianSpawnTimer = (byte) (20 + this.rand.nextInt(80));

					    
					if (ModUtils.canMobGrief(this.worldObj))
					    
					{
						List<EntityPlayer> list = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(128.0D, 64.0D, 128.0D));
						if (!list.isEmpty())
						{
							EntityPlayer player = list.get(this.rand.nextInt(list.size()));
							int attempt = 0;

							for (int placed = 0; attempt < 25 && placed < 12 + this.worldObj.difficultySetting.getDifficultyId() * 2; ++attempt)
							{
								int xx = MathUtil.floor(player.posX) + this.rand.nextInt(9) - 4;
								int yy = MathUtil.floor(player.posY) + 9 + this.rand.nextInt(6);
								int zz = MathUtil.floor(player.posZ) + this.rand.nextInt(9) - 4;
								if (this.worldObj.isAirBlock(xx, yy, zz) && this.worldObj.isAirBlock(xx, yy - 1, zz))
								{
									this.worldObj.setBlock(xx, yy, zz, BlockList.obsidian_falling, 0, 3);
									++placed;
								}

								if (placed > 5 && this.rand.nextInt(15) <= 1)
									break;
							}
						}
					}
				}
			}

			if (!this.worldObj.isRemote)
			{
				boolean hasBlockBelow = false;
				int ix = MathUtil.floor(this.posX);
				int iz = MathUtil.floor(this.posZ);

				for (int yy = MathUtil.floor(this.posY); (double) yy > this.posY - 22.0D; --yy)
				{
					if (!this.worldObj.isAirBlock(ix, yy, iz))
					{
						hasBlockBelow = true;
						break;
					}
				}

				if (hasBlockBelow)
				{
					this.motionY *= 0.9D;
					if (Math.abs(this.motionY) < 0.04D)
						this.motionY = 0.0D;
				}
				else
				{
					this.motionY = -0.3D;
					++this.endermanSpawnTimer;
					++this.obsidianSpawnTimer;
					this.rotationPitch = -90.0F;
					this.lastAttacker = (EntityPlayerMP) this.worldObj.getClosestPlayerToEntity(this, 512.0D);
				}

				if (this.lastAttacker != null)
				{
					double diffX = this.posX - this.lastAttacker.posX;
					double diffY = this.posY - this.lastAttacker.posY;
					double diffZ = this.posZ - this.lastAttacker.posZ;
					double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
					this.rotationYaw = DragonUtil.rotateSmoothly(this.rotationYaw, (float) (Math.atan2(diffZ, diffX) * 180.0D / 3.141592653589793D) - 270.0F, 2.0F);
					this.rotationPitch = DragonUtil.rotateSmoothly(this.rotationPitch, (float) -(Math.atan2(diffY, distance) * 180.0D / 3.141592653589793D), 8.0F);
				}
				else if (this.rotationPitch < 0.0F)
					this.rotationPitch -= 2.0F;

			}
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		amount = source == DamageSource.drown ? amount : Math.min(5.0F, amount * 0.45F);
		if (super.attackEntityFrom(source, amount))
		{
			if (source == DamageSource.drown)
			{
				this.lightningTarget = null;
				this.healthRegenTimer = 60;
				return true;
			}
			if (source.getEntity() instanceof EntityPlayerMP)
				this.lastAttacker = (EntityPlayerMP) source.getEntity();

			if (this.lightningTarget == null)
				this.lightningStartCounter = (byte) (this.lightningStartCounter - (int) amount);

			return true;
		}
		return false;
	}

	@Override
	protected void onDeathUpdate()
	{
		this.worldObj.spawnParticle("hugeexplosion", this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, 0.0D, 0.0D, 0.0D);
		if (!this.worldObj.isRemote)
		{
			if (++this.deathTime > 99)
			{
				DragonUtil.spawnXP(this, this.getExperiencePoints(this.attackingPlayer));
				this.setDead();
			}

			if (this.deathTime < 81 && this.deathTime % 10 == 0)
			{
				EntityWeatherEffect bolt = new EntityWeatherLightningBoltDemon(this.worldObj, this.posX, this.posY, this.posZ, this, false);
				this.worldObj.weatherEffects.add(bolt);
				PacketPipeline.sendToAllAround(bolt, 512.0D, new C05CustomWeather(bolt, (byte) 0));
			}

		}
	}

	@Override
	public void knockBack(Entity entity, float damage, double xPower, double zPower)
	{
	}

	@Override
	public void addVelocity(double xVelocity, double yVelocity, double zVelocity)
	{
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data)
	{
		this.setDead();
		this.motionY = 7.0D;
		return super.onSpawnWithEgg(data);
	}

	public boolean isDoingLightningAttack()
	{
		return this.lightningTarget != null;
	}

	@Override
	protected String getHurtSound()
	{
		return "hardcoreenderexpansion:mob.enderdemon.hurt";
	}

	@Override
	protected String getDeathSound()
	{
		return "hardcoreenderexpansion:mob.enderdemon.death";
	}

	@Override
	protected float getSoundVolume()
	{
		return 5.5F;
	}

	@Override
	public String getCommandSenderName()
	{
		return this.hasCustomNameTag() ? this.getCustomNameTag() : StatCollector.translateToLocal("entity.enderDemon.name");
	}

	@Override
	protected void despawnEntity()
	{
	}

	static
	{
		endermanStrength = new PotionEffect(Potion.damageBoost.id, 600, 2, true);
	}
}
