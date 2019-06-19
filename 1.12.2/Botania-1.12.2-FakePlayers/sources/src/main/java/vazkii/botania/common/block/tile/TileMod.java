/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Jan 21, 2014, 9:18:28 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import ru.will.git.botania.ModUtils;
import ru.will.git.eventhelper.fake.FakePlayerContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TileMod extends TileEntity
{
	    
	public final FakePlayerContainer fake = ModUtils.NEXUS_FACTORY.wrapFake(this);
	    

	@Override
	public boolean shouldRefresh(World world, BlockPos pos,
								 @Nonnull IBlockState oldState, @Nonnull IBlockState newState)
	{
		return oldState.getBlock() != newState.getBlock();
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagCompound ret = super.writeToNBT(nbt);
		this.writePacketNBT(ret);

		    
		this.fake.writeToNBT(nbt);
		    

		return ret;
	}

	@Nonnull
	@Override
	public final NBTTagCompound getUpdateTag()
	{
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.readPacketNBT(nbt);

		    
		this.fake.readFromNBT(nbt);
		    
	}

	public void writePacketNBT(NBTTagCompound cmp)
	{
	}

	public void readPacketNBT(NBTTagCompound cmp)
	{
	}

	@Override
	public final SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		this.writePacketNBT(tag);
		return new SPacketUpdateTileEntity(this.pos, -999, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
	{
		super.onDataPacket(net, packet);
		this.readPacketNBT(packet.getNbtCompound());
	}

}
