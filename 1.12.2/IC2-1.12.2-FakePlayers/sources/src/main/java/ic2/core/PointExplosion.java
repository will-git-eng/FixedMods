package ic2.core;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import ru.will.git.ic2.EventConfig;
import ru.will.git.ic2.ModUtils;
import ic2.api.event.ExplosionEvent;
import ic2.core.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
	private float explosionSize;

	
	public final FakePlayerContainer fake;
	

	public PointExplosion(World world, Entity entity, EntityLivingBase exploder, double x, double y, double z, float power, float dropRate1, int entityDamage)
	{
		super(world, exploder, x, y, z, power, true, true);
		this.world = world;
		this.entity = entity;
		this.dropRate = dropRate1;
		this.entityDamage = entityDamage;
		this.explosionSize = power;

		
		this.fake = ModUtils.NEXUS_FACTORY.wrapFake(world);
		if (!this.fake.setRealPlayer(entity))
			this.fake.setRealPlayer(exploder);
		
	}

	@Override
	public void doExplosionA()
	{
		double explosionX = this.getPosition().x;
		double explosionY = this.getPosition().y;
		double explosionZ = this.getPosition().z;
		ExplosionEvent event = new ExplosionEvent(this.world, this.entity, this.getPosition(), this.explosionSize, this.getExplosivePlacedBy(), 0, 1.0D);
		if (!MinecraftForge.EVENT_BUS.post(event))
		{
			for (int x = Util.roundToNegInf(explosionX) - 1; x <= Util.roundToNegInf(explosionX) + 1; ++x)
			{
				for (int y = Util.roundToNegInf(explosionY) - 1; y <= Util.roundToNegInf(explosionY) + 1; ++y)
				{
					for (int z = Util.roundToNegInf(explosionZ) - 1; z <= Util.roundToNegInf(explosionZ) + 1; ++z)
					{
						BlockPos pos = new BlockPos(x, y, z);
						IBlockState block = this.world.getBlockState(pos);
						if (block.getBlock().getExplosionResistance(this.world, pos, this.getExplosivePlacedBy(), this) < this.explosionSize * 10.0F)
						{
							
							if (EventConfig.explosionEvent && this.fake.cantBreak(pos))
								continue;
							

							this.getAffectedBlockPositions().add(pos);
						}
					}
				}
			}

			for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this.getExplosivePlacedBy(), new AxisAlignedBB(explosionX - 2.0D, explosionY - 2.0D, explosionZ - 2.0D, explosionX + 2.0D, explosionY + 2.0D, explosionZ + 2.0D)))
			{
				
				if (EventConfig.explosionEvent && this.fake.cantAttack(entity))
					continue;
				

				entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), this.entityDamage);
			}

			this.explosionSize = 1.0F / this.dropRate;
		}
	}
}
