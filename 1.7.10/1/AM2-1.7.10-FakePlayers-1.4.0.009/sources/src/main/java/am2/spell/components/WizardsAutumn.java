package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.spell.SpellUtils;
import am2.utility.DummyEntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class WizardsAutumn implements ISpellComponent
{
	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[] { Blocks.sapling, null, null, null };
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[1] = new ItemStack(ItemsCommonProxy.rune, 1, 6);
		var10000[2] = Items.stick;
		var10000[3] = Items.iron_ingot;
		return var10000;
	}

	@Override
	public int getID()
	{
		return 70;
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		if (!world.isRemote)
		{
			int radius = 2;
			radius = SpellUtils.instance.getModifiedInt_Add(radius, stack, caster, caster, world, 0, SpellModifiers.RADIUS);

			    
			EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
			    

			for (int i = -radius; i <= radius; ++i)
				for (int j = -radius; j <= radius; ++j)
					for (int k = -radius; k <= radius; ++k)
					{
						    
						if (EventUtils.cantBreak(player, x, y, z))
							return false;
						    

						Block block = world.getBlock(x + i, y + j, z + k);
						int meta = world.getBlockMetadata(x + i, y + j, z + k);
						if (block != null && block.isLeaves(world, x + i, y + j, z + k) && block.removedByPlayer(world, DummyEntityPlayer.fromEntityLiving(caster), x + i, y + j, z + k))
						{
							block.onBlockDestroyedByPlayer(world, x + i, y + j, z + k, meta);
							block.harvestBlock(world, DummyEntityPlayer.fromEntityLiving(caster), x + i, y + j, z + k, meta);
						}
					}
		}

		return true;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		return this.applyEffectBlock(stack, world, (int) Math.floor(target.posX), (int) Math.floor(target.posY), (int) Math.floor(target.posZ), 0, target.posX, target.posY, target.posZ, caster);
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 15.0F;
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
	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.NATURE);
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
