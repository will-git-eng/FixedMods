package thaumcraft.common.container;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.crafting.IArcaneWorkbench;
import thaumcraft.common.tiles.crafting.TileArcaneWorkbench;

public class InventoryArcaneWorkbench extends InventoryCrafting implements IArcaneWorkbench
{
	TileEntity workbench;

	public InventoryArcaneWorkbench(TileEntity tileEntity, Container container)
	{
		super(container, 5, 3);
		this.workbench = tileEntity;
	}

	
	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		ItemStack stack = super.decrStackSize(index, count);
		if (!stack.isEmpty())
			this.notifyOtherContainers();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		super.setInventorySlotContents(index, stack);
		this.notifyOtherContainers();
	}

	private void notifyOtherContainers()
	{
		if (this.workbench instanceof TileArcaneWorkbench)
			for (Container container : ((TileArcaneWorkbench) this.workbench).getContainers().values())
			{
				if (container != this.eventHandler)
					container.onCraftMatrixChanged(this);
			}
	}
	

	@Override
	public String getName()
	{
		return "container.arcaneworkbench";
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		this.workbench.markDirty();
	}
}
