package ic2.core.item.tfbp;

import ru.will.git.ic2.EventConfig;
import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.init.InternalName;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

    
{
	public ItemTFBPChilling(InternalName internalName)
	{
		super(internalName);
	}

	@Override
	public int getConsume()
	{
		return 2000;
	}

	@Override
	public int getRange()
	{
		return 50;
    
	@Override
	public boolean terraform(World world, int x, int z, int yCoord)
	{
		return this.terraform(world, x, z, yCoord, ModUtils.getModFake(world));
	}

	public boolean isSurroundedBySnow(World world, int x, int y, int z)
	{
		return this.isSurroundedBySnow(world, x, y, z, ModUtils.getModFake(world));
	}

	public boolean isSnowHere(World world, int x, int y, int z)
	{
		return this.isSnowHere(world, x, y, z, ModUtils.getModFake(world));
    

	@Override
    
	{
		int y = TileEntityTerra.getFirstBlockFrom(world, x, z, yCoord + 10);
		if (y == -1)
			return false;
		else
		{
			Block block = world.getBlock(x, y, z);
			if (block != Blocks.water && block != Blocks.flowing_water)
			{
				if (block == Blocks.ice)
				{
					Block blockBelow = world.getBlock(x, y - 1, z);
					if (blockBelow == Blocks.water || blockBelow == Blocks.flowing_water)
    
						if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y - 1, z))
    

						world.setBlock(x, y - 1, z, Blocks.ice, 0, 7);
						return true;
					}
				}
    
    
					if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

					world.setBlock(x, y, z, Blocks.snow, 0, 7);
					return true;
				}

				if (!Blocks.snow_layer.canPlaceBlockAt(world, x, y + 1, z) && block != Blocks.ice)
					return false;
				else
    
					if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y + 1, z))
    

					world.setBlock(x, y + 1, z, Blocks.snow_layer, 0, 7);
					return true;
				}
			}
			else
    
				if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

				world.setBlock(x, y, z, Blocks.ice, 0, 7);
				return true;
			}
		}
	}

    
	{
    
	}

    
	{
		int saveY = y;
		y = TileEntityTerra.getFirstBlockFrom(world, x, z, y + 16);
		if (saveY > y)
			return false;
		else
		{
			Block block = world.getBlock(x, y, z);
			if (block != Blocks.snow && block != Blocks.snow_layer)
			{
				if (Blocks.snow_layer.canPlaceBlockAt(world, x, y + 1, z) || block == Blocks.ice)
    
					if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y + 1, z))
    

					world.setBlock(x, y + 1, z, Blocks.snow_layer, 0, 7);
				}

				return false;
			}
			else
				return true;
		}
	}
}
