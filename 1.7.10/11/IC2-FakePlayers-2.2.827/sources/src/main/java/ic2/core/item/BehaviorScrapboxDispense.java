package ic2.core.item;

import ru.will.git.ic2.EventConfig;

import ic2.api.recipe.Recipes;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class BehaviorScrapboxDispense extends BehaviorDefaultDispenseItem
{
	@Override
	protected ItemStack dispenseStack(IBlockSource blockSource, ItemStack stack)
    
		if (!EventConfig.scrapboxDropEnabled)
    

		EnumFacing facing = EnumFacing.getFront(blockSource.getBlockMetadata());
		IPosition position = BlockDispenser.func_149939_a(blockSource);
		doDispense(blockSource.getWorld(), Recipes.scrapboxDrops.getDrop(stack, true), 6, facing, position);
		return stack;
	}
}
