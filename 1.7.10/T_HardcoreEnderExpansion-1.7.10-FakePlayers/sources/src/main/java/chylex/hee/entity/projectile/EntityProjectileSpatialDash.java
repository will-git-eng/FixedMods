package chylex.hee.entity.projectile;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.entity.fx.FXType;
import chylex.hee.mechanics.enhancements.EnhancementEnumHelper;
import chylex.hee.mechanics.enhancements.types.SpatialDashGemEnhancements;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C20Effect;
import chylex.hee.packets.client.C21EffectEntity;
import chylex.hee.packets.client.C22EffectLine;
import chylex.hee.system.achievements.AchievementManager;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.MathUtil;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.hee.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityProjectileSpatialDash extends EntityThrowable
{
	private List<Enum> enhancements = new ArrayList<Enum>();
	private byte ticks;

	    
	public final FakePlayerContainer fake = new FakePlayerContainerEntity(ModUtils.profile, this);
	    

	public EntityProjectileSpatialDash(World world)
	{
		super(world);
	}

	public EntityProjectileSpatialDash(World world, EntityLivingBase thrower, List<Enum> enhancements)
	{
		super(world, thrower);
		this.motionX *= 1.75D;
		this.motionY *= 1.75D;
		this.motionZ *= 1.75D;
		this.enhancements.addAll(enhancements);

		    
		if (thrower instanceof EntityPlayer)
			this.fake.setRealPlayer((EntityPlayer) thrower);
		    
	}

	@SideOnly(Side.CLIENT)
	public EntityProjectileSpatialDash(World world, double x, double y, double z)
	{
		super(world, x, y, z);
	}

	@Override
	public void onUpdate()
	{
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;

		this.onEntityUpdate();

		if (!this.worldObj.isRemote)
		{
			for (int cycles = this.enhancements.contains(SpatialDashGemEnhancements.INSTANT) ? 48 : 1; cycles > 0; cycles--)
			{
				if (++this.ticks > (this.enhancements.contains(SpatialDashGemEnhancements.RANGE) ? 48 : 28))
					this.setDead();

				Vec3 vecPos = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
				Vec3 vecPosWithMotion = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
				Vec3 hitVec;

				MovingObjectPosition mop = this.worldObj.rayTraceBlocks(vecPos, vecPosWithMotion);
				vecPos = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);

				if (mop != null)
					hitVec = Vec3.createVectorHelper(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
				else
					hitVec = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

				Entity finalEntity = null;
				List<Entity> collisionList = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1D, 1D, 1D));
				double minDist = Double.MAX_VALUE, dist;
				EntityLivingBase thrower = this.getThrower();

				for (Entity e : collisionList)
				{
					if (e.canBeCollidedWith() && (e != thrower || this.ticks >= 5))
					{
						AxisAlignedBB aabb = e.boundingBox.expand(0.3F, 0.3F, 0.3F);
						MovingObjectPosition mopTest = aabb.calculateIntercept(vecPos, hitVec);

						if (mopTest != null)
						{
							dist = vecPos.distanceTo(mopTest.hitVec);

							if (dist < minDist)
							{
								finalEntity = e;
								minDist = dist;
							}
						}
					}
				}

				if (finalEntity != null)
					mop = new MovingObjectPosition(finalEntity);

				if (mop != null)
				{
					this.onImpact(mop);
					break;
				}

				this.posX += this.motionX;
				this.posY += this.motionY;
				this.posZ += this.motionZ;
				this.setPosition(this.posX, this.posY, this.posZ);
			}

			PacketPipeline.sendToAllAround(this, 128D, new C22EffectLine(FXType.Line.SPATIAL_DASH_MOVE, this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ, this.posX, this.posY, this.posZ));

		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop)
	{
		if (!this.worldObj.isRemote)
		{
			if (this.getThrower() instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP) this.getThrower();

				PacketPipeline.sendToAllAround(player, 64D, new C21EffectEntity(FXType.Entity.GEM_TELEPORT_FROM, player));

				if (player.playerNetServerHandler.func_147362_b().isChannelOpen() && player.worldObj == this.worldObj)
				{ // OBFUSCATED get network manager
					if (player.isRiding())
						player.mountEntity(null);
					boolean tryAchievement = player.posY <= 0D;

					int x, y, z;

					if (mop.typeOfHit == MovingObjectType.BLOCK)
					{
						x = mop.blockX;
						y = mop.blockY;
						z = mop.blockZ;
					}
					else if (mop.typeOfHit == MovingObjectType.ENTITY)
					{
						x = MathUtil.floor(this.posX);
						y = MathUtil.floor(this.posY);
						z = MathUtil.floor(this.posZ);
					}
					else
					{
						this.setDead();
						return;
					}

					    
					if (this.fake.cantBreak(x, y, z))
					{
						this.setDead();
						return;
					}
					    

					boolean found = false;
					Block block;
					BlockPosM tmpPos = BlockPosM.tmp(x, y, z);

					for (int yTest = y; yTest <= y + 8; yTest++)
					{
						if ((block = tmpPos.setY(yTest).getBlock(this.worldObj)).getBlockHardness(this.worldObj, x, yTest, z) == -1)
							break;

						if (this.canSpawnIn(block, tmpPos.moveUp().getBlock(this.worldObj)))
						{
							    
							if (this.fake.cantBreak(x, yTest, z))
								continue;
							    

							player.setPositionAndUpdate(x + 0.5D, yTest + 0.01D, z + 0.5D);
							found = true;
							break;
						}
					}

					if (!found)
						for (int xTest = x - 1; xTest <= x + 1; xTest++)
						{
							for (int zTest = z - 1; zTest <= z + 1; zTest++)
							{
								for (int yTest = y + 1; yTest <= y + 8; yTest++)
								{
									if ((block = tmpPos.set(xTest, yTest, zTest).getBlock(this.worldObj)).getBlockHardness(this.worldObj, x, yTest, z) == -1)
										break;

									if (this.canSpawnIn(block, tmpPos.moveUp().getBlock(this.worldObj)))
									{
										    
										if (this.fake.cantBreak(xTest, yTest, zTest))
											continue;
										    

										player.setPositionAndUpdate(xTest + 0.5D, yTest + 0.01D, zTest + 0.5D);
										found = true;
										break;
									}
								}
							}
						}

					if (!found)
						player.setPositionAndUpdate(x + 0.5D, y + 0.01D, z + 0.5D);
					if (tryAchievement && BlockPosM.tmp(x, y, z).getBlock(this.worldObj).isOpaqueCube())
						player.addStat(AchievementManager.TP_NEAR_VOID, 1);
					player.fallDistance = 0F;

					PacketPipeline.sendToAllAround(player, 64D, new C20Effect(FXType.Basic.GEM_TELEPORT_TO, player));
				}
			}

			this.setDead();
		}
	}

	@Override
	public void setDead()
	{
		super.setDead();

		if (this.worldObj.isRemote)
			for (int a = 0; a < 25; a++)
			{
				HardcoreEnderExpansion.fx.spatialDashExplode(this);
			}
	}

	private boolean canSpawnIn(Block blockBottom, Block blockTop)
	{
		return (blockBottom.getMaterial() == Material.air || !blockBottom.isOpaqueCube()) && (blockTop.getMaterial() == Material.air || !blockTop.isOpaqueCube());
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		nbt.setByte("tickTimer", this.ticks);
		nbt.setString("enhancements", EnhancementEnumHelper.serialize(this.enhancements));
		nbt.removeTag("inTile");
		nbt.removeTag("shake");

		    
		this.fake.writeToNBT(nbt);
		    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.ticks = nbt.getByte("tickTimer");
		this.enhancements = EnhancementEnumHelper.deserialize("enhancements", SpatialDashGemEnhancements.class);

		    
		this.fake.readFromNBT(nbt);
		    
	}
}
