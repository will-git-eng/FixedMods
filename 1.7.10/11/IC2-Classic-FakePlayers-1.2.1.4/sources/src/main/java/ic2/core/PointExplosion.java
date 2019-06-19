package ic2.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerWorld;
import ru.will.git.ic2.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class PointExplosion
{
	private Random ExplosionRNG = new Random();
	private World worldObj;
	public int explosionX;
	public int explosionY;
	public int explosionZ;
	public Entity exploder;
	public float explosionSize;
	public float explosionDropRate;
	public float explosionDamage;
	public Set<ChunkPosition> destroyedBlockPositions = new HashSet();
	private Explosion fakeExplosion;
    
    

	public PointExplosion(World world, Entity entity, int x, int y, int z, float power, float drop, float entitydamage)
	{
		this.worldObj = world;
		this.exploder = entity;
		this.explosionSize = power;
		this.explosionDropRate = drop;
		this.explosionDamage = entitydamage;
		this.explosionX = x;
		this.explosionY = y;
		this.explosionZ = z;
		if (this.explosionX < 0)
			--this.explosionX;

		if (this.explosionZ < 0)
			--this.explosionZ;

		this.fakeExplosion = new Explosion(world, entity, x, y, z, power);
    
		this.fake = new FakePlayerContainerWorld(ModUtils.profile, world);
		if (entity instanceof EntityPlayer)
    
	}

	public void doExplosionA(int lowX, int lowY, int lowZ, int highX, int highY, int highZ)
	{
		for (int x = this.explosionX - lowX; x <= this.explosionX + highX; ++x)
			for (int y = this.explosionY - lowY; y <= this.explosionY + highY; ++y)
				for (int z = this.explosionZ - lowZ; z <= this.explosionZ + highZ; ++z)
				{
					Block id = this.worldObj.getBlock(x, y, z);
					float resis = 0.0F;
					if (id != null)
						resis = id.getExplosionResistance(this.exploder, this.worldObj, x, y, z, this.explosionX, this.explosionY, this.explosionZ);

					if (this.explosionSize >= resis / 10.0F)
						this.destroyedBlockPositions.add(new ChunkPosition(x, y, z));
				}

		this.explosionSize *= 2.0F;
		int k = MathHelper.floor_double(this.explosionX - this.explosionSize - 1.0D);
		int i1 = MathHelper.floor_double(this.explosionX + this.explosionSize + 1.0D);
		int k2 = MathHelper.floor_double(this.explosionY - this.explosionSize - 1.0D);
		int l1 = MathHelper.floor_double(this.explosionY + this.explosionSize + 1.0D);
		int i2 = MathHelper.floor_double(this.explosionZ - this.explosionSize - 1.0D);
		int j2 = MathHelper.floor_double(this.explosionZ + this.explosionSize + 1.0D);
		List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this.exploder, AxisAlignedBB.getBoundingBox(k, k2, i2, i1, l1, j2));
		Vec3 vec3d = Vec3.createVectorHelper(this.explosionX, this.explosionY, this.explosionZ);

		for (int k3 = 0; k3 < list.size(); ++k3)
		{
			Entity entity = list.get(k3);
			double d4 = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / this.explosionSize;
			if (d4 <= 1.0D)
    
				if (this.fake.cantDamage(entity))
    

				double d2 = entity.posX - this.explosionX;
				double d3 = entity.posY - this.explosionY;
				d4 = entity.posZ - this.explosionZ;
				double d5 = MathHelper.sqrt_double(d2 * d2 + d3 * d3 + d4 * d4);
				d2 = d2 / d5;
				d3 = d3 / d5;
				d4 = d4 / d5;
				double d6 = this.worldObj.getBlockDensity(vec3d, entity.boundingBox);
				double d7 = (1.0D - d4) * d6;
				entity.attackEntityFrom(this.damagesource, (int) (((d7 * d7 + d7) / 2.0D * 8.0D * this.explosionSize + 1.0D) * this.explosionDamage));
				entity.motionX += d2 * d7;
				entity.motionY += d3 * d7;
				entity.motionZ += d4 * d7;
			}
		}

	}

	public void doExplosionB(boolean flag)
	{
		this.worldObj.playSoundEffect(this.explosionX, this.explosionY, this.explosionZ, "random.explode", 4.0F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
		ArrayList<ChunkPosition> arraylist = new ArrayList();
		arraylist.addAll(this.destroyedBlockPositions);

		for (int i = arraylist.size() - 1; i >= 0; --i)
		{
			ChunkPosition chunkposition = arraylist.get(i);
			int j = chunkposition.chunkPosX;
			int k = chunkposition.chunkPosY;
    
			if (this.fake.cantBreak(j, k, l))
    

			Block i2 = this.worldObj.getBlock(j, k, l);
			if (flag)
			{
				double d = j + this.worldObj.rand.nextFloat();
				double d2 = k + this.worldObj.rand.nextFloat();
				double d3 = l + this.worldObj.rand.nextFloat();
				double d4 = d - this.explosionX;
				double d5 = d2 - this.explosionY;
				double d6 = d3 - this.explosionZ;
				double d7 = MathHelper.sqrt_double(d4 * d4 + d5 * d5 + d6 * d6);
				d4 = d4 / d7;
				d5 = d5 / d7;
				d6 = d6 / d7;
				double d8 = 0.5D / (d7 / this.explosionSize + 0.1D);
				d8 = d8 * (this.worldObj.rand.nextFloat() * this.worldObj.rand.nextFloat() + 0.3F);
				d4 = d4 * d8;
				d5 = d5 * d8;
				d6 = d6 * d8;
				this.worldObj.spawnParticle("explode", (d + this.explosionX * 1.0D) / 2.0D, (d2 + this.explosionY * 1.0D) / 2.0D, (d3 + this.explosionZ * 1.0D) / 2.0D, d4, d5, d6);
				this.worldObj.spawnParticle("smoke", d, d2, d3, d4, d5, d6);
			}

			if (i2 != null)
			{
				i2.dropBlockAsItemWithChance(this.worldObj, j, k, l, this.worldObj.getBlockMetadata(j, k, l), this.explosionDropRate, 0);
				this.worldObj.setBlockToAir(j, k, l);
				i2.onBlockDestroyedByExplosion(this.worldObj, j, k, l, this.fakeExplosion);
			}
		}

	}
}
