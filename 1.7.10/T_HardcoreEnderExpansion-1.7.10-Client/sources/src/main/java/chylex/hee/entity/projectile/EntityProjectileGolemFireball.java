package chylex.hee.entity.projectile;

import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.util.BlockPosM;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityProjectileGolemFireball extends EntityLargeFireball
{
	public EntityProjectileGolemFireball(World world)
	{
		super(world);
		this.setSize(0.2F, 0.2F);
	}

	public EntityProjectileGolemFireball(World world, EntityLivingBase shooter, double x, double y, double z, double xDiff, double yDiff, double zDiff)
	{
		super(world, shooter, xDiff, yDiff, zDiff);
		this.setPosition(x, y, z);
		this.setSize(0.2F, 0.2F);
	}

	@Override
	protected void onImpact(MovingObjectPosition mop)
	{
		if (!this.worldObj.isRemote)
		{
			if (mop.entityHit != null)
				mop.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), ModCommonProxy.opMobs ? 8.0F : 4.0F);

			Explosion explosion = new EntityProjectileGolemFireball.FieryExplosion(this.worldObj, this.shootingEntity, this.posX, this.posY, this.posZ, ModCommonProxy.opMobs ? 3.0F : 2.35F);
			explosion.doExplosionA();
			explosion.doExplosionB(true);
			this.setDead();
		}
	}

	static class FieryExplosion extends Explosion
	{
		private final World world;

		public FieryExplosion(World world, Entity cause, double x, double y, double z, float strength)
		{
			super(world, cause, x, y, z, strength);
			this.world = world;
			this.isSmoking = world.getGameRules().getGameRuleBooleanValue("mobGriefing");
		}

		@Override
		public void doExplosionB(boolean doParticles)
		{
			    
			if (this.world.isRemote)
				this.affectedBlockPositions.clear();
			    

			super.doExplosionB(doParticles);

			BlockPosM tmpPos = BlockPosM.tmp();

			for (ChunkPosition pos : (Iterable<? extends ChunkPosition>) this.affectedBlockPositions)
			{
				if (tmpPos.set(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ).isAir(this.world) && this.world.rand.nextInt(9) == 0)
					tmpPos.setBlock(this.world, Blocks.fire);
			}
		}
	}
}
