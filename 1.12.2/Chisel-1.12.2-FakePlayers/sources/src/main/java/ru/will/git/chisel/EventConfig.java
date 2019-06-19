package ru.will.git.chisel;

import ru.will.git.eventhelper.config.Config;
import ru.will.git.eventhelper.config.ConfigItemBlockList;
import ru.will.git.eventhelper.config.ConfigUtils;
import ru.will.git.eventhelper.config.ItemBlockList;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

@Config(name = "Chisel")
public final class EventConfig
{
	private static final String CATEGORY_BLACKLISTS = "blacklists";

	@ConfigItemBlockList(name = "chisel",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список блоков для Стамески",
						 oldName = "chiselBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList chiselBlackList = new ItemBlockList();

	static
	{
		ConfigUtils.readConfig(EventConfig.class);
	}
}