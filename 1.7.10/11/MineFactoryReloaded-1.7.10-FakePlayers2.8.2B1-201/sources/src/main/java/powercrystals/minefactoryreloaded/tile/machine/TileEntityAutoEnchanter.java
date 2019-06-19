package powercrystals.minefactoryreloaded.tile.machine;

import cofh.core.util.fluid.FluidTankAdv;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import powercrystals.minefactoryreloaded.core.AutoEnchantmentHelper;
import powercrystals.minefactoryreloaded.core.ITankContainerBucketable;
import powercrystals.minefactoryreloaded.gui.client.GuiAutoEnchanter;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.container.ContainerAutoEnchanter;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

import java.util.Map;
import java.util.Random;

public class TileEntityAutoEnchanter extends TileEntityFactoryPowered implements ITankContainerBucketable
{
	private Random _rand = new Random();
	private int _targetLevel = 30;

	public TileEntityAutoEnchanter()
	{
		super(Machine.AutoEnchanter);
		this.setManageSolids(true);
		this._tanks[0].setLock(FluidRegistry.getFluid("mobessence"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiAutoEnchanter(this.getContainer(var1), this);
	}

	@Override
	public ContainerAutoEnchanter getContainer(InventoryPlayer var1)
	{
		return new ContainerAutoEnchanter(this, var1);
	}

	@Override
	public int getWorkMax()
	{
		return this._inventory[0] != null && this._inventory[0].getItem().equals(Items.glass_bottle) ? 250 : this._targetLevel + (int) (Math.pow((double) this._targetLevel / 7.5D, 4.0D) * 10.0D * this.getEnchantmentMultiplier());
	}

	private double getEnchantmentMultiplier()
	{
		ItemStack var1 = this._inventory[0];
		if (var1 == null)
			return 1.0D;
		Map var2 = EnchantmentHelper.getEnchantments(var1);
		return var2 != null && var2.size() != 0 ? Math.pow((double) var2.size() + 1.0D, 2.0D) : 1.0D;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 1;
	}

	public int getTargetLevel()
	{
		return this._targetLevel;
	}

	public void setTargetLevel(int var1)
	{
		this._targetLevel = var1;
		if (this._targetLevel > 30)
			this._targetLevel = 30;

		if (this._targetLevel < 1)
			this._targetLevel = 1;

		if (this.getWorkDone() >= this.getWorkMax())
			this.activateMachine();

	}

	@Override
	protected boolean activateMachine()
	{
		if (this.worldObj.isRemote)
			return false;
		ItemStack var1 = this._inventory[0];
		ItemStack var2 = this._inventory[1];
		if (var1 == null)
		{
			this.setWorkDone(0);
			return false;
		}
		if (var1.stackSize <= 0)
		{
			this.setInventorySlotContents(0, null);
			this.setWorkDone(0);
			return false;
		}
		if (var2 != null)
		{
			if (var2.stackSize >= var2.getMaxStackSize() || var2.stackSize >= this.getInventoryStackLimit())
			{
				this.setWorkDone(0);
				return false;
			}

			if (var2.stackSize <= 0)
			{
				this.setInventorySlotContents(1, null);
				var2 = null;
			}
		}

		if ((var1.getItem().getItemEnchantability(var1) != 0 || var1.getItem().equals(Items.glass_bottle)) && !var1.getItem().equals(Items.enchanted_book))
		{
			if (this.getWorkDone() >= this.getWorkMax())
			{
				if (var1.getItem().equals(Items.glass_bottle))
				{
					if (var2 == null)
						var2 = new ItemStack(Items.experience_bottle, 0, 0);

					if (!var2.getItem().equals(Items.experience_bottle))
					{
						this.setWorkDone(0);
						return false;
					}

					if (--var1.stackSize <= 0)
						this._inventory[0] = null;

					++var2.stackSize;
					this.setInventorySlotContents(1, var2);
					this.setWorkDone(0);
				}
				else
				{
					if (var2 != null)
						return false;

					var2 = AutoEnchantmentHelper.addRandomEnchantment(this._rand, var1, this._targetLevel);
					if (var1.stackSize <= 0)
						this._inventory[0] = null;

					this.setInventorySlotContents(1, var2);
					this.setWorkDone(0);
				}

				return true;
			}
			if (this.drain(this._tanks[0], 4, false) == 4)
			{
				if (!this.incrementWorkDone())
					return false;
				this.drain(this._tanks[0], 4, true);
				return true;
			}
			return false;
		}
		if (var2 == null)
		{
			this._inventory[0] = null;
			this.setInventorySlotContents(1, var1);
		}
		else
		{
			if (!var1.isItemEqual(var2) || !ItemStack.areItemStackTagsEqual(var1, var2))
			{
				this.setWorkDone(0);
				return false;
			}

			int var3 = Math.min(var2.getMaxStackSize() - var2.stackSize, var1.stackSize);
			var3 = Math.min(this.getInventoryStackLimit() - var2.stackSize, var3);
			if (var3 <= 0)
			{
				this.setWorkDone(0);
				return false;
			}

			var2.stackSize += var3;
			var1.stackSize -= var3;
			if (var1.stackSize <= 0)
				this.setInventorySlotContents(0, null);
		}

		this.setWorkDone(0);
		return true;
	}

	@Override
	public int getSizeInventory()
	{
		return 2;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection var1)
	{
		return 2;
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3)
	{
		if (var1 != 0)
			return false;
		ItemStack var4 = this._inventory[0];
		return var4 == null || var4.stackSize < this.getInventoryStackLimit() && var2.isItemEqual(var4) && ItemStack.areItemStackTagsEqual(var2, var4);
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3)
	{
		return var1 == 1;
	}

	@Override
	public void writePortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		var2.setInteger("targetLevel", this._targetLevel);
	}

	@Override
	public void readPortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		this.setTargetLevel(var2.getInteger("targetLevel"));
	}

	@Override
	public void writeItemNBT(NBTTagCompound var1)
	{
		super.writeItemNBT(var1);
		if (this._targetLevel != 30)
			var1.setInteger("targetLevel", this._targetLevel);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
    
    
			this._targetLevel = nbt.getInteger("targetLevel");
	}

	@Override
	public boolean allowBucketFill(ItemStack var1)
	{
		return true;
	}

	@Override
	public int fill(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return this.fill(var2, var3);
	}

	@Override
	public FluidStack drain(ForgeDirection var1, int var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	public FluidStack drain(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	protected FluidTankAdv[] createTanks()
	{
		return new FluidTankAdv[] { new FluidTankAdv(4000) };
	}

	@Override
	public boolean canFill(ForgeDirection var1, Fluid var2)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection var1, Fluid var2)
	{
		return false;
	}
}
