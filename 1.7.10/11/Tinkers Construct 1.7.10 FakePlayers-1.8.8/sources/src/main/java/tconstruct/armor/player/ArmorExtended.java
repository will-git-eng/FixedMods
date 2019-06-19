package tconstruct.armor.player;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import tconstruct.library.accessory.IHealthAccessory;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class ArmorExtended implements IInventory
{
	public ItemStack[] inventory = new ItemStack[7];
	public WeakReference<EntityPlayer> parent;
	public UUID globalID = UUID.fromString("B243BE32-DC1B-4C53-8D13-8752D5C69D5B");

	public void init(EntityPlayer player)
	{
		this.parent = new WeakReference<EntityPlayer>(player);
	}

	@Override
	public int getSizeInventory()
	{
		return this.inventory.length;
	}

	public boolean isStackInSlot(int slot)
	{
		return this.inventory[slot] != null;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return this.inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int quantity)
	{
		if (this.inventory[slot] != null)
    
			if (this.inventory[slot].stackSize <= quantity)
			{
				ItemStack stack = this.inventory[slot];
				this.inventory[slot] = null;
				return stack;
			}
			ItemStack split = this.inventory[slot].splitStack(quantity);
			if (this.inventory[slot].stackSize == 0)
				this.inventory[slot] = null;
			EntityPlayer player = this.parent.get();
			TPlayerStats stats = TPlayerStats.get(player);
			this.recalculateHealth(player, stats);
			return split;
		}
		else
			return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
    
    
		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
			itemstack.stackSize = this.getInventoryStackLimit();

		EntityPlayer player = this.parent.get();
		TPlayerStats stats = TPlayerStats.get(player);
		this.recalculateHealth(player, stats);
	}

	@Override
	public String getInventoryName()
	{
		return "";
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
	public void markDirty()
	{
		EntityPlayer player = this.parent.get();
    
		this.recalculateHealth(player, stats);

    
	}

	public void recalculateHealth(EntityPlayer player, TPlayerStats stats)
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();

		if (this.inventory[4] != null || this.inventory[5] != null || this.inventory[6] != null)
		{
			int bonusHP = 0;
			for (int i = 4; i < 7; i++)
			{
				ItemStack stack = this.inventory[i];
				if (stack != null && stack.getItem() instanceof IHealthAccessory)
					bonusHP += ((IHealthAccessory) stack.getItem()).getHealthBoost(stack);
			}
			int prevHealth = stats.bonusHealth;
			stats.bonusHealth = bonusHP;

			int healthChange = bonusHP - prevHealth;
			if (healthChange != 0)
			{
				IAttributeInstance attributeinstance = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.maxHealth);
				try
				{
					attributeinstance.removeModifier(attributeinstance.getModifier(this.globalID));
				}
				catch (Exception ignored)
				{
				}
				attributeinstance.applyModifier(new AttributeModifier(this.globalID, "tconstruct.heartCanister", bonusHP, 0));

			}
		}
		else if (this.parent != null && this.parent.get() != null)
		{
			int prevHealth = stats.bonusHealth;
			int bonusHP = 0;
			stats.bonusHealth = bonusHP;
			int healthChange = bonusHP - prevHealth;
			if (healthChange != 0)
			{
				IAttributeInstance attributeinstance = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.maxHealth);
				try
				{
					attributeinstance.removeModifier(attributeinstance.getModifier(this.globalID));
				}
				catch (Exception ignored)
				{
				}
			}
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	public void openChest()
	{
	}

	public void closeChest()
	{
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack)
	{
		return false;
	}

    
	public void saveToNBT(NBTTagCompound tagCompound)
	{
		NBTTagList tagList = new NBTTagList();
		NBTTagCompound invSlot;

		for (int i = 0; i < this.inventory.length; ++i)
		{
			if (this.inventory[i] != null)
			{
				invSlot = new NBTTagCompound();
				invSlot.setByte("Slot", (byte) i);
				this.inventory[i].writeToNBT(invSlot);
				tagList.appendTag(invSlot);
			}
		}

		tagCompound.setTag("Inventory", tagList);
	}

	public void readFromNBT(NBTTagCompound tagCompound)
	{
		if (tagCompound != null)
		{
			NBTTagList tagList = tagCompound.getTagList("Inventory", 10);
			for (int i = 0; i < tagList.tagCount(); ++i)
			{
				NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
				int j = nbttagcompound.getByte("Slot") & 255;
				ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

				if (itemstack != null)
					this.inventory[j] = itemstack;
			}
		}
	}

	public void dropItems()
	{
    
		if (player == null)
    

		for (int i = 0; i < 4; ++i)
		{
			if (this.inventory[i] != null)
			{
				player.func_146097_a(this.inventory[i], true, false);
				this.inventory[i] = null;
			}
		}
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	public void writeInventoryToStream(ByteBuf os)
	{
		for (int i = 0; i < this.inventory.length; i++)
		{
			ByteBufUtils.writeItemStack(os, this.inventory[i]);
		}
	}

	public void readInventoryFromStream(ByteBuf is)
	{
		for (int i = 0; i < this.inventory.length; i++)
		{
			this.inventory[i] = ByteBufUtils.readItemStack(is);
		}
	}
}
