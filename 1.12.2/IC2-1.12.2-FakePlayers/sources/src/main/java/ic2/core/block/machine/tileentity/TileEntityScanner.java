package ic2.core.block.machine.tileentity;

import ru.will.git.ic2.EventConfig;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.IPatternStorage;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotScannable;
import ic2.core.block.machine.container.ContainerScanner;
import ic2.core.block.machine.gui.GuiScanner;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.uu.UuGraph;
import ic2.core.uu.UuIndex;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityScanner extends TileEntityElectricMachine
		implements IHasGui, INetworkClientTileEntityEventListener
{
	private ItemStack currentStack = StackUtil.emptyStack;
	private ItemStack pattern = StackUtil.emptyStack;
	private final int energyusecycle = 256;
	public int progress = 0;
	public final int duration = 3300;
	public final InvSlotConsumable inputSlot = new InvSlotScannable(this, "input", 1);
	public final InvSlot diskSlot = new InvSlotConsumableId(this, "disk", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, ItemName.crystal_memory.getInstance());
	private TileEntityScanner.State state = TileEntityScanner.State.IDLE;
	public double patternUu;
	public double patternEu;

	public TileEntityScanner()
	{
		super(512000, 4);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean newActive = false;
		if (this.progress < 3300)
			if (!this.inputSlot.isEmpty() && (StackUtil.isEmpty(this.currentStack) || StackUtil.checkItemEquality(this.currentStack, this.inputSlot.get())))
				if (this.getPatternStorage() == null && this.diskSlot.isEmpty())
				{
					this.state = State.NO_STORAGE;
					this.reset();
				}
				else if (this.energy.getEnergy() >= 256.0D)
				{
					if (StackUtil.isEmpty(this.currentStack))
						this.currentStack = StackUtil.copyWithSize(this.inputSlot.get(), 1);

					this.pattern = UuGraph.find(this.currentStack);

					if (StackUtil.isEmpty(this.pattern) || EventConfig.scannerBlackList.contains(this.currentStack))
						this.state = State.FAILED;
					else if (this.isPatternRecorded(this.pattern))
					{
						this.state = State.ALREADY_RECORDED;
						this.reset();
					}
					else
					{
						newActive = true;
						this.state = State.SCANNING;
						this.energy.useEnergy(256.0D);
						++this.progress;
						if (this.progress >= 3300)
						{
							this.refreshInfo();
							if (this.patternUu != Double.POSITIVE_INFINITY)
							{
								this.state = State.COMPLETED;
								this.inputSlot.consume(1, false, true);
								this.markDirty();
							}
							else
								this.state = State.FAILED;
						}
					}
				}
				else
					this.state = State.NO_ENERGY;
			else
			{
				this.state = State.IDLE;
				this.reset();
			}
		else if (StackUtil.isEmpty(this.pattern))
		{
			this.state = TileEntityScanner.State.IDLE;
			this.progress = 0;
		}

		this.setActive(newActive);
	}

	public void reset()
	{
		this.progress = 0;
		this.currentStack = StackUtil.emptyStack;
		this.pattern = StackUtil.emptyStack;
	}

	private boolean isPatternRecorded(ItemStack stack)
	{
		if (!this.diskSlot.isEmpty() && this.diskSlot.get().getItem() instanceof ItemCrystalMemory)
		{
			ItemStack crystalMemory = this.diskSlot.get();
			if (StackUtil.checkItemEquality(((ItemCrystalMemory) crystalMemory.getItem()).readItemStack(crystalMemory), stack))
				return true;
		}

		IPatternStorage storage = this.getPatternStorage();
		if (storage == null)
			return false;
		for (ItemStack stored : storage.getPatterns())
		{
			if (StackUtil.checkItemEquality(stored, stack))
				return true;
		}

		return false;
	}

	private void record()
	{
		if (!StackUtil.isEmpty(this.pattern) && this.patternUu != Double.POSITIVE_INFINITY)
		{
			if (!this.savetoDisk(this.pattern))
			{
				IPatternStorage storage = this.getPatternStorage();
				if (storage == null)
				{
					this.state = TileEntityScanner.State.TRANSFER_ERROR;
					return;
				}

				if (!storage.addPattern(this.pattern))
				{
					this.state = TileEntityScanner.State.TRANSFER_ERROR;
					return;
				}
			}

			this.reset();
		}
		else
			this.reset();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.progress = nbttagcompound.getInteger("progress");
		NBTTagCompound contentTag = nbttagcompound.getCompoundTag("currentStack");
		this.currentStack = new ItemStack(contentTag);
		contentTag = nbttagcompound.getCompoundTag("pattern");
		this.pattern = new ItemStack(contentTag);
		int stateIdx = nbttagcompound.getInteger("state");
		this.state = stateIdx < TileEntityScanner.State.values().length ? TileEntityScanner.State.values()[stateIdx] : TileEntityScanner.State.IDLE;
		this.refreshInfo();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("progress", this.progress);
		if (!StackUtil.isEmpty(this.currentStack))
		{
			NBTTagCompound contentTag = new NBTTagCompound();
			this.currentStack.writeToNBT(contentTag);
			nbt.setTag("currentStack", contentTag);
		}

		if (!StackUtil.isEmpty(this.pattern))
		{
			NBTTagCompound contentTag = new NBTTagCompound();
			this.pattern.writeToNBT(contentTag);
			nbt.setTag("pattern", contentTag);
		}

		nbt.setInteger("state", this.state.ordinal());
		return nbt;
	}

	@Override
	public ContainerBase<TileEntityScanner> getGuiContainer(EntityPlayer player)
	{
		return new ContainerScanner(player, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiScanner(new ContainerScanner(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	public IPatternStorage getPatternStorage()
	{
		World world = this.getWorld();

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			TileEntity target = world.getTileEntity(this.pos.offset(dir));
			if (target instanceof IPatternStorage)
				return (IPatternStorage) target;
		}

		return null;
	}

	public boolean savetoDisk(ItemStack stack)
	{
		if (!this.diskSlot.isEmpty() && stack != null)
		{
			if (this.diskSlot.get().getItem() instanceof ItemCrystalMemory)
			{
				ItemStack crystalMemory = this.diskSlot.get();
				((ItemCrystalMemory) crystalMemory.getItem()).writecontentsTag(crystalMemory, stack);
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		switch (event)
		{
			case 0:
				this.reset();
				break;
			case 1:
				if (this.progress >= 3300)
					this.record();
		}

	}

	private void refreshInfo()
	{
		if (!StackUtil.isEmpty(this.pattern))
			this.patternUu = UuIndex.instance.getInBuckets(this.pattern);

	}

	public int getPercentageDone()
	{
		return 100 * this.progress / 3300;
	}

	public int getSubPercentageDoneScaled(int width)
	{
		return width * (100 * this.progress % 3300) / 3300;
	}

	public boolean isDone()
	{
		return this.progress >= 3300;
	}

	public TileEntityScanner.State getState()
	{
		return this.state;
	}

	public enum State
	{
		IDLE,
		SCANNING,
		COMPLETED,
		FAILED,
		NO_STORAGE,
		NO_ENERGY,
		TRANSFER_ERROR,
		ALREADY_RECORDED
	}
}
