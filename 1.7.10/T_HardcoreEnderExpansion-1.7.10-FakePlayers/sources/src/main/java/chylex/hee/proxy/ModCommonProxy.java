package chylex.hee.proxy;

import chylex.hee.mechanics.compendium.player.PlayerCompendiumData;
import chylex.hee.system.ConfigHandler;
import ru.will.git.hee.EventConfig;
import net.minecraft.entity.player.EntityPlayer;

public class ModCommonProxy
{
	public static boolean opMobs;
	public static boolean hardcoreEnderbacon;
	public static int achievementStartId;
	public static int renderIdObsidianSpecial;
	public static int renderIdFlowerPot;
	public static int renderIdSpookyLeaves;
	public static int renderIdCrossedDecoration;
	public static int renderIdVoidChest;
	public static int renderIdTransportBeacon;

	public void loadConfiguration()
	{
		ConfigHandler.loadGeneral();

		    
		EventConfig.init();
		    
	}

	public EntityPlayer getClientSidePlayer()
	{
		return null;
	}

	public PlayerCompendiumData getClientCompendiumData()
	{
		return null;
	}

	public void registerRenderers()
	{
	}

	public void registerSidedEvents()
	{
	}

	public void openGui(String type)
	{
	}

	public void sendMessage(ModCommonProxy.MessageType msgType, int[] data)
	{
	}

	public enum MessageType
	{
		DEBUG_TITLE_SET,
		TRANSPORT_BEACON_GUI,
		ENHANCEMENT_SLOT_RESET
	}
}
