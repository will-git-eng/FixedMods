package thaumcraft.common.items.casters.foci;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
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
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXFocusPartImpact;

public class FocusEffectFire extends FocusEffect
{
	@Override
	public String getResearch()
	{
		return "BASEAUROMANCY";
	}

	@Override
	public String getKey()
	{
		return "thaumcraft.FIRE";
	}

	@Override
	public Aspect getAspect()
	{
		return Aspect.FIRE;
	}

	@Override
	public int getComplexity()
	{
		return this.getSettingValue("duration") + this.getSettingValue("power") * 2;
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
		World world = focusPackage.world;
		PacketHandler.INSTANCE.sendToAllAround(new PacketFXFocusPartImpact(target.hitVec.x, target.hitVec.y, target.hitVec.z, new String[] { this.getKey() }), new TargetPoint(world.provider.getDimension(), target.hitVec.x, target.hitVec.y, target.hitVec.z, 64.0D));
		if (target.typeOfHit == Type.ENTITY && target.entityHit != null)
		{
			if (target.entityHit.isImmuneToFire())
				return false;

			
			FakePlayerContainer fake = focusPackage.getFake();
			if (fake.cantAttack(target.entityHit))
				return false;
			

			float fire = (float) (1 + this.getSettingValue("duration") * this.getSettingValue("duration"));
			float damage = this.getDamageForDisplay(finalPower);
			fire = fire * finalPower;
			EntityLivingBase caster = focusPackage.getCaster();
			target.entityHit.attackEntityFrom(new EntityDamageSourceIndirect("fireball", target.entityHit != null ? target.entityHit : caster, caster).setFireDamage(), damage);
			if (fire > 0.0F)
				target.entityHit.setFire(Math.round(fire));

			return true;
		}
		if (target.typeOfHit == Type.BLOCK && this.getSettingValue("duration") > 0)
		{
			BlockPos pos = target.getBlockPos();
			pos = pos.offset(target.sideHit);
			if (world.isAirBlock(pos) && world.rand.nextFloat() < finalPower)
			{
				IBlockState newState = Blocks.FIRE.getDefaultState();

				
				FakePlayerContainer fake = focusPackage.getFake();
				if (fake.cantPlace(pos, newState))
					return false;
				

				world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				world.setBlockState(pos, newState, 11);
				return true;
			}
		}

		return false;
	}

	@Override
	public NodeSetting[] createSettings()
	{
		return new NodeSetting[] { new NodeSetting("power", "focus.common.power", new NodeSetting.NodeSettingIntRange(1, 5)), new NodeSetting("duration", "focus.fire.burn", new NodeSetting.NodeSettingIntRange(0, 5)) };
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderParticleFX(World world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ)
	{
		FXDispatcher.GenPart pp = new FXDispatcher.GenPart();
		pp.grav = -0.2F;
		pp.age = 10;
		pp.alpha = new float[] { 0.7F };
		pp.partStart = 640;
		pp.partInc = 1;
		pp.partNum = 10;
		pp.slowDown = 0.75D;
		pp.scale = new float[] { (float) (1.5D + world.rand.nextGaussian() * 0.20000000298023224D) };
		FXDispatcher.INSTANCE.drawGenericParticles(posX, posY, posZ, motionX, motionY, motionZ, pp);
	}

	@Override
	public void onCast(Entity caster)
	{
		caster.world.playSound(null, caster.getPosition().up(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F + (float) (caster.world.rand.nextGaussian() * 0.05000000074505806D));
	}
}
