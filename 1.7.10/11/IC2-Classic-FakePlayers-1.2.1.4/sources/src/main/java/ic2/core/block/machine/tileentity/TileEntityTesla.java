package ic2.core.block.machine.tileentity;

import java.util.List;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyContainer;
import ic2.api.energy.tile.IEnergySink;
import ic2.core.IC2DamageSource;
import ic2.core.block.TileEntityBlock;
import ic2.core.item.armor.ItemArmorHazmat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityTesla extends TileEntityBlock implements IEnergySink, IEnergyContainer
{
	public int energy = 0;
	public int ticker = 0;
	public int maxEnergy = 10000;
	public int maxInput = 128;
	public boolean addedToEnergyNet = false;

	public TileEntityTesla()
	{
		this.addGuiFields(new String[] { "energy" });
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.energy = nbttagcompound.getShort("energy");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setShort("energy", (short) this.energy);
	}

	@Override
	public void onLoaded()
	{
		super.onLoaded();
		if (this.isSimulating())
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			this.addedToEnergyNet = true;
		}

	}

	@Override
	public void onUnloaded()
	{
		if (this.isSimulating() && this.addedToEnergyNet)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			this.addedToEnergyNet = false;
		}

		super.onUnloaded();
	}

	@Override
	public boolean canUpdate()
	{
		return this.isSimulating();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (this.isSimulating() && this.redstoned())
			if (this.energy >= getCost())
			{
				int damage = this.energy / getCost();
				--this.energy;
				if (this.ticker++ % 32 == 0 && this.shock(damage))
					this.energy = 0;

				this.getNetwork().updateTileGuiField(this, "energy");
			}
	}

	public boolean shock(int damage)
	{
		boolean shock = false;
		List list1 = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(this.xCoord - 4, this.yCoord - 4, this.zCoord - 4, this.xCoord + 5, this.yCoord + 5, this.zCoord + 5));

		for (int l = 0; l < list1.size(); ++l)
		{
			EntityLivingBase victim = (EntityLivingBase) list1.get(l);
			if (!ItemArmorHazmat.hasCompleteHazmat(victim))
    
				if (this.fake.cantDamage(victim))
    

				shock = true;
				victim.attackEntityFrom(IC2DamageSource.electricity, damage);

				for (int i = 0; i < damage; ++i)
					this.worldObj.spawnParticle("reddust", victim.posX + this.worldObj.rand.nextFloat(), victim.posY + this.worldObj.rand.nextFloat() * 2.0F, victim.posZ + this.worldObj.rand.nextFloat(), 0.0D, 0.0D, 1.0D);
			}
		}

		return shock;
	}

	public boolean redstoned()
	{
		return this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord) || this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
	}

	public static int getCost()
	{
		return 400;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return true;
	}

	@Override
	public double getDemandedEnergy()
	{
		return this.maxEnergy - this.energy;
	}

	@Override
	public double injectEnergy(ForgeDirection directionFrom, double amount, double volt)
	{
		if (amount > this.maxInput)
			return 0.0D;
		else
		{
			this.energy = (int) (this.energy + amount);
			int re = 0;
			if (this.energy > this.maxEnergy)
			{
				re = this.energy - this.maxEnergy;
				this.energy = this.maxEnergy;
			}

			this.getNetwork().updateTileGuiField(this, "energy");
			return re;
		}
	}

	@Override
	public int getSinkTier()
	{
		return EnergyNet.instance.getTierFromPower(this.maxInput);
	}

	@Override
	public int getStoredEnergy()
	{
		return this.energy;
	}

	@Override
	public int getEnergyCapacity()
	{
		return this.maxEnergy;
	}

	@Override
	public int getEnergyUsage()
	{
		return this.energy;
	}

	@Override
	public int getEnergyProduction()
	{
		return 0;
	}

	@Override
	public int getMaxEnergyInput()
	{
		return 128;
	}
}
