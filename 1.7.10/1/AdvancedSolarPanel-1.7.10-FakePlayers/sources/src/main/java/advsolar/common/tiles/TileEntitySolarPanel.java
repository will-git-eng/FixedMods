package advsolar.common.tiles;

import advsolar.common.container.ContainerAdvSolarPanel;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkUpdateListener;
import ic2.api.tile.IWrenchable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TileEntitySolarPanel extends TileEntityBase
		implements IEnergyTile, IWrenchable, IEnergySource, IInventory, INetworkDataProvider, INetworkUpdateListener
{
	public static Random randomizer = new Random();
	public int ticker;
	public int generating;
	public int genDay;
	public int genNight;
	public boolean initialized;
	public boolean sunIsUp;
	public boolean skyIsVisible;
	private short facing = 2;
	private boolean noSunWorld;
	private boolean wetBiome;
	private int machineTire;
	public boolean addedToEnergyNet;
	private boolean created = false;
	private ItemStack[] chargeSlots;
	public int fuel;
	private int lastX;
	private int lastY;
	private int lastZ;
	public int storage;
	private int solarType;
	public String panelName;
	public int production;
	public int maxStorage;
	public boolean loaded = false;
	private static List<String> fields = Arrays.asList(new String[0]);

	public TileEntitySolarPanel(String gName, int typeSolar, int gDay, int gNight, int gOutput, int gmaxStorage)
	{
		this.solarType = typeSolar;
		this.genDay = gDay;
		this.genNight = gNight;
		this.storage = 0;
		this.panelName = gName;
		this.sunIsUp = false;
		this.skyIsVisible = false;
		this.maxStorage = gmaxStorage;
		this.chargeSlots = new ItemStack[4];
		this.initialized = false;
		this.production = gOutput;
		this.ticker = randomizer.nextInt(this.tickRate());
		this.lastX = this.xCoord;
		this.lastY = this.yCoord;
		this.lastZ = this.zCoord;
		this.machineTire = Integer.MAX_VALUE;
	}

	@Override
	public void validate()
	{
		super.validate();
		if (!this.isInvalid() && this.worldObj.blockExists(this.xCoord, this.yCoord, this.zCoord))
			this.onLoaded();
	}

	@Override
	public void invalidate()
	{
		if (this.loaded)
			this.onUnloaded();

		super.invalidate();
	}

	public void onLoaded()
	{
		if (!this.worldObj.isRemote)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			this.addedToEnergyNet = true;
		}

		this.loaded = true;
	}

	@Override
	public void onChunkUnload()
	{
		if (this.loaded)
			this.onUnloaded();

		super.onChunkUnload();
	}

	public void onUnloaded()
	{
		if (!this.worldObj.isRemote && this.addedToEnergyNet)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			this.addedToEnergyNet = false;
		}

		this.loaded = false;
	}

	public void intialize()
	{
		this.wetBiome = this.worldObj.getWorldChunkManager().getBiomeGenAt(this.xCoord, this.zCoord).getIntRainfall() > 0;
		this.noSunWorld = this.worldObj.provider.hasNoSky;
		this.updateVisibility();
		this.initialized = true;
		if (!this.addedToEnergyNet)
			this.onLoaded();

	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (!this.initialized && this.worldObj != null)
			this.intialize();

		if (!this.worldObj.isRemote)
		{
			if (this.lastX != this.xCoord || this.lastZ != this.zCoord || this.lastY != this.yCoord)
			{
				this.lastX = this.xCoord;
				this.lastY = this.yCoord;
				this.lastZ = this.zCoord;
				this.onUnloaded();
				this.intialize();
			}

			this.gainFuel();
			if (this.generating > 0)
				if (this.storage + this.generating <= this.maxStorage)
					this.storage += this.generating;
				else
					this.storage = this.maxStorage;

			boolean needInvUpdate = false;
			double sentPacket = 0.0D;

			for (int i = 0; i < this.chargeSlots.length; ++i)
			{
				if (this.chargeSlots[i] != null && this.chargeSlots[i].getItem() instanceof IElectricItem && this.storage > 0)
				{
					sentPacket = ElectricItem.manager.charge(this.chargeSlots[i], (double) this.storage, Integer.MAX_VALUE, false, false);
					if (sentPacket > 0.0D)
						needInvUpdate = true;

					this.storage = (int) ((double) this.storage - sentPacket);
				}
			}

			if (needInvUpdate)
				this.markDirty();

		}
	}

	public int gainFuel()
	{
		if (this.ticker++ % this.tickRate() == 0)
			this.updateVisibility();

		if (this.sunIsUp && this.skyIsVisible)
		{
			this.generating = this.genDay;
			return this.generating;
		}
		if (this.skyIsVisible)
		{
			this.generating = this.genNight;
			return this.generating;
		}
		this.generating = 0;
		return this.generating;
	}

	public void updateVisibility()
	{
		Boolean rainWeather = this.wetBiome && (this.worldObj.isRaining() || this.worldObj.isThundering());
		this.sunIsUp = this.worldObj.isDaytime() && !rainWeather;

		this.skyIsVisible = this.worldObj.canBlockSeeTheSky(this.xCoord, this.yCoord + 1, this.zCoord) && !this.noSunWorld;

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.storage = nbttagcompound.getInteger("storage");
		this.lastX = nbttagcompound.getInteger("lastX");
		this.lastY = nbttagcompound.getInteger("lastY");
		this.lastZ = nbttagcompound.getInteger("lastZ");
		NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
		this.chargeSlots = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;
			if (j >= 0 && j < this.chargeSlots.length)
				this.chargeSlots[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		NBTTagList nbttaglist = new NBTTagList();
		nbttagcompound.setInteger("storage", this.storage);
		nbttagcompound.setInteger("lastX", this.lastX);
		nbttagcompound.setInteger("lastY", this.lastY);
		nbttagcompound.setInteger("lastZ", this.lastZ);

		for (int i = 0; i < this.chargeSlots.length; ++i)
		{
			if (this.chargeSlots[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.chargeSlots[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("Items", nbttaglist);
	}

	public boolean isAddedToEnergyNet()
	{
		return this.addedToEnergyNet;
	}

	public int getMaxEnergyOutput()
	{
		return this.production;
	}

	public int gaugeEnergyScaled(int i)
	{
		return this.storage * i / this.maxStorage;
	}

	public int gaugeFuelScaled(int i)
	{
		return i;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistance((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	public int tickRate()
	{
		return 128;
	}

	@Override
	public short getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(short facing)
	{
		this.facing = facing;
	}

	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityplayer, int i)
	{
		return false;
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public float getWrenchDropRate()
	{
		return 1.0F;
	}

	@Override
	public ItemStack getWrenchDrop(EntityPlayer entityPlayer)
	{
		return new ItemStack(this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord), 1, this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord));
	}

	public ItemStack[] getContents()
	{
		return this.chargeSlots;
	}

	@Override
	public int getSizeInventory()
	{
		return 4;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.chargeSlots[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.chargeSlots[i] != null)
		{
			if (this.chargeSlots[i].stackSize <= j)
			{
				ItemStack itemstack = this.chargeSlots[i];
				this.chargeSlots[i] = null;
				return itemstack;
			}
			ItemStack itemstack1 = this.chargeSlots[i].splitStack(j);
			if (this.chargeSlots[i].stackSize == 0)
				this.chargeSlots[i] = null;

			return itemstack1;
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.chargeSlots[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
			itemstack.stackSize = this.getInventoryStackLimit();

	}

	@Override
	public String getInventoryName()
	{
		return "Advanced Solar Panel";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	public Container getGuiContainer(InventoryPlayer inventoryplayer)
	{
		return new ContainerAdvSolarPanel(inventoryplayer, this);
	}

	public String getInvName()
	{
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		if (this.chargeSlots[var1] != null)
		{
			ItemStack var2 = this.chargeSlots[var1];
			this.chargeSlots[var1] = null;
			return var2;
		}
		return null;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
	}

	@Override
	public List<String> getNetworkedFields()
	{
		return fields;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return true;
	}

	@Override
	public double getOfferedEnergy()
	{
		return (double) Math.min(this.production, this.storage);
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.storage = (int) ((double) this.storage - amount);
	}

	@Override
	public int getSourceTier()
	{
		return this.machineTire;
	}
}
