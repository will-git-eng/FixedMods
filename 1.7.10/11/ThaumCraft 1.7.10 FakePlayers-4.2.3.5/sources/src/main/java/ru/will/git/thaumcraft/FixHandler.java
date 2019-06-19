package ru.will.git.thaumcraft;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import thaumcraft.common.blocks.BlockMetalDevice;
import thaumcraft.common.items.ItemEldritchObject;

public final class FixHandler
{
	public static void init()
	{
		if (EventConfig.advCrucibleDupeFix || EventConfig.obeliskPlacerCreativeOnly)
		{
			FixHandler handler = new FixHandler();
			FMLCommonHandler.instance().bus().register(handler);
			MinecraftForge.EVENT_BUS.register(handler);
		}
	}

	@SubscribeEvent
	public void onFill(FillBucketEvent event)
	{
		if (EventConfig.advCrucibleDupeFix)
		{
			World world = event.world;
			if (!world.isRemote)
			{
				int x = event.target.blockX;
				int y = event.target.blockY;
				int z = event.target.blockZ;
				Block block = world.getBlock(x, y, z);
				if (block instanceof BlockMetalDevice)
				{
					int meta = world.getBlockMetadata(x, y, z);
					if (meta == 0)
						event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if (EventConfig.obeliskPlacerCreativeOnly)
		{
			EntityPlayer player = event.player;
			if (!player.worldObj.isRemote && !player.capabilities.isCreativeMode)
			{
				InventoryPlayer inventoryPlayer = player.inventory;
				ItemStack stack = inventoryPlayer.getItemStack();
				if (stack != null)
				{
					Item item = stack.getItem();
					if (item instanceof ItemEldritchObject && stack.getItemDamage() == 4)
						inventoryPlayer.setItemStack(null);
				}
			}
		}
	}

	@SubscribeEvent
	public void onItemToss(ItemTossEvent event)
	{
		if (EventConfig.obeliskPlacerCreativeOnly)
		{
			EntityPlayer player = event.player;
			if (!player.worldObj.isRemote && !player.capabilities.isCreativeMode)
			{
				ItemStack stack = event.entityItem.getEntityItem();
				if (stack != null)
				{
					Item item = stack.getItem();
					if (item instanceof ItemEldritchObject && stack.getItemDamage() == 4)
						event.setCanceled(true);
				}
			}
		}
	}
}
