package chylex.hee.mechanics.charms.handler;

import chylex.hee.entity.fx.FXType;
import chylex.hee.mechanics.charms.CharmPouchInfo;
import chylex.hee.mechanics.charms.CharmRecipe;
import chylex.hee.mechanics.charms.CharmType;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C06SetPlayerVelocity;
import chylex.hee.packets.client.C07AddPlayerVelocity;
import chylex.hee.packets.client.C21EffectEntity;
import chylex.hee.packets.client.C22EffectLine;
import chylex.hee.system.ReflectionPublicizer;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.CollectionUtil;
import chylex.hee.system.util.DragonUtil;
import chylex.hee.system.util.MathUtil;
import ru.will.git.reflectionmedic.util.EventUtils;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent.Finish;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class CharmEvents
{
	private final TObjectByteHashMap<UUID> playerRegen = new TObjectByteHashMap();
	private final TObjectFloatHashMap<UUID> playerSpeed = new TObjectFloatHashMap();
	private final TObjectFloatHashMap<UUID> playerStealDealtDamage = new TObjectFloatHashMap();
	private final TObjectByteHashMap<UUID> playerLastResortCooldown = new TObjectByteHashMap();
	private final AttributeModifier attrSpeed = new AttributeModifier(UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"), "HeeCharmSpeed", 0.15D, 2);

	public static float[] getProp(EntityPlayer player, String prop)
	{
		CharmPouchInfo info = CharmPouchHandler.getActivePouch(player);
		if (info == null)
			return ArrayUtils.EMPTY_FLOAT_ARRAY;
		else
		{
			TFloatArrayList values = new TFloatArrayList(5);

			for (Pair<CharmType, CharmRecipe> entry : info.charms)
			{
				float value = entry.getRight().getProp(prop);
				if (value != -1.0F)
					values.add(value);
			}

			return values.toArray();
		}
	}

	public static float getPropSummed(EntityPlayer player, String prop)
	{
		float finalValue = 0.0F;

		for (float val : getProp(player, prop))
		{
			finalValue += val;
		}

		return finalValue;
	}

	public static float getPropPercentIncrease(EntityPlayer player, String prop, float baseValue)
	{
		float finalValue = 0.0F;

		for (float val : getProp(player, prop))
		{
			finalValue += val * baseValue - baseValue;
		}

		return finalValue;
	}

	public static float getPropPercentDecrease(EntityPlayer player, String prop, float baseValue)
	{
		float finalValue = 0.0F;

		for (float val : getProp(player, prop))
		{
			float tmp = baseValue * val;
			finalValue += tmp;
			baseValue -= tmp;
		}

		return finalValue;
	}

	public void onDisabled()
	{
		if (!this.playerSpeed.isEmpty())
			for (EntityPlayerMP player : (Iterable<EntityPlayerMP>) MinecraftServer.getServer().getConfigurationManager().playerEntityList)
			{
				UUID id = player.getGameProfile().getId();
				if (this.playerSpeed.containsKey(id))
				{
					IAttributeInstance attribute = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.movementSpeed);
					if (attribute != null)
						attribute.removeModifier(this.attrSpeed);
				}
			}

		this.playerRegen.clear();
		this.playerSpeed.clear();
		this.playerStealDealtDamage.clear();
		this.playerLastResortCooldown.clear();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerTick(PlayerTickEvent e)
	{
		if (e.side == Side.SERVER)
			if (e.phase == Phase.START)
			{
				CharmPouchInfo info = CharmPouchHandler.getActivePouch(e.player);
				if (info != null && info.isIdle(e.player.worldObj))
					CharmPouchHandler.setActivePouch(e.player, null);

				UUID playerID = e.player.getGameProfile().getId();
				float spd = getPropSummed(e.player, "spd");
				float prevSpd = this.playerSpeed.get(playerID);
				if (MathUtil.floatEquals(prevSpd, this.playerSpeed.getNoEntryValue()) || !MathUtil.floatEquals(prevSpd, spd))
				{
					IAttributeInstance attribute = e.player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.movementSpeed);
					if (attribute != null)
					{
						attribute.removeModifier(this.attrSpeed);
						attribute.applyModifier(new AttributeModifier(this.attrSpeed.getID(), this.attrSpeed.getName() + spd, this.attrSpeed.getAmount() * spd, this.attrSpeed.getOperation()));
					}

					this.playerSpeed.put(playerID, spd);
				}

				if (e.player.shouldHeal() && e.player.getFoodStats().getFoodLevel() >= 18)
				{
					float regen = getPropPercentDecrease(e.player, "regenspd", 90.0F);
					if (regen > 0.0F && this.playerRegen.adjustOrPutValue(playerID, (byte) 1, (byte) 0) >= 100.0F - regen)
					{
						e.player.heal(1.0F);
						this.playerRegen.put(playerID, (byte) 0);
					}
				}

				if (this.playerLastResortCooldown.containsKey(playerID) && this.playerLastResortCooldown.adjustOrPutValue(playerID, (byte) -1, (byte) -100) <= -100)
					this.playerLastResortCooldown.remove(playerID);
			}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingHurt(LivingHurtEvent event)
	{
		boolean isTargetPlayer = event.entityLiving instanceof EntityPlayer;
		DamageSource damageSource = event.source;
		Entity source = damageSource.getEntity();
		if (source == null)
		{
			if (isTargetPlayer)
			{
				EntityPlayer targetPlayer = (EntityPlayer) event.entity;
				if (damageSource == DamageSource.fall)
				{
					event.ammount -= getPropSummed(targetPlayer, "fallblocks") * 0.5F;
					if (event.ammount <= 0.001F)
						event.ammount = 0.0F;
				}
				else if (damageSource.isMagicDamage())
					event.ammount -= getPropPercentDecrease(targetPlayer, "reducemagicdmg", event.ammount);
			}
		}
		else
		{
			boolean isSourcePlayer = source instanceof EntityPlayer;
			if (isSourcePlayer)
			{
				EntityPlayer sourcePlayer = (EntityPlayer) source;

				    
				if (EventUtils.cantDamage(sourcePlayer, event.entity))
					return;
				    

				event.ammount += getPropPercentIncrease(sourcePlayer, "dmg", event.ammount);
				float[] crit = getProp(sourcePlayer, "critchance");
				if (crit.length > 0)
				{
					float[] critDmg = getProp(sourcePlayer, "critdmg");
					float val = 0.0F;

					for (int a = 0; a < crit.length; ++a)
					{
						if (event.entity.worldObj.rand.nextFloat() < crit[a])
							val += critDmg[a] * event.ammount - event.ammount;
					}

					if (val > 0.0F)
					{
						event.ammount += val;
						PacketPipeline.sendToAllAround(event.entity, 64.0D, new C21EffectEntity(FXType.Entity.CHARM_CRITICAL, event.entity));
					}
				}

				float[] badEff = getProp(sourcePlayer, "badeffchance");
				if (badEff.length > 0)
				{
					float[] badEffLvl = getProp(sourcePlayer, "badefflvl");
					float[] badEffTime = getProp(sourcePlayer, "badefftime");
					boolean causedEffect = false;
					List<Potion> potionEffects = CollectionUtil.newList(Potion.weakness, Potion.moveSlowdown, Potion.blindness, Potion.poison, null);

					for (int a = 0; a < badEff.length && !potionEffects.isEmpty(); ++a)
					{
						if (event.entity.worldObj.rand.nextFloat() < badEff[a])
						{
							Potion type = potionEffects.remove(event.entity.worldObj.rand.nextInt(potionEffects.size()));
							if (type == null)
								event.entity.setFire((int) badEffTime[a]);
							else
								event.entityLiving.addPotionEffect(new PotionEffect(type.id, 20 * (int) badEffTime[a], (int) badEffLvl[a] - 1));

							causedEffect = true;
						}
					}

					if (causedEffect)
						PacketPipeline.sendToAllAround(event.entity, 64.0D, new C21EffectEntity(FXType.Entity.CHARM_WITCH, event.entity));
				}

				float magic = getPropPercentDecrease(sourcePlayer, "dmgtomagic", event.ammount);
				if (magic > 0.001F)
				{
					event.ammount -= magic;
					event.entity.hurtResistantTime = 0;
					event.entity.attackEntityFrom(DamageSource.magic, magic);
				}
			}

			if (isTargetPlayer)
			{
				EntityPlayer targetPlayer = (EntityPlayer) event.entityLiving;
				event.ammount -= getPropPercentDecrease(targetPlayer, "reducedmg", event.ammount);
				if (targetPlayer.isBlocking())
				{
					event.ammount -= getPropPercentDecrease(targetPlayer, "reducedmgblock", event.ammount);
					boolean showBlockingEffect = false;
					float[] reflectDmg = getProp(targetPlayer, "blockreflectdmg");
					if (reflectDmg.length > 0)
					{
						float reflected = 0.0F;

						for (int a = 0; a < reflectDmg.length; ++a)
						{
							reflected += event.ammount * reflectDmg[a];
						}

						source.attackEntityFrom(DamageSource.causePlayerDamage(targetPlayer), reflected);
						showBlockingEffect = true;
					}

					float repulseAmt = getPropSummed(targetPlayer, "blockrepulsepower");
					if (repulseAmt > 0.001F)
					{
						float mp = 0.5F + 0.8F * repulseAmt;
						double[] vec = DragonUtil.getNormalizedVector(source.posX - targetPlayer.posX, source.posZ - targetPlayer.posZ);
						vec[0] *= mp;
						vec[1] *= mp;
						if (source instanceof EntityPlayer)
						{
							PacketPipeline.sendToPlayer((EntityPlayer) source, new C07AddPlayerVelocity(vec[0], 0.25D, vec[1]));
							source.motionX += vec[0];
							source.motionY += 0.25D;
							source.motionZ += vec[1];
						}
						else
							source.addVelocity(vec[0], 0.25D, vec[1]);

						showBlockingEffect = true;
					}

					if (showBlockingEffect)
						PacketPipeline.sendToAllAround(event.entity, 64.0D, new C21EffectEntity(FXType.Entity.CHARM_BLOCK_EFFECT, event.entity));
				}

				float[] redirMobs = getProp(targetPlayer, "redirmobs");
				if (redirMobs.length > 0)
				{
					float[] redirAmt = getProp(targetPlayer, "rediramt");
					List<EntityLivingBase> nearbyEntities = event.entity.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, targetPlayer.boundingBox.expand(6.0D, 3.0D, 6.0D));
					Iterator<EntityLivingBase> iter = nearbyEntities.iterator();

					for (int a = 0; a < redirMobs.length; ++a)
					{
						for (int mob = 0; mob < Math.round(redirMobs[a]); ++mob)
						{
							while (iter.hasNext())
							{
								EntityLivingBase entity = iter.next();
								if (entity != targetPlayer && entity != source)
								{
									    
									if (EventUtils.cantDamage(targetPlayer, entity))
										continue;
									    

									entity.attackEntityFrom(DamageSource.causePlayerDamage(targetPlayer), redirAmt[a] * event.ammount);
									PacketPipeline.sendToAllAround(targetPlayer, 64.0D, new C22EffectLine(FXType.Line.CHARM_DAMAGE_REDIRECTION, entity, targetPlayer));
									event.ammount -= redirAmt[a];
									break;
								}
							}
						}
					}
				}
			}

			if (isSourcePlayer)
			{
				EntityPlayer sourcePlayer = (EntityPlayer) source;
				float[] stealHealth = getProp(sourcePlayer, "stealhealth");
				if (stealHealth.length > 0)
				{
					float[] stealDealt = getProp(sourcePlayer, "stealdealt");
					int randIndex = sourcePlayer.worldObj.rand.nextInt(stealHealth.length);
					if (this.playerStealDealtDamage.adjustOrPutValue(sourcePlayer.getGameProfile().getId(), event.ammount, event.ammount) >= stealDealt[randIndex])
					{
						sourcePlayer.heal(stealHealth[randIndex]);
						this.playerStealDealtDamage.adjustValue(sourcePlayer.getGameProfile().getId(), -event.ammount);
					}
				}
			}
		}

	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDeath(LivingDeathEvent event)
	{
		if (event.entity instanceof EntityPlayer)
		{
			EntityPlayer targetPlayer = (EntityPlayer) event.entity;
			float[] lastResortCooldown = getProp(targetPlayer, "lastresortcooldown");
			if (lastResortCooldown.length > 0 && !this.playerLastResortCooldown.containsKey(targetPlayer.getGameProfile().getId()))
			{
				float[] lastResortDist = getProp(targetPlayer, "lastresortblocks");
				int randIndex = targetPlayer.worldObj.rand.nextInt(lastResortCooldown.length);
				BlockPosM tmpPos = BlockPosM.tmp();

				for (int attempt = 0; attempt < 128; ++attempt)
				{
					float ang = targetPlayer.worldObj.rand.nextFloat() * 2.0F * 3.1415927F;
					tmpPos.x = MathUtil.floor(targetPlayer.posX + MathHelper.cos(ang) * lastResortDist[randIndex]);
					tmpPos.y = MathUtil.floor(targetPlayer.posY) - 2;
					tmpPos.z = MathUtil.floor(targetPlayer.posZ + MathHelper.sin(ang) * lastResortDist[randIndex]);
					int yAttempt = 0;

					for (int origY = tmpPos.y; yAttempt <= 6; ++yAttempt)
					{
						if (!tmpPos.setY(origY - 1).isAir(targetPlayer.worldObj) && tmpPos.setY(origY).isAir(targetPlayer.worldObj) && tmpPos.setY(origY + 1).isAir(targetPlayer.worldObj))
						{
							PacketPipeline.sendToAllAround(targetPlayer, 64.0D, new C21EffectEntity(FXType.Entity.CHARM_LAST_RESORT, targetPlayer));
							targetPlayer.setPositionAndUpdate(tmpPos.x + 0.5D, tmpPos.y + 0.01D, tmpPos.z + 0.5D);
							attempt = 129;
							break;
						}
					}
				}

				targetPlayer.setHealth(targetPlayer.prevHealth);
				targetPlayer.motionX = targetPlayer.motionY = targetPlayer.motionZ = 0.0D;
				this.playerLastResortCooldown.put(targetPlayer.getGameProfile().getId(), (byte) (int) (-100.0F + lastResortCooldown[randIndex] * 20.0F));
				PacketPipeline.sendToPlayer(targetPlayer, new C06SetPlayerVelocity(0.0D, 0.0D, 0.0D));
				PacketPipeline.sendToAllAround(targetPlayer, 64.0D, new C21EffectEntity(FXType.Entity.CHARM_LAST_RESORT, targetPlayer));
				event.setCanceled(true);
				return;
			}
		}

		Entity source = event.source.getEntity();
		if (source instanceof EntityPlayer)
		{
			EntityPlayer sourcePlayer = (EntityPlayer) source;
			float[] impactRad = getProp(sourcePlayer, "impactrad");
			if (impactRad.length > 0)
			{
				float[] impactAmt = getProp(sourcePlayer, "impactamt");
				float lastDamage = event.entityLiving.lastDamage;

				for (int a = 0; a < impactRad.length; ++a)
				{
					for (EntityLivingBase entity : (Iterable<EntityLivingBase>) event.entity.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, event.entity.boundingBox.expand(impactRad[a], impactRad[a], impactRad[a])))
					{
						if (entity != sourcePlayer && entity != event.entity && (!(entity instanceof EntityPlayer) || sourcePlayer.canAttackPlayer((EntityPlayer) entity)) && entity.getDistanceToEntity(event.entity) <= impactRad[a])
						{
							    
							if (EventUtils.cantDamage(sourcePlayer, entity))
								continue;
							    

							entity.attackEntityFrom(DamageSource.generic, impactAmt[a] * lastDamage);
							PacketPipeline.sendToAllAround(event.entity, 64.0D, new C22EffectLine(FXType.Line.CHARM_SLAUGHTER_IMPACT, entity, event.entity));
						}
					}
				}
			}
		}

	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDrops(LivingDropsEvent e)
	{
		if (e.recentlyHit && e.source.getEntity() instanceof EntityPlayer && e.entityLiving instanceof EntityLiving && !e.entityLiving.isChild() && e.entity.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
		{
			int xp = (Integer) ReflectionPublicizer.invoke(ReflectionPublicizer.entityLivingBaseGetExperiencePoints, e.entityLiving, new Object[] { e.source.getEntity() });
			xp = MathUtil.ceil(getPropPercentIncrease((EntityPlayer) e.source.getEntity(), "exp", (float) xp));
			DragonUtil.spawnXP(e.entity, xp);
		}

	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBlockBreak(BreakEvent e)
	{
		if (e.getPlayer() != null)
			e.setExpToDrop(e.getExpToDrop() + MathUtil.ceil(getPropPercentIncrease(e.getPlayer(), "exp", e.getExpToDrop())));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemDestroyed(PlayerDestroyItemEvent e)
	{
		if (e.entity != null && e.entity.worldObj != null && !e.entity.worldObj.isRemote)
			if (e.original.isItemStackDamageable() && e.original.getItem().isRepairable())
			{
				float[] repair = getProp(e.entityPlayer, "recdurabilitychance");
				if (repair.length > 0)
				{
					float[] repairAmt = getProp(e.entityPlayer, "recdurabilityamt");
					float toRepair = 0.0F;

					for (int a = 0; a < repair.length; ++a)
					{
						if (e.entity.worldObj.rand.nextFloat() < repair[a])
							toRepair += repairAmt[a];
					}

					if (MathUtil.floatEquals(toRepair, 0.0F))
						return;

					ItemStack newIS = e.original.copy();
					newIS.stackSize = 1;
					newIS.setItemDamage(newIS.getMaxDamage() - MathUtil.floor(newIS.getMaxDamage() * Math.min(1.0F, toRepair)));
					EntityItem newItem = new EntityItem(e.entity.worldObj, e.entity.posX, e.entity.posY + e.entityPlayer.getEyeHeight() - 0.3D, e.entity.posZ, newIS);
					newItem.delayBeforeCanPickup = 40;
					float power = 0.3F;
					float yawRadians = (float) Math.toRadians(e.entityPlayer.rotationYaw);
					float randomAngle = e.entity.worldObj.rand.nextFloat() * 3.1415927F * 2.0F;
					newItem.motionX = -MathHelper.sin(yawRadians) * MathHelper.cos(yawRadians) * power;
					newItem.motionZ = MathHelper.cos(yawRadians) * MathHelper.cos(yawRadians) * power;
					newItem.motionY = -MathHelper.sin((float) Math.toRadians(e.entity.rotationPitch)) * power + 0.1F;
					power = 0.02F * e.entity.worldObj.rand.nextFloat();
					newItem.motionX += MathHelper.cos(randomAngle) * power;
					newItem.motionY += (e.entity.worldObj.rand.nextFloat() - e.entity.worldObj.rand.nextFloat()) * 0.1F;
					newItem.motionZ += MathHelper.sin(randomAngle) * power;
					e.entity.worldObj.spawnEntityInWorld(newItem);
				}
			}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerFinishUsingItem(Finish e)
	{
		if (!e.entity.worldObj.isRemote)
			if (e.item.getItemUseAction() == EnumAction.eat && e.item.getItem() instanceof ItemFood)
			{
				int hungerRecovered = ((ItemFood) e.item.getItem()).func_150905_g(e.item);
				float healthRecovered = getPropPercentIncrease(e.entityPlayer, "healthperhunger", hungerRecovered);
				if (healthRecovered > 0.0F)
					e.entityPlayer.heal(healthRecovered);
			}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBreakSpeed(BreakSpeed e)
	{
		if (!e.entity.worldObj.isRemote)
			e.newSpeed += getPropPercentIncrease(e.entityPlayer, "breakspd", e.originalSpeed);

	}
}
