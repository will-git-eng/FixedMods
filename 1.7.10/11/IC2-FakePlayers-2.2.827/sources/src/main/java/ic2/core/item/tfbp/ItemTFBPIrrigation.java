package ic2.core.item.tfbp;

import ru.will.git.ic2.EventConfig;
import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.Ic2Items;
import ic2.core.block.BlockRubSapling;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.init.InternalName;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

    
{
	public ItemTFBPIrrigation(InternalName internalName)
	{
		super(internalName);
	}

	@Override
	public int getConsume()
	{
		return 3000;
	}

	@Override
	public int getRange()
	{
		return 60;
    
	@Override
	public boolean terraform(World world, int x, int z, int yCoord)
	{
		return this.terraform(world, x, z, yCoord, ModUtils.getModFake(world));
	}

	public void createLeaves(World world, int x, int y, int z, Block oldBlock, int meta)
	{
		this.createLeaves(world, x, y, z, oldBlock, meta, ModUtils.getModFake(world));
	}

	public boolean spreadGrass(World world, int x, int y, int z)
	{
		return this.spreadGrass(world, x, y, z, ModUtils.getModFake(world));
    

	@Override
    
	{
		if (world.rand.nextInt('뮀') == 0)
		{
			world.getWorldInfo().setRaining(true);
			return true;
		}
		else
		{
			int y = TileEntityTerra.getFirstBlockFrom(world, x, z, yCoord + 10);
			if (y == -1)
				return false;
    
			{
    
				return true;
			}
			else
			{
				Block block = world.getBlock(x, y, z);
				if (block != Blocks.tallgrass)
				{
					if (block == Blocks.sapling)
					{
						((BlockSapling) Blocks.sapling).func_149878_d(world, x, y, z, world.rand);
						return true;
					}
					else if (StackUtil.equals(block, Ic2Items.rubberSapling))
					{
						((BlockRubSapling) StackUtil.getBlock(Ic2Items.rubberSapling)).func_149878_d(world, x, y, z, world.rand);
						return true;
					}
					else if (block == Blocks.log)
    
						if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y + 1, z))
    

						int meta = world.getBlockMetadata(x, y, z);
						world.setBlock(x, y + 1, z, Blocks.log, meta, 7);
    
    
    
    
    
						return true;
					}
					else if (block == Blocks.wheat)
    
						if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

						world.setBlockMetadataWithNotify(x, y, z, 7, 7);
						return true;
					}
					else if (block == Blocks.fire)
    
						if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

						world.setBlockToAir(x, y, z);
						return true;
					}
					else
						return false;
				}
				else
    
			}
		}
	}

    
	{
		if (oldBlock.isAir(world, x, y, z))
    
			if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

			world.setBlock(x, y, z, Blocks.leaves, meta, 7);
		}
	}

    
	{
		if (world.rand.nextBoolean())
			return false;
		else
		{
			y = TileEntityTerra.getFirstBlockFrom(world, x, z, y);
			Block block = world.getBlock(x, y, z);
			if (block == Blocks.dirt)
    
				if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

				world.setBlock(x, y, z, Blocks.grass, 0, 7);
				return true;
			}
			else if (block == Blocks.grass)
    
				if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y + 1, z))
    

				world.setBlock(x, y + 1, z, Blocks.tallgrass, 1, 7);
				return true;
			}
			else
				return false;
		}
	}
}
