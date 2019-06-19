package net.machinemuse.powersuits.entity;

import java.util.ArrayList;
import java.util.Random;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.machinemuse.ModUtils;

import net.machinemuse.api.ModuleManager;
import net.machinemuse.powersuits.powermodule.weapon.BladeLauncherModule;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public class EntitySpinningBlade extends EntityThrowable
{
	public static final int SIZE = 24;
	public double damage;
	public Entity shootingEntity;
    
	public final FakePlayerContainer fake = new FakePlayerContainerEntity(ModUtils.profile, this);

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		this.fake.writeToNBT(nbt);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.fake.readFromNBT(nbt);
    

	public EntitySpinningBlade(World world)
	{
		super(world);
	}

	public EntitySpinningBlade(World par1World, EntityLivingBase shootingEntity)
	{
		super(par1World, shootingEntity);
		this.shootingEntity = shootingEntity;
		if (shootingEntity instanceof EntityPlayer)
		{
			this.shootingItem = ((EntityPlayer) shootingEntity).getCurrentEquippedItem();
			if (this.shootingItem != null)
				this.damage = ModuleManager.computeModularProperty(this.shootingItem, BladeLauncherModule.BLADE_DAMAGE);
		}
		Vec3 direction = shootingEntity.getLookVec().normalize();
		double speed = 1.0;
		double scale = 1;
		this.motionX = direction.xCoord * speed;
		this.motionY = direction.yCoord * speed;
		this.motionZ = direction.zCoord * speed;
		double r = 1;
		double xoffset = 1.3f + r - direction.yCoord * shootingEntity.getEyeHeight();
		double yoffset = -.2;
		double zoffset = 0.3f;
		double horzScale = Math.sqrt(direction.xCoord * direction.xCoord + direction.zCoord * direction.zCoord);
		double horzx = direction.xCoord / horzScale;
		double horzz = direction.zCoord / horzScale;
		this.posX = shootingEntity.posX + direction.xCoord * xoffset - direction.yCoord * horzx * yoffset - horzz * zoffset;
		this.posY = shootingEntity.posY + shootingEntity.getEyeHeight() + direction.yCoord * xoffset + (1 - Math.abs(direction.yCoord)) * yoffset;
		this.posZ = shootingEntity.posZ + direction.zCoord * xoffset - direction.yCoord * horzz * yoffset + horzx * zoffset;
    
		if (shootingEntity instanceof EntityPlayer)
    
	}

    
	@Override
	protected float getGravityVelocity()
	{
		return 0;
	}

	public int getMaxLifetime()
	{
		return 200;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (this.ticksExisted > this.getMaxLifetime())
			this.setDead();
	}

	@Override
	protected void onImpact(MovingObjectPosition hitMOP)
	{
		if (hitMOP.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
		{
			World world = this.worldObj;
			if (world == null)
    
			if (this.fake.cantBreak(hitMOP.blockX, hitMOP.blockY, hitMOP.blockZ))
			{
				this.kill();
				return;
    

			Block block = world.getBlock(hitMOP.blockX, hitMOP.blockY, hitMOP.blockZ);
			if (block instanceof IShearable)
			{
				IShearable target = (IShearable) block;
				if (target.isShearable(this.shootingItem, world, hitMOP.blockX, hitMOP.blockY, hitMOP.blockZ) && !world.isRemote)
				{
					ArrayList<ItemStack> drops = target.onSheared(this.shootingItem, world, hitMOP.blockX, hitMOP.blockY, hitMOP.blockZ, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, this.shootingItem));
					Random rand = new Random();

					for (ItemStack stack : drops)
					{
						float f = 0.7F;
						double d = rand.nextFloat() * f + (1.0F - f) * 0.5D;
						double d1 = rand.nextFloat() * f + (1.0F - f) * 0.5D;
						double d2 = rand.nextFloat() * f + (1.0F - f) * 0.5D;
						EntityItem entityitem = new EntityItem(world, hitMOP.blockX + d, hitMOP.blockY + d1, hitMOP.blockZ + d2, stack);
						entityitem.delayBeforeCanPickup = 10;
						world.spawnEntityInWorld(entityitem);
					}
					if (this.shootingEntity instanceof EntityPlayer)
						((EntityPlayer) this.shootingEntity).addStat(StatList.mineBlockStatArray[Block.getIdFromBlock(block)], 1);
				}
    
			}
			else
				this.kill();
		}
		else if (hitMOP.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && hitMOP.entityHit != this.shootingEntity)
    
			if (this.fake.cantDamage(hitMOP.entityHit))
			{
				this.kill();
				return;
    

			if (hitMOP.entityHit instanceof IShearable)
			{
				IShearable target = (IShearable) hitMOP.entityHit;
				Entity entity = hitMOP.entityHit;
				if (target.isShearable(this.shootingItem, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ))
				{
					ArrayList<ItemStack> drops = target.onSheared(this.shootingItem, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, this.shootingItem));

					Random rand = new Random();
					for (ItemStack drop : drops)
					{
						EntityItem ent = entity.entityDropItem(drop, 1.0F);
						ent.motionY += rand.nextFloat() * 0.05F;
						ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
						ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
					}
				}
			}
			else
				hitMOP.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.shootingEntity), (int) this.damage);
		}
	}
}
