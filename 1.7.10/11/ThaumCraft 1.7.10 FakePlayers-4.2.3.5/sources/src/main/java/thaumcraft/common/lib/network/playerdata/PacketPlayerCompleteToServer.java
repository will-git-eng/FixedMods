package thaumcraft.common.lib.network.playerdata;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.research.ResearchManager;

public class PacketPlayerCompleteToServer implements IMessage, IMessageHandler<PacketPlayerCompleteToServer, IMessage>
{
	private String key;
	private int dim;
	private String username;
	private byte type;

	public PacketPlayerCompleteToServer()
	{
	}

	public PacketPlayerCompleteToServer(String key, String username, int dim, byte type)
	{
		this.key = key;
		this.dim = dim;
		this.username = username;
		this.type = type;
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		ByteBufUtils.writeUTF8String(buffer, this.key);
		buffer.writeInt(this.dim);
		ByteBufUtils.writeUTF8String(buffer, this.username);
		buffer.writeByte(this.type);
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.key = ByteBufUtils.readUTF8String(buffer);
		this.dim = buffer.readInt();
		this.username = ByteBufUtils.readUTF8String(buffer);
		this.type = buffer.readByte();
	}

	@Override
	public IMessage onMessage(PacketPlayerCompleteToServer message, MessageContext ctx)
	{
		World world = DimensionManager.getWorld(message.dim);
		if (world != null)
		{
			String playerName = message.username;

    
			EntityPlayerMP player = ctx.getServerHandler().playerEntity;
			if (player == null || player.worldObj != world || !player.getCommandSenderName().equals(playerName))
    

			if (player != null)
			{
    
				ResearchItem research = ResearchCategories.getResearch(researchKey);
				if (research == null)
    

				if (!ResearchManager.isResearchComplete(playerName, researchKey))
					if (ResearchManager.doesPlayerHaveRequisites(playerName, researchKey))
					{
						if (message.type == 1)
							ResearchManager.createResearchNoteForPlayer(world, player, researchKey);
						else if (message.type == 0)
    
    

    
							boolean secondary = tags != null && tags.size() > 0 && (Config.researchDifficulty == -1 || Config.researchDifficulty == 0 && research.isSecondary());
							if (!secondary)
    

    
							for (Aspect aspect : aspects)
							{
								if (aspect == null || Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(playerName, aspect) < tags.getAmount(aspect))
									return null;
    

							for (Aspect aspect : aspects)
							{
								int amount = tags.getAmount(aspect);
								Thaumcraft.proxy.playerKnowledge.addAspectPool(playerName, aspect, (short) -amount);
								ResearchManager.scheduleSave(player);
								PacketHandler.INSTANCE.sendTo(new PacketAspectPool(aspect.getTag(), (short) -amount, Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(playerName, aspect)), player);
							}

							PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(researchKey), player);
							Thaumcraft.proxy.getResearchManager().completeResearch(player, researchKey);
							if (research.siblings != null)
								for (String sibling : research.siblings)
								{
									if (!ResearchManager.isResearchComplete(playerName, sibling) && ResearchManager.doesPlayerHaveRequisites(playerName, sibling))
									{
										PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(sibling), player);
										Thaumcraft.proxy.getResearchManager().completeResearch(player, sibling);
									}
								}
						}

						world.playSoundAtEntity(player, "thaumcraft:learn", 0.75F, 1.0F);
					}
					else
						player.addChatMessage(new ChatComponentTranslation(StatCollector.translateToLocal("tc.researcherror")));
			}
		}
		return null;
	}
}
