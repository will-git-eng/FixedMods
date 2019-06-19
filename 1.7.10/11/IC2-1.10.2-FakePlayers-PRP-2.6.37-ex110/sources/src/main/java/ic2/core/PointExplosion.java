package ic2.core;

import ru.will.git.ic2.FakePlayerContainer;
import ru.will.git.ic2.FakePlayerContainerWorld;
import ru.will.git.ic2.ModUtils;

import ic2.api.event.ExplosionEvent;
import ic2.core.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class PointExplosion extends Explosion
{
	private final World world;
	private final Entity entity;
	private final float dropRate;
	private final int entityDamage;
    
    

	public PointExplosion(World world1, Entity entity, EntityLivingBase exploder, double x, double y, double z, float power, float dropRate, int entityDamage)
	{
		super(world1, exploder, x, y, z, power, true, true);
		this.world = world1;
		this.entity = entity;
		this.dropRate = dropRate;
		this.entityDamage = entityDamage;
    
		this.fake = new FakePlayerContainerWorld(ModUtils.profile, this.world);
		if (entity instanceof EntityPlayer)
			this.fake.setRealPlayer((EntityPlayer) entity);
		else if (exploder instanceof EntityPlayer)
    
	}

	@Override
	public void doExplosionA()
	{
		double explosionX = this.getPosition().xCoord;
		double explosionY = this.getPosition().yCoord;
		double explosionZ = this.getPosition().zCoord;
		ExplosionEvent event = new ExplosionEvent(this.world, this.entity, this.getPosition(), this.explosionSize, this.getExplosivePlacedBy(), 0, 1.0D);
		if (!MinecraftForge.EVENT_BUS.post(event))
		{
			for (int x = Util.roundToNegInf(explosionX) - 1; x <= Util.roundToNegInf(explosionX) + 1; ++x)
				for (int y = Util.roundToNegInf(explosionY) - 1; y <= Util.roundToNegInf(explosionY) + 1; ++y)
					for (int z = Util.roundToNegInf(explosionZ) - 1; z <= Util.roundToNegInf(explosionZ) + 1; ++z)
					{
						BlockPos pos = new BlockPos(x, y, z);
						IBlockState block = this.world.getBlockState(pos);
						if (block.getBlock().getExplosionResistance(this.world, pos, this.getExplosivePlacedBy(), this) < this.explosionSize * 10.0F)
    
							if (this.fake.cantBreak(this.world, pos))
    

							this.getAffectedBlockPositions().add(pos);
						}
					}

			for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this.getExplosivePlacedBy(), new AxisAlignedBB(explosionX - 2.0D, explosionY - 2.0D, explosionZ - 2.0D, explosionX + 2.0D, explosionY + 2.0D, explosionZ + 2.0D)))
    
				if (this.fake.cantDamage(entity))
    

				entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), this.entityDamage);
			}

			this.explosionSize = 1.0F / this.dropRate;
		}
	}
}
