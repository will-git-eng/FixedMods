package crazypants.enderio.teleport.packet;

import com.enderio.core.common.util.Util;
import com.enderio.core.common.vecmath.Vector3d;
import ru.will.git.enderio.EventConfig;
import ru.will.git.reflectionmedic.util.EventUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import crazypants.enderio.api.teleport.IItemOfTravel;
import crazypants.enderio.api.teleport.TeleportEntityEvent;
import crazypants.enderio.api.teleport.TravelSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.common.MinecraftForge;

public class PacketTravelEvent implements IMessage, IMessageHandler<PacketTravelEvent, IMessage>
{

	int x;
	int y;
	int z;
	int powerUse;
	boolean conserveMotion;
	int entityId;
	int source;

	public PacketTravelEvent()
	{
	}

	public PacketTravelEvent(Entity entity, int x, int y, int z, int powerUse, boolean conserveMotion, TravelSource source)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.powerUse = powerUse;
		this.conserveMotion = conserveMotion;
		this.entityId = entity instanceof EntityPlayer ? -1 : entity.getEntityId();
		this.source = source.ordinal();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.x);
		buf.writeInt(this.y);
		buf.writeInt(this.z);
		buf.writeInt(this.powerUse);
		buf.writeBoolean(this.conserveMotion);
		buf.writeInt(this.entityId);
		buf.writeInt(this.source);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.powerUse = buf.readInt();
		this.conserveMotion = buf.readBoolean();
		this.entityId = buf.readInt();
		this.source = buf.readInt();
	}

	@Override
	public IMessage onMessage(PacketTravelEvent message, MessageContext ctx)
	{
		Entity toTp = message.entityId == -1 ? ctx.getServerHandler().playerEntity : ctx.getServerHandler().playerEntity.worldObj.getEntityByID(message.entityId);

		int x = message.x, y = message.y, z = message.z;

		TravelSource source = TravelSource.values()[message.source];

		doServerTeleport(toTp, x, y, z, message.powerUse, message.conserveMotion, source);

		return null;
	}

	public static boolean doServerTeleport(Entity toTp, int x, int y, int z, int powerUse, boolean conserveMotion, TravelSource source)
	{
		EntityPlayer player = toTp instanceof EntityPlayer ? (EntityPlayer) toTp : null;

		TeleportEntityEvent evt = new TeleportEntityEvent(toTp, source, x, y, z);
		if (MinecraftForge.EVENT_BUS.post(evt))
			return false;
		x = evt.targetX;
		y = evt.targetY;
    
		if (EventConfig.blinkEvent && player != null && source == TravelSource.STAFF_BLINK)
			if (EventUtils.cantBreak(player, (int) player.posX, (int) player.posY, (int) player.posZ) || EventUtils.cantBreak(player, x, y, z))
    

		toTp.worldObj.playSoundEffect(toTp.posX, toTp.posY, toTp.posZ, source.sound, 1.0F, 1.0F);

		toTp.playSound(source.sound, 1.0F, 1.0F);

		if (player != null)
			player.setPositionAndUpdate(x + 0.5, y + 1.1, z + 0.5);
		else
			toTp.setPosition(x, y, z);

		toTp.worldObj.playSoundEffect(x, y, z, source.sound, 1.0F, 1.0F);
		toTp.fallDistance = 0;

		if (player != null)
		{
			if (conserveMotion)
			{
				Vector3d velocityVex = Util.getLookVecEio(player);
				S12PacketEntityVelocity p = new S12PacketEntityVelocity(toTp.getEntityId(), velocityVex.x, velocityVex.y, velocityVex.z);
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(p);
			}

			if (powerUse > 0 && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof IItemOfTravel)
			{
				ItemStack item = player.getCurrentEquippedItem().copy();
				((IItemOfTravel) item.getItem()).extractInternal(item, powerUse);
				toTp.setCurrentItemOrArmor(0, item);
			}
		}

		return true;
	}
}
