package com.rwtema.extrautils.tileentity.enderquarry;

import com.rwtema.extrautils.ExtraUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockEnderQuarry extends Block
{
	int[] tiletype = { 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };
	IIcon[] top = new IIcon[3];
	IIcon[] bottom = new IIcon[3];
	IIcon[] side = new IIcon[3];

	public BlockEnderQuarry()
	{
		super(Material.rock);
		this.setBlockName("extrautils:enderQuarry");
		this.setBlockTextureName("extrautils:enderQuarry");
		this.setCreativeTab(ExtraUtils.creativeTabExtraUtils);
		this.setHardness(1.0F);
		this.setStepSound(Block.soundTypeStone);
    
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
	{
		super.onBlockPlacedBy(world, x, y, z, entity, stack);
		if (entity instanceof EntityPlayer)
		{
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileEntityEnderQuarry)
				((TileEntityEnderQuarry) tile).fake.setProfile(((EntityPlayer) entity).getGameProfile());
		}
    

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityEnderQuarry();
	}

	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5)
	{
		TileEntity tile;
		if ((tile = par1World.getTileEntity(par2, par3, par4)) instanceof TileEntityEnderQuarry)
			((TileEntityEnderQuarry) tile).detectInventories();
	}

	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
	{
		if (par1World.isRemote)
			return true;
		else
		{
			TileEntity tile;
			if ((tile = par1World.getTileEntity(par2, par3, par4)) instanceof TileEntityEnderQuarry)
			{
				((TileEntityEnderQuarry) tile).startFencing(par5EntityPlayer);
				if (par5EntityPlayer.getHeldItem() == null && par5EntityPlayer.capabilities.isCreativeMode && par5EntityPlayer.isSneaking() && ((TileEntityEnderQuarry) tile).started)
				{
					par5EntityPlayer.addChatComponentMessage(new ChatComponentText("Overclock Mode Activated"));
					((TileEntityEnderQuarry) tile).debug();
				}
			}

			return true;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IIconRegister)
	{
		this.top[0] = this.top[2] = par1IIconRegister.registerIcon("extrautils:enderQuarry_top");
		this.top[1] = par1IIconRegister.registerIcon("extrautils:enderQuarry_top_active");
		this.bottom[0] = this.bottom[1] = this.bottom[2] = par1IIconRegister.registerIcon("extrautils:enderQuarry_bottom");
		this.side[0] = blockIcon = par1IIconRegister.registerIcon("extrautils:enderQuarry");
		this.side[1] = par1IIconRegister.registerIcon("extrautils:enderQuarry_active");
		this.side[2] = par1IIconRegister.registerIcon("extrautils:enderQuarry_finished");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2)
	{
		if (par2 > 2 || par2 < 0)
			par2 = 0;

		return par1 == 0 ? this.bottom[par2] : par1 == 1 ? this.top[par2] : this.side[par2];
	}
}