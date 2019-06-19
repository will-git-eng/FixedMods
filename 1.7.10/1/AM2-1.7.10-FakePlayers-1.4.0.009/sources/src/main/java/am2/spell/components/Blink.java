package am2.spell.components;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.blocks.tileentities.TileEntityAstralBarrier;
import am2.buffs.BuffList;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleMoveOnHeading;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SpellUtils;
import am2.utility.DimensionUtilities;
import am2.utility.KeystoneUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class Blink implements ISpellComponent
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
		else
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			if (world.isRemote)
				ExtendedProperties.For((EntityLivingBase) target).astralBarrierBlocked = false;

			double distance = this.GetTeleportDistance(stack, caster, target);
			double motionX = -MathHelper.sin(target.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(target.rotationPitch / 180.0F * 3.141593F) * distance;
			double motionZ = MathHelper.cos(target.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(target.rotationPitch / 180.0F * 3.141593F) * distance;
			double motionY = -MathHelper.sin(target.rotationPitch / 180.0F * 3.141593F) * distance;
			float f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
			double d = motionX / f2;
			double d1 = motionY / f2;
			double d2 = motionZ / f2;
			d = d * distance;
			d1 = d1 * distance;
			d2 = d2 * distance;
			MathHelper.sqrt_double(d * d + d2 * d2);
			ArrayList<Long> keystoneKeys = KeystoneUtilities.instance.GetKeysInInvenory((EntityLivingBase) target);
			double newX = target.posX + d;
			double newZ = target.posZ + d2;
			double newY = target.posY + d1;
			boolean coordsValid = false;
			boolean astralBarrierBlocked = false;

			TileEntityAstralBarrier finalBlocker;
			for (finalBlocker = null; !coordsValid && distance > 0.0D; newY = target.posY + d1)
			{
				if (caster.isPotionActive(BuffList.astralDistortion.id))
				{
					coordsValid = true;
					newX = caster.posX;
					newY = caster.posY;
					newZ = caster.posZ;
				}

				for (TileEntityAstralBarrier blocker = DimensionUtilities.GetBlockingAstralBarrier(world, (int) newX, (int) newY, (int) newZ, keystoneKeys); blocker != null; blocker = DimensionUtilities.GetBlockingAstralBarrier(world, (int) newX, (int) newY, (int) newZ, keystoneKeys))
				{
					finalBlocker = blocker;
					astralBarrierBlocked = true;
					int dx = (int) newX - blocker.xCoord;
					int dy = (int) newY - blocker.yCoord;
					int dz = (int) newZ - blocker.zCoord;
					int sqDist = dx * dx + dy * dy + dz * dz;
					int delta = blocker.getRadius() - (int) Math.floor(Math.sqrt(sqDist));
					distance -= delta;
					if (distance < 0.0D)
						break;

					motionX = -MathHelper.sin(target.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(target.rotationPitch / 180.0F * 3.141593F) * distance;
					motionZ = MathHelper.cos(target.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(target.rotationPitch / 180.0F * 3.141593F) * distance;
					motionY = -MathHelper.sin(target.rotationPitch / 180.0F * 3.141593F) * distance;
					f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
					d = motionX / f2;
					d1 = motionY / f2;
					d2 = motionZ / f2;
					d = d * distance;
					d1 = d1 * distance;
					d2 = d2 * distance;
					MathHelper.sqrt_double(d * d + d2 * d2);
					newX = target.posX + d;
					newZ = target.posZ + d2;
					newY = target.posY + d1;
				}

				if (distance < 0.0D)
				{
					coordsValid = false;
					break;
				}

				if (this.CheckCoords(world, (int) Math.floor(newX), (int) newY, (int) Math.floor(newZ)))
				{
					newX = Math.floor(newX) + 0.5D;
					newZ = Math.floor(newZ) + 0.5D;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.floor(newX), (int) newY, (int) Math.ceil(newZ)))
				{
					newX = Math.floor(newX) + 0.5D;
					newZ = Math.ceil(newZ) + 0.5D;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.ceil(newX), (int) newY, (int) Math.floor(newZ)))
				{
					newX = Math.ceil(newX) + 0.5D;
					newZ = Math.floor(newZ) + 0.5D;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.ceil(newX), (int) newY, (int) Math.ceil(newZ)))
				{
					newX = Math.ceil(newX) + 0.5D;
					newZ = Math.ceil(newZ) + 0.5D;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.floor(newX), (int) newY - 1, (int) Math.floor(newZ)))
				{
					newX = Math.floor(newX) + 0.5D;
					newZ = Math.floor(newZ) + 0.5D;
					--newY;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.floor(newX), (int) newY - 1, (int) Math.ceil(newZ)))
				{
					newX = Math.floor(newX) + 0.5D;
					newZ = Math.ceil(newZ) + 0.5D;
					--newY;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.ceil(newX), (int) newY - 1, (int) Math.floor(newZ)))
				{
					newX = Math.ceil(newX) + 0.5D;
					newZ = Math.floor(newZ) + 0.5D;
					--newY;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.ceil(newX), (int) newY - 1, (int) Math.ceil(newZ)))
				{
					newX = Math.ceil(newX) + 0.5D;
					newZ = Math.ceil(newZ) + 0.5D;
					--newY;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.floor(newX), (int) newY + 1, (int) Math.floor(newZ)))
				{
					newX = Math.floor(newX) + 0.5D;
					newZ = Math.floor(newZ) + 0.5D;
					++newY;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.floor(newX), (int) newY + 1, (int) Math.ceil(newZ)))
				{
					newX = Math.floor(newX) + 0.5D;
					newZ = Math.ceil(newZ) + 0.5D;
					++newY;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.ceil(newX), (int) newY + 1, (int) Math.floor(newZ)))
				{
					newX = Math.ceil(newX) + 0.5D;
					newZ = Math.floor(newZ) + 0.5D;
					++newY;
					coordsValid = true;
					break;
				}

				if (this.CheckCoords(world, (int) Math.ceil(newX), (int) newY + 1, (int) Math.ceil(newZ)))
				{
					newX = Math.ceil(newX) + 0.5D;
					newZ = Math.ceil(newZ) + 0.5D;
					++newY;
					coordsValid = true;
					break;
				}

				--distance;
				motionX = -MathHelper.sin(target.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(target.rotationPitch / 180.0F * 3.141593F) * distance;
				motionZ = MathHelper.cos(target.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(target.rotationPitch / 180.0F * 3.141593F) * distance;
				motionY = -MathHelper.sin(target.rotationPitch / 180.0F * 3.141593F) * distance;
				f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
				d = motionX / f2;
				d1 = motionY / f2;
				d2 = motionZ / f2;
				d = d * distance;
				d1 = d1 * distance;
				d2 = d2 * distance;
				MathHelper.sqrt_double(d * d + d2 * d2);
				newX = target.posX + d;
				newZ = target.posZ + d2;
			}

			if (world.isRemote && astralBarrierBlocked && coordsValid)
			{
				ExtendedProperties.For((EntityLivingBase) target).astralBarrierBlocked = true;
				if (finalBlocker != null)
					finalBlocker.onEntityBlocked((EntityLivingBase) target);
			}

			if (!world.isRemote && !coordsValid && target instanceof EntityPlayer)
			{
				((EntityPlayer) target).addChatMessage(new ChatComponentText("Can\'t find a place to blink forward to."));
				return false;
			}
			else
			{
				    
				EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
				if (EventUtils.cantBreak(player, newX, newY, newZ))
					return false;
				    

				if (!world.isRemote)
					((EntityLivingBase) target).setPositionAndUpdate(newX, newY, newZ);

				return true;
			}
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 160.0F;
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
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "sparkle", x, y, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 2.0D, 1.0D);
				particle.AddParticleController(new ParticleMoveOnHeading(particle, MathHelper.wrapAngleTo180_double((target instanceof EntityLivingBase ? ((EntityLivingBase) target).rotationYawHead : target.rotationYaw) + 90.0F), MathHelper.wrapAngleTo180_double(target.rotationPitch), 0.1D + rand.nextDouble() * 0.5D, 1, false));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05F));
				particle.setMaxAge(20);
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

	private boolean CheckCoords(World world, int x, int y, int z)
	{
		if (y < 0)
			return false;
		else
		{
			Block firstBlock = world.getBlock(x, y, z);
			Block secondBlock = world.getBlock(x, y + 1, z);
			AxisAlignedBB firstBlockBB = null;
			AxisAlignedBB secondBlockBB = null;
			if (firstBlock != null)
				firstBlockBB = firstBlock.getCollisionBoundingBoxFromPool(world, x, y, z);

			if (secondBlock != null)
				secondBlockBB = secondBlock.getCollisionBoundingBoxFromPool(world, x, y, z);

			return firstBlockBB == null && secondBlockBB == null;
		}
	}

	protected double GetTeleportDistance(ItemStack stack, EntityLivingBase caster, Entity target)
	{
		double distance = 12.0D;
		return SpellUtils.instance.getModifiedDouble_Add(distance, stack, caster, target, caster.worldObj, 0, SpellModifiers.RANGE);
	}

	@Override
	public int getID()
	{
		return 5;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 13);
		var10000[1] = Items.ender_pearl;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.05F;
	}
}
