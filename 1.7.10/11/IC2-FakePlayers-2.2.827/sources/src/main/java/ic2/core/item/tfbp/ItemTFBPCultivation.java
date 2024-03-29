package ic2.core.item.tfbp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import ru.will.git.ic2.EventConfig;
import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.IC2;
import ic2.core.Ic2Items;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.init.InternalName;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

    
{
	public static ArrayList<Block> plants = new ArrayList();

	public ItemTFBPCultivation(InternalName internalName)
	{
		super(internalName);
	}

	public static void init()
	{
		plants.add(Blocks.tallgrass);
		plants.add(Blocks.red_flower);
		plants.add(Blocks.yellow_flower);
		plants.add(Blocks.sapling);
		plants.add(Blocks.wheat);
		plants.add(Blocks.red_mushroom_block);
		plants.add(Blocks.brown_mushroom);
		plants.add(Blocks.pumpkin);
		if (Ic2Items.rubberSapling != null)
			plants.add(StackUtil.getBlock(Ic2Items.rubberSapling));
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int l, float a, float b, float c)
	{
		if (super.onItemUse(itemstack, entityplayer, world, i, j, k, l, a, b, c))
		{
			if (entityplayer.dimension == 1)
				IC2.achievements.issueAchievement(entityplayer, "terraformEndCultivation");

			return true;
		}
		else
			return false;
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
	}

	public boolean growPlantsOn(World world, int x, int y, int z, Block block)
	{
		return this.growPlantsOn(world, x, y, z, block, ModUtils.getModFake(world));
    

	@Override
    
	{
		int y = TileEntityTerra.getFirstSolidBlockFrom(world, x, z, yCoord + 10);
		if (y == -1)
			return false;
    
			return true;
		else
		{
			Block block = world.getBlock(x, y, z);
			if (block == Blocks.dirt)
    
				if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

				world.setBlock(x, y, z, Blocks.grass, 0, 7);
				return true;
			}
			else
    
		}
	}

    
	{
		if (!block.isAir(world, x, y, z) && (block != Blocks.tallgrass || world.rand.nextInt(4) != 0))
			return false;
		else
		{
			Block plant = this.pickRandomPlant(world.rand);
			if (plant == Blocks.wheat)
    
				if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y - 1, z))
    

				world.setBlock(x, y - 1, z, Blocks.farmland, 0, 7);
    
			if (EventConfig.terraEvent && ModUtils.cantBreakOrNotInPrivate(player, x, y, z))
    

			if (plant == Blocks.tallgrass)
				world.setBlock(x, y, z, plant, 1, 7);
			else
				world.setBlock(x, y, z, plant, 0, 7);

			return true;
		}
	}

	public Block pickRandomPlant(Random random)
	{
		Iterator<Block> iter = plants.iterator();

		Block block;
		do
		{
			if (!iter.hasNext())
				return Blocks.tallgrass;

			block = iter.next();
		}
		while (random.nextInt(5) > 1);

		return block;
	}
}
