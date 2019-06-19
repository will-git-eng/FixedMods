package mcp.mobius.betterbarrels.common.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.common.blocks.logic.Coordinates;
import mcp.mobius.betterbarrels.common.blocks.logic.ItemImmut;
import mcp.mobius.betterbarrels.common.blocks.logic.OreDictPair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class StorageLocal implements IBarrelStorage
{
    
    
    
	private ItemStack prevOutputStack = null;
	private ItemStack itemTemplate = null;
	private ItemStack renderingTemplate = null;

    
    

    
    
    
    
    
    
    

	private Set<Coordinates> linkedStorages = new HashSet<Coordinates>();

	private ItemImmut cachedBarrelOreItem = null;
	private static HashMap<OreDictPair, Boolean> oreDictCache = new HashMap<OreDictPair, Boolean>();

	public StorageLocal()
	{
		this.markDirty();
	}

	public StorageLocal(NBTTagCompound tag)
	{
		this.readTagCompound(tag);
		this.markDirty();
	}

	public StorageLocal(int nupgrades)
	{
		for (int i = 0; i < nupgrades; i++)
			this.addStorageUpgrade();
		this.markDirty();
	}

	private ItemStack getStackFromSlot(int slot)
	{
		return slot == 0 ? this.inputStack : this.outputStack;
	}

	private int getFreeSpace()
	{
		return this.totalCapacity - (this.deleteExcess ? 0 : this.totalAmount);
    
	@Override
	public boolean hasItem()
	{
		return this.itemTemplate != null;
	}

	@Override
	public ItemStack getItem()
	{
		return this.itemTemplate;
	}

	@Override
	public ItemStack getItemForRender()
	{
		if (this.renderingTemplate == null)
		{
			this.renderingTemplate = this.itemTemplate.copy();
			if (this.renderingTemplate.hasTagCompound() && this.renderingTemplate.getTagCompound().hasKey("ench"))
				this.renderingTemplate.getTagCompound().removeTag("ench");
			if (this.renderingTemplate.hasTagCompound() && this.renderingTemplate.getTagCompound().hasKey("CustomPotionEffects"))
				this.renderingTemplate.getTagCompound().removeTag("CustomPotionEffects");
			if (this.renderingTemplate.getItem() == Items.potionitem)
				this.renderingTemplate.setItemDamage(0);
			if (this.renderingTemplate.getItem() == Items.experience_bottle)
				this.renderingTemplate = new ItemStack(Items.potionitem, 0, 0);
		}
		return this.renderingTemplate;
	}

	@Override
	public void setItem(ItemStack stack)
	{
		if (stack != null)
		{
			this.itemTemplate = stack.copy();
			this.itemTemplate.stackSize = 0;
			this.stackAmount = stack.getMaxStackSize();
    
			this.renderingTemplate = null;
			this.getItemForRender();
		}
		else
		{
			this.itemTemplate = null;
			this.renderingTemplate = null;
			this.stackAmount = 64;
			this.cachedBarrelOreItem = null;
		}
		this.totalCapacity = this.maxstacks * this.stackAmount;
	}

	@Override
	public boolean sameItem(ItemStack stack)
	{
		if (this.itemTemplate == null)
		{
			if (this.keepLastItem)
				return false;
			return true;
		}
		if (stack == null)
			return false;

		if (!this.itemTemplate.isItemEqual(stack))
		{
    
			;
    
		}

		return ItemStack.areItemStackTagsEqual(this.itemTemplate, stack);
	}

    
	@Override
	public NBTTagCompound writeTagCompound()
	{
		NBTTagCompound retTag = new NBTTagCompound();

		retTag.setInteger("amount", this.totalAmount);
		retTag.setInteger("maxstacks", this.maxstacks);
		retTag.setInteger("upgCapacity", this.upgCapacity);

		if (this.itemTemplate != null)
		{
			NBTTagCompound var3 = new NBTTagCompound();
			this.itemTemplate.writeToNBT(var3);
			retTag.setTag("current_item", var3);
		}
		if (this.keepLastItem)
			retTag.setBoolean("keepLastItem", this.keepLastItem);
		if (this.deleteExcess)
			retTag.setBoolean("deleteExcess", this.deleteExcess);
		if (this.alwaysProvide)
			retTag.setBoolean("alwaysProvide", this.alwaysProvide);
		return retTag;
	}

	@Override
	public void readTagCompound(NBTTagCompound tag)
	{
		this.totalAmount = tag.getInteger("amount");
		this.maxstacks = tag.getInteger("maxstacks");
		this.upgCapacity = tag.getInteger("upgCapacity");
		this.itemTemplate = tag.hasKey("current_item") ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("current_item")) : null;
		this.keepLastItem = tag.hasKey("keepLastItem") ? tag.getBoolean("keepLastItem") : false;
		this.deleteExcess = tag.hasKey("deleteExcess") ? tag.getBoolean("deleteExcess") : false;
		this.alwaysProvide = tag.hasKey("alwaysProvide") ? tag.getBoolean("alwaysProvide") : false;
    
		if (this.itemTemplate != null && this.totalAmount < 0)
		{
			this.totalAmount = 0;
			if (!this.keepLastItem)
				this.keepLastItem = true;
    
		if (!this.deleteExcess && this.totalCapacity - this.totalAmount < this.stackAmount)
		{
			this.inputStack = this.itemTemplate.copy();
			this.inputStack.stackSize = this.stackAmount - (this.totalCapacity - this.totalAmount);
			this.prevInputStack = this.inputStack.copy();
		}
	}

    
	@Override
	public int addStack(ItemStack stack)
	{
		boolean skip = stack == null || !this.sameItem(stack);
		if (this.itemTemplate == null && this.keepLastItem && stack != null)
			skip = false;

		if (skip)
			return 0;

		int deposit;

		if (this.inputStack == null)
		{
			this.inputStack = stack;
			deposit = stack.stackSize;
		}
		else
		{
			deposit = Math.min(stack.stackSize, this.stackAmount - this.inputStack.stackSize);
			this.inputStack.stackSize += deposit;
		}

		this.markDirty();

		stack.stackSize -= deposit;

		deposit = this.deleteExcess ? this.stackAmount : deposit;

		return deposit;
	}

	@Override
	public ItemStack getStack()
	{
		if (this.itemTemplate != null)
			return this.getStack(this.stackAmount);
		else
			return null;
	}

	@Override
	public ItemStack getStack(int amount)
	{
		this.markDirty();

		ItemStack retStack = null;
		if (this.itemTemplate != null)
		{
			amount = Math.min(amount, this.stackAmount);
			if (!this.alwaysProvide)
				amount = Math.min(amount, this.totalAmount);

			retStack = this.itemTemplate.copy();
			if (!this.alwaysProvide)
				this.outputStack.stackSize -= amount;
			retStack.stackSize = amount;
		}

		this.markDirty();
		return retStack;
	}

    
	@Override
	public boolean switchGhosting()
	{
		this.keepLastItem = !this.keepLastItem;
		this.markDirty();
		return this.keepLastItem;
	}

	@Override
	public boolean isGhosting()
	{
		return this.keepLastItem;
	}

	@Override
	public void setGhosting(boolean locked)
	{
		this.keepLastItem = locked;
		if (!locked && this.totalAmount <= 0)
			this.setItem(null);
	}

	@Override
	public boolean isVoid()
	{
		return this.deleteExcess;
	}

	@Override
	public void setVoid(boolean delete)
	{
		this.deleteExcess = delete;
	}

	@Override
	public boolean isCreative()
	{
		return this.alwaysProvide;
	}

	@Override
	public void setCreative(boolean infinite)
	{
		this.alwaysProvide = infinite;
	}

    
	@Override
	public int getAmount()
	{
		return this.totalAmount;
	}

	@Override
	public void setAmount(int amount)
	{
		this.totalAmount = amount;
	}

	protected void recalcCapacities()
	{
		this.maxstacks = this.basestacks * (this.upgCapacity + 1);
		this.totalCapacity = this.maxstacks * this.stackAmount;
	}

	@Override
	public void setBaseStacks(int basestacks)
	{
		this.basestacks = basestacks;
		this.recalcCapacities();
	}

	@Override
	public int getMaxStacks()
	{
		return this.maxstacks;
	}

	@Override
	public void addStorageUpgrade()
	{
		this.upgCapacity += 1;
		this.recalcCapacities();
	}

	@Override
	public void rmStorageUpgrade()
	{
		this.upgCapacity -= 1;
		this.recalcCapacities();
    
	private static final int[] accessibleSides = new int[] { 0, 1 };

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return accessibleSides;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side)
	{
		if (slot == 1)
			return false;
		if (this.getFreeSpace() <= 0)
			return false;
		return this.sameItem(itemstack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side)
	{
		if (slot == 0)
			return false;
		if (!this.hasItem())
			return false;
		if (itemstack == null)
			return true;
    
    
	@Override
	public int getSizeInventory()
	{
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		this.markDirty();
		return this.getStackFromSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int quantity)
	{
		if (slot == 0)
			throw new RuntimeException("[JABBA] Tried to decr the stack size of the input slot");

		ItemStack stack = this.outputStack.copy();
		int stackSize = Math.min(quantity, stack.stackSize);
		stack.stackSize = stackSize;
		this.outputStack.stackSize -= stackSize;

		this.markDirty();
		return stack;
	}

	@Override
	public ItemStack decrStackSize_Hopper(int slot, int quantity)
	{
		if (slot == 0)
			throw new RuntimeException("[JABBA] Tried to decr the stack size of the input slot");

		ItemStack stack = this.outputStack.copy();
		int stackSize = Math.min(quantity, stack.stackSize);
		stack.stackSize = stackSize;
    
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return this.getStackFromSlot(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		if (slot == 0)
			this.inputStack = itemstack;
		else
			this.outputStack = itemstack;

		this.markDirty();
	}

	@Override
	public String getInventoryName()
	{
		return "jabba.localstorage";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		if (BetterBarrels.exposeFullStorageSize)
			return this.totalCapacity;
		return 64;
	}

	@Override
	public void markDirty()
    
    
		if (this.inputStack != null)
		{
			if (this.itemTemplate == null)
				this.setItem(this.inputStack);

			if (this.totalCapacity - this.totalAmount > 0)
			{
				if (this.prevInputStack == null)
					this.totalAmount += this.inputStack.stackSize;
				else
    
				if (this.totalAmount > this.totalCapacity)
					this.totalAmount = this.totalCapacity;
			}
			if (this.deleteExcess || this.totalCapacity - this.totalAmount >= this.stackAmount)
    
				this.inputStack = null;
				this.prevInputStack = null;
			}
			else
    
    
				this.inputStack.stackSize = this.stackAmount - (this.totalCapacity - this.totalAmount);
				this.prevInputStack = this.inputStack.copy();
			}
    
		if (!this.alwaysProvide && this.prevOutputStack != null)
		{
			if (this.outputStack != null)
				this.totalAmount -= this.prevOutputStack.stackSize - this.outputStack.stackSize;
			else
    
			if (this.totalAmount < 0)
				this.totalAmount = 0;
    
		if (this.totalAmount == 0 && !this.keepLastItem)
		{
			this.setItem(null);
			this.outputStack = null;
			this.prevOutputStack = null;
			this.inputStack = null;
			this.prevInputStack = null;
		}
		else if (this.itemTemplate != null)
    
			if (this.outputStack == null)
    
			this.outputStack.stackSize = this.alwaysProvide ? this.totalCapacity : this.totalAmount;
			if (!BetterBarrels.exposeFullStorageSize)
    
			this.prevOutputStack = this.outputStack.copy();
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
    

	@Override
	public void openInventory()
	{
    

	@Override
	public void closeInventory()
	{
    

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack)
	{
		return this.sameItem(itemstack);
    
	@Override
	public ItemStack getStoredItemType()
	{
		if (this.itemTemplate != null)
		{
			ItemStack stack = this.itemTemplate.copy();
			stack.stackSize = this.alwaysProvide ? this.totalCapacity : this.totalAmount;
			return stack;
		}
		else if (this.keepLastItem)
			return new ItemStack(Blocks.end_portal, 0);
		else
			return null;
	}

	@Override
	public void setStoredItemCount(int amount)
	{
		if (amount > this.totalCapacity)
			amount = this.totalCapacity;
		this.totalAmount = amount;
		this.markDirty();
	}

	@Override
	public void setStoredItemType(ItemStack type, int amount)
	{
		this.setItem(type);
		if (amount > this.totalCapacity)
			amount = this.totalCapacity;
		this.totalAmount = amount;
		this.markDirty();
	}

	@Override
	public int getMaxStoredCount()
	{
		return this.deleteExcess ? this.totalCapacity + this.stackAmount : this.totalCapacity;
	}
}
