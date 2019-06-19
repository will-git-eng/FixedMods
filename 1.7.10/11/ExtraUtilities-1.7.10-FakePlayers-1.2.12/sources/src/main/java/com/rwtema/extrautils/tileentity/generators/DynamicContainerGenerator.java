package com.rwtema.extrautils.tileentity.generators;

import com.rwtema.extrautils.dynamicgui.*;
import invtweaks.api.container.InventoryContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

@InventoryContainer
public class DynamicContainerGenerator extends DynamicContainer
{
	TileEntityGenerator gen;
	public TileEntityGeneratorFurnace genFurnace = null;

	public DynamicContainerGenerator(IInventory player, TileEntityGenerator gen)
	{
		this.gen = gen;
		if (this.gen instanceof TileEntityGeneratorFurnace)
			this.genFurnace = (TileEntityGeneratorFurnace) this.gen;

		this.widgets.add(new WidgetText(5, 5, BlockGenerator.names[gen.getBlockMetadata()] + " Generator", 162));
		int x = 5;
		int y = 19;
		if (gen instanceof IInventory)
		{
			IInventory inv = (IInventory) gen;

			for (int i = 0; i < inv.getSizeInventory(); ++i)
			{
				IWidget widg = new WidgetSlot(inv, i, x, y);
				this.widgets.add(widg);
				x += widg.getW() + 5;
			}
		}

		if (gen instanceof IFluidHandler)
		{
			FluidTankInfo[] tanks = gen.getTankInfo(null);

			for (FluidTankInfo tank : tanks)
			{
				IWidget widg = new WidgetTank(tank, x, y, 2);
				this.widgets.add(widg);
				x += widg.getW() + 5;
			}
		}

		IWidget w = new DynamicContainerGenerator.WidgetTextCooldown(gen, x, y, 120);
		this.widgets.add(w);
		x = x + w.getW() + 5;
		this.widgets.add(new WidgetEnergy(gen, ForgeDirection.UP, x, y));
		this.cropAndAddPlayerSlots(player);
		this.validate();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
    
		if (this.gen == null)
			return true;
		int x = this.gen.xCoord;
		int y = this.gen.yCoord;
		int z = this.gen.zCoord;
    
	}

	public class WidgetTextCooldown extends WidgetTextData
	{
		TileEntityGenerator gen;

		public WidgetTextCooldown(TileEntityGenerator gen, int x, int y, int w)
		{
			super(x, y, w);
			this.gen = gen;
		}

		@Override
		public int getNumParams()
		{
			return 2;
		}

		@Override
		public Object[] getData()
		{
			return new Object[] { (long) (10.0D * this.gen.coolDown), (long) Math.ceil(10.0D * this.gen.genLevel() * (double) this.gen.getMultiplier()) };
		}

		@Override
		public String getConstructedText()
		{
			if (this.curData != null && this.curData[0] != null)
			{
				double t;
				double t2;
				try
				{
					t = (double) (Long) this.curData[0] / 200.0D;
					t2 = (double) (Long) this.curData[1] / 10.0D;
				}
				catch (Exception var6)
				{
					return "";
				}

				return this.gen.getBlurb(t, t2);
			}
			else
				return "";
		}
	}
}
