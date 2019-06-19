package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.network.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleMoveOnHeading;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Knockback implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (target instanceof EntityLivingBase)
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			double speed = 1.5D;
			speed = SpellUtils.instance.getModifiedDouble_Add(speed, stack, caster, target, world, 0, SpellModifiers.VELOCITY_ADDED);
			double vertSpeed = 0.325D;
			EntityLivingBase curEntity = (EntityLivingBase) target;
			double deltaZ = curEntity.posZ - caster.posZ;
			double deltaX = curEntity.posX - caster.posX;
			double angle = Math.atan2(deltaZ, deltaX);
			if (curEntity instanceof EntityPlayer)
				AMNetHandler.INSTANCE.sendVelocityAddPacket(world, curEntity, speed * Math.cos(angle), vertSpeed, speed * Math.sin(angle));
			else
			{
				curEntity.motionX += speed * Math.cos(angle);
				curEntity.motionZ += speed * Math.sin(angle);
				curEntity.motionY += vertSpeed;
			}

			return true;
		}
		else
			return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 60.0F;
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
		for (int i = 0; i < 25; ++i)
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

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.AIR, Affinity.WATER, Affinity.EARTH);
	}

	@Override
	public int getID()
	{
		return 28;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 16);
		var10000[1] = Blocks.piston;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
