package ru.will.git.draconicevolution;

import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class EventConfig
{
	public static int staffMaxRange = 11;
	public static int pickaxeMaxRange = 11;

	public static void load()
	{
		try
		{
			Configuration cfg = new Configuration(new File(Loader.instance().getConfigDir(), "Events/DraconicEvolution.cfg"));
			String c = Configuration.CATEGORY_GENERAL;

			staffMaxRange = cfg.getInt("staffMaxRange", c, staffMaxRange, 0, Integer.MAX_VALUE, "Максимальный радиус разрушения посохом");
			pickaxeMaxRange = cfg.getInt("pickaxeMaxRange", c, pickaxeMaxRange, 0, Integer.MAX_VALUE, "Максимальный радиус разрушения киркой");

			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	static
	{
		load();
	}
}
