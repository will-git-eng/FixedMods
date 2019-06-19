package tconstruct.util.network;

import com.google.common.base.Strings;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import tconstruct.tools.inventory.ToolStationContainer;
import tconstruct.tools.logic.ToolStationLogic;

import java.util.regex.Pattern;

public class ToolStationPacket extends AbstractPacket
    
	private static final Pattern TEXT_CLEAN_PATTERN = Pattern.compile("[^a-zA-Z0-9а-яА-Я ]");
    

	private int x, y, z;
	private String toolName;

	public ToolStationPacket()
	{
	}

	public ToolStationPacket(int x, int y, int z, String toolName)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.toolName = toolName;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		ByteBufUtils.writeUTF8String(buffer, this.toolName);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		this.toolName = ByteBufUtils.readUTF8String(buffer);
	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{
	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{
		int x = this.x;
		int y = this.y;
		int z = this.z;

    
		if (player.openContainer instanceof ToolStationContainer)
		{
			ToolStationContainer container = (ToolStationContainer) player.openContainer;
			ToolStationLogic logic = container.logic;
			if (logic != null && logic.xCoord == x && logic.yCoord == y && logic.zCoord == z)
			{
				if (!Strings.isNullOrEmpty(this.toolName))
				{
					this.toolName = TEXT_CLEAN_PATTERN.matcher(this.toolName).replaceAll("");
					if (this.toolName.length() > MAX_LINE_LENGTH)
						this.toolName = this.toolName.substring(0, MAX_LINE_LENGTH + 1);
				}

				logic.setToolname(this.toolName);
			}
    
	}

}
