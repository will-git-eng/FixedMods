package ru.will.git.immersiveengineering;

import ru.will.git.reflectionmedic.util.FastUtils;
import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static boolean logDimensionConnectionsRemoving = false;
	public static boolean preventDimensionConnectionsRemoving = false;

	static
	{
		init();
	}

	public static void init()
	{
		try
		{
			final Configuration cfg = FastUtils.getConfig("ImmersiveEnginnering");
			String c = Configuration.CATEGORY_GENERAL;

			logDimensionConnectionsRemoving = cfg.getBoolean("logDimensionConnectionsRemoving", c, logDimensionConnectionsRemoving, "Логирование удаления проводов при отсутствии их игрового мира");
			preventDimensionConnectionsRemoving = cfg.getBoolean("preventDimensionConnectionsRemoving", c, preventDimensionConnectionsRemoving, "Запрет удаления проводов при отсутствии их игрового мира");

			cfg.save();
		}
		catch (final Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}
