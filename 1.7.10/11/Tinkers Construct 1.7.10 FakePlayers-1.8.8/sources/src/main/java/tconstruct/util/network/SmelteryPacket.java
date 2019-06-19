package tconstruct.util.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import mantle.common.network.PacketUpdateTE;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import tconstruct.TConstruct;
import tconstruct.smeltery.inventory.SmelteryContainer;
import tconstruct.smeltery.logic.SmelteryLogic;

public class SmelteryPacket extends AbstractPacket
{

	int dimension, x, y, z, fluidID;
	boolean isShiftPressed;

	public SmelteryPacket()
	{

	}

	public SmelteryPacket(int dimension, int x, int y, int z, boolean isShiftPressed, int fluidID)
	{
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
		this.isShiftPressed = isShiftPressed;
		this.fluidID = fluidID;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{
		buffer.writeInt(this.dimension);
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		buffer.writeBoolean(this.isShiftPressed);
		buffer.writeInt(this.fluidID);

	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{
		this.dimension = buffer.readInt();
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		this.isShiftPressed = buffer.readBoolean();
		this.fluidID = buffer.readInt();
	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{
    
		if (player.openContainer instanceof SmelteryContainer)
		{
			SmelteryContainer container = (SmelteryContainer) player.openContainer;
			SmelteryLogic logic = container.logic;
			if (logic != null && logic.hasWorldObj() && logic.getWorldObj().provider.dimensionId == this.dimension && logic.xCoord == this.x && logic.yCoord == this.y && logic.zCoord == this.z)
			{
				FluidStack temp = null;

				for (FluidStack liquid : logic.moltenMetal)
				{
					if (liquid.getFluidID() == this.fluidID)
						temp = liquid;
				}

				if (temp != null)
				{
					logic.moltenMetal.remove(temp);
					if (this.isShiftPressed)
						logic.moltenMetal.add(temp);
					else
						logic.moltenMetal.add(0, temp);
				}

				NBTTagCompound data = new NBTTagCompound();
				logic.writeToNBT(data);
				TConstruct.packetPipeline.sendToDimension(new PacketUpdateTE(this.x, this.y, this.z, data), this.dimension);
			}
    
	}
}
