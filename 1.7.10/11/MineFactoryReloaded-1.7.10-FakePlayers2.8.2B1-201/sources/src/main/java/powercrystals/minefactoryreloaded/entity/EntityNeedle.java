package powercrystals.minefactoryreloaded.entity;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.minefactoryreloaded.ModUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.MFRRegistry;

import java.util.List;

public class EntityNeedle extends Entity implements IProjectile, IEntityAdditionalSpawnData
{
	private String _owner;
	private int ticksInAir;
	private ItemStack _ammoSource;
	private double distance;
    
    

	public EntityNeedle(World var1)
	{
		super(var1);
		this.ticksInAir = 0;
		this.renderDistanceWeight = 10.0D;
		this.setSize(0.5F, 0.5F);
	}

	public EntityNeedle(World var1, EntityPlayer var2, ItemStack var3, float var4)
	{
		this(var1);
		this._owner = var2.getCommandSenderName();
		this._ammoSource = var3;
		this.setLocationAndAngles(var2.posX, var2.posY + var2.getEyeHeight(), var2.posZ, var2.rotationYaw, var2.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F);
		this.motionY = -MathHelper.sin(this.rotationPitch / 180.0F * 3.1415927F);
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, 3.25F, var4);
    
    
	}

	@Override
	public void writeSpawnData(ByteBuf var1)
	{
	}

	@Override
	public void readSpawnData(ByteBuf var1)
	{
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * 0.16F;
		this.posY -= 0.08D;
		this.posZ -= -(MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
	}

	@Override
	protected void entityInit()
	{
		this.dataWatcher.addObject(16, Byte.valueOf((byte) 0));
	}

	@Override
	public void setThrowableHeading(double var1, double var3, double var5, float var7, float var8)
	{
		double var9 = MathHelper.sqrt_double(var1 * var1 + var3 * var3 + var5 * var5);
		var1 = var1 / var9;
		var3 = var3 / var9;
		var5 = var5 / var9;
		var1 = var1 + this.rand.nextGaussian() * 0.0075D * var8;
		var3 = var3 + this.rand.nextGaussian() * 0.0075D * var8;
		var5 = var5 + this.rand.nextGaussian() * 0.0075D * var8;
		var1 = var1 * var7;
		var3 = var3 * var7;
		var5 = var5 * var7;
		this.motionX = var1;
		this.motionY = var3;
		this.motionZ = var5;
		float var11 = MathHelper.sqrt_double(var1 * var1 + var5 * var5);
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(var1, var5) * 180.0D / 3.141592653589793D);
		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(var3, var11) * 180.0D / 3.141592653589793D);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double var1, double var3, double var5, float var7, float var8, int var9)
	{
		this.setPosition(var1, var3, var5);
		this.setRotation(var7, var8);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double var1, double var3, double var5)
	{
		this.motionX = var1;
		this.motionY = var3;
		this.motionZ = var5;
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float var7 = MathHelper.sqrt_double(var1 * var1 + var5 * var5);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(var1, var5) * 180.0D / 3.141592653589793D);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(var3, var7) * 180.0D / 3.141592653589793D);
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
		}

	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float var1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / 3.141592653589793D);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, var1) * 180.0D / 3.141592653589793D);
		}

		++this.ticksInAir;
		Vec3 var17 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
		Vec3 var2 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		MovingObjectPosition mop = this.worldObj.func_147447_a(var17, var2, false, true, false);
		var17 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
		var2 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		if (mop != null)
			var2 = Vec3.createVectorHelper(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);

		Entity var4 = null;
		List var5 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
		double var6 = 0.0D;
		double var8 = 0.3D;
		EntityPlayer player = this._owner == null ? null : this.worldObj.getPlayerEntityByName(this._owner);

		for (int var11 = 0; var11 < var5.size(); ++var11)
		{
			Entity var12 = (Entity) var5.get(var11);
			if (var12 != player | this.ticksInAir >= 2 && var12.canBeCollidedWith())
			{
				AxisAlignedBB var13 = var12.boundingBox.expand(var8, var8, var8);
				MovingObjectPosition var14 = var13.calculateIntercept(var17, var2);
				if (var14 != null)
				{
					double var15 = var17.distanceTo(var14.hitVec);
					if (var15 < var6 || var6 == 0.0D)
					{
						var4 = var12;
						var6 = var15;
					}
				}
			}
		}

		if (var4 != null)
			mop = new MovingObjectPosition(var4);

		if (mop != null && mop.entityHit != null && mop.entityHit instanceof EntityPlayer)
		{
			EntityPlayer var21 = (EntityPlayer) mop.entityHit;
			if (var21.capabilities.disableDamage || player != null && !player.canAttackPlayer(var21))
				mop = null;
		}

		float var22 = 0.0F;
		var22 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		this.distance += var22;
		if (mop != null && !this.worldObj.isRemote)
		{
    
				if (mop.entityHit != null && !this.fake.cantDamage(mop.entityHit))
    
				else if (!this.fake.cantBreak(mop.blockX, mop.blockY, mop.blockZ))
					MFRRegistry.getNeedleAmmoTypes().get(this._ammoSource.getItem()).onHitBlock(this._ammoSource, player, this.worldObj, mop.blockX, mop.blockY, mop.blockZ, mop.sideHit, this.distance);

			this.setDead();
		}

		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		var22 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / 3.141592653589793D);

		for (this.rotationPitch = (float) (Math.atan2(this.motionY, var22) * 180.0D / 3.141592653589793D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
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
		float var25 = 0.995F;
		var8 = 0.05000000074505806D;
		if (this._falling | var22 < 0.05D)
		{
			this._falling = true;
			this.motionY -= 0.01D;
			var25 = 0.99F;
		}

		if (this.isInWater())
		{
			double var26 = 0.25D;

			for (int var27 = 0; var27 < 4; ++var27)
			{
				this.worldObj.spawnParticle("bubble", this.posX - this.motionX * var26, this.posY - this.motionY * var26, this.posZ - this.motionZ * var26, this.motionX, this.motionY, this.motionZ);
			}

			var25 = 0.8F;
		}

		this.motionX *= var25;
		this.motionY *= var25;
		this.motionZ *= var25;
		this.setPosition(this.posX, this.posY, this.posZ);
		this.func_145775_I();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound var1)
	{
		var1.setTag("ammoSource", this._ammoSource.writeToNBT(new NBTTagCompound()));
		var1.setDouble("distance", this.distance);
		var1.setBoolean("falling", this._falling);
		if (this._owner != null)
    
    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound var1)
	{
		if (var1.hasKey("ammoSource"))
		{
			this._ammoSource = ItemStack.loadItemStackFromNBT(var1.getCompoundTag("ammoSource"));
			this.distance = var1.getDouble("distance");
			this._falling = var1.getBoolean("falling");
			if (var1.hasKey("owner"))
				this._owner = var1.getString("owner");
    
    
	}

	@Override
	public boolean canTriggerWalking()
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
	public boolean canAttackWithItem()
	{
		return false;
	}
}
