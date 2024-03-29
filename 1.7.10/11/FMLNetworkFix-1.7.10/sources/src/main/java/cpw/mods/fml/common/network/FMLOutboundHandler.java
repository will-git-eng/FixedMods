package cpw.mods.fml.common.network;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;

import java.util.List;

public class FMLOutboundHandler extends ChannelOutboundHandlerAdapter
{
	public static final AttributeKey<OutboundTarget> FML_MESSAGETARGET = new AttributeKey<OutboundTarget>("fml:outboundTarget");
	public static final AttributeKey<Object> FML_MESSAGETARGETARGS = new AttributeKey<Object>("fml:outboundTargetArgs");

	public enum OutboundTarget
	{
    
		NOWHERE(Sets.immutableEnumSet(Side.CLIENT, Side.SERVER))
				{
					@Override
					public void validateArgs(Object args)
    
					}

					@Override
					public List<NetworkDispatcher> selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet)
					{
						return null;
					}

    
	DISPATCHER(Sets.immutableEnumSet(Side.SERVER))
			{
				@Override
				public void validateArgs(Object args)
				{
					if (!(args instanceof NetworkDispatcher))
						throw new RuntimeException("DISPATCHER expects a NetworkDispatcher");
				}

				@Override
				public List<NetworkDispatcher> selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet)
				{
					return ImmutableList.of((NetworkDispatcher) args);
				}
    
	REPLY(Sets.immutableEnumSet(Side.SERVER))
			{
				@Override
				public void validateArgs(Object args)
    
				}

				@Override
				public List<NetworkDispatcher> selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet)
				{
					return ImmutableList.of(packet.getDispatcher());
				}
    
	PLAYER(Sets.immutableEnumSet(Side.SERVER))
			{
				@Override
				public void validateArgs(Object args)
				{
    
					if (!(args instanceof EntityPlayer))
    
				}

				@Override
				public List<NetworkDispatcher> selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet)
				{
    
    
					NetworkDispatcher dispatcher = null;
					if (player != null)
					{
						NetHandlerPlayServer playerNetServerHandler = player.playerNetServerHandler;
						if (playerNetServerHandler != null)
						{
							NetworkManager netManager = playerNetServerHandler.netManager;
							if (netManager != null)
							{
								Channel channel = netManager.channel();
								if (channel != null)
								{
									Attribute<NetworkDispatcher> attr = channel.attr(NetworkDispatcher.FML_DISPATCHER);
									if (attr != null)
										dispatcher = attr.get();
								}
							}
						}
    

					return dispatcher == null ? ImmutableList.<NetworkDispatcher>of() : ImmutableList.of(dispatcher);
				}
    
	ALL(Sets.immutableEnumSet(Side.SERVER))
			{
				@Override
				public void validateArgs(Object args)
				{
				}

				@SuppressWarnings("unchecked")
				@Override
				public List<NetworkDispatcher> selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet)
				{
					ImmutableList.Builder<NetworkDispatcher> builder = ImmutableList.builder();
					for (EntityPlayerMP player : (List<EntityPlayerMP>) FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
					{
						NetworkDispatcher dispatcher = player.playerNetServerHandler.netManager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
						if (dispatcher != null)
							builder.add(dispatcher);
					}
					return builder.build();
				}
    
	DIMENSION(Sets.immutableEnumSet(Side.SERVER))
			{
				@Override
				public void validateArgs(Object args)
				{
					if (!(args instanceof Integer))
						throw new RuntimeException("DIMENSION expects an integer argument");
				}

				@SuppressWarnings("unchecked")
				@Override
				public List<NetworkDispatcher> selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet)
				{
					int dimension = (Integer) args;
					ImmutableList.Builder<NetworkDispatcher> builder = ImmutableList.builder();
					for (EntityPlayerMP player : (List<EntityPlayerMP>) FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
					{
						if (dimension == player.dimension)
						{
    
							if (dispatcher != null)
								builder.add(dispatcher);
						}
					}
					return builder.build();
				}
    
	ALLAROUNDPOINT(Sets.immutableEnumSet(Side.SERVER))
			{
				@Override
				public void validateArgs(Object args)
				{
					if (!(args instanceof TargetPoint))
						throw new RuntimeException("ALLAROUNDPOINT expects a TargetPoint argument");
				}

				@SuppressWarnings("unchecked")
				@Override
				public List<NetworkDispatcher> selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet)
				{
					TargetPoint tp = (TargetPoint) args;
					ImmutableList.Builder<NetworkDispatcher> builder = ImmutableList.builder();
					for (EntityPlayerMP player : (List<EntityPlayerMP>) FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
					{
						if (player.dimension == tp.dimension)
						{
							double d4 = tp.x - player.posX;
							double d5 = tp.y - player.posY;
							double d6 = tp.z - player.posZ;

							if (d4 * d4 + d5 * d5 + d6 * d6 < tp.range * tp.range)
							{
								NetworkDispatcher dispatcher = player.playerNetServerHandler.netManager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
								if (dispatcher != null)
									builder.add(dispatcher);
							}
						}
					}
					return builder.build();
				}
    
	TOSERVER(Sets.immutableEnumSet(Side.CLIENT))
			{
				@Override
				public void validateArgs(Object args)
				{
				}

				@Override
				public List<NetworkDispatcher> selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet)
				{
					NetworkManager clientConnection = FMLCommonHandler.instance().getClientToServerNetworkManager();
					return clientConnection == null || clientConnection.channel().attr(NetworkDispatcher.FML_DISPATCHER).get() == null ? ImmutableList.<NetworkDispatcher>of() : ImmutableList.of(clientConnection.channel().attr(NetworkDispatcher.FML_DISPATCHER).get());
				}
			};

		OutboundTarget(ImmutableSet<Side> sides)
		{
			this.allowed = sides;
		}

		public final ImmutableSet<Side> allowed;

		public abstract void validateArgs(Object args);

		public abstract List<NetworkDispatcher> selectNetworks(Object args, ChannelHandlerContext context, FMLProxyPacket packet);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
	{
		if (!(msg instanceof FMLProxyPacket))
			return;
		FMLProxyPacket pkt = (FMLProxyPacket) msg;
		OutboundTarget outboundTarget;
		Object args = null;
    
		if (dispatcher != null)
		{
			ctx.write(msg, promise);
			return;
		}

		outboundTarget = ctx.channel().attr(FML_MESSAGETARGET).get();
		Side channelSide = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();
		if (outboundTarget != null && outboundTarget.allowed.contains(channelSide))
		{
			args = ctx.channel().attr(FML_MESSAGETARGETARGS).get();
			outboundTarget.validateArgs(args);
		}
		else if (channelSide == Side.CLIENT)
			outboundTarget = OutboundTarget.TOSERVER;
		else
			throw new FMLNetworkException("Packet arrived at the outbound handler without a valid target!");

    
		if (dispatchers == null)
		{
			ctx.write(msg, promise);
			promise.setSuccess();
			return;
		}
		for (NetworkDispatcher targetDispatcher : dispatchers)
		{
			targetDispatcher.sendProxy((FMLProxyPacket) msg);
		}
		promise.setSuccess();
	}

}