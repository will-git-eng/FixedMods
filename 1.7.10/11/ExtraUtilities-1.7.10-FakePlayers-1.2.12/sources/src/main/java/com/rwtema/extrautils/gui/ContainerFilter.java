package com.rwtema.extrautils.gui;

import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.helper.XUHelper;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import invtweaks.api.container.InventoryContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;

import java.util.List;
import java.util.Map;

@InventoryContainer
public class ContainerFilter extends Container
{
	private EntityPlayer player = null;
	private int currentFilter = -1;

	public ContainerFilter(EntityPlayer player, int invId)
	{
		this.player = player;
		this.currentFilter = invId;

		for (int k = 0; k < 9; ++k)
		{
			this.addSlotToContainer(new SlotGhostItemContainer(player.inventory, k, 8 + k * 18, 18, this.currentFilter));
		}

		for (int j = 0; j < 3; ++j)
		{
			for (int var6 = 0; var6 < 9; ++var6)
			{
				this.addSlotToContainer(new Slot(player.inventory, var6 + j * 9 + 9, 8 + var6 * 18, 50 + j * 18));
			}
		}

		for (int var5 = 0; var5 < 9; ++var5)
		{
			if (var5 == this.currentFilter)
				this.addSlotToContainer(new SlotDisabled(player.inventory, var5, 8 + var5 * 18, 108));
			else
				this.addSlotToContainer(new Slot(player.inventory, var5, 8 + var5 * 18, 108));
		}
	}

	@Override
	public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer par4EntityPlayer)
    
		if (par3 == 2 && par2 == this.currentFilter)
    

		if (par1 >= 0 && par1 < 9)
		{
			ItemStack item = par4EntityPlayer.inventory.getItemStack();
			return this.clickItemStack(par1, item);
		}
		else
			return super.slotClick(par1, par2, par3, par4EntityPlayer);
	}

	public ItemStack clickItemStack(int par1, ItemStack item)
	{
		if (item != null)
		{
			item = item.copy();
			item.stackSize = 1;
		}

		String keyname = "items_" + par1;
		ItemStack filter = this.player.inventory.getStackInSlot(this.currentFilter);
		if (filter == null)
			return item;
		else
		{
			NBTTagCompound tags = filter.getTagCompound();
			if (item != null)
			{
				if (tags == null)
					tags = new NBTTagCompound();

				if (tags.hasKey(keyname))
				{
					if (FluidContainerRegistry.isFilledContainer(item) && ItemStack.areItemStacksEqual(ItemStack.loadItemStackFromNBT(tags.getCompoundTag(keyname)), item))
					{
						new NBTTagCompound();
						if (tags.hasKey("isLiquid_" + par1))
							tags.removeTag("isLiquid_" + par1);
						else
							tags.setBoolean("isLiquid_" + par1, true);

						return item;
					}

					if (tags.hasKey("isLiquid_" + par1))
						tags.removeTag("isLiquid_" + par1);

					tags.removeTag(keyname);
				}
				else if (tags.hasKey("isLiquid_" + par1))
					tags.removeTag("isLiquid_" + par1);

				NBTTagCompound itemTags = new NBTTagCompound();
				item.writeToNBT(itemTags);
				tags.setTag(keyname, itemTags);
				filter.setTagCompound(tags);
			}
			else if (tags != null)
			{
				if (tags.hasKey("isLiquid_" + par1))
					tags.removeTag("isLiquid_" + par1);

				tags.removeTag(keyname);
				if (tags.hasNoTags())
					filter.setTagCompound(null);
				else
					filter.setTagCompound(tags);
			}

			return item;
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
		Slot slot = (Slot) this.inventorySlots.get(par2);
		if (slot != null && slot.getHasStack())
			if (slot instanceof SlotGhostItemContainer)
				this.slotClick(slot.slotNumber, 0, 0, par1EntityPlayer);
			else
				for (int i = 0; i < 9; ++i)
				{
					if (!((SlotGhostItemContainer) this.inventorySlots.get(i)).getHasStack())
					{
						this.clickItemStack(i, slot.getStack());
						return null;
					}

					if (XUHelper.canItemsStack(slot.getStack(), ((SlotGhostItemContainer) this.inventorySlots.get(i)).getStack()))
						return null;
				}

		return null;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
    
		ItemStack item = this.player.inventory.getStackInSlot(this.currentFilter);
    
	}

	@ContainerSectionCallback
	public Map<ContainerSection, List<Slot>> getSlots()
	{
		return InventoryTweaksHelper.getSlots(this, true);
	}
}
