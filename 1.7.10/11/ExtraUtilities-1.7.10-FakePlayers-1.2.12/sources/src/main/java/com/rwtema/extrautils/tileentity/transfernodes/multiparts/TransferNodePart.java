package com.rwtema.extrautils.tileentity.transfernodes.multiparts;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.ISidedHollowConnect;
import codechicken.multipart.RedstoneInteractions;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.scalatraits.TRedstoneTile;
import ru.will.git.extrautilities.SafeInventoryWrapper;
import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.ExtraUtilsMod;
import com.rwtema.extrautils.block.Box;
import com.rwtema.extrautils.block.BoxModel;
import com.rwtema.extrautils.helper.XUHelper;
import com.rwtema.extrautils.tileentity.transfernodes.BlockTransferNode;
import com.rwtema.extrautils.tileentity.transfernodes.TileEntityTransferNode;
import com.rwtema.extrautils.tileentity.transfernodes.nodebuffer.INode;
import com.rwtema.extrautils.tileentity.transfernodes.nodebuffer.INodeBuffer;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IPipeCosmetic;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.StdPipes;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import scala.util.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
    
public abstract class TransferNodePart extends MCMetaTilePart
		implements INode, IPipeCosmetic, ISidedHollowConnect, TSlottedPart, TFacePart, SafeInventoryWrapper.SafeChecker
{
	public static Random rand = new Random();
	private static DummyPipePart[] dummyPipes = new DummyPipePart[6];
	public TileEntityTransferNode node;
	public boolean valid = true;
	public boolean init = false;
	public int blockMasks = -1;
	public byte[] flagmasks = { (byte) 1, (byte) 2, (byte) 4, (byte) 8, (byte) 16, (byte) 32 };
    
	@Override
	public boolean isUseableByPlayer0(EntityPlayer player)
	{
		TileEntity tile = this.tile();
		if (tile == null)
			return false;
		int x = tile.xCoord;
		int y = tile.yCoord;
		int z = tile.zCoord;
		return tile.getWorldObj().getTileEntity(x, y, z) == tile && player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) <= 64;
    

	public TransferNodePart(int meta, TileEntityTransferNode node)
	{
		super(meta);
		this.id = rand.nextInt();
		this.node = node;
		node.blockMetadata = meta;
	}

	public TransferNodePart(TileEntityTransferNode node)
	{
		this.id = rand.nextInt();
		this.node = node;
		node.blockMetadata = this.meta;
	}

	@Override
	public TileEntity getBlockTile()
	{
		return this.tile();
	}

	@Override
	public int getHollowSize(int side)
	{
		return 6;
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		return Arrays.asList(new ItemStack(this.getBlock(), 1, this.getBlock().damageDropped(this.meta)));
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return new ItemStack(this.getBlock(), 1, this.getBlock().damageDropped(this.getMetadata()));
	}

	@Override
	public void bufferChanged()
	{
		this.getNode().bufferChanged();
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		if (this.getWorld().isRemote)
			return true;
		else if (XUHelper.isWrench(item))
		{
			int newmetadata = StdPipes.getNextPipeType(this.getWorld(), part.blockX, part.blockY, part.blockZ, this.getNode().pipe_type);
			this.getNode().pipe_type = (byte) newmetadata;
			this.sendDescUpdate();
			return true;
		}
		else
		{
			player.openGui(ExtraUtilsMod.instance, 0, this.getWorld(), this.x(), this.y(), this.z());
			return true;
		}
	}

	@Override
	public void onRemoved()
	{
		if (!this.getWorld().isRemote)
		{
			List<ItemStack> drops = new ArrayList();

			for (int i = 0; i < this.node.upgrades.getSizeInventory(); ++i)
			{
				if (this.node.upgrades.getStackInSlot(i) != null)
					drops.add(this.node.upgrades.getStackInSlot(i));
			}

			this.tile().dropItems(drops);
		}

	}

	@Override
	public void onWorldJoin()
	{
		if (this.getWorld() != null)
			this.node.setWorldObj(this.getWorld());

		this.node.xCoord = this.x();
		this.node.yCoord = this.y();
		this.node.zCoord = this.z();
		this.node.onWorldJoin();
		this.reloadBlockMasks();
	}

	@Override
	public void onWorldSeparate()
	{
		this.node.invalidate();
	}

	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		ArrayList<AxisAlignedBB> t = new ArrayList();
		ArrayList<Cuboid6> t2 = new ArrayList();
		ExtraUtils.transferNode.addCollisionBoxesToList(this.getWorld(), this.x(), this.y(), this.z(), AxisAlignedBB.getBoundingBox((double) this.x(), (double) this.y(), (double) this.z(), (double) (this.x() + 1), (double) (this.y() + 1), (double) (this.z() + 1)), t, null);

		for (AxisAlignedBB aT : t)
		{
			t2.add(new Cuboid6(aT.minX, aT.minY, aT.minZ, aT.maxX, aT.maxY, aT.maxZ).sub(new Vector3((double) this.x(), (double) this.y(), (double) this.z())));
		}

		return t2;
	}

	@Override
	public void save(NBTTagCompound tag)
	{
		super.save(tag);
		NBTTagCompound subtag = new NBTTagCompound();
		this.node.writeToNBT(subtag);
		tag.setTag("node", subtag);
	}

	@Override
	public void load(NBTTagCompound tag)
	{
		super.load(tag);
		this.node.readFromNBT(tag.getCompoundTag("node"));
	}

	@Override
	public boolean doesTick()
	{
		return true;
	}

	@Override
	public void update()
	{
		if (this.node != null && !this.world().isRemote)
		{
			this.node.blockMetadata = this.meta;
			if (this.getWorld().getTileEntity(this.x(), this.y(), this.z()) == this.tile())
			{
				if (this.node.getWorldObj() == null)
					this.onWorldJoin();

				this.node.updateEntity();
			}
		}

	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(this.meta);
		packet.writeByte(this.node.pipe_type);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		this.meta = packet.readByte();
		this.node.pipe_type = packet.readByte();
	}

	@Override
	public Block getBlock()
	{
		return ExtraUtils.transferNode;
	}

	@Override
	public int getNodeX()
	{
		return this.node.getNodeX();
	}

	@Override
	public int getNodeY()
	{
		return this.node.getNodeY();
	}

	@Override
	public int getNodeZ()
	{
		return this.node.getNodeZ();
	}

	@Override
	public ForgeDirection getNodeDir()
	{
		this.node.blockMetadata = this.meta;
		return this.node.getNodeDir();
	}

	@Override
	public int getPipeX()
	{
		return this.node.getPipeX();
	}

	@Override
	public int getPipeY()
	{
		return this.node.getPipeY();
	}

	@Override
	public int getPipeZ()
	{
		return this.node.getPipeZ();
	}

	@Override
	public int getPipeDir()
	{
		return this.node.getPipeDir();
	}

	@Override
	public List<ItemStack> getUpgrades()
	{
		return this.node.getUpgrades();
	}

	@Override
	public boolean checkRedstone()
	{
		return this.node.checkRedstone();
	}

	@Override
	public BoxModel getModel(ForgeDirection dir)
	{
		return this.node.getModel(dir);
	}

	@Override
	public String getNodeType()
	{
		return this.node.getNodeType();
	}

	@Override
	public boolean transferItems(IBlockAccess world, int x, int y, int z, ForgeDirection dir, INodeBuffer buffer)
	{
		return this.getNode().transferItems(world, x, y, z, dir, buffer);
	}

	@Override
	public boolean canInput(IBlockAccess world, int x, int y, int z, ForgeDirection dir)
	{
		return !this.isBlocked(dir) && this.getNode().canInput(world, x, y, z, dir);
	}

	@Override
	public ArrayList<ForgeDirection> getOutputDirections(IBlockAccess world, int x, int y, int z, ForgeDirection dir, INodeBuffer buffer)
	{
		return this.getNode().getOutputDirections(world, x, y, z, dir, buffer);
	}

	@Override
	public boolean canOutput(IBlockAccess world, int x, int y, int z, ForgeDirection dir)
	{
		return !this.isBlocked(dir) && this.getNode().canOutput(world, x, y, z, dir);
	}

	@Override
	public int limitTransfer(TileEntity dest, ForgeDirection side, INodeBuffer buffer)
	{
		return this.getNode().limitTransfer(dest, side, buffer);
	}

	@Override
	public IInventory getFilterInventory(IBlockAccess world, int x, int y, int z)
	{
		return this.getNode().getFilterInventory(world, x, y, z);
	}

	@Override
	public boolean shouldConnectToTile(IBlockAccess world, int x, int y, int z, ForgeDirection dir)
	{
		return !this.isBlocked(dir) && this.getNode().shouldConnectToTile(world, x, y, z, dir);
	}

	public void reloadBlockMasks()
	{
		this.blockMasks = 0;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			dummyPipes[dir.ordinal()].h = 0.5F + this.baseSize();
			if (dir == this.getNodeDir())
				this.blockMasks |= this.flagmasks[dir.ordinal()];
			else if (!this.tile().canAddPart(dummyPipes[dir.ordinal()]))
				this.blockMasks |= this.flagmasks[dir.ordinal()];
		}

	}

	@Override
	public void onPartChanged(TMultiPart part)
	{
		this.reloadBlockMasks();
	}

	@Override
	public void onNeighborChanged()
	{
		this.node.updateRedstone();
		this.reloadBlockMasks();
	}

	@Override
	public boolean isPowered()
	{
		return this.node.isPowered();
	}

	@Override
	public boolean recalcRedstone()
	{
		if (this.tile() instanceof TRedstoneTile)
		{
			TRedstoneTile rsT = (TRedstoneTile) this.tile();

			for (int side = 0; side < 6; ++side)
			{
				if (rsT.weakPowerLevel(side) > 0)
					return true;
			}
		}

		for (int side = 0; side < 6; ++side)
		{
			if (RedstoneInteractions.getPowerTo(this.world(), this.x(), this.y(), this.z(), side, 31) > 0)
				return true;
		}

		return false;
	}

	public boolean isBlocked(ForgeDirection dir)
	{
		if (this.node.getWorldObj() == null)
			this.onWorldJoin();

		if (this.blockMasks < 0)
			this.reloadBlockMasks();

		return (this.blockMasks & this.flagmasks[dir.ordinal()]) == this.flagmasks[dir.ordinal()];
	}

	@Override
	public IIcon baseTexture()
	{
		return this.getNode().baseTexture();
	}

	@Override
	public IIcon pipeTexture(ForgeDirection dir, boolean blocked)
	{
		return this.getNode().pipeTexture(dir, blocked);
	}

	@Override
	public IIcon invPipeTexture(ForgeDirection dir)
	{
		return this.getNode().invPipeTexture(dir);
	}

	@Override
	public IIcon socketTexture(ForgeDirection dir)
	{
		return this.getNode().socketTexture(dir);
	}

	@Override
	public String getPipeType()
	{
		return this.getNode().getPipeType();
	}

	@Override
	public float baseSize()
	{
		return this.getNode().baseSize();
	}

	@Override
	public boolean occlusionTest(TMultiPart npart)
	{
		return npart instanceof DummyPipePart || super.occlusionTest(npart);
	}

	@Override
	public final Cuboid6 getBounds()
	{
		Box bounds = ((BlockTransferNode) this.getBlock()).getWorldModel(this.getWorld(), this.x(), this.y(), this.z()).boundingBox();
		return new Cuboid6((double) bounds.minX, (double) bounds.minY, (double) bounds.minZ, (double) bounds.maxX, (double) bounds.maxY, (double) bounds.maxZ);
	}

	@Override
	public final HashSet<IndexedCuboid6> getSubParts()
	{
		HashSet<IndexedCuboid6> boxes = new HashSet();

		for (Box bounds : ((BlockTransferNode) this.getBlock()).getWorldModel(this.getWorld(), this.x(), this.y(), this.z()))
		{
			boxes.add(new IndexedCuboid6(Integer.valueOf(0), new Cuboid6((double) bounds.minX, (double) bounds.minY, (double) bounds.minZ, (double) bounds.maxX, (double) bounds.maxY, (double) bounds.maxZ)));
		}

		return boxes;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean drawHighlight(MovingObjectPosition hit, EntityPlayer player, float frame)
	{
		GL11.glEnable(3042);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GL11.glDisable(3553);
		GL11.glDepthMask(false);
		float f1 = 0.002F;
		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) frame;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) frame;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) frame;
		RenderGlobal.drawOutlinedBoundingBox(this.getBounds().add(new Vector3((double) this.x(), (double) this.y(), (double) this.z())).toAABB().expand((double) f1, (double) f1, (double) f1).getOffsetBoundingBox(-d0, -d1, -d2), -1);
		GL11.glDepthMask(true);
		GL11.glEnable(3553);
		GL11.glDisable(3042);
		return true;
	}

	@Override
	public int getSlotMask()
	{
		return this.getNode().getNodeDir() == ForgeDirection.UNKNOWN ? 64 : 64 | 1 << this.getNode().getNodeDir().ordinal();
	}

	@Override
	public int redstoneConductionMap()
	{
		return 0;
	}

	@Override
	public boolean solid(int arg0)
	{
		return false;
	}

	static
	{
		for (int i = 0; i < 6; ++i)
		{
			dummyPipes[i] = new DummyPipePart(i, 0.625F);
		}

	}
}
