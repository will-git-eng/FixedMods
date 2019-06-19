package chylex.hee.entity.projectile;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.DragonUtil;
import ru.will.git.hee.ModUtils;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityProjectileFlamingBall extends EntityFireball
{
	public EntityProjectileFlamingBall(World world)
	{
		super(world);
		this.setSize(0.15F, 0.15F);
	}

	public EntityProjectileFlamingBall(World world, EntityLivingBase shooter, double x, double y, double z, double xDiff, double yDiff, double zDiff)
	{
		super(world, shooter, xDiff, yDiff, zDiff);
		this.setSize(0.15F, 0.15F);
		this.setPosition(x, y, z);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (this.worldObj.isRemote)
		{
			HardcoreEnderExpansion.fx.flame(this.worldObj, this.posX, this.posY + 0.4D, this.posZ, 5);
			if (this.ticksExisted == 1 && this.rand.nextInt(4) <= 1)
				this.worldObj.playSound(this.posX, this.posY, this.posZ, "mob.ghast.fireball", 0.8F, this.rand.nextFloat() * 0.1F + 1.2F, false);
		}

		if (this.ticksExisted > 35)
			this.setDead();

	}

	@Override
	protected void onImpact(MovingObjectPosition mop)
	{
		if (!this.worldObj.isRemote)
		{
			if (mop.entityHit != null && this.shootingEntity != null)
			{
				if (mop.entityHit instanceof EntityProjectileFlamingBall)
					return;

				boolean isLiving = mop.entityHit instanceof EntityLivingBase;
				if (isLiving && this.rand.nextInt(ModCommonProxy.opMobs ? 3 : 4) == 0)
				{
					double[] vec = DragonUtil.getNormalizedVector(this.shootingEntity.posX - mop.entityHit.posX, this.shootingEntity.posZ - mop.entityHit.posZ);
					((EntityLivingBase) mop.entityHit).knockBack(this.shootingEntity, 0.9F, vec[0] * 0.15D * (0.85D + 0.2D * this.rand.nextDouble()), vec[1] * 0.15D * (0.85D + 0.4D * this.rand.nextDouble()));
				}

				mop.entityHit.attackEntityFrom(DamageSource.onFire, 3.0F);
				if (isLiving)
					mop.entityHit.attackEntityFrom(DamageSource.causeMobDamage(this.shootingEntity), ModCommonProxy.opMobs ? 8.0F : 5.0F);

				mop.entityHit.fire += 3 + EnchantmentProtection.getFireTimeForEntity(mop.entityHit, 25);
			}

			else if (ModUtils.canMobGrief(this.worldObj) && this.rand.nextInt(3) == 0)
			{
				switch (mop.sideHit)
				{
					case 2:
						--mop.blockZ;
						break;
					case 3:
						++mop.blockZ;
						break;
					case 4:
						--mop.blockX;
						break;
					case 5:
						++mop.blockX;
				}

				BlockPosM tmpPos = BlockPosM.tmp();
				if (tmpPos.set(mop.blockX, mop.blockY, mop.blockZ).getMaterial(this.worldObj) == Material.air)
					tmpPos.setBlock(this.worldObj, Blocks.fire);
				else if (tmpPos.set(mop.blockX, mop.blockY + 1, mop.blockZ).getMaterial(this.worldObj) == Material.air)
					tmpPos.setBlock(this.worldObj, Blocks.fire);
			}

			this.setDead();
		}
	}

	@Override
	protected float getMotionFactor()
	{
		return 0.8F;
	}

	@Override
	public boolean isBurning()
	{
		return false;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return false;
	}

	@Override
	public boolean isEntityInvulnerable()
	{
		return true;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.ticksExisted = 30;
	}
}
