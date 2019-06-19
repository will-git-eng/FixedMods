package thaumicenergistics.common.integration.tc;

import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEFluidStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.*;
import thaumicenergistics.api.IThETransportPermissions;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.common.fluids.GaseousEssentia;
import thaumicenergistics.common.storage.AspectStack;
import thaumicenergistics.common.tiles.TileEssentiaVibrationChamber;
import thaumicenergistics.common.tiles.abstraction.TileEVCBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

    
public final class EssentiaTileContainerHelper
{
    
	public static final EssentiaTileContainerHelper INSTANCE = new EssentiaTileContainerHelper();

    
	public final IThETransportPermissions perms = ThEApi.instance().transportPermissions();

    
	public FluidStack extractFromContainer(final IAspectContainer container, final FluidStack request, final Actionable mode)
    
    
		if (request == null || request.getFluid() == null || request.amount == 0)
    
		if (container == null)
    
    
    
    
		if (!(fluid instanceof GaseousEssentia))
    
    
    
    
    
		if (extractedAmount_EU <= 0)
    
		return new FluidStack(fluid, (int) EssentiaConversionHelper.INSTANCE.convertEssentiaAmountToFluidAmount(extractedAmount_EU));
	}

    
	public long extractFromContainer(final IAspectContainer container, int amountToDrain, final Aspect aspectToDrain, final Actionable mode)
    
    
		if (amountToDrain == 0)
    
		if (container == null)
    

    
		if (aspects == null)
    
    
    
		if (!this.perms.canExtractFromAspectContainerTile(container))
    
    
    
		if (containerAmount == 0)
    
		if (amountToDrain > containerAmount)
    
		if (mode == Actionable.MODULATE)
    
		return amountToDrain;
	}

    
	public Aspect getAspectInContainer(final IAspectContainer container)
    
    
		if (containerStack == null)
			return null;

		return containerStack.getAspect();
	}

	public IAspectStack getAspectStackFromContainer(final IAspectContainer container)
    
		if (container == null)
    
		AspectList aspectList = container.getAspects();

		if (aspectList == null)
    
    
		aspectStack.setAspect(aspectList.getAspectsSortedAmount()[0]);

		if (!aspectStack.hasAspect())
    
		aspectStack.setStackSize(aspectList.getAmount(aspectStack.getAspect()));

		return aspectStack;
	}

    
	public List<IAspectStack> getAspectStacksFromContainer(final IAspectContainer container)
	{
    
		if (container == null)
    
		AspectList aspectList = container.getAspects();

		if (aspectList == null)
    
		for (Entry<Aspect, Integer> essentia : aspectList.aspects.entrySet())
		{
			if (essentia != null && essentia.getValue() != 0)
				stacks.add(new AspectStack(essentia.getKey(), essentia.getValue()));
		}

		return stacks;

	}

    
	public int getContainerCapacity(final IAspectContainer container)
	{
		return this.perms.getAspectContainerTileCapacity(container);
	}

	public int getContainerStoredAmount(final IAspectContainer container)
	{
    
		for (IAspectStack essentia : this.getAspectStacksFromContainer(container))
		{
			if (essentia != null)
				stored += (int) essentia.getStackSize();
		}

		return stored;
	}

    
	public long injectEssentiaIntoContainer(final IAspectContainer container, int amountToFill, final Aspect aspectToFill, final Actionable mode)
    
    
		if (!this.perms.canInjectToAspectContainerTile(container))
    
    
		if (storedEssentia != null && container instanceof TileJarFillable)
    
    
			if (aspectToFill != storedEssentia.getAspect())
				return 0;
		}
    
			if (!container.doesContainerAccept(aspectToFill))
    
    
		if (amountToFill > containerCurrentCapacity)
    
		if (mode == Actionable.MODULATE)
    
    
			amountToFill -= remaining;
		}

		return amountToFill;
	}

    
	public long injectFluidIntoContainer(final IAspectContainer container, final IAEFluidStack fluidStack, final Actionable mode)
    
    
		if (fluidStack == null)
    
    
		if (!this.perms.canInjectToAspectContainerTile(container))
    
    
    
		if (!(fluid instanceof GaseousEssentia))
    
    
    
		long injectedAmount_EU = this.injectEssentiaIntoContainer(container, (int) amountToFill, gasAspect, mode);

		return EssentiaConversionHelper.INSTANCE.convertEssentiaAmountToFluidAmount(injectedAmount_EU);
	}

    
	public void registerDefaultContainers()
    
    
    
		this.perms.addAspectContainerTileToBothPermissions(TileJarFillable.class, 64);
    
    
    
    
		this.perms.addAspectContainerTileToInjectPermissions(TileEssentiaVibrationChamber.class, TileEVCBase.MAX_ESSENTIA_STORED);
	}

}
