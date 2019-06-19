package cofh.core.block;

import cofh.api.core.ISecurable;
import cofh.core.init.CoreProps;
import cofh.core.network.*;
import cofh.core.util.CoreUtils;
import cofh.core.util.RegistrySocial;
import cofh.core.util.helpers.SecurityHelper;
import cofh.core.util.helpers.ServerHelper;
import ru.will.git.cofh.ModUtils;
import ru.will.git.eventhelper.fake.FakePlayerContainer;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class TileCore extends TileEntity implements ITileInfoPacketHandler
{
	
	public final FakePlayerContainer fake = ModUtils.NEXUS_FACTORY.wrapFake(this);

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		this.fake.writeToNBT(compound);
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		this.fake.readFromNBT(compound);
		super.readFromNBT(compound);
	}
	

	public void blockBroken()
	{
	}

	public void blockDismantled()
	{
		this.blockBroken();
	}

	public void blockPlaced()
	{
	}

	public void callBlockUpdate()
	{
		IBlockState state = this.world.getBlockState(this.pos);
		this.world.notifyBlockUpdate(this.pos, state, state, 3);
	}

	public void callNeighborStateChange()
	{
		this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
	}

	public void callNeighborTileChange()
	{
		this.world.updateComparatorOutputLevel(this.pos, this.getBlockType());
	}

	public void markChunkDirty()
	{
		this.world.markChunkDirty(this.pos, this);
	}

	@Override
	public void onChunkUnload()
	{
		if (!this.tileEntityInvalid)
			this.invalidate();

	}

	@Override
	public void onLoad()
	{
		if (ServerHelper.isClientWorld(this.world) && !this.hasClientUpdate())
			this.world.tickableTileEntities.remove(this);

		this.validate();
	}

	public void onNeighborBlockChange()
	{
	}

	public void onNeighborTileChange(BlockPos pos)
	{
	}

	public boolean canPlayerAccess(EntityPlayer player)
	{
		if (!(this instanceof ISecurable))
			return true;
		ISecurable.AccessMode access = ((ISecurable) this).getAccess();
		String name = player.getName();
		if (!access.isPublic() && (!CoreProps.enableOpSecureAccess || !CoreUtils.isOp(name)))
		{
			GameProfile profile = ((ISecurable) this).getOwner();
			UUID ownerID = profile.getId();
			if (SecurityHelper.isDefaultUUID(ownerID))
				return true;
			UUID otherID = SecurityHelper.getID(player);
			return ownerID.equals(otherID) || access.isFriendsOnly() && RegistrySocial.playerHasAccess(name, profile);
		}
		return true;
	}

	public boolean canPlayerDismantle(EntityPlayer player)
	{
		return true;
	}

	public boolean hasClientUpdate()
	{
		return false;
	}

	public boolean isUsable(EntityPlayer player)
	{
		return player.getDistanceSq(this.pos) <= 64.0D && this.world.getTileEntity(this.pos) == this;
	}

	public boolean onWrench(EntityPlayer player, EnumFacing side)
	{
		return false;
	}

	public int getComparatorInputOverride()
	{
		return 0;
	}

	public int getLightValue()
	{
		return 0;
	}

	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return state;
	}

	protected final boolean timeCheck()
	{
		return this.world.getTotalWorldTime() % 32L == 0L;
	}

	protected final boolean timeCheckHalf()
	{
		return this.world.getTotalWorldTime() % 16L == 0L;
	}

	protected final boolean timeCheckQuarter()
	{
		return this.world.getTotalWorldTime() % 8L == 0L;
	}

	protected final boolean timeCheckEighth()
	{
		return this.world.getTotalWorldTime() % 4L == 0L;
	}

	@Override
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		return PacketHandler.toTilePacket(this.getTilePacket(), this.getPos());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
	{
		PacketHandler.handleNBTPacket(pkt.getNbtCompound());
	}

	@Override
	public NBTTagCompound getUpdateTag()
	{
		return PacketHandler.toNBTTag(this.getTilePacket(), super.getUpdateTag());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag)
	{
		PacketHandler.handleNBTPacket(tag);
	}

	public PacketBase getAccessPacket()
	{
		PacketBase payload = PacketTileInfo.newPacket(this);
		payload.addByte(TileCore.TilePacketID.C_ACCESS.ordinal());
		return payload;
	}

	public PacketBase getConfigPacket()
	{
		PacketBase payload = PacketTileInfo.newPacket(this);
		payload.addByte(TileCore.TilePacketID.C_CONFIG.ordinal());
		return payload;
	}

	public PacketBase getModePacket()
	{
		PacketBase payload = PacketTileInfo.newPacket(this);
		payload.addByte(TileCore.TilePacketID.C_MODE.ordinal());
		return payload;
	}

	protected void handleAccessPacket(PacketBase payload)
	{
		this.markChunkDirty();
	}

	protected void handleConfigPacket(PacketBase payload)
	{
		this.markChunkDirty();
	}

	protected void handleModePacket(PacketBase payload)
	{
		this.markChunkDirty();
		this.callNeighborTileChange();
	}

	public void sendAccessPacket()
	{
		if (ServerHelper.isClientWorld(this.world))
			PacketHandler.sendToServer(this.getAccessPacket());

	}

	public void sendConfigPacket()
	{
		if (ServerHelper.isClientWorld(this.world))
			PacketHandler.sendToServer(this.getConfigPacket());

	}

	public void sendModePacket()
	{
		if (ServerHelper.isClientWorld(this.world))
			PacketHandler.sendToServer(this.getModePacket());

	}

	public PacketBase getFluidPacket()
	{
		PacketBase payload = PacketTileInfo.newPacket(this);
		payload.addByte(TileCore.TilePacketID.S_FLUID.ordinal());
		return payload;
	}

	public PacketBase getGuiPacket()
	{
		PacketBase payload = PacketTileInfo.newPacket(this);
		payload.addByte(TileCore.TilePacketID.S_GUI.ordinal());
		return payload;
	}

	public PacketBase getTilePacket()
	{
		return new PacketTile(this);
	}

	protected void handleFluidPacket(PacketBase payload)
	{
	}

	protected void handleGuiPacket(PacketBase payload)
	{
	}

	public void sendFluidPacket()
	{
		PacketHandler.sendToDimension(this.getFluidPacket(), this.world.provider.getDimension());
	}

	public void sendTilePacket(Side side)
	{
		if (this.world != null)
			if (side == Side.CLIENT && ServerHelper.isServerWorld(this.world))
				PacketHandler.sendToAllAround(this.getTilePacket(), this);
			else if (side == Side.SERVER && ServerHelper.isClientWorld(this.world))
				PacketHandler.sendToServer(this.getTilePacket());
	}

	protected void updateLighting()
	{
		int light2 = this.world.getLightFor(EnumSkyBlock.BLOCK, this.pos);
		int light1 = this.getLightValue();
		if (light1 != light2 && this.world.checkLightFor(EnumSkyBlock.BLOCK, this.pos))
		{
			IBlockState state = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, state, state, 3);
		}

	}

	@Override
	public void handleTileInfoPacket(PacketBase payload, boolean isServer, EntityPlayer thePlayer)
	{
		switch (TileCore.TilePacketID.values()[payload.getByte()])
		{
			case S_GUI:
				this.handleGuiPacket(payload);
				return;
			case S_FLUID:
				this.handleFluidPacket(payload);
				return;
			case C_ACCESS:
				this.handleAccessPacket(payload);
				return;
			case C_CONFIG:
				this.handleConfigPacket(payload);
				return;
			case C_MODE:
				this.handleModePacket(payload);
				return;
			default:
		}
	}

	public Object getGuiClient(InventoryPlayer inventory)
	{
		return null;
	}

	public Object getGuiServer(InventoryPlayer inventory)
	{
		return null;
	}

	public Object getConfigGuiClient(InventoryPlayer inventory)
	{
		return null;
	}

	public Object getConfigGuiServer(InventoryPlayer inventory)
	{
		return null;
	}

	public int getInvSlotCount()
	{
		return 0;
	}

	public boolean hasGui()
	{
		return false;
	}

	public boolean hasConfigGui()
	{
		return false;
	}

	public boolean openGui(EntityPlayer player)
	{
		return false;
	}

	public boolean openConfigGui(EntityPlayer player)
	{
		return false;
	}

	public void receiveGuiNetworkData(int id, int data)
	{
	}

	public void sendGuiNetworkData(Container container, IContainerListener player)
	{
	}

	public enum TilePacketID
	{
		C_ACCESS,
		C_CONFIG,
		C_MODE,
		S_FLUID,
		S_GUI
	}
}
