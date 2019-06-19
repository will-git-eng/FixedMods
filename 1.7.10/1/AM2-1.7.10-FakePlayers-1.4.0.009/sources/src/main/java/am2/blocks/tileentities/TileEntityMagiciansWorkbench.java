package am2.blocks.tileentities;

import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import am2.api.blocks.IKeystoneLockable;
import am2.blocks.BlockMagiciansWorkbench;
import am2.containers.ContainerMagiciansWorkbench;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityMagiciansWorkbench extends TileEntity implements IInventory, IKeystoneLockable, ISidedInventory
{
	private ItemStack[] inventory = new ItemStack[this.getSizeInventory()];
	public IInventory firstCraftResult = new InventoryCraftResult();
	public IInventory secondCraftResult = new InventoryCraftResult();
	private final LinkedList<TileEntityMagiciansWorkbench.RememberedRecipe> rememberedRecipes = new LinkedList();
	private byte upgradeState = 0;
	public static final byte UPG_CRAFT = 1;
	public static final byte UPG_ADJ_INV = 2;
	private int numPlayersUsing = 0;
	private float drawerOffset = 0.0F;
	private float prevDrawerOffset = 0.0F;
	private static final float drawerIncrement = 0.05F;
	private static final float drawerMax = 0.5F;
	private static final float drawerMin = 0.0F;
	private static final byte REMEMBER_RECIPE = 0;
	private static final byte FORGET_RECIPE = 1;
	private static final byte SYNC_REMEMBERED_RECIPES = 2;
	private static final byte LOCK_RECIPE = 4;

	    
	public final Map<EntityPlayer, ContainerMagiciansWorkbench> containers = new WeakHashMap<EntityPlayer, ContainerMagiciansWorkbench>();
	    

	@Override
	public void updateEntity()
	{
		this.setPrevDrawerOffset(this.getDrawerOffset());
		if (this.numPlayersUsing > 0)
		{
			if (this.getDrawerOffset() == 0.0F)
				;

			if (this.getDrawerOffset() < 0.5F)
				this.setDrawerOffset(this.getDrawerOffset() + 0.05F);
			else
				this.setDrawerOffset(0.5F);
		}
		else
		{
			if (this.getDrawerOffset() == 0.5F)
				super.worldObj.playSoundEffect(super.xCoord + 0.5D, super.yCoord + 0.5D, super.zCoord + 0.5D, "random.chestclosed", 0.5F, super.worldObj.rand.nextFloat() * 0.1F + 0.9F);

			if (this.getDrawerOffset() - 0.05F > 0.0F)
				this.setDrawerOffset(this.getDrawerOffset() - 0.05F);
			else
				this.setDrawerOffset(0.0F);
		}

	}

	@Override
	public boolean receiveClientEvent(int par1, int par2)
	{
		if (par1 == 1)
		{
			this.numPlayersUsing = par2;
			return true;
		}
		else
			return super.receiveClientEvent(par1, par2);
	}

	public float getPrevDrawerOffset()
	{
		return this.prevDrawerOffset;
	}

	public void setPrevDrawerOffset(float prevDrawerOffset)
	{
		this.prevDrawerOffset = prevDrawerOffset;
	}

	public float getDrawerOffset()
	{
		return this.drawerOffset;
	}

	public void setDrawerOffset(float drawerOffset)
	{
		this.drawerOffset = drawerOffset;
	}

	@Override
	public void openInventory()
	{
		if (this.numPlayersUsing < 0)
			this.numPlayersUsing = 0;

		++this.numPlayersUsing;
		super.worldObj.addBlockEvent(super.xCoord, super.yCoord, super.zCoord, this.getBlockType(), 1, this.numPlayersUsing);
	}

	@Override
	public void closeInventory()
	{
		if (this.getBlockType() != null && this.getBlockType() instanceof BlockMagiciansWorkbench)
		{
			--this.numPlayersUsing;
			super.worldObj.addBlockEvent(super.xCoord, super.yCoord, super.zCoord, this.getBlockType(), 1, this.numPlayersUsing);
		}

	}

	public boolean getUpgradeStatus(byte flag)
	{
		return (this.upgradeState & flag) == flag;
	}

	public void setUpgradeStatus(byte flag, boolean set)
	{
		if (set)
			this.upgradeState |= flag;
		else
			this.upgradeState = (byte) (this.upgradeState & ~flag);

		if (!super.worldObj.isRemote)
			super.worldObj.markBlockForUpdate(super.xCoord, super.yCoord, super.zCoord);

	}

	public void rememberRecipe(ItemStack output, ItemStack[] recipeItems, boolean is2x2)
	{
		for (TileEntityMagiciansWorkbench.RememberedRecipe recipe : this.rememberedRecipes)
			if (recipe.output.isItemEqual(output))
			{
				    
				if (!ItemStack.areItemStackTagsEqual(recipe.output, output))
					continue;
				    

				return;
			}

		if (this.popRecipe())
		{
			for (ItemStack stack : recipeItems)
				if (stack != null)
					stack.stackSize = 1;

			this.rememberedRecipes.add(new TileEntityMagiciansWorkbench.RememberedRecipe(output, recipeItems, is2x2));
			super.worldObj.markBlockForUpdate(super.xCoord, super.yCoord, super.zCoord);
		}
	}

	private boolean popRecipe()
	{
		if (this.rememberedRecipes.size() < 8)
			return true;
		else
		{
			for (int index = 0; index < this.rememberedRecipes.size(); ++index)
				if (!this.rememberedRecipes.get(index).isLocked)
				{
					this.rememberedRecipes.remove(index);
					return true;
				}

			return false;
		}
	}

	public LinkedList<TileEntityMagiciansWorkbench.RememberedRecipe> getRememberedRecipeItems()
	{
		return this.rememberedRecipes;
	}

	@Override
	public int getSizeInventory()
	{
		return 48;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return i >= 0 && i < this.getSizeInventory() ? this.inventory[i] : null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.inventory[i] != null)
		{
			if (this.inventory[i].stackSize <= j)
			{
				ItemStack itemstack = this.inventory[i];
				this.inventory[i] = null;
				return itemstack;
			}
			else
			{
				ItemStack itemstack1 = this.inventory[i].splitStack(j);
				if (this.inventory[i].stackSize == 0)
					this.inventory[i] = null;

				return itemstack1;
			}
		}
		else
			return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		if (this.inventory[i] != null)
		{
			ItemStack itemstack = this.inventory[i];
			this.inventory[i] = null;
			return itemstack;
		}
		else
			return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.inventory[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
			itemstack.stackSize = this.getInventoryStackLimit();

	}

	@Override
	public String getInventoryName()
	{
		return "Magician\'s Workbench";
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
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return super.worldObj.getTileEntity(super.xCoord, super.yCoord, super.zCoord) != this ? false : entityplayer.getDistanceSq(super.xCoord + 0.5D, super.yCoord + 0.5D, super.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return i > this.getStorageStart();
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		S35PacketUpdateTileEntity packet = new S35PacketUpdateTileEntity(super.xCoord, super.yCoord, super.zCoord, super.worldObj.getBlockMetadata(super.xCoord, super.yCoord, super.zCoord), compound);
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		this.readFromNBT(pkt.func_148857_g());
	}

	public void setRecipeLocked(int index, boolean locked)
	{
		if (index >= 0 && index < this.rememberedRecipes.size())
			this.rememberedRecipes.get(index).isLocked = locked;

		if (super.worldObj.isRemote)
		{
			AMDataWriter writer = new AMDataWriter();
			writer.add(super.xCoord);
			writer.add(super.yCoord);
			writer.add(super.zCoord);
			writer.add(index);
			writer.add(locked);
			AMNetHandler.INSTANCE.sendPacketToServer((byte) 52, writer.generate());
		}

	}

	public void toggleRecipeLocked(int index)
	{
		if (index >= 0 && index < this.rememberedRecipes.size())
			this.setRecipeLocked(index, !this.rememberedRecipes.get(index).isLocked);

	}

	public int getStorageStart()
	{
		return 18;
	}

	public int getStorageSize()
	{
		return 27;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("ArcaneReconstructorInventory", 10);
		this.inventory = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			String tag = String.format("ArrayIndex", new Object[] { Integer.valueOf(i) });
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < this.inventory.length)
				this.inventory[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
		}

		NBTTagList recall = nbttagcompound.getTagList("rememberedRecipes", 10);
		this.rememberedRecipes.clear();

		for (int i = 0; i < recall.tagCount(); ++i)
		{
			NBTTagCompound rememberedRecipe = recall.getCompoundTagAt(i);
			ItemStack output = ItemStack.loadItemStackFromNBT(rememberedRecipe);
			boolean is2x2 = rememberedRecipe.getBoolean("is2x2");
			NBTTagList componentNBT = rememberedRecipe.getTagList("components", 10);
			ItemStack[] components = new ItemStack[componentNBT.tagCount()];

			for (int n = 0; n < componentNBT.tagCount(); ++n)
			{
				NBTTagCompound componentTAG = componentNBT.getCompoundTagAt(n);
				if (componentTAG.getBoolean("componentExisted"))
				{
					ItemStack component = ItemStack.loadItemStackFromNBT(componentTAG);
					components[n] = component;
				}
				else
					components[n] = null;
			}

			TileEntityMagiciansWorkbench.RememberedRecipe rec = new TileEntityMagiciansWorkbench.RememberedRecipe(output, components, is2x2);
			rec.isLocked = rememberedRecipe.getBoolean("isLocked");
			this.rememberedRecipes.add(rec);
		}

		this.upgradeState = nbttagcompound.getByte("upgradestate");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.inventory.length; ++i)
			if (this.inventory[i] != null)
			{
				String tag = String.format("ArrayIndex", new Object[] { Integer.valueOf(i) });
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte(tag, (byte) i);
				this.inventory[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}

		nbttagcompound.setTag("ArcaneReconstructorInventory", nbttaglist);
		NBTTagList recall = new NBTTagList();
		int count = 0;

		for (TileEntityMagiciansWorkbench.RememberedRecipe recipe : this.rememberedRecipes)
			try
			{
				NBTTagCompound output = new NBTTagCompound();
				recipe.output.writeToNBT(output);
				output.setBoolean("is2x2", recipe.is2x2);
				NBTTagList components = new NBTTagList();

				for (int i = 0; i < recipe.components.length; ++i)
				{
					NBTTagCompound component = new NBTTagCompound();
					component.setBoolean("componentExisted", recipe.components[i] != null);
					if (recipe.components[i] != null)
						recipe.components[i].writeToNBT(component);

					components.appendTag(component);
				}

				output.setTag("components", components);
				output.setBoolean("isLocked", recipe.isLocked);
				recall.appendTag(output);
			}
			catch (Throwable var11)
			{
				;
			}

		nbttagcompound.setTag("rememberedRecipes", recall);
		nbttagcompound.setByte("upgradestate", this.upgradeState);
	}

	@Override
	public ItemStack[] getRunesInKey()
	{
		ItemStack[] runes = new ItemStack[] { this.inventory[45], this.inventory[46], this.inventory[47] };
		return runes;
	}

	@Override
	public boolean keystoneMustBeHeld()
	{
		return false;
	}

	@Override
	public boolean keystoneMustBeInActionBar()
	{
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		int[] slots = new int[this.getStorageSize()];

		for (int i = 0; i < slots.length; ++i)
			slots[i] = i + this.getStorageStart();

		return slots;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return i > this.getStorageStart();
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i > this.getStorageStart();
	}

	public class RememberedRecipe
	{
		public final ItemStack output;
		public final ItemStack[] components;
		private boolean isLocked;
		public final boolean is2x2;

		public RememberedRecipe(ItemStack output, ItemStack[] components, boolean is2x2)
		{
			this.output = output;
			this.components = components;
			this.isLocked = false;
			this.is2x2 = is2x2;
		}

		public void lock()
		{
			this.isLocked = true;
		}

		public void unlock()
		{
			this.isLocked = false;
		}

		public boolean isLocked()
		{
			return this.isLocked;
		}
	}
}
