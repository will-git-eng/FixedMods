package ru.will.git.clientwg.util;

import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public final class BlockMetaPair
{
	public static final int ALL_META = -1;
	public final Block block;
	public final int meta;

	public BlockMetaPair(Block block, int meta)
	{
		this.block = block;
		this.meta = meta;
	}

	public boolean isMatch(Block block, int meta)
	{
		return this.block == block && (this.meta == ALL_META || this.meta == meta);
	}

	public boolean isMatch(World world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		return block != Blocks.air && this.isMatch(block, world.getBlockMetadata(x, y, z));
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		BlockMetaPair that = (BlockMetaPair) o;
		return this.meta == that.meta && Objects.equal(this.block, that.block);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.block, this.meta);
	}
}
