package cofh.thermalexpansion.block.apparatus;

import cofh.api.core.IAccelerable;
import cofh.api.item.IAugmentItem.AugmentType;
import cofh.api.tileentity.IInventoryConnection;
import cofh.core.block.TilePowered;
import cofh.core.entity.FakePlayerCore;
import cofh.core.util.core.EnergyConfig;
import cofh.core.util.core.SideConfig;
import cofh.core.util.core.SlotConfig;
import cofh.core.util.helpers.AugmentHelper;
import cofh.core.util.helpers.BlockHelper;
import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.init.TETextures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.LinkedList;

public abstract class TileApparatusBase extends TilePowered implements IAccelerable, IInventoryConnection, ITickable
{
	public static final SideConfig[] SIDE_CONFIGS = new SideConfig[BlockApparatus.Type.values().length];
	public static final SlotConfig[] SLOT_CONFIGS = new SlotConfig[BlockApparatus.Type.values().length];
	public static final EnergyConfig[] ENERGY_CONFIGS = new EnergyConfig[BlockApparatus.Type.values().length];
	public static final HashSet[] VALID_AUGMENTS = new HashSet[BlockApparatus.Type.values().length];
	public static final int[] LIGHT_VALUES = new int[BlockApparatus.Type.values().length];
	protected static boolean enableSecurity = true;
	protected static final HashSet<String> VALID_AUGMENTS_BASE = new HashSet<>();
	int processMax;
	int processRem;
	boolean hasModeAugment;
	FakePlayerCore fakePlayer;
	LinkedList<ItemStack> stuffedItems = new LinkedList<>();
	EnergyConfig energyConfig;
	int depth = 0;
	int radius = 0;

	public static void config()
	{
		String comment = "Enable this to allow for Apparatus to be securable.";
	}

	@Override
	public void onLoad()
	{
		if (ServerHelper.isServerWorld(this.world))
		{
			this.fakePlayer = new FakePlayerCore((WorldServer) this.world, this.fake.getProfile());
		}
	}

	public TileApparatusBase()
	{
		this.sideConfig = SIDE_CONFIGS[this.getType()];
		this.slotConfig = SLOT_CONFIGS[this.getType()];
		this.enableAutoOutput = true;
		this.setDefaultSides();
	}

	@Override
	protected Object getMod()
	{
		return ThermalExpansion.instance;
	}

	@Override
	protected String getModVersion()
	{
		return "5.5.3";
	}

	@Override
	protected String getTileName()
	{
		return "tile.thermalexpansion.apparatus." + BlockApparatus.Type.values()[this.getType()].getName() + ".name";
	}

	@Override
	public boolean enableSecurity()
	{
		return enableSecurity;
	}

	@Override
	public boolean sendRedstoneUpdates()
	{
		return true;
	}

	@Override
	public void setDefaultSides()
	{
		this.sideCache = this.getDefaultSides();
		this.sideCache[this.facing] = 0;
		this.sideCache[this.facing ^ 1] = 1;
	}

	@Override
	public void update()
	{
		if (this.world.getTotalWorldTime() % 16L == 0L && this.redstoneControlOrDisable())
		{
			if (!this.isStuffingEmpty())
				this.outputBuffer();

			if (this.isStuffingEmpty())
				this.activate();
		}

		this.chargeEnergy();
	}

	protected void activate()
	{
	}

	protected boolean isStuffingEmpty()
	{
		return this.stuffedItems.isEmpty();
	}

	protected boolean outputBuffer()
	{
		if (this.getTransferOut())
			for (int i = 0; i < 6; ++i)
			{
				if (this.sideCache[i] == 1)
				{
					EnumFacing side = EnumFacing.VALUES[i];
					TileEntity curTile = BlockHelper.getAdjacentTileEntity(this, side);
					if (InventoryHelper.isAccessibleOutput(curTile, side))
					{
						LinkedList<ItemStack> newStuffed = new LinkedList<>();

						for (ItemStack curItem : this.stuffedItems)
						{
							if (curItem.isEmpty())
								curItem = ItemStack.EMPTY;
							else
								curItem = InventoryHelper.addToInventory(curTile, side, curItem);

							if (!curItem.isEmpty())
								newStuffed.add(curItem);
						}

						this.stuffedItems = newStuffed;
					}
				}
			}

		return this.isStuffingEmpty();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("StuffedInv", 10);
		this.stuffedItems.clear();

		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound compound = list.getCompoundTagAt(i);
			this.stuffedItems.add(new ItemStack(compound));
		}

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();

		for (ItemStack item : this.stuffedItems)
		{
			if (!item.isEmpty())
			{
				NBTTagCompound compound = new NBTTagCompound();
				item.writeToNBT(compound);
				list.appendTag(compound);
			}
		}

		nbt.setTag("StuffedInv", list);
		return nbt;
	}

	@Override
	protected void preAugmentInstall()
	{
		this.radius = 0;
		this.depth = 0;
	}

	@Override
	protected void postAugmentInstall()
	{
	}

	@Override
	protected boolean isValidAugment(AugmentType type, String id)
	{
		return (type != AugmentType.CREATIVE || this.isCreative) && (type != AugmentType.MODE || !this.hasModeAugment) && (VALID_AUGMENTS_BASE.contains(id) || super.isValidAugment(type, id));
	}

	@Override
	protected boolean installAugmentToSlot(int slot)
	{
		String id = AugmentHelper.getAugmentIdentifier(this.augments[slot]);
		if ("apparatusDepth".equals(id))
		{
			++this.depth;
			return true;
		}
		if ("apparatusRadius".equals(id))
		{
			++this.radius;
			return true;
		}
		return super.installAugmentToSlot(slot);
	}

	@Override
	public int updateAccelerable()
	{
		return 0;
	}

	@Override
	public ConnectionType canConnectInventory(EnumFacing from)
	{
		return from != null && from.ordinal() != this.facing && this.sideCache[from.ordinal()] == 1 ? ConnectionType.FORCE : ConnectionType.DEFAULT;
	}

	@Override
	public boolean allowYAxisFacing()
	{
		return true;
	}

	@Override
	public boolean setFacing(int side, boolean alternate)
	{
		if (side >= 0 && side <= 5)
		{
			this.facing = (byte) side;
			this.sideCache[this.facing] = 0;
			this.sideCache[this.facing ^ 1] = 1;
			this.markChunkDirty();
			this.sendTilePacket(Side.CLIENT);
			return true;
		}
		return false;
	}

	@Override
	public int getNumPasses()
	{
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTexture(int side, int pass)
	{
		return pass == 0 ? side != this.facing ? TETextures.APPARATUS_SIDE : this.redstoneControlOrDisable() ? TETextures.APPARATUS_ACTIVE[this.getType()] : TETextures.APPARATUS_FACE[this.getType()] : side < 6 ? TETextures.CONFIG[this.sideConfig.sideTypes[this.sideCache[side]]] : TETextures.APPARATUS_SIDE;
	}

	static
	{
		VALID_AUGMENTS_BASE.add("apparatusDepth");
		VALID_AUGMENTS_BASE.add("apparatusRadius");
	}
}
