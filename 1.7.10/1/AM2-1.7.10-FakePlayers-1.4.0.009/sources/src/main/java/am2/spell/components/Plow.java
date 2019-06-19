package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Plow implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		Block block = world.getBlock(x, y, z);
		world.getBlock(x, y + 1, z);
		if (block != Blocks.dirt && block != Blocks.grass)
			return false;
		else
		{
			    
			EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
			if (EventUtils.cantBreak(player, x, y, z))
				return false;
			    

			if (!world.isRemote)
				world.setBlock(x, y, z, Blocks.farmland);

			return true;
		}
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 75.0F;
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
		for (int i = 0; i < 10; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "rock", x, y + 1.0D, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 1.0D, 1.0D);
				particle.addVelocity(rand.nextDouble() * 0.2D - 0.1D, 0.20000000298023224D, rand.nextDouble() * 0.2D - 0.1D);
				particle.setDontRequireControllers();
				particle.setAffectedByGravity();
				particle.setMaxAge(20);
				particle.setParticleScale(0.05F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.EARTH);
	}

	@Override
	public int getID()
	{
		return 42;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 6);
		var10000[1] = Items.stone_hoe;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
