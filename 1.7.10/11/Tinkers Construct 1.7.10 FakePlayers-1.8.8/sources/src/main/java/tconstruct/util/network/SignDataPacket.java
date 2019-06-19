package tconstruct.util.network;

import ru.will.git.reflectionmedic.util.EventUtils;
import com.google.common.base.Strings;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mantle.common.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tconstruct.TConstruct;
import tconstruct.tools.logic.BattlesignLogic;

import java.util.regex.Pattern;

public class SignDataPacket extends AbstractPacket
    
	private static final Pattern TEXT_CLEAN_PATTERN = Pattern.compile("[^a-zA-Z0-9а-яА-Я ]");
    

	private int dimension, x, y, z, length;
	private String[] text;

	public SignDataPacket()
	{
	}

	public SignDataPacket(int dimension, int x, int y, int z, String[] text)
	{
		this.text = text;
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
		this.length = text.length;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{
		buffer.writeInt(this.dimension);
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		buffer.writeInt(this.length);
		for (int i = 0; i < this.length; i++)
		{
			ByteBufUtils.writeUTF8String(buffer, this.text[i]);
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{
		this.dimension = buffer.readInt();
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		this.length = buffer.readInt();
		this.text = new String[this.length];
		for (int i = 0; i < this.length; i++)
		{
			this.text[i] = ByteBufUtils.readUTF8String(buffer);
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{
		TileEntity te = player.worldObj.getTileEntity(this.x, this.y, this.z);

		if (te instanceof BattlesignLogic)
		{
			BattlesignLogic logic = (BattlesignLogic) te;
			logic.setText(this.text);
		}
	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{
		World world = player.worldObj;
		int x = this.x;
		int y = this.y;
		int z = this.z;
    
		if (!world.blockExists(x, y, z))
    

		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof BattlesignLogic)
    
			if (EventUtils.cantBreak(player, x, y, z))
				return;

			for (int i = 0; i < text.length; i++)
			{
				String s = text[i];
				if (!Strings.isNullOrEmpty(s))
				{
					s = TEXT_CLEAN_PATTERN.matcher(s).replaceAll("");
					if (s.length() > MAX_LINE_LENGTH)
						s = s.substring(0, MAX_LINE_LENGTH + 1);
					text[i] = s;
				}
    

			BattlesignLogic logic = (BattlesignLogic) te;
    
    
    
    
	}
}
