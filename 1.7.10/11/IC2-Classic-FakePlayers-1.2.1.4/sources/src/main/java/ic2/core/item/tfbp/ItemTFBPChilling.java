package ic2.core.item.tfbp;

import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
    
public class ItemTFBPChilling extends ItemTFBP implements ITerraformingBPFakePlayer
{
	public ItemTFBPChilling(int index)
	{
		super(index);
	}

	@Override
	public int getConsume(ItemStack item)
	{
		return 2000;
	}

	@Override
	public int getRange(ItemStack item)
	{
		return 50;
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
		else
		{
			Block id = world.getBlock(x, y, z);
			if (id != Blocks.water && id != Blocks.flowing_water)
			{
				if (id == Blocks.ice)
				{
					Block id2 = world.getBlock(x, y - 1, z);
					if (id2 == Blocks.flowing_water || id2 == Blocks.water)
					{
						ModUtils.setBlock(player, world, x, y - 1, z, Blocks.ice);
						return true;
					}
				}

				if (id == Blocks.snow_layer && this.isSurroundedBySnow(player, world, x, y, z))
				{
					ModUtils.setBlock(player, world, x, y, z, Blocks.snow);
					return true;
				}
				else
				{
					if (Blocks.snow_layer.canPlaceBlockAt(world, x, y + 1, z) || id == Blocks.ice)
						ModUtils.setBlock(player, world, x, y + 1, z, Blocks.snow_layer);

					return false;
				}
			}
			else
			{
				ModUtils.setBlock(player, world, x, y, z, Blocks.ice);
				return true;
			}
		}
	}

	public boolean isSurroundedBySnow(World world, int x, int y, int z)
	{
		return this.isSurroundedBySnow(ModUtils.getModFake(world), world, x, y, z);
	}

	public boolean isSurroundedBySnow(EntityPlayer player, World world, int x, int y, int z)
	{
		return this.isSnowHere(player, world, x + 1, y, z) && this.isSnowHere(player, world, x - 1, y, z) && this.isSnowHere(player, world, x, y, z + 1) && this.isSnowHere(player, world, x, y, z - 1);
	}

	public boolean isSnowHere(World world, int x, int y, int z)
	{
		return this.isSnowHere(ModUtils.getModFake(world), world, x, y, z);
	}

	public boolean isSnowHere(EntityPlayer player, World world, int x, int y, int z)
	{
		int saveY = y;
		y = TileEntityTerra.getFirstBlockFrom(world, x, z, y + 16);
		if (saveY > y)
			return false;
		else
		{
			Block id = world.getBlock(x, y, z);
			if (id != Blocks.snow && id != Blocks.snow_layer)
			{
				if (Blocks.snow_layer.canPlaceBlockAt(world, x, y + 1, z) || id == Blocks.ice)
					ModUtils.setBlock(player, world, x, y + 1, z, Blocks.snow_layer);

				return false;
			}
			else
				return true;
		}
	}
}
