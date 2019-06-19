package ic2.core.item.tfbp;

import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
    
public class ItemTFBPMushroom extends ItemTFBP implements ITerraformingBPFakePlayer
{
	public ItemTFBPMushroom(int index)
	{
		super(index);
	}

	@Override
	public int getConsume(ItemStack item)
	{
		return 8000;
	}

	@Override
	public int getRange(ItemStack item)
	{
		return 25;
	}

	@Override
	public boolean terraform(ItemStack item, World world, int x, int z, int yCoord)
	{
		return this.terraform(ModUtils.getModFake(world), item, world, x, z, yCoord);
	}

	@Override
	public boolean terraform(EntityPlayer player, ItemStack item, World world, int x, int z, int yCoord)
	{
		int y = TileEntityTerra.getFirstSolidBlockFrom(world, x, z, yCoord + 20);
		return y != -1 && this.growBlockWithDependancy(player, world, x, y, z, Blocks.brown_mushroom_block, Blocks.brown_mushroom);
	}

	public boolean growBlockWithDependancy(World world, int x, int y, int z, Block id, Block dependancy)
	{
		return this.growBlockWithDependancy(ModUtils.getModFake(world), world, x, y, z, id, dependancy);
	}

	public boolean growBlockWithDependancy(EntityPlayer player, World world, int x, int y, int z, Block id, Block dependancy)
	{
		for (int xm = x - 1; dependancy != Blocks.air && xm < x + 1; ++xm)
			for (int zm = z - 1; zm < z + 1; ++zm)
				for (int ym = y + 5; ym > y - 2; --ym)
				{
					Block block = world.getBlock(xm, ym, zm);
					if (dependancy == Blocks.mycelium)
					{
						if (block == dependancy || block == Blocks.brown_mushroom_block || block == Blocks.red_mushroom_block)
							break;

						if (world.isAirBlock(xm, ym, zm))
							continue;

						if (block == Blocks.dirt || block == Blocks.grass)
						{
							ModUtils.setBlock(player, world, xm, ym, zm, dependancy);
							TileEntityTerra.setBiomeAt(world, x, z, BiomeGenBase.mushroomIsland);
							return true;
						}
					}

					if (dependancy == Blocks.brown_mushroom)
					{
						if (block == Blocks.brown_mushroom || block == Blocks.red_mushroom)
							break;

						if (!world.isAirBlock(xm, ym, zm) && this.growBlockWithDependancy(player, world, xm, ym, zm, Blocks.brown_mushroom, Blocks.mycelium))
							return true;
					}
				}

		if (id == Blocks.brown_mushroom)
		{
			Block base = world.getBlock(x, y, z);
			if (base != Blocks.mycelium)
			{
				if (base != Blocks.brown_mushroom_block && base != Blocks.red_mushroom_block)
					return false;

				ModUtils.setBlock(player, world, x, y, z, Blocks.mycelium);
			}

			Block above = world.getBlock(x, y + 1, z);
			if (above != null && above != Blocks.tallgrass)
				return false;
			else
			{
				Block shroom = Blocks.brown_mushroom;
				if (world.rand.nextBoolean())
					shroom = Blocks.red_mushroom;

				ModUtils.setBlock(player, world, x, y + 1, z, shroom);
				return true;
			}
		}
		else
		{
			if (id == Blocks.brown_mushroom_block)
			{
				Block base = world.getBlock(x, y + 1, z);
				if (base != Blocks.brown_mushroom && base != Blocks.red_mushroom)
					return false;

				if (((BlockMushroom) base).func_149884_c(world, x, y + 1, z, world.rand))
				{
					for (int xm2 = x - 1; xm2 < x + 1; ++xm2)
						for (int zm2 = z - 1; zm2 < z + 1; ++zm2)
						{
							Block block = world.getBlock(xm2, y + 1, zm2);
							if (block == Blocks.brown_mushroom || block == Blocks.red_mushroom)
								ModUtils.setBlock(player, world, xm2, y + 1, zm2, Blocks.air);
						}

					return true;
				}
			}

			return false;
		}
	}
}
