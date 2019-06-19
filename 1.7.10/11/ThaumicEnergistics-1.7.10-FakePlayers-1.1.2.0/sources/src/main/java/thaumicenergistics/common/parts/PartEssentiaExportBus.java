package thaumicenergistics.common.parts;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.common.tiles.TileJarFillableVoid;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.integration.tc.EssentiaTileContainerHelper;

    
public class PartEssentiaExportBus extends ThEPartEssentiaIOBus_Base
{

	private static final String NBT_KEY_VOID = "IsVoidAllowed";

    
	private boolean isVoidAllowed = false;

	public PartEssentiaExportBus()
	{
		super(AEPartsEnum.EssentiaExportBus, SecurityPermissions.EXTRACT);
	}

	@Override
	public boolean aspectTransferAllowed(final Aspect aspect)
	{
		return true;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

    
	@Override
	public boolean doWork(final int amountToFillContainer)
    
    
		if (this.facingContainer == null)
    
		for (Aspect filterAspect : this.filteredAspects)
    
    
			if (filterAspect == null)
    
    
			if (EssentiaTileContainerHelper.INSTANCE.injectEssentiaIntoContainer(this.facingContainer, 1, filterAspect, Actionable.SIMULATE) <= 0)
				if (!(this.isVoidAllowed && EssentiaTileContainerHelper.INSTANCE.getAspectInContainer(this.facingContainer) == filterAspect))
    
			IMEEssentiaMonitor essMonitor = this.getGridBlock().getEssentiaMonitor();
			if (essMonitor == null)
    
    
    
			if (extractedAmount <= 0)
				continue;

    
    
			if (this.isVoidAllowed && this.facingContainer instanceof TileJarFillableVoid)
				filledAmount = extractedAmount;
			else
    
    
			if (filledAmount <= 0)
    
			extractedAmount = essMonitor.extractEssentia(filterAspect, filledAmount, Actionable.MODULATE, this.asMachineSource, true);
			if (extractedAmount <= 0)
				continue;
			if (this.isVoidAllowed && this.facingContainer instanceof TileJarFillableVoid)
				filledAmount = extractedAmount;
			else
				filledAmount = EssentiaTileContainerHelper.INSTANCE.injectEssentiaIntoContainer(this.facingContainer, (int) extractedAmount, filterAspect, Actionable.SIMULATE);
			if (filledAmount <= 0)
    
    
    
			if (!this.isVoidAllowed)
    
    
    
			return true;
		}

		return false;
	}

	@Override
	public void getBoxes(final IPartCollisionHelper helper)
    
    
    
		helper.addBox(6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F);
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[2];
	}

    
	@Override
	public boolean isVoidAllowed()
	{
		return this.isVoidAllowed;
	}

	@Override
	public void onClientRequestFilterList(final EntityPlayer player)
    
		super.onClientRequestFilterList(player);
	}

	@Override
	public void readFromNBT(final NBTTagCompound data)
    
    
		if (data.hasKey(PartEssentiaExportBus.NBT_KEY_VOID))
			this.isVoidAllowed = data.getBoolean(PartEssentiaExportBus.NBT_KEY_VOID);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(final IPartRenderHelper helper, final RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;

    
    
		helper.setBounds(4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.5F);
    
    
		helper.setBounds(4.0F, 4.0F, 12.5F, 12.0F, 12.0F, 13.5F);
    
		helper.setBounds(5.0F, 5.0F, 13.5F, 11.0F, 11.0F, 14.5F);
    
    
		helper.setBounds(5.0F, 5.0F, 14.5F, 11.0F, 11.0F, 15.0F);
    
    
		helper.setBounds(6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F);
    
		helper.setInvColor(ThEPartBase.INVENTORY_OVERLAY_COLOR);
		ts.setBrightness(0xF000F0);
		IIcon faceOverlayTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[1];
		helper.renderInventoryFace(faceOverlayTexture, ForgeDirection.UP, renderer);
		helper.renderInventoryFace(faceOverlayTexture, ForgeDirection.DOWN, renderer);
		helper.renderInventoryFace(faceOverlayTexture, ForgeDirection.EAST, renderer);
    
		helper.setBounds(6.0F, 6.0F, 11.0F, 10.0F, 10.0F, 12.0F);
		this.renderInventoryBusLights(helper, renderer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;

    
    
		helper.setBounds(4.0F, 4.0F, 12.0F, 12.0F, 12.0F, 12.5F);
    
    
    
		helper.setBounds(4.0F, 4.0F, 12.5F, 12.0F, 12.0F, 13.5F);
    
		helper.setBounds(5.0F, 5.0F, 13.5F, 11.0F, 11.0F, 14.5F);
    
    
    
		helper.setBounds(5.0F, 5.0F, 14.5F, 11.0F, 11.0F, 15.0F);
    
    
		helper.setBounds(6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F);
    
		ts.setColorOpaque_I(this.getHost().getColor().blackVariant);

		if (this.isActive())
			Tessellator.instance.setBrightness(ThEPartBase.ACTIVE_FACE_BRIGHTNESS);

		IIcon faceOverlayTexture = BlockTextureManager.ESSENTIA_EXPORT_BUS.getTextures()[1];
		helper.renderFace(x, y, z, faceOverlayTexture, ForgeDirection.UP, renderer);
		helper.renderFace(x, y, z, faceOverlayTexture, ForgeDirection.DOWN, renderer);
		helper.renderFace(x, y, z, faceOverlayTexture, ForgeDirection.EAST, renderer);
    
		helper.setBounds(6.0F, 6.0F, 11.0F, 10.0F, 10.0F, 12.0F);
		this.renderStaticBusLights(x, y, z, helper, renderer);
	}

    
	public void toggleVoidMode(final EntityPlayer player)
    
		this.isVoidAllowed = !this.isVoidAllowed;
	}

	@Override
	public void writeToNBT(final NBTTagCompound data, final PartItemStack saveType)
    
		super.writeToNBT(data, saveType);

    
		if (!doSave)
			for (Aspect aspect : this.filteredAspects)
			{
				if (aspect != null)
    
					doSave = true;
					break;
				}
    
		if (doSave && this.isVoidAllowed)
			data.setBoolean(PartEssentiaExportBus.NBT_KEY_VOID, this.isVoidAllowed);
	}

}
