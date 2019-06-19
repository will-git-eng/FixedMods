package chylex.hee.entity.boss;

import chylex.hee.entity.GlobalMobData;
import chylex.hee.entity.RandomNameGenerator;
import chylex.hee.entity.mob.util.DamageSourceMobUnscaled;
import chylex.hee.init.BlockList;
import chylex.hee.init.ItemList;
import chylex.hee.mechanics.causatum.CausatumMeters;
import chylex.hee.mechanics.causatum.CausatumUtils;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C07AddPlayerVelocity;
import chylex.hee.packets.client.C08PlaySound;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.achievements.AchievementManager;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.DragonUtil;
import chylex.hee.system.util.MathUtil;
import chylex.hee.tileentity.TileEntityLaserBeam;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class EntityMiniBossEnderEye extends EntityFlying implements IBossDisplayData, GlobalMobData.IIgnoreEnderGoo
{
	private byte sleepTimer;
	private byte healTimer;
	private byte attackTimer;
	private short laserTopY;
	private AttackType attackType;
	private AttackType lastAttackType;
	public EntityLivingBase target;

	public EntityMiniBossEnderEye(World world)
	{
		super(world);
		this.setSize(1.25F, 1.25F);
		this.experienceValue = 35;
		this.scoreValue = 25;
		this.isImmuneToFire = true;
		this.ignoreFrustumCheck = true;
		RandomNameGenerator.generateEntityName(this, this.rand.nextInt(3) + 4);
	}

	public EntityMiniBossEnderEye(World world, double x, double y, double z)
	{
		this(world);
		this.setPosition(x, y, z);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(16, (byte) 1);
		this.dataWatcher.addObject(17, (byte) 0);
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(ModCommonProxy.opMobs ? 350.0D : 250.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(1.8D);
	}

	@Override
	protected void updateEntityActionState()
	{
		if (this.isAsleep())
		{
			if (++this.healTimer >= 7 - this.worldObj.difficultySetting.getDifficultyId() && this.getHealth() < this.getMaxHealth())
			{
				this.healTimer = 0;
				this.heal(1.0F);
			}

			this.sleepTimer = 0;
			this.motionX = this.motionY = this.motionZ = 0.0D;
		}
		else if (this.target == null)
		{
			if (Math.abs(this.motionX) > 0.0D)
				this.motionX *= 0.25D;

			if (Math.abs(this.motionY) > 0.0D)
				this.motionY *= 0.25D;

			if (Math.abs(this.motionZ) > 0.0D)
				this.motionZ *= 0.25D;

			if (++this.sleepTimer == 60)
				this.motionX = this.motionY = this.motionZ = 0.0D;

			if (this.sleepTimer > 80)
			{
				this.setIsAsleep(true);
				this.setAttackAnimationTime((byte) 0);
				this.attackType = null;
			}
			else
			{
				List<EntityPlayer> nearPlayers = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(8.0D, 4.0D, 8.0D));
				if (!nearPlayers.isEmpty())
				{
					this.target = nearPlayers.get(0);
					this.sleepTimer = 0;
				}

				this.attackTimer = (byte) (-65 + this.rand.nextInt(15));
			}
		}
		else if (this.getDistanceSqToEntity(this.target) < 60.0D)
		{
			double diffX = this.posX - this.target.posX;
			double diffY = this.posY - (this.target.posY + (double) this.target.height * 0.5D);
			double diffZ = this.posZ - this.target.posZ;
			double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
			this.rotationYaw = DragonUtil.rotateSmoothly(this.rotationYaw, (float) MathUtil.toDeg(Math.atan2(diffZ, diffX)) - 270.0F, 6.0F);
			this.rotationPitch = DragonUtil.rotateSmoothly(this.rotationPitch, (float) -MathUtil.toDeg(Math.atan2(diffY, distance)), 8.0F);
			byte attackAnim = this.getAttackAnimationTime();
			if (attackAnim == 0 && --this.attackTimer < -100)
			{
				this.attackTimer = (byte) (this.attackTimer + 38 + this.rand.nextInt(20));

				for (int attempt = 0; attempt < 3; ++attempt)
				{
					this.attackType = AttackType.getById(this.rand.nextInt(AttackType.values().length));
					if (this.attackType != this.lastAttackType)
						break;
				}

				this.lastAttackType = this.attackType;
				this.setAttackAnimationTime((byte) 1);
			}
			else if (attackAnim >= 1)
			{
				this.setMoveForward(0.0F);
				if (attackAnim < this.attackType.getLength())
					this.setAttackAnimationTime((byte) (attackAnim + 1));
				else
					this.setAttackAnimationTime((byte) 0);

				if (this.attackType == AttackType.Poof)
				{
					if (attackAnim == 34)
					{
						if (this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))
						{
							BlockPosM tmpPos = BlockPosM.tmp();
							int a = 0;

							for (int hits = 0; a < 200 && hits < 16 + this.worldObj.difficultySetting.getDifficultyId(); ++a)
							{
								Block block = tmpPos.set(this).move(this.rand.nextInt(15) - 7, this.rand.nextInt(8) - 4, this.rand.nextInt(15) - 7).getBlock(this.worldObj);
								if (block.getMaterial() != Material.air && block.getBlockHardness(this.worldObj, tmpPos.x, tmpPos.y, tmpPos.z) != -1.0F)
								{
									    
									if (!this.worldObj.isRemote)
										    
										tmpPos.setAir(this.worldObj);
									this.worldObj.playAuxSFX(2001, tmpPos.x, tmpPos.y, tmpPos.z, Block.getIdFromBlock(Blocks.obsidian));
									++hits;
								}
							}
						}

						for (EntityPlayer player : (Iterable<? extends EntityPlayer>) this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(6.0D, 6.0D, 6.0D)))
						{
							double[] vec = DragonUtil.getNormalizedVector(player.posX - this.posX, player.posZ - this.posZ);
							boolean blocking = player.isBlocking();
							vec[0] *= blocking ? 1.4D : 2.4D;
							vec[1] *= blocking ? 1.4D : 2.4D;
							PacketPipeline.sendToPlayer(player, new C07AddPlayerVelocity(vec[0], 0.34D, vec[1]));
							player.motionX += vec[0];
							player.motionY += 0.34D;
							player.motionZ += vec[1];
							player.attackEntityFrom(new DamageSourceMobUnscaled(this), DamageSourceMobUnscaled.getDamage(ModCommonProxy.opMobs ? 7.0F : 4.0F, this.worldObj.difficultySetting));
						}

						PacketPipeline.sendToAllAround(this, 64.0D, new C08PlaySound((byte) 0, this.posX, this.posY, this.posZ, 1.0F, this.rand.nextFloat() * 0.2F + 0.9F));
					}
				}
				else if (this.attackType == AttackType.Nausea)
				{
					if (attackAnim == 17)
					{
						PotionEffect effNausea = new PotionEffect(Potion.confusion.id, 220, 0, true);

						for (EntityPlayer player : (Iterable<? extends EntityPlayer>) this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(6.0D, 6.0D, 6.0D)))
						{
							player.addPotionEffect(effNausea);
						}
					}
					else if (attackAnim == 19)
						PacketPipeline.sendToAllAround(this, 64.0D, new C08PlaySound((byte) 1, this.posX, this.posY, this.posZ, 1.0F, this.rand.nextFloat() * 0.2F + 0.9F));
					else if (attackAnim == 26)
					{
						PotionEffect effBlind = new PotionEffect(Potion.blindness.id, 160, 0, true);
						PotionEffect effSlow = new PotionEffect(Potion.moveSlowdown.id, 120, 0, true);

						for (EntityPlayer player : (Iterable<? extends EntityPlayer>) this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(6.0D, 6.0D, 6.0D)))
						{
							player.addPotionEffect(effBlind);
							player.addPotionEffect(effSlow);
						}
					}
				}
				else if (this.attackType == AttackType.LaserBeams)
					if (attackAnim > 35 && attackAnim < 99 && attackAnim % 7 == 0)
					{
						BlockPosM tmpPos = BlockPosM.tmp(this);
						int myY = MathUtil.floor(this.posY);

						for (int attempt = 0; attempt < 12; ++attempt)
						{
							tmpPos.set(MathUtil.floor(this.posX) + this.rand.nextInt(17) - 8, -1, MathUtil.floor(this.posZ) + this.rand.nextInt(17) - 8);
							if (tmpPos.setY(myY).isAir(this.worldObj))
							{
								for (tmpPos.y = myY; tmpPos.y > myY - 6 && tmpPos.isAir(this.worldObj); --tmpPos.y)
								{
									if (tmpPos.y == myY - 4)
									{
										tmpPos.y = -1;
										break;
									}
								}

								if (tmpPos.y != -1)
								{
									int minY = tmpPos.y;
									if (this.laserTopY == 0)
										this.laserTopY = (short) (myY + 8);

									for (tmpPos.y = minY + 1; tmpPos.y < this.laserTopY && tmpPos.isAir(this.worldObj); ++tmpPos.y)
									{
										tmpPos.setBlock(this.worldObj, BlockList.laser_beam);
										TileEntityLaserBeam beam = (TileEntityLaserBeam) tmpPos.getTileEntity(this.worldObj);
										if (beam != null)
											beam.setTicksLeft(102 - attackAnim);
									}

									PacketPipeline.sendToAllAround(this, 64.0D, new C08PlaySound((byte) 2, this.posX, this.posY, this.posZ, 0.85F, this.rand.nextFloat() * 0.1F + 0.95F));
									break;
								}
							}
						}
					}
					else if (attackAnim == 102)
					{
						this.laserTopY = 0;
						PacketPipeline.sendToAllAround(this, 64.0D, new C08PlaySound((byte) 3, this.posX, this.posY, this.posZ, 1.0F, this.rand.nextFloat() * 0.2F + 0.9F));
					}
			}

			if (attackAnim == 0)
			{
				double yD = this.posY + (double) (this.height * 0.5F) - (this.target.boundingBox.minY + (double) this.target.height + 0.4D);
				if (Math.abs(yD) >= 0.8D)
					this.motionY -= Math.abs(yD) * 0.005D * Math.signum(yD);

				if (distance >= 3.0D)
					this.setMoveForward((float) this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
				else if (Math.abs(yD) < 1.0D)
					this.target.attackEntityFrom(new DamageSourceMobUnscaled(this), DamageSourceMobUnscaled.getDamage(ModCommonProxy.opMobs ? 6.0F : 3.0F, this.worldObj.difficultySetting));

				if (this.target.isDead)
					this.target = null;
			}
		}
		else
			this.target = null;

	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if (!this.isEntityInvulnerable() && source != DamageSource.inWall && source != DamageSource.drown && source != DamageSource.cactus && !source.isFireDamage() && !source.isMagicDamage() && !source.isProjectile())
		{
			if (this.isAsleep())
			{
				this.setIsAsleep(false);

				if (this.worldObj.difficultySetting.getDifficultyId() > 1 || ModCommonProxy.opMobs)
				{
					BlockPosM tmpPos = BlockPosM.tmp();
					int a = 0;

					for (int hits = 0; a < 400 && hits < 5 + this.worldObj.difficultySetting.getDifficultyId() * 10 + (ModCommonProxy.opMobs ? 30 : 0); ++a)
					{
						tmpPos.set(this.posX + (double) this.rand.nextInt(15) - 7.0D, this.posY + (double) this.rand.nextInt(8) - 4.0D, this.posZ + (double) this.rand.nextInt(15) - 7.0D);
						Block block = tmpPos.getBlock(this.worldObj);
						if (block != Blocks.air)
						{
							float hardness = block.getBlockHardness(this.worldObj, tmpPos.x, tmpPos.y, tmpPos.z);
							if (hardness != -1.0F && hardness <= 5.0F)
							{
								    
								if (!this.worldObj.isRemote)
									    
									tmpPos.setAir(this.worldObj);
								this.worldObj.playAuxSFX(2001, tmpPos.x, tmpPos.y, tmpPos.z, Block.getIdFromBlock(Blocks.obsidian));
								++hits;
							}
						}
					}
				}
			}

			if (amount < 7.0F)
				return true;
			amount = 7.0F + Math.min((amount - 7.0F) * 0.5F, 5.0F);
			if (this.getAttackAnimationTime() > 0)
				amount *= 0.275F;

			if (super.attackEntityFrom(source, amount))
			{
				CausatumUtils.increase(source, CausatumMeters.ENDER_EYE_DAMAGE, amount * 2.0F);
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public void onDeath(DamageSource source)
	{
		if (source.getEntity() instanceof EntityPlayer)
			((EntityPlayer) source.getEntity()).addStat(AchievementManager.ENDER_EYE_KILL, 1);

		super.onDeath(source);
	}

	@Override
	public void knockBack(Entity entity, float damage, double xPower, double zPower)
	{
		this.isAirBorne = true;
		double dist = Math.sqrt(xPower * xPower + zPower * zPower);
		this.motionX -= xPower / dist * 0.04D;
		this.motionY += 0.005D;
		this.motionZ -= zPower / dist * 0.04D;
		if (this.motionY > 0.05D)
			this.motionY = 0.05D;

	}

	@Override
	public void addVelocity(double xVelocity, double yVelocity, double zVelocity)
	{
		super.addVelocity(xVelocity / 10.0D, yVelocity / 10.0D, zVelocity / 10.0D);
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int looting)
	{
		this.dropItem(Items.ender_eye, 1);
		this.dropItem(Item.getItemFromBlock(Blocks.obsidian), this.rand.nextInt(4 + looting) + 3);
		this.dropItem(ItemList.spatial_dash_gem, 1);
	}

	@Override
	public boolean canBePushed()
	{
		return !this.isAsleep() || this.rotationPitch != 0.0F;
	}

	@Override
	protected void collideWithEntity(Entity entity)
	{
		if (this.canBePushed())
			entity.applyEntityCollision(this);
	}

	public void setIsAsleep(boolean isAsleep)
	{
		this.dataWatcher.updateObject(16, (byte) (isAsleep ? 1 : 0));
	}

	public boolean isAsleep()
	{
		return this.dataWatcher.getWatchableObjectByte(16) != 0;
	}

	public void setAttackAnimationTime(byte time)
	{
		this.dataWatcher.updateObject(17, time);
	}

	public byte getAttackAnimationTime()
	{
		return this.dataWatcher.getWatchableObjectByte(17);
	}

	@Override
	public boolean getCanSpawnHere()
	{
		return true;
	}

	@Override
	protected String getLivingSound()
	{
		return this.isAsleep() ? null : "hardcoreenderexpansion:mob.endereye.living";
	}

	@Override
	protected String getHurtSound()
	{
		return "hardcoreenderexpansion:mob.endereye.hurt";
	}

	@Override
	protected String getDeathSound()
	{
		return "hardcoreenderexpansion:mob.endereye.death";
	}

	@Override
	public float getSoundVolume()
	{
		return 0.75F;
	}

	@Override
	public float getSoundPitch()
	{
		return this.rand.nextFloat() * 0.25F + 0.875F;
	}

	@Override
	public String getCommandSenderName()
	{
		return this.hasCustomNameTag() ? this.getCustomNameTag() : StatCollector.translateToLocal("entity.enderEye.name");
	}

	@Override
	protected void despawnEntity()
	{
	}
}
