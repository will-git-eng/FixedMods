package thaumcraft.common.items.tools;

import ru.will.git.eventhelper.reflectionmedicMod;
import ru.will.git.eventhelper.util.EventUtils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.IThaumcraftItems;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;

import java.util.Set;

public class ItemElementalPickaxe extends ItemPickaxe implements IThaumcraftItems
{
	public ItemElementalPickaxe(ToolMaterial enumtoolmaterial)
	{
		super(enumtoolmaterial);
		this.setCreativeTab(ConfigItems.TABTC);
		this.setRegistryName("elemental_pick");
		this.setUnlocalizedName("elemental_pick");
		ConfigItems.ITEM_VARIANT_HOLDERS.add(this);
	}

	@Override
	public Item getItem()
	{
		return this;
	}

	@Override
	public String[] getVariantNames()
	{
		return new String[] { "normal" };
	}

	@Override
	public int[] getVariantMeta()
	{
		return new int[] { 0 };
	}

	@Override
	public ItemMeshDefinition getCustomMesh()
	{
		return null;
	}

	@Override
	public ModelResourceLocation getCustomModelResourceLocation(String variant)
	{
		return new ModelResourceLocation("thaumcraft:" + variant);
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		return ImmutableSet.of("pickaxe");
	}

	@Override
	public EnumRarity getRarity(ItemStack itemstack)
	{
		return EnumRarity.RARE;
	}

	@Override
	public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
	{
		return stack2.isItemEqual(new ItemStack(ItemsTC.ingots, 1, 0)) || super.getIsRepairable(stack1, stack2);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
	{
		if (!player.world.isRemote && (!(entity instanceof EntityPlayer) || FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled()))
		{
			
			if (!(reflectionmedicMod.paranoidProtection && EventUtils.cantAttack(player, entity)))
				
				entity.setFire(2);
		}

		return super.onLeftClickEntity(stack, player, entity);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		if (tab != ConfigItems.TABTC && tab != CreativeTabs.SEARCH)
			super.getSubItems(tab, items);
		else
		{
			ItemStack w1 = new ItemStack(this);
			EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.REFINING, 1);
			EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.SOUNDING, 2);
			items.add(w1);
		}

	}
}
