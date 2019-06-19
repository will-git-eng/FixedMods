package ic2.core.recipe;

import ic2.api.item.ICustomDamageItem;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import ic2.core.util.StackUtil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class GradualRecipe implements IRecipe
{
	public ICustomDamageItem item;
	public ItemStack chargeMaterial;
	public int amount;
	public boolean hidden;
	private ResourceLocation name;

	public static void addAndRegister(ItemStack itemToFill, int amount, Object... args)
	{
		try
		{
			if (itemToFill == null)
				AdvRecipe.displayError("Null item to fill", null, null, true);
			else
			{
				if (!(itemToFill.getItem() instanceof ICustomDamageItem))
					AdvRecipe.displayError("Filling item must extends ItemGradualInt", null, itemToFill, true);

				ICustomDamageItem fillingItem = (ICustomDamageItem) itemToFill.getItem();
				Boolean hidden = Boolean.FALSE;
				ItemStack filler = null;
				int var7 = args.length;
				int var8 = 0;

				while (true)
				{
					label61:
					{
						if (var8 < var7)
						{
							Object o = args[var8];
							if (o instanceof Boolean)
							{
								hidden = (Boolean) o;
								break label61;
							}

							try
							{
								filler = AdvRecipe.getRecipeObject(o).getInputs().get(0);
							}
							catch (IndexOutOfBoundsException var11)
							{
								AdvRecipe.displayError("Invalid filler item: " + o, null, itemToFill, true);
								break label61;
							}
							catch (Exception var12)
							{
								var12.printStackTrace();
								AdvRecipe.displayError("unknown type", "O: " + o + "\nT: " + o.getClass().getName(), itemToFill, true);
								break label61;
							}
						}

						Rezepte.registerRecipe(new GradualRecipe(fillingItem, filler, amount, hidden));
						break;
					}

					++var8;
				}
			}
		}
		catch (RuntimeException var13)
		{
			if (!MainConfig.ignoreInvalidRecipes)
				throw var13;
		}

	}

	public GradualRecipe(ICustomDamageItem item, ItemStack chargeMaterial, int amount)
	{
		this(item, chargeMaterial, amount, false);
	}

	public GradualRecipe(ICustomDamageItem item, ItemStack chargeMaterial, int amount, boolean hidden)
	{
		this.item = item;
		this.chargeMaterial = chargeMaterial;
		this.amount = amount;
		this.hidden = hidden;
	}

	@Override
	public boolean matches(InventoryCrafting ic, World world)
	{
		return this.getCraftingResult(ic) != StackUtil.emptyStack;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting ic)
	{
		ItemStack gridItem = null;
		int chargeMats = 0;

		for (int slot = 0; slot < ic.getSizeInventory(); ++slot)
		{
			ItemStack stack = ic.getStackInSlot(slot);
			if (!StackUtil.isEmpty(stack))
				if (gridItem == null && stack.getItem() == this.item)
					gridItem = stack;
				else
				{
					if (!StackUtil.checkItemEquality(stack, this.chargeMaterial))
						return StackUtil.emptyStack;

					++chargeMats;
				}
		}

		if (gridItem != null && chargeMats > 0)
		{
			
			// ItemStack stack = gridItem.copy();
			ItemStack stack = StackUtil.copyWithSize(gridItem, 1);
			

			int damage = this.item.getCustomDamage(stack) - this.amount * chargeMats;
			if (damage > this.item.getMaxCustomDamage(stack))
				damage = this.item.getMaxCustomDamage(stack);
			else if (damage < 0)
				damage = 0;

			this.item.setCustomDamage(stack, damage);
			return stack;
		}
		return StackUtil.emptyStack;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack((Item) this.item);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}

	public boolean canShow()
	{
		return AdvRecipe.canShow(new Object[] { this.chargeMaterial }, this.getRecipeOutput(), this.hidden);
	}

	@Override
	public IRecipe setRegistryName(ResourceLocation name)
	{
		this.name = name;
		return this;
	}

	@Override
	public ResourceLocation getRegistryName()
	{
		return this.name;
	}

	@Override
	public Class<IRecipe> getRegistryType()
	{
		return IRecipe.class;
	}

	@Override
	public boolean canFit(int x, int y)
	{
		return x * y >= 2;
	}
}
