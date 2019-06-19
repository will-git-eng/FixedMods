package ru.will.git.botania;

import ru.will.git.eventhelper.config.Config;
import ru.will.git.eventhelper.config.ConfigBoolean;
import ru.will.git.eventhelper.config.ConfigUtils;

@Config(name = "Botania")
public final class EventConfig
{
	private static final String CATEGORY_OTHER = "other";

	@ConfigBoolean(category = CATEGORY_OTHER, comment = "Выключить урон посторонним игрокам от реликвий")
	public static boolean relicNoDamage = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запрет взаимодействия Бассейна маны с предметами посторонних игроков")
	public static boolean protectDropManaPool = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запрет взаимодействия Лепесткового аптекаря с предметами посторонних игроков")
	public static boolean protectDropPetalApothecary = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запрет взаимодействия Бассейна маны и/или Лепесткового аптекаря с предметами без валидной информации о владельце")
	public static boolean paranoidDropProtection = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Кнопка 'Поделиться' в Лексике ботании работает только для операторов")
	public static boolean botaniaShareOpsOnly = false;

	static
	{
		ConfigUtils.readConfig(EventConfig.class);
	}
}
