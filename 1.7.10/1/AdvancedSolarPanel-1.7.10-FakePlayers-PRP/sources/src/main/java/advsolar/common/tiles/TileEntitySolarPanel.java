package advsolar.common.tiles;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

public class TileEntitySolarPanel extends TileEntityBase implements IEnergyTile, IWrenchable, IEnergySource, IInventory, INetworkDataProvider, INetworkUpdateListener
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

	    
	public boolean isGuiOpened;
	    

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
		this.lastX = super.xCoord;
		this.lastY = super.yCoord;
		this.lastZ = super.zCoord;
		this.machineTire = Integer.MAX_VALUE;
	}

	@Override
	public void validate()
	{
		super.validate();
		if (!this.isInvalid() && super.worldObj.blockExists(super.xCoord, super.yCoord, super.zCoord))
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
		if (!super.worldObj.isRemote)
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
		if (!super.worldObj.isRemote && this.addedToEnergyNet)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			this.addedToEnergyNet = false;
		}

		this.loaded = false;
	}

	public void intialize()
	{
		this.wetBiome = super.worldObj	.getWorldChunkManager()
										.getBiomeGenAt(super.xCoord, super.zCoord)
										.getIntRainfall() > 0;
		this.noSunWorld = super.worldObj.provider.hasNoSky;
		this.updateVisibility();
		this.initialized = true;
		if (!this.addedToEnergyNet)
			this.onLoaded();

	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (!this.initialized && super.worldObj != null)
			this.intialize();

		if (!super.worldObj.isRemote)
		{
			if (this.lastX != super.xCoord || this.lastZ != super.zCoord || this.lastY != super.yCoord)
			{
				this.lastX = super.xCoord;
				this.lastY = super.yCoord;
				this.lastZ = super.zCoord;
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
				if (this.chargeSlots[i] != null && this.chargeSlots[i].getItem() instanceof IElectricItem && this.storage > 0)
				{
					sentPacket = ElectricItem.manager.charge(this.chargeSlots[i], this.storage, Integer.MAX_VALUE, false, false);
					if (sentPacket > 0.0D)
						needInvUpdate = true;

					this.storage = (int) (this.storage - sentPacket);
				}

			if (needInvUpdate)
				super.markDirty();

		}
	}

	public int gainFuel()
	{
		if (this.ticker++ % this.tickRate() == 0)
			this.updateVisibility();

		if (this.sunIsUp && this.skyIsVisible)
		{
			this.generating = 0 + this.genDay;
			return this.generating;
		}
		else if (this.skyIsVisible)
		{
			this.generating = 0 + this.genNight;
			return this.generating;
		}
		else
		{
			this.generating = 0;
			return this.generating;
		}
	}

	public void updateVisibility()
	{
		Boolean rainWeather = Boolean.valueOf(this.wetBiome && (super.worldObj.isRaining() || super.worldObj.isThundering()));
		if (super.worldObj.isDaytime() && !rainWeather.booleanValue())
			this.sunIsUp = true;
		else
			this.sunIsUp = false;

		if (super.worldObj.canBlockSeeTheSky(super.xCoord, super.yCoord + 1, super.zCoord) && !this.noSunWorld)
			this.skyIsVisible = true;
		else
			this.skyIsVisible = false;

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
			if (this.chargeSlots[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.chargeSlots[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
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
		return player.getDistance(super.xCoord + 0.5, super.yCoord + 0.5, super.zCoord + 0.5) <= 64 && this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this;
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
		return new ItemStack(super.worldObj.getBlock(super.xCoord, super.yCoord, super.zCoord), 1, super.worldObj.getBlockMetadata(super.xCoord, super.yCoord, super.zCoord));
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
			else
			{
				ItemStack itemstack1 = this.chargeSlots[i].splitStack(j);
				if (this.chargeSlots[i].stackSize == 0)
					this.chargeSlots[i] = null;

				return itemstack1;
			}
		}
		else
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
		else
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
		return Math.min(this.production, this.storage);
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.storage = (int) (this.storage - amount);
	}

	@Override
	public int getSourceTier()
	{
		return this.machineTire;
	}
}
