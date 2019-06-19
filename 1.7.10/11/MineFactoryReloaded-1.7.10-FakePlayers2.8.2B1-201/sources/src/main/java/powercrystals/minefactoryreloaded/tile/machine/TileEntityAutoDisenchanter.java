package powercrystals.minefactoryreloaded.tile.machine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.gui.client.GuiAutoDisenchanter;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.container.ContainerAutoDisenchanter;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

public class TileEntityAutoDisenchanter extends TileEntityFactoryPowered
{
	private boolean _repeatDisenchant;

	public TileEntityAutoDisenchanter()
	{
		super(Machine.AutoDisenchanter);
		this.setManageSolids(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiAutoDisenchanter(this.getContainer(var1), this);
	}

	@Override
	public ContainerAutoDisenchanter getContainer(InventoryPlayer var1)
	{
		return new ContainerAutoDisenchanter(this, var1);
	}

	public boolean getRepeatDisenchant()
	{
		return this._repeatDisenchant;
	}

	public void setRepeatDisenchant(boolean var1)
	{
		this._repeatDisenchant = var1;
	}

	@Override
	public int getSizeInventory()
	{
		return 5;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection var1)
	{
		return 4;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int var3)
    
		Item item = stack == null ? null : stack.getItem();
		if (item != null && item.getClass().getName().equals("ic2.core.item.tool.ItemDrillIridium"))
			return false;
		if (item != null && item.getItemStackLimit(stack) > 1)
    

		return stack != null && (slot != 0 ? slot == 1 && stack.getItem().equals(Items.book) : stack.getEnchantmentTagList() != null || stack.getItem().equals(Items.enchanted_book));
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3)
	{
		return var1 == 2 || var1 == 3;
	}

	@Override
	protected boolean activateMachine()
	{
		if (this._inventory[4] == null)
		{
			if (this._inventory[0] == null)
				return false;

			this._inventory[4] = this._inventory[0].splitStack(1);
			if (this._inventory[0].stackSize <= 0)
				this._inventory[0] = null;

			this.markChunkDirty();
		}

    
		Item item = stack == null ? null : stack.getItem();
		if (item != null && item.getClass().getName().equals("ic2.core.item.tool.ItemDrillIridium"))
			return false;
		if (item != null && item.getItemStackLimit(stack) > 1)
    

		boolean isBook = stack.getItem().equals(Items.enchanted_book);
		NBTTagList nbtList = isBook ? Items.enchanted_book.func_92110_g(stack) : stack.getEnchantmentTagList();
		if ((nbtList == null || nbtList.tagCount() <= 0) && this._inventory[2] == null)
		{
			this._inventory[2] = stack;
			this.setInventorySlotContents(4, null);
		}
		else if (nbtList != null && nbtList.tagCount() > 0 && (this._inventory[1] != null && this._inventory[1].getItem().equals(Items.book)) & this._inventory[2] == null & this._inventory[3] == null)
		{
			if (this.getWorkDone() >= this.getWorkMax())
			{
				this.decrStackSize(1, 1);
				NBTTagCompound nbt;
				if (isBook)
				{
					nbt = nbtList.getCompoundTagAt(0);
					nbtList.removeTag(0);
					if (nbtList.tagCount() == 0)
						this._inventory[4] = new ItemStack(Items.book, 1);
				}
				else
				{
					int index = this.worldObj.rand.nextInt(nbtList.tagCount());
					nbt = nbtList.getCompoundTagAt(index);
					nbtList.removeTag(index);
					if (nbtList.tagCount() == 0)
					{
						stack.getTagCompound().removeTag("ench");
						if (stack.getTagCompound().hasNoTags())
							stack.setTagCompound(null);
    
					if (stack.isItemStackDamageable() && !stack.getHasSubtypes())
					{
						int damage = this.worldObj.rand.nextInt(1 + stack.getMaxDamage() / 4);
						int maxDamage = stack.getMaxDamage();
						damage = Math.min(maxDamage, damage + 1 + maxDamage / 10) + (maxDamage == 1 ? 1 : 0);
						if (stack.attemptDamageItem(damage, this.worldObj.rand))
							this._inventory[4] = null;
					}
				}

				if (!this._repeatDisenchant || this._inventory[4] != null && this._inventory[4].getEnchantmentTagList() == null)
				{
					this._inventory[2] = this._inventory[4];
					this._inventory[4] = null;
				}

				this.setInventorySlotContents(3, new ItemStack(Items.enchanted_book, 1));
				NBTTagCompound newNbt = new NBTTagCompound();
				NBTTagList newNbtList = new NBTTagList();
				newNbtList.appendTag(nbt);
				newNbt.setTag("StoredEnchantments", newNbtList);
				this._inventory[3].setTagCompound(newNbt);
				this.setWorkDone(0);
			}
			else
			{
				this.markChunkDirty();
				return this.incrementWorkDone();
			}

			return true;
		}

		return false;
	}

	@Override
	public int getWorkMax()
	{
		return 600;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 1;
	}

	@Override
	public void writePortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		var2.setBoolean("repeatDisenchant", this._repeatDisenchant);
	}

	@Override
	public void readPortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		this._repeatDisenchant = var2.getBoolean("repeatDisenchant");
	}

	@Override
	public void writeItemNBT(NBTTagCompound var1)
	{
		super.writeItemNBT(var1);
		if (this._repeatDisenchant)
			var1.setBoolean("repeatDisenchant", this._repeatDisenchant);

	}

	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		this._repeatDisenchant = var1.getBoolean("repeatDisenchant");
	}
}
