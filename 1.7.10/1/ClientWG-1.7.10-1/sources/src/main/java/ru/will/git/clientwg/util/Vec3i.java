package ru.will.git.clientwg.util;

import net.minecraft.util.MathHelper;

public final class Vec3i
{
	public final int x;
	public final int y;
	public final int z;

	public Vec3i(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3i(double x, double y, double z)
	{
		this.x = MathHelper.floor_double(x);
		this.y = MathHelper.floor_double(y);
		this.z = MathHelper.floor_double(z);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;

		Vec3i vec3i = (Vec3i) o;
		return this.x == vec3i.x && this.y == vec3i.y && this.z == vec3i.z;
	}

	@Override
	public int hashCode()
	{
		int result = this.x;
		result = 31 * result + this.y;
		result = 31 * result + this.z;
		return result;
	}
}
