package thaumcraft.common.entities.monster;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;
import thaumcraft.common.entities.ai.pech.AIPechItemEntityGoto;
import thaumcraft.common.entities.ai.pech.AIPechTradePlayer;
import thaumcraft.common.entities.projectile.EntityPechBlast;
import thaumcraft.common.items.ItemManaBean;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.utils.InventoryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EntityPech extends EntityMob implements IRangedAttackMob
{
	public ItemStack[] loot = new ItemStack[9];
	public boolean trading = false;
	public boolean updateAINextTick = false;
	private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 0.6D, 20, 50, 15.0F);
	private EntityAIArrowAttack aiBlastAttack = new EntityAIArrowAttack(this, 0.6D, 20, 30, 15.0F);
	private AIAttackOnCollide aiMeleeAttack = new AIAttackOnCollide(this, EntityLivingBase.class, 0.6D, false);
	private EntityAIAvoidEntity aiAvoidPlayer = new EntityAIAvoidEntity(this, EntityPlayer.class, 8.0F, 0.5D, 0.6D);
	public float mumble = 0.0F;
	int chargecount = 0;
	static HashMap<Integer, Integer> valuedItems = new HashMap<>();
	public static HashMap<Integer, ArrayList<List>> tradeInventory = new HashMap<>();

	@Override
	public String getCommandSenderName()
	{
		if (this.hasCustomNameTag())
			return this.getCustomNameTag();
		switch (this.getPechType())
		{
			case 0:
				return StatCollector.translateToLocal("entity.Thaumcraft.Pech.name");
			case 1:
				return StatCollector.translateToLocal("entity.Thaumcraft.Pech.1.name");
			case 2:
				return StatCollector.translateToLocal("entity.Thaumcraft.Pech.2.name");
			default:
				return StatCollector.translateToLocal("entity.Thaumcraft.Pech.name");
		}
	}

	public EntityPech(World world)
	{
		super(world);
		this.setSize(0.6F, 1.8F);
		this.getNavigator().setBreakDoors(false);
		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new AIPechTradePlayer(this));
		this.tasks.addTask(3, new AIPechItemEntityGoto(this));
		this.tasks.addTask(5, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.5D));
		this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
		this.tasks.addTask(9, new EntityAIWander(this, 0.6D));
		this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
		this.tasks.addTask(11, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		if (world != null && !world.isRemote)
			this.setCombatTask();

		this.equipmentDropChances[0] = 0.2F;
	}

	@Override
	public void setCurrentItemOrArmor(int par1, ItemStack par2ItemStack)
	{
		super.setCurrentItemOrArmor(par1, par2ItemStack);
		if (!this.worldObj.isRemote && par1 == 0)
			this.updateAINextTick = true;

	}

	@Override
	protected void addRandomArmor()
	{
		super.addRandomArmor();
		switch (this.rand.nextInt(20))
		{
			case 0:
			case 12:
				ItemStack wand = new ItemStack(ConfigItems.itemWandCasting);
				ItemStack focus = new ItemStack(ConfigItems.itemFocusPech);
				((ItemWandCasting) wand.getItem()).setFocus(wand, focus);
				((ItemWandCasting) wand.getItem()).addVis(wand, Aspect.EARTH, 2 + this.rand.nextInt(6), true);
				((ItemWandCasting) wand.getItem()).addVis(wand, Aspect.ENTROPY, 2 + this.rand.nextInt(6), true);
				((ItemWandCasting) wand.getItem()).addVis(wand, Aspect.WATER, 2 + this.rand.nextInt(6), true);
				((ItemWandCasting) wand.getItem()).addVis(wand, Aspect.AIR, this.rand.nextInt(4), true);
				((ItemWandCasting) wand.getItem()).addVis(wand, Aspect.FIRE, this.rand.nextInt(4), true);
				((ItemWandCasting) wand.getItem()).addVis(wand, Aspect.ORDER, this.rand.nextInt(4), true);
				this.setCurrentItemOrArmor(0, wand);
				break;
			case 1:
				this.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
				break;
			case 2:
			case 4:
			case 10:
			case 11:
			case 13:
				this.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
				break;
			case 3:
				this.setCurrentItemOrArmor(0, new ItemStack(Items.stone_axe));
				break;
			case 5:
				this.setCurrentItemOrArmor(0, new ItemStack(Items.iron_sword));
				break;
			case 6:
				this.setCurrentItemOrArmor(0, new ItemStack(Items.iron_axe));
				break;
			case 7:
				this.setCurrentItemOrArmor(0, new ItemStack(Items.fishing_rod));
				break;
			case 8:
				this.setCurrentItemOrArmor(0, new ItemStack(Items.stone_pickaxe));
				break;
			case 9:
				this.setCurrentItemOrArmor(0, new ItemStack(Items.iron_pickaxe));
		}

	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1EntityLivingData)
	{
		par1EntityLivingData = super.onSpawnWithEgg(par1EntityLivingData);
		this.addRandomArmor();
		ItemStack itemstack = this.getHeldItem();
		if (itemstack != null && itemstack.getItem() == ConfigItems.itemWandCasting)
		{
			this.setPechType(1);
			this.equipmentDropChances[0] = 0.1F;
		}
		else if (itemstack != null)
		{
			if (itemstack.getItem() == Items.bow)
				this.setPechType(2);

			this.enchantEquipment();
		}

		this.setCanPickUpLoot(this.rand.nextFloat() < 0.75F * this.worldObj.func_147462_b(this.posX, this.posY, this.posZ));
		return super.onSpawnWithEgg(par1EntityLivingData);
	}

	@Override
	public boolean getCanSpawnHere()
	{
		BiomeGenBase biome = this.worldObj.getBiomeGenForCoords(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ));
		boolean magicBiome = false;
		if (biome != null)
			magicBiome = BiomeDictionary.isBiomeOfType(biome, Type.MAGICAL) && biome.biomeID != Config.biomeTaintID;

		int count = 0;

		try
		{
			List l = this.worldObj.getEntitiesWithinAABB(EntityPech.class, this.boundingBox.expand(16.0D, 16.0D, 16.0D));
			if (l != null)
				count = l.size();
		}
		catch (Exception ignored)
		{
		}

		if (this.worldObj.provider.dimensionId != 0 && biome.biomeID != Config.biomeMagicalForestID && biome.biomeID != Config.biomeEerieID)
			magicBiome = false;

		return count < 4 && magicBiome && super.getCanSpawnHere();
	}

	@Override
	public float getEyeHeight()
	{
		return this.height * 0.66F;
	}

	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(13, (byte) 0);
		this.dataWatcher.addObject(14, (short) 0);
		this.dataWatcher.addObject(16, (byte) 0);
	}

	public int getPechType()
	{
		return this.dataWatcher.getWatchableObjectByte(13);
	}

	public int getAnger()
	{
		return this.dataWatcher.getWatchableObjectShort(14);
	}

	public boolean isTamed()
	{
		return this.dataWatcher.getWatchableObjectByte(16) == 1;
	}

	public void setPechType(int par1)
	{
		this.dataWatcher.updateObject(13, (byte) par1);
	}

	public void setAnger(int par1)
	{
		this.dataWatcher.updateObject(14, (short) par1);
	}

	public void setTamed(boolean par1)
	{
		this.dataWatcher.updateObject(16, (byte) (par1 ? 1 : 0));
	}

	@Override
	public boolean isAIEnabled()
	{
		return true;
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setByte("PechType", (byte) this.getPechType());
		par1NBTTagCompound.setShort("Anger", (short) this.getAnger());
		par1NBTTagCompound.setBoolean("Tamed", this.isTamed());
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.loot.length; ++i)
		{
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
			if (this.loot[i] != null)
				this.loot[i].writeToNBT(nbttagcompound1);

			nbttaglist.appendTag(nbttagcompound1);
		}

		par1NBTTagCompound.setTag("Loot", nbttaglist);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readEntityFromNBT(par1NBTTagCompound);
		if (par1NBTTagCompound.hasKey("PechType"))
		{
			byte b0 = par1NBTTagCompound.getByte("PechType");
			this.setPechType(b0);
		}

		this.setAnger(par1NBTTagCompound.getShort("Anger"));
		this.setTamed(par1NBTTagCompound.getBoolean("Tamed"));
		if (par1NBTTagCompound.hasKey("Loot"))
		{
			NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Loot", 10);

			for (int i = 0; i < this.loot.length; ++i)
			{
				this.loot[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
			}
		}

		this.updateAINextTick = true;
	}

	@Override
	protected boolean canDespawn()
	{
		try
		{
			if (this.loot == null)
				return true;
			int q = 0;

			for (ItemStack is : this.loot)
			{
				if (is != null && is.stackSize > 0)
					++q;
			}

			return q < 5;
		}
		catch (Exception var6)
		{
			return true;
		}
	}

	@Override
	public boolean allowLeashing()
	{
		return false;
	}

	@Override
	protected void dropFewItems(boolean flag, int i)
	{
		for (int a = 0; a < this.loot.length; ++a)
		{
			if (this.loot[a] != null && this.worldObj.rand.nextFloat() < 0.88F)
				this.entityDropItem(this.loot[a].copy(), 1.5F);
		}

		Aspect[] aspects = Aspect.getPrimalAspects().toArray(new Aspect[0]);

		for (int a = 0; a < 1 + i; ++a)
		{
			if (this.rand.nextBoolean())
			{
				ItemStack is = new ItemStack(ConfigItems.itemManaBean);
				((ItemManaBean) is.getItem()).setAspects(is, new AspectList().add(aspects[this.rand.nextInt(aspects.length)], 1));
				this.entityDropItem(is, 1.5F);
			}
		}

		if (this.worldObj.rand.nextInt(10) < 1 + i)
			this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 18), 1.5F);

		super.dropFewItems(flag, i);
	}

	@Override
	protected void dropRareDrop(int par1)
	{
		this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 9), 1.5F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte par1)
	{
		if (par1 == 16)
			this.mumble = 3.1415927F;
		else if (par1 == 17)
			this.mumble = 6.2831855F;
		else if (par1 == 18)
			for (int i = 0; i < 5; ++i)
			{
				double d0 = this.rand.nextGaussian() * 0.02D;
				double d1 = this.rand.nextGaussian() * 0.02D;
				double d2 = this.rand.nextGaussian() * 0.02D;
				this.worldObj.spawnParticle("happyVillager", this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + 0.5D + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d0, d1, d2);
			}

		if (par1 == 19)
		{
			for (int i = 0; i < 5; ++i)
			{
				double d0 = this.rand.nextGaussian() * 0.02D;
				double d1 = this.rand.nextGaussian() * 0.02D;
				double d2 = this.rand.nextGaussian() * 0.02D;
				this.worldObj.spawnParticle("angryVillager", this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + 0.5D + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d0, d1, d2);
			}

			this.mumble = 6.2831855F;
		}
		else
			super.handleHealthUpdate(par1);

	}

	@Override
	public void playLivingSound()
	{
		if (!this.worldObj.isRemote)
		{
			if (this.rand.nextInt(3) == 0)
			{
				List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(4.0D, 2.0D, 4.0D));

				for (int i = 0; i < list.size(); ++i)
				{
					Entity entity1 = (Entity) list.get(i);
					if (entity1 instanceof EntityPech)
					{
						this.worldObj.setEntityState(this, (byte) 17);
						this.playSound("thaumcraft:pech_trade", this.getSoundVolume(), this.getSoundPitch());
						return;
					}
				}
			}

			this.worldObj.setEntityState(this, (byte) 16);
		}

		super.playLivingSound();
	}

	@Override
	public int getTalkInterval()
	{
		return 120;
	}

	@Override
	protected float getSoundVolume()
	{
		return 0.4F;
	}

	@Override
	protected String getLivingSound()
	{
		return "thaumcraft:pech_idle";
	}

	@Override
	protected String getHurtSound()
	{
		return "thaumcraft:pech_hit";
	}

	@Override
	protected String getDeathSound()
	{
		return "thaumcraft:pech_death";
	}

	@Override
	protected Entity findPlayerToAttack()
	{
		return this.getAnger() == 0 ? null : super.findPlayerToAttack();
	}

	public void setCombatTask()
	{
		this.tasks.removeTask(this.aiMeleeAttack);
		this.tasks.removeTask(this.aiArrowAttack);
		this.tasks.removeTask(this.aiBlastAttack);
		ItemStack itemstack = this.getHeldItem();
		if (itemstack != null && itemstack.getItem() == Items.bow)
			this.tasks.addTask(2, this.aiArrowAttack);
		else if (itemstack != null && itemstack.getItem() == ConfigItems.itemWandCasting)
			this.tasks.addTask(2, this.aiBlastAttack);
		else
			this.tasks.addTask(2, this.aiMeleeAttack);

		if (this.isTamed())
			this.tasks.removeTask(this.aiAvoidPlayer);
		else
			this.tasks.addTask(4, this.aiAvoidPlayer);

	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase entitylivingbase, float f)
	{
		if (this.getPechType() == 2)
		{
			EntityArrow entityarrow = new EntityArrow(this.worldObj, this, entitylivingbase, 1.6F, (float) (14 - this.worldObj.difficultySetting.getDifficultyId() * 4));
			int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, this.getHeldItem());
			int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, this.getHeldItem());
			entityarrow.setDamage((double) (f * 2.0F) + this.rand.nextGaussian() * 0.25D + (double) ((float) this.worldObj.difficultySetting.getDifficultyId() * 0.11F));
			if (i > 0)
				entityarrow.setDamage(entityarrow.getDamage() + (double) i * 0.5D + 0.5D);

			if (j > 0)
				entityarrow.setKnockbackStrength(j);

			this.playSound("random.bow", 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
			this.worldObj.spawnEntityInWorld(entityarrow);
		}
		else if (this.getPechType() == 1)
		{
			EntityPechBlast blast = new EntityPechBlast(this.worldObj, this, 1, 0, this.rand.nextFloat() < 0.1F);
			double d0 = entitylivingbase.posX + entitylivingbase.motionX - this.posX;
			double d1 = entitylivingbase.posY + (double) entitylivingbase.getEyeHeight() - 1.500000023841858D - this.posY;
			double d2 = entitylivingbase.posZ + entitylivingbase.motionZ - this.posZ;
			float f1 = MathHelper.sqrt_double(d0 * d0 + d2 * d2);
			blast.setThrowableHeading(d0, d1 + (double) (f1 * 0.1F), d2, 1.5F, 4.0F);
			this.playSound("thaumcraft:ice", 0.4F, 1.0F + this.rand.nextFloat() * 0.1F);
			this.worldObj.spawnEntityInWorld(blast);
		}

		this.swingItem();
	}

	private void becomeAngryAt(Entity par1Entity)
	{
		this.entityToAttack = par1Entity;
		if (this.getAnger() <= 0)
		{
			this.worldObj.setEntityState(this, (byte) 19);
			this.playSound("thaumcraft:pech_charge", this.getSoundVolume(), this.getSoundPitch());
		}

		this.setAttackTarget((EntityLivingBase) par1Entity);
		this.setAnger(400 + this.rand.nextInt(400));
		this.setTamed(false);
		this.updateAINextTick = true;
	}

	@Override
	public int getTotalArmorValue()
	{
		int i = super.getTotalArmorValue() + 2;
		if (i > 20)
			i = 20;

		return i;
	}

	@Override
	public boolean attackEntityFrom(DamageSource damageSource, float damage)
	{
		if (this.isEntityInvulnerable())
			return false;
		Entity entity = damageSource.getEntity();
		if (entity instanceof EntityPlayer)
		{
			List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(32.0D, 16.0D, 32.0D));

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = (Entity) list.get(i);
				if (entity1 instanceof EntityPech)
				{
					EntityPech entitypech = (EntityPech) entity1;
					entitypech.becomeAngryAt(entity);
				}
			}

			this.becomeAngryAt(entity);
		}

		return super.attackEntityFrom(damageSource, damage);
	}

	@Override
	public void onUpdate()
	{
		if (this.mumble > 0.0F)
			this.mumble *= 0.75F;

		if (this.getAnger() > 0)
			this.setAnger(this.getAnger() - 1);

		if (this.getAnger() > 0 && (this.entityToAttack == null || this.getAttackTarget() == null))
		{
    
    
    

			if (this.entityToAttack != null)
			{
				if (this.chargecount > 0)
					--this.chargecount;

				if (this.chargecount == 0)
				{
					this.chargecount = 100;
					this.playSound("thaumcraft:pech_charge", this.getSoundVolume(), this.getSoundPitch());
				}

				this.worldObj.setEntityState(this, (byte) 17);
			}
		}

		if (this.worldObj.isRemote && this.rand.nextInt(15) == 0 && this.getAnger() > 0)
		{
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.worldObj.spawnParticle("angryVillager", this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + 0.5D + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d0, d1, d2);
		}

		if (this.worldObj.isRemote && this.rand.nextInt(25) == 0 && this.isTamed())
		{
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.worldObj.spawnParticle("happyVillager", this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + 0.5D + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d0, d1, d2);
		}

		super.onUpdate();
	}

	@Override
	public void updateAITasks()
	{
		if (this.updateAINextTick)
		{
			this.updateAINextTick = false;
			this.setCombatTask();
		}

		super.updateAITasks();
		if (this.ticksExisted % 40 == 0)
			this.heal(1.0F);

	}

	public boolean canPickup(ItemStack stack)
    
		if (stack == null || stack.stackSize <= 0)
			return false;
		if (!this.isTamed() && valuedItems.containsKey(Item.getIdFromItem(stack.getItem())))
			return true;
		for (int i = 0; i < this.loot.length; ++i)
		{
			if (this.loot[i] != null && this.loot[i].stackSize <= 0)
				this.loot[i] = null;

			if (this.loot[i] == null)
				return true;

			if (InventoryUtils.areItemStacksEqualStrict(stack, this.loot[i]) && stack.stackSize + this.loot[i].stackSize <= this.loot[i].getMaxStackSize())
				return true;
		}

		return false;
	}

	public ItemStack pickupItem(ItemStack stack)
    
		if (stack == null || stack.stackSize <= 0)
			return null;
		if (!this.isTamed() && this.isValued(stack))
		{
			if (this.rand.nextInt(10) < this.getValue(stack))
			{
				this.setTamed(true);
				this.updateAINextTick = true;
				this.worldObj.setEntityState(this, (byte) 18);
			}

			--stack.stackSize;
			return stack.stackSize <= 0 ? null : stack;
		}
		for (int i = 0; i < this.loot.length; ++i)
		{
			if (this.loot[i] != null && this.loot[i].stackSize <= 0)
				this.loot[i] = null;

			if (stack != null && stack.stackSize > 0 && this.loot[i] != null && this.loot[i].stackSize < this.loot[i].getMaxStackSize() && InventoryUtils.areItemStacksEqualStrict(stack, this.loot[i]))
			{
				if (stack.stackSize + this.loot[i].stackSize <= this.loot[i].getMaxStackSize())
				{
					this.loot[i].stackSize += stack.stackSize;
					return null;
				}

				int sz = Math.min(stack.stackSize, this.loot[i].getMaxStackSize() - this.loot[i].stackSize);
				this.loot[i].stackSize += sz;
				stack.stackSize -= sz;
			}

			if (stack != null && stack.stackSize <= 0)
				stack = null;
		}

		for (int a = 0; a < this.loot.length; ++a)
		{
			if (this.loot[a] != null && this.loot[a].stackSize <= 0)
				this.loot[a] = null;

			if (stack != null && stack.stackSize > 0 && this.loot[a] == null)
			{
				this.loot[a] = stack.copy();
				return null;
			}
		}

		if (stack != null && stack.stackSize <= 0)
			stack = null;

		return stack;
	}

	@Override
	public boolean interact(EntityPlayer player)
	{
		if (!player.isSneaking() && (player.getHeldItem() == null || !(player.getHeldItem().getItem() instanceof ItemNameTag)))
		{
			if (!this.worldObj.isRemote && this.isTamed())
			{
				player.openGui(Thaumcraft.instance, 1, this.worldObj, this.getEntityId(), 0, 0);
				return true;
			}
			return super.interact(player);
		}
		return false;
	}

	public boolean isValued(ItemStack item)
	{
		if (item == null)
			return false;
		boolean value = valuedItems.containsKey(Item.getIdFromItem(item.getItem()));
		if (!value)
		{
			AspectList al = ThaumcraftCraftingManager.getObjectTags(item);
			al = ThaumcraftCraftingManager.getBonusTags(item, al);
			if (al.getAmount(Aspect.GREED) > 0)
				value = true;
		}

		return value;
	}

	public int getValue(ItemStack item)
	{
		if (item == null)
			return 0;
		int value = valuedItems.containsKey(Item.getIdFromItem(item.getItem())) ? valuedItems.get(Item.getIdFromItem(item.getItem())) : 0;
		if (value == 0)
		{
			AspectList al = ThaumcraftCraftingManager.getObjectTags(item);
			al = ThaumcraftCraftingManager.getBonusTags(item, al);
			value = Math.min(32, al.getAmount(Aspect.GREED));
		}

		return value;
	}

	static
	{
		valuedItems.put(Item.getIdFromItem(ConfigItems.itemManaBean), 1);
		valuedItems.put(Item.getIdFromItem(Items.gold_ingot), 2);
		valuedItems.put(Item.getIdFromItem(Items.golden_apple), 2);
		valuedItems.put(Item.getIdFromItem(Items.ender_pearl), 3);
		valuedItems.put(Item.getIdFromItem(Items.diamond), 4);
		valuedItems.put(Item.getIdFromItem(Items.emerald), 5);
		ArrayList<List> forInv = new ArrayList<>();
		forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemManaBean)));
		forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 16)));
		forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 31)));
		forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 21)));
		if (Config.foundCopperIngot)
			forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 17)));

		if (Config.foundTinIngot)
			forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 18)));

		if (Config.foundSilverIngot)
			forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 19)));

		if (Config.foundLeadIngot)
			forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 20)));

		forInv.add(Arrays.asList(2, new ItemStack(Items.blaze_rod)));
		forInv.add(Arrays.asList(2, new ItemStack(ConfigBlocks.blockCustomPlant, 1, 0)));
		forInv.add(Arrays.asList(2, new ItemStack(Items.potionitem, 1, 8201)));
		forInv.add(Arrays.asList(2, new ItemStack(Items.potionitem, 1, 8194)));
		forInv.add(Arrays.asList(3, new ItemStack(Items.experience_bottle)));
		forInv.add(Arrays.asList(3, new ItemStack(ConfigItems.itemResource, 1, 9)));
		forInv.add(Arrays.asList(3, new ItemStack(Items.golden_apple, 1, 0)));
		forInv.add(Arrays.asList(3, new ItemStack(Items.potionitem, 1, 8265)));
		forInv.add(Arrays.asList(3, new ItemStack(Items.potionitem, 1, 8262)));
		forInv.add(Arrays.asList(5, new ItemStack(Items.golden_apple, 1, 1)));
		forInv.add(Arrays.asList(4, new ItemStack(ConfigItems.itemPickThaumium)));
		forInv.add(Arrays.asList(5, new ItemStack(ConfigBlocks.blockCustomPlant, 1, 1)));
		forInv.add(Arrays.asList(5, new ItemStack(ConfigBlocks.blockCustomPlant, 1, 1)));
		tradeInventory.put(0, forInv);
		ArrayList<List> forMag = new ArrayList<>();
		forMag.add(Arrays.asList(1, new ItemStack(ConfigItems.itemManaBean)));

		for (int a = 0; a < 6; ++a)
		{
			forMag.add(Arrays.asList(1, new ItemStack(ConfigItems.itemShard, 1, a)));
		}

		forMag.add(Arrays.asList(1, new ItemStack(ConfigItems.itemResource, 1, 9)));
		forMag.add(Arrays.asList(2, new ItemStack(ConfigItems.itemResource, 1, 9)));
		forMag.add(Arrays.asList(2, new ItemStack(Items.potionitem, 1, 8193)));
		forMag.add(Arrays.asList(2, new ItemStack(Items.potionitem, 1, 8261)));
		forMag.add(Arrays.asList(3, Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(Config.enchHaste, 1))));
		forMag.add(Arrays.asList(3, new ItemStack(Items.golden_apple, 1, 0)));
		forMag.add(Arrays.asList(3, new ItemStack(Items.potionitem, 1, 8225)));
		forMag.add(Arrays.asList(3, new ItemStack(Items.potionitem, 1, 8229)));

		for (int a = 0; a < 7; ++a)
		{
			forMag.add(Arrays.asList(4, new ItemStack(ConfigBlocks.blockCrystal, 1, a)));
		}

		forMag.add(Arrays.asList(5, new ItemStack(Items.golden_apple, 1, 1)));
		forMag.add(Arrays.asList(5, Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(Config.enchRepair, 1))));
		forMag.add(Arrays.asList(5, new ItemStack(ConfigItems.itemFocusPouch)));
		forMag.add(Arrays.asList(5, new ItemStack(ConfigItems.itemFocusPech)));
		forMag.add(Arrays.asList(5, new ItemStack(ConfigItems.itemAmuletVis, 1, 0)));
		tradeInventory.put(1, forMag);
		ArrayList<List> forArc = new ArrayList<>();
		forArc.add(Arrays.asList(1, new ItemStack(ConfigItems.itemManaBean)));

		for (int a = 0; a < 15; ++a)
		{
			forArc.add(Arrays.asList(1, new ItemStack(ConfigBlocks.blockCandle, 1, a)));
		}

		forArc.add(Arrays.asList(2, new ItemStack(Items.ghast_tear)));
		forArc.add(Arrays.asList(2, new ItemStack(Items.potionitem, 1, 8194)));
		forArc.add(Arrays.asList(2, new ItemStack(Items.potionitem, 1, 8201)));
		forArc.add(Arrays.asList(2, Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(Enchantment.power, 1))));
		forArc.add(Arrays.asList(3, new ItemStack(Items.experience_bottle)));
		forArc.add(Arrays.asList(3, new ItemStack(ConfigItems.itemResource, 1, 9)));
		forArc.add(Arrays.asList(3, new ItemStack(Items.potionitem, 1, 8270)));
		forArc.add(Arrays.asList(3, new ItemStack(Items.potionitem, 1, 8225)));
		forArc.add(Arrays.asList(3, new ItemStack(Items.golden_apple, 1, 0)));
		forArc.add(Arrays.asList(5, new ItemStack(Items.golden_apple, 1, 1)));
		forArc.add(Arrays.asList(4, new ItemStack(ConfigItems.itemBootsThaumium)));
		forArc.add(Arrays.asList(5, new ItemStack(ConfigItems.itemRingRunic, 1, 0)));
		forArc.add(Arrays.asList(5, Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(Enchantment.flame, 1))));
		forArc.add(Arrays.asList(5, Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(Enchantment.infinity, 1))));
		tradeInventory.put(2, forArc);
	}
}
