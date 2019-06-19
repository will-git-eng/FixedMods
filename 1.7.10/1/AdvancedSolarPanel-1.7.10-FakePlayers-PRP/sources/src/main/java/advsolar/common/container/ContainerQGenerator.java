package advsolar.common.container;

import advsolar.common.tiles.TileEntityQGenerator;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

public class ContainerQGenerator extends Container
{
	private TileEntityQGenerator tileentity;

	public ContainerQGenerator(InventoryPlayer inventoryplayer, TileEntityQGenerator tileentityqgenerator)
	{
		this.tileentity = tileentityqgenerator;

		for (int i = 0; i < 3; ++i)
			for (int k = 0; k < 9; ++k)
				this.addSlotToContainer(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, 110 + i * 18));

		for (int j = 0; j < 9; ++j)
			this.addSlotToContainer(new Slot(inventoryplayer, j, 8 + j * 18, 168));

	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for (int i = 0; i < super.crafters.size(); ++i)
		{
			ICrafting icrafting = (ICrafting) super.crafters.get(i);
			if (icrafting instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP) icrafting;
				NetworkManager network = IC2.network.get();
				network.updateTileEntityFieldTo(this.tileentity, "production", player);
				network.updateTileEntityFieldTo(this.tileentity, "maxPacketSize", player);
			}
			else
			{
				icrafting.sendProgressBarUpdate(this, 0, this.tileentity.production);
				icrafting.sendProgressBarUpdate(this, 1, this.tileentity.maxPacketSize);
			}
			    

			icrafting.sendProgressBarUpdate(this, 2, this.tileentity.active ? 1 : 0);
		}

	}

	@Override
	public void updateProgressBar(int i, int j)
	{
		if (i == 0)
			this.tileentity.production = j;

		if (i == 1)
			this.tileentity.maxPacketSize = j;

		if (i == 2)
			this.tileentity.active = j != 0;

	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return this.tileentity.isUseableByPlayer(entityplayer);
	}
}
