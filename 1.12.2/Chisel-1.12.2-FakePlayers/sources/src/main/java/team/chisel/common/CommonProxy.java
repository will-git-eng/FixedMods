package team.chisel.common;

import ru.will.git.chisel.EventConfig;
import ru.will.git.eventhelper.config.ConfigUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy implements Reference
{
	public void construct(FMLPreInitializationEvent event)
	{
	}

	public void preInit(FMLPreInitializationEvent event)
	{
		    
		ConfigUtils.readConfig(EventConfig.class);
		    
	}

	public void init()
	{
	}

	public void postInit()
	{
	}

	public void preTextureStitch()
	{
	}

	public void registerTileEntities()
	{
	}

	public World getClientWorld()
	{
		return null;
	}

	public EntityPlayer getClientPlayer()
	{
		return null;
	}
}
