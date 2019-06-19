package thaumcraft.common.lib.network.playerdata;

import ru.will.git.thaumcraft.CooldownManager;
import ru.will.git.thaumcraft.EventConfig;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.research.ScanResult;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.EntityUtils;

public class PacketScannedToServer implements IMessage, IMessageHandler<PacketScannedToServer, IMessage>
{
	private int playerid;
	private int dim;
	private byte type;
	private int id;
	private int md;
	private int entityid;
	private String phenomena;
	private String prefix;

	public PacketScannedToServer()
	{
	}

	public PacketScannedToServer(ScanResult scan, EntityPlayer player, String prefix)
	{
		this.playerid = player.getEntityId();
		this.dim = player.worldObj.provider.dimensionId;
		this.type = scan.type;
		this.id = scan.id;
		this.md = scan.meta;
		this.entityid = scan.entity == null ? 0 : scan.entity.getEntityId();
		this.phenomena = scan.phenomena;
		this.prefix = prefix;
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeInt(this.playerid);
		buffer.writeInt(this.dim);
		buffer.writeByte(this.type);
		buffer.writeInt(this.id);
		buffer.writeInt(this.md);
		buffer.writeInt(this.entityid);
		ByteBufUtils.writeUTF8String(buffer, this.phenomena);
		ByteBufUtils.writeUTF8String(buffer, this.prefix);
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		this.playerid = buffer.readInt();
		this.dim = buffer.readInt();
		this.type = buffer.readByte();
		this.id = buffer.readInt();
		this.md = buffer.readInt();
		this.entityid = buffer.readInt();
		this.phenomena = ByteBufUtils.readUTF8String(buffer);
		this.prefix = ByteBufUtils.readUTF8String(buffer);
	}

	@Override
	public IMessage onMessage(PacketScannedToServer message, MessageContext ctx)
	{
		World world = DimensionManager.getWorld(message.dim);
		if (world == null)
			return null;

    
		EntityPlayer player = ctx.getServerHandler().playerEntity;
		if (player != null && player.getEntityId() == message.playerid && player.worldObj == world)
		{
			Entity scannedEntity = message.entityid == 0 ? null : world.getEntityByID(message.entityid);
			ScanResult scanResult = new ScanResult(message.type, message.id, message.md, scannedEntity, message.phenomena);

			if (EventConfig.validateThaumometer)
			{
				ItemStack stack = player.getHeldItem();
				if (stack == null || stack.stackSize <= 0 || stack.getItem() != ConfigItems.itemThaumometer)
					return null;

				if (!"@".equals(message.prefix))
					return null;

				if (scanResult.type == 2)
				{
					if (scannedEntity == null)
						return null;
					if (scanResult.id != 0 || scanResult.meta != 0)
						return null;
					if (message.phenomena == null || !message.phenomena.isEmpty())
						return null;
					if (scannedEntity != EntityUtils.getPointedEntity(player.worldObj, player, 0.5, 10, 0, true))
						return null;
				}
				else
				{
					MovingObjectPosition mop = getMovingObjectPositionFromPlayer(player.worldObj, player, true);
					if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
						return null;
					if (scanResult.type == 3)
					{
						if (scanResult.id != 0 || scanResult.meta != 0)
							return null;
						TileEntity tile = world.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
						if (!(tile instanceof INode))
							return null;
						if (message.phenomena == null || !message.phenomena.equals("NODE" + ((INode) tile).getId()))
							return null;
					}
					else if (scanResult.type == 1)
					{
						if (message.phenomena == null || !message.phenomena.isEmpty())
							return null;

						Block block = world.getBlock(mop.blockX, mop.blockY, mop.blockZ);
						if (block != Blocks.air)
						{
							int meta = block.getDamageValue(world, mop.blockX, mop.blockY, mop.blockZ);
							ItemStack blockStack = block.getPickBlock(mop, player.worldObj, mop.blockX, mop.blockY, mop.blockZ);

							try
							{
								if (blockStack == null)
									blockStack = BlockUtils.createStackedBlock(block, meta);
							}
							catch (Exception ignored)
							{
							}

							try
							{
								if (blockStack == null)
								{
									if (scanResult.id != Block.getIdFromBlock(block))
										return null;
									if (scanResult.meta != meta)
										return null;
								}
								else
								{
									if (scanResult.id != Item.getIdFromItem(blockStack.getItem()))
										return null;
									if (scanResult.meta != blockStack.getItemDamage())
										return null;
								}
							}
							catch (Exception ignored)
							{
							}
						}
					}
				}
			}

			if (!COOLDOWN_MANAGER.add(player))
				return null;

			ScanManager.completeScan(player, scanResult, message.prefix);
    

		return null;
    
	private static final CooldownManager COOLDOWN_MANAGER = new CooldownManager(EventConfig.thaumometerCooldown);

	private static MovingObjectPosition getMovingObjectPositionFromPlayer(World world, EntityPlayer player, boolean p_77621_3_)
	{
		float f = 1.0F;
		float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
		float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
    
		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
		Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 5.0D;
		if (player instanceof EntityPlayerMP)
			d3 = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
		Vec3 vec31 = vec3.addVector((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
		return world.func_147447_a(vec3, vec31, p_77621_3_, !p_77621_3_, false);
    
}
