package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cofh.api.item.IToolHammer;
import ru.will.git.reflectionmedic.util.EventUtils;
import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.common.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHAPI|item")
public class ItemIETool extends ItemIEBase implements IToolHammer, ITool
{
	static int hammerMaxDamage;

	public ItemIETool()
	{
		super("tool", 1, "hammer", "wirecutter", "voltmeter", "manual");
		hammerMaxDamage = Config.getInt("hammerDurabiliy");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if (adv && stack.getItemDamage() == 0)
		{
			int nbtDamage = ItemNBTHelper.getInt(stack, "hammerDmg");
			list.add("Durability: " + (hammerMaxDamage - nbtDamage) + " / " + hammerMaxDamage);
		}
		if (ItemNBTHelper.hasKey(stack, "linkingPos"))
		{
			int[] link = ItemNBTHelper.getIntArray(stack, "linkingPos");
			if (link != null && link.length > 3)
				list.add(StatCollector.translateToLocalFormatted(Lib.DESC_INFO + "attachedToDim", link[1], link[2], link[3], link[0]));
		}
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return stack.getItemDamage() == 0;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if (stack.getItemDamage() == 0)
		{
			int nbtDamage = ItemNBTHelper.getInt(stack, "hammerDmg") + 1;
			if (nbtDamage < hammerMaxDamage)
			{
				ItemStack container = stack.copy();
				ItemNBTHelper.setInt(container, "hammerDmg", nbtDamage);
				return container;
			}
		}
		return null;
	}

	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		return stack.getItemDamage() != 0;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
    
			if (EventUtils.cantBreak(player, x, y, z))
    

			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (stack.getItemDamage() == 0)
			{
				String[] interdictedMultiblocks = null;
				if (ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
				{
					NBTTagList list = stack.getTagCompound().getTagList("multiblockInterdiction", 8);
					interdictedMultiblocks = new String[list.tagCount()];
					for (int i = 0; i < interdictedMultiblocks.length; i++)
					{
						interdictedMultiblocks[i] = list.getStringTagAt(i);
					}
				}
				for (IMultiblock mb : MultiblockHandler.getMultiblocks())
				{
					if (mb.isBlockTrigger(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z)))
					{
						boolean allowed = true;
						if (interdictedMultiblocks != null)
							for (String s : interdictedMultiblocks)
							{
								if (mb.getUniqueName().equalsIgnoreCase(s))
								{
									allowed = false;
									break;
								}
							}
						if (allowed && mb.createStructure(world, x, y, z, side, player))
							return true;
					}
				}
				if (world.getBlock(x, y, z) == Blocks.piston)
				{
					int meta = world.getBlockMetadata(x, y, z);
					if (!BlockPistonBase.isExtended(meta))
					{
						int dir = BlockPistonBase.getPistonOrientation(meta);
						dir = (dir + 1) % 6;
						world.setBlockMetadataWithNotify(x, y, z, meta - (meta & 7) + dir, 3);
					}

				}
				return false;
			}
			else if (stack.getItemDamage() == 1 && tileEntity instanceof IImmersiveConnectable)
			{
				IImmersiveConnectable nodeHere = (IImmersiveConnectable) tileEntity;
				ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(Utils.toCC(nodeHere), world, new TargetingInfo(side, hitX, hitY, hitZ));
				IESaveData.setDirty(world.provider.dimensionId);
				return true;
			}
			else if (stack.getItemDamage() == 2)
			{
				if (!player.isSneaking() && (tileEntity instanceof IEnergyReceiver || tileEntity instanceof IEnergyProvider))
				{
					int max = 0;
					int stored = 0;
					if (tileEntity instanceof IEnergyReceiver)
					{
						max = ((IEnergyReceiver) tileEntity).getMaxEnergyStored(ForgeDirection.getOrientation(side));
						stored = ((IEnergyReceiver) tileEntity).getEnergyStored(ForgeDirection.getOrientation(side));
					}
					else
					{
						max = ((IEnergyProvider) tileEntity).getMaxEnergyStored(ForgeDirection.getOrientation(side));
						stored = ((IEnergyProvider) tileEntity).getEnergyStored(ForgeDirection.getOrientation(side));
					}
					if (max > 0)
						player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_INFO + "energyStorage", stored, max));
					return true;
				}
				if (player.isSneaking() && tileEntity instanceof IImmersiveConnectable)
				{
					if (!ItemNBTHelper.hasKey(stack, "linkingPos"))
						ItemNBTHelper.setIntArray(stack, "linkingPos", new int[] { world.provider.dimensionId, x, y, z });
					else
					{
						int[] pos = ItemNBTHelper.getIntArray(stack, "linkingPos");
						if (pos[0] == world.provider.dimensionId)
						{
							IImmersiveConnectable nodeHere = (IImmersiveConnectable) tileEntity;
							TileEntity te2 = world.getTileEntity(pos[1], pos[2], pos[3]);
							if (!(te2 instanceof IImmersiveConnectable))
							{
								player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_WARN + "invalidPoint"));
								return true;
							}
							IImmersiveConnectable nodeLink = (IImmersiveConnectable) te2;
							Set<AbstractConnection> connections = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(nodeLink), world);
							for (AbstractConnection con : connections)
							{
								if (Utils.toCC(nodeHere).equals(con.end))
									player.addChatComponentMessage(new ChatComponentTranslation(Lib.CHAT_INFO + "averageLoss", Utils.formatDouble(con.getAverageLossRate() * 100, "###.000")));
							}
						}
						ItemNBTHelper.remove(stack, "linkingPos");
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		if (player.getCurrentEquippedItem() != null && this.equals(player.getCurrentEquippedItem().getItem()))
			return player.getCurrentEquippedItem().getItemDamage() == 0;
		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (stack.getItemDamage() == 3)
		{
			player.triggerAchievement(IEAchievements.openManual);
			if (world.isRemote)
				player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Manual, world, (int) player.posX, (int) player.posY, (int) player.posZ);
		}
		return stack;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass)
	{
		if (this.getToolClasses(stack).contains(toolClass))
			return 2;
		else
			return -1;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return stack.getItemDamage() == 0 && ItemNBTHelper.getInt(stack, "hammerDmg") > 0;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "hammerDmg") / (double) hammerMaxDamage;
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return hammerMaxDamage;
	}

	@Override
	public boolean isDamaged(ItemStack stack)
	{
		return false;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		int meta = stack.getItemDamage();
		return meta == 0 ? ImmutableSet.of(Lib.TOOL_HAMMER) : meta == 1 ? ImmutableSet.of(Lib.TOOL_WIRECUTTER) : new HashSet<>();
	}

	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta)
	{
		if (ForgeHooks.isToolEffective(stack, block, meta))
			return 6;
		return super.getDigSpeed(stack, block, meta);
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|item")
	public boolean isUsable(ItemStack stack, EntityLivingBase living, int x, int y, int z)
	{
		return stack != null && stack.getItemDamage() == 0;
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|item")
	public void toolUsed(ItemStack stack, EntityLivingBase living, int x, int y, int z)
	{
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return item.getItemDamage() != 3;
	}
}