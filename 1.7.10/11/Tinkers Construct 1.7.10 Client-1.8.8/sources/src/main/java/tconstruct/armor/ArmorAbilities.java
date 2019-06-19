package tconstruct.armor;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tconstruct.TConstruct;
import tconstruct.armor.items.TravelGear;
import tconstruct.armor.player.TPlayerStats;
import tconstruct.library.modifier.IModifyable;
import tconstruct.util.network.HealthUpdatePacket;

import java.util.ArrayList;
import java.util.List;

public class ArmorAbilities
{
    
	double prevMotionY;

	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event)
	{
		EntityPlayer player = event.player;
    
		if (stats.climbWalls)
		{
			double motionX = player.posX - player.lastTickPosX;
			double motionZ = player.posZ - player.lastTickPosZ;
			double motionY = player.posY - player.lastTickPosY - 0.762;
			if (motionY > 0.0D && (motionX == 0D || motionZ == 0D))
				player.fallDistance = 0.0F;
    
		ItemStack feet = player.getCurrentArmor(0);
		if (feet != null)
    
			if (feet.hasTagCompound() && feet.getItem() instanceof IModifyable && !player.isSneaking())
			{
				NBTTagCompound tag = feet.getTagCompound().getCompoundTag(((IModifyable) feet.getItem()).getBaseTagName());
				int sole = tag.getInteger("Slimy Soles");
				if (sole > 0)
					if (!player.isSneaking() && player.onGround && this.prevMotionY < -0.4)
						player.motionY = -this.prevMotionY * Math.min(0.99, sole * 0.2);
			}
			this.prevMotionY = player.motionY;
		}
    
		boolean stepBoosted = stepBoostedPlayers.contains(player.getGameProfile().getName());
		if (stepBoosted)
			player.stepHeight = 1.1f;
		if (!stepBoosted && feet != null && feet.getItem() instanceof TravelGear)
			stepBoostedPlayers.add(player.getGameProfile().getName());
		else if (stepBoosted && (feet == null || !(feet.getItem() instanceof TravelGear)))
		{
			stepBoostedPlayers.remove(player.getGameProfile().getName());
			player.stepHeight -= 0.6f;
    
    
	}

	@SubscribeEvent
	public void dimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		if (event.player == null || !(event.player instanceof EntityPlayerMP))
    
    
		TConstruct.packetPipeline.sendTo(new HealthUpdatePacket(oldHealth), (EntityPlayerMP) event.player);
	}
}
