package ru.will.git.immersiveengineering;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter;
import blusunrize.immersiveengineering.common.gui.ContainerAssembler;
import blusunrize.immersiveengineering.common.gui.ContainerSorter;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.compat.GregTechHelper;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public final class NetworkMessageHandler
{
	public static void handleRequestBlockUpdate(EntityPlayer player, int x, int y, int z, int dimensionId)
	{
		if (player != null && player.dimension == dimensionId)
		{
			World world = player.worldObj;
			if (!world.isRemote && player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) <= 64)
			{
				ItemStack heldItem = player.getHeldItem();
				if (heldItem != null && OreDictionary.itemMatches(new ItemStack(IEContent.itemTool, 1, 2), heldItem, true) && world.blockExists(x, y, z))
				{
					TileEntity tile = world.getTileEntity(x, y, z);
					if (tile instanceof IEnergyReceiver || Lib.GREG && GregTechHelper.gregtech_isValidEnergyOutput(tile))
						world.markBlockForUpdate(x, y, z);
				}
			}
		}
	}

	public static void handleTileSync(EntityPlayer player, int x, int y, int z, int dimensionId, NBTTagCompound nbt)
	{
		if (player != null && player.dimension == dimensionId && nbt != null)
		{
			World world = player.worldObj;
			if (!world.isRemote && player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) <= 64 && world.blockExists(x, y, z))
			{
				TileEntity tile = world.getTileEntity(x, y, z);
				if (tile instanceof TileEntityIEBase)
				{
					if (tile instanceof TileEntityConveyorSorter)
					{
						Container openContainer = player.openContainer;
						if (!(openContainer instanceof ContainerSorter))
							return;
						TileEntityConveyorSorter sorter = ((ContainerSorter) openContainer).getTile();
						if (sorter == null || sorter.xCoord != x || sorter.yCoord != y || sorter.zCoord != z)
							return;
					}
					else if (tile instanceof TileEntityAssembler)
					{
						Container openContainer = player.openContainer;
						if (!(openContainer instanceof ContainerAssembler))
							return;
						TileEntityAssembler assembler = ((ContainerAssembler) openContainer).getTile();
						if (assembler == null || assembler.xCoord != x || assembler.yCoord != y || assembler.zCoord != z)
							return;
					}

					((TileEntityIEBase) tile).receiveMessageFromClient(nbt);
				}
			}
		}
	}
}
