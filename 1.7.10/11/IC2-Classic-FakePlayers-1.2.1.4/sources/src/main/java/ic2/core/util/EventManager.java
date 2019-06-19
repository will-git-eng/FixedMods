package ic2.core.util;

import java.util.List;

import ru.will.git.reflectionmedic.util.EventUtils;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ic2.api.item.IWrenchHandler;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

public class EventManager
{
	public EventManager()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBlockClick(PlayerInteractEvent evt)
	{
		if (evt.action == Action.RIGHT_CLICK_BLOCK)
		{
			World world = evt.world;
			int x = evt.x;
			int y = evt.y;
			int z = evt.z;
			EntityPlayer player = evt.entityPlayer;
			ItemStack item = player.getCurrentEquippedItem();
			TileEntity tile = world.getTileEntity(x, y, z);
			IWrenchHandler handler = this.getWrenchHandler(item);
			if (handler != null && tile instanceof IWrenchable && handler.canWrench(item, x, y, z, player))
    
				if (EventUtils.cantBreak(player, x, y, z))
    

				boolean flag = this.handleWrench(handler, (IWrenchable) tile, item, player, world, x, y, z, evt.face);
				if (flag)
					evt.setCanceled(true);
			}
		}
	}

	private boolean handleWrench(IWrenchHandler par1, IWrenchable wrenchable, ItemStack item, EntityPlayer player, World world, int x, int y, int z, int side)
	{
		if (wrenchable instanceof TileEntityTerra)
		{
			TileEntityTerra terra = (TileEntityTerra) wrenchable;
			if (terra.ejectBlueprint())
			{
				if (IC2.platform.isSimulating())
					par1.useWrench(item, x, y, z, player);

				if (IC2.platform.isRendering())
					IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.defaultVolume);

				return true;
			}
		}

		if (IC2.keyboard.isAltKeyDown(player))
		{
			if (player.isSneaking())
				side = (wrenchable.getFacing() + 5) % 6;
			else
				side = (wrenchable.getFacing() + 1) % 6;
		}
		else if (player.isSneaking())
			side += side % 2 * -2 + 1;

		if (wrenchable.wrenchCanSetFacing(player, side))
		{
			if (IC2.platform.isSimulating())
			{
				wrenchable.setFacing((short) side);
				par1.useWrench(item, x, y, z, player);
			}

			if (IC2.platform.isRendering())
				IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.defaultVolume);

			return IC2.platform.isSimulating();
		}
		else if (!wrenchable.wrenchCanRemove(player))
			return false;
		else
		{
			if (IC2.platform.isSimulating())
			{
				Block block = world.getBlock(x, y, z);
				int meta = world.getBlockMetadata(x, y, z);
				boolean dropOriginalBlock = false;
				if (wrenchable.getWrenchDropRate() < 1.0F && this.checkIfNoloss(par1))
				{
					dropOriginalBlock = true;
					par1.useWrench(item, x, y, z, player);
				}
				else
				{
					dropOriginalBlock = world.rand.nextFloat() <= wrenchable.getWrenchDropRate();
					par1.useWrench(item, x, y, z, player);
				}

				List<ItemStack> drops = block.getDrops(world, x, y, z, meta, 0);
				if (dropOriginalBlock)
					if (drops.isEmpty())
						drops.add(wrenchable.getWrenchDrop(player));
					else
						drops.set(0, wrenchable.getWrenchDrop(player));

				for (ItemStack itemStack : drops)
					StackUtil.dropAsEntity(world, x, y, z, itemStack);

				world.setBlockToAir(x, y, z);
			}

			if (IC2.platform.isRendering())
				IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.defaultVolume);

			return IC2.platform.isSimulating();
		}
	}

	private boolean checkIfNoloss(IWrenchHandler par1)
	{
		return IC2.losslessAddonWrenches;
	}

	private IWrenchHandler getWrenchHandler(ItemStack par1)
	{
		if (par1 == null)
			return null;
		else
		{
			for (IWrenchHandler handler : IC2.handlers)
				if (handler.supportsItem(par1))
					return handler;

			return null;
		}
	}
}
