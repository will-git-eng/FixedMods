package ru.will.git.ic2;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public final class FakePlayerContainerEntity extends FakePlayerContainer
{
	private final Entity entity;

	public FakePlayerContainerEntity(GameProfile modFakeProfile, Entity entity)
	{
		super(modFakeProfile);
		this.entity = entity;
	}

	@Override
	public final World getWorld()
	{
		return this.entity.worldObj;
	}
}