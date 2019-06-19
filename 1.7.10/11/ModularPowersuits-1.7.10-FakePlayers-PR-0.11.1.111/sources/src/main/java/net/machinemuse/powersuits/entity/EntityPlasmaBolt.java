package net.machinemuse.powersuits.entity;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.machinemuse.ExplosionByPlayer;
import ru.will.git.machinemuse.ModUtils;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityPlasmaBolt extends EntityThrowable
{
	public double size;
	public static final int SIZE = 24;
	public double damagingness;
	public double explosiveness;
    
    

	public EntityPlasmaBolt(World world)
	{
		super(world);
	}

	public EntityPlasmaBolt(World world, EntityLivingBase shootingEntity, double explosiveness, double damagingness, int chargeTicks)
	{
		super(world);
		this.shootingEntity = shootingEntity;
		this.size = chargeTicks > 50 ? 50 : chargeTicks;
		this.explosiveness = explosiveness;
		this.damagingness = damagingness;
		Vec3 direction = shootingEntity.getLookVec().normalize();
		double scale = 1.0;
		this.motionX = direction.xCoord * scale;
		this.motionY = direction.yCoord * scale;
		this.motionZ = direction.zCoord * scale;
		double r = this.size / 50.0;
		double xoffset = 1.3f + r - direction.yCoord * shootingEntity.getEyeHeight();
		double yoffset = -.2;
		double zoffset = 0.3f;
		double horzScale = Math.sqrt(direction.xCoord * direction.xCoord + direction.zCoord * direction.zCoord);
		double horzx = direction.xCoord / horzScale;
		double horzz = direction.zCoord / horzScale;
		this.posX = shootingEntity.posX + direction.xCoord * xoffset - direction.yCoord * horzx * yoffset - horzz * zoffset;
		this.posY = shootingEntity.posY + shootingEntity.getEyeHeight() + direction.yCoord * xoffset + (1 - Math.abs(direction.yCoord)) * yoffset;
		this.posZ = shootingEntity.posZ + direction.zCoord * xoffset - direction.yCoord * horzz * yoffset + horzx * zoffset;
    
		if (shootingEntity instanceof EntityPlayer)
    
	}

	@Override
	public void onEntityUpdate()
	{
		super.onEntityUpdate();
		if (this.ticksExisted > this.getMaxLifetime())
			this.setDead();
		if (this.isInWater())
		{
			this.setDead();
			for (int var3 = 0; var3 < this.size; ++var3)
				this.worldObj.spawnParticle("flame", this.posX + Math.random() * 1, this.posY + Math.random() * 1, this.posZ + Math.random() * 0.1, 0.0D, 0.0D, 0.0D);

		}
	}

	public int getMaxLifetime()
	{
		return 200;
	}

    
	@Override
	protected boolean canTriggerWalking()
	{
		return false;
	}

    
	@Override
	public boolean canAttackWithItem()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getShadowSize()
	{
		return 0.0F;
	}

	@Override
	protected void entityInit()
	{
		this.renderDistanceWeight = 10.0D;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound var1)
	{
		this.setDead();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound var1)
	{
	}

    
	@Override
	protected float getGravityVelocity()
	{
		return 0;
	}

	@Override
	protected void onImpact(MovingObjectPosition event)
	{

		double damage = this.size / 50.0 * this.damagingness;
		switch (event.typeOfHit)
		{
    
				if (event.entityHit != null && event.entityHit != this.shootingEntity && !this.fake.cantDamage(event.entityHit))
					event.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.shootingEntity), (int) damage);
				break;
			case BLOCK:
				break;
			default:
				break;
		}
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
    
			ExplosionByPlayer.createExplosion(this.fake.get(), this.worldObj, this, this.posX, this.posY, this.posZ, (float) (this.size / 50.0f * 3 * this.explosiveness), flag);
		}
		for (int var3 = 0; var3 < 8; ++var3)
			this.worldObj.spawnParticle("flame", this.posX + Math.random() * 0.1, this.posY + Math.random() * 0.1, this.posZ + Math.random() * 0.1, 0.0D, 0.0D, 0.0D);

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			this.setDead();
	}
}
