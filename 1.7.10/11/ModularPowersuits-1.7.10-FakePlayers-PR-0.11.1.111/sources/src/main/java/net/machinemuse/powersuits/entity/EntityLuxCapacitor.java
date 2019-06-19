package net.machinemuse.powersuits.entity;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.machinemuse.ModUtils;

import net.machinemuse.powersuits.block.TileEntityLuxCapacitor;
import net.machinemuse.powersuits.common.MPSItems;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EntityLuxCapacitor extends EntityThrowable
{
	public double red;
	public double green;
    
	public final FakePlayerContainer fake = new FakePlayerContainerEntity(ModUtils.profile, this);

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		this.fake.writeToNBT(nbt);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.fake.readFromNBT(nbt);
    

	public EntityLuxCapacitor(World par1World)
	{
		super(par1World);
	}

	public EntityLuxCapacitor(World world, EntityLivingBase shootingEntity, double r, double g, double b)
	{
		this(world, shootingEntity);
		this.red = r;
		this.green = g;
		this.blue = b;
	}

	public EntityLuxCapacitor(World par1World, EntityLivingBase shootingEntity)
	{
		super(par1World, shootingEntity);
		Vec3 direction = shootingEntity.getLookVec().normalize();
		double speed = 1.0;
		this.motionX = direction.xCoord * speed;
		this.motionY = direction.yCoord * speed;
		this.motionZ = direction.zCoord * speed;
		double r = 0.4375;
		double xoffset = 0.1;
		double yoffset = 0;
		double zoffset = 0;
		double horzScale = Math.sqrt(direction.xCoord * direction.xCoord + direction.zCoord * direction.zCoord);
		double horzx = direction.xCoord / horzScale;
		double horzz = direction.zCoord / horzScale;
		this.posX = shootingEntity.posX + direction.xCoord * xoffset - direction.yCoord * horzx * yoffset - horzz * zoffset;
		this.posY = shootingEntity.posY + shootingEntity.getEyeHeight() + direction.yCoord * xoffset + (1 - Math.abs(direction.yCoord)) * yoffset;
		this.posZ = shootingEntity.posZ + direction.zCoord * xoffset - direction.yCoord * horzz * yoffset + horzx * zoffset;
    
		if (shootingEntity instanceof EntityPlayer)
    
	}

    
	@Override
	protected float getGravityVelocity()
	{
		return 0;
	}

	@Override
	public void onEntityUpdate()
	{
		super.onEntityUpdate();
		if (this.ticksExisted > 400)
			this.setDead();
	}

	@Override
	protected void onImpact(MovingObjectPosition movingobjectposition)
	{

		if (!this.isDead && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
		{
			ForgeDirection dir = ForgeDirection.values()[movingobjectposition.sideHit].getOpposite();
			int x = movingobjectposition.blockX - dir.offsetX;
			int y = movingobjectposition.blockY - dir.offsetY;
			int z = movingobjectposition.blockZ - dir.offsetZ;
			if (y > 0)
			{
				Block block = this.worldObj.getBlock(x, y, z);
				if (block == null || block.isAir(this.worldObj, x, y, z))
				{
					Block blockToStickTo = this.worldObj.getBlock(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ);
					if (blockToStickTo.isNormalCube(this.worldObj, x, y, z))
    
						if (this.fake.cantBreak(x, y, z))
						{
							this.setDead();
							return;
    

						this.worldObj.setBlock(x, y, z, MPSItems.getInstance().luxCapacitor, 0, 7);
						this.worldObj.setTileEntity(x, y, z, new TileEntityLuxCapacitor(dir, this.red, this.green, this.blue));
					}
					else
						for (ForgeDirection d : ForgeDirection.values())
						{
							int xo = x + d.offsetX;
							int yo = y + d.offsetY;
							int zo = z + d.offsetZ;
							blockToStickTo = this.worldObj.getBlock(xo, yo, zo);
							if (blockToStickTo.isNormalCube(this.worldObj, x, y, z))
    
								if (this.fake.cantBreak(x, y, z))
								{
									this.setDead();
									return;
    

								this.worldObj.setBlock(x, y, z, MPSItems.getInstance().luxCapacitor, 0, 7);
								this.worldObj.setTileEntity(x, y, z, new TileEntityLuxCapacitor(d, this.red, this.green, this.blue));
							}
						}
				}
			}
			this.setDead();
		}
	}
}
