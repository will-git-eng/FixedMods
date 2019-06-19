package com.rwtema.extrautils.tileentity.transfernodes;

import ru.will.git.extrautilities.SafeInventoryWrapper;
import com.rwtema.extrautils.tileentity.transfernodes.nodebuffer.INodeBuffer;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IFilterPipe;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IPipe;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.IPipeCosmetic;
import com.rwtema.extrautils.tileentity.transfernodes.pipes.StdPipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

    
public class TileEntityFilterPipe extends TileEntity
		implements IFilterPipe, IPipe, IPipeCosmetic, SafeInventoryWrapper.SafeChecker
{
    
	@Override
	public boolean isUseableByPlayer0(EntityPlayer player)
	{
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) <= 64;
    

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		super.readFromNBT(tags);
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
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
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

	@Override
	public ArrayList<ForgeDirection> getOutputDirections(IBlockAccess world, int x, int y, int z, ForgeDirection dir, INodeBuffer buffer)
	{
		return StdPipes.getPipeType(9).getOutputDirections(world, x, y, z, dir, buffer);
	}

	@Override
	public boolean transferItems(IBlockAccess world, int x, int y, int z, ForgeDirection dir, INodeBuffer buffer)
	{
		return StdPipes.getPipeType(9).transferItems(world, x, y, z, dir, buffer);
	}

	@Override
	public boolean canInput(IBlockAccess world, int x, int y, int z, ForgeDirection dir)
	{
		return StdPipes.getPipeType(9).canInput(world, x, y, z, dir);
	}

	@Override
	public boolean canOutput(IBlockAccess world, int x, int y, int z, ForgeDirection dir)
	{
		return StdPipes.getPipeType(9).canOutput(world, x, y, z, dir);
	}

	@Override
	public int limitTransfer(TileEntity dest, ForgeDirection side, INodeBuffer buffer)
	{
		return StdPipes.getPipeType(9).limitTransfer(dest, side, buffer);
	}

	@Override
	public boolean shouldConnectToTile(IBlockAccess world, int x, int y, int z, ForgeDirection dir)
	{
		return StdPipes.getPipeType(9).shouldConnectToTile(world, x, y, z, dir);
	}

	@Override
	public IIcon baseTexture()
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(9)).baseTexture();
	}

	@Override
	public IIcon pipeTexture(ForgeDirection dir, boolean blocked)
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(9)).pipeTexture(dir, blocked);
	}

	@Override
	public IIcon invPipeTexture(ForgeDirection dir)
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(9)).invPipeTexture(dir);
	}

	@Override
	public IIcon socketTexture(ForgeDirection dir)
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(9)).socketTexture(dir);
	}

	@Override
	public String getPipeType()
	{
		return StdPipes.getPipeType(9).getPipeType();
	}

	@Override
	public float baseSize()
	{
		return ((IPipeCosmetic) StdPipes.getPipeType(9)).baseSize();
	}
}
