package thaumcraft.common.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileMirror;
import thaumcraft.common.tiles.TileMirrorEssentia;

import java.util.ArrayList;
import java.util.List;

public class BlockMirror extends BlockContainer
{
	public IIcon icon;
	public IIcon iconEss;

	public BlockMirror()
	{
		super(Material.glass);
		this.setHardness(1.0F);
		this.setResistance(10.0F);
		this.setStepSound(new CustomStepSound("jar", 0.5F, 2.0F));
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		this.icon = ir.registerIcon("thaumcraft:mirrorframe");
		this.iconEss = ir.registerIcon("thaumcraft:mirrorframe2");
	}

	@Override
	public IIcon getIcon(int i, int m)
	{
		return m < 6 ? this.icon : this.iconEss;
	}

	@Override
	public int damageDropped(int par1)
	{
		return par1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 6));
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		if (metadata <= 5)
			new TileMirror();

		return metadata > 5 && metadata <= 11 ? new TileMirrorEssentia() : super.createTileEntity(world, metadata);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int md)
	{
		return new TileMirror();
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public void onBlockHarvested(World par1World, int par2, int par3, int par4, int par5, EntityPlayer par6EntityPlayer)
	{
		this.dropBlockAsItem(par1World, par2, par3, par4, par5, 0);
		super.onBlockHarvested(par1World, par2, par3, par4, par5, par6EntityPlayer);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> drops = new ArrayList();
		int md = world.getBlockMetadata(x, y, z);
		if (md < 6)
		{
			ItemStack drop = new ItemStack(this, 1, 0);

    
			TileEntity tile = world.getTileEntity(x, y, z);
    
    
    

				if (tm.linked)
				{
					drop.setTagInfo("linkX", new NBTTagInt(tm.linkX));
					drop.setTagInfo("linkY", new NBTTagInt(tm.linkY));
					drop.setTagInfo("linkZ", new NBTTagInt(tm.linkZ));
					drop.setTagInfo("linkDim", new NBTTagInt(tm.linkDim));
					drop.setTagInfo("dimname", new NBTTagString(DimensionManager.getProvider(world.provider.dimensionId).getDimensionName()));
					drop.setItemDamage(1);
					tm.invalidateLink();
				}

				drops.add(drop);
			}

			return drops;
		}
		else
		{
			ItemStack drop = new ItemStack(this, 1, 6);

    
			TileEntity tile = world.getTileEntity(x, y, z);
    
    
    

				if (tm.linked)
				{
					drop.setTagInfo("linkX", new NBTTagInt(tm.linkX));
					drop.setTagInfo("linkY", new NBTTagInt(tm.linkY));
					drop.setTagInfo("linkZ", new NBTTagInt(tm.linkZ));
					drop.setTagInfo("linkDim", new NBTTagInt(tm.linkDim));
					drop.setTagInfo("dimname", new NBTTagString(DimensionManager.getProvider(world.provider.dimensionId).getDimensionName()));
					drop.setItemDamage(7);
					tm.invalidateLink();
				}

				drops.add(drop);
			}

			return drops;
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		int md = world.getBlockMetadata(x, y, z);
		if (md < 6 && !world.isRemote && entity instanceof EntityItem && !entity.isDead && ((EntityItem) entity).timeUntilPortal == 0)
		{
			TileMirror taf = (TileMirror) world.getTileEntity(x, y, z);
			if (taf != null)
				taf.transport((EntityItem) entity);
		}

	}

	@Override
	public int onBlockPlaced(World par1World, int par2, int par3, int par4, int par5, float par6, float par7, float par8, int par9)
	{
		if (par9 > 6)
			par9 = 6;
		else if (par9 > 0 && par9 < 6)
			par9 = 0;

		return par9 + par5;
	}

	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, Block l)
	{
		if (!world.isRemote)
		{
			int i1 = world.getBlockMetadata(i, j, k);
			boolean flag = false;
			if (!world.isSideSolid(i - 1, j, k, ForgeDirection.getOrientation(5)) && i1 % 6 == 5)
				flag = true;

			if (!world.isSideSolid(i + 1, j, k, ForgeDirection.getOrientation(4)) && i1 % 6 == 4)
				flag = true;

			if (!world.isSideSolid(i, j, k - 1, ForgeDirection.getOrientation(3)) && i1 % 6 == 3)
				flag = true;

			if (!world.isSideSolid(i, j, k + 1, ForgeDirection.getOrientation(2)) && i1 % 6 == 2)
				flag = true;

			if (!world.isSideSolid(i, j - 1, k, ForgeDirection.getOrientation(1)) && i1 % 6 == 1)
				flag = true;

			if (!world.isSideSolid(i, j + 1, k, ForgeDirection.getOrientation(0)) && i1 % 6 == 0)
				flag = true;

			if (flag)
			{
				this.dropBlockAsItem(world, i, j, k, i1, 0);
				world.setBlockToAir(i, j, k);
			}
		}

	}

	private boolean checkIfAttachedToBlock(World world, int i, int j, int k)
	{
		return this.canPlaceBlockAt(world, i, j, k);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int i, int j, int k, int l)
	{
		return l == 0 && world.isSideSolid(i, j + 1, k, ForgeDirection.getOrientation(0)) || l == 1 && world.isSideSolid(i, j - 1, k, ForgeDirection.getOrientation(1)) || l == 2 && world.isSideSolid(i, j, k + 1, ForgeDirection.getOrientation(2)) || l == 3 && world.isSideSolid(i, j, k - 1, ForgeDirection.getOrientation(3)) || l == 4 && world.isSideSolid(i + 1, j, k, ForgeDirection.getOrientation(4)) || l == 5 && world.isSideSolid(i - 1, j, k, ForgeDirection.getOrientation(5));
	}

	@Override
	public boolean canPlaceBlockAt(World world, int i, int j, int k)
	{
		return world.isSideSolid(i - 1, j, k, ForgeDirection.getOrientation(5)) || world.isSideSolid(i + 1, j, k, ForgeDirection.getOrientation(4)) || world.isSideSolid(i, j, k - 1, ForgeDirection.getOrientation(3)) || world.isSideSolid(i, j, k + 1, ForgeDirection.getOrientation(2)) || world.isSideSolid(i, j - 1, k, ForgeDirection.getOrientation(1)) || world.isSideSolid(i, j + 1, k, ForgeDirection.getOrientation(0));
	}

	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
	{
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return null;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		this.setBlockBoundsBasedOnState(par1World, par2, par3, par4);
		return super.getSelectedBoundingBoxFromPool(par1World, par2, par3, par4);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
	{
		this.setBlockBoundsForBlockRender(par1IBlockAccess.getBlockMetadata(par2, par3, par4));
	}

	@Override
	public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity)
	{
	}

	public void setBlockBoundsForBlockRender(int par1)
	{
		float w = 0.0625F;
		switch (par1 % 6)
		{
			case 0:
				this.setBlockBounds(0.0F, 1.0F - w, 0.0F, 1.0F, 1.0F, 1.0F);
				break;
			case 1:
				this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, w, 1.0F);
				break;
			case 2:
				this.setBlockBounds(0.0F, 0.0F, 1.0F - w, 1.0F, 1.0F, 1.0F);
				break;
			case 3:
				this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, w);
				break;
			case 4:
				this.setBlockBounds(1.0F - w, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				break;
			case 5:
				this.setBlockBounds(0.0F, 0.0F, 0.0F, w, 1.0F, 1.0F);
		}

	}
}
