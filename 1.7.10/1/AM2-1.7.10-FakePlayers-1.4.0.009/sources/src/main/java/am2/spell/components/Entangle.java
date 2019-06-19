package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.RitualShapeHelper;
import am2.api.ArsMagicaApi;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.buffs.BuffEffectEntangled;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleApproachEntity;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Entangle implements ISpellComponent, IRitualInteraction
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
			    

			int duration = 140 + SpellUtils.instance.getModifiedInt_Mul(60, stack, caster, target, world, 0, SpellModifiers.DURATION);
			duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);
			int x = (int) Math.floor(target.posX);
			int y = (int) Math.floor(target.posY);
			int z = (int) Math.floor(target.posZ);
			if (RitualShapeHelper.instance.checkForRitual(this, world, x, y, z) != null)
			{
				duration += 3600 * (SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0) + 1);
				RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
			}

			if (!world.isRemote)
				((EntityLivingBase) target).addPotionEffect(new BuffEffectEntangled(duration, SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0)));

			return true;
		}
		else
			return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 80.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		ArsMagicaApi var10000 = ArsMagicaApi.instance;
		return ArsMagicaApi.getBurnoutFromMana(this.manaCost(caster)) * 4.0F;
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
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "plant", x, y, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 2.0D, 1.0D);
				particle.AddParticleController(new ParticleApproachEntity(particle, target, 0.15000000596046448D, 0.4000000059604645D, 1, false));
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
		return EnumSet.of(Affinity.NATURE);
	}

	@Override
	public int getID()
	{
		return 14;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[3];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 6);
		var10000[1] = Blocks.vine;
		var10000[2] = Items.slime_ball;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.05F;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape()
	{
		return RitualShapeHelper.instance.hourglass;
	}

	@Override
	public ItemStack[] getReagents()
	{
		return new ItemStack[] { new ItemStack(Items.slime_ball), new ItemStack(Blocks.web) };
	}

	@Override
	public int getReagentSearchRadius()
	{
		return 3;
	}
}
