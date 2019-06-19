package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.immersiveengineering.ModUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class EntityChemthrowerShot extends EntityIEProjectile
{
	private Fluid fluid;
    
	public final FakePlayerContainer fake = new FakePlayerContainerEntity(ModUtils.profile, this);

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		this.fake.writeToNBT(nbt);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.fake.readFromNBT(nbt);
    

	public EntityChemthrowerShot(World world)
	{
		super(world);
	}

	public EntityChemthrowerShot(World world, double x, double y, double z, double ax, double ay, double az, Fluid fluid)
	{
		super(world, x, y, z, ax, ay, az);
		this.fluid = fluid;
		this.setFluidSynced();
	}

	public EntityChemthrowerShot(World world, EntityLivingBase living, double ax, double ay, double az, Fluid fluid)
	{
		super(world, living, ax, ay, az);
		this.fluid = fluid;
    
		if (living instanceof EntityPlayer)
    
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(dataMarker_fluid, 0);
	}

	public void setFluidSynced()
	{
		if (this.getFluid() != null)
			this.dataWatcher.updateObject(dataMarker_fluid, FluidRegistry.getFluidID(this.getFluid()));
	}

	public Fluid getFluidSynced()
	{
		return FluidRegistry.getFluid(this.dataWatcher.getWatchableObjectInt(dataMarker_fluid));
	}

	public Fluid getFluid()
	{
		return this.fluid;
	}

	@Override
	public double getGravity()
	{
		if (this.getFluid() != null)
		{
			boolean isGas = this.getFluid().isGaseous() || ChemthrowerHandler.isGas(this.getFluid());
			return (isGas ? .025f : .05F) * (this.getFluid().getDensity() < 0 ? -1 : 1);
		}
		return super.getGravity();
	}

	@Override
	public boolean canIgnite()
	{
		return ChemthrowerHandler.isFlammable(this.getFluid());
	}

	@Override
	public void onEntityUpdate()
	{
		if (this.getFluid() == null && this.worldObj.isRemote)
			this.fluid = this.getFluidSynced();
		Block b = this.worldObj.getBlock((int) this.posX, (int) this.posY, (int) this.posZ);
		if (b != null && this.canIgnite() && (b.getMaterial() == Material.fire || b.getMaterial() == Material.lava))
			this.setFire(6);
		super.onEntityUpdate();
	}

	@Override
	public void onImpact(MovingObjectPosition mop)
	{
		if (!this.worldObj.isRemote && this.getFluid() != null)
		{
			ChemthrowerEffect effect = ChemthrowerHandler.getEffect(this.getFluid());
			boolean fire = this.getFluid().getTemperature() > 1000;
			if (effect != null)
			{
				ItemStack thrower = null;
				EntityPlayer shooter = (EntityPlayer) this.getShooter();
				if (shooter != null)
					thrower = shooter.getCurrentEquippedItem();

				if (mop.typeOfHit == MovingObjectType.ENTITY)
    
					if (this.fake.cantDamage(mop.entityHit))
    

					effect.applyToEntity((EntityLivingBase) mop.entityHit, shooter, thrower, this.fluid);
				}
				else if (mop.typeOfHit == MovingObjectType.BLOCK)
    
					if (this.fake.cantBreak(mop.blockX, mop.blockY, mop.blockZ))
    

					effect.applyToBlock(this.worldObj, mop, shooter, thrower, this.fluid);
				}
			}
			else if (mop.entityHit != null && this.getFluid().getTemperature() > 500)
    
				if (this.fake.cantDamage(mop.entityHit))
    

				int tempDiff = this.getFluid().getTemperature() - 300;
				int damage = Math.abs(tempDiff) / 500;
				if (mop.entityHit.attackEntityFrom(DamageSource.lava, damage))
					mop.entityHit.hurtResistantTime = (int) (mop.entityHit.hurtResistantTime * .75);
			}
			if (mop.entityHit != null)
			{
				int f = this.isBurning() ? this.fire : fire ? 3 : 0;
				if (f > 0)
    
					if (this.fake.cantDamage(mop.entityHit))
    

					mop.entityHit.setFire(f);
					if (mop.entityHit.attackEntityFrom(DamageSource.inFire, 2))
						mop.entityHit.hurtResistantTime = (int) (mop.entityHit.hurtResistantTime * .75);
				}
			}
		}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}