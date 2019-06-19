package blusunrize.immersiveengineering.common.util.network;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import ru.will.git.immersiveengineering.NetworkMessageHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class MessageTileSync implements IMessage
{
	int dimension;
	int x;
	int y;
	int z;
	NBTTagCompound nbt;

	public MessageTileSync(TileEntityIEBase tile, NBTTagCompound nbt)
	{
		this.dimension = tile.getWorldObj().provider.dimensionId;
		this.x = tile.xCoord;
		this.y = tile.yCoord;
		this.z = tile.zCoord;
		this.nbt = nbt;
	}

	public MessageTileSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.dimension = buf.readInt();
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.dimension);
		buf.writeInt(this.x);
		buf.writeInt(this.y);
		buf.writeInt(this.z);
		ByteBufUtils.writeTag(buf, this.nbt);
	}

	public static class Handler implements IMessageHandler<MessageTileSync, IMessage>
	{
		@Override
		public IMessage onMessage(MessageTileSync message, MessageContext ctx)
		{
    
    

			return null;
		}
	}
}