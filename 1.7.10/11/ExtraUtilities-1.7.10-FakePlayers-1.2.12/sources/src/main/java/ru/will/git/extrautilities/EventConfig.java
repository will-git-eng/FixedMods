package ru.will.git.extrautilities;

import ru.will.git.reflectionmedic.util.FastUtils;
import net.minecraftforge.common.config.Configuration;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

public final class EventConfig
{
	public static int enderQuarryMaxSize = 64;
	public static boolean enderQuarryOnlyPrivate = false;
	public static boolean enderPumpOnlyPrivate = false;

	public static boolean skipTicksTransferNode = false;
	public static int skipTicksAmountTransferNode = 1;

	public static boolean cursedEarthSpreading = true;
	public static boolean kikokuBypassArmor = true;

	public static void init()
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("ExtraUtilities");
			String c = CATEGORY_GENERAL;
			enderQuarryMaxSize = cfg.getInt("enderQuarryMaxSize", c, enderQuarryMaxSize, 3, Integer.MAX_VALUE, "Максимальный размер Карьера Края");
			enderQuarryOnlyPrivate = cfg.getBoolean("enderQuarryOnlyPrivate", c, enderQuarryOnlyPrivate, "Карьер может работать только в привате владельца");
			enderPumpOnlyPrivate = cfg.getBoolean("enderPumpOnlyPrivate", c, enderPumpOnlyPrivate, "Помпа может работать только в привате владельца");
			skipTicksTransferNode = cfg.getBoolean("skipTicksTransferNode", c, skipTicksTransferNode, "Пропускать тики при обработке сетей труб");
			skipTicksAmountTransferNode = cfg.getInt("skipTicksAmountTransferNode", c, skipTicksAmountTransferNode, 1, Integer.MAX_VALUE, "Количество пропускаемых тиков");
			cursedEarthSpreading = cfg.getBoolean("cursedEarthSpreadingNode", c, cursedEarthSpreading, "Распространение Проклятой земли");
			kikokuBypassArmor = cfg.getBoolean("kikokuBypassArmor", c, kikokuBypassArmor, "Включить игнорирование брони Kikoku");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}