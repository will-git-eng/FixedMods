package ru.will.git.emt;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

import ru.will.git.reflectionmedic.util.FastUtils;

import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static int thorHammerCooldown = 1;
	public static int electricThorHammerCooldown = 1;

	static
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("EMT");
			thorHammerCooldown = cfg.getInt("thorHammerCooldown", CATEGORY_GENERAL, thorHammerCooldown, 0, Integer.MAX_VALUE, "Кулдаун для Молота Тора (0 - нет кулдауна) в секундах");
			electricThorHammerCooldown = cfg.getInt("electricThorHammerCooldown", CATEGORY_GENERAL, electricThorHammerCooldown, 0, Integer.MAX_VALUE, "Кулдаун для Электрического молота Тора (0 - нет кулдауна) в секундах");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}