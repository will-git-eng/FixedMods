package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.api.ArsMagicaApi;
import am2.api.math.AMVector3;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.spell.SpellUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

public class RandomTeleport implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		    
		if (target.isDead || EventUtils.cantDamage(caster, target))
			return false;
		    

		AMVector3 rLoc = this.getRandomTeleportLocation(world, stack, caster, target);
		return this.teleportTo(rLoc.x, rLoc.y, rLoc.z, target);
	}

	private AMVector3 getRandomTeleportLocation(World world, ItemStack stack, EntityLivingBase caster, Entity target)
	{
		AMVector3 origin = new AMVector3(target);
		float maxDist = 9.0F;
		maxDist = (float) SpellUtils.instance.getModifiedDouble_Mul(maxDist, stack, caster, target, world, 0, SpellModifiers.RANGE);
		origin.add(new AMVector3((world.rand.nextDouble() - 0.5D) * maxDist, (world.rand.nextDouble() - 0.5D) * maxDist, (world.rand.nextDouble() - 0.5D) * maxDist));
		return origin;
	}

	protected boolean teleportTo(double x, double y, double z, Entity target)
	{
		if (target instanceof EntityLivingBase)
		{
			EnderTeleportEvent event = new EnderTeleportEvent((EntityLivingBase) target, x, y, z, 0.0F);
			if (MinecraftForge.EVENT_BUS.post(event))
				return false;

			x = event.targetX;
			y = event.targetY;
			z = event.targetZ;
		}

		double xx = target.posX;
		double yy = target.posY;
		double zz = target.posZ;
		target.posX = x;
		target.posY = y;
		target.posZ = z;
		boolean locationValid = false;
		int i = MathHelper.floor_double(target.posX);
		int j = MathHelper.floor_double(target.posY);
		int k = MathHelper.floor_double(target.posZ);
		if (target.worldObj.blockExists(i, j, k))
		{
			boolean targetBlockIsSolid = false;

			while (!targetBlockIsSolid && j > 0)
			{
				Block l = target.worldObj.getBlock(i, j - 1, k);
				if (l != Blocks.air && l.getMaterial().blocksMovement())
					targetBlockIsSolid = true;
				else
				{
					--target.posY;
					--j;
				}
			}

			    
			EntityPlayer player = target instanceof EntityPlayer ? (EntityPlayer) target : ModUtils.getModFake(target.worldObj);
			if (!EventUtils.cantBreak(player, x, y, z))
				    
				if (targetBlockIsSolid)
				{
					target.setPosition(target.posX, target.posY, target.posZ);
					if (target.worldObj.getCollidingBoundingBoxes(target, target.boundingBox).isEmpty())
						locationValid = true;
				}
		}

		if (!locationValid)
		{
			target.setPosition(xx, yy, zz);
			return false;
		}
		else
			return true;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 52.5F;
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
		world.spawnParticle("portal", target.posX + (rand.nextDouble() - 0.5D) * target.width, target.posY + rand.nextDouble() * target.height - 0.25D, target.posZ + (rand.nextDouble() - 0.5D) * target.width, (rand.nextDouble() - 0.5D) * 2.0D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2.0D);
	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.ENDER);
	}

	@Override
	public int getID()
	{
		return 43;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 0);
		var10000[1] = Items.ender_pearl;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
