package ru.will.git.clientwg.asm;

import ru.will.git.clientwg.ProtectionHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

public final class MethodHooks
{
	public static final String OWNER = "ru/will/git/clientwg/asm/MethodHooks";
	public static final String NAME_BLOCK_INTERACT = "cancelBlockInteract";
	public static final String DESC_BLOCK_INTERACT = "()Z";
	public static final String NAME_ENTITY_INTERACT = "cancelEntityInteract";
	public static final String DESC_ENTITY_INTERACT = "()Z";

	@SideOnly(Side.CLIENT)
	public static boolean cancelBlockInteract()
	{
		Minecraft mc = Minecraft.getMinecraft();
		MovingObjectPosition mop = mc.objectMouseOver;
		return mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && !ProtectionHandler.canInteractWithBlock(mc.thePlayer, mop.blockX, mop.blockY, mop.blockZ);
	}

	@SideOnly(Side.CLIENT)
	public static boolean cancelEntityInteract()
	{
		Minecraft mc = Minecraft.getMinecraft();
		MovingObjectPosition mop = mc.objectMouseOver;
		return mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && !ProtectionHandler.canInteractWithEntity(mc.thePlayer, mop.entityHit);
	}
}
