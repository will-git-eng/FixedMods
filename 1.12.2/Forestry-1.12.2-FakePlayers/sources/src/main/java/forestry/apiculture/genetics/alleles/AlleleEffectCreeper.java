/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture.genetics.alleles;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import ru.will.git.forestry.EventConfig;
import ru.will.git.forestry.ModUtils;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.core.genetics.EffectData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class AlleleEffectCreeper extends AlleleEffectThrottled
{

	private static final int explosionChance = 50;
	private static final byte defaultForce = 12;
	private static final byte indexExplosionTimer = 1;
	private static final byte indexExplosionForce = 2;

	public AlleleEffectCreeper()
	{
		super("creeper", true, 20, false, true);
	}

	@Override
	public IEffectData validateStorage(IEffectData storedData)
	{
		if (!(storedData instanceof EffectData))
			return new EffectData(3, 0);

		if (((EffectData) storedData).getIntSize() < 3)
			return new EffectData(3, 0);

		return storedData;
	}

	@Override
	public IEffectData doEffectThrottled(IBeeGenome genome, IEffectData storedData, IBeeHousing housing)
	{
		
		if (!EventConfig.enableCreeper)
			return storedData;
		

		World world = housing.getWorldObj();
		BlockPos housingCoords = housing.getCoordinates();

		if (storedData.getInteger(indexExplosionTimer) > 0)
		{

			progressExplosion(housing, storedData, world, housingCoords);

			return storedData;
		}

		List<EntityPlayer> players = getEntitiesInRange(genome, housing, EntityPlayer.class);
		for (EntityPlayer player : players)
		{
			int chance = explosionChance;
			storedData.setInteger(indexExplosionForce, defaultForce);


			int count = BeeManager.armorApiaristHelper.wearsItems(player, this.getUID(), true);
			if (count > 3)
				continue; 
			else if (count > 2)
			{
				chance = 5;
				storedData.setInteger(indexExplosionForce, 6);
			}
			else if (count > 1)
			{
				chance = 20;
				storedData.setInteger(indexExplosionForce, 8);
			}
			else if (count > 0)
			{
				chance = 35;
				storedData.setInteger(indexExplosionForce, 10);
			}

			if (world.rand.nextInt(1000) >= chance)
				continue;

			float pitch = (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F;
			world.playSound(null, housingCoords.getX(), housingCoords.getY(), housingCoords.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, pitch);
			storedData.setInteger(indexExplosionTimer, 2); 
		}

		return storedData;
	}

	
	private static void progressExplosion(IEffectData storedData, World world, BlockPos pos)
	{
		progressExplosion(null, storedData, world, pos);
	}
	


	private static void progressExplosion(
			@Nullable IBeeHousing housing, IEffectData storedData, World world, BlockPos pos)
	{
		int explosionTimer = storedData.getInteger(indexExplosionTimer);
		explosionTimer--;
		storedData.setInteger(indexExplosionTimer, explosionTimer);

		if (explosionTimer > 0)
			return;

		
		if (EventConfig.eventCreeper)
		{
			FakePlayerContainer fake = housing == null ? ModUtils.NEXUS_FACTORY.wrapFake(world) : housing.getFakePlayerContainer();
			fake.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), storedData.getInteger(indexExplosionForce), false);
			return;
		}
		

		world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), storedData.getInteger(indexExplosionForce), false);
	}
}
