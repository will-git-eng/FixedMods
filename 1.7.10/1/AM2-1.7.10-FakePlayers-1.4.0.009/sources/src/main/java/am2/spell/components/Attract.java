package am2.spell.components;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.math.AMVector3;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleApproachPoint;
import am2.playerextensions.ExtendedProperties;
import am2.utility.MathUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class Attract implements ISpellComponent
{
	private final HashMap arcs = new HashMap();

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		this.doTK_Extrapolated(stack, world, impactX, impactY, impactZ, caster);
		return true;
	}

	private boolean doTK_Extrapolated(ItemStack stack, World world, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		if (caster instanceof EntityPlayer)
		{
			double range = ExtendedProperties.For(caster).TK_Distance;
			MovingObjectPosition mop = ItemsCommonProxy.spell.getMovingObjectPosition(caster, world, range, false, false);
			if (mop == null)
			{
				impactX = caster.posX + Math.cos(Math.toRadians(caster.rotationYaw + 90.0F)) * range;
				impactZ = caster.posZ + Math.sin(Math.toRadians(caster.rotationYaw + 90.0F)) * range;
				impactY = caster.posY + caster.getEyeHeight() + -Math.sin(Math.toRadians(caster.rotationPitch)) * range;
			}
		}

		EntityLivingBase target = this.getClosestEntityToPointWithin(caster, world, new AMVector3(impactX, impactY, impactZ), 16.0D);
		if (target == null)
			return false;
		else
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			int hDist = 3;
			AMVector3 movement = MathUtilities.GetMovementVectorBetweenPoints(new AMVector3(target), new AMVector3(impactX, impactY, impactZ));
			if (!world.isRemote)
			{
				float factor = 0.75F;
				double x = -(movement.x * factor);
				double y = -(movement.y * factor);
				double z = -(movement.z * factor);
				target.addVelocity(x, y, z);
				if (Math.abs(target.motionX) > Math.abs(x * 2.0D))
					target.motionX = x * (target.motionX / target.motionX);

				if (Math.abs(target.motionY) > Math.abs(y * 2.0D))
					target.motionY = y * (target.motionY / target.motionY);

				if (Math.abs(target.motionZ) > Math.abs(z * 2.0D))
					target.motionZ = z * (target.motionZ / target.motionZ);
			}

			return true;
		}
	}

	private EntityLivingBase getClosestEntityToPointWithin(EntityLivingBase caster, World world, AMVector3 point, double radius)
	{
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(point.x - radius, point.y - radius, point.z - radius, point.x + radius, point.y + radius, point.z + radius);
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, bb);
		EntityLivingBase closest = null;

		for (EntityLivingBase e : entities)
			if (e != caster && (closest == null || point.distanceSqTo(new AMVector3(e)) < point.distanceSqTo(new AMVector3(closest))))
				closest = e;

		return closest;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		this.doTK_Extrapolated(stack, world, target.posX, target.posY, target.posZ, caster);
		return true;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 2.6F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		ArsMagicaApi var10000 = ArsMagicaApi.instance;
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
		AMCore var10000 = AMCore.instance;
		AMParticle effect = (AMParticle) AMCore.proxy.particleManager.spawn(world, "arcane", x, y, z);
		if (effect != null)
		{
			effect.addRandomOffset(1.0D, 1.0D, 1.0D);
			effect.AddParticleController(new ParticleApproachPoint(effect, x, y, z, 0.02500000037252903D, 0.02500000037252903D, 1, false));
			effect.setRGBColorF(0.8F, 0.3F, 0.7F);
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
		return 2;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[2];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 0);
		var10000[1] = Items.iron_ingot;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.0F;
	}
}
