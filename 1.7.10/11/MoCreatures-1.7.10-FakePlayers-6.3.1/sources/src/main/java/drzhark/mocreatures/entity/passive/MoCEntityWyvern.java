package drzhark.mocreatures.entity.passive;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.entity.MoCEntityTameableAnimal;
import drzhark.mocreatures.entity.item.MoCEntityEgg;
import drzhark.mocreatures.inventory.MoCAnimalChest;
import drzhark.mocreatures.network.MoCMessageHandler;
import drzhark.mocreatures.network.message.MoCMessageAnimation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MoCEntityWyvern extends MoCEntityTameableAnimal
{
	public MoCAnimalChest localchest;
	public ItemStack localstack;
	public int mouthCounter;
	public int wingFlapCounter;
	public int diveCounter;
	public static final String[] wyvernNames = new String[] { "Jungle", "Swamp", "Savanna", "Sand", "Mother", "Undead", "Light", "Dark", "Arctic", "Cave", "Mountain", "Sea" };

	public MoCEntityWyvern(World world)
	{
		super(world);
		this.setSize(1.9F, 1.7F);
		this.setAdult(false);
		super.stepHeight = 1.0F;
		if (super.rand.nextInt(6) == 0)
			this.setEdad(50 + super.rand.nextInt(50));
		else
			this.setEdad(80 + super.rand.nextInt(20));

	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.getType() >= 5 ? 80.0D : 40.0D);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		super.dataWatcher.addObject(22, Byte.valueOf((byte) 0));
		super.dataWatcher.addObject(23, Byte.valueOf((byte) 0));
		super.dataWatcher.addObject(24, Byte.valueOf((byte) 0));
		super.dataWatcher.addObject(25, Byte.valueOf((byte) 0));
		super.dataWatcher.addObject(26, Byte.valueOf((byte) 0));
	}

	public boolean getIsFlying()
	{
		return super.dataWatcher.getWatchableObjectByte(25) == 1;
	}

	public void setIsFlying(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(25, Byte.valueOf(input));
	}

	@Override
	public byte getArmorType()
	{
		return super.dataWatcher.getWatchableObjectByte(24);
	}

	@Override
	public void setArmorType(byte i)
	{
		super.dataWatcher.updateObject(24, Byte.valueOf(i));
	}

	@Override
	public boolean getIsRideable()
	{
		return super.dataWatcher.getWatchableObjectByte(22) == 1;
	}

	@Override
	public void setRideable(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(22, Byte.valueOf(input));
	}

	public boolean getIsChested()
	{
		return super.dataWatcher.getWatchableObjectByte(23) == 1;
	}

	public void setIsChested(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(23, Byte.valueOf(input));
	}

	public boolean getIsSitting()
	{
		return super.dataWatcher.getWatchableObjectByte(26) == 1;
	}

	public void setSitting(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(26, Byte.valueOf(input));
	}

	@Override
	public void selectType()
	{
		if (this.getType() == 0)
		{
			int i = super.rand.nextInt(100);
			if (i <= 12)
				this.setType(1);
			else if (i <= 24)
				this.setType(2);
			else if (i <= 36)
				this.setType(3);
			else if (i <= 48)
				this.setType(4);
			else if (i <= 60)
				this.setType(9);
			else if (i <= 72)
				this.setType(10);
			else if (i <= 84)
				this.setType(11);
			else if (i <= 95)
				this.setType(12);
			else
				this.setType(5);
		}

	}

	@Override
	public boolean isNotScared()
	{
		return true;
	}

	@Override
	public ResourceLocation getTexture()
	{
		switch (this.getType())
		{
			case 1:
				return MoCreatures.proxy.getTexture("wyvernjungle.png");
			case 2:
				return MoCreatures.proxy.getTexture("wyvernmix.png");
			case 3:
				return MoCreatures.proxy.getTexture("wyvernsand.png");
			case 4:
				return MoCreatures.proxy.getTexture("wyvernsun.png");
			case 5:
				return MoCreatures.proxy.getTexture("wyvernmother.png");
			case 6:
				return MoCreatures.proxy.getTexture("wyvernundead.png");
			case 7:
				return MoCreatures.proxy.getTexture("wyvernlight.png");
			case 8:
				return MoCreatures.proxy.getTexture("wyverndark.png");
			case 9:
				return MoCreatures.proxy.getTexture("wyvernarctic.png");
			case 10:
				return MoCreatures.proxy.getTexture("wyverncave.png");
			case 11:
				return MoCreatures.proxy.getTexture("wyvernmountain.png");
			case 12:
				return MoCreatures.proxy.getTexture("wyvernsea.png");
			default:
				return MoCreatures.proxy.getTexture("wyvernsun.png");
		}
	}

	@Override
	public void onLivingUpdate()
	{
		if (!this.getIsFlying() && this.isOnAir() && super.motionY < 0.0D)
			super.motionY *= 0.6D;

		if (this.mouthCounter > 0 && ++this.mouthCounter > 30)
			this.mouthCounter = 0;

		if (this.diveCounter > 0 && ++this.diveCounter > 5)
			this.diveCounter = 0;

		if (super.jumpPending)
		{
			if (this.wingFlapCounter == 0)
				MoCTools.playCustomSound(this, "wingflap", super.worldObj);

			this.wingFlapCounter = 1;
		}

		if (this.isOnAir() && super.rand.nextInt(30) == 0)
			this.wingFlapCounter = 1;

		if (this.wingFlapCounter > 0 && ++this.wingFlapCounter > 20)
			this.wingFlapCounter = 0;

		if (this.isFlyingAlone())
			this.wingFlapCounter = 1;

		if (MoCreatures.isServer())
		{
			if (!this.getIsAdult() && super.rand.nextInt(500) == 0)
			{
				this.setEdad(this.getEdad() + 1);
				if (this.getEdad() >= this.getMaxAge())
					this.setAdult(true);
			}

			if (this.isFlyingAlone() && super.rand.nextInt(60) == 0 && !this.isMovementCeased())
				this.wingFlap();

			if (this.isFlyingAlone() && !this.hasPath() && !this.isMovementCeased() && super.entityToAttack == null && super.rand.nextInt(20) == 0)
				this.updateWanderPath();

			if (super.riddenByEntity != null)
				this.setIsFlying(false);
			else if (super.entityToAttack != null && super.rand.nextInt(20) == 0)
				this.setIsFlying(true);
			else if (!this.getIsTamed() && super.rand.nextInt(300) == 0)
				this.setIsFlying(!this.getIsFlying());

			if (!this.getIsTamed() && super.dimension == MoCreatures.WyvernLairDimensionID && super.rand.nextInt(50) == 0 && super.posY < 10.0D)
				this.setDead();
		}

		if (super.motionY > 0.5D)
			super.motionY = 0.5D;

		super.onLivingUpdate();
	}

	public void wingFlap()
	{
		if (this.wingFlapCounter == 0)
			MoCTools.playCustomSound(this, "wyvernwingflap", super.worldObj);

		this.wingFlapCounter = 1;
		super.motionY = 0.5D;
	}

	@Override
	public float getSizeFactor()
	{
		return this.getEdad() * 0.01F;
	}

	@Override
	public boolean isFlyingAlone()
	{
		return this.getIsFlying() && super.riddenByEntity == null;
	}

	@Override
	public int flyingHeight()
	{
		return 18;
	}

	protected float getFlyingSpeed()
	{
		return 0.7F;
	}

	@Override
	public boolean interact(EntityPlayer entityplayer)
	{
		if (super.interact(entityplayer))
			return false;
		else
		{
			ItemStack itemstack = entityplayer.inventory.getCurrentItem();
			if (itemstack != null && itemstack.getItem() == MoCreatures.whip && this.getIsTamed() && super.riddenByEntity == null)
			{
				this.setSitting(!this.getIsSitting());
				return true;
			}
			else if (itemstack == null || this.getIsRideable() || this.getEdad() <= 90 || !this.getIsTamed() || itemstack.getItem() != Items.saddle && itemstack.getItem() != MoCreatures.horsesaddle)
			{
				if (itemstack != null && this.getIsTamed() && this.getEdad() > 90 && itemstack.getItem() == Items.iron_horse_armor)
				{
					if (this.getArmorType() == 0)
						MoCTools.playCustomSound(this, "armorput", super.worldObj);

					this.dropArmor();
					this.setArmorType((byte) 1);
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

					return true;
				}
				else if (itemstack != null && this.getIsTamed() && this.getEdad() > 90 && itemstack.getItem() == Items.golden_horse_armor)
				{
					if (this.getArmorType() == 0)
						MoCTools.playCustomSound(this, "armorput", super.worldObj);

					this.dropArmor();
					this.setArmorType((byte) 2);
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

					return true;
				}
				else if (itemstack != null && this.getIsTamed() && this.getEdad() > 90 && itemstack.getItem() == Items.diamond_horse_armor)
				{
					if (this.getArmorType() == 0)
						MoCTools.playCustomSound(this, "armorput", super.worldObj);

					this.dropArmor();
					this.setArmorType((byte) 3);
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

					return true;
				}
				else if (itemstack != null && this.getIsTamed() && this.getEdad() > 90 && !this.getIsChested() && itemstack.getItem() == Item.getItemFromBlock(Blocks.chest))
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

					entityplayer.inventory.addItemStackToInventory(new ItemStack(MoCreatures.key));
					this.setIsChested(true);
					super.worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, (super.rand.nextFloat() - super.rand.nextFloat()) * 0.2F + 1.0F);
					return true;
				}
				else if (itemstack != null && itemstack.getItem() == MoCreatures.key && this.getIsChested())
				{
    
						this.localchest = new MoCAnimalChest(this, "WyvernChest", 14);

					if (MoCreatures.isServer())
						entityplayer.displayGUIChest(this.localchest);

					return true;
				}
				else if (itemstack != null && itemstack.getItem() == MoCreatures.essencelight && this.getIsTamed() && this.getEdad() > 90 && this.getType() < 5)
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
					else
						entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

					if (MoCreatures.isServer())
					{
						int i = this.getType() + 49;
						MoCEntityEgg entityegg = new MoCEntityEgg(super.worldObj, i);
						entityegg.setPosition(entityplayer.posX, entityplayer.posY, entityplayer.posZ);
						entityplayer.worldObj.spawnEntityInWorld(entityegg);
						entityegg.motionY += super.worldObj.rand.nextFloat() * 0.05F;
						entityegg.motionX += (super.worldObj.rand.nextFloat() - super.worldObj.rand.nextFloat()) * 0.3F;
						entityegg.motionZ += (super.worldObj.rand.nextFloat() - super.worldObj.rand.nextFloat()) * 0.3F;
					}

					return true;
				}
				else if (itemstack != null && this.getType() == 5 && itemstack.getItem() == MoCreatures.essenceundead && this.getIsTamed())
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
					else
						entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

					if (MoCreatures.isServer())
						this.setType(6);

					return true;
				}
				else if (itemstack != null && this.getType() == 5 && itemstack.getItem() == MoCreatures.essencelight && this.getIsTamed())
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
					else
						entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

					if (MoCreatures.isServer())
						this.setType(7);

					return true;
				}
				else if (itemstack != null && this.getType() == 5 && itemstack.getItem() == MoCreatures.essencedarkness && this.getIsTamed())
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
					else
						entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

					if (MoCreatures.isServer())
						this.setType(8);

					return true;
				}
				else if (this.getIsRideable() && this.getEdad() > 90 && super.riddenByEntity == null)
				{
					entityplayer.rotationYaw = super.rotationYaw;
					entityplayer.rotationPitch = super.rotationPitch;
					if (MoCreatures.isServer())
					{
						entityplayer.mountEntity(this);
						this.setSitting(false);
					}

					return true;
				}
				else
					return false;
			}
			else
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				this.setRideable(true);
				return true;
			}
		}
	}

	@Override
	public void dropArmor()
	{
		if (MoCreatures.isServer())
		{
			int i = this.getArmorType();
			if (i != 0)
				MoCTools.playCustomSound(this, "armoroff", super.worldObj);

			if (i == 1)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.iron_horse_armor, 1));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			if (i == 2)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.golden_horse_armor, 1));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			if (i == 3)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.diamond_horse_armor, 1));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			this.setArmorType((byte) 0);
		}

	}

	@Override
	public boolean rideableEntity()
	{
		return true;
	}

	@Override
	protected String getDeathSound()
	{
		return "mocreatures:wyverndying";
	}

	@Override
	protected String getHurtSound()
	{
		this.openMouth();
		return "mocreatures:wyvernhurt";
	}

	@Override
	protected String getLivingSound()
	{
		this.openMouth();
		return "mocreatures:wyverngrunt";
	}

	@Override
	public int getTalkInterval()
	{
		return 400;
	}

	@Override
	protected boolean isMovementCeased()
	{
		return super.riddenByEntity != null || this.getIsSitting();
	}

	@Override
	public boolean isFlyer()
	{
		return true;
	}

	@Override
	protected void fall(float f)
	{
	}

	@Override
	public double getMountedYOffset()
	{
		return super.height * 0.9D * this.getSizeFactor();
	}

	@Override
	public void updateRiderPosition()
	{
		double dist = this.getSizeFactor() * 0.3D;
		double newPosX = super.posX - dist * Math.cos(MoCTools.realAngle(super.renderYawOffset - 90.0F) / 57.29578F);
		double newPosZ = super.posZ - dist * Math.sin(MoCTools.realAngle(super.renderYawOffset - 90.0F) / 57.29578F);
		super.riddenByEntity.setPosition(newPosX, super.posY + this.getMountedYOffset() + super.riddenByEntity.getYOffset(), newPosZ);
	}

	@Override
	protected void attackEntity(Entity entity, float f)
	{
		if (super.attackTime <= 0 && f < 3.0D && entity.boundingBox.maxY > super.boundingBox.minY && entity.boundingBox.minY < super.boundingBox.maxY)
		{
			super.attackTime = 20;
			boolean flag = super.rand.nextInt(3) == 0;
			if (flag)
			{
				if (entity instanceof EntityPlayer)
					MoCreatures.poisonPlayer((EntityPlayer) entity);

				((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.poison.id, 200, 0));
				MoCTools.playCustomSound(this, "wyvernpoisoning", super.worldObj);
			}

			int dmg = 5;
			if (this.getType() >= 5)
				dmg = 10;

			entity.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
			this.openMouth();
		}

	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float i)
	{
		if (super.attackEntityFrom(damagesource, i))
		{
			Entity entity = damagesource.getEntity();
			if (entity != null && this.getIsTamed() && entity instanceof EntityPlayer)
				return false;
			else if (super.riddenByEntity != null && entity == super.riddenByEntity)
				return false;
			else
			{
				if (entity != this && super.worldObj.difficultySetting.getDifficultyId() > 0)
					super.entityToAttack = entity;

				return true;
			}
		}
		else
			return false;
	}

	@Override
	protected Entity findPlayerToAttack()
	{
		if (super.worldObj.difficultySetting.getDifficultyId() > 0 && !this.getIsTamed())
		{
			EntityPlayer entityplayer = super.worldObj.getClosestVulnerablePlayerToEntity(this, 10.0D);
			if (entityplayer != null)
				return entityplayer;

			if (super.rand.nextInt(500) == 0)
			{
				EntityLivingBase entityliving = this.getClosestEntityLiving(this, 8.0D);
				return entityliving;
			}
		}

		return null;
	}

	@Override
	public boolean entitiesToIgnore(Entity entity)
	{
		return super.entitiesToIgnore(entity) || entity instanceof MoCEntityWyvern || entity instanceof EntityPlayer;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("Saddle", this.getIsRideable());
		nbttagcompound.setBoolean("Chested", this.getIsChested());
		nbttagcompound.setByte("ArmorType", this.getArmorType());
		nbttagcompound.setBoolean("isSitting", this.getIsSitting());
		if (this.getIsChested() && this.localchest != null)
		{
			NBTTagList nbttaglist = new NBTTagList();

			for (int i = 0; i < this.localchest.getSizeInventory(); ++i)
			{
				this.localstack = this.localchest.getStackInSlot(i);
				if (this.localstack != null)
				{
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte) i);
					this.localstack.writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}

			nbttagcompound.setTag("Items", nbttaglist);
		}

	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readEntityFromNBT(nbttagcompound);
		this.setRideable(nbttagcompound.getBoolean("Saddle"));
		this.setIsChested(nbttagcompound.getBoolean("Chested"));
		this.setArmorType(nbttagcompound.getByte("ArmorType"));
		this.setSitting(nbttagcompound.getBoolean("isSitting"));
		if (this.getIsChested())
		{
    
			this.localchest = new MoCAnimalChest(this, "WyvernChest", 14);

			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
				int j = nbttagcompound1.getByte("Slot") & 255;
				if (j >= 0 && j < this.localchest.getSizeInventory())
					this.localchest.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound1));
			}
		}

	}

	@Override
	public double roperYOffset()
	{
		return this.getIsAdult() ? 0.0D : (130 - this.getEdad()) * 0.01D;
	}

	@Override
	public int nameYOffset()
	{
		int yOff = this.getEdad() * -1;
		if (yOff < -120)
			yOff = -120;

		return yOff;
	}

	@Override
	public boolean isMyHealFood(ItemStack par1ItemStack)
	{
		return par1ItemStack != null && (par1ItemStack.getItem() == MoCreatures.ratRaw || par1ItemStack.getItem() == MoCreatures.rawTurkey);
	}

	private void openMouth()
	{
		if (MoCreatures.isServer())
		{
			this.mouthCounter = 1;
			MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), 1), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));
		}

	}

	@Override
	public void performAnimation(int animationType)
	{
		if (animationType == 1)
			this.mouthCounter = 1;

		if (animationType == 2)
			this.diveCounter = 1;

	}

	@Override
	public void makeEntityDive()
	{
		if (MoCreatures.isServer())
			MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), 2), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));

		super.makeEntityDive();
	}

	@Override
	protected void dropFewItems(boolean flag, int x)
	{
		int i = MathHelper.floor_double(super.posX);
		int j = MathHelper.floor_double(super.boundingBox.minY);
		int k = MathHelper.floor_double(super.posZ);
		int chance = MoCreatures.proxy.wyvernEggDropChance;
		if (this.getType() == 5)
			chance = MoCreatures.proxy.motherWyvernEggDropChance;

		MoCTools.BiomeName(super.worldObj, i, j, k);
		if (super.rand.nextInt(100) < chance)
			this.entityDropItem(new ItemStack(MoCreatures.mocegg, 1, this.getType() + 49), 0.0F);

	}

	@Override
	public boolean canBeCollidedWith()
	{
		return super.riddenByEntity == null;
	}

	@Override
	public void dropMyStuff()
	{
		if (MoCreatures.isServer())
		{
			this.dropArmor();
			MoCTools.dropSaddle(this, super.worldObj);
			if (this.getIsChested())
			{
				MoCTools.dropInventory(this, this.localchest);
				MoCTools.dropCustomItem(this, super.worldObj, new ItemStack(Blocks.chest, 1));
				this.setIsChested(false);
			}
		}

	}

	@Override
	public float getAdjustedYOffset()
	{
		return this.getIsSitting() ? 0.4F : 0.0F;
	}

	@Override
	public double getCustomSpeed()
	{
		return super.riddenByEntity != null ? this.getType() < 5 ? 2.0D : 3.0D : 0.8D;
	}

	private int getMaxAge()
	{
		return this.getType() >= 5 ? 180 : 100;
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute()
	{
		return this.getType() == 6 ? EnumCreatureAttribute.UNDEAD : super.getCreatureAttribute();
	}

	@Override
	public boolean swimmerEntity()
	{
		return true;
	}
}
