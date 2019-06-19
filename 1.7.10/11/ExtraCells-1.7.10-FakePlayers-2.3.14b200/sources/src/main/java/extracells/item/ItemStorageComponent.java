package extracells.item;

import appeng.api.implementations.items.IStorageComponent;
import extracells.integration.Integration;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import java.util.List;

public class ItemStorageComponent extends ItemECBase implements IStorageComponent
{

	private IIcon[] icons;
	public final String[] suffixes = { "physical.256k", "physical.1024k", "physical.4096k", "physical.16384k", "fluid.1k", "fluid.4k", "fluid.16k", "fluid.64k", "fluid.256k", "fluid.1024k", "fluid.4096k", "gas.1k", "gas.4k", "gas.16k", "gas.64k", "gas.256k", "gas.1024k", "gas.4096k" };
	public final int[] size = { 262144, 1048576, 4194304, 16777216, 1024, 4096, 16384, 65536, 262144, 1048576, 4194304 };

	public ItemStorageComponent()
	{
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getBytes(ItemStack is)
    
    
    
	}

	@Override
	public IIcon getIconFromDamage(int dmg)
	{
		int j = MathHelper.clamp_int(dmg, 0, this.suffixes.length);
		return this.icons[j];
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack)
	{
		if (itemStack.getItemDamage() >= 4)
			return EnumRarity.rare;
		return EnumRarity.epic;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList)
	{
		for (int j = 0; j < this.suffixes.length; ++j)
		{
			if (!(this.suffixes[j].contains("gas") && !Integration.Mods.MEKANISMGAS.isEnabled()))
				itemList.add(new ItemStack(item, 1, j));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return "extracells.item.storage.component." + this.suffixes[itemStack.getItemDamage()];
	}

	@Override
	public boolean isStorageComponent(ItemStack is)
	{
		return is.getItem() == this;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.icons = new IIcon[this.suffixes.length];

		for (int i = 0; i < this.suffixes.length; ++i)
		{
			this.icons[i] = iconRegister.registerIcon("extracells:" + "storage.component." + this.suffixes[i]);
		}
	}
}
