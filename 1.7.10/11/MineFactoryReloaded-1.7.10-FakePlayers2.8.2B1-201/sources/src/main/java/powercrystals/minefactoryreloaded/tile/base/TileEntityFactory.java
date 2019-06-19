package powercrystals.minefactoryreloaded.tile.base;

import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;
import cofh.api.inventory.IInventoryConnection;
import cofh.api.tileentity.IPortableData;
import cofh.asm.relauncher.Strippable;
import cofh.lib.util.position.IRotateableTile;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import ru.will.git.minefactoryreloaded.ModUtils;
import com.google.common.base.Strings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.MineFactoryReloadedClient;
import powercrystals.minefactoryreloaded.core.HarvestAreaManager;
import powercrystals.minefactoryreloaded.core.IHarvestAreaContainer;
import powercrystals.minefactoryreloaded.core.MFRUtil;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryInventory;
import powercrystals.minefactoryreloaded.net.Packets;
import powercrystals.minefactoryreloaded.setup.Machine;

import java.util.Locale;

@Strippable({ "buildcraft.api.transport.IPipeConnection" })
public abstract class TileEntityFactory extends TileEntityBase
		implements IRotateableTile, IInventoryConnection, IPortableData, IHarvestAreaContainer, IPipeConnection
{
	private static final int[][] _textureSelection = { { 0, 1, 2, 3, 4, 5 }, { 0, 1, 2, 3, 4, 5 }, { 0, 1, 2, 3, 4, 5 }, { 0, 1, 3, 2, 5, 4 }, { 0, 1, 5, 4, 2, 3 }, { 0, 1, 4, 5, 3, 2 } };
	private ForgeDirection _forwardDirection;
	private boolean _canRotate = false;
	private boolean _manageFluids = false;
	private boolean _manageSolids = false;
	private boolean _isActive = false;
	private boolean _prevActive;
	protected byte _activeSyncTimeout = 101;
	private long _lastActive = -100L;
	private int _lastUpgrade = 0;
	protected int _rednetState;
	protected HarvestAreaManager<TileEntityFactory> _areaManager;
	protected Machine _machine;
    
    

	protected TileEntityFactory(Machine var1)
	{
		this._machine = var1;
		this._forwardDirection = ForgeDirection.NORTH;
	}

	@Override
	public void cofh_validate()
	{
		super.cofh_validate();
		this.onRotate();
		if (this.worldObj.isRemote && this.hasHAM())
			MineFactoryReloadedClient.addTileToAreaList(this);

	}

	@Override
	public void invalidate()
	{
		if (this.worldObj != null && this.worldObj.isRemote && this.hasHAM())
			MineFactoryReloadedClient.removeTileFromAreaList(this);

		super.invalidate();
	}

	protected static void createEntityHAM(TileEntityFactory var0)
	{
		createHAM(var0, 2, 2, 1, 1.0F, false);
	}

	protected static void createHAM(TileEntityFactory var0, int var1)
	{
		createHAM(var0, var1, 0, 0, 1.0F, true);
	}

	protected static void createHAM(TileEntityFactory var0, int var1, int var2, int var3, boolean var4)
	{
		createHAM(var0, var1, var2, var3, 1.0F, var4);
	}

	protected static void createHAM(TileEntityFactory var0, int var1, int var2, int var3, float var4, boolean var5)
	{
		var0._areaManager = new TileEntityFactory.FactoryAreaManager(var0, var1, var2, var3, var4, var5);
	}

	@Override
	public boolean hasHAM()
	{
		return this.getHAM() != null;
	}

	@Override
	public HarvestAreaManager<TileEntityFactory> getHAM()
	{
		return this._areaManager;
	}

	public World getWorld()
	{
		return this.worldObj;
	}

	@Override
	public ForgeDirection getDirectionFacing()
	{
		return this._forwardDirection;
	}

	@Override
	public boolean canRotate()
	{
		return this._canRotate;
	}

	@Override
	public boolean canRotate(ForgeDirection var1)
	{
		return this._canRotate;
	}

	protected void setCanRotate(boolean var1)
	{
		this._canRotate = var1;
	}

	@Override
	public void rotate(ForgeDirection var1)
	{
		if (this.canRotate())
			this.rotate(false);

	}

	public void rotate(boolean var1)
	{
		if (this.worldObj != null && !this.worldObj.isRemote)
		{
			switch ((var1 ? this._forwardDirection.getOpposite() : this._forwardDirection).ordinal())
			{
				case 2:
					this._forwardDirection = ForgeDirection.EAST;
					break;
				case 3:
					this._forwardDirection = ForgeDirection.WEST;
					break;
				case 4:
					this._forwardDirection = ForgeDirection.NORTH;
					break;
				case 5:
					this._forwardDirection = ForgeDirection.SOUTH;
					break;
				default:
					this._forwardDirection = ForgeDirection.NORTH;
			}

			this.onRotate();
		}

	}

	@Override
	public void rotateDirectlyTo(int var1)
	{
		ForgeDirection var2 = this._forwardDirection;
		this._forwardDirection = ForgeDirection.getOrientation(var1);
		if (this.worldObj != null && var2 != this._forwardDirection)
			this.onRotate();

	}

	protected void onRotate()
	{
		if (!this.isInvalid() && this.worldObj.blockExists(this.xCoord, this.yCoord, this.zCoord))
		{
			this.markForUpdate();
			MFRUtil.notifyNearbyBlocks(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}

	}

	public int getRotatedSide(int var1)
	{
		return _textureSelection[this._forwardDirection.ordinal()][var1];
	}

	public ForgeDirection getDropDirection()
	{
		return this.canRotate() ? this.getDirectionFacing().getOpposite() : ForgeDirection.UP;
	}

	public ForgeDirection[] getDropDirections()
	{
		return ForgeDirection.VALID_DIRECTIONS;
	}

	public boolean isActive()
	{
		return this._isActive;
	}

	public void setIsActive(boolean var1)
	{
		if (this._isActive != var1 & this.worldObj != null && !this.worldObj.isRemote && this._lastActive < this.worldObj.getTotalWorldTime())
		{
			this._lastActive = this.worldObj.getTotalWorldTime() + this._activeSyncTimeout;
			this._prevActive = this._isActive;
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}

		this._isActive = var1;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (!this.worldObj.isRemote && this._prevActive != this._isActive && this._lastActive < this.worldObj.getTotalWorldTime())
		{
			this._prevActive = this._isActive;
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}

	}

	public void setOwner(String var1)
	{
		if (var1 == null)
			var1 = "";

		if (this._owner == null || this._owner.isEmpty())
			this._owner = var1;

	}

	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return null;
	}

	public ContainerFactoryInventory getContainer(InventoryPlayer var1)
	{
		return null;
	}

	public String getGuiBackground()
	{
		return this._machine == null ? null : this._machine.getName().toLowerCase(Locale.US);
	}

	@Override
	public void markDirty()
	{
		if (this.worldObj != null && !this.worldObj.isRemote && this.hasHAM())
		{
			HarvestAreaManager var1 = this.getHAM();
			int var2 = var1.getUpgradeLevel();
			if (this._lastUpgrade != var2)
				Packets.sendToAllPlayersWatching(this.worldObj, this.xCoord, this.yCoord, this.zCoord, var1.getUpgradePacket());

			this._lastUpgrade = var2;
		}

		super.markDirty();
	}

	protected void writePacketData(NBTTagCompound var1)
	{
	}

	protected void readPacketData(NBTTagCompound var1)
	{
	}

	public void markForUpdate()
	{
		this._lastActive = 0L;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (this.worldObj != null && this._lastActive < this.worldObj.getTotalWorldTime())
		{
			NBTTagCompound var1 = new NBTTagCompound();
			var1.setByte("r", (byte) this._forwardDirection.ordinal());
			var1.setBoolean("a", this._isActive);
			this.writePacketData(var1);
			S35PacketUpdateTileEntity var2 = new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, var1);
			return var2;
		}
		else
			return null;
	}

	@Override
	public void onDataPacket(NetworkManager var1, S35PacketUpdateTileEntity var2)
	{
		NBTTagCompound var3 = var2.func_148857_g();
		switch (var2.func_148853_f())
		{
			case 0:
				this.rotateDirectlyTo(var3.getByte("r"));
				this._prevActive = this._isActive;
				this._isActive = var3.getBoolean("a");
				this.readPacketData(var3);
				if (this._prevActive != this._isActive)
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

				if (this._lastActive < 0L && this.hasHAM())
					Packets.sendToServer((short) 3, this);

				this._lastActive = 5L;
				break;
			case 255:
				if (this.hasHAM())
					this.getHAM().setUpgradeLevel(var3.getInteger("_upgradeLevel"));
		}

	}

	@Override
	public String getDataType()
	{
		return this._machine.getInternalName() + ".name";
	}

	@Override
	public void writePortableData(EntityPlayer var1, NBTTagCompound var2)
	{
	}

	@Override
	public void readPortableData(EntityPlayer var1, NBTTagCompound var2)
	{
	}

	@Override
	public void writeToNBT(NBTTagCompound var1)
	{
		super.writeToNBT(var1);
		var1.setInteger("rotation", this.getDirectionFacing().ordinal());
		if (!Strings.isNullOrEmpty(this._owner))
			var1.setString("owner", this._owner);

    
    
	}

	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		if (var1.hasKey("rotation"))
			this.rotateDirectlyTo(var1.getInteger("rotation"));

		if (var1.hasKey("owner"))
			this._owner = var1.getString("owner");

    
    
	}

	public void onRedNetChanged(ForgeDirection var1, int var2)
	{
		this._rednetState = var2;
	}

	public int getRedNetOutput(ForgeDirection var1)
	{
		return 0;
	}

	public void setManageFluids(boolean var1)
	{
		this._manageFluids = var1;
	}

	public boolean manageFluids()
	{
		return this._manageFluids;
	}

	public void setManageSolids(boolean var1)
	{
		this._manageSolids = var1;
	}

	public boolean manageSolids()
	{
		return this._manageSolids;
	}

	@Override
	public ConnectionType canConnectInventory(ForgeDirection var1)
	{
		return this.manageSolids() ? ConnectionType.FORCE : ConnectionType.DENY;
	}

	@Override
	@Strippable({ "buildcraft.api.transport.IPipeConnection" })
	public ConnectOverride overridePipeConnection(PipeType var1, ForgeDirection var2)
	{
		return var1 == PipeType.FLUID ? this.manageFluids() ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT : var1 == PipeType.ITEM ? this.canConnectInventory(var2).canConnect ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT : var1 == PipeType.STRUCTURE ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
	}

	protected static class FactoryAreaManager extends HarvestAreaManager<TileEntityFactory>
	{
		public FactoryAreaManager(TileEntityFactory var1, int var2, int var3, int var4, float var5, boolean var6)
		{
			super(var1, var2, var3, var4, var5, var6);
		}
	}
}
