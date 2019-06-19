/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Jan 24, 2014, 8:03:44 PM (GMT)]
 */
package vazkii.botania.api.subtile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.IManaNetwork;
import vazkii.botania.api.mana.IManaPool;

import java.awt.*;

/**
 * The basic class for a Functional Flower.
 */
public class SubTileFunctional extends SubTileEntity
{

	public static final int LINK_RANGE = 10;

	private static final String TAG_MANA = "mana";

	private static final String TAG_POOL_X = "poolX";
	private static final String TAG_POOL_Y = "poolY";
	private static final String TAG_POOL_Z = "poolZ";

	public int mana;

	public int redstoneSignal = 0;

	int sizeLastCheck = -1;
	TileEntity linkedPool = null;
	public int knownMana = -1;

	BlockPos cachedPoolCoordinates = null;

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

		this.linkPool();

		if (this.linkedPool != null && this.isValidBinding())
		{
			IManaPool pool = (IManaPool) this.linkedPool;
			int manaInPool = pool.getCurrentMana();
			int manaMissing = this.getMaxMana() - this.mana;
			int manaToRemove = Math.min(manaMissing, manaInPool);
			pool.recieveMana(-manaToRemove);
			this.addMana(manaToRemove);
		}

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
				BotaniaAPI.internalHandler.sparkleFX(this.supertile.getWorld(), this.supertile.getPos().getX() + 0.3 + Math.random() * 0.5, this.supertile.getPos().getY() + 0.5 + Math.random() * 0.5, this.supertile.getPos().getZ() + 0.3 + Math.random() * 0.5, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, (float) Math.random(), 5);
		}
	}

	public void linkPool()
	{
		boolean needsNew = false;
		if (this.linkedPool == null)
		{
			needsNew = true;

			if (this.cachedPoolCoordinates != null)
			{
				needsNew = false;
				if (this.supertile.getWorld().isBlockLoaded(this.cachedPoolCoordinates))
				{
					needsNew = true;
					TileEntity tileAt = this.supertile.getWorld().getTileEntity(this.cachedPoolCoordinates);
					if (tileAt != null && tileAt instanceof IManaPool && !tileAt.isInvalid())
					{
						this.linkedPool = tileAt;
						needsNew = false;
					}
					this.cachedPoolCoordinates = null;
				}
			}
		}
		else
		{
			TileEntity tileAt = this.supertile.getWorld().getTileEntity(this.linkedPool.getPos());
			if (tileAt != null && tileAt instanceof IManaPool)
				this.linkedPool = tileAt;
		}

		if (needsNew && this.ticksExisted == 1)
		{ // Only for new flowers
			IManaNetwork network = BotaniaAPI.internalHandler.getManaNetworkInstance();
			int size = network.getAllPoolsInWorld(this.supertile.getWorld()).size();
			if (BotaniaAPI.internalHandler.shouldForceCheck() || size != this.sizeLastCheck)
			{
				this.linkedPool = network.getClosestPool(this.supertile.getPos(), this.supertile.getWorld(), LINK_RANGE);
				this.sizeLastCheck = size;
			}
		}
	}

	public void linkToForcefully(TileEntity pool)
	{
		this.linkedPool = pool;
	}

	public void addMana(int mana)
	{
		this.mana = Math.min(this.getMaxMana(), this.mana + mana);
	}

	@Override
	public boolean onWanded(EntityPlayer player, ItemStack wand)
	{
		if (player == null)
			return false;

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

		int x = cmp.getInteger(TAG_POOL_X);
		int y = cmp.getInteger(TAG_POOL_Y);
		int z = cmp.getInteger(TAG_POOL_Z);

		this.cachedPoolCoordinates = y < 0 ? null : new BlockPos(x, y, z);
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound cmp)
	{
		    
		super.writeToPacketNBT(cmp);
		    

		cmp.setInteger(TAG_MANA, this.mana);

		if (this.cachedPoolCoordinates != null)
		{
			cmp.setInteger(TAG_POOL_X, this.cachedPoolCoordinates.getX());
			cmp.setInteger(TAG_POOL_Y, this.cachedPoolCoordinates.getY());
			cmp.setInteger(TAG_POOL_Z, this.cachedPoolCoordinates.getZ());
		}
		else
		{
			int x = this.linkedPool == null ? 0 : this.linkedPool.getPos().getX();
			int y = this.linkedPool == null ? -1 : this.linkedPool.getPos().getY();
			int z = this.linkedPool == null ? 0 : this.linkedPool.getPos().getZ();

			cmp.setInteger(TAG_POOL_X, x);
			cmp.setInteger(TAG_POOL_Y, y);
			cmp.setInteger(TAG_POOL_Z, z);
		}
	}

	@Override
	public BlockPos getBinding()
	{
		if (this.linkedPool == null)
			return null;
		return this.linkedPool.getPos();
	}

	@Override
	public boolean canSelect(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side)
	{
		return true;
	}

	@Override
	public boolean bindTo(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side)
	{
		int range = 10;
		range *= range;

		double dist = pos.distanceSq(this.supertile.getPos());
		if (range >= dist)
		{
			TileEntity tile = player.world.getTileEntity(pos);
			if (tile instanceof IManaPool)
			{
				this.linkedPool = tile;
				return true;
			}
		}

		return false;
	}

	public boolean isValidBinding()
	{
		return this.linkedPool != null && this.linkedPool.hasWorld() && !this.linkedPool.isInvalid() && this.supertile.getWorld().isBlockLoaded(this.linkedPool.getPos(), false) && this.supertile.getWorld().getTileEntity(this.linkedPool.getPos()) == this.linkedPool;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res)
	{
		String name = I18n.format("tile.botania:flower." + this.getUnlocalizedName() + ".name");
		int color = this.getColor();
		BotaniaAPI.internalHandler.drawComplexManaHUD(color, this.knownMana, this.getMaxMana(), name, res, BotaniaAPI.internalHandler.getBindDisplayForFlowerType(this), this.isValidBinding());
	}

}
