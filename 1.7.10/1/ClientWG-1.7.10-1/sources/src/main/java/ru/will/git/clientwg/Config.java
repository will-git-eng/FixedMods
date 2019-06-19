package ru.will.git.clientwg;

import ru.will.git.clientwg.util.BlockMetaPair;
import ru.will.git.clientwg.util.ItemMetaPair;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public final class Config
{
	public final List<BlockMetaPair> forceAccessBlocks = new ArrayList<BlockMetaPair>();
	public final List<ItemMetaPair> forceAccessItems = new ArrayList<ItemMetaPair>();
	public final List<Class<? extends Entity>> forceAccessEntities = new ArrayList<Class<? extends Entity>>();

	public boolean hasForceAccess(Block block, int meta)
	{
		for (BlockMetaPair pair : this.forceAccessBlocks)
		{
			if (pair.isMatch(block, meta))
				return true;
		}
		return false;
	}

	public boolean hasForceAccess(World world, int x, int y, int z)
	{
		for (BlockMetaPair pair : this.forceAccessBlocks)
		{
			if (pair.isMatch(world, x, y, z))
				return true;
		}
		return false;
	}

	public boolean hasForceAccess(ItemStack stack)
	{
		if (stack != null)
			for (ItemMetaPair pair : this.forceAccessItems)
			{
				if (pair.isMatch(stack))
					return true;
			}
		return false;
	}

	public boolean hasForceAccess(Entity entity)
	{
		if (entity.getClass().getName().startsWith("noppes.npcs."))
			return true;
		for (Class<? extends Entity> entityClass : this.forceAccessEntities)
		{
			if (entityClass.isInstance(entity))
				return true;
		}
		return false;
	}
}
