/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Aug 16, 2015, 3:56:14 PM (GMT)]
 */
package vazkii.botania.common.entity;

import ru.will.git.eventhelper.reflectionmedicMod;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.core.helper.PlayerHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.item.relic.ItemKingKey;

import javax.annotation.Nonnull;
import java.util.List;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class EntityBabylonWeapon extends EntityThrowableCopy implements ILightProvider
{

	private static final String TAG_CHARGING = "charging";
	private static final String TAG_VARIETY = "variety";
	private static final String TAG_CHARGE_TICKS = "chargeTicks";
	private static final String TAG_LIVE_TICKS = "liveTicks";
	private static final String TAG_DELAY = "delay";
	private static final String TAG_ROTATION = "rotation";

	private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(EntityBabylonWeapon.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> VARIETY = EntityDataManager.createKey(EntityBabylonWeapon.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> CHARGE_TICKS = EntityDataManager.createKey(EntityBabylonWeapon.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> LIVE_TICKS = EntityDataManager.createKey(EntityBabylonWeapon.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> DELAY = EntityDataManager.createKey(EntityBabylonWeapon.class, DataSerializers.VARINT);
	private static final DataParameter<Float> ROTATION = EntityDataManager.createKey(EntityBabylonWeapon.class, DataSerializers.FLOAT);

	public EntityBabylonWeapon(World world)
	{
		super(world);
	}

	public EntityBabylonWeapon(World world, EntityLivingBase thrower)
	{
		super(world, thrower);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.setSize(0F, 0F);

		this.dataManager.register(CHARGING, false);
		this.dataManager.register(VARIETY, 0);
		this.dataManager.register(CHARGE_TICKS, 0);
		this.dataManager.register(LIVE_TICKS, 0);
		this.dataManager.register(DELAY, 0);
		this.dataManager.register(ROTATION, 0F);
	}

	@Override
	public boolean isImmuneToExplosions()
	{
		return true;
	}

	@Override
	public void onUpdate()
	{
		EntityLivingBase thrower = this.getThrower();
		if (!this.world.isRemote && (thrower == null || !(thrower instanceof EntityPlayer) || thrower.isDead))
		{
			this.setDead();
			return;
		}
		EntityPlayer player = (EntityPlayer) thrower;
		boolean charging = this.isCharging();
		if (!this.world.isRemote)
		{
			ItemStack stack = player == null ? ItemStack.EMPTY : PlayerHelper.getFirstHeldItem(player, ModItems.kingKey);
			boolean newCharging = !stack.isEmpty() && ItemKingKey.isCharging(stack);
			if (charging != newCharging)
			{
				this.setCharging(newCharging);
				charging = newCharging;
			}
		}

		double x = this.motionX;
		double y = this.motionY;
		double z = this.motionZ;

		int liveTime = this.getLiveTicks();
		int delay = this.getDelay();
		charging &= liveTime == 0;

		if (charging)
		{
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;

			int chargeTime = this.getChargeTicks();
			this.setChargeTicks(chargeTime + 1);

			if (this.world.rand.nextInt(20) == 0)
				this.world.playSound(null, this.posX, this.posY, this.posZ, ModSounds.babylonSpawn, SoundCategory.PLAYERS, 0.1F, 1F + this.world.rand.nextFloat() * 3F);
		}
		else
		{
			if (liveTime < delay)
			{
				this.motionX = 0;
				this.motionY = 0;
				this.motionZ = 0;
			}
			else if (liveTime == delay && player != null)
			{
				Vector3 playerLook;
				RayTraceResult lookat = ToolCommons.raytraceFromEntity(this.world, player, true, 64);
				if (lookat == null)
					playerLook = new Vector3(player.getLookVec()).multiply(64).add(Vector3.fromEntity(player));
				else
					playerLook = new Vector3(lookat.getBlockPos().getX() + 0.5, lookat.getBlockPos().getY() + 0.5, lookat.getBlockPos().getZ() + 0.5);

				Vector3 thisVec = Vector3.fromEntityCenter(this);
				Vector3 motionVec = playerLook.subtract(thisVec).normalize().multiply(2);

				x = motionVec.x;
				y = motionVec.y;
				z = motionVec.z;
				this.world.playSound(null, this.posX, this.posY, this.posZ, ModSounds.babylonAttack, SoundCategory.PLAYERS, 2F, 0.1F + this.world.rand.nextFloat() * 3F);
			}
			this.setLiveTicks(liveTime + 1);

			if (!this.world.isRemote)
			{
				AxisAlignedBB axis = new AxisAlignedBB(this.posX, this.posY, this.posZ, this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ).grow(2);
				List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, axis);
				for (EntityLivingBase living : entities)
				{
					if (living == thrower)
						continue;

					if (living.hurtTime == 0)
					{
						    
						if (reflectionmedicMod.paranoidProtection && this.fake.cantAttack(living))
							return;
						    

						if (player != null)
							living.attackEntityFrom(DamageSource.causePlayerDamage(player), 20);
						else
							living.attackEntityFrom(DamageSource.GENERIC, 20);
						this.onImpact(new RayTraceResult(living));
						return;
					}
				}
			}
		}

		super.onUpdate();

		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;

		if (liveTime > delay)
			Botania.proxy.wispFX(this.posX, this.posY, this.posZ, 1F, 1F, 0F, 0.3F, 0F);

		if (liveTime > 200 + delay)
			this.setDead();
	}

	@Override
	protected void onImpact(RayTraceResult pos)
	{
		EntityLivingBase thrower = this.getThrower();
		if (pos.entityHit == null || pos.entityHit != thrower)
		{
			this.fake.createExplosion(this, this.posX, this.posY, this.posZ, 3F, false);
			    

			this.setDead();
		}
	}

	@Override
	public void writeEntityToNBT(@Nonnull NBTTagCompound cmp)
	{
		super.writeEntityToNBT(cmp);
		cmp.setBoolean(TAG_CHARGING, this.isCharging());
		cmp.setInteger(TAG_VARIETY, this.getVariety());
		cmp.setInteger(TAG_CHARGE_TICKS, this.getChargeTicks());
		cmp.setInteger(TAG_LIVE_TICKS, this.getLiveTicks());
		cmp.setInteger(TAG_DELAY, this.getDelay());
		cmp.setFloat(TAG_ROTATION, this.getRotation());
	}

	@Override
	public void readEntityFromNBT(@Nonnull NBTTagCompound cmp)
	{
		super.readEntityFromNBT(cmp);
		this.setCharging(cmp.getBoolean(TAG_CHARGING));
		this.setVariety(cmp.getInteger(TAG_VARIETY));
		this.setChargeTicks(cmp.getInteger(TAG_CHARGE_TICKS));
		this.setLiveTicks(cmp.getInteger(TAG_LIVE_TICKS));
		this.setDelay(cmp.getInteger(TAG_DELAY));
		this.setRotation(cmp.getFloat(TAG_ROTATION));
	}

	@Override
	@Optional.Method(modid = "albedo")
	public Light provideLight()
	{
		return Light.builder().pos(this).color(1F, 1F, 0F).radius(8).build();
	}

	public boolean isCharging()
	{
		return this.dataManager.get(CHARGING);
	}

	public void setCharging(boolean charging)
	{
		this.dataManager.set(CHARGING, charging);
	}

	public int getVariety()
	{
		return this.dataManager.get(VARIETY);
	}

	public void setVariety(int var)
	{
		this.dataManager.set(VARIETY, var);
	}

	public int getChargeTicks()
	{
		return this.dataManager.get(CHARGE_TICKS);
	}

	public void setChargeTicks(int ticks)
	{
		this.dataManager.set(CHARGE_TICKS, ticks);
	}

	public int getLiveTicks()
	{
		return this.dataManager.get(LIVE_TICKS);
	}

	public void setLiveTicks(int ticks)
	{
		this.dataManager.set(LIVE_TICKS, ticks);
	}

	public int getDelay()
	{
		return this.dataManager.get(DELAY);
	}

	public void setDelay(int delay)
	{
		this.dataManager.set(DELAY, delay);
	}

	public float getRotation()
	{
		return this.dataManager.get(ROTATION);
	}

	public void setRotation(float rot)
	{
		this.dataManager.set(ROTATION, rot);
	}

}
