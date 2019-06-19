package cofh.thermalexpansion.block.apparatus;

import cofh.api.tileentity.IInventoryConnection;
import cofh.core.gui.container.ContainerTileAugmentable;
import cofh.core.util.RegistrySocial;
import cofh.core.util.core.SideConfig;
import cofh.core.util.core.SlotConfig;
import cofh.core.util.helpers.SecurityHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.client.apparatus.GuiCollector;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class TileCollector extends TileApparatusBase implements IInventoryConnection, ITickable
{
	private static final int TYPE = BlockApparatus.Type.COLLECTOR.getMetadata();
	public static final float[] DEFAULT_DROP_CHANCES = { 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F };
	private boolean ignoreTeam = true;
	private boolean ignoreFriends = true;
	private boolean ignoreOwner = true;
	protected boolean augmentEntityCollection;

	public static void initialize()
	{
		TileApparatusBase.SIDE_CONFIGS[TYPE] = new SideConfig();
		TileApparatusBase.SIDE_CONFIGS[TYPE].numConfig = 2;
		TileApparatusBase.SIDE_CONFIGS[TYPE].slotGroups = new int[][] { new int[0], new int[0] };
		TileApparatusBase.SIDE_CONFIGS[TYPE].sideTypes = new int[] { 0, 4 };
		TileApparatusBase.SIDE_CONFIGS[TYPE].defaultSides = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
		TileApparatusBase.SLOT_CONFIGS[TYPE] = new SlotConfig();
		TileApparatusBase.SLOT_CONFIGS[TYPE].allowInsertionSlot = new boolean[0];
		TileApparatusBase.SLOT_CONFIGS[TYPE].allowExtractionSlot = new boolean[0];
		GameRegistry.registerTileEntity(TileCollector.class, "thermalexpansion:apparatus_collector");
	}

	public static void config()
	{
		String category = "Apparatus.Collector";
		BlockApparatus.enable[TYPE] = ThermalExpansion.CONFIG.get(category, "Enable", true);
	}

	public TileCollector()
	{
		this.inventory = new ItemStack[1];
		Arrays.fill(this.inventory, ItemStack.EMPTY);
		this.radius = 1;
		this.depth = 1;
	}

	@Override
	public int getType()
	{
		return TYPE;
	}

	@Override
	protected void activate()
	{
		this.collectItemsInArea();
	}

	private void collectItemsInArea()
	{
		AxisAlignedBB area;
		switch (this.facing)
		{
			case 0:
				area = new AxisAlignedBB(this.pos.add(-this.radius, -1 - this.depth, -this.radius), this.pos.add(1 + this.radius, 0, 1 + this.radius));
				break;
			case 1:
				area = new AxisAlignedBB(this.pos.add(-this.radius, 1, -this.radius), this.pos.add(1 + this.radius, 2 + this.depth, 1 + this.radius));
				break;
			case 2:
				area = new AxisAlignedBB(this.pos.add(-this.radius, -this.radius, -1 - this.depth), this.pos.add(1 + this.radius, 1 + this.radius, 0));
				break;
			case 3:
				area = new AxisAlignedBB(this.pos.add(-this.radius, -this.radius, 1), this.pos.add(1 + this.radius, 1 + this.radius, 2 + this.depth));
				break;
			case 4:
				area = new AxisAlignedBB(this.pos.add(-1 - this.depth, -this.radius, -this.radius), this.pos.add(0, 1 + this.radius, 1 + this.radius));
				break;
			default:
				area = new AxisAlignedBB(this.pos.add(1, -this.radius, -this.radius), this.pos.add(2 + this.depth, 1 + this.radius, 1 + this.radius));
		}

		for (EntityItem entityItem : this.world.getEntitiesWithinAABB(EntityItem.class, area))
		{
			if (!entityItem.isDead && entityItem.getItem().getCount() > 0)
			{
				this.stuffedItems.add(entityItem.getItem());
				entityItem.world.removeEntity(entityItem);
			}
		}

		if (this.augmentEntityCollection)
		{
			List<EntityLivingBase> entityLiving = this.world.getEntitiesWithinAABB(EntityLivingBase.class, area);
			Iterator var11 = entityLiving.iterator();

			while (true)
			{
				EntityLivingBase entity;
				float[] dropChances;
				while (true)
				{
					if (!var11.hasNext())
						return;

					entity = (EntityLivingBase) var11.next();
					dropChances = DEFAULT_DROP_CHANCES;
					if (entity instanceof EntityLiving)
					{
						EntityLiving living = (EntityLiving) entity;
						dropChances = new float[] { living.inventoryHandsDropChances[0], living.inventoryHandsDropChances[1], living.inventoryArmorDropChances[0], living.inventoryArmorDropChances[1], living.inventoryArmorDropChances[2], living.inventoryArmorDropChances[3] };
						break;
					}

					if (!this.isSecured() || !(entity instanceof EntityPlayer) || !this.doNotCollectItemsFrom((EntityPlayer) entity))
						break;
				}

				for (int i = 0; i < 6; ++i)
				{
					EntityEquipmentSlot slot = EntityEquipmentSlot.values()[i];
					ItemStack equipmentInSlot = entity.getItemStackFromSlot(slot);
					if (!equipmentInSlot.isEmpty() && dropChances[i] >= 1.0F)
					{
						if (this.fake.cantAttack(entity))
							break;

						this.stuffedItems.add(equipmentInSlot);
						entity.setItemStackToSlot(slot, ItemStack.EMPTY);
					}
				}
			}
		}
	}

	private boolean doNotCollectItemsFrom(EntityPlayer player)
	{
		String name = player.getName();
		UUID ownerID = this.owner.getId();
		UUID otherID = SecurityHelper.getID(player);
		return ownerID.equals(otherID) ? this.ignoreOwner : this.ignoreFriends && RegistrySocial.playerHasAccess(name, this.owner);
	}

	@Override
	public Object getGuiClient(InventoryPlayer inventory)
	{
		return new GuiCollector(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory)
	{
		return new ContainerTileAugmentable(inventory, this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		return super.writeToNBT(nbt);
	}

	@Override
	public ConnectionType canConnectInventory(EnumFacing from)
	{
		return from != null && from.ordinal() != this.facing && this.sideCache[from.ordinal()] == 1 ? ConnectionType.FORCE : ConnectionType.DEFAULT;
	}
}
