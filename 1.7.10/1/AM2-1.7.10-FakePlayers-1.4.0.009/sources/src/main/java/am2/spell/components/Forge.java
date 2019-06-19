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
import am2.particles.ParticleHoldPosition;
import am2.utility.EntityUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class Forge implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		if (!this.CanApplyFurnaceToBlockAtCoords(caster, world, x, y, z))
			return false;
		else
		{
			    
			EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
			if (EventUtils.cantBreak(player, x, y, z))
				return false;
			    

			this.ApplyFurnaceToBlockAtCoords(caster, world, x, y, z);
			return true;
		}
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (target instanceof EntityVillager && AMCore.config.forgeSmeltsVillagers())
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			if (!world.isRemote && !EntityUtilities.isSummon((EntityLivingBase) target))
				target.dropItem(Items.emerald, 1);

			if (caster instanceof EntityPlayer)
				target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) caster), 5000.0F);
			else
				target.attackEntityFrom(DamageSource.causeMobDamage(caster), 5000.0F);

			return true;
		}
		else
			return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 55.0F;
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
		AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "radiant", x + 0.5D, y + 0.5D, z + 0.5D);
		if (particle != null)
		{
			particle.AddParticleController(new ParticleHoldPosition(particle, 20, 1, false));
			particle.setMaxAge(20);
			particle.setParticleScale(0.3F);
			particle.setRGBColorF(0.7F, 0.4F, 0.2F);
			particle.SetParticleAlpha(0.1F);
			if (colorModifier > -1)
				particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.FIRE);
	}

	private boolean ApplyFurnaceToBlockAtCoords(EntityLivingBase entity, World world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		if (block == Blocks.air)
			return false;
		else if (block == Blocks.ice)
		{
			if (!world.isRemote)
				world.setBlock(x, y, z, Blocks.water);

			return true;
		}
		else
		{
			int meta = world.getBlockMetadata(x, y, z);
			ItemStack smelted = FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(block, 1, meta));
			if (smelted == null)
				return false;
			else
			{
				if (!world.isRemote)
					if (this.ItemIsBlock(smelted.getItem()))
						world.setBlock(x, y, z, ((ItemBlock) smelted.getItem()).field_150939_a);
					else
					{
						entity.entityDropItem(new ItemStack(smelted.getItem(), 1, smelted.getItemDamage()), 0.0F);
						world.setBlock(x, y, z, Blocks.air);
					}

				return true;
			}
		}
	}

	private boolean CanApplyFurnaceToBlockAtCoords(EntityLivingBase entity, World world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		if (block == Blocks.air)
			return false;
		else if (block == Blocks.ice)
			return true;
		else
		{
			int meta = world.getBlockMetadata(x, y, z);
			ItemStack smelted = FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(block, 1, meta));
			return smelted != null;
		}
	}

	public boolean ItemIsBlock(Item smelted)
	{
		return smelted instanceof ItemBlock;
	}

	@Override
	public int getID()
	{
		return 18;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 14);
		var10000[1] = Blocks.furnace;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
