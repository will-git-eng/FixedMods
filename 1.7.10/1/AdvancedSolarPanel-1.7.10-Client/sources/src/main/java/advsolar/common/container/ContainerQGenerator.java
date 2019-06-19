package advsolar.common.container;

import advsolar.common.tiles.TileEntityQGenerator;
import ru.will.git.advsolar.ModUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

public class ContainerQGenerator extends Container
{
	private TileEntityQGenerator tileentity;

	    
	private int prevProduction;
	private int prevMaxPacketSize;
	private boolean prevActive;
	    

	public ContainerQGenerator(InventoryPlayer inventoryplayer, TileEntityQGenerator tileentityqgenerator)
	{
		this.tileentity = tileentityqgenerator;

		for (int i = 0; i < 3; ++i)
		{
			for (int k = 0; k < 9; ++k)
			{
				this.addSlotToContainer(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, 110 + i * 18));
			}
		}

		for (int j = 0; j < 9; ++j)
		{
			this.addSlotToContainer(new Slot(inventoryplayer, j, 8 + j * 18, 168));
		}
	}

	    
	@Override
	public void addCraftingToCrafters(ICrafting icrafting)
	{
		super.addCraftingToCrafters(icrafting);
		ModUtils.sendContainerInt(this, icrafting, 0, 3, this.tileentity.production);
		ModUtils.sendContainerInt(this, icrafting, 1, 4, this.tileentity.maxPacketSize);
		icrafting.sendProgressBarUpdate(this, 2, this.tileentity.active ? 1 : 0);
	}
	    

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for (int i = 0; i < this.crafters.size(); ++i)
		{
			ICrafting icrafting = (ICrafting) this.crafters.get(i);

			if (this.prevProduction != this.tileentity.production)
				ModUtils.sendContainerInt(this, icrafting, 0, 3, this.tileentity.production);
			if (this.prevMaxPacketSize != this.tileentity.maxPacketSize)
				ModUtils.sendContainerInt(this, icrafting, 1, 4, this.tileentity.maxPacketSize);
			if (this.prevActive != this.tileentity.active)
				icrafting.sendProgressBarUpdate(this, 2, this.tileentity.active ? 1 : 0);
			    
		}

		    
		this.prevProduction = this.tileentity.production;
		this.prevMaxPacketSize = this.tileentity.maxPacketSize;
		this.prevActive = this.tileentity.active;
		    
	}

	@Override
	public void updateProgressBar(int id, int val)
	{

		this.tileentity.production = ModUtils.recieveContainerInt(0, 3, id, val, this.tileentity.production);
		this.tileentity.maxPacketSize = ModUtils.recieveContainerInt(1, 4, id, val, this.tileentity.maxPacketSize);
		    

		if (id == 2)
			this.tileentity.active = val != 0;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return this.tileentity.isUseableByPlayer(entityplayer);
	}
}
