package ru.will.git.thaumcraft.tile;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import ru.will.git.thaumcraft.ModUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public final class OwnerTileEntity extends TileEntity
{
	public final FakePlayerContainer fake = new FakePlayerContainerTileEntity(ModUtils.profile, this);

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		this.fake.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.fake.readFromNBT(nbt);
	}
}
