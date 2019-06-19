package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.buffs.BuffList;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class EnderIntervention implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (!world.isRemote && target instanceof EntityLivingBase)
		{
			if (((EntityLivingBase) target).isPotionActive(BuffList.astralDistortion.id))
			{
				if (target instanceof EntityPlayer)
					((EntityPlayer) target).addChatMessage(new ChatComponentText("The distortion around you prevents you from teleporting"));

				return true;
			}
			else if (target.dimension == 1)
			{
				if (target instanceof EntityPlayer)
					((EntityPlayer) target).addChatMessage(new ChatComponentText("Nothing happens..."));

				return true;
			}
			else if (target.dimension == -1)
			{
				if (target instanceof EntityPlayer)
					((EntityPlayer) target).addChatMessage(new ChatComponentText("You are already in the nether."));

				return false;
			}
			else
			{
				    
				if (target.isDead || EventUtils.cantDamage(caster, target))
					return false;
				    

				AMCore.proxy.addDeferredDimensionTransfer((EntityLivingBase) target, -1);
				return true;
			}
		}
		else
			return true;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 400.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return ArsMagicaApi.getBurnoutFromMana(this.manaCost(caster));
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster)
	{
		return new ItemStack[] { new ItemStack(ItemsCommonProxy.essence, 1, 9) };
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier)
	{
		for (int i = 0; i < 100; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "ghost", x, y - 1.0D, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 2.0D, 1.0D);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0.0F, 0.1F, 1, false));
				particle.setMaxAge(25 + rand.nextInt(10));
				particle.setRGBColorF(0.7F, 0.2F, 0.2F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.ENDER);
	}

	@Override
	public int getID()
	{
		return 13;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[5];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 13);
		var10000[1] = Blocks.obsidian;
		var10000[2] = Blocks.obsidian;
		var10000[3] = Items.flint_and_steel;
		var10000[4] = Items.ender_pearl;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.4F;
	}
}
