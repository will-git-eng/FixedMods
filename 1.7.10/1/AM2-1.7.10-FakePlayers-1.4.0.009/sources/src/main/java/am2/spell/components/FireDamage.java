package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.RitualShapeHelper;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.power.IPowerNode;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.blocks.BlocksCommonProxy;
import am2.damage.DamageSources;
import am2.entities.EntityDarkling;
import am2.entities.EntityFireElemental;
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.power.PowerNodeRegistry;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class FireDamage implements ISpellComponent, IRitualInteraction
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		Block block = world.getBlock(x, y, z);
		if (block == BlocksCommonProxy.obelisk)
		{
			ItemStack[] reagents = RitualShapeHelper.instance.checkForRitual(this, world, x, y, z);
			if (reagents != null)
			{
				if (!world.isRemote)
				{
					    
					EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
					if (EventUtils.cantBreak(player, x, y, z))
						return false;
					    

					RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
					RitualShapeHelper.instance.consumeRitualShape(this, world, x, y, z);
					world.setBlock(x, y, z, BlocksCommonProxy.blackAurem);
					PowerNodeRegistry.For(world).registerPowerNode((IPowerNode) world.getTileEntity(x, y, z));
				}

				return true;
			}
		}

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
			    

			float baseDamage = 6.0F;
			double damage = SpellUtils.instance.getModifiedDouble_Add(baseDamage, stack, caster, target, world, 0, SpellModifiers.DAMAGE);
			return this.isNetherMob(target) ? true : SpellHelper.instance.attackTargetSpecial(stack, target, DamageSources.causeEntityFireDamage(caster), SpellUtils.instance.modifyDamage(caster, (float) damage));
		}
	}

	private boolean isNetherMob(Entity target)
	{
		return target instanceof EntityPigZombie || target instanceof EntityDarkling || target instanceof EntityFireElemental || target instanceof EntityGhast;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 120.0F;
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
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "explosion_2", x, y, z);
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
		return EnumSet.of(Affinity.FIRE);
	}

	@Override
	public int getID()
	{
		return 15;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[3];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 14);
		var10000[1] = Items.flint_and_steel;
		ItemOre var1 = ItemsCommonProxy.itemOre;
		var10000[2] = new ItemStack(ItemsCommonProxy.itemOre, 1, 0);
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
		ItemOre var10007 = ItemsCommonProxy.itemOre;
		var10000[1] = new ItemStack(ItemsCommonProxy.itemOre, 1, 6);
		return var10000;
	}

	@Override
	public int getReagentSearchRadius()
	{
		return 3;
	}
}
