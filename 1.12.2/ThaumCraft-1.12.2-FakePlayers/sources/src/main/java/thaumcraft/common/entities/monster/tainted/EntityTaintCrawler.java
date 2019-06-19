package thaumcraft.common.entities.monster.tainted;

import ru.will.git.thaumcraft.EventConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.api.potions.PotionFluxTaint;
import thaumcraft.common.blocks.world.taint.BlockTaintFibre;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.BlockUtils;

public class EntityTaintCrawler extends EntityMob implements ITaintedMob
{
	BlockPos lastPos = new BlockPos(0, 0, 0);

	public EntityTaintCrawler(World par1World)
	{
		super(par1World);
		this.setSize(0.5F, 0.4F);
		this.experienceValue = 3;
	}

	@Override
	protected void initEntityAI()
	{
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
		this.tasks.addTask(3, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
	}

	@Override
	public boolean canAttackClass(Class clazz)
	{
		return !ITaintedMob.class.isAssignableFrom(clazz);
	}

	@Override
	public boolean isOnSameTeam(Entity otherEntity)
	{
		return otherEntity instanceof ITaintedMob || super.isOnSameTeam(otherEntity);
	}

	@Override
	public float getEyeHeight()
	{
		return 0.1F;
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.275D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
	}

	@Override
	protected float getSoundPitch()
	{
		return 0.7F;
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.ENTITY_SILVERFISH_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundEvents.ENTITY_SILVERFISH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_SILVERFISH_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos p_180429_1_, Block p_180429_2_)
	{
		this.playSound(SoundEvents.ENTITY_SILVERFISH_STEP, 0.15F, 1.0F);
	}

	@Override
	protected boolean canTriggerWalking()
	{
		return false;
	}

	@Override
	public void onUpdate()
	{
		if (!this.world.isRemote && this.isEntityAlive() && this.ticksExisted % 40 == 0 && this.lastPos != this.getPosition())
		{
			this.lastPos = this.getPosition();
			IBlockState bs = this.world.getBlockState(this.getPosition());
			Material bm = bs.getMaterial();
			if (!bs.getBlock().isLeaves(bs, this.world, this.getPosition()) && !bm.isLiquid() && bm != ThaumcraftMaterials.MATERIAL_TAINT && (this.world.isAirBlock(this.getPosition()) || bs.getBlock().isReplaceable(this.world, this.getPosition()) || bs.getBlock() instanceof BlockFlower || bs.getBlock() instanceof IPlantable) && BlockUtils.isAdjacentToSolidBlock(this.world, this.getPosition()) && !BlockTaintFibre.isOnlyAdjacentToTaint(this.world, this.getPosition()))
				this.world.setBlockState(this.getPosition(), BlocksTC.taintFibre.getDefaultState());
		}

		super.onUpdate();
	}

	@Override
	protected boolean isValidLightLevel()
	{
		return true;
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute()
	{
		return EnumCreatureAttribute.ARTHROPOD;
	}

	@Override
	protected Item getDropItem()
	{
		return Item.getItemById(0);
	}

	@Override
	protected void dropFewItems(boolean flag, int i)
	{
		if (this.world.rand.nextInt(8) == 0)
			this.entityDropItem(ConfigItems.FLUX_CRYSTAL.copy(), this.height / 2.0F);
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance p_180482_1_, IEntityLivingData p_180482_2_)
	{
		return p_180482_2_;
	}

	@Override
	public boolean attackEntityAsMob(Entity victim)
	{
		if (super.attackEntityAsMob(victim))
		{
			
			if (EventConfig.potionFluxTaint && victim instanceof EntityLivingBase)
			{
				byte b0 = 0;
				if (this.world.getDifficulty() == EnumDifficulty.NORMAL)
					b0 = 3;
				else if (this.world.getDifficulty() == EnumDifficulty.HARD)
					b0 = 6;

				if (b0 > 0 && this.rand.nextInt(b0 + 1) > 2)
					((EntityLivingBase) victim).addPotionEffect(new PotionEffect(PotionFluxTaint.instance, b0 * 20, 0));
			}

			return true;
		}
		return false;
	}
}
