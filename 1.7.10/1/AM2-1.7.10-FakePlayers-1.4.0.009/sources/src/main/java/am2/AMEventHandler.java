package am2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ru.will.git.am2.EventConfig;

import am2.api.ArsMagicaApi;
import am2.api.events.ManaCostEvent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.ContingencyTypes;
import am2.armor.ArmorHelper;
import am2.armor.infusions.GenericImbuement;
import am2.blocks.BlocksCommonProxy;
import am2.blocks.tileentities.TileEntityAstralBarrier;
import am2.bosses.BossSpawnHelper;
import am2.buffs.BuffEffectTemporalAnchor;
import am2.buffs.BuffList;
import am2.buffs.BuffStatModifiers;
import am2.damage.DamageSources;
import am2.entities.EntityFlicker;
import am2.items.ItemOre;
import am2.items.ItemsCommonProxy;
import am2.network.AMNetHandler;
import am2.playerextensions.AffinityData;
import am2.playerextensions.ExtendedProperties;
import am2.playerextensions.RiftStorage;
import am2.playerextensions.SkillData;
import am2.utility.DimensionUtilities;
import am2.utility.EntityUtilities;
import am2.utility.InventoryUtilities;
import am2.utility.KeystoneUtilities;
import am2.utility.MathUtilities;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.brewing.PotionBrewedEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AchievementEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;

public class AMEventHandler
{
	@SubscribeEvent
	public void onPotionBrewed(PotionBrewedEvent brewEvent)
	{
		for (ItemStack stack : brewEvent.brewingStacks)
			if (stack != null && stack.getItem() instanceof ItemPotion)
			{
				ItemPotion ptn = (ItemPotion) stack.getItem();
				List<PotionEffect> fx = ptn.getEffects(stack.getItemDamage());
				if (fx == null)
					return;

				for (PotionEffect pe : fx)
				{
					if (pe.getPotionID() == BuffList.greaterManaPotion.id)
					{
						stack = InventoryUtilities.replaceItem(stack, ItemsCommonProxy.greaterManaPotion);
						break;
					}

					if (pe.getPotionID() == BuffList.epicManaPotion.id)
					{
						stack = InventoryUtilities.replaceItem(stack, ItemsCommonProxy.epicManaPotion);
						break;
					}

					if (pe.getPotionID() == BuffList.legendaryManaPotion.id)
					{
						stack = InventoryUtilities.replaceItem(stack, ItemsCommonProxy.legendaryManaPotion);
						break;
					}
				}
			}

	}

	@SubscribeEvent
	public void onEndermanTeleport(EnderTeleportEvent event)
	{
		EntityLivingBase ent = event.entityLiving;
		ArrayList<Long> keystoneKeys = KeystoneUtilities.instance.GetKeysInInvenory(ent);
		TileEntityAstralBarrier blockingBarrier = DimensionUtilities.GetBlockingAstralBarrier(event.entityLiving.worldObj, (int) event.targetX, (int) event.targetY, (int) event.targetZ, keystoneKeys);
		if (!ent.isPotionActive(BuffList.astralDistortion.id) && blockingBarrier == null)
		{
			if (!ent.worldObj.isRemote && ent instanceof EntityEnderman && ent.worldObj.rand.nextDouble() < 0.009999999776482582D)
			{
				EntityFlicker flicker = new EntityFlicker(ent.worldObj);
				flicker.setPosition(ent.posX, ent.posY, ent.posZ);
				flicker.setFlickerType(Affinity.ENDER);
				ent.worldObj.spawnEntityInWorld(flicker);
			}

		}
		else
		{
			event.setCanceled(true);
			if (blockingBarrier != null)
				blockingBarrier.onEntityBlocked(ent);

		}
	}

	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if (event.entity instanceof EntityLivingBase)
		{
			event.entity.registerExtendedProperties("ArsMagicaExProps", new ExtendedProperties());
			((EntityLivingBase) event.entity).getAttributeMap().registerAttribute(ArsMagicaApi.maxManaBonus);
			((EntityLivingBase) event.entity).getAttributeMap().registerAttribute(ArsMagicaApi.maxBurnoutBonus);
			((EntityLivingBase) event.entity).getAttributeMap().registerAttribute(ArsMagicaApi.xpGainModifier);
			((EntityLivingBase) event.entity).getAttributeMap().registerAttribute(ArsMagicaApi.burnoutReductionRate);
			((EntityLivingBase) event.entity).getAttributeMap().registerAttribute(ArsMagicaApi.manaRegenTimeModifier);
			if (event.entity instanceof EntityPlayer)
			{
				event.entity.registerExtendedProperties("ArsMagicaVoidStorage", new RiftStorage());
				event.entity.registerExtendedProperties("AffinityData", new AffinityData());
				event.entity.registerExtendedProperties("SpellKnowledgeData", new SkillData((EntityPlayer) event.entity));
			}
		}
		else if (event.entity instanceof EntityItemFrame)
			AMCore.proxy.itemFrameWatcher.startWatchingFrame((EntityItemFrame) event.entity);

	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityDeath(LivingDeathEvent event)
	{
		EntityLivingBase soonToBeDead = event.entityLiving;
		if (soonToBeDead.isPotionActive(BuffList.temporalAnchor.id))
		{
			event.setCanceled(true);
			PotionEffect pe = soonToBeDead.getActivePotionEffect(BuffList.temporalAnchor);
			if (pe instanceof BuffEffectTemporalAnchor)
			{
				BuffEffectTemporalAnchor buff = (BuffEffectTemporalAnchor) pe;
				buff.stopEffect(soonToBeDead);
			}

			soonToBeDead.removePotionEffect(BuffList.temporalAnchor.id);
		}
		else
		{
			if (ExtendedProperties.For(soonToBeDead).getContingencyType() == ContingencyTypes.DEATH)
				ExtendedProperties.For(soonToBeDead).procContingency();

			if (soonToBeDead instanceof EntityPlayer)
				AMCore.proxy.playerTracker.onPlayerDeath((EntityPlayer) soonToBeDead);
			else if (soonToBeDead instanceof EntityCreature && !EntityUtilities.isSummon(soonToBeDead) && EntityUtilities.isAIEnabled((EntityCreature) soonToBeDead) && event.source.getSourceOfDamage() instanceof EntityPlayer)
				EntityUtilities.handleCrystalPhialAdd((EntityCreature) soonToBeDead, (EntityPlayer) event.source.getSourceOfDamage());

			if (EntityUtilities.isSummon(soonToBeDead))
			{
				ReflectionHelper.setPrivateValue(EntityLivingBase.class, soonToBeDead, Integer.valueOf(0), new String[] { "field_70718_bc", "recentlyHit" });
				int ownerID = EntityUtilities.getOwner(soonToBeDead);
				Entity e = soonToBeDead.worldObj.getEntityByID(ownerID);
				if (e != null & e instanceof EntityLivingBase)
					ExtendedProperties.For((EntityLivingBase) e).removeSummon();
			}

			if (soonToBeDead instanceof EntityVillager && ((EntityVillager) soonToBeDead).isChild())
				BossSpawnHelper.instance.onVillagerChildKilled((EntityVillager) soonToBeDead);

		}
	}

	@SubscribeEvent
	public void onPlayerGetAchievement(AchievementEvent event)
	{
		if (!event.entityPlayer.worldObj.isRemote && event.achievement == AchievementList.theEnd2)
		{
			AMCore var10000 = AMCore.instance;
			PlayerTracker var2 = AMCore.proxy.playerTracker;
			PlayerTracker.storeExtendedPropertiesForRespawn(event.entityPlayer);
		}

	}

	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event)
	{
		if (EntityUtilities.isSummon(event.entityLiving) && !(event.entityLiving instanceof EntityHorse))
			event.setCanceled(true);

		if (event.source == DamageSources.darkNexus)
			event.setCanceled(true);

		if (!event.entityLiving.worldObj.isRemote && event.entityLiving instanceof EntityPig && event.entityLiving.getRNG().nextDouble() < 0.30000001192092896D)
		{
			EntityItem animalFat = new EntityItem(event.entityLiving.worldObj);
			ItemOre var10004 = ItemsCommonProxy.itemOre;
			ItemStack stack = new ItemStack(ItemsCommonProxy.itemOre, 1, 8);
			animalFat.setPosition(event.entity.posX, event.entity.posY, event.entity.posZ);
			animalFat.setEntityItemStack(stack);
			event.drops.add(animalFat);
		}

	}

	@SubscribeEvent
	public void onEntityJump(LivingJumpEvent event)
	{
		if (event.entityLiving.isPotionActive(BuffList.agility.id))
			event.entityLiving.motionY *= 1.5D;

		if (event.entityLiving.isPotionActive(BuffList.leap.id))
		{
			Entity velocityTarget = event.entityLiving;
			if (event.entityLiving.ridingEntity != null)
			{
				if (event.entityLiving.ridingEntity instanceof EntityMinecart)
					event.entityLiving.ridingEntity.setPosition(event.entityLiving.ridingEntity.posX, event.entityLiving.ridingEntity.posY + 1.5D, event.entityLiving.ridingEntity.posZ);

				velocityTarget = event.entityLiving.ridingEntity;
			}

			double yVelocity = 0.0D;
			double xVelocity = 0.0D;
			double zVelocity = 0.0D;
			Vec3 vec = event.entityLiving.getLookVec().normalize();
			switch (event.entityLiving.getActivePotionEffect(BuffList.leap).getAmplifier() + 1)
			{
				case 0:
					yVelocity = 0.4D;
					xVelocity = velocityTarget.motionX * 1.08D * Math.abs(vec.xCoord);
					zVelocity = velocityTarget.motionZ * 1.08D * Math.abs(vec.zCoord);
					break;
				case 1:
					yVelocity = 0.7D;
					xVelocity = velocityTarget.motionX * 1.25D * Math.abs(vec.xCoord);
					zVelocity = velocityTarget.motionZ * 1.25D * Math.abs(vec.zCoord);
					break;
				case 2:
					yVelocity = 1.0D;
					xVelocity = velocityTarget.motionX * 1.75D * Math.abs(vec.xCoord);
					zVelocity = velocityTarget.motionZ * 1.75D * Math.abs(vec.zCoord);
			}

			float maxHorizontalVelocity = 1.45F;
			if (event.entityLiving.ridingEntity != null && (event.entityLiving.ridingEntity instanceof EntityMinecart || event.entityLiving.ridingEntity instanceof EntityBoat) || event.entityLiving.isPotionActive(BuffList.haste.id))
			{
				maxHorizontalVelocity += 25.0F;
				xVelocity *= 2.5D;
				zVelocity *= 2.5D;
			}

			if (xVelocity > maxHorizontalVelocity)
				xVelocity = maxHorizontalVelocity;
			else if (xVelocity < -maxHorizontalVelocity)
				xVelocity = -maxHorizontalVelocity;

			if (zVelocity > maxHorizontalVelocity)
				zVelocity = maxHorizontalVelocity;
			else if (zVelocity < -maxHorizontalVelocity)
				zVelocity = -maxHorizontalVelocity;

			if (ExtendedProperties.For(event.entityLiving).getIsFlipped())
				yVelocity *= -1.0D;

			velocityTarget.addVelocity(xVelocity, yVelocity, zVelocity);
		}

		if (event.entityLiving.isPotionActive(BuffList.entangled.id))
			event.entityLiving.motionY = 0.0D;

		if (event.entityLiving instanceof EntityPlayer)
		{
			ItemStack boots = ((EntityPlayer) event.entityLiving).inventory.armorInventory[0];
			if (boots != null && boots.getItem() == ItemsCommonProxy.enderBoots && event.entityLiving.isSneaking())
				ExtendedProperties.For(event.entityLiving).toggleFlipped();

			if (ExtendedProperties.For(event.entityLiving).getFlipRotation() > 0.0F)
				((EntityPlayer) event.entityLiving).addVelocity(0.0D, -2.0D * event.entityLiving.motionY, 0.0D);
		}

	}

	@SubscribeEvent
	public void onEntityFall(LivingFallEvent event)
	{
		EntityLivingBase ent = event.entityLiving;
		float f = event.distance;
		ent.isAirBorne = false;
		if (!ent.isPotionActive(BuffList.slowfall.id) && !ent.isPotionActive(BuffList.shrink.id) && (!(ent instanceof EntityPlayer) || AffinityData.For(ent).getAffinityDepth(Affinity.NATURE) != 1.0F))
		{
			if (ent.isPotionActive(BuffList.gravityWell.id))
				ent.fallDistance *= 1.5F;

			f = f - ExtendedProperties.For(ent).getFallProtection();
			ExtendedProperties.For(ent).setFallProtection(0);
			if (f <= 0.0F)
			{
				ent.fallDistance = 0.0F;
				event.setCanceled(true);
			}
		}
		else
		{
			event.setCanceled(true);
			ent.fallDistance = 0.0F;
		}
	}

	@SubscribeEvent
	public void onEntityLiving(LivingUpdateEvent event)
	{
		EntityLivingBase ent = event.entityLiving;
		World world = ent.worldObj;
		BuffStatModifiers.instance.applyStatModifiersBasedOnBuffs(ent);
		ExtendedProperties extendedProperties = ExtendedProperties.For(ent);
		extendedProperties.handleSpecialSyncData();
		extendedProperties.manaBurnoutTick();
		if (ent instanceof EntityPlayer)
		{
			if (ent.worldObj.isRemote)
			{
				int divisor = extendedProperties.getAuraDelay() > 0 ? extendedProperties.getAuraDelay() : 1;
				if (ent.ticksExisted % divisor == 0)
				{
					AMCore var10000 = AMCore.instance;
					AMCore.proxy.particleManager.spawnAuraParticles(ent);
				}

				AMCore.proxy.setViewSettings();
			}

			ArmorHelper.HandleArmorInfusion((EntityPlayer) ent);
			ArmorHelper.HandleArmorEffects((EntityPlayer) ent, world);
			extendedProperties.flipTick();
			if (extendedProperties.getIsFlipped())
			{
				if (((EntityPlayer) ent).motionY < 2.0D)
					((EntityPlayer) ent).motionY += 0.15000000596046448D;

				double posY = ent.posY + ent.height;
				if (!world.isRemote)
					posY += ent.getEyeHeight();

				if (world.rayTraceBlocks(Vec3.createVectorHelper(ent.posX, posY, ent.posZ), Vec3.createVectorHelper(ent.posX, posY + 1.0D, ent.posZ), true) != null)
				{
					if (!ent.onGround && ent.fallDistance > 0.0F)
					{
						try
						{
							Method m = ReflectionHelper.findMethod(Entity.class, ent, new String[] { "func_70069_a", "fall" }, new Class[] { Float.TYPE });
							m.setAccessible(true);
							m.invoke(ent, new Object[] { Float.valueOf(ent.fallDistance) });
						}
						catch (Throwable var8)
						{
							var8.printStackTrace();
						}

						ent.fallDistance = 0.0F;
					}

					ent.onGround = true;
				}
				else
				{
					if (ent.motionY > 0.0D)
						if (world.isRemote)
							ent.fallDistance = (float) (ent.fallDistance + (ent.posY - ent.prevPosY));
						else
							ent.fallDistance = (float) (ent.fallDistance + (((EntityPlayer) ent).field_71095_bQ - ((EntityPlayer) ent).field_71096_bN) * 2.0D);

					ent.onGround = false;
				}
			}

			if (ArmorHelper.isInfusionPreset(((EntityPlayer) ent).getCurrentArmor(1), "step_up"))
				ent.stepHeight = 1.0111F;
			else if (ent.stepHeight == 1.0111F)
				ent.stepHeight = 0.5F;

			IAttributeInstance attr = ent.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
			if (ArmorHelper.isInfusionPreset(((EntityPlayer) ent).getCurrentArmor(0), "run_spd"))
			{
				if (attr.getModifier(GenericImbuement.imbuedHasteID) == null)
					attr.applyModifier(GenericImbuement.imbuedHaste);
			}
			else if (attr.getModifier(GenericImbuement.imbuedHasteID) != null)
				attr.removeModifier(GenericImbuement.imbuedHaste);
		}

		if (!ent.onGround && ent.fallDistance >= 4.0F && extendedProperties.getContingencyType() == ContingencyTypes.FALL && extendedProperties.getContingencyEffect() != null)
		{
			int distanceToGround = MathUtilities.getDistanceToGround(ent, world);
			if (distanceToGround < -8.0D * ent.motionY)
				extendedProperties.procContingency();
		}

		if (extendedProperties.getContingencyType() == ContingencyTypes.ON_FIRE && ent.isBurning())
			extendedProperties.procContingency();

		if (!ent.worldObj.isRemote && ent.ticksExisted % 200 == 0)
			extendedProperties.setSyncAuras();

		extendedProperties.handleExtendedPropertySync();
		if (ent instanceof EntityPlayer)
		{
			AffinityData.For(ent).handleExtendedPropertySync();
			SkillData.For((EntityPlayer) ent).handleExtendedPropertySync();
			if (!ent.isPotionActive(BuffList.flight.id) && !ent.isPotionActive(BuffList.levitation.id) && !((EntityPlayer) ent).capabilities.isCreativeMode)
			{
				if (extendedProperties.hadFlight)
				{
					((EntityPlayer) ent).capabilities.allowFlying = false;
					((EntityPlayer) ent).capabilities.isFlying = false;
					extendedProperties.hadFlight = false;
				}
			}
			else
			{
				extendedProperties.hadFlight = true;
				if (ent.isPotionActive(BuffList.levitation) && ((EntityPlayer) ent).capabilities.isFlying)
				{
					float factor = 0.4F;
					ent.motionX *= factor;
					ent.motionZ *= factor;
					ent.motionY *= 9.999999747378752E-5D;
				}
			}
		}

		if (ent.isPotionActive(BuffList.agility.id))
			ent.stepHeight = 1.01F;
		else if (ent.stepHeight == 1.01F)
			ent.stepHeight = 0.5F;

		if (!ent.worldObj.isRemote && EntityUtilities.isSummon(ent) && !EntityUtilities.isTileSpawnedAndValid(ent))
		{
			int owner = EntityUtilities.getOwner(ent);
			Entity ownerEnt = ent.worldObj.getEntityByID(owner);
			if (!EntityUtilities.decrementSummonDuration(ent))
				ent.attackEntityFrom(DamageSources.unsummon, 5000.0F);

			if (owner == -1 || ownerEnt == null || ownerEnt.isDead || ownerEnt.getDistanceSqToEntity(ent) > 900.0D)
				if (ent instanceof EntityLiving && !((EntityLiving) ent).getCustomNameTag().equals(""))
				{
					EntityUtilities.setOwner(ent, (EntityLivingBase) null);
					EntityUtilities.setSummonDuration(ent, -1);
					EntityUtilities.revertAI((EntityCreature) ent);
				}
				else
					ent.attackEntityFrom(DamageSources.unsummon, 5000.0F);
		}

		if (event.entityLiving.isPotionActive(BuffList.leap))
		{
			int amplifier = event.entityLiving.getActivePotionEffect(BuffList.leap).getAmplifier() + 1;
			switch (amplifier)
			{
				case 0:
					extendedProperties.setFallProtection(8);
					break;
				case 1:
					extendedProperties.setFallProtection(20);
					break;
				case 2:
					extendedProperties.setFallProtection(45);
			}
		}

		if (event.entityLiving.isPotionActive(BuffList.gravityWell) && event.entityLiving.motionY < 0.0D && event.entityLiving.motionY > -3.0D)
			event.entityLiving.motionY *= 1.6D;

		if ((event.entityLiving.isPotionActive(BuffList.slowfall) || event.entityLiving.isPotionActive(BuffList.shrink) || ent instanceof EntityPlayer && AffinityData.For(ent).getAffinityDepth(Affinity.NATURE) == 1.0F && !ent.isSneaking()) && !event.entityLiving.onGround && event.entityLiving.motionY < 0.0D)
			event.entityLiving.motionY *= 0.7999999999999999D;

		if (event.entityLiving.isPotionActive(BuffList.wateryGrave) && event.entityLiving.isInWater())
		{
			double pullVel = -0.5D;
			pullVel = pullVel * (event.entityLiving.getActivePotionEffect(BuffList.wateryGrave).getAmplifier() + 1);
			if (event.entityLiving.motionY > pullVel)
				event.entityLiving.motionY -= 0.1D;
		}

		if (ent.worldObj.isRemote)
			extendedProperties.spawnManaLinkParticles();

		if (ent.ticksExisted % 20 == 0)
			extendedProperties.cleanupManaLinks();

		if (world.isRemote)
			AMCore.proxy.sendLocalMovementData(ent);

	}

	@SubscribeEvent
	public void onBucketFill(FillBucketEvent event)
	{
		ItemStack result = this.attemptFill(event.world, event.target);
		if (result != null)
		{
			event.result = result;
			event.setResult(Result.ALLOW);
		}

	}

	private ItemStack attemptFill(World world, MovingObjectPosition p)
	{
		Block block = world.getBlock(p.blockX, p.blockY, p.blockZ);
		if (block == BlocksCommonProxy.liquidEssence && world.getBlockMetadata(p.blockX, p.blockY, p.blockZ) == 0)
		{
			world.setBlock(p.blockX, p.blockY, p.blockZ, Blocks.air);
			return new ItemStack(ItemsCommonProxy.itemAMBucket);
		}
		else
			return null;
	}

	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event)
	{
		if (event.target instanceof EntityItemFrame)
			AMCore.proxy.itemFrameWatcher.startWatchingFrame((EntityItemFrame) event.target);

	}

	@SubscribeEvent
	public void onPlayerTossItem(ItemTossEvent event)
	{
		if (!event.entityItem.worldObj.isRemote)
			EntityItemWatcher.instance.addWatchedItem(event.entityItem);

	}

	@SubscribeEvent
	public void onEntityAttacked(LivingAttackEvent event)
	{
		if (event.source.isFireDamage() && event.entityLiving instanceof EntityPlayer && ((EntityPlayer) event.entityLiving).inventory.armorInventory[3] != null && ((EntityPlayer) event.entityLiving).inventory.armorInventory[3].getItem() == ItemsCommonProxy.fireEars)
			event.setCanceled(true);
		else if (event.entityLiving.isPotionActive(BuffList.manaShield) && ExtendedProperties.For(event.entityLiving).getCurrentMana() >= event.ammount * 250.0F)
		{
			ExtendedProperties.For(event.entityLiving).deductMana(event.ammount * 100.0F);
			ExtendedProperties.For(event.entityLiving).forceSync();

			for (int i = 0; i < Math.min(event.ammount, 5 * AMCore.config.getGFXLevel()); ++i)
				AMCore.proxy.particleManager.BoltFromPointToPoint(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY + event.entityLiving.worldObj.rand.nextFloat() * event.entityLiving.getEyeHeight(), event.entityLiving.posZ, event.entityLiving.posX - 1.0D + event.entityLiving.worldObj.rand.nextFloat() * 2.0F, event.entityLiving.posY - 1.0D + event.entityLiving.worldObj.rand.nextFloat() * 2.0F, event.entityLiving.posZ - 1.0D + event.entityLiving.worldObj.rand.nextFloat() * 2.0F, 6, -1);

			event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "arsmagica2:misc.event.mana_shield_block", 1.0F, event.entityLiving.worldObj.rand.nextFloat() + 0.5F);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onEntityHurt(LivingHurtEvent event)
	{
		if (event.source.isFireDamage() && event.entityLiving instanceof EntityPlayer && ((EntityPlayer) event.entityLiving).inventory.armorInventory[3] != null && ((EntityPlayer) event.entityLiving).inventory.armorInventory[3].getItem() == ItemsCommonProxy.fireEars)
			event.setCanceled(true);
		else
		{
			if (event.entityLiving.isPotionActive(BuffList.magicShield))
				event.ammount *= 0.25F;

			if (event.entityLiving.isPotionActive(BuffList.manaShield))
			{
				float manaToTake = Math.min(ExtendedProperties.For(event.entityLiving).getCurrentMana(), event.ammount * 250.0F);
				event.ammount -= manaToTake / 250.0F;
				ExtendedProperties.For(event.entityLiving).deductMana(manaToTake);
				ExtendedProperties.For(event.entityLiving).forceSync();

				for (int i = 0; i < Math.min(event.ammount, 5 * AMCore.config.getGFXLevel()); ++i)
					AMCore.proxy.particleManager.BoltFromPointToPoint(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY + event.entityLiving.worldObj.rand.nextFloat() * event.entityLiving.getEyeHeight(), event.entityLiving.posZ, event.entityLiving.posX - 1.0D + event.entityLiving.worldObj.rand.nextFloat() * 2.0F, event.entityLiving.posY + event.entityLiving.getEyeHeight() - 1.0D + event.entityLiving.worldObj.rand.nextFloat() * 2.0F, event.entityLiving.posZ - 1.0D + event.entityLiving.worldObj.rand.nextFloat() * 2.0F, 6, -1);

				event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "arsmagica2:misc.event.mana_shield_block", 1.0F, event.entityLiving.worldObj.rand.nextFloat() + 0.5F);
				if (event.ammount <= 0.0F)
				{
					event.setCanceled(true);
					return;
				}
			}

			Entity entitySource = event.source.getSourceOfDamage();
			if (entitySource instanceof EntityPlayer && ((EntityPlayer) entitySource).inventory.armorInventory[2] != null && ((EntityPlayer) entitySource).inventory.armorInventory[2].getItem() == ItemsCommonProxy.earthGuardianArmor && ((EntityPlayer) entitySource).getCurrentEquippedItem() == null)
			{
				event.ammount += 4.0F;

				    
				if (EventConfig.enableEarthArmorKnockback)
				    
				{
					double deltaZ = event.entityLiving.posZ - entitySource.posZ;
					double deltaX = event.entityLiving.posX - entitySource.posX;
					double angle = Math.atan2(deltaZ, deltaX);
					double speed = ((EntityPlayer) entitySource).isSprinting() ? 3.0D : 2.0D;
					double vertSpeed = ((EntityPlayer) entitySource).isSprinting() ? 0.5D : 0.325D;

					if (event.entityLiving instanceof EntityPlayer)
						AMNetHandler.INSTANCE.sendVelocityAddPacket(event.entityLiving.worldObj, event.entityLiving, speed * Math.cos(angle), vertSpeed, speed * Math.sin(angle));
					else
					{
						event.entityLiving.motionX += speed * Math.cos(angle);
						event.entityLiving.motionZ += speed * Math.sin(angle);
						event.entityLiving.motionY += vertSpeed;
					}
				}

				event.entityLiving.worldObj.playSoundAtEntity(event.entityLiving, "arsmagica2:spell.cast.earth", 0.4F, event.entityLiving.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			}

			ExtendedProperties extendedProperties = ExtendedProperties.For(event.entityLiving);
			EntityLivingBase ent = event.entityLiving;
			if (extendedProperties.getContingencyType() == ContingencyTypes.DAMAGE_TAKEN)
				extendedProperties.procContingency();

			if (extendedProperties.getContingencyType() == ContingencyTypes.HEALTH_LOW && ent.getHealth() <= ent.getMaxHealth() / 3.0F)
				extendedProperties.procContingency();

			if (ent.isPotionActive(BuffList.fury.id))
				event.ammount /= 2.0F;

			if (entitySource instanceof EntityLivingBase && ((EntityLivingBase) entitySource).isPotionActive(BuffList.shrink))
				event.ammount /= 2.0F;

		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		if (event.entity instanceof EntityLivingBase && ((EntityLivingBase) event.entity).isPotionActive(BuffList.temporalAnchor.id))
			((EntityLivingBase) event.entity).removePotionEffect(BuffList.temporalAnchor.id);

	}

	@SubscribeEvent
	public void onBreakSpeed(BreakSpeed event)
	{
		EntityPlayer player = event.entityPlayer;
		if (player.isPotionActive(BuffList.fury.id))
			event.newSpeed = event.originalSpeed * 5.0F;

	}

	@SubscribeEvent
	public void onManaCost(ManaCostEvent event)
	{
		if (event.caster.getHeldItem() != null && event.caster.getHeldItem().getItem() == ItemsCommonProxy.arcaneSpellbook)
		{
			event.manaCost *= 0.75F;
			event.burnout *= 0.4F;
		}

	}

	@SubscribeEvent
	public void onPlayerPickupItem(EntityItemPickupEvent event)
	{
		if (event.entityPlayer != null)
			if (!event.entityPlayer.worldObj.isRemote && ExtendedProperties.For(event.entityPlayer).getMagicLevel() <= 0 && event.item.getEntityItem().getItem() == ItemsCommonProxy.arcaneCompendium)
			{
				event.entityPlayer.addChatMessage(new ChatComponentText("You have unlocked the secrets of the arcane!"));
				AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP) event.entityPlayer, "shapes", true);
				AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP) event.entityPlayer, "components", true);
				AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP) event.entityPlayer, "modifiers", true);
				ExtendedProperties.For(event.entityPlayer).setMagicLevelWithMana(1);
				ExtendedProperties.For(event.entityPlayer).forceSync();
			}
			else if (event.item.getEntityItem().getItem() == ItemsCommonProxy.spell)
			{
				if (event.entityPlayer.worldObj.isRemote)
					AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP) event.entityPlayer, "spell_book", false);
			}
			else
			{
				Item item = event.item.getEntityItem().getItem();
				int meta = event.item.getEntityItem().getItemDamage();
				if (event.entityPlayer.worldObj.isRemote && item.getUnlocalizedName() != null && AMCore.proxy.items.getArsMagicaItems().contains(item) || item instanceof ItemBlock && AMCore.proxy.blocks.getArsMagicaBlocks().contains(((ItemBlock) item).field_150939_a))
					AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP) event.entityPlayer, item.getUnlocalizedName().replace("item.", "").replace("arsmagica2:", "").replace("tile.", "") + (meta > -1 ? "@" + meta : ""), false);
			}
	}
}
