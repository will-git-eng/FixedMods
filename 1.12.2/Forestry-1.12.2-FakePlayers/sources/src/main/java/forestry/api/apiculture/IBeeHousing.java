/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.apiculture;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import ru.will.git.forestry.ModUtils;
import com.mojang.authlib.GameProfile;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.IErrorLogicSource;
import forestry.api.core.ILocatable;
import forestry.api.genetics.IHousing;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IBeeHousing extends IHousing, IErrorLogicSource, IClimateProvider, ILocatable
{

	/**
	 * Used by {@link IBeeRoot#createBeeHousingModifier(IBeeHousing)}
	 * to combine bee modifiers from several sources that can change over time.
	 *
	 * @return IBeeModifiers from the housing, frames, etc.
	 */
	Iterable<IBeeModifier> getBeeModifiers();

	/**
	 * Used by {@link IBeeRoot#createBeeHousingListener(IBeeHousing)}
	 * to combine bee listeners from several sources that can change over time.
	 *
	 * @return IBeeListeners from the housing, multiblock parts, etc.
	 */
	Iterable<IBeeListener> getBeeListeners();

	IBeeHousingInventory getBeeInventory();

	IBeekeepingLogic getBeekeepingLogic();

	int getBlockLightValue();

	boolean canBlockSeeTheSky();

	boolean isRaining();

	@Nullable
	GameProfile getOwner();

	/**
	 * @return exact coordinates where bee particle FX should spawn from
	 * @since Forestry 4.2
	 */
	Vec3d getBeeFXCoordinates();

	
	@Nonnull
	default FakePlayerContainer getFakePlayerContainer()
	{
		FakePlayerContainer fake;
		if (this instanceof TileEntity)
			fake = ModUtils.NEXUS_FACTORY.wrapFake((TileEntity) this);
		else if (this instanceof Entity)
			fake = ModUtils.NEXUS_FACTORY.wrapFake((Entity) this);
		else
			fake = ModUtils.NEXUS_FACTORY.wrapFake(this.getWorldObj());
		GameProfile ownerProfile = this.getOwner();
		if (ownerProfile != null && ownerProfile.isComplete())
			fake.setProfile(ownerProfile);
		return fake;
	}
	
}
