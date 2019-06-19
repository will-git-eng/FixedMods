package tconstruct.armor;

import ru.will.git.tconstruct.EventConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import tconstruct.armor.player.ArmorExtended;
import tconstruct.armor.player.TPlayerStats;

public class PlayerAbilityHelper
{

	public static void toggleGoggles(EntityPlayer player, boolean active)
	{
		TPlayerStats stats = TPlayerStats.get(player);
		stats.activeGoggles = active;
		if (!stats.activeGoggles)
			player.removePotionEffect(Potion.nightVision.id);
		else
			player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 15 * 20, 0, true));
	}

	public static void swapBelt(EntityPlayer player, ArmorExtended armor)
	{
		NBTTagList slots = new NBTTagList();
		InventoryPlayer hotbar = player.inventory;
		ItemStack belt = armor.inventory[3];
		if (belt == null)
    
    

		for (int slot = 0; slot < 9; ++slot)
		{
			ItemStack stack = hotbar.mainInventory[slot];
			if (stack != null)
    
				if (stack.stackSize <= 0)
					continue;
				if (EventConfig.inList(EventConfig.travelBeltBlackList, stack))
				{
					skippedSlots[slot] = true;
					continue;
    

				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte) slot);
				stack.writeToNBT(itemTag);
				slots.appendTag(itemTag);
			}
			hotbar.mainInventory[slot] = null;
		}

		NBTTagList replaceSlots = belt.getTagCompound().getTagList("Inventory", 10);
		for (int i = 0; i < replaceSlots.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound = replaceSlots.getCompoundTagAt(i);
			int slot = nbttagcompound.getByte("Slot") & 255;
			if (slot >= 0 && slot < hotbar.mainInventory.length)
    
				if (slot < skippedSlots.length && skippedSlots[slot])
    

				ItemStack stack = ItemStack.loadItemStackFromNBT(nbttagcompound);
				if (stack != null)
					hotbar.mainInventory[slot] = stack;
			}
		}
    
		if (player.inventoryContainer != null)
			player.inventoryContainer.detectAndSendChanges();
		if (player.openContainer != null)
    
	}

	public static void setEntitySize(Entity entity, float width, float height)
	{
		float f2;

		if (width != entity.width || height != entity.height)
		{
			f2 = entity.width;
			entity.width = width;
			entity.height = height;
			entity.boundingBox.maxX = entity.boundingBox.minX + entity.width;
			entity.boundingBox.maxZ = entity.boundingBox.minZ + entity.width;
			entity.boundingBox.maxY = entity.boundingBox.minY + entity.height;

			if (entity.width > f2 && !entity.worldObj.isRemote)
				entity.moveEntity(f2 - entity.width, 0.0D, f2 - entity.width);
		}

		f2 = width % 2.0F;

		if (f2 < 0.375D)
			entity.myEntitySize = Entity.EnumEntitySize.SIZE_1;
		else if (f2 < 0.75D)
			entity.myEntitySize = Entity.EnumEntitySize.SIZE_2;
		else if (f2 < 1.0D)
			entity.myEntitySize = Entity.EnumEntitySize.SIZE_3;
		else if (f2 < 1.375D)
			entity.myEntitySize = Entity.EnumEntitySize.SIZE_4;
		else if (f2 < 1.75D)
			entity.myEntitySize = Entity.EnumEntitySize.SIZE_5;
		else
			entity.myEntitySize = Entity.EnumEntitySize.SIZE_6;
	}
}
