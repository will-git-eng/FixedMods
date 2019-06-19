package ru.will.git.ttinkerer;

import ru.will.git.reflectionmedic.util.EventUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import thaumic.tinkerer.common.block.BlockSummon;

public final class ItemBlockSummon extends ItemBlock
{
	public ItemBlockSummon(Block block)
	{
		super(block);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		if (EventUtils.cantBreak(player, x, y, z))
			return false;
		if (EventConfig.summonerOnlyOnePerChunk && world.blockExists(x, y, z))
		{
			Chunk chunk = world.getChunkFromBlockCoords(x, z);
			int maxY = world.getHeight();
			for (int chunkX = 0; chunkX < 16; chunkX++)
			{
				for (int chunkZ = 0; chunkZ < 16; chunkZ++)
				{
					for (int chunkY = 0; chunkY < maxY; chunkY++)
					{
						Block block = chunk.getBlock(chunkX, chunkY, chunkZ);
						if (block instanceof BlockSummon)
							return false;
					}
				}
			}
		}
		return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
	}
}
