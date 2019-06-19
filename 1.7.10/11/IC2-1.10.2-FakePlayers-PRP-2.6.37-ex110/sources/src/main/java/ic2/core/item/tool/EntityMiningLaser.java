package ic2.core.item.tool;

import java.util.List;

import ru.will.git.ic2.FakePlayerContainer;
import ru.will.git.ic2.FakePlayerContainerEntity;
import ru.will.git.ic2.ModUtils;

import ic2.api.event.LaserEvent;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.block.MaterialIC2TNT;
import ic2.core.ref.BlockName;
import ic2.core.util.StackUtil;
import ic2.core.util.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

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
    
    
    
	}

	@Override
	protected void entityInit()
	{
	}

	public void setLaserHeading(double motionX, double motionY, double motionZ, double speed)
	{
		double currentSpeed = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
		this.motionX = motionX / currentSpeed * speed;
		this.motionY = motionY / currentSpeed * speed;
		this.motionZ = motionZ / currentSpeed * speed;
		this.prevRotationYaw = this.rotationYaw = (float) Math.toDegrees(Math.atan2(motionX, motionZ));
		this.prevRotationPitch = this.rotationPitch = (float) Math.toDegrees(Math.atan2(motionY, MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ)));
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
			RayTraceResult result = this.worldObj.rayTraceBlocks(oldPosition, newPosition, false, true, false);
			oldPosition = new Vec3d(this.posX, this.posY, this.posZ);
			if (result != null)
				newPosition = new Vec3d(result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord);
			else
				newPosition = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			Entity entity = null;
			List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d = 0.0D;

			for (int l = 0; l < list.size(); ++l)
			{
				Entity entity1 = (Entity) list.get(l);
				if (entity1.canBeCollidedWith() && (entity1 != this.owner || this.ticksInAir >= 5))
				{
					float f4 = 0.3F;
					AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().expand(f4, f4, f4);
					RayTraceResult movingobjectposition1 = axisalignedbb1.calculateIntercept(oldPosition, newPosition);
					if (movingobjectposition1 != null)
					{
						double d1 = oldPosition.distanceTo(movingobjectposition1.hitVec);
						if (d1 < d || d == 0.0D)
						{
							entity = entity1;
							d = d1;
						}
					}
				}
			}

			if (entity != null)
				result = new RayTraceResult(entity);

			if (result != null && result.typeOfHit != Type.MISS && !this.worldObj.isRemote)
			{
				if (this.explosive)
				{
					this.explode();
					this.setDead();
					return;
				}

				switch (result.typeOfHit)
				{
					case BLOCK:
						if (!this.hitBlock(result.getBlockPos(), result.sideHit))
							this.power -= 0.5F;
						break;
					case ENTITY:
						this.hitEntity(result.entityHit);
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
		LaserEvent.LaserExplodesEvent event = new LaserEvent.LaserExplodesEvent(this.worldObj, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, 5.0F, 0.85F, 0.55F);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled())
			this.setDead();
		else
		{
			this.copyDataFromEvent(event);
    
    

			explosion.doExplosion();
		}
	}

	private void hitEntity(Entity entity)
	{
		LaserEvent.LaserHitsEntityEvent event = new LaserEvent.LaserHitsEntityEvent(this.worldObj, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, entity);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled())
			this.setDead();
		else
		{
			this.copyDataFromEvent(event);
			entity = event.hitEntity;
    
			if (damage > 0 && !this.fake.cantDamage(entity))
			{
				entity.setFire(damage * (this.smelt ? 2 : 1));
				if (entity.attackEntityFrom(new EntityDamageSourceIndirect("arrow", this, this.owner).setProjectile(), damage) && (this.owner instanceof EntityPlayer && entity instanceof EntityDragon && ((EntityDragon) entity).getHealth() <= 0.0F || entity instanceof EntityDragonPart && ((EntityDragonPart) entity).entityDragonObj instanceof EntityDragon && ((EntityLivingBase) ((EntityDragonPart) entity).entityDragonObj).getHealth() <= 0.0F))
					IC2.achievements.issueAchievement((EntityPlayer) this.owner, "killDragonMiningLaser");
			}

			this.setDead();
		}
	}

	private boolean hitBlock(BlockPos pos, EnumFacing side)
	{
		LaserEvent.LaserHitsBlockEvent event = new LaserEvent.LaserHitsBlockEvent(this.worldObj, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, pos, side, 0.9F, true, true);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled())
		{
			this.setDead();
			return true;
		}
		else
		{
			this.copyDataFromEvent(event);
			IBlockState state = this.worldObj.getBlockState(event.pos);
			Block block = state.getBlock();
			if (!block.isAir(state, this.worldObj, event.pos) && block != Blocks.GLASS && block != Blocks.GLASS_PANE && block != BlockName.glass.getInstance())
			{
				if (this.worldObj.isRemote)
					return true;
				else
				{
					float hardness = state.getBlockHardness(this.worldObj, event.pos);
					if (hardness < 0.0F)
					{
						this.setDead();
						return true;
					}
					else
					{
						this.power -= hardness / 1.5F;
						if (this.power < 0.0F)
							return true;
						else
    
							if (this.fake.cantBreak(this.worldObj, event.pos))
    

							if (state.getMaterial() != Material.TNT && state.getMaterial() != MaterialIC2TNT.instance)
							{
								if (this.smelt)
									if (state.getMaterial() == Material.WOOD)
										event.dropBlock = false;
									else
										for (ItemStack isa : block.getDrops(this.worldObj, event.pos, state, 0))
										{
											ItemStack is = FurnaceRecipes.instance().getSmeltingResult(isa);
											if (is != null)
											{
												if (StackUtil.placeBlock(is, this.worldObj, event.pos))
												{
													event.removeBlock = false;
													event.dropBlock = false;
												}
												else
												{
													event.dropBlock = false;
													StackUtil.dropAsEntity(this.worldObj, event.pos, is);
												}

												this.power = 0.0F;
											}
										}
							}
							else
								block.onBlockDestroyedByExplosion(this.worldObj, event.pos, new Explosion(this.worldObj, this, event.pos.getX() + 0.5D, event.pos.getY() + 0.5D, event.pos.getZ() + 0.5D, 1.0F, false, true));

							if (event.removeBlock)
							{
								if (event.dropBlock)
									block.dropBlockAsItemWithChance(this.worldObj, event.pos, state, event.dropChance, 0);

								this.worldObj.setBlockToAir(event.pos);
								if (this.worldObj.rand.nextInt(10) == 0 && state.getMaterial().getCanBurn())
									this.worldObj.setBlockState(event.pos, Blocks.FIRE.getDefaultState());
							}

							--this.blockBreaks;
							return true;
						}
					}
				}
			}
			else
				return false;
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    
    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    
    
	}

	void copyDataFromEvent(LaserEvent event)
    
    
    

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
    
    
    
		}
	}
}
