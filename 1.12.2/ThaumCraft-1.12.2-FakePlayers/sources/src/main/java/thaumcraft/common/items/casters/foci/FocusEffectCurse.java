package thaumcraft.common.items.casters.foci;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.casters.FocusEffect;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.casters.NodeSetting;
import thaumcraft.api.casters.Trajectory;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockBamf;

public class FocusEffectCurse extends FocusEffect
{
	@Override
	public String getResearch()
	{
		return "FOCUSCURSE";
	}

	@Override
	public String getKey()
	{
		return "thaumcraft.CURSE";
	}

	@Override
	public Aspect getAspect()
	{
		return Aspect.DEATH;
	}

	@Override
	public int getComplexity()
	{
		return this.getSettingValue("duration") + this.getSettingValue("power") * 3;
	}

	@Override
	public float getDamageForDisplay(float finalPower)
	{
		return (1.0F + (float) this.getSettingValue("power")) * finalPower;
	}

	@Override
	public boolean execute(RayTraceResult target, Trajectory trajectory, float finalPower, int num)
	{
		FocusPackage focusPackage = this.getPackage();
		PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockBamf(target.hitVec.x, target.hitVec.y, target.hitVec.z, 6946821, true, true, null), new TargetPoint(focusPackage.world.provider.getDimension(), target.hitVec.x, target.hitVec.y, target.hitVec.z, 64.0D));
		if (target.typeOfHit == Type.ENTITY && target.entityHit != null)
		{
			
			FakePlayerContainer fake = focusPackage.getFake();
			if (fake.cantAttack(target.entityHit))
				return false;
			

			float damage = this.getDamageForDisplay(finalPower);
			int duration = 20 * this.getSettingValue("duration");
			int eff = (int) ((float) this.getSettingValue("power") * finalPower / 2.0F);
			if (eff < 0)
				eff = 0;

			EntityLivingBase caster = focusPackage.getCaster();
			target.entityHit.attackEntityFrom(DamageSource.causeIndirectMagicDamage(target.entityHit != null ? target.entityHit : caster, caster), damage);
			if (target.entityHit instanceof EntityLivingBase)
			{
				EntityLivingBase living = (EntityLivingBase) target.entityHit;
				living.addPotionEffect(new PotionEffect(MobEffects.POISON, duration, Math.round((float) eff)));
				float c = 0.85F;
				if (focusPackage.world.rand.nextFloat() < c)
				{
					living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, Math.round((float) eff)));
					c -= 0.15F;
				}

				if (focusPackage.world.rand.nextFloat() < c)
				{
					living.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, duration, Math.round((float) eff)));
					c -= 0.15F;
				}

				if (focusPackage.world.rand.nextFloat() < c)
				{
					living.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, duration * 2, Math.round((float) eff)));
					c -= 0.15F;
				}

				if (focusPackage.world.rand.nextFloat() < c)
				{
					living.addPotionEffect(new PotionEffect(MobEffects.HUNGER, duration * 3, Math.round((float) eff)));
					c -= 0.15F;
				}

				if (focusPackage.world.rand.nextFloat() < c)
					living.addPotionEffect(new PotionEffect(MobEffects.UNLUCK, duration * 3, Math.round((float) eff)));
			}
		}
		else if (target.typeOfHit == Type.BLOCK)
		{
			float f = (float) Math.min(8.0D, 1.5D * (double) this.getSettingValue("power") * (double) finalPower);

			
			FakePlayerContainer fake = focusPackage.getFake();
			

			for (MutableBlockPos pos : BlockPos.getAllInBoxMutable(target.getBlockPos().add((double) -f, (double) -f, (double) -f), target.getBlockPos().add((double) f, (double) f, (double) f)))
			{
				if (pos.distanceSqToCenter(target.hitVec.x, target.hitVec.y, target.hitVec.z) <= (double) (f * f) && focusPackage.world.isAirBlock(pos.up()) && focusPackage.world.isBlockFullCube(pos))
				{
					IBlockState newState = BlocksTC.effectSap.getDefaultState();

					
					if (fake.cantPlace(pos, newState))
						return false;
					

					focusPackage.world.setBlockState(pos.up(), newState);
				}
			}
		}

		return false;
	}

	@Override
	public NodeSetting[] createSettings()
	{
		return new NodeSetting[] { new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5)), new NodeSetting("duration", "focus.common.duration", new NodeSetting.NodeSettingIntRange(1, 10)) };
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderParticleFX(World world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ)
	{
		FXGeneric fb = new FXGeneric(world, posX, posY, posZ, motionX, motionY, motionZ);
		fb.setMaxAge(8);
		fb.setRBGColorF(0.41F + world.rand.nextFloat() * 0.2F, 0.0F, 0.019F + world.rand.nextFloat() * 0.2F);
		fb.setAlphaF(0.0F, world.rand.nextFloat(), world.rand.nextFloat(), world.rand.nextFloat(), 0.0F);
		fb.setGridSize(16);
		fb.setParticles(72 + world.rand.nextInt(4), 1, 1);
		fb.setScale(2.0F + world.rand.nextFloat() * 4.0F);
		fb.setLoop(false);
		fb.setSlowDown(0.9D);
		fb.setGravity(0.0F);
		fb.setRotationSpeed(world.rand.nextFloat(), 0.0F);
		ParticleEngine.addEffectWithDelay(world, fb, world.rand.nextInt(4));
	}

	@Override
	public void onCast(Entity caster)
	{
		caster.world.playSound(null, caster.getPosition().up(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, 0.15F, 1.0F + caster.getEntityWorld().rand.nextFloat() / 2.0F);
	}
}
