package ic2.core.block.invslot;

import ru.will.git.ic2.EventConfig;
import ic2.api.upgrade.*;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvSlotUpgrade extends InvSlot
{
	private static final int maxStackSize = 64;
	public int augmentation;
	public int extraProcessTime;
	public double processTimeMultiplier;
	public int extraEnergyDemand;
	public double energyDemandMultiplier;
	public int extraEnergyStorage;
	public double energyStorageMultiplier;
	public int extraTier;
	private List<Redstone.IRedstoneModifier> redstoneModifiers = Collections.emptyList();

	public static InvSlotUpgrade createUnchecked(TileEntityInventory base, String name, int count)
	{
		return new InvSlotUpgrade((TileEntityInventory & IUpgradableBlock) base, name, count);
	}

	public <T extends TileEntityInventory & IUpgradableBlock> InvSlotUpgrade(T base, String name, int count)
	{
		super(base, name, InvSlot.Access.NONE, count);
		this.resetRates();
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		Item rawItem = stack.getItem();
		if (!(rawItem instanceof IUpgradeItem))
			return false;
		IUpgradeItem item = (IUpgradeItem) rawItem;
		boolean result = item.isSuitableFor(stack, ((IUpgradableBlock) this.base).getUpgradableProperties());

		
		if (result)
		{
			int maxOverclockerCount = EventConfig.maxOverclockerCount;
			if (maxOverclockerCount > 0)
			{
				ItemStack overclockerStack = ItemName.upgrade.getItemStack(ItemUpgradeModule.UpgradeType.overclocker);
				if (stack.isItemEqual(overclockerStack))
					for (int slot = 0, count = 0; slot < this.size(); slot++)
					{
						ItemStack stackInSlot = this.get(slot);
						if (!StackUtil.isEmpty(stackInSlot) && stackInSlot.isItemEqual(overclockerStack))
						{
							count += stackInSlot.getCount();
							if (count >= maxOverclockerCount)
								return false;
						}
					}
			}
		}
		

		return result;
	}

	@Override
	public void onChanged()
	{
		this.resetRates();
		IUpgradableBlock block = (IUpgradableBlock) this.base;
		List<Redstone.IRedstoneModifier> newRedstoneModifiers = new ArrayList<>();

		for (int i = 0; i < this.size(); ++i)
		{
			ItemStack stack = this.get(i);
			if (!StackUtil.isEmpty(stack) && this.accepts(stack))
			{
				IUpgradeItem upgrade = (IUpgradeItem) stack.getItem();
				boolean all = upgrade instanceof IFullUpgrade;
				int size = StackUtil.getSize(stack);
				if (all || upgrade instanceof IAugmentationUpgrade)
					this.augmentation += ((IAugmentationUpgrade) upgrade).getAugmentation(stack, block) * size;

				if (all || upgrade instanceof IProcessingUpgrade)
				{
					IProcessingUpgrade procUpgrade = (IProcessingUpgrade) upgrade;
					this.extraProcessTime += procUpgrade.getExtraProcessTime(stack, block) * size;
					this.processTimeMultiplier *= Math.pow(procUpgrade.getProcessTimeMultiplier(stack, block), (double) size);
					this.extraEnergyDemand += procUpgrade.getExtraEnergyDemand(stack, block) * size;
					this.energyDemandMultiplier *= Math.pow(procUpgrade.getEnergyDemandMultiplier(stack, block), (double) size);
				}

				if (all || upgrade instanceof IEnergyStorageUpgrade)
				{
					IEnergyStorageUpgrade engUpgrade = (IEnergyStorageUpgrade) upgrade;
					this.extraEnergyStorage += engUpgrade.getExtraEnergyStorage(stack, block) * size;
					this.energyStorageMultiplier *= Math.pow(engUpgrade.getEnergyStorageMultiplier(stack, block), (double) size);
				}

				if (all || upgrade instanceof ITransformerUpgrade)
					this.extraTier += ((ITransformerUpgrade) upgrade).getExtraTier(stack, block) * size;

				if (all || upgrade instanceof IRedstoneSensitiveUpgrade)
				{
					IRedstoneSensitiveUpgrade redUpgrade = (IRedstoneSensitiveUpgrade) upgrade;
					if (redUpgrade.modifiesRedstoneInput(stack, block))
						newRedstoneModifiers.add(new UpgradeRedstoneModifier(redUpgrade, stack, block));
				}
			}
		}

		for (TileEntityComponent component : this.base.getComponents())
		{
			if (component instanceof Redstone)
			{
				Redstone rs = (Redstone) component;
				rs.removeRedstoneModifiers(this.redstoneModifiers);
				rs.addRedstoneModifiers(newRedstoneModifiers);
				rs.update();
			}
		}

		this.redstoneModifiers = newRedstoneModifiers.isEmpty() ? Collections.emptyList() : newRedstoneModifiers;
	}

	private void resetRates()
	{
		this.augmentation = 0;
		this.extraProcessTime = 0;
		this.processTimeMultiplier = 1.0D;
		this.extraEnergyDemand = 0;
		this.energyDemandMultiplier = 1.0D;
		this.extraEnergyStorage = 0;
		this.energyStorageMultiplier = 1.0D;
		this.extraTier = 0;
	}

	public int getOperationsPerTick(int defaultOperationLength)
	{
		return defaultOperationLength == 0 ? 64 : this.getOpsPerTick(this.getStackOpLen(defaultOperationLength));
	}

	public int getOperationLength(int defaultOperationLength)
	{
		if (defaultOperationLength == 0)
			return 1;
		double stackOpLen = this.getStackOpLen(defaultOperationLength);
		int opsPerTick = this.getOpsPerTick(stackOpLen);
		return Math.max(1, (int) Math.round(stackOpLen * (double) opsPerTick / 64.0D));
	}

	private double getStackOpLen(int defaultOperationLength)
	{
		return ((double) defaultOperationLength + (double) this.extraProcessTime) * 64.0D * this.processTimeMultiplier;
	}

	private int getOpsPerTick(double stackOpLen)
	{
		return (int) Math.min(Math.ceil(64.0D / stackOpLen), 2.147483647E9D);
	}

	public int getEnergyDemand(int defaultEnergyDemand)
	{
		return applyModifier(defaultEnergyDemand, this.extraEnergyDemand, this.energyDemandMultiplier);
	}

	public int getEnergyStorage(int defaultEnergyStorage, int defaultOperationLength, int defaultEnergyDemand)
	{
		int opLen = this.getOperationLength(defaultOperationLength);
		int energyDemand = this.getEnergyDemand(defaultEnergyDemand);
		return applyModifier(defaultEnergyStorage, this.extraEnergyStorage + opLen * energyDemand, this.energyStorageMultiplier);
	}

	public int getTier(int defaultTier)
	{
		return applyModifier(defaultTier, this.extraTier, 1.0D);
	}

	private static int applyModifier(int base, int extra, double multiplier)
	{
		double ret = (double) Math.round(((double) base + (double) extra) * multiplier);
		return ret > 2.147483647E9D ? Integer.MAX_VALUE : (int) ret;
	}

	public boolean tickNoMark()
	{
		IUpgradableBlock block = (IUpgradableBlock) this.base;
		boolean ret = false;

		for (int i = 0; i < this.size(); ++i)
		{
			ItemStack stack = this.get(i);
			if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IUpgradeItem && ((IUpgradeItem) stack.getItem()).onTick(stack, block))
				ret = true;
		}

		return ret;
	}

	public void tick()
	{
		if (this.tickNoMark())
			this.base.markDirty();

	}

	private static class UpgradeRedstoneModifier implements Redstone.IRedstoneModifier
	{
		private final IRedstoneSensitiveUpgrade upgrade;
		private final ItemStack stack;
		private final IUpgradableBlock block;

		UpgradeRedstoneModifier(IRedstoneSensitiveUpgrade upgrade, ItemStack stack, IUpgradableBlock block)
		{
			this.upgrade = upgrade;
			this.stack = stack.copy();
			this.block = block;
		}

		@Override
		public int getRedstoneInput(int redstoneInput)
		{
			return this.upgrade.getRedstoneInput(this.stack, this.block, redstoneInput);
		}
	}
}
