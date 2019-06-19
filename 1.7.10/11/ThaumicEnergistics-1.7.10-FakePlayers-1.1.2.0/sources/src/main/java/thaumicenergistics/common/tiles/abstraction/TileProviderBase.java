package thaumicenergistics.common.tiles.abstraction;

import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.MachineSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.localization.WailaText;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.networking.TileCableBus;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.IEssentiaGrid;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.common.integration.IWailaSource;
import thaumicenergistics.common.registries.EnumCache;
import thaumicenergistics.common.tiles.TileEssentiaProvider;
import thaumicenergistics.common.tiles.TileInfusionProvider;
import thaumicenergistics.common.utils.EffectiveSide;

import java.util.List;

    
public abstract class TileProviderBase extends AENetworkTile implements IColorableTile, IWailaSource
{
    
	protected static final String NBT_KEY_COLOR = "TEColor", NBT_KEY_ATTACHMENT = "TEAttachSide", NBT_KEY_ISCOLORFORCED = "ColorForced";

    
	private MachineSource asMachineSource;

    
	protected int attachmentSide;

    
	protected IMEEssentiaMonitor monitor = null;

    
	protected boolean isActive;

    
	protected boolean isColorForced = false;

	public TileProviderBase()
    
		this.asMachineSource = new MachineSource(this);
	}

    
	private AEColor[] getNeighborCableColors()
	{
		AEColor[] sideColors = new AEColor[6];

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
    
    
			if (tileEntity == null)
    
    
			if (tileEntity instanceof TileCableBus)
				sideColors[side.ordinal()] = ((TileCableBus) tileEntity).getColor();

		}

		return sideColors;
	}

    
	protected int extractEssentiaFromNetwork(final Aspect wantedAspect, final int wantedAmount, final boolean mustMatch)
    
		if (this.getEssentiaMonitor())
    
    
    
			if (amountExtracted == 0)
    
    
    
				if (mustMatch)
					if (amountExtracted != wantedAmount)
    
    
    
    
    
			return (int) amountExtracted;
    
		return 0;

	}

    
	protected boolean getEssentiaMonitor()
	{
		IMEEssentiaMonitor essentiaMonitor = null;
    
    
		if (node != null)
    
    
    
			if (grid != null)
				essentiaMonitor = grid.getCache(IEssentiaGrid.class);
    
    
		return this.monitor != null;
	}

	protected abstract double getIdlePowerusage();

	@Override
	protected abstract ItemStack getItemFromTile(Object obj);

    
	protected void onPowerChange(final boolean isPowered)
	{
	}

    
	protected void setProviderColor(final AEColor gridColor)
    
    
		if (EffectiveSide.isServerSide())
		{
    
    
			if( gridNode != null )
    
				this.getProxy().getNode().updateState();
			}
    
			this.markForUpdate();
			this.saveChanges();
		}
	}

    
	@Override
	public void addWailaInformation(final List<String> tooltip)
    
    
    
		if (this.isActive())
			tooltip.add(WailaText.DeviceOnline.getLocal());
		else
    
		tooltip.add("Color: " + this.getColor().toString());
	}

	@MENetworkEventSubscribe
	public final void channelEvent(final MENetworkChannelsChanged event)
    
    
    
		if (EffectiveSide.isServerSide())
    
		this.markForUpdate();
	}

	public void checkGridConnectionColor()
    
    
		if (FMLCommonHandler.instance().getEffectiveSide().isClient() || this.worldObj == null)
    
    
		if (this.isColorForced)
    
    
    
    
		if (this.attachmentSide != ForgeDirection.UNKNOWN.ordinal())
			if (sideColors[this.attachmentSide] != null)
    
    
				if (sideColors[this.attachmentSide] == currentColor)
    
				this.setProviderColor(sideColors[this.attachmentSide]);

				return;
    
    
		for (int index = 0; index < 6; index++)
		{
			if (sideColors[index] != null)
				if (sideColors[index] == currentColor)
    
    
					this.saveChanges();

					return;
    
				else if (currentColor == AEColor.Transparent)
    
    
					this.setProviderColor(sideColors[index]);

					return;
				}
    
    
		this.setProviderColor(AEColor.Transparent);

	}

    
	public long getAspectAmountInNetwork(final Aspect searchAspect)
    
    
		if (this.getEssentiaMonitor())
    
		return 0;
	}

	@Override
	public AECableType getCableConnectionType(final ForgeDirection direction)
	{
		return AECableType.SMART;
	}

    
	@Override
	public AEColor getColor()
	{
		return this.getProxy().getColor();
	}

	public AEColor getGridColor()
	{
		return this.getProxy().getGridColor();
	}

    
	public MachineSource getMachineSource()
	{
		return this.asMachineSource;
	}

	public boolean isActive()
    
		if (EffectiveSide.isServerSide())
		{
    
			if (this.getProxy() != null && this.getProxy().getNode() != null)
    
    
    
				if (prevActive != this.isActive)
					this.onPowerChange(this.isActive);
			}
		}

		return this.isActive;
	}

    
	public void onBreakBlock()
	{
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void onLoadNBT(final NBTTagCompound data)
	{
    
		if (data.hasKey(TileProviderBase.NBT_KEY_ISCOLORFORCED))
    
    
		if (data.hasKey(TileProviderBase.NBT_KEY_COLOR))
    
    
		if (data.hasKey(TileProviderBase.NBT_KEY_ATTACHMENT))
    
		this.setupProvider(attachmentSideFromNBT);
	}

	@Override
	public void onReady()
    
    
		this.checkGridConnectionColor();
	}

	@TileEvent(TileEventType.NETWORK_READ)
	@SideOnly(Side.CLIENT)
	public boolean onReceiveNetworkData(final ByteBuf data)
    
    
		this.isActive = data.readBoolean();

		return true;
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void onSaveNBT(final NBTTagCompound data)
    
    
    
		data.setBoolean(TileProviderBase.NBT_KEY_ISCOLORFORCED, this.isColorForced);
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void onSendNetworkData(final ByteBuf data)
    
    
		data.writeBoolean(this.isActive());
	}

	@MENetworkEventSubscribe
	public final void powerEvent(final MENetworkPowerStatusChange event)
	{
		this.markForUpdate();
	}

    
	@Override
	public boolean recolourBlock(final ForgeDirection side, final AEColor color, final EntityPlayer player)
    
    
		this.setProviderColor(color);

		return true;
	}

    
	public void setOwner(final EntityPlayer player)
	{
		this.getProxy().setOwner(player);
	}

    
	public void setupProvider(final int attachmentSide)
    
		if (FMLCommonHandler.instance().getEffectiveSide().isServer())
    
    
    
			this.getProxy().setIdlePowerUsage(this.getIdlePowerusage());
		}
	}

}
