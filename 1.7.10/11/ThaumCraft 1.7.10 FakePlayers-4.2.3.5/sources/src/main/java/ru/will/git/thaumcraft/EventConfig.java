package ru.will.git.thaumcraft;

import ru.will.git.reflectionmedic.util.FastUtils;
import com.google.common.collect.Sets;
import net.minecraftforge.common.config.Configuration;

import java.util.Set;

public final class EventConfig
{
	private static final String[] DEFAULT_BLOCKS = { "minecraft:bedrock", "modid:blockname:meta" };
	public static final ItemBlockList golemCoreUseBlackList = new ItemBlockList();
	public static final ItemBlockList excavationFocusBlackList = new ItemBlockList();
	public static final ItemBlockList runicMatrixBlackList = new ItemBlockList();
	public static boolean blockEldritchNothing = true;
	public static boolean invincibleGolems = true;
	public static boolean primalCrusherPlayerOnly = true;
	public static boolean allWorldGen = false;
	public static boolean enableFocusWarding = true;
	public static boolean enableFocusTrade = true;
	public static boolean customChampionsNaming = false;
	public static boolean registerFluxGas = false;
	public static boolean advCrucibleDupeFix = false;
	public static boolean obeliskPlacerCreativeOnly = false;
	public static int fluxScrubberSkipTicks = 0;
	public static int visRelayUpdatePeriod = 0;
	public static boolean protectCrucible = false;

	public static boolean validateThaumometer = false;
	public static int thaumometerCooldown = 23;

	public static boolean enableUnhunger = true;
	public static boolean enableDeathGase = true;
	public static boolean enableBlurredVision = true;
	public static boolean enableVisExhaust = true;
	public static boolean enableInfectiousVisExhaust = true;
	public static boolean enableThaumarhia = true;
	public static boolean enableSunScorned = true;

	public static boolean enableBottleTaint = true;
	public static boolean enablePlayerFluidDeathAspectDrop = true;
	public static float fluidDeathAspectDropChance = 1;

	public static String golemRemovePermission = "thaumcraft.golemremove";

	public static boolean golemAiOptimize = false;
	public static boolean itemAspectMapOptimize = false;

	public static boolean fixFocusDupe = false;
	public static boolean infusionStrictShardCheck = false;

	static
	{
		init();
	}

	public static void init()
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("ThaumCraft");
			String c = Configuration.CATEGORY_GENERAL;

			readItemBlockList(cfg, "golemCoreUseBlackList", c, "Чёрный список блоков и предметов для Сердца голема: Использование", golemCoreUseBlackList, DEFAULT_BLOCKS);
			readItemBlockList(cfg, "excavationFocusBlackList", c, "Чёрный список блоков для Набалдашника: Разрушение", excavationFocusBlackList, DEFAULT_BLOCKS);
			readItemBlockList(cfg, "runicMatrixBlackList", c, "Чёрный список блоков и предметов для Рунической матрицы", runicMatrixBlackList, DEFAULT_BLOCKS);

			blockEldritchNothing = cfg.getBoolean("blockEldritchNothing", c, blockEldritchNothing, "Включить BlockEldritchNothing");
			invincibleGolems = cfg.getBoolean("invincibleGolems", c, invincibleGolems, "Бессмертие для големов");
			primalCrusherPlayerOnly = cfg.getBoolean("primalCrusherPlayerOnly", c, primalCrusherPlayerOnly, "Сингулярный крушитель может использоваться только игроками (защита от дюпа с OpenComputers)");
			allWorldGen = cfg.getBoolean("allWorldGen", c, allWorldGen, "Генерация во всех мирах (кроме Ада и Края)");
			enableFocusWarding = cfg.getBoolean("enableFocusWarding", c, enableFocusWarding, "Включить Набалдашник: Укрепление");
			enableFocusTrade = cfg.getBoolean("enableFocusTrade", c, enableFocusTrade, "Включить Набалдашник: Равноценный обмен");
			customChampionsNaming = cfg.getBoolean("customChampionsNaming", c, customChampionsNaming, "Добавлять индвидуальные имена для мобов-чемпионов");
			registerFluxGas = cfg.getBoolean("registerFluxGas", c, registerFluxGas, "Регистрация FluxGas как жидкости (защита от краша с раздатчиком)");
			advCrucibleDupeFix = cfg.getBoolean("advCrucibleDupeFix", c, advCrucibleDupeFix, "Расширенный фикс дюпа воды с Тиглем");
			obeliskPlacerCreativeOnly = cfg.getBoolean("obeliskPlacerCreativeOnly", c, obeliskPlacerCreativeOnly, "Eldritch Obelisk Placer только для творческого режима");
			fluxScrubberSkipTicks = cfg.getInt("fluxScrubberSkipTicks", c, fluxScrubberSkipTicks, 0, Integer.MAX_VALUE, "Пропуск тиков для Полотёра порчи (0 - выключено)");
			visRelayUpdatePeriod = cfg.getInt("visRelayUpdatePeriod", c, visRelayUpdatePeriod, 0, Integer.MAX_VALUE, "Период автоматического перестроения связей Вис-канала (0 - выключено)");
			protectCrucible = cfg.getBoolean("protectCrucible", c, protectCrucible, "Запрет взаимодействия Тигля с предметами посторонних игроков");

			validateThaumometer = cfg.getBoolean("validateThaumometer", c, validateThaumometer, "Проверять корректность использования Таумометра (защита от мгновенного исследования всех предметов в игре) (может слегка снизить точность 'прицеливания' Таумометром) (может нарушить работу Таумометра)");
			thaumometerCooldown = cfg.getInt("thaumometerCooldown", c, thaumometerCooldown, 0, Integer.MAX_VALUE, "Кулдаун для Таумометра в тиках (0 - нет кулдауна)");

			enableUnhunger = cfg.getBoolean("enableUnhunger", c, enableUnhunger, "Включить эффект Странный голод");
			enableDeathGase = cfg.getBoolean("enableDeathGase", c, enableDeathGase, "Включить эффект Смертельный взгляд");
			enableBlurredVision = cfg.getBoolean("enableBlurredVision", c, enableBlurredVision, "Включить эффект Затуманенное зрение");
			enableVisExhaust = cfg.getBoolean("enableVisExhaust", c, enableVisExhaust, "Включить эффект Магическое истощение");
			enableInfectiousVisExhaust = cfg.getBoolean("enableInfectiousVisExhaust", c, enableInfectiousVisExhaust, "Включить эффект Магическая зараза");
			enableThaumarhia = cfg.getBoolean("enableThaumarhia", c, enableThaumarhia, "Включить эффект Таумария");
			enableSunScorned = cfg.getBoolean("enableSunScorned", c, enableSunScorned, "Включить эффект Солнечная болезнь");

			enableBottleTaint = cfg.getBoolean("enableBottleTaint", c, enableBottleTaint, "Включить Порчу в бутылке");
			enablePlayerFluidDeathAspectDrop = cfg.getBoolean("enablePlayerFluidDeathAspectDrop", c, enablePlayerFluidDeathAspectDrop, "Включить дроп аспектов при смерти игрока от Жидкой смерти");
			fluidDeathAspectDropChance = cfg.getFloat("fluidDeathAspectDropChance", c, fluidDeathAspectDropChance, 0, 1, "Шанс дропа аспектов при смерти моба от Жидкой смерти");

			golemRemovePermission = cfg.getString("golemRemovePermission", c, golemRemovePermission, "Permission для удаления чужих големов");

			golemAiOptimize = cfg.getBoolean("golemAiOptimize", c, golemAiOptimize, "Оптимизировать AI големов (небезопасно)");
			itemAspectMapOptimize = cfg.getBoolean("itemAspectMapOptimize", c, itemAspectMapOptimize, "Оптимизировать поиск аспектов для предметов (небезопасно)");

			fixFocusDupe = cfg.getBoolean("fixFocusDupe", c, fixFocusDupe, "Исправить дюп жезлов при использовании некоторых фокусов (небезопасно)");
			infusionStrictShardCheck = cfg.getBoolean("infusionStrictShardCheck", c, infusionStrictShardCheck, "Проверять subID кристаллов при использовании Рунической матрицы");

			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	private static void readItemBlockList(Configuration cfg, String name, String category, String comment, ItemBlockList list, String... def)
	{
		Set<String> data = Sets.newHashSet(def);
		readStringSet(cfg, name, category, comment, data);
		list.addRaw(data);
	}

	private static void readStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static Set<String> getStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[0]));
	}

	private static Set<String> getStringSet(Configuration cfg, String name, String category, String comment, String... def)
	{
		return Sets.newHashSet(cfg.getStringList(name, category, def, comment));
	}
}
