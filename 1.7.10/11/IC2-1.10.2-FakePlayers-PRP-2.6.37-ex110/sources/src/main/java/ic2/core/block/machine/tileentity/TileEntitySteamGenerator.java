package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.ExplosionIC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.block.machine.gui.GuiSteamGenerator;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.Localization;
import ic2.core.ref.FluidName;
import ic2.core.util.BiomeUtil;
import ic2.core.util.LiquidUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySteamGenerator extends TileEntityInventory implements IHasGui, IGuiValueProvider, IFluidHandler, INetworkClientTileEntityEventListener
{
	private final float maxHeat = 500.0F;
	private final int maxCalcification = 100000;
	private int heatInput = 0;
	private int inputMB = 0;
	public FluidTank waterTank = new FluidTank(10000);
	private int calcification = 0;
	private int outputMB = 0;
	private TileEntitySteamGenerator.outputType outputFluid = TileEntitySteamGenerator.outputType.NONE;
	private float systemHeat;
	private int pressure = 0;
	private boolean newActive = false;

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.waterTank.readFromNBT(nbttagcompound.getCompoundTag("WaterTank"));
		this.inputMB = nbttagcompound.getInteger("inputmb");
		this.pressure = nbttagcompound.getInteger("pressurevalve");
		this.systemHeat = nbttagcompound.getFloat("systemheat");
		this.calcification = nbttagcompound.getInteger("calcification");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setTag("WaterTank", this.waterTank.writeToNBT(new NBTTagCompound()));
		nbt.setInteger("inputmb", this.inputMB);
		nbt.setInteger("pressurevalve", this.pressure);
		nbt.setFloat("systemheat", this.systemHeat);
		nbt.setInteger("calcification", this.calcification);
		return nbt;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.systemHeat = Math.max(this.systemHeat, BiomeUtil.getBiomeTemperature(this.worldObj, this.pos));
		if (this.isCalcified())
		{
			if (this.getActive())
				this.setActive(false);
		}
		else
		{
			this.newActive = this.work();
			if (this.getActive() != this.newActive)
				this.setActive(this.newActive);
		}

		if (!this.getActive())
			this.cooldown(0.01F);

	}

	private boolean work()
	{
		if (this.waterTank.getFluidAmount() > 0 && this.inputMB > 0)
		{
			FluidStack output = this.getOutputfluid();
			if (output != null)
			{
				this.outputMB = output.amount;
				this.outputFluid = this.getOutputType(output);
				output.amount -= LiquidUtil.distribute(this, output, false);
				if (output.amount > 0)
					if (this.outputFluid.isSteam() && this.worldObj.rand.nextInt(10) == 0)
    
						ExplosionIC2 explosion = new ExplosionIC2(this.worldObj, null, this.pos, 1, 1.0F, ExplosionIC2.Type.Heat);
						explosion.fake.setParent(this.fake);
    
					}
					else
						this.waterTank.fill(output, true);

				return true;
			}
		}

		this.outputMB = 0;
		this.outputFluid = TileEntitySteamGenerator.outputType.NONE;
		this.heatInput = 0;
		return this.heatupmax();
	}

	private boolean heatupmax()
	{
		this.heatInput = this.requestHeat(1200);
		if (this.heatInput > 0)
		{
			this.heatup(this.heatInput);
			return true;
		}
		else
			return false;
	}

	private TileEntitySteamGenerator.outputType getOutputType(FluidStack fluid)
	{
		return fluid.getFluid().equals(FluidName.superheated_steam.getInstance()) ? TileEntitySteamGenerator.outputType.SUPERHEATEDSTEAM : fluid.getFluid().equals(FluidName.steam.getInstance()) ? TileEntitySteamGenerator.outputType.STEAM : fluid.getFluid().equals(FluidName.distilled_water.getInstance()) ? TileEntitySteamGenerator.outputType.DISTILLEDWATER : fluid.getFluid().equals(FluidRegistry.WATER) ? TileEntitySteamGenerator.outputType.WATER : TileEntitySteamGenerator.outputType.NONE;
	}

	private FluidStack getOutputfluid()
	{
		if (this.waterTank.getFluid() == null)
			return null;
		else
		{
			Fluid fluidInTank = this.waterTank.getFluid().getFluid();
			boolean cancalcification = fluidInTank.equals(FluidRegistry.WATER);
			if (this.systemHeat < 100.0F)
			{
				this.heatupmax();
				return this.waterTank.drain(this.inputMB, true);
			}
			else
			{
				int hUneeded = 100 + Math.round(this.pressure / 220.0F * 100.0F);
				int targetTemp = (int) (100L + Math.round(this.pressure / 220.0F * 100.0F * 2.74D));
				if (this.getSystemHeat() == targetTemp)
				{
					int heat = this.requestHeat(this.inputMB * hUneeded);
					this.heatInput = heat;
					if (heat == this.inputMB * hUneeded)
					{
						if (cancalcification)
							++this.calcification;

						this.waterTank.drain(this.inputMB, true);
						return new FluidStack(this.systemHeat >= 374.0F ? FluidName.superheated_steam.getInstance() : FluidName.steam.getInstance(), this.inputMB * 100);
					}
					else
					{
						this.heatup(heat);
						return this.waterTank.drain(this.inputMB, true);
					}
				}
				else if (this.systemHeat <= targetTemp)
				{
					this.heatupmax();
					return this.waterTank.drain(this.inputMB, true);
				}
				else
				{
					this.heatInput = 0;
					int count = this.inputMB;

					while (this.systemHeat > targetTemp)
					{
						this.cooldown(0.1F);
						if (cancalcification)
							++this.calcification;

						--count;
						if (count == 0)
							break;
					}

					this.waterTank.drain(this.inputMB - count, true);
					return new FluidStack(FluidName.steam.getInstance(), (this.inputMB - count) * 100);
				}
			}
		}
	}

	private void heatup(int heatinput)
	{
		this.systemHeat += heatinput * 5.0E-4F;
		if (this.systemHeat > 500.0F)
		{
    
			ExplosionIC2 explosion = new ExplosionIC2(this.worldObj, null, this.pos, 10, 0.01F, ExplosionIC2.Type.Heat);
			explosion.fake.setParent(this.fake);
    
		}
	}

	private void cooldown(float cool)
	{
		this.systemHeat = Math.max(this.systemHeat - cool, BiomeUtil.getBiomeTemperature(this.worldObj, this.pos));
	}

	private int requestHeat(int requestHeat)
	{
		int targetHeat = requestHeat;

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			TileEntity target = this.worldObj.getTileEntity(this.pos.offset(dir));
			if (target instanceof IHeatSource)
			{
				int amount = ((IHeatSource) target).requestHeat(dir.getOpposite(), targetHeat);
				if (amount > 0)
				{
					targetHeat -= amount;
					if (targetHeat == 0)
						return requestHeat;
				}
			}
		}

		return requestHeat - targetHeat;
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		if (event <= 2000 && event >= -2000)
			this.inputMB = Math.max(Math.min(this.inputMB + event, 1000), 0);
		else
		{
			if (event > 2000)
				this.pressure = Math.min(this.pressure + event - 2000, 300);

			if (event < -2000)
				this.pressure = Math.max(this.pressure + event + 2000, 0);
		}

	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		return !this.canFill(from, resource.getFluid()) ? 0 : this.waterTank.fill(resource, doFill);
	}

	public int gaugeLiquidScaled(int i, int tank)
	{
		return tank == 0 ? this.waterTank.getFluidAmount() <= 0 ? 0 : this.waterTank.getFluidAmount() * i / this.waterTank.getCapacity() : 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return false;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return fluid.equals(FluidName.distilled_water.getInstance()) || fluid.equals(FluidRegistry.WATER);
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return new FluidTankInfo[] { this.waterTank.getInfo() };
	}

	@Override
	public ContainerBase<TileEntitySteamGenerator> getGuiContainer(EntityPlayer player)
	{
		return new ContainerSteamGenerator(player, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiSteamGenerator(new ContainerSteamGenerator(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("heat".equals(name))
			return this.systemHeat == 0.0F ? 0.0D : this.systemHeat / 500.0D;
		else if ("calcification".equals(name))
			return this.calcification == 0 ? 0.0D : this.calcification / 100000.0D;
		else
			throw new IllegalArgumentException();
	}

	public int getOutputMB()
	{
		return this.outputMB;
	}

	public int getInputMB()
	{
		return this.inputMB;
	}

	public int getHeatInput()
	{
		return this.heatInput;
	}

	public int getPressure()
	{
		return this.pressure;
	}

	public float getSystemHeat()
	{
		return Math.round(this.systemHeat * 10.0F) / 10.0F;
	}

	public float getCalcification()
	{
		return Math.round(this.calcification / 100000.0F * 100.0F * 100.0F) / 100.0F;
	}

	public boolean isCalcified()
	{
		return this.calcification >= 100000;
	}

	public String getOutputFluidName()
	{
		return this.outputFluid.getName();
	}

	private static enum outputType
	{
		NONE(""),
		WATER(Localization.translate("ic2.SteamGenerator.output.water")),
		DISTILLEDWATER(Localization.translate("ic2.SteamGenerator.output.destiwater")),
		STEAM(Localization.translate("ic2.SteamGenerator.output.steam")),
		SUPERHEATEDSTEAM(Localization.translate("ic2.SteamGenerator.output.hotsteam"));

		private final String name;

		private outputType(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return this.name;
		}

		public boolean isWater()
		{
			return this == WATER || this == DISTILLEDWATER;
		}

		public boolean isSteam()
		{
			return this == STEAM || this == SUPERHEATEDSTEAM;
		}
	}
}
