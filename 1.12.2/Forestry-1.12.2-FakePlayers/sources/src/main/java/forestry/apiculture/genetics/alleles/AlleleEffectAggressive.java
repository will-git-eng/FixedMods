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
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.core.utils.DamageSourceForestry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

import java.util.List;

public class AlleleEffectAggressive extends AlleleEffectThrottled
{
	private static final DamageSource damageSourceBeeAggressive = new DamageSourceForestry("bee.aggressive");

	public AlleleEffectAggressive()
	{
		super("aggressive", true, 40, false, false);
	}

	@Override
	public IEffectData doEffectThrottled(IBeeGenome genome, IEffectData storedData, IBeeHousing housing)
	{
		
		if (!EventConfig.enableAggressive)
			return storedData;
		

		List<EntityLivingBase> entities = getEntitiesInRange(genome, housing, EntityLivingBase.class);

		
		if (entities.isEmpty())
			return storedData;
		FakePlayerContainer fake = EventConfig.eventAggressive ? housing.getFakePlayerContainer() : null;
		

		for (EntityLivingBase entity : entities)
		{
			int damage = 4;

			// Entities are not attacked if they wear a full set of apiarist's armor.
			int count = BeeManager.armorApiaristHelper.wearsItems(entity, this.getUID(), true);
			damage -= count;
			if (damage <= 0)
				continue;

			
			if (fake != null && fake.cantAttack(entity))
				continue;
			

			entity.attackEntityFrom(damageSourceBeeAggressive, damage);
		}

		return storedData;
	}

}
