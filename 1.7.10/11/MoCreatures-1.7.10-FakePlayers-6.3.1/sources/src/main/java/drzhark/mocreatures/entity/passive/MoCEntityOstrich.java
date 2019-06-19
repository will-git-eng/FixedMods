package drzhark.mocreatures.entity.passive;

import java.util.List;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.entity.MoCEntityTameableAnimal;
import drzhark.mocreatures.entity.item.MoCEntityEgg;
import drzhark.mocreatures.inventory.MoCAnimalChest;
import drzhark.mocreatures.network.MoCMessageHandler;
import drzhark.mocreatures.network.message.MoCMessageAnimation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class MoCEntityOstrich extends MoCEntityTameableAnimal
{
	private int eggCounter;
	private int hidingCounter;
	public int mouthCounter;
	public int wingCounter;
	public int sprintCounter;
	public int jumpCounter;
	public int transformCounter;
	public int transformType;
	public boolean canLayEggs;
	public MoCAnimalChest localchest;
	public ItemStack localstack;

	public MoCEntityOstrich(World world)
	{
		super(world);
		this.setSize(1.0F, 1.6F);
		this.setEdad(35);
		super.roper = null;
		this.eggCounter = super.rand.nextInt(1000) + 1000;
		super.stepHeight = 1.0F;
		this.canLayEggs = false;
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.calculateMaxHealth());
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
		super.dataWatcher.addObject(27, Byte.valueOf((byte) 0));
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

	public boolean getEggWatching()
	{
		return super.dataWatcher.getWatchableObjectByte(23) == 1;
	}

	public void setEggWatching(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(23, Byte.valueOf(input));
	}

	public boolean getHiding()
	{
		return super.dataWatcher.getWatchableObjectByte(24) == 1;
	}

	public void setHiding(boolean flag)
	{
		if (!super.worldObj.isRemote)
		{
			byte input = (byte) (flag ? 1 : 0);
			super.dataWatcher.updateObject(24, Byte.valueOf(input));
		}
	}

	public byte getHelmet()
	{
		return super.dataWatcher.getWatchableObjectByte(25);
	}

	public void setHelmet(byte b)
	{
		super.dataWatcher.updateObject(25, Byte.valueOf(b));
	}

	public byte getFlagColor()
	{
		return super.dataWatcher.getWatchableObjectByte(26);
	}

	public void setFlagColor(byte b)
	{
		super.dataWatcher.updateObject(26, Byte.valueOf(b));
	}

	public boolean getIsChested()
	{
		return super.dataWatcher.getWatchableObjectByte(27) == 1;
	}

	public void setIsChested(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(27, Byte.valueOf(input));
	}

	@Override
	public boolean renderName()
	{
		return this.getDisplayName() && super.riddenByEntity == null;
	}

	@Override
	protected boolean isMovementCeased()
	{
		return this.getHiding() || super.riddenByEntity != null;
	}

	@Override
	public boolean isNotScared()
	{
		return this.getType() == 2 && super.entityToAttack != null || this.getType() > 2;
	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float i)
	{
		if (this.getIsTamed() && this.getHelmet() != 0)
		{
			int j = 0;
			switch (this.getHelmet())
			{
				case 1:
					j = 1;
					break;
				case 2:
				case 5:
				case 6:
					j = 2;
					break;
				case 3:
				case 7:
					j = 3;
					break;
				case 4:
				case 9:
				case 10:
				case 11:
				case 12:
					j = 4;
				case 8:
			}

			i -= j;
			if (i <= 0.0F)
				i = 1.0F;
		}

		if (!super.attackEntityFrom(damagesource, i))
			return false;
		else
		{
			Entity entity = damagesource.getEntity();
			if ((super.riddenByEntity == null || entity != super.riddenByEntity) && (!(entity instanceof EntityPlayer) || !this.getIsTamed()))
			{
				if (entity != this && super.worldObj.difficultySetting.getDifficultyId() > 0 && this.getType() > 2)
				{
					super.entityToAttack = entity;
					this.flapWings();
				}

				return true;
			}
			else
				return false;
		}
	}

	@Override
	public void onDeath(DamageSource damagesource)
	{
		super.onDeath(damagesource);
		this.dropMyStuff();
	}

	@Override
	protected void attackEntity(Entity entity, float f)
	{
		if (super.attackTime <= 0 && f < 2.0F && entity.boundingBox.maxY > super.boundingBox.minY && entity.boundingBox.minY < super.boundingBox.maxY)
		{
			super.attackTime = 20;
			this.openMouth();
			this.flapWings();
			entity.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
		}

	}

	public float calculateMaxHealth()
	{
		switch (this.getType())
		{
			case 1:
				return 10.0F;
			case 2:
				return 15.0F;
			case 3:
				return 20.0F;
			case 4:
				return 20.0F;
			case 5:
				return 20.0F;
			default:
				return 20.0F;
		}
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return super.riddenByEntity == null;
	}

	@Override
	public void selectType()
	{
		if (this.getType() == 0)
		{
			int j = super.rand.nextInt(100);
			if (j <= 20)
				this.setType(1);
			else if (j <= 65)
				this.setType(2);
			else if (j <= 95)
				this.setType(3);
			else
				this.setType(4);
		}

	}

	@Override
	public ResourceLocation getTexture()
	{
		if (this.transformCounter != 0 && this.transformType > 4)
		{
			String newText = "ostricha.png";
			if (this.transformType == 5)
				newText = "ostriche.png";

			if (this.transformType == 6)
				newText = "ostrichf.png";

			if (this.transformType == 7)
				newText = "ostrichg.png";

			if (this.transformType == 8)
				newText = "ostrichh.png";

			if (this.transformCounter % 5 == 0)
				return MoCreatures.proxy.getTexture(newText);

			if (this.transformCounter > 50 && this.transformCounter % 3 == 0)
				return MoCreatures.proxy.getTexture(newText);

			if (this.transformCounter > 75 && this.transformCounter % 4 == 0)
				return MoCreatures.proxy.getTexture(newText);
		}

		switch (this.getType())
		{
			case 1:
				return MoCreatures.proxy.getTexture("ostrichc.png");
			case 2:
				return MoCreatures.proxy.getTexture("ostrichb.png");
			case 3:
				return MoCreatures.proxy.getTexture("ostricha.png");
			case 4:
				return MoCreatures.proxy.getTexture("ostrichd.png");
			case 5:
				return MoCreatures.proxy.getTexture("ostriche.png");
			case 6:
				return MoCreatures.proxy.getTexture("ostrichf.png");
			case 7:
				return MoCreatures.proxy.getTexture("ostrichg.png");
			case 8:
				return MoCreatures.proxy.getTexture("ostrichh.png");
			default:
				return MoCreatures.proxy.getTexture("ostricha.png");
		}
	}

	@Override
	public double getCustomSpeed()
	{
		double OstrichSpeed = 0.8D;
		if (this.getType() == 1)
			OstrichSpeed = 0.8D;
		else if (this.getType() == 2)
			OstrichSpeed = 0.8D;
		else if (this.getType() == 3)
			OstrichSpeed = 1.1D;
		else if (this.getType() == 4)
			OstrichSpeed = 1.3D;
		else if (this.getType() == 5)
		{
			OstrichSpeed = 1.4D;
			super.isImmuneToFire = true;
		}

		if (this.sprintCounter > 0 && this.sprintCounter < 200)
			OstrichSpeed *= 1.5D;

		if (this.sprintCounter > 200)
			OstrichSpeed *= 0.5D;

		return OstrichSpeed;
	}

	@Override
	public boolean rideableEntity()
	{
		return true;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (this.getHiding())
			super.prevRenderYawOffset = super.renderYawOffset = super.rotationYaw = super.prevRotationYaw;

		if (this.mouthCounter > 0 && ++this.mouthCounter > 20)
			this.mouthCounter = 0;

		if (this.wingCounter > 0 && ++this.wingCounter > 80)
			this.wingCounter = 0;

		if (this.jumpCounter > 0 && ++this.jumpCounter > 8)
			this.jumpCounter = 0;

		if (this.sprintCounter > 0 && ++this.sprintCounter > 300)
			this.sprintCounter = 0;

		if (this.transformCounter > 0)
		{
			if (this.transformCounter == 40)
				MoCTools.playCustomSound(this, "transform", super.worldObj);

			if (++this.transformCounter > 100)
			{
				this.transformCounter = 0;
				if (this.transformType != 0)
				{
					this.dropArmor();
					this.setType(this.transformType);
				}
			}
		}

	}

	public void transform(int tType)
	{
		if (MoCreatures.isServer())
			MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), tType), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));

		this.transformType = tType;
		if (super.riddenByEntity == null && this.transformType != 0)
		{
			this.dropArmor();
			this.transformCounter = 1;
		}

	}

	@Override
	public void performAnimation(int animationType)
	{
		if (animationType >= 5 && animationType < 9)
		{
			this.transformType = animationType;
			this.transformCounter = 1;
		}

	}

	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();
		if (this.getIsTamed() && MoCreatures.isServer() && super.rand.nextInt(300) == 0 && this.getHealth() <= this.getMaxHealth() && super.deathTime == 0)
			this.setHealth(this.getHealth() + 1.0F);

		if (MoCreatures.isServer())
		{
			if (this.getType() == 8 && this.sprintCounter > 0 && this.sprintCounter < 150 && super.riddenByEntity != null)
				MoCTools.buckleMobs(this, Double.valueOf(2.0D), super.worldObj);

			if (!this.isNotScared() && super.fleeingTick > 0 && super.fleeingTick < 2)
			{
				super.fleeingTick = 0;
				this.setHiding(true);
				this.setPathToEntity((PathEntity) null);
			}

			if (this.getHiding() && ++this.hidingCounter > 500 && !this.getIsTamed())
			{
				this.setHiding(false);
				this.hidingCounter = 0;
			}

			if (this.getType() == 1 && super.rand.nextInt(200) == 0)
			{
				this.setEdad(this.getEdad() + 1);
				if (this.getEdad() >= 100)
				{
					this.setAdult(true);
					this.setType(0);
					this.selectType();
				}
			}

			if (this.canLayEggs && this.getType() == 2 && !this.getEggWatching() && --this.eggCounter <= 0 && super.rand.nextInt(5) == 0)
			{
				EntityPlayer entityplayer1 = super.worldObj.getClosestPlayerToEntity(this, 12.0D);
				if (entityplayer1 != null)
				{
					double distP = MoCTools.getSqDistanceTo(entityplayer1, super.posX, super.posY, super.posZ);
					if (distP < 10.0D)
					{
						int OstrichEggType = 30;
						MoCEntityOstrich maleOstrich = this.getClosestMaleOstrich(this, 8.0D);
						if (maleOstrich != null && super.rand.nextInt(100) < MoCreatures.proxy.ostrichEggDropChance)
						{
							MoCEntityEgg entityegg = new MoCEntityEgg(super.worldObj, OstrichEggType);
							entityegg.setPosition(super.posX, super.posY, super.posZ);
							super.worldObj.spawnEntityInWorld(entityegg);
							if (!this.getIsTamed())
							{
								this.setEggWatching(true);
								if (maleOstrich != null)
									maleOstrich.setEggWatching(true);

								this.openMouth();
							}

							super.worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, (super.rand.nextFloat() - super.rand.nextFloat()) * 0.2F + 1.0F);
							this.eggCounter = super.rand.nextInt(2000) + 2000;
							this.canLayEggs = false;
						}
					}
				}
			}

			if (this.getEggWatching())
			{
				MoCEntityEgg myEgg = (MoCEntityEgg) this.getBoogey(8.0D);
				if (myEgg != null && MoCTools.getSqDistanceTo(myEgg, super.posX, super.posY, super.posZ) > 4.0D)
				{
					PathEntity pathentity = super.worldObj.getPathEntityToEntity(this, myEgg, 16.0F, true, false, false, true);
					this.setPathToEntity(pathentity);
				}

				if (myEgg == null)
				{
					this.setEggWatching(false);
					EntityPlayer eggStealer = super.worldObj.getClosestPlayerToEntity(this, 10.0D);
					if (eggStealer != null && !this.getIsTamed())
					{
						EnumDifficulty var10001 = super.worldObj.difficultySetting;
						if (super.worldObj.difficultySetting != EnumDifficulty.PEACEFUL)
						{
							super.entityToAttack = eggStealer;
							this.flapWings();
						}
					}
				}
			}
		}

	}

	protected MoCEntityOstrich getClosestMaleOstrich(Entity entity, double d)
	{
		double d1 = -1.0D;
		MoCEntityOstrich entityliving = null;
		List list = super.worldObj.getEntitiesWithinAABBExcludingEntity(entity, entity.boundingBox.expand(d, d, d));

		for (int i = 0; i < list.size(); ++i)
		{
			Entity entity1 = (Entity) list.get(i);
			if (entity1 instanceof MoCEntityOstrich && (!(entity1 instanceof MoCEntityOstrich) || ((MoCEntityOstrich) entity1).getType() >= 3))
			{
				double d2 = entity1.getDistanceSq(entity.posX, entity.posY, entity.posZ);
				if ((d < 0.0D || d2 < d * d) && (d1 == -1.0D || d2 < d1))
				{
					d1 = d2;
					entityliving = (MoCEntityOstrich) entity1;
				}
			}
		}

		return entityliving;
	}

	@Override
	public boolean entitiesToInclude(Entity entity)
	{
		return entity instanceof MoCEntityEgg && ((MoCEntityEgg) entity).eggType == 30;
	}

	@Override
	public boolean interact(EntityPlayer entityplayer)
	{
		if (super.interact(entityplayer))
			return false;
		else
		{
			ItemStack itemstack = entityplayer.inventory.getCurrentItem();
			if (!this.getIsTamed() || this.getType() <= 1 || itemstack == null || this.getIsRideable() || itemstack.getItem() != MoCreatures.horsesaddle && itemstack.getItem() != Items.saddle)
			{
				if (!this.getIsTamed() && itemstack != null && this.getType() == 2 && itemstack.getItem() == Items.melon_seeds)
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

					this.openMouth();
					MoCTools.playCustomSound(this, "eating", super.worldObj);
					this.canLayEggs = true;
					return true;
				}
				else if (itemstack != null && itemstack.getItem() == MoCreatures.whip && this.getIsTamed() && super.riddenByEntity == null)
				{
					this.setHiding(!this.getHiding());
					return true;
				}
				else if (itemstack != null && this.getIsTamed() && this.getType() > 1 && itemstack.getItem() == MoCreatures.essencedarkness)
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
					else
						entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

					if (this.getType() == 6)
						this.setHealth(this.getMaxHealth());
					else
						this.transform(6);

					MoCTools.playCustomSound(this, "drinking", super.worldObj);
					return true;
				}
				else if (itemstack != null && this.getIsTamed() && this.getType() > 1 && itemstack.getItem() == MoCreatures.essenceundead)
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
					else
						entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

					if (this.getType() == 7)
						this.setHealth(this.getMaxHealth());
					else
						this.transform(7);

					MoCTools.playCustomSound(this, "drinking", super.worldObj);
					return true;
				}
				else if (itemstack != null && this.getIsTamed() && this.getType() > 1 && itemstack.getItem() == MoCreatures.essencelight)
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
					else
						entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

					if (this.getType() == 8)
						this.setHealth(this.getMaxHealth());
					else
						this.transform(8);

					MoCTools.playCustomSound(this, "drinking", super.worldObj);
					return true;
				}
				else if (itemstack != null && this.getIsTamed() && this.getType() > 1 && itemstack.getItem() == MoCreatures.essencefire)
				{
					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
					else
						entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

					if (this.getType() == 5)
						this.setHealth(this.getMaxHealth());
					else
						this.transform(5);

					MoCTools.playCustomSound(this, "drinking", super.worldObj);
					return true;
				}
				else if (this.getIsTamed() && this.getIsChested() && this.getType() > 1 && itemstack != null && itemstack.getItem() == Item.getItemFromBlock(Blocks.wool))
				{
					int colorInt = itemstack.getItemDamage();
					if (colorInt == 0)
						colorInt = 16;

					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

					MoCTools.playCustomSound(this, "mob.chickenplop", super.worldObj);
					this.dropFlag();
					this.setFlagColor((byte) colorInt);
					return true;
				}
				else if (itemstack != null && this.getType() > 1 && this.getIsTamed() && !this.getIsChested() && itemstack.getItem() == Item.getItemFromBlock(Blocks.chest))
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
    
						this.localchest = new MoCAnimalChest(this, "OstrichChest", 9);

					if (MoCreatures.isServer())
						entityplayer.displayGUIChest(this.localchest);

					return true;
				}
				else
				{
					if (this.getIsTamed() && this.getType() > 1 && itemstack != null)
					{
						Item item = itemstack.getItem();
						if (item instanceof ItemArmor)
						{
							byte helmetType = 0;
							if (itemstack.getItem() == Items.leather_helmet)
								helmetType = 1;
							else if (itemstack.getItem() == Items.iron_helmet)
								helmetType = 2;
							else if (itemstack.getItem() == Items.golden_helmet)
								helmetType = 3;
							else if (itemstack.getItem() == Items.diamond_helmet)
								helmetType = 4;
							else if (itemstack.getItem() == MoCreatures.helmetHide)
								helmetType = 5;
							else if (itemstack.getItem() == MoCreatures.helmetFur)
								helmetType = 6;
							else if (itemstack.getItem() == MoCreatures.helmetCroc)
								helmetType = 7;
							else if (itemstack.getItem() == MoCreatures.scorpHelmetDirt)
								helmetType = 9;
							else if (itemstack.getItem() == MoCreatures.scorpHelmetFrost)
								helmetType = 10;
							else if (itemstack.getItem() == MoCreatures.scorpHelmetCave)
								helmetType = 11;
							else if (itemstack.getItem() == MoCreatures.scorpHelmetNether)
								helmetType = 12;

							if (helmetType != 0)
							{
								entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);
								this.dropArmor();
								MoCTools.playCustomSound(this, "armoroff", super.worldObj);
								this.setHelmet(helmetType);
								return true;
							}
						}
					}

					if (this.getIsRideable() && this.getIsAdult() && super.riddenByEntity == null)
					{
						entityplayer.rotationYaw = super.rotationYaw;
						entityplayer.rotationPitch = super.rotationPitch;
						this.setHiding(false);
						if (!super.worldObj.isRemote && (super.riddenByEntity == null || super.riddenByEntity == entityplayer))
							entityplayer.mountEntity(this);

						return true;
					}
					else
						return false;
				}
			}
			else
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				super.worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, (super.rand.nextFloat() - super.rand.nextFloat()) * 0.2F + 1.0F);
				this.setRideable(true);
				return true;
			}
		}
	}

	private void dropFlag()
	{
		if (MoCreatures.isServer() && this.getFlagColor() != 0)
		{
			int color = this.getFlagColor();
			if (color == 16)
				color = 0;

			EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Blocks.wool, 1, color));
			entityitem.delayBeforeCanPickup = 10;
			super.worldObj.spawnEntityInWorld(entityitem);
			this.setFlagColor((byte) 0);
		}

	}

	private void openMouth()
	{
		this.mouthCounter = 1;
	}

	private void flapWings()
	{
		this.wingCounter = 1;
	}

	@Override
	protected String getHurtSound()
	{
		this.openMouth();
		return "mocreatures:ostrichhurt";
	}

	@Override
	protected String getLivingSound()
	{
		this.openMouth();
		return this.getType() == 1 ? "mocreatures:ostrichchick" : "mocreatures:ostrichgrunt";
	}

	@Override
	protected String getDeathSound()
	{
		this.openMouth();
		return "mocreatures:ostrichdying";
	}

	@Override
	protected Item getDropItem()
	{
		boolean flag = super.rand.nextInt(100) < MoCreatures.proxy.rareItemDropChance;
		return flag && this.getType() == 8 ? MoCreatures.unicornhorn : this.getType() == 5 && flag ? MoCreatures.heartfire : this.getType() == 6 && flag ? MoCreatures.heartdarkness : this.getType() == 7 ? flag ? MoCreatures.heartundead : Items.rotten_flesh : MoCreatures.ostrichraw;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readEntityFromNBT(nbttagcompound);
		this.setRideable(nbttagcompound.getBoolean("Saddle"));
		this.setEggWatching(nbttagcompound.getBoolean("EggWatch"));
		this.setHiding(nbttagcompound.getBoolean("Hiding"));
		this.setHelmet(nbttagcompound.getByte("Helmet"));
		this.setFlagColor(nbttagcompound.getByte("FlagColor"));
		this.setIsChested(nbttagcompound.getBoolean("Bagged"));
		if (this.getIsChested())
		{
    
			this.localchest = new MoCAnimalChest(this, "OstrichChest", 18);

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
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("Saddle", this.getIsRideable());
		nbttagcompound.setBoolean("EggWatch", this.getEggWatching());
		nbttagcompound.setBoolean("Hiding", this.getHiding());
		nbttagcompound.setByte("Helmet", this.getHelmet());
		nbttagcompound.setByte("FlagColor", this.getFlagColor());
		nbttagcompound.setBoolean("Bagged", this.getIsChested());
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
	public boolean getCanSpawnHere()
	{
		return this.getCanSpawnHereCreature() && this.getCanSpawnHereLiving();
	}

	@Override
	public int nameYOffset()
	{
		return this.getType() > 1 ? -105 : -5 - this.getEdad();
	}

	@Override
	public double roperYOffset()
	{
		return this.getType() > 1 ? 0.0D : (120 - this.getEdad()) * 0.01D;
	}

	@Override
	public boolean updateMount()
	{
		return this.getIsTamed();
	}

	@Override
	public boolean forceUpdates()
	{
		return this.getIsTamed();
	}

	@Override
	public boolean isMyHealFood(ItemStack par1ItemStack)
	{
		return this.isItemEdible(par1ItemStack.getItem());
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
	public void dropArmor()
	{
		if (MoCreatures.isServer())
		{
			EntityItem entityitem = null;
			switch (this.getHelmet())
			{
				case 0:
				case 8:
					return;
				case 1:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.leather_helmet, 1));
					break;
				case 2:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.iron_helmet, 1));
					break;
				case 3:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.golden_helmet, 1));
					break;
				case 4:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.diamond_helmet, 1));
					break;
				case 5:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.helmetHide, 1));
					break;
				case 6:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.helmetFur, 1));
					break;
				case 7:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.helmetCroc, 1));
					break;
				case 9:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.scorpHelmetDirt, 1));
					break;
				case 10:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.scorpHelmetFrost, 1));
					break;
				case 11:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.scorpHelmetCave, 1));
					break;
				case 12:
					entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.scorpHelmetNether, 1));
			}

			if (entityitem != null)
			{
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			this.setHelmet((byte) 0);
		}

	}

	@Override
	public boolean isFlyer()
	{
		return this.getType() == 5 || this.getType() == 6;
	}

	@Override
	protected void fall(float f)
	{
		if (!this.isFlyer())
			;
	}

	@Override
	protected double myFallSpeed()
	{
		return 0.99D;
	}

	@Override
	protected double flyerThrust()
	{
		return 0.6D;
	}

	@Override
	protected float flyerFriction()
	{
		return 0.96F;
	}

	@Override
	protected boolean selfPropelledFlyer()
	{
		return this.getType() == 6;
	}

	@Override
	public void makeEntityJump()
	{
		if (this.jumpCounter > 5)
			this.jumpCounter = 1;

		if (this.jumpCounter == 0)
		{
			MoCTools.playCustomSound(this, "wingflap", super.worldObj);
			super.jumpPending = true;
			this.jumpCounter = 1;
		}

	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute()
	{
		return this.getType() == 7 ? EnumCreatureAttribute.UNDEAD : super.getCreatureAttribute();
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return 1;
	}
}
