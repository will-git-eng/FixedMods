package crazypants.enderio.machine.light;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.BlockEio;
import crazypants.enderio.ModObject;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockLightNode extends BlockEio
{

	public static BlockLightNode create()
	{
		BlockLightNode result = new BlockLightNode();
		result.init();
		return result;
	}

	public BlockLightNode()
	{
		super(ModObject.blockLightNode.unlocalisedName, TileLightNode.class, Material.air);
		this.setCreativeTab(null);
		this.setBlockBounds(0, 0, 0, 0, 0, 0);
		this.setTickRandomly(true);
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean isBlockSolid(IBlockAccess iblockaccess, int x, int y, int z, int l)
	{
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, int x, int y, int z)
	{
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	{
    
		if (!(tile instanceof TileLightNode))
    

		TileLightNode te = (TileLightNode) tile;
		if (te != null)
			te.onBlockRemoved();
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		return world.getBlockMetadata(x, y, z) > 0 ? 15 : 0;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block par5)
	{
    
		if (!(tile instanceof TileLightNode))
    

		TileLightNode te = (TileLightNode) tile;
		if (te != null)
			te.onNeighbourChanged();
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random r)
	{
    
		if (!(tile instanceof TileLightNode))
    

		TileLightNode te = (TileLightNode) tile;
		if (te != null)
			te.checkParent();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iIconRegister)
	{
		this.blockIcon = iIconRegister.registerIcon("enderio:blockElectricLightFace");
	}

	@Override
	public int quantityDropped(Random p_149745_1_)
	{
		return 0;
	}

}
