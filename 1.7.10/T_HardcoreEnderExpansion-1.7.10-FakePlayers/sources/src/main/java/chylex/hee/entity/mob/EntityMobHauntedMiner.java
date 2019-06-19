package chylex.hee.entity.mob;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.entity.projectile.EntityProjectileMinerShot;
import chylex.hee.init.ItemList;
import chylex.hee.item.ItemScorchingPickaxe;
import chylex.hee.mechanics.causatum.CausatumMeters;
import chylex.hee.mechanics.causatum.CausatumUtils;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C07AddPlayerVelocity;
import chylex.hee.packets.client.C08PlaySound;
import chylex.hee.proxy.ModCommonProxy;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.DragonUtil;
import chylex.hee.system.util.MathUtil;
import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.hee.ModUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityMobHauntedMiner extends EntityFlying implements IMob
{
	private static final byte ATTACK_TIMER = 80;
	private static final byte ATTACK_NONE = 0, ATTACK_PROJECTILES = 1, ATTACK_LAVA = 2, ATTACK_BLAST_WAVE = 3;

	private AxisAlignedBB bottomBB = AxisAlignedBB.getBoundingBox(0D, 0D, 0D, 0D, 0D, 0D);
	private EntityLivingBase target;
	private double targetX, targetY, targetZ;
	private byte wanderResetTimer = -120, nextAttackTimer = ATTACK_TIMER, currentAttack = ATTACK_NONE, currentAttackTime;

	private int attackLavaCurrentX, attackLavaCurrentY, attackLavaCurrentZ;
	private byte attackLavaCounter, attackLavaDone;

	public EntityMobHauntedMiner(World world)
	{
		super(world);
		this.setSize(2.2F, 1.7F);
		this.isImmuneToFire = true;
		this.experienceValue = 10;
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(16, ATTACK_NONE);
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(ModCommonProxy.opMobs ? 100D : 85D);
	}

	@Override
	protected void updateEntityActionState()
	{
		if (!this.worldObj.isRemote && this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL)
			this.setDead();

		this.despawnEntity();
		if (this.dead)
			return;

		if (this.target == null)
		{
			if (--this.wanderResetTimer < -120 || this.rand.nextInt(300) == 0 || this.motionX == 0D && this.motionZ == 0D && this.rand.nextInt(20) == 0)
			{
				this.wanderResetTimer = 0;
				BlockPosM tmpPos = BlockPosM.tmp();

				for (int attempt = 0; attempt < 32; attempt++)
				{
					tmpPos.set(this).move(this.rand.nextInt(14) - this.rand.nextInt(14), 0, this.rand.nextInt(14) - this.rand.nextInt(14));

					if (tmpPos.isAir(this.worldObj))
					{
						while (tmpPos.moveDown().isAir(this.worldObj) && Math.abs(this.posY - tmpPos.y) < 10)
						{
						}
						if (Math.abs(this.posY - tmpPos.y) >= 10)
							continue;
					}
					else
					{
						while (!tmpPos.moveUp().isAir(this.worldObj) && Math.abs(this.posY - tmpPos.y) < 10)
						{
						}
						if (Math.abs(this.posY - tmpPos.y) >= 10)
							continue;
					}

					this.targetX = tmpPos.x + this.rand.nextDouble();
					this.targetY = tmpPos.y + this.rand.nextDouble() * 0.2D + 3D;
					this.targetZ = tmpPos.z + this.rand.nextDouble();
					this.wanderResetTimer += 40;
					break;
				}

				this.wanderResetTimer += this.rand.nextInt(40) + 20;
			}

			if (this.rand.nextInt(50) == 0)
			{
				List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(32D, 16D, 32D));

				if (!entities.isEmpty())
				{
					Entity temp = entities.get(this.rand.nextInt(entities.size()));

					if (temp instanceof EntityPlayer)
					{
						if (this.rand.nextInt(6) == 0)
						{
							InventoryPlayer inv = ((EntityPlayer) temp).inventory;
							int foundMiningStuff = 0;

							for (int a = 0; a < inv.mainInventory.length; a += this.rand.nextInt(3) + 1)
							{
								ItemStack is = inv.mainInventory[a];
								if (is == null)
									continue;

								Item item = is.getItem();
								if (item == ItemList.scorching_pickaxe || item instanceof ItemPickaxe)
									foundMiningStuff += 4;
								else if (item == Items.iron_ingot || item == Items.gold_ingot || item == Items.diamond || item == Items.redstone || item == Items.dye && is.getItemDamage() == 4 || item == Items.emerald || item == Items.coal || item == ItemList.end_powder || item == ItemList.igneous_rock || item == ItemList.instability_orb || item == ItemList.stardust)
									foundMiningStuff += 1 + (is.stackSize >> 3);
								else if (item instanceof ItemBlock && ItemScorchingPickaxe.isBlockOre(((ItemBlock) item).field_150939_a))
									foundMiningStuff += 1 + (is.stackSize >> 3);
							}

							if (foundMiningStuff >= 13 + this.rand.nextInt(6))
								this.target = (EntityPlayer) temp;
						}
					}
					else if (temp instanceof EntityLivingBase && !(temp instanceof EntityEnderman) && !temp.isImmuneToFire())
						this.target = (EntityLivingBase) temp;
				}
			}
		}
		else
		{
			this.targetX = this.target.posX;
			this.targetZ = this.target.posZ;
			this.targetY = this.target.posY + 2D;

			if (!this.worldObj.isRemote)
				if (this.currentAttack != ATTACK_NONE)
				{
					boolean hasFinished = false;
					++this.currentAttackTime;

					switch (this.currentAttack)
					{
						case ATTACK_PROJECTILES:
							if (this.currentAttackTime == 50)
							{
								Vec3 look = this.getLookVec();

								look.rotateAroundY(MathUtil.toRad(36F));
								this.worldObj.spawnEntityInWorld(new EntityProjectileMinerShot(this.worldObj, this, this.posX + look.xCoord * 1.5D, this.posY + 0.7D, this.posZ + look.zCoord * 1.5D, this.target));
								look.rotateAroundY(MathUtil.toRad(-72F));
								this.worldObj.spawnEntityInWorld(new EntityProjectileMinerShot(this.worldObj, this, this.posX + look.xCoord * 1.5D, this.posY + 0.7D, this.posZ + look.zCoord * 1.5D, this.target));
								hasFinished = true;

								PacketPipeline.sendToAllAround(this, 64D, new C08PlaySound(C08PlaySound.SPAWN_FIREBALL, this.posX, this.posY, this.posZ, 2F, 1.8F));
							}

							break;

						case ATTACK_LAVA:
							if (this.currentAttackTime % 8 == 0)
							{
								this.currentAttackTime -= 8;

								if (this.attackLavaCounter == 0)
								{
									this.attackLavaCounter = 1;
									BlockPosM testPos = new BlockPosM(), tmpPos = BlockPosM.tmp();

									for (int attempt = 0; attempt < 64; attempt++)
									{
										tmpPos.set(this).move(this.rand.nextInt(5) - this.rand.nextInt(5), 4, this.rand.nextInt(5) - this.rand.nextInt(5));

										for (int yAttempt = 0; yAttempt < 7; yAttempt++)
										{
											if (tmpPos.isAir(this.worldObj) && testPos.set(tmpPos).moveDown().getBlock(this.worldObj).isOpaqueCube())
											{
												this.attackLavaCurrentX = tmpPos.x;
												this.attackLavaCurrentY = tmpPos.y - 2;
												this.attackLavaCurrentZ = tmpPos.z;
												attempt = 65;
												break;
											}
											tmpPos.moveDown();
										}
									}
								}
								else
								{
									BlockPosM tmpPos = BlockPosM.tmp();

									    
									if (ModUtils.canMobGrief(this.worldObj))
										    
										for (int px = -1; px <= 1; px++)
										{
											for (int pz = -1; pz <= 1; pz++)
											{
												if (px == 0 && pz == 0)
													continue;
												tmpPos.set(this.attackLavaCurrentX + px, this.attackLavaCurrentY - 1 + this.attackLavaCounter, this.attackLavaCurrentZ + pz);

												Block block = tmpPos.getBlock(this.worldObj);

												if (block == Blocks.flowing_lava || block == Blocks.lava)
													continue;
												if (!MathUtil.floatEquals(block.getBlockHardness(this.worldObj, tmpPos.x, tmpPos.y, tmpPos.z), -1F))
												{
													tmpPos.setAir(this.worldObj);
													this.worldObj.playAuxSFX(2001, tmpPos.x, tmpPos.y, tmpPos.z, Block.getIdFromBlock(block));
												}
											}
										}

									tmpPos.set(this.attackLavaCurrentX, this.attackLavaCurrentY - 1 + this.attackLavaCounter, this.attackLavaCurrentZ);

									    
									if (ModUtils.canMobGrief(this.worldObj) && !EventUtils.cantBreak(ModUtils.getModFake(this.worldObj), tmpPos.x, tmpPos.y, tmpPos.z))
									    
									{
										tmpPos.setBlock(this.worldObj, Blocks.flowing_lava);
										for (int a = 0; a < 5; a++)
										{
											Blocks.flowing_lava.updateTick(this.worldObj, tmpPos.x, tmpPos.y, tmpPos.z, this.rand);
										}
									}

									if (++this.attackLavaCounter == 6)
									{
										if (++this.attackLavaDone >= 4)
										{
											this.attackLavaDone = 0;
											hasFinished = true;
										}

										this.attackLavaCounter = 0;
										this.attackLavaCurrentX = this.attackLavaCurrentY = this.attackLavaCurrentZ = 0;
									}
								}
							}

							break;

						case ATTACK_BLAST_WAVE:
							if (this.currentAttackTime == 30)
							{
								for (Entity entity : (List<Entity>) this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(12D, 4D, 12D).offset(0D, -2D, 0D)))
								{
									double dist = MathUtil.distance(entity.posX - this.posX, entity.posZ - this.posZ);
									if (dist > 12D)
										continue;

									double[] vec = DragonUtil.getNormalizedVector(entity.posX - this.posX, entity.posZ - this.posZ);
									double strength = 0.4D + (12D - dist) * 0.2D;
									vec[0] *= strength;
									vec[1] *= strength;

									entity.attackEntityFrom(DamageSource.causeMobDamage(this), 13F);
									if (entity instanceof EntityPlayer)
										PacketPipeline.sendToPlayer((EntityPlayer) entity, new C07AddPlayerVelocity(vec[0], 0.4D, vec[1]));

									entity.motionX += vec[0];
									entity.motionY += 0.4D;
									entity.motionZ += vec[1];
								}

								PacketPipeline.sendToAllAround(this, 24D, new C08PlaySound(C08PlaySound.HAUNTEDMINER_ATTACK_BLAST, this.posX, this.posY, this.posZ, 1.5F, 1F));
								BlockPosM testPos = new BlockPosM(), tmpPos = BlockPosM.tmp();

								for (int attempt = 0; attempt < 90; attempt++)
								{
									tmpPos.set(this).move(this.rand.nextInt(21) - 10, -1, this.rand.nextInt(21) - 10);
									if (MathUtil.distance(tmpPos.x - this.posX, tmpPos.z - this.posZ) > 10D)
										continue;

									for (int yAttempt = 0; yAttempt < 4; yAttempt++)
									{
										if (tmpPos.isAir(this.worldObj) && !testPos.set(tmpPos).moveDown().isAir(this.worldObj))
										{
											    
											if (ModUtils.canMobGrief(this.worldObj) && !EventUtils.cantBreak(ModUtils.getModFake(this.worldObj), tmpPos.x, tmpPos.y, tmpPos.z))
												    
												tmpPos.setBlock(this.worldObj, Blocks.fire);
											break;
										}
										--tmpPos.y;
									}
								}

								hasFinished = true;
							}

							break;

						default:
							hasFinished = true;
					}

					if (hasFinished || this.currentAttackTime > 120)
					{
						this.currentAttack = ATTACK_NONE;
						this.nextAttackTimer = (byte) (ATTACK_TIMER - 5 * this.worldObj.difficultySetting.getDifficultyId());
						this.currentAttackTime = 0;
						this.dataWatcher.updateObject(16, (byte) 0);
					}
				}
				else if (--this.nextAttackTimer <= 0)
				{
					this.currentAttack = MathUtil.distance(this.target.posX - this.posX, this.target.posZ - this.posZ) < 7.5D && this.rand.nextInt(3) != 0 || this.rand.nextInt(6) == 0 ? ATTACK_BLAST_WAVE : this.rand.nextInt(4) != 0 ? ATTACK_PROJECTILES : ATTACK_LAVA;
					this.dataWatcher.updateObject(16, this.currentAttack);
				}

			if (this.target.isDead || this.currentAttack == ATTACK_NONE && this.getDistanceToEntity(this.target) > 40D)
			{
				this.target = null;
				if (this.currentAttack != ATTACK_NONE)
					this.dataWatcher.updateObject(16, this.currentAttack = ATTACK_NONE);
			}
		}

		double speed = 0.075D;

		if (this.target != null)
		{
			double dist = this.getDistanceToEntity(this.target);

			if (dist > 13D)
				speed = this.currentAttack == ATTACK_NONE ? 0.2D : 0.06D;
			else if (dist < 9D)
				speed = 0D;
		}

		double[] xz = DragonUtil.getNormalizedVector(this.targetX - this.posX, this.targetZ - this.posZ);
		this.motionX = xz[0] * speed;
		this.motionZ = xz[1] * speed;
		if (Math.abs(this.targetY - this.posY) > 1D)
			this.motionY = (this.targetY - this.posY) * 0.02D;

		if (MathUtil.distance(this.targetX - this.posX, this.targetZ - this.posZ) > 0.1D)
			this.renderYawOffset = this.rotationYaw = this.rotationYawHead = -MathUtil.toDeg((float) Math.atan2(this.targetX - this.posX, this.targetZ - this.posZ));
		else
			this.motionX = this.motionZ = 0D;
	}

	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();

		if (this.worldObj.isRemote)
		{
			for (int a = 0; a < 2; a++)
			{
				HardcoreEnderExpansion.fx.flame(this.worldObj, this.posX + (this.rand.nextDouble() - 0.5D) * 0.2D, this.posY, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.2D, 0D, -0.05D, 0D, 8);
			}

			byte attack = this.dataWatcher.getWatchableObjectByte(16);

			if (attack != ATTACK_NONE && !this.dead)
			{
				this.rotationYaw = this.renderYawOffset = this.rotationYawHead;
				Vec3 look = this.getLookVec();

				look.rotateAroundY(MathUtil.toRad(36F));
				HardcoreEnderExpansion.fx.spell(this.worldObj, this.posX + look.xCoord * 1.5D + (this.rand.nextDouble() - 0.5D) * 0.2D, this.posY + 0.7D, this.posZ + look.zCoord * 1.5D + (this.rand.nextDouble() - 0.5D) * 0.2D, 0.9F, 0.6F, 0F);
				look.rotateAroundY(MathUtil.toRad(-72F));
				HardcoreEnderExpansion.fx.spell(this.worldObj, this.posX + look.xCoord * 1.5D + (this.rand.nextDouble() - 0.5D) * 0.2D, this.posY + 0.7D, this.posZ + look.zCoord * 1.5D + (this.rand.nextDouble() - 0.5D) * 0.2D, 0.9F, 0.6F, 0F);

				++this.currentAttackTime;

				if (attack == ATTACK_BLAST_WAVE)
					if (this.currentAttackTime == 29)
						for (int flame = 0; flame < 180; flame++)
						{
							HardcoreEnderExpansion.fx.flame(this.worldObj, this.posX + (this.rand.nextDouble() - 0.5D) * 0.2D, this.posY + this.height * 0.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * 0.2D, (this.rand.nextDouble() - 0.5D) * 2D, (this.rand.nextDouble() - 0.5D) * 2D, (this.rand.nextDouble() - 0.5D) * 2D, 5 + this.rand.nextInt(20));
						}
			}
			else
				this.currentAttackTime = 0;
		}
		else if (!this.dead)
		{
			List<Entity> nearEntities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.bottomBB.setBounds(this.posX - 1.65D, this.posY - 3, this.posZ - 1.65D, this.posX + 1.65D, this.posY, this.posZ + 1.65D));

			for (Entity entity : nearEntities)
			{
				if (entity instanceof EntityMobHauntedMiner)
					continue;
				entity.attackEntityFrom(DamageSource.causeMobDamage(this), 3F);
				entity.setFire(5);
				entity.hurtResistantTime -= 2;
			}

			if (this.currentAttack != ATTACK_NONE)
				this.rotationYaw = this.rotationYawHead;
		}
	}

	@Override
	public void setRevengeTarget(EntityLivingBase newTarget)
	{
		if (this.target == null || newTarget.getDistanceSqToEntity(this) < this.target.getDistanceSqToEntity(this) && !(newTarget instanceof EntityMobHauntedMiner))
		{
			this.target = newTarget;
			this.nextAttackTimer = ATTACK_TIMER;
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		boolean damaged = super.attackEntityFrom(source, amount);
		Entity sourceEntity = source.getEntity();

		if (damaged && sourceEntity instanceof EntityLivingBase)
		{
			CausatumUtils.increase(source, CausatumMeters.END_MOB_DAMAGE, amount * 0.25F);

			if (!(sourceEntity instanceof EntityMobHauntedMiner))
			{
				this.target = (EntityLivingBase) sourceEntity;
				this.nextAttackTimer = 5;
			}

			if (this.rand.nextInt(7) == 0 || this.getHealth() <= 0F && this.rand.nextInt(3) != 0)
			{
				int maxTargeted = this.worldObj.difficultySetting.getDifficultyId() - 2 + this.rand.nextInt(2);
				List<EntityMobHauntedMiner> nearby = this.worldObj.getEntitiesWithinAABB(EntityMobHauntedMiner.class, this.boundingBox.expand(48D, 30D, 48D)), viable = new ArrayList<EntityMobHauntedMiner>();

				while (!nearby.isEmpty())
				{
					EntityMobHauntedMiner miner = nearby.remove(this.rand.nextInt(nearby.size()));
					if (miner == this)
						continue;

					double dist = this.getDistanceToEntity(miner);

					if (miner.target == null && dist < 16D)
						viable.add(miner);
					else if (miner.target == sourceEntity)
						if (--maxTargeted == 0)
							break;
				}

				if (maxTargeted > 0 && !viable.isEmpty())
					viable.get(this.rand.nextInt(viable.size())).setRevengeTarget((EntityLivingBase) sourceEntity);
			}
		}

		return damaged;
	}

	@Override
	public void knockBack(Entity entity, float damage, double xPower, double zPower)
	{
	}

	@Override
	public void dropFewItems(boolean recentlyHit, int looting)
	{
		for (int a = 0; a < this.rand.nextInt(2 + this.rand.nextInt(2) + looting); a++)
		{
			this.dropItem(ItemList.infernium, 1);
		}
	}

	@Override
	public String getCommandSenderName()
	{
		return this.hasCustomNameTag() ? this.getCustomNameTag() : StatCollector.translateToLocal("entity.hauntedMiner.name");
	}
}
