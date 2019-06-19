package tconstruct.common;

import ru.will.git.tconstruct.EventConfig;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.HashMap;

public class TProxyCommon implements IGuiHandler
{
	public void initialize()
    
    
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		IGuiHandler handler = serverGuiHandlers.get(ID);
		if (handler != null)
			return handler.getServerGuiElement(ID, player, world, x, y, z);
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		IGuiHandler handler = clientGuiHandlers.get(ID);
		if (handler != null)
			return handler.getClientGuiElement(ID, player, world, x, y, z);
		return null;
	}

	private static HashMap<Integer, IGuiHandler> serverGuiHandlers = new HashMap<Integer, IGuiHandler>();
	private static HashMap<Integer, IGuiHandler> clientGuiHandlers = new HashMap<Integer, IGuiHandler>();

	public static void registerServerGuiHandler(int gui, IGuiHandler handler)
	{
		serverGuiHandlers.put(gui, handler);
	}

	public static void registerClientGuiHandler(int gui, IGuiHandler handler)
	{
		clientGuiHandlers.put(gui, handler);
	}
}
