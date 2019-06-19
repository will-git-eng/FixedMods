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
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleArcToEntity;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class LifeDrain implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (!world.isRemote && target instanceof EntityLivingBase && ((EntityLivingBase) target).getCreatureAttribute() != EnumCreatureAttribute.UNDEAD)
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			int magnitude = SpellUtils.instance.getModifiedInt_Add(4, stack, caster, target, world, 0, SpellModifiers.DAMAGE);
			boolean success = SpellHelper.instance.attackTargetSpecial(stack, target, DamageSource.causeIndirectMagicDamage(caster, caster), SpellUtils.instance.modifyDamage(caster, magnitude));
			if (success)
			{
				caster.heal((int) Math.ceil(magnitude / 4));
				return true;
			}
			else
				return false;
		}
		else
			return true;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 300.0F;
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
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "ember", x, y, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 1.0D, 1.0D);
				particle.setIgnoreMaxAge(true);
				particle.AddParticleController(new ParticleArcToEntity(particle, 1, caster, false).SetSpeed(0.03F).generateControlPoints());
				particle.setRGBColorF(1.0F, 0.2F, 0.2F);
				particle.SetParticleAlpha(0.5F);
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
		return 31;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[3];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 0);
		ItemOre var1 = ItemsCommonProxy.itemOre;
		var10000[1] = new ItemStack(ItemsCommonProxy.itemOre, 1, 6);
		var10000[2] = BlocksCommonProxy.aum;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
