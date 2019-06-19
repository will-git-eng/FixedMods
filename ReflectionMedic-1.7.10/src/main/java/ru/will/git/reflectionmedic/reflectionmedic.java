package ru.will.git.reflectionmedic;

import ru.will.git.reflectionmedic.command.CommandReloadAllConfigs;
import ru.will.git.reflectionmedic.config.ConfigUtils;
import ru.will.git.reflectionmedic.inject.InjectionManager;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import java.io.File;
import java.util.List;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

@SideOnly(Side.SERVER)
@Mod(modid = "reflectionmedic", name = "reflectionmedic", version = "@VERSION@", acceptableRemoteVersions = "*")
public final class reflectionmedic
{
	public static final Logger LOGGER = LogManager.getLogger("reflectionmedic");
	public static final File cfgDir = new File(Loader.instance().getConfigDir(), "Events");
	public static final List<RegisteredListener> listeners = Lists.newArrayList();
	public static String craftPackage = "org.bukkit.craftbukkit.v1_7_R4";
	public static boolean explosions = true;
	public static boolean debug = true;

	@EventHandler
	public void onServerStart(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandReloadAllConfigs());
	}

	@EventHandler
	public final void serverStarted(FMLServerStartedEvent event)
	{
		Configuration cfg = ConfigUtils.getConfig("reflectionmedic");
		String c = CATEGORY_GENERAL;
		String[] plugins = cfg.getStringList("plugins", c, new String[] { "WorldGuard", "GriefPreventionPlus" }, "Plugins for sending events");
		boolean pluginHooking = cfg.getBoolean("pluginHooking", c, true, "Hooking plugins (allow checking regions)");
		craftPackage = cfg.getString("craftPackage", c, craftPackage, "CraftBukkit package (for reflection)");
		explosions = cfg.getBoolean("explosions", c, explosions, "Explosions enabled");
		debug = cfg.getBoolean("debug", c, debug, "Debugging enabled");
		cfg.save();

		PluginManager plManager = Bukkit.getPluginManager();
		for (String plName : plugins)
		{
			Plugin plugin = plManager.getPlugin(plName);
			if (plugin == null)
				LOGGER.warn("Plugin {} not found!", plName);
			else
				listeners.addAll(HandlerList.getRegisteredListeners(plugin));
		}
		if (pluginHooking)
			InjectionManager.init();
	}

	public static void callEvent(Event event)
	{
		for (RegisteredListener listener : listeners)
		{
			try
			{
				listener.callEvent(event);
			}
			catch (Throwable throwable)
			{
				if (debug)
					LOGGER.error("Failed event call", throwable);
			}
		}
	}

	public static void error(Throwable throwable, String message, Object... args)
	{
		if (debug)
			LOGGER.error(new FormattedMessage(message, args), throwable);
		else
			LOGGER.error(message, args);
	}
}
