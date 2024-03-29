package tconstruct.items.tools;

import ru.will.git.reflectionmedic.util.EventUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import tconstruct.library.ActiveToolMod;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.AOEHarvestTool;
import tconstruct.library.tools.AbilityHelper;
import tconstruct.tools.TinkerTools;

import java.util.HashSet;
import java.util.Set;

public class LumberAxe extends AOEHarvestTool
{
	public LumberAxe()
	{
		super(0, 1, 1);
		this.setUnlocalizedName("InfiTool.LumberAxe");
	}

	@Override
	protected Material[] getEffectiveMaterials()
	{
		return materials;
	}

	@Override
	protected String getHarvestType()
	{
		return "axe";
	}

	@Override
	public float getRepairCost()
	{
		return 4.0f;
	}

	@Override
	public float getDurabilityModifier()
	{
		return 2.5f;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack itemstack, World world, Block block, int x, int y, int z, EntityLivingBase player)
	{
		if (block != null && block.getMaterial() == Material.leaves)
			return false;

		return AbilityHelper.onBlockChanged(itemstack, world, block, x, y, z, player, this.random);
	}

	static Material[] materials = { Material.wood, Material.vine, Material.circuits, Material.cactus, Material.gourd };

    

    

	@Override
	public float breakSpeedModifier()
	{
		return 0.4f;
	}

	@Override
	public float stoneboundModifier()
	{
		return 216f;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player)
	{
		if (!stack.hasTagCompound() || player.isSneaking())
			return super.onBlockStartBreak(stack, x, y, z, player);

		World world = player.worldObj;
		final Block wood = world.getBlock(x, y, z);

		if (wood == null)
			return super.onBlockStartBreak(stack, x, y, z, player);

		if (wood.isWood(world, x, y, z) || wood.getMaterial() == Material.sponge)
			if (this.detectTree(world, x, y, z, wood))
			{
				NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
				int meta = world.getBlockMetadata(x, y, z);
    
				return true;
			}

		return super.onBlockStartBreak(stack, x, y, z, player);
	}

	private boolean detectTree(World world, int x, int y, int z, Block wood)
	{
		int height = y;
		boolean foundTop = false;
		do
		{
			height++;
			Block block = world.getBlock(x, height, z);
			if (block != wood)
			{
				height--;
				foundTop = true;
			}
		}
		while (!foundTop);

		int numLeaves = 0;
		if (height - y < 50)
			for (int xPos = x - 1; xPos <= x + 1; xPos++)
			{
				for (int yPos = height - 1; yPos <= height + 1; yPos++)
				{
					for (int zPos = z - 1; zPos <= z + 1; zPos++)
					{
						Block leaves = world.getBlock(xPos, yPos, zPos);
						if (leaves != null && leaves.isLeaves(world, xPos, yPos, zPos))
							numLeaves++;
					}
				}
			}

		return numLeaves > 3;
    
	private void breakTree(World world, int x, int y, int z, int xStart, int yStart, int zStart, ItemStack stack, NBTTagCompound tags, Block bID, int meta, EntityPlayer player)
	{
		this.breakTree(new HashSet<ChunkPosition>(), world, x, y, z, xStart, yStart, zStart, stack, tags, bID, meta, player);
    
    
	private void breakTree(Set<ChunkPosition> set, World world, int x, int y, int z, int xStart, int yStart, int zStart, ItemStack stack, NBTTagCompound tags, Block bID, int meta, EntityPlayer player)
    
		if (!set.add(new ChunkPosition(x, y, z)))
    

		for (int xPos = x - 1; xPos <= x + 1; xPos++)
		{
			for (int yPos = y; yPos <= y + 1; yPos++)
			{
				for (int zPos = z - 1; zPos <= z + 1; zPos++)
				{
					if (!tags.getBoolean("Broken"))
					{
						Block localBlock = world.getBlock(xPos, yPos, zPos);
						if (bID == localBlock)
						{
							int localMeta = world.getBlockMetadata(xPos, yPos, zPos);
							int hlvl = localBlock.getHarvestLevel(localMeta);
							float localHardness = localBlock == null ? Float.MAX_VALUE : localBlock.getBlockHardness(world, xPos, yPos, zPos);

							if (hlvl <= tags.getInteger("HarvestLevel") && !(localHardness < 0))
							{
								boolean cancelHarvest = false;
								for (ActiveToolMod mod : TConstructRegistry.activeModifiers)
								{
									if (mod.beforeBlockBreak(this, stack, xPos, yPos, zPos, player))
										cancelHarvest = true;
    
								BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(x, y, z, world, localBlock, localMeta, player);
								event.setCanceled(cancelHarvest);
								MinecraftForge.EVENT_BUS.post(event);
								cancelHarvest = event.isCanceled();

								int xDist = xPos - xStart;
								int yDist = yPos - yStart;
								int zDist = zPos - zStart;

								if (9 * xDist * xDist + yDist * yDist + 9 * zDist * zDist < 2500)
    
									if (EventUtils.cantBreak(player, xPos, yPos, zPos))
    

    
										this.breakTree(set, world, xPos, yPos, zPos, xStart, yStart, zStart, stack, tags, bID, meta, player);
									else if (localBlock == bID && localMeta % 4 == meta % 4)
									{
										if (!player.capabilities.isCreativeMode)
										{
											localBlock.harvestBlock(world, player, x, y, z, localMeta);
											this.onBlockDestroyed(stack, world, localBlock, xPos, yPos, zPos, player);
										}

										world.setBlockToAir(xPos, yPos, zPos);
    
											this.breakTree(set, world, xPos, yPos, zPos, xStart, yStart, zStart, stack, tags, bID, meta, player);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Item getHeadItem()
	{
		return TinkerTools.broadAxeHead;
	}

	@Override
	public Item getHandleItem()
	{
		return TinkerTools.toughRod;
	}

	@Override
	public Item getAccessoryItem()
	{
		return TinkerTools.largePlate;
	}

	@Override
	public Item getExtraItem()
	{
		return TinkerTools.toughBinding;
	}

	@Override
	public int getPartAmount()
	{
		return 4;
	}

	@Override
	public String getIconSuffix(int partType)
	{
		switch (partType)
		{
			case 0:
				return "_lumberaxe_head";
			case 1:
				return "_lumberaxe_head_broken";
			case 2:
				return "_lumberaxe_handle";
			case 3:
				return "_lumberaxe_shield";
			case 4:
				return "_lumberaxe_binding";
			default:
				return "";
		}
	}

	@Override
	public String getEffectSuffix()
	{
		return "_lumberaxe_effect";
	}

	@Override
	public String getDefaultFolder()
	{
		return "lumberaxe";
	}

	@Override
	public int durabilityTypeAccessory()
	{
		return 2;
	}

	@Override
	public int durabilityTypeExtra()
	{
		return 1;
	}
}
