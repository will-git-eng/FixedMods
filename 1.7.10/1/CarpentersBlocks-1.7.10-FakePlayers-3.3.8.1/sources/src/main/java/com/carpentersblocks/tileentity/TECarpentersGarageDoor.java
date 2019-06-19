package com.carpentersblocks.tileentity;

import com.carpentersblocks.data.GarageDoor;
import ru.will.git.carpentersblocks.ModUtils;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TECarpentersGarageDoor extends TEBase
{
	    
	public final FakePlayerContainer fake = new FakePlayerContainerTileEntity(ModUtils.profile, this);

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		this.fake.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.fake.readFromNBT(nbt);
	}
	    

	@Override
	/**
	 * Garage door state change sounds are handled strictly client-side so that
	 * only the nearest state change is audible.
	 *
	 * @param net
	 *            The NetworkManager the packet originated from
	 * @param pkt
	 *            The data packet
	 */
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		if (this.getWorldObj().isRemote)
		{
			GarageDoor data = GarageDoor.INSTANCE;
			int oldState = data.getState(this);
			super.onDataPacket(net, pkt);
			if (data.getState(this) != oldState)
				data.playStateChangeSound(this);
		}
	}
}
