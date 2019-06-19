package drzhark.mocreatures.entity.passive;

import java.util.List;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.entity.MoCEntityTameableAnimal;
import drzhark.mocreatures.entity.item.MoCEntityPlatform;
import drzhark.mocreatures.inventory.MoCAnimalChest;
import drzhark.mocreatures.network.MoCMessageHandler;
import drzhark.mocreatures.network.message.MoCMessageAnimation;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class MoCEntityElephant extends MoCEntityTameableAnimal
{
	public int sprintCounter;
	public int sitCounter;
	public MoCAnimalChest localelephantchest;
	public MoCAnimalChest localelephantchest2;
	public MoCAnimalChest localelephantchest3;
	public MoCAnimalChest localelephantchest4;
	public ItemStack localstack;
	boolean hasPlatform;
	public int tailCounter;
	private byte tuskUses;
	private byte temper;

	public MoCEntityElephant(World world)
	{
		super(world);
		this.setAdult(true);
		this.setTamed(false);
		this.setEdad(50);
		this.setSize(1.1F, 3.0F);
		super.stepHeight = 1.0F;
		if (MoCreatures.isServer())
			if (super.rand.nextInt(4) == 0)
				this.setAdult(false);
			else
				this.setAdult(true);

	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.calculateMaxHealth());
	}

	@Override
	public void selectType()
	{
		this.checkSpawningBiome();
		if (this.getType() == 0)
		{
			int i = super.rand.nextInt(100);
			if (i <= 50)
				this.setType(1);
			else
				this.setType(2);

			this.setHealth(this.getMaxHealth());
		}

	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		super.dataWatcher.addObject(22, Byte.valueOf((byte) 0));
		super.dataWatcher.addObject(23, Byte.valueOf((byte) 0));
		super.dataWatcher.addObject(24, Byte.valueOf((byte) 0));
	}

	public byte getTusks()
	{
		return super.dataWatcher.getWatchableObjectByte(23);
	}

	public void setTusks(byte b)
	{
		super.dataWatcher.updateObject(23, Byte.valueOf(b));
	}

	@Override
	public byte getArmorType()
	{
		return super.dataWatcher.getWatchableObjectByte(22);
	}

	@Override
	public void setArmorType(byte b)
	{
		super.dataWatcher.updateObject(22, Byte.valueOf(b));
	}

	public byte getStorage()
	{
		return super.dataWatcher.getWatchableObjectByte(24);
	}

	public void setStorage(byte b)
	{
		super.dataWatcher.updateObject(24, Byte.valueOf(b));
	}

	@Override
	public ResourceLocation getTexture()
	{
		switch (this.getType())
		{
			case 1:
				return MoCreatures.proxy.getTexture("elephantafrican.png");
			case 2:
				return MoCreatures.proxy.getTexture("elephantindian.png");
			case 3:
				return MoCreatures.proxy.getTexture("mammoth.png");
			case 4:
				return MoCreatures.proxy.getTexture("mammothsonghua.png");
			case 5:
				return MoCreatures.proxy.getTexture("elephantindianpretty.png");
			default:
				return MoCreatures.proxy.getTexture("elephantafrican.png");
		}
	}

	public float calculateMaxHealth()
	{
		switch (this.getType())
		{
			case 1:
				return 40.0F;
			case 2:
				return 30.0F;
			case 3:
				return 50.0F;
			case 4:
				return 60.0F;
			case 5:
				return 40.0F;
			default:
				return 30.0F;
		}
	}

	@Override
	public double getCustomSpeed()
	{
		if (this.sitCounter != 0)
			return 0.0D;
		else
		{
			double tSpeed = 0.5D;
			if (this.getType() == 1)
				tSpeed = 0.6D;
			else if (this.getType() == 2)
				tSpeed = 0.7D;
			else if (this.getType() == 3)
				tSpeed = 0.5D;
			else if (this.getType() == 4)
				tSpeed = 0.5D;
			else if (this.getType() == 5)
				tSpeed = 0.7D;

			if (this.sprintCounter > 0 && this.sprintCounter < 150)
				tSpeed *= 1.5D;

			if (this.sprintCounter > 150)
				tSpeed *= 0.5D;

			return tSpeed;
		}
	}

	@Override
	public void onLivingUpdate()
	{
		if (this.tailCounter > 0 && ++this.tailCounter > 8)
			this.tailCounter = 0;

		if (super.rand.nextInt(200) == 0)
			this.tailCounter = 1;

		super.onLivingUpdate();
		if (MoCreatures.isServer())
		{
			if (!this.getIsAdult() && super.rand.nextInt(1000) == 0)
			{
				this.setEdad(this.getEdad() + 1);
				if (this.getEdad() >= 100)
					this.setAdult(true);
			}

			if (this.sprintCounter > 0 && this.sprintCounter < 150 && super.riddenByEntity != null)
				MoCTools.buckleMobsNotPlayers(this, Double.valueOf(3.0D), super.worldObj);

			if (this.getIsTamed() && super.riddenByEntity == null && this.getArmorType() >= 1 && super.rand.nextInt(20) == 0)
			{
				EntityPlayer ep = super.worldObj.getClosestPlayerToEntity(this, 3.0D);
				if (ep != null && (!MoCreatures.proxy.enableOwnership || ep.getCommandSenderName().equals(this.getOwnerName())) && ep.isSneaking())
					this.sit();
			}

			if (MoCreatures.proxy.elephantBulldozer && this.getIsTamed() && super.riddenByEntity != null && this.getTusks() > 0)
			{
				int height = 2;
				if (this.getType() == 3)
					height = 3;

				if (this.getType() == 4)
					height = 3;

				int dmg = MoCTools.destroyBlocksInFront(this, 2.0D, this.getTusks(), height);
				this.checkTusks(dmg);
			}

			if (super.riddenByEntity != null && super.riddenByEntity instanceof EntityPlayer && this.sitCounter != 0 && this.getArmorType() >= 3 && !this.secondRider())
			{
				List list = super.worldObj.getEntitiesWithinAABBExcludingEntity(this, super.boundingBox.expand(2.0D, 2.0D, 2.0D));

				for (int i = 0; i < list.size(); ++i)
				{
					Entity entity1 = (Entity) list.get(i);
					if (entity1 instanceof EntityPlayer && entity1 != super.riddenByEntity && ((EntityPlayer) entity1).isSneaking())
						this.mountSecondPlayer(entity1);
				}
			}

			if (super.riddenByEntity == null && super.rand.nextInt(100) == 0)
				this.destroyPlatforms();
		}

		if (this.sitCounter != 0 && ++this.sitCounter > 100)
			this.sitCounter = 0;

	}

	private boolean secondRider()
	{
		List list = super.worldObj.getEntitiesWithinAABBExcludingEntity(this, super.boundingBox.expand(3.0D, 3.0D, 3.0D));

		for (int i = 0; i < list.size(); ++i)
		{
			Entity entity1 = (Entity) list.get(i);
			if (entity1 instanceof MoCEntityPlatform && entity1.riddenByEntity != null)
				return true;
		}

		return false;
	}

	private void checkTusks(int dmg)
	{
		this.tuskUses += (byte) dmg;
		if (this.getTusks() == 1 && this.tuskUses > 59 || this.getTusks() == 2 && this.tuskUses > 250 || this.getTusks() == 3 && this.tuskUses > 1000)
		{
			MoCTools.playCustomSound(this, "turtlehurt", super.worldObj);
			this.setTusks((byte) 0);
		}

	}

	private void destroyPlatforms()
	{
		int j = 0;
		List list = super.worldObj.getEntitiesWithinAABBExcludingEntity(this, super.boundingBox.expand(3.0D, 3.0D, 3.0D));

		for (int i = 0; i < list.size(); ++i)
		{
			Entity entity1 = (Entity) list.get(i);
			if (entity1 instanceof MoCEntityPlatform)
			{
				entity1.setDead();
				++j;
			}
		}

	}

	private void sit()
	{
		this.sitCounter = 1;
		if (MoCreatures.isServer())
			MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), 0), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));

		this.setPathToEntity((PathEntity) null);
	}

	@Override
	public void performAnimation(int animationType)
	{
		if (animationType == 0)
		{
			this.sitCounter = 1;
			this.setPathToEntity((PathEntity) null);
		}

	}

	@Override
	public boolean interact(EntityPlayer entityplayer)
	{
		if (super.interact(entityplayer))
			return false;
		else
		{
			ItemStack itemstack = entityplayer.inventory.getCurrentItem();
			if (itemstack != null && !this.getIsTamed() && !this.getIsAdult() && itemstack.getItem() == Items.cake)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "eating", super.worldObj);
				this.temper = (byte) (this.temper + 2);
				this.setHealth(this.getMaxHealth());
				if (MoCreatures.isServer() && !this.getIsAdult() && !this.getIsTamed() && this.temper >= 10)
					MoCTools.tameWithName(entityplayer, this);

				return true;
			}
			else if (itemstack != null && !this.getIsTamed() && !this.getIsAdult() && itemstack.getItem() == MoCreatures.sugarlump)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "eating", super.worldObj);
				++this.temper;
				this.setHealth(this.getMaxHealth());
				if (MoCreatures.isServer() && !this.getIsAdult() && !this.getIsTamed() && this.temper >= 10)
				{
					this.setTamed(true);
					MoCTools.tameWithName(entityplayer, this);
				}

				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && this.getArmorType() == 0 && itemstack.getItem() == MoCreatures.elephantHarness)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "roping", super.worldObj);
				this.setArmorType((byte) 1);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && this.getArmorType() >= 1 && this.getStorage() == 0 && itemstack.getItem() == MoCreatures.elephantChest)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "roping", super.worldObj);
				entityplayer.inventory.addItemStackToInventory(new ItemStack(MoCreatures.key));
				this.setStorage((byte) 1);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && this.getArmorType() >= 1 && this.getStorage() == 1 && itemstack.getItem() == MoCreatures.elephantChest)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "roping", super.worldObj);
				this.setStorage((byte) 2);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && this.getType() == 3 && this.getArmorType() >= 1 && this.getStorage() == 2 && itemstack.getItem() == Item.getItemFromBlock(Blocks.chest))
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "roping", super.worldObj);
				this.setStorage((byte) 3);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && this.getType() == 3 && this.getArmorType() >= 1 && this.getStorage() == 3 && itemstack.getItem() == Item.getItemFromBlock(Blocks.chest))
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "roping", super.worldObj);
				this.setStorage((byte) 4);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && this.getArmorType() == 1 && this.getType() == 2 && itemstack.getItem() == MoCreatures.elephantGarment)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "roping", super.worldObj);
				this.setArmorType((byte) 2);
				this.setType(5);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && this.getArmorType() == 2 && this.getType() == 5 && itemstack.getItem() == MoCreatures.elephantHowdah)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "roping", super.worldObj);
				this.setArmorType((byte) 3);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && this.getArmorType() == 1 && this.getType() == 4 && itemstack.getItem() == MoCreatures.mammothPlatform)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "armoroff", super.worldObj);
				this.setArmorType((byte) 3);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && itemstack.getItem() == MoCreatures.tusksWood)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "armoroff", super.worldObj);
				this.dropTusks();
				this.tuskUses = (byte) itemstack.getItemDamage();
				this.setTusks((byte) 1);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && itemstack.getItem() == MoCreatures.tusksIron)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "armoroff", super.worldObj);
				this.dropTusks();
				this.tuskUses = (byte) itemstack.getItemDamage();
				this.setTusks((byte) 2);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && this.getIsAdult() && itemstack.getItem() == MoCreatures.tusksDiamond)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				MoCTools.playCustomSound(this, "armoroff", super.worldObj);
				this.dropTusks();
				this.tuskUses = (byte) itemstack.getItemDamage();
				this.setTusks((byte) 3);
				return true;
			}
			else
			{
				if (itemstack != null && itemstack.getItem() == MoCreatures.key && this.getStorage() > 0)
				{
    
						this.localelephantchest = new MoCAnimalChest(this, "ElephantChest", 18);

					if (this.getStorage() == 1)
					{
						if (MoCreatures.isServer())
							entityplayer.displayGUIChest(this.localelephantchest);

						return true;
					}

					if (this.getStorage() == 2)
					{
    
							this.localelephantchest2 = new MoCAnimalChest(this, "ElephantChest", 18);

						InventoryLargeChest doubleChest = new InventoryLargeChest("ElephantChest", this.localelephantchest, this.localelephantchest2);
						if (MoCreatures.isServer())
							entityplayer.displayGUIChest(doubleChest);

						return true;
					}

					if (this.getStorage() == 3)
					{
    
							this.localelephantchest2 = new MoCAnimalChest(this, "ElephantChest", 18);

    
							this.localelephantchest3 = new MoCAnimalChest(this, "ElephantChest", 9);

						InventoryLargeChest doubleChest = new InventoryLargeChest("ElephantChest", this.localelephantchest, this.localelephantchest2);
						InventoryLargeChest tripleChest = new InventoryLargeChest("ElephantChest", doubleChest, this.localelephantchest3);
						if (MoCreatures.isServer())
							entityplayer.displayGUIChest(tripleChest);

						return true;
					}

					if (this.getStorage() == 4)
					{
    
							this.localelephantchest2 = new MoCAnimalChest(this, "ElephantChest", 18);

    
							this.localelephantchest3 = new MoCAnimalChest(this, "ElephantChest", 9);

    
							this.localelephantchest4 = new MoCAnimalChest(this, "ElephantChest", 9);

						InventoryLargeChest doubleChest = new InventoryLargeChest("ElephantChest", this.localelephantchest, this.localelephantchest2);
						InventoryLargeChest doubleChestb = new InventoryLargeChest("ElephantChest", this.localelephantchest3, this.localelephantchest4);
						InventoryLargeChest fourChest = new InventoryLargeChest("ElephantChest", doubleChest, doubleChestb);
						if (MoCreatures.isServer())
							entityplayer.displayGUIChest(fourChest);

						return true;
					}
				}

				if (itemstack == null || this.getTusks() <= 0 || itemstack.getItem() != Items.diamond_pickaxe && itemstack.getItem() != Items.wooden_pickaxe && itemstack.getItem() != Items.stone_pickaxe && itemstack.getItem() != Items.iron_pickaxe && itemstack.getItem() != Items.golden_pickaxe)
				{
					if (this.getIsTamed() && this.getIsAdult() && this.getArmorType() >= 1 && this.sitCounter != 0)
					{
						entityplayer.rotationYaw = super.rotationYaw;
						entityplayer.rotationPitch = super.rotationPitch;
						this.sitCounter = 0;
						if (MoCreatures.isServer())
							entityplayer.mountEntity(this);

						return true;
					}
					else
						return false;
				}
				else
				{
					MoCTools.playCustomSound(this, "armoroff", super.worldObj);
					this.dropTusks();
					return true;
				}
			}
		}
	}

	private void mountSecondPlayer(Entity entity)
	{
		double yOff = 2.0D;
		MoCEntityPlatform platform = new MoCEntityPlatform(super.worldObj, this.getEntityId(), yOff, 1.25D);
		platform.setPosition(super.posX, super.posY + yOff, super.posZ);
		super.worldObj.spawnEntityInWorld(platform);
		entity.mountEntity(platform);
	}

	private void dropTusks()
	{
		if (MoCreatures.isServer())
		{
			int i = this.getTusks();
			if (i == 1)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.tusksWood, 1, this.tuskUses));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			if (i == 2)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.tusksIron, 1, this.tuskUses));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			if (i == 3)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.tusksDiamond, 1, this.tuskUses));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			this.setTusks((byte) 0);
			this.tuskUses = 0;
		}
	}

	@Override
	public boolean rideableEntity()
	{
		return true;
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
	public boolean checkSpawningBiome()
	{
		int i = MathHelper.floor_double(super.posX);
		int j = MathHelper.floor_double(super.boundingBox.minY);
		int k = MathHelper.floor_double(super.posZ);
		BiomeGenBase currentbiome = MoCTools.Biomekind(super.worldObj, i, j, k);
		MoCTools.BiomeName(super.worldObj, i, j, k);
		if (BiomeDictionary.isBiomeOfType(currentbiome, Type.FROZEN))
		{
			this.setType(3 + super.rand.nextInt(2));
			this.setHealth(this.getMaxHealth());
			return true;
		}
		else if (BiomeDictionary.isBiomeOfType(currentbiome, Type.DESERT))
		{
			this.setType(1);
			this.setHealth(this.getMaxHealth());
			return true;
		}
		else if (BiomeDictionary.isBiomeOfType(currentbiome, Type.JUNGLE))
		{
			this.setType(2);
			this.setHealth(this.getMaxHealth());
			return true;
		}
		else if (!BiomeDictionary.isBiomeOfType(currentbiome, Type.PLAINS) && !BiomeDictionary.isBiomeOfType(currentbiome, Type.FOREST))
			return false;
		else
		{
			this.setType(1 + super.rand.nextInt(2));
			this.setHealth(this.getMaxHealth());
			return true;
		}
	}

	@Override
	public float getSizeFactor()
	{
		float sizeF = 1.25F;
		switch (this.getType())
		{
			case 2:
			case 5:
				sizeF *= 0.8F;
			case 3:
			default:
				break;
			case 4:
				sizeF *= 1.2F;
		}

		if (!this.getIsAdult())
			sizeF *= this.getEdad() * 0.01F;

		return sizeF;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readEntityFromNBT(nbttagcompound);
		this.setTusks(nbttagcompound.getByte("Tusks"));
		this.setArmorType(nbttagcompound.getByte("Harness"));
		this.setStorage(nbttagcompound.getByte("Storage"));
		this.tuskUses = nbttagcompound.getByte("TuskUses");
		if (this.getStorage() > 0)
		{
    
			this.localelephantchest = new MoCAnimalChest(this, "ElephantChest", 18);

			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
				int j = nbttagcompound1.getByte("Slot") & 255;
				if (j >= 0 && j < this.localelephantchest.getSizeInventory())
					this.localelephantchest.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound1));
			}
		}

		if (this.getStorage() >= 2)
		{
    
			this.localelephantchest2 = new MoCAnimalChest(this, "ElephantChest", 18);

			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
				int j = nbttagcompound1.getByte("Slot") & 255;
				if (j >= 0 && j < this.localelephantchest2.getSizeInventory())
					this.localelephantchest2.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound1));
			}
		}

		if (this.getStorage() >= 3)
		{
    
			this.localelephantchest3 = new MoCAnimalChest(this, "ElephantChest", 9);

			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
				int j = nbttagcompound1.getByte("Slot") & 255;
				if (j >= 0 && j < this.localelephantchest3.getSizeInventory())
					this.localelephantchest3.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound1));
			}
		}

		if (this.getStorage() >= 4)
		{
    
			this.localelephantchest4 = new MoCAnimalChest(this, "ElephantChest", 9);

			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
				int j = nbttagcompound1.getByte("Slot") & 255;
				if (j >= 0 && j < this.localelephantchest4.getSizeInventory())
					this.localelephantchest4.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound1));
			}
		}

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setByte("Tusks", this.getTusks());
		nbttagcompound.setByte("Harness", this.getArmorType());
		nbttagcompound.setByte("Storage", this.getStorage());
		nbttagcompound.setByte("TuskUses", this.tuskUses);
		if (this.getStorage() > 0 && this.localelephantchest != null)
		{
			NBTTagList nbttaglist = new NBTTagList();

			for (int i = 0; i < this.localelephantchest.getSizeInventory(); ++i)
			{
				this.localstack = this.localelephantchest.getStackInSlot(i);
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

		if (this.getStorage() >= 2 && this.localelephantchest2 != null)
		{
			NBTTagList nbttaglist = new NBTTagList();

			for (int i = 0; i < this.localelephantchest2.getSizeInventory(); ++i)
			{
				this.localstack = this.localelephantchest2.getStackInSlot(i);
				if (this.localstack != null)
				{
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte) i);
					this.localstack.writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}

			nbttagcompound.setTag("Items2", nbttaglist);
		}

		if (this.getStorage() >= 3 && this.localelephantchest3 != null)
		{
			NBTTagList nbttaglist = new NBTTagList();

			for (int i = 0; i < this.localelephantchest3.getSizeInventory(); ++i)
			{
				this.localstack = this.localelephantchest3.getStackInSlot(i);
				if (this.localstack != null)
				{
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte) i);
					this.localstack.writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}

			nbttagcompound.setTag("Items3", nbttaglist);
		}

		if (this.getStorage() >= 4 && this.localelephantchest4 != null)
		{
			NBTTagList nbttaglist = new NBTTagList();

			for (int i = 0; i < this.localelephantchest4.getSizeInventory(); ++i)
			{
				this.localstack = this.localelephantchest4.getStackInSlot(i);
				if (this.localstack != null)
				{
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte) i);
					this.localstack.writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}

			nbttagcompound.setTag("Items4", nbttaglist);
		}

	}

	@Override
	public boolean isMyHealFood(ItemStack par1ItemStack)
	{
		return par1ItemStack != null && (par1ItemStack.getItem() == Items.baked_potato || par1ItemStack.getItem() == Items.bread || par1ItemStack.getItem() == MoCreatures.haystack);
	}

	@Override
	public boolean renderName()
	{
		return this.getDisplayName() && super.riddenByEntity == null && super.ridingEntity == null;
	}

	@Override
	protected boolean isMovementCeased()
	{
		return super.riddenByEntity != null || this.sitCounter != 0;
	}

	@Override
	public void setType(int i)
	{
		super.dataWatcher.updateObject(19, Integer.valueOf(i));
	}

	@Override
	public void Riding()
	{
		if (super.riddenByEntity != null && super.riddenByEntity instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer) super.riddenByEntity;
			List list = super.worldObj.getEntitiesWithinAABBExcludingEntity(this, super.boundingBox.expand(1.0D, 0.0D, 1.0D));
			if (list != null)
				for (int i = 0; i < list.size(); ++i)
				{
					Entity entity = (Entity) list.get(i);
					if (!entity.isDead)
						entity.onCollideWithPlayer(entityplayer);
				}

			if (entityplayer.isSneaking() && MoCreatures.isServer())
			{
				if (this.sitCounter == 0)
					this.sit();

				if (this.sitCounter >= 50)
					entityplayer.mountEntity((Entity) null);
			}
		}

	}

	@Override
	public boolean canBePushed()
	{
		return super.riddenByEntity == null;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return super.riddenByEntity == null;
	}

	@Override
	public void updateRiderPosition()
	{
		double dist = 1.0D;
		switch (this.getType())
		{
			case 1:
			case 3:
				dist = 0.8D;
				break;
			case 2:
			case 5:
				dist = 0.1D;
				break;
			case 4:
				dist = 1.2D;
		}

		double newPosX = super.posX - dist * Math.cos(MoCTools.realAngle(super.renderYawOffset - 90.0F) / 57.29578F);
		double newPosZ = super.posZ - dist * Math.sin(MoCTools.realAngle(super.renderYawOffset - 90.0F) / 57.29578F);
		super.riddenByEntity.setPosition(newPosX, super.posY + this.getMountedYOffset() + super.riddenByEntity.getYOffset(), newPosZ);
	}

	@Override
	public double getMountedYOffset()
	{
		double yOff = 0.0D;
		boolean sit = this.sitCounter != 0;
		switch (this.getType())
		{
			case 1:
				yOff = 0.55D;
				if (sit)
					yOff = -0.05D;
				break;
			case 2:
			case 5:
				yOff = 0.0D;
				if (sit)
					yOff = -0.5D;
				break;
			case 3:
				yOff = 0.55D;
				if (sit)
					yOff = -0.05D;
				break;
			case 4:
				yOff = 1.2D;
				if (sit)
					yOff = 0.45D;
		}

		return yOff + super.height * 0.75D;
	}

	@Override
	public boolean isEntityInsideOpaqueBlock()
	{
		return false;
	}

	@Override
	public int getTalkInterval()
	{
		return 300;
	}

	@Override
	protected String getDeathSound()
	{
		return "mocreatures:elephantdying";
	}

	@Override
	protected String getHurtSound()
	{
		return "mocreatures:elephanthurt";
	}

	@Override
	protected String getLivingSound()
	{
		return !this.getIsAdult() && this.getEdad() < 80 ? "mocreatures:elephantcalf" : "mocreatures:elephantgrunt";
	}

	@Override
	protected Item getDropItem()
	{
		return MoCreatures.animalHide;
	}

	@Override
	public boolean getCanSpawnHere()
	{
		return MoCreatures.entityMap.get(this.getClass()).getFrequency() > 0 && this.getCanSpawnHereCreature() && this.getCanSpawnHereLiving();
	}

	@Override
	public void dropMyStuff()
	{
		if (MoCreatures.isServer())
		{
			this.dropTusks();
			this.destroyPlatforms();
			if (this.getStorage() > 0)
			{
				if (this.getStorage() > 0)
				{
					MoCTools.dropCustomItem(this, super.worldObj, new ItemStack(MoCreatures.elephantChest, 1));
					if (this.localelephantchest != null)
						MoCTools.dropInventory(this, this.localelephantchest);
				}

				if (this.getStorage() >= 2)
				{
					if (this.localelephantchest2 != null)
						MoCTools.dropInventory(this, this.localelephantchest2);

					MoCTools.dropCustomItem(this, super.worldObj, new ItemStack(MoCreatures.elephantChest, 1));
				}

				if (this.getStorage() >= 3)
				{
					if (this.localelephantchest3 != null)
						MoCTools.dropInventory(this, this.localelephantchest3);

					MoCTools.dropCustomItem(this, super.worldObj, new ItemStack(Blocks.chest, 1));
				}

				if (this.getStorage() >= 4)
				{
					if (this.localelephantchest4 != null)
						MoCTools.dropInventory(this, this.localelephantchest4);

					MoCTools.dropCustomItem(this, super.worldObj, new ItemStack(Blocks.chest, 1));
				}

				this.setStorage((byte) 0);
			}

			this.dropArmor();
		}

	}

	@Override
	public void dropArmor()
	{
		if (MoCreatures.isServer())
		{
			if (this.getArmorType() >= 1)
				MoCTools.dropCustomItem(this, super.worldObj, new ItemStack(MoCreatures.elephantHarness, 1));

			if (this.getType() == 5 && this.getArmorType() >= 2)
			{
				MoCTools.dropCustomItem(this, super.worldObj, new ItemStack(MoCreatures.elephantGarment, 1));
				if (this.getArmorType() == 3)
					MoCTools.dropCustomItem(this, super.worldObj, new ItemStack(MoCreatures.elephantHowdah, 1));

				this.setType(2);
			}

			if (this.getType() == 4 && this.getArmorType() == 3)
				MoCTools.dropCustomItem(this, super.worldObj, new ItemStack(MoCreatures.mammothPlatform, 1));

			this.setArmorType((byte) 0);
		}
	}

	@Override
	public int nameYOffset()
	{
		return this.getIsAdult() ? (int) (this.getSizeFactor() * -110.0F) : (int) (100 / this.getEdad() * this.getSizeFactor() * -110.0F);
	}

	@Override
	public double roperYOffset()
	{
		return this.getIsAdult() ? this.getSizeFactor() * -0.5D : (double) (100 / this.getEdad()) * (double) this.getSizeFactor() * -0.5D;
	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float i)
	{
		if (super.attackEntityFrom(damagesource, i))
		{
			Entity entity = damagesource.getEntity();
			if (entity != null && this.getIsTamed() && entity instanceof EntityPlayer)
				return false;
			else if (super.riddenByEntity != entity && super.ridingEntity != entity)
			{
				if (entity != this)
				{
					EnumDifficulty var10001 = super.worldObj.difficultySetting;
					if (super.worldObj.difficultySetting != EnumDifficulty.PEACEFUL)
						super.entityToAttack = entity;
				}

				return true;
			}
			else
				return true;
		}
		else
			return false;
	}

	@Override
	protected void attackEntity(Entity entity, float f)
	{
		if (super.attackTime <= 0 && f < 2.0D && entity.boundingBox.maxY > super.boundingBox.minY && entity.boundingBox.minY < super.boundingBox.maxY)
		{
			super.attackTime = 20;
			entity.attackEntityFrom(DamageSource.causeMobDamage(this), 4.0F);
		}

	}

	@Override
	protected void fall(float f)
	{
		int i = (int) Math.ceil(f - 3.0F);
		if (i > 0)
		{
			i = i / 3;
			if (i > 0)
				this.attackEntityFrom(DamageSource.fall, i);

			if (super.riddenByEntity != null && i > 0)
				super.riddenByEntity.attackEntityFrom(DamageSource.fall, i);

			Block block = super.worldObj.getBlock(MathHelper.floor_double(super.posX), MathHelper.floor_double(super.posY - 0.2000000029802322D - super.prevRotationPitch), MathHelper.floor_double(super.posZ));
			if (!block.isAir(super.worldObj, MathHelper.floor_double(super.posX), MathHelper.floor_double(super.posY - 0.2000000029802322D - super.prevRotationPitch), MathHelper.floor_double(super.posZ)))
			{
				SoundType stepsound = block.stepSound;
				super.worldObj.playSoundAtEntity(this, stepsound.getStepResourcePath(), stepsound.getVolume() * 0.5F, stepsound.getPitch() * 0.75F);
			}
		}

	}
}
