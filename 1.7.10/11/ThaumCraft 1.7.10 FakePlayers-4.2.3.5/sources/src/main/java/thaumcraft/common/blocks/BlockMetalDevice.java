package thaumcraft.common.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntitySpecialItem;
import thaumcraft.common.items.ItemShard;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.*;

import java.util.List;
import java.util.Random;

public class BlockMetalDevice extends BlockContainer
{
	public IIcon[] icon = new IIcon[23];
	public IIcon iconGlow;
	private int delay = 0;

	public BlockMetalDevice()
	{
		super(Material.iron);
		this.setHardness(3.0F);
		this.setResistance(17.0F);
		this.setStepSound(Block.soundTypeMetal);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		this.icon[0] = ir.registerIcon("thaumcraft:metalbase");

		for (int a = 1; a <= 6; ++a)
		{
			this.icon[a] = ir.registerIcon("thaumcraft:crucible" + a);
		}

		this.icon[7] = ir.registerIcon("thaumcraft:goldbase");
		this.icon[8] = ir.registerIcon("thaumcraft:grate");
		this.icon[9] = ir.registerIcon("thaumcraft:grate_hatch");
		this.icon[10] = ir.registerIcon("thaumcraft:lamp_side");
		this.icon[11] = ir.registerIcon("thaumcraft:lamp_top");
		this.icon[12] = ir.registerIcon("thaumcraft:lamp_grow_side");
		this.icon[13] = ir.registerIcon("thaumcraft:lamp_grow_top");
		this.icon[14] = ir.registerIcon("thaumcraft:lamp_grow_side_off");
		this.icon[15] = ir.registerIcon("thaumcraft:lamp_grow_top_off");
		this.icon[16] = ir.registerIcon("thaumcraft:alchemyblock");
		this.icon[17] = ir.registerIcon("thaumcraft:brainbox");
		this.icon[18] = ir.registerIcon("thaumcraft:lamp_fert_side");
		this.icon[19] = ir.registerIcon("thaumcraft:lamp_fert_top");
		this.icon[20] = ir.registerIcon("thaumcraft:lamp_fert_side_off");
		this.icon[21] = ir.registerIcon("thaumcraft:lamp_fert_top_off");
		this.icon[22] = ir.registerIcon("thaumcraft:alchemyblockadv");
		this.iconGlow = ir.registerIcon("thaumcraft:animatedglow");
	}

	@Override
	public IIcon getIcon(int i, int md)
	{
		return md == 3 ? this.icon[22] : md == 7 ? this.icon[10] : md == 8 ? this.icon[12] : md != 10 && md != 9 && md != 11 ? md == 12 ? this.icon[17] : md == 13 ? this.icon[18] : md != 14 && md != 2 ? md != 0 && md != 1 && md != 5 && md != 6 ? this.icon[7] : this.icon[0] : this.icon[0] : this.icon[16];
	}

	@Override
	public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int side)
	{
		int metadata = iblockaccess.getBlockMetadata(i, j, k);
		if (metadata != 5 && metadata != 6)
		{
			if (metadata == 7)
				return side <= 1 ? this.icon[11] : this.icon[10];
			else
			{
				if (metadata == 8)
				{
					TileEntity te = iblockaccess.getTileEntity(i, j, k);
					if (te instanceof TileArcaneLampGrowth)
					{
						if (((TileArcaneLampGrowth) te).charges > 0)
						{
							if (side <= 1)
								return this.icon[13];

							return this.icon[12];
						}

						if (side <= 1)
							return this.icon[15];

						return this.icon[14];
					}
				}
				else if (metadata == 13)
				{
					TileEntity te = iblockaccess.getTileEntity(i, j, k);
					if (te instanceof TileArcaneLampFertility)
					{
						if (((TileArcaneLampFertility) te).charges > 0)
						{
							if (side <= 1)
								return this.icon[19];

							return this.icon[18];
						}

						if (side <= 1)
							return this.icon[21];

						return this.icon[20];
					}
				}
				else
				{
					if (metadata == 10 || metadata == 9 || metadata == 11)
						return this.icon[16];

					if (metadata == 12)
						return this.icon[17];

					if (metadata == 3)
						return this.icon[22];
				}

				if (side == 1)
					return this.icon[1];
				else if (side == 0)
					return this.icon[2];
				else
					return this.icon[3];
			}
		}
		else
			return this.icon[8];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
		par3List.add(new ItemStack(par1, 1, 5));
		par3List.add(new ItemStack(par1, 1, 7));
		par3List.add(new ItemStack(par1, 1, 8));
		par3List.add(new ItemStack(par1, 1, 13));
		par3List.add(new ItemStack(par1, 1, 9));
		par3List.add(new ItemStack(par1, 1, 3));
		par3List.add(new ItemStack(par1, 1, 12));
		par3List.add(new ItemStack(par1, 1, 14));
		par3List.add(new ItemStack(par1, 1, 2));
	}

	@Override
	public int getRenderType()
	{
		return ConfigBlocks.blockMetalDeviceRI;
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
	public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity)
	{
		if (!world.isRemote)
		{
			int metadata = world.getBlockMetadata(i, j, k);
			if (metadata == 0)
			{
				TileCrucible tile = (TileCrucible) world.getTileEntity(i, j, k);
				if (tile != null && entity instanceof EntityItem && !(entity instanceof EntitySpecialItem) && tile.heat > 150 && tile.tank.getFluidAmount() > 0)
					tile.attemptSmelt((EntityItem) entity);
				else
				{
					++this.delay;
					if (this.delay < 10)
						return;

					this.delay = 0;
					if (entity instanceof EntityLivingBase && tile != null && tile.heat > 150 && tile.tank.getFluidAmount() > 0)
					{
						entity.attackEntityFrom(DamageSource.inFire, 1.0F);
						world.playSoundEffect(i, j, k, "random.fizz", 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
					}
				}
			}
		}

	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int i, int j, int k)
	{
		int metadata = world.getBlockMetadata(i, j, k);
		if (metadata != 5 && metadata != 6)
		{
			if (metadata != 7 && metadata != 8 && metadata != 13)
			{
				if (metadata == 10)
					this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
				else if (metadata == 11)
					this.setBlockBounds(0.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				else if (metadata == 12)
					this.setBlockBounds(BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W13, BlockRenderer.W13, BlockRenderer.W13);
				else if (metadata == 2)
					this.setBlockBounds(BlockRenderer.W5, 0.5F, BlockRenderer.W5, BlockRenderer.W11, 1.0F, BlockRenderer.W11);
				else if (metadata == 14)
				{
					TileEntity te = world.getTileEntity(i, j, k);
					if (te instanceof TileVisRelay)
						switch (ForgeDirection.getOrientation(((TileVisRelay) te).orientation).getOpposite())
						{
							case UP:
								this.setBlockBounds(BlockRenderer.W5, 0.5F, BlockRenderer.W5, BlockRenderer.W11, 1.0F, BlockRenderer.W11);
								break;
							case DOWN:
								this.setBlockBounds(BlockRenderer.W5, 0.0F, BlockRenderer.W5, BlockRenderer.W11, 0.5F, BlockRenderer.W11);
								break;
							case EAST:
								this.setBlockBounds(0.5F, BlockRenderer.W5, BlockRenderer.W5, 1.0F, BlockRenderer.W11, BlockRenderer.W11);
								break;
							case WEST:
								this.setBlockBounds(0.0F, BlockRenderer.W5, BlockRenderer.W5, 0.5F, BlockRenderer.W11, BlockRenderer.W11);
								break;
							case SOUTH:
								this.setBlockBounds(BlockRenderer.W5, BlockRenderer.W5, 0.5F, BlockRenderer.W11, BlockRenderer.W11, 1.0F);
								break;
							case NORTH:
								this.setBlockBounds(BlockRenderer.W5, BlockRenderer.W5, 0.0F, BlockRenderer.W11, BlockRenderer.W11, 0.5F);
						}
				}
				else
					this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			}
			else
				this.setBlockBounds(BlockRenderer.W4, BlockRenderer.W2, BlockRenderer.W4, BlockRenderer.W12, BlockRenderer.W14, BlockRenderer.W12);
		}
		else
			this.setBlockBounds(0.0F, 0.8125F, 0.0F, 1.0F, 1.0F, 1.0F);

		super.setBlockBoundsBasedOnState(world, i, j, k);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity)
	{
		int metadata = world.getBlockMetadata(i, j, k);
		if (metadata == 0)
		{
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3125F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			float f = 0.125F;
			this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 0.85F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.85F, f);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			this.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 0.85F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			this.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 0.85F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}
		else if (metadata == 2)
		{
			this.setBlockBounds(BlockRenderer.W5, 0.5F, BlockRenderer.W5, BlockRenderer.W11, 1.0F, BlockRenderer.W11);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}
		else if (metadata == 5)
		{
			if (par7Entity != null && !(par7Entity instanceof EntityItem))
			{
				this.setBlockBounds(0.0F, 0.8125F, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}
		}
		else if (metadata == 6)
		{
			this.setBlockBounds(0.0F, 0.8125F, 0.0F, 1.0F, 1.0F, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}
		else if (metadata != 7 && metadata != 8 && metadata != 13)
		{
			if (metadata == 12)
			{
				this.setBlockBounds(BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W13, BlockRenderer.W13, BlockRenderer.W13);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}
			else if (metadata == 14)
			{
				TileEntity te = world.getTileEntity(i, j, k);
				if (te instanceof TileVisRelay)
				{
					switch (ForgeDirection.getOrientation(((TileVisRelay) te).orientation).getOpposite())
					{
						case UP:
							this.setBlockBounds(BlockRenderer.W5, 0.5F, BlockRenderer.W5, BlockRenderer.W11, 1.0F, BlockRenderer.W11);
							break;
						case DOWN:
							this.setBlockBounds(BlockRenderer.W5, 0.0F, BlockRenderer.W5, BlockRenderer.W11, 0.5F, BlockRenderer.W11);
							break;
						case EAST:
							this.setBlockBounds(0.5F, BlockRenderer.W5, BlockRenderer.W5, 1.0F, BlockRenderer.W11, BlockRenderer.W11);
							break;
						case WEST:
							this.setBlockBounds(0.0F, BlockRenderer.W5, BlockRenderer.W5, 0.5F, BlockRenderer.W11, BlockRenderer.W11);
							break;
						case SOUTH:
							this.setBlockBounds(BlockRenderer.W5, BlockRenderer.W5, 0.5F, BlockRenderer.W11, BlockRenderer.W11, 1.0F);
							break;
						case NORTH:
							this.setBlockBounds(BlockRenderer.W5, BlockRenderer.W5, 0.0F, BlockRenderer.W11, BlockRenderer.W11, 0.5F);
					}

					super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
				}
			}
			else
			{
				this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}
		}
		else
		{
			this.setBlockBounds(BlockRenderer.W4, BlockRenderer.W2, BlockRenderer.W4, BlockRenderer.W12, BlockRenderer.W14, BlockRenderer.W12);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int i, int j, int k, Random r)
	{
		if (r.nextInt(10) == 0)
		{
			TileEntity te = w.getTileEntity(i, j, k);
			if (te instanceof TileCrucible && ((TileCrucible) te).tank.getFluidAmount() > 0 && ((TileCrucible) te).heat > 150)
				w.playSound(i, j, k, "liquid.lavapop", 0.1F + r.nextFloat() * 0.1F, 1.2F + r.nextFloat() * 0.2F, false);
		}

	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata == 6 ? 5 : metadata != 10 && metadata != 11 ? metadata : 9;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return metadata == 0 ? new TileCrucible() : metadata == 5 ? new TileGrate() : metadata == 6 ? new TileGrate() : metadata == 1 ? new TileAlembic() : metadata == 7 ? new TileArcaneLamp() : metadata == 8 ? new TileArcaneLampGrowth() : metadata == 10 ? new TileThaumatorium() : metadata == 11 ? new TileThaumatoriumTop() : metadata == 12 ? new TileBrainbox() : metadata == 13 ? new TileArcaneLampFertility() : metadata == 14 ? new TileVisRelay() : metadata == 2 ? new TileMagicWorkbenchCharger() : super.createTileEntity(world, metadata);
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int rs)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileThaumatorium)
			return Container.calcRedstoneFromInventory((IInventory) te);
		else if (te instanceof TileAlembic)
		{
			float r = (float) ((TileAlembic) te).amount / (float) ((TileAlembic) te).maxAmount;
			return MathHelper.floor_float(r * 14.0F) + (((TileAlembic) te).amount > 0 ? 1 : 0);
		}
		else if (te instanceof TileCrucible)
		{
			float var10000 = ((TileCrucible) te).aspects.visSize();
			((TileCrucible) te).getClass();
			float r = var10000 / 100.0F;
			return MathHelper.floor_float(r * 14.0F) + (((TileCrucible) te).aspects.visSize() > 0 ? 1 : 0);
		}
		else
			return 0;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int md)
	{
		return null;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block nbid)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		int md = world.getBlockMetadata(x, y, z);
		if (te instanceof TileCrucible)
			((TileCrucible) te).getBellows();

		if (!world.isRemote)
		{
			if (te instanceof TileAlembic)
				world.markBlockForUpdate(x, y, z);
			else if (te instanceof TileArcaneLamp)
			{
				TileArcaneLamp telb = (TileArcaneLamp) te;
				if (world.isAirBlock(x + telb.facing.offsetX, y + telb.facing.offsetY, z + telb.facing.offsetZ))
				{
					this.dropBlockAsItem(world, x, y, z, 7, 0);
					world.setBlockToAir(x, y, z);
				}
			}
			else if (te instanceof TileArcaneLampGrowth)
			{
				TileArcaneLampGrowth telb = (TileArcaneLampGrowth) te;
				if (world.isAirBlock(x + telb.facing.offsetX, y + telb.facing.offsetY, z + telb.facing.offsetZ))
				{
					this.dropBlockAsItem(world, x, y, z, 8, 0);
					world.setBlockToAir(x, y, z);
				}
			}
			else if (te instanceof TileBrainbox)
			{
				TileBrainbox telb = (TileBrainbox) te;
				if (world.isAirBlock(x + telb.facing.offsetX, y + telb.facing.offsetY, z + telb.facing.offsetZ))
				{
					this.dropBlockAsItem(world, x, y, z, 12, 0);
					world.setBlockToAir(x, y, z);
				}
			}
			else if (te instanceof TileVisRelay && md == 14)
			{
				TileVisRelay telb = (TileVisRelay) te;
				if (world.isAirBlock(x + ForgeDirection.getOrientation(telb.orientation).getOpposite().offsetX, y + ForgeDirection.getOrientation(telb.orientation).getOpposite().offsetY, z + ForgeDirection.getOrientation(telb.orientation).getOpposite().offsetZ))
				{
					this.dropBlockAsItem(world, x, y, z, 14, 0);
					world.setBlockToAir(x, y, z);
				}
			}
			else if (md == 10)
			{
				if (world.getBlock(x, y + 1, z) != this || world.getBlockMetadata(x, y + 1, z) != 11 || world.getBlock(x, y - 1, z) != this || world.getBlockMetadata(x, y - 1, z) != 0)
				{
					InventoryUtils.dropItems(world, x, y, z);
					world.setBlockToAir(x, y, z);
					world.setBlock(x, y, z, this, 9, 3);
					return;
				}

				TileEntity tile = world.getTileEntity(x, y, z);
				if (tile instanceof TileThaumatorium)
					((TileThaumatorium) tile).getUpgrades();
			}
			else if (md == 11)
			{
				if (world.getBlock(x, y - 1, z) != this || world.getBlockMetadata(x, y - 1, z) != 10)
				{
					world.setBlockToAir(x, y, z);
					world.setBlock(x, y, z, this, 9, 3);
					return;
				}

				TileEntity tile = world.getTileEntity(x, y - 1, z);
				if (tile instanceof TileThaumatorium)
					((TileThaumatorium) tile).getUpgrades();
			}

			boolean flag = world.isBlockIndirectlyGettingPowered(x, y, z);
			if (flag || nbid.canProvidePower())
				this.onPoweredBlockChange(world, x, y, z, flag);
		}

		super.onNeighborBlockChange(world, x, y, z, nbid);
	}

	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, Block par5, int par6)
	{
		InventoryUtils.dropItems(par1World, par2, par3, par4);
		TileEntity te = par1World.getTileEntity(par2, par3, par4);
		if (te instanceof TileCrucible)
			((TileCrucible) te).spillRemnants();
		else if (te instanceof TileAlembic && ((TileAlembic) te).aspectFilter != null)
			par1World.spawnEntityInWorld(new EntityItem(par1World, par2 + 0.5F, par3 + 0.5F, par4 + 0.5F, new ItemStack(ConfigItems.itemResource, 1, 13)));
		else if (te instanceof TileArcaneLamp)
			((TileArcaneLamp) te).removeLights();

		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		if (metadata == 0 && !world.isRemote)
		{
			FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(player.inventory.getCurrentItem());
			if (fs != null && fs.isFluidEqual(new FluidStack(FluidRegistry.WATER, 1000)))
			{
				int volume = fs.amount;
				TileEntity te = world.getTileEntity(x, y, z);
				if (te instanceof TileCrucible)
				{
					TileCrucible tile = (TileCrucible) te;
					if (tile.tank.getFluidAmount() >= tile.tank.getCapacity())
						return true;

					tile.fill(ForgeDirection.UNKNOWN, FluidContainerRegistry.getFluidForFilledItem(player.inventory.getCurrentItem()), true);

    
    

					player.inventoryContainer.detectAndSendChanges();
					te.markDirty();
					world.markBlockForUpdate(x, y, z);
					world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "game.neutral.swim", 0.33F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F);
				}
			}
		}

		if (metadata == 1 && !world.isRemote && !player.isSneaking() && player.getHeldItem() == null)
		{
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileAlembic)
			{
				TileAlembic tile = (TileAlembic) te;
				String msg = "";
				if (tile.aspect != null && tile.amount != 0)
				{
					if (tile.amount < tile.maxAmount * 0.4D)
						msg = StatCollector.translateToLocal("tile.alembic.msg.2");
					else if (tile.amount < tile.maxAmount * 0.8D)
						msg = StatCollector.translateToLocal("tile.alembic.msg.3");
					else if (tile.amount < tile.maxAmount)
						msg = StatCollector.translateToLocal("tile.alembic.msg.4");
					else if (tile.amount == tile.maxAmount)
						msg = StatCollector.translateToLocal("tile.alembic.msg.5");
				}
				else
					msg = StatCollector.translateToLocal("tile.alembic.msg.1");

				player.addChatMessage(new ChatComponentTranslation("§3" + msg));
				world.playSoundEffect(x, y, z, "thaumcraft:alembicknock", 0.2F, 1.0F);
			}
		}

		if (metadata == 1)
		{
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileAlembic)
			{
				if (player.isSneaking() && ((TileAlembic) te).aspectFilter != null)
				{
					((TileAlembic) te).aspectFilter = null;
					world.markBlockForUpdate(x, y, z);
					te.markDirty();
					if (world.isRemote)
						world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "thaumcraft:page", 1.0F, 1.1F, false);
					else
					{
						ForgeDirection fd = ForgeDirection.getOrientation(side);
						world.spawnEntityInWorld(new EntityItem(world, x + 0.5F + fd.offsetX / 3.0F, y + 0.5F, z + 0.5F + fd.offsetZ / 3.0F, new ItemStack(ConfigItems.itemResource, 1, 13)));
					}

					return true;
				}

				if (player.isSneaking() && player.getHeldItem() == null)
				{
					((TileAlembic) te).amount = 0;
					((TileAlembic) te).aspect = null;
					if (world.isRemote)
					{
						world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "thaumcraft:alembicknock", 0.2F, 1.0F, false);
						world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "game.neutral.swim", 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);
					}
				}
				else
				{
					if (player.getHeldItem() != null && ((TileAlembic) te).aspectFilter == null && player.getHeldItem().getItem() == ConfigItems.itemResource && player.getHeldItem().getItemDamage() == 13)
					{
						if (((TileAlembic) te).amount == 0 && ((IEssentiaContainerItem) player.getHeldItem().getItem()).getAspects(player.getHeldItem()) == null)
							return true;

						if (((TileAlembic) te).amount == 0 && ((IEssentiaContainerItem) player.getHeldItem().getItem()).getAspects(player.getHeldItem()) != null)
							((TileAlembic) te).aspect = ((IEssentiaContainerItem) player.getHeldItem().getItem()).getAspects(player.getHeldItem()).getAspects()[0];

						--player.getHeldItem().stackSize;
						((TileAlembic) te).aspectFilter = ((TileAlembic) te).aspect;
						world.markBlockForUpdate(x, y, z);
						te.markDirty();
						if (world.isRemote)
							world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "thaumcraft:page", 1.0F, 0.9F, false);

						return true;
					}

					if (player.getHeldItem() != null && ((TileAlembic) te).amount > 0 && (player.getHeldItem().getItem() == ConfigItems.itemJarFilled || player.getHeldItem().isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 0)) || player.getHeldItem().isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 3))))
					{
						boolean doit = false;
						ItemStack drop = null;
						if (!player.getHeldItem().isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 0)) && !player.getHeldItem().isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 3)))
						{
							drop = player.getHeldItem();
							if ((((ItemJarFilled) drop.getItem()).getAspects(drop) == null || ((ItemJarFilled) drop.getItem()).getAspects(drop).visSize() == 0 || ((ItemJarFilled) drop.getItem()).getAspects(drop).getAmount(((TileAlembic) te).aspect) > 0) && (((ItemJarFilled) drop.getItem()).getFilter(drop) == null || ((ItemJarFilled) drop.getItem()).getFilter(drop) == ((TileAlembic) te).aspect))
							{
								int amount = Math.min(((ItemJarFilled) drop.getItem()).getAspects(drop) == null ? 64 : 64 - ((ItemJarFilled) drop.getItem()).getAspects(drop).visSize(), ((TileAlembic) te).amount);
								if (drop.getItemDamage() == 3)
									amount = ((TileAlembic) te).amount;

								if (amount > 0)
								{
									((TileAlembic) te).amount -= amount;
									AspectList as = ((ItemJarFilled) drop.getItem()).getAspects(drop);
									if (as == null)
										as = new AspectList();

									as.add(((TileAlembic) te).aspect, amount);
									if (as.getAmount(((TileAlembic) te).aspect) > 64)
									{
										int q = as.getAmount(((TileAlembic) te).aspect) - 64;
										as.reduce(((TileAlembic) te).aspect, q);
									}

									((ItemJarFilled) drop.getItem()).setAspects(drop, as);
									if (((TileAlembic) te).amount <= 0)
										((TileAlembic) te).aspect = null;

									doit = true;
									player.setCurrentItemOrArmor(0, drop);
								}
							}
						}
						else
						{
							drop = new ItemStack(ConfigItems.itemJarFilled, 1, player.getHeldItem().getItemDamage());
							doit = true;
							((ItemJarFilled) drop.getItem()).setAspects(drop, new AspectList().add(((TileAlembic) te).aspect, ((TileAlembic) te).amount));
							((TileAlembic) te).amount = 0;
							((TileAlembic) te).aspect = null;
							--player.getHeldItem().stackSize;
							if (!player.inventory.addItemStackToInventory(drop) && !world.isRemote)
								world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, drop));
						}

						if (doit)
						{
							te.markDirty();
							world.markBlockForUpdate(x, y, z);
							if (world.isRemote)
								world.playSound(x + 0.5F, y + 0.5F, z + 0.5F, "game.neutral.swim", 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);
						}

						return true;
					}
				}
			}
		}

		if (metadata == 5)
		{
			world.setBlockMetadataWithNotify(x, y, z, 6, 2);
			world.playAuxSFXAtEntity(player, 1003, x, y, z, 0);
			return true;
		}
		else if (metadata == 6)
		{
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
			world.playAuxSFXAtEntity(player, 1003, x, y, z, 0);
			return true;
		}
		else if (world.isRemote)
			return true;
		else
		{
			if (metadata == 10)
			{
				TileEntity te = world.getTileEntity(x, y, z);
				if (te instanceof TileThaumatorium && !player.isSneaking())
				{
					player.openGui(Thaumcraft.instance, 3, world, x, y, z);
					return true;
				}
			}

			if (metadata == 11)
			{
				TileEntity te = world.getTileEntity(x, y - 1, z);
				if (te instanceof TileThaumatorium && !player.isSneaking())
				{
					player.openGui(Thaumcraft.instance, 3, world, x, y - 1, z);
					return true;
				}
			}

			if ((metadata == 14 || metadata == 2) && !world.isRemote && !player.isSneaking() && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemShard)
			{
				TileEntity te = world.getTileEntity(x, y, z);
				if (te instanceof TileVisRelay)
				{
					TileVisRelay tile = (TileVisRelay) te;
					byte c = (byte) player.getHeldItem().getItemDamage();
					if (c != tile.color && c != 6)
						tile.color = c;
					else
						tile.color = -1;

					tile.removeThisNode();
					tile.nodeRefresh = true;
					tile.markDirty();
					world.markBlockForUpdate(x, y, z);
					world.playSoundEffect(x, y, z, "thaumcraft:crystal", 0.2F, 1.0F);
				}
			}

			return super.onBlockActivated(world, x, y, z, player, side, par7, par8, par9);
		}
	}

	public void onPoweredBlockChange(World par1World, int par2, int par3, int par4, boolean flag)
	{
		int l = par1World.getBlockMetadata(par2, par3, par4);
		if (l == 5 && flag)
		{
			par1World.setBlockMetadataWithNotify(par2, par3, par4, 6, 2);
			par1World.playAuxSFXAtEntity(null, 1003, par2, par3, par4, 0);
		}
		else if (l == 6 && !flag)
		{
			par1World.setBlockMetadataWithNotify(par2, par3, par4, 5, 2);
			par1World.playAuxSFXAtEntity(null, 1003, par2, par3, par4, 0);
		}

	}

	@Override
	public void onBlockPlacedBy(World world, int par2, int par3, int par4, EntityLivingBase ent, ItemStack stack)
	{
		int l = MathHelper.floor_double(ent.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		if (stack.getItemDamage() == 1)
		{
			TileEntity tile = world.getTileEntity(par2, par3, par4);
			if (tile instanceof TileAlembic)
			{
				if (l == 0)
					((TileAlembic) tile).facing = 2;

				if (l == 1)
					((TileAlembic) tile).facing = 5;

				if (l == 2)
					((TileAlembic) tile).facing = 3;

				if (l == 3)
					((TileAlembic) tile).facing = 4;
			}
		}

	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		int md = world.getBlockMetadata(x, y, z);
		if (md == 3)
			return 11;
		else if (md == 7)
			return 15;
		else
		{
			if (md == 8)
			{
				TileEntity te = world.getTileEntity(x, y, z);
				if (te instanceof TileArcaneLampGrowth)
				{
					if (((TileArcaneLampGrowth) te).charges > 0)
						return 15;

					return 8;
				}
			}
			else if (md == 13)
			{
				TileEntity te = world.getTileEntity(x, y, z);
				if (te instanceof TileArcaneLampFertility)
				{
					if (((TileArcaneLampFertility) te).charges > 0)
						return 15;

					return 8;
				}
			}
			else if (md == 14)
			{
				TileEntity te = world.getTileEntity(x, y, z);
				if (te instanceof TileVisRelay)
				{
					if (VisNetHandler.isNodeValid(((TileVisRelay) te).getParent()))
						return 10;

					return 2;
				}
			}

			return super.getLightValue(world, x, y, z);
		}
	}
}
