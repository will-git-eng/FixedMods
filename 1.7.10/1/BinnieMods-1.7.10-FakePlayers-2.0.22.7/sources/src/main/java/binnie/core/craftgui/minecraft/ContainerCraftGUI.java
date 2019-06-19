package binnie.core.craftgui.minecraft;

import binnie.core.BinnieCore;
import binnie.core.craftgui.minecraft.control.ControlSlot;
import binnie.core.craftgui.minecraft.control.EnumHighlighting;
import binnie.core.machines.IMachine;
import binnie.core.machines.Machine;
import binnie.core.machines.network.INetwork;
import binnie.core.machines.power.*;
import binnie.core.machines.transfer.TransferRequest;
import binnie.core.network.packet.MessageContainerUpdate;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerCraftGUI extends Container
{
	private Window window;
	private Map<String, NBTTagCompound> syncedNBT;
	private Map<String, NBTTagCompound> sentNBT;
	private Map<Integer, TankInfo> syncedTanks;
	private PowerInfo syncedPower;
	private ProcessInfo syncedProcess;
	private int errorType;
	private ErrorState error;
	private int mousedOverSlotNumber;

	public ContainerCraftGUI(Window window)
	{
		this.window = window;
		this.syncedNBT = new HashMap<>();
		this.sentNBT = new HashMap<>();
		this.syncedTanks = new HashMap<>();
		this.syncedPower = new PowerInfo();
		this.syncedProcess = new ProcessInfo();
		this.errorType = 0;
		this.error = null;
		this.mousedOverSlotNumber = -1;
		IMachine machine = Machine.getMachine(window.getInventory());
		if (this.getSide() != Side.SERVER)
			return;

		this.inventoryItemStacks = new ListMap();
		this.inventorySlots = new ListMap();
		if (machine == null)
			return;

		GameProfile user = machine.getOwner();
		if (user != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("username", user.getName());
			this.sendNBTToClient("username", nbt);
		}
	}

	@Override
	protected Slot addSlotToContainer(Slot slot)
	{
		return super.addSlotToContainer(slot);
	}

	private Side getSide()
	{
		return this.window.isServer() ? Side.SERVER : Side.CLIENT;
	}

	@Override
	public Slot getSlot(int index)
	{
		if (index < 0 || index >= this.inventorySlots.size())
			return null;
		return (Slot) this.inventorySlots.get(index);
	}

	@Override
	public void putStackInSlot(int index, ItemStack stack)
	{
		if (this.getSlot(index) != null)
			this.getSlot(index).putStack(stack);
	}

	@Override
	public void putStacksInSlots(ItemStack[] par1ArrayOfItemStack)
	{
		for (int i = 0; i < par1ArrayOfItemStack.length; ++i)
		{
			if (this.getSlot(i) != null)
				this.getSlot(i).putStack(par1ArrayOfItemStack[i]);
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed(player);
		WindowInventory inventory = this.window.getWindowInventory();
		for (int i = 0; i < inventory.getSizeInventory(); ++i)
		{
			if (!inventory.dispenseOnClose(i))
				continue;

			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null)
				continue;

			stack = new TransferRequest(stack, player.inventory).transfer(true);
			if (stack != null)
				player.dropPlayerItemWithRandomChoice(stack, false);
		}
	}

	@Override
	public ItemStack slotClick(int slotNum, int mouseButton, int modifier, EntityPlayer player)
	{
		    
		if (this.window.checkItemSlot())
		{
			int itemSlot = this.window.getItemSlot();
			if (itemSlot == slotNum)
				return null;
			if (modifier == 2 && mouseButton == itemSlot)
				return null;
		}
		if (!this.window.isUseableByPlayer(player))
			return null;
		    

		Slot slot = this.getSlot(slotNum);
		if (slot instanceof CustomSlot && ((CustomSlot) slot).handleClick())
		{
			((CustomSlot) slot).onSlotClick(this, mouseButton, modifier, player);
			return player.inventory.getItemStack();
		}
		return super.slotClick(slotNum, mouseButton, modifier, player);
	}

	public void sendNBTToClient(String key, NBTTagCompound nbt)
	{
		this.syncedNBT.put(key, nbt);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		if (player instanceof EntityPlayerMP)
		{
			if (!this.crafters.contains(player))
				this.crafters.add(player);
			this.sentNBT.clear();
		}

		if (!this.window.isUseableByPlayer(player))
			return false;
		    

		IInventory inventory = this.window.getInventory();
		return inventory == null || inventory.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		    
		if (this.window.checkItemSlot() && this.window.getItemSlot() == slotID)
			return null;
		if (!this.window.isUseableByPlayer(player))
			return null;
		    

		return this.shiftClick(player, slotID);
	}

	private ItemStack shiftClick(EntityPlayer player, int index)
	{
		TransferRequest request = this.getShiftClickRequest(player, index);
		if (request == null)
			return null;

		ItemStack stack = request.transfer(true);
		Slot shiftClickedSlot = (Slot) this.inventorySlots.get(index);
		shiftClickedSlot.putStack(stack);
		shiftClickedSlot.onSlotChanged();
		return null;
	}

	private TransferRequest getShiftClickRequest(EntityPlayer player, int index)
	{
		if (index < 0)
			return null;

		Slot shiftClickedSlot = (Slot) this.inventorySlots.get(index);
		ItemStack itemstack = null;
		if (shiftClickedSlot.getHasStack())
			itemstack = shiftClickedSlot.getStack().copy();

		IInventory playerInventory = player.inventory;
		IInventory containerInventory = this.window.getInventory();
		IInventory windowInventory = this.window.getWindowInventory();
		IInventory fromPlayer = containerInventory == null ? windowInventory : containerInventory;
		int[] target = new int[36];
		for (int i = 0; i < 36; ++i)
		{
			target[i] = i;
		}

		TransferRequest request;
		if (shiftClickedSlot.inventory == playerInventory)
			request = new TransferRequest(itemstack, fromPlayer).setOrigin(shiftClickedSlot.inventory);
		else
			request = new TransferRequest(itemstack, playerInventory).setOrigin(shiftClickedSlot.inventory).setTargetSlots(target);

		if (this.window instanceof IWindowAffectsShiftClick)
			((IWindowAffectsShiftClick) this.window).alterRequest(request);
		return request;
	}

	public ItemStack tankClick(EntityPlayer player, int slotID)
	{
		ItemStack stack = player.inventory.getItemStack();
		if (stack == null || stack.stackSize != 1)
			return null;

		ItemStack heldItem = stack.copy();
		heldItem = new TransferRequest(heldItem, this.window.getInventory()).setOrigin(player.inventory).setTargetSlots(new int[0]).setTargetTanks(new int[] { slotID }).transfer(true);
		player.inventory.setItemStack(heldItem);
		if (player instanceof EntityPlayerMP)
			((EntityPlayerMP) player).updateHeldItem();
		return heldItem;
	}

	public boolean handleNBT(Side side, EntityPlayer player, String name, NBTTagCompound action)
	{
		if (side == Side.SERVER)
		{
			if (name.equals("tank-click"))
				this.tankClick(player, action.getByte("id"));
			if (name.equals("slot-reg"))
			{
				int type = action.getByte("t");
				int index = action.getShort("i");
				int slotNumber = action.getShort("n");
				this.getOrCreateSlot(InventoryType.values()[type % 4], index, slotNumber);

				for (Object crafterObject : this.crafters)
				{
					ICrafting crafter = (ICrafting) crafterObject;
					crafter.sendContainerAndContentsToPlayer(this, this.getInventory());
				}
			}
		}

		if (name.contains("tank-update"))
			this.onTankUpdate(action);
		else if (name.equals("power-update"))
			this.onPowerUpdate(action);
		else if (name.equals("process-update"))
			this.onProcessUpdate(action);
		else if (name.equals("error-update"))
			this.onErrorUpdate(action);
		else if (name.equals("mouse-over-slot"))
			this.onMouseOverSlot(player, action);
		else if (name.equals("shift-click-info"))
			this.onRecieveShiftClickHighlights(player, action);
		return false;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		ITankMachine tanks = Machine.getInterface(ITankMachine.class, this.window.getInventory());
		IPoweredMachine powered = Machine.getInterface(IPoweredMachine.class, this.window.getInventory());
		IErrorStateSource error = Machine.getInterface(IErrorStateSource.class, this.window.getInventory());
		IProcess process = Machine.getInterface(IProcess.class, this.window.getInventory());
		if (tanks != null && this.window.isServer())
			for (int i = 0; i < tanks.getTankInfos().length; ++i)
			{
				TankInfo tank = tanks.getTankInfos()[i];
				if (!this.getTankInfo(i).equals(tank))
				{
					this.syncedNBT.put("tank-update-" + i, this.createTankNBT(i, tank));
					this.syncedTanks.put(i, tank);
				}
			}

		if (powered != null && this.window.isServer())
			this.syncedNBT.put("power-update", this.createPowerNBT(powered.getPowerInfo()));
		if (process != null && this.window.isServer())
			this.syncedNBT.put("process-update", this.createProcessNBT(process.getInfo()));
		if (error != null && this.window.isServer())
			this.syncedNBT.put("error-update", this.createErrorNBT(error));

		INetwork.SendGuiNBT machineSync = Machine.getInterface(INetwork.SendGuiNBT.class, this.window.getInventory());
		if (machineSync != null)
			machineSync.sendGuiNBT(this.syncedNBT);

		Map<String, NBTTagCompound> sentThisTime = new HashMap<>();
		for (Map.Entry<String, NBTTagCompound> nbt : this.syncedNBT.entrySet())
		{
			nbt.getValue().setString("type", nbt.getKey());
			boolean shouldSend = true;
			NBTTagCompound lastSent = this.sentNBT.get(nbt.getKey());
			if (lastSent != null)
				shouldSend = !lastSent.equals(nbt.getValue());

			if (shouldSend)
			{
				for (Object crafter : this.crafters)
				{
					if (crafter instanceof EntityPlayerMP)
					{
						EntityPlayerMP player = (EntityPlayerMP) crafter;
						BinnieCore.proxy.sendToPlayer(new MessageContainerUpdate(nbt.getValue()), player);
					}
				}
				sentThisTime.put(nbt.getKey(), nbt.getValue());
			}
		}

		this.sentNBT.putAll(sentThisTime);
		this.syncedNBT.clear();
	}

	private NBTTagCompound createErrorNBT(IErrorStateSource error)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		ErrorState state = null;
		if (error.canWork() != null)
		{
			nbt.setByte("type", (byte) 0);
			state = error.canWork();
		}
		else if (error.canProgress() != null)
		{
			nbt.setByte("type", (byte) 1);
			state = error.canProgress();
		}

		if (state != null)
			state.writeToNBT(nbt);
		return nbt;
	}

	public NBTTagCompound createPowerNBT(PowerInfo powerInfo)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		powerInfo.writeToNBT(nbt);
		return nbt;
	}

	public NBTTagCompound createProcessNBT(ProcessInfo powerInfo)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		powerInfo.writeToNBT(nbt);
		return nbt;
	}

	public NBTTagCompound createTankNBT(int tank, TankInfo tankInfo)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		tankInfo.writeToNBT(nbt);
		nbt.setByte("tank", (byte) tank);
		return nbt;
	}

	public void onTankUpdate(NBTTagCompound nbt)
	{
		int tankID = nbt.getByte("tank");
		TankInfo tank = new TankInfo();
		tank.readFromNBT(nbt);
		this.syncedTanks.put(tankID, tank);
	}

	public void onProcessUpdate(NBTTagCompound nbt)
	{
		(this.syncedProcess = new ProcessInfo()).readFromNBT(nbt);
	}

	public void onPowerUpdate(NBTTagCompound nbt)
	{
		this.syncedPower = new PowerInfo();
		this.syncedPower.readFromNBT(nbt);
	}

	public PowerInfo getPowerInfo()
	{
		return this.syncedPower;
	}

	public ProcessInfo getProcessInfo()
	{
		return this.syncedProcess;
	}

	public TankInfo getTankInfo(int tank)
	{
		return this.syncedTanks.containsKey(tank) ? this.syncedTanks.get(tank) : new TankInfo();
	}

	public void onErrorUpdate(NBTTagCompound nbt)
	{
		this.errorType = nbt.getByte("type");
		if (nbt.hasKey("name"))
		{
			this.error = new ErrorState("", "");
			this.error.readFromNBT(nbt);
		}
		else
			this.error = null;
	}

	public ErrorState getErrorState()
	{
		return this.error;
	}

	public int getErrorType()
	{
		return this.errorType;
	}

	public CustomSlot[] getCustomSlots()
	{
		List<CustomSlot> slots = new ArrayList<>();
		for (Object object : this.inventorySlots)
		{
			if (object instanceof CustomSlot)
				slots.add((CustomSlot) object);
		}
		return slots.toArray(new CustomSlot[0]);
	}

	public void setMouseOverSlot(Slot slot)
	{
		if (slot.slotNumber == this.mousedOverSlotNumber)
			return;

		this.mousedOverSlotNumber = slot.slotNumber;
		ControlSlot.highlighting.get(EnumHighlighting.SHIFT_CLICK).clear();
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setShort("slot", (short) slot.slotNumber);
		this.window.sendClientAction("mouse-over-slot", nbt);
	}

	private void onMouseOverSlot(EntityPlayer player, NBTTagCompound data)
	{
		int slotnumber = data.getShort("slot");
		TransferRequest request = this.getShiftClickRequest(player, slotnumber);
		if (request == null)
			return;

		request.transfer(false);
		NBTTagCompound nbt = new NBTTagCompound();
		List<Integer> slots = new ArrayList<>();
		for (TransferRequest.TransferSlot tslot : request.getInsertedSlots())
		{
			Slot slot = this.getSlot(tslot.inventory, tslot.id);
			if (slot != null)
				slots.add(slot.slotNumber);
		}

		int[] array = new int[slots.size()];
		for (int i = 0; i < slots.size(); ++i)
		{
			array[i] = slots.get(i);
		}

		nbt.setIntArray("slots", array);
		nbt.setShort("origin", (short) slotnumber);
		this.syncedNBT.put("shift-click-info", nbt);
	}

	private void onRecieveShiftClickHighlights(EntityPlayer player, NBTTagCompound data)
	{
		ControlSlot.highlighting.get(EnumHighlighting.SHIFT_CLICK).clear();
		for (int slotnumber : data.getIntArray("slots"))
		{
			ControlSlot.highlighting.get(EnumHighlighting.SHIFT_CLICK).add(slotnumber);
		}
	}

	private CustomSlot getSlot(IInventory inventory, int id)
	{
		for (Object o : this.inventorySlots)
		{
			CustomSlot slot = (CustomSlot) o;
			if (slot.inventory == inventory && slot.getSlotIndex() == id)
				return slot;
		}
		return null;
	}

	public void recieveNBT(Side side, EntityPlayer player, NBTTagCompound action)
	{
		String name = action.getString("type");
		if (this.handleNBT(side, player, name, action))
			return;

		this.window.recieveGuiNBT(this.getSide(), player, name, action);
		INetwork.RecieveGuiNBT machine = Machine.getInterface(INetwork.RecieveGuiNBT.class, this.window.getInventory());
		if (machine != null)
			machine.recieveGuiNBT(this.getSide(), player, name, action);
	}

	public Slot getOrCreateSlot(InventoryType type, int index)
	{
		IInventory inventory = this.getInventory(type);
		Slot slot = this.getSlot(inventory, index);
		if (slot == null)
		{
			slot = new CustomSlot(inventory, index);
			this.addSlotToContainer(slot);
		}

		    
		if (type == InventoryType.Player && this.window.checkItemSlot())
		{
			EntityPlayer player = this.window.getPlayer();
			if (slot.getSlotIndex() == player.inventory.currentItem)
				this.window.setItemSlot(slot.slotNumber);
		}
		    

		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByte("t", (byte) type.ordinal());
		nbt.setShort("i", (short) index);
		nbt.setShort("n", (short) slot.slotNumber);
		this.window.sendClientAction("slot-reg", nbt);
		return slot;
	}

	protected IInventory getInventory(InventoryType type)
	{
		if (type == InventoryType.Machine)
			return this.window.getInventory();
		if (type == InventoryType.Player)
			return this.window.getPlayer().inventory;
		if (type == InventoryType.Window)
			return this.window.getWindowInventory();
		return null;
	}

	private Slot getOrCreateSlot(InventoryType type, int index, int slotNumber)
	{
		IInventory inventory = this.getInventory(type);
		if (this.inventorySlots.get(slotNumber) != null)
			return null;

		Slot slot = new CustomSlot(inventory, index);
		slot.slotNumber = slotNumber;
		this.inventorySlots.add(slotNumber, slot);
		this.inventoryItemStacks.add(slotNumber, null);
		return slot;
	}
}
