package am2.containers.slots;

import am2.containers.ContainerMagiciansWorkbench;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

public class SlotMagiciansWorkbenchCrafting extends Slot
{
	private final IInventory craftMatrix;
	private final EntityPlayer thePlayer;
	private final ContainerMagiciansWorkbench workbench;
	private int amountCrafted;

	public SlotMagiciansWorkbenchCrafting(EntityPlayer player, IInventory craftMatrix, IInventory craftResult, ContainerMagiciansWorkbench workbench, int index, int x, int y)
	{
		super(craftResult, index, x, y);
		this.thePlayer = player;
		this.craftMatrix = craftMatrix;
		this.workbench = workbench;
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return false;
	}

	@Override
	public ItemStack decrStackSize(int par1)
	{
		if (this.getHasStack())
			this.amountCrafted += Math.min(par1, this.getStack().stackSize);

		return super.decrStackSize(par1);
	}

	@Override
	protected void onCrafting(ItemStack par1ItemStack, int par2)
	{
		this.amountCrafted += par2;
		this.onCrafting(par1ItemStack);
	}

	@Override
	protected void onCrafting(ItemStack itemCrafted)
	{
		itemCrafted.onCrafting(this.thePlayer.worldObj, this.thePlayer, this.amountCrafted);
		this.amountCrafted = 0;
		ItemStack[] components = new ItemStack[this.craftMatrix.getSizeInventory()];

		for (int i = 0; i < components.length; ++i)
			if (this.craftMatrix.getStackInSlot(i) != null)
				components[i] = this.craftMatrix.getStackInSlot(i).copy();
			else
				components[i] = null;

		this.workbench.getWorkbench().rememberRecipe(itemCrafted, components, this.craftMatrix.getSizeInventory() == 4);
	}

	@Override
	public void onSlotChange(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		if (par1ItemStack != null && par2ItemStack != null && par1ItemStack.getItem() == par2ItemStack.getItem())
		{
			int i = par2ItemStack.stackSize - par1ItemStack.stackSize;
			if (i > 0)
			{
				this.onCrafting(par1ItemStack, i);
				this.doComponentDecrements();
			}
		}

	}

	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
		this.onCrafting(par2ItemStack);
		ItemCraftedEvent event = new ItemCraftedEvent(par1EntityPlayer, par2ItemStack, this.craftMatrix);
		MinecraftForge.EVENT_BUS.post(event);
		this.doComponentDecrements();
	}

	private void doComponentDecrements()
	{
		for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i)
		{
			ItemStack itemstack1 = this.craftMatrix.getStackInSlot(i);
			if (itemstack1 != null)
				if (itemstack1.stackSize <= 1 && this.searchAndDecrement(itemstack1))
					this.workbench.onCraftMatrixChanged(this.craftMatrix);
				else
					this.doStandardDecrement(this.craftMatrix, itemstack1, i);
		}

	}

	private boolean searchAndDecrement(ItemStack stack)
	{
		for (int n = this.workbench.getWorkbench().getStorageStart(); n < this.workbench.getWorkbench().getStorageStart() + this.workbench.getWorkbench().getStorageSize(); ++n)
		{
			ItemStack wbStack = this.workbench.getWorkbench().getStackInSlot(n);
			if (wbStack != null && stack.isItemEqual(wbStack))
			{
				    
				if (!ItemStack.areItemStackTagsEqual(stack, wbStack))
					continue;
				    

				this.doStandardDecrement(this.workbench.getWorkbench(), wbStack, n);
				return true;
			}
		}

		return false;
	}

	private void doStandardDecrement(IInventory inventory, ItemStack itemstack1, int i)
	{
		if (itemstack1.getItem().hasContainerItem(itemstack1))
		{
			ItemStack itemstack2 = itemstack1.getItem().getContainerItem(itemstack1);
			if (itemstack2.isItemStackDamageable() && itemstack2.getItemDamage() > itemstack2.getMaxDamage())
			{
				MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(this.thePlayer, itemstack2));
				itemstack2 = null;
			}

			if (itemstack2 != null)
				inventory.setInventorySlotContents(i, itemstack2);
			else
				inventory.decrStackSize(i, 1);
		}
		else
			inventory.decrStackSize(i, 1);

	}
}
