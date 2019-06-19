package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.compat.EtFuturumHelper;
import blusunrize.immersiveengineering.common.util.compat.IC2Helper;
import cofh.api.energy.IEnergyContainerItem;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.immersiveengineering.ExplosionByPlayer;
import ru.will.git.immersiveengineering.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class EntityRevolvershot extends Entity
{
	private int field_145795_e = -1;
	private int field_145793_f = -1;
	private int field_145794_g = -1;
	private Block field_145796_h;
	private boolean inGround;
	public EntityLivingBase shootingEntity;
	private int ticksAlive;
	private int ticksInAir;

	private int tickLimit = 40;
	int bulletType = 0;
	public boolean bulletElectro = false;
    
    

	public EntityRevolvershot(World world)
	{
		super(world);
		this.renderDistanceWeight = 10;
		this.setSize(.125f, .125f);
	}

	public EntityRevolvershot(World world, double x, double y, double z, double ax, double ay, double az, int type)
	{
		super(world);
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		this.bulletType = type;
	}

	public EntityRevolvershot(World world, EntityLivingBase living, double ax, double ay, double az, int type, ItemStack stack)
	{
		super(world);
		this.shootingEntity = living;
		this.setSize(0.125F, 0.125F);
		this.setLocationAndAngles(living.posX + ax, living.posY + living.getEyeHeight() + ay, living.posZ + az, living.rotationYaw, living.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = this.motionY = this.motionZ = 0.0D;
    
		if (living instanceof EntityPlayer)
    
	}

	@Override
	protected void entityInit()
	{
	}

	public void setTickLimit(int limit)
	{
		this.tickLimit = limit;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.boundingBox.getAverageEdgeLength() * 4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1 * d1;
	}

	@Override
	public void onUpdate()
	{
		if (!this.worldObj.isRemote && (this.shootingEntity != null && this.shootingEntity.isDead || !this.worldObj.blockExists((int) this.posX, (int) this.posY, (int) this.posZ)))
			this.setDead();
		else
		{
			super.onUpdate();

			if (this.inGround)
			{
				if (this.worldObj.getBlock(this.field_145795_e, this.field_145793_f, this.field_145794_g) == this.field_145796_h)
				{
					++this.ticksAlive;
					if (this.ticksAlive == 600)
						this.setDead();

					return;
				}

				this.inGround = false;
				this.motionX *= this.rand.nextFloat() * 0.2F;
				this.motionY *= this.rand.nextFloat() * 0.2F;
				this.motionZ *= this.rand.nextFloat() * 0.2F;
				this.ticksAlive = 0;
				this.ticksInAir = 0;
			}
			else
				++this.ticksInAir;

			if (this.ticksInAir >= this.tickLimit)
			{
				this.onExpire();
				this.setDead();
				return;
			}

			Vec3 vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			Vec3 vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec3, vec31);
			vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if (movingobjectposition != null)
				vec31 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);

			Entity entity = null;
			List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = (Entity) list.get(i);
				if (entity1.canBeCollidedWith() && !entity1.isEntityEqual(this.shootingEntity))
				{
					float f = 0.3F;
					AxisAlignedBB axisalignedbb = entity1.boundingBox.expand(f, f, f);
					MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

					if (movingobjectposition1 != null)
					{
						double d1 = vec3.distanceTo(movingobjectposition1.hitVec);
						if (d1 < d0 || d0 == 0.0D)
						{
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null)
				movingobjectposition = new MovingObjectPosition(entity);

			if (movingobjectposition != null)
				this.onImpact(movingobjectposition);

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;

			for (this.rotationPitch = (float) (Math.atan2(f1, this.motionY) * 180.0D / Math.PI) - 90.0F; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
			{
			}

			while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
			{
				this.prevRotationPitch += 360.0F;
			}
			while (this.rotationYaw - this.prevRotationYaw < -180.0F)
			{
				this.prevRotationYaw -= 360.0F;
			}
			while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
			{
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

			if (this.isInWater())
				for (int j = 0; j < 4; ++j)
				{
					float f3 = 0.25F;
					this.worldObj.spawnParticle("bubble", this.posX - this.motionX * f3, this.posY - this.motionY * f3, this.posZ - this.motionZ * f3, this.motionX, this.motionY, this.motionZ);
				}

			if (this.ticksExisted % 4 == 0)
				this.worldObj.spawnParticle("smoke", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	protected void onImpact(MovingObjectPosition mop)
	{
		boolean headshot = false;

		if (mop.entityHit != null)
    
			if (this.fake.cantDamage(mop.entityHit))
			{
				this.setDead();
				return;
    

			if (mop.entityHit instanceof EntityLivingBase)
				headshot = Utils.isVecInEntityHead((EntityLivingBase) mop.entityHit, Vec3.createVectorHelper(this.posX, this.posY, this.posZ));

			String dmgKey = this.bulletType == 0 ? "Casull" : this.bulletType == 1 ? "AP" : this.bulletType == 2 ? "Buck" : this.bulletType == 4 ? "Dragon" : this.bulletType == 5 ? "Homing" : this.bulletType == 6 ? "Wolfpack" : this.bulletType == 7 ? "Silver" : this.bulletType == 8 ? "Potion" : "";
			double damage = Config.getDouble("BulletDamage-" + dmgKey);
			if (headshot)
			{
				damage *= 1.5;
				EntityLivingBase living = (EntityLivingBase) mop.entityHit;
				if (living.isChild() && !living.isEntityInvulnerable() && (living.hurtResistantTime > 0 ? living.getHealth() <= 0 : living.getHealth() <= damage))
					if (this.worldObj.isRemote)
					{
						this.worldObj.makeFireworks(this.posX, this.posY, this.posZ, 0, 0, 0, Utils.getRandomFireworkExplosion(this.worldObj.rand, 4));
						this.worldObj.playSound(this.posX, this.posY, this.posZ, "immersiveengineering:birthdayParty", 1.5f, 1, false);
						mop.entityHit.getEntityData().setBoolean("headshot", true);
					}
					else if (this.shootingEntity instanceof EntityPlayer)
						((EntityPlayer) this.shootingEntity).triggerAchievement(IEAchievements.secret_birthdayParty);
			}

			if (!this.worldObj.isRemote)
				switch (this.bulletType)
				{
					case 0:
						mop.entityHit.attackEntityFrom(IEDamageSources.causeCasullDamage(this, this.shootingEntity), (float) damage);
						break;
					case 1:
						mop.entityHit.attackEntityFrom(IEDamageSources.causePiercingDamage(this, this.shootingEntity), (float) damage);
						break;
					case 2:
						mop.entityHit.attackEntityFrom(IEDamageSources.causeBuckshotDamage(this, this.shootingEntity), (float) damage);
						mop.entityHit.hurtResistantTime = 0;
						break;
					case 4:
						if (mop.entityHit.attackEntityFrom(IEDamageSources.causeDragonsbreathDamage(this, this.shootingEntity), (float) damage))
							mop.entityHit.setFire(3);
						break;
					case 5:
						mop.entityHit.attackEntityFrom(IEDamageSources.causeHomingDamage(this, this.shootingEntity), (float) damage);
						break;
					case 6:
						mop.entityHit.attackEntityFrom(IEDamageSources.causeWolfpackDamage(this, this.shootingEntity), (float) damage);
						break;
					case 7:
						mop.entityHit.attackEntityFrom(IEDamageSources.causeSilverDamage(this, this.shootingEntity), (float) damage);
						break;
					case 8:
						mop.entityHit.attackEntityFrom(IEDamageSources.causePotionDamage(this, this.shootingEntity), (float) damage);
						break;
				}
		}
		if (!this.worldObj.isRemote)
		{
    
				ExplosionByPlayer.createExplosion(this.fake.get(), this.worldObj, this.shootingEntity, this.posX, this.posY, this.posZ, 2, false);
			this.secondaryImpact(mop);
		}
		this.setDead();
	}

	public void secondaryImpact(MovingObjectPosition mop)
	{
		if (this.bulletElectro && mop.entityHit instanceof EntityLivingBase)
		{
			((EntityLivingBase) mop.entityHit).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 15, 4));
			for (int i = 0; i <= 4; i++)
			{
				ItemStack stack = ((EntityLivingBase) mop.entityHit).getEquipmentInSlot(i);
				if (stack != null && stack.getItem() instanceof IEnergyContainerItem)
				{
					int maxStore = ((IEnergyContainerItem) stack.getItem()).getMaxEnergyStored(stack);
					int drain = Math.min((int) (maxStore * .15f), ((IEnergyContainerItem) stack.getItem()).getEnergyStored(stack));
					int hasDrained = 0;
					while (hasDrained < drain)
					{
						int actualDrain = ((IEnergyContainerItem) stack.getItem()).extractEnergy(stack, drain, false);
						if (actualDrain <= 0)
							break;
						hasDrained += actualDrain;
					}
				}
				if (stack != null && Lib.IC2)
				{
					double charge = IC2Helper.getMaxItemCharge(stack);
					IC2Helper.dischargeItem(stack, charge * .15f);
				}
			}
		}

		if (this.bulletType == 6)
		{
			Vec3 v = Vec3.createVectorHelper(-this.motionX, -this.motionY, -this.motionZ);
			int split = 6;
			for (int i = 0; i < split; i++)
			{
				float angle = i * (360f / split);
				Matrix4 matrix = new Matrix4();
				matrix.rotate(angle, v.xCoord, v.yCoord, v.zCoord);
				Vec3 vecDir = Vec3.createVectorHelper(0, 1, 0);
				matrix.apply(vecDir);

				EntityWolfpackShot bullet = new EntityWolfpackShot(this.worldObj, this.shootingEntity, vecDir.xCoord * 1.5, vecDir.yCoord * 1.5, vecDir.zCoord * 1.5, this.bulletType, null);
				if (mop.entityHit instanceof EntityLivingBase)
					bullet.targetOverride = (EntityLivingBase) mop.entityHit;
				bullet.setPosition(this.posX + vecDir.xCoord, this.posY + vecDir.yCoord, this.posZ + vecDir.zCoord);
				bullet.motionX = vecDir.xCoord * .375;
				bullet.motionY = vecDir.yCoord * .375;
				bullet.motionZ = vecDir.zCoord * .375;
				this.worldObj.spawnEntityInWorld(bullet);
			}
		}
		if (this.bulletType == 8 && this.bulletPotion != null && this.bulletPotion.getItem() instanceof ItemPotion)
		{
			List<PotionEffect> effects = ((ItemPotion) this.bulletPotion.getItem()).getEffects(this.bulletPotion);
			if (effects != null)
				if (this.bulletPotion.getItem().getClass().getName().equalsIgnoreCase("ganymedes01.etfuturum.items.LingeringPotion"))
					EtFuturumHelper.createLingeringPotionEffect(this.worldObj, this.posX, this.posY, this.posZ, this.bulletPotion, this.shootingEntity);
				else if (ItemPotion.isSplash(this.bulletPotion.getItemDamage()))
				{
					List<EntityLivingBase> livingEntities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.boundingBox.expand(4.0D, 2.0D, 4.0D));
					if (livingEntities != null && !livingEntities.isEmpty())
						for (EntityLivingBase living : livingEntities)
						{
							double dist = this.getDistanceSqToEntity(living);
							if (dist < 16D)
    
								if (this.fake.cantDamage(living))
    

								double dist2 = 1 - Math.sqrt(dist) / 4D;
								if (living == mop.entityHit)
									dist2 = 1D;
								for (PotionEffect p : effects)
								{
									int id = p.getPotionID();
									if (Potion.potionTypes[id].isInstant())
										Potion.potionTypes[id].affectEntity(this.shootingEntity, living, p.getAmplifier(), dist2);
									else
									{
										int j = (int) (dist2 * p.getDuration() + .5D);
										if (j > 20)
											living.addPotionEffect(new PotionEffect(id, j, p.getAmplifier()));
									}
								}
							}
						}
				}
				else if (mop.entityHit instanceof EntityLivingBase)
					for (PotionEffect p : effects)
					{
						if (p.getDuration() < 1)
							p = new PotionEffect(p.getPotionID(), 1);
						((EntityLivingBase) mop.entityHit).addPotionEffect(p);
					}
			this.worldObj.playAuxSFX(2002, (int) Math.round(this.posX), (int) Math.round(this.posY), (int) Math.round(this.posZ), this.bulletPotion.getItemDamage());
		}
	}

	public void onExpire()
	{

	}

	protected float getMotionFactor()
	{
		return 0.95F;
	}

    
	protected void writeEntityToNBT(NBTTagCompound nbt)
    
		nbt.setShort("xTile", (short) this.field_145795_e);
		nbt.setShort("yTile", (short) this.field_145793_f);
		nbt.setShort("zTile", (short) this.field_145794_g);
		nbt.setByte("inTile", (byte) Block.getIdFromBlock(this.field_145796_h));
		nbt.setByte("inGround", (byte) (this.inGround ? 1 : 0));
		nbt.setTag("direction", this.newDoubleNBTList(this.motionX, this.motionY, this.motionZ));
		nbt.setShort("bulletType", (short) this.bulletType);
		if (this.bulletPotion != null)
    
    
	}

    
	protected void readEntityFromNBT(NBTTagCompound nbt)
    
		this.field_145795_e = nbt.getShort("xTile");
		this.field_145793_f = nbt.getShort("yTile");
		this.field_145794_g = nbt.getShort("zTile");
		this.field_145796_h = Block.getBlockById(nbt.getByte("inTile") & 255);
		this.inGround = nbt.getByte("inGround") == 1;
		this.bulletType = nbt.getShort("bulletType");
		if (nbt.hasKey("bulletPotion"))
			this.bulletPotion = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("bulletPotion"));

		if (nbt.hasKey("direction", 9))
		{
			NBTTagList nbttaglist = nbt.getTagList("direction", 6);
			this.motionX = nbttaglist.func_150309_d(0);
			this.motionY = nbttaglist.func_150309_d(1);
			this.motionZ = nbttaglist.func_150309_d(2);
		}
		else
    
    
	}

	@Override
	public float getCollisionBorderSize()
	{
		return 1.0F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getShadowSize()
	{
		return 0.0F;
	}

	@Override
	public float getBrightness(float p_70013_1_)
	{
		return 1.0F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getBrightnessForRender(float p_70070_1_)
	{
		return 15728880;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		return false;
	}
}