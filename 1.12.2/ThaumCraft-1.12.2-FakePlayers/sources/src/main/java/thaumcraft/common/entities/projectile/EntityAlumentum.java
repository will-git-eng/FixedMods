package thaumcraft.common.entities.projectile;

import ru.will.git.thaumcraft.entity.EntityThrowableByPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.client.fx.FXDispatcher;


public class EntityAlumentum extends EntityThrowableByPlayer
{
	public EntityAlumentum(World par1World)
	{
		super(par1World);
	}

	public EntityAlumentum(World par1World, EntityLivingBase par2EntityLiving)
	{
		super(par1World, par2EntityLiving);
	}

	public EntityAlumentum(World par1World, double par2, double par4, double par6)
	{
		super(par1World, par2, par4, par6);
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy)
	{
		super.shoot(x, y, z, 0.75F, inaccuracy);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (this.world.isRemote)
		{
			for (double i = 0.0D; i < 3.0D; ++i)
			{
				double coeff = i / 3.0D;
				FXDispatcher.INSTANCE.drawAlumentum((float) (this.prevPosX + (this.posX - this.prevPosX) * coeff), (float) (this.prevPosY + (this.posY - this.prevPosY) * coeff) + this.height / 2.0F, (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * coeff), 0.0125F * (this.rand.nextFloat() - 0.5F), 0.0125F * (this.rand.nextFloat() - 0.5F), 0.0125F * (this.rand.nextFloat() - 0.5F), this.rand.nextFloat() * 0.2F, this.rand.nextFloat() * 0.1F, this.rand.nextFloat() * 0.1F, 0.5F, 4.0F);
				FXDispatcher.INSTANCE.drawGenericParticles(this.posX + this.world.rand.nextGaussian() * 0.20000000298023224D, this.posY + this.world.rand.nextGaussian() * 0.20000000298023224D, this.posZ + this.world.rand.nextGaussian() * 0.20000000298023224D, 0.0D, 0.0D, 0.0D, 1.0F, 1.0F, 1.0F, 0.7F, false, 448, 8, 1, 8, 0, 0.3F, 0.0F, 1);
			}
		}

	}

	@Override
	protected void onImpact(RayTraceResult par1RayTraceResult)
	{
		if (!this.world.isRemote)
		{
			
			// this.world.createExplosion(this, this.posX, this.posY, this.posZ, 1.1F, true);
			this.fake.createExplosion(this, this.posX, this.posY, this.posZ, 1.1F, true);
			

			this.setDead();
		}
	}
}
