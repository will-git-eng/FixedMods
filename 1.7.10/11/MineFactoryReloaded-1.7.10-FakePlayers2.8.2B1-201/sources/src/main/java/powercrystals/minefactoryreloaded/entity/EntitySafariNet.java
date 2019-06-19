package powercrystals.minefactoryreloaded.entity;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.minefactoryreloaded.EventConfig;
import ru.will.git.minefactoryreloaded.ModUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.item.ItemSafariNet;

public class EntitySafariNet extends EntityThrowable
    
    

	public EntitySafariNet(World var1)
	{
		super(var1);
		this.renderDistanceWeight = 10.0D;
		this.dataWatcher.addObjectByDataType(13, 5);
	}

	public EntitySafariNet(World var1, double var2, double var4, double var6, ItemStack var8)
	{
		super(var1, var2, var4, var6);
		this.renderDistanceWeight = 10.0D;
		this.dataWatcher.addObject(13, var8);
	}

	public EntitySafariNet(World var1, EntityLivingBase var2, ItemStack var3)
	{
		super(var1, var2);
		this.renderDistanceWeight = 10.0D;
    
		if (var2 instanceof EntityPlayer)
    
	}

	public ItemStack getStoredEntity()
	{
		return this.dataWatcher.getWatchableObjectItemStack(13);
	}

	public void setStoredEntity(ItemStack var1)
	{
		this.dataWatcher.updateObject(13, var1);
	}

	protected boolean onHitBlock(ItemStack stack, MovingObjectPosition mop)
    
		if (EventConfig.safariNet && this.fake.cantBreak(mop.blockX, mop.blockY, mop.blockZ))
		{
			this.dropAsStack(stack);
			return false;
    

		if (ItemSafariNet.isEmpty(stack))
			this.dropAsStack(stack);
		else
		{
			ItemSafariNet.releaseEntity(stack, this.worldObj, mop.blockX, mop.blockY, mop.blockZ, mop.sideHit);
			if (ItemSafariNet.isSingleUse(stack))
				this.dropAsStack(null);
			else
				this.dropAsStack(stack);
		}

		return true;
	}

	protected boolean onHitEntity(ItemStack stack, MovingObjectPosition mop)
    
		if (EventConfig.safariNet && mop.entityHit != null && this.fake.cantDamage(mop.entityHit))
		{
			this.dropAsStack(stack);
			return false;
    

		if (ItemSafariNet.isEmpty(stack) && mop.entityHit instanceof EntityLivingBase)
		{
			ItemSafariNet.captureEntity(stack, (EntityLivingBase) mop.entityHit);
			this.dropAsStack(stack);
		}
		else
		{
			if (!ItemSafariNet.isEmpty(stack))
			{
				Entity var3 = ItemSafariNet.releaseEntity(stack, this.worldObj, (int) mop.entityHit.posX, (int) mop.entityHit.posY, (int) mop.entityHit.posZ, 1);
				if (mop.entityHit instanceof EntityLivingBase)
				{
					if (var3 instanceof EntityLiving)
						((EntityLiving) var3).setAttackTarget((EntityLivingBase) mop.entityHit);

					if (var3 instanceof EntityCreature)
						((EntityCreature) var3).setTarget(mop.entityHit);
				}

				if (ItemSafariNet.isSingleUse(stack))
				{
					this.setDead();
					return true;
				}
			}

			this.dropAsStack(stack);
		}

		return true;
	}

	protected void impact(double var1, double var3, double var5, int var7)
	{
	}

	@Override
	protected void onImpact(MovingObjectPosition mop)
	{
		ItemStack stack = this.dataWatcher.getWatchableObjectItemStack(13);
		boolean success = false;
		double x;
		double y;
		double z;
		int side;
		if (mop.typeOfHit == MovingObjectType.ENTITY)
		{
			success = this.onHitEntity(stack, mop);
			x = mop.entityHit.posX;
			y = mop.entityHit.posY;
			z = mop.entityHit.posZ;
			side = -1;
		}
		else
		{
			success = this.onHitBlock(stack, mop);
			x = mop.blockX;
			y = mop.blockY;
			z = mop.blockZ;
			side = mop.sideHit;
		}

		if (success)
			this.impact(x, y, z, side);

	}

	protected void dropAsStack(ItemStack var1)
	{
		if (!this.worldObj.isRemote && var1 != null)
		{
			EntityItem var2 = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, var1.copy());
			var2.delayBeforeCanPickup = 40;
			this.worldObj.spawnEntityInWorld(var2);
		}

		this.setDead();
	}

	public IIcon getIcon()
	{
		return this.dataWatcher.getWatchableObjectItemStack(13).getIconIndex();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound var1)
	{
		super.writeEntityToNBT(var1);
		NBTTagCompound var2 = new NBTTagCompound();
		this.dataWatcher.getWatchableObjectItemStack(13).writeToNBT(var2);
    
    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound var1)
	{
		super.readEntityFromNBT(var1);
		NBTTagCompound var2 = var1.getCompoundTag("safariNetStack");
    
    
	}
}
