package com.rwtema.extrautils.block;

import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.tileentity.TileEntityEnderThermicLavaPump;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class BlockEnderthermicPump extends Block
{
	IIcon pump;
	IIcon pumpTop;
	IIcon pumpBottom;

	public BlockEnderthermicPump()
	{
		super(Material.rock);
		this.setBlockName("extrautils:enderThermicPump");
		this.setBlockTextureName("extrautils:enderThermicPump");
		this.setCreativeTab(ExtraUtils.creativeTabExtraUtils);
		this.setHardness(1.0F);
		this.setStepSound(Block.soundTypeStone);
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(par1, 1, 0));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IIconRegister)
	{
		this.pump = par1IIconRegister.registerIcon("extrautils:enderThermicPump_side");
		this.pumpTop = par1IIconRegister.registerIcon("extrautils:enderThermicPump_top");
		this.pumpBottom = par1IIconRegister.registerIcon("extrautils:enderThermicPump");
		super.registerBlockIcons(par1IIconRegister);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2)
	{
		return par1 == 0 ? this.pumpBottom : par1 == 1 ? this.pumpTop : this.pump;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityEnderThermicLavaPump();
	}

	@Override
	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack)
	{
		TileEntity tile = par1World.getTileEntity(par2, par3, par4);
		if (tile instanceof TileEntityEnderThermicLavaPump && par5EntityLiving instanceof EntityPlayer)
		{
			TileEntityEnderThermicLavaPump pump = (TileEntityEnderThermicLavaPump) tile;
			EntityPlayer player = (EntityPlayer) par5EntityLiving;
    
    
		}
	}
}
