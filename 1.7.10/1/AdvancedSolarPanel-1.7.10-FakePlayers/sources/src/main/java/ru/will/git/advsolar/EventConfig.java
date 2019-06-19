package ru.will.git.advsolar;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

public final class EventConfig
{
	public static int qGenMaxProduction = 32000;
	public static int qGenMaxPacket = 32000;

	static
	{
		try
		{
			File mainDirectory = FMLCommonHandler.instance().getMinecraftServerInstance().getFile(".");
			Configuration config = new Configuration(new File(mainDirectory, "config/Events/AdvSolar.cfg"));
			config.load();
			qGenMaxProduction = config.getInt("qGenMaxProduction", CATEGORY_GENERAL, qGenMaxProduction, 1, Integer.MAX_VALUE, "Квантовый генератор - максимальная генерация");
			qGenMaxPacket = config.getInt("qGenMaxPacket", CATEGORY_GENERAL, qGenMaxPacket, 1, Integer.MAX_VALUE, "Квантовый генератор - максимальный размер пакета энергии");
			config.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}