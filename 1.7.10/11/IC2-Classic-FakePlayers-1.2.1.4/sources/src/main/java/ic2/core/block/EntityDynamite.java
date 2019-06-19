package ic2.core.block;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.ic2.ModUtils;

import ic2.core.IC2;
import ic2.core.PointExplosion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityDynamite extends Entity implements IProjectile
{
	public boolean sticky;
	public static int netId = 142;
	public int stickX;
	public int stickY;
	public int stickZ;
	public int fuse;
	private boolean inGround;
	public EntityLivingBase owner;
	private int ticksInGround;
    
    

	public EntityDynamite(World world, double x, double y, double z)
	{
		super(world);
		this.sticky = false;
		this.fuse = 100;
		this.inGround = false;
		this.ticksInAir = 0;
		this.setSize(0.5F, 0.5F);
		this.setPosition(x, y, z);
		this.yOffset = 0.0F;
	}

	public EntityDynamite(World world, double x, double y, double z, boolean sticky)
	{
		this(world, x, y, z);
		this.sticky = sticky;
	}

	public EntityDynamite(World world)
	{
		this(world, 0.0D, 0.0D, 0.0D);
	}

	public EntityDynamite(World world, EntityLivingBase entityliving)
	{
		super(world);
		this.sticky = false;
		this.fuse = 100;
		this.inGround = false;
		this.ticksInAir = 0;
		this.owner = entityliving;
		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(entityliving.posX, entityliving.posY + entityliving.getEyeHeight(), entityliving.posZ, entityliving.rotationYaw, entityliving.rotationPitch);
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * 3.141593F) * 0.16F;
		this.posY -= 0.1000000014901161D;
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * 3.141593F) * 0.16F;
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.141593F);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.141593F);
		this.motionY = -MathHelper.sin(this.rotationPitch / 180.0F * 3.141593F);
    
		if (entityliving instanceof EntityPlayer)
    
	}

	@Override
	protected void entityInit()
	{
	}

	@Override
	public void setThrowableHeading(double d, double d1, double d2, float f, float f1)
	{
		float f2 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
		d = d / f2;
		d1 = d1 / f2;
		d2 = d2 / f2;
		d = d + this.rand.nextGaussian() * 0.007499999832361937D * f1;
		d1 = d1 + this.rand.nextGaussian() * 0.007499999832361937D * f1;
		d2 = d2 + this.rand.nextGaussian() * 0.007499999832361937D * f1;
		d = d * f;
		d1 = d1 * f;
		d2 = d2 * f;
		this.motionX = d;
		this.motionY = d1;
		this.motionZ = d2;
		float f3 = MathHelper.sqrt_double(d * d + d2 * d2);
		float n = (float) (Math.atan2(d, d2) * 180.0D / 3.141592741012573D);
		this.rotationYaw = n;
		this.prevRotationYaw = n;
		float n2 = (float) (Math.atan2(d1, f3) * 180.0D / 3.141592741012573D);
		this.rotationPitch = n2;
		this.prevRotationPitch = n2;
		this.ticksInGround = 0;
	}

	@Override
	public void setVelocity(double d, double d1, double d2)
	{
		this.motionX = d;
		this.motionY = d1;
		this.motionZ = d2;
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt_double(d * d + d2 * d2);
			float n = (float) (Math.atan2(d, d2) * 180.0D / 3.141592741012573D);
			this.rotationYaw = n;
			this.prevRotationYaw = n;
			float n2 = (float) (Math.atan2(d1, f) * 180.0D / 3.141592741012573D);
			this.rotationPitch = n2;
			this.prevRotationPitch = n2;
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.ticksInGround = 0;
		}

	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			float n = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / 3.141592741012573D);
			this.rotationYaw = n;
			this.prevRotationYaw = n;
			float n2 = (float) (Math.atan2(this.motionY, f) * 180.0D / 3.141592741012573D);
			this.rotationPitch = n2;
			this.prevRotationPitch = n2;
		}

		if (this.fuse-- <= 0)
		{
			if (IC2.platform.isSimulating())
			{
				this.setDead();
				this.explode();
			}
			else
				this.setDead();
		}
		else if (this.fuse < 100 && this.fuse % 2 == 0)
			this.worldObj.spawnParticle("smoke", this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);

		if (this.inGround)
		{
			++this.ticksInGround;
			if (this.ticksInGround >= 200)
				this.setDead();

			if (this.sticky)
			{
				this.fuse -= 3;
				this.motionX = 0.0D;
				this.motionY = 0.0D;
				this.motionZ = 0.0D;
				if (this.worldObj.getBlock(this.stickX, this.stickY, this.stickZ) != null)
					return;
			}
		}

		++this.ticksInAir;
		Vec3 vec3d = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
		Vec3 vec3d2 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		MovingObjectPosition movingobjectposition = this.worldObj.func_147447_a(vec3d, vec3d2, false, true, false);
		vec3d = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
		vec3d2 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		if (movingobjectposition != null)
		{
			vec3d2 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
			float remainX = (float) (movingobjectposition.hitVec.xCoord - this.posX);
			float remainY = (float) (movingobjectposition.hitVec.yCoord - this.posY);
			float remainZ = (float) (movingobjectposition.hitVec.zCoord - this.posZ);
			float f2 = MathHelper.sqrt_double(remainX * remainX + remainY * remainY + remainZ * remainZ);
			this.stickX = movingobjectposition.blockX;
			this.stickY = movingobjectposition.blockY;
			this.stickZ = movingobjectposition.blockZ;
			this.posX -= remainX / f2 * 0.0500000007450581D;
			this.posY -= remainY / f2 * 0.0500000007450581D;
			this.posZ -= remainZ / f2 * 0.0500000007450581D;
			this.posX += remainX;
			this.posY += remainY;
			this.posZ += remainZ;
			this.motionX *= 0.75F - this.rand.nextFloat();
			this.motionY *= -0.300000011920929D;
			this.motionZ *= 0.75F - this.rand.nextFloat();
			this.inGround = true;
		}
		else
		{
			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			this.inGround = false;
		}

		float f3 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / 3.141592741012573D);

		for (this.rotationPitch = (float) (Math.atan2(this.motionY, f3) * 180.0D / 3.141592741012573D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
			;

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
			this.prevRotationPitch += 360.0F;

		while (this.rotationYaw - this.prevRotationYaw < -180.0F)
			this.prevRotationYaw -= 360.0F;

		while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
			this.prevRotationYaw += 360.0F;

		this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
		this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
		float f4 = 0.98F;
		float f5 = 0.04F;
		if (this.isInWater())
		{
			this.fuse += 2000;

			for (int i1 = 0; i1 < 4; ++i1)
			{
				float f6 = 0.25F;
				this.worldObj.spawnParticle("bubble", this.posX - this.motionX * f6, this.posY - this.motionY * f6, this.posZ - this.motionZ * f6, this.motionX, this.motionY, this.motionZ);
			}

			f4 = 0.75F;
		}

		this.motionX *= f4;
		this.motionY *= f4;
		this.motionZ *= f4;
		this.motionY -= f5;
		this.setPosition(this.posX, this.posY, this.posZ);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setByte("inGround", (byte) (this.inGround ? 1 : 0));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		this.inGround = nbttagcompound.getByte("inGround") == 1;
	}

	@Override
	public float getShadowSize()
	{
		return 0.0F;
	}

	public void explode()
	{
    
    

		explosion.doExplosionA(1, 1, 1, 1, 1, 1);
		explosion.doExplosionB(true);
	}
}
