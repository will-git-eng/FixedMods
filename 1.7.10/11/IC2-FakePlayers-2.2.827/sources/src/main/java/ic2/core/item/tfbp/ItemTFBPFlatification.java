package ic2.core.item.tfbp;

import java.util.HashSet;
import java.util.Set;

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
	public static Set<Block> removable = new HashSet();

	public ItemTFBPFlatification(InternalName internalName)
	{
		super(internalName);
	}

	public static void init()
	{
		removable.add(Blocks.snow);
		removable.add(Blocks.ice);
		removable.add(Blocks.grass);
		removable.add(Blocks.stone);
		removable.add(Blocks.gravel);
		removable.add(Blocks.sand);
		removable.add(Blocks.dirt);
		removable.add(Blocks.leaves);
		removable.add(Blocks.log);
		removable.add(Blocks.tallgrass);
		removable.add(Blocks.red_flower);
		removable.add(Blocks.yellow_flower);
		removable.add(Blocks.sapling);
		removable.add(Blocks.wheat);
		removable.add(Blocks.red_mushroom_block);
		removable.add(Blocks.brown_mushroom);
		removable.add(Blocks.pumpkin);
		if (Ic2Items.rubberLeaves != null)
			removable.add(StackUtil.getBlock(Ic2Items.rubberLeaves));

		if (Ic2Items.rubberSapling != null)
			removable.add(StackUtil.getBlock(Ic2Items.rubberSapling));

		if (Ic2Items.rubberWood != null)
			removable.add(StackUtil.getBlock(Ic2Items.rubberWood));
	}

	@Override
	public int getConsume()
	{
		return 4000;
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
		int y = TileEntityTerra.getFirstBlockFrom(world, x, z, yCoord + 20);
		if (y == -1)
			return false;
		else
		{
			if (world.getBlock(x, y, z) == Blocks.snow_layer)
				--y;

			if (y == yCoord)
				return false;
			else if (y < yCoord)
    
				if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y + 1, z))
    

				world.setBlock(x, y + 1, z, Blocks.dirt, 0, 7);
				return true;
			}
			else if (this.canRemove(world.getBlock(x, y, z)))
    
				if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

				world.setBlockToAir(x, y, z);
				return true;
			}
			else
				return false;
		}
	}

	public boolean canRemove(Block block)
	{
		return removable.contains(block);
	}
}
