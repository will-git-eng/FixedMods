/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Jan 25, 2015, 6:47:35 PM (GMT)]
 */
package vazkii.botania.common.entity;

import ru.will.git.botania.ModUtils;
import ru.will.git.eventhelper.fake.FakePlayerContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nonnull;

public class EntityThornChakram extends EntityThrowable
{

	private static final DataParameter<Integer> BOUNCES = EntityDataManager.createKey(EntityThornChakram.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> FLARE = EntityDataManager.createKey(EntityThornChakram.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> RETURN_TO = EntityDataManager.createKey(EntityThornChakram.class, DataSerializers.VARINT);
	private static final int MAX_BOUNCES = 16;
	private boolean bounced = false;
	private ItemStack stack = ItemStack.EMPTY;

	    
	public final FakePlayerContainer fake = ModUtils.NEXUS_FACTORY.wrapFake(this);
	    

	public EntityThornChakram(World world)
	{
		super(world);
	}

	public EntityThornChakram(World world, EntityLivingBase e, ItemStack stack)
	{
		super(world, e);
		this.stack = stack.copy();

		    
		this.fake.setRealPlayer(e);
		    
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataManager.register(BOUNCES, 0);
		this.dataManager.register(FLARE, false);
		this.dataManager.register(RETURN_TO, -1);
	}

	@Override
	public boolean isImmuneToExplosions()
	{
		return true;
	}

	@Override
	public void onUpdate()
	{
		// Standard motion
		double mx = this.motionX;
		double my = this.motionY;
		double mz = this.motionZ;

		super.onUpdate();

		if (!this.bounced)
		{
			// Reset the drag applied by super
			this.motionX = mx;
			this.motionY = my;
			this.motionZ = mz;
		}

		this.bounced = false;

		// Returning motion
		if (this.isReturning())
		{
			Entity thrower = this.getThrower();
			if (thrower != null)
			{
				Vector3 motion = Vector3.fromEntityCenter(thrower).subtract(Vector3.fromEntityCenter(this)).normalize();
				this.motionX = motion.x;
				this.motionY = motion.y;
				this.motionZ = motion.z;
			}
		}

		// Client FX
		if (this.world.isRemote && this.isFire())
		{
			double r = 0.1;
			double m = 0.1;
			for (int i = 0; i < 3; i++)
			{
				this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + r * (Math.random() - 0.5), this.posY + r * (Math.random() - 0.5), this.posZ + r * (Math.random() - 0.5), m * (Math.random() - 0.5), m * (Math.random() - 0.5), m * (Math.random() - 0.5));
			}
		}

		// Server state control
		if (!this.world.isRemote && (this.getTimesBounced() >= MAX_BOUNCES || this.ticksExisted > 60))
		{
			EntityLivingBase thrower = this.getThrower();
			if (thrower == null)
				this.dropAndKill();
			else
			{
				this.setEntityToReturnTo(thrower.getEntityId());
				if (this.getDistanceSq(thrower) < 2)
					this.dropAndKill();
			}
		}
	}

	private void dropAndKill()
	{
		ItemStack stack = this.getItemStack();
		EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, stack);
		this.world.spawnEntity(item);
		this.setDead();
	}

	private ItemStack getItemStack()
	{
		return !this.stack.isEmpty() ? this.stack.copy() : new ItemStack(ModItems.thornChakram, 1, this.isFire() ? 1 : 0);
	}

	@Override
	protected void onImpact(@Nonnull RayTraceResult pos)
	{
		if (this.isReturning())
			return;

		switch (pos.typeOfHit)
		{
			case BLOCK:
				Block block = this.world.getBlockState(pos.getBlockPos()).getBlock();
				if (block instanceof BlockBush || block instanceof BlockLeaves)
					return;

				int bounces = this.getTimesBounced();
				if (bounces < MAX_BOUNCES)
				{
					Vector3 currentMovementVec = new Vector3(this.motionX, this.motionY, this.motionZ);
					EnumFacing dir = pos.sideHit;
					Vector3 normalVector = new Vector3(dir.getXOffset(), dir.getYOffset(), dir.getZOffset()).normalize();
					Vector3 movementVec = normalVector.multiply(-2 * currentMovementVec.dotProduct(normalVector)).add(currentMovementVec);

					this.motionX = movementVec.x;
					this.motionY = movementVec.y;
					this.motionZ = movementVec.z;
					this.bounced = true;

					if (!this.world.isRemote)
						this.setTimesBounced(this.getTimesBounced() + 1);
				}

				break;
			case ENTITY:
				if (!this.world.isRemote && pos.entityHit instanceof EntityLivingBase)
				{
					EntityLivingBase thrower = this.getThrower();
					if (pos.entityHit != thrower)
					{
						    
						if (this.fake.cantAttack(pos.entityHit))
							break;
						    

						pos.entityHit.attackEntityFrom(thrower != null ? thrower instanceof EntityPlayer ? DamageSource.causeThrownDamage(this, thrower) : DamageSource.causeMobDamage(thrower) : DamageSource.GENERIC, 12);
						if (this.isFire())
							pos.entityHit.setFire(5);
						else if (this.world.rand.nextInt(3) == 0)
							((EntityLivingBase) pos.entityHit).addPotionEffect(new PotionEffect(MobEffects.POISON, 60, 0));
					}
				}

				break;
			default:
				break;
		}
	}

	@Override
	protected float getGravityVelocity()
	{
		return 0F;
	}

	private int getTimesBounced()
	{
		return this.dataManager.get(BOUNCES);
	}

	private void setTimesBounced(int times)
	{
		this.dataManager.set(BOUNCES, times);
	}

	public boolean isFire()
	{
		return this.dataManager.get(FLARE);
	}

	public void setFire(boolean fire)
	{
		this.dataManager.set(FLARE, fire);
	}

	private boolean isReturning()
	{
		return this.getEntityToReturnTo() > -1;
	}

	private int getEntityToReturnTo()
	{
		return this.dataManager.get(RETURN_TO);
	}

	private void setEntityToReturnTo(int entityID)
	{
		this.dataManager.set(RETURN_TO, entityID);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		super.writeEntityToNBT(compound);
		if (!this.stack.isEmpty())
			compound.setTag("fly_stack", this.stack.writeToNBT(new NBTTagCompound()));
		compound.setBoolean("flare", this.isFire());

		    
		this.fake.writeToNBT(compound);
		    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		super.readEntityFromNBT(compound);
		if (compound.hasKey("fly_stack"))
			this.stack = new ItemStack(compound.getCompoundTag("fly_stack"));
		this.setFire(compound.getBoolean("flare"));

		    
		this.fake.readFromNBT(compound);
		    
	}

}
