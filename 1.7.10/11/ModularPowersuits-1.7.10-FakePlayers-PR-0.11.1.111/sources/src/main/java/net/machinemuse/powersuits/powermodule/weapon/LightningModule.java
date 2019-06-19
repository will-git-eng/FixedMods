package net.machinemuse.powersuits.powermodule.weapon;

import java.util.List;

import ru.will.git.reflectionmedic.util.EventUtils;

import net.machinemuse.api.IModularItem;
import net.machinemuse.api.ModuleManager;
import net.machinemuse.api.moduletrigger.IRightClickModule;
import net.machinemuse.powersuits.item.ItemComponent;
import net.machinemuse.powersuits.powermodule.PowerModuleBase;
import net.machinemuse.utils.ElectricItemUtils;
import net.machinemuse.utils.MuseCommonStrings;
import net.machinemuse.utils.MuseHeatUtils;
import net.machinemuse.utils.MuseItemUtils;
import net.machinemuse.utils.MusePlayerUtils;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

    
public class LightningModule extends PowerModuleBase implements IRightClickModule
{
	public static final String MODULE_LIGHTNING = "Lightning Summoner";
	public static final String LIGHTNING_ENERGY_CONSUMPTION = "Energy Consumption";
	public static final String HEAT = "Heat Emission";

	public LightningModule(List<IModularItem> validItems)
	{
		super(validItems);
		this.addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.hvcapacitor, 1));
		this.addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.solenoid, 2));
		this.addInstallCost(MuseItemUtils.copyAndResize(ItemComponent.fieldEmitter, 2));
		this.addBaseProperty(LIGHTNING_ENERGY_CONSUMPTION, 490000, "");
		this.addBaseProperty(HEAT, 100, "");
	}

	@Override
	public String getTextureFile()
	{
		return "bluestar";
	}

	@Override
	public String getCategory()
	{
		return MuseCommonStrings.CATEGORY_WEAPON;
	}

	@Override
	public String getDataName()
	{
		return MODULE_LIGHTNING;
	}

	@Override
	public String getUnlocalizedName()
	{
		return "lightningSummoner";
	}

	@Override
	public String getDescription()
	{
		return "Allows you to summon lightning for a large energy cost.";
	}

	@Override
	public void onRightClick(EntityPlayer player, World world, ItemStack item)
	{
		try
		{
			double range = 64;
			double energyConsumption = ModuleManager.computeModularProperty(item, LIGHTNING_ENERGY_CONSUMPTION);
			if (energyConsumption < ElectricItemUtils.getPlayerEnergy(player))
			{
				ElectricItemUtils.drainPlayerEnergy(player, energyConsumption);
				MuseHeatUtils.heatPlayer(player, ModuleManager.computeModularProperty(item, HEAT));
    
				if (EventUtils.cantBreak(player, MOP.hitVec.xCoord, MOP.hitVec.yCoord, MOP.hitVec.zCoord))
    

				world.spawnEntityInWorld(new EntityLightningBolt(player.worldObj, MOP.hitVec.xCoord, MOP.hitVec.yCoord, MOP.hitVec.zCoord));

    
			}
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
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
