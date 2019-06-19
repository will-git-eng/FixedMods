package com.rwtema.extrautils.tileentity.transfernodes;

import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.helper.XURandom;
import com.rwtema.extrautils.network.NetworkHandler;
import com.rwtema.extrautils.tileentity.enderquarry.IChunkLoad;
import com.rwtema.extrautils.tileentity.transfernodes.nodebuffer.INode;
import com.rwtema.extrautils.tileentity.transfernodes.nodebuffer.INodeBuffer;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IPipe;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IPipeCosmetic;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.StdPipes;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public abstract class TileEntityTransferNode extends TileEntity implements IPipe, INode, IPipeCosmetic, IChunkLoad
{
	public static int baseMaxCoolDown = 20;
	public int pipe_x = 0;
	public int pipe_y = 0;
	public int pipe_z = 0;
	public int pipe_dir = 6;
	public int pipe_type = 0;
	public float pr = 1.0F;
	public float pg = 0.0F;
	public float pb = 0.0F;
	public TileEntityTransferNodeUpgradeInventory upgrades = new TileEntityTransferNodeUpgradeInventory(6, this);
	public boolean isReceiver = false;
	public String type;
	public INodeBuffer buffer;
	public boolean powered = false;
	public boolean init = false;
	public TileEntityTransferNode.SearchType searchType = TileEntityTransferNode.SearchType.RANDOM_WALK;
	public HashSet<TileEntityTransferNode.SearchPosition> prevSearch = new HashSet();
	public ArrayList<TileEntityTransferNode.SearchPosition> toSearch = new ArrayList();
	public Random rand = XURandom.getInstance();
	protected int coolDown;
	protected int maxCoolDown = 384;
	protected int stepCoolDown = 1;
	protected boolean oldVersion = false;
	boolean joinedWorld = false;
	public boolean catchingDirty = false;
	public boolean isDirty = false;
	int ptype = 0;

	public TileEntityTransferNode(String s, INodeBuffer buffer)
	{
		this.type = s;
		this.buffer = buffer;
	}

	public int upgradeNo(int n)
	{
		if (ExtraUtils.nodeUpgrade == null)
			return 0;
		else
		{
			int k = 0;

			for (int i = 0; i < this.upgrades.getSizeInventory(); ++i)
			{
				ItemStack item = this.upgrades.getStackInSlot(i);
				if (item != null && this.upgrades.hasUpgradeNo(item) && this.upgrades.getUpgradeNo(item) == n)
					k += this.upgrades.getStackInSlot(i).stackSize;
			}

			return k;
		}
	}

	public void calcUpgradeModifiers()
	{
		if (this.worldObj != null && !this.worldObj.isRemote)
		{
			if (this.isReceiver)
				TransferNodeEnderRegistry.clearTileRegistrations(this.buffer);

			this.isReceiver = false;
			this.stepCoolDown = 2;
			TileEntityTransferNode.SearchType prevSearchType = this.searchType;
			this.searchType = TileEntityTransferNode.SearchType.RANDOM_WALK;
			int prev_pipe_type = this.pipe_type;
			if (this.upgrades.isValidPipeType(this.pipe_type))
				this.pipe_type = 0;

			for (int i = 0; i < this.upgrades.getSizeInventory(); ++i)
			{
				if (this.upgrades.getStackInSlot(i) != null && ExtraUtils.nodeUpgrade != null && this.upgrades.getStackInSlot(i).getItem() == ExtraUtils.nodeUpgrade)
				{
					if (this.upgrades.getStackInSlot(i).getItemDamage() == 0)
						for (int k = 0; k < this.upgrades.getStackInSlot(i).stackSize && this.stepCoolDown < this.maxCoolDown; ++k)
						{
							++this.stepCoolDown;
						}
					else if (this.upgrades.getStackInSlot(i).getItemDamage() == 6 && this.upgrades.getStackInSlot(i).hasDisplayName())
					{
						TransferNodeEnderRegistry.registerTile(new Frequency(this.upgrades.getStackInSlot(i)), this.buffer);
						this.isReceiver = true;
					}
					else if (this.upgrades.getStackInSlot(i).getItemDamage() == 7)
						this.searchType = SearchType.DEPTH_FIRST;
					else if (this.upgrades.getStackInSlot(i).getItemDamage() == 8)
						this.searchType = SearchType.BREADTH_FIRST;
				}
				else if (this.upgrades.pipeType(this.upgrades.getStackInSlot(i)) > 0)
					this.pipe_type = this.upgrades.pipeType(this.upgrades.getStackInSlot(i));
			}

			if (prevSearchType != this.searchType)
				this.resetSearch();

			if (prev_pipe_type != this.pipe_type)
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

		}
	}

	@Override
	public void updateEntity()
	{
		if (this.worldObj != null && !this.worldObj.isRemote)
		{
			this.catchingDirty = true;
			if (!this.joinedWorld)
			{
				this.joinedWorld = true;
				this.onWorldJoin();
			}

			this.processBuffer();
			if (ExtraUtils.nodeUpgrade != null)
				this.sendEnder();

			this.catchingDirty = false;
			if (this.isDirty)
			{
				super.markDirty();
				this.isDirty = false;
			}

		}
	}

	@Override
	public void markDirty()
	{
		if (this.catchingDirty)
			this.isDirty = true;
		else
			super.markDirty();

	}

	public abstract void processBuffer();

	public void sendEnder()
	{
		for (int i = 0; i < this.upgrades.getSizeInventory(); ++i)
		{
			ItemStack item = this.upgrades.getStackInSlot(i);
			if (item != null && item.getItem() == ExtraUtils.nodeUpgrade && item.getItemDamage() == 5)
				TransferNodeEnderRegistry.doTransfer(this.buffer, new Frequency(item), item.stackSize);
		}

	}

	public void updateRedstone()
	{
		if (this.worldObj != null)
		{
			TileEntity tile = this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord);
			if (tile instanceof INode)
				this.powered = ((INode) tile).recalcRedstone();

		}
	}

	@Override
	public boolean recalcRedstone()
	{
		return this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public boolean isPowered()
	{
		if (!this.init && this.worldObj != null)
		{
			this.powered = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
			this.init = true;
		}

		return this.powered == (this.upgradeNo(-2) == 0);
	}

	@Override
	public boolean checkRedstone()
	{
		if (this.isPowered())
		{
			this.resetSearch();
			return true;
		}
		else
			return false;
	}

	@Override
	public int getNodeX()
	{
		return this.xCoord;
	}

	@Override
	public int getNodeY()
	{
		return this.yCoord;
	}

	@Override
	public int getNodeZ()
	{
		return this.zCoord;
	}

	@Override
	public ForgeDirection getNodeDir()
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata() % 6);
	}

	@Override
	public int getPipeX()
	{
		return this.pipe_x;
	}

	@Override
	public int getPipeY()
	{
		return this.pipe_y;
	}

	@Override
	public int getPipeZ()
	{
		return this.pipe_z;
	}

	@Override
	public int getPipeDir()
	{
		return this.pipe_dir;
	}

	@Override
	public List<ItemStack> getUpgrades()
	{
		List<ItemStack> u = new ArrayList();

		for (int i = 0; i < this.upgrades.getSizeInventory(); ++i)
		{
			if (this.upgrades.getStackInSlot(i) != null)
				u.add(this.upgrades.getStackInSlot(i));
		}

		return u;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		if (tag.hasKey("pipe_x"))
			this.pipe_x = tag.getInteger("pipe_x");
		else
			this.pipe_x = 0;

		if (tag.hasKey("pipe_y"))
			this.pipe_y = tag.getInteger("pipe_y");
		else
			this.pipe_y = 0;

		if (tag.hasKey("pipe_z"))
			this.pipe_z = tag.getInteger("pipe_z");
		else
			this.pipe_z = 0;

		this.pipe_dir = tag.getInteger("pipe_dir");
		if (tag.hasKey("pipe_type"))
			this.pipe_type = tag.getByte("pipe_type");
		else
			this.pipe_type = 0;

		for (int i = 0; i < this.upgrades.getSizeInventory(); ++i)
		{
			if (tag.hasKey("upgrade_" + i))
				this.upgrades.setInventorySlotContents(i, ItemStack.loadItemStackFromNBT(tag.getCompoundTag("upgrade_" + i)));
			else
				this.upgrades.setInventorySlotContents(i, null);
		}

		this.buffer.readFromNBT(tag);
		NBTTagCompound t = tag.getCompoundTag("prevSearch");
		int s = t.getInteger("size");

		for (int i = 0; i < s; ++i)
		{
			this.prevSearch.add(SearchPosition.loadFromTag(t.getCompoundTag(Integer.toString(i))));
		}

		t = tag.getCompoundTag("toSearch");
		s = t.getInteger("size");

		for (int i = 0; i < s; ++i)
		{
			this.toSearch.add(SearchPosition.loadFromTag(t.getCompoundTag(Integer.toString(i))));
		}

		if (tag.getByte("version") == 0)
			this.oldVersion = true;

	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
		if (this.pipe_x != 0)
			par1NBTTagCompound.setInteger("pipe_x", this.pipe_x);

		if (this.pipe_y != 0)
			par1NBTTagCompound.setInteger("pipe_y", this.pipe_y);

		if (this.pipe_z != 0)
			par1NBTTagCompound.setInteger("pipe_z", this.pipe_z);

		if (this.pipe_dir != 0)
			par1NBTTagCompound.setInteger("pipe_dir", this.pipe_dir);

		if (this.pipe_type != 0)
			par1NBTTagCompound.setByte("pipe_type", (byte) this.pipe_type);

		for (int i = 0; i < this.upgrades.getSizeInventory(); ++i)
		{
			if (this.upgrades.getStackInSlot(i) != null)
			{
				NBTTagCompound newItem = new NBTTagCompound();
				this.upgrades.getStackInSlot(i).writeToNBT(newItem);
				par1NBTTagCompound.setTag("upgrade_" + i, newItem);
			}
		}

		this.buffer.writeToNBT(par1NBTTagCompound);
		NBTTagCompound t2 = new NBTTagCompound();
		t2.setInteger("size", this.prevSearch.size());
		int i = 0;

		for (TileEntityTransferNode.SearchPosition p : this.prevSearch)
		{
			t2.setTag(Integer.toString(i), p.getTag());
			++i;
		}

		NBTTagCompound t3 = new NBTTagCompound();
		t3.setInteger("size", this.toSearch.size());

		for (i = 0; i < this.toSearch.size(); ++i)
		{
			t3.setTag(Integer.toString(i), this.toSearch.get(i).getTag());
		}

		par1NBTTagCompound.setTag("prevSearch", t2);
		par1NBTTagCompound.setTag("toSearch", t3);
		if (!this.oldVersion)
			par1NBTTagCompound.setByte("version", (byte) 1);

	}

	public int getType()
	{
		if (this.ptype == 0)
		{
			this.ptype = -1;
			Class clazz = this.getClass();
			if (clazz.equals(TileEntityTransferNodeInventory.class))
				this.ptype = 3;
			else if (clazz.equals(TileEntityTransferNodeLiquid.class))
				this.ptype = 4;
			else if (clazz.equals(TileEntityRetrievalNodeInventory.class))
				this.ptype = 5;
			else if (clazz.equals(TileEntityRetrievalNodeLiquid.class))
				this.ptype = 6;
			else if (clazz.equals(TileEntityTransferNodeEnergy.class) || clazz.equals(TileEntityTransferNodeHyperEnergy.class))
				this.ptype = 7;
		}

		return this.ptype;
	}

	public void sendParticleUpdate()
	{
		if (!ExtraUtils.disableNodeParticles && this.joinedWorld)
			if (this.upgradeNo(-1) != 0)
				NetworkHandler.sendParticleEvent(this.worldObj, this.getType(), this.xCoord + this.pipe_x, this.yCoord + this.pipe_y, this.zCoord + this.pipe_z);
	}

	public void sendEnderParticleUpdate()
	{
		if (!ExtraUtils.disableNodeParticles && this.joinedWorld)
			if (this.upgradeNo(-1) != 0)
				NetworkHandler.sendParticleEvent(this.worldObj, 8, this.xCoord + this.pipe_x, this.yCoord + this.pipe_y, this.zCoord + this.pipe_z);
	}

	public boolean handleInventories()
	{
		boolean advance = false;
		boolean rr = this.upgradeNo(9) != 0;
		boolean ss = this.buffer.shouldSearch();
		if (ss)
		{
			IPipe pipe = TNHelper.getPipe(this.worldObj, this.xCoord + this.pipe_x, this.yCoord + this.pipe_y, this.zCoord + this.pipe_z);
			if (pipe == null)
			{
				this.resetSearch();
				return false;
			}

			this.sendParticleUpdate();
			advance = pipe.transferItems(this.worldObj, this.xCoord + this.pipe_x, this.yCoord + this.pipe_y, this.zCoord + this.pipe_z, ForgeDirection.getOrientation(this.pipe_dir), this.buffer);
		}

		if (rr)
			return ss;
		else if (!this.buffer.shouldSearch())
		{
			this.resetSearch();
			return false;
		}
		else
			return advance;
	}

	public boolean advPipeSearch()
	{
		if (this.pipe_dir == 6)
			this.pipe_dir = this.getNodeDir().getOpposite().ordinal();

		IPipe pipeBlock = TNHelper.getPipe(this.worldObj, this.xCoord + this.pipe_x, this.yCoord + this.pipe_y, this.zCoord + this.pipe_z);
		if (pipeBlock != null)
		{
			this.prevSearch.add(new TileEntityTransferNode.SearchPosition(this.pipe_x, this.pipe_y, this.pipe_z, ForgeDirection.getOrientation(this.pipe_dir)));
			ArrayList<ForgeDirection> dirs = pipeBlock.getOutputDirections(this.worldObj, this.xCoord + this.pipe_x, this.yCoord + this.pipe_y, this.zCoord + this.pipe_z, ForgeDirection.getOrientation(this.pipe_dir), this.buffer);
			label0:
			switch (this.searchType)
			{
				case RANDOM_WALK:
					if (!dirs.isEmpty())
						this.toSearch.add(new SearchPosition(this.pipe_x, this.pipe_y, this.pipe_z, dirs.get(0)).adv());
					break;
				case DEPTH_FIRST:
					Iterator i$ = dirs.iterator();

					while (true)
					{
						if (!i$.hasNext())
							break label0;

						ForgeDirection d = (ForgeDirection) i$.next();
						TileEntityTransferNode.SearchPosition s = new SearchPosition(this.pipe_x, this.pipe_y, this.pipe_z, d).adv();
						if (!this.prevSearch.contains(s) && !this.toSearch.contains(s))
							if (this.toSearch.isEmpty())
								this.toSearch.add(new SearchPosition(this.pipe_x, this.pipe_y, this.pipe_z, d).adv());
							else
								this.toSearch.add(0, new SearchPosition(this.pipe_x, this.pipe_y, this.pipe_z, d).adv());
					}
				case BREADTH_FIRST:
					for (ForgeDirection d : dirs)
					{
						TileEntityTransferNode.SearchPosition s = new SearchPosition(this.pipe_x, this.pipe_y, this.pipe_z, d).adv();
						if (!this.prevSearch.contains(s) && !this.toSearch.contains(s))
							this.toSearch.add(s);
					}
			}
		}

		if (!this.loadNextPos())
		{
			this.pipe_dir = this.getNodeDir().getOpposite().ordinal();
			this.resetSearch();
			return false;
		}
		else
			return true;
	}

	public void resetSearch()
	{
		if (this.pipe_x != 0 || this.pipe_y != 0 || this.pipe_z != 0 || !this.prevSearch.isEmpty())
		{
			this.pipe_x = 0;
			this.pipe_y = 0;
			this.pipe_z = 0;
			this.pipe_dir = -1;
			this.toSearch.clear();
			this.prevSearch.clear();
		}

	}

	public boolean loadNextPos()
	{
		if (this.toSearch.isEmpty())
			return false;
		else
		{
			TileEntityTransferNode.SearchPosition pos = this.toSearch.remove(0);
			if (this.searchType != TileEntityTransferNode.SearchType.RANDOM_WALK)
			{
				while (this.prevSearch.contains(pos) && !this.toSearch.isEmpty())
				{
					pos = this.toSearch.remove(0);
				}

				if (this.prevSearch.contains(pos))
					return false;

				this.prevSearch.add(pos.copy());
			}

			this.pipe_x = pos.x;
			this.pipe_y = pos.y;
			this.pipe_z = pos.z;
			this.pipe_dir = pos.side.ordinal();
			return true;
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound t = new NBTTagCompound();
		t.setByte("d", (byte) this.pipe_type);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 4, t);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		if (this.worldObj.isRemote)
			if (pkt.func_148857_g().hasKey("d"))
			{
				if (pkt.func_148857_g().getByte("d") != this.pipe_type)
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

				this.pipe_type = pkt.func_148857_g().getByte("d");
			}
	}

	public boolean isUseableByPlayer(EntityPlayer player)
    
    
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		this.onWorldLeave();
	}

	@Override
	public void onChunkLoad()
	{
	}

	public void onWorldJoin()
	{
		this.buffer.setNode(this);
		this.calcUpgradeModifiers();
	}

	public void onWorldLeave()
	{
		TransferNodeEnderRegistry.clearTileRegistrations(this.buffer);
	}

	@Override
	public ArrayList<ForgeDirection> getOutputDirections(IBlockAccess world, int x, int y, int z, ForgeDirection dir, INodeBuffer buffer)
	{
		return StdPipes.getPipeType(this.pipe_type).getOutputDirections(world, x, y, z, dir, buffer);
	}

	@Override
	public boolean transferItems(IBlockAccess world, int x, int y, int z, ForgeDirection dir, INodeBuffer buffer)
	{
		return StdPipes.getPipeType(this.pipe_type).transferItems(world, x, y, z, dir, buffer);
	}

	@Override
	public boolean canInput(IBlockAccess world, int x, int y, int z, ForgeDirection dir)
	{
		return dir != this.getNodeDir() && StdPipes.getPipeType(this.pipe_type).canInput(world, x, y, z, dir);
	}

	@Override
	public boolean canOutput(IBlockAccess world, int x, int y, int z, ForgeDirection dir)
	{
		return dir != this.getNodeDir() && StdPipes.getPipeType(this.pipe_type).canOutput(world, x, y, z, dir);
	}

	@Override
	public int limitTransfer(TileEntity dest, ForgeDirection side, INodeBuffer buffer)
	{
		return StdPipes.getPipeType(this.pipe_type).limitTransfer(dest, side, buffer);
	}

	@Override
	public IInventory getFilterInventory(IBlockAccess world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public boolean shouldConnectToTile(IBlockAccess world, int x, int y, int z, ForgeDirection dir)
	{
		return dir != this.getNodeDir() && StdPipes.getPipeType(this.pipe_type).shouldConnectToTile(world, x, y, z, dir);
	}

	@Override
	public IIcon baseTexture()
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(this.pipe_type)).baseTexture();
	}

	@Override
	public IIcon pipeTexture(ForgeDirection dir, boolean blocked)
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(this.pipe_type)).pipeTexture(dir, blocked);
	}

	@Override
	public IIcon invPipeTexture(ForgeDirection dir)
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(this.pipe_type)).invPipeTexture(dir);
	}

	@Override
	public IIcon socketTexture(ForgeDirection dir)
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(this.pipe_type)).socketTexture(dir);
	}

	@Override
	public String getPipeType()
	{
		return StdPipes.getPipeType(this.pipe_type).getPipeType();
	}

	@Override
	public float baseSize()
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(this.pipe_type)).baseSize();
	}

	@Override
	public TileEntityTransferNode getNode()
	{
		return this;
	}

	@Override
	public String getNodeType()
	{
		return this.type;
	}

	public void update()
	{
	}

	public boolean initDirection()
	{
		return false;
	}

	@Override
	public void bufferChanged()
	{
		this.markDirty();
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		this.onWorldLeave();
	}

	public static class SearchPosition
	{
		public ForgeDirection side = ForgeDirection.UNKNOWN;
		public int x;
		public int y;
		public int z;

		public SearchPosition(int par1, int par2, int par3, ForgeDirection par4)
		{
			this.x = par1;
			this.y = par2;
			this.z = par3;
			this.side = par4;
		}

		public static byte getOrd(ForgeDirection e)
		{
			return (byte) e.ordinal();
		}

		public static TileEntityTransferNode.SearchPosition loadFromTag(NBTTagCompound tag)
		{
			return new TileEntityTransferNode.SearchPosition(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), ForgeDirection.getOrientation(tag.getByte("side")));
		}

		public TileEntityTransferNode.SearchPosition copy()
		{
			return new TileEntityTransferNode.SearchPosition(this.x, this.y, this.z, this.side);
		}

		public String toString()
		{
			return "SearchPosition{side=" + this.side + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + '}';
		}

		public TileEntityTransferNode.SearchPosition adv()
		{
			this.x += this.side.offsetX;
			this.y += this.side.offsetY;
			this.z += this.side.offsetZ;
			return this;
		}

		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			else if (o != null && this.getClass() == o.getClass())
			{
				TileEntityTransferNode.SearchPosition that = (TileEntityTransferNode.SearchPosition) o;
				return this.effectiveX() == that.effectiveX() && this.effectiveY() == that.effectiveY() && this.effectiveZ() == that.effectiveZ();
			}
			else
				return false;
		}

		public int effectiveX()
		{
			return this.x * 2 - this.side.offsetX;
		}

		public int effectiveY()
		{
			return this.y * 2 - this.side.offsetY;
		}

		public int effectiveZ()
		{
			return this.z * 2 - this.side.offsetZ;
		}

		public int hashCode()
		{
			int result = this.effectiveX();
			result = 31 * result + this.effectiveY();
			result = 31 * result + this.effectiveZ();
			return result;
		}

		public NBTTagCompound getTag()
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("x", this.x);
			tag.setInteger("y", this.y);
			tag.setInteger("z", this.z);
			tag.setByte("side", (byte) this.side.ordinal());
			return tag;
		}
	}

	public enum SearchType
	{
		RANDOM_WALK,
		DEPTH_FIRST,
		BREADTH_FIRST
	}
}
