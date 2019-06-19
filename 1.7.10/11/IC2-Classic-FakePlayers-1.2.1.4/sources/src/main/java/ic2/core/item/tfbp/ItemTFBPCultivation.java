package ic2.core.item.tfbp;

import java.util.ArrayList;
import java.util.Random;

import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.IC2;
import ic2.core.Ic2Items;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
    
public class ItemTFBPCultivation extends ItemTFBP implements ITerraformingBPFakePlayer
{
	public static ArrayList<Block> plantIDs = new ArrayList();

	public ItemTFBPCultivation(int index)
	{
		super(index);
	}

	public static void init()
	{
		plantIDs.add(Blocks.tallgrass);
		plantIDs.add(Blocks.red_flower);
		plantIDs.add(Blocks.yellow_flower);
		plantIDs.add(Blocks.sapling);
		plantIDs.add(Blocks.wheat);
		plantIDs.add(Blocks.red_mushroom);
		plantIDs.add(Blocks.brown_mushroom);
		plantIDs.add(Blocks.pumpkin);
		plantIDs.add(Blocks.melon_block);
		if (Ic2Items.rubberSapling != null)
			plantIDs.add(Block.getBlockFromItem(Ic2Items.rubberSapling.getItem()));

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
	public int getConsume(ItemStack item)
	{
		return 4000;
	}

	@Override
	public int getRange(ItemStack item)
	{
		return 40;
	}

	@Override
	public boolean terraform(ItemStack item, World world, int x, int z, int yCoord)
	{
		return this.terraform(ModUtils.getModFake(world), item, world, x, z, yCoord);
	}

	@Override
	public boolean terraform(EntityPlayer player, ItemStack item, World world, int x, int z, int yCoord)
	{
		int y = TileEntityTerra.getFirstSolidBlockFrom(world, x, z, yCoord + 10);
		if (y == -1)
			return false;
		else if (TileEntityTerra.switchGround(world, Blocks.sand, Blocks.dirt, x, y, z, true))
			return true;
		else
		{
			Block id = world.getBlock(x, y, z);
			if (id == Blocks.dirt)
			{
				ModUtils.setBlock(player, world, x, y, z, Blocks.grass);
				return true;
			}
			else
				return id == Blocks.grass && this.growPlantsOn(player, world, x, y + 1, z);
		}
	}

	public boolean growPlantsOn(World world, int x, int y, int z)
	{
		return this.growPlantsOn(ModUtils.getModFake(world), world, x, y, z);
	}

	public boolean growPlantsOn(EntityPlayer player, World world, int x, int y, int z)
	{
		Block id = world.getBlock(x, y, z);
		if (id == null || id == Blocks.tallgrass && world.rand.nextInt(4) == 0)
		{
			Block plant = this.pickRandomPlantId(world.rand);
			if (plant == Blocks.wheat)
				ModUtils.setBlock(player, world, x, y - 1, z, Blocks.farmland);

			if (plant == Blocks.tallgrass)
			{
				ModUtils.setBlock(player, world, x, y, z, plant, 1, 3);
				return true;
			}
			else
			{
				ModUtils.setBlock(player, world, x, y, z, plant);
				return true;
			}
		}
		else
			return false;
	}

	public Block pickRandomPlantId(Random random)
	{
		for (int i = 0; i < plantIDs.size(); ++i)
			if (random.nextInt(5) <= 1)
				return plantIDs.get(i);

		return Blocks.tallgrass;
	}
}
