package ru.will.git.reflectionmedic.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

import static ru.will.git.reflectionmedic.ModConstants.COREMOD_NAME;
import static ru.will.git.reflectionmedic.ModConstants.MC_VERSION;

// Dummy coremod to prevent NoClassDefFoundError at AutomaticEventSubscriber work state (FMLConstructionEvent)
@IFMLLoadingPlugin.Name(COREMOD_NAME)
@IFMLLoadingPlugin.MCVersion(MC_VERSION)
public final class CoreMod implements IFMLLoadingPlugin
{
	@Override
	public String[] getASMTransformerClass()
	{
		return new String[0];
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Nullable
	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
