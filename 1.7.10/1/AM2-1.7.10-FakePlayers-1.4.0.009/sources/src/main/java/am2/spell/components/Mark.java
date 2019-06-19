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
import am2.particles.ParticleConverge;
import am2.playerextensions.ExtendedProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Mark implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		    
		EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
		if (EventUtils.cantBreak(player, impactX, impactY, impactZ))
			return false;
		    

		ExtendedProperties.For(caster).setMarkLocation(impactX, impactY, impactZ, caster.worldObj.provider.dimensionId);
		return true;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (!(target instanceof EntityLivingBase))
			return false;
		else
		{
			    
			EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
			if (EventUtils.cantBreak(player, target.posX, target.posY, target.posZ))
				return false;
			    

			ExtendedProperties.For(caster).setMarkLocation(target.posX, target.posY, target.posZ, caster.worldObj.provider.dimensionId);
			return true;
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 5.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 0.0F;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster)
	{
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier)
	{
		int offset = 1;
		this.SetupParticle(world, caster.posX - 0.5D, caster.posY + offset, caster.posZ, 0.2D, 0.0D, colorModifier);
		this.SetupParticle(world, caster.posX + 0.5D, caster.posY + offset, caster.posZ, -0.2D, 0.0D, colorModifier);
		this.SetupParticle(world, caster.posX, caster.posY + offset, caster.posZ - 0.5D, 0.0D, 0.2D, colorModifier);
		this.SetupParticle(world, caster.posX, caster.posY + offset, caster.posZ + 0.5D, 0.0D, -0.2D, colorModifier);
	}

	private void SetupParticle(World world, double x, double y, double z, double motionx, double motionz, int colorModifier)
	{
		AMParticle effect = (AMParticle) AMCore.proxy.particleManager.spawn(world, "symbols", x, y, z);
		if (effect != null)
		{
			effect.AddParticleController(new ParticleConverge(effect, motionx, -0.1D, motionz, 1, true));
			effect.setMaxAge(40);
			effect.setIgnoreMaxAge(false);
			effect.setParticleScale(0.1F);
			if (colorModifier > -1)
				effect.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.NONE);
	}

	@Override
	public int getID()
	{
		return 37;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 14);
		var10000[1] = new ItemStack(Items.map, 1, 32767);
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.0F;
	}
}
