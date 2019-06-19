package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Drought implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		    
		EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
		if (EventUtils.cantBreak(player, x, y, z))
			return false;
		    

		Block block = world.getBlock(x, y, z);
		if (!(block instanceof BlockFlower) && !(block instanceof BlockTallGrass))
		{
			if (block != Blocks.grass && block != Blocks.mycelium && block != Blocks.sandstone && block != Blocks.dirt)
			{
				if (block == Blocks.stone)
				{
					world.setBlock(x, y, z, Blocks.cobblestone);
					return true;
				}
				else if (block == Blocks.stonebrick && world.getBlockMetadata(x, y, z) != 2)
				{
					world.setBlockMetadataWithNotify(x, y, z, 2, 2);
					return true;
				}
				else if (block != Blocks.water && block != Blocks.flowing_water)
				{
					switch (face)
					{
						case 0:
							--y;
							break;
						case 1:
							++y;
							break;
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

					block = world.getBlock(x, y, z);
					if (block != Blocks.water && block != Blocks.flowing_water)
						return false;
					else
					{
						    
						if (EventUtils.cantBreak(player, x, y, z))
							return false;
						    

						world.setBlock(x, y, z, Blocks.air);
						return true;
					}
				}
				else
				{
					world.setBlock(x, y, z, Blocks.air);
					return true;
				}
			}
			else
			{
				world.setBlock(x, y, z, Blocks.sand);
				return true;
			}
		}
		else
		{
			world.setBlock(x, y, z, Blocks.tallgrass, 0, 2);
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
		return 60.0F;
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
		for (int i = 0; i < 25; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "ember", x + 0.5D, y + 1.0D, z + 0.5D);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 0.0D, 1.0D);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0.0F, 0.1F, 1, false));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05F));
				particle.setAffectedByGravity();
				particle.setRGBColorF(0.9F, 0.8F, 0.5F);
				particle.setMaxAge(40);
				particle.setParticleScale(0.1F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.FIRE, Affinity.AIR);
	}

	@Override
	public int getID()
	{
		return 12;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[3];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 11);
		var10000[1] = Blocks.sand;
		var10000[2] = Blocks.deadbush;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return affinity == Affinity.FIRE ? 0.008F : 0.004F;
	}
}
