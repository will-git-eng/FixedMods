package com.rwtema.extrautils.tileentity.transfernodes.multiparts;

import ru.will.git.extrautilities.SafeInventoryWrapper;
import com.rwtema.extrautils.ExtraUtilsMod;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IFilterPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
    
public class FilterPipePart extends PipePart implements IFilterPipe, SafeInventoryWrapper.SafeChecker
{
    
	@Override
	public boolean isUseableByPlayer0(EntityPlayer player)
	{
		TileEntity tile = this.tile();
		if (tile == null)
			return false;
		int x = tile.xCoord;
		int y = tile.yCoord;
		int z = tile.zCoord;
		return tile.getWorldObj().getTileEntity(x, y, z) == tile && player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) <= 64;
    

	public FilterPipePart(int meta)
	{
		super(9);
	}

	public FilterPipePart(InventoryBasic items)
	{
		super(9);
		this.items = items;
	}

	public FilterPipePart()
	{
		super(9);
	}

	@Override
	public void onRemoved()
	{
		if (!this.getWorld().isRemote)
		{
			List<ItemStack> drops = new ArrayList();

			for (int i = 0; i < this.items.getSizeInventory(); ++i)
			{
				if (this.items.getStackInSlot(i) != null)
					drops.add(this.items.getStackInSlot(i));
			}

			this.tile().dropItems(drops);
		}

	}

	@Override
	public String getType()
	{
		return "extrautils:transfer_pipe_filter";
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		player.openGui(ExtraUtilsMod.instance, 0, this.getWorld(), this.x(), this.y(), this.z());
		return true;
	}

	@Override
	public void load(NBTTagCompound tags)
	{
		super.load(tags);
		if (tags.hasKey("items"))
		{
			NBTTagCompound item_tags = tags.getCompoundTag("items");

			for (int i = 0; i < this.items.getSizeInventory(); ++i)
			{
				if (item_tags.hasKey("item_" + i))
					this.items.setInventorySlotContents(i, ItemStack.loadItemStackFromNBT(item_tags.getCompoundTag("item_" + i)));
			}
		}

	}

	@Override
	public void save(NBTTagCompound par1NBTTagCompound)
	{
		super.save(par1NBTTagCompound);
		NBTTagCompound item_tags = new NBTTagCompound();

		for (int i = 0; i < this.items.getSizeInventory(); ++i)
		{
			if (this.items.getStackInSlot(i) != null)
			{
				NBTTagCompound item = new NBTTagCompound();
				this.items.getStackInSlot(i).writeToNBT(item);
				item_tags.setTag("item_" + i, item);
			}
		}

		par1NBTTagCompound.setTag("items", item_tags);
	}

	@Override
	public IInventory getFilterInventory(IBlockAccess world, int x, int y, int z)
    
    
	}
}
