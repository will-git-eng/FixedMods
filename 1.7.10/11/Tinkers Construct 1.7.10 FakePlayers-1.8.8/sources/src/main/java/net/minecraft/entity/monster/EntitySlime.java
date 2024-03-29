package net.minecraft.entity.monster;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class EntitySlime extends EntityLiving implements IMob
{
	public float squishAmount;
	public float squishFactor;
	public float prevSquishFactor;
    
	private int slimeJumpDelay;
	private static final String __OBFID = "CL_00001698";

	public EntitySlime(World p_i1742_1_)
	{
		super(p_i1742_1_);
		int i = 1 << this.rand.nextInt(3);
		this.yOffset = 0.0F;
		this.slimeJumpDelay = this.rand.nextInt(20) + 10;
		this.setSlimeSize(i);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(16, new Byte((byte) 1));
	}

	public void setSlimeSize(int p_70799_1_)
	{
		this.dataWatcher.updateObject(16, new Byte((byte) p_70799_1_));
		this.setSize(0.6F * (float) p_70799_1_, 0.6F * (float) p_70799_1_);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue((double) (p_70799_1_ * p_70799_1_));
		this.setHealth(this.getMaxHealth());
		this.experienceValue = p_70799_1_;
	}

    
	public int getSlimeSize()
	{
		return this.dataWatcher.getWatchableObjectByte(16);
	}

    
	@Override
	public void writeEntityToNBT(NBTTagCompound p_70014_1_)
	{
		super.writeEntityToNBT(p_70014_1_);
		p_70014_1_.setInteger("Size", this.getSlimeSize() - 1);
	}

    
	@Override
	public void readEntityFromNBT(NBTTagCompound p_70037_1_)
	{
		super.readEntityFromNBT(p_70037_1_);
		int i = p_70037_1_.getInteger("Size");

		if (i < 0)
		{
			i = 0;
		}

		this.setSlimeSize(i + 1);
	}

    
	protected String getSlimeParticle()
	{
		return "slime";
	}

    
	protected String getJumpSound()
	{
		return "mob.slime." + (this.getSlimeSize() > 1 ? "big" : "small");
	}

    
	@Override
	public void onUpdate()
	{
		if (!this.worldObj.isRemote && this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL && this.getSlimeSize() > 0)
		{
			this.isDead = true;
		}

		this.squishFactor += (this.squishAmount - this.squishFactor) * 0.5F;
		this.prevSquishFactor = this.squishFactor;
		boolean flag = this.onGround;
		super.onUpdate();
		int i;

		if (this.onGround && !flag)
		{
			i = this.getSlimeSize();

			for (int j = 0; j < i * 8; ++j)
			{
				float f = this.rand.nextFloat() * (float) Math.PI * 2.0F;
				float f1 = this.rand.nextFloat() * 0.5F + 0.5F;
				float f2 = MathHelper.sin(f) * (float) i * 0.5F * f1;
				float f3 = MathHelper.cos(f) * (float) i * 0.5F * f1;
				this.worldObj.spawnParticle(this.getSlimeParticle(), this.posX + (double) f2, this.boundingBox.minY, this.posZ + (double) f3, 0.0D, 0.0D, 0.0D);
			}

			if (this.makesSoundOnLand())
			{
				this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
			}

			this.squishAmount = -0.5F;
		}
		else if (!this.onGround && flag)
		{
			this.squishAmount = 1.0F;
		}

		this.alterSquishAmount();

		if (this.worldObj.isRemote)
		{
			i = this.getSlimeSize();
			this.setSize(0.6F * (float) i, 0.6F * (float) i);
		}
	}

	@Override
	protected void updateEntityActionState()
	{
		this.despawnEntity();
		EntityPlayer entityplayer = this.worldObj.getClosestVulnerablePlayerToEntity(this, 16.0D);

		if (entityplayer != null)
		{
			this.faceEntity(entityplayer, 10.0F, 20.0F);
		}

		if (this.onGround && this.slimeJumpDelay-- <= 0)
		{
			this.slimeJumpDelay = this.getJumpDelay();

			if (entityplayer != null)
			{
				this.slimeJumpDelay /= 3;
			}

			this.isJumping = true;

			if (this.makesSoundOnJump())
			{
				this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
			}

			this.moveStrafing = 1.0F - this.rand.nextFloat() * 2.0F;
			this.moveForward = (float) (1 * this.getSlimeSize());
		}
		else
		{
			this.isJumping = false;

			if (this.onGround)
			{
				this.moveStrafing = this.moveForward = 0.0F;
			}
		}
	}

	protected void alterSquishAmount()
	{
		this.squishAmount *= 0.6F;
	}

    
	protected int getJumpDelay()
	{
		return this.rand.nextInt(20) + 10;
	}

	protected EntitySlime createInstance()
	{
		return new EntitySlime(this.worldObj);
	}

    
	@Override
	public void setDead()
	{
		int i = this.getSlimeSize();

		if (!this.worldObj.isRemote && i > 1 && this.getHealth() <= 0.0F)
		{
			int j = 2 + this.rand.nextInt(3);

			for (int k = 0; k < j; ++k)
			{
				float f = ((float) (k % 2) - 0.5F) * (float) i / 4.0F;
				float f1 = ((float) (k / 2) - 0.5F) * (float) i / 4.0F;
				EntitySlime entityslime = this.createInstance();
				entityslime.setSlimeSize(i / 2);
				entityslime.setLocationAndAngles(this.posX + (double) f, this.posY + 0.5D, this.posZ + (double) f1, this.rand.nextFloat() * 360.0F, 0.0F);
				this.worldObj.spawnEntityInWorld(entityslime);
			}
		}

		super.setDead();
	}

    
	@Override
	public void onCollideWithPlayer(EntityPlayer p_70100_1_)
	{
		if (this.canDamagePlayer())
		{
			int i = this.getSlimeSize();

			if (this.canEntityBeSeen(p_70100_1_) && this.getDistanceSqToEntity(p_70100_1_) < 0.6D * (double) i * 0.6D * (double) i && p_70100_1_.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getAttackStrength()))
			{
				this.playSound("mob.attack", 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
			}
		}
	}

    
	protected boolean canDamagePlayer()
	{
		return this.getSlimeSize() > 1;
	}

    
	protected int getAttackStrength()
	{
		return this.getSlimeSize();
	}

    
	@Override
	protected String getHurtSound()
	{
		return "mob.slime." + (this.getSlimeSize() > 1 ? "big" : "small");
	}

    
	@Override
	protected String getDeathSound()
	{
		return "mob.slime." + (this.getSlimeSize() > 1 ? "big" : "small");
	}

	@Override
	protected Item getDropItem()
	{
		return this.getSlimeSize() == 1 ? Items.slime_ball : Item.getItemById(0);
	}

    
	@Override
	public boolean getCanSpawnHere()
	{
		Chunk chunk = this.worldObj.getChunkFromBlockCoords(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ));

		if (this.worldObj.getWorldInfo().getTerrainType().handleSlimeSpawnReduction(this.rand, this.worldObj))
		{
			return false;
		}
		else
		{
			if (this.getSlimeSize() == 1 || this.worldObj.difficultySetting != EnumDifficulty.PEACEFUL)
			{
				BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ));

				if (biomegenbase == BiomeGenBase.swampland && this.posY > 50.0D && this.posY < 70.0D && this.rand.nextFloat() < 0.5F && this.rand.nextFloat() < this.worldObj.getCurrentMoonPhaseFactor() && this.worldObj.getBlockLightValue(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) <= this.rand.nextInt(8))
				{
					return super.getCanSpawnHere();
				}

				if (this.rand.nextInt(10) == 0 && chunk.getRandomWithSeed(987234911L).nextInt(10) == 0 && this.posY < 40.0D)
				{
					return super.getCanSpawnHere();
				}
			}

			return false;
		}
	}

    
	@Override
	protected float getSoundVolume()
	{
		return 0.4F * (float) this.getSlimeSize();
	}

    
	@Override
	public int getVerticalFaceSpeed()
	{
		return 0;
	}

    
	protected boolean makesSoundOnJump()
	{
		return this.getSlimeSize() > 0;
	}

    
	protected boolean makesSoundOnLand()
	{
		return this.getSlimeSize() > 2;
	}
}