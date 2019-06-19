package powercrystals.minefactoryreloaded.entity;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.minefactoryreloaded.EventConfig;
import ru.will.git.minefactoryreloaded.ExplosionByPlayer;
import ru.will.git.minefactoryreloaded.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.setup.MFRConfig;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class EntityRocket extends Entity
{
	private int _ticksAlive;
	private String _owner;
	private Entity _target;
    
    

	public EntityRocket(World var1)
	{
		super(var1);
		this._ticksAlive = 0;
		this.renderDistanceWeight = 10.0D;
		this._lostTarget = null;
	}

	public EntityRocket(World var1, EntityLivingBase var2)
	{
		this(var1);
		this.setSize(1.0F, 1.0F);
		this.setLocationAndAngles(var2.posX, var2.posY + var2.getEyeHeight(), var2.posZ, var2.rotationYaw, var2.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.recalculateVelocity();
		if (var2 instanceof EntityPlayer)
		{
    
    
		}
	}

	public EntityRocket(World var1, EntityLivingBase var2, Entity var3)
	{
		this(var1, var2);
		this._target = var3;
	}

	private void recalculateVelocity()
	{
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F);
		this.motionY = -MathHelper.sin(this.rotationPitch / 180.0F * 3.1415927F);
	}

	@Override
	protected void entityInit()
	{
	}

	@Override
	public void onUpdate()
    
		if (!EventConfig.enableRocket)
		{
			this.setDead();
			return;
    

		super.onUpdate();
		++this._ticksAlive;
		if (this._ticksAlive > 600)
		{
    
				ExplosionByPlayer.newExplosion(this.fake.get(), this.worldObj, this, this.posX, this.posY, this.posZ, 4.0F, true, true);

			this.setDead();
		}

		if (this.worldObj.isRemote)
			for (int var1 = 0; var1 < 4; ++var1)
			{
				this.worldObj.spawnParticle("smoke", this.posX + this.motionX * var1 / 4.0D, this.posY + this.motionY * var1 / 4.0D, this.posZ + this.motionZ * var1 / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ);
			}

		if (!this.worldObj.isRemote)
		{
			Vec3 var18 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			Vec3 var2 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			MovingObjectPosition mop = this.worldObj.func_147447_a(var18, var2, false, true, false);
			var18 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			var2 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			if (mop != null)
				var2 = Vec3.createVectorHelper(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);

			Entity var4 = null;
			List var5 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double var6 = 0.0D;
			double var8 = 0.3D;
			EntityPlayer var10 = this._owner == null ? null : this.worldObj.getPlayerEntityByName(this._owner);
			int var11 = 0;

			for (int var12 = var5.size(); var11 < var12; ++var11)
			{
				Entity var13 = (Entity) var5.get(var11);
				if (var13 != var10 | this._ticksAlive > 5 && var13.canBeCollidedWith())
				{
					AxisAlignedBB var14 = var13.boundingBox.expand(var8, var8, var8);
					MovingObjectPosition var15 = var14.calculateIntercept(var18, var2);
					if (var15 != null)
					{
						double var16 = var18.distanceTo(var15.hitVec);
						if (var16 < var6 | var6 == 0.0D)
						{
							var4 = var13;
							var6 = var16;
						}
					}
				}
			}

			if (var4 != null)
				mop = new MovingObjectPosition(var4);

			if (mop != null && mop.entityHit instanceof EntityPlayer)
			{
				EntityPlayer var27 = (EntityPlayer) mop.entityHit;
				if (var27.capabilities.disableDamage || var10 != null && !var10.canAttackPlayer(var27))
					mop = null;
			}

			if (mop != null && !this.worldObj.isRemote)
			{
    
					ExplosionByPlayer.newExplosion(this.fake.get(), this.worldObj, this, mop.entityHit.posX, mop.entityHit.posY, mop.entityHit.posZ, 4.0F, true, true);
    
					ExplosionByPlayer.newExplosion(this.fake.get(), this.worldObj, this, mop.blockX, mop.blockY, mop.blockZ, 4.0F, true, true);

				this.setDead();
			}
		}

		Vec3 var20 = this.findTarget();
		if (var20 != null)
		{
			float var22 = this.clampAngle(360.0F - (float) (Math.atan2(var20.xCoord, var20.zCoord) * 180.0D / 3.141592653589793D), 360.0F, false);
			float var23 = this.clampAngle(-((float) (Math.atan2(var20.yCoord, Math.sqrt(var20.xCoord * var20.xCoord + var20.zCoord * var20.zCoord)) * 180.0D / 3.141592653589793D)), 360.0F, false);
			float var24 = this.clampAngle(var22 - this.rotationYaw, 3.0F, true);
			float var25 = this.clampAngle(var23 - this.rotationPitch, 3.0F, true);
			float var26;
			if (Math.max(var22, this.rotationYaw) - Math.min(var22, this.rotationYaw) > 180.0F)
				var26 = this.rotationYaw - var24;
			else
				var26 = this.rotationYaw + var24;

			float var7;
			if (Math.max(var23, this.rotationPitch) - Math.min(var23, this.rotationPitch) > 180.0F)
				var7 = this.rotationPitch - var25;
			else
				var7 = this.rotationPitch + var25;

			this.rotationYaw = this.clampAngle(var26, 360.0F, false);
			this.rotationPitch = this.clampAngle(var7, 360.0F, false);
			this.recalculateVelocity();
		}

		this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
	}

	private float clampAngle(float var1, float var2, boolean var3)
	{
		if (var1 >= 0.0F)
			var1 = var1 % 360.0F;
		else
			var1 = -(-var1 % 360.0F);

		if (var1 < 0.0F & !var3)
			var1 += 360.0F;

		if (Math.abs(var1) > var2)
			var1 = Math.copySign(var2, var1);

		return var1;
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
			double var7 = MathHelper.sqrt_double(var1 * var1 + var5 * var5);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(var1, var5) * 180.0D / 3.141592653589793D);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(var3, var7) * 180.0D / 3.141592653589793D);
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
		}

	}

	private Vec3 findTarget()
	{
		if (this._lostTarget != null)
		{
			UUID var1 = new UUID(this._lostTarget.getLong("UUIDMost"), this._lostTarget.getLong("UUIDLeast"));
			double var2 = this._lostTarget.getDouble("xTarget");
			double var4 = this._lostTarget.getDouble("yTarget");
			double var6 = this._lostTarget.getDouble("zTarget");
			List var8 = this.worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(var2 - 5.0D, var4 - 5.0D, var6 - 5.0D, var2 + 5.0D, var4 + 5.0D, var6 + 5.0D));
			Iterator var9 = var8.iterator();

			Entity var10;
			while (true)
			{
				if (!var9.hasNext())
					return Vec3.createVectorHelper(var2 - this.posX, var4 - this.posY, var6 - this.posZ);

				var10 = (Entity) var9.next();
				if (var10.getUniqueID().equals(var1))
					break;
			}

			this._target = var10;
			this._lostTarget = null;
		}

		return this._target != null ? Vec3.createVectorHelper(this._target.posX - this.posX, this._target.posY - this.posY + this._target.getEyeHeight(), this._target.posZ - this.posZ) : null;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound var1)
	{
		if (this._target != null)
		{
			NBTTagCompound var2 = new NBTTagCompound();
			var1.setDouble("xTarget", this._target.posX);
			var1.setDouble("yTarget", this._target.posY);
			var1.setDouble("zTarget", this._target.posZ);
			UUID var3 = this._target.getUniqueID();
			var1.setLong("UUIDMost", var3.getMostSignificantBits());
			var1.setLong("UUIDLeast", var3.getLeastSignificantBits());
			var1.setTag("target", var2);
		}

		if (this._owner != null)
    
    
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound var1)
	{
		if (var1.hasKey("target"))
			this._lostTarget = var1.getCompoundTag("target");

		if (var1.hasKey("owner"))
    
    
	}
}
