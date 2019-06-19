package ic2.core.item.tool;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import ru.will.git.ic2.EventConfig;
import ru.will.git.ic2.ModUtils;
import ic2.api.event.LaserEvent;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.Ic2Player;
import ic2.core.block.MaterialIC2TNT;
import ic2.core.ref.BlockName;
import ic2.core.util.StackUtil;
import ic2.core.util.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityMiningLaser extends Entity implements IThrowableEntity
{
	public float range = 0.0F;
	public float power = 0.0F;
	public int blockBreaks = 0;
	public boolean explosive = false;
	public static final double laserSpeed = 1.0D;
	public EntityLivingBase owner;
	public boolean headingSet = false;
	public boolean smelt = false;
	private int ticksInAir = 0;

	
	public final FakePlayerContainer fake = ModUtils.NEXUS_FACTORY.wrapFake(this);
	

	public EntityMiningLaser(World world)
	{
		super(world);
		this.setSize(0.8F, 0.8F);
	}

	public EntityMiningLaser(World world, Vector3 start, Vector3 dir, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive)
	{
		super(world);
		this.owner = owner;
		this.setSize(0.8F, 0.8F);
		this.setPosition(start.x, start.y, start.z);
		this.setLaserHeading(dir.x, dir.y, dir.z, 1.0D);
		this.range = range;
		this.power = power;
		this.blockBreaks = blockBreaks;
		this.explosive = explosive;

		
		this.fake.setRealPlayer(owner);
		
	}

	@Override
	protected void entityInit()
	{
	}

	public void setLaserHeading(double motionX, double motionY, double motionZ, double speed)
	{
		double currentSpeed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
		this.motionX = motionX / currentSpeed * speed;
		this.motionY = motionY / currentSpeed * speed;
		this.motionZ = motionZ / currentSpeed * speed;
		this.prevRotationYaw = this.rotationYaw = (float) Math.toDegrees(Math.atan2(motionX, motionZ));
		this.prevRotationPitch = this.rotationPitch = (float) Math.toDegrees(Math.atan2(motionY, Math.sqrt(motionX * motionX + motionZ * motionZ)));
		this.headingSet = true;
	}

	@Override
	public void setVelocity(double motionX, double motionY, double motionZ)
	{
		this.setLaserHeading(motionX, motionY, motionZ, 1.0D);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (!IC2.platform.isSimulating() || this.range >= 1.0F && this.power > 0.0F && this.blockBreaks > 0)
		{
			++this.ticksInAir;
			Vec3d oldPosition = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d newPosition = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			World world = this.getEntityWorld();
			RayTraceResult result = world.rayTraceBlocks(oldPosition, newPosition, false, true, false);
			oldPosition = new Vec3d(this.posX, this.posY, this.posZ);
			if (result != null)
				newPosition = new Vec3d(result.hitVec.x, result.hitVec.y, result.hitVec.z);
			else
				newPosition = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			Entity hitEntity = null;
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));
			double distance = 0.0D;

			for (Entity entity : list)
			{
				if (entity.canBeCollidedWith() && (entity != this.owner || this.ticksInAir >= 5))
				{
					AxisAlignedBB hitBox = entity.getEntityBoundingBox().grow(0.3D);
					RayTraceResult intercept = hitBox.calculateIntercept(oldPosition, newPosition);
					if (intercept != null)
					{
						double newDistance = oldPosition.distanceTo(intercept.hitVec);
						if (newDistance < distance || distance == 0.0D)
						{
							hitEntity = entity;
							distance = newDistance;
						}
					}
				}
			}

			RayTraceResult blockHit = result;
			if (hitEntity != null)
				result = new RayTraceResult(hitEntity);

			if (result != null && result.typeOfHit != Type.MISS && !world.isRemote)
			{
				if (this.explosive)
				{
					this.explode();
					this.setDead();
					return;
				}

				switch (result.typeOfHit)
				{
					case ENTITY:
						if (this.hitEntity(result.entityHit))
							break;

						if (blockHit == null)
						{
							this.power -= 0.5F;
							break;
						}
						result = blockHit;
					case BLOCK:
						if (!this.hitBlock(result.getBlockPos(), result.sideHit))
							this.power -= 0.5F;
						break;
					default:
						throw new RuntimeException("invalid hit type: " + result.typeOfHit);
				}
			}
			else
				this.power -= 0.5F;

			this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			this.range = (float) (this.range - Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ));
			if (this.isInWater())
				this.setDead();

		}
		else
		{
			if (this.explosive)
				this.explode();

			this.setDead();
		}
	}

	private void explode()
	{
		World world = this.getEntityWorld();
		LaserEvent.LaserExplodesEvent event = new LaserEvent.LaserExplodesEvent(world, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, 5.0F, 0.85F, 0.55F);
		if (MinecraftForge.EVENT_BUS.post(event))
			this.setDead();
		else
		{
			this.copyDataFromEvent(event);
			ExplosionIC2 explosion = new ExplosionIC2(world, this, this.posX, this.posY, this.posZ, event.explosionPower, event.explosionDropRate);

			
			explosion.fake.setParent(this.fake);
			

			explosion.doExplosion();
		}
	}

	private boolean hitEntity(Entity entity)
	{
		LaserEvent.LaserHitsEntityEvent event = new LaserEvent.LaserHitsEntityEvent(this.getEntityWorld(), this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, entity);
		if (MinecraftForge.EVENT_BUS.post(event))
		{
			if (event.passThrough)
				return false;
			this.setDead();
			return true;
		}
		this.copyDataFromEvent(event);
		entity = event.hitEntity;
		int damage = (int) this.power;

		if (damage > 0 && (!EventConfig.laserEvent || !this.fake.cantAttack(entity)))
		{
			entity.setFire(damage * (this.smelt ? 2 : 1));
			if (entity.attackEntityFrom(new EntityDamageSourceIndirect("arrow", this, this.owner).setProjectile(), (float) damage) && (this.owner instanceof EntityPlayer && entity instanceof EntityDragon && ((EntityDragon) entity).getHealth() <= 0.0F || entity instanceof MultiPartEntityPart && ((MultiPartEntityPart) entity).parent instanceof EntityDragon && ((EntityLivingBase) ((MultiPartEntityPart) entity).parent).getHealth() <= 0.0F))
				IC2.achievements.issueAchievement((EntityPlayer) this.owner, "killDragonMiningLaser");
		}

		this.setDead();
		return true;
	}

	private boolean hitBlock(BlockPos pos, EnumFacing side)
	{
		World world = this.getEntityWorld();
		LaserEvent.LaserHitsBlockEvent event = new LaserEvent.LaserHitsBlockEvent(world, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, pos, side, 0.9F, true, true);
		if (MinecraftForge.EVENT_BUS.post(event))
		{
			this.setDead();
			return true;
		}
		this.copyDataFromEvent(event);
		IBlockState state = world.getBlockState(event.pos);
		Block block = state.getBlock();
		EntityPlayer playerOwner = this.owner instanceof EntityPlayer ? (EntityPlayer) this.owner : Ic2Player.get(world);
		if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, playerOwner)))
		{
			this.setDead();
			return true;
		}
		if (!block.isAir(state, world, event.pos) && block != Blocks.GLASS && block != Blocks.GLASS_PANE && block != BlockName.glass.getInstance())
		{
			if (world.isRemote)
				return true;
			float hardness = state.getBlockHardness(world, event.pos);
			if (hardness < 0.0F)
			{
				this.setDead();
				return true;
			}
			this.power -= hardness / 1.5F;
			if (this.power < 0.0F)
				return true;

			
			if (EventConfig.laserEvent && this.fake.cantBreak(event.pos))
			{
				this.setDead();
				return true;
			}
			

			List<ItemStack> replacements = new ArrayList<>();
			if (state.getMaterial() != Material.TNT && state.getMaterial() != MaterialIC2TNT.instance)
			{
				if (this.smelt)
					if (state.getMaterial() == Material.WOOD)
						event.dropBlock = false;
					else
					{
						for (ItemStack isa : StackUtil.getDrops(world, event.pos, state, block, 0))
						{
							ItemStack is = FurnaceRecipes.instance().getSmeltingResult(isa);
							if (!StackUtil.isEmpty(is))
								replacements.add(is);
						}

						event.dropBlock = replacements.isEmpty();
					}
			}
			else
				block.onBlockDestroyedByExplosion(world, event.pos, new Explosion(world, this, event.pos.getX() + 0.5D, event.pos.getY() + 0.5D, event.pos.getZ() + 0.5D, 1.0F, false, true));

			if (event.removeBlock)
			{
				if (event.dropBlock)
					block.dropBlockAsItemWithChance(world, event.pos, state, event.dropChance, 0);

				world.setBlockToAir(event.pos);

				for (ItemStack replacement : replacements)
				{
					if (!StackUtil.placeBlock(replacement, world, event.pos))
						StackUtil.dropAsEntity(world, event.pos, replacement);

					this.power = 0.0F;
				}

				if (world.rand.nextInt(10) == 0 && state.getMaterial().getCanBurn())
					world.setBlockState(event.pos, Blocks.FIRE.getDefaultState());
			}

			--this.blockBreaks;
			return true;
		}
		return false;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		
		this.fake.writeToNBT(nbttagcompound);
		
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		
		this.fake.readFromNBT(nbttagcompound);
		
	}

	void copyDataFromEvent(LaserEvent event)
	{
		
		if (this.owner != event.owner)
			this.fake.setRealPlayer(event.owner);
		

		this.owner = event.owner;
		this.range = event.range;
		this.power = event.power;
		this.blockBreaks = event.blockBreaks;
		this.explosive = event.explosive;
		this.smelt = event.smelt;
	}

	@Override
	public Entity getThrower()
	{
		return this.owner;
	}

	@Override
	public void setThrower(Entity entity)
	{
		if (entity instanceof EntityLivingBase)
		{
			
			if (this.owner != entity)
				this.fake.setRealPlayer(entity);
			

			this.owner = (EntityLivingBase) entity;
		}
	}
}
