package com.brandon3055.draconicevolution.common.items.tools;

import com.brandon3055.draconicevolution.client.render.IRenderTweak;
import com.brandon3055.draconicevolution.common.ModItems;
import com.brandon3055.draconicevolution.common.handler.BalanceConfigHandler;
import com.brandon3055.draconicevolution.common.items.tools.baseclasses.MiningTool;
import com.brandon3055.draconicevolution.common.lib.References;
import com.brandon3055.draconicevolution.common.lib.Strings;
import com.brandon3055.draconicevolution.common.utills.IInventoryTool;
import com.brandon3055.draconicevolution.common.utills.IUpgradableItem;
import com.brandon3055.draconicevolution.common.utills.ItemConfigField;
import ru.will.git.draconicevolution.EventConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class DraconicPickaxe extends MiningTool implements IInventoryTool, IRenderTweak
{

	public DraconicPickaxe()
	{
		super(ModItems.AWAKENED);
		this.setHarvestLevel("pickaxe", 10);
		this.setUnlocalizedName(Strings.draconicPickaxeName);
		this.setCapacity(BalanceConfigHandler.draconicToolsBaseStorage);
		this.setMaxExtract(BalanceConfigHandler.draconicToolsMaxTransfer);
		this.setMaxReceive(BalanceConfigHandler.draconicToolsMaxTransfer);
		this.energyPerOperation = BalanceConfigHandler.draconicToolsEnergyPerAction;
		ModItems.register(this);
	}

	@Override
	public List<ItemConfigField> getFields(ItemStack stack, int slot)
	{
		List<ItemConfigField> list = super.getFields(stack, slot);
		list.add(new ItemConfigField(References.INT_ID, slot, References.DIG_AOE).setMinMaxAndIncromente(0, EnumUpgrade.DIG_AOE.getUpgradePoints(stack), 1).readFromItem(stack, 0).setModifier("AOE"));
		list.add(new ItemConfigField(References.INT_ID, slot, References.DIG_DEPTH).setMinMaxAndIncromente(1, EnumUpgrade.DIG_DEPTH.getUpgradePoints(stack), 1).readFromItem(stack, 1));
		list.add(new ItemConfigField(References.BOOLEAN_ID, slot, References.OBLITERATE).readFromItem(stack, false));
		return list;
	}

	@Override
	public String getInventoryName()
	{
		return StatCollector.translateToLocal("info.de.toolInventoryOblit.txt");
	}

	@Override
	public int getInventorySlots()
	{
		return 9;
	}

	@Override
	public boolean isEnchantValid(Enchantment enchant)
	{
		return enchant.type == EnumEnchantmentType.digger;
	}

	@Override
	public void tweakRender(IItemRenderer.ItemRenderType type)
	{
		GL11.glTranslated(0.34, 0.69, 0.1);
		GL11.glRotatef(90, 1, 0, 0);
		GL11.glRotatef(140, 0, -1, 0);
		GL11.glRotatef(180, 0, 0, 1);
		GL11.glScaled(0.7, 0.7, 0.7);

		if (type == IItemRenderer.ItemRenderType.INVENTORY)
		{
			GL11.glScalef(11.8F, 11.8F, 11.8F);
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glTranslated(-1.2, 0, -0.35);
		}
		else if (type == IItemRenderer.ItemRenderType.ENTITY)
		{
			GL11.glRotatef(90.5F, 0, 1, 0);
			GL11.glTranslated(0, 0, -0.9);
		}
	}

	@Override
	public int getUpgradeCap(ItemStack itemstack)
	{
		return BalanceConfigHandler.draconicToolsMaxUpgrades;
	}

	@Override
	public int getMaxTier(ItemStack itemstack)
	{
		return 2;
	}

	@Override
	public List<String> getUpgradeStats(ItemStack stack)
	{
		return super.getUpgradeStats(stack);
	}

	@Override
	public int getCapacity(ItemStack stack)
	{
		int points = IUpgradableItem.EnumUpgrade.RF_CAPACITY.getUpgradePoints(stack);
		return BalanceConfigHandler.draconicToolsBaseStorage + points * BalanceConfigHandler.draconicToolsStoragePerUpgrade;
	}

	@Override
	public int getMaxUpgradePoints(int upgradeIndex)
	{
		if (upgradeIndex == EnumUpgrade.RF_CAPACITY.index)
			return BalanceConfigHandler.draconicToolsMaxCapacityUpgradePoints;
		if (upgradeIndex == EnumUpgrade.DIG_AOE.index)
		{



			return max;
		}
		if (upgradeIndex == EnumUpgrade.DIG_DEPTH.index)
			return BalanceConfigHandler.draconicToolsMaxDigDepthUpgradePoints;
		if (upgradeIndex == EnumUpgrade.DIG_SPEED.index)
			return BalanceConfigHandler.draconicToolsMaxDigSpeedUpgradePoints;
		return BalanceConfigHandler.draconicToolsMaxUpgradePoints;
	}

	@Override
	public int getBaseUpgradePoints(int upgradeIndex)
	{
		if (upgradeIndex == EnumUpgrade.DIG_AOE.index)
			return BalanceConfigHandler.draconicToolsMinDigAOEUpgradePoints;
		if (upgradeIndex == EnumUpgrade.DIG_DEPTH.index)
			return BalanceConfigHandler.draconicToolsMinDigDepthUpgradePoints;
		if (upgradeIndex == EnumUpgrade.DIG_SPEED.index)
			return BalanceConfigHandler.draconicToolsMinDigSpeedUpgradePoints;
		return 0;

}
