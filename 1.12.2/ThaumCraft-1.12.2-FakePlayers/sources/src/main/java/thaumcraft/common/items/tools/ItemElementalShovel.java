package thaumcraft.common.items.tools;

import ru.will.git.eventhelper.util.EventUtils;
import ru.will.git.eventhelper.util.FastUtils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.api.items.IArchitect;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.IThaumcraftItems;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.lib.utils.Utils;

import java.util.ArrayList;
import java.util.Set;

public class ItemElementalShovel extends ItemSpade implements IArchitect, IThaumcraftItems
{
	private static final Block[] isEffective = { Blocks.GRASS, Blocks.DIRT, Blocks.SAND, Blocks.GRAVEL, Blocks.SNOW_LAYER, Blocks.SNOW, Blocks.CLAY, Blocks.FARMLAND, Blocks.SOUL_SAND, Blocks.MYCELIUM };
	EnumFacing side = EnumFacing.DOWN;

	public ItemElementalShovel(ToolMaterial enumtoolmaterial)
	{
		super(enumtoolmaterial);
		this.setCreativeTab(ConfigItems.TABTC);
		this.setRegistryName("elemental_shovel");
		this.setUnlocalizedName("elemental_shovel");
		ConfigItems.ITEM_VARIANT_HOLDERS.add(this);
	}

	@Override
	public Item getItem()
	{
		return this;
	}

	@Override
	public String[] getVariantNames()
	{
		return new String[] { "normal" };
	}

	@Override
	public int[] getVariantMeta()
	{
		return new int[] { 0 };
	}

	@Override
	public ItemMeshDefinition getCustomMesh()
	{
		return null;
	}

	@Override
	public ModelResourceLocation getCustomModelResourceLocation(String variant)
	{
		return new ModelResourceLocation("thaumcraft:" + variant);
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		return ImmutableSet.of("shovel");
	}

	@Override
	public EnumRarity getRarity(ItemStack itemstack)
	{
		return EnumRarity.RARE;
	}

	@Override
	public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
	{
		return stack2.isItemEqual(new ItemStack(ItemsTC.ingots, 1, 0)) || super.getIsRepairable(stack1, stack2);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float par8, float par9, float par10)
	{
		IBlockState bs = world.getBlockState(pos);
		TileEntity te = world.getTileEntity(pos);
		if (te == null)
			for (int aa = -1; aa <= 1; ++aa)
			{
				for (int bb = -1; bb <= 1; ++bb)
				{
					int xOffset = 0;
					int yOffset = 0;
					int zOffset = 0;
					byte o = getOrientation(player.getHeldItem(hand));
					if (o == 1)
					{
						yOffset = bb;
						if (side.ordinal() <= 1)
						{
							int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
							if (l != 0 && l != 2)
								zOffset = aa;
							else
								xOffset = aa;
						}
						else if (side.ordinal() <= 3)
							zOffset = aa;
						else
							xOffset = aa;
					}
					else if (o == 2)
						if (side.ordinal() <= 1)
						{
							int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
							yOffset = bb;
							if (l != 0 && l != 2)
								zOffset = aa;
							else
								xOffset = aa;
						}
						else
						{
							zOffset = bb;
							xOffset = aa;
						}
					else if (side.ordinal() <= 1)
					{
						xOffset = aa;
						zOffset = bb;
					}
					else if (side.ordinal() <= 3)
					{
						xOffset = aa;
						yOffset = bb;
					}
					else
					{
						zOffset = aa;
						yOffset = bb;
					}

					BlockPos p2 = pos.offset(side).add(xOffset, yOffset, zOffset);
					if (bs.getBlock().canPlaceBlockAt(world, p2))
					{
						boolean creative = player.capabilities.isCreativeMode;

						
						// if (!creative && !InventoryUtils.consumePlayerItem(player, Item.getItemFromBlock(bs.getBlock()), bs.getBlock().getMetaFromState(bs)))
						ItemStack removedStack = ItemStack.EMPTY;
						if (!creative && (removedStack = InventoryUtils.consumePlayerItemStack(player, Item.getItemFromBlock(bs.getBlock()), bs.getBlock().getMetaFromState(bs))).isEmpty())
						
						{
							
							// if (bs.getBlock() == Blocks.GRASS && (creative || InventoryUtils.consumePlayerItem(player, Item.getItemFromBlock(Blocks.DIRT), 0)))
							if (bs.getBlock() == Blocks.GRASS && (creative || !(removedStack = InventoryUtils.consumePlayerItemStack(player, Item.getItemFromBlock(Blocks.DIRT), 0)).isEmpty()))
							
							{
								IBlockState newState = Blocks.DIRT.getDefaultState();

								
								if (EventUtils.cantPlace(player, p2, newState))
								{
									if (FastUtils.isValidRealPlayer(player))
									{
										if (player instanceof EntityPlayerMP)
											((EntityPlayerMP) player).connection.sendPacket(new SPacketBlockChange(world, p2));
										if (!creative && !removedStack.isEmpty())
											player.inventory.addItemStackToInventory(removedStack);
									}
									continue;
								}
								

								world.playSound((double) p2.getX(), (double) p2.getY(), (double) p2.getZ(), bs.getBlock().getSoundType().getBreakSound(), SoundCategory.BLOCKS, 0.6F, 0.9F + world.rand.nextFloat() * 0.2F, false);
								world.setBlockState(p2, newState);
								player.getHeldItem(hand).damageItem(1, player);
								if (world.isRemote)
									FXDispatcher.INSTANCE.drawBamf(p2, 8401408, false, false, side);

								player.swingArm(hand);
								if (player.getHeldItem(hand).isEmpty() || player.getHeldItem(hand).getCount() < 1)
									break;
							}
						}
						else
						{
							
							if (EventUtils.cantPlace(player, p2, bs))
							{
								if (FastUtils.isValidRealPlayer(player))
								{
									if (player instanceof EntityPlayerMP)
										((EntityPlayerMP) player).connection.sendPacket(new SPacketBlockChange(world, p2));
									if (!creative && !removedStack.isEmpty())
										player.inventory.addItemStackToInventory(removedStack);
								}
								continue;
							}
							

							world.playSound((double) p2.getX(), (double) p2.getY(), (double) p2.getZ(), bs.getBlock().getSoundType().getBreakSound(), SoundCategory.BLOCKS, 0.6F, 0.9F + world.rand.nextFloat() * 0.2F, false);
							world.setBlockState(p2, bs);
							player.getHeldItem(hand).damageItem(1, player);
							if (world.isRemote)
								FXDispatcher.INSTANCE.drawBamf(p2, 8401408, false, false, side);

							player.swingArm(hand);
						}
					}
				}
			}

		return EnumActionResult.FAIL;
	}

	private boolean isEffectiveAgainst(Block block)
	{
		for (Block block1 : isEffective)
		{
			if (block1 == block)
				return true;
		}

		return false;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		if (tab != ConfigItems.TABTC && tab != CreativeTabs.SEARCH)
			super.getSubItems(tab, items);
		else
		{
			ItemStack w1 = new ItemStack(this);
			EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.DESTRUCTIVE, 1);
			items.add(w1);
		}

	}

	@Override
	public ArrayList<BlockPos> getArchitectBlocks(ItemStack focusstack, World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		ArrayList<BlockPos> b = new ArrayList<>();
		if (!player.isSneaking())
			return b;

		IBlockState bs = world.getBlockState(pos);

		for (int aa = -1; aa <= 1; ++aa)
		{
			for (int bb = -1; bb <= 1; ++bb)
			{
				int xx = 0;
				int yy = 0;
				int zz = 0;
				byte o = getOrientation(focusstack);
				if (o == 1)
				{
					yy = bb;
					if (side.ordinal() <= 1)
					{
						int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
						if (l != 0 && l != 2)
							zz = aa;
						else
							xx = aa;
					}
					else if (side.ordinal() <= 3)
						zz = aa;
					else
						xx = aa;
				}
				else if (o == 2)
					if (side.ordinal() <= 1)
					{
						int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
						yy = bb;
						if (l != 0 && l != 2)
							zz = aa;
						else
							xx = aa;
					}
					else
					{
						zz = bb;
						xx = aa;
					}
				else if (side.ordinal() <= 1)
				{
					xx = aa;
					zz = bb;
				}
				else if (side.ordinal() <= 3)
				{
					xx = aa;
					yy = bb;
				}
				else
				{
					zz = aa;
					yy = bb;
				}

				BlockPos p2 = pos.offset(side).add(xx, yy, zz);
				world.getBlockState(p2);
				if (bs.getBlock().canPlaceBlockAt(world, p2))
					b.add(p2);
			}
		}

		return b;
	}

	@Override
	public boolean showAxis(ItemStack stack, World world, EntityPlayer player, EnumFacing side, IArchitect.EnumAxis axis)
	{
		return false;
	}

	public static byte getOrientation(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("or") ? stack.getTagCompound().getByte("or") : 0;
	}

	public static void setOrientation(ItemStack stack, byte o)
	{
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		if (stack.hasTagCompound())
			stack.getTagCompound().setByte("or", (byte) (o % 3));

	}

	@Override
	public RayTraceResult getArchitectMOP(ItemStack stack, World world, EntityLivingBase player)
	{
		return Utils.rayTrace(world, player, false);
	}

	@Override
	public boolean useBlockHighlight(ItemStack stack)
	{
		return true;
	}
}
