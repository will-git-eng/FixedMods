package ru.will.git.clientwg;

import ru.will.git.clientwg.network.NetworkManager;
import ru.will.git.clientwg.util.ProtectedRegion;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.List;

import static ru.will.git.clientwg.ModConstants.*;

@Mod(modid = MODID, name = NAME, version = VERSION)
public final class ClientWG
{
	@Mod.Instance
	public static ClientWG instance;

	public final Config config = new Config();
	public final List<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		NetworkManager.register();
		ProtectionHandler.register();
	}
}
