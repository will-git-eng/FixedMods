/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Jan 24, 2014, 8:03:36 PM (GMT)]
 */
package vazkii.botania.api.subtile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaCollector;

import java.awt.*;
import java.util.List;

/**
 * The basic class for a Generating Flower.
 */
public class SubTileGenerating extends SubTileEntity
{

	public static final int LINK_RANGE = 6;

	private static final String TAG_MANA = "mana";

	private static final String TAG_COLLECTOR_X = "collectorX";
	private static final String TAG_COLLECTOR_Y = "collectorY";
	private static final String TAG_COLLECTOR_Z = "collectorZ";
	private static final String TAG_PASSIVE_DECAY_TICKS = "passiveDecayTicks";

	protected int mana;

	public int redstoneSignal = 0;

	int sizeLastCheck = -1;
	protected TileEntity linkedCollector = null;
	public int knownMana = -1;
	public int passiveDecayTicks;

	BlockPos cachedCollectorCoordinates = null;

	/**
	 * If set to true, redstoneSignal will be updated every tick.
	 */
	public boolean acceptsRedstone()
	{
		return false;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		this.linkCollector();

		if (this.canGeneratePassively())
		{
			int delay = this.getDelayBetweenPassiveGeneration();
			if (delay > 0 && this.ticksExisted % delay == 0 && !this.supertile.getWorld().isRemote)
			{
				if (this.shouldSyncPassiveGeneration())
					this.sync();
				this.addMana(this.getValueForPassiveGeneration());
			}
		}
		this.emptyManaIntoCollector();

		if (this.acceptsRedstone())
		{
			this.redstoneSignal = 0;
			for (EnumFacing dir : EnumFacing.VALUES)
			{
				int redstoneSide = this.supertile.getWorld().getRedstonePower(this.supertile.getPos().offset(dir), dir);
				this.redstoneSignal = Math.max(this.redstoneSignal, redstoneSide);
			}
		}

		if (this.supertile.getWorld().isRemote)
		{
			double particleChance = 1F - (double) this.mana / (double) this.getMaxMana() / 3.5F;
			Color color = new Color(this.getColor());
			if (Math.random() > particleChance)
			{
				Vec3d offset = this.getWorld().getBlockState(this.getPos()).getOffset(this.getWorld(), this.getPos());
				double x = this.getPos().getX() + offset.x;
				double y = this.getPos().getY() + offset.y;
				double z = this.getPos().getZ() + offset.z;
				BotaniaAPI.internalHandler.sparkleFX(this.supertile.getWorld(), x + 0.3 + Math.random() * 0.5, y + 0.5 + Math.random() * 0.5, z + 0.3 + Math.random() * 0.5, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, (float) Math.random(), 5);
			}
		}

		boolean passive = this.isPassiveFlower();
		if (!this.supertile.getWorld().isRemote)
		{
			int muhBalance = BotaniaAPI.internalHandler.getPassiveFlowerDecay();

			if (passive && muhBalance > 0 && this.passiveDecayTicks > muhBalance)
			{
				IBlockState state = this.supertile.getWorld().getBlockState(this.supertile.getPos());
				this.supertile.getWorld().playEvent(2001, this.supertile.getPos(), Block.getStateId(state));
				if (this.supertile.getWorld().getBlockState(this.supertile.getPos().down()).isSideSolid(this.supertile.getWorld(), this.supertile.getPos().down(), EnumFacing.UP))
					this.supertile.getWorld().setBlockState(this.supertile.getPos(), Blocks.DEADBUSH.getDefaultState());
				else
					this.supertile.getWorld().setBlockToAir(this.supertile.getPos());
			}
		}

		if (passive)
			this.passiveDecayTicks++;
	}

	public void linkCollector()
	{
		boolean needsNew = false;
		if (this.linkedCollector == null)
		{
			needsNew = true;

			if (this.cachedCollectorCoordinates != null)
			{
				needsNew = false;
				if (this.supertile.getWorld().isBlockLoaded(this.cachedCollectorCoordinates))
				{
					needsNew = true;
					TileEntity tileAt = this.supertile.getWorld().getTileEntity(this.cachedCollectorCoordinates);
					if (tileAt != null && tileAt instanceof IManaCollector && !tileAt.isInvalid())
					{
						this.linkedCollector = tileAt;
						needsNew = false;
					}
					this.cachedCollectorCoordinates = null;
				}
			}
		}
		else
		{
			TileEntity tileAt = this.supertile.getWorld().getTileEntity(this.linkedCollector.getPos());
			if (tileAt != null && tileAt instanceof IManaCollector)
				this.linkedCollector = tileAt;
		}

		if (needsNew && this.ticksExisted == 1)
		{ // New flowers only
			IManaNetwork network = BotaniaAPI.internalHandler.getManaNetworkInstance();
			int size = network.getAllCollectorsInWorld(this.supertile.getWorld()).size();
			if (BotaniaAPI.internalHandler.shouldForceCheck() || size != this.sizeLastCheck)
			{
				this.linkedCollector = network.getClosestCollector(this.supertile.getPos(), this.supertile.getWorld(), LINK_RANGE);
				this.sizeLastCheck = size;
			}
		}
	}

	public void linkToForcefully(TileEntity collector)
	{
		this.linkedCollector = collector;
	}

	public void addMana(int mana)
	{
		this.mana = Math.min(this.getMaxMana(), this.mana + mana);
	}

	public void emptyManaIntoCollector()
	{
		if (this.linkedCollector != null && this.isValidBinding())
		{
			IManaCollector collector = (IManaCollector) this.linkedCollector;
			if (!collector.isFull() && this.mana > 0)
			{
				int manaval = Math.min(this.mana, collector.getMaxMana() - collector.getCurrentMana());
				this.mana -= manaval;
				collector.recieveMana(manaval);
			}
		}
	}

	public boolean isPassiveFlower()
	{
		return false;
	}

	public boolean shouldSyncPassiveGeneration()
	{
		return false;
	}

	public boolean canGeneratePassively()
	{
		return false;
	}

	public int getDelayBetweenPassiveGeneration()
	{
		return 20;
	}

	public int getValueForPassiveGeneration()
	{
		return 1;
	}

	@Override
	public List<ItemStack> getDrops(List<ItemStack> list)
	{
		List<ItemStack> drops = super.getDrops(list);
		this.populateDropStackNBTs(drops);
		return drops;
	}

	public void populateDropStackNBTs(List<ItemStack> drops)
	{
		if (this.isPassiveFlower() && this.ticksExisted > 0 && BotaniaAPI.internalHandler.getPassiveFlowerDecay() > 0)
		{
			ItemStack drop = drops.get(0);
			if (!drop.isEmpty())
			{
				if (!drop.hasTagCompound())
					drop.setTagCompound(new NBTTagCompound());
				NBTTagCompound cmp = drop.getTagCompound();
				cmp.setInteger(TAG_PASSIVE_DECAY_TICKS, this.passiveDecayTicks);
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, entity, stack);
		if (this.isPassiveFlower())
		{
			NBTTagCompound cmp = stack.getTagCompound();
			this.passiveDecayTicks = cmp.getInteger(TAG_PASSIVE_DECAY_TICKS);
		}
	}

	@Override
	public boolean onWanded(EntityPlayer player, ItemStack wand)
	{
		if (player == null)
			return false;

		if (!player.world.isRemote)
			this.sync();

		this.knownMana = this.mana;
		SoundEvent evt = ForgeRegistries.SOUND_EVENTS.getValue(DING_SOUND_EVENT);
		if (evt != null)
			player.playSound(evt, 0.1F, 1F);

		return super.onWanded(player, wand);
	}

	public int getMaxMana()
	{
		return 20;
	}

	public int getColor()
	{
		return 0xFFFFFF;
	}

	@Override
	public void readFromPacketNBT(NBTTagCompound cmp)
	{
		    
		super.readFromPacketNBT(cmp);
		    

		this.mana = cmp.getInteger(TAG_MANA);
		this.passiveDecayTicks = cmp.getInteger(TAG_PASSIVE_DECAY_TICKS);

		int x = cmp.getInteger(TAG_COLLECTOR_X);
		int y = cmp.getInteger(TAG_COLLECTOR_Y);
		int z = cmp.getInteger(TAG_COLLECTOR_Z);

		this.cachedCollectorCoordinates = y < 0 ? null : new BlockPos(x, y, z);
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound cmp)
	{
		    
		super.writeToPacketNBT(cmp);
		    

		cmp.setInteger(TAG_MANA, this.mana);
		cmp.setInteger(TAG_TICKS_EXISTED, this.ticksExisted);
		cmp.setInteger(TAG_PASSIVE_DECAY_TICKS, this.passiveDecayTicks);

		if (this.cachedCollectorCoordinates != null)
		{
			cmp.setInteger(TAG_COLLECTOR_X, this.cachedCollectorCoordinates.getX());
			cmp.setInteger(TAG_COLLECTOR_Y, this.cachedCollectorCoordinates.getY());
			cmp.setInteger(TAG_COLLECTOR_Z, this.cachedCollectorCoordinates.getZ());
		}
		else
		{
			int x = this.linkedCollector == null ? 0 : this.linkedCollector.getPos().getX();
			int y = this.linkedCollector == null ? -1 : this.linkedCollector.getPos().getY();
			int z = this.linkedCollector == null ? 0 : this.linkedCollector.getPos().getZ();

			cmp.setInteger(TAG_COLLECTOR_X, x);
			cmp.setInteger(TAG_COLLECTOR_Y, y);
			cmp.setInteger(TAG_COLLECTOR_Z, z);
		}
	}

	@Override
	public BlockPos getBinding()
	{
		if (this.linkedCollector == null)
			return null;
		return this.linkedCollector.getPos();
	}

	@Override
	public boolean canSelect(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side)
	{
		return true;
	}

	@Override
	public boolean bindTo(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side)
	{
		int range = 6;
		range *= range;

		double dist = pos.distanceSq(this.supertile.getPos());
		if (range >= dist)
		{
			TileEntity tile = player.world.getTileEntity(pos);
			if (tile instanceof IManaCollector)
			{
				this.linkedCollector = tile;
				return true;
			}
		}

		return false;
	}

	public boolean isValidBinding()
	{
		return this.linkedCollector != null && !this.linkedCollector.isInvalid() && this.supertile.getWorld().getTileEntity(this.linkedCollector.getPos()) == this.linkedCollector;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res)
	{
		String name = I18n.format("tile.botania:flower." + this.getUnlocalizedName() + ".name");
		int color = this.getColor();
		BotaniaAPI.internalHandler.drawComplexManaHUD(color, this.knownMana, this.getMaxMana(), name, res, BotaniaAPI.internalHandler.getBindDisplayForFlowerType(this), this.isValidBinding());
	}

	@Override
	public boolean isOvergrowthAffected()
	{
		return !this.isPassiveFlower();
	}

}
