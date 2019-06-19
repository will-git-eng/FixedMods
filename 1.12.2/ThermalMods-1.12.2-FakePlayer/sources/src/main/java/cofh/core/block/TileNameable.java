package cofh.core.block;

import cofh.api.core.IPortableData;
import cofh.core.fluid.FluidTankCore;
import cofh.core.network.ITilePacketHandler;
import cofh.core.network.PacketBase;
import cofh.core.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileNameable extends TileCore implements ITilePacketHandler, IPortableData
{
	public String customName = "";

	public void setCustomName(String name)
	{
		if (!name.isEmpty())
			this.customName = name;

	}

	protected abstract Object getMod();

	protected abstract String getModVersion();

	protected abstract String getTileName();

	public int getType()
	{
		return 0;
	}

	protected boolean readPortableTagInternal(EntityPlayer player, NBTTagCompound tag)
	{
		return true;
	}

	protected boolean writePortableTagInternal(EntityPlayer player, NBTTagCompound tag)
	{
		return true;
	}

	public int getScaledProgress(int scale)
	{
		return 0;
	}

	public int getScaledSpeed(int scale)
	{
		return 0;
	}

	@Override
	public boolean openGui(EntityPlayer player)
	{
		if (this.hasGui())
			player.openGui(this.getMod(), 0, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());

		return this.hasGui();
	}

	@Override
	public boolean openConfigGui(EntityPlayer player)
	{
		if (this.hasConfigGui())
			player.openGui(this.getMod(), 1, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());

		return this.hasConfigGui();
	}

	@Override
	public void sendGuiNetworkData(Container container, IContainerListener listener)
	{
		if (listener instanceof EntityPlayer)
		{
			PacketBase guiPacket = this.getGuiPacket();
			if (guiPacket != null)
				PacketHandler.sendTo(guiPacket, (EntityPlayer) listener);
		}

	}

	public FluidTankCore getTank()
	{
		return null;
	}

	public FluidStack getTankFluid()
	{
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("Name"))
			this.customName = nbt.getString("Name");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setString("Version", this.getModVersion());
		if (!this.customName.isEmpty())
			nbt.setString("Name", this.customName);

		return nbt;
	}

	@Override
	public PacketBase getTilePacket()
	{
		PacketBase payload = super.getTilePacket();
		payload.addString(this.customName);
		return payload;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleTilePacket(PacketBase payload)
	{
		this.customName = payload.getString();
		this.world.checkLight(this.pos);
	}

	@Override
	public String getDataType()
	{
		return this.getTileName();
	}

	@Override
	public void readPortableData(EntityPlayer player, NBTTagCompound tag)
	{
		if (this.canPlayerAccess(player))
			if (this.readPortableTagInternal(player, tag))
			{
				this.markChunkDirty();
				this.sendTilePacket(Side.CLIENT);
			}
	}

	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag)
	{
		if (this.canPlayerAccess(player))
			this.writePortableTagInternal(player, tag);
	}
}
