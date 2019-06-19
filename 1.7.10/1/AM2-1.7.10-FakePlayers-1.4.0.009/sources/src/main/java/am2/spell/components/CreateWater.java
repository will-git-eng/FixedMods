package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CreateWater implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		Block block = world.getBlock(x, y, z);
		if (block == Blocks.cauldron)
		{
			world.setBlockMetadataWithNotify(x, y, z, 3, 2);
			world.notifyBlockChange(x, y, z, block);
			return true;
		}
		else
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
			if (!world.isAirBlock(x, y, z) && block != Blocks.snow && block != Blocks.water && block != Blocks.flowing_water && !(block instanceof BlockFlower))
				return false;
			else
			{
				    
				EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
				if (EventUtils.cantBreak(player, x, y, z))
					return false;
				    

				world.setBlock(x, y, z, Blocks.water);
				Blocks.water.onNeighborBlockChange(world, x, y, z, Blocks.air);
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
		return 25.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 30.0F;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster)
	{
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier)
	{
		for (int i = 0; i < 15; ++i)
			world.spawnParticle("splash", x - 0.5D + rand.nextDouble(), y, z - 0.5D + rand.nextDouble(), 0.5D - rand.nextDouble(), 0.1D, 0.5D - rand.nextDouble());

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.WATER);
	}

	@Override
	public int getID()
	{
		return 7;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 2);
		var10000[1] = Items.water_bucket;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.001F;
	}
}
