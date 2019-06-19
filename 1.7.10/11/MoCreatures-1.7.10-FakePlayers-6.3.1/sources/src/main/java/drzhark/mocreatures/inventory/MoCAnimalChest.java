package drzhark.mocreatures.inventory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class MoCAnimalChest extends InventoryBasic
{
	public MoCAnimalChest(String name, int size)
	{
		super(name, true, size);
    
	public Entity entity;

	public MoCAnimalChest(Entity entity, String name, int size)
	{
		super(name, true, size);
		this.entity = entity;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return (this.entity == null || this.entity.isEntityAlive()) && super.isUseableByPlayer(player);
    

	public void loadInventoryFromNBT(NBTTagList par1NBTTagList)
	{
		for (int var2 = 0; var2 < this.getSizeInventory(); ++var2)
			this.setInventorySlotContents(var2, (ItemStack) null);

		for (int var5 = 0; var5 < par1NBTTagList.tagCount(); ++var5)
		{
			NBTTagCompound var3 = par1NBTTagList.getCompoundTagAt(var5);
			int var4 = var3.getByte("Slot") & 255;
			if (var4 >= 0 && var4 < this.getSizeInventory())
				this.setInventorySlotContents(var4, ItemStack.loadItemStackFromNBT(var3));
		}

	}

	public NBTTagList saveInventoryToNBT()
	{
		NBTTagList var1 = new NBTTagList();

		for (int var2 = 0; var2 < this.getSizeInventory(); ++var2)
		{
			ItemStack var3 = this.getStackInSlot(var2);
			if (var3 != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) var2);
				var3.writeToNBT(var4);
				var1.appendTag(var4);
			}
		}

		return var1;
	}
}
