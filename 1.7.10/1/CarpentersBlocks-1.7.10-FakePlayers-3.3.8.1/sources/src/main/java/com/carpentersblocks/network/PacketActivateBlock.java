package com.carpentersblocks.network;

import com.carpentersblocks.util.EntityLivingUtil;
import ru.will.git.reflectionmedic.util.EventUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class PacketActivateBlock extends TilePacket
{

	private int side;

	public PacketActivateBlock()
	{
	}

	public PacketActivateBlock(int x, int y, int z, int side)
	{
		super(x, y, z);
		this.side = side;
	}

	@Override
	public void processData(EntityPlayer player, ByteBufInputStream bbis) throws IOException
	{
		super.processData(player, bbis);

		ItemStack itemStack = player.getHeldItem();
		this.side = bbis.readInt();

		    
		if (player.getDistanceSq(this.x, this.y, this.z) > 64)
			return;
		if (!player.worldObj.blockExists(this.x, this.y, this.z))
			return;
		if (EventUtils.cantBreak(player, this.x, this.y, this.z))
			return;
		    

		boolean result = player.worldObj.getBlock(this.x, this.y, this.z).onBlockActivated(player.worldObj, this.x, this.y, this.z, player, this.side, 1.0F, 1.0F, 1.0F);

		if (!result && itemStack != null && itemStack.getItem() instanceof ItemBlock)
		{
			itemStack.tryPlaceItemIntoWorld(player, player.worldObj, this.x, this.y, this.z, this.side, 1.0F, 1.0F, 1.0F);
			EntityLivingUtil.decrementCurrentSlot(player);
		}
	}

	@Override
	public void appendData(ByteBuf buffer) throws IOException
	{
		super.appendData(buffer);
		buffer.writeInt(this.side);
	}

}
