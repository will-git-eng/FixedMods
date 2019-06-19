package thaumicenergistics.common.container;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.util.InventoryAdaptor;
import ru.will.git.thaumicenergistics.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.api.grid.ICraftingIssuerHost;
import thaumicenergistics.api.gui.ICraftingIssuerContainer;
import thaumicenergistics.client.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.client.gui.ThEGuiHelper;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.container.slot.SlotArcaneCraftingResult;
import thaumicenergistics.common.container.slot.SlotArmor;
import thaumicenergistics.common.container.slot.SlotRestrictive;
import thaumicenergistics.common.integration.tc.ArcaneRecipeHelper;
import thaumicenergistics.common.network.packet.client.Packet_C_ArcaneCraftingTerminal;
import thaumicenergistics.common.network.packet.client.Packet_C_Sync;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.common.utils.ThEUtils;

import java.util.ArrayList;
import java.util.List;

    
public class ContainerPartArcaneCraftingTerminal extends ContainerWithPlayerInventory
		implements IMEMonitorHandlerReceiver<IAEItemStack>, ICraftingIssuerContainer
{
    
	public class ArcaneCrafingCost
	{
    
		public final float visCost;

    
		public final Aspect primal;

    
		public final boolean hasEnoughVis;

		public ArcaneCrafingCost(final float visCost, final Aspect primal, final boolean hasEnough)
    
			this.visCost = Math.round(visCost * 10.0F) / 10.0F;

			this.primal = primal;

			this.hasEnoughVis = hasEnough;
		}
	}

    
	private static int PLAYER_INV_POSITION_Y = 162, HOTBAR_INV_POSITION_Y = 220;

    
	private static int CRAFTING_GRID_SIZE = 3;

    
	public static int CRAFTING_GRID_TOTAL_SIZE = ContainerPartArcaneCraftingTerminal.CRAFTING_GRID_SIZE * ContainerPartArcaneCraftingTerminal.CRAFTING_GRID_SIZE;

    
	public static int CRAFTING_SLOT_X_POS = 44, CRAFTING_SLOT_Y_POS = 90;

    
	private static int RESULT_SLOT_X_POS = 116, RESULT_SLOT_Y_POS = 126;

    
	private static int WAND_SLOT_XPOS = 116, WAND_SLOT_YPOS = 90;

    
	public static int VIEW_SLOT_XPOS = 206, VIEW_SLOT_YPOS = 8;

    
	public static int ARMOR_SLOT_X_POS = 8, ARMOR_SLOT_Y_POS = 81, ARMOR_SLOT_COUNT = 4;

    
	private static CraftingManager CRAFT_MANAGER = CraftingManager.getInstance();

    
	private final PartArcaneCraftingTerminal terminal;

    
	private final PlayerSource playerSource;

    
	private IMEMonitor<IAEItemStack> monitor;

    
	private int firstCraftingSlotNumber = -1, lastCraftingSlotNumber = -1;

    
	private int firstViewSlotNumber = -1, lastViewSlotNumber = -1;

    
	private final SlotRestrictive wandSlot;

    
	private final SlotArcaneCraftingResult resultSlot;

    
	private ItemStack wand;

    
	private AspectList requiredAspects;

    
	private List<ArcaneCrafingCost> craftingCost = new ArrayList<>();

    
	private SortOrder cachedSortOrder = PartArcaneCraftingTerminal.DEFAULT_SORT_ORDER;

    
	private SortDir cachedSortDirection = PartArcaneCraftingTerminal.DEFAULT_SORT_DIR;

    
	private ViewItems cachedViewMode = PartArcaneCraftingTerminal.DEFAULT_VIEW_MODE;

    
	public ContainerPartArcaneCraftingTerminal(final PartArcaneCraftingTerminal terminal, final EntityPlayer player)
    
    
    
    
    
		Slot craftingSlot = null;
		for (int row = 0; row < ContainerPartArcaneCraftingTerminal.CRAFTING_GRID_SIZE; row++)
		{
			for (int column = 0; column < ContainerPartArcaneCraftingTerminal.CRAFTING_GRID_SIZE; column++)
    
    
    
    
				if (row + column == 0)
					this.firstCraftingSlotNumber = craftingSlot.slotNumber;
			}
    
		if (craftingSlot != null)
    
    
    
    
    
		SlotRestrictive viewSlot = null;
		for (int viewSlotID = PartArcaneCraftingTerminal.VIEW_SLOT_MIN; viewSlotID <= PartArcaneCraftingTerminal.VIEW_SLOT_MAX; viewSlotID++)
    
			int row = viewSlotID - PartArcaneCraftingTerminal.VIEW_SLOT_MIN;
    
    
    
			if (row == 0)
				this.firstViewSlotNumber = viewSlot.slotNumber;
    
		if (viewSlot != null)
    
		for (int armorIndex = 0; armorIndex < ContainerPartArcaneCraftingTerminal.ARMOR_SLOT_COUNT; ++armorIndex)
    
    
    
			this.addSlotToContainer(armorSlot);

    
	private boolean attachToMonitor()
    
		if (EffectiveSide.isClientSide())
    
		if (this.monitor != null)
    
		IGrid grid = this.terminal.getGridBlock().getGrid();
		if (grid != null)
    
			this.monitor = this.terminal.getItemInventory();
			if (this.monitor != null)
    
				this.monitor.addListener(this, grid.hashCode());
				return true;
			}
		}

		return false;
	}

    
	private boolean clearCraftingGrid(final boolean sendUpdate)
    
		if (EffectiveSide.isClientSide())
    
		boolean clearedAll = true;

		for (int index = this.firstCraftingSlotNumber; index <= this.lastCraftingSlotNumber; index++)
    
    
			if (slot == null || !slot.getHasStack())
    
    
    
			if (!didMerge)
    
				clearedAll = false;
				continue;
    
    
				slot.putStack(null);
			else
    
    
				slot.onSlotChanged();
			}
    
    
		return clearedAll;
	}

    
	private void doShiftAutoCrafting(final EntityPlayer player)
    
    
    
    
		ItemStack slotStackOriginal = resultStack.copy();

		for (autoCraftCounter = slotStackOriginal.stackSize; autoCraftCounter <= 64; autoCraftCounter += slotStackOriginal.stackSize)
    
    
			if (didMerge)
    
    
    
    
    
    
    
					break;
			}
    
				break;

    
		if (autoCraftCounter > 0)
    
    
			this.detectAndSendChanges();
		}
	}

    
	private boolean doStacksMatch(final IAEItemStack keyStack, final IAEItemStack potentialMatch)
    
		if (keyStack.getItemStack().isItemEqual(potentialMatch.getItemStack()))
    
    
		int[] keyIDs = OreDictionary.getOreIDs(keyStack.getItemStack());
    
		if (keyIDs.length == 0 || matchIDs.length == 0)
    
		for (int keyID : keyIDs)
		{
			for (int matchID : matchIDs)
			{
				if (keyID == matchID)
					return true;
			}
		}

		return false;
	}

    
	private ItemStack findMatchingArcaneResult()
	{
    
		IArcaneRecipe matchingRecipe = ArcaneRecipeHelper.INSTANCE.findMatchingArcaneResult(this.terminal, 0, ContainerPartArcaneCraftingTerminal.CRAFTING_GRID_TOTAL_SIZE, this.player);

    
    
		return arcaneResult;
	}

    
	private ItemStack findMatchingRegularResult()
    
    
    
		{
			craftingInventory.setInventorySlotContents(slotIndex, this.terminal.getStackInSlot(slotIndex));
    
		return ContainerPartArcaneCraftingTerminal.CRAFT_MANAGER.findMatchingRecipe(craftingInventory, this.terminal.getWorldObj());
	}

    
	private ItemStack[] getViewCells()
	{
		List<ItemStack> viewCells = new ArrayList<>();

		Slot viewSlot;
		for (int viewSlotIndex = this.firstViewSlotNumber; viewSlotIndex <= this.lastViewSlotNumber; viewSlotIndex++)
    
    
			if (viewSlot == null || !viewSlot.getHasStack())
    
			viewCells.add(viewSlot.getStack());
		}

		return viewCells.toArray(new ItemStack[0]);
	}

    
	private void getWand()
    
    
    
		if (ThEUtils.isItemValidWand(this.wandSlot.getStack(), false))
    
			this.wand = this.wandSlot.getStack();

			return;
    
		this.wand = null;
	}

    
	private boolean mergeWithMENetwork(final ItemStack itemStack)
    
    
    
		if (leftOver != null && leftOver.getStackSize() > 0)
    
    
    
			itemStack.stackSize = (int) leftOver.getStackSize();

			return true;
    
		itemStack.stackSize = 0;

		return true;
	}

    
	private boolean mergeWithViewCells(final ItemStack itemStack)
    
		if (!this.terminal.isItemValidForSlot(PartArcaneCraftingTerminal.VIEW_SLOT_MIN, itemStack))
			return false;

		Slot viewSlot;
		for (int viewSlotIndex = this.firstViewSlotNumber; viewSlotIndex <= this.lastViewSlotNumber; viewSlotIndex++)
    
    
    
    
			if (viewSlot.getHasStack())
    
    
    
			return true;
    
		return false;
	}

    
	@SideOnly(Side.CLIENT)
	private void updateGUIViewCells()
    
    
		if (gui instanceof GuiArcaneCraftingTerminal)
			((GuiArcaneCraftingTerminal) gui).onViewCellsChanged(this.getViewCells());
	}

    
	private ItemStack validateWandVisAmount(final IArcaneRecipe forRecipe)
	{
		boolean hasAll = true;
		AspectList wandAspectList = null;
    
    
		if (this.requiredAspects == null)
    
    
		if (this.wand != null)
    
    
			wandAspectList = wandItem.getAllVis(this.wand);
    
		for (Aspect currentAspect : recipeAspects)
    
    
    
    
			if (wandItem != null && wandAspectList != null)
    
    
				hasEnough = wandAspectList.getAmount(currentAspect) >= requiredVis;
			}

    
    
			this.craftingCost.add(new ArcaneCrafingCost(requiredVis / 100.0F, currentAspect, hasEnough));
    
    
			return ArcaneRecipeHelper.INSTANCE.getRecipeOutput(this.terminal, 0, ContainerPartArcaneCraftingTerminal.CRAFTING_GRID_TOTAL_SIZE, forRecipe);

		return null;

	}

	@Override
	protected boolean detectAndSendChangesMP(final EntityPlayerMP playerMP)
	{
    
		if (this.cachedSortOrder != this.terminal.getSortingOrder())
    
			this.cachedSortOrder = this.terminal.getSortingOrder();
			sendModeUpdate = true;
    
		if (this.cachedSortDirection != this.terminal.getSortingDirection())
    
			this.cachedSortDirection = this.terminal.getSortingDirection();
			sendModeUpdate = true;
    
		if (this.cachedViewMode != this.terminal.getViewMode())
    
			this.cachedViewMode = this.terminal.getViewMode();
			sendModeUpdate = true;
    
    
    
    
    
				this.onClientRequestFullUpdate(this.player);

		return false;
	}

    
	protected boolean slotClickedWasInCraftingInventory(final int slotNumber)
	{
		return slotNumber >= this.firstCraftingSlotNumber && slotNumber <= this.lastCraftingSlotNumber;
	}

	@Override
	public boolean canInteractWith(final EntityPlayer player)
	{
		if (this.terminal != null)
			return this.terminal.isUseableByPlayer(player);
		return false;
	}

    
	public void changeSlotsYOffset(final int deltaY)
	{
		for (Object slotObj : this.inventorySlots)
    
    
			if (slot.slotNumber >= this.firstViewSlotNumber && slot.slotNumber <= this.lastViewSlotNumber)
    
			slot.yDisplayPosition += deltaY;
		}
	}

    
	public List<ArcaneCrafingCost> getCraftingCost(final boolean forceUpdate)
	{
		if (forceUpdate)
		{
			this.craftingCost.clear();
			this.findMatchingArcaneResult();
    
		if (this.craftingCost.isEmpty())
    
    
		return this.craftingCost;
	}

	@Override
	public ICraftingIssuerHost getCraftingHost()
	{
		return this.terminal;
	}

    
	@Override
	public boolean isValid(final Object authToken)
	{
		if (this.monitor == null)
    
		IGrid grid = this.terminal.getGridBlock().getGrid();
		if (grid != null)
			if (grid.hashCode() == (Integer) authToken)
    
    
		this.onClientRequestFullUpdate(this.player);

		return false;
	}

    
	public void onClientNEIRequestSetCraftingGrid(final EntityPlayer player, final IAEItemStack[] gridItems)
    
		if (this.clearCraftingGrid(false))
		{
			for (int craftingSlotIndex = 0; craftingSlotIndex < 9; craftingSlotIndex++)
    
    
    
    
    
				if (matchingStack != null)
    
    
					slot.putStack(matchingStack);
				}
    
			this.detectAndSendChanges();
		}

	}

    
	public void onClientRequestAutoCraft(final EntityPlayer player, final IAEItemStack result)
    
    
    
		if (player.openContainer instanceof ContainerCraftAmount)
    
    
			cca.setOpenContext(new ContainerOpenContext(te));
			cca.getOpenContext().setWorld(te.getWorldObj());
			cca.getOpenContext().setX(te.xCoord);
			cca.getOpenContext().setY(te.yCoord);
			cca.getOpenContext().setZ(te.zCoord);
    
			cca.getCraftingItem().putStack(result.getItemStack());
    
			if (player instanceof EntityPlayerMP)
				((EntityPlayerMP) player).isChangingQuantityOnly = false;
			cca.detectAndSendChanges();
		}
	}

    
	public void onClientRequestClearCraftingGrid(final EntityPlayer player)
	{
		this.clearCraftingGrid(true);
	}

    
	public void onClientRequestDeposit(final EntityPlayer player, final int mouseButton)
    
		if (player == null || this.monitor == null)
    
		if (mouseButton == ThEGuiHelper.MOUSE_BUTTON_RIGHT)
    
    
    
		if (playerHolding == null)
    
    
		boolean depositOne = mouseButton == ThEGuiHelper.MOUSE_BUTTON_RIGHT || mouseButton == ThEGuiHelper.MOUSE_WHEEL_MOTION;

    
    
    
		if (leftOverStack != null && leftOverStack.getStackSize() > 0)
    
    
    
			player.inventory.setItemStack(leftOverStack.getItemStack());
		}
    
			if (depositOne && playerHolding.stackSize > 1)
    
				playerHolding.stackSize--;
    
				leftOverStack = AEApi.instance().storage().createItemStack(playerHolding);
			}
    
    
		Packet_C_Sync.sendPlayerHeldItem(player, leftOverStack == null ? null : leftOverStack.getItemStack());
	}

    
	public void onClientRequestDepositRegion(final EntityPlayer player, final int slotNumber)
	{
    
    
			slotsToDeposit = this.getNonEmptySlotsFromPlayerInventory();
    
    
		if (slotsToDeposit != null)
		{
			for (Slot slot : slotsToDeposit)
    
				if (slot == null || !slot.getHasStack())
    
    
    
				if (!didMerge)
    
    
					slot.putStack(null);
    
					slot.onSlotChanged();
    
			this.detectAndSendChanges();
		}
	}

    
	public void onClientRequestExtract(final EntityPlayer player, final IAEItemStack requestedStack, final int mouseButton, final boolean isShiftHeld)
    
		if (player == null || this.monitor == null)
    
		if (requestedStack == null || requestedStack.getStackSize() == 0)
    
    
		int amountToExtract = 0;
		switch (mouseButton)
		{
    
				amountToExtract = (int) Math.min(maxStackSize, requestedStack.getStackSize());
				break;

    
    
					amountToExtract = 1;
				else
    
					double halfRequest = requestedStack.getStackSize() / 2.0D;
					double halfMax = maxStackSize / 2.0D;
					halfRequest = Math.ceil(halfRequest);
					halfMax = Math.ceil(halfMax);
					amountToExtract = (int) Math.min(halfMax, halfRequest);
				}
				break;

			case ThEGuiHelper.MOUSE_BUTTON_WHEEL:
				if (player.capabilities.isCreativeMode)
				{
					ItemStack creativeCopy = requestedStack.getItemStack();
					creativeCopy.stackSize = creativeCopy.getMaxStackSize();
					player.inventory.setItemStack(creativeCopy);
					Packet_C_Sync.sendPlayerHeldItem(player, creativeCopy);
				}
				break;

    
    
					amountToExtract = 1;
    
    
    
    
    
    
		if (extractedStack != null && extractedStack.getStackSize() > 0)
    
    
			if (mouseButton == ThEGuiHelper.MOUSE_BUTTON_LEFT && isShiftHeld)
			{
    
				if (player.inventory.addItemStackToInventory(extractedStack.getItemStack()))
    
    
					return;
				} */
				ItemStack stack = extractedStack.getItemStack();
				extractedStack.setStackSize(Math.min(extractedStack.getStackSize(), stack.getMaxStackSize()));
				stack.stackSize = (int) extractedStack.getStackSize();
				InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
				stack = adaptor.simulateAdd(stack);
				if (stack != null)
					extractedStack.setStackSize(extractedStack.getStackSize() - stack.stackSize);
				if (extractedStack.getStackSize() <= 0)
					return;
				extractedStack = this.monitor.extractItems(extractedStack, Actionable.SIMULATE, this.playerSource);
				if (extractedStack == null || extractedStack.getStackSize() <= 0)
					return;
				extractedStack = this.monitor.extractItems(extractedStack, Actionable.MODULATE, this.playerSource);
				if (extractedStack == null || extractedStack.getStackSize() <= 0)
					return;
				adaptor.addItems(extractedStack.getItemStack());
    
    
    
			if (playerHolding != null)
    
    
				if (playerHolding.stackSize < maxStackSize && ModUtils.isMatch(playerHolding, extractedStack.getItemStack()))
    
    
    
						return;

    
    
    
					toExtract.setStackSize(amountToExtract); */
					extractedStack.setStackSize(amountToExtract);
					extractedStack = this.monitor.extractItems(extractedStack, Actionable.SIMULATE, this.playerSource);
					if (extractedStack == null || extractedStack.getStackSize() <= 0)
						return;
					extractedStack = this.monitor.extractItems(extractedStack, Actionable.MODULATE, this.playerSource);
					if (extractedStack == null || extractedStack.getStackSize() <= 0)
						return;
					playerHolding.stackSize += extractedStack.getStackSize();
    
				}
    
					return;
			}
			else
    
				extractedStack = this.monitor.extractItems(extractedStack, Actionable.MODULATE, this.playerSource);
				if (extractedStack == null || extractedStack.getStackSize() <= 0)
    
    
				player.inventory.setItemStack(extractedStack.getItemStack());
			}

    
    
			Packet_C_Sync.sendPlayerHeldItem(player, player.inventory.getItemStack());
		}

	}

    
	public void onClientRequestFullUpdate(final EntityPlayer player)
    
		if (this.monitor != null && this.terminal.isActive())
    
    
			Packet_C_ArcaneCraftingTerminal.sendAllNetworkItems(player, fullList);
		}
    
			Packet_C_ArcaneCraftingTerminal.sendAllNetworkItems(player, AEApi.instance().storage().createItemList());
	}

    
	public void onClientRequestSetSort(final SortOrder order, final SortDir dir, final ViewItems viewMode)
    
		this.terminal.setSorts(order, dir, viewMode);
	}

    
	public void onClientRequestSwapArmor(final EntityPlayer player)
	{
		this.terminal.swapStoredArmor(player);
		this.detectAndSendChanges();
		Packet_C_ArcaneCraftingTerminal.updateAspectCost(player);
	}

    
	@Override
	public void onContainerClosed(final EntityPlayer player)
    
		super.onContainerClosed(player);

		if (this.terminal != null)
    
		if (EffectiveSide.isServerSide())
			if (this.monitor != null)
				this.monitor.removeListener(this);
	}

    
	@Override
	public void onCraftMatrixChanged(final IInventory inventory)
    
		this.requiredAspects = null;
    
    
    
    
    
		this.resultSlot.setResultAspects(this.requiredAspects);
    
		this.terminal.setInventorySlotContentsWithoutNotify(PartArcaneCraftingTerminal.RESULT_SLOT_INDEX, craftResult);

	}

    
	@Override
	public void onListUpdate()
    
    
			Packet_C_ArcaneCraftingTerminal.sendAllNetworkItems(this.player, AEApi.instance().storage().createItemList());
	}

    
	public void onViewCellChange()
    
    
			this.updateGUIViewCells();
	}

    
	@Override
	public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> changes, final BaseActionSource actionSource)
	{
		if (this.monitor == null)
			return;

		for (IAEItemStack change : changes)
    
    
			if (newAmount == null)
    
    
				newAmount.setStackSize(0);
    
			Packet_C_ArcaneCraftingTerminal.stackAmountChanged(this.player, newAmount);
		}
	}

    
	public void registerForUpdates()
    
		this.terminal.registerListener(this);
	}

    
	public ItemStack requestCraftingReplenishment(final ItemStack itemStack)
	{
		if (this.monitor == null)
    
    
    
    
		if (replenishment != null)
    
    
    
    
		{
			if (this.doStacksMatch(requestStack, potentialMatch))
    
    
    
    
				if (replenishment != null && replenishment.getStackSize() > 0)
					return replenishment.getItemStack();
			}
    
		return null;
	}

    
	@Override
	public ItemStack transferStackInSlot(final EntityPlayer player, final int slotNumber)
    
    
    
    
		if (slot != null && slot.getHasStack())
		{
    
    
			if (this.slotClickedWasInCraftingInventory(slotNumber))
    
    
				if (!didMerge)
    
    
    
						didMerge = this.mergeSlotWithPlayerInventory(slotStack);
				}
    
			else if (this.slotClickedWasInPlayerInventory(slotNumber) || this.slotClickedWasInHotbarInventory(slotNumber))
    
    
    
				if (!didMerge)
    
    
					if (!didMerge)
    
    
						if (!didMerge)
    
    
    
								didMerge = this.swapSlotInventoryHotbar(slotNumber, slotStack);
						}
					}
				}
    
			else if (slot == this.resultSlot)
    
				this.doShiftAutoCrafting(player);

				return null;
    
			else if (slot == this.wandSlot)
    
    
				if (!didMerge)
    
    
    
						didMerge = this.mergeWithMENetwork(slotStack);
				}
    
			else if (slotNumber >= this.firstViewSlotNumber && slotNumber <= this.lastViewSlotNumber)
    
    
    
					didMerge = this.mergeSlotWithPlayerInventory(slotStack);
    
			if (didMerge)
    
    
					slot.putStack(null);
    
    
				this.detectAndSendChanges();
			}

    
		return null;
	}
}
