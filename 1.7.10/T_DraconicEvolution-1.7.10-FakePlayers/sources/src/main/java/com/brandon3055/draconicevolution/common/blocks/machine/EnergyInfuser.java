package com.brandon3055.draconicevolution.common.blocks.machine;

import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.gui.GuiHandler;
import com.brandon3055.draconicevolution.common.ModBlocks;
import com.brandon3055.draconicevolution.common.blocks.BlockCustomDrop;
import com.brandon3055.draconicevolution.common.lib.References;
import com.brandon3055.draconicevolution.common.lib.Strings;
import com.brandon3055.draconicevolution.common.tileentities.TileEnergyInfuser;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

/**
 * Created by Brandon on 27/06/2014.
 */
public class EnergyInfuser extends BlockCustomDrop
{
	public EnergyInfuser()
	{
		super(Material.iron);
		this.setBlockName(Strings.energyInfuserName);
		this.setCreativeTab(DraconicEvolution.tabBlocksItems);
		this.setStepSound(soundTypeStone);
		this.setBlockBounds(0f, 0f, 0f, 1f, 0.375f, 1f);
		ModBlocks.register(this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon(References.RESOURCESPREFIX + "machine_side");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEnergyInfuser();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float prx, float pry, float prz)
	{
		if (!world.isRemote)
			    
			player.openGui(DraconicEvolution.instance, GuiHandler.GUIID_ENERGY_INFUSER, world, x, y, z);
		world.markBlockForUpdate(x, y, z);
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return -1;
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
	protected boolean dropInventory()
	{
		return true;
	}

	@Override
	protected boolean hasCustomDropps()
	{
		return false;
	}

	@Override
	protected void getCustomTileEntityDrops(TileEntity te, List<ItemStack> droppes)
	{

	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side)
	{
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		return true;
	}
}
