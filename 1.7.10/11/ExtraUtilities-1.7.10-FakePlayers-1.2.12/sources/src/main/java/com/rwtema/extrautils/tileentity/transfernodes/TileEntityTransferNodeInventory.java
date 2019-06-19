package com.rwtema.extrautils.tileentity.transfernodes;

import ru.will.git.extrautilities.EventConfig;
import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.block.Box;
import com.rwtema.extrautils.block.BoxModel;
import com.rwtema.extrautils.helper.XUHelper;
import com.rwtema.extrautils.inventory.LiquidInventory;
import com.rwtema.extrautils.item.ItemNodeUpgrade;
import com.rwtema.extrautils.tileentity.transfernodes.nodebuffer.INodeBuffer;
import com.rwtema.extrautils.tileentity.transfernodes.nodebuffer.INodeInventory;
import com.rwtema.extrautils.tileentity.transfernodes.nodebuffer.ItemBuffer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityTransferNodeInventory extends TileEntityTransferNode implements INodeInventory, ISidedInventory
{
	private static final int[] contents = { 0 };
	private static final int[] nullcontents = new int[0];
	private static InvCrafting crafting = new InvCrafting(3, 3);
	private static ForgeDirection[] orthY = { ForgeDirection.NORTH, ForgeDirection.NORTH, ForgeDirection.UP, ForgeDirection.UP, ForgeDirection.UP, ForgeDirection.UP, ForgeDirection.UNKNOWN };
	private static ForgeDirection[] orthX = { ForgeDirection.WEST, ForgeDirection.WEST, ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.UNKNOWN };
	private boolean hasCStoneGen = false;
	private int genCStoneCounter = 0;
	private long checkTimer = 0L;
	private IRecipe cachedRecipe;
	private int prevStack;
	private boolean delay;
	private boolean isDirty;

	public TileEntityTransferNodeInventory()
	{
		super("Inv", new ItemBuffer());
		this.pr = 1.0F;
		this.pg = 0.0F;
		this.pb = 0.0F;
		this.cachedRecipe = null;
		this.prevStack = 0;
		this.delay = false;
		this.isDirty = false;
	}

	public TileEntityTransferNodeInventory(String txt, INodeBuffer buffer)
	{
		super(txt, buffer);
		this.pr = 1.0F;
		this.pg = 0.0F;
		this.pb = 0.0F;
		this.cachedRecipe = null;
		this.prevStack = 0;
		this.delay = false;
		this.isDirty = false;
	}

	public static IRecipe findMatchingRecipe(InventoryCrafting inv, World world)
	{
		for (int i = 0; i < CraftingManager.getInstance().getRecipeList().size(); ++i)
		{
			IRecipe recipe = (IRecipe) CraftingManager.getInstance().getRecipeList().get(i);
			if (recipe.matches(inv, world))
				return recipe;
		}

		return null;
	}

	private static int getFirstExtractableItemStackSlot(IInventory inv, int side)
	{
		for (int i : XUHelper.getInventorySideSlots(inv, side))
		{
			ItemStack item = inv.getStackInSlot(i);
			if (item != null && item.stackSize > 0 && (!(inv instanceof ISidedInventory) || ((ISidedInventory) inv).canExtractItem(i, item, side)))
			{
				if (!item.getItem().hasContainerItem(item))
					return i;

				ItemStack t = item.getItem().getContainerItem(item);

				for (int j : XUHelper.getInventorySideSlots(inv, side))
				{
					if ((j != i && inv.getStackInSlot(j) == null || j == i && item.stackSize == 1) && inv.isItemValidForSlot(j, t) && (!(inv instanceof ISidedInventory) || ((ISidedInventory) inv).canInsertItem(i, t, side)))
						return i;
				}
			}
		}

		return -1;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
    
    

	@Override
	public void processBuffer()
	{
		if (this.worldObj != null && !this.worldObj.isRemote)
		{
			if (this.coolDown > 0)
				this.coolDown -= this.stepCoolDown;

			if (this.checkRedstone())
    
			if (EventConfig.skipTicksTransferNode)
				if (++this.ticksSkipped > EventConfig.skipTicksAmountTransferNode)
					this.ticksSkipped = 0;
				else
    

			this.startDelayMarkDirty();

			for (; this.coolDown <= 0; this.loadbuffer())
			{
				this.coolDown += baseMaxCoolDown;
				if (this.handleInventories())
					this.advPipeSearch();
			}

			this.finishMarkDirty();
		}

	}

	public void loadbuffer()
    
		if (this.buffer == null)
    

		if (this.buffer.getBuffer() == null || ((ItemStack) this.buffer.getBuffer()).stackSize < ((ItemStack) this.buffer.getBuffer()).getMaxStackSize())
		{
			int dir = this.getBlockMetadata() % 6;
			IInventory inv = TNHelper.getInventory(this.worldObj.getTileEntity(this.xCoord + Facing.offsetsXForSide[dir], this.yCoord + Facing.offsetsYForSide[dir], this.zCoord + Facing.offsetsZForSide[dir]));
			if (inv != null)
				if (inv instanceof ISidedInventory)
				{
					dir = Facing.oppositeSide[dir];
					ISidedInventory invs = (ISidedInventory) inv;
    
					if (aint == null)
    

					for (int i = 0; i < aint.length && (this.buffer.getBuffer() == null || ((ItemStack) this.buffer.getBuffer()).stackSize < ((ItemStack) this.buffer.getBuffer()).getMaxStackSize()); ++i)
					{
						ItemStack stack = invs.getStackInSlot(aint[i]);
						if (stack != null && stack.stackSize > 0 && (this.buffer.getBuffer() == null || XUHelper.canItemsStack((ItemStack) this.buffer.getBuffer(), stack, false, true)) && invs.canExtractItem(aint[i], stack, dir))
						{
							ItemStack stack1 = stack.copy();
							ItemStack stack2;
							if (this.upgradeNo(3) == 0)
								stack2 = XUHelper.invInsert(this, invs.decrStackSize(aint[i], 1), -1);
							else
								stack2 = XUHelper.invInsert(this, invs.getStackInSlot(aint[i]), -1);

							if (this.upgradeNo(3) == 0)
							{
								if (stack2 == null)
								{
									inv.markDirty();
									return;
								}

								inv.setInventorySlotContents(aint[i], stack1);
							}
							else
								inv.setInventorySlotContents(aint[i], stack2);

							inv.markDirty();
						}
					}
				}
				else
				{
					int j = inv.getSizeInventory();

					for (int k = 0; k < j && (this.buffer.getBuffer() == null || ((ItemStack) this.buffer.getBuffer()).stackSize < ((ItemStack) this.buffer.getBuffer()).getMaxStackSize()); ++k)
					{
						ItemStack stack = inv.getStackInSlot(k);
						if (stack != null && (this.buffer.getBuffer() == null || XUHelper.canItemsStack((ItemStack) this.buffer.getBuffer(), stack, false, true)))
						{
							ItemStack stack1 = stack.copy();
							ItemStack stack2;
							if (this.upgradeNo(3) == 0)
								stack2 = XUHelper.invInsert(this, inv.decrStackSize(k, 1), -1);
							else
								stack2 = XUHelper.invInsert(this, inv.getStackInSlot(k), -1);

							if (stack2 != null && stack2.stackSize == 0)
								stack2 = null;

							if (this.upgradeNo(3) == 0)
							{
								if (stack2 == null)
								{
									inv.markDirty();
									return;
								}

								inv.setInventorySlotContents(k, stack1);
							}
							else
								inv.setInventorySlotContents(k, stack2);

							inv.markDirty();
						}
					}
				}
			else if (this.upgradeNo(2) > 0)
			{
				if (this.genCobble())
					return;

				if (this.doCraft())
					return;

				this.suckItems();
			}

		}
	}

	public void startDelayMarkDirty()
	{
		if (this.delay)
			throw new RuntimeException("Tile Entity to be marked for delayMarkDirty is already marked as such");
		else
		{
			this.delay = true;
			this.isDirty = false;
		}
	}

	public void finishMarkDirty()
	{
		if (this.isDirty)
			super.markDirty();

		this.delay = false;
		this.isDirty = false;
	}

	@Override
	public void markDirty()
	{
		if (!this.delay)
		{
			this.isDirty = false;
			super.markDirty();
		}
		else
			this.isDirty = true;

	}

	private void suckItems()
	{
		if (this.buffer.getBuffer() == null || ((ItemStack) this.buffer.getBuffer()).stackSize < ((ItemStack) this.buffer.getBuffer()).getMaxStackSize())
		{
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() % 6);
			double r = Math.log(this.upgradeNo(2)) / Math.log(2.0D);
			if (r > 3.5D)
				r = 3.5D;

			for (Object o : this.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1).offset(dir.offsetX * (1.0D + r), dir.offsetY * (1.0D + r), dir.offsetZ * (1.0D + r)).expand(r, r, r)))
			{
				EntityItem item = (EntityItem) o;
				ItemStack itemstack = item.getEntityItem();
				if (item.isEntityAlive() && itemstack != null && (this.buffer.getBuffer() == null || XUHelper.canItemsStack((ItemStack) this.buffer.getBuffer(), itemstack, false, true)))
				{
					ItemStack itemstack1 = itemstack.copy();
					if (this.upgradeNo(3) == 0)
						itemstack1.stackSize = 1;

					int n = itemstack1.stackSize;
					itemstack1 = XUHelper.invInsert(this, itemstack1, -1);
					if (itemstack1 != null)
						n -= itemstack1.stackSize;

					if (n > 0)
					{
						itemstack.stackSize -= n;
						if (itemstack.stackSize > 0)
							item.setEntityItemStack(itemstack);
						else
							item.setDead();

						if (this.upgradeNo(3) == 0)
							return;
					}
				}
			}
		}

	}

	private boolean doCraft()
	{
		if (this.buffer.getBuffer() == null || ((ItemStack) this.buffer.getBuffer()).stackSize < ((ItemStack) this.buffer.getBuffer()).getMaxStackSize())
		{
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() % 6);
			boolean craft = this.worldObj.getBlock(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ) == Blocks.crafting_table;
			if (!craft)
				return false;

			ForgeDirection dirX = orthX[dir.ordinal()];
			ForgeDirection dirY = orthY[dir.ordinal()];
			int[] slots = new int[9];
			IInventory[] inventories = new IInventory[9];
			boolean isEmpty = true;

			for (int dx = -1; dx <= 1; ++dx)
			{
				for (int dy = -1; dy <= 1; ++dy)
				{
					TileEntity tile = this.worldObj.getTileEntity(this.xCoord + dir.offsetX * 2 + dirX.offsetX * dx + dirY.offsetX * dy, this.yCoord + dir.offsetY * 2 + dirX.offsetY * dx + dirY.offsetY * dy, this.zCoord + dir.offsetZ * 2 + dirX.offsetZ * dx + dirY.offsetZ * dy);
					int j = dx + 1 + 3 * (-dy + 1);
					boolean a = tile instanceof IInventory;
					boolean b = a || tile instanceof IFluidHandler;
					if (b)
					{
						if (a)
							inventories[j] = (IInventory) tile;
						else
							inventories[j] = new LiquidInventory((IFluidHandler) tile, dir.getOpposite());

						int i = getFirstExtractableItemStackSlot(inventories[j], dir.getOpposite().ordinal());
						slots[j] = i;
						if (i >= 0)
						{
							ItemStack item = inventories[j].getStackInSlot(i);
							crafting.setInventorySlotContents(j, item.copy());
							isEmpty = false;
						}
						else
							crafting.setInventorySlotContents(j, null);
					}
					else
					{
						inventories[j] = null;
						crafting.setInventorySlotContents(j, null);
					}
				}
			}

			if (isEmpty)
				return true;

			if (this.cachedRecipe == null || !this.cachedRecipe.matches(crafting, this.worldObj) || this.cachedRecipe.getCraftingResult(crafting) == null)
			{
				int p = crafting.hashCode();
				if (p == this.prevStack && this.prevStack != 0 && this.rand.nextInt(10) > 0)
					return true;

				this.prevStack = p;
				IRecipe r = findMatchingRecipe(crafting, this.worldObj);
				if (r == null || r.getCraftingResult(crafting) == null || !this.isItemValidForSlot(0, r.getCraftingResult(crafting)))
					return true;

				this.cachedRecipe = r;
			}

			ItemStack stack = this.cachedRecipe.getCraftingResult(crafting);
			this.prevStack = 0;
			if (this.buffer.getBuffer() != null)
			{
				if (!XUHelper.canItemsStack(stack, (ItemStack) this.buffer.getBuffer(), false, true, false))
					return true;

				if (stack.stackSize + ((ItemStack) this.buffer.getBuffer()).stackSize > stack.getMaxStackSize())
					return true;
			}

			if (!this.isItemValidForSlot(0, stack))
				return true;

			ItemStack[] items = new ItemStack[9];

			for (int i = 0; i < 9; ++i)
			{
				if (inventories[i] != null && slots[i] >= 0)
				{
					ItemStack c = inventories[i].getStackInSlot(slots[i]);
					boolean flag = false;
					if (c == null || !XUHelper.canItemsStack(crafting.getStackInSlot(i), c))
						flag = true;

					if (!flag)
					{
						items[i] = inventories[i].decrStackSize(slots[i], 1);
						if (items[i] != null && items[i].stackSize != 1)
							flag = true;
					}

					if (flag)
					{
						for (int j = 0; j <= i; ++j)
						{
							if (items[j] != null && inventories[j] != null)
							{
								items[j] = XUHelper.invInsert(inventories[j], items[j], dir.getOpposite().ordinal());
								if (items[j] != null)
									XUHelper.dropItem(this.getWorldObj(), this.getNodeX(), this.getNodeY(), this.getNodeZ(), items[j]);
							}
						}

						return true;
					}

					if (c.getItem().hasContainerItem(c))
					{
						ItemStack t = c.getItem().getContainerItem(c);
						if (t != null && (!t.isItemStackDamageable() || t.getItemDamage() <= t.getMaxDamage()))
							XUHelper.invInsert(inventories[i], t, dir.getOpposite().ordinal());
					}
				}
			}

			XUHelper.invInsert(this, stack, -1);
		}

		return true;
	}

	private boolean genCobble()
	{
		if (ExtraUtils.disableCobblegen)
			return false;
		else
		{
			if (this.buffer.getBuffer() == null || ((ItemStack) this.buffer.getBuffer()).getItem() == Item.getItemFromBlock(Blocks.cobblestone) && ((ItemStack) this.buffer.getBuffer()).stackSize < 64)
			{
				int dir = this.getBlockMetadata() % 6;
				this.genCStoneCounter = (this.genCStoneCounter + 1) % (1 + this.upgradeNo(0));
				if (this.genCStoneCounter != 0)
					return false;

				if (this.worldObj.getTotalWorldTime() - this.checkTimer > 100L)
				{
					this.checkTimer = this.worldObj.getTotalWorldTime();
					this.hasCStoneGen = false;
					if (this.worldObj.getBlock(this.xCoord + Facing.offsetsXForSide[dir], this.yCoord + Facing.offsetsYForSide[dir], this.zCoord + Facing.offsetsZForSide[dir]) == Blocks.cobblestone)
					{
						boolean hasLava = false;
						boolean hasWater = false;

						for (int i = 2; (!hasWater || !hasLava) && i < 6; ++i)
						{
							hasWater |= this.worldObj.getBlock(this.xCoord + Facing.offsetsXForSide[dir] + Facing.offsetsXForSide[i], this.yCoord + Facing.offsetsYForSide[dir], this.zCoord + Facing.offsetsZForSide[dir] + Facing.offsetsZForSide[i]).getMaterial() == Material.water;
							hasLava |= this.worldObj.getBlock(this.xCoord + Facing.offsetsXForSide[dir] + Facing.offsetsXForSide[i], this.yCoord + Facing.offsetsYForSide[dir], this.zCoord + Facing.offsetsZForSide[dir] + Facing.offsetsZForSide[i]).getMaterial() == Material.lava;
						}

						if (hasWater && hasLava)
							this.hasCStoneGen = true;
					}
				}

				if (this.hasCStoneGen)
				{
					if (this.buffer.getBuffer() == null)
						this.buffer.setBuffer(new ItemStack(Blocks.cobblestone, this.upgradeNo(2)));
					else
					{
						ItemStack var10000 = (ItemStack) this.buffer.getBuffer();
						var10000.stackSize += 1 + this.upgradeNo(2);
						if (((ItemStack) this.buffer.getBuffer()).stackSize > 64)
							((ItemStack) this.buffer.getBuffer()).stackSize = 64;
					}

					return true;
				}
			}

			return false;
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return i == 0 ? (ItemStack) this.buffer.getBuffer() : null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (i != 0)
			return null;
		else if (this.buffer.getBuffer() != null)
			if (((ItemStack) this.buffer.getBuffer()).stackSize <= j)
			{
				ItemStack itemstack = (ItemStack) this.buffer.getBuffer();
				this.buffer.setBuffer(null);
				this.markDirty();
				return itemstack;
			}
			else
			{
				ItemStack itemstack = ((ItemStack) this.buffer.getBuffer()).splitStack(j);
				if (((ItemStack) this.buffer.getBuffer()).stackSize == 0)
					this.buffer.setBuffer(null);

				this.markDirty();
				return itemstack;
			}
		else
			return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		if (i != 0)
			return null;
		else if (this.buffer.getBuffer() != null)
		{
			ItemStack itemstack = (ItemStack) this.buffer.getBuffer();
			this.buffer.setBuffer(null);
			return itemstack;
		}
		else
			return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.buffer.setBuffer(itemstack);
		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
			itemstack.stackSize = this.getInventoryStackLimit();

		this.markDirty();
	}

	@Override
	public String getInventoryName()
	{
		return "gui.transferNode";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
    
    
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		for (int j = 0; j < this.upgrades.getSizeInventory(); ++j)
		{
			ItemStack upgrade = this.upgrades.getStackInSlot(j);
			if (upgrade != null && ItemNodeUpgrade.isFilter(upgrade) && !ItemNodeUpgrade.matchesFilterItem(itemstack, upgrade))
				return false;
		}

		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int j)
	{
		return j >= 0 && j < 6 && j != this.getBlockMetadata() % 6 ? nullcontents : contents;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return (j < 0 || j >= 6 || j == this.getBlockMetadata() % 6) && this.isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return false;
	}

	@Override
	public TileEntityTransferNodeInventory getNode()
	{
		return this;
	}

	@Override
	public BoxModel getModel(ForgeDirection dir)
	{
		BoxModel boxes = new BoxModel();
		boxes.add(new Box(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.0625F, 0.9375F).rotateToSide(dir).setTextureSides(dir.ordinal(), BlockTransferNode.nodeBase));
		boxes.add(new Box(0.1875F, 0.0625F, 0.1875F, 0.8125F, 0.25F, 0.8125F).rotateToSide(dir));
		boxes.add(new Box(0.3125F, 0.25F, 0.3125F, 0.6875F, 0.375F, 0.6875F).rotateToSide(dir));
		boxes.add(new Box(0.375F, 0.25F, 0.375F, 0.625F, 0.375F, 0.625F).rotateToSide(dir).setTexture(BlockTransferNode.nodeBase).setAllSideInvisible().setSideInvisible(dir.getOpposite().ordinal(), Boolean.FALSE));
		return boxes;
	}
}
