/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Jan 26, 2014, 5:09:12 PM (GMT)]
 */
package vazkii.botania.common.entity;

import ru.will.git.botania.ModUtils;
import ru.will.git.eventhelper.fake.FakePlayerContainer;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.*;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.tile.TileMod;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.equipment.bauble.ItemTinyPlanet;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.*;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class EntityManaBurst extends EntityThrowable implements IManaBurst, ILightProvider
{

	private static final String TAG_TICKS_EXISTED = "ticksExisted";
	private static final String TAG_COLOR = "color";
	private static final String TAG_MANA = "mana";
	private static final String TAG_STARTING_MANA = "startingMana";
	private static final String TAG_MIN_MANA_LOSS = "minManaLoss";
	private static final String TAG_TICK_MANA_LOSS = "manaLossTick";
	private static final String TAG_SPREADER_X = "spreaderX";
	private static final String TAG_SPREADER_Y = "spreaderY";
	private static final String TAG_SPREADER_Z = "spreaderZ";
	private static final String TAG_GRAVITY = "gravity";
	private static final String TAG_LENS_STACK = "lensStack";
	private static final String TAG_LAST_MOTION_X = "lastMotionX";
	private static final String TAG_LAST_MOTION_Y = "lastMotionY";
	private static final String TAG_LAST_MOTION_Z = "lastMotionZ";
	private static final String TAG_HAS_SHOOTER = "hasShooter";
	private static final String TAG_SHOOTER_UUID_MOST = "shooterUUIDMost";
	private static final String TAG_SHOOTER_UUID_LEAST = "shooterUUIDLeast";

	private static final DataParameter<Integer> COLOR = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> MANA = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> START_MANA = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> MIN_MANA_LOSS = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.VARINT);
	private static final DataParameter<Float> MANA_LOSS_PER_TICK = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> GRAVITY = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.FLOAT);
	private static final DataParameter<BlockPos> SOURCE_COORDS = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.BLOCK_POS);
	private static final DataParameter<ItemStack> SOURCE_LENS = EntityDataManager.createKey(EntityManaBurst.class, DataSerializers.ITEM_STACK);

	float accumulatedManaLoss = 0;
	boolean fake = false;
	final Set<BlockPos> alreadyCollidedAt = new HashSet<>();
	boolean fullManaLastTick = true;
	UUID shooterIdentity = null;
	int _ticksExisted = 0;
	boolean scanBeam = false;
	public final List<PositionProperties> propsList = new ArrayList<>();

	    
	private final FakePlayerContainer fakeContainer = ModUtils.NEXUS_FACTORY.wrapFake(this);

	@Override
	@Nonnull
	public final FakePlayerContainer getFakePlayerContainer()
	{
		return this.fakeContainer;
	}
	    

	public EntityManaBurst(World world)
	{
		super(world);
		this.setSize(0F, 0F);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataManager.register(COLOR, 0);
		this.dataManager.register(MANA, 0);
		this.dataManager.register(START_MANA, 0);
		this.dataManager.register(MIN_MANA_LOSS, 0);
		this.dataManager.register(MANA_LOSS_PER_TICK, 0F);
		this.dataManager.register(GRAVITY, 0F);
		this.dataManager.register(SOURCE_COORDS, BlockPos.ORIGIN);
		this.dataManager.register(SOURCE_LENS, ItemStack.EMPTY);
	}

	public EntityManaBurst(IManaSpreader spreader, boolean fake)
	{
		this(((TileEntity) spreader).getWorld());

		TileEntity tile = (TileEntity) spreader;

		this.fake = fake;

		this.setBurstSourceCoords(tile.getPos());
		this.setLocationAndAngles(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5, 0, 0);
		this.rotationYaw = -(spreader.getRotationX() + 90F);
		this.rotationPitch = spreader.getRotationY();

		float f = 0.4F;
		double mx = MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f / 2D;
		double mz = -(MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f) / 2D;
		double my = MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI) * f / 2D;
		this.setMotion(mx, my, mz);

		    
		if (spreader instanceof TileMod)
			this.fakeContainer.setParent(((TileMod) spreader).fake);
		    
	}

	public EntityManaBurst(EntityPlayer player, EnumHand hand)
	{
		this(player.world);

		this.setBurstSourceCoords(new BlockPos(0, -1, 0));
		this.setLocationAndAngles(player.posX, player.posY + player.getEyeHeight(), player.posZ, player.rotationYaw + 180, -player.rotationPitch);

		this.posX -= (hand == EnumHand.OFF_HAND ? -1 : 1) * MathHelper.cos((this.rotationYaw + 180) / 180.0F * (float) Math.PI) * 0.16F;
		this.posY -= 0.10000000149011612D;
		this.posZ -= (hand == EnumHand.OFF_HAND ? -1 : 1) * MathHelper.sin((this.rotationYaw + 180) / 180.0F * (float) Math.PI) * 0.16F;

		this.setPosition(this.posX, this.posY, this.posZ);
		float f = 0.4F;
		double mx = MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f / 2D;
		double mz = -(MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f) / 2D;
		double my = MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI) * f / 2D;
		this.setMotion(mx, my, mz);

		    
		this.fakeContainer.setRealPlayer(player);
		    
	}

	// Copy of EntityThrowable.onUpdate. Relevant edits indicated.
	private void superUpdate()
	{
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;
		// super.onUpdate(); Botania - inline supersuperclass's onUpdate
		if (!this.world.isRemote)
		{
			this.setFlag(6, this.isGlowing());
		}

		this.onEntityUpdate();

		if (this.throwableShake > 0)
		{
			--this.throwableShake;
		}

		// Botania - remove inGround check and its else branch. Bursts are never inGround.

		Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d, vec3d1);
		vec3d = new Vec3d(this.posX, this.posY, this.posZ);
		vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		if (raytraceresult != null)
		{
			vec3d1 = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
		}

		if (!this.scanBeam && !this.world.isRemote)
		{ // Botania - only do entity colliding on server and while not scanning
			Entity entity = null;
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().offset(this.motionX, this.motionY, this.motionZ).grow(1.0D));
			double d0 = 0.0D;
			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = list.get(i);

				if (entity1.canBeCollidedWith())
				{
					if (entity1 == this.ignoreEntity)
					{
					}
					else if (this.ticksExisted < 2 && this.ignoreEntity == null)
					{
						this.ignoreEntity = entity1;
					}
					else
					{
						AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
						RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);

						if (raytraceresult1 != null)
						{
							double d1 = vec3d.squareDistanceTo(raytraceresult1.hitVec);

							if (d1 < d0 || d0 == 0.0D)
							{
								entity = entity1;
								d0 = d1;
							}
						}
					}
				}
			}

			if (this.ignoreEntity != null)
			{
				/*if (flag)
				{
					this.ignoreTime = 2;
				}
				else if (this.ignoreTime-- <= 0)
				{
					this.ignoreEntity = null;
				}*/
			}

			if (entity != null)
			{
				raytraceresult = new RayTraceResult(entity);
			}
		} // End wrap - only do entity colliding on server

		if (raytraceresult != null)
		{
			if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(raytraceresult.getBlockPos()).getBlock() == Blocks.PORTAL)
			{
				this.setPortal(raytraceresult.getBlockPos());
			}
			else
			{
				this.onImpact(raytraceresult);
			}
		}

		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

		for (this.rotationPitch = (float) (MathHelper.atan2(this.motionY, f) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
		{
		}

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
		{
			this.prevRotationPitch += 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw < -180.0F)
		{
			this.prevRotationYaw -= 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
		{
			this.prevRotationYaw += 360.0F;
		}

		this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
		this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
		float f2 = this.getGravityVelocity();

		// Botania - don't do water particles, bursts are never inWater
		/*
		if (isInWater())
		{
			for (int j = 0; j < 4; ++j)
			{
				float f3 = 0.25F;
				world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * f3, posY - motionY * f3, posZ - motionZ * f3, motionX, motionY, motionZ, new int[0]);
			}
		}*/

		// Botania - don't apply drag
		// this.motionX *= (double)f1;
		// this.motionY *= (double)f1;
		// this.motionZ *= (double)f1;
		this.motionY -= f2;
		this.setPosition(this.posX, this.posY, this.posZ);
	}

	@Override
	public void onUpdate()
	{
		this.setTicksExisted(this.getTicksExisted() + 1);
		this.superUpdate();

		if (!this.fake && !this.isDead && !this.scanBeam)
			this.ping();

		ILensEffect lens = this.getLensInstance();
		if (lens != null)
			lens.updateBurst(this, this.getSourceLens());

		int mana = this.getMana();
		if (this.getTicksExisted() >= this.getMinManaLoss())
		{
			this.accumulatedManaLoss += this.getManaLossPerTick();
			int loss = (int) this.accumulatedManaLoss;
			this.setMana(mana - loss);
			this.accumulatedManaLoss -= loss;

			if (this.getMana() <= 0)
				this.setDead();
		}

		this.particles();

		this.setMotion(this.motionX, this.motionY, this.motionZ);

		this.fullManaLastTick = this.getMana() == this.getStartingMana();

		if (this.scanBeam)
		{
			PositionProperties props = new PositionProperties(this);
			if (this.propsList.isEmpty())
				this.propsList.add(props);
			else
			{
				PositionProperties lastProps = this.propsList.get(this.propsList.size() - 1);
				if (!props.coordsEqual(lastProps))
					this.propsList.add(props);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
	{
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}

	@Override
	public boolean handleWaterMovement()
	{
		return false;
	}

	@Override
	public boolean isInLava()
	{
		//Avoids expensive getBlockState check in Entity#onEntityUpdate (see super impl)
		return false;
	}

	private TileEntity collidedTile = null;
	private boolean noParticles = false;

	public TileEntity getCollidedTile(boolean noParticles)
	{
		this.noParticles = noParticles;

		int iterations = 0;
		while (!this.isDead && iterations < ConfigHandler.spreaderTraceTime)
		{
			this.onUpdate();
			iterations++;
		}

		if (this.fake)
			this.incrementFakeParticleTick();

		return this.collidedTile;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		nbt.setInteger(TAG_TICKS_EXISTED, this.getTicksExisted());
		nbt.setInteger(TAG_COLOR, this.getColor());
		nbt.setInteger(TAG_MANA, this.getMana());
		nbt.setInteger(TAG_STARTING_MANA, this.getStartingMana());
		nbt.setInteger(TAG_MIN_MANA_LOSS, this.getMinManaLoss());
		nbt.setFloat(TAG_TICK_MANA_LOSS, this.getManaLossPerTick());
		nbt.setFloat(TAG_GRAVITY, this.getGravity());

		ItemStack stack = this.getSourceLens();
		NBTTagCompound lensCmp = new NBTTagCompound();
		if (!stack.isEmpty())
			lensCmp = stack.writeToNBT(lensCmp);
		nbt.setTag(TAG_LENS_STACK, lensCmp);

		BlockPos coords = this.getBurstSourceBlockPos();
		nbt.setInteger(TAG_SPREADER_X, coords.getX());
		nbt.setInteger(TAG_SPREADER_Y, coords.getY());
		nbt.setInteger(TAG_SPREADER_Z, coords.getZ());

		nbt.setDouble(TAG_LAST_MOTION_X, this.motionX);
		nbt.setDouble(TAG_LAST_MOTION_Y, this.motionY);
		nbt.setDouble(TAG_LAST_MOTION_Z, this.motionZ);

		UUID identity = this.getShooterUUID();
		boolean hasShooter = identity != null;
		nbt.setBoolean(TAG_HAS_SHOOTER, hasShooter);
		if (hasShooter)
		{
			nbt.setLong(TAG_SHOOTER_UUID_MOST, identity.getMostSignificantBits());
			nbt.setLong(TAG_SHOOTER_UUID_LEAST, identity.getLeastSignificantBits());
		}

		    
		this.fakeContainer.writeToNBT(nbt);
		    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.setTicksExisted(nbt.getInteger(TAG_TICKS_EXISTED));
		this.setColor(nbt.getInteger(TAG_COLOR));
		this.setMana(nbt.getInteger(TAG_MANA));
		this.setStartingMana(nbt.getInteger(TAG_STARTING_MANA));
		this.setMinManaLoss(nbt.getInteger(TAG_MIN_MANA_LOSS));
		this.setManaLossPerTick(nbt.getFloat(TAG_TICK_MANA_LOSS));
		this.setGravity(nbt.getFloat(TAG_GRAVITY));

		NBTTagCompound lensCmp = nbt.getCompoundTag(TAG_LENS_STACK);
		ItemStack stack = new ItemStack(lensCmp);
		if (!stack.isEmpty())
			this.setSourceLens(stack);
		else
			this.setSourceLens(ItemStack.EMPTY);

		int x = nbt.getInteger(TAG_SPREADER_X);
		int y = nbt.getInteger(TAG_SPREADER_Y);
		int z = nbt.getInteger(TAG_SPREADER_Z);

		this.setBurstSourceCoords(new BlockPos(x, y, z));

		double lastMotionX = nbt.getDouble(TAG_LAST_MOTION_X);
		double lastMotionY = nbt.getDouble(TAG_LAST_MOTION_Y);
		double lastMotionZ = nbt.getDouble(TAG_LAST_MOTION_Z);

		this.setMotion(lastMotionX, lastMotionY, lastMotionZ);

		boolean hasShooter = nbt.getBoolean(TAG_HAS_SHOOTER);
		if (hasShooter)
		{
			long most = nbt.getLong(TAG_SHOOTER_UUID_MOST);
			long least = nbt.getLong(TAG_SHOOTER_UUID_LEAST);
			UUID identity = this.getShooterUUID();
			if (identity == null || most != identity.getMostSignificantBits() || least != identity.getLeastSignificantBits())
				this.shooterIdentity = new UUID(most, least);
		}

		    
		this.fakeContainer.readFromNBT(nbt);
		    
	}

	public void particles()
	{
		if (this.isDead || !this.world.isRemote)
			return;

		ILensEffect lens = this.getLensInstance();
		if (lens != null && !lens.doParticles(this, this.getSourceLens()))
			return;

		Color color = new Color(this.getColor());
		float r = color.getRed() / 255F;
		float g = color.getGreen() / 255F;
		float b = color.getBlue() / 255F;
		float osize = this.getParticleSize();
		float size = osize;

		if (this.fake)
		{
			if (this.getMana() == this.getStartingMana())
				size = 2F;
			else if (this.fullManaLastTick)
				size = 4F;

			if (!this.noParticles && this.shouldDoFakeParticles())
				Botania.proxy.sparkleFX(this.posX, this.posY, this.posZ, r, g, b, 0.4F * size, 1, true);
		}
		else
		{
			boolean monocle = Botania.proxy.isClientPlayerWearingMonocle();
			if (monocle)
				Botania.proxy.setWispFXDepthTest(false);

			if (ConfigHandler.subtlePowerSystem)
				Botania.proxy.wispFX(this.posX, this.posY, this.posZ, r, g, b, 0.1F * size, (float) (Math.random() - 0.5F) * 0.02F, (float) (Math.random() - 0.5F) * 0.02F, (float) (Math.random() - 0.5F) * 0.01F);
			else
			{
				float or = r;
				float og = g;
				float ob = b;

				double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b; // Standard relative luminance calculation

				double savedPosX = this.posX;
				double savedPosY = this.posY;
				double savedPosZ = this.posZ;

				Vector3 currentPos = Vector3.fromEntity(this);
				Vector3 oldPos = new Vector3(this.prevPosX, this.prevPosY, this.prevPosZ);
				Vector3 diffVec = oldPos.subtract(currentPos);
				Vector3 diffVecNorm = diffVec.normalize();

				double distance = 0.095;

				do
				{
					if (luminance < 0.1)
					{
						r = or + (float) Math.random() * 0.125F;
						g = og + (float) Math.random() * 0.125F;
						b = ob + (float) Math.random() * 0.125F;
					}
					size = osize + ((float) Math.random() - 0.5F) * 0.065F + (float) Math.sin(new Random(this.entityUniqueID.getMostSignificantBits()).nextInt(9001)) * 0.4F;
					Botania.proxy.wispFX(this.posX, this.posY, this.posZ, r, g, b, 0.2F * size, (float) -this.motionX * 0.01F, (float) -this.motionY * 0.01F, (float) -this.motionZ * 0.01F);

					this.posX += diffVecNorm.x * distance;
					this.posY += diffVecNorm.y * distance;
					this.posZ += diffVecNorm.z * distance;

					currentPos = Vector3.fromEntity(this);
					diffVec = oldPos.subtract(currentPos);
					if (this.getEntityData().hasKey(ItemTinyPlanet.TAG_ORBIT))
						break;
				}
				while (Math.abs(diffVec.mag()) > distance);

				Botania.proxy.wispFX(this.posX, this.posY, this.posZ, or, og, ob, 0.1F * size, (float) (Math.random() - 0.5F) * 0.06F, (float) (Math.random() - 0.5F) * 0.06F, (float) (Math.random() - 0.5F) * 0.06F);

				this.posX = savedPosX;
				this.posY = savedPosY;
				this.posZ = savedPosZ;
			}

			if (monocle)
				Botania.proxy.setWispFXDepthTest(true);
		}
	}

	public float getParticleSize()
	{
		return (float) this.getMana() / (float) this.getStartingMana();
	}

	@Override
	protected void onImpact(@Nonnull RayTraceResult rtr)
	{
		boolean collided = false;
		boolean dead = false;

		if (rtr.entityHit == null)
		{
			TileEntity tile = this.world.getTileEntity(rtr.getBlockPos());
			IBlockState state = this.world.getBlockState(rtr.getBlockPos());
			Block block = state.getBlock();

			if (block instanceof IManaCollisionGhost && ((IManaCollisionGhost) block).isGhost(state, this.world, rtr.getBlockPos()) && !(block instanceof IManaTrigger) || block instanceof BlockBush || block instanceof BlockLeaves)
				return;

			if (BotaniaAPI.internalHandler.isBuildcraftPipe(tile))
				return;

			BlockPos coords = this.getBurstSourceBlockPos();
			if (tile != null && !tile.getPos().equals(coords))
				this.collidedTile = tile;

			if (tile == null || !tile.getPos().equals(coords))
			{
				if (!this.fake && !this.noParticles && (!this.world.isRemote || tile instanceof IClientManaHandler) && tile instanceof IManaReceiver && ((IManaReceiver) tile).canRecieveManaFromBursts())
					this.onRecieverImpact((IManaReceiver) tile, tile.getPos());

				if (block instanceof IManaTrigger)
					((IManaTrigger) block).onBurstCollision(this, this.world, rtr.getBlockPos());

				boolean ghost = block instanceof IManaCollisionGhost;
				dead = !ghost;
				if (ghost)
					return;
			}

			collided = true;
		}

		ILensEffect lens = this.getLensInstance();
		if (lens != null)
			dead = lens.collideBurst(this, rtr, this.collidedTile != null && this.collidedTile instanceof IManaReceiver && ((IManaReceiver) this.collidedTile).canRecieveManaFromBursts(), dead, this.getSourceLens());

		if (collided && !this.hasAlreadyCollidedAt(rtr.getBlockPos()))
			this.alreadyCollidedAt.add(rtr.getBlockPos());

		if (dead && !this.isDead)
		{
			if (!this.fake)
			{
				Color color = new Color(this.getColor());
				float r = color.getRed() / 255F;
				float g = color.getGreen() / 255F;
				float b = color.getBlue() / 255F;

				int mana = this.getMana();
				int maxMana = this.getStartingMana();
				float size = (float) mana / (float) maxMana;

				if (!ConfigHandler.subtlePowerSystem)
					for (int i = 0; i < 4; i++)
					{
						Botania.proxy.wispFX(this.posX, this.posY, this.posZ, r, g, b, 0.15F * size, (float) (Math.random() - 0.5F) * 0.04F, (float) (Math.random() - 0.5F) * 0.04F, (float) (Math.random() - 0.5F) * 0.04F);
					}
				Botania.proxy.sparkleFX((float) this.posX, (float) this.posY, (float) this.posZ, r, g, b, 4, 2);
			}

			this.setDead();
		}
	}

	private void onRecieverImpact(IManaReceiver tile, BlockPos pos)
	{
		ILensEffect lens = this.getLensInstance();
		int mana = this.getMana();

		if (lens != null)
		{
			ItemStack stack = this.getSourceLens();
			mana = lens.getManaToTransfer(this, this, stack, tile);
		}

		if (tile instanceof IManaCollector)
			mana *= ((IManaCollector) tile).getManaYieldMultiplier(this);

		tile.recieveMana(mana);

		if (tile instanceof IThrottledPacket)
			((IThrottledPacket) tile).markDispatchable();
		else
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this.world, pos);
	}

	@Override
	public void setDead()
	{
		super.setDead();

		if (!this.fake)
		{
			TileEntity tile = this.getShooter();
			if (tile instanceof IManaSpreader)
				((IManaSpreader) tile).setCanShoot(true);
		}
		else
			this.setDeathTicksForFakeParticle();
	}

	private TileEntity getShooter()
	{
		return this.world.getTileEntity(this.getBurstSourceBlockPos());
	}

	@Override
	protected float getGravityVelocity()
	{
		return this.getGravity();
	}

	@Override
	public boolean isFake()
	{
		return this.fake;
	}

	@Override
	public void setFake(boolean fake)
	{
		this.fake = fake;
	}

	public void setScanBeam()
	{
		this.scanBeam = true;
	}

	@Override
	public int getColor()
	{
		return this.dataManager.get(COLOR);
	}

	@Override
	public void setColor(int color)
	{
		this.dataManager.set(COLOR, color);
	}

	@Override
	public int getMana()
	{
		return this.dataManager.get(MANA);
	}

	@Override
	public void setMana(int mana)
	{
		this.dataManager.set(MANA, mana);
	}

	@Override
	public int getStartingMana()
	{
		return this.dataManager.get(START_MANA);
	}

	@Override
	public void setStartingMana(int mana)
	{
		this.dataManager.set(START_MANA, mana);
	}

	@Override
	public int getMinManaLoss()
	{
		return this.dataManager.get(MIN_MANA_LOSS);
	}

	@Override
	public void setMinManaLoss(int minManaLoss)
	{
		this.dataManager.set(MIN_MANA_LOSS, minManaLoss);
	}

	@Override
	public float getManaLossPerTick()
	{
		return this.dataManager.get(MANA_LOSS_PER_TICK);
	}

	@Override
	public void setManaLossPerTick(float mana)
	{
		this.dataManager.set(MANA_LOSS_PER_TICK, mana);
	}

	@Override
	public float getGravity()
	{
		return this.dataManager.get(GRAVITY);
	}

	@Override
	public void setGravity(float gravity)
	{
		this.dataManager.set(GRAVITY, gravity);
	}

	@Override
	public BlockPos getBurstSourceBlockPos()
	{
		return this.dataManager.get(SOURCE_COORDS);
	}

	@Override
	public void setBurstSourceCoords(BlockPos pos)
	{
		this.dataManager.set(SOURCE_COORDS, pos);
	}

	@Override
	public ItemStack getSourceLens()
	{
		return this.dataManager.get(SOURCE_LENS);
	}

	@Override
	public void setSourceLens(ItemStack lens)
	{
		this.dataManager.set(SOURCE_LENS, lens);
	}

	@Override
	public int getTicksExisted()
	{
		return this._ticksExisted;
	}

	public void setTicksExisted(int ticks)
	{
		this._ticksExisted = ticks;
	}

	private ILensEffect getLensInstance()
	{
		ItemStack lens = this.getSourceLens();
		if (!lens.isEmpty() && lens.getItem() instanceof ILensEffect)
			return (ILensEffect) lens.getItem();

		return null;
	}

	@Override
	public void setMotion(double x, double y, double z)
	{
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}

	@Override
	public boolean hasAlreadyCollidedAt(BlockPos pos)
	{
		return this.alreadyCollidedAt.contains(pos);
	}

	@Override
	public void setCollidedAt(BlockPos pos)
	{
		if (!this.hasAlreadyCollidedAt(pos))
			this.alreadyCollidedAt.add(pos.toImmutable());
	}

	@Override
	public void setShooterUUID(UUID uuid)
	{
		this.shooterIdentity = uuid;
	}

	@Override
	public UUID getShooterUUID()
	{
		return this.shooterIdentity;
	}

	@Override
	public void ping()
	{
		TileEntity tile = this.getShooter();
		if (tile instanceof IPingable)
			((IPingable) tile).pingback(this, this.getShooterUUID());
	}

	protected boolean shouldDoFakeParticles()
	{
		if (ConfigHandler.staticWandBeam)
			return true;

		TileEntity tile = this.getShooter();
		return tile instanceof IManaSpreader && (this.getMana() != this.getStartingMana() && this.fullManaLastTick || Math.abs(((IManaSpreader) tile).getBurstParticleTick() - this.getTicksExisted()) < 4);
	}

	private void incrementFakeParticleTick()
	{
		TileEntity tile = this.getShooter();
		if (tile instanceof IManaSpreader)
		{
			IManaSpreader spreader = (IManaSpreader) tile;
			spreader.setBurstParticleTick(spreader.getBurstParticleTick() + 2);
			if (spreader.getLastBurstDeathTick() != -1 && spreader.getBurstParticleTick() > spreader.getLastBurstDeathTick())
				spreader.setBurstParticleTick(0);
		}
	}

	private void setDeathTicksForFakeParticle()
	{
		BlockPos coords = this.getBurstSourceBlockPos();
		TileEntity tile = this.world.getTileEntity(coords);
		if (tile instanceof IManaSpreader)
			((IManaSpreader) tile).setLastBurstDeathTick(this.getTicksExisted());
	}

	@Override
	@Optional.Method(modid = "albedo")
	public Light provideLight()
	{
		int color = this.getColor();
		return Light.builder().pos(new Vec3d(this.posX - this.motionX, this.posY - this.motionY, this.posZ - this.motionZ)).color(color, false).radius(this.getParticleSize() * 8).build();
	}

	public static class PositionProperties
	{

		public final BlockPos coords;
		public final IBlockState state;

		public boolean invalid = false;

		public PositionProperties(Entity entity)
		{
			int x = MathHelper.floor(entity.posX);
			int y = MathHelper.floor(entity.posY);
			int z = MathHelper.floor(entity.posZ);
			this.coords = new BlockPos(x, y, z);
			this.state = entity.world.getBlockState(this.coords);
		}

		public boolean coordsEqual(PositionProperties props)
		{
			return this.coords.equals(props.coords);
		}

		public boolean contentsEqual(World world)
		{
			if (!world.isBlockLoaded(this.coords))
			{
				this.invalid = true;
				return false;
			}

			return world.getBlockState(this.coords) == this.state;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(this.coords, this.state);
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof PositionProperties && ((PositionProperties) o).state == this.state && ((PositionProperties) o).coords.equals(this.coords);
		}
	}

}
