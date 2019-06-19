package ru.will.git.ae;

import ru.will.git.eventhelper.config.*;
import net.minecraftforge.common.config.Configuration;

@Config(name = "AppEng")
public final class EventConfig
{
	private static final String CATEGORY_BLACKLIST = "blacklist";
	private static final String CATEGORY_OTHER = "other";
	private static final String CATEGORY_PERFORMANCE = "performance";
	private static final String CATEGORY_PERFORMANCE_CRAFT_TERM = CATEGORY_PERFORMANCE + Configuration.CATEGORY_SPLITTER + "craftTerm";

	@ConfigItemBlockList(name = "annihilationPlane",
						 category = CATEGORY_BLACKLIST,
						 comment = "Чёрный список блоков для МЭ плоскости истребления")
	public static final ItemBlockList annihilationPlaneBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "formationPlane",
						 category = CATEGORY_BLACKLIST,
						 comment = "Чёрный список блоков для МЭ плоскости формирования")
	public static final ItemBlockList formationPlaneBlackList = new ItemBlockList();

	@ConfigBoolean(category = CATEGORY_PERFORMANCE,
				   comment = "Использовать сочетание ConcurrentSkipListMap и ConcurrentHashMap в ItemList (может повысить производительность) (небезопасно - возможна 'рассинхронизация' этих двух коллекций)")
	public static boolean useHybridItemList = false;

	@ConfigInt(name = "cooldown",
			   category = CATEGORY_PERFORMANCE_CRAFT_TERM,
			   comment = "Кулдаун для попыток крафта предмета в МЭ терминале крафта (в тиках)",
			   min = 0)
	public static int craftTermCooldown = 0;

	@ConfigInt(name = "maxTimesToCraft",
			   category = CATEGORY_PERFORMANCE_CRAFT_TERM,
			   comment = "Максимальное количество попыток крафта предмета за один клик в МЭ терминале крафта",
			   min = 0)
	public static int craftTermMaxTimesToCraft = Integer.MAX_VALUE;

	@ConfigBoolean(category = CATEGORY_OTHER, comment = "Дроп содержимого блоков-контейнеров при их разрушении")
	public static boolean dropInvOnBreak = true;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Расходовать предметы в МЭ плоскости формирования только при успешном дропе (может стать причиной дюпа)")
	public static boolean formationPlaneDropCheck = true;

	static
	{
		ConfigUtils.readConfig(EventConfig.class);
	}
}
