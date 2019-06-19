package thaumcraft.common.items.wands.foci;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.thaumcraft.EventConfig;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.BlockCoordinates;
import thaumcraft.api.IArchitect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.tiles.TileWarded;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemFocusWarding extends ItemFocusBasic implements IArchitect
{
	public IIcon iconOrnament;
	IIcon depthIcon = null;
	private static final AspectList cost = new AspectList().add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.WATER, 10);
	public static HashMap<String, Long> delay = new HashMap();
	ArrayList<BlockCoordinates> checked = new ArrayList();

	public ItemFocusWarding()
	{
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@Override
	public String getSortingHelper(ItemStack itemstack)
	{
		return "BWA" + super.getSortingHelper(itemstack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.depthIcon = ir.registerIcon("thaumcraft:focus_warding_depth");
		this.icon = ir.registerIcon("thaumcraft:focus_warding");
		this.iconOrnament = ir.registerIcon("thaumcraft:focus_warding_orn");
	}

	@Override
	public IIcon getFocusDepthLayerIcon(ItemStack itemstack)
	{
		return this.depthIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int par1, int renderPass)
	{
		return renderPass == 1 ? this.icon : this.iconOrnament;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	public IIcon getOrnament(ItemStack itemstack)
	{
		return this.iconOrnament;
	}

	@Override
	public int getFocusColor(ItemStack itemstack)
	{
		return 16771535;
	}

	@Override
	public AspectList getVisCost(ItemStack itemstack)
	{
		return cost.copy();
	}

	@Override
	public ItemStack onFocusRightClick(ItemStack stack, World world, EntityPlayer player, MovingObjectPosition mop)
    
		if (!EventConfig.enableFocusWarding)
    

		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
		player.swingItem();
		if (!world.isRemote && mop != null && mop.typeOfHit == MovingObjectType.BLOCK)
		{
			String key = mop.blockX + ":" + mop.blockY + ":" + mop.blockZ + ":" + world.provider.dimensionId;
			if (delay.containsKey(key) && delay.get(key) > System.currentTimeMillis())
				return stack;

			delay.put(key, System.currentTimeMillis() + 500L);
			TileEntity tt = world.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
			boolean solid = world.isBlockNormalCubeDefault(mop.blockX, mop.blockY, mop.blockZ, true);
			if (tt == null && solid)
			{
				for (BlockCoordinates c : this.getArchitectBlocks(stack, world, mop.blockX, mop.blockY, mop.blockZ, mop.sideHit, player))
				{
					if (!wand.consumeAllVis(stack, player, this.getVisCost(stack), true, false))
						break;

					if (world.getTileEntity(c.x, c.y, c.z) == null && world.isBlockNormalCubeDefault(c.x, c.y, c.z, true))
    
						if (EventUtils.cantBreak(player, c.x, c.y, c.z))
    

						Block bi = world.getBlock(c.x, c.y, c.z);
						int md = world.getBlockMetadata(c.x, c.y, c.z);
						int ll = bi.getLightValue(world, c.x, c.y, c.z);
						world.setBlock(c.x, c.y, c.z, ConfigBlocks.blockWarded, md, 3);
						TileEntity tile = world.getTileEntity(c.x, c.y, c.z);
						if (tile instanceof TileWarded)
						{
							TileWarded tw = (TileWarded) tile;
							tw.block = bi;
							tw.blockMd = (byte) md;
							tw.light = (byte) ll;
							tw.owner = player.getCommandSenderName().hashCode();
							world.markBlockForUpdate(c.x, c.y, c.z);
							PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(c.x, c.y, c.z, 16556032), new TargetPoint(world.provider.dimensionId, c.x, c.y, c.z, 32.0D));
						}
					}
				}

				world.playSoundEffect(mop.blockX + 0.5D, mop.blockY + 0.5D, mop.blockZ + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
			}
			else if (tt instanceof TileWarded)
			{
				TileWarded tw = (TileWarded) tt;
				if (tw.owner == player.getCommandSenderName().hashCode())
				{
					for (BlockCoordinates c : this.getArchitectBlocks(stack, world, mop.blockX, mop.blockY, mop.blockZ, mop.sideHit, player))
					{
						TileEntity tile = world.getTileEntity(c.x, c.y, c.z);
						if (tile instanceof TileWarded)
						{
							TileWarded tw2 = (TileWarded) tile;
							if (tw2.owner == player.getCommandSenderName().hashCode())
    
								if (EventUtils.cantBreak(player, c.x, c.y, c.z))
    

								world.setBlock(c.x, c.y, c.z, tw2.block, tw2.blockMd, 3);
								world.markBlockForUpdate(c.x, c.y, c.z);
								PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(c.x, c.y, c.z, 16556032), new TargetPoint(world.provider.dimensionId, c.x, c.y, c.z, 32.0D));
							}
						}
					}

					world.playSoundEffect(mop.blockX + 0.5D, mop.blockY + 0.5D, mop.blockZ + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
				}
			}
		}

		return stack;
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank)
	{
		switch (rank)
		{
			case 1:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal };
			case 2:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal, FocusUpgradeType.architect };
			case 3:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal, FocusUpgradeType.enlarge };
			case 4:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal, FocusUpgradeType.enlarge };
			case 5:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal, FocusUpgradeType.enlarge };
			default:
				return null;
		}
	}

	@Override
	public boolean canApplyUpgrade(ItemStack focusstack, EntityPlayer player, FocusUpgradeType type, int rank)
	{
		return !type.equals(FocusUpgradeType.enlarge) || this.isUpgradedWith(focusstack, FocusUpgradeType.architect);
	}

	@Override
	public int getMaxAreaSize(ItemStack focusstack)
	{
		return 3 + this.getUpgradeLevel(focusstack, FocusUpgradeType.enlarge);
	}

	@Override
	public ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, int side, EntityPlayer player)
	{
		ArrayList<BlockCoordinates> out = new ArrayList();
		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
		wand.getFocus(stack);
		this.checked.clear();
		boolean tiles = false;
		TileEntity tt = world.getTileEntity(x, y, z);
		boolean solid = world.isBlockNormalCubeDefault(x, y, z, true);
		if ((tt != null || !solid) && tt instanceof TileWarded)
			tiles = true;

		int sizeX = 0;
		int sizeY = 0;
		int sizeZ = 0;
		if (this.isUpgradedWith(wand.getFocusItem(stack), FocusUpgradeType.architect))
		{
			sizeX = WandManager.getAreaX(stack);
			sizeY = WandManager.getAreaY(stack);
			sizeZ = WandManager.getAreaZ(stack);
		}

		if (side != 2 && side != 3)
			this.checkNeighbours(world, x, y, z, new BlockCoordinates(x, y, z), side, sizeX, sizeY, sizeZ, out, player, tiles);
		else
			this.checkNeighbours(world, x, y, z, new BlockCoordinates(x, y, z), side, sizeZ, sizeY, sizeX, out, player, tiles);

		return out;
	}

	public void checkNeighbours(World world, int x, int y, int z, BlockCoordinates pos, int side, int sizeX, int sizeY, int sizeZ, ArrayList<BlockCoordinates> list, EntityPlayer player, boolean tiles)
	{
		if (!this.checked.contains(pos))
		{
			this.checked.add(pos);
			switch (side)
			{
				case 0:
				case 1:
					if (Math.abs(pos.x - x) > sizeX)
						return;

					if (Math.abs(pos.z - z) > sizeZ)
						return;

					if (Math.abs(pos.y - y) > sizeY)
						return;
					break;
				case 2:
				case 3:
					if (Math.abs(pos.x - x) > sizeX)
						return;

					if (Math.abs(pos.y - y) > sizeZ)
						return;

					if (Math.abs(pos.z - z) > sizeY)
						return;
					break;
				case 4:
				case 5:
					if (Math.abs(pos.y - y) > sizeX)
						return;

					if (Math.abs(pos.z - z) > sizeZ)
						return;

					if (Math.abs(pos.x - x) > sizeY)
						return;
			}

			TileEntity tt = world.getTileEntity(pos.x, pos.y, pos.z);
			boolean solid = world.isBlockNormalCubeDefault(pos.x, pos.y, pos.z, true);
			if (!tiles || tt instanceof TileWarded)
				if (tiles || tt == null && solid)
				{
					if (tiles && tt instanceof TileWarded)
					{
						TileWarded tw2 = (TileWarded) tt;
						if (tw2.owner != player.getCommandSenderName().hashCode())
							return;
					}

					if (!world.isAirBlock(pos.x, pos.y, pos.z))
					{
						list.add(pos);

						for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
						{
							BlockCoordinates cc = new BlockCoordinates(pos.x + dir.offsetX, pos.y + dir.offsetY, pos.z + dir.offsetZ);
							this.checkNeighbours(world, x, y, z, cc, side, sizeX, sizeY, sizeZ, list, player, tiles);
						}

					}
				}
		}
	}

	@Override
	public boolean showAxis(ItemStack stack, World world, EntityPlayer player, int side, IArchitect.EnumAxis axis)
	{
		int dim = WandManager.getAreaDim(stack);
		if (dim == 0)
			return true;
		else
		{
			switch (side)
			{
				case 0:
				case 1:
					if (axis == IArchitect.EnumAxis.X && dim == 1 || axis == IArchitect.EnumAxis.Z && dim == 2 || axis == IArchitect.EnumAxis.Y && dim == 3)
						return true;
					break;
				case 2:
				case 3:
					if (axis == IArchitect.EnumAxis.Y && dim == 1 || axis == IArchitect.EnumAxis.X && dim == 2 || axis == IArchitect.EnumAxis.Z && dim == 3)
						return true;
					break;
				case 4:
				case 5:
					if (axis == IArchitect.EnumAxis.Y && dim == 1 || axis == IArchitect.EnumAxis.Z && dim == 2 || axis == IArchitect.EnumAxis.X && dim == 3)
						return true;
			}

			return false;
		}
	}
}
