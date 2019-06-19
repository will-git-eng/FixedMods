package thaumcraft.common.entities.projectile;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.thaumcraft.ModUtils;
import ru.will.git.thaumcraft.tile.OwnerTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import thaumcraft.codechicken.lib.math.MathHelper;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.EntityUtils;

import java.util.ArrayList;

public class EntityShockOrb extends EntityThrowable
{
	public int area = 4;
    
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
    

	public EntityShockOrb(World par1World)
	{
		super(par1World);
	}

	public EntityShockOrb(World par1World, EntityLivingBase par2EntityLiving)
	{
    
		if (par2EntityLiving instanceof EntityPlayer)
    
	}

	@Override
	protected float getGravityVelocity()
	{
		return 0.05F;
	}

	@Override
	protected void onImpact(MovingObjectPosition mop)
	{
		if (!this.worldObj.isRemote)
		{
			ArrayList<Entity> list = EntityUtils.getEntitiesInRange(this.worldObj, this.posX, this.posY, this.posZ, this, Entity.class, this.area);
			for (Entity entity : list)
    
				if (EntityUtils.canEntityBeSeen(this, entity) && !this.fake.cantDamage(entity))
					entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.getThrower()), this.damage);
			}

			for (int i = 0; i < 20; ++i)
			{
				int x = MathHelper.floor_double(this.posX) + this.rand.nextInt(this.area) - this.rand.nextInt(this.area);
				int y = MathHelper.floor_double(this.posY) + this.area;

				int z;
				for (z = MathHelper.floor_double(this.posZ) + this.rand.nextInt(this.area) - this.rand.nextInt(this.area); this.worldObj.isAirBlock(x, y, z) && y > MathHelper.floor_double(this.posY) - this.area; --y)
				{
				}

				if (this.worldObj.isAirBlock(x, y + 1, z) && !this.worldObj.isAirBlock(x, y, z) && this.worldObj.getBlock(x, y + 1, z) != ConfigBlocks.blockAiry && EntityUtils.canEntityBeSeen(this, x + 0.5D, y + 1.5D, z + 0.5D))
    
    
					{
    
						TileEntity tile = this.worldObj.getTileEntity(x, y + 1, z);
						if (tile instanceof OwnerTileEntity)
    
					}
				}
			}
		}

		Thaumcraft.proxy.burst(this.worldObj, this.posX, this.posY, this.posZ, 3.0F);
		this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "thaumcraft:shock", 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
		this.setDead();
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (this.ticksExisted > 500)
			this.setDead();

	}

	@Override
	public float getShadowSize()
	{
		return 0.1F;
	}

	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		if (this.isEntityInvulnerable())
			return false;
		else
		{
			this.setBeenAttacked();
			if (p_70097_1_.getEntity() != null)
			{
				Vec3 vec3 = p_70097_1_.getEntity().getLookVec();
				if (vec3 != null)
				{
					this.motionX = vec3.xCoord;
					this.motionY = vec3.yCoord;
					this.motionZ = vec3.zCoord;
					this.motionX *= 0.9D;
					this.motionY *= 0.9D;
					this.motionZ *= 0.9D;
					this.worldObj.playSoundAtEntity(this, "thaumcraft:zap", 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
				}

				return true;
			}
			else
				return false;
		}
	}
}
