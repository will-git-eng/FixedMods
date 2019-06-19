package ru.will.git.botania;

import ru.will.git.eventhelper.nexus.ModNexus;
import ru.will.git.eventhelper.nexus.ModNexusFactory;
import ru.will.git.eventhelper.nexus.NexusUtils;
import ru.will.git.eventhelper.util.EventUtils;
import com.google.common.base.Strings;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

@ModNexus(name = "Botania")
public final class ModUtils
{
	public static final ModNexusFactory NEXUS_FACTORY = NexusUtils.getFactory();
	private static final String ITEM_TAG_THROWER_CHECK_RES_ALLOW = "ItemThrowerCheckResAllow";
	private static final String ITEM_TAG_THROWER_CHECK_RES_DENY = "ItemThrowerCheckResDeny";

	public static boolean canThrowerInteract(@Nullable EntityItem entityItem, @Nonnull BlockPos pos)
	{
		if (entityItem == null)
			return false;

		Set<String> tags = entityItem.getTags();
		if (tags.contains(ITEM_TAG_THROWER_CHECK_RES_ALLOW))
			return true;
		if (tags.contains(ITEM_TAG_THROWER_CHECK_RES_DENY))
			return false;

		boolean result = canThrowerInteract0(entityItem, pos);
		if (result)
			entityItem.addTag(ITEM_TAG_THROWER_CHECK_RES_ALLOW);
		else
			entityItem.addTag(ITEM_TAG_THROWER_CHECK_RES_DENY);
		return result;
	}

	private static boolean canThrowerInteract0(@Nullable EntityItem entityItem, @Nonnull BlockPos pos)
	{
		if (entityItem == null)
			return false;

		String throwerName = entityItem.getThrower();
		if (Strings.isNullOrEmpty(throwerName))
			return !EventConfig.paranoidDropProtection;

		World world = entityItem.getEntityWorld();
		EntityPlayer player = world.getPlayerEntityByName(throwerName);
		if (player == null)
			return !EventConfig.paranoidDropProtection;

		return !EventUtils.cantInteract(player, EnumHand.MAIN_HAND, pos, EnumFacing.UP);
	}
}
