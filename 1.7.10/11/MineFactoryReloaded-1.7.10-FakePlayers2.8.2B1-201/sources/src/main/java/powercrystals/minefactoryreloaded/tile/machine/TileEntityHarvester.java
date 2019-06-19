package powercrystals.minefactoryreloaded.tile.machine;

import cofh.core.util.fluid.FluidTankAdv;
import cofh.lib.util.position.Area;
import cofh.lib.util.position.BlockPosition;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.HarvestType;
import powercrystals.minefactoryreloaded.api.IFactoryHarvestable;
import powercrystals.minefactoryreloaded.core.*;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiHarvester;
import powercrystals.minefactoryreloaded.gui.container.ContainerHarvester;
import powercrystals.minefactoryreloaded.setup.MFRConfig;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

import java.util.*;
import java.util.Map.Entry;

public class TileEntityHarvester extends TileEntityFactoryPowered implements ITankContainerBucketable
{
	private static boolean skip = false;
	private static Map<String, Boolean> DEFAULT_SETTINGS;
	private Map<String, Boolean> _settings;
	private Map<String, Boolean> _immutableSettings;
	private Random _rand;
	private IHarvestManager _treeManager;
	private BlockPosition _lastTree;

	public TileEntityHarvester()
	{
		super(Machine.Harvester);
		createHAM(this, 1);
		this.setManageSolids(true);
		this._settings = new HashMap();
		this._settings.putAll(DEFAULT_SETTINGS);
		this._immutableSettings = Collections.unmodifiableMap(this._settings);
		this._rand = new Random();
		this.setCanRotate(true);
		skip = MFRConfig.harvesterSkip.getBoolean(false);
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		if (this._treeManager != null)
			this._treeManager.free();

		this._lastTree = null;
	}

	@Override
	public void validate()
	{
		super.validate();
		if (!this.worldObj.isRemote)
		{
			createHAM(this, 1);
			this.onFactoryInventoryChanged();
			if (this._treeManager != null && this._areaManager.getHarvestArea().contains(this._treeManager.getOrigin()))
				this._treeManager.setWorld(this.worldObj);
			else
				this._treeManager = new TreeHarvestManager(this.worldObj, new Area(new BlockPosition(this), 0, 0, 0), HarvestMode.FruitTree, this._immutableSettings);
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiHarvester(this.getContainer(var1), this);
	}

	@Override
	public ContainerHarvester getContainer(InventoryPlayer var1)
	{
		return new ContainerHarvester(this, var1);
	}

	public Map<String, Boolean> getSettings()
	{
		return this._settings;
	}

	public Map<String, Boolean> getImmutableSettings()
	{
		return this._immutableSettings;
	}

	@Override
	protected boolean shouldPumpLiquid()
	{
		return true;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 5 + this.getExtraIdleTime(10);
	}

	protected int getExtraIdleTime(int var1)
	{
		return this._tanks[0].getFluidAmount() * var1 / this._tanks[0].getCapacity();
	}

	@Override
	public boolean activateMachine()
	{
		BlockPosition pos = this.getNextHarvest();
		if (pos == null)
		{
			this.setIdleTicks(this.getIdleTicksMax());
			return false;
    
		if (this.fake.cantBreak(pos.x, pos.y, pos.z))
		{
			this.setIdleTicks(this.getExtraIdleTime(10));
			return false;
    

		Block block = this.worldObj.getBlock(pos.x, pos.y, pos.z);
		int meta = this.worldObj.getBlockMetadata(pos.x, pos.y, pos.z);
		IFactoryHarvestable harvestable = MFRRegistry.getHarvestables().get(block);
		List drop = harvestable.getDrops(this.worldObj, this._rand, this._immutableSettings, pos.x, pos.y, pos.z);
		harvestable.preHarvest(this.worldObj, pos.x, pos.y, pos.z);
		if (drop instanceof ArrayList)
			ForgeEventFactory.fireBlockHarvesting((ArrayList) drop, this.worldObj, block, pos.x, pos.y, pos.z, meta, 0, 1.0F, this._settings.get("silkTouch") == Boolean.TRUE, null);

		if (harvestable.breakBlock())
		{
			if (!this.worldObj.setBlock(pos.x, pos.y, pos.z, Blocks.air, 0, 2))
				return false;

			if (this._settings.get("playSounds") == Boolean.TRUE)
				this.worldObj.playAuxSFXAtEntity(null, 2001, pos.x, pos.y, pos.z, Block.getIdFromBlock(block) + (meta << 12));
		}

		this.setIdleTicks(this.getExtraIdleTime(10));
		this.doDrop(drop);
		this._tanks[0].fill(FluidRegistry.getFluidStack("sludge", 10), true);
		harvestable.postHarvest(this.worldObj, pos.x, pos.y, pos.z);
		return true;
	}

	private BlockPosition getNextHarvest()
	{
		if (!this._treeManager.getIsDone())
			return this.getNextTreeSegment(this._lastTree, false);
		BlockPosition var1 = this._areaManager.getNextBlock();
		this._lastTree = null;
		if (skip)
		{
			int var2 = this.getExtraIdleTime(10);
			if (var2 > 0 && var2 > this._rand.nextInt(15))
				return null;
		}

		if (!this.worldObj.blockExists(var1.x, var1.y, var1.z))
			return null;
		Block var5 = this.worldObj.getBlock(var1.x, var1.y, var1.z);
		if (!MFRRegistry.getHarvestables().containsKey(var5))
		{
			this._lastTree = null;
			return null;
		}
		this._settings.put("isHarvestingTree", Boolean.FALSE);
		IFactoryHarvestable var3 = MFRRegistry.getHarvestables().get(var5);
		HarvestType var4 = var3.getHarvestType();
		if (var4 == HarvestType.Gourd || var3.canBeHarvested(this.worldObj, this._immutableSettings, var1.x, var1.y, var1.z))
			switch (var4)
			{
				case Gourd:
					return this.getNextAdjacent(var1.x, var1.y, var1.z, var3);
				case Column:
				case LeaveBottom:
					return this.getNextVertical(var1.x, var1.y, var1.z, var4 == HarvestType.Column ? 0 : 1, var3);
				case Tree:
				case TreeFlipped:
				case TreeLeaf:
					return this.getNextTreeSegment(var1, var4 == HarvestType.TreeFlipped);
				case TreeFruit:
				case Normal:
					return var1;
			}

		return null;
	}

	private BlockPosition getNextAdjacent(int var1, int var2, int var3, IFactoryHarvestable var4)
	{
		for (SideOffset var8 : SideOffset.SIDES)
		{
			int var9 = var1 + var8.offsetX;
			int var10 = var2 + var8.offsetY;
			int var11 = var3 + var8.offsetX;
			if (this.worldObj.blockExists(var9, var10, var11) && var4.canBeHarvested(this.worldObj, this._immutableSettings, var9, var10, var11))
				return new BlockPosition(var9, var10, var11);
		}

		return null;
	}

	private BlockPosition getNextVertical(int var1, int var2, int var3, int var4, IFactoryHarvestable var5)
	{
		int var6 = -1;
		int var7 = MFRConfig.verticalHarvestSearchMaxVertical.getInt();
		Block var8 = var5.getPlant();

		for (int var9 = var4; var9 < var7; var6 = var9++)
		{
			Block var10 = this.worldObj.getBlock(var1, var2 + var9, var3);
			if (!var10.equals(var8) || !var5.canBeHarvested(this.worldObj, this._immutableSettings, var1, var2 + var9, var3))
				break;
		}

		return var6 >= 0 ? new BlockPosition(var1, var2 + var6, var3) : null;
	}

	private BlockPosition getNextTreeSegment(BlockPosition var1, boolean var2)
	{
		this._settings.put("isHarvestingTree", Boolean.TRUE);
		if (!var1.equals(this._lastTree) || this._treeManager.getIsDone())
		{
			int var4 = 0;
			int var5 = MFRConfig.treeSearchMaxVertical.getInt();
			if (var2)
			{
				var4 = var5;
				var5 = 0;
			}

			this._lastTree = new BlockPosition(var1);
			Area var6 = new Area(this._lastTree, MFRConfig.treeSearchMaxHorizontal.getInt(), var4, var5);
			this._treeManager.reset(this.worldObj, var6, var2 ? HarvestMode.HarvestTreeInverted : HarvestMode.HarvestTree, this._immutableSettings);
		}

		Map var8 = MFRRegistry.getHarvestables();

		while (!this._treeManager.getIsDone())
		{
			BlockPosition var9 = this._treeManager.getNextBlock();
			this._treeManager.moveNext();
			if (!this.worldObj.blockExists(var9.x, var9.y, var9.z))
				return null;

			Block var3 = this.worldObj.getBlock(var9.x, var9.y, var9.z);
			if (var8.containsKey(var3))
			{
				IFactoryHarvestable var10 = (IFactoryHarvestable) var8.get(var3);
				HarvestType var7 = var10.getHarvestType();
				if (var7 == HarvestType.Tree | var7 == HarvestType.TreeFlipped | var7 == HarvestType.TreeLeaf | var7 == HarvestType.TreeFruit && var10.canBeHarvested(this.worldObj, this._immutableSettings, var9.x, var9.y, var9.z))
					return var9;
			}
		}

		return null;
	}

	@Override
	public boolean allowBucketDrain(ItemStack var1)
	{
		return true;
	}

	@Override
	public int fill(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection var1, int var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	public FluidStack drain(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	protected FluidTankAdv[] createTanks()
	{
		return new FluidTankAdv[] { new FluidTankAdv(4000) };
	}

	@Override
	public void writePortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		NBTTagCompound var3 = new NBTTagCompound();

		for (Entry var5 : this._settings.entrySet())
		{
			String var6 = (String) var5.getKey();
			if ("playSounds" != var6 && "isHarvestingTree" != var6)
				var3.setBoolean(var6, var5.getValue() == Boolean.TRUE);
		}

		var2.setTag("harvesterSettings", var3);
	}

	@Override
	public void readPortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		NBTTagCompound var3 = (NBTTagCompound) var2.getTag("harvesterSettings");
		if (var3 != null)
			for (String var5 : this._settings.keySet())
			{
				if (!"playSounds".equals(var5))
				{
					boolean var6 = var3.getBoolean(var5);
					this._settings.put(var5.intern(), var6);
				}
			}

	}

	@Override
	public void writeItemNBT(NBTTagCompound var1)
	{
		super.writeItemNBT(var1);
		NBTTagCompound var2 = new NBTTagCompound();

		for (Entry var4 : this._settings.entrySet())
		{
			String var5 = (String) var4.getKey();
			if (!("playSounds" == var5 | "isHarvestingTree" == var5) && DEFAULT_SETTINGS.get(var5) != var4.getValue())
				var2.setBoolean(var5, var4.getValue() == Boolean.TRUE);
		}

		if (!var2.hasNoTags())
			var1.setTag("harvesterSettings", var2);

	}

	@Override
	public void writeToNBT(NBTTagCompound var1)
	{
    
    
    
    
			var1.setInteger("bpos", this._areaManager.getPosition());
	}

	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		NBTTagCompound var2 = (NBTTagCompound) var1.getTag("harvesterSettings");
		if (var2 != null)
			for (String var4 : this._settings.keySet())
			{
				if (!"playSounds".equals(var4))
				{
					boolean var5 = var2.getBoolean(var4);
					this._settings.put(var4.intern(), var5);
				}
			}

		if (this._treeManager != null)
			this._treeManager.free();

		this._treeManager = new TreeHarvestManager(var1, this._immutableSettings);
		if (!this._treeManager.getIsDone())
			this._lastTree = this._treeManager.getOrigin();

		this._areaManager.getHarvestArea();
		this._areaManager.setPosition(var1.getInteger("bpos"));
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public int getStartInventorySide(ForgeDirection var1)
	{
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection var1)
	{
		return 0;
	}

	@Override
	public boolean canFill(ForgeDirection var1, Fluid var2)
	{
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection var1, Fluid var2)
	{
		return true;
	}

	@Override
	public int getUpgradeSlot()
	{
		return 0;
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3)
	{
		return var1 == 0 && this.isUsableAugment(var2);
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3)
	{
		return false;
	}

	static
	{
		HashMap var0 = new HashMap();
		var0.put("silkTouch", Boolean.FALSE);
		var0.put("harvestSmallMushrooms", Boolean.FALSE);
		var0.put("playSounds", MFRConfig.playSounds.getBoolean(true));
		var0.put("isHarvestingTree", Boolean.FALSE);
		DEFAULT_SETTINGS = Collections.unmodifiableMap(var0);
	}
}
