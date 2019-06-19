package powercrystals.minefactoryreloaded.tile.machine;

import cofh.lib.util.position.BlockPosition;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.IFactoryPlantable;
import powercrystals.minefactoryreloaded.api.ReplacementBlock;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiPlanter;
import powercrystals.minefactoryreloaded.gui.container.ContainerPlanter;
import powercrystals.minefactoryreloaded.gui.container.ContainerUpgradeable;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

public class TileEntityPlanter extends TileEntityFactoryPowered
{
	protected boolean keepLastItem = false;

	public TileEntityPlanter()
	{
		super(Machine.Planter);
		createHAM(this, 1);
		this._areaManager.setOverrideDirection(ForgeDirection.UP);
		this._areaManager.setOriginOffset(0, 1, 0);
		this.setManageSolids(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiPlanter(this.getContainer(var1), this);
	}

	@Override
	public ContainerUpgradeable getContainer(InventoryPlayer var1)
	{
		return new ContainerPlanter(this, var1);
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
			ItemStack bpStack = this._inventory[this.getPlanterSlotIdFromBp(pos)];

			for (int slot = 10; slot <= 25; ++slot)
			{
				ItemStack stack = this.getStackInSlot(slot);
				if (stack != null && (bpStack == null || this.stacksEqual(bpStack, stack)) && MFRRegistry.getPlantables().containsKey(stack.getItem()) && (!this.keepLastItem || stack.stackSize >= 2))
				{
					IFactoryPlantable plantable = MFRRegistry.getPlantables().get(stack.getItem());
					if (plantable.canBePlanted(stack, false) && plantable.canBePlantedHere(this.worldObj, pos.x, pos.y, pos.z, stack))
    
						if (this.fake.cantBreak(pos.x, pos.y, pos.z))
						{
							this.setIdleTicks(this.getIdleTicksMax());
							return false;
    

						plantable.prePlant(this.worldObj, pos.x, pos.y, pos.z, stack);
						ReplacementBlock var6 = plantable.getPlantedBlock(this.worldObj, pos.x, pos.y, pos.z, stack);
						if (var6 != null && var6.replaceBlock(this.worldObj, pos.x, pos.y, pos.z, stack))
						{
							plantable.postPlant(this.worldObj, pos.x, pos.y, pos.z, stack);
							this.decrStackSize(slot, 1);
							return true;
						}
					}
				}
			}

			this.setIdleTicks(this.getIdleTicksMax());
			return false;
		}
	}

	@Override
	public void writePortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		var2.setBoolean("keepLastItem", this.keepLastItem);
	}

	@Override
	public void readPortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		this.keepLastItem = var2.getBoolean("keepLastItem");
	}

	@Override
	public void writeItemNBT(NBTTagCompound var1)
	{
		super.writeItemNBT(var1);
		if (this.keepLastItem)
			var1.setBoolean("keepLastItem", this.keepLastItem);

	}

	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		this.keepLastItem = var1.getBoolean("keepLastItem");
	}

	protected boolean stacksEqual(ItemStack var1, ItemStack var2)
	{
		if (!(var1 == null | var2 == null) && var1.getItem().equals(var2.getItem()) && var1.getItemDamage() == var2.getItemDamage() && var1.hasTagCompound() == var2.hasTagCompound())
		{
			if (!var1.hasTagCompound())
				return true;
			else
			{
				NBTTagCompound var3 = (NBTTagCompound) var1.getTagCompound().copy();
				NBTTagCompound var4 = (NBTTagCompound) var2.getTagCompound().copy();
				var3.removeTag("display");
				var4.removeTag("display");
				var3.removeTag("ench");
				var4.removeTag("ench");
				var3.removeTag("RepairCost");
				var4.removeTag("RepairCost");
				return var3.equals(var4);
			}
		}
		else
			return false;
	}

	protected int getPlanterSlotIdFromBp(BlockPosition var1)
	{
		int var2 = this._areaManager.getRadius();
		int var3 = Math.round(1.49F * (var1.x - this.xCoord) / var2);
		int var4 = Math.round(1.49F * (var1.z - this.zCoord) / var2);
		return 4 + var3 + 3 * var4;
	}

	public boolean getConsumeAll()
	{
		return this.keepLastItem;
	}

	public void setConsumeAll(boolean var1)
	{
		this.keepLastItem = var1;
	}

	@Override
	public int getSizeInventory()
	{
		return 26;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 5;
	}

	@Override
	public int getStartInventorySide(ForgeDirection var1)
	{
		return 9;
	}

	@Override
	public boolean shouldDropSlotWhenBroken(int var1)
	{
		return var1 > 8;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection var1)
	{
		return 17;
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
			if (var1 > 9)
			{
				IFactoryPlantable var4 = MFRRegistry.getPlantables().get(var2.getItem());
				return var4 != null && var4.canBePlanted(var2, false);
			}

			if (var1 == 9)
				return this.isUsableAugment(var2);
		}

		return false;
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3)
	{
		return var1 >= 10;
	}
}
