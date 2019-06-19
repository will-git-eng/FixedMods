package com.rwtema.extrautils.tileentity.transfernodes;

import java.util.List;
import java.util.Random;

import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.ExtraUtilsMod;
import com.rwtema.extrautils.ICreativeTabSorting;
import com.rwtema.extrautils.block.BlockMultiBlockSelection;
import com.rwtema.extrautils.block.Box;
import com.rwtema.extrautils.block.BoxModel;
import com.rwtema.extrautils.helper.XUHelper;
import com.rwtema.extrautils.helper.XURandom;
import com.rwtema.extrautils.multipart.FMPBase;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IPipe;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IPipeBlock;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IPipeCosmetic;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.StdPipes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockTransferPipe extends BlockMultiBlockSelection implements ICreativeTabSorting, IPipeBlock
{
	public static final float pipe_width = 0.125F;
	public static IIcon pipes_oneway;
	public static IIcon pipes_nozzle = null;
	public static IIcon pipes_grouping = null;
	public static IIcon pipes = null;
	public static IIcon pipes_noninserting = null;
	public static IIcon pipes_xover = null;
	public static IIcon pipes_1way = null;
	public static IIcon[] pipes_diamond = new IIcon[6];
	public static IIcon pipes_supply = null;
	public static IIcon pipes_modsorting = null;
	public static IIcon pipes_energy = null;
	public static IIcon pipes_nozzle_energy = null;
	public static IIcon pipes_energy_extract = null;
	public static IIcon pipes_nozzle_energy_extract = null;
	public static IIcon pipes_hyperrationing = null;
	private Random random = XURandom.getInstance();
	public final int pipePage;

	public BlockTransferPipe(int pipePage)
	{
		super(Material.sponge);
		this.pipePage = pipePage;
		this.setBlockName("extrautils:pipes" + (pipePage == 0 ? "" : "." + pipePage));
		this.setBlockTextureName("extrautils:pipes");
		this.setCreativeTab(ExtraUtils.creativeTabExtraUtils);
		this.setHardness(0.1F);
		this.setStepSound(soundTypeStone);
	}

	@Override
	public int getMobilityFlag()
	{
		return 0;
	}

	@Override
	public int damageDropped(int par1)
	{
		return this.pipePage != 0 || par1 > 7 && par1 != 15 ? par1 : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IIconRegister)
	{
		pipes = par1IIconRegister.registerIcon("extrautils:pipes");
		pipes_oneway = par1IIconRegister.registerIcon("extrautils:pipes_1way");
		pipes_nozzle = par1IIconRegister.registerIcon("extrautils:pipes_nozzle");
		pipes_grouping = par1IIconRegister.registerIcon("extrautils:pipes_grouping");
		pipes_noninserting = par1IIconRegister.registerIcon("extrautils:pipes_noninserting");
		pipes_1way = par1IIconRegister.registerIcon("extrautils:pipes_1way2");

		for (int i = 0; i < 6; ++i)
			pipes_diamond[i] = par1IIconRegister.registerIcon("extrautils:pipes_diamond" + i);

		pipes_supply = par1IIconRegister.registerIcon("extrautils:pipes_supply");
		pipes_energy = par1IIconRegister.registerIcon("extrautils:pipes_energy");
		pipes_energy_extract = par1IIconRegister.registerIcon("extrautils:pipes_energy_extract");
		pipes_xover = par1IIconRegister.registerIcon("extrautils:pipes_crossover");
		pipes_modsorting = par1IIconRegister.registerIcon("extrautils:pipes_modgrouping");
		pipes_nozzle_energy = par1IIconRegister.registerIcon("extrautils:pipes_nozzle_energy");
		pipes_nozzle_energy_extract = par1IIconRegister.registerIcon("extrautils:pipes_nozzle_energy_extract");
		pipes_hyperrationing = par1IIconRegister.registerIcon("extrautils:pipes_hypersupply");
		super.registerBlockIcons(par1IIconRegister);
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		switch (this.pipePage)
		{
			case 0:
				par3List.add(new ItemStack(par1, 1, 0));
				par3List.add(new ItemStack(par1, 1, 8));
				par3List.add(new ItemStack(par1, 1, 9));
				par3List.add(new ItemStack(par1, 1, 10));
				par3List.add(new ItemStack(par1, 1, 11));
				par3List.add(new ItemStack(par1, 1, 12));
				par3List.add(new ItemStack(par1, 1, 13));
				par3List.add(new ItemStack(par1, 1, 14));
				break;
			case 1:
				par3List.add(new ItemStack(par1, 1, 0));
		}

	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
	{
		int metadata = world.getBlockMetadata(x, y, z) + this.pipePage * 16;
		if (metadata == 9)
		{
			if (world.getTileEntity(x, y, z) != null)
				par5EntityPlayer.openGui(ExtraUtilsMod.instance, 0, world, x, y, z);

			return true;
		}
		else if (XUHelper.isWrench(par5EntityPlayer.getCurrentEquippedItem()))
		{
			metadata = StdPipes.getNextPipeType(world, x, y, z, metadata);
			if (metadata < 16)
				world.setBlock(x, y, z, ExtraUtils.transferPipe, metadata, 3);
			else
				world.setBlock(x, y, z, ExtraUtils.transferPipe2, metadata - 16, 3);

			return true;
		}
		else
			return false;
	}

	@Override
	public BoxModel getWorldModel(IBlockAccess world, int x, int y, int z)
	{
		return this.getPipeModel(world, x, y, z, (IPipe) null);
	}

	public BoxModel getPipeModel(IBlockAccess world, int x, int y, int z, IPipe pipe_underlying)
	{
		if (pipe_underlying == null)
		{
			pipe_underlying = TNHelper.getPipe(world, x, y, z);
			if (pipe_underlying == null)
				return new BoxModel();
		}

		if (!(pipe_underlying instanceof IPipeCosmetic))
			return new BoxModel();
		else
		{
			IPipeCosmetic pipe = (IPipeCosmetic) pipe_underlying;
			BoxModel boxes = new BoxModel();

			for (int i = 0; i < 6; ++i)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				if (TNHelper.getPipe(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) != null)
				{
					if (TNHelper.doesPipeConnect(world, x, y, z, dir))
						boxes.add(new Box(0.375F, 0.0F, 0.375F, 0.625F, 0.375F, 0.625F).rotateToSide(dir).setTexture(pipe.pipeTexture(dir, !TNHelper.canInput(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.getOpposite()))).setLabel("pipe"));
    
				else if (y + dir.offsetY >= 0 && y + dir.offsetY < 256)
				{
					TileEntity tile = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
					if (tile != null && pipe_underlying.shouldConnectToTile(world, x, y, z, dir))
					{
						boxes.add(new Box(0.375F, 0.1875F, 0.375F, 0.625F, 0.375F, 0.625F).rotateToSide(dir).setTexture(pipe.invPipeTexture(dir)).setLabel("pipe"));
						boxes.add(new Box(0.3125F, 0.0F, 0.3125F, 0.6875F, 0.1875F, 0.6875F).rotateToSide(dir).setTexture(pipe.socketTexture(dir)).setLabel("nozzle"));
					}
				}
			}

			boxes.add(new Box(0.5F - pipe.baseSize(), 0.5F - pipe.baseSize(), 0.5F - pipe.baseSize(), 0.5F + pipe.baseSize(), 0.5F + pipe.baseSize(), 0.5F + pipe.baseSize()).setTexture(pipe.baseTexture()).setLabel("base"));
			return boxes;
		}
	}

	@Override
	public BoxModel getInventoryModel(int metadata)
	{
		BoxModel boxes = new BoxModel();
		IPipe pipe = this.getPipe(metadata);
		if (pipe != null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				boxes.add(new Box(0.375F, 0.0F, 0.375F, 0.625F, 0.375F, 0.625F).rotateToSide(dir).setTexture(((IPipeCosmetic) pipe).invPipeTexture(dir)).setLabel("pipe"));

			if (((IPipeCosmetic) this.getPipe(metadata)).baseSize() > 0.125F)
				boxes.add(new Box(0.5F - ((IPipeCosmetic) pipe).baseSize(), 0.5F - ((IPipeCosmetic) pipe).baseSize(), 0.5F - ((IPipeCosmetic) pipe).baseSize(), 0.5F + ((IPipeCosmetic) pipe).baseSize(), 0.5F + ((IPipeCosmetic) pipe).baseSize(), 0.5F + ((IPipeCosmetic) pipe).baseSize()).setTexture(((IPipeCosmetic) this.getPipe(metadata)).baseTexture()).setLabel("base"));
		}

		return boxes;
	}

	@Override
	public void prepareForRender(String label)
	{
	}

	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, Block par5, int par6)
	{
		TileEntity tile = par1World.getTileEntity(par2, par3, par4);
		if (par1World.getBlock(par2, par3, par4) != FMPBase.getFMPBlockId() && tile instanceof TileEntityFilterPipe)
		{
			TileEntityFilterPipe tileentity = (TileEntityFilterPipe) tile;
			if (tileentity.items != null)
			{
				for (int i = 0; i < 6; ++i)
				{
					ItemStack itemstack = tileentity.items.getStackInSlot(i);
					if (itemstack != null)
					{
						float f = this.random.nextFloat() * 0.8F + 0.1F;
						float f1 = this.random.nextFloat() * 0.8F + 0.1F;

						EntityItem entityitem;
						for (float f2 = this.random.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; par1World.spawnEntityInWorld(entityitem))
						{
							int k1 = this.random.nextInt(21) + 10;
							if (k1 > itemstack.stackSize)
								k1 = itemstack.stackSize;

							itemstack.stackSize -= k1;
							entityitem = new EntityItem(par1World, par2 + f, par3 + f1, par4 + f2, new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));
							float f3 = 0.05F;
							entityitem.motionX = (float) this.random.nextGaussian() * f3;
							entityitem.motionY = (float) this.random.nextGaussian() * f3 + 0.2F;
							entityitem.motionZ = (float) this.random.nextGaussian() * f3;
							if (itemstack.hasTagCompound())
								entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
						}
					}
				}

				par1World.func_147453_f(par2, par3, par4, par5);
			}
		}

		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return metadata == 9;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityFilterPipe();
	}

	@Override
	public String getSortingName(ItemStack par1ItemStack)
	{
		ItemStack i2 = par1ItemStack.copy();
		i2.setItemDamage(0);
		return i2.getDisplayName() + "_" + par1ItemStack.getDisplayName();
	}

	@Override
	public IPipe getPipe(int metadata)
	{
		return StdPipes.getPipeType(metadata + this.pipePage * 16);
	}
}
