package am2.spell.components;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.RitualShapeHelper;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.blocks.BlocksCommonProxy;
import am2.items.ItemEssence;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleApproachEntity;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class LifeTap implements ISpellComponent, IRitualInteraction
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		if (world.getBlock(x, y, z) == Blocks.mob_spawner)
		{
			ItemStack[] reagents = RitualShapeHelper.instance.checkForRitual(this, world, x, y, z);
			if (reagents != null)
			{
				if (!world.isRemote)
				{
					    
					EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
					if (EventUtils.cantBreak(player, x, y, z))
						return false;
					    

					world.setBlockToAir(x, y, z);
					RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
					RitualShapeHelper.instance.consumeRitualShape(this, world, x, y, z);
					EntityItem item = new EntityItem(world);
					item.setPosition(x + 0.5D, y + 0.5D, z + 0.5D);
					item.setEntityItemStack(new ItemStack(BlocksCommonProxy.inertSpawner));
					world.spawnEntityInWorld(item);
				}

				return true;
			}
		}

		return false;
	}

	    
	private static final Set<EntityLivingBase> casters = Collections.newSetFromMap(new WeakHashMap<EntityLivingBase, Boolean>());
	    

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (!(target instanceof EntityLivingBase))
			return false;
		else
		{
			if (!world.isRemote)
			{
				    
				if (target.isDead || casters.contains(caster) || EventUtils.cantDamage(caster, target))
					return false;
				    

				double damage = SpellUtils.instance.getModifiedDouble_Mul(2.0D, stack, caster, target, world, 0, SpellModifiers.DAMAGE);
				ExtendedProperties casterProperties = ExtendedProperties.For(caster);
				float manaRefunded = (float) (damage * 0.01D * casterProperties.getMaxMana());

				casters.add(caster);
				try
				{
					if (!caster.attackEntityFrom(DamageSource.outOfWorld, (int) Math.floor(damage)))
						return false;
				}
				finally
				{
					casters.remove(caster);
				}
				    

				casterProperties.setCurrentMana(casterProperties.getCurrentMana() + manaRefunded);
				casterProperties.forceSync();
			}

			return true;
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 0.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 50.0F;
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
				particle.addRandomOffset(2.0D, 2.0D, 2.0D);
				particle.setMaxAge(15);
				particle.setParticleScale(0.1F);
				particle.AddParticleController(new ParticleApproachEntity(particle, target, 0.1D, 0.1D, 1, false));
				if (rand.nextBoolean())
					particle.setRGBColorF(0.4F, 0.1F, 0.5F);
				else
					particle.setRGBColorF(0.1F, 0.5F, 0.1F);

				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.LIFE, Affinity.ENDER);
	}

	@Override
	public int getID()
	{
		return 32;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 0);
		var10000[1] = BlocksCommonProxy.aum;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape()
	{
		return RitualShapeHelper.instance.corruption;
	}

	@Override
	public ItemStack[] getReagents()
	{
		ItemStack[] var10000 = new ItemStack[] { new ItemStack(ItemsCommonProxy.mobFocus), null };
		ItemEssence var10007 = ItemsCommonProxy.essence;
		var10000[1] = new ItemStack(ItemsCommonProxy.essence, 1, 9);
		return var10000;
	}

	@Override
	public int getReagentSearchRadius()
	{
		return 3;
	}
}
