package emt.entity;

import ru.will.git.emt.ModUtils;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityShield extends Entity
{
	public EntityPlayer owner;




	public EntityShield(World world)
	{
		super(world);
		this.ignoreFrustumCheck = true;
	}

	public EntityShield(World world, EntityPlayer player)
	{
		super(world);
		this.setSize(4, 4);
		this.owner = player;
		this.dataWatcher.updateObject(11, this.owner.getDisplayName());
		this.setPosition(player.posX, player.posY, player.posZ);


	}

	@Override
	protected void entityInit()
	{
		this.dataWatcher.addObject(11, "");
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt)


	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt)


	}

	@Override
	public AxisAlignedBB getBoundingBox()
	{
		return this.boundingBox;
	}

	@Override
	public boolean canBePushed()
	{
		return true;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}

	@Override
	public void setPosition(double x, double y, double z)
	{
		this.posX = x;
		this.posY = y + 0.5f;
		this.posZ = z;
		float f = this.width / 2.0F;
		float f1 = this.height;
		this.boundingBox.setBounds(x - f, y - this.yOffset - 2 + this.ySize, z - f, x + f, y - this.yOffset - 2 + this.ySize + f1, z + f);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (this.needCheck && this.owner == null)
		{
			this.owner = this.worldObj.getPlayerEntityByName(this.dataWatcher.getWatchableObjectString(11));
			this.needCheck = false;
		}
		if (!this.needCheck && this.owner == null)
		{
			this.setDead();
			return;
		}

		if (!this.worldObj.isRemote && this.owner != null)
		{
			this.setPosition(this.owner.posX, this.owner.posY, this.owner.posZ);
			if (!this.owner.isUsingItem())
				this.setDead();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float p_70070_1_)
	{
		return 240;
	}

	@Override
	public void applyEntityCollision(Entity entity)
	{
		if (entity.riddenByEntity != this && entity.ridingEntity != this)

			if (this.fake.cantDamage(entity))


			double ePosX = entity.posX - this.posX;
			double ePosZ = entity.posZ - this.posZ;
			entity.addVelocity(ePosX / 5D, 0.0D, ePosZ / 5D);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRender3d(double x, double y, double z)
	{
		return true;
	}
}
