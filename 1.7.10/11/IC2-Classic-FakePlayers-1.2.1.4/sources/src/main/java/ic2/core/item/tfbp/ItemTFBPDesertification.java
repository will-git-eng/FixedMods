package ic2.core.item.tfbp;

import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.Ic2Items;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
    
public class ItemTFBPDesertification extends ItemTFBP implements ITerraformingBPFakePlayer
{
	public ItemTFBPDesertification(int index)
	{
		super(index);
	}

	@Override
	public int getConsume(ItemStack item)
	{
		return 2500;
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
		int y = TileEntityTerra.getFirstBlockFrom(world, x, z, yCoord + 10);
		if (y == -1)
			return false;
		else if (!TileEntityTerra.switchGround(world, Blocks.dirt, Blocks.sand, x, y, z, false) && !TileEntityTerra.switchGround(world, Blocks.grass, Blocks.sand, x, y, z, false) && !TileEntityTerra.switchGround(world, Blocks.farmland, Blocks.sand, x, y, z, false))
		{
			Block id = world.getBlock(x, y, z);
			if (id != Blocks.water && id != Blocks.flowing_water && id != Blocks.snow && id != Blocks.leaves && id != Block.getBlockFromItem(Ic2Items.rubberLeaves.getItem()) && !this.isPlant(id))
			{
				if (id != Blocks.ice && id != Blocks.snow)
				{
					if ((id == Blocks.planks || id == Blocks.log || id == Blocks.log2 || id == Block.getBlockFromItem(Ic2Items.rubberWood.getItem())) && world.rand.nextInt(15) == 0)
					{
						ModUtils.setBlock(player, world, x, y, z, Blocks.fire);
						return true;
					}
					else
						return false;
				}
				else
				{
					ModUtils.setBlock(player, world, x, y, z, Blocks.water);
					return true;
				}
			}
			else
			{
				ModUtils.setBlock(player, world, x, y, z, Blocks.air);
				return true;
			}
		}
		else
		{
			TileEntityTerra.switchGround(world, Blocks.dirt, Blocks.sand, x, y, z, false);
			return true;
		}
	}

	public boolean isPlant(Block id)
	{
		for (int i = 0; i < ItemTFBPCultivation.plantIDs.size(); ++i)
			if (ItemTFBPCultivation.plantIDs.get(i) == id)
				return true;

		return false;
	}
}
