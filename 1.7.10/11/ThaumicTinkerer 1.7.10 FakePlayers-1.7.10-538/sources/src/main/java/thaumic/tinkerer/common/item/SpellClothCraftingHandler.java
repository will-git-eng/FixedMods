package thaumic.tinkerer.common.item;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumic.tinkerer.api.INoRemoveEnchant;
import thaumic.tinkerer.common.ThaumicTinkerer;

    
public class SpellClothCraftingHandler
{

	@SubscribeEvent
	public void ItemCrafted(PlayerEvent.ItemCraftedEvent event)
	{
		boolean foundCloth = false;
		boolean foundEnchanted = false;
		ItemStack cloth = null;
		int slotEnchanted = 0;

		for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++)
		{
			ItemStack stack = event.craftMatrix.getStackInSlot(i);
			if (stack != null)
			{
				Item item = ThaumicTinkerer.registry.getFirstItemFromClass(ItemSpellCloth.class);
				if (stack.isItemEnchanted() && !(stack.getItem() instanceof INoRemoveEnchant) && !foundEnchanted)
				{
					foundEnchanted = true;
					slotEnchanted = i;
				}
				else if (stack.getItem() == item && !foundCloth)
				{
					foundCloth = true;
					cloth = stack;
				}
				else
				{
					return;
				}
			}
		}
		if (foundCloth && foundEnchanted)
		{
    
		}
	}
}
