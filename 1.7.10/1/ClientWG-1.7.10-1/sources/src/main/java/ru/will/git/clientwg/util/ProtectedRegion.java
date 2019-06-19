package ru.will.git.clientwg.util;

import com.google.common.base.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public final class ProtectedRegion
{
	public final Region region;
	public final boolean hasAccess;
	public final boolean pvp;
	public final boolean animalsDamage;

	public ProtectedRegion(Region region, boolean hasAccess, boolean pvp, boolean animalsDamage)
	{
		this.region = region;
		this.hasAccess = hasAccess;
		this.pvp = pvp;
		this.animalsDamage = animalsDamage;
	}

	public boolean hasAccess(int x, int y, int z)
	{
		return this.hasAccess || !this.region.isInRegion(x, y, z);
	}

	public boolean hasAccess(Entity entity)
	{
		return this.hasAccess || this.hasForceAccess(entity) || !this.region.isInRegion(entity);
	}

	public boolean hasForceAccess(Entity entity)
	{
		return this.pvp && entity instanceof EntityPlayer || Utils.isHostile(entity) || this.animalsDamage && Utils.isNonHostile(entity);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		ProtectedRegion that = (ProtectedRegion) o;
		return this.hasAccess == that.hasAccess && Objects.equal(this.region, that.region);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.region, this.hasAccess);
	}
}
