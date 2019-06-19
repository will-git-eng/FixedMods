package ru.will.git.am2;

import java.io.File;

import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static boolean denyStackSpell = true;

	static
	{
		try
		{
			File cfgDir = new File(Loader	.instance()
											.getConfigDir(), "Events");
			cfgDir.mkdirs();
			Configuration cfg = new Configuration(new File(cfgDir, "AM2.cfg"));
			cfg.load();
			denyStackSpell = cfg.getBoolean("denyStackSpell", "general", denyStackSpell, "Запретить стакать свитки заклинаний (защита от дюпа)");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}
