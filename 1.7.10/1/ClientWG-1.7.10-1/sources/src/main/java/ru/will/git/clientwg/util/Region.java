package ru.will.git.clientwg.util;

import com.google.common.base.Objects;
import net.minecraft.entity.Entity;

public final class Region
{
	public final Vec3i min;
	public final Vec3i max;

	public Region(Vec3i min, Vec3i max)
	{
		this.min = min;
		this.max = max;
	}

	public boolean isInRegion(Vec3i vec)
	{
		return this.min.x <= vec.x && vec.x <= this.max.x && this.min.z <= vec.z && vec.z <= this.max.z && this.min.y <= vec.y && vec.y <= this.max.y;
	}

	public boolean isInRegion(int x, int y, int z)
	{
		return this.isInRegion(new Vec3i(x, y, z));
	}

	public boolean isInRegion(Entity entity)
	{
		return this.isInRegion(new Vec3i(entity.posX, entity.posY, entity.posZ));
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		Region region = (Region) o;
		return Objects.equal(this.min, region.min) && Objects.equal(this.max, region.max);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.min, this.max);
	}
}
