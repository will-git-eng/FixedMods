package ru.will.git.hee;

import ru.will.git.reflectionmedic.util.FastUtils;
import com.google.common.base.Throwables;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.Location;

import java.lang.reflect.Method;
import java.util.UUID;

public final class ModUtils
{
	public static final GameProfile profile = new GameProfile(UUID.fromString("805fd021-044d-466a-a1eb-d2fa6f2f9058"), "[HardcoreEnderExpansion]");
	private static final Method respawnPlayer;
	private static FakePlayer player = null;

	public static FakePlayer getModFake(World world)
	{
		if (player == null)
			player = FastUtils.getFake(world, profile);
		else
			player.worldObj = world;

		return player;
	}

	public static EntityPlayerMP respawnPlayer(EntityPlayerMP player, int dimension, boolean theEnd)
	{
		ServerConfigurationManager cfgManager = player.mcServer.getConfigurationManager();
		if (respawnPlayer != null)
			try
			{
				return (EntityPlayerMP) respawnPlayer.invoke(cfgManager, player, dimension, theEnd, null, true);
			}
			catch (Throwable throwable)
			{
				throw Throwables.propagate(throwable);
			}
		return cfgManager.respawnPlayer(player, dimension, theEnd);
	}

	public static boolean canMobGrief(World world)
	{
		return EventConfig.mobGrief && world != null && world.getGameRules().getGameRuleBooleanValue("mobGriefing");
	}

	static
	{
		Method method = null;

		try
		{
			String[] names = { "func_72368_a", "respawnPlayer" };
			method = ReflectionHelper.findMethod(ServerConfigurationManager.class, null, names, EntityPlayerMP.class, int.class, boolean.class, Location.class, boolean.class);
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace();
		}

		respawnPlayer = method;
	}
}