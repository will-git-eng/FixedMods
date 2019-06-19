package powercrystals.minefactoryreloaded.gui.container;

import cofh.core.util.CoreUtils;
import cofh.lib.gui.container.ContainerBase;
import cofh.lib.gui.slot.SlotAcceptValid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryInventory;

public class ContainerFactoryInventory extends ContainerBase
{
	protected TileEntityFactoryInventory _te;
	private int _tankAmount;
	private int _tankIndex;
	public boolean drops;
    
	public TileEntityFactoryInventory getTileEntity()
	{
		return this._te;
    

	public ContainerFactoryInventory(TileEntityFactoryInventory tile, InventoryPlayer inventoryPlayer)
	{
		this._te = tile;
		if (this._te.getSizeInventory() > 0)
			this.addSlots();

		this.bindPlayerInventory(inventoryPlayer);
	}

	protected void addSlots()
	{
		this.addSlotToContainer(new SlotAcceptValid(this._te, 0, 8, 15));
		this.addSlotToContainer(new SlotAcceptValid(this._te, 1, 26, 15));
		this.addSlotToContainer(new SlotAcceptValid(this._te, 2, 44, 15));
		this.addSlotToContainer(new SlotAcceptValid(this._te, 3, 8, 33));
		this.addSlotToContainer(new SlotAcceptValid(this._te, 4, 26, 33));
		this.addSlotToContainer(new SlotAcceptValid(this._te, 5, 44, 33));
		this.addSlotToContainer(new SlotAcceptValid(this._te, 6, 8, 51));
		this.addSlotToContainer(new SlotAcceptValid(this._te, 7, 26, 51));
		this.addSlotToContainer(new SlotAcceptValid(this._te, 8, 44, 51));
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		FluidTankInfo[] var1 = this._te.getTankInfo(ForgeDirection.UNKNOWN);
		int var2 = var1.length;

		for (int var3 = 0; var3 < this.crafters.size(); ++var3)
		{
			((ICrafting) this.crafters.get(var3)).sendProgressBarUpdate(this, 33, (this._te.hasDrops() ? 1 : 0) | (CoreUtils.isRedstonePowered(this._te) ? 2 : 0));
			int var4 = var2;

			while (var4-- > 0)
			{
				((ICrafting) this.crafters.get(var3)).sendProgressBarUpdate(this, 30, var4);
				if (var1[var4] != null && var1[var4].fluid != null)
				{
					((ICrafting) this.crafters.get(var3)).sendProgressBarUpdate(this, 31, var1[var4].fluid.amount);
					((ICrafting) this.crafters.get(var3)).sendProgressBarUpdate(this, 32, var1[var4].fluid.getFluid().getID());
				}
				else if (var1[var4] != null)
				{
					((ICrafting) this.crafters.get(var3)).sendProgressBarUpdate(this, 31, 0);
					((ICrafting) this.crafters.get(var3)).sendProgressBarUpdate(this, 32, 0);
				}
			}
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int var1, int var2)
	{
		super.updateProgressBar(var1, var2);
		if (var1 == 30)
			this._tankIndex = var2;
		else if (var1 == 31)
			this._tankAmount = var2;
		else if (var1 == 32)
		{
			Fluid var3 = FluidRegistry.getFluid(var2);
			if (var3 == null)
				this._te.getTanks()[this._tankIndex].setFluid(null);
			else
				this._te.getTanks()[this._tankIndex].setFluid(new FluidStack(var3, this._tankAmount));
		}
		else if (var1 == 33)
		{
			this.drops = (var2 & 1) != 0;
			this.redstone = (var2 & 2) != 0;
		}

	}

	@Override
	public boolean canInteractWith(EntityPlayer var1)
	{
		return !this._te.isInvalid() && this._te.isUseableByPlayer(var1);
	}

	@Override
	protected int getSizeInventory()
	{
		return this._te.getSizeInventory();
	}

	@Override
	protected int getPlayerInventoryVerticalOffset()
	{
		return 84;
	}
}
