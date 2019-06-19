package extracells.item;

import extracells.registries.ItemEnum;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ItemFluidPattern extends ItemECBase
{

	public static Fluid getFluid(ItemStack itemStack)
	{
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());

		return FluidRegistry.getFluid(itemStack.getTagCompound().getString("fluidID"));
	}

	public static ItemStack getPatternForFluid(Fluid fluid)
	{
		ItemStack itemStack = new ItemStack(ItemEnum.FLUIDPATTERN.getItem(), 1);
		itemStack.setTagCompound(new NBTTagCompound());
		if (fluid != null)
			itemStack.getTagCompound().setString("fluidID", fluid.getName());
		return itemStack;
	}

	IIcon icon;

	public ItemFluidPattern()
	{
		this.setMaxStackSize(1);
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass)
	{
		if (pass == 0)
		{
    
    
			if (fluid != null)
    
		}
		return this.icon;
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemStack)
	{
		Fluid fluid = getFluid(itemStack);
		if (fluid == null)
			return StatCollector.translateToLocal(this.getUnlocalizedName(itemStack));
		return StatCollector.translateToLocal(this.getUnlocalizedName(itemStack)) + ": " + fluid.getLocalizedName(new FluidStack(fluid, 1));
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack)
	{
		return EnumRarity.uncommon;
	}

	@Override
	public int getSpriteNumber()
	{
		return 1;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList)
	{
		for (Fluid fluid : FluidRegistry.getRegisteredFluidIDsByFluid().keySet())
		{
			String name = "";
			ItemStack itemStack = new ItemStack(this, 1);
			itemStack.setTagCompound(new NBTTagCompound());
			itemStack.getTagCompound().setString("fluidID", fluid.getName());
			itemList.add(itemStack);
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return "extracells.item.fluid.pattern";
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
	{
		if (entityPlayer.isSneaking())
			return ItemEnum.FLUIDPATTERN.getSizedStack(itemStack.stackSize);
		return itemStack;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.icon = iconRegister.registerIcon("extracells:fluid.pattern");
	}

	@Override
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}
}
