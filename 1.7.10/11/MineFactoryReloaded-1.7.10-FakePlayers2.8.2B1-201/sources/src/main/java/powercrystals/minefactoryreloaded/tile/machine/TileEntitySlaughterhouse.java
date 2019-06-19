package powercrystals.minefactoryreloaded.tile.machine;

import cofh.core.util.fluid.FluidTankAdv;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.core.GrindingDamage;
import powercrystals.minefactoryreloaded.setup.Machine;

import java.util.Iterator;
import java.util.List;

public class TileEntitySlaughterhouse extends TileEntityGrinder
{
	public TileEntitySlaughterhouse()
	{
		super(Machine.Slaughterhouse);
		this._damageSource = new GrindingDamage("mfr.slaughterhouse", 2);
		this.setManageSolids(false);
		this._tanks[0].setLock(FluidRegistry.getFluid("meat"));
		this._tanks[1].setLock(FluidRegistry.getFluid("pinkslime"));
	}

	@Override
	public void setWorldObj(World var1)
	{
		super.setWorldObj(var1);
		if (this._grindingWorld != null)
			this._grindingWorld.setAllowSpawns(true);

	}

	@Override
	protected FluidTankAdv[] createTanks()
	{
		return new FluidTankAdv[] { new FluidTankAdv(4000), new FluidTankAdv(2000) };
	}

	@Override
	public boolean activateMachine()
	{
		this._grindingWorld.cleanReferences();
		List<EntityLivingBase> entities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this._areaManager.getHarvestArea().toAxisAlignedBB());
		Iterator<EntityLivingBase> iterator = entities.iterator();

		EntityLivingBase entity;
		label34:
		while (true)
		{
			if (!iterator.hasNext())
			{
				this.setIdleTicks(this.getIdleTicksMax());
				return false;
			}

			entity = iterator.next();

			for (Class clazz : MFRRegistry.getSlaughterhouseBlacklist())
			{
				if (clazz.isInstance(entity))
					continue label34;
			}

			if ((!(entity instanceof EntityAgeable) || ((EntityAgeable) entity).getGrowingAge() >= 0) && !entity.isEntityInvulnerable() && entity.getHealth() > 0.0F && this._grindingWorld.addEntityForGrinding(entity))
				break;
    
		if (this.fake.cantDamage(entity))
		{
			this.setIdleTicks(this.getIdleTicksMax());
			return false;
    

		float var7 = (float) Math.pow(entity.boundingBox.getAverageEdgeLength(), 2.0D);
		this.damageEntity(entity);
		if (entity.getHealth() <= 0.0F)
		{
			if (this._rand.nextInt(8) != 0)
				this.fillTank(this._tanks[0], "meat", var7);
			else
				this.fillTank(this._tanks[1], "pinkslime", var7);

			this.setIdleTicks(10);
		}
		else
			this.setIdleTicks(5);

		return true;
	}

	@Override
	public void acceptXPOrb(EntityXPOrb var1)
	{
	}

	@Override
	protected void damageEntity(EntityLivingBase var1)
	{
		this.setRecentlyHit(var1, 0);
		var1.attackEntityFrom(this._damageSource, 2.6584558E36F);
	}
}
