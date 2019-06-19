package powercrystals.minefactoryreloaded.tile.machine;

import buildcraft.api.transport.IPipeTile.PipeType;
import cofh.asm.relauncher.Strippable;
import cofh.core.util.CoreUtils;
import cofh.lib.inventory.IInventoryManager;
import cofh.lib.inventory.InventoryManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import powercrystals.minefactoryreloaded.core.MFRUtil;
import powercrystals.minefactoryreloaded.core.UtilInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiEjector;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.container.ContainerEjector;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryInventory;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryInventory;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class TileEntityEjector extends TileEntityFactoryInventory
{
	protected boolean _lastRedstoneState;
	protected boolean _whitelist = false;
	protected boolean _matchNBT = true;
	protected boolean _ignoreDamage = true;
	protected boolean _hasItems = false;
	protected ForgeDirection[] _pullDirections = new ForgeDirection[0];

	public TileEntityEjector()
	{
		super(Machine.Ejector);
		this.setManageSolids(true);
		this.setCanRotate(true);
	}

	@Override
	protected void onRotate()
	{
		LinkedList var1 = new LinkedList();
		var1.addAll(MFRUtil.VALID_DIRECTIONS);
		var1.remove(this.getDirectionFacing());
		this._pullDirections = (ForgeDirection[]) var1.toArray(new ForgeDirection[5]);
		super.onRotate();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (!this.worldObj.isRemote)
		{
			boolean var1 = this._rednetState != 0 || CoreUtils.isRedstonePowered(this);
			if (var1 & !this._lastRedstoneState & (!this._whitelist | this._whitelist == this._hasItems))
			{
				ForgeDirection direction = this.getDirectionFacing();
				Map<ForgeDirection, IInventory> chests = UtilInventory.findChests(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this._pullDirections);

				label134:
				for (Entry<ForgeDirection, IInventory> inv : chests.entrySet())
				{
					if (inv.getKey() != direction)
    
						if (inv.getValue() instanceof TileEntity)
						{
							TileEntity tile = (TileEntity) inv.getValue();
							if (this.fake.cantBreak(tile.xCoord, tile.yCoord, tile.zCoord))
								continue;
    

						IInventoryManager manager = InventoryManager.create(inv.getValue(), inv.getKey().getOpposite());
						Map<Integer, ItemStack> contents = manager.getContents();

						for (Entry<Integer, ItemStack> content : contents.entrySet())
						{
							ItemStack stack = content.getValue();
							if (stack != null && stack.stackSize >= 1 && manager.canRemoveItem(stack, content.getKey().intValue()))
							{
								boolean var11 = false;
								int var12 = 1;
								int var13 = this.getSizeItemList();

								while (var13-- > 0)
								{
									if (this.itemMatches(this._inventory[var13], stack))
									{
										var11 = true;
										var12 = Math.max(1, this._inventory[var13].stackSize);
										break;
									}
								}

								if (this._whitelist == var11)
								{
									ItemStack var16 = stack.copy();
									var12 = Math.min(stack.stackSize, var12);
									var16.stackSize = var12;
									ItemStack var14 = UtilInventory.dropStack(this, var16, direction, direction);
									if (var14 == null || var14.stackSize < var12)
									{
										manager.removeItem(var12 - (var14 == null ? 0 : var14.stackSize), var16);
										break label134;
									}
								}
							}
						}
					}
				}
			}

			this._lastRedstoneState = var1;
		}
	}

	protected boolean itemMatches(ItemStack var1, ItemStack var2)
	{
		return !(var1 == null | var2 == null) && var1.getItem().equals(var2.getItem()) && (this._ignoreDamage || var1.isItemEqual(var2) && (!this._matchNBT || var1.getTagCompound() == null && var2.getTagCompound() == null || var1.getTagCompound() != null && var2.getTagCompound() != null && var1.getTagCompound().equals(var2.getTagCompound())));
	}

	@Override
	protected void onFactoryInventoryChanged()
	{
		super.onFactoryInventoryChanged();
		int var1 = this.getSizeItemList();

		while (var1-- > 0)
		{
			if (this._inventory[var1] != null)
			{
				this._hasItems = true;
				return;
			}
		}

	}

	public int getSizeItemList()
	{
		return 9;
	}

	@Override
	public int getSizeInventory()
	{
		return this.getSizeItemList();
	}

	@Override
	public boolean shouldDropSlotWhenBroken(int var1)
	{
		return false;
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3)
	{
		return false;
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3)
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2)
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiEjector(this.getContainer(var1), this);
	}

	@Override
	public ContainerFactoryInventory getContainer(InventoryPlayer var1)
	{
		return new ContainerEjector(this, var1);
	}

	@Override
	public void writePortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		var2.setBoolean("whitelist", this._whitelist);
		var2.setBoolean("matchNBT", this._matchNBT);
		var2.setBoolean("ignoreDamage", this._ignoreDamage);
	}

	@Override
	public void readPortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		this._whitelist = var2.getBoolean("whitelist");
		this._matchNBT = !var2.hasKey("matchNBT") || var2.getBoolean("matchNBT");
		this._ignoreDamage = var2.getBoolean("ignoreDamage");
	}

	@Override
	public void writeItemNBT(NBTTagCompound var1)
	{
		super.writeItemNBT(var1);
		if (this._whitelist)
			var1.setBoolean("whitelist", this._whitelist);

		if (!this._matchNBT)
			var1.setBoolean("matchNBT", this._matchNBT);

		if (!this._ignoreDamage)
			var1.setBoolean("ignoreDamage", this._ignoreDamage);

	}

	@Override
	public void writeToNBT(NBTTagCompound var1)
	{
		super.writeToNBT(var1);
		var1.setBoolean("redstone", this._lastRedstoneState);
	}

	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		this._lastRedstoneState = var1.getBoolean("redstone");
		this._whitelist = var1.getBoolean("whitelist");
		this._matchNBT = !var1.hasKey("matchNBT") || var1.getBoolean("matchNBT");
		this._ignoreDamage = !var1.hasKey("ignoreDamage") || var1.getBoolean("ignoreDamage");
	}

	public boolean getIsWhitelist()
	{
		return this._whitelist;
	}

	public boolean getIsNBTMatch()
	{
		return this._matchNBT;
	}

	public boolean getIsIDMatch()
	{
		return this._ignoreDamage;
	}

	public void setIsWhitelist(boolean var1)
	{
		this._whitelist = var1;
	}

	public void setIsNBTMatch(boolean var1)
	{
		this._matchNBT = var1;
	}

	public void setIsIDMatch(boolean var1)
	{
		this._ignoreDamage = var1;
	}

	@Override
	public ConnectionType canConnectInventory(ForgeDirection var1)
	{
		return var1 == this.getDirectionFacing() ? ConnectionType.FORCE : ConnectionType.DENY;
	}

	@Override
	@Strippable({ "buildcraft.api.transport.IPipeConnection" })
	public ConnectOverride overridePipeConnection(PipeType var1, ForgeDirection var2)
	{
		return var1 == PipeType.STRUCTURE ? ConnectOverride.CONNECT : var2 == this.getDirectionFacing() ? super.overridePipeConnection(var1, var2) : ConnectOverride.DISCONNECT;
	}
}
