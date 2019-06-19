package team.chisel.common.inventory;

import ru.will.git.chisel.EventConfig;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotChiselInput extends Slot
{
	private final ContainerChisel container;

	public SlotChiselInput(ContainerChisel container, @Nonnull InventoryChiselSelection inv, int i, int j, int k)
	{
		super(inv, i, j, k);
		this.container = container;
	}

	    
	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return super.isItemValid(stack) && !EventConfig.chiselBlackList.contains(stack);
	}
	    

	@Override
	public void onSlotChanged()
	{
		super.onSlotChanged();
		this.container.onChiselSlotChanged();
		this.container.getInventoryChisel().updateItems();
	}
}
