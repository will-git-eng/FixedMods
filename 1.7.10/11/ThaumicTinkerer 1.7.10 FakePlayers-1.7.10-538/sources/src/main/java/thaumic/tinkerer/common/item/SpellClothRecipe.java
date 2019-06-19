    
package thaumic.tinkerer.common.item;

import ru.will.git.ttinkerer.EventConfig;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumic.tinkerer.api.INoRemoveEnchant;

public class SpellClothRecipe implements IRecipe
{
	Item item;

	public SpellClothRecipe(Item item)
	{
		this.item = item;
	}

	@Override
	public boolean matches(InventoryCrafting var1, World var2)
	{
		boolean foundCloth = false;
		boolean foundEnchanted = false;
		for (int i = 0; i < var1.getSizeInventory(); i++)
		{
			ItemStack stack = var1.getStackInSlot(i);
			if (stack != null)
			{
				Item item = stack.getItem();
    
				if (item instanceof ItemSpellCloth && (foundCloth || itemEnchanted))
    

				if (itemEnchanted && !(item instanceof INoRemoveEnchant) && !foundEnchanted)
    
					if (EventConfig.disableClothRecipeStack && stack.stackSize != 1)
						return false;
					if (item.hasContainerItem(stack))
    

					foundEnchanted = true;
				}
				else if (item == this.item && !foundCloth)
					foundCloth = true;
				else
    
			}
		}

		return foundCloth && foundEnchanted;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		ItemStack stackToDisenchant = null;
		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && stack.isItemEnchanted())
			{
    
    

				break;
			}
		}

		if (stackToDisenchant == null)
			return null;

		NBTTagCompound cmp = (NBTTagCompound) stackToDisenchant.getTagCompound().copy();
    
		stackToDisenchant.setTagCompound(cmp);

		return stackToDisenchant;
	}

	@Override
	public int getRecipeSize()
	{
		return 10;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return null;
	}
}