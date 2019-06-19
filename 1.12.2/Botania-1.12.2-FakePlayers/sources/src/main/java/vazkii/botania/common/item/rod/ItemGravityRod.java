/**
 * This class was created by <Flaxbeard>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Aug 25, 2014, 2:57:16 PM (GMT)]
 */
package vazkii.botania.common.item.rod;

import ru.will.git.eventhelper.util.EventUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IManaProficiencyArmor;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.MathHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.entity.EntityThrownItem;
import vazkii.botania.common.item.ItemMod;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.LibItemNames;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemGravityRod extends ItemMod implements IManaUsingItem
{

	private static final float RANGE = 3F;
	private static final int COST = 2;

	private static final String TAG_TICKS_TILL_EXPIRE = "ticksTillExpire";
	private static final String TAG_TICKS_COOLDOWN = "ticksCooldown";
	private static final String TAG_TARGET = "target";
	private static final String TAG_DIST = "dist";

	public ItemGravityRod()
	{
		super(LibItemNames.GRAVITY_ROD);
		this.setMaxStackSize(1);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged)
	{
		return newStack.getItem() != this;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity par3Entity, int slot, boolean held)
	{
		if (!(par3Entity instanceof EntityPlayer))
			return;

		int ticksTillExpire = ItemNBTHelper.getInt(stack, TAG_TICKS_TILL_EXPIRE, 0);
		int ticksCooldown = ItemNBTHelper.getInt(stack, TAG_TICKS_COOLDOWN, 0);

		if (ticksTillExpire == 0)
		{
			ItemNBTHelper.setInt(stack, TAG_TARGET, -1);
			ItemNBTHelper.setDouble(stack, TAG_DIST, -1);
		}

		if (ticksCooldown > 0)
			ticksCooldown--;

		ticksTillExpire--;
		ItemNBTHelper.setInt(stack, TAG_TICKS_TILL_EXPIRE, ticksTillExpire);
		ItemNBTHelper.setInt(stack, TAG_TICKS_COOLDOWN, ticksCooldown);

		EntityPlayer player = (EntityPlayer) par3Entity;
		PotionEffect haste = player.getActivePotionEffect(MobEffects.HASTE);
		float check = haste == null ? 0.16666667F : haste.getAmplifier() == 1 ? 0.5F : 0.4F;
		if (player.getHeldItemMainhand() == stack && player.swingProgress == check && !world.isRemote)
			leftClick(player);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		int targetID = ItemNBTHelper.getInt(stack, TAG_TARGET, -1);
		int ticksCooldown = ItemNBTHelper.getInt(stack, TAG_TICKS_COOLDOWN, 0);
		double length = ItemNBTHelper.getDouble(stack, TAG_DIST, -1);

		if (ticksCooldown == 0)
		{
			Entity victim = null;
			if (targetID != -1)
			{
				Entity taritem = player.world.getEntityByID(targetID);
				if (taritem != null)
				{
					boolean found = false;
					Vector3 target = Vector3.fromEntityCenter(player);
					List<Entity> entities = new ArrayList<>();
					int distance = 1;
					while (entities.size() == 0 && distance < 25)
					{
						target = target.add(new Vector3(player.getLookVec()).multiply(distance)).add(0, 0.5, 0);
						entities = player.world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(target.x - RANGE, target.y - RANGE, target.z - RANGE, target.x + RANGE, target.y + RANGE, target.z + RANGE));
						distance++;
						if (entities.contains(taritem))
							found = true;
					}

					if (found)
						victim = taritem;
				}
			}

			if (victim == null)
			{
				Vector3 target = Vector3.fromEntityCenter(player);
				List<Entity> entities = new ArrayList<>();
				int distance = 1;
				while (entities.size() == 0 && distance < 25)
				{
					target = target.add(new Vector3(player.getLookVec()).multiply(distance)).add(0, 0.5, 0);
					entities = player.world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(target.x - RANGE, target.y - RANGE, target.z - RANGE, target.x + RANGE, target.y + RANGE, target.z + RANGE));
					distance++;
				}

				if (entities.size() > 0)
				{
					victim = entities.get(0);
					length = 5.5D;
					if (victim instanceof EntityItem)
						length = 2.0D;
				}
			}

			if (victim != null)
			{
				if (BotaniaAPI.isEntityBlacklistedFromGravityRod(victim.getClass()))
					return ActionResult.newResult(EnumActionResult.FAIL, stack);

				    
				if (!ManaItemHandler.requestManaExactForTool(stack, player, COST, false))
					return ActionResult.newResult(EnumActionResult.FAIL, stack);
				if (EventUtils.cantAttack(player, victim))
					return ActionResult.newResult(EnumActionResult.FAIL, stack);
				    

				if (ManaItemHandler.requestManaExactForTool(stack, player, COST, true))
				{
					if (victim instanceof EntityItem)
						((EntityItem) victim).setPickupDelay(5);
					else if (victim instanceof EntityLivingBase)
					{
						EntityLivingBase targetEntity = (EntityLivingBase) victim;
						targetEntity.fallDistance = 0.0F;
						if (targetEntity.getActivePotionEffect(MobEffects.SLOWNESS) == null)
							targetEntity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 2, 3, true, true));
					}

					Vector3 target3 = Vector3.fromEntityCenter(player).add(new Vector3(player.getLookVec()).multiply(length)).add(0, 0.5, 0);
					if (victim instanceof EntityItem)
						target3 = target3.add(0, 0.25, 0);

					for (int i = 0; i < 4; i++)
					{
						float r = 0.5F + (float) Math.random() * 0.5F;
						float b = 0.5F + (float) Math.random() * 0.5F;
						float s = 0.2F + (float) Math.random() * 0.1F;
						float m = 0.1F;
						float xm = ((float) Math.random() - 0.5F) * m;
						float ym = ((float) Math.random() - 0.5F) * m;
						float zm = ((float) Math.random() - 0.5F) * m;
						Botania.proxy.wispFX(victim.posX + victim.width / 2, victim.posY + victim.height / 2, victim.posZ + victim.width / 2, r, 0F, b, s, xm, ym, zm);
					}

					MathHelper.setEntityMotionFromVector(victim, target3, 0.3333333F);

					ItemNBTHelper.setInt(stack, TAG_TARGET, victim.getEntityId());
					ItemNBTHelper.setDouble(stack, TAG_DIST, length);
				}

				ItemNBTHelper.setInt(stack, TAG_TICKS_TILL_EXPIRE, 5);
				return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
			}
		}
		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	@Override
	public boolean usesMana(ItemStack stack)
	{
		return true;
	}

	public static void leftClick(EntityPlayer player)
	{
		ItemStack stack = player.getHeldItemMainhand();
		if (!stack.isEmpty() && stack.getItem() == ModItems.gravityRod)
		{
			int targetID = ItemNBTHelper.getInt(stack, TAG_TARGET, -1);
			ItemNBTHelper.getDouble(stack, TAG_DIST, -1);
			if (targetID != -1)
			{
				Entity victim = player.world.getEntityByID(targetID);
				if (victim != null)
				{
					boolean found = false;
					Vector3 target = Vector3.fromEntityCenter(player);
					List<Entity> entities = Collections.emptyList();
					int distance = 1;
					while (entities.isEmpty() && distance < 25)
					{
						target = target.add(new Vector3(player.getLookVec()).multiply(distance)).add(0, 0.5, 0);
						entities = player.world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(target.x - RANGE, target.y - RANGE, target.z - RANGE, target.x + RANGE, target.y + RANGE, target.z + RANGE));
						distance++;
						if (entities.contains(victim))
							found = true;
					}

					if (found)
					{
						ItemNBTHelper.setInt(stack, TAG_TARGET, -1);
						ItemNBTHelper.setDouble(stack, TAG_DIST, -1);

						    
						if (EventUtils.cantAttack(player, victim))
						{
							ItemNBTHelper.setInt(stack, TAG_TICKS_COOLDOWN, 10);
							return;
						}
						    

						Vector3 moveVector = new Vector3(player.getLookVec().normalize());
						if (victim instanceof EntityItem)
						{
							((EntityItem) victim).setPickupDelay(20);
							float mot = IManaProficiencyArmor.Helper.hasProficiency(player, stack) ? 2.25F : 1.5F;
							victim.motionX = moveVector.x * mot;
							victim.motionY = moveVector.y;
							victim.motionZ = moveVector.z * mot;
							if (!player.world.isRemote)
							{
								EntityThrownItem thrown = new EntityThrownItem(victim.world, victim.posX, victim.posY, victim.posZ, (EntityItem) victim);
								victim.world.spawnEntity(thrown);
							}
							victim.setDead();
						}
						else
						{
							victim.motionX = moveVector.x * 3.0F;
							victim.motionY = moveVector.y * 1.5F;
							victim.motionZ = moveVector.z * 3.0F;
						}
						ItemNBTHelper.setInt(stack, TAG_TICKS_COOLDOWN, 10);
					}
				}
			}
		}
	}
}