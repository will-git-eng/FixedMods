package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.spell.SpellUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Ignition implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		switch (face)
		{
			case 2:
				--z;
				break;
			case 3:
				++z;
				break;
			case 4:
				--x;
				break;
			case 5:
				++x;
		}

		Block block = world.getBlock(x, y, z);
		if (!world.isAirBlock(x, y, z) && block != Blocks.snow && !(block instanceof BlockFlower))
		{
			++y;
			block = world.getBlock(x, y, z);
			if (!world.isAirBlock(x, y, z) && block != Blocks.snow && !(block instanceof BlockFlower))
				return false;
			else
			{
				    
				EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
				if (EventUtils.cantBreak(player, x, y, z))
					return false;
				    

				if (!world.isRemote)
					world.setBlock(x, y, z, Blocks.fire);

				return true;
			}
		}
		else
		{
			    
			EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
			if (EventUtils.cantBreak(player, x, y, z))
				return false;
			    

			if (!world.isRemote)
				world.setBlock(x, y, z, Blocks.fire);

			return true;
		}
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		int burnTime = SpellUtils.instance.getModifiedInt_Mul(3, stack, caster, target, world, 0, SpellModifiers.DURATION);
		burnTime = SpellUtils.instance.modifyDurationBasedOnArmor(caster, burnTime);
		if (target.isBurning())
			return false;
		else
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			target.setFire(burnTime);
			return true;
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 35.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 10.0F;
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
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "explosion_2", x + 0.5D, y + 0.5D, z + 0.5D);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 0.5D, 1.0D);
				particle.addVelocity(rand.nextDouble() * 0.2D - 0.1D, 0.3D, rand.nextDouble() * 0.2D - 0.1D);
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
		return 26;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 11);
		var10000[1] = Items.flint_and_steel;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
