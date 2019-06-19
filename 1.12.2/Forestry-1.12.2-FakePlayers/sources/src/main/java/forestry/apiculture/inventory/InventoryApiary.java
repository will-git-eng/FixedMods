package forestry.apiculture.inventory;

import ru.will.git.forestry.EventConfig;
import forestry.api.apiculture.*;
import forestry.apiculture.InventoryBeeHousing;
import forestry.core.utils.SlotUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;

public class InventoryApiary extends InventoryBeeHousing implements IApiaryInventory
{
	public static final int SLOT_FRAMES_1 = 9;
	public static final int SLOT_FRAMES_COUNT = 3;

	public InventoryApiary()
	{
		super(12);
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack itemStack)
	{
		if (SlotUtil.isSlotInRange(slotIndex, SLOT_FRAMES_1, SLOT_FRAMES_COUNT))
			return itemStack.getItem() instanceof IHiveFrame && this.getStackInSlot(slotIndex).isEmpty();

		return super.canSlotAccept(slotIndex, itemStack);
	}


	@Override
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack)
	{

		if (!EventConfig.enableApiaryFramesAutomation && SlotUtil.isSlotInRange(slotIndex, SLOT_FRAMES_1, SLOT_FRAMES_COUNT))
			return false;

		return super.isItemValidForSlot(slotIndex, itemStack);
	}

	public Collection<Tuple<IHiveFrame, ItemStack>> getFrames()
	{
		Collection<Tuple<IHiveFrame, ItemStack>> hiveFrames = new ArrayList<>(SLOT_FRAMES_COUNT);

		for (int i = SLOT_FRAMES_1; i < SLOT_FRAMES_1 + SLOT_FRAMES_COUNT; i++)
		{
			ItemStack stackInSlot = this.getStackInSlot(i);
			Item itemInSlot = stackInSlot.getItem();
			if (itemInSlot instanceof IHiveFrame)
			{
				IHiveFrame frame = (IHiveFrame) itemInSlot;
				hiveFrames.add(new Tuple<>(frame, stackInSlot.copy()));
			}
		}

		return hiveFrames;
	}

	@Override
	public void wearOutFrames(IBeeHousing beeHousing, int amount)
	{
		IBeekeepingMode beekeepingMode = BeeManager.beeRoot.getBeekeepingMode(beeHousing.getWorldObj());
		int wear = Math.round(amount * beekeepingMode.getWearModifier());

		for (int i = SLOT_FRAMES_1; i < SLOT_FRAMES_1 + SLOT_FRAMES_COUNT; i++)
		{
			ItemStack hiveFrameStack = this.getStackInSlot(i);
			Item hiveFrameItem = hiveFrameStack.getItem();
			if (hiveFrameItem instanceof IHiveFrame)
			{
				IHiveFrame hiveFrame = (IHiveFrame) hiveFrameItem;

				ItemStack queenStack = this.getQueen();
				IBee queen = BeeManager.beeRoot.getMember(queenStack);
				if (queen != null)
				{
					ItemStack usedFrame = hiveFrame.frameUsed(beeHousing, hiveFrameStack, queen, wear);

					this.setInventorySlotContents(i, usedFrame);
				}
			}
		}
	}
}
