package thaumcraft.common.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.common.config.ConfigBlocks;

public class TileChestHungry extends TileEntity implements IInventory
{
	private ItemStack[] chestContents = new ItemStack[36];
	public float lidAngle;
	public float prevLidAngle;
	public int numUsingPlayers;
	private int ticksSinceSync;

	@Override
	public int getSizeInventory()
	{
		return 27;
	}

	@Override
	public ItemStack getStackInSlot(int par1)
	{
		return this.chestContents[par1];
	}

	@Override
	public ItemStack decrStackSize(int slot, int count)
	{
    
		if (this.chestContents[slot] != null)
		{
			if (this.chestContents[slot].stackSize <= count)
			{
				ItemStack itemstack = this.chestContents[slot];
				this.chestContents[slot] = null;
				this.markDirty();
				return itemstack;
			}
			else
			{
				ItemStack itemstack = this.chestContents[slot].splitStack(count);

				if (this.chestContents[slot].stackSize == 0)
					this.chestContents[slot] = null;

				this.markDirty();
				return itemstack;
			}
		}
		else
    
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int par1)
	{
		if (this.chestContents[par1] != null)
		{
			ItemStack var2 = this.chestContents[par1];
			this.chestContents[par1] = null;
			return var2;
		}
		else
			return null;
	}

	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
	{
		this.chestContents[par1] = par2ItemStack;
		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
			par2ItemStack.stackSize = this.getInventoryStackLimit();

		this.markDirty();
	}

	@Override
	public String getInventoryName()
	{
		return "Hungry Chest";
	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		NBTTagList var2 = par1NBTTagCompound.getTagList("Items", 10);
		this.chestContents = new ItemStack[this.getSizeInventory()];

		for (int var3 = 0; var3 < var2.tagCount(); ++var3)
		{
			NBTTagCompound var4 = var2.getCompoundTagAt(var3);
			int var5 = var4.getByte("Slot") & 255;
			if (var5 >= 0 && var5 < this.chestContents.length)
				this.chestContents[var5] = ItemStack.loadItemStackFromNBT(var4);
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
		NBTTagList var2 = new NBTTagList();

		for (int var3 = 0; var3 < this.chestContents.length; ++var3)
		{
			if (this.chestContents[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) var3);
				this.chestContents[var3].writeToNBT(var4);
				var2.appendTag(var4);
			}
		}

		par1NBTTagCompound.setTag("Items", var2);
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
	{
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && par1EntityPlayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (++this.ticksSinceSync % 20 * 4 == 0)
			;

		this.prevLidAngle = this.lidAngle;
		float var1 = 0.1F;
		if (this.numUsingPlayers > 0 && this.lidAngle == 0.0F)
		{
			double var2 = this.xCoord + 0.5D;
			double var4 = this.zCoord + 0.5D;
			this.worldObj.playSoundEffect(var2, this.yCoord + 0.5D, var4, "random.chestopen", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
		}

		if (this.numUsingPlayers == 0 && this.lidAngle > 0.0F || this.numUsingPlayers > 0 && this.lidAngle < 1.0F)
		{
			float var8 = this.lidAngle;
			if (this.numUsingPlayers > 0)
				this.lidAngle += var1;
			else
				this.lidAngle -= var1;

			if (this.lidAngle > 1.0F)
				this.lidAngle = 1.0F;

			float var3 = 0.5F;
			if (this.lidAngle < var3 && var8 >= var3)
			{
				double var4 = this.xCoord + 0.5D;
				double var6 = this.zCoord + 0.5D;
				this.worldObj.playSoundEffect(var4, this.yCoord + 0.5D, var6, "random.chestclosed", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			}

			if (this.lidAngle < 0.0F)
				this.lidAngle = 0.0F;
		}

	}

	@Override
	public boolean receiveClientEvent(int par1, int par2)
	{
		if (par1 == 1)
		{
			this.numUsingPlayers = par2;
			return true;
		}
		else if (par1 == 2)
		{
			if (this.lidAngle < par2 / 10.0F)
				this.lidAngle = par2 / 10.0F;

			return true;
		}
		else
			return this.tileEntityInvalid;
	}

	@Override
	public void openInventory()
	{
		++this.numUsingPlayers;
		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, ConfigBlocks.blockChestHungry, 1, this.numUsingPlayers);
	}

	@Override
	public void closeInventory()
	{
		--this.numUsingPlayers;
		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, ConfigBlocks.blockChestHungry, 1, this.numUsingPlayers);
	}

	@Override
	public void invalidate()
	{
		this.updateContainingBlockInfo();
		super.invalidate();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}
}
