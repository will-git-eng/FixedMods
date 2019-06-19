package ru.will.git.clientwg;

import ru.will.git.clientwg.util.ProtectedRegion;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class ProtectionHandler
{
	public static void register()
	{
		ProtectionHandler handler = new ProtectionHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
	}

	@SubscribeEvent
	public void onInteract(PlayerInteractEvent event)
	{
		if (event.world.isRemote && !canInteractWithBlock(event.entityPlayer, event.x, event.y, event.z))
			event.setCanceled(true);
	}

	public static boolean canInteractWithBlock(EntityPlayer player, int x, int y, int z)
	{
		if (ClientWG.instance.config.hasForceAccess(player.getHeldItem()))
			return true;

		if (x == 0 && y == 0 && z == 0)
		{
			ItemStack stack = player.getHeldItem();
			if (stack != null)
			{
				Item item = stack.getItem();
				if (item == Items.milk_bucket || item instanceof ItemFood)
					return true;
			}
		}

		return ClientWG.instance.config.hasForceAccess(player.worldObj, x, y, z) || canInteractWithPos(player) && canInteractWithPos(x, y, z);
	}

	public static boolean canInteractWithEntity(EntityPlayer player, Entity entity)
	{
		if (ClientWG.instance.config.hasForceAccess(player.getHeldItem()))
			return true;

		if (ClientWG.instance.config.hasForceAccess(entity))
			return true;

		for (ProtectedRegion region : ClientWG.instance.regions)
		{
			if (region.hasForceAccess(entity) && region.region.isInRegion(entity))
				return true;
		}

		if (canInteractWithPos(player))
		{
			for (ProtectedRegion region : ClientWG.instance.regions)
			{
				if (!region.hasAccess(entity))
					return false;
			}
			return true;
		}

		return false;
	}

	private static boolean canInteractWithPos(Entity entity)
	{
		int x = MathHelper.floor_double(entity.posX);
		int y = MathHelper.floor_double(entity.posY);
		int z = MathHelper.floor_double(entity.posZ);
		return canInteractWithPos(x, y, z);
	}

	private static boolean canInteractWithPos(int x, int y, int z)
	{
		if (x == 0 && y == 0 && z == 0)
			return true;

		for (ProtectedRegion region : ClientWG.instance.regions)
		{
			if (!region.hasAccess(x, y, z))
				return false;
		}
		return true;
	}
}
