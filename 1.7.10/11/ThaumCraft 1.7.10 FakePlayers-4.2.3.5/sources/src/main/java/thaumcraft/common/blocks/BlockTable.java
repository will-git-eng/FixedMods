package thaumcraft.common.blocks;

import ru.will.git.reflectionmedic.util.EventUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.TileArcaneWorkbench;
import thaumcraft.common.tiles.TileDeconstructionTable;
import thaumcraft.common.tiles.TileResearchTable;
import thaumcraft.common.tiles.TileTable;

import java.util.List;

public class BlockTable extends BlockContainer implements IWandable
{
	public IIcon icon;
	public IIcon iconQuill;

	public BlockTable()
	{
		super(Material.wood);
		this.setHardness(2.5F);
		this.setStepSound(soundTypeWood);
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		this.icon = ir.registerIcon("thaumcraft:woodplain");
		this.iconQuill = ir.registerIcon("thaumcraft:tablequill");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		return this.icon;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		return side == ForgeDirection.UP || super.isSideSolid(world, x, y, z, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 14));
		par3List.add(new ItemStack(par1, 1, 15));
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return (metadata <= 1 || metadata >= 6) && metadata < 14 ? new TileTable() : metadata == 14 ? new TileDeconstructionTable() : metadata == 15 ? new TileArcaneWorkbench() : new TileResearchTable();
	}

	@Override
	public void onBlockPlacedBy(World par1World, int x, int y, int z, EntityLivingBase entity, ItemStack is)
	{
		int md = par1World.getBlockMetadata(x, y, z);
		if (md < 14)
    
			if (entity instanceof EntityPlayer && EventUtils.cantBreak((EntityPlayer) entity, x, y, z))
    

			int var7 = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int out = var7 == 0 ? 0 : var7 == 1 ? 1 : var7 == 2 ? 0 : var7 == 3 ? 1 : 0;
			par1World.setBlock(x, y, z, this, out, 3);
		}
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return ConfigBlocks.blockTableRI;
	}

	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, Block par5, int par6)
	{
		InventoryUtils.dropItems(par1World, par2, par3, par4);
		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}

	@Override
	public int getDamageValue(World par1World, int par2, int par3, int par4)
	{
		int md = par1World.getBlockMetadata(par2, par3, par4);
		return md >= 2 && md <= 9 ? 2 : super.getDamageValue(par1World, par2, par3, par4);
	}

	@Override
	public int damageDropped(int par1)
	{
		return par1 == 14 ? 14 : par1 == 15 ? 15 : 0;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block par5)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		int md = world.getBlockMetadata(x, y, z);
		if (tile instanceof TileResearchTable)
		{
			int mm = world.getBlockMetadata(x + ForgeDirection.getOrientation(md).offsetX, y + ForgeDirection.getOrientation(md).offsetY, z + ForgeDirection.getOrientation(md).offsetZ);
			if (mm < 6)
			{
				InventoryUtils.dropItems(world, x, y, z);
				world.setTileEntity(x, y, z, new TileTable());
				world.setBlock(x, y, z, this, 0, 3);
			}
		}
		else if (md >= 6 && md < 14)
		{
			TileEntity tile2 = world.getTileEntity(x + ForgeDirection.getOrientation(md - 4).offsetX, y + ForgeDirection.getOrientation(md - 4).offsetY, z + ForgeDirection.getOrientation(md - 4).offsetZ);
			if (!(tile2 instanceof TileResearchTable))
				world.setBlock(x, y, z, this, 0, 3);
		}

		super.onNeighborBlockChange(world, x, y, z, par5);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int idk, float what, float these, float are)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		int md = world.getBlockMetadata(x, y, z);
		if (md > 1 && tileEntity != null && !player.isSneaking())
		{
			if (world.isRemote)
				return true;
			else if (tileEntity instanceof TileArcaneWorkbench)
    
				if (((TileArcaneWorkbench) tileEntity).containers.containsKey(player))
    

				player.openGui(Thaumcraft.instance, 13, world, x, y, z);
				return true;
			}
			else if (tileEntity instanceof TileDeconstructionTable)
			{
				player.openGui(Thaumcraft.instance, 8, world, x, y, z);
				return true;
			}
			else
			{
				if (tileEntity instanceof TileResearchTable)
					player.openGui(Thaumcraft.instance, 10, world, x, y, z);
				else
					for (int a = 2; a < 6; ++a)
					{
						TileEntity tile = world.getTileEntity(x + ForgeDirection.getOrientation(a).offsetX, y + ForgeDirection.getOrientation(a).offsetY, z + ForgeDirection.getOrientation(a).offsetZ);
						if (tile instanceof TileResearchTable)
						{
							player.openGui(Thaumcraft.instance, 10, world, x + ForgeDirection.getOrientation(a).offsetX, y + ForgeDirection.getOrientation(a).offsetY, z + ForgeDirection.getOrientation(a).offsetZ);
							break;
						}
					}

				return true;
			}
		}
		else
			return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int md)
	{
		return null;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		int md = world.getBlockMetadata(x, y, z);
		switch (md)
		{
			case 2:
			case 6:
				return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ - 1.0D, x + this.maxX, y + this.maxY, z + this.maxZ);
			case 3:
			case 7:
				return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ + 1.0D);
			case 4:
			case 8:
				return AxisAlignedBB.getBoundingBox(x + this.minX - 1.0D, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
			case 5:
			case 9:
				return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX + 1.0D, y + this.maxY, z + this.maxZ);
			default:
				return super.getSelectedBoundingBoxFromPool(world, x, y, z);
		}
	}

	@Override
	public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md)
	{
		if (md <= 1)
		{
			ItemWandCasting wand = (ItemWandCasting) wandstack.getItem();
			world.setBlock(x, y, z, ConfigBlocks.blockTable, 15, 3);
			world.setTileEntity(x, y, z, new TileArcaneWorkbench());
			TileArcaneWorkbench tawb = (TileArcaneWorkbench) world.getTileEntity(x, y, z);
			if (tawb != null && !wand.isStaff(wandstack))
			{
				tawb.setInventorySlotContents(10, wandstack.copy());
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}

			tawb.markDirty();
			world.markBlockForUpdate(x, y, z);
			world.playSoundEffect(x + 0.5D, y + 0.1D, z + 0.5D, "random.click", 0.15F, 0.5F);
			return 0;
		}
		else
			return -1;
	}

	@Override
	public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player)
	{
		return null;
	}

	@Override
	public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count)
	{
	}

	@Override
	public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count)
	{
	}
}
