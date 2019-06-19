package powercrystals.minefactoryreloaded.item.gun;

import ru.will.git.minefactoryreloaded.EventConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.MineFactoryReloadedClient;
import powercrystals.minefactoryreloaded.entity.EntityRocket;
import powercrystals.minefactoryreloaded.item.base.ItemFactoryGun;
import powercrystals.minefactoryreloaded.net.Packets;
import powercrystals.minefactoryreloaded.setup.MFRThings;

public class ItemRocketLauncher extends ItemFactoryGun
{
	@Override
	protected boolean hasGUI(ItemStack var1)
	{
		return false;
	}

	@Override
	protected boolean fire(ItemStack stack, World world, EntityPlayer player)
    
		if (!EventConfig.enableRocket)
			return false;
		if (EventConfig.fixInfiniteRocket)
    

		int rocketSlot = -1;
		Item rocketItem = MFRThings.rocketItem;
		ItemStack[] inventory = player.inventory.mainInventory;

		for (int slot = 0, size = inventory.length; slot < size; ++slot)
		{
			if (inventory[slot] != null && inventory[slot].getItem() == rocketItem)
			{
				rocketSlot = slot;
				break;
			}
		}

		if (rocketSlot > 0)
		{
			int meta = inventory[rocketSlot].getItemDamage();
			if (!player.capabilities.isCreativeMode && --inventory[rocketSlot].stackSize <= 0)
				inventory[rocketSlot] = null;

			if (world.isRemote)
				Packets.sendToServer((short) 11, player, meta == 0 ? MineFactoryReloadedClient.instance.getLockedEntity() : Integer.MIN_VALUE);
			else if (!player.addedToChunk)
			{
				EntityRocket entityRocket = new EntityRocket(world, player, null);
				world.spawnEntityInWorld(entityRocket);
			}

			return true;
		}
		return false;
	}

	@Override
	protected int getDelay(ItemStack var1, boolean var2)
	{
		return var2 ? 100 : 40;
	}

	@Override
	protected String getDelayTag(ItemStack var1)
	{
		return "mfr:SPAMRLaunched";
	}
}
