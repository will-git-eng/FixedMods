package net.machinemuse.powersuits.powermodule.tool;

import java.util.List;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import net.machinemuse.api.IModularItem;
import net.machinemuse.api.ModuleManager;
import net.machinemuse.api.moduletrigger.IRightClickModule;
import net.machinemuse.powersuits.item.ItemComponent;
import net.machinemuse.powersuits.powermodule.PowerModuleBase;
import net.machinemuse.utils.ElectricItemUtils;
import net.machinemuse.utils.MuseCommonStrings;
import net.machinemuse.utils.MuseItemUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

    
public class FlintAndSteelModule extends PowerModuleBase implements IRightClickModule
{

	public static final String MODULE_FLINT_AND_STEEL = "Flint and Steel";
	public static final String IGNITION_ENERGY_CONSUMPTION = "Ignition Energy Consumption";
	public ItemStack fas = new ItemStack(Items.flint_and_steel);
	Random ran = new Random();

	public FlintAndSteelModule(List<IModularItem> validItems)
	{
		super(validItems);
		this.addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.servoMotor, 1));
		this.addInstallCost(this.fas);
		this.addBaseProperty(IGNITION_ENERGY_CONSUMPTION, 1000, "J");
	}

	@Override
	public String getTextureFile()
	{
		return null;
	}

	@Override
	public IIcon getIcon(ItemStack item)
	{
		return this.fas.getIconIndex();
	}

	@Override
	public String getCategory()
	{
		return MuseCommonStrings.CATEGORY_TOOL;
	}

	@Override
	public String getDataName()
	{
		return MODULE_FLINT_AND_STEEL;
	}

	@Override
	public String getUnlocalizedName()
	{
		return "flintAndSteel";
	}

	@Override
	public String getDescription()
	{
		return "A portable igniter that creates fire through the power of energy.";
	}

	@Override
	public void onRightClick(EntityPlayer player, World world, ItemStack item)
	{
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		double energyConsumption = ModuleManager.computeModularProperty(itemStack, IGNITION_ENERGY_CONSUMPTION);
		if (energyConsumption < ElectricItemUtils.getPlayerEnergy(player))
		{
			x += side == 5 ? 1 : side == 4 ? -1 : 0;
			y += side == 1 ? 1 : side == 0 ? -1 : 0;
			z += side == 3 ? 1 : side == 2 ? -1 : 0;

			if (player.canPlayerEdit(x, y, z, side, itemStack))
			{
				Block clickedBlock = world.getBlock(x, y, z);

				if (clickedBlock == Blocks.air)
    
					if (EventUtils.cantBreak(player, x, y, z))
    

					ElectricItemUtils.drainPlayerEnergy(player, energyConsumption);
					world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "fire.ignite", 1.0F, this.ran.nextFloat() * 0.4F + 0.8F);
					world.setBlock(x, y, z, Blocks.fire);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack itemStack, World world, EntityPlayer player, int par4)
	{
	}
}
