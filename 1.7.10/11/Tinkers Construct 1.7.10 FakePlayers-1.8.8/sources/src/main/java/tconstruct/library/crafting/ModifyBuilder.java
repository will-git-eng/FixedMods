package tconstruct.library.crafting;

import ru.will.git.tconstruct.EventConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import tconstruct.library.event.ModifyEvent;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.tools.Weapon;
import tconstruct.library.weaponry.IAmmo;
import tconstruct.library.weaponry.ProjectileWeapon;
import tconstruct.modifiers.tools.ModCreativeToolModifier;

import java.util.ArrayList;
import java.util.List;

public class ModifyBuilder
{
	public static ModifyBuilder instance = new ModifyBuilder();
	public List<ItemModifier> itemModifiers = new ArrayList<ItemModifier>();

	public ItemStack modifyItem(ItemStack input, ItemStack[] modifiers)
	{
    
		if (copy.getItem() instanceof IModifyable)
		{
			IModifyable item = (IModifyable) copy.getItem();

			boolean built = false;
			for (ItemModifier mod : this.itemModifiers)
			{
				if (mod.matches(modifiers, copy) && mod.validType(item))
    
					if (EventConfig.maxCreativeMods >= 0 && copy.hasTagCompound() && mod instanceof ModCreativeToolModifier)
					{
						NBTTagCompound nbt = copy.getTagCompound().getCompoundTag(item.getBaseTagName());
						int count = nbt.getInteger(ModCreativeToolModifier.NBT_CREATIVE_MOD_COUNT);
						if (count >= EventConfig.maxCreativeMods)
							return null;
    

					ModifyEvent event = new ModifyEvent(mod, item, copy);
					MinecraftForge.EVENT_BUS.post(event);
					if (event.isCanceled())
						continue;

					built = true;
    
					mod.modify(modifiers, copy);

    
					if (EventConfig.maxWeaponMods >= 0 && mods > EventConfig.maxWeaponMods)
						if (item instanceof Weapon || item instanceof IAmmo || item instanceof ProjectileWeapon)
    
    
					if (mods < 0)
						return null;
				}
			}
			if (built)
				return copy;
		}
		return null;
	}

	public static void registerModifier(ItemModifier mod)
	{
		if (mod == null)
			throw new NullPointerException("Modifier cannot be null.");
		instance.itemModifiers.add(mod);
	}
}
