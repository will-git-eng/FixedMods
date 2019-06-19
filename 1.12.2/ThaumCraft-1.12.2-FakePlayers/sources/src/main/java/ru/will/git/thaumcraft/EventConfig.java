package ru.will.git.thaumcraft;

import ru.will.git.eventhelper.config.*;
import net.minecraftforge.common.config.Configuration;

@Config(name = "ThaumCraft")
public final class EventConfig
{
	private static final String CATEGORY_BLACKLIST = "blacklist";
	private static final String CATEGORY_POTION = "potion";
	private static final String CATEGORY_OTHER = "other";
	private static final String CATEGORY_OTHER_LIQUID_DEATH = CATEGORY_OTHER + Configuration.CATEGORY_SPLITTER + "liquidDeath";
	private static final String CATEGORY_PROTECTION = "protection";

	@ConfigItemBlockList(name = "golemSealUse",
						 category = CATEGORY_BLACKLIST,
						 comment = "Чёрный список блоков и предметов для Печати контроля 'Использование'")
	public static final ItemBlockList golemCoreUseBlackList = new ItemBlockList(true);

	@ConfigItemBlockList(name = "focusBreak",
						 category = CATEGORY_BLACKLIST,
						 comment = "Чёрный список блоков для Фокуса 'Разрушение'")
	public static final ItemBlockList focusBreakBlackList = new ItemBlockList(true);

	@ConfigItemBlockList(name = "runicMatrix",
						 category = CATEGORY_BLACKLIST,
						 comment = "Чёрный список блоков и предметов для Рунической матрицы")
	public static final ItemBlockList runicMatrixBlackList = new ItemBlockList(true);

	@ConfigBoolean(category = CATEGORY_POTION, comment = "Включить эффект 'Яд от заражения'")
	public static boolean potionFluxTaint = true;

	@ConfigBoolean(category = CATEGORY_POTION, comment = "Включить эффект 'Вирус Выбросов'")
	public static boolean potionVisExhaust = true;

	@ConfigBoolean(category = CATEGORY_POTION, comment = "Включить эффект 'Бактериальный вирус Выбросов'")
	public static boolean potionInfectiousVisExhaust = true;

	@ConfigBoolean(category = CATEGORY_POTION, comment = "Включить эффект 'Неестественный голод'")
	public static boolean potionUnhunger = true;

	@ConfigBoolean(category = CATEGORY_POTION, comment = "Включить эффект 'Защищённый от искажения'")
	public static boolean potionWarpWard = true;

	@ConfigBoolean(category = CATEGORY_POTION, comment = "Включить эффект 'Смертельный взгляд'")
	public static boolean potionDeathGaze = true;

	@ConfigBoolean(category = CATEGORY_POTION, comment = "Включить эффект 'Размытое зрение'")
	public static boolean potionBlurredVision = true;

	@ConfigBoolean(category = CATEGORY_POTION, comment = "Включить эффект 'Презирающий Солнце'")
	public static boolean potionSunScorned = true;

	@ConfigBoolean(category = CATEGORY_POTION, comment = "Включить эффект 'Таумория'")
	public static boolean potionThaumarhia = true;

	@ConfigBoolean(category = CATEGORY_OTHER, comment = "Включить Бутылку с заражением")
	public static boolean enableBottleTaint = true;

	@ConfigBoolean(category = CATEGORY_OTHER, comment = "Включить Разлом от Выбросов")
	public static boolean enableFluxRift = true;

	@ConfigBoolean(category = CATEGORY_OTHER, comment = "Включить Семя заражения")
	public static boolean enableTaintSeed = true;

	@ConfigBoolean(name = "playerAspectDrop",
				   category = CATEGORY_OTHER_LIQUID_DEATH,
				   comment = "Включить дроп аспектов при смерти игрока от Жидкой смерти")
	public static boolean enablePlayerLiquidDeathAspectDrop = true;

	@ConfigFloat(name = "aspectDropChance",
				 category = CATEGORY_OTHER_LIQUID_DEATH,
				 comment = "Шанс дропа аспектов при смерти моба от Жидкой смерти",
				 min = 0,
				 max = 1)
	public static float liquidDeathAspectDropChance = 1;

	@ConfigBoolean(category = CATEGORY_PROTECTION,
				   comment = "Запрет взаимодействия Тигля с предметами посторонних игроков")

	public static boolean protectCrucible = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запрет взаимодействия Тигля с предметами без валидной информации о владельце")
	public static boolean paranoidDropProtection = false;

}
