package ru.will.git.ic2;

import ru.will.git.eventhelper.config.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;

@Config(name = "IC2")
public final class EventConfig
{
	private static final String CATEGORY_EVENTS = "events";
	private static final String CATEGORY_OTHER = "other";
	private static final String CATEGORY_BLACKLISTS = "blacklists";
	private static final String CATEGORY_ENERGY = "energy";
	private static final String CATEGORY_LASER = "laser";
	private static final String CATEGORY_LASER_ENERGY = CATEGORY_LASER + Configuration.CATEGORY_SPLITTER + CATEGORY_ENERGY;

	@ConfigItemBlockList(name = "tradeOMat",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список предметов для Обменного аппарата")
	public static final ItemBlockList tradeOMatBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "scanner",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список предметов для Сканера")
	public static final ItemBlockList scannerBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "minerPlace",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список устанавливаемых блоков для Буровой установки")
	public static final ItemBlockList minerPlaceBlackList = new ItemBlockList(true);

	@ConfigItemBlockList(name = "minerBreak",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список разрушаемых блоков для Буровой установки")
	public static final ItemBlockList minerBreakBlackList = new ItemBlockList();

	@ConfigBoolean(name = "terra", category = CATEGORY_EVENTS, comment = "Терраформер (замена/установка блоков)")
	public static boolean terraEvents = true;

	@ConfigBoolean(name = "pump", category = CATEGORY_EVENTS, comment = "Помпа (выкачивание жидкости)")
	public static boolean pumpEvent = true;

	@ConfigBoolean(name = "miner", category = CATEGORY_EVENTS, comment = "Буровая установка (разрушение блоков)")
	public static boolean minerEvent = true;

	@ConfigBoolean(name = "advminer",
				   category = CATEGORY_EVENTS,
				   comment = "Продвинутая буровая установка (разрушение блоков)")
	public static boolean advminerEvent = true;

	@ConfigBoolean(name = "tesla", category = CATEGORY_EVENTS, comment = "Катушка Теслы (урон по мобам)")
	public static boolean teslaEvent = true;

	@ConfigBoolean(name = "sprayer", category = CATEGORY_EVENTS, comment = "Пульверизатор (установка блоков)")
	public static boolean sprayerEvent = true;

	@ConfigBoolean(name = "laser",
				   category = CATEGORY_EVENTS,
				   comment = "Шахтёрский лазер (разрушение блоков и урон по мобам)")
	public static boolean laserEvent = true;

	@ConfigBoolean(name = "plasma", category = CATEGORY_EVENTS, comment = "Плазменная пушка (взрыв)")
	public static boolean plasmaEvent = true;

	@ConfigBoolean(name = "explosion", category = CATEGORY_EVENTS, comment = "Взрывы (разрушение блоков)")
	public static boolean explosionEvent = true;

	@ConfigInt(name = "maxBreakY",
			   category = CATEGORY_LASER,
			   comment = "Максимальная высота, на которой может работать Шахтёрский лазер",
			   min = 1)
	public static int laserMaxBreakY = 255;

	@ConfigString(name = "maxBreakYWarnMsg",
				  category = CATEGORY_LASER,
				  comment = "Предупреждение, отправляемое игроку при попытке использовать Шахтёрский лазер выше максимальной высоты")
	public static String laserMaxBreakYWarnMsg = TextFormatting.RED + "Шахтёрский лазер нельзя использовать на высоте выше %d";

	@ConfigBoolean(name = "scatterEnabled",
				   category = CATEGORY_LASER,
				   comment = "Шахтёрский лазер (режим 'Разброс')",
				   oldCategory = CATEGORY_OTHER)
	public static boolean laserScatterEnabled = true;

	@ConfigBoolean(name = "explosiveEnabled",
				   category = CATEGORY_LASER,
				   comment = "Шахтёрский лазер (режим 'Взрывоопасный')",
				   oldCategory = CATEGORY_OTHER)
	public static boolean laserExplosiveEnabled = true;

	@ConfigBoolean(name = "superHeatEnabled",
				   category = CATEGORY_LASER,
				   comment = "Шахтёрский лазер (режим 'Перегревающий')",
				   oldCategory = CATEGORY_OTHER)
	public static boolean laserSuperHeatEnabled = true;

	@ConfigBoolean(name = "longRangeEnabled",
				   category = CATEGORY_LASER,
				   comment = "Шахтёрский лазер (режим 'Дальнего действия')",
				   oldCategory = CATEGORY_OTHER)
	public static boolean laserLongRangeEnabled = true;

	@ConfigBoolean(name = "miningEnabled",
				   category = CATEGORY_LASER,
				   comment = "Шахтёрский лазер (режим 'Добыча')",
				   oldCategory = CATEGORY_OTHER)
	public static boolean laserMiningEnabled = true;

	@ConfigBoolean(name = "lowFocusEnabled",
				   category = CATEGORY_LASER,
				   comment = "Шахтёрский лазер (режим 'Короткого фокуса')",
				   oldCategory = CATEGORY_OTHER)
	public static boolean laserLowFocusEnabled = true;

	@ConfigBoolean(name = "horizontalEnabled",
				   category = CATEGORY_LASER,
				   comment = "Шахтёрский лазер (режим 'Горизонтальный')",
				   oldCategory = CATEGORY_OTHER)
	public static boolean laserHorizontalEnabled = true;

	@ConfigBoolean(name = "3x3Enabled",
				   category = CATEGORY_LASER,
				   comment = "Шахтёрский лазер (режим '3x3')",
				   oldCategory = CATEGORY_OTHER)
	public static boolean laser3x3Enabled = true;

	@ConfigInt(name = "mining", category = CATEGORY_LASER_ENERGY, comment = "Энергия для режима 'Добыча'", min = 1)
	public static int laserMiningEnergy = 1250;

	@ConfigInt(name = "focus",
			   category = CATEGORY_LASER_ENERGY,
			   comment = "Энергия для режима 'Короткого фокуса'",
			   min = 1)
	public static int laserLowFocusEnergy = 100;

	@ConfigInt(name = "longRange",
			   category = CATEGORY_LASER_ENERGY,
			   comment = "Энергия для режима 'Дальнего действия'",
			   min = 1)
	public static int laserLongRangeEnergy = 5000;

	@ConfigInt(name = "horizontal",
			   category = CATEGORY_LASER_ENERGY,
			   comment = "Энергия для режима 'Горизонтальный'",
			   min = 1)
	public static int laserHorizontalEnergy = 3000;

	@ConfigInt(name = "superHeat",
			   category = CATEGORY_LASER_ENERGY,
			   comment = "Энергия для режима 'Перегревающий'",
			   min = 1)
	public static int laserSuperHeatEnergy = 2500;

	@ConfigInt(name = "scatter", category = CATEGORY_LASER_ENERGY, comment = "Энергия для режима 'Разброс'", min = 1)
	public static int laserScatterEnergy = 10000;

	@ConfigInt(name = "explosive",
			   category = CATEGORY_LASER_ENERGY,
			   comment = "Энергия для режима 'Взрывоопасный'",
			   min = 1)
	public static int laserExplosiveEnergy = 5000;

	@ConfigInt(name = "3x3", category = CATEGORY_LASER_ENERGY, comment = "Энергия для режима '3x3'", min = 1)
	public static int laser3x3Energy = 7500;

	@ConfigBoolean(category = CATEGORY_OTHER, comment = "Радиация")
	public static boolean radiationEnabled = true;

	@ConfigBoolean(category = CATEGORY_OTHER, comment = "Установка блоков Буровой установкой")
	public static boolean minerPlacingEnabled = true;

	@ConfigBoolean(category = CATEGORY_OTHER, comment = "Автоматическая привязка персональных блоков к владельцу")
	public static boolean personalBlockAutoPrivateEnabled = false;

	@ConfigString(category = CATEGORY_OTHER,
				  comment = "Permission для доступа к персональным блокам (сейфам, торговым аппаратам и пр.)")
	public static String safeAccessPermission = "ic2.accesssafe";

	@ConfigInt(category = CATEGORY_OTHER,
			   comment = "Максимальное количество Ускорителей в механизме (0 - без ограничений)",
			   min = 0)
	public static int maxOverclockerCount = 0;
}
