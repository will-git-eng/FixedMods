package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.network.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleMoveOnHeading;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Repel implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (target == null)
			return false;
		else if (target != caster)
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			this.performRepel(world, caster, target);
			return true;
		}
		else
		{
			EntityLivingBase source = caster;
			if (target instanceof EntityLivingBase)
				source = (EntityLivingBase) target;

			for (Entity e : (Iterable<Entity>) world.getEntitiesWithinAABB(Entity.class, source.boundingBox.expand(2.0D, 2.0D, 2.0D)))
			{
				    
				if (target.isDead || EventUtils.cantDamage(caster, e))
					continue;
				    

				this.performRepel(world, caster, e);
			}

			return true;
		}
	}

	private void performRepel(World world, EntityLivingBase caster, Entity target)
	{
		Vec3 casterPos = Vec3.createVectorHelper(caster.posX, caster.posY, caster.posZ);
		Vec3 targetPos = Vec3.createVectorHelper(target.posX, target.posY, target.posZ);
		double distance = casterPos.distanceTo(targetPos) + 0.1D;
		Vec3 delta = Vec3.createVectorHelper(targetPos.xCoord - casterPos.xCoord, targetPos.yCoord - casterPos.yCoord, targetPos.zCoord - casterPos.zCoord);
		double dX = delta.xCoord / 2.5D / distance;
		double dY = delta.yCoord / 2.5D / distance;
		double dZ = delta.zCoord / 2.5D / distance;
		if (target instanceof EntityPlayer)
			AMNetHandler.INSTANCE.sendVelocityAddPacket(world, (EntityPlayer) target, dX, dY, dZ);

		target.motionX += dX;
		target.motionY += dY;
		target.motionZ += dZ;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 5.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return ArsMagicaApi.getBurnoutFromMana(this.manaCost(caster));
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster)
	{
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier)
	{
		AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "sparkle", x, y, z);
		if (particle != null)
		{
			particle.addRandomOffset(1.0D, 2.0D, 1.0D);
			double dx = caster.posX - target.posX;
			double dz = caster.posZ - target.posZ;
			double angle = Math.toDegrees(Math.atan2(-dz, -dx));
			particle.AddParticleController(new ParticleMoveOnHeading(particle, angle, 0.0D, 0.1D + rand.nextDouble() * 0.5D, 1, false));
			particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05F));
			particle.setMaxAge(20);
			if (colorModifier > -1)
				particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.NONE);
	}

	@Override
	public int getID()
	{
		return 47;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 15);
		var10000[1] = Items.water_bucket;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.0F;
	}
}
