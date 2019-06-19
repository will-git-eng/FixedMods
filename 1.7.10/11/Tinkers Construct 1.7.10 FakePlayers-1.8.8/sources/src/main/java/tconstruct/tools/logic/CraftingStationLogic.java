package tconstruct.tools.logic;

import ru.will.git.tconstruct.EventConfig;
import mantle.blocks.abstracts.InventoryLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import tconstruct.tools.inventory.CraftingStationContainer;

import java.lang.ref.WeakReference;

public class CraftingStationLogic extends InventoryLogic implements ISidedInventory
{
    
	public WeakReference<IInventory> doubleChest;
	public WeakReference<IInventory> patternChest;
	public WeakReference<IInventory> furnace;
	public boolean tinkerTable;
	public boolean stencilTable;
	public boolean doubleFirst;

	public CraftingStationLogic()
	{
    
    
	}

	@Override
	public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z)
	{
		this.chest = null;
		this.doubleChest = null;
		this.patternChest = null;
		this.furnace = null;
		this.tinkerTable = false;
		int[] ys = { y, y - 1, y + 1 };
		for (byte iy = 0; iy < 3; iy++)
		{
			int yPos = ys[iy];
			for (int xPos = x - 1; xPos <= x + 1; xPos++)
			{
				for (int zPos = z - 1; zPos <= z + 1; zPos++)
				{
					TileEntity tile = world.getTileEntity(xPos, yPos, zPos);
					if (this.chest == null && tile instanceof TileEntityChest)
					{
						this.chest = new WeakReference(tile);
						this.checkForChest(world, xPos, yPos, zPos, 1, 0);
						this.checkForChest(world, xPos, yPos, zPos, -1, 0);
						this.checkForChest(world, xPos, yPos, zPos, 0, 1);
    
						if (!EventConfig.enableCraftingStationDoubleChest)
						{
							IInventory inventoryChest = this.doubleChest == null ? null : this.doubleChest.get();
							if (inventoryChest instanceof TileEntityChest)
							{
								TileEntityChest chest = (TileEntityChest) inventoryChest;
								int xx = chest.xCoord;
								int yy = chest.yCoord;
								int zz = chest.zCoord;
								Block block = chest.getWorldObj().getBlock(xx, yy, zz);
								if (block instanceof BlockChest)
									if (((BlockChest) block).func_149951_m(chest.getWorldObj(), xx, yy, zz) instanceof InventoryLargeChest)
										this.doubleChest = null;
							}
    
					}
					else if (this.patternChest == null && tile instanceof PatternChestLogic)
						this.patternChest = new WeakReference(tile);
					else if (this.furnace == null && (tile instanceof TileEntityFurnace || tile instanceof FurnaceLogic))
						this.furnace = new WeakReference(tile);
					else if (this.tinkerTable == false && tile instanceof ToolStationLogic)
						this.tinkerTable = true;
				}
			}
		}

		return new CraftingStationContainer(inventoryplayer, this, x, y, z);
	}

	void checkForChest(World world, int x, int y, int z, int dx, int dz)
	{
		TileEntity tile = world.getTileEntity(x + dx, y, z + dz);
		if (tile instanceof TileEntityChest)
		{
			this.doubleChest = new WeakReference(tile);
			this.doubleFirst = dx + dz < 0;
		}
    
	public WeakReference[] getInventories()
	{
		return new WeakReference[] { this.chest, this.doubleChest, this.patternChest, this.furnace };
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return isUseableByPlayer(this.getInventories(), player) && super.isUseableByPlayer(player);
	}

	public static boolean isUseableByPlayer(WeakReference[] inventories, EntityPlayer player)
	{
		for (WeakReference<IInventory> ref : inventories)
		{
			if (ref != null)
			{
				IInventory inv = ref.get();
				if (inv != null && !inv.isUseableByPlayer(player))
					return false;
			}
		}

		return true;
    

	@Override
	protected String getDefaultName()
	{
		return "crafters.CraftingStation";
	}

	@Override
	public boolean canDropInventorySlot(int slot)
	{
		return slot != 0;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[] {};
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return false;
	}

	@Override
	public ItemStack decrStackSize(int slot, int quantity)
	{
		if (slot == 0)
			for (int i = 1; i < this.getSizeInventory(); i++)
			{
				this.decrStackSize(i, 1);
			}
		return super.decrStackSize(slot, quantity);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return false;
	}

	@Override
	public String getInventoryName()
	{
		return this.getDefaultName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	@Override
	public void closeInventory()
    

	}

	@Override
	public void openInventory()
    

	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}
}
