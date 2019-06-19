package ru.will.git.reflectionmedic;

import ru.will.git.reflectionmedic.command.CommandReloadAllConfigs;
import ru.will.git.reflectionmedic.config.ConfigBoolean;
import ru.will.git.reflectionmedic.config.ConfigEnum;
import ru.will.git.reflectionmedic.config.ConfigUtils;
import ru.will.git.reflectionmedic.integration.IntegrationType;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;

import static ru.will.git.reflectionmedic.ModConstants.*;

@Mod(modid = MODID, name = NAME, version = VERSION, acceptableRemoteVersions = "*")
public final class reflectionmedicMod
{
	public static final File CFG_DIR = new File(Loader.instance().getConfigDir(), "Events");
	public static final Logger LOGGER = LogManager.getLogger(NAME);

	@ConfigBoolean(comment = "Enable debugging messages")
	public static boolean debug = true;

	@Nonnull
	@ConfigEnum(comment = "Default API for integration (AUTO, SPONGE, BUKKIT)")
	public static IntegrationType integrationType = IntegrationType.AUTO;

	@ConfigBoolean(comment = "Enable additional checks to grief prevention (may be needed for Bukkit)")
	public static boolean paranoidProtection = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ConfigUtils.readConfig(this.getClass(), NAME);
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandReloadAllConfigs());
	}
}