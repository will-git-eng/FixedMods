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
import forestry.api.farming.*;
import forestry.core.utils.BlockUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class FarmLogicHomogeneous extends FarmLogicSoil
{
	protected NonNullList<ItemStack> produce = NonNullList.create();

	public FarmLogicHomogeneous(IFarmProperties properties, boolean isManual)
	{
		super(properties, isManual);
	}

	@Override
	public boolean isAcceptedGermling(ItemStack itemstack)
	{
		for (IFarmable germling : this.getFarmables())
		{
			if (germling.isGermling(itemstack))
				return true;
		}
		return false;
	}

	@Override
	public boolean isAcceptedWindfall(ItemStack itemstack)
	{
		for (IFarmable germling : this.getFarmables())
		{
			if (germling.isWindfall(itemstack))
				return true;
		}
		return false;
	}

	protected boolean trySetCrop(World world, IFarmHousing farmHousing, BlockPos position, FarmDirection direction)
	{
		for (IFarmable candidate : this.getFarmables())
		{
			if (farmHousing.plantGermling(candidate, world, position, direction))
				return true;
		}

		return false;
	}

	@Override
	public boolean cultivate(World world, IFarmHousing farmHousing, BlockPos pos, FarmDirection direction, int extent)
	{
		return this.maintainSoil(world, farmHousing, pos, direction, extent) || this.maintainGermlings(world, farmHousing, pos.up(), direction, extent);
	}

	private boolean maintainSoil(World world, IFarmHousing farmHousing, BlockPos pos, FarmDirection direction, int extent)
	{
		if (!farmHousing.canPlantSoil(this.isManual))
			return false;

		for (ISoil soil : this.getSoils())
		{
			NonNullList<ItemStack> resources = NonNullList.create();
			resources.add(soil.getResource());
			if (!farmHousing.getFarmInventory().hasResources(resources))
				continue;

			for (int i = 0; i < extent; i++)
			{
				BlockPos position = this.translateWithOffset(pos, direction, i);
				IBlockState soilState = world.getBlockState(position);

				if (!world.isBlockLoaded(position) || farmHousing.isValidPlatform(world, pos))
					break;

				if (!BlockUtil.isBreakableBlock(soilState, world, pos) || this.isAcceptedSoil(soilState))
					continue;

				BlockPos platformPosition = position.down();
				if (!farmHousing.isValidPlatform(world, platformPosition))
					break;

				
				if (EventConfig.eventFarm)
				{
					FakePlayerContainer fake = farmHousing.getFakePlayerContainer(world);
					if (fake.cantBreak(position))
						break;
				}
				

				this.produce.addAll(BlockUtil.getBlockDrops(world, position));

				BlockUtil.setBlockWithPlaceSound(world, position, soil.getSoilState());
				farmHousing.getFarmInventory().removeResources(resources);
				return true;
			}
		}

		return false;
	}

	protected abstract boolean maintainGermlings(World world, IFarmHousing farmHousing, BlockPos pos, FarmDirection direction, int extent);
}