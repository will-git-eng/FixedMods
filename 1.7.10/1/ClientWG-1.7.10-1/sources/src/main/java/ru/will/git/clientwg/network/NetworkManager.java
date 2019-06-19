package ru.will.git.clientwg.network;

import ru.will.git.clientwg.ModConstants;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class NetworkManager
{
	public static void register()
	{
		SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModConstants.MODID);
		wrapper.registerMessage(ConfigMessage.Handler.class, ConfigMessage.class, 0, Side.CLIENT);
		wrapper.registerMessage(RegionsMessage.Handler.class, RegionsMessage.class, 1, Side.CLIENT);
	}
}
