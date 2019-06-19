package ru.will.git.clientwg.network;

import ru.will.git.clientwg.ClientWG;
import ru.will.git.clientwg.Config;
import ru.will.git.clientwg.util.BlockMetaPair;
import ru.will.git.clientwg.util.ItemMetaPair;
import com.google.common.base.Charsets;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.registry.GameData;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class ConfigMessage implements IMessage
{
	private final List<BlockMetaPair> forceAccessBlocks = new ArrayList<BlockMetaPair>();
	private final List<ItemMetaPair> forceAccessItems = new ArrayList<ItemMetaPair>();
	private final List<Class<? extends Entity>> forceAccessEntities = new ArrayList<Class<? extends Entity>>();

	public ConfigMessage()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		int blocksSize = buf.readInt();
		for (int i = 0; i < blocksSize; i++)
		{
			BlockMetaPair pair = readBlockMetaPair(buf);
			if (pair != null)
				this.forceAccessBlocks.add(pair);
		}

		int itemsSize = buf.readInt();
		for (int i = 0; i < itemsSize; i++)
		{
			ItemMetaPair pair = readItemMetaPair(buf);
			if (pair != null)
				this.forceAccessItems.add(pair);
		}

		int entitiesSize = buf.readInt();
		for (int i = 0; i < entitiesSize; i++)
		{
			Class<? extends Entity> clazz = readClass(buf, Entity.class);
			if (clazz != null)
				this.forceAccessEntities.add(clazz);
		}
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
	}

	private static BlockMetaPair readBlockMetaPair(ByteBuf buf)
	{
		int id = buf.readInt();
		int meta = buf.readInt();
		Block block = GameData.getBlockRegistry().getObjectById(id);
		return block == null || block == Blocks.air ? null : new BlockMetaPair(block, meta);
	}

	private static ItemMetaPair readItemMetaPair(ByteBuf buf)
	{
		int id = buf.readInt();
		int meta = buf.readInt();
		Item item = GameData.getItemRegistry().getObjectById(id);
		return item == null ? null : new ItemMetaPair(item, meta);
	}

	private static <T> Class<? extends T> readClass(ByteBuf buf, Class<T> superClass)
	{
		String className = readString(buf);
		try
		{
			Class<?> clazz = Class.forName(className);
			return superClass.isAssignableFrom(clazz) ? (Class<? extends T>) clazz : null;
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace();
		}
		return null;
	}

	private static String readString(ByteBuf buf)
	{
		short size = buf.readShort();
		byte[] bytes = buf.readBytes(size).array();
		return new String(bytes, Charsets.UTF_8);
	}

	public static final class Handler implements IMessageHandler<ConfigMessage, IMessage>
	{
		@Override
		public IMessage onMessage(ConfigMessage message, MessageContext ctx)
		{
			Config config = ClientWG.instance.config;
			config.forceAccessBlocks.clear();
			config.forceAccessBlocks.addAll(message.forceAccessBlocks);
			config.forceAccessItems.clear();
			config.forceAccessItems.addAll(message.forceAccessItems);
			config.forceAccessEntities.clear();
			config.forceAccessEntities.addAll(message.forceAccessEntities);
			return null;
		}
	}
}
