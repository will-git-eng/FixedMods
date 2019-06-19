package crazypants.enderio.enderface;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import crazypants.enderio.network.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;

public class PacketOpenServerGUI implements IMessage, IMessageHandler<PacketOpenServerGUI, IMessage>
{

	int x;
	int y;
	int z;
	int side;
	Vec3 hitVec;

	public PacketOpenServerGUI()
	{
	}

	public PacketOpenServerGUI(int x, int y, int z, int side, Vec3 hitVec)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.side = side;
		this.hitVec = hitVec;
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		buffer.writeInt(this.side);
		buffer.writeDouble(this.hitVec.xCoord);
		buffer.writeDouble(this.hitVec.yCoord);
		buffer.writeDouble(this.hitVec.zCoord);
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		this.side = buffer.readInt();
		this.hitVec = Vec3.createVectorHelper(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}

	@Override
	public IMessage onMessage(PacketOpenServerGUI message, MessageContext ctx)
	{
		EntityPlayerMP player = ctx.getServerHandler().playerEntity;
		Container c = player.openContainer;

		PacketHandler.INSTANCE.sendTo(new PacketLockClientContainer(player.openContainer.windowId), player);
		Vec3 hitVec = message.hitVec;
		player.theItemInWorldManager.activateBlockOrUseItem(player, player.worldObj, null, message.x, message.y, message.z, message.side, (float) hitVec.xCoord, (float) hitVec.yCoord, (float) hitVec.zCoord);
		player.theItemInWorldManager.thisPlayerMP = player;
		if (c != player.openContainer)
		{
    
    
		}
		else
			PacketHandler.INSTANCE.sendTo(new PacketLockClientContainer(), player);
		return null;
	}
}
