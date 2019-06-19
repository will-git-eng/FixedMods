package thaumic.tinkerer.common.block.tile;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.movable.IMovableTile;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import ru.will.git.ttinkerer.ModUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.block.mobilizer.BlockMobilizer;

public class TileEntityMobilizer extends TileEntity
{
	public boolean linked;

	public int firstRelayX;
	public int secondRelayX;

	public int firstRelayZ;
	public int secondRelayZ;
	public boolean dead = false;

    
    

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setBoolean("Linked", this.linked);

		nbt.setInteger("FirstRelayX", this.firstRelayX);
		nbt.setInteger("FirstRelayZ", this.firstRelayZ);

		nbt.setInteger("SecondRelayX", this.secondRelayX);
		nbt.setInteger("SecondRelayZ", this.secondRelayZ);

    
    
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		this.linked = nbt.getBoolean("Linked");

		this.firstRelayX = nbt.getInteger("FirstRelayX");
		this.firstRelayZ = nbt.getInteger("FirstRelayZ");

		this.secondRelayX = nbt.getInteger("SecondRelayX");
		this.secondRelayZ = nbt.getInteger("SecondRelayZ");

    
    
	}

	public void verifyRelay()
	{
		TileEntity te = this.worldObj.getTileEntity(this.firstRelayX, this.yCoord, this.firstRelayZ);
		if (te instanceof TileEntityRelay)
			((TileEntityRelay) te).verifyPartner();
		if (!(this.linked && te instanceof TileEntityRelay && ((TileEntityRelay) te).partnerX == this.secondRelayX && ((TileEntityRelay) te).partnerZ == this.secondRelayZ))
			this.linked = false;
	}

	@Override
	public void updateEntity()
    
		if (this.dead)
    
    
		if (this.linked && this.worldObj.getTotalWorldTime() % 100 == 0 && !this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
    
			int targetX = this.xCoord + this.movementDirection.offsetX;
    
			if (this.worldObj.getBlock(targetX, this.yCoord, targetZ) != Block.getBlockFromName("air") || this.worldObj.getBlock(targetX, this.yCoord + 1, targetZ) != Block.getBlockFromName("air"))
				this.movementDirection = this.movementDirection.getOpposite();
    
		if (this.linked && this.worldObj.getTotalWorldTime() % 100 == 1 && !this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
    
			int targetX = this.xCoord + this.movementDirection.offsetX;
    
			if (this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord) != ThaumicTinkerer.registry.getFirstBlockFromClass(BlockMobilizer.class))
    
    
				if (!this.worldObj.isRemote)
				{
					TileEntity passenger = this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);
    
					if (this.fake.cantBreak(targetX, this.yCoord, targetZ))
    
    
    
					Block passengerId = this.worldObj.getBlock(this.xCoord, this.yCoord + 1, this.zCoord);

					if (this.worldObj.isAirBlock(this.xCoord, this.yCoord + 1, this.zCoord) || passengerId.canPlaceBlockAt(this.worldObj, targetX, this.yCoord + 1, targetZ))
						if (passenger == null)
						{
							if (passengerId != Block.getBlockFromName("bedrock") && passengerId != Block.getBlockFromName(""))
    
								if (this.fake.cantBreak(targetX, this.yCoord + 1, targetZ))
    

								this.worldObj.setBlock(targetX, this.yCoord + 1, targetZ, passengerId, this.worldObj.getBlockMetadata(this.xCoord, this.yCoord + 1, this.zCoord), 3);
								if (passengerId != Block.getBlockFromName("air") && passengerId != Block.getBlockFromName("piston_head"))
    
									if (this.fake.cantBreak(this.xCoord, this.yCoord + 1, this.zCoord))
    

									this.worldObj.setBlock(this.xCoord, this.yCoord + 1, this.zCoord, Block.getBlockFromName("air"), 0, 2);
								}
    
						}
						else if (api != null)
						{
							if (api.registries().movable().askToMove(passenger))
    
								if (this.fake.cantBreak(targetX, this.yCoord + 1, targetZ))
									return;
								if (this.fake.cantBreak(this.xCoord, this.yCoord + 1, this.zCoord))
    

								this.worldObj.setBlock(targetX, this.yCoord + 1, targetZ, this.worldObj.getBlock(this.xCoord, this.yCoord + 1, this.zCoord), this.worldObj.getBlockMetadata(this.xCoord, this.yCoord + 1, this.zCoord), 3);
								passenger.invalidate();
								this.worldObj.setBlockToAir(this.xCoord, this.yCoord + 1, this.zCoord);
								api.registries().movable().getHandler(passenger).moveTile(passenger, this.worldObj, targetX, this.yCoord + 1, targetZ);
								api.registries().movable().doneMoving(passenger);
								passenger.validate();
    
						}
						else if (passenger instanceof IMovableTile || passenger.getClass().getName().startsWith("net.minecraft.tileentity"))
    
							if (this.fake.cantBreak(targetX, this.yCoord + 1, targetZ))
								return;
							if (this.fake.cantBreak(this.xCoord, this.yCoord + 1, this.zCoord))
    

							boolean imovable = passenger instanceof IMovableTile;
							if (imovable)
								((IMovableTile) passenger).prepareToMove();
							this.worldObj.setBlock(targetX, this.yCoord + 1, targetZ, this.worldObj.getBlock(this.xCoord, this.yCoord + 1, this.zCoord), this.worldObj.getBlockMetadata(this.xCoord, this.yCoord + 1, this.zCoord), 3);
							passenger.invalidate();
    
							Chunk c = this.worldObj.getChunkFromBlockCoords(targetX, targetZ);

							c.func_150812_a(targetX & 0xF, this.yCoord + 1, targetZ & 0xF, passenger);

							if (c.isChunkLoaded)
							{
								this.worldObj.addTileEntity(passenger);
								this.worldObj.markBlockForUpdate(targetX, this.yCoord + 1, targetZ);
							}
							if (imovable)
								((IMovableTile) passenger).doneMoving();
							passenger.validate();

    
    
					if (this.fake.cantBreak(targetX, this.yCoord, targetZ))
    

					this.invalidate();
					this.worldObj.removeTileEntity(this.xCoord, this.yCoord, this.zCoord);
					this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, Block.getBlockFromName("air"), 0, 2);
					this.worldObj.setBlock(targetX, this.yCoord, targetZ, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockMobilizer.class));

					int oldX = this.xCoord;
					int oldZ = this.zCoord;

					this.xCoord = targetX;
					this.zCoord = targetZ;
					this.validate();
					this.worldObj.addTileEntity(this);
					this.worldObj.removeTileEntity(oldX, this.yCoord, oldZ);

					this.worldObj.notifyBlockChange(oldX, this.yCoord, oldZ, Block.getBlockFromName("air"));

				}

		}
	}

}
