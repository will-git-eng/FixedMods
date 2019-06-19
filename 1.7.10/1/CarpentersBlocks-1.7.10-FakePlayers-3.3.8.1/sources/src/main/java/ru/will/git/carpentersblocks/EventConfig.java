package ru.will.git.carpentersblocks;

import ru.will.git.reflectionmedic.util.FastUtils;

import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static String safeAccessPermission = "carpentersblocks.accesssafe";

	static
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("CarpentersBlocks");
			safeAccessPermission = cfg.getString("safeAccessPermission", "other", safeAccessPermission, "Permission для доступа к сейфу");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}