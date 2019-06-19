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

import ru.will.git.eventhelper.util.EventUtils;
import ru.will.git.forestry.EventConfig;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class AlleleEffectExploration extends AlleleEffectThrottled
{

	public AlleleEffectExploration()
	{
		super("exploration", false, 80, true, false);
	}

	@Override
	public IEffectData doEffectThrottled(IBeeGenome genome, IEffectData storedData, IBeeHousing housing)
	{
		
		if (!EventConfig.enableExploration)
			return storedData;
		

		List<EntityPlayer> players = getEntitiesInRange(genome, housing, EntityPlayer.class);

		
		if (players.isEmpty())
			return storedData;
		BlockPos pos = housing.getCoordinates();
		

		for (EntityPlayer player : players)
		{
			
			if (EventConfig.eventExploration && !BlockPos.ORIGIN.equals(pos) && EventUtils.cantInteract(player, EnumHand.MAIN_HAND, pos, EnumFacing.UP))
				continue;
			

			player.addExperience(2);
		}

		return storedData;
	}

}
