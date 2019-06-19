package chylex.hee.entity.projectile;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.entity.technical.EntityTechnicalCurseBlock;
import chylex.hee.entity.technical.EntityTechnicalCurseEntity;
import chylex.hee.mechanics.curse.CurseType;
import chylex.hee.system.util.BlockPosM;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.hee.ModUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class EntityProjectileCurse extends EntityThrowable
{
	private UUID throwerID;
	private CurseType curseType;
	private boolean eternal;

	    
	public final FakePlayerContainer fake = new FakePlayerContainerEntity(ModUtils.profile, this);
	    

	public EntityProjectileCurse(World world)
	{
		super(world);
	}

	public EntityProjectileCurse(World world, EntityPlayer thrower, CurseType type, boolean eternal)
	{
		super(world, thrower);
		this.throwerID = thrower.getUniqueID();
		this.curseType = type;
		this.eternal = eternal;

		    
		this.fake.setRealPlayer(thrower);
		    
	}

	public CurseType getType()
	{
		return this.curseType == null ? (this.curseType = CurseType.getFromDamage(this.dataWatcher.getWatchableObjectByte(16) - 1)) : this.curseType;
	}

	@Override
	protected void entityInit()
	{
		this.dataWatcher.addObject(16, (byte) 0);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (this.worldObj.isRemote)
		{
			this.getType();

			if (this.curseType != null)
				for (int a = 0; a < 1 + this.rand.nextInt(2); a++)
				{
					HardcoreEnderExpansion.fx.curse(this.worldObj, this.posX + (this.rand.nextDouble() - 0.5D) * 0.15D, this.posY + (this.rand.nextDouble() - 0.5D) * 0.15D, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.15D, this.curseType);
				}
		}
		else if (this.ticksExisted == 1)
			this.dataWatcher.updateObject(16, (byte) (this.curseType.damage + 1));
	}

	@Override
	protected void onImpact(MovingObjectPosition mop)
	{
		if (!this.worldObj.isRemote)
		{
			if (mop.typeOfHit == MovingObjectType.ENTITY)
			{
				for (EntityLivingBase entity : (List<EntityLivingBase>) this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.boundingBox.expand(4D, 2D, 4D)))
				{
					if (this.getDistanceSqToEntity(entity) < 16D && !entity.getUniqueID().equals(this.throwerID) && !(entity instanceof IBossDisplayData))
					{
						    
						if (this.fake.cantDamage(entity))
							continue;
						    

						this.worldObj.spawnEntityInWorld(new EntityTechnicalCurseEntity(this.worldObj, entity, this.curseType, this.eternal));
					}
				}
			}
			else if (mop.typeOfHit == MovingObjectType.BLOCK)
			{
				int yy = BlockPosM.tmp(mop.blockX, mop.blockY, mop.blockZ).getBlock(this.worldObj).isReplaceable(this.worldObj, mop.blockX, mop.blockY, mop.blockZ) ? mop.blockY - 1 : mop.blockY;

				    
				if (this.fake.cantBreak(mop.blockX, yy, mop.blockZ))
				{
					this.setDead();
					return;
				}
				    

				this.worldObj.spawnEntityInWorld(new EntityTechnicalCurseBlock(this.worldObj, mop.blockX, yy, mop.blockZ, this.throwerID, this.curseType, this.eternal));
			}

			this.setDead();
		}
		else if (this.curseType != null)
		{
			this.worldObj.playSound(this.posX, this.posY, this.posZ, "hardcoreenderexpansion:mob.random.curse", 0.8F, 0.9F + this.rand.nextFloat() * 0.2F, false);
			for (int a = 0; a < 40; a++)
			{
				HardcoreEnderExpansion.fx.curse(this.worldObj, this.posX + (this.rand.nextDouble() - 0.5D) * 1.5D, this.posY + (this.rand.nextDouble() - 0.5D) * 1.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * 1.5D, this.curseType);
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		nbt.setByte("curse", this.curseType.damage);
		nbt.setBoolean("eternal", this.eternal);
		nbt.setLong("thr1", this.throwerID.getLeastSignificantBits());
		nbt.setLong("thr2", this.throwerID.getMostSignificantBits());

		    
		this.fake.writeToNBT(nbt);
		    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		if ((this.curseType = CurseType.getFromDamage(nbt.getByte("curse"))) == null)
			this.setDead();
		this.eternal = nbt.getBoolean("eternal");
		this.throwerID = new UUID(nbt.getLong("thr2"), nbt.getLong("thr1"));

		    
		this.fake.readFromNBT(nbt);
		    
	}
}
