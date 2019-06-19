package am2.items;

import am2.AMCore;
import am2.texture.ResourceManager;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemRuneBag extends Item
{
	    
	public ItemRuneBag()
	{
		this.setMaxStackSize(1);
	}
	    

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer entityplayer)
	{
		    
		if (stack.stackSize > 1)
			return stack;
		    

		if (entityplayer.isSneaking())
			FMLNetworkHandler.openGui(entityplayer, AMCore.instance, 20, world, (int) entityplayer.posX, (int) entityplayer.posY, (int) entityplayer.posZ);

		return stack;
	}

	private ItemStack[] getMyInventory(ItemStack itemStack)
	{
		return this.ReadFromStackTagCompound(itemStack);
	}

	public void UpdateStackTagCompound(ItemStack itemStack, ItemStack[] values)
	{
		if (itemStack.stackTagCompound == null)
			itemStack.stackTagCompound = new NBTTagCompound();

		for (int i = 0; i < values.length; ++i)
		{
			ItemStack stack = values[i];
			if (stack == null)
				itemStack.stackTagCompound.removeTag("runebagmeta" + i);
			else
				itemStack.stackTagCompound.setInteger("runebagmeta" + i, stack.getItemDamage());
		}

	}

	@Override
	public boolean getShareTag()
	{
		return true;
	}

	public void UpdateStackTagCompound(ItemStack itemStack, InventoryRuneBag inventory)
	{
		if (itemStack.stackTagCompound == null)
			itemStack.stackTagCompound = new NBTTagCompound();

		for (int i = 0; i < inventory.getSizeInventory(); ++i)
		{
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null)
				itemStack.stackTagCompound.setInteger("runebagmeta" + i, stack.getItemDamage());
		}

	}

	public ItemStack[] ReadFromStackTagCompound(ItemStack itemStack)
	{
		if (itemStack.stackTagCompound == null)
			return new ItemStack[InventoryRuneBag.inventorySize];
		else
		{
			ItemStack[] items = new ItemStack[InventoryRuneBag.inventorySize];

			for (int i = 0; i < items.length; ++i)
				if (itemStack.stackTagCompound.hasKey("runebagmeta" + i) && itemStack.stackTagCompound.getInteger("runebagmeta" + i) != -1)
				{
					int meta = 0;
					meta = itemStack.stackTagCompound.getInteger("runebagmeta" + i);
					items[i] = new ItemStack(ItemsCommonProxy.rune, 1, meta);
				}
				else
					items[i] = null;

			return items;
		}
	}

	public InventoryRuneBag ConvertToInventory(ItemStack runeBagStack)
	{
		InventoryRuneBag irb = new InventoryRuneBag();
		irb.SetInventoryContents(this.getMyInventory(runeBagStack));
		return irb;
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{
		super.itemIcon = ResourceManager.RegisterTexture("rune_bag", par1IconRegister);
	}
}
