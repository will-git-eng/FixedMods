package powercrystals.minefactoryreloaded.net;

import ru.will.git.minefactoryreloaded.EventConfig;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import powercrystals.minefactoryreloaded.core.UtilInventory;
import powercrystals.minefactoryreloaded.entity.EntityRocket;
import powercrystals.minefactoryreloaded.gui.container.*;
import powercrystals.minefactoryreloaded.gui.slot.SlotFake;
import powercrystals.minefactoryreloaded.setup.MFRThings;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactory;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryInventory;
import powercrystals.minefactoryreloaded.tile.machine.*;
import powercrystals.minefactoryreloaded.tile.rednet.TileEntityRedNetLogic;

public class ServerPacketHandler implements IMessageHandler<ServerPacketHandler.MFRMessage, IMessage>
{
	@Override
	public IMessage onMessage(ServerPacketHandler.MFRMessage message, MessageContext messageContext)
    
		if (EventConfig.networkFix && message.buf != null)
    

		if (message.packet != null)
			messageContext.getServerHandler().sendPacket(message.packet);
		return null;
    
	private static Packet readData(ByteBuf buf)
	{
		return readData(buf, null);
    
    
	private static Packet readData(ByteBuf buf, EntityPlayer player)
    
		if (EventConfig.networkFix && (buf == null || player == null))
    

    
		if (EventConfig.networkFix && player.worldObj.provider.dimensionId != dimensionId)
    

		WorldServer world = DimensionManager.getWorld(dimensionId);
		switch (buf.readUnsignedShort())
		{
			case 0:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
    
				if (!world.blockExists(x, y, z))
    

				TileEntity tile = world.getTileEntity(x, y, z);
				if (tile instanceof TileEntityAutoEnchanter)
					((TileEntityAutoEnchanter) tile).setTargetLevel(((TileEntityAutoEnchanter) tile).getTargetLevel() + b);
				else if (tile instanceof TileEntityBlockSmasher)
					((TileEntityBlockSmasher) tile).setFortune(((TileEntityBlockSmasher) tile).getFortune() + b);
				else if (tile instanceof TileEntityAutoDisenchanter)
					((TileEntityAutoDisenchanter) tile).setRepeatDisenchant(b == 1);
			}
			break;
			case 1:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerHarvester))
						return null;
					ContainerHarvester factoryInventory = (ContainerHarvester) openContainer;
					TileEntityFactoryInventory tileFactory = factoryInventory.getTileEntity();
					if (x != tileFactory.xCoord || y != tileFactory.yCoord || z != tileFactory.zCoord)
						return null;
					tile = tileFactory;
				}
    
					tile = world.getTileEntity(x, y, z);

				if (tile instanceof TileEntityHarvester)
					((TileEntityHarvester) tile).getSettings().put(ByteBufUtils.readUTF8String(buf), buf.readBoolean());
				break;
			}
			case 2:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerFactoryInventory))
						return null;
					ContainerFactoryInventory factoryInventory = (ContainerFactoryInventory) openContainer;
					TileEntityFactoryInventory tileFactory = factoryInventory.getTileEntity();
					if (x != tileFactory.xCoord || y != tileFactory.yCoord || z != tileFactory.zCoord)
						return null;
					tile = tileFactory;
				}
    
					tile = world.getTileEntity(x, y, z);

				if (tile instanceof TileEntityChronotyper)
					((TileEntityChronotyper) tile).setMoveOld(!((TileEntityChronotyper) tile).getMoveOld());
				else if (tile instanceof TileEntityDeepStorageUnit)
				{
					((TileEntityDeepStorageUnit) tile).setIsActive(!((TileEntityDeepStorageUnit) tile).isActive());
					((TileEntityDeepStorageUnit) tile).markForUpdate();
					Packets.sendToAllPlayersWatching(tile);
				}
				break;
			}
			case 3:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				TileEntity tile = world.getTileEntity(x, y, z);
				if (tile instanceof TileEntityFactory && ((TileEntityFactory) tile).hasHAM())
					return ((TileEntityFactory) tile).getHAM().getUpgradePacket();
				break;
			}
			case 4:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				byte action = buf.readByte();
    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerAutoJukebox))
						return null;
					ContainerAutoJukebox containerAutoJukebox = (ContainerAutoJukebox) openContainer;
					TileEntityFactoryInventory tileFactory = containerAutoJukebox.getTileEntity();
					if (x != tileFactory.xCoord || y != tileFactory.yCoord || z != tileFactory.zCoord)
						return null;
					tile = tileFactory;
				}
    
					tile = world.getTileEntity(x, y, z);

				if (tile instanceof TileEntityAutoJukebox)
				{
					TileEntityAutoJukebox jukebox = (TileEntityAutoJukebox) tile;
					if (action == 1)
						jukebox.playRecord();
					else if (action == 2)
						jukebox.stopRecord();
					else if (action == 3)
						jukebox.copyRecord();
				}
				break;
			}
			case 5:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerAutoSpawner))
						return null;
					ContainerAutoSpawner containerAutoSpawner = (ContainerAutoSpawner) openContainer;
					TileEntityFactoryInventory tileFactory = containerAutoSpawner.getTileEntity();
					if (x != tileFactory.xCoord || y != tileFactory.yCoord || z != tileFactory.zCoord)
						return null;
					tile = tileFactory;
				}
    
					tile = world.getTileEntity(x, y, z);

				if (tile instanceof TileEntityAutoSpawner)
					((TileEntityAutoSpawner) tile).setSpawnExact(!((TileEntityAutoSpawner) tile).getSpawnExact());
				break;
			}
			case 6:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				int circuitIndex = buf.readInt();
    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerRedNetLogic))
						return null;
					ContainerRedNetLogic redNetLogic = (ContainerRedNetLogic) openContainer;
					TileEntityRedNetLogic tileRedNetLogic = redNetLogic.getTileEntity();
					if (x != tileRedNetLogic.xCoord || y != tileRedNetLogic.yCoord || z != tileRedNetLogic.zCoord)
						return null;
					tile = tileRedNetLogic;
				}
    
					tile = world.getTileEntity(x, y, z);

				if (tile instanceof TileEntityRedNetLogic)
					((TileEntityRedNetLogic) tile).sendCircuitDefinition(circuitIndex);
				break;
			}
			case 7:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				int circuitIndex = buf.readInt();
				String circuitClassName = ByteBufUtils.readUTF8String(buf);
    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerRedNetLogic))
						return null;
					ContainerRedNetLogic redNetLogic = (ContainerRedNetLogic) openContainer;
					TileEntityRedNetLogic tileRedNetLogic = redNetLogic.getTileEntity();
					if (x != tileRedNetLogic.xCoord || y != tileRedNetLogic.yCoord || z != tileRedNetLogic.zCoord)
						return null;
					tile = tileRedNetLogic;
				}
    
					tile = world.getTileEntity(x, y, z);

				if (tile instanceof TileEntityRedNetLogic)
				{
					((TileEntityRedNetLogic) tile).initCircuit(circuitIndex, circuitClassName);
					((TileEntityRedNetLogic) tile).sendCircuitDefinition(circuitIndex);
				}
				break;
			}
			case 8:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				byte action = buf.readByte();
				int circuitIndex = buf.readInt();
				int pinIndex = buf.readInt();
				int buffer = buf.readInt();
				int pin = buf.readInt();

    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerRedNetLogic))
						return null;
					ContainerRedNetLogic redNetLogic = (ContainerRedNetLogic) openContainer;
					TileEntityRedNetLogic tileRedNetLogic = redNetLogic.getTileEntity();
					if (x != tileRedNetLogic.xCoord || y != tileRedNetLogic.yCoord || z != tileRedNetLogic.zCoord)
						return null;
					tile = tileRedNetLogic;
				}
    
					tile = world.getTileEntity(x, y, z);

				if (tile instanceof TileEntityRedNetLogic)
				{
					if (action == 0)
						((TileEntityRedNetLogic) tile).setInputPinMapping(circuitIndex, pinIndex, buffer, pin);
					else if (action == 1)
						((TileEntityRedNetLogic) tile).setOutputPinMapping(circuitIndex, pinIndex, buffer, pin);

					((TileEntityRedNetLogic) tile).sendCircuitDefinition(circuitIndex);
				}
				break;
			}
			case 9:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				int playerId = buf.readInt();
				TileEntity tile;
    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					if (playerId == player.getEntityId())
						playerFromPacket = player;
					else
						return null;

					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerRedNetLogic))
						return null;
					ContainerRedNetLogic redNetLogic = (ContainerRedNetLogic) openContainer;
					TileEntityRedNetLogic tileRedNetLogic = redNetLogic.getTileEntity();
					if (x != tileRedNetLogic.xCoord || y != tileRedNetLogic.yCoord || z != tileRedNetLogic.zCoord)
						return null;
					tile = tileRedNetLogic;
				}
    
				{
					tile = world.getTileEntity(x, y, z);
					playerFromPacket = (EntityPlayer) world.getEntityByID(playerId);
				}

				if (tile instanceof TileEntityRedNetLogic)
					((TileEntityRedNetLogic) tile).reinitialize(playerFromPacket);
				break;
			}
			case 10:
			{
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				int action = buf.readInt();

    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerFactoryInventory))
						return null;
					ContainerFactoryInventory factoryInventory = (ContainerFactoryInventory) openContainer;
					TileEntityFactoryInventory tileFactory = factoryInventory.getTileEntity();
					if (x != tileFactory.xCoord || y != tileFactory.yCoord || z != tileFactory.zCoord)
						return null;
					tile = tileFactory;
				}
    
					tile = world.getTileEntity(x, y, z);

				if (tile instanceof TileEntityEnchantmentRouter)
					switch (action)
					{
						case 1:
							((TileEntityEnchantmentRouter) tile).setMatchLevels(!((TileEntityEnchantmentRouter) tile).getMatchLevels());
							return null;
						case 2:
							((TileEntityItemRouter) tile).setRejectUnmapped(!((TileEntityItemRouter) tile).getRejectUnmapped());
					}
				else if (tile instanceof TileEntityItemRouter)
					((TileEntityItemRouter) tile).setRejectUnmapped(!((TileEntityItemRouter) tile).getRejectUnmapped());
				else if (tile instanceof TileEntityEjector)
					switch (action)
					{
						case 1:
							((TileEntityEjector) tile).setIsWhitelist(!((TileEntityEjector) tile).getIsWhitelist());
							return null;
						case 2:
							((TileEntityEjector) tile).setIsNBTMatch(!((TileEntityEjector) tile).getIsNBTMatch());
							return null;
						case 3:
							((TileEntityEjector) tile).setIsIDMatch(!((TileEntityEjector) tile).getIsIDMatch());
					}
				else if (tile instanceof TileEntityAutoAnvil)
					((TileEntityAutoAnvil) tile).setRepairOnly(!((TileEntityAutoAnvil) tile).getRepairOnly());
				else if (tile instanceof TileEntityChunkLoader)
					((TileEntityChunkLoader) tile).setRadius((short) action);
				else if (tile instanceof TileEntityPlanter)
					((TileEntityPlanter) tile).setConsumeAll(!((TileEntityPlanter) tile).getConsumeAll());
				else if (tile instanceof TileEntityMobRouter)
					switch (action)
					{
						case 1:
							((TileEntityMobRouter) tile).setWhiteList(!((TileEntityMobRouter) tile).getWhiteList());
							return null;
						case 2:
							((TileEntityMobRouter) tile).setMatchMode(((TileEntityMobRouter) tile).getMatchMode() + 1);
							return null;
						case 3:
							((TileEntityMobRouter) tile).setMatchMode(((TileEntityMobRouter) tile).getMatchMode() - 1);
					}
				break;
			}
			case 11:
				int shooterId = buf.readInt();
				int targetEntityId = buf.readInt();
    
				if (EventConfig.networkFix)
					if (shooterId == player.getEntityId())
						shooter = player;
					else
						return null;
    
					shooter = world.getEntityByID(shooterId);

    
				if (shooter instanceof EntityLivingBase && EventConfig.enableRocket)
    
					if (EventConfig.fixInfiniteRocket && shooter instanceof EntityPlayer)
					{
						EntityPlayer shooterPlayer = (EntityPlayer) shooter;
						if (!(shooterPlayer.capabilities.isCreativeMode || shooterPlayer.inventory.consumeInventoryItem(MFRThings.rocketItem)))
							return null;
    

					EntityRocket entityRocket = new EntityRocket(world, (EntityLivingBase) shooter, targetEntity);
					world.spawnEntityInWorld(entityRocket);
				}
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			default:
				break;
			case 20:
				int x = buf.readInt();
				int y = buf.readInt();
				int z = buf.readInt();
				int playerId = buf.readInt();
				int slotIndex = buf.readInt();
				byte action = buf.readByte();
    
				if (EventConfig.networkFix)
					if (playerId == player.getEntityId())
						playerFromPacket = player;
					else
						return null;
    
					playerFromPacket = (EntityPlayer) world.getEntityByID(playerId);

    
				if (!world.blockExists(x, y, z))
					return null;
				if (EventConfig.networkFix)
				{
					Container openContainer = player.openContainer;
					if (!(openContainer instanceof ContainerFactoryInventory))
						return null;
					ContainerFactoryInventory factoryInventory = (ContainerFactoryInventory) openContainer;
					TileEntityFactoryInventory tileFactory = factoryInventory.getTileEntity();
					if (x != tileFactory.xCoord || y != tileFactory.yCoord || z != tileFactory.zCoord)
						return null;
    
					if (!(slot instanceof SlotFake))
						return null;
					tile = tileFactory;
				}
    
					tile = world.getTileEntity(x, y, z);

				if (tile instanceof IInventory)
    
					if (!(tile instanceof TileEntityFactoryInventory))
    

					IInventory inventory = (IInventory) tile;
					ItemStack cursorStack = playerFromPacket.inventory.getItemStack();
					if (cursorStack == null)
						inventory.setInventorySlotContents(slotIndex, null);
					else
					{
						cursorStack = cursorStack.copy();
						cursorStack.stackSize = action == 1 ? -1 : 1;
						ItemStack stackInSlot = inventory.getStackInSlot(slotIndex);
						if (!UtilInventory.stacksEqual(stackInSlot, cursorStack))
							cursorStack.stackSize = 1;
						else
							cursorStack.stackSize = Math.max(cursorStack.stackSize + stackInSlot.stackSize, 1);
						inventory.setInventorySlotContents(slotIndex, cursorStack);
					}
				}
		}

		return null;
	}

	public static class MFRMessage implements IMessage
	{
		public ByteBuf buf;
		public Packet packet;

		public MFRMessage()
		{
		}

		public MFRMessage(short packetId, TileEntity tile, Object... args)
		{
			ByteBuf buf = Unpooled.buffer();
			buf.writeInt(tile.getWorldObj().provider.dimensionId);
			buf.writeShort(packetId);
			buf.writeInt(tile.xCoord);
			buf.writeInt(tile.yCoord);
			buf.writeInt(tile.zCoord);
			handleObjects(buf, args);
			this.buf = buf;
		}

		public MFRMessage(short packetId, Entity entity, Object... args)
		{
			ByteBuf buf = Unpooled.buffer();
			buf.writeInt(entity.worldObj.provider.dimensionId);
			buf.writeShort(packetId);
			buf.writeInt(entity.getEntityId());
			handleObjects(buf, args);
			this.buf = buf;
		}

		@Override
		public void fromBytes(ByteBuf buf)
    
			if (EventConfig.networkFix)
			{
				this.buf = buf.readBytes(buf.readableBytes()).copy();
				return;
    

			this.packet = ServerPacketHandler.readData(buf);
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeBytes(this.buf);
		}

		private static void handleObjects(ByteBuf buf, Object[] args)
		{
			for (Object arg : args)
			{
				Class argClass = arg.getClass();
				if (argClass.equals(Integer.class))
					buf.writeInt((Integer) arg);
				else if (argClass.equals(Boolean.class))
					buf.writeBoolean((Boolean) arg);
				else if (argClass.equals(Byte.class))
					buf.writeByte((Byte) arg);
				else if (argClass.equals(Short.class))
					buf.writeShort((Short) arg);
				else if (argClass.equals(String.class))
					ByteBufUtils.writeUTF8String(buf, (String) arg);
				else if (Entity.class.isAssignableFrom(argClass))
					buf.writeInt(((Entity) arg).getEntityId());
				else if (argClass.equals(Double.class))
					buf.writeDouble((Double) arg);
				else if (argClass.equals(Float.class))
					buf.writeFloat((Float) arg);
				else if (argClass.equals(Long.class))
					buf.writeLong((Long) arg);
			}

		}
	}
}
