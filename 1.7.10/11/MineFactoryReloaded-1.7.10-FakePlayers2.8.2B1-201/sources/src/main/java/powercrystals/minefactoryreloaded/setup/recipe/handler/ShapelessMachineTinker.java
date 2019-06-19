package powercrystals.minefactoryreloaded.setup.recipe.handler;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import powercrystals.minefactoryreloaded.core.UtilInventory;
import powercrystals.minefactoryreloaded.setup.Machine;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class ShapelessMachineTinker extends ShapelessRecipes
{
	protected List<List<ItemStack>> _tinkerItems;
	protected ItemStack _machine;

	private static ItemStack createMachineWithLore(Machine var0, String var1)
	{
		ItemStack var2 = var0.getItemStack();
		NBTTagCompound var3 = new NBTTagCompound();
		var3.setTag("display", new NBTTagCompound());
		NBTTagList var4 = new NBTTagList();
		var3.getCompoundTag("display").setTag("Lore", var4);
		var4.appendTag(new NBTTagString(var1));
		var2.setTagCompound(var3);
		return var2;
	}

	private static List<ItemStack> createIngredientListforNEI(Machine var0, ItemStack... var1)
	{
		LinkedList var2 = new LinkedList();
		var2.addAll(Arrays.asList(var1));
		var2.add(var0.getItemStack());
		return var2;
	}

	public ShapelessMachineTinker(Machine var1, String var2, String... var3)
	{
		super(createMachineWithLore(var1, var2), null);
		this._machine = var1.getItemStack();
		this._tinkerItems = new LinkedList();

		for (String var7 : var3)
		{
			this._tinkerItems.add(OreDictionary.getOres(var7));
		}

		RecipeSorter.register("minefactoryreloaded:shapelessTinker", this.getClass(), Category.SHAPELESS, "after:minecraft:shapeless");
	}

	public ShapelessMachineTinker(Machine var1, String var2, ItemStack... var3)
	{
		super(createMachineWithLore(var1, var2), createIngredientListforNEI(var1, var3));
		this._machine = var1.getItemStack();
		this._tinkerItems = new LinkedList();

		for (ItemStack var7 : var3)
		{
			LinkedList var8 = new LinkedList();
			var8.add(var7);
			this._tinkerItems.add(var8);
		}

		RecipeSorter.register("minefactoryreloaded:shapelessTinker", this.getClass(), Category.SHAPELESS, "after:minecraft:shapeless");
	}

	protected abstract boolean isMachineTinkerable(ItemStack var1);

	protected abstract ItemStack getTinkeredMachine(ItemStack var1);

	@Override
	public boolean matches(InventoryCrafting inv, World var2)
	{
		int slot = inv.getSizeInventory();
		boolean var4 = false;
		LinkedList<List> var5 = new LinkedList();
		var5.addAll(this._tinkerItems);

		label36:
		while (slot-- > 0)
		{
			ItemStack stack = inv.getStackInSlot(slot);
    
				if (stack != null && stack.stackSize == 1 && UtilInventory.stacksEqual(this._machine, stack, false))
				{
					if (var4 || !this.isMachineTinkerable(stack))
						return false;

					var4 = true;
				}
				else
				{
					if (var4 && var5.isEmpty())
						return true;

					label122:
					for (List var8 : var5)
					{
						Iterator var9 = var8.iterator();

						while (true)
						{
							if (!var9.hasNext())
								continue label122;

							ItemStack var10 = (ItemStack) var9.next();
							if (UtilInventory.stacksEqual(stack, var10))
								break;
						}

						var5.remove(var8);
						continue label36;
					}

					return false;
				}
		}

		return var4 && var5.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1)
	{
		int var2 = var1.getSizeInventory();

		while (var2-- > 0)
		{
    
			if (stack != null && stack.stackSize > 1)
    

			if (UtilInventory.stacksEqual(this._machine, stack, false) && this.isMachineTinkerable(stack))
				return this.getTinkeredMachine(stack);
		}

		return null;
	}
}
