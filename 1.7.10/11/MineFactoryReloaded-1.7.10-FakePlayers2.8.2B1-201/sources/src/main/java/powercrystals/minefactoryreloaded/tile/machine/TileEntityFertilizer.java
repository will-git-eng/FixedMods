package powercrystals.minefactoryreloaded.tile.machine;

import cofh.lib.util.position.BlockPosition;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.FertilizerType;
import powercrystals.minefactoryreloaded.api.IFactoryFertilizable;
import powercrystals.minefactoryreloaded.api.IFactoryFertilizer;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiUpgradeable;
import powercrystals.minefactoryreloaded.gui.container.ContainerUpgradeable;
import powercrystals.minefactoryreloaded.setup.MFRConfig;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

import java.util.Map;
import java.util.Random;

public class TileEntityFertilizer extends TileEntityFactoryPowered
{
	private Random _rand = new Random();

	public TileEntityFertilizer()
	{
		super(Machine.Fertilizer);
		createHAM(this, 1);
		this.setManageSolids(true);
		this.setCanRotate(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiUpgradeable(this.getContainer(var1), this);
	}

	@Override
	public ContainerUpgradeable getContainer(InventoryPlayer var1)
	{
		return new ContainerUpgradeable(this, var1);
	}

	@Override
	public boolean activateMachine()
	{
		BlockPosition pos = this._areaManager.getNextBlock();
		if (!this.worldObj.blockExists(pos.x, pos.y, pos.z))
		{
			this.setIdleTicks(this.getIdleTicksMax());
			return false;
		}
		else
		{
			Map<Block, IFactoryFertilizable> fertilizables = MFRRegistry.getFertilizables();
			Block block = this.worldObj.getBlock(pos.x, pos.y, pos.z);
			if (!fertilizables.containsKey(block))
			{
				this.setIdleTicks(this.getIdleTicksMax());
				return false;
			}
			else
			{
				IFactoryFertilizable fertilizable = fertilizables.get(block);
				Map<Item, IFactoryFertilizer> fertilizers = MFRRegistry.getFertilizers();
				int slot = 0;

				for (int size = this.getSizeInventory(); slot < size; ++slot)
				{
					ItemStack stack = this.getStackInSlot(slot);
					if (stack != null && fertilizers.containsKey(stack.getItem()))
    
						if (this.fake.cantBreak(pos.x, pos.y, pos.z))
						{
							this.setIdleTicks(this.getIdleTicksMax());
							return false;
    

						IFactoryFertilizer fertilizer = fertilizers.get(stack.getItem());
						FertilizerType type = fertilizer.getFertilizerType(stack);
						if (type != FertilizerType.None && fertilizable.canFertilize(this.worldObj, pos.x, pos.y, pos.z, type) && fertilizable.fertilize(this.worldObj, this._rand, pos.x, pos.y, pos.z, type))
						{
							fertilizer.consume(stack);
							if (MFRConfig.playSounds.getBoolean(true))
								this.worldObj.playAuxSFXAtEntity(null, 2005, pos.x, pos.y, pos.z, this._rand.nextInt(10) + 5);

							if (stack.stackSize <= 0)
								this.setInventorySlotContents(slot, null);

							return true;
						}
					}
				}

				this.setIdleTicks(this.getIdleTicksMax());
				return false;
			}
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 10;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 20;
	}

	@Override
	public int getStartInventorySide(ForgeDirection var1)
	{
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection var1)
	{
		return 9;
	}

	@Override
	public int getUpgradeSlot()
	{
		return 9;
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3)
	{
		if (var2 != null)
		{
			if (var1 < 9)
				return MFRRegistry.getFertilizers().containsKey(var2.getItem());

			if (var1 == 9)
				return this.isUsableAugment(var2);
		}

		return false;
	}
}
