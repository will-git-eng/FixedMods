package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

abstract class TerraformerBase
{
	abstract boolean terraform(World var1, BlockPos var2);

	void init()
	{
	}

	
	public static boolean setBlockState(World world, BlockPos pos, IBlockState state)
	{
		return !TileEntityTerra.cantPlace(world, pos, state) && world.setBlockState(pos, state);
	}

	public static boolean setBlockToAir(World world, BlockPos pos)
	{
		return !TileEntityTerra.cantBreak(world, pos) && world.setBlockToAir(pos);
	}
	
}
