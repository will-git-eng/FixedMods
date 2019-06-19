package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.IWireCoil;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.gui.IESlot.ICallbackContainer;
import blusunrize.immersiveengineering.common.items.ItemToolbox;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.UUID;

public class ContainerToolbox extends Container implements ICallbackContainer
{
	private World worldObj;
	private int blockedSlot;
	public IInventory input;
	ItemStack toolbox = null;
	EntityPlayer player = null;
    
	private static final String NBT_KEY_UID = "UID";
	private int blockedSlotNumber = -1;
	private int blockedSlotIndex = -1;

	@Override
	public void onCraftMatrixChanged(IInventory inventory)
	{
		if (!this.worldObj.isRemote)
		{
			((ItemToolbox) this.toolbox.getItem()).setContainedItems(this.toolbox, ((InventoryStorageItem) this.input).stackList);
			ItemStack hand = this.player.getCurrentEquippedItem();
			if (hand != null && !this.toolbox.equals(hand) && this.canInteractWith(this.player))
				this.player.setCurrentItemOrArmor(0, this.toolbox);
			this.player.inventory.markDirty();
		}
		super.onCraftMatrixChanged(inventory);
	}

	private static boolean isSameItemInventory(ItemStack base, ItemStack comparison)
	{
		if (base == null || comparison == null)
			return false;

		if (base.getItem() != comparison.getItem())
			return false;

		if (!base.hasTagCompound() || !comparison.hasTagCompound())
			return false;

		String baseUID = base.getTagCompound().getString(NBT_KEY_UID);
		String comparisonUID = comparison.getTagCompound().getString(NBT_KEY_UID);
		return baseUID != null && baseUID.equals(comparisonUID);
    

	public ContainerToolbox(InventoryPlayer inventoryPlayer, World world)
	{
		this.worldObj = world;
		this.player = inventoryPlayer.player;
		this.toolbox = inventoryPlayer.getCurrentItem();
		this.internalSlots = ((ItemToolbox) this.toolbox.getItem()).getInternalSlots(this.toolbox);
		this.input = new InventoryStorageItem(this, this.toolbox);
    
		this.blockedSlotIndex = inventoryPlayer.currentItem;
		if (this.toolbox != null && this.toolbox.getItem() instanceof ItemToolbox)
		{
			if (!this.toolbox.hasTagCompound())
				this.toolbox.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = this.toolbox.getTagCompound();
			if (!nbt.hasKey(NBT_KEY_UID))
				nbt.setString(NBT_KEY_UID, UUID.randomUUID().toString());
    

		int i = 0;
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 48, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 30, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 48, 42));

		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 75, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 93, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 111, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 75, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 93, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 111, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 129, 42));

		for (int j = 0; j < 6; j++)
		{
			this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 35 + j * 18, 77));
		}
		for (int j = 0; j < 7; j++)
		{
			this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 26 + j * 18, 112));
		}

		this.bindPlayerInventory(inventoryPlayer);

		if (!world.isRemote)
			try
			{
				((InventoryStorageItem) this.input).stackList = ((ItemToolbox) this.toolbox.getItem()).getContainedItems(this.toolbox);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		this.onCraftMatrixChanged(this.input);
	}

	@Override
	public boolean canInsert(ItemStack stack, int slotNumer, Slot slotObject)
	{
		if (stack == null)
			return true;
		if (IEContent.itemToolbox.equals(stack.getItem()))
			return false;
		if (slotNumer < 3)
			return stack.getItem() instanceof ItemFood;
		if (slotNumer < 10)
		{
			if (stack.getItem() instanceof ITool)
				return ((ITool) stack.getItem()).isTool(stack);
			return stack.getItem() instanceof ItemTool;
		}
		if (slotNumer < 16)
		{
			if (stack.getItem() instanceof IWireCoil)
				return true;
			if (Block.getBlockFromItem(stack.getItem()) != null && Block.getBlockFromItem(stack.getItem()).hasTileEntity(stack.getItemDamage()))
				return Block.getBlockFromItem(stack.getItem()).createTileEntity(this.worldObj, stack.getItemDamage()) instanceof IImmersiveConnectable;
		}
		else
			return true;
		return false;
	}

	@Override
	public boolean canTake(ItemStack stack, int slotNumer, Slot slotObject)
	{
		return true;
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 157 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
    
			if (slot.getSlotIndex() == this.blockedSlotIndex)
    
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
    
		if (slot == this.blockedSlotNumber)
    

		ItemStack stack = null;
		Slot slotObject = (Slot) this.inventorySlots.get(slot);

		if (slotObject != null && slotObject.getHasStack())
    
			if (!this.canInteractWith(player))
    

			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if (slot < this.internalSlots)
			{
				if (!this.mergeItemStack(stackInSlot, this.internalSlots, this.internalSlots + 36, true))
					return null;
			}
			else if (stackInSlot != null)
			{
				boolean b = true;
				for (int i = 0; i < this.internalSlots; i++)
				{
					Slot s = (Slot) this.inventorySlots.get(i);
					if (s != null && s.isItemValid(stackInSlot))
					{
						if (this.mergeItemStack(stackInSlot, i, i + 1, true))
						{
							b = false;
							break;
						}
						continue;
					}
				}
				if (b)
					return null;
			}

			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if (stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(this.player, stack);
		}
		return stack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
    
    
	}

	@Override
	public ItemStack slotClick(int slot, int button, int buttonType, EntityPlayer player)
	{
		if (slot == this.blockedSlot || buttonType != 0 && button == player.inventory.currentItem)
    
		if (slot == this.blockedSlotNumber)
			return null;
		if (buttonType == 2 && button == this.blockedSlotNumber)
			return null;
		if (!this.canInteractWith(player))
    

		((ItemToolbox) this.toolbox.getItem()).setContainedItems(this.toolbox, ((InventoryStorageItem) this.input).stackList);

		return super.slotClick(slot, button, buttonType, player);
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed(player);
		if (!this.worldObj.isRemote)
		{
			((ItemToolbox) this.toolbox.getItem()).setContainedItems(this.toolbox, ((InventoryStorageItem) this.input).stackList);
    
			if (hand != null && !this.toolbox.equals(hand) && this.canInteractWith(player))
				this.player.setCurrentItemOrArmor(0, this.toolbox);
			this.player.inventory.markDirty();
		}
	}
}