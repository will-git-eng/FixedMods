package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.blocks.BlocksCommonProxy;
import am2.damage.DamageSources;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleHoldPosition;
import am2.particles.ParticleOrbitEntity;
import am2.playerextensions.AffinityData;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Heal implements ISpellComponent
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
			    

			if (((EntityLivingBase) target).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD)
			{
				int healing = SpellUtils.instance.getModifiedInt_Mul(10, stack, caster, target, world, 0, SpellModifiers.HEALING);
				target.setFire(2);
				return SpellHelper.instance.attackTargetSpecial(stack, target, DamageSources.causeEntityHolyDamage(caster), healing * (0.5F + 2.0F * AffinityData.For(caster).getAffinityDepth(Affinity.LIFE)));
			}

			int healing = SpellUtils.instance.getModifiedInt_Mul(2, stack, caster, target, world, 0, SpellModifiers.HEALING);
			if (ExtendedProperties.For((EntityLivingBase) target).getCanHeal())
			{
				((EntityLivingBase) target).heal(healing);
				ExtendedProperties.For((EntityLivingBase) target).setHealCooldown(60);
				return true;
			}
		}

		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 225.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		ArsMagicaApi var10000 = ArsMagicaApi.instance;
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
		if (target instanceof EntityLivingBase && ((EntityLivingBase) target).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD)
			for (int i = 0; i < 25; ++i)
			{
				AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "symbols", x, y - 1.0D, z);
				if (particle != null)
				{
					particle.addRandomOffset(1.0D, 1.0D, 1.0D);
					particle.AddParticleController(new ParticleHoldPosition(particle, 20, 1, true));
					particle.AddParticleController(new ParticleFloatUpward(particle, 0.0F, -0.01F, 2, false));
					particle.AddParticleController(new ParticleFadeOut(particle, 2, false).setFadeSpeed(0.02F));
					particle.setParticleScale(0.1F);
					particle.setRGBColorF(1.0F, 0.2F, 0.2F);
				}
			}
		else
			for (int i = 0; i < 25; ++i)
			{
				AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "sparkle", x, y - 1.0D, z);
				if (particle != null)
				{
					particle.addRandomOffset(1.0D, 1.0D, 1.0D);
					particle.AddParticleController(new ParticleFloatUpward(particle, 0.0F, 0.1F, 1, false));
					particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.5D, 2, false).setIgnoreYCoordinate(true).SetTargetDistance(0.30000001192092896D + rand.nextDouble() * 0.3D));
					particle.setMaxAge(20);
					particle.setParticleScale(0.2F);
					particle.setRGBColorF(0.1F, 1.0F, 0.1F);
					if (colorModifier > -1)
						particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
				}
			}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.LIFE);
	}

	@Override
	public int getID()
	{
		return 25;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 6);
		var10000[1] = BlocksCommonProxy.aum;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.05F;
	}
}
