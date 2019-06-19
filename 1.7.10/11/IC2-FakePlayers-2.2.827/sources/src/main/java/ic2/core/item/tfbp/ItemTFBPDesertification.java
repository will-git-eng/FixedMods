package ic2.core.item.tfbp;

import ru.will.git.ic2.EventConfig;
import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.Ic2Items;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.init.InternalName;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

    
{
	public ItemTFBPDesertification(InternalName internalName)
	{
		super(internalName);
	}

	@Override
	public int getConsume()
	{
		return 2500;
	}

	@Override
	public int getRange()
	{
		return 40;
    
	@Override
	public boolean terraform(World world, int x, int z, int yCoord)
	{
		return this.terraform(world, x, z, yCoord, ModUtils.getModFake(world));
    

	@Override
    
	{
		int y = TileEntityTerra.getFirstBlockFrom(world, x, z, yCoord + 10);
		if (y == -1)
			return false;
    
    
			if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

			Block block = world.getBlock(x, y, z);
			if (block != Blocks.water && block != Blocks.flowing_water && block != Blocks.snow_layer && block != Blocks.leaves && !StackUtil.equals(block, Ic2Items.rubberLeaves) && !this.isPlant(block))
			{
				if (block != Blocks.ice && block != Blocks.snow)
				{
					if ((block == Blocks.planks || block == Blocks.log || StackUtil.equals(block, Ic2Items.rubberWood)) && world.rand.nextInt(15) == 0)
					{
						world.setBlock(x, y, z, Blocks.fire, 0, 7);
						return true;
					}
					else
						return false;
				}
				else
				{
					world.setBlock(x, y, z, Blocks.flowing_water, 0, 7);
					return true;
				}
			}
			else
			{
				world.setBlockToAir(x, y, z);
				return true;
			}
		}
		else
		{
    
			return true;
		}
	}

	public boolean isPlant(Block block)
	{
		return ItemTFBPCultivation.plants.contains(block);
	}
}
