package ic2.core.item.tfbp;

import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.core.Ic2Items;
import ic2.core.block.BlockRubSapling;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
    
public class ItemTFBPIrrigation extends ItemTFBP implements ITerraformingBPFakePlayer
{
	public ItemTFBPIrrigation(int index)
	{
		super(index);
	}

	@Override
	public int getConsume(ItemStack item)
	{
		return 3000;
	}

	@Override
	public int getRange(ItemStack item)
	{
		return 60;
	}

	@Override
	public boolean terraform(ItemStack item, World world, int x, int z, int yCoord)
	{
		return this.terraform(ModUtils.getModFake(world), item, world, x, z, yCoord);
	}

	@Override
	public boolean terraform(EntityPlayer player, ItemStack item, World world, int x, int z, int yCoord)
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
			else if (TileEntityTerra.switchGround(world, Blocks.sand, Blocks.dirt, x, y, z, true))
			{
				TileEntityTerra.switchGround(world, Blocks.sand, Blocks.dirt, x, y, z, true);
				return true;
			}
			else
			{
				Block id = world.getBlock(x, y, z);
				if (id != Blocks.tallgrass)
				{
					if (id == Blocks.sapling)
					{
						((BlockSapling) Blocks.sapling).func_149878_d(world, x, y, z, world.rand);
						return true;
					}
					else if (id == Block.getBlockFromItem(Ic2Items.rubberSapling.getItem()))
					{
						((BlockRubSapling) Block.getBlockFromItem(Ic2Items.rubberSapling.getItem())).growTree(world, x, y, z, world.rand);
						return true;
					}
					else if (id == Blocks.log)
					{
						int meta = world.getBlockMetadata(x, y, z);
						ModUtils.setBlock(player, world, x, y + 1, z, Blocks.log, meta, 3);
						this.createLeaves(player, world, x, y + 2, z, meta);
						this.createLeaves(player, world, x + 1, y + 1, z, meta);
						this.createLeaves(player, world, x - 1, y + 1, z, meta);
						this.createLeaves(player, world, x, y + 1, z + 1, meta);
						this.createLeaves(player, world, x, y + 1, z - 1, meta);
						return true;
					}
					else if (id == Blocks.wheat)
					{
						world.setBlockMetadataWithNotify(x, y, z, 7, 3);
						return true;
					}
					else if (id == Blocks.fire)
					{
						world.setBlockMetadataWithNotify(x, y, z, 0, 3);
						return true;
					}
					else
						return false;
				}
				else
					return this.spreadGrass(player, world, x + 1, y, z) || this.spreadGrass(player, world, x - 1, y, z) || this.spreadGrass(player, world, x, y, z + 1) || this.spreadGrass(player, world, x, y, z - 1);
			}
		}
	}

	public void createLeaves(World world, int x, int y, int z, int meta)
	{
		this.createLeaves(ModUtils.getModFake(world), world, x, y, z, meta);
	}

	public void createLeaves(EntityPlayer player, World world, int x, int y, int z, int meta)
	{
		if (world.getBlock(x, y, z) == Blocks.air)
			ModUtils.setBlock(player, world, x, y, z, Blocks.leaves, meta, 3);
	}

	public boolean spreadGrass(World world, int x, int y, int z)
	{
		return this.spreadGrass(ModUtils.getModFake(world), world, x, y, z);
	}

	public boolean spreadGrass(EntityPlayer player, World world, int x, int y, int z)
	{
		if (world.rand.nextBoolean())
			return false;
		else
		{
			y = TileEntityTerra.getFirstBlockFrom(world, x, z, y);
			Block id = world.getBlock(x, y, z);
			if (id == Blocks.dirt)
			{
				ModUtils.setBlock(player, world, x, y, z, Blocks.grass);
				return true;
			}
			else if (id == Blocks.grass)
			{
				ModUtils.setBlock(player, world, x, y + 1, z, Blocks.tallgrass, 1, 3);
				return true;
			}
			else
				return false;
		}
	}
}
