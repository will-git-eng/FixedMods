    
package thaumic.tinkerer.common.item.kami.tool;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.ttinkerer.EventConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.block.kami.BlockBedrockPortal;
import thaumic.tinkerer.common.core.handler.ConfigHandler;
import thaumic.tinkerer.common.dim.WorldProviderBedrock;

import java.util.List;

public final class ToolHandler
{
	public static Material[] materialsPick = { Material.rock, Material.iron, Material.ice, Material.glass, Material.piston, Material.anvil };
	public static Material[] materialsShovel = { Material.grass, Material.ground, Material.sand, Material.snow, Material.craftedSnow, Material.clay };
	public static Material[] materialsAxe = { Material.coral, Material.leaves, Material.plants, Material.wood };

	public static int getMode(ItemStack tool)
	{
		return tool.getItemDamage();
	}

	public static int getNextMode(int mode)
    
    
	}

	public static void changeMode(ItemStack tool)
	{
		int mode = getMode(tool);
		tool.setItemDamage(getNextMode(mode));
	}

	public static boolean isRightMaterial(Material material, Material[] materialsListing)
    
    
		for (Material mat : materialsListing)
		{
			if (material == mat)
				return true;
		}

		return false;
	}

	public static void removeBlocksInIteration(EntityPlayer player, World world, int x, int y, int z, int xs, int ys, int zs, int xe, int ye, int ze, Block block, Material[] materialsListing, boolean silk, int fortune)
	{
		MovingObjectPosition mop = raytraceFromEntity(player.worldObj, player, false, 4.5d);
		if (mop == null)
			return;
		for (int x1 = xs; x1 < xe; x1++)
		{
			for (int y1 = ys; y1 < ye; y1++)
			{
				for (int z1 = zs; z1 < ze; z1++)
				{
					if (x == x1 + x && y == y1 + y && z == z1 + z)
						continue;
					breakExtraBlock(player.worldObj, x1 + x, y1 + y, z1 + z, player, x, y, z, materialsListing);
				}
			}
		}
		List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(x + xs, y + ys, z + zs, x + xe, y + ye, z + ze));
		for (EntityItem item : items)
		{
			item.setPosition(player.posX, player.posY + 1, player.posZ);
		}
	}

	protected static void breakExtraBlock(World world, int x, int y, int z, EntityPlayer playerEntity, int refX, int refY, int refZ, Material[] materialsListing)
	{
		if (world.isAirBlock(x, y, z))
    
		if (!(playerEntity instanceof EntityPlayerMP))
			return;
		EntityPlayerMP player = (EntityPlayerMP) playerEntity;
		Block block = world.getBlock(x, y, z);
    
		if (!block.canHarvestBlock(player, meta) || !isRightMaterial(block.getMaterial(), materialsListing))
    
		if (!EventConfig.enableIchorPickAdvBedrockBreaking && block.getBlockHardness(world, x, y, z) < 0)
			return;

		if (EventUtils.cantBreak(player, x, y, z))
    
    
		BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(world, player.theItemInWorldManager.getGameType(), player, x, y, z);
		if (event.isCanceled())
			return;

		if (player.capabilities.isCreativeMode)
		{
			block.onBlockHarvested(world, x, y, z, meta, player);
			if (block.removedByPlayer(world, player, x, y, z, false))
    
			if (!world.isRemote)
				player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
			return;
    
    
		if (!world.isRemote)
    
    
			block.onBlockHarvested(world, x, y, z, meta, player);

    
			{
				block.onBlockDestroyedByPlayer(world, x, y, z, meta);
				if (block != Blocks.bedrock)
					block.harvestBlock(world, player, x, y, z, meta);

				block.dropXpOnBlockBreak(world, x, y, z, event.getExpToDrop());
    
			player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
    
		else
    
    
    
    
			world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
			if (block.removedByPlayer(world, player, x, y, z, true))
				block.onBlockDestroyedByPlayer(world, x, y, z, meta);

			Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C07PacketPlayerDigging(2, x, y, z, Minecraft.getMinecraft().objectMouseOver.sideHit));
		}
	}

	public static void removeBlockWithDrops(EntityPlayer player, World world, int x, int y, int z, int bx, int by, int bz, Block block, Material[] materialsListing, boolean silk, int fortune, float blockHardness, int metadata)
	{
		if (!world.blockExists(x, y, z))
			return;

		Block blk = world.getBlock(x, y, z);

		if (block != null && blk != block)
    
		if (!EventConfig.enableIchorPickAdvBedrockBreaking && blk.getBlockHardness(world, x, y, z) < 0)
			return;

		if (EventUtils.cantBreak(player, x, y, z))
    

		int meta = world.getBlockMetadata(x, y, z);
		Material mat = world.getBlock(x, y, z).getMaterial();
		if (blk != null && !blk.isAir(world, x, y, z) && (blk.getPlayerRelativeBlockHardness(player, world, x, y, z) != 0 || blk == Blocks.bedrock && y <= 253 && world.provider instanceof WorldProviderBedrock))
		{
			if (!blk.canHarvestBlock(player, meta) || !isRightMaterial(mat, materialsListing))
				return;
			if (ConfigHandler.bedrockDimensionID != 0 && block == Blocks.bedrock && (world.provider.isSurfaceWorld() && y < 5 || y > 253 && world.provider instanceof WorldProviderBedrock))
				world.setBlock(x, y, z, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockBedrockPortal.class));
			if (ConfigHandler.bedrockDimensionID != 0 && world.provider.dimensionId == ConfigHandler.bedrockDimensionID && blk == Blocks.bedrock && y <= 253)
				world.setBlock(x, y, z, Blocks.air);
			if (!player.capabilities.isCreativeMode && blk != Blocks.bedrock)
    
			}
			else
				world.setBlockToAir(x, y, z);
		}
	}

	public static String getToolModeStr(IAdvancedTool tool, ItemStack stack)
	{
		return StatCollector.translateToLocal("ttmisc.mode." + tool.getType() + "." + ToolHandler.getMode(stack));
	}

    
	public static MovingObjectPosition raytraceFromEntity(World world, Entity player, boolean par3, double range)
	{
		float f = 1.0F;
		float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
		float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * f;
		double d1 = player.prevPosY + (player.posY - player.prevPosY) * f;
		if (!world.isRemote && player instanceof EntityPlayer)
			d1 += 1.62D;
		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * f;
		Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = range;
		if (player instanceof EntityPlayerMP)
			d3 = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
		Vec3 vec31 = vec3.addVector(f7 * d3, f6 * d3, f8 * d3);
		return world.rayTraceBlocks(vec3, vec31, par3);
	}
}