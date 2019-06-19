package powercrystals.minefactoryreloaded.tile.machine;

import cofh.lib.util.position.BlockPosition;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiLiquidRouter;
import powercrystals.minefactoryreloaded.gui.container.ContainerLiquidRouter;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryInventory;

public class TileEntityLiquidRouter extends TileEntityFactoryInventory implements IFluidHandler
{
	private static final ForgeDirection[] _outputDirections = { ForgeDirection.DOWN, ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST };
	protected boolean[] _filledDirection = new boolean[6];

	public TileEntityLiquidRouter()
	{
		super(Machine.LiquidRouter);

		for (int var1 = 0; var1 < 6; ++var1)
		{
			this._filledDirection[var1] = false;
		}

		this.setManageFluids(true);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		for (int var1 = 0; var1 < 6; ++var1)
		{
			this._filledDirection[var1] = false;
		}

	}

	private int pumpLiquid(FluidStack resource, boolean doFill)
	{
		if (resource != null && resource.amount > 0)
		{
			int amount = resource.amount;
			int[] var4 = this.getRoutesForLiquid(resource);
			int[] var5 = this.getDefaultRoutes();
			if (this.hasRoutes(var4))
				amount = this.weightedRouteLiquid(resource, var4, amount, doFill);
			else if (this.hasRoutes(var5))
				amount = this.weightedRouteLiquid(resource, var5, amount, doFill);

			return resource.amount - amount;
		}
		else
			return 0;
	}

	private int weightedRouteLiquid(FluidStack resource, int[] var2, int amount, boolean doFill)
	{
		if (amount >= this.totalWeight(var2))
		{
			int prevAmount = amount;

			for (int side = 0; side < var2.length; ++side)
			{
				TileEntity tile = BlockPosition.getAdjacentTileEntity(this, _outputDirections[side]);
				int newAmount = prevAmount * var2[side] / this.totalWeight(var2);
				if (tile instanceof IFluidHandler && newAmount > 0)
				{
					amount -= ((IFluidHandler) tile).fill(_outputDirections[side].getOpposite(), new FluidStack(resource, newAmount), doFill);
					if (amount <= 0)
						break;
				}
			}
		}

		if (0 < amount && amount < this.totalWeight(var2))
		{
			int var9 = this.weightedRandomSide(var2);
			TileEntity tile = BlockPosition.getAdjacentTileEntity(this, _outputDirections[var9]);
			if (tile instanceof IFluidHandler)
				amount -= ((IFluidHandler) tile).fill(_outputDirections[var9].getOpposite(), new FluidStack(resource, amount), doFill);
		}

		return amount;
	}

	private int weightedRandomSide(int[] var1)
	{
		int var2 = this.worldObj.rand.nextInt(this.totalWeight(var1));

		for (int var3 = 0; var3 < var1.length; ++var3)
		{
			var2 -= var1[var3];
			if (var2 < 0)
				return var3;
		}

		return -1;
	}

	private int totalWeight(int[] var1)
	{
		int var2 = 0;

		for (int var6 : var1)
		{
			var2 += var6;
		}

		return var2;
	}

	private boolean hasRoutes(int[] var1)
	{
		for (int var5 : var1)
		{
			if (var5 > 0)
				return true;
		}

		return false;
	}

	private int[] getRoutesForLiquid(FluidStack var1)
	{
		int[] var2 = new int[6];

		for (int var3 = 0; var3 < 6; ++var3)
		{
			ItemStack var4 = this._inventory[var3];
			Item var5 = var4 != null ? var4.getItem() : null;
			if ((var5 == null || !var1.isFluidEqual(FluidContainerRegistry.getFluidForFilledItem(this._inventory[var3]))) && (!(var5 instanceof IFluidContainerItem) || !var1.isFluidEqual(((IFluidContainerItem) var5).getFluid(var4))))
				var2[var3] = 0;
			else
				var2[var3] = this._inventory[var3].stackSize;
		}

		return var2;
	}

	private int[] getDefaultRoutes()
	{
		int[] var1 = new int[6];

		for (int var2 = 0; var2 < 6; ++var2)
		{
			if (FluidContainerRegistry.isEmptyContainer(this._inventory[var2]))
				var1[var2] = this._inventory[var2].stackSize;
			else
				var1[var2] = 0;
		}

		return var1;
	}

	@Override
	public void writePortableData(EntityPlayer var1, NBTTagCompound var2)
	{
	}

	@Override
	public void readPortableData(EntityPlayer var1, NBTTagCompound var2)
	{
    
    

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		int dir = from.ordinal();
		if (dir < this._filledDirection.length && !this._filledDirection[dir])
    
			if (fillCounter >= 100)
				return 0;
			fillCounter++;

			int amount;
			try
			{
				amount = this.pumpLiquid(resource, doFill);
			}
			finally
			{
				fillCounter--;
    

			this._filledDirection[dir] = doFill & amount > 0;
			return amount;
		}
		else
			return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection var1, int var2, boolean var3)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return null;
	}

	@Override
	public int getSizeInventory()
	{
		return 6;
	}

	@Override
	public boolean shouldDropSlotWhenBroken(int var1)
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiLiquidRouter(this.getContainer(var1), this);
	}

	@Override
	public ContainerLiquidRouter getContainer(InventoryPlayer var1)
	{
		return new ContainerLiquidRouter(this, var1);
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3)
	{
		return false;
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3)
	{
		return false;
	}

	@Override
	public boolean canFill(ForgeDirection var1, Fluid var2)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection var1, Fluid var2)
	{
		return false;
	}
}
