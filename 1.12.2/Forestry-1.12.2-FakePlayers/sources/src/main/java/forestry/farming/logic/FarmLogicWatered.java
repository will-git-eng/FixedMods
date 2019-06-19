/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.farming.logic;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import ru.will.git.forestry.EventConfig;
import forestry.api.farming.FarmDirection;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmProperties;
import forestry.api.farming.ISoil;
import forestry.core.utils.BlockUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public abstract class FarmLogicWatered extends FarmLogicSoil
{
	private static final FluidStack STACK_WATER = new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);

	protected NonNullList<ItemStack> produce = NonNullList.create();

	public FarmLogicWatered(IFarmProperties properties, boolean isManual)
	{
		super(properties, isManual);
	}

	@Override
	public int getFertilizerConsumption()
	{
		return 5;
	}

	@Override
	public int getWaterConsumption(float hydrationModifier)
	{
		return (int) (20 * hydrationModifier);
	}

	@Override
	public NonNullList<ItemStack> collect(World world, IFarmHousing farmHousing)
	{
		NonNullList<ItemStack> products = this.produce;
		this.produce = NonNullList.create();
		return products;
	}

	@Override
	public boolean cultivate(World world, IFarmHousing farmHousing, BlockPos pos, FarmDirection direction, int extent)
	{
		if (this.maintainSoil(world, farmHousing, pos, direction, extent))
			return true;

		if (!this.isManual && this.maintainWater(world, farmHousing, pos, direction, extent))
			return true;

		return this.maintainCrops(world, farmHousing, pos.up(), direction, extent);

	}

	private boolean maintainSoil(World world, IFarmHousing farmHousing, BlockPos pos, FarmDirection direction, int extent)
	{
		if (!farmHousing.canPlantSoil(this.isManual))
			return false;

		for (ISoil soil : this.getSoils())
		{
			NonNullList<ItemStack> resources = NonNullList.create();
			resources.add(soil.getResource());

			for (int i = 0; i < extent; i++)
			{
				BlockPos position = this.translateWithOffset(pos, direction, i);
				if (!world.isBlockLoaded(position))
					break;

				IBlockState state = world.getBlockState(position);
				if (!BlockUtil.isBreakableBlock(state, world, pos) || this.isAcceptedSoil(state) || this.isWaterSourceBlock(world, position) || !farmHousing.getFarmInventory().hasResources(resources))
					continue;

				BlockPos platformPosition = position.down();
				if (!farmHousing.isValidPlatform(world, platformPosition))
					break;

				if (!BlockUtil.isReplaceableBlock(state, world, position))
				{
					
					if (EventConfig.eventFarm)
					{
						FakePlayerContainer fake = farmHousing.getFakePlayerContainer(world);
						if (fake.cantBreak(position))
							break;
					}
					

					this.produce.addAll(BlockUtil.getBlockDrops(world, position));
					world.setBlockToAir(position);
					return this.trySetSoil(world, farmHousing, position, soil.getResource(), soil.getSoilState());
				}

				if (!this.isManual)
				{
					if (this.trySetWater(world, farmHousing, position))
						return true;

					return this.trySetSoil(world, farmHousing, position, soil.getResource(), soil.getSoilState());
				}
			}
		}

		return false;
	}

	private boolean maintainWater(World world, IFarmHousing farmHousing, BlockPos pos, FarmDirection direction, int extent)
	{
		// Still not done, check water then
		for (int i = 0; i < extent; i++)
		{
			BlockPos position = this.translateWithOffset(pos, direction, i);

			if (!world.isBlockLoaded(position))
				break;

			BlockPos platformPosition = position.down();
			if (!farmHousing.isValidPlatform(world, platformPosition))
				break;

			if (BlockUtil.isBreakableBlock(world, pos) && this.trySetWater(world, farmHousing, position))
				return true;
		}

		return false;
	}

	protected boolean maintainCrops(World world, IFarmHousing farmHousing, BlockPos pos, FarmDirection direction, int extent)
	{
		return false;
	}

	private boolean trySetSoil(World world, IFarmHousing farmHousing, BlockPos position, ItemStack resource, IBlockState ground)
	{
		NonNullList<ItemStack> resources = NonNullList.create();
		resources.add(resource);
		if (!farmHousing.getFarmInventory().hasResources(resources))
			return false;

		
		if (EventConfig.eventFarm)
		{
			FakePlayerContainer fake = farmHousing.getFakePlayerContainer(world);
			if (fake.cantPlace(position, ground))
				return false;
		}
		

		if (!BlockUtil.setBlockWithPlaceSound(world, position, ground))
			return false;
		farmHousing.getFarmInventory().removeResources(resources);
		return true;
	}

	private boolean trySetWater(World world, IFarmHousing farmHousing, BlockPos position)
	{
		if (this.isWaterSourceBlock(world, position) || !this.canPlaceWater(world, position))
			return false;

		if (!farmHousing.hasLiquid(STACK_WATER))
			return false;

		IBlockState newState = Blocks.WATER.getDefaultState();

		
		if (EventConfig.eventFarm)
		{
			FakePlayerContainer fake = farmHousing.getFakePlayerContainer(world);
			if (fake.cantPlace(position, newState))
				return false;
		}
		

		this.produce.addAll(BlockUtil.getBlockDrops(world, position));
		BlockUtil.setBlockWithPlaceSound(world, position, newState);
		farmHousing.removeLiquid(STACK_WATER);
		return true;
	}

	private boolean canPlaceWater(World world, BlockPos position)
	{
		// don't place water close to other water
		for (int x = -2; x <= 2; x++)
		{
			for (int z = -2; z <= 2; z++)
			{
				BlockPos offsetPosition = position.add(x, 0, z);
				if (this.isWaterSourceBlock(world, offsetPosition))
					return false;
			}
		}

		// don't place water if it can flow into blocks next to it
		for (int x = -1; x <= 1; x++)
		{
			BlockPos offsetPosition = position.add(x, 0, 0);
			if (world.isAirBlock(offsetPosition))
				return false;
		}
		for (int z = -1; z <= 1; z++)
		{
			BlockPos offsetPosition = position.add(0, 0, z);
			if (world.isAirBlock(offsetPosition))
				return false;
		}

		return true;
	}

}
