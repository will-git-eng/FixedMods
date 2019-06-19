package com.rwtema.extrautils.gui;

import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import invtweaks.api.container.InventoryContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

@InventoryContainer
public class ContainerFilterPipe extends Container
    
    

	public ContainerFilterPipe(IInventory player, IInventory pipe)
    
    

		this.addSlotToContainer(new Slot(pipe, 0, 80, 90));
		this.addSlotToContainer(new Slot(pipe, 1, 80, 15));
		this.addSlotToContainer(new Slot(pipe, 2, 43, 33));
		this.addSlotToContainer(new Slot(pipe, 3, 117, 72));
		this.addSlotToContainer(new Slot(pipe, 4, 43, 72));
		this.addSlotToContainer(new Slot(pipe, 5, 117, 33));

		for (int iy = 0; iy < 3; ++iy)
		{
			for (int ix = 0; ix < 9; ++ix)
			{
				this.addSlotToContainer(new Slot(player, ix + iy * 9 + 9, 8 + ix * 18, 111 + iy * 18));
			}
		}

		for (int ix = 0; ix < 9; ++ix)
		{
			this.addSlotToContainer(new Slot(player, ix, 8 + ix * 18, 169));
		}

	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
		return null;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
    
    
	}

	@ContainerSectionCallback
	public Map<ContainerSection, List<Slot>> getSlots()
	{
		return InventoryTweaksHelper.getSlots(this, true);
	}
}
