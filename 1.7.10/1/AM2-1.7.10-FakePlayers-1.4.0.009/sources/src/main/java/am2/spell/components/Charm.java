package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.RitualShapeHelper;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.buffs.BuffEffectCharmed;
import am2.buffs.BuffList;
import am2.items.ItemCrystalPhylactery;
import am2.items.ItemEssence;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SpellUtils;
import am2.utility.EntityUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class Charm implements ISpellComponent, IRitualInteraction
{
	@Override
	public int getID()
	{
		return 59;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[3];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 14);
		ItemEssence var1 = ItemsCommonProxy.essence;
		var10000[1] = new ItemStack(ItemsCommonProxy.essence, 1, 8);
		ItemCrystalPhylactery var2 = ItemsCommonProxy.crystalPhylactery;
		var10000[2] = new ItemStack(ItemsCommonProxy.crystalPhylactery, 1, 0);
		return var10000;
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (target instanceof EntityCreature && !((EntityCreature) target).isPotionActive(BuffList.charmed) && !EntityUtilities.isSummon((EntityCreature) target))
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			int duration = SpellUtils.instance.getModifiedInt_Mul(600, stack, caster, target, world, 0, SpellModifiers.DURATION);
			duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);
			int x = (int) Math.floor(target.posX);
			int y = (int) Math.floor(target.posY);
			int z = (int) Math.floor(target.posZ);
			if (RitualShapeHelper.instance.checkForRitual(this, world, x, y, z) != null)
			{
				duration += 3600 * (SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0) + 1);
				RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
			}

			if (target instanceof EntityAnimal)
			{
				((EntityAnimal) target).func_146082_f((EntityPlayer) null);
				return true;
			}
			else if (ExtendedProperties.For(caster).getCanHaveMoreSummons())
			{
				if (caster instanceof EntityPlayer)
				{
					if (target instanceof EntityCreature)
					{
						BuffEffectCharmed charmBuff = new BuffEffectCharmed(duration, 1);
						charmBuff.setCharmer(caster);
						((EntityCreature) target).addPotionEffect(charmBuff);
					}

					return true;
				}
				else if (caster instanceof EntityLiving)
				{
					if (target instanceof EntityCreature)
					{
						BuffEffectCharmed charmBuff = new BuffEffectCharmed(duration, 2);
						charmBuff.setCharmer(caster);
						((EntityCreature) target).addPotionEffect(charmBuff);
					}

					return true;
				}
				else
					return false;
			}
			else
			{
				if (caster instanceof EntityPlayer)
					((EntityPlayer) caster).addChatMessage(new ChatComponentText("You cannot have any more summons."));

				return true;
			}
		}
		else
			return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 300.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 300.0F;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster)
	{
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier)
	{
		for (int i = 0; i < 10; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "heart", x, y, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 2.0D, 1.0D);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0.0F, 0.05F + rand.nextFloat() * 0.1F, 1, false));
				particle.setMaxAge(20);
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
	public float getAffinityShift(Affinity affinity)
	{
		return 0.1F;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape()
	{
		return RitualShapeHelper.instance.hourglass;
	}

	@Override
	public ItemStack[] getReagents()
	{
		return new ItemStack[] { new ItemStack(Items.wheat), new ItemStack(Items.wheat_seeds), new ItemStack(Items.carrot) };
	}

	@Override
	public int getReagentSearchRadius()
	{
		return 3;
	}
}
