package am2.spell.components;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.RitualShapeHelper;
import am2.api.ArsMagicaApi;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.math.AMVector3;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.buffs.BuffList;
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleExpandingCollapsingRingAtPoint;
import am2.playerextensions.ExtendedProperties;
import am2.utility.DimensionUtilities;
import am2.utility.EntityUtilities;
import am2.utility.KeystoneUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class Recall implements ISpellComponent, IRitualInteraction
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (!(target instanceof EntityLivingBase))
			return false;
		else if (!caster.isPotionActive(BuffList.astralDistortion.id) && !((EntityLivingBase) target).isPotionActive(BuffList.astralDistortion.id))
		{
			    
			if (target.isDead)
				return false;
			if (caster != target && target instanceof EntityPlayer || EventUtils.cantDamage(caster, target))
				return false;
			    

			int x = (int) Math.floor(target.posX);
			int y = (int) Math.floor(target.posY);
			int z = (int) Math.floor(target.posZ);
			ItemStack[] ritualRunes = RitualShapeHelper.instance.checkForRitual(this, world, x, y, z, false);
			if (ritualRunes != null)
				return this.handleRitualReagents(ritualRunes, world, x, y, z, caster, target);
			else
			{
				ExtendedProperties prop = ExtendedProperties.For(caster);
				if (!prop.getMarkSet())
				{
					if (caster instanceof EntityPlayer && !world.isRemote)
						((EntityPlayer) caster).addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.noMark")));

					return false;
				}
				else if (prop.getMarkDimension() != caster.dimension)
				{
					if (caster instanceof EntityPlayer && !world.isRemote)
						((EntityPlayer) caster).addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.diffDimMark")));

					return false;
				}
				else
				{
					    
					EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
					if (EventUtils.cantBreak(player, prop.getMarkX(), prop.getMarkY(), prop.getMarkZ()))
						return false;
					    

					if (!world.isRemote)
						((EntityLivingBase) target).setPositionAndUpdate(prop.getMarkX(), prop.getMarkY(), prop.getMarkZ());

					return true;
				}
			}
		}
		else
		{
			if (caster instanceof EntityPlayer)
				((EntityPlayer) caster).addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.cantTeleport")));

			return false;
		}
	}

	private boolean handleRitualReagents(ItemStack[] ritualRunes, World world, int x, int y, int z, EntityLivingBase caster, Entity target)
	{
		boolean hasVinteumDust = false;

		for (ItemStack stack : ritualRunes)
			if (stack.getItem() == ItemsCommonProxy.itemOre)
			{
				int var10000 = stack.getItemDamage();
				ItemOre var10001 = ItemsCommonProxy.itemOre;
				if (var10000 == 0)
				{
					hasVinteumDust = true;
					break;
				}
			}

		if (!hasVinteumDust && ritualRunes.length == 3)
		{
			long key = KeystoneUtilities.instance.getKeyFromRunes(ritualRunes);
			AMVector3 vector = AMCore.proxy.blocks.getNextKeystonePortalLocation(world, x, y, z, false, key);
			if (vector != null && !vector.equals(new AMVector3(x, y, z)))
			{
				    
				EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
				if (EventUtils.cantBreak(player, vector.x, vector.y - target.height, vector.z))
					return false;
				    

				RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
				RitualShapeHelper.instance.consumeRitualShape(this, world, x, y, z);
				((EntityLivingBase) target).setPositionAndUpdate(vector.x, vector.y - target.height, vector.z);
				return true;
			}
			else
			{
				if (caster instanceof EntityPlayer && !world.isRemote)
					((EntityPlayer) caster).addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.noMatchingGate")));

				return false;
			}
		}
		else if (hasVinteumDust)
		{
			ArrayList<ItemStack> copy = new ArrayList();

			for (ItemStack stack : ritualRunes)
				if (stack.getItem() == ItemsCommonProxy.rune && stack.getItemDamage() <= 16)
					copy.add(stack);

			ItemStack[] newRunes = copy.toArray(new ItemStack[copy.size()]);
			long key = KeystoneUtilities.instance.getKeyFromRunes(newRunes);
			EntityPlayer player = EntityUtilities.getPlayerForCombo(world, (int) key);
			if (player == null)
			{
				if (caster instanceof EntityPlayer && !world.isRemote)
					((EntityPlayer) caster).addChatMessage(new ChatComponentText("am2.tooltip.noMatchingPlayer"));

				return false;
			}
			else if (player == caster)
			{
				if (caster instanceof EntityPlayer && !world.isRemote)
					((EntityPlayer) caster).addChatMessage(new ChatComponentText("am2.tooltip.cantSummonSelf"));

				return false;
			}
			else
			{
				    
				EntityPlayer p = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
				if (EventUtils.cantBreak(p, x, y, z))
					return false;
				    

				RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
				if (target.worldObj.provider.dimensionId != caster.worldObj.provider.dimensionId)
					DimensionUtilities.doDimensionTransfer(player, caster.worldObj.provider.dimensionId);

				((EntityLivingBase) target).setPositionAndUpdate(x, y, z);
				return true;
			}
		}
		else
			return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 500.0F;
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
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "arcane", x, y - 1.0D, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 0.0D, 1.0D);
				particle.AddParticleController(new ParticleExpandingCollapsingRingAtPoint(particle, x, y - 1.0D, z, 0.1D, 3.0D, 0.3D, 1, false).setCollapseOnce());
				particle.setMaxAge(20);
				particle.setParticleScale(0.2F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.ARCANE);
	}

	@Override
	public int getID()
	{
		return 44;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[4];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 11);
		var10000[1] = Items.compass;
		var10000[2] = new ItemStack(Items.map, 1, 32767);
		var10000[3] = Items.ender_pearl;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.1F;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape()
	{
		return RitualShapeHelper.instance.ringedCross;
	}

	@Override
	public ItemStack[] getReagents()
	{
		return new ItemStack[] { new ItemStack(ItemsCommonProxy.rune, 1, 32767), new ItemStack(ItemsCommonProxy.rune, 1, 32767), new ItemStack(ItemsCommonProxy.rune, 1, 32767) };
	}

	@Override
	public int getReagentSearchRadius()
	{
		return RitualShapeHelper.instance.ringedCross.getWidth();
	}
}
