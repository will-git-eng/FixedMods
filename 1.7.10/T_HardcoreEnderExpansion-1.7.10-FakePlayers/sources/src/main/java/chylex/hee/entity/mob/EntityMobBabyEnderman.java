package chylex.hee.entity.mob;

import chylex.hee.entity.GlobalMobData;
import chylex.hee.entity.mob.util.IEndermanRenderer;
import chylex.hee.init.BlockList;
import chylex.hee.init.ItemList;
import chylex.hee.mechanics.causatum.CausatumMeters;
import chylex.hee.mechanics.causatum.CausatumUtils;
import chylex.hee.mechanics.misc.Baconizer;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C00ClearInventorySlot;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.MathUtil;
import ru.will.git.hee.EventConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntityMobBabyEnderman extends EntityMob implements IEndermanRenderer, GlobalMobData.IIgnoreEnderGoo
{
	private EntityPlayer target;
	private final List<ItemPriorityLevel> itemPriorities = new ArrayList<ItemPriorityLevel>();
	private ItemPriorityLevel carryingLevel = ItemPriorityLevel.RANDOM;
	private byte itemDecisionTimer;
	private byte attentionLossTimer = -125;
	private boolean isFamilyChosen;
	private boolean isScared;

	public EntityMobBabyEnderman(World world)
	{
		super(world);
		this.setSize(0.5F, 1.26F);
		this.stepHeight = 1.0F;

		Collections.addAll(this.itemPriorities, ItemPriorityLevel.values);

		int a = 0;

		for (int size = this.itemPriorities.size(); a < this.rand.nextInt(20); ++a)
		{
			int index1 = this.rand.nextInt(size);
			int index2 = this.rand.nextInt(size);
			if (index1 != index2)
				Collections.swap(this.itemPriorities, index1, index2);
		}

	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(ModCommonProxy.opMobs ? 15.0D : 11.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(ModCommonProxy.opMobs ? 0.75D : 0.7D);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(16, new ItemStack(Blocks.bedrock));
	}

	@Override
	protected Entity findPlayerToAttack()
	{
		return this.entityToAttack;
	}

	@Override
	public void onLivingUpdate()
	{
		if (this.isWet())
			this.attackEntityFrom(DamageSource.drown, 1.0F);

		this.isJumping = false;
		if (this.entityToAttack != null)
			this.faceEntity(this.entityToAttack, 100.0F, 100.0F);

		boolean hasIS = this.isCarrying();
		if (!this.worldObj.isRemote)
		{
			if (this.target == null)
				if (!hasIS && !this.isScared && this.rand.nextInt(550) == 0 && this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))
				{
					List<EntityPlayer> list = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(6.0D, 3.0D, 6.0D));
					if (!list.isEmpty())
					{
						this.target = list.get(this.rand.nextInt(list.size()));
						ItemStack headArmor = this.target.getCurrentArmor(3);
						if (headArmor != null && headArmor.getItem() == ItemList.enderman_head)
							this.target = null;
						else
							this.attentionLossTimer = (byte) (64 + this.rand.nextInt(62));
					}
				}
				else
				{
					List<EntityItem> list = this.worldObj.getEntitiesWithinAABB(EntityItem.class, this.boundingBox.expand(1.0D, 0.0D, 1.0D));
					if (!list.isEmpty() && ++this.itemDecisionTimer > this.rand.nextInt(70) + 15)
					{
						int carryingLevelIndex = this.itemPriorities.indexOf(this.carryingLevel);
						EntityItem item = list.get(this.rand.nextInt(list.size()));
						ItemStack is = item.getEntityItem();

						for (ItemPriorityLevel level : this.itemPriorities)
						{
							if (level.isValid(is))
							{
								if (this.itemPriorities.indexOf(level) < carryingLevelIndex)
								{
									if (hasIS)
									{
										EntityItem newItem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, this.getCarrying());
										float power = 0.3F;
										float yawRadians = (float) Math.toRadians((double) this.rotationYaw);
										float randomAngle = this.rand.nextFloat() * 3.1415927F * 2.0F;
										newItem.motionX = (double) (-MathHelper.sin(yawRadians) * MathHelper.cos(yawRadians) * power);
										newItem.motionZ = (double) (MathHelper.cos(yawRadians) * MathHelper.cos(yawRadians) * power);
										newItem.motionY = (double) (-MathHelper.sin((float) Math.toRadians((double) this.rotationPitch)) * power + 0.1F);
										power = 0.02F * this.rand.nextFloat();
										newItem.motionX += (double) (MathHelper.cos(randomAngle) * power);
										newItem.motionY += (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
										newItem.motionZ += (double) (MathHelper.sin(randomAngle) * power);
										this.worldObj.spawnEntityInWorld(newItem);
									}

									this.setCarriedItemStack(is);
									item.setDead();
								}
								break;
							}
						}

						this.itemDecisionTimer = 0;
					}
				}
			else if (this.target != null)
				if (--this.attentionLossTimer >= -124 && !this.target.isDead)
				{
					if (!hasIS && (double) this.getDistanceToEntity(this.target) < 1.8D)
					{
						    
						if (EventConfig.babyEndermanTheft)
							    
							for (int attempt = 0; attempt < 60; ++attempt)
							{
								int slot = this.rand.nextInt(this.target.inventory.mainInventory.length);
								if (slot != this.target.inventory.currentItem)
								{
									ItemStack is = this.target.inventory.mainInventory[slot];
									if (is != null)
									{
										ItemStack carrying = is.copy();
										carrying.stackSize = 1;
										this.setCarriedItemStack(carrying);
										if (--this.target.inventory.mainInventory[slot].stackSize == 0)
										{
											this.target.inventory.mainInventory[slot] = null;
											PacketPipeline.sendToPlayer(this.target, new C00ClearInventorySlot(slot));
										}
										break;
									}
								}
							}

						PathEntity escapePath = null;
						BlockPosM tmpPos = BlockPosM.tmp();

						for (int pathatt = 0; pathatt < 100; ++pathatt)
						{
							double ang = this.rand.nextDouble() * 2.0D * 3.141592653589793D;
							double len = 8.0D + this.rand.nextDouble() * 6.0D;
							tmpPos.set(this.posX + Math.cos(ang) * len, this.posY + (double) this.rand.nextInt(4) - 2.0D, this.posZ + Math.sin(ang) * len);
							Block low = tmpPos.getBlock(this.worldObj);
							if ((low.getMaterial() == Material.air || low == BlockList.crossed_decoration) && tmpPos.moveUp().getMaterial(this.worldObj) == Material.air)
							{
								escapePath = this.worldObj.getEntityPathToXYZ(this, tmpPos.x, tmpPos.y, tmpPos.z, 16.0F, false, true, false, false);
								break;
							}
						}

						if (escapePath != null)
							this.setPathToEntity(escapePath);

						this.target = null;
					}
				}
				else
					this.target = null;

			this.entityToAttack = this.target;
		}

		super.onLivingUpdate();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		boolean flag = super.attackEntityFrom(source, amount);
		if (flag)
			CausatumUtils.increase(source, CausatumMeters.END_MOB_DAMAGE, amount);

		if (flag && !this.isFamilyChosen && !this.worldObj.isRemote && source.getEntity() instanceof EntityPlayer)
		{
			List<EntityEnderman> endermanList = this.worldObj.getEntitiesWithinAABB(EntityEnderman.class, this.boundingBox.expand(32.0D, 32.0D, 32.0D));
			Collections.sort(endermanList, new DistanceComparator<EntityEnderman>(this));
			int familySize = Math.min(endermanList.size(), 2 + this.rand.nextInt(3) + this.rand.nextInt(2));

			for (int a = 0; a < familySize; ++a)
			{
				EntityEnderman orig = endermanList.get(a);
				EntityMobAngryEnderman angryEnderman = new EntityMobAngryEnderman(this.worldObj, orig.posX, orig.posY, orig.posZ);
				angryEnderman.copyLocationAndAnglesFrom(orig);
				angryEnderman.setTarget(source.getEntity());
				orig.setDead();
				this.worldObj.spawnEntityInWorld(angryEnderman);
			}

			this.isFamilyChosen = this.isScared = true;
		}

		this.entityToAttack = null;
		return flag;
	}

	@Override
	protected String getLivingSound()
	{
		return Baconizer.soundNormal("mob.endermen.idle");
	}

	@Override
	protected String getHurtSound()
	{
		return Baconizer.soundNormal("mob.endermen.hit");
	}

	@Override
	protected String getDeathSound()
	{
		return Baconizer.soundDeath("mob.endermen.death");
	}

	@Override
	protected float getSoundPitch()
	{
		return 1.25F;
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int looting)
	{
		if (this.isCarrying())
			this.entityDropItem(this.getCarrying(), 0.0F);

	}

	@Override
	protected boolean isValidLightLevel()
	{
		return this.worldObj.provider.dimensionId == 1 || super.isValidLightLevel();
	}

	@Override
	protected void despawnEntity()
	{
		if (!this.isCarrying())
			super.despawnEntity();

	}

	public void setCarriedItemStack(ItemStack is)
	{
		this.dataWatcher.updateObject(16, is);

		for (ItemPriorityLevel level : this.itemPriorities)
		{
			if (level.isValid(is))
			{
				this.carryingLevel = level;
				break;
			}
		}

	}

	@Override
	public void setEquipmentDropChance(int slot, float chance)
	{
		super.setEquipmentDropChance(slot, chance);
		if (MathUtil.floatEquals(chance, 0.0F) && !this.isDead)
			this.setDead();

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		NBTTagList tagPriorities = new NBTTagList();

		for (ItemPriorityLevel level : this.itemPriorities)
		{
			tagPriorities.appendTag(new NBTTagString(level.name()));
		}

		nbt.setTag("priorities", tagPriorities);
		ItemStack is = this.getCarrying();
		if (is != null)
			nbt.setTag("carrying", is.writeToNBT(new NBTTagCompound()));

		nbt.setBoolean("isFamilyChosen", this.isFamilyChosen);
		nbt.setBoolean("isScared", this.isScared);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		NBTTagList tagPriorities = nbt.getTagList("priorities", 8);
		if (tagPriorities.tagCount() > 0)
		{
			this.itemPriorities.clear();

			for (int a = 0; a < tagPriorities.tagCount(); ++a)
			{
				this.itemPriorities.add(ItemPriorityLevel.valueOf(tagPriorities.getStringTagAt(a)));
			}
		}

		if (nbt.hasKey("carrying"))
			this.setCarriedItemStack(ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("carrying")));

		this.isFamilyChosen = nbt.getBoolean("isFamilyChosen");
		this.isScared = nbt.getBoolean("isScared");
	}

	@Override
	public boolean isScreaming()
	{
		return false;
	}

	@Override
	public boolean isCarrying()
	{
		ItemStack is = this.getCarrying();
		return is != null && is.getItem() != Item.getItemFromBlock(Blocks.bedrock);
	}

	@Override
	public ItemStack getCarrying()
	{
		return this.dataWatcher.getWatchableObjectItemStack(16);
	}

	@Override
	public String getCommandSenderName()
	{
		return this.hasCustomNameTag() ? this.getCustomNameTag() : StatCollector.translateToLocal(Baconizer.mobName("entity.babyEnderman.name"));
	}
}
