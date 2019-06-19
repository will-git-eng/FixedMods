package ic2.core.block;

import java.util.List;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import ru.will.git.ic2.ModUtils;

import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkUpdateListener;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.ITickCallback;
import ic2.core.network.NetworkManager;
import ic2.core.network.internal.INetworkGuiDataProvider;
import ic2.core.util.FilteredList;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class TileEntityBlock extends TileEntity implements INetworkDataProvider, INetworkUpdateListener, IWrenchable, INetworkGuiDataProvider
{
	private boolean active = false;
	private short facing = 0;
	public boolean prevActive = false;
	public short prevFacing = 0;
	public boolean loaded = false;
	private final boolean isServer = IC2.platform.isSimulating();
	private final NetworkManager manager = IC2.network.get();
	private final List<String> networkFields = new FilteredList();
    
    

	public TileEntityBlock()
	{
		this.addNetworkFields(new String[] { "active", "facing" });
	}

	protected void addNetworkFields(String... fields)
	{
		for (String field : fields)
			this.networkFields.add(field);
	}

	protected void addGuiFields(String... fields)
	{
		for (String field : fields)
			this.guiFields.add(field);

	}

	@Override
	public List<String> getGuiFields()
	{
		return this.guiFields;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		return this.networkFields;
	}

	@Override
	public void validate()
	{
		super.validate();
		if (!this.loaded)
			if (!this.isInvalid() && this.worldObj != null)
			{
				if (this.isSimulating())
					IC2.addSingleTickCallback(this.worldObj, new ITickCallback()
					{
						@Override
						public void tickCallback(World world)
						{
							TileEntityBlock.this.onLoaded();
						}
					});
				else
					this.onLoaded();
			}
			else
				IC2.log.warn(this + " (" + this.xCoord + "," + this.yCoord + "," + this.zCoord + ") was not added, isInvalid=" + this.isInvalid() + ", worldObj=" + this.worldObj);

	}

	@Override
	public void invalidate()
	{
		if (this.loaded)
			this.onUnloaded();

		super.invalidate();
	}

	@Override
	public void onChunkUnload()
	{
		if (this.loaded)
			this.onUnloaded();

		super.onChunkUnload();
	}

	public void onLoaded()
	{
		if (this.isRendering())
			IC2.network.get().requestInitialData(this);

		this.loaded = true;
	}

	public void onUnloaded()
	{
		this.loaded = false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		short short1 = nbttagcompound.getShort("facing");
		this.facing = short1;
    
    
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
    
    
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public boolean getActive()
	{
		return this.active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
		if (this.prevActive != active)
			this.getNetwork().updateTileEntityField(this, "active");

		this.prevActive = active;
	}

	public void setActiveWithoutNotify(boolean active)
	{
		this.active = active;
		this.prevActive = active;
	}

	@Override
	public short getFacing()
	{
		return this.facing;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("active") && this.prevActive != this.active || field.equals("facing") && this.prevFacing != this.facing)
		{
			Block block = this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord);
			if (block == null)
				System.out.println("[IC2] Invalid TE at " + this.xCoord + "/" + this.yCoord + "/" + this.zCoord + ", no corresponding block");
			else
			{
				boolean newActive = this.active;
				short newFacing = this.facing;
				this.active = this.prevActive;
				this.facing = this.prevFacing;
				IIcon[] textureIndex = new IIcon[6];

				for (int side = 0; side < 6; ++side)
					textureIndex[side] = IC2.platform.getBlockTexture(block, this.worldObj, this.xCoord, this.yCoord, this.zCoord, side);

				this.active = newActive;
				this.facing = newFacing;

				for (int side = 0; side < 6; ++side)
				{
					IIcon newTextureIndex = IC2.platform.getBlockTexture(block, this.worldObj, this.xCoord, this.yCoord, this.zCoord, side);
					if (textureIndex[side] != newTextureIndex)
					{
						this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
						break;
					}
				}
			}

			this.prevActive = this.active;
			this.prevFacing = this.facing;
		}

	}

	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return false;
	}

	@Override
	public void setFacing(short facing)
	{
		this.facing = facing;
		if (this.prevFacing != facing)
			this.getNetwork().updateTileEntityField(this, "facing");

		this.prevFacing = facing;
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer)
	{
		return true;
	}

	@Override
	public float getWrenchDropRate()
	{
		return 1.0F;
	}

	@Override
	public ItemStack getWrenchDrop(EntityPlayer entityPlayer)
	{
		return new ItemStack(this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord), 1, this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord));
	}

	public void onBlockBreak(Block a, int b)
	{
	}

	public boolean hasTileMeta()
	{
		return false;
	}

	public int getTileMeta()
	{
		return 0;
	}

	public boolean isSimulating()
	{
		return this.isServer;
	}

	public boolean isRendering()
	{
		return !this.isServer;
	}

	public NetworkManager getNetwork()
	{
		return this.manager;
	}

	protected NBTTagCompound getNBT(NBTTagCompound data, String tag)
	{
		if (!data.hasKey(tag))
			data.setTag(tag, new NBTTagCompound());

		return data.getCompoundTag(tag);
	}
}
