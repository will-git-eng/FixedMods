package com.brandon3055.draconicevolution.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;


public class ExtendedPlayer implements IExtendedEntityProperties
{

	public static final String EXT_PROP_NAME = "DEPlayerProperties";

	private final EntityPlayer player;
	private int spawnCount;

	public ExtendedPlayer(EntityPlayer player)
	{
		this.player = player;
		this.spawnCount = 0;
	}


	public static void register(EntityPlayer player)
	{
		player.registerExtendedProperties(ExtendedPlayer.EXT_PROP_NAME, new ExtendedPlayer(player));
	}


	public static ExtendedPlayer get(EntityPlayer player)
	{
		return (ExtendedPlayer) player.getExtendedProperties(EXT_PROP_NAME);
	}

	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		NBTTagCompound properties = new NBTTagCompound();
		properties.setInteger("SpawnCount", this.spawnCount);
		compound.setTag(EXT_PROP_NAME, properties);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{

		if (properties == null)


		this.spawnCount = properties.getInteger("SpawnCount");
	}

	@Override
	public void init(Entity entity, World world)
	{
	}

	public int getSpawnCount()
	{
		return this.spawnCount;
	}

	public void setSpawnCount(int count)
	{
		this.spawnCount = count;
	}
}
