package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.damage.DamageSources;
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LightningDamage implements ISpellComponent
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
			    

			float baseDamage = 12.0F;
			double damage = SpellUtils.instance.getModifiedDouble_Add(baseDamage, stack, caster, target, world, 0, SpellModifiers.DAMAGE);
			return SpellHelper.instance.attackTargetSpecial(stack, target, DamageSources.causeEntityLightningDamage(caster), SpellUtils.instance.modifyDamage(caster, (float) damage));
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 180.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 20.0F;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster)
	{
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier)
	{
		for (int i = 0; i < 5; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "lightning", x, y, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 0.5D, 1.0D);
				particle.addVelocity(rand.nextDouble() * 0.2D - 0.1D, rand.nextDouble() * 0.2D, rand.nextDouble() * 0.2D - 0.1D);
				particle.setAffectedByGravity();
				particle.setDontRequireControllers();
				particle.setMaxAge(5);
				particle.setParticleScale(0.1F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.LIGHTNING);
	}

	@Override
	public int getID()
	{
		return 34;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[4];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 16);
		var10000[1] = Items.iron_ingot;
		var10000[2] = Items.stick;
		ItemOre var1 = ItemsCommonProxy.itemOre;
		var10000[3] = new ItemStack(ItemsCommonProxy.itemOre, 1, 0);
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
