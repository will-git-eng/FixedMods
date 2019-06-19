package ic2.core.block;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockMultiID extends BlockContainerCommon
{
	public static int[][] sideAndFacingToSpriteOffset = new int[][] { { 3, 2, 0, 0, 0, 0 }, { 2, 3, 1, 1, 1, 1 }, { 1, 1, 3, 2, 5, 4 }, { 0, 0, 2, 3, 4, 5 }, { 4, 5, 4, 5, 3, 2 }, { 5, 4, 5, 4, 2, 3 } };
	public boolean[] canRender = new boolean[6];
	public int colorMulti;
	public boolean specialRender = false;

	protected BlockMultiID(Material mat)
	{
		super(mat);
		this.setCreativeTab(IC2.tabIC2);
	}

	protected abstract IIcon[] getIconSheet(int var1);

	protected abstract int getIconMeta(int var1);

	protected abstract int getMaxSheetSize(int var1);

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_)
	{
		return this.specialRender ? this.colorMulti : super.colorMultiplier(p_149720_1_, p_149720_2_, p_149720_3_, p_149720_4_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_)
	{
		return this.specialRender && !this.canRender[p_149646_5_] ? false : super.shouldSideBeRendered(p_149646_1_, p_149646_2_, p_149646_3_, p_149646_4_, p_149646_5_);
	}

	@Override
	public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int side)
	{
		TileEntity te = iblockaccess.getTileEntity(i, j, k);
		int meta = iblockaccess.getBlockMetadata(i, j, k);
		int extraMeta = this.getIconMeta(meta);
		int facing = 0;
		if (te instanceof TileEntityBlock)
		{
			TileEntityBlock block = (TileEntityBlock) te;
			facing = block.getFacing();
			if (block.hasTileMeta())
				extraMeta = block.getTileMeta();
		}

		return isActive(iblockaccess, i, j, k) ? this.getIconSheet(meta)[extraMeta + (sideAndFacingToSpriteOffset[side][facing] + 6) * this.getMaxSheetSize(meta)] : this.getIconSheet(meta)[extraMeta + sideAndFacingToSpriteOffset[side][facing] * this.getMaxSheetSize(meta)];
	}

	@Override
	public IIcon getIcon(int side, int meta)
	{
		return this.getIconSheet(meta)[this.getIconMeta(meta) + sideAndFacingToSpriteOffset[side][3] * this.getMaxSheetSize(meta)];
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float a, float b, float c)
	{
		if (entityPlayer.isSneaking())
			return false;
		else
		{
			TileEntity te = world.getTileEntity(x, y, z);
			return te instanceof IHasGui && (!IC2.platform.isSimulating() || IC2.platform.launchGui(entityPlayer, (IHasGui) te));
		}
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList ret = super.getDrops(world, x, y, z, metadata, fortune);
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof IInventory)
		{
			IInventory inv = (IInventory) te;

			for (int i = 0; i < inv.getSizeInventory(); ++i)
			{
				ItemStack itemStack = inv.getStackInSlot(i);
				if (itemStack != null)
				{
					ret.add(itemStack);
					inv.setInventorySlotContents(i, (ItemStack) null);
				}
			}
		}

		return ret;
	}

	@Override
	public abstract TileEntityBlock createNewTileEntity(World var1, int var2);

	@Override
	public void breakBlock(World world, int x, int y, int z, Block a, int b)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityBlock)
			((TileEntityBlock) te).onBlockBreak(a, b);

		boolean firstItem = true;

		for (ItemStack itemStack : this.getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0))
			if (firstItem)
				firstItem = false;
			else
				StackUtil.dropAsEntity(world, x, y, z, itemStack);

		super.breakBlock(world, x, y, z, a, b);
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack)
	{
		if (IC2.platform.isSimulating())
		{
			TileEntityBlock te = (TileEntityBlock) world.getTileEntity(i, j, k);
			if (entityliving == null)
				te.setFacing((short) 2);
			else
    
				if (entityliving instanceof EntityPlayer)
    

				int l = MathHelper.floor_double(entityliving.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
				switch (l)
				{
					case 0:
						te.setFacing((short) 2);
						break;
					case 1:
						te.setFacing((short) 5);
						break;
					case 2:
						te.setFacing((short) 3);
						break;
					case 3:
						te.setFacing((short) 4);
				}
			}
		}
	}

	public static boolean isActive(IBlockAccess iblockaccess, int i, int j, int k)
	{
		TileEntity te = iblockaccess.getTileEntity(i, j, k);
		return te instanceof TileEntityBlock && ((TileEntityBlock) te).getActive();
	}

	@Override
	public void getSubBlocks(Item j, CreativeTabs tabs, List itemList)
	{
		for (int i = 0; i < 16; ++i)
		{
			ItemStack is = new ItemStack(this, 1, i);
			if (Item.getItemFromBlock(this).getUnlocalizedName(is) != null)
				itemList.add(is);
		}

	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	}

	public void resetSpecialRender()
	{
		this.canRender = new boolean[6];
		this.specialRender = false;
		this.colorMulti = 0;
	}

	@SideOnly(Side.CLIENT)
	public void applySpecialRender(RenderBlockCable.RenderInfo par1)
	{
		this.canRender = par1.renderSides;
		this.specialRender = true;
		this.colorMulti = par1.color;
	}

	@Override
	public int getRenderType()
	{
		return IC2.platform.getRenderId("rotated");
	}
}
