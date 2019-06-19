/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Jan 28, 2014, 9:09:39 PM (GMT)]
 */
package vazkii.botania.common.block.subtile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.recipe.RecipePureDaisy;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.SubTileEntity;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.lexicon.LexiconData;

import java.util.Arrays;

public class SubTilePureDaisy extends SubTileEntity
{

	private static final String TAG_POSITION = "position";
	private static final String TAG_TICKS_REMAINING = "ticksRemaining";
	private static final int UPDATE_ACTIVE_EVENT = 0;
	private static final int RECIPE_COMPLETE_EVENT = 1;

	private static final BlockPos[] POSITIONS = { new BlockPos(-1, 0, -1), new BlockPos(-1, 0, 0), new BlockPos(-1, 0, 1), new BlockPos(0, 0, 1), new BlockPos(1, 0, 1), new BlockPos(1, 0, 0), new BlockPos(1, 0, -1), new BlockPos(0, 0, -1), };

	private int positionAt = 0;
	private final int[] ticksRemaining = new int[POSITIONS.length];

	// Bitfield of active positions, used clientside for particles
	private int activePositions = 0;

	public SubTilePureDaisy()
	{
		Arrays.fill(this.ticksRemaining, -1);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (this.getWorld().isRemote)
		{
			for (int i = 0; i < POSITIONS.length; i++)
			{
				if ((this.activePositions >>> i & 1) > 0)
				{
					BlockPos coords = this.getPos().add(POSITIONS[i]);
					Botania.proxy.sparkleFX(coords.getX() + Math.random(), coords.getY() + Math.random(), coords.getZ() + Math.random(), 1F, 1F, 1F, (float) Math.random(), 5);
				}
			}

			return;
		}

		this.positionAt++;
		if (this.positionAt == POSITIONS.length)
			this.positionAt = 0;

		BlockPos acoords = POSITIONS[this.positionAt];
		BlockPos coords = this.supertile.getPos().add(acoords);
		World world = this.supertile.getWorld();
		if (!world.isAirBlock(coords))
		{
			world.profiler.startSection("findRecipe");
			RecipePureDaisy recipe = this.findRecipe(coords);
			world.profiler.endSection();

			if (recipe != null)
			{
				if (this.ticksRemaining[this.positionAt] == -1)
					this.ticksRemaining[this.positionAt] = recipe.getTime();
				this.ticksRemaining[this.positionAt]--;

				if (this.ticksRemaining[this.positionAt] <= 0)
				{
					this.ticksRemaining[this.positionAt] = -1;

					if (recipe.set(world, coords, this))
					{
						world.addBlockEvent(this.getPos(), this.supertile.getBlockType(), RECIPE_COMPLETE_EVENT, this.positionAt);
						if (ConfigHandler.blockBreakParticles)
							this.supertile.getWorld().playEvent(2001, coords, Block.getStateId(recipe.getOutputState()));
					}
				}
			}
			else
				this.ticksRemaining[this.positionAt] = -1;
		}
		else
			this.ticksRemaining[this.positionAt] = -1;

		this.updateActivePositions();
	}

	private void updateActivePositions()
	{
		int newActivePositions = 0;
		for (int i = 0; i < this.ticksRemaining.length; i++)
		{
			if (this.ticksRemaining[i] > -1)
				newActivePositions |= 1 << i;
		}

		if (newActivePositions != this.activePositions)
			this.getWorld().addBlockEvent(this.getPos(), this.supertile.getBlockType(), UPDATE_ACTIVE_EVENT, newActivePositions);
	}

	private RecipePureDaisy findRecipe(BlockPos coords)
	{
		IBlockState state = this.getWorld().getBlockState(coords);

		for (RecipePureDaisy recipe : BotaniaAPI.pureDaisyRecipes)
		{
			if (recipe.matches(this.getWorld(), coords, this, state))
				return recipe;
		}

		return null;
	}

	@Override
	public boolean receiveClientEvent(int type, int param)
	{
		switch (type)
		{
			case UPDATE_ACTIVE_EVENT:
				this.activePositions = param;
				return true;
			case RECIPE_COMPLETE_EVENT:
				if (this.getWorld().isRemote)
				{
					BlockPos coords = this.getPos().add(POSITIONS[param]);
					for (int i = 0; i < 25; i++)
					{
						double x = coords.getX() + Math.random();
						double y = coords.getY() + Math.random() + 0.5;
						double z = coords.getZ() + Math.random();

						Botania.proxy.wispFX(x, y, z, 1F, 1F, 1F, (float) Math.random() / 2F);
					}
				}

				return true;
			default:
				return super.receiveClientEvent(type, param);
		}
	}

	@Override
	public RadiusDescriptor getRadius()
	{
		return new RadiusDescriptor.Square(this.toBlockPos(), 1);
	}

	@Override
	public void readFromPacketNBT(NBTTagCompound cmp)
	{
		    
		super.readFromPacketNBT(cmp);
		    

		this.positionAt = cmp.getInteger(TAG_POSITION);

		if (this.supertile.getWorld() != null && !this.supertile.getWorld().isRemote)
			for (int i = 0; i < this.ticksRemaining.length; i++)
			{
				this.ticksRemaining[i] = cmp.getInteger(TAG_TICKS_REMAINING + i);
			}
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound cmp)
	{
		    
		super.writeToPacketNBT(cmp);
		    

		cmp.setInteger(TAG_POSITION, this.positionAt);
		for (int i = 0; i < this.ticksRemaining.length; i++)
		{
			cmp.setInteger(TAG_TICKS_REMAINING + i, this.ticksRemaining[i]);
		}
	}

	@Override
	public LexiconEntry getEntry()
	{
		return LexiconData.pureDaisy;
	}
}
