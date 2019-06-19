package thaumcraft.common.items.casters.foci;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXFocusPartImpact;

public class FocusEffectAir extends FocusEffect
{
	@Override
	public String getResearch()
	{
		return "FOCUSELEMENTAL";
	}

	@Override
	public String getKey()
	{
		return "thaumcraft.AIR";
	}

	@Override
	public Aspect getAspect()
	{
		return Aspect.AIR;
	}

	@Override
	public int getComplexity()
	{
		return this.getSettingValue("power") * 2;
	}

	@Override
	public float getDamageForDisplay(float finalPower)
	{
		return (float) (1 + this.getSettingValue("power")) * finalPower;
	}

	@Override
	public boolean execute(RayTraceResult target, Trajectory trajectory, float finalPower, int num)
	{
		FocusPackage focusPackage = this.getPackage();
		PacketHandler.INSTANCE.sendToAllAround(new PacketFXFocusPartImpact(target.hitVec.x, target.hitVec.y, target.hitVec.z, new String[] { this.getKey() }), new TargetPoint(focusPackage.world.provider.getDimension(), target.hitVec.x, target.hitVec.y, target.hitVec.z, 64.0D));
		focusPackage.world.playSound(null, target.hitVec.x, target.hitVec.y, target.hitVec.z, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.5F, 0.66F);
		if (target.typeOfHit == Type.ENTITY && target.entityHit != null)
		{
			float damage = this.getDamageForDisplay(finalPower);
			EntityLivingBase caster = focusPackage.getCaster();
			target.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(target.entityHit != null ? target.entityHit : caster, caster), damage);
			if (target.entityHit instanceof EntityLivingBase)
			{
				
				FakePlayerContainer fake = focusPackage.getFake();
				if (fake.cantAttack(target.entityHit))
					return false;
				

				if (trajectory != null)
					((EntityLivingBase) target.entityHit).knockBack(caster, damage * 0.25F, -trajectory.direction.x, -trajectory.direction.z);
				else
					((EntityLivingBase) target.entityHit).knockBack(caster, damage * 0.25F, (double) -MathHelper.sin(target.entityHit.rotationYaw * 0.017453292F), (double) MathHelper.cos(target.entityHit.rotationYaw * 0.017453292F));
			}

			return true;
		}
		return false;
	}

	@Override
	public NodeSetting[] createSettings()
	{
		return new NodeSetting[] { new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5)) };
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderParticleFX(World world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ)
	{
		FXDispatcher.GenPart pp = new FXDispatcher.GenPart();
		pp.grav = -0.1F;
		pp.age = 20 + world.rand.nextInt(10);
		pp.alpha = new float[] { 0.5F, 0.0F };
		pp.grid = 32;
		pp.partStart = 337;
		pp.partInc = 1;
		pp.partNum = 5;
		pp.slowDown = 0.75D;
		pp.rot = (float) world.rand.nextGaussian() / 2.0F;
		float s = (float) (2.0D + world.rand.nextGaussian() * 0.5D);
		pp.scale = new float[] { s, s * 2.0F };
		FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, pp);
	}

	@Override
	public void onCast(Entity caster)
	{
		caster.world.playSound(null, caster.getPosition().up(), SoundsTC.wind, SoundCategory.PLAYERS, 0.125F, 2.0F);
	}
}
