    
package thaumic.tinkerer.common.block.tile.transvector;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import ru.will.git.ttinkerer.ModUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumic.tinkerer.common.block.tile.TileCamo;

public abstract class TileTransvector extends TileCamo
{
	private static final String TAG_X_TARGET = "xt";
	private static final String TAG_Y_TARGET = "yt";
	private static final String TAG_Z_TARGET = "zt";
	private static final String TAG_CHEATY_MODE = "cheatyMode";

	public int x = 0, y = -1, z = 0;
    
    

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound cmp)
	{
		super.writeCustomNBT(cmp);

		cmp.setInteger(TAG_X_TARGET, this.x);
		cmp.setInteger(TAG_Y_TARGET, this.y);
		cmp.setInteger(TAG_Z_TARGET, this.z);
    
    
	}

	@Override
	public void readCustomNBT(NBTTagCompound cmp)
	{
		super.readCustomNBT(cmp);

		this.x = cmp.getInteger(TAG_X_TARGET);
		this.y = cmp.getInteger(TAG_Y_TARGET);
		this.z = cmp.getInteger(TAG_Z_TARGET);
    
    
	}

	public final TileEntity getTile()
	{
		if (!this.worldObj.blockExists(this.x, this.y, this.z))
			return null;

		TileEntity tile = this.worldObj.getTileEntity(this.x, this.y, this.z);

		if (tile == null && this.tileRequiredAtLink() || (Math.abs(this.x - this.xCoord) > this.getMaxDistance() || Math.abs(this.y - this.yCoord) > this.getMaxDistance() || Math.abs(this.z - this.zCoord) > this.getMaxDistance()) && !this.cheaty)
		{
			this.y = -1;
			return null;
		}

		return tile;
	}

	public abstract int getMaxDistance();

	boolean tileRequiredAtLink()
	{
		return !this.cheaty;
	}

}
