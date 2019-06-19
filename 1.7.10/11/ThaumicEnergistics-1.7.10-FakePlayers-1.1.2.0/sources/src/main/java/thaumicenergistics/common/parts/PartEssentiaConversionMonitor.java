package thaumicenergistics.common.parts;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.security.PlayerSource;
import appeng.client.texture.CableBusTextures;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutablePair;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper.AspectItemType;
import thaumicenergistics.common.storage.AspectStack;
import thaumicenergistics.common.utils.EffectiveSide;

import java.util.Collections;
import java.util.List;

    
public class PartEssentiaConversionMonitor extends PartEssentiaStorageMonitor
{
    
	private static long DOUBLE_CLICK_TICKS = 2 * 20;

    
	private int depositedPlayerID = -1;

    
	private int depositedTick = 0;

    
	private Aspect depositedAspect = null;

	public PartEssentiaConversionMonitor()
	{
		super(AEPartsEnum.EssentiaConversionMonitor);

		this.darkCornerTexture = CableBusTextures.PartConversionMonitor_Colored;
		this.lightCornerTexture = CableBusTextures.PartConversionMonitor_Dark;
	}

    
	private boolean drainEssentiaContainer(final EntityPlayer player, final int slotIndex, final Aspect mustMatchAspect)
    
    
    
		if (request == null)
    
    
    
		if (mustMatchAspect != null)
			if (request.getAspect() != mustMatchAspect)
    
		IMEEssentiaMonitor essMonitor = this.getGridBlock().getEssentiaMonitor();
		if (essMonitor == null)
    
    
    
		if (request.isEmpty())
    
    
		player.inventory.decrStackSize(slotIndex, 1);
		if (drained != null)
    
		this.depositedAspect = request.getAspect();

		return true;

	}

    
	private boolean fillEssentiaContainer(final EntityPlayer player, final ItemStack heldItem, final AspectItemType itemType)
    
		if (itemType == AspectItemType.JarLabel)
    
			EssentiaItemContainerHelper.INSTANCE.setLabelAspect(heldItem, this.trackedEssentia.getAspectStack().getAspect());
			return true;
    
    
		if (itemType != AspectItemType.EssentiaContainer)
    
		if (!this.doesPlayerHavePermission(player, SecurityPermissions.EXTRACT))
    
    
		if (containerAmount > 0)
    
    
			if (this.trackedEssentia.getAspectStack().getAspect() != containerAspect)
				return false;
    
    
		if (jarLabelAspect != null)
			if (this.trackedEssentia.getAspectStack().getAspect() != jarLabelAspect)
    
		IMEEssentiaMonitor essMonitor = this.getGridBlock().getEssentiaMonitor();
		if (essMonitor == null)
    
    
    
    
		if (fillRequest.isEmpty())
    
    
    
    
		if (extractedAmount <= 0)
    
    
    
		if (filledContainer == null)
    
		extractedAmount = essMonitor.extractEssentia(fillRequest.getAspect(), fillRequest.getStackSize(), Actionable.MODULATE, playerSource, true);
		if (extractedAmount <= 0)
			return false;
		fillRequest.setStackSize(extractedAmount);
		filledContainer = EssentiaItemContainerHelper.INSTANCE.injectIntoContainer(heldItem, fillRequest);
		if (filledContainer == null)
    
    
    
		InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
		ItemStack rejectedItem = adaptor.addItems(filledContainer.right);
		if (rejectedItem != null)
    
			TileEntity te = this.getHostTile();
			ForgeDirection side = this.getSide();

			List<ItemStack> list = Collections.singletonList(rejectedItem);
			Platform.spawnDrops(player.worldObj, te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ, list);
    
		if (player.openContainer != null)
    
    
    
		return true;
	}

    
	private void insertAllEssentiaIntoNetwork(final EntityPlayer player)
	{
		ItemStack tracking = null;
		int prevStackSize = 0;

		for (int slotIndex = 0; slotIndex < player.inventory.getSizeInventory(); ++slotIndex)
    
    
    
			if (tracking == null || (prevStackSize = tracking.stackSize) == 0)
    
    
    
    
    
			if (tracking != null && tracking.stackSize > 0)
				if (prevStackSize != tracking.stackSize)
					--slotIndex;

		}
	}

    
	private void markFirstClick(final EntityPlayer player)
    
    
		this.depositedTick = MinecraftServer.getServer().getTickCounter();
	}

    
	private boolean wasDoubleClick(final EntityPlayer player)
    
    
		if (this.depositedPlayerID != -1 && this.depositedPlayerID == AEApi.instance().registries().players().getID(player.getGameProfile()))
			if (MinecraftServer.getServer().getTickCounter() - this.depositedTick <= PartEssentiaConversionMonitor.DOUBLE_CLICK_TICKS)
    
				this.depositedPlayerID = -1;
				this.depositedTick = 0;
				return true;
    
		return false;
	}

    
	@Override
	protected boolean onActivateWithAspectItem(final EntityPlayer player, final ItemStack heldItem, final AspectItemType itemType)
    
    
		if (!this.trackedEssentia.isValid() || !this.isLocked())
    
		return this.fillEssentiaContainer(player, heldItem, itemType);

	}

	@Override
	public boolean onShiftActivate(final EntityPlayer player, final Vec3 position)
    
		if (EffectiveSide.isClientSide())
    
		if (!this.activationCheck(player))
    
		if (!this.doesPlayerHavePermission(player, SecurityPermissions.INJECT))
    
    
    
		if (EssentiaItemContainerHelper.INSTANCE.getItemType(heldItem) != AspectItemType.EssentiaContainer)
    
    
    
		if (this.wasDoubleClick(player))
    
			this.insertAllEssentiaIntoNetwork(player);
			return true;
    
		boolean didDrain = this.drainEssentiaContainer(player, player.inventory.currentItem, null);
		if (didDrain)
			this.markFirstClick(player);

		return didDrain;
	}

}
