package ic2.core.item.tfbp;

import java.util.ArrayList;

import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.Ic2Items;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
    
public class ItemTFBPFlatification extends ItemTFBP implements ITerraformingBPFakePlayer
{
	public static ArrayList<Block> removeIDs = new ArrayList();

	public ItemTFBPFlatification(int index)
	{
		super(index);
	}

	public static void init()
	{
		removeIDs.add(Blocks.snow);
		removeIDs.add(Blocks.ice);
		removeIDs.add(Blocks.grass);
		removeIDs.add(Blocks.stone);
		removeIDs.add(Blocks.gravel);
		removeIDs.add(Blocks.sand);
		removeIDs.add(Blocks.dirt);
		removeIDs.add(Blocks.leaves);
		removeIDs.add(Blocks.log);
		removeIDs.add(Blocks.log2);
		removeIDs.add(Blocks.tallgrass);
		removeIDs.add(Blocks.red_flower);
		removeIDs.add(Blocks.yellow_flower);
		removeIDs.add(Blocks.sapling);
		removeIDs.add(Blocks.wheat);
		removeIDs.add(Blocks.red_mushroom);
		removeIDs.add(Blocks.brown_mushroom);
		removeIDs.add(Blocks.pumpkin);
		if (Ic2Items.rubberLeaves != null)
			removeIDs.add(Block.getBlockFromItem(Ic2Items.rubberLeaves.getItem()));

		if (Ic2Items.rubberSapling != null)
			removeIDs.add(Block.getBlockFromItem(Ic2Items.rubberSapling.getItem()));

		if (Ic2Items.rubberWood != null)
			removeIDs.add(Block.getBlockFromItem(Ic2Items.rubberWood.getItem()));

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
		int y = TileEntityTerra.getFirstBlockFrom(world, x, z, yCoord + 20);
		if (y == -1)
			return false;
		else
		{
			if (world.getBlock(x, y, z) == Blocks.snow)
				--y;

			if (y == yCoord)
				return false;
			else if (y < yCoord)
			{
				ModUtils.setBlock(player, world, x, y + 1, z, Blocks.dirt);
				return true;
			}
			else if (this.canRemove(world.getBlock(x, y, z)))
			{
				ModUtils.setBlock(player, world, x, y, z, Blocks.air);
				return true;
			}
			else
				return false;
		}
	}

	public boolean canRemove(Block block)
	{
		return removeIDs.contains(block);
	}
}
