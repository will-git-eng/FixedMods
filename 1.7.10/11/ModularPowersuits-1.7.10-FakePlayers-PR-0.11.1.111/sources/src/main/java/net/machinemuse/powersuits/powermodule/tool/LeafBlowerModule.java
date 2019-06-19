package net.machinemuse.powersuits.powermodule.tool;

import java.util.List;
import java.util.Objects;

import ru.will.git.reflectionmedic.util.EventUtils;

import net.machinemuse.api.IModularItem;
import net.machinemuse.api.ModuleManager;
import net.machinemuse.api.moduletrigger.IRightClickModule;
import net.machinemuse.powersuits.item.ItemComponent;
import net.machinemuse.powersuits.powermodule.PowerModuleBase;
import net.machinemuse.powersuits.powermodule.PropertyModifierIntLinearAdditive;
import net.machinemuse.utils.ElectricItemUtils;
import net.machinemuse.utils.MuseCommonStrings;
import net.machinemuse.utils.MuseItemUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSnow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

    
public class LeafBlowerModule extends PowerModuleBase implements IRightClickModule
{
	private static final String MODULE_LEAF_BLOWER = "Leaf Blower";
	private static final String LEAF_BLOWER_ENERGY_CONSUMPTION = "Energy Consumption";
	private static final String PLANT_RADIUS = "Plant Radius";
	private static final String LEAF_RADIUS = "Leaf Radius";
	private static final String SNOW_RADIUS = "Snow Radius";

	public LeafBlowerModule(List<IModularItem> validItems)
	{
		super(validItems);
		this.addInstallCost(new ItemStack(Items.iron_ingot, 3));
		this.addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.solenoid, 1));
		this.addBaseProperty(LEAF_BLOWER_ENERGY_CONSUMPTION, 100, "J");
		this.addBaseProperty(PLANT_RADIUS, 1, "m");
		this.addBaseProperty(LEAF_RADIUS, 1, "m");
		this.addBaseProperty(SNOW_RADIUS, 1, "m");
		this.addIntTradeoffProperty(PLANT_RADIUS, PLANT_RADIUS, 8, "m", 1, 0);
		this.addIntTradeoffProperty(LEAF_RADIUS, LEAF_RADIUS, 8, "m", 1, 0);
		this.addIntTradeoffProperty(SNOW_RADIUS, SNOW_RADIUS, 5, "m", 1, 0);
	}

	public PowerModuleBase addIntTradeoffProperty(String tradeoffName, String propertyName, double multiplier, String unit, int roundTo, int offset)
	{
		units.put(propertyName, unit);
		return this.addPropertyModifier(propertyName, new PropertyModifierIntLinearAdditive(tradeoffName, multiplier, roundTo, offset));
	}

	@Override
	public String getCategory()
	{
		return MuseCommonStrings.CATEGORY_TOOL;
	}

	@Override
	public String getDataName()
	{
		return MODULE_LEAF_BLOWER;
	}

	@Override
	public String getUnlocalizedName()
	{
		return "leafBlower";
	}

	@Override
	public String getDescription()
	{
		return "Create a torrent of air to knock plants out of the ground and leaves off of trees.";
	}

	@Override
	public String getTextureFile()
	{
		return "leafblower";
	}

	@Override
	public void onRightClick(EntityPlayer player, World world, ItemStack item)
	{
	}

	private boolean blockCheckAndHarvest(String blocktype, EntityPlayer player, World world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		if (block == null)
    
		if (Objects.equals(blocktype, "plants") && block instanceof BlockBush && block.canHarvestBlock(player, meta))
    
			if (EventUtils.cantBreak(player, x, y, z))
    

			block.harvestBlock(world, player, x, y, z, meta);
			world.setBlockToAir(x, y, z);
			return true;
    
		if (Objects.equals(blocktype, "leaves") && block instanceof BlockLeaves && block.canHarvestBlock(player, meta))
    
			if (EventUtils.cantBreak(player, x, y, z))
    

			block.harvestBlock(world, player, x, y, z, meta);
			world.setBlockToAir(x, y, z);
			return true;
    
		if (Objects.equals(blocktype, "snow") && block instanceof BlockSnow && block.canHarvestBlock(player, meta))
    
			if (EventUtils.cantBreak(player, x, y, z))
    

			block.harvestBlock(world, player, x, y, z, meta);
			world.setBlockToAir(x, y, z);
		}
		return false;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		Block blockID = world.getBlock(x, y, z);
		int plant = (int) ModuleManager.computeModularProperty(itemStack, PLANT_RADIUS);
		int leaf = (int) ModuleManager.computeModularProperty(itemStack, LEAF_RADIUS);
		int snow = (int) ModuleManager.computeModularProperty(itemStack, SNOW_RADIUS);
    
    
    
		this.useBlower(snow, "snow", itemStack, player, world, x, y, z);
		return false;
	}

	private void useBlower(int radius, String blocktypename, ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z)
	{
		int totalEnergyDrain = 0;
		for (int i = -radius; i < radius; i++)
			for (int j = -radius; j < radius; j++)
				for (int k = -radius; k < radius; k++)
					if (this.blockCheckAndHarvest(blocktypename, player, world, x + i, y + j, z + k))
						totalEnergyDrain += ModuleManager.computeModularProperty(itemStack, LEAF_BLOWER_ENERGY_CONSUMPTION);
		ElectricItemUtils.drainPlayerEnergy(player, totalEnergyDrain);
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
