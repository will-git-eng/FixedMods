package am2.spell.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.blocks.AMFlower;
import am2.blocks.BlocksCommonProxy;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleOrbitPoint;
import am2.utility.DummyEntityPlayer;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;

public class Grow implements ISpellComponent
{
	private static final ArrayList<AMFlower> growableAMflowers = new ArrayList(Arrays.asList(new AMFlower[] { BlocksCommonProxy.cerublossom, BlocksCommonProxy.desertNova, BlocksCommonProxy.wakebloom, BlocksCommonProxy.aum, BlocksCommonProxy.tarmaRoot }));

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		    
		EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
		if (EventUtils.cantBreak(player, x, y, z))
			return false;
		    

		Block block = world.getBlock(x, y, z);
		BonemealEvent event = new BonemealEvent(DummyEntityPlayer.fromEntityLiving(caster), world, block, x, y, z);
		if (MinecraftForge.EVENT_BUS.post(event))
			return false;
		else if (event.getResult() == Result.ALLOW)
			return true;
		else
		{
			if (world.rand.nextInt(100) < 3 && block.isNormalCube() && (world.getBlock(x, y + 1, z).isAir((IBlockAccess) null, 0, 0, 0) || world.getBlock(x, y + 1, z) == Blocks.tallgrass))
			{
				Collections.shuffle(growableAMflowers);

				for (AMFlower flower : growableAMflowers)
					if (flower.canGrowOn(world, x, y + 1, z))
					{
						if (!world.isRemote)
							world.setBlock(x, y + 1, z, flower, 0, 2);

						return true;
					}
			}

			if (block instanceof BlockMushroom)
			{
				if (!world.isRemote && world.rand.nextInt(10) < 1)
					((BlockMushroom) block).func_149884_c(world, x, y, z, world.rand);

				return true;
			}
			else if (block == Blocks.water && world.getBlock(x, y + 1, z) == Blocks.air)
			{
				if (!world.isRemote && world.rand.nextInt(100) < 3)
					world.setBlock(x, y + 1, z, BlocksCommonProxy.wakebloom);

				return true;
			}
			else if (block == Blocks.tallgrass && Blocks.tallgrass.canBlockStay(world, x, y + 1, z))
			{
				if (!world.isRemote && world.rand.nextInt(10) < 2)
					world.setBlock(x, y, z, Blocks.tallgrass, 1, 2);

				return true;
			}
			else if (block == Blocks.deadbush && Blocks.tallgrass.canBlockStay(world, x, y, z))
			{
				if (!world.isRemote && world.rand.nextInt(10) < 2)
					world.setBlock(x, y, z, Blocks.tallgrass, 1, 2);

				return true;
			}
			else
			{
				if (block instanceof IGrowable)
				{
					IGrowable igrowable = (IGrowable) block;
					if (igrowable.func_149851_a(world, x, y, z, world.isRemote))
					{
						if (!world.isRemote && world.rand.nextInt(10) < 3 && igrowable.func_149852_a(world, world.rand, x, y, z))
							igrowable.func_149853_b(world, world.rand, x, y, z);

						return true;
					}
				}

				return true;
			}
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
		return 17.4F;
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
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "plant", x + 0.5D, y + 1.0D, z + 0.5D);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 1.0D, 1.0D);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0.0F, 0.1F, 1, false));
				particle.AddParticleController(new ParticleOrbitPoint(particle, x + 0.5D, y + 0.5D, z + 0.5D, 2, false).setIgnoreYCoordinate(true).SetOrbitSpeed(0.10000000149011612D).SetTargetDistance(0.30000001192092896D + rand.nextDouble() * 0.3D));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05F).setKillParticleOnFinish(true));
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
		return 22;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[3];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 6);
		var10000[1] = new ItemStack(Items.dye, 1, 15);
		var10000[2] = BlocksCommonProxy.witchwoodLog;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.02F;
	}
}
