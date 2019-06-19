package extracells.part;

import appeng.api.config.Actionable;
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.render.TextureManager;
import extracells.util.FluidUtil;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.commons.lang3.tuple.MutablePair;

public class PartFluidConversionMonitor extends PartFluidStorageMonitor
{

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		if (super.onActivate(player, pos))
			return true;
		if (player == null || player.worldObj == null)
			return true;
		if (player.worldObj.isRemote)
			return true;
		ItemStack heldItem = player.getCurrentEquippedItem();
		IMEMonitor<IAEFluidStack> monitor = this.getFluidStorage();
		if (this.locked && heldItem != null && monitor != null)
		{
			ItemStack container = heldItem.copy();
			container.stackSize = 1;
			if (FluidUtil.isFilled(container))
			{
				FluidStack containerFluid = FluidUtil.getFluidFromContainer(container);
				if (containerFluid == null)
					return true;
				IAEFluidStack containerAeFluid = FluidUtil.createAEFluidStack(containerFluid);
				IAEFluidStack notInjected = monitor.injectItems(containerAeFluid.copy(), Actionable.SIMULATE, new MachineSource(this));
				if (monitor.canAccept(containerAeFluid) && (notInjected == null || notInjected.getStackSize() == 0L))
				{
    
					MutablePair<Integer, ItemStack> drainedContainer = FluidUtil.drainStack(container, containerFluid);
					int amount = drainedContainer.getLeft();
					if (amount > 0)
					{
						containerFluid.amount = amount;
						containerAeFluid.setStackSize(amount);
						notInjected = monitor.injectItems(containerAeFluid.copy(), Actionable.SIMULATE, new MachineSource(this));
						if (monitor.canAccept(containerAeFluid) && (notInjected == null || notInjected.getStackSize() == 0L))
						{
							monitor.injectItems(containerAeFluid, Actionable.MODULATE, new MachineSource(this));
							ItemStack emptyContainer = drainedContainer.right;
							if (emptyContainer != null)
							{
								TileEntity tile = this.getHost().getTile();
								ForgeDirection side = this.getSide();
								this.dropItems(tile.getWorldObj(), tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ, emptyContainer);
							}
							ItemStack containerCopy = heldItem.copy();
							containerCopy.stackSize--;
							if (containerCopy.stackSize <= 0)
								player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
							else
								player.inventory.setInventorySlotContents(player.inventory.currentItem, containerCopy);
						}
    
				}
				return true;
			}
			else if (FluidUtil.isEmpty(container))
			{
				if (this.fluid == null)
					return true;
				IAEFluidStack extract;
				if (container.getItem() instanceof IFluidContainerItem)
					extract = monitor.extractItems(FluidUtil.createAEFluidStack(this.fluid, ((IFluidContainerItem) container.getItem()).getCapacity(container)), Actionable.SIMULATE, new MachineSource(this));
				else
					extract = monitor.extractItems(FluidUtil.createAEFluidStack(this.fluid), Actionable.SIMULATE, new MachineSource(this));
				if (extract != null)
    
    
					if (extract.getStackSize() <= 0)
						return true;
					extract = monitor.extractItems(extract, Actionable.MODULATE, new MachineSource(this));
					if (extract == null || extract.getStackSize() <= 0)
    

					MutablePair<Integer, ItemStack> empty1 = FluidUtil.fillStack(container, extract.getFluidStack());
					if (empty1.left == 0)
    
    
    

						return true;
					}
					ItemStack empty = empty1.right;
					if (empty != null)
						this.dropItems(this.getHost().getTile().getWorldObj(), this.getHost().getTile().xCoord + this.getSide().offsetX, this.getHost().getTile().yCoord + this.getSide().offsetY, this.getHost().getTile().zCoord + this.getSide().offsetZ, empty);
					ItemStack s3 = heldItem.copy();
					s3.stackSize = s3.stackSize - 1;
					if (s3.stackSize == 0)
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					else
						player.inventory.setInventorySlotContents(player.inventory.currentItem, s3);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
		rh.setTexture(side);
		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderInventoryBox(renderer);
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderInventoryBox(renderer);

		ts.setBrightness(13 << 20 | 13 << 4);

		rh.setInvColor(0xFFFFFF);
		rh.renderInventoryFace(TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

		rh.setBounds(3, 3, 15, 13, 13, 16);
		rh.setInvColor(AEColor.Transparent.blackVariant);
		rh.renderInventoryFace(TextureManager.CONVERSION_MONITOR.getTextures()[0], ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.mediumVariant);
		rh.renderInventoryFace(TextureManager.CONVERSION_MONITOR.getTextures()[1], ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.whiteVariant);
		rh.renderInventoryFace(TextureManager.CONVERSION_MONITOR.getTextures()[2], ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 12, 11, 11, 13);
		this.renderInventoryBusLights(rh, renderer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
		rh.setTexture(side);
		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderBlock(x, y, z, renderer);
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderBlock(x, y, z, renderer);

		if (this.isActive())
			Tessellator.instance.setBrightness(13 << 20 | 13 << 4);

		ts.setColorOpaque_I(0xFFFFFF);
		rh.renderFace(x, y, z, TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

		IPartHost host = this.getHost();
		rh.setBounds(3, 3, 15, 13, 13, 16);
		ts.setColorOpaque_I(host.getColor().mediumVariant);
		rh.renderFace(x, y, z, TextureManager.CONVERSION_MONITOR.getTextures()[0], ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().whiteVariant);
		rh.renderFace(x, y, z, TextureManager.CONVERSION_MONITOR.getTextures()[1], ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().blackVariant);
		rh.renderFace(x, y, z, TextureManager.CONVERSION_MONITOR.getTextures()[2], ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 12, 11, 11, 13);
		this.renderStaticBusLights(x, y, z, rh, renderer);
	}

}
