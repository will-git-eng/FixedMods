package com.brandon3055.draconicevolution.common.tileentities;

import com.brandon3055.draconicevolution.common.handler.ConfigHandler;
import ru.will.git.draconicevolution.EventConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Arrays;



public abstract class CustomSpawnerBaseLogic
{

	public int spawnDelay = 20;

	public String entityName = "";

	public double renderRotation0;
	public double renderRotation1;
	private int minSpawnDelay = 400;
	private int maxSpawnDelay = 600;

	private int spawnCount = 6;
	private Entity renderedEntity;
	private int maxNearbyEntities = 20;

	public boolean powered = false;
	public boolean ltPowered = false;

	public boolean requiresPlayer = true;

	public boolean ignoreSpawnRequirements = false;

	public int spawnSpeed = 1;

	private int activatingRangeFromPlayer = 24;

	private int spawnRange = 4;
	public int skeletonType = 0;


	public String getEntityNameToSpawn()
	{
		return this.entityName;
	}

	public void setEntityName(String name)
	{
		this.entityName = name;
	}


	public boolean isActivated()
	{
		if (!this.requiresPlayer)
			return true;
		else
			return this.getSpawnerWorld().getClosestPlayer(this.getSpawnerX() + 0.5D, this.getSpawnerY() + 0.5D, this.getSpawnerZ() + 0.5D, this.activatingRangeFromPlayer) != null;
	}

	public void updateSpawner()

		if (!EventConfig.enableSpawner)
		{
			int x = this.getSpawnerX();
			int y = this.getSpawnerY();
			int z = this.getSpawnerZ();
			this.getSpawnerWorld().setBlockToAir(x, y, z);
			return;
		}
		if (!EventConfig.enableSpawnerUpgrades)
		{
			this.requiresPlayer = true;
			this.ignoreSpawnRequirements = false;
		}
		if (!EventConfig.enableSpawnerWitcherSkeleton)


		if (!this.powered && this.isActivated())
		{
			double d2;

			if (this.getSpawnerWorld().isRemote)
			{
				double d0 = this.getSpawnerX() + this.getSpawnerWorld().rand.nextFloat();
				double d1 = this.getSpawnerY() + this.getSpawnerWorld().rand.nextFloat();
				d2 = this.getSpawnerZ() + this.getSpawnerWorld().rand.nextFloat();
				this.getSpawnerWorld().spawnParticle("smoke", d0, d1, d2, 0.0D, 0.0D, 0.0D);
				this.getSpawnerWorld().spawnParticle("flame", d0, d1, d2, 0.0D, 0.0D, 0.0D);

				if (this.spawnDelay > 0)
					--this.spawnDelay;

				this.renderRotation1 = this.renderRotation0;
				this.renderRotation0 = (this.renderRotation0 + 1000.0F / (this.spawnDelay + 200.0F)) % 360.0D;
			}
			else
			{
				if (this.spawnDelay == -1)
					this.resetTimer();

				if (this.spawnDelay > 0)
				{
					--this.spawnDelay;
					return;
				}

				boolean flag = false;

				for (int i = 0; i < this.spawnCount; ++i)
				{
					Entity entity = EntityList.createEntityByName(this.getEntityNameToSpawn(), this.getSpawnerWorld());

					if (entity == null)

					if (!EventConfig.enableSpawnerWitcherSkeleton && entity instanceof EntitySkeleton)
					{
						EntitySkeleton skeleton = (EntitySkeleton) entity;
						if (skeleton.getSkeletonType() != 0)
							return;


					int j = this.getSpawnerWorld().getEntitiesWithinAABB(entity.getClass(), AxisAlignedBB.getBoundingBox(this.getSpawnerX(), this.getSpawnerY(), this.getSpawnerZ(), this.getSpawnerX() + 1, this.getSpawnerY() + 1, this.getSpawnerZ() + 1).expand(this.spawnRange * 2, 4.0D, this.spawnRange * 2)).size();

					if (j >= this.maxNearbyEntities)
					{
						this.resetTimer();
						return;
					}

					int x = this.getSpawnerX() + (int) ((this.getSpawnerWorld().rand.nextDouble() - this.getSpawnerWorld().rand.nextDouble()) * this.spawnRange);
					int y = this.getSpawnerY() + this.getSpawnerWorld().rand.nextInt(3) - 1;
					int z = this.getSpawnerZ() + (int) ((this.getSpawnerWorld().rand.nextDouble() - this.getSpawnerWorld().rand.nextDouble()) * this.spawnRange);
					EntityLiving entityliving = entity instanceof EntityLiving ? (EntityLiving) entity : null;
					entity.setLocationAndAngles(x + 0.5, y + 0.5, z + 0.5, this.getSpawnerWorld().rand.nextFloat() * 360.0F, 0.0F);

					if (entityliving == null || entityliving.getCanSpawnHere() || this.ignoreSpawnRequirements && this.getSpawnerWorld().getBlock(x, y, z) == Blocks.air)
					{
						this.spawnEntity(entity);
						this.getSpawnerWorld().playAuxSFX(2004, this.getSpawnerX(), this.getSpawnerY(), this.getSpawnerZ(), 0);

						if (entityliving != null)
							entityliving.spawnExplosionParticle();

						flag = true;

					}
				}

				if (flag)
					this.resetTimer();
			}
		}
	}

	public Entity spawnEntity(Entity entity)
	{
		if (entity instanceof EntityLivingBase && entity.worldObj != null)
		{
			if (entity instanceof EntitySkeleton)
			{
				if (this.skeletonType == 1)
				{
					entity.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
					((EntitySkeleton) entity).setEquipmentDropChance(0, 0f);
				}
				else
					entity.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
			}
			else
				((EntityLiving) entity).onSpawnWithEgg(null);

			if (!this.requiresPlayer)
			{
				((EntityLiving) entity).func_110163_bv();
				entity.getEntityData().setLong("SpawnedByDESpawner", this.getSpawnerWorld().getTotalWorldTime());
			}

			if (!this.getSpawnerWorld().isRemote)
				this.getSpawnerWorld().spawnEntityInWorld(entity);
		}

		return entity;
	}

	private void resetTimer()
	{
		if (this.maxSpawnDelay <= this.minSpawnDelay)
			this.spawnDelay = this.minSpawnDelay;
		else
		{
			int i = this.maxSpawnDelay - this.minSpawnDelay;
			this.spawnDelay = this.minSpawnDelay + this.getSpawnerWorld().rand.nextInt(i);
		}

		this.blockEvent(1);
	}

	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		this.entityName = par1NBTTagCompound.getString("EntityId");
		this.spawnDelay = par1NBTTagCompound.getShort("Delay");
		if (ConfigHandler.spawnerListType != Arrays.asList(ConfigHandler.spawnerList).contains(this.entityName))
		{
			this.entityName = "Pig";
			par1NBTTagCompound.setBoolean("Running", false);
		}

		this.powered = par1NBTTagCompound.getBoolean("Powered");
		this.spawnSpeed = par1NBTTagCompound.getShort("Speed");
		this.requiresPlayer = par1NBTTagCompound.getBoolean("RequiresPlayer");
		this.ignoreSpawnRequirements = par1NBTTagCompound.getBoolean("IgnoreSpawnRequirements");
		this.skeletonType = par1NBTTagCompound.getInteger("SkeletonType");

		this.minSpawnDelay = par1NBTTagCompound.getShort("MinSpawnDelay");
		this.maxSpawnDelay = par1NBTTagCompound.getShort("MaxSpawnDelay");
		this.spawnCount = par1NBTTagCompound.getShort("SpawnCount");

		if (par1NBTTagCompound.hasKey("MaxNearbyEntities", 99))
		{
			this.maxNearbyEntities = par1NBTTagCompound.getShort("MaxNearbyEntities");
			this.activatingRangeFromPlayer = par1NBTTagCompound.getShort("RequiredPlayerRange");
		}

		if (par1NBTTagCompound.hasKey("SpawnRange", 99))
			this.spawnRange = par1NBTTagCompound.getShort("SpawnRange");

		if (this.getSpawnerWorld() != null && this.getSpawnerWorld().isRemote)

		if (!EventConfig.enableSpawnerUpgrades)
		{
			int spawnRate = 1;
			this.spawnSpeed = spawnRate;
			this.minSpawnDelay = 400 - spawnRate * 150;
			this.maxSpawnDelay = 600 - spawnRate * 200;
		}
		if (!EventConfig.enableSpawnerWitcherSkeleton)

	}

	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		par1NBTTagCompound.setString("EntityId", this.getEntityNameToSpawn());
		par1NBTTagCompound.setShort("Delay", (short) this.spawnDelay);
		par1NBTTagCompound.setShort("MinSpawnDelay", (short) this.minSpawnDelay);
		par1NBTTagCompound.setShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
		par1NBTTagCompound.setShort("SpawnCount", (short) this.spawnCount);
		par1NBTTagCompound.setShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
		par1NBTTagCompound.setShort("RequiredPlayerRange", (short) this.activatingRangeFromPlayer);
		par1NBTTagCompound.setShort("SpawnRange", (short) this.spawnRange);
		par1NBTTagCompound.setBoolean("Powered", this.powered);
		par1NBTTagCompound.setShort("Speed", (short) this.spawnSpeed);
		par1NBTTagCompound.setBoolean("RequiresPlayer", this.requiresPlayer);
		par1NBTTagCompound.setBoolean("IgnoreSpawnRequirements", this.ignoreSpawnRequirements);
		par1NBTTagCompound.setInteger("SkeletonType", this.skeletonType);
	}


	public boolean setDelayToMin(int par1)
	{
		if (par1 == 1 && this.getSpawnerWorld().isRemote)
		{
			this.spawnDelay = this.minSpawnDelay;
			return true;
		}
		else
			return false;
	}

	@SideOnly(Side.CLIENT)
	public Entity getEntityForRenderer()
	{
		if (this.renderedEntity == null)
		{
			Entity entity = EntityList.createEntityByName(this.getEntityNameToSpawn(), this.getSpawnerWorld());
			entity = this.spawnEntity(entity);
			if (entity instanceof EntitySkeleton)
				((EntitySkeleton) entity).setSkeletonType(this.skeletonType);
			this.renderedEntity = entity;
		}

		return this.renderedEntity;
	}

	public abstract void blockEvent(int var1);

	public abstract World getSpawnerWorld();

	public abstract int getSpawnerX();

	public abstract int getSpawnerY();

	public abstract int getSpawnerZ();

	public void setSpawnRate(int i)

		if (!EventConfig.enableSpawnerUpgrades)


		this.spawnSpeed = i;
		this.minSpawnDelay = 400 - i * 150;
		this.maxSpawnDelay = 600 - i * 200;
		this.spawnCount = 4 + i * 2;
		if (i == 3)
		{
			this.minSpawnDelay = 40;
			this.maxSpawnDelay = 40;
			this.spawnCount = 12;
		}
		if (this.minSpawnDelay < 0)
			this.minSpawnDelay = 0;
		if (this.maxSpawnDelay < 1)
			this.maxSpawnDelay = 1;
		this.resetTimer();
	}
}
