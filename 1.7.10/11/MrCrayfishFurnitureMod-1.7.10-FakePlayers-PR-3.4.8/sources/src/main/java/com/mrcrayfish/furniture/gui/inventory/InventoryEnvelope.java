package com.mrcrayfish.furniture.gui.inventory;

import java.util.UUID;

import com.mrcrayfish.furniture.MrCrayfishFurnitureMod;
import com.mrcrayfish.furniture.items.IMail;
import com.mrcrayfish.furniture.util.NBTHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryEnvelope extends InventoryBasic
{
	protected EntityPlayer playerEntity;
	protected static ItemStack envelope;
	protected boolean reading = false;
	protected String uniqueID = "";

	public InventoryEnvelope(EntityPlayer player, ItemStack envelope)
	{
		super("Envelope", false, getInventorySize());
		this.playerEntity = player;
		InventoryEnvelope.envelope = envelope;
		if (!this.hasInventory())
		{
			this.uniqueID = UUID.randomUUID().toString();
			this.createInventory();
		}

		this.loadInventory();
    
	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		ItemStack current = player.getCurrentEquippedItem();
		if (current == null || current.getItem() == null || !current.hasTagCompound())
			return false;

		NBTTagCompound nbt = current.getTagCompound().getCompoundTag("Envelope");
		String id = nbt.getString("UniqueID");
		if (!id.equals(this.uniqueID))
			return false;

		return super.isUseableByPlayer(player);
    

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (!this.reading)
			this.saveInventory();

	}

	public static boolean isSigned()
	{
		boolean isValid = false;
		if (envelope.getItem() == MrCrayfishFurnitureMod.itemEnvelopeSigned)
			isValid = true;

		return isValid;
	}

	@Override
	public void openInventory()
	{
		this.loadInventory();
	}

	@Override
	public void closeInventory()
	{
		this.saveInventory();
	}

	protected static int getInventorySize()
	{
		return 6;
	}

	protected boolean hasInventory()
	{
		return NBTHelper.hasTag(envelope, "Envelope");
	}

	protected void createInventory()
	{
		this.writeToNBT();
	}

	protected void setNBT()
	{
		for (ItemStack itemStack : this.playerEntity.inventory.mainInventory)
			if (itemStack != null && itemStack.getItem() instanceof IMail)
			{
				NBTTagCompound nbt = itemStack.getTagCompound();
				if (nbt != null && nbt.getCompoundTag("Envelope").getString("UniqueID") == this.uniqueID)
				{
					itemStack.setTagCompound(envelope.getTagCompound());
					break;
				}
			}

	}

	public void loadInventory()
	{
		this.readFromNBT();
	}

	public void saveInventory()
	{
		this.writeToNBT();
		this.setNBT();
	}

	public String getSender()
	{
		return NBTHelper.getString(envelope, "Author");
	}

	protected void readFromNBT()
	{
		this.reading = true;
		NBTTagCompound nbt = NBTHelper.getCompoundTag(envelope, "Envelope");
		if ("".equals(this.uniqueID))
		{
			this.uniqueID = nbt.getString("UniqueID");
			if ("".equals(this.uniqueID))
				this.uniqueID = UUID.randomUUID().toString();
		}

		NBTTagList itemList = (NBTTagList) NBTHelper.getCompoundTag(envelope, "Envelope").getTag("Items");

		for (int i = 0; i < itemList.tagCount(); ++i)
		{
			NBTTagCompound slotEntry = itemList.getCompoundTagAt(i);
			int j = slotEntry.getByte("Slot") & 255;
			if (j >= 0 && j < this.getSizeInventory())
				this.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(slotEntry));
		}

		this.reading = false;
	}

	protected void writeToNBT()
	{
		NBTTagList itemList = new NBTTagList();

		for (int i = 0; i < this.getSizeInventory(); ++i)
			if (this.getStackInSlot(i) != null)
			{
				NBTTagCompound slotEntry = new NBTTagCompound();
				slotEntry.setByte("Slot", (byte) i);
				this.getStackInSlot(i).writeToNBT(slotEntry);
				itemList.appendTag(slotEntry);
			}

		NBTTagCompound inventory = new NBTTagCompound();
		inventory.setTag("Items", itemList);
		inventory.setString("UniqueID", this.uniqueID);
		NBTHelper.setCompoundTag(envelope, "Envelope", inventory);
	}
}
