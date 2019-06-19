package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleArcToEntity;
import am2.playerextensions.ExtendedProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ManaDrain implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (!(target instanceof EntityLivingBase))
			return false;
		else
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			double manaStolen = 250.0D;
			ExtendedProperties targetProperties = ExtendedProperties.For((EntityLivingBase) target);
			if (manaStolen > targetProperties.getCurrentMana())
				manaStolen = targetProperties.getCurrentMana();

			targetProperties.setCurrentMana((float) (targetProperties.getCurrentMana() - manaStolen));
			targetProperties.forceSync();
			ExtendedProperties casterProperties = ExtendedProperties.For(caster);
			casterProperties.setCurrentMana((float) (casterProperties.getCurrentMana() + manaStolen));
			casterProperties.forceSync();
			return true;
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 20.0F;
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
		for (int i = 0; i < 15; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 1.0D, 1.0D);
				particle.setIgnoreMaxAge(true);
				particle.AddParticleController(new ParticleArcToEntity(particle, 1, caster, false).SetSpeed(0.03F).generateControlPoints());
				particle.setRGBColorF(0.0F, 0.4F, 1.0F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.ARCANE);
	}

	@Override
	public int getID()
	{
		return 36;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[3];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 0);
		ItemOre var1 = ItemsCommonProxy.itemOre;
		var10000[1] = new ItemStack(ItemsCommonProxy.itemOre, 1, 7);
		var1 = ItemsCommonProxy.itemOre;
		var10000[2] = new ItemStack(ItemsCommonProxy.itemOre, 1, 0);
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
