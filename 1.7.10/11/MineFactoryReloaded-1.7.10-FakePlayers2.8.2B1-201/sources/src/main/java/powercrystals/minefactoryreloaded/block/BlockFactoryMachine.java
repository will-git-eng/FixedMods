package powercrystals.minefactoryreloaded.block;

import cofh.lib.util.position.IRotateableTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.api.rednet.IRedNetOmniNode;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.RedNetConnectionType;
import powercrystals.minefactoryreloaded.setup.MFRThings;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityBase;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactory;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryInventory;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityLaserDrill;

import java.util.ArrayList;

public class BlockFactoryMachine extends BlockFactory implements IRedNetOmniNode, ITileEntityProvider
{
	private int _mfrMachineBlockIndex;
	private static int[] itemRotation = { 0, 1, 3, 2, 5, 4 };

	public BlockFactoryMachine(int var1)
	{
		super(1.5F);
		this.setBlockName("mfr.machine." + var1);
		this._mfrMachineBlockIndex = var1;
		this.providesPower = true;
		this.setHarvestLevel("wrench", 0);
	}

	public int getBlockIndex()
	{
		return this._mfrMachineBlockIndex;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister var1)
	{
		Machine.LoadTextures(this._mfrMachineBlockIndex, var1);
	}

	@Override
	public IIcon getIcon(IBlockAccess var1, int var2, int var3, int var4, int var5)
	{
		int var6 = var1.getBlockMetadata(var2, var3, var4);
		boolean var7 = false;
		TileEntity var8 = var1.getTileEntity(var2, var3, var4);
		if (var8 instanceof TileEntityFactory)
		{
			var5 = ((TileEntityFactory) var8).getRotatedSide(var5);
			var7 = ((TileEntityFactory) var8).isActive();
		}

		return Machine.getMachineFromIndex(this._mfrMachineBlockIndex, var6).getIcon(var5, var7);
	}

	@Override
	public IIcon getIcon(int var1, int var2)
	{
		var1 = itemRotation[var1];
		return Machine.getMachineFromIndex(this._mfrMachineBlockIndex, var2).getIcon(var1, false);
	}

	@Override
	public int getLightOpacity(IBlockAccess var1, int var2, int var3, int var4)
	{
		return var1.getTileEntity(var2, var3, var4) instanceof TileEntityLaserDrill ? 0 : super.getLightOpacity(var1, var2, var3, var4);
	}

	@Override
	public void onNeighborChange(IBlockAccess var1, int var2, int var3, int var4, int var5, int var6, int var7)
	{
		TileEntity var8 = var1.getTileEntity(var2, var3, var4);
		if (var8 instanceof TileEntityFactory)
			((TileEntityFactory) var8).onNeighborTileChange(var5, var6, var7);

	}

	private void dropContents(TileEntity var1, ArrayList<ItemStack> var2)
	{
		if (var1 instanceof IInventory)
		{
			World var3 = var1.getWorldObj();
			IInventory var4 = (IInventory) var1;
			TileEntityFactoryInventory var5 = null;
			if (var1 instanceof TileEntityFactoryInventory)
				var5 = (TileEntityFactoryInventory) var1;

			int var6 = var4.getSizeInventory();

			while (var6-- > 0)
			{
				if (var5 == null || var5.shouldDropSlotWhenBroken(var6))
				{
					ItemStack var7 = var4.getStackInSlot(var6);
					if (var7 != null)
					{
						var4.setInventorySlotContents(var6, null);
						if (var2 != null)
							var2.add(var7);
						else
							this.dropStack(var3, var1.xCoord, var1.yCoord, var1.zCoord, var7);
					}
				}
			}
		}

	}

	private void dropStack(World var1, int var2, int var3, int var4, ItemStack var5)
	{
		while (var5.stackSize > 0)
		{
			float var6 = var1.rand.nextFloat() * 0.8F + 0.1F;
			float var7 = var1.rand.nextFloat() * 0.8F + 0.1F;
			float var8 = var1.rand.nextFloat() * 0.8F + 0.1F;
			int var9 = Math.min(var1.rand.nextInt(21) + 10, var5.stackSize);
			EntityItem var10 = new EntityItem(var1, var2 + var6, var3 + var7, var4 + var8, var5.splitStack(var9));
			float var11 = 0.05F;
			var10.motionX = (float) var1.rand.nextGaussian() * var11;
			var10.motionY = (float) var1.rand.nextGaussian() * var11 + 0.2F;
			var10.motionZ = (float) var1.rand.nextGaussian() * var11;
			var1.spawnEntityInWorld(var10);
		}

	}

	@Override
	public void breakBlock(World var1, int var2, int var3, int var4, Block var5, int var6)
	{
		TileEntity var7 = getTile(var1, var2, var3, var4);
		if (var7 != null)
		{
			this.dropContents(var7, null);
			if (var7 instanceof TileEntityFactoryInventory)
				((TileEntityFactoryInventory) var7).onBlockBroken();
		}

		super.breakBlock(var1, var2, var3, var4, var5, var6);
	}

	@Override
	public ArrayList<ItemStack> dismantleBlock(EntityPlayer var1, World var2, int var3, int var4, int var5, boolean var6)
	{
		ArrayList<ItemStack> var7 = new ArrayList(1);
		ItemStack var8 = new ItemStack(this.getItemDropped(var2.getBlockMetadata(var3, var4, var5), var2.rand, 0), 1, this.damageDropped(var2.getBlockMetadata(var3, var4, var5)));
		var7.add(var8);
		TileEntity var9 = getTile(var2, var3, var4, var5);
		if (var9 instanceof TileEntityBase)
		{
			this.dropContents(var9, var7);
			if (var9 instanceof TileEntityFactoryInventory)
				((TileEntityFactoryInventory) var9).onDisassembled();

			NBTTagCompound var10 = new NBTTagCompound();
			((TileEntityBase) var9).writeItemNBT(var10);
			if (!var10.hasNoTags())
				var8.setTagCompound(var10);
		}

		var2.setBlockToAir(var3, var4, var5);
		if (!var6)
			for (ItemStack var11 : var7)
			{
				this.dropStack(var2, var3, var4, var5, var11);
			}

		return var7;
	}

	@Override
	public boolean canDismantle(EntityPlayer var1, World var2, int var3, int var4, int var5)
	{
		return getTile(var2, var3, var4, var5) instanceof TileEntityFactory;
	}

	@Override
	public void onBlockPlacedBy(World var1, int var2, int var3, int var4, EntityLivingBase var5, ItemStack var6)
	{
		super.onBlockPlacedBy(var1, var2, var3, var4, var5, var6);
		if (var5 != null)
		{
			TileEntity var7 = getTile(var1, var2, var3, var4);
			if (var7 instanceof IRotateableTile && ((IRotateableTile) var7).canRotate())
				switch (MathHelper.floor_double(var5.rotationYaw * 4.0F / 360.0F + 0.5D) & 3)
				{
					case 0:
						((IRotateableTile) var7).rotateDirectlyTo(3);
						break;
					case 1:
						((IRotateableTile) var7).rotateDirectlyTo(4);
						break;
					case 2:
						((IRotateableTile) var7).rotateDirectlyTo(2);
						break;
					case 3:
						((IRotateableTile) var7).rotateDirectlyTo(5);
				}

			if (var7 instanceof TileEntityFactory)
    
				if (var5 instanceof EntityPlayer)
    

				if (var5 instanceof ICommandSender && var5.addedToChunk)
					((TileEntityFactory) var7).setOwner(var5.getCommandSenderName());
				else
					((TileEntityFactory) var7).setOwner(null);
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return Machine.getMachineFromIndex(this._mfrMachineBlockIndex, var2).getNewTileEntity();
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World var1, int var2, int var3, int var4, int var5)
	{
		TileEntity var6 = getTile(var1, var2, var3, var4);
		return var6 instanceof TileEntityFactoryInventory ? ((TileEntityFactoryInventory) var6).getComparatorOutput(var5) : 0;
	}

	@Override
	public boolean activated(World var1, int var2, int var3, int var4, EntityPlayer var5, int var6)
	{
		if (super.activated(var1, var2, var3, var4, var5, var6))
			return true;
		else
		{
			TileEntity var7 = getTile(var1, var2, var3, var4);
			if (var7 == null)
				return false;
			else if (var7 instanceof TileEntityFactoryInventory && ((TileEntityFactoryInventory) var7).acceptUpgrade(var5.getHeldItem()))
			{
				if (var5.capabilities.isCreativeMode)
					++var5.getHeldItem().stackSize;

				if (var5.getHeldItem().stackSize <= 0)
					var5.setCurrentItemOrArmor(0, null);

				return true;
			}
			else if (var7 instanceof TileEntityFactory && ((TileEntityFactory) var7).getContainer(var5.inventory) != null)
			{
				if (!var1.isRemote)
					var5.openGui(MineFactoryReloadedCore.instance(), 0, var1, var2, var3, var4);

				return true;
			}
			else
				return false;
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess var1, int var2, int var3, int var4, ForgeDirection var5)
	{
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess var1, int var2, int var3, int var4, int var5)
	{
		TileEntity var6 = var1.getTileEntity(var2, var3, var4);
		return var6 instanceof TileEntityFactory ? ((TileEntityFactory) var6).getRedNetOutput(ForgeDirection.getOrientation(var5)) : 0;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess var1, int var2, int var3, int var4, int var5)
	{
		return this.isProvidingWeakPower(var1, var2, var3, var4, var5);
	}

	@Override
	public RedNetConnectionType getConnectionType(World var1, int var2, int var3, int var4, ForgeDirection var5)
	{
		return RedNetConnectionType.DecorativeSingle;
	}

	@Override
	public int[] getOutputValues(World var1, int var2, int var3, int var4, ForgeDirection var5)
	{
		return null;
	}

	@Override
	public void onInputsChanged(World var1, int var2, int var3, int var4, ForgeDirection var5, int[] var6)
	{
	}

	@Override
	public int getOutputValue(World var1, int var2, int var3, int var4, ForgeDirection var5, int var6)
	{
		TileEntity var7 = getTile(var1, var2, var3, var4);
		return var7 instanceof TileEntityFactory ? ((TileEntityFactory) var7).getRedNetOutput(var5) : 0;
	}

	@Override
	public void onInputChanged(World var1, int var2, int var3, int var4, ForgeDirection var5, int var6)
	{
		TileEntity var7 = getTile(var1, var2, var3, var4);
		if (var7 instanceof TileEntityFactory)
		{
			((TileEntityFactory) var7).onRedNetChanged(var5, var6);
			this.onNeighborBlockChange(var1, var2, var3, var4, MFRThings.rednetCableBlock);
		}

	}
}
