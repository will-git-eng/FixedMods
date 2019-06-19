package thaumcraft.common.entities.projectile;

import ru.will.git.thaumcraft.EventConfig;
import ru.will.git.thaumcraft.entity.EntityThrowableByPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.api.potions.PotionFluxTaint;
import thaumcraft.client.fx.FXDispatcher;

import java.util.List;

public class EntityBottleTaint extends EntityThrowableByPlayer
{
	public EntityBottleTaint(World world)
	{
		super(world);
	}

	public EntityBottleTaint(World world, EntityLivingBase thrower)
	{
		super(world, thrower);
	}

	public EntityBottleTaint(World world, double x, double y, double z)
	{
		super(world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleStatusUpdate(byte id)
	{
		if (id == 3)
		{
			for (int a = 0; a < 100; ++a)
			{
				FXDispatcher.INSTANCE.taintsplosionFX(this);
			}

			FXDispatcher.INSTANCE.bottleTaintBreak(this.posX, this.posY, this.posZ);
		}
	}

	@Override
	protected void onImpact(RayTraceResult ray)
	{
		if (!this.world.isRemote)
		{
			
			if (!EventConfig.enableBottleTaint)
			{
				this.setDead();
				return;
			}

			if (EventConfig.potionFluxTaint)
			
			{
				List<EntityLivingBase> ents = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).grow(5.0D, 5.0D, 5.0D));
				if (ents.size() > 0)
					for (EntityLivingBase el : ents)
					{
						if (!(el instanceof ITaintedMob) && !el.isEntityUndead())
						{
							
							if (this.fake.cantAttack(el))
								continue;
							

							el.addPotionEffect(new PotionEffect(PotionFluxTaint.instance, 100, 0, false, true));
						}
					}
			}

			for (int a = 0; a < 10; ++a)
			{
				if (this.world.rand.nextBoolean())
				{
					int xx = (int) ((this.rand.nextFloat() - this.rand.nextFloat()) * 4.0F);
					int zz = (int) ((this.rand.nextFloat() - this.rand.nextFloat()) * 4.0F);
					BlockPos p = this.getPosition().add(xx, 0, zz);
					IBlockState newState = null;

					if (this.world.isBlockNormalCube(p.down(), false) && this.world.getBlockState(p).getBlock().isReplaceable(this.world, p))
						newState = BlocksTC.fluxGoo.getDefaultState();
					else
					{
						p = p.down();
						if (this.world.isBlockNormalCube(p.down(), false) && this.world.getBlockState(p).getBlock().isReplaceable(this.world, p))
							newState = BlocksTC.fluxGoo.getDefaultState();
					}

					if (newState != null)
					{
						
						if (this.fake.cantReplace(p, newState))
							continue;
						

						this.world.setBlockState(p, newState);
					}
				}
			}

			this.world.setEntityState(this, (byte) 3);
			this.setDead();
		}

	}
}
