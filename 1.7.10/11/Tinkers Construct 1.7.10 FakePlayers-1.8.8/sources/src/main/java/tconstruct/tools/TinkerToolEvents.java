package tconstruct.tools;

import ru.will.git.tconstruct.EventConfig;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import tconstruct.TConstruct;
import tconstruct.armor.player.TPlayerStats;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.entity.ProjectileBase;
import tconstruct.library.event.PartBuilderEvent;
import tconstruct.library.event.ToolBuildEvent;
import tconstruct.library.event.ToolCraftEvent;
import tconstruct.library.tools.*;
import tconstruct.util.ItemHelper;
import tconstruct.util.config.PHConstruct;
import tconstruct.util.network.MovementUpdatePacket;

import java.util.List;

public class TinkerToolEvents
{
	@SubscribeEvent
	public void onCrafting(ItemCraftedEvent event)
    
		if (event.crafting == null || event.player == null || event.player.worldObj == null || event.craftMatrix == null)
    

		Item item = event.crafting.getItem();
		if (!event.player.worldObj.isRemote)
		{
			if (item == Item.getItemFromBlock(TinkerTools.toolStationWood))
			{
				if (!PHConstruct.beginnerBook)
					return;

				TPlayerStats stats = TPlayerStats.get(event.player);
				if (!stats.materialManual)
				{
					stats.materialManual = true;
					AbilityHelper.spawnItemAtPlayer(event.player, new ItemStack(TinkerTools.manualBook, 1, 1));
				}
    
    
				for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++)
				{
					ItemStack stack = event.craftMatrix.getStackInSlot(i);
					if (stack == null)
    
					if (stack.getItem() == Item.getItemFromBlock(TinkerTools.toolStationWood) && stack.getItemDamage() == 5)
					{
						event.crafting.setTagCompound(stack.getTagCompound());
						break;
					}
				}
		}
	}

	@SubscribeEvent
	public void buildTool(ToolBuildEvent event)
    
		if (event.handleStack.getItem() == Items.bone)
		{
    
			return;
    
		List<ItemStack> sticks = OreDictionary.getOres("stickWood");
		for (ItemStack stick : sticks)
		{
			if (OreDictionary.itemMatches(stick, event.handleStack, false))
			{
    
				return;
			}
		}
	}

	@SubscribeEvent
	public void craftTool(ToolCraftEvent.NormalTool event)
	{
		NBTTagCompound toolTag = event.toolTag.getCompoundTag("InfiTool");
		if (PHConstruct.denyMattock && event.tool == TinkerTools.mattock)
		{
			int head = toolTag.getInteger("Head");
			int handle = toolTag.getInteger("Handle");
			int accessory = toolTag.getInteger("Accessory");

			if (!this.allowCrafting(head, handle, accessory))
			{
				event.setResult(Result.DENY);
				return;
			}
    
		this.handlePaper(toolTag, event.tool);
		this.handleThaumium(toolTag, event.tool);

		if (event.tool == TinkerTools.battlesign)
		{
			int modifiers = toolTag.getInteger("Modifiers");
			modifiers += 1;
			toolTag.setInteger("Modifiers", modifiers);
		}
	}

	private void handlePaper(NBTTagCompound toolTag, ToolCore tool)
	{
		int modifiers = toolTag.getInteger("Modifiers");
		if (toolTag.getInteger("Head") == TinkerTools.MaterialID.Paper)
			modifiers++;
		if (toolTag.getInteger("Handle") == TinkerTools.MaterialID.Paper)
			modifiers++;
		if (toolTag.getInteger("Accessory") == TinkerTools.MaterialID.Paper)
			modifiers++;
		if (toolTag.getInteger("Extra") == TinkerTools.MaterialID.Paper)
    
		if (tool.getPartAmount() == 2 && toolTag.getInteger("Head") == TinkerTools.MaterialID.Paper)
			modifiers++;

		toolTag.setInteger("Modifiers", modifiers);
	}

	private void handleThaumium(NBTTagCompound toolTag, ToolCore tool)
    
		int thaum = 0;
		if (toolTag.getInteger("Head") == TinkerTools.MaterialID.Thaumium)
			thaum++;
		if (toolTag.getInteger("Handle") == TinkerTools.MaterialID.Thaumium)
			thaum++;
		if (toolTag.getInteger("Accessory") == TinkerTools.MaterialID.Thaumium)
			thaum++;
		if (toolTag.getInteger("Extra") == TinkerTools.MaterialID.Thaumium)
    
    
		if (tool.getPartAmount() == 2)
			bonusModifiers = thaum;

		int modifiers = toolTag.getInteger("Modifiers");
		modifiers += bonusModifiers;
		toolTag.setInteger("Modifiers", modifiers);
	}

	private boolean allowCrafting(int head, int handle, int accessory)
	{
		int[] nonMetals = { 0, 1, 3, 4, 5, 6, 7, 8, 9, 17 };
		for (int i = 0; i < nonMetals.length; i++)
		{
			if (head == nonMetals[i] || handle == nonMetals[i] || accessory == nonMetals[i])
				return false;
		}
		return true;
	}

	@SubscribeEvent
	public void craftPart(PartBuilderEvent.NormalPart event)
    
		if (event.pattern.getItem() == TinkerTools.woodPattern && event.pattern.getItemDamage() == 23)
		{
			ItemStack result = craftBowString(event.material);
			if (result != null)
				event.overrideResult(new ItemStack[] { result, null });
    
		if (event.pattern.getItem() == TinkerTools.woodPattern && event.pattern.getItemDamage() == 24)
		{
			ItemStack result = craftFletching(event.material);
			if (result != null)
				event.overrideResult(new ItemStack[] { result, null });
		}
	}

	public static ItemStack craftBowString(ItemStack stack)
	{
		if (stack.stackSize < 3)
			return null;

		BowstringMaterial mat = (BowstringMaterial) TConstructRegistry.getCustomMaterial(stack, BowstringMaterial.class);
		if (mat != null)
			return mat.craftingItem.copy();
		return null;
	}

	public static ItemStack craftFletching(ItemStack stack)
	{
    
		if (mat == null)
			mat = (FletchingMaterial) TConstructRegistry.getCustomMaterial(stack, FletchlingLeafMaterial.class);
		if (mat != null)
			return mat.craftingItem.copy();
		return null;
	}

	@SubscribeEvent
	public void onAttack(LivingAttackEvent event)
    
		if (event.entityLiving instanceof EntityPlayer)
		{
    
			ItemStack stack = player.getCurrentEquippedItem();
			if (stack != null && stack.getItem() == TinkerTools.battlesign)
    
				if (!stack.hasTagCompound() || stack.getTagCompound().getCompoundTag("InfiTool").getBoolean("Broken"))
					return;

				DamageSource source = event.source;
				if (!source.isUnblockable() && !source.isMagicDamage() && !source.isExplosion())
				{
					if (source.isProjectile())
					{
						Entity projectile = source.getSourceOfDamage();
						Vec3 motion = Vec3.createVectorHelper(projectile.motionX, projectile.motionY, projectile.motionZ);
    
    
						if (strength < 0.1)
    
						event.setCanceled(true);

						double speed = projectile.motionX * projectile.motionX + projectile.motionY * projectile.motionY + projectile.motionZ * projectile.motionZ;
						speed = Math.sqrt(speed);

    
						projectile.motionX = look.xCoord * speed;
						projectile.motionY = look.yCoord * speed;
						projectile.motionZ = look.zCoord * speed;

						projectile.rotationYaw = (float) (Math.atan2(projectile.motionX, projectile.motionZ) * 180.0D / Math.PI);
    
						TConstruct.packetPipeline.sendToAll(new MovementUpdatePacket(projectile));

						if (projectile instanceof EntityArrow)
						{
    
    
							projectile.motionX /= -0.10000000149011612D;
							projectile.motionY /= -0.10000000149011612D;
    
    
    
    
    
							if (projectile instanceof ProjectileBase)
								((ProjectileBase) projectile).defused = false;
						}
					}
					else
					{
						Entity attacker = source.getEntity();
						if (attacker != null)
							attacker.attackEntityFrom(DamageSource.causeThornsDamage(player), event.ammount);
    
					AbilityHelper.damageTool(stack, (int) Math.ceil(event.ammount / 2f), player, false);
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingDrop(LivingDropsEvent event)
	{
		if (event.entityLiving == null)
			return;

		if (event.recentlyHit)
		{
			if (event.entityLiving.getClass() == EntitySkeleton.class)
			{
    
				if (EventConfig.enableBeheading && event.source.damageType.equals("player"))
				{
					EntityPlayer player = (EntityPlayer) event.source.getEntity();
					ItemStack stack = player.getCurrentEquippedItem();
					if (stack != null && stack.hasTagCompound() && stack.getItem() instanceof ToolCore)
					{
						int beheading = stack.getTagCompound().getCompoundTag("InfiTool").getInteger("Beheading");
						if (stack.getItem() == TinkerTools.cleaver)
							beheading += 2;
						if (beheading > 0 && TConstruct.random.nextInt(100) < beheading * 10)
							ItemHelper.addDrops(event, new ItemStack(Items.skull, 1, enemy.getSkeletonType()));
					}
				}

				if (enemy.getSkeletonType() == 1 && TConstruct.random.nextInt(Math.max(1, 5 - event.lootingLevel)) == 0)
					ItemHelper.addDrops(event, new ItemStack(TinkerTools.materials, 1, 8));
    
			if (EventConfig.enableBeheading && event.entityLiving.getClass() == EntityZombie.class)
			{
				EntityZombie enemy = (EntityZombie) event.entityLiving;

				if (event.source.damageType.equals("player"))
				{
					EntityPlayer player = (EntityPlayer) event.source.getEntity();
					ItemStack stack = player.getCurrentEquippedItem();

					if (stack != null && stack.hasTagCompound() && stack.getItem() instanceof ToolCore)
					{
						int beheading = stack.getTagCompound().getCompoundTag("InfiTool").getInteger("Beheading");
						if (stack != null && stack.hasTagCompound() && stack.getItem() == TinkerTools.cleaver)
							beheading += 2;
						if (beheading > 0 && TConstruct.random.nextInt(100) < beheading * 10)
							ItemHelper.addDrops(event, new ItemStack(Items.skull, 1, 2));
					}

					if (stack != null && stack.hasTagCompound() && stack.getItem() == TinkerTools.cleaver && TConstruct.random.nextInt(100) < 10)
						ItemHelper.addDrops(event, new ItemStack(Items.skull, 1, 2));

				}
    
			if (EventConfig.enableBeheading && event.entityLiving.getClass() == EntityCreeper.class)
			{
				EntityCreeper enemy = (EntityCreeper) event.entityLiving;

				if (event.source.damageType.equals("player"))
				{
					EntityPlayer player = (EntityPlayer) event.source.getEntity();
					ItemStack stack = player.getCurrentEquippedItem();
					if (stack != null && stack.hasTagCompound() && stack.getItem() instanceof ToolCore)
					{
						int beheading = stack.getTagCompound().getCompoundTag("InfiTool").getInteger("Beheading");
						if (stack.getItem() == TinkerTools.cleaver)
							beheading += 2;
						if (beheading > 0 && TConstruct.random.nextInt(100) < beheading * 5)
							ItemHelper.addDrops(event, new ItemStack(Items.skull, 1, 4));
					}
				}
			}
		}

		if (event.entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.entityLiving;

			if (PHConstruct.dropPlayerHeads)
			{
				ItemStack dropStack = new ItemStack(Items.skull, 1, 3);
				NBTTagCompound nametag = new NBTTagCompound();
				nametag.setString("SkullOwner", player.getDisplayName());
				dropStack.setTagCompound(nametag);
				ItemHelper.addDrops(event, dropStack);
    
			else if (EventConfig.enableBeheading && event.source.damageType.equals("player"))
			{
				EntityPlayer source = (EntityPlayer) event.source.getEntity();
				ItemStack stack = source.getCurrentEquippedItem();
				if (stack != null && stack.hasTagCompound() && stack.getItem() instanceof ToolCore)
				{
					int beheading = stack.getTagCompound().getCompoundTag("InfiTool").getInteger("Beheading");
					if (stack.getItem() == TinkerTools.cleaver)
						beheading += 2;
					if (beheading > 0 && TConstruct.random.nextInt(100) < beheading * 50)
					{
						ItemStack dropStack = new ItemStack(Items.skull, 1, 3);
						NBTTagCompound nametag = new NBTTagCompound();
						nametag.setString("SkullOwner", player.getDisplayName());
						dropStack.setTagCompound(nametag);
						ItemHelper.addDrops(event, dropStack);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void registerOre(OreRegisterEvent evt)
	{

		if (evt.Name.equals("crystalQuartz"))
			TinkerTools.modAttack.addStackToMatchList(evt.Ore, 2);
		else if (evt.Name.equals("crystalCertusQuartz"))
			TinkerTools.modAttack.addStackToMatchList(evt.Ore, 24);
	}

	@SubscribeEvent
	public void damageToolsOnDeath(PlayerDropsEvent event)
	{
		if (!PHConstruct.deathPenality)
			return;

    
		if (difficulty == EnumDifficulty.PEACEFUL || difficulty == EnumDifficulty.EASY)
			return;

    
		if (difficulty == EnumDifficulty.HARD)
    
    
    
    
		int derp = 1;
		if (event.entityPlayer.ticksExisted < 60 * 5 * 20)
		{
			derp = TPlayerStats.get(event.entityPlayer).derpLevel;
			if (derp <= 0)
				derp = 1;
			punishment *= derp;
		}

		boolean damaged = false;
		for (EntityItem drop : event.drops)
    
			if (!(drop.getEntityItem().getItem() instanceof ToolCore) || !drop.getEntityItem().hasTagCompound())
    
			NBTTagCompound tags = drop.getEntityItem().getTagCompound().getCompoundTag("InfiTool");
			int dur = tags.getInteger("TotalDurability");
			dur /= punishment;

			AbilityHelper.damageTool(drop.getEntityItem(), dur, event.entityPlayer, true);
			damaged = true;
		}

		if (damaged)
    
		TPlayerStats.get(event.entityPlayer).derpLevel = derp + 1;
	}
}
