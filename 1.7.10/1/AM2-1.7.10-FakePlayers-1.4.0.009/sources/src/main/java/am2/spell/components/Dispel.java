package am2.spell.components;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.buffs.BuffEffect;
import am2.buffs.BuffList;
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleOrbitEntity;
import am2.playerextensions.ExtendedProperties;
import am2.utility.EntityUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class Dispel implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (target instanceof EntityLivingBase && !(target instanceof IBossDisplayData))
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			if (EntityUtilities.isSummon((EntityLivingBase) target) && EntityUtilities.getOwner((EntityLivingBase) target) == caster.getEntityId())
			{
				target.attackEntityFrom(DamageSource.magic, 50000.0F);
				return true;
			}
			else
			{
				List<Integer> effectsToRemove = new ArrayList();
				Iterator iter = ((EntityLivingBase) target).getActivePotionEffects().iterator();
				int magnitudeLeft = 6;

				while (iter.hasNext())
				{
					Integer potionID = Integer.valueOf(((PotionEffect) iter.next()).getPotionID());
					BuffList var10000 = BuffList.instance;
					if (!BuffList.isDispelBlacklisted(potionID.intValue()))
					{
						PotionEffect pe = ((EntityLivingBase) target).getActivePotionEffect(Potion.potionTypes[potionID.intValue()]);
						int magnitudeCost = pe.getAmplifier();
						if (magnitudeLeft >= magnitudeCost)
						{
							magnitudeLeft -= magnitudeCost;
							effectsToRemove.add(potionID);
							if (pe instanceof BuffEffect && !world.isRemote)
								((BuffEffect) pe).stopEffect((EntityLivingBase) target);
						}
					}
				}

				if (effectsToRemove.size() == 0 && ExtendedProperties.For((EntityLivingBase) target).getNumSummons() == 0)
					return false;
				else
				{
					if (!world.isRemote)
						this.removePotionEffects((EntityLivingBase) target, effectsToRemove);

					return true;
				}
			}
		}
		else
			return false;
	}

	private void removePotionEffects(EntityLivingBase target, List<Integer> effectsToRemove)
	{
		for (Integer i : effectsToRemove)
		{
			if ((i.intValue() == BuffList.flight.id || i.intValue() == BuffList.levitation.id) && target instanceof EntityPlayer && target.isPotionActive(BuffList.flight.id))
			{
				((EntityPlayer) target).capabilities.isFlying = false;
				((EntityPlayer) target).capabilities.allowFlying = false;
			}

			target.removePotionEffect(i.intValue());
		}

	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 200.0F;
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
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 2.0D, 1.0D);
				particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.1F + rand.nextFloat() * 0.1F, 1, false));
				if (rand.nextBoolean())
					particle.setRGBColorF(0.7F, 0.1F, 0.7F);

				particle.setMaxAge(20);
				particle.setParticleScale(0.1F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
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
		return 10;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[4];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 13);
		ItemOre var1 = ItemsCommonProxy.itemOre;
		var10000[1] = new ItemStack(ItemsCommonProxy.itemOre, 1, 2);
		var1 = ItemsCommonProxy.itemOre;
		var10000[2] = new ItemStack(ItemsCommonProxy.itemOre, 1, 5);
		var10000[3] = Items.milk_bucket;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.0F;
	}
}
