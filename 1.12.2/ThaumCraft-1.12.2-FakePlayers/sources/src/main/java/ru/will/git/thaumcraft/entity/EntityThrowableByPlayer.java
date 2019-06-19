package ru.will.git.thaumcraft.entity;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import ru.will.git.thaumcraft.ModUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class EntityThrowableByPlayer extends EntityThrowable
{
	public final FakePlayerContainer fake = ModUtils.NEXUS_FACTORY.wrapFake(this);

	public EntityThrowableByPlayer(World world)
	{
		super(world);
	}

	public EntityThrowableByPlayer(World world, double x, double y, double z)
	{
		super(world, x, y, z);
	}

	public EntityThrowableByPlayer(World world, EntityLivingBase thrower)
	{
		super(world, thrower);
		this.fake.setRealPlayer(thrower);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		super.writeEntityToNBT(compound);
		this.fake.writeToNBT(compound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		super.readEntityFromNBT(compound);
		this.fake.readFromNBT(compound);
	}
}
