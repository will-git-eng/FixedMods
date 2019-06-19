package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.BlockName;
import net.minecraft.block.*;
import net.minecraft.block.BlockDoublePlant.EnumBlockHalf;
import net.minecraft.block.BlockDoublePlant.EnumPlantType;
import net.minecraft.block.BlockTallGrass.EnumType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;


class Cultivation extends TerraformerBase
{
	static ArrayList<IBlockState> plants = new ArrayList<>();

	@Override
	void init()
	{
		plants.add(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, EnumType.GRASS));
		plants.add(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, EnumType.GRASS));
		plants.add(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, EnumType.FERN));
		plants.add(Blocks.RED_FLOWER.getDefaultState());
		plants.add(Blocks.YELLOW_FLOWER.getDefaultState());
		plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, EnumPlantType.GRASS));
		plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, EnumPlantType.ROSE));
		plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, EnumPlantType.SUNFLOWER));

		for (BlockPlanks.EnumType type : BlockSapling.TYPE.getAllowedValues())
		{
			plants.add(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, type));
		}

		plants.add(Blocks.WHEAT.getDefaultState());
		plants.add(Blocks.RED_MUSHROOM.getDefaultState());
		plants.add(Blocks.BROWN_MUSHROOM.getDefaultState());
		plants.add(Blocks.PUMPKIN.getDefaultState());
		plants.add(Blocks.MELON_BLOCK.getDefaultState());
		plants.add(BlockName.sapling.getInstance().getDefaultState());
	}

	@Override
	boolean terraform(World world, BlockPos pos)
	{
		pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 10);
		if (pos == null)
			return false;
		if (TileEntityTerra.switchGround(world, pos, Blocks.SAND, Blocks.DIRT.getDefaultState(), true))
			return true;
		if (TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.getDefaultState(), true))
		{
			int i = 4;

			while (true)
			{
				--i;
				if (i <= 0 || !TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.getDefaultState(), true))
					break;
			}
		}

		Block block = world.getBlockState(pos).getBlock();
		if (block == Blocks.DIRT)
		{
			setBlockState(world, pos, Blocks.GRASS.getDefaultState());
			return true;
		}
		return block == Blocks.GRASS && growPlantsOn(world, pos);
	}

	private static boolean growPlantsOn(World world, BlockPos pos)
	{
		BlockPos above = pos.up();
		IBlockState state = world.getBlockState(above);
		Block block = state.getBlock();
		if (block.isAir(state, world, above) || block == Blocks.TALLGRASS && world.rand.nextInt(4) == 0)
		{
			IBlockState plant = pickRandomPlant(world.rand);
			if (plant.getProperties().containsKey(BlockDirectional.FACING))
				plant = plant.withProperty(BlockDirectional.FACING, EnumFacing.HORIZONTALS[world.rand.nextInt(EnumFacing.HORIZONTALS.length)]);

			if (plant.getBlock() instanceof BlockCrops)
				setBlockState(world, pos, Blocks.FARMLAND.getDefaultState());
			else if (plant.getBlock() == Blocks.DOUBLE_PLANT)
			{
				plant = plant.withProperty(BlockDoublePlant.HALF, EnumBlockHalf.LOWER);
				setBlockState(world, above, plant.withProperty(BlockDoublePlant.HALF, EnumBlockHalf.LOWER));
				setBlockState(world, above.up(), plant.withProperty(BlockDoublePlant.HALF, EnumBlockHalf.UPPER));
				return true;
			}

			setBlockState(world, above, plant);
			return true;
		}
		return false;
	}

	private static IBlockState pickRandomPlant(Random random)
	{
		return plants.get(random.nextInt(plants.size()));
	}
}
