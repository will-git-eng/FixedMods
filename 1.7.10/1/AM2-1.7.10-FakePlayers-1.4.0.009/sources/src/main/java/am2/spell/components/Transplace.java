package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.RitualShapeHelper;
import am2.api.ArsMagicaApi;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.blocks.BlocksCommonProxy;
import am2.blocks.tileentities.TileEntityOtherworldAura;
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleArcToPoint;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class Transplace implements ISpellComponent, IRitualInteraction
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		Block block = world.getBlock(x, y, z);
		if (!world.isRemote && caster instanceof EntityPlayer && block == BlocksCommonProxy.inertSpawner)
		{
			ItemStack[] items = RitualShapeHelper.instance.checkForRitual(this, world, x, y, z);
			if (items != null)
			{
				    
				if (EventUtils.cantBreak((EntityPlayer) caster, x, y, z))
					return false;
				    

				RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
				RitualShapeHelper.instance.consumeRitualShape(this, world, x, y, z);
				world.setBlock(x, y, z, BlocksCommonProxy.otherworldAura);
				TileEntity te = world.getTileEntity(x, y, z);
				if (te != null && te instanceof TileEntityOtherworldAura)
					((TileEntityOtherworldAura) te).setPlacedByUsername(((EntityPlayer) caster).getCommandSenderName());

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		    
		if (target.isDead || EventUtils.cantDamage(caster, target))
			return false;
		    

		if (!world.isRemote && target != null && !target.isDead)
		{
			double tPosX = target.posX;
			double tPosY = target.posY;
			double tPosZ = target.posZ;
			double cPosX = caster.posX;
			double cPosY = caster.posY;
			double cPosZ = caster.posZ;
			caster.setPositionAndUpdate(tPosX, tPosY, tPosZ);
			if (target instanceof EntityLiving)
				((EntityLiving) target).setPositionAndUpdate(cPosX, cPosY, cPosZ);
			else
				target.setPosition(cPosX, cPosY, cPosZ);
		}

		if (target instanceof EntityLiving)
			((EntityLiving) target).faceEntity(caster, 180.0F, 180.0F);

		return true;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 100.0F;
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
		for (int i = 0; i < 15; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "sparkle2", caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 1.0D, 1.0D);
				particle.AddParticleController(new ParticleArcToPoint(particle, 1, target.posX, target.posY + target.getEyeHeight(), target.posZ, false).SetSpeed(0.05F).generateControlPoints());
				particle.setMaxAge(40);
				particle.setParticleScale(0.2F);
				particle.setRGBColorF(1.0F, 0.0F, 0.0F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

		for (int i = 0; i < 15; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "sparkle2", target.posX, target.posY + target.getEyeHeight(), target.posZ);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 1.0D, 1.0D);
				particle.AddParticleController(new ParticleArcToPoint(particle, 1, caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ, false).SetSpeed(0.05F).generateControlPoints());
				particle.setMaxAge(40);
				particle.setParticleScale(0.2F);
				particle.setRGBColorF(0.0F, 0.0F, 1.0F);
				if (colorModifier > -1)
					particle.setRGBColorF((255 - (colorModifier >> 16 & 255)) / 255.0F, (255 - (colorModifier >> 8 & 255)) / 255.0F, (255 - (colorModifier & 255)) / 255.0F);
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
		return 55;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[4];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 14);
		var10000[1] = Items.compass;
		var10007 = ItemsCommonProxy.rune;
		var10000[2] = new ItemStack(ItemsCommonProxy.rune, 1, 2);
		var10000[3] = Items.ender_pearl;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.02F;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape()
	{
		return RitualShapeHelper.instance.ringedCross;
	}

	@Override
	public ItemStack[] getReagents()
	{
		ItemStack[] var10000 = new ItemStack[6];
		ItemOre var10007 = ItemsCommonProxy.itemOre;
		var10000[0] = new ItemStack(ItemsCommonProxy.itemOre, 1, 3);
		var10000[1] = new ItemStack(ItemsCommonProxy.mageArmor);
		var10000[2] = new ItemStack(ItemsCommonProxy.mageBoots);
		var10000[3] = new ItemStack(ItemsCommonProxy.mageHood);
		var10000[4] = new ItemStack(ItemsCommonProxy.mageLeggings);
		var10000[5] = new ItemStack(ItemsCommonProxy.playerFocus);
		return var10000;
	}

	@Override
	public int getReagentSearchRadius()
	{
		return 3;
	}
}
