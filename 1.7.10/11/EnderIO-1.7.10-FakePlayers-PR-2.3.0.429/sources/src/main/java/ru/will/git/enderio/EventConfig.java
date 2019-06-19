package ru.will.git.enderio;

import ru.will.git.reflectionmedic.util.FastUtils;
import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static boolean blinkEvent = true;
	public static boolean spawnerIgnoreCapacitorType = false;
	public static boolean spawnerDisableWitcherSkeletons = false;
	public static boolean crafterIgnoreCapacitorType = false;

	static
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("EnderIO");
			blinkEvent = cfg.getBoolean("blinkEvent", "events", blinkEvent, "Ивент для телепортации с помощью меча");
			spawnerIgnoreCapacitorType = cfg.getBoolean("spawnerIgnoreCapacitorType", "other", spawnerIgnoreCapacitorType, "Игнорировать тип конденсатора в спавнере");
			spawnerDisableWitcherSkeletons = cfg.getBoolean("spawnerDisableWitcherSkeletons", "other", spawnerDisableWitcherSkeletons, "Выключить спавн скелетов-иссушителей в спавнере");
			crafterIgnoreCapacitorType = cfg.getBoolean("crafterIgnoreCapacitorType", "other", crafterIgnoreCapacitorType, "Игнорировать тип конденсатора в крафтере");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}