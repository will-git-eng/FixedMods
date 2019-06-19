package thaumcraft.common.items.casters.foci;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
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
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXFocusPartImpact;

public class FocusEffectFlux extends FocusEffect
{
	@Override
	public String getResearch()
	{
		return "FOCUSFLUX";
	}

	@Override
	public String getKey()
	{
		return "thaumcraft.FLUX";
	}

	@Override
	public Aspect getAspect()
	{
		return Aspect.FLUX;
	}

	@Override
	public int getComplexity()
	{
		return this.getSettingValue("power") * 3;
	}

	@Override
	public float getDamageForDisplay(float finalPower)
	{
		return (float) (3 + this.getSettingValue("power")) * finalPower;
	}

	@Override
	public boolean execute(RayTraceResult target, Trajectory trajectory, float finalPower, int num)
	{
		FocusPackage focusPackage = this.getPackage();
		PacketHandler.INSTANCE.sendToAllAround(new PacketFXFocusPartImpact(target.hitVec.x, target.hitVec.y, target.hitVec.z, new String[] { this.getKey() }), new TargetPoint(focusPackage.world.provider.getDimension(), target.hitVec.x, target.hitVec.y, target.hitVec.z, 64.0D));
		if (target.typeOfHit == Type.ENTITY && target.entityHit != null)
		{
			
			FakePlayerContainer fake = focusPackage.getFake();
			if (fake.cantAttack(target.entityHit))
				return false;
			

			float damage = this.getDamageForDisplay(finalPower);
			EntityLivingBase caster = focusPackage.getCaster();
			target.entityHit.attackEntityFrom(DamageSource.causeIndirectMagicDamage(target.entityHit != null ? target.entityHit : caster, caster), damage);
		}

		return false;
	}

	@Override
	public NodeSetting[] createSettings()
	{
		return new NodeSetting[] { new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5)) };
	}

	@Override
	public void onCast(Entity caster)
	{
		caster.world.playSound(null, caster.getPosition().up(), SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.PLAYERS, 2.0F, 2.0F + (float) (caster.world.rand.nextGaussian() * 0.10000000149011612D));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderParticleFX(World world, double x, double y, double z, double vx, double vy, double vz)
	{
		FXGeneric fb = new FXGeneric(world, x, y, z, vx + world.rand.nextGaussian() * 0.01D, vy + world.rand.nextGaussian() * 0.01D, vz + world.rand.nextGaussian() * 0.01D);
		fb.setMaxAge((int) (15.0F + 10.0F * world.rand.nextFloat()));
		fb.setRBGColorF(0.25F + world.rand.nextFloat() * 0.25F, 0.0F, 0.25F + world.rand.nextFloat() * 0.25F);
		fb.setAlphaF(0.0F, 1.0F, 1.0F, 0.0F);
		fb.setGridSize(64);
		fb.setParticles(128, 14, 1);
		fb.setScale(2.0F + world.rand.nextFloat(), 0.25F + world.rand.nextFloat() * 0.25F);
		fb.setLoop(true);
		fb.setSlowDown(0.9D);
		fb.setGravity((float) (world.rand.nextGaussian() * 0.10000000149011612D));
		fb.setRandomMovementScale(0.0125F, 0.0125F, 0.0125F);
		fb.setRotationSpeed((float) world.rand.nextGaussian());
		ParticleEngine.addEffectWithDelay(world, fb, world.rand.nextInt(4));
	}
}
