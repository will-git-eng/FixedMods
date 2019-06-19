package ru.will.git.clientwg.network;

import ru.will.git.clientwg.ClientWG;
import ru.will.git.clientwg.util.ProtectedRegion;
import ru.will.git.clientwg.util.Region;
import ru.will.git.clientwg.util.Vec3i;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public final class RegionsMessage implements IMessage
{
	private final List<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();

	public RegionsMessage()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		int size = buf.readInt();
		for (int i = 0; i < size; i++)
		{
			Vec3i min = readVec3i(buf);
			Vec3i max = readVec3i(buf);
			boolean hasAccess = buf.readBoolean();
			boolean pvp = buf.readBoolean();
			boolean animalsDamage = buf.readBoolean();
			this.regions.add(new ProtectedRegion(new Region(min, max), hasAccess, pvp, animalsDamage));
		}
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
	}

	private static Vec3i readVec3i(ByteBuf buf)
	{
		return new Vec3i(buf.readInt(), buf.readInt(), buf.readInt());
	}

	public static final class Handler implements IMessageHandler<RegionsMessage, IMessage>
	{
		@Override
		public IMessage onMessage(RegionsMessage message, MessageContext ctx)
		{
			ClientWG.instance.regions.clear();
			ClientWG.instance.regions.addAll(message.regions);
			return null;
		}
	}
}
