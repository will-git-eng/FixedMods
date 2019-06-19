package com.rwtema.extrautils.tileentity.enderconstructor;

import com.rwtema.extrautils.dynamicgui.*;
import invtweaks.api.container.InventoryContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

@InventoryContainer
public class DynamicContainerEnderConstructor extends DynamicContainer
{
	public TileEnderConstructor tile;
	public IInventory player;

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
		return super.transferStackInSlot(par1EntityPlayer, par2);
	}

	public DynamicContainerEnderConstructor(IInventory player, TileEnderConstructor tile)
	{
		this.tile = tile;
		this.player = player;

		for (int j = 0; j < 3; ++j)
		{
			for (int i = 0; i < 3; ++i)
			{
				this.widgets.add(new WidgetSlot(tile.inv, i + j * 3, 30 + i * 18, 17 + j * 18));
			}
		}

		this.widgets.add(new WidgetSlotRespectsInsertExtract(tile, 9, 124, 35));
		this.widgets.add(new Arrow(tile, 90, 35));
		this.widgets.add(new WidgetSlotGhost(tile.inv, 9, 92, 13));
		this.widgets.add(new WidgetEFText(tile, 9, 75, 124));
		this.cropAndAddPlayerSlots(player);
		this.validate();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
    
    
	}

	public static class Arrow extends WidgetProgressArrow
	{
		TileEnderConstructor tile;

		public Arrow(TileEnderConstructor tile, int x, int y)
		{
			super(x, y);
			this.tile = tile;
		}

		@Override
		public int getWidth()
		{
			return this.tile.getDisplayProgress();
		}
	}

	public static class WidgetEFText extends WidgetTextData
	{
		IEnderFluxHandler tile;

		public WidgetEFText(IEnderFluxHandler tile, int x, int y, int w)
		{
			super(x, y, w);
			this.tile = tile;
		}

		@Override
		public int getNumParams()
		{
			return 1;
		}

		@Override
		public Object[] getData()
		{
			return new Object[] { this.tile.getAmountRequested(), (byte) (this.tile.isActive() ? 1 : 0) };
		}

		@Override
		public String getConstructedText()
		{
			return this.curData != null && this.curData.length == 2 && this.curData[0] instanceof Float && this.curData[1] instanceof Boolean ? ((Byte) this.curData[1]).byteValue() == 1 ? "Ender-Flux: " + ((Float) this.curData[0]).floatValue() / 1000.0F + " EF" : "" : "";
		}
	}
}
