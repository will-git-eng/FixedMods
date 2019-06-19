package com.rwtema.extrautils.tileentity.enderconstructor;

import cofh.api.energy.EnergyStorage;
import com.rwtema.extrautils.helper.XUHelper;
import com.rwtema.extrautils.helper.XURandom;
import com.rwtema.extrautils.sounds.ISoundTile;
import com.rwtema.extrautils.sounds.Sounds;
import com.rwtema.extrautils.tileentity.enderquarry.IChunkLoad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Random;

public class TileEnderConstructor extends TileEntity
		implements IEnderFluxHandler, ISidedInventory, IChunkLoad, ISoundTile
{
	static Random rand = XURandom.getInstance();
	public EnergyStorage energy = new TileEnderConstructor.CustomEnergy(20000);
	public InventoryKraft inv = new InventoryKraft(this);
	public ItemStack outputslot = null;
	int coolDown = 0;
	ResourceLocation location = new ResourceLocation("extrautils", "ambient.qed");

	@Override
	public void setWorldObj(World p_145834_1_)
	{
		super.setWorldObj(p_145834_1_);
		if (p_145834_1_ != null && p_145834_1_.isRemote)
			Sounds.registerSoundTile(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		this.energy.readFromNBT(par1NBTTagCompound);
		this.inv.readFromNBT(par1NBTTagCompound);
		this.outputslot = ItemStack.loadItemStackFromNBT(par1NBTTagCompound.getCompoundTag("output"));
	}

	@Override
	public void onChunkLoad()
	{
		this.inv.markDirty();
	}

	@Override
	public void markDirty()
	{
		if (this.worldObj != null)
		{
			super.markDirty();
			this.inv.markDirty();
		}

	}

	@Override
	public int getSizeInventory()
	{
		return 10;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return i >= 9 ? this.outputslot : this.inv.matrix.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (i != 9)
			return null;
		else if (this.outputslot == null)
			return null;
		else
		{
			ItemStack itemstack;
			if (this.outputslot.stackSize <= j)
			{
				itemstack = this.outputslot;
				this.outputslot = null;
				this.markDirty();
			}
			else
			{
				itemstack = this.outputslot.splitStack(j);
				if (this.outputslot.stackSize == 0)
					this.outputslot = null;

				this.markDirty();
			}

			return itemstack;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return this.getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if (i == 9)
			this.outputslot = itemstack;
		else
			this.inv.setInventorySlotContents(i, itemstack);

	}

	@Override
	public String getInventoryName()
	{
		return this.inv.getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
    
    
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return i != 9 && this.inv.getStackInSlot(i) != null && itemstack != null && XUHelper.canItemsStack(itemstack, this.inv.getStackInSlot(i));
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 9;
	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
		this.energy.writeToNBT(par1NBTTagCompound);
		this.inv.writeToNBT(par1NBTTagCompound);
		if (this.outputslot != null)
		{
			NBTTagCompound output = new NBTTagCompound();
			this.outputslot.writeToNBT(output);
			par1NBTTagCompound.setTag("output", output);
		}

	}

	public int getDisplayProgress()
	{
		return this.energy.getEnergyStored() * 22 / this.energy.getMaxEnergyStored();
	}

	@Override
	public boolean isActive()
	{
		return this.getBlockMetadata() == 1 && this.getWorldObj().isRemote || !this.getWorldObj().isRemote && this.canAddMorez();
	}

	@Override
	public int recieveEnergy(int amount, Transfer simulate)
	{
		return this.energy.receiveEnergy(amount, simulate == Transfer.SIMULATE);
	}

	@Override
	public float getAmountRequested()
	{
		return (float) (this.energy.getMaxEnergyStored() - this.energy.getEnergyStored());
	}

	public boolean canAddMorez()
	{
		ItemStack item = this.inv.getStackInSlot(9);
		return item != null && (this.outputslot == null || XUHelper.canItemsStack(item, this.outputslot) && this.outputslot.stackSize + item.stackSize <= this.outputslot.getMaxStackSize() && this.outputslot.stackSize + item.stackSize <= this.getInventoryStackLimit());
	}

	@Override
	public void updateEntity()
	{
		if (this.worldObj.isRemote && this.getBlockMetadata() == 1)
		{
			double dx1 = (double) this.xCoord + rand.nextDouble();
			double dy1 = (double) this.yCoord + rand.nextDouble();
			double dz1 = (double) this.zCoord + rand.nextDouble();
			double dx2 = (double) this.xCoord + rand.nextDouble();
			double dy2 = (double) this.yCoord + rand.nextDouble();
			double dz2 = (double) this.zCoord + rand.nextDouble();
			this.worldObj.spawnParticle("portal", dx1, dy1, dz1, dx2 - dx1, dy2 - dy1, dz2 - dz1);
		}

		if (!this.worldObj.isRemote)
		{
			int newMeta = -1;
			if (this.energy.getEnergyStored() == this.energy.getMaxEnergyStored() && this.canAddMorez())
			{
				ItemStack result = this.inv.result.getStackInSlot(0).copy();

				for (int i = 0; i < 9; ++i)
				{
					this.inv.matrix.decrStackSize(i, 1);
				}

				this.inv.result.markDirty(this.inv.matrix);
				if (this.outputslot == null)
					this.outputslot = result;
				else
					this.outputslot.stackSize += result.stackSize;

				this.energy.setEnergyStored(0);
				if (!this.canAddMorez())
					newMeta = 4;
			}

			if (this.energy.getEnergyStored() > 0)
				if (this.canAddMorez())
				{
					newMeta = 1;
					this.coolDown = 20;
				}
				else
					this.energy.extractEnergy(1, false);

			if (this.coolDown > 0)
			{
				--this.coolDown;
				if (this.coolDown == 0)
					newMeta = 0;
			}

			if (newMeta != -1 && newMeta != this.getBlockMetadata())
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, newMeta, 2);
		}

	}

	@Override
	public boolean shouldSoundPlay()
	{
		return this.getBlockMetadata() == 1;
	}

	@Override
	public ResourceLocation getSound()
	{
		return this.location;
	}

	@Override
	public TileEntity getTile()
	{
		return this;
	}

	public static class CustomEnergy extends EnergyStorage
	{
		public CustomEnergy(int capacity)
		{
			super(capacity);
		}

		@Override
		public EnergyStorage readFromNBT(NBTTagCompound nbt)
		{
			return super.readFromNBT(nbt);
		}

		@Override
		public void setEnergyStored(int energy)
		{
			super.setEnergyStored(energy);
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			return super.receiveEnergy(maxReceive, simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return super.extractEnergy(maxExtract, simulate);
		}

		@Override
		public void setCapacity(int capacity)
		{
			super.setCapacity(capacity);
		}
	}
}
