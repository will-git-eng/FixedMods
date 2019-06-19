package powercrystals.minefactoryreloaded.tile.tank;

import cofh.core.util.fluid.FluidTankAdv;
import com.google.common.base.Throwables;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import java.util.Arrays;

public class FluidTankMulti implements IFluidTank
{
	FluidTankAdv[] tanks = new FluidTankAdv[2];
	int length;
	int index;
	private FluidStack fluid = null;
	private TankNetwork grid;

	public FluidTankMulti(TankNetwork grid)
	{
		this.grid = grid;
	}

	public void addTank(FluidTankAdv tank)
	{
		if (tank == null)
			throw new IllegalArgumentException("null");
		int index = this.length;

		while (index-- > 0)
		{
			if (this.tanks[index] == tank)
				return;
		}

		if (++this.length >= this.tanks.length)
		{
			FluidTankAdv[] var3 = this.tanks;
			this.tanks = new FluidTankAdv[this.length * 2];
			System.arraycopy(var3, 0, this.tanks, 0, this.length - 1);
		}

		this.tanks[this.length - 1] = tank;
		this.fill(tank.drain(tank.getCapacity(), true), true);
	}

	public void removeTank(FluidTankAdv tank)
	{
		if (tank == null)
			throw new IllegalArgumentException("null");
		int length = this.length;

		while (length-- > 0 && this.tanks[length] != tank)
		{
		}

		if (length >= 0)
		{
			FluidTankAdv[] arr = this.tanks;
			if (--this.length != length)
				System.arraycopy(arr, length + 1, arr, length, this.length - length + 1);

			if (this.length <= arr.length / 4)
			{
				this.tanks = new FluidTankAdv[arr.length / 2];
				System.arraycopy(arr, 0, this.tanks, 0, this.tanks.length);
			}

			if (this.index >= length)
				--this.index;

			FluidStack var4 = tank.getFluid();
			if (var4 != null)
			{
				this.fluid.amount -= var4.amount;
				if (this.fluid.amount <= 0)
				{
					this.fluid = null;
					this.grid.updateNodes();
				}
			}

			this.tanks[this.length] = null;
			tank.setFluid(var4);
		}
	}

	public void empty()
	{
		for (int var1 = this.length; var1-- > 0; this.tanks[var1] = null)
		{
		}

		this.length = 0;
		this.index = 0;
		this.fluid = null;
	}

	@Override
	public FluidStack getFluid()
	{
		return this.fluid;
	}

	@Override
	public int getFluidAmount()
	{
		return this.fluid == null ? 0 : this.fluid.amount;
	}

	@Override
	public int getCapacity()
	{
		return this.length * TileEntityTank.CAPACITY;
	}

	@Override
	public FluidTankInfo getInfo()
	{
		return new FluidTankInfo(this);
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		try
		{
			int filled = 0;
			if (resource != null && (this.fluid == null || this.fluid.isFluidEqual(resource)))
			{
				int index;
				for (index = this.index; index < this.length; ++index)
    
					if (index < 0 || index >= this.tanks.length)
    

					filled += this.tanks[index].fill(resource, doFill);
					if (filled >= resource.amount)
						break;
				}

				if (index == this.length)
					--index;

				if (doFill)
				{
					this.index = index;
					boolean fluidWasNull = false;
					if (this.fluid == null)
					{
						fluidWasNull = true;
						this.fluid = new FluidStack(resource, 0);
					}

					this.fluid.amount += filled;
					if (fluidWasNull)
						this.grid.updateNodes();
				}
			}

			return filled;
		}
		catch (Throwable throwable)
		{
			System.out.format("%s, ", Arrays.toString(this.tanks));
			System.out.format("index: %s, length: %s, tanks.length: %s, grid: %s\n", this.index, this.length, this.tanks.length, this.grid);
			throw Throwables.propagate(throwable);
		}
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		try
		{
			if (this.fluid == null)
				return null;
			FluidStack result = new FluidStack(this.fluid, 0);
			int index = this.index;

			while (index >= 0)
    
				if (index < 0)
    

				int amount = this.tanks[index].getFluidAmount();
				FluidStack drained = this.tanks[index].drain(maxDrain, doDrain);
				if (drained != null)
				{
					result.amount += drained.amount;
					maxDrain -= drained.amount;
					if (amount == drained.amount)
						--index;
				}

				if (drained == null || maxDrain <= 0)
					break;
			}

    
			if (index < 0)
    

			if (doDrain)
			{
				this.index = index;
				this.fluid.amount -= result.amount;
				if (this.fluid.amount <= 0)
				{
					this.fluid = null;
					this.grid.updateNodes();
				}
			}

			return result;
		}
		catch (Throwable throwable)
		{
			System.out.format("%s, ", Arrays.toString(this.tanks));
			System.out.format("index: %s, length: %s, tanks.length: %s, grid: %s\n", this.index, this.length, this.tanks.length, this.grid);
			throw Throwables.propagate(throwable);
		}
	}

	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		return this.fluid != null && this.fluid.isFluidEqual(resource) ? this.drain(resource.amount, doDrain) : null;
	}
}
