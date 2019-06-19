package ru.will.git.forestry;

import ru.will.git.eventhelper.config.Config;
import ru.will.git.eventhelper.config.ConfigBoolean;
import ru.will.git.eventhelper.config.ConfigUtils;

@Config(name = "Forestry")
public final class EventConfig
{
	private static final String CATEGORY_EVENTS = "events";
	private static final String CATEGORY_EFFECTS = "effects";
	private static final String CATEGORY_OTHER = "other";

	@ConfigBoolean(name = "farm", category = CATEGORY_EVENTS, comment = "Фермы (установка и разрушение блоков)")
	public static boolean eventFarm = true;

	@ConfigBoolean(name = "aggressive", category = CATEGORY_EVENTS, comment = "Aggressive (нанесение урона)")
	public static boolean eventAggressive = true;

	@ConfigBoolean(name = "creeper", category = CATEGORY_EVENTS, comment = "Creeper (взрыв)")
	public static boolean eventCreeper = true;

	@ConfigBoolean(name = "exploration", category = CATEGORY_EVENTS, comment = "Exploration (генерация опыта)")
	public static boolean eventExploration = false;

	@ConfigBoolean(name = "radioactive",
				   category = CATEGORY_EVENTS,
				   comment = "Radioactive (нанесение урона и разрушение блоков)")
	public static boolean eventRadioactive = true;

	@ConfigBoolean(name = "aggressive", category = CATEGORY_EFFECTS, comment = "Aggressive (нанесение урона)")
	public static boolean enableAggressive = true;

	@ConfigBoolean(name = "creeper", category = CATEGORY_EFFECTS, comment = "Creeper (взрыв)")
	public static boolean enableCreeper = true;

	@ConfigBoolean(name = "exploration", category = CATEGORY_EFFECTS, comment = "Exploration (генерация опыта)")
	public static boolean enableExploration = true;

	@ConfigBoolean(name = "radioactive",
				   category = CATEGORY_EFFECTS,
				   comment = "Radioactive (нанесение урона и разрушение блоков)")
	public static boolean enableRadioactive = true;

	@ConfigBoolean(name = "ressuration", category = CATEGORY_EFFECTS, comment = "Ressuration (опасно) (спавн мобов)")
	public static boolean enableRessuration = false;

	@ConfigBoolean(name = "enableApiaryFramesAutomation",
				   category = CATEGORY_OTHER,
				   comment = "Разрешить автоматизацию пополнения рамок в пасеках")
	public static boolean enableApiaryFramesAutomation = false;

	static
	{
		ConfigUtils.readConfig(EventConfig.class);
	}
}
