package erogenousbeef.bigreactors.common.multiblock;

import cofh.api.energy.IEnergyProvider;
import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.data.SourceProductMapping;
import erogenousbeef.bigreactors.api.registry.Reactants;
import erogenousbeef.bigreactors.api.registry.ReactorInterior;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.data.RadiationData;
import erogenousbeef.bigreactors.common.data.StandardReactants;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;
import erogenousbeef.bigreactors.common.interfaces.IReactorFuelInfo;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.multiblock.helpers.CoolantContainer;
import erogenousbeef.bigreactors.common.multiblock.helpers.FuelContainer;
import erogenousbeef.bigreactors.common.multiblock.helpers.RadiationHelper;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IActivateable;
import erogenousbeef.bigreactors.common.multiblock.interfaces.ITickableMultiblockPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.*;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.multiblock.ReactorUpdateMessage;
import erogenousbeef.bigreactors.net.message.multiblock.ReactorUpdateWasteEjectionMessage;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockControllerBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MultiblockReactor extends RectangularMultiblockControllerBase
		implements IEnergyProvider, IReactorFuelInfo, IMultipleFluidHandler, IActivateable
{
	public static final int FuelCapacityPerFuelRod = 4 * Reactants.standardSolidReactantAmount; // 4 ingots per rod

	public static final int FLUID_SUPERHEATED = CoolantContainer.HOT;
	public static final int FLUID_COOLANT = CoolantContainer.COLD;

	private static final float passiveCoolingPowerEfficiency = 0.5f; // 50% power penalty, so this comes out as about 1/3 a basic water-cooled reactor
	private static final float passiveCoolingTransferEfficiency = 0.2f; // 20% of available heat transferred per tick when passively cooled
	private static final float reactorHeatLossConductivity = 0.001f; // circa 1RF per tick per external surface block

	// Game stuff - stored
	protected boolean active;
	private float reactorHeat;
	private float fuelHeat;
	private WasteEjectionSetting wasteEjection;
	private float energyStored;
	protected FuelContainer fuelContainer;
	protected RadiationHelper radiationHelper;
	protected CoolantContainer coolantContainer;

	// Game stuff - derived at runtime
	protected float fuelToReactorHeatTransferCoefficient;
	protected float reactorToCoolantSystemHeatTransferCoefficient;
	protected float reactorHeatLossCoefficient;

	protected Iterator<TileEntityReactorFuelRod> currentFuelRod;
	int reactorVolume;

	// UI stuff
	private float energyGeneratedLastTick;
	private float fuelConsumedLastTick;

	public enum WasteEjectionSetting
	{
		kAutomatic, // Full auto, always remove waste
		kManual, // Manual, only on button press
	}

	public static final WasteEjectionSetting[] s_EjectionSettings = WasteEjectionSetting.values();

	// Lists of connected parts
	private Set<TileEntityReactorPowerTap> attachedPowerTaps;
	private Set<ITickableMultiblockPart> attachedTickables;

	private Set<TileEntityReactorControlRod> attachedControlRods; // Highest internal Y-coordinate in the fuel column
	private Set<TileEntityReactorAccessPort> attachedAccessPorts;
	private Set<TileEntityReactorPart> attachedControllers;

	private Set<TileEntityReactorFuelRod> attachedFuelRods;
	private Set<TileEntityReactorCoolantPort> attachedCoolantPorts;

	private Set<TileEntityReactorGlass> attachedGlass;

	// Updates
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;
	private static final int maxEnergyStored = 10000000;

	public MultiblockReactor(World world)
	{
		super(world);

		// Game stuff
		this.active = false;
		this.reactorHeat = 0f;
		this.fuelHeat = 0f;
		this.energyStored = 0f;
		this.wasteEjection = WasteEjectionSetting.kAutomatic;

		// Derived stats
		this.fuelToReactorHeatTransferCoefficient = 0f;
		this.reactorToCoolantSystemHeatTransferCoefficient = 0f;
		this.reactorHeatLossCoefficient = 0f;

		// UI and stats
		this.energyGeneratedLastTick = 0f;
		this.fuelConsumedLastTick = 0f;

		this.attachedPowerTaps = new HashSet<TileEntityReactorPowerTap>();
		this.attachedTickables = new HashSet<ITickableMultiblockPart>();
		this.attachedControlRods = new HashSet<TileEntityReactorControlRod>();
		this.attachedAccessPorts = new HashSet<TileEntityReactorAccessPort>();
		this.attachedControllers = new HashSet<TileEntityReactorPart>();
		this.attachedFuelRods = new HashSet<TileEntityReactorFuelRod>();
		this.attachedCoolantPorts = new HashSet<TileEntityReactorCoolantPort>();
		this.attachedGlass = new HashSet<TileEntityReactorGlass>();

		this.currentFuelRod = null;

		this.updatePlayers = new HashSet<EntityPlayer>();

		this.ticksSinceLastUpdate = 0;
		this.fuelContainer = new FuelContainer();
		this.radiationHelper = new RadiationHelper();
		this.coolantContainer = new CoolantContainer();

		this.reactorVolume = 0;
	}

	public void beginUpdatingPlayer(EntityPlayer playerToUpdate)
	{
		this.updatePlayers.add(playerToUpdate);
		this.sendIndividualUpdate(playerToUpdate);
	}

	public void stopUpdatingPlayer(EntityPlayer playerToRemove)
	{
		this.updatePlayers.remove(playerToRemove);
	}

	@Override
	protected void onBlockAdded(IMultiblockPart part)
	{
		if (part instanceof TileEntityReactorAccessPort)
			this.attachedAccessPorts.add((TileEntityReactorAccessPort) part);

		if (part instanceof TileEntityReactorControlRod)
		{
			TileEntityReactorControlRod controlRod = (TileEntityReactorControlRod) part;
			this.attachedControlRods.add(controlRod);
		}

		if (part instanceof TileEntityReactorPowerTap)
			this.attachedPowerTaps.add((TileEntityReactorPowerTap) part);

		if (part instanceof TileEntityReactorPart)
		{
			TileEntityReactorPart reactorPart = (TileEntityReactorPart) part;
			if (BlockReactorPart.isController(reactorPart.getBlockMetadata()))
				this.attachedControllers.add(reactorPart);
		}

		if (part instanceof ITickableMultiblockPart)
			this.attachedTickables.add((ITickableMultiblockPart) part);

		if (part instanceof TileEntityReactorFuelRod)
		{
			TileEntityReactorFuelRod fuelRod = (TileEntityReactorFuelRod) part;
			this.attachedFuelRods.add(fuelRod);

			// Reset iterator
			this.currentFuelRod = this.attachedFuelRods.iterator();

			if (this.worldObj.isRemote)
				this.worldObj.markBlockForUpdate(fuelRod.xCoord, fuelRod.yCoord, fuelRod.zCoord);
		}

		if (part instanceof TileEntityReactorCoolantPort)
			this.attachedCoolantPorts.add((TileEntityReactorCoolantPort) part);

		if (part instanceof TileEntityReactorGlass)
			this.attachedGlass.add((TileEntityReactorGlass) part);
	}

	@Override
	protected void onBlockRemoved(IMultiblockPart part)
	{
		if (part instanceof TileEntityReactorAccessPort)
			this.attachedAccessPorts.remove(part);

		if (part instanceof TileEntityReactorControlRod)
			this.attachedControlRods.remove(part);

		if (part instanceof TileEntityReactorPowerTap)
			this.attachedPowerTaps.remove(part);

		if (part instanceof TileEntityReactorPart)
		{
			TileEntityReactorPart reactorPart = (TileEntityReactorPart) part;
			if (BlockReactorPart.isController(reactorPart.getBlockMetadata()))
				this.attachedControllers.remove(reactorPart);
		}

		if (part instanceof ITickableMultiblockPart)
			this.attachedTickables.remove(part);

		if (part instanceof TileEntityReactorFuelRod)
		{
			this.attachedFuelRods.remove(part);
			this.currentFuelRod = this.attachedFuelRods.iterator();
		}

		if (part instanceof TileEntityReactorCoolantPort)
			this.attachedCoolantPorts.remove(part);

		if (part instanceof TileEntityReactorGlass)
			this.attachedGlass.remove(part);
	}

	@Override
	protected void isMachineWhole() throws MultiblockValidationException
	{
		// Ensure that there is at least one controller and control rod attached.
		if (this.attachedControlRods.size() < 1)
			throw new MultiblockValidationException("Not enough control rods. Reactors require at least 1.");

		if (this.attachedControllers.size() < 1)
			throw new MultiblockValidationException("Not enough controllers. Reactors require at least 1.");

		super.isMachineWhole();
	}

	@Override
	public void updateClient()
	{
	}

	// Update loop. Only called when the machine is assembled.
	@Override
	public boolean updateServer()
	{
		if (Float.isNaN(this.getReactorHeat()))
			this.setReactorHeat(0.0f);

		float oldHeat = this.getReactorHeat();
		float oldEnergy = this.getEnergyStored();
		this.energyGeneratedLastTick = 0f;
		this.fuelConsumedLastTick = 0f;

		float newHeat = 0f;

		if (this.getActive())
		{
			// Select a control rod to radiate from. Reset the iterator and select a new Y-level if needed.
			if (!this.currentFuelRod.hasNext())
				this.currentFuelRod = this.attachedFuelRods.iterator();

			// Radiate from that control rod
			TileEntityReactorFuelRod source = this.currentFuelRod.next();
			TileEntityReactorControlRod sourceControlRod = (TileEntityReactorControlRod) this.worldObj.getTileEntity(source.xCoord, this.getMaximumCoord().y, source.zCoord);
			if (sourceControlRod != null)
			{
				RadiationData radData = this.radiationHelper.radiate(this.worldObj, this.fuelContainer, source, sourceControlRod, this.getFuelHeat(), this.getReactorHeat(), this.attachedControlRods.size());

				// Assimilate results of radiation
				if (radData != null)
				{
					this.addFuelHeat(radData.getFuelHeatChange(this.attachedFuelRods.size()));
					this.addReactorHeat(radData.getEnvironmentHeatChange(this.getReactorVolume()));
					this.fuelConsumedLastTick += radData.fuelUsage;
				}
			}
		}

		// Allow radiation to decay even when reactor is off.
		this.radiationHelper.tick(this.getActive());

		// If we can, poop out waste and inject new fuel.
		if (this.wasteEjection == WasteEjectionSetting.kAutomatic)
			this.ejectWaste(false, null);

		this.refuel();

		// Heat Transfer: Fuel Pool <> Reactor Environment
		float tempDiff = this.fuelHeat - this.reactorHeat;
		if (tempDiff > 0.01f)
		{
			float rfTransferred = tempDiff * this.fuelToReactorHeatTransferCoefficient;
			float fuelRf = StaticUtils.Energy.getRFFromVolumeAndTemp(this.attachedFuelRods.size(), this.fuelHeat);

			fuelRf -= rfTransferred;
			this.setFuelHeat(StaticUtils.Energy.getTempFromVolumeAndRF(this.attachedFuelRods.size(), fuelRf));

			// Now see how much the reactor's temp has increased
			float reactorRf = StaticUtils.Energy.getRFFromVolumeAndTemp(this.getReactorVolume(), this.getReactorHeat());
			reactorRf += rfTransferred;
			this.setReactorHeat(StaticUtils.Energy.getTempFromVolumeAndRF(this.getReactorVolume(), reactorRf));
		}

		// If we have a temperature differential between environment and coolant system, move heat between them.
		tempDiff = this.getReactorHeat() - this.getCoolantTemperature();
		if (tempDiff > 0.01f)
		{
			float rfTransferred = tempDiff * this.reactorToCoolantSystemHeatTransferCoefficient;
			float reactorRf = StaticUtils.Energy.getRFFromVolumeAndTemp(this.getReactorVolume(), this.getReactorHeat());

			if (this.isPassivelyCooled())
			{
				rfTransferred *= passiveCoolingTransferEfficiency;
				this.generateEnergy(rfTransferred * passiveCoolingPowerEfficiency);
			}
			else
			{
				rfTransferred -= this.coolantContainer.onAbsorbHeat(rfTransferred);
				this.energyGeneratedLastTick = this.coolantContainer.getFluidVaporizedLastTick(); // Piggyback so we don't have useless stuff in the update packet
			}

			reactorRf -= rfTransferred;
			this.setReactorHeat(StaticUtils.Energy.getTempFromVolumeAndRF(this.getReactorVolume(), reactorRf));
		}

		// Do passive heat loss - this is always versus external environment
		tempDiff = this.getReactorHeat() - this.getPassiveCoolantTemperature();
		if (tempDiff > 0.000001f)
		{
			float rfLost = Math.max(1f, tempDiff * this.reactorHeatLossCoefficient); // Lose at least 1RF/t
			float reactorNewRf = Math.max(0f, StaticUtils.Energy.getRFFromVolumeAndTemp(this.getReactorVolume(), this.getReactorHeat()) - rfLost);
			this.setReactorHeat(StaticUtils.Energy.getTempFromVolumeAndRF(this.getReactorVolume(), reactorNewRf));
		}

		// Prevent cryogenics
		if (this.reactorHeat < 0f)
			this.setReactorHeat(0f);
		if (this.fuelHeat < 0f)
			this.setFuelHeat(0f);

		// Distribute available power
		int energyAvailable = (int) this.getEnergyStored();
		int energyRemaining = energyAvailable;
		if (this.attachedPowerTaps.size() > 0 && energyRemaining > 0)
		{
			// First, try to distribute fairly
			int splitEnergy = energyRemaining / this.attachedPowerTaps.size();
			for (TileEntityReactorPowerTap powerTap : this.attachedPowerTaps)
			{
				if (energyRemaining <= 0)
					break;
				if (!powerTap.isConnected())
					continue;

				energyRemaining -= splitEnergy - powerTap.onProvidePower(splitEnergy);
			}

			// Next, just hose out whatever we can, if we have any left
			if (energyRemaining > 0)
				for (TileEntityReactorPowerTap powerTap : this.attachedPowerTaps)
				{
					if (energyRemaining <= 0)
						break;
					if (!powerTap.isConnected())
						continue;

					energyRemaining = powerTap.onProvidePower(energyRemaining);
				}
		}

		if (energyAvailable != energyRemaining)
			this.reduceStoredEnergy(energyAvailable - energyRemaining);

		// Send updates periodically
		this.ticksSinceLastUpdate++;
		if (this.ticksSinceLastUpdate >= ticksBetweenUpdates)
		{
			this.ticksSinceLastUpdate = 0;
			this.sendTickUpdate();
		}

		// TODO: Overload/overheat

		// Update any connected tickables
		for (ITickableMultiblockPart tickable : this.attachedTickables)
		{
			tickable.onMultiblockServerTick();
		}

		if (this.attachedGlass.size() > 0 && this.fuelContainer.shouldUpdate())
			this.markReferenceCoordForUpdate();

		return oldHeat != this.getReactorHeat() || oldEnergy != this.getEnergyStored();
	}

	public void setEnergyStored(float oldEnergy)
	{
		this.energyStored = oldEnergy;
		if (this.energyStored < 0.0 || Float.isNaN(this.energyStored))
			this.energyStored = 0.0f;
		else if (this.energyStored > maxEnergyStored)
			this.energyStored = maxEnergyStored;
	}

	/**
	 * Generate energy, internally. Will be multiplied by the BR Setting
	 * powerProductionMultiplier
	 *
	 * @param newEnergy Base, unmultiplied energy to generate
	 */
	protected void generateEnergy(float newEnergy)
	{
		newEnergy = newEnergy * BigReactors.powerProductionMultiplier * BigReactors.reactorPowerProductionMultiplier;
		this.energyGeneratedLastTick += newEnergy;
		this.addStoredEnergy(newEnergy);
	}

	/**
	 * Add some energy to the internal storage buffer. Will not increase the
	 * buffer above the maximum or reduce it below 0.
	 *
	 * @param newEnergy
	 */
	protected void addStoredEnergy(float newEnergy)
	{
		if (Float.isNaN(newEnergy))
			return;

		this.energyStored += newEnergy;
		if (this.energyStored > maxEnergyStored)
			this.energyStored = maxEnergyStored;
		if (-0.00001f < this.energyStored && this.energyStored < 0.00001f)
			// Clamp to zero
			this.energyStored = 0f;
	}

	/**
	 * Remove some energy from the internal storage buffer. Will not reduce the
	 * buffer below 0.
	 *
	 * @param energy Amount by which the buffer should be reduced.
	 */
	protected void reduceStoredEnergy(float energy)
	{
		this.addStoredEnergy(-1f * energy);
	}

	@Override
	public void setActive(boolean act)
	{
		if (act == this.active)
			return;
		this.active = act;

		for (IMultiblockPart part : this.connectedParts)
		{
			if (this.active)
				part.onMachineActivated();
			else
				part.onMachineDeactivated();
		}

		if (this.worldObj.isRemote)
			// Force controllers to re-render on client
			for (IMultiblockPart part : this.attachedControllers)
			{
				this.worldObj.markBlockForUpdate(part.xCoord, part.yCoord, part.zCoord);
			}
		else
			this.markReferenceCoordForUpdate();
	}

	protected void addReactorHeat(float newCasingHeat)
	{
		if (Float.isNaN(newCasingHeat))
			return;

		this.reactorHeat += newCasingHeat;
		// Clamp to zero to prevent floating point issues
		if (-0.00001f < this.reactorHeat && this.reactorHeat < 0.00001f)
			this.reactorHeat = 0.0f;
	}

	public float getReactorHeat()
	{
		return this.reactorHeat;
	}

	public void setReactorHeat(float newHeat)
	{
		if (Float.isNaN(newHeat))
			this.reactorHeat = 0.0f;
		else
			this.reactorHeat = newHeat;
	}

	protected void addFuelHeat(float additionalHeat)
	{
		if (Float.isNaN(additionalHeat))
			return;

		this.fuelHeat += additionalHeat;
		if (-0.00001f < this.fuelHeat & this.fuelHeat < 0.00001f)
			this.fuelHeat = 0f;
	}

	public float getFuelHeat()
	{
		return this.fuelHeat;
	}

	public void setFuelHeat(float newFuelHeat)
	{
		if (Float.isNaN(newFuelHeat))
			this.fuelHeat = 0f;
		else
			this.fuelHeat = newFuelHeat;
	}

	@Override
	public int getFuelRodCount()
	{
		return this.attachedControlRods.size();
	}

	// Static validation helpers
	// Water, air, and metal blocks
	@Override
	protected void isBlockGoodForInterior(World world, int x, int y, int z) throws MultiblockValidationException
	{
		if (world.isAirBlock(x, y, z))
			return;

		Material material = world.getBlock(x, y, z).getMaterial();
		if (material == Material.water)
			return;

		Block block = world.getBlock(x, y, z);
		if (block == Blocks.iron_block || block == Blocks.gold_block || block == Blocks.diamond_block || block == Blocks.emerald_block)
			return;

		// Permit registered moderator blocks
		int metadata = world.getBlockMetadata(x, y, z);

		if (ReactorInterior.getBlockData(ItemHelper.oreProxy.getOreName(new ItemStack(block, 1, metadata))) != null)
			return;

		// Permit TE fluids
		if (block != null)
		{
			if (block instanceof IFluidBlock)
			{
				Fluid fluid = ((IFluidBlock) block).getFluid();

				    
				if (fluid == null)
					throw new MultiblockValidationException(String.format("%d, %d, %d - Null fluid found, not valid for the reactor's interior", x, y, z));
				    

				String fluidName = fluid.getName();
				if (ReactorInterior.getFluidData(fluidName) != null)
					return;

				throw new MultiblockValidationException(String.format("%d, %d, %d - The fluid %s is not valid for the reactor's interior", x, y, z, fluidName));
			}
			else
				throw new MultiblockValidationException(String.format("%d, %d, %d - %s is not valid for the reactor's interior", x, y, z, block.getLocalizedName()));
		}
		else
			throw new MultiblockValidationException(String.format("%d, %d, %d - Null block found, not valid for the reactor's interior", x, y, z));
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		data.setBoolean("reactorActive", this.active);
		data.setFloat("heat", this.reactorHeat);
		data.setFloat("fuelHeat", this.fuelHeat);
		data.setFloat("storedEnergy", this.energyStored);
		data.setInteger("wasteEjection2", this.wasteEjection.ordinal());
		data.setTag("fuelContainer", this.fuelContainer.writeToNBT(new NBTTagCompound()));
		data.setTag("radiation", this.radiationHelper.writeToNBT(new NBTTagCompound()));
		data.setTag("coolantContainer", this.coolantContainer.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		if (data.hasKey("reactorActive"))
			this.setActive(data.getBoolean("reactorActive"));

		if (data.hasKey("heat"))
			this.setReactorHeat(Math.max(this.getReactorHeat(), data.getFloat("heat")));

		if (data.hasKey("storedEnergy"))
			this.setEnergyStored(Math.max(this.getEnergyStored(), data.getFloat("storedEnergy")));

		if (data.hasKey("wasteEjection2"))
			this.wasteEjection = s_EjectionSettings[data.getInteger("wasteEjection2")];

		if (data.hasKey("fuelHeat"))
			this.setFuelHeat(data.getFloat("fuelHeat"));

		if (data.hasKey("fuelContainer"))
			this.fuelContainer.readFromNBT(data.getCompoundTag("fuelContainer"));

		if (data.hasKey("radiation"))
			this.radiationHelper.readFromNBT(data.getCompoundTag("radiation"));

		if (data.hasKey("coolantContainer"))
			this.coolantContainer.readFromNBT(data.getCompoundTag("coolantContainer"));
	}

	@Override
	protected int getMinimumNumberOfBlocksForAssembledMachine()
	{
		// Hollow cube.
		return 26;
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data)
	{
		this.writeToNBT(data);
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data)
	{
		this.readFromNBT(data);
		this.onFuelStatusChanged();
	}

	// Network & Storage methods
	/*
	 * Serialize a reactor into a given Byte buffer
	 * @param buf The byte buffer to serialize into
	 */
	public void serialize(ByteBuf buf)
	{
		int fuelTypeID, wasteTypeID, coolantTypeID, vaporTypeID;

		// Marshal fluid types into integers
		Fluid coolantType, vaporType;
		coolantType = this.coolantContainer.getCoolantType();
		vaporType = this.coolantContainer.getVaporType();
		coolantTypeID = coolantType == null ? -1 : coolantType.getID();
		vaporTypeID = vaporType == null ? -1 : vaporType.getID();

		// Basic data
		buf.writeBoolean(this.active);
		buf.writeFloat(this.reactorHeat);
		buf.writeFloat(this.fuelHeat);
		buf.writeFloat(this.energyStored);
		buf.writeFloat(this.radiationHelper.getFertility());

		// Statistics
		buf.writeFloat(this.energyGeneratedLastTick);
		buf.writeFloat(this.fuelConsumedLastTick);

		// Coolant data
		buf.writeInt(coolantTypeID);
		buf.writeInt(this.coolantContainer.getCoolantAmount());
		buf.writeInt(vaporTypeID);
		buf.writeInt(this.coolantContainer.getVaporAmount());

		this.fuelContainer.serialize(buf);
	}

	/*
	 * Deserialize a reactor's data from a given Byte buffer
	 * @param buf The byte buffer containing reactor data
	 */
	public void deserialize(ByteBuf buf)
	{
		// Basic data
		this.setActive(buf.readBoolean());
		this.setReactorHeat(buf.readFloat());
		this.setFuelHeat(buf.readFloat());
		this.setEnergyStored(buf.readFloat());
		this.radiationHelper.setFertility(buf.readFloat());

		// Statistics
		this.setEnergyGeneratedLastTick(buf.readFloat());
		this.setFuelConsumedLastTick(buf.readFloat());

		// Coolant data
		int coolantTypeID = buf.readInt();
		int coolantAmt = buf.readInt();
		int vaporTypeID = buf.readInt();
		int vaporAmt = buf.readInt();

		// Fuel & waste data
		this.fuelContainer.deserialize(buf);

		if (coolantTypeID == -1)
			this.coolantContainer.emptyCoolant();
		else
			this.coolantContainer.setCoolant(new FluidStack(FluidRegistry.getFluid(coolantTypeID), coolantAmt));

		if (vaporTypeID == -1)
			this.coolantContainer.emptyVapor();
		else
			this.coolantContainer.setVapor(new FluidStack(FluidRegistry.getFluid(vaporTypeID), vaporAmt));

	}

	protected IMessage getUpdatePacket()
	{
		return new ReactorUpdateMessage(this);
	}

	/**
	 * Sends a full state update to a player.
	 */
	protected void sendIndividualUpdate(EntityPlayer player)
	{
		if (this.worldObj.isRemote)
			return;

		CommonPacketHandler.INSTANCE.sendTo(this.getUpdatePacket(), (EntityPlayerMP) player);
	}

	/**
	 * Send an update to any clients with GUIs open
	 */
	protected void sendTickUpdate()
	{
		if (this.worldObj.isRemote)
			return;
		if (this.updatePlayers.size() <= 0)
			return;

		for (EntityPlayer player : this.updatePlayers)
		{
			CommonPacketHandler.INSTANCE.sendTo(this.getUpdatePacket(), (EntityPlayerMP) player);
		}
	}

	/**
	 * Attempt to distribute a stack of ingots to a given access port, sensitive
	 * to the amount and type of ingots already in it.
	 *
	 * @param port               The port to which we're distributing ingots.
	 * @param itemsToDistribute  The stack of ingots to distribute. Will be modified during the
	 *                           operation and may be returned with stack size 0.
	 * @param distributeToInputs Should we try to send ingots to input ports?
	 * @return The number of waste items distributed, i.e. the differential in
	 * stack size for wasteToDistribute.
	 */
	private int tryDistributeItems(TileEntityReactorAccessPort port, ItemStack itemsToDistribute, boolean distributeToInputs)
	{
		ItemStack existingStack = port.getStackInSlot(TileEntityReactorAccessPort.SLOT_OUTLET);
		int initialWasteAmount = itemsToDistribute.stackSize;
		if (!port.isInlet() || distributeToInputs || this.attachedAccessPorts.size() < 2)
		{
			// Dump waste preferentially to outlets, unless we only have one access port
			if (existingStack == null)
			{
				if (itemsToDistribute.stackSize > port.getInventoryStackLimit())
				{
					ItemStack newStack = itemsToDistribute.splitStack(port.getInventoryStackLimit());
					port.setInventorySlotContents(TileEntityReactorAccessPort.SLOT_OUTLET, newStack);
				}
				else
				{
					port.setInventorySlotContents(TileEntityReactorAccessPort.SLOT_OUTLET, itemsToDistribute.copy());
					itemsToDistribute.stackSize = 0;
				}
			}
			else if (existingStack.isItemEqual(itemsToDistribute))
				if (existingStack.stackSize + itemsToDistribute.stackSize <= existingStack.getMaxStackSize())
				{
					existingStack.stackSize += itemsToDistribute.stackSize;
					itemsToDistribute.stackSize = 0;
				}
				else
				{
					int amt = existingStack.getMaxStackSize() - existingStack.stackSize;
					itemsToDistribute.stackSize -= existingStack.getMaxStackSize() - existingStack.stackSize;
					existingStack.stackSize += amt;
				}

			port.onItemsReceived();
		}

		return initialWasteAmount - itemsToDistribute.stackSize;
	}

	@Override
	protected void onAssimilated(MultiblockControllerBase otherMachine)
	{
		this.attachedPowerTaps.clear();
		this.attachedTickables.clear();
		this.attachedAccessPorts.clear();
		this.attachedControllers.clear();
		this.attachedControlRods.clear();
		this.currentFuelRod = null;
	}

	@Override
	protected void onAssimilate(MultiblockControllerBase otherMachine)
	{
		if (!(otherMachine instanceof MultiblockReactor))
		{
			BRLog.warning("[%s] Reactor @ %s is attempting to assimilate a non-Reactor machine! That machine's data will be lost!", this.worldObj.isRemote ? "CLIENT" : "SERVER", this.getReferenceCoord());
			return;
		}

		MultiblockReactor otherReactor = (MultiblockReactor) otherMachine;

		if (otherReactor.reactorHeat > this.reactorHeat)
			this.setReactorHeat(otherReactor.reactorHeat);
		if (otherReactor.fuelHeat > this.fuelHeat)
			this.setFuelHeat(otherReactor.fuelHeat);

		if (otherReactor.getEnergyStored() > this.getEnergyStored())
			this.setEnergyStored(otherReactor.getEnergyStored());

		this.fuelContainer.merge(otherReactor.fuelContainer);
		this.radiationHelper.merge(otherReactor.radiationHelper);
		this.coolantContainer.merge(otherReactor.coolantContainer);
	}

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockPart part, NBTTagCompound data)
	{
		this.readFromNBT(data);
	}

	public float getEnergyStored()
	{
		return this.energyStored;
	}

	/**
	 * Directly set the waste ejection setting. Will dispatch network updates
	 * from server to interested clients.
	 *
	 * @param newSetting The new waste ejection setting.
	 */
	public void setWasteEjection(WasteEjectionSetting newSetting)
	{
		if (this.wasteEjection != newSetting)
		{
			this.wasteEjection = newSetting;

			if (!this.worldObj.isRemote)
			{
				this.markReferenceCoordDirty();

				if (this.updatePlayers.size() > 0)
					for (EntityPlayer player : this.updatePlayers)
					{
						CommonPacketHandler.INSTANCE.sendTo(new ReactorUpdateWasteEjectionMessage(this), (EntityPlayerMP) player);
					}
			}
		}
	}

	public WasteEjectionSetting getWasteEjection()
	{
		return this.wasteEjection;
	}

	protected void refuel()
	{
		// For now, we only need to check fuel ports when we have more space than can accomodate 1 ingot
		if (this.fuelContainer.getRemainingSpace() < Reactants.standardSolidReactantAmount)
			return;

		int amtAdded = 0;

		// Loop: Consume input reactants from all ports
		for (TileEntityReactorAccessPort port : this.attachedAccessPorts)
		{
			if (this.fuelContainer.getRemainingSpace() <= 0)
				break;

			if (!port.isConnected())
				continue;

			// See what type of reactant the port contains; if none, skip it.
			String portReactantType = port.getInputReactantType();
			int portReactantAmount = port.getInputReactantAmount();
			if (portReactantType == null || portReactantAmount <= 0)
				continue;

			if (!Reactants.isFuel(portReactantType))
				continue;

			// HACK; TEMPORARY
			// Alias blutonium to yellorium temporarily, until mixed fuels are implemented
			if (portReactantType.equals(StandardReactants.blutonium))
				portReactantType = StandardReactants.yellorium;

			// How much fuel can we actually add from this type of reactant?
			int amountToAdd = this.fuelContainer.addFuel(portReactantType, portReactantAmount, false);
			if (amountToAdd <= 0)
				continue;

			    
			if (amountToAdd < portReactantAmount)
			{
				ItemStack portReactantStack = port.getStackInSlot(TileEntityReactorAccessPort.SLOT_INLET);
				SourceProductMapping mapping = Reactants.getSolidToReactant(portReactantStack);
				int validSize = MathHelper.floor_double((double) amountToAdd / (double) mapping.getProductAmount());
				portReactantAmount = mapping.getProductAmount(validSize);
				if (portReactantAmount <= 0)
					continue;
				amountToAdd = this.fuelContainer.addFuel(portReactantType, portReactantAmount, false);
				if (amountToAdd <= 0)
					continue;
			}
			    

			int portCanAdd = port.consumeReactantItem(amountToAdd);
			if (portCanAdd <= 0)
				continue;

			amtAdded = this.fuelContainer.addFuel(portReactantType, portReactantAmount, true);
		}

		if (amtAdded > 0)
		{
			this.markReferenceCoordForUpdate();
			this.markReferenceCoordDirty();
		}
	}

	/**
	 * Attempt to eject waste contained in the reactor
	 *
	 * @param dumpAll     If true, any waste remaining after ejection will be discarded.
	 * @param destination If set, waste will only be ejected to ports with coordinates
	 *                    matching this one.
	 */
	public void ejectWaste(boolean dumpAll, CoordTriplet destination)
	{
		// For now, we can optimize by only running this when we have enough waste to product an ingot
		int amtEjected = 0;

		String wasteReactantType = this.fuelContainer.getWasteType();
		if (wasteReactantType == null)
			return;

		int minimumReactantAmount = Reactants.getMinimumReactantToProduceSolid(wasteReactantType);
		if (this.fuelContainer.getWasteAmount() >= minimumReactantAmount)
		{

			for (TileEntityReactorAccessPort port : this.attachedAccessPorts)
			{
				if (this.fuelContainer.getWasteAmount() < minimumReactantAmount)
					continue;

				if (!port.isConnected())
					continue;
				if (destination != null && !destination.equals(port.xCoord, port.yCoord, port.zCoord))
					continue;

				// First time through, we eject only to outlet ports
				if (destination == null && !port.isInlet())
				{
					int reactantEjected = port.emitReactant(wasteReactantType, this.fuelContainer.getWasteAmount());
					this.fuelContainer.dumpWaste(reactantEjected);
					amtEjected += reactantEjected;
				}
			}

			if (destination == null && this.fuelContainer.getWasteAmount() > minimumReactantAmount)
				// Loop a second time when destination is null and we still have waste
				for (TileEntityReactorAccessPort port : this.attachedAccessPorts)
				{
					if (this.fuelContainer.getWasteAmount() < minimumReactantAmount)
						continue;

					if (!port.isConnected())
						continue;
					int reactantEjected = port.emitReactant(wasteReactantType, this.fuelContainer.getWasteAmount());
					this.fuelContainer.dumpWaste(reactantEjected);
					amtEjected += reactantEjected;
				}
		}

		if (dumpAll)
		{
			amtEjected += this.fuelContainer.getWasteAmount();
			this.fuelContainer.setWaste(null);
		}

		if (amtEjected > 0)
		{
			this.markReferenceCoordForUpdate();
			this.markReferenceCoordDirty();
		}
	}

	/**
	 * Eject fuel contained in the reactor.
	 *
	 * @param dumpAll     If true, any remaining fuel will simply be lost.
	 * @param destination If not null, then fuel will only be distributed to a port
	 *                    matching these coordinates.
	 */
	public void ejectFuel(boolean dumpAll, CoordTriplet destination)
	{
		// For now, we can optimize by only running this when we have enough waste to product an ingot
		int amtEjected = 0;

		String fuelReactantType = this.fuelContainer.getFuelType();
		if (fuelReactantType == null)
			return;

		int minimumReactantAmount = Reactants.getMinimumReactantToProduceSolid(fuelReactantType);
		if (this.fuelContainer.getFuelAmount() >= minimumReactantAmount)
			for (TileEntityReactorAccessPort port : this.attachedAccessPorts)
			{
				if (this.fuelContainer.getFuelAmount() < minimumReactantAmount)
					continue;

				if (!port.isConnected())
					continue;
				if (destination != null && !destination.equals(port.xCoord, port.yCoord, port.zCoord))
					continue;

				int reactantEjected = port.emitReactant(fuelReactantType, this.fuelContainer.getFuelAmount());
				this.fuelContainer.dumpFuel(reactantEjected);
				amtEjected += reactantEjected;
			}

		if (dumpAll)
		{
			amtEjected += this.fuelContainer.getFuelAmount();
			this.fuelContainer.setFuel(null);
		}

		if (amtEjected > 0)
		{
			this.markReferenceCoordForUpdate();
			this.markReferenceCoordDirty();
		}
	}

	@Override
	protected void onMachineAssembled()
	{
		this.recalculateDerivedValues();
	}

	@Override
	protected void onMachineRestored()
	{
		this.recalculateDerivedValues();
	}

	@Override
	protected void onMachinePaused()
	{
	}

	@Override
	protected void onMachineDisassembled()
	{
		this.active = false;
	}

	private void recalculateDerivedValues()
	{
		// Recalculate size of fuel/waste tank via fuel rods
		CoordTriplet minCoord, maxCoord;
		minCoord = this.getMinimumCoord();
		maxCoord = this.getMaximumCoord();

		this.fuelContainer.setCapacity(this.attachedFuelRods.size() * FuelCapacityPerFuelRod);

		// Calculate derived stats

		// Calculate heat transfer based on fuel rod environment
		this.fuelToReactorHeatTransferCoefficient = 0f;
		for (TileEntityReactorFuelRod fuelRod : this.attachedFuelRods)
		{
			this.fuelToReactorHeatTransferCoefficient += fuelRod.getHeatTransferRate();
		}

		// Calculate heat transfer to coolant system based on reactor interior surface area.
		// This is pretty simple to start with - surface area of the rectangular prism defining the interior.
		int xSize = maxCoord.x - minCoord.x - 1;
		int ySize = maxCoord.y - minCoord.y - 1;
		int zSize = maxCoord.z - minCoord.z - 1;

		int surfaceArea = 2 * (xSize * ySize + xSize * zSize + ySize * zSize);

		this.reactorToCoolantSystemHeatTransferCoefficient = IHeatEntity.conductivityIron * surfaceArea;

		// Calculate passive heat loss.
		// Get external surface area
		xSize += 2;
		ySize += 2;
		zSize += 2;

		surfaceArea = 2 * (xSize * ySize + xSize * zSize + ySize * zSize);
		this.reactorHeatLossCoefficient = reactorHeatLossConductivity * surfaceArea;

		if (this.worldObj.isRemote)
			// Make sure our fuel rods re-render
			this.onFuelStatusChanged();
		else
			// Force an update of the client's multiblock information
			this.markReferenceCoordForUpdate();

		this.calculateReactorVolume();

		if (this.attachedCoolantPorts.size() > 0)
		{
			int outerVolume = StaticUtils.ExtraMath.Volume(minCoord, maxCoord) - this.reactorVolume;
			this.coolantContainer.setCapacity(Math.max(0, Math.min(50000, outerVolume * 100)));
		}
		else
			this.coolantContainer.setCapacity(0);
	}

	@Override
	protected int getMaximumXSize()
	{
		return BigReactors.maximumReactorSize;
	}

	@Override
	protected int getMaximumZSize()
	{
		return BigReactors.maximumReactorSize;
	}

	@Override
	protected int getMaximumYSize()
	{
		return BigReactors.maximumReactorHeight;
	}

	/**
	 * Used to update the UI
	 */
	public void setEnergyGeneratedLastTick(float energyGeneratedLastTick)
	{
		this.energyGeneratedLastTick = energyGeneratedLastTick;
	}

	/**
	 * UI Helper
	 */
	public float getEnergyGeneratedLastTick()
	{
		return this.energyGeneratedLastTick;
	}

	/**
	 * Used to update the UI
	 */
	public void setFuelConsumedLastTick(float fuelConsumed)
	{
		this.fuelConsumedLastTick = fuelConsumed;
	}

	/**
	 * UI Helper
	 */
	public float getFuelConsumedLastTick()
	{
		return this.fuelConsumedLastTick;
	}

	/**
	 * UI Helper
	 *
	 * @return Percentile fuel richness (fuel/fuel+waste), or 0 if all control
	 * rods are empty
	 */
	public float getFuelRichness()
	{
		int amtFuel, amtWaste;
		amtFuel = this.fuelContainer.getFuelAmount();
		amtWaste = this.fuelContainer.getWasteAmount();

		if (amtFuel + amtWaste <= 0f)
			return 0f;
		else
			return (float) amtFuel / (float) (amtFuel + amtWaste);
	}

	// IEnergyProvider
	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		int amtRemoved = (int) Math.min(maxExtract, this.energyStored);
		if (!simulate)
			this.reduceStoredEnergy(amtRemoved);
		return amtRemoved;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return false;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return (int) this.energyStored;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return maxEnergyStored;
	}

	// Redstone helper
	public void setAllControlRodInsertionValues(int newValue)
	{
		if (this.assemblyState != AssemblyState.Assembled)
			return;

		for (TileEntityReactorControlRod cr : this.attachedControlRods)
		{
			if (cr.isConnected())
				cr.setControlRodInsertion((short) newValue);
		}
	}

	public void changeAllControlRodInsertionValues(short delta)
	{
		if (this.assemblyState != AssemblyState.Assembled)
			return;

		for (TileEntityReactorControlRod cr : this.attachedControlRods)
		{
			if (cr.isConnected())
				cr.setControlRodInsertion((short) (cr.getControlRodInsertion() + delta));
		}
	}

	public CoordTriplet[] getControlRodLocations()
	{
		CoordTriplet[] coords = new CoordTriplet[this.attachedControlRods.size()];
		int i = 0;
		for (TileEntityReactorControlRod cr : this.attachedControlRods)
		{
			coords[i++] = cr.getWorldLocation();
		}
		return coords;
	}

	@Override
	public int getFuelAmount()
	{
		return this.fuelContainer.getFuelAmount();
	}

	@Override
	public int getWasteAmount()
	{
		return this.fuelContainer.getWasteAmount();
	}

	public String getFuelType()
	{
		return this.fuelContainer.getFuelType();
	}

	public String getWasteType()
	{
		return this.fuelContainer.getWasteType();
	}

	public int getEnergyStoredPercentage()
	{
		return (int) (this.energyStored / MultiblockReactor.maxEnergyStored * 100f);
	}

	@Override
	public int getCapacity()
	{
		if (this.worldObj.isRemote && this.assemblyState != AssemblyState.Assembled)
			// Estimate capacity
			return this.attachedFuelRods.size() * FuelCapacityPerFuelRod;

		return this.fuelContainer.getCapacity();
	}

	public float getFuelFertility()
	{
		return this.radiationHelper.getFertilityModifier();
	}

	// Coolant subsystem
	public CoolantContainer getCoolantContainer()
	{
		return this.coolantContainer;
	}

	protected float getPassiveCoolantTemperature()
	{
		return IHeatEntity.ambientHeat;
	}

	protected float getCoolantTemperature()
	{
		if (this.isPassivelyCooled())
			return this.getPassiveCoolantTemperature();
		else
			return this.coolantContainer.getCoolantTemperature(this.getReactorHeat());
	}

	public boolean isPassivelyCooled()
	{
		return this.coolantContainer == null || this.coolantContainer.getCapacity() <= 0;
	}

	protected int getReactorVolume()
	{
		return this.reactorVolume;
	}

	protected void calculateReactorVolume()
	{
		CoordTriplet minInteriorCoord = this.getMinimumCoord();
		minInteriorCoord.x += 1;
		minInteriorCoord.y += 1;
		minInteriorCoord.z += 1;

		CoordTriplet maxInteriorCoord = this.getMaximumCoord();
		maxInteriorCoord.x -= 1;
		maxInteriorCoord.y -= 1;
		maxInteriorCoord.z -= 1;

		this.reactorVolume = StaticUtils.ExtraMath.Volume(minInteriorCoord, maxInteriorCoord);
	}

	// Client-only
	protected void onFuelStatusChanged()
	{
		if (this.worldObj.isRemote)
			// On the client, re-render all the fuel rod blocks when the fuel status changes
			for (TileEntityReactorFuelRod fuelRod : this.attachedFuelRods)
			{
				this.worldObj.markBlockForUpdate(fuelRod.xCoord, fuelRod.yCoord, fuelRod.zCoord);
			}
	}

	private static final FluidTankInfo[] emptyTankInfo = new FluidTankInfo[0];

	@Override
	public FluidTankInfo[] getTankInfo()
	{
		if (this.isPassivelyCooled())
			return emptyTankInfo;

		return this.coolantContainer.getTankInfo(-1);
	}

	@Override
	protected void markReferenceCoordForUpdate()
	{
		CoordTriplet rc = this.getReferenceCoord();
		if (this.worldObj != null && rc != null)
			this.worldObj.markBlockForUpdate(rc.x, rc.y, rc.z);
	}

	@Override
	protected void markReferenceCoordDirty()
	{
		if (this.worldObj == null || this.worldObj.isRemote)
			return;

		CoordTriplet referenceCoord = this.getReferenceCoord();
		if (referenceCoord == null)
			return;

		TileEntity saveTe = this.worldObj.getTileEntity(referenceCoord.x, referenceCoord.y, referenceCoord.z);
		this.worldObj.markTileEntityChunkModified(referenceCoord.x, referenceCoord.y, referenceCoord.z, saveTe);
	}

	@Override
	public boolean getActive()
	{
		return this.active;
	}

	public String getDebugInfo()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Assembled: ").append(Boolean.toString(this.isAssembled())).append("\n");
		sb.append("Attached Blocks: ").append(Integer.toString(this.connectedParts.size())).append("\n");
		if (this.getLastValidationException() != null)
			sb.append("Validation Exception:\n").append(this.getLastValidationException().getMessage()).append("\n");

		if (this.isAssembled())
		{
			sb.append("\nActive: ").append(Boolean.toString(this.getActive()));
			sb.append("\nStored Energy: ").append(Float.toString(this.getEnergyStored()));
			sb.append("\nCasing Heat: ").append(Float.toString(this.getReactorHeat()));
			sb.append("\nFuel Heat: ").append(Float.toString(this.getFuelHeat()));
			sb.append("\n\nReactant Tanks:\n");
			sb.append(this.fuelContainer.getDebugInfo());
			sb.append("\n\nActively Cooled: ").append(Boolean.toString(!this.isPassivelyCooled()));
			if (!this.isPassivelyCooled())
			{
				sb.append("\n\nCoolant Tanks:\n");
				sb.append(this.coolantContainer.getDebugInfo());
			}
		}

		return sb.toString();
	}
}
