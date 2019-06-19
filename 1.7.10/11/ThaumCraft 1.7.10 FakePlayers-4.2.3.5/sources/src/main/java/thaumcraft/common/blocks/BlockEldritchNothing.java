package thaumcraft.common.blocks;

import ru.will.git.thaumcraft.EventConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.tiles.TileEldritchNothing;

import java.util.Random;

public class BlockEldritchNothing extends Block
{
    
    

	public BlockEldritchNothing()
	{
		super(Material.rock);
		this.setBlockUnbreakable();
		this.setResistance(6000000.0F);
		this.setStepSound(Block.soundTypeCloth);
		this.setLightLevel(0.2F);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		this.setTickRandomly(true);
    
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		super.updateTick(world, x, y, z, rand);
		if (!EventConfig.blockEldritchNothing && world.getBlock(x, y, z) == ConfigBlocks.blockEldritchNothing)
			world.setBlockToAir(x, y, z);
    

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		this.icon = ir.registerIcon("thaumcraft:blank");
	}

	@Override
	public IIcon getIcon(int i, int m)
	{
		return this.icon;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World w, int i, int j, int k)
	{
		return AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return metadata == 1;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return metadata == 1 ? new TileEldritchNothing() : null;
	}

	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3)
	{
		return Item.getItemById(0);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    
		if (this.alreadyNotify)
		{
			super.onNeighborBlockChange(world, x, y, z, block);
			return;
		}
    

		if (BlockUtils.isBlockExposed(world, x, y, z))
			world.setBlockMetadataWithNotify(x, y, z, 1, 3);
		else
    
    

		super.onNeighborBlockChange(world, x, y, z, block);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if (entity.ticksExisted > 20 && (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode))
			entity.attackEntityFrom(DamageSource.outOfWorld, 8.0F);

	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		float f = 0.125F;
		return AxisAlignedBB.getBoundingBox(x + f, (double) y + (double) f, z + f, x + 1 - f, y + 1 - f, z + 1 - f);
	}
}
