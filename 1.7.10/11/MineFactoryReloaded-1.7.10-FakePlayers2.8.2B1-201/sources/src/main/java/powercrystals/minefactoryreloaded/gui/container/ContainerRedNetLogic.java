package powercrystals.minefactoryreloaded.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import powercrystals.minefactoryreloaded.tile.rednet.TileEntityRedNetLogic;

public class ContainerRedNetLogic extends Container
{
    
	public TileEntityRedNetLogic getTileEntity()
	{
		return this.logic;
    

	public ContainerRedNetLogic(TileEntityRedNetLogic tile)
	{
		this.logic = tile;
		++tile.crafters;
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed(player);
		--this.logic.crafters;
	}

	@Override
	public void putStackInSlot(int var1, ItemStack var2)
	{
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1)
	{
		return true;
	}
}
