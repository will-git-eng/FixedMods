package am2.entities;

import java.util.List;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;

import am2.AMCore;
import am2.PlayerTracker;
import am2.bosses.EntityWinterGuardian;
import am2.buffs.BuffEffectFrostSlowed;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleMoveOnHeading;
import am2.playerextensions.ExtendedProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityWinterGuardianArm extends EntityLiving
{
	private final int maxTicksToExist;
	private EntityLivingBase throwingEntity;
	private Integer entityHit;
	private double projectileSpeed;
	private boolean takenArm;
	private static final int DW_THROWING_ENTITY = 20;
	private static final int DW_PROJECTILE_SPEED = 21;

	    
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
	    

	public EntityWinterGuardianArm(World par1World)
	{
		super(par1World);
		this.takenArm = false;
		super.ticksExisted = 0;
		this.maxTicksToExist = 120;
		super.noClip = true;
		this.entityHit = null;
	}

	public EntityWinterGuardianArm(World world, EntityLivingBase entityLiving, double projectileSpeed)
	{
		this(world);
		this.throwingEntity = entityLiving;
		this.setSize(0.25F, 0.25F);
		this.setLocationAndAngles(entityLiving.posX, entityLiving.posY + entityLiving.getEyeHeight(), entityLiving.posZ, entityLiving.rotationYaw, entityLiving.rotationPitch);
		super.posX -= MathHelper.cos(super.rotationYaw / 180.0F * 3.141593F) * 0.16F;
		super.posY -= 0.10000000149011612D;
		super.posZ -= MathHelper.sin(super.rotationYaw / 180.0F * 3.141593F) * 0.16F;
		this.setPosition(super.posX, super.posY, super.posZ);
		super.yOffset = 0.0F;
		float f = 0.05F;
		super.motionX = -MathHelper.sin(super.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(super.rotationPitch / 180.0F * 3.141593F) * f;
		super.motionZ = MathHelper.cos(super.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(super.rotationPitch / 180.0F * 3.141593F) * f;
		super.motionY = -MathHelper.sin(super.rotationPitch / 180.0F * 3.141593F) * f;
		this.setHeading(super.motionX, super.motionY, super.motionZ, projectileSpeed, projectileSpeed);
		this.projectileSpeed = projectileSpeed;

		    
		if (entityLiving instanceof EntityPlayer)
			this.fake.setRealPlayer((EntityPlayer) entityLiving);
		    
	}

	public void setHeading(double movementX, double movementY, double movementZ, double projectileSpeed, double projectileSpeed2)
	{
		float f = MathHelper.sqrt_double(movementX * movementX + movementY * movementY + movementZ * movementZ);
		movementX = movementX / f;
		movementY = movementY / f;
		movementZ = movementZ / f;
		movementX = movementX + super.rand.nextGaussian() * 0.007499999832361937D * projectileSpeed2;
		movementY = movementY + super.rand.nextGaussian() * 0.007499999832361937D * projectileSpeed2;
		movementZ = movementZ + super.rand.nextGaussian() * 0.007499999832361937D * projectileSpeed2;
		movementX = movementX * projectileSpeed;
		movementY = movementY * projectileSpeed;
		movementZ = movementZ * projectileSpeed;
		super.motionX = movementX;
		super.motionY = movementY;
		super.motionZ = movementZ;
		float f1 = MathHelper.sqrt_double(movementX * movementX + movementZ * movementZ);
		super.prevRotationYaw = super.rotationYaw = (float) (Math.atan2(movementX, movementZ) * 180.0D / 3.141592653589793D);
		super.prevRotationPitch = super.rotationPitch = (float) (Math.atan2(movementY, f1) * 180.0D / 3.141592653589793D);
	}

	@Override
	public void setDead()
	{
		if (this.getThrowingEntity() != null)
			if (this.getThrowingEntity() instanceof EntityWinterGuardian)
				((EntityWinterGuardian) this.getThrowingEntity()).returnOneArm();
			else if (this.getThrowingEntity() instanceof EntityPlayer && !super.worldObj.isRemote)
				if (this.getThrowingEntity().getHealth() <= 0.0F)
					PlayerTracker.storeSoulboundItemForRespawn((EntityPlayer) this.getThrowingEntity(), ItemsCommonProxy.winterArmEnchanted.copy());
				else if (!((EntityPlayer) this.getThrowingEntity()).inventory.addItemStackToInventory(ItemsCommonProxy.winterArmEnchanted.copy()))
				{
					EntityItem item = new EntityItem(super.worldObj);
					item.setPosition(super.posX, super.posY, super.posZ);
					item.setEntityItemStack(ItemsCommonProxy.winterArmEnchanted.copy());
					super.worldObj.spawnEntityInWorld(item);
				}

		if (this.entityHit != null)
		{
			Entity entityhit = super.worldObj.getEntityByID(this.entityHit.intValue());
			if (entityhit != null)
			{
				entityhit.motionX = 0.0D;
				entityhit.motionY = 0.0D;
				entityhit.motionZ = 0.0D;
			}
		}

		super.setDead();
	}

	@Override
	public void onUpdate()
	{
		if (super.worldObj.isRemote || this.getThrowingEntity() != null && !this.getThrowingEntity().isDead)
		{
			++super.ticksExisted;
			if (super.ticksExisted >= this.maxTicksToExist && !super.worldObj.isRemote)
				this.setDead();
			else
			{
				if (!this.takenArm && this.getThrowingEntity() != null && this.getThrowingEntity() instanceof EntityWinterGuardian)
				{
					((EntityWinterGuardian) this.getThrowingEntity()).launchOneArm();
					this.takenArm = true;
				}

				Vec3 vec3d = Vec3.createVectorHelper(super.posX, super.posY, super.posZ);
				Vec3 vec3d1 = Vec3.createVectorHelper(super.posX + super.motionX, super.posY + super.motionY, super.posZ + super.motionZ);
				MovingObjectPosition movingobjectposition = super.worldObj.rayTraceBlocks(vec3d, vec3d1);
				vec3d = Vec3.createVectorHelper(super.posX, super.posY, super.posZ);
				vec3d1 = Vec3.createVectorHelper(super.posX + super.motionX, super.posY + super.motionY, super.posZ + super.motionZ);
				if (movingobjectposition != null)
					vec3d1 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);

				Entity entity = null;
				List list = super.worldObj.getEntitiesWithinAABBExcludingEntity(this, super.boundingBox.addCoord(super.motionX, super.motionY, super.motionZ).expand(1.0D, 1.0D, 1.0D));
				double d = 0.0D;

				for (int j = 0; j < list.size(); ++j)
				{
					Entity entity1 = (Entity) list.get(j);
					if (entity1.canBeCollidedWith() && (!entity1.isEntityEqual(this.getThrowingEntity()) || super.ticksExisted >= 25))
					{
						float f2 = 0.3F;
						AxisAlignedBB axisalignedbb = entity1.boundingBox.expand(f2, f2, f2);
						MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
						if (movingobjectposition1 != null)
						{
							double d1 = vec3d.distanceTo(movingobjectposition1.hitVec);
							if (d1 < d || d == 0.0D)
							{
								entity = entity1;
								d = d1;
							}
						}
					}
				}

				if (entity != null)
					movingobjectposition = new MovingObjectPosition(entity);

				if (movingobjectposition != null)
					this.HitObject(movingobjectposition);

				super.posX += super.motionX;
				super.posY += super.motionY;
				super.posZ += super.motionZ;
				float f = MathHelper.sqrt_double(super.motionX * super.motionX + super.motionZ * super.motionZ);
				super.rotationYaw = (float) (Math.atan2(super.motionX, super.motionZ) * 180.0D / 3.1415927410125732D);

				for (super.rotationPitch = (float) (Math.atan2(super.motionY, f) * 180.0D / 3.1415927410125732D); super.rotationPitch - super.prevRotationPitch < -180.0F; super.prevRotationPitch -= 360.0F)
					;

				while (super.rotationPitch - super.prevRotationPitch >= 180.0F)
					super.prevRotationPitch += 360.0F;

				while (super.rotationYaw - super.prevRotationYaw < -180.0F)
					super.prevRotationYaw -= 360.0F;

				while (super.rotationYaw - super.prevRotationYaw >= 180.0F)
					super.prevRotationYaw += 360.0F;

				super.rotationPitch = super.prevRotationPitch + (super.rotationPitch - super.prevRotationPitch) * 0.4F;
				super.rotationYaw = super.prevRotationYaw + (super.rotationYaw - super.prevRotationYaw) * 0.4F;
				float f1 = 0.95F;
				if (this.isInWater())
				{
					for (int k = 0; k < 4; ++k)
					{
						float f3 = 0.25F;
						super.worldObj.spawnParticle("bubble", super.posX - super.motionX * f3, super.posY - super.motionY * f3, super.posZ - super.motionZ * f3, super.motionX, super.motionY, super.motionZ);
					}

					f1 = 0.8F;
				}
				else
					for (int i = 0; i < 2; ++i)
					{
						AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(super.worldObj, "ember", super.posX + super.rand.nextFloat() * 0.2D - 0.1D, super.posY + 1.2D, super.posZ + super.rand.nextFloat() * 0.2D - 0.1D);
						if (particle != null)
						{
							particle.setIgnoreMaxAge(false);
							particle.setMaxAge(15);
							particle.setParticleScale(0.35F);
							particle.setRGBColorF(0.5098F, 0.7843F, 0.7843F);
							particle.AddParticleController(new ParticleMoveOnHeading(particle, Math.toDegrees(super.rotationPitch), Math.toDegrees(super.rotationYaw), 0.20000000298023224D, 1, false));
						}
					}

				this.setPosition(super.posX, super.posY, super.posZ);
				int halflife = 80;
				if (super.ticksExisted > 30 && super.ticksExisted < halflife)
				{
					super.motionX *= 0.800000011920929D;
					super.motionY *= 0.800000011920929D;
					super.motionZ *= 0.800000011920929D;
				}
				else if (super.ticksExisted > halflife && this.getThrowingEntity() != null)
				{
					double deltaX = super.posX - this.getThrowingEntity().posX;
					double deltaZ = super.posZ - this.getThrowingEntity().posZ;
					double deltaY = super.posY - (this.getThrowingEntity().posY + this.getThrowingEntity().getEyeHeight());
					double angle = Math.atan2(deltaZ, deltaX);
					double speed = Math.min((super.ticksExisted - halflife) / 10.0F, this.getProjectileSpeed());
					double horizontalDistance = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
					float pitchRotation = (float) -Math.atan2(deltaY, horizontalDistance);
					super.motionX = -Math.cos(angle) * speed;
					super.motionZ = -Math.sin(angle) * speed;
					super.motionY = Math.sin(pitchRotation) * speed;
					if (this.entityHit != null)
					{
						Entity entityhit = super.worldObj.getEntityByID(this.entityHit.intValue());
						if (entityhit != null)
						{
							entityhit.posX = super.posX;
							entityhit.posY = super.posY - entityhit.height / 2.0F + 1.2D;
							entityhit.posZ = super.posZ;
							entityhit.motionX = super.motionX;
							entityhit.motionY = super.motionY;
							entityhit.motionZ = super.motionZ;
							entityhit.lastTickPosX = super.lastTickPosX;
							entityhit.lastTickPosY = super.lastTickPosY - entityhit.height / 2.0F + 1.2D;
							entityhit.lastTickPosZ = super.lastTickPosZ;
							entityhit.fallDistance = 0.0F;
						}
					}

					if (this.getDistanceSqToEntity(this.getThrowingEntity()) < 9.0D && !super.worldObj.isRemote)
						this.setDead();
				}

			}
		}
		else
			this.setDead();
	}

	protected void HitObject(MovingObjectPosition mop)
	{
		if (mop.entityHit != null && mop.entityHit instanceof EntityLivingBase)
		{
			if (mop.entityHit == this.getThrowingEntity() || this.getThrowingEntity() == null)
				return;

			if (this.getThrowingEntity() != null && this.entityHit == null)
			{
				    
				if (this.fake.cantDamage(mop.entityHit))
				{
					this.setDead();
					return;
				}
				    

				mop.entityHit.attackEntityFrom(DamageSource.causeMobDamage(this.getThrowingEntity()), 3.0F);
				this.entityHit = Integer.valueOf(mop.entityHit.getEntityId());
				super.ticksExisted = 80;
				if (mop.entityHit instanceof EntityLivingBase)
				{
					ExtendedProperties.For((EntityLivingBase) mop.entityHit).deductMana(ExtendedProperties.For((EntityLivingBase) mop.entityHit).getMaxMana() * 0.1F);
					((EntityLivingBase) mop.entityHit).addPotionEffect(new PotionEffect(Potion.weakness.id, 60, 2));
					((EntityLivingBase) mop.entityHit).addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 60, 2));
					((EntityLivingBase) mop.entityHit).addPotionEffect(new BuffEffectFrostSlowed(60, 2));
				}
			}
		}

	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		super.dataWatcher.addObject(20, Integer.valueOf(0));
		super.dataWatcher.addObject(21, Integer.valueOf(20));
	}

	public void setThrowingEntity(EntityLivingBase entity)
	{
		this.throwingEntity = entity;
		super.dataWatcher.updateObject(20, Integer.valueOf(entity.getEntityId()));
	}

	public void setProjectileSpeed(double speed)
	{
		this.projectileSpeed = speed;
		super.dataWatcher.updateObject(21, Integer.valueOf((int) (speed * 10.0D)));
	}

	private double getProjectileSpeed()
	{
		return super.dataWatcher.getWatchableObjectInt(21) / 10;
	}

	private EntityLivingBase getThrowingEntity()
	{
		if (this.throwingEntity == null)
		{
			Entity entity = super.worldObj.getEntityByID(super.dataWatcher.getWatchableObjectInt(20));

			    
			if (!(entity instanceof EntityLivingBase))
				return null;
			    

			this.throwingEntity = (EntityLivingBase) entity;
		}

		return this.throwingEntity;
	}

	@Override
	public ItemStack getHeldItem()
	{
		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack)
	{
	}

	@Override
	public ItemStack[] getLastActiveItems()
	{
		return new ItemStack[0];
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
	{
		return false;
	}

	@Override
	protected boolean canDespawn()
	{
		return false;
	}
}
