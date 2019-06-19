package ic2.core;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerWorld;
import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.ic2.EventConfig;
import ru.will.git.ic2.ModUtils;

import ic2.api.event.ExplosionEvent;
import ic2.core.util.Util;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class PointExplosion extends Explosion
{
	private final World world;
	private final Entity entity;
	private final float dropRate;
    
    

	public PointExplosion(World world1, Entity entity, EntityLivingBase exploder, double x, double y, double z, float power, float dropRate1, int entityDamage1)
	{
		super(world1, exploder, x, y, z, power);
		this.world = world1;
		this.entity = entity;
		this.dropRate = dropRate1;
    
		this.fake = new FakePlayerContainerWorld(ModUtils.profile, this.world);
		if (entity instanceof EntityPlayer)
			this.fake.setRealPlayer((EntityPlayer) entity);
		else if (exploder instanceof EntityPlayer)
    
	}

	@Override
	public void doExplosionA()
    
		if (!EventConfig.explosionEnabled)
    

		ExplosionEvent event = new ExplosionEvent(this.world, this.entity, this.explosionX, this.explosionY, this.explosionZ, (double) this.explosionSize, (EntityLivingBase) this.exploder, 0, 1.0D);
		if (!MinecraftForge.EVENT_BUS.post(event))
		{
			for (int x = Util.roundToNegInf(this.explosionX) - 1; x <= Util.roundToNegInf(this.explosionX) + 1; ++x)
				for (int y = Util.roundToNegInf(this.explosionY) - 1; y <= Util.roundToNegInf(this.explosionY) + 1; ++y)
					for (int z = Util.roundToNegInf(this.explosionZ) - 1; z <= Util.roundToNegInf(this.explosionZ) + 1; ++z)
					{
						Block block = this.world.getBlock(x, y, z);
						if (block.getExplosionResistance(this.exploder, this.world, x, y, z, this.explosionX, this.explosionY, this.explosionZ) < this.explosionSize * 10.0F)
    
							if (EventConfig.explosionRegionOnly && !EventUtils.isInPrivate(this.world, x, y, z))
								continue;
							if (EventConfig.explosionEvent && this.fake.cantBreak(x, y, z))
    

							this.affectedBlockPositions.add(new ChunkPosition(x, y, z));
						}
					}

			for (Entity entity : (Iterable<Entity>) this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, AxisAlignedBB.getBoundingBox(this.explosionX - 2.0D, this.explosionY - 2.0D, this.explosionZ - 2.0D, this.explosionX + 2.0D, this.explosionY + 2.0D, this.explosionZ + 2.0D)))
    
				if (this.fake.cantDamage(entity))
    

				entity.attackEntityFrom(DamageSource.setExplosionSource(this), this.entityDamage);
			}

			this.explosionSize = 1.0F / this.dropRate;
		}
	}
}