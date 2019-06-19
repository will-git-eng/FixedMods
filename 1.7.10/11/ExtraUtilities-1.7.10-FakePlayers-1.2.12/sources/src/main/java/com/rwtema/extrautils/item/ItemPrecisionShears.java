package com.rwtema.extrautils.item;

import cofh.api.block.IDismantleable;
import com.rwtema.extrautils.EventHandlerEntityItemStealer;
import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.helper.XURandom;
import com.rwtema.extrautils.network.NetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ItemPrecisionShears extends ItemShears implements IItemMultiTransparency
{
	public static final Item[] toolsToMimic = { Items.stone_pickaxe, Items.stone_axe, Items.stone_shovel, Items.stone_sword, Items.stone_hoe, Items.shears };
	public static final ItemStack[] toolStacks = new ItemStack[toolsToMimic.length];
	public static final int[] COOLDOWN;
	public Random rand = XURandom.getInstance();
	private IIcon[] icons;

	public static int getCooldown(ItemStack stack)
	{
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
		if (i < 0)
			i = 0;

		if (i >= COOLDOWN.length)
			i = COOLDOWN.length - 1;

		return COOLDOWN[i];
	}

	public ItemPrecisionShears()
	{
		this.setCreativeTab(ExtraUtils.creativeTabExtraUtils);
		this.setUnlocalizedName("extrautils:shears");
		this.setMaxStackSize(1);
		this.setMaxDamage(1024);
	}

	public int cofh_canEnchantApply(ItemStack stack, Enchantment ench)
	{
		return ench.type == EnumEnchantmentType.digger ? 1 : -1;
	}

	@Override
	public int getItemEnchantability()
	{
		return Items.iron_pickaxe.getItemEnchantability();
	}

	@Override
	public boolean isItemTool(ItemStack p_77616_1_)
	{
		return p_77616_1_.stackSize == 1;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer player)
	{
		World worldObj = player.worldObj;
		if (worldObj.isRemote)
			return false;
		else
		{
			Block block = worldObj.getBlock(x, y, z);
			int meta = worldObj.getBlockMetadata(x, y, z);
			worldObj.playAuxSFXAtEntity(player, 2001, x, y, z, Block.getIdFromBlock(block) + (worldObj.getBlockMetadata(x, y, z) << 12));
			boolean flag1 = block.canHarvestBlock(player, meta);
			if (itemstack != null)
			{
				itemstack.func_150999_a(worldObj, block, x, y, z, player);
				if (itemstack.stackSize == 0)
					player.destroyCurrentEquippedItem();
			}

			List<EntityItem> extraDrops = null;
			List<EntityItem> baseCapturedDrops = null;
			EventHandlerEntityItemStealer.startCapture();
			if (block instanceof IDismantleable && ((IDismantleable) block).canDismantle(player, worldObj, x, y, z))
				((IDismantleable) block).dismantleBlock(player, worldObj, x, y, z, false);
			else
			{
				block.onBlockHarvested(worldObj, x, y, z, meta, player);
				if (block.removedByPlayer(worldObj, player, x, y, z, true))
				{
					block.onBlockDestroyedByPlayer(worldObj, x, y, z, meta);
					if (flag1 || player.capabilities.isCreativeMode)
					{
						extraDrops = EventHandlerEntityItemStealer.getCapturedEntities();
						EventHandlerEntityItemStealer.startCapture();
						block.harvestBlock(worldObj, player, x, y, z, meta);
						baseCapturedDrops = EventHandlerEntityItemStealer.getCapturedEntities();
					}
				}
			}

			EventHandlerEntityItemStealer.stopCapture();
			boolean added = false;
			if (baseCapturedDrops == null)
				baseCapturedDrops = EventHandlerEntityItemStealer.getCapturedEntities();

			if (extraDrops != null)
				baseCapturedDrops.addAll(extraDrops);

			for (EntityItem j : baseCapturedDrops)
			{
				if (player.inventory.addItemStackToInventory(j.getEntityItem()))
				{
					added = true;
					NetworkHandler.sendParticle(worldObj, "reddust", j.posX, j.posY, j.posZ, 0.5D + this.rand.nextDouble() * 0.15D, 0.35D, 0.65D + this.rand.nextDouble() * 0.3D, false);
				}

				if (j.getEntityItem() != null && j.getEntityItem().stackSize > 0)
					worldObj.spawnEntityInWorld(new EntityItem(j.worldObj, j.posX, j.posY, j.posZ, j.getEntityItem()));
			}

			if (added)
			{
				for (int i = 0; i < 10; ++i)
				{
					NetworkHandler.sendParticle(worldObj, "reddust", x + this.rand.nextDouble(), y + this.rand.nextDouble(), z + this.rand.nextDouble(), 0.5D + this.rand.nextDouble() * 0.15D, 0.35D, 0.65D + this.rand.nextDouble() * 0.3D, false);
				}

				((EntityPlayerMP) player).mcServer.getConfigurationManager().syncPlayerInventory((EntityPlayerMP) player);
			}

			return true;
		}
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!player.isSneaking())
			return false;
		else if (!this.check(par1ItemStack, world))
			return true;
		else if (world.isAirBlock(x, y, z))
			return false;
		else
		{
			Block block = world.getBlock(x, y, z);
			int meta = world.getBlockMetadata(x, y, z);
			if (block.getBlockHardness(world, x, y, z) >= 0.0F || block instanceof IDismantleable && ((IDismantleable) block).canDismantle(player, world, x, y, z))
			{
				if (block.canHarvestBlock(player, meta))
				{
					player.swingItem();
					if (!world.isRemote && player instanceof EntityPlayerMP)
					{
						if (!this.check(par1ItemStack, world))
							return true;
						else
						{
							if (!world.isAirBlock(x, y, z) && block.getBlockHardness(world, x, y, z) >= 0.0F)
								((EntityPlayerMP) player).theItemInWorldManager.tryHarvestBlock(x, y, z);

							return true;
						}
					}
					else
						return true;
				}
				else
					return false;
			}
			else
				return false;
		}
	}

	private void collectItems(World world, EntityPlayer player, double x, double y, double z, List before, List after)
	{
		Iterator iter = after.iterator();
		boolean added = false;

		while (iter.hasNext())
		{
			EntityItem j = (EntityItem) iter.next();
			if (j.getClass() == EntityItem.class && !before.contains(j) && player.inventory.addItemStackToInventory(j.getEntityItem()))
			{
				NetworkHandler.sendParticle(world, "reddust", j.posX, j.posY, j.posZ, 0.5D + this.rand.nextDouble() * 0.15D, 0.35D, 0.65D + this.rand.nextDouble() * 0.3D, false);
				added = true;
				if (j.getEntityItem() == null || j.getEntityItem().stackSize == 0)
					j.setDead();
			}
		}

		if (added)
		{
			for (int i = 0; i < 10; ++i)
			{
				NetworkHandler.sendParticle(world, "reddust", x + this.rand.nextDouble(), y + this.rand.nextDouble(), z + this.rand.nextDouble(), 0.5D + this.rand.nextDouble() * 0.15D, 0.35D, 0.65D + this.rand.nextDouble() * 0.3D, false);
    
    
				((EntityPlayerMP) player).mcServer.getConfigurationManager().syncPlayerInventory((EntityPlayerMP) player);
		}

	}

	@Override
	public boolean itemInteractionForEntity(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity)
	{
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ).offset(0.5D, 0.5D, 0.5D).expand(3.0D, 3.0D, 3.0D);
		List items = player.worldObj.getEntitiesWithinAABB(EntityItem.class, aabb);
		boolean sheared = super.itemInteractionForEntity(itemstack, player, entity);
		if (sheared)
			this.collectItems(player.worldObj, player, entity.posX - 0.5D, entity.posY - 0.5D, entity.posZ - 0.5D, items, player.worldObj.getEntitiesWithinAABB(EntityItem.class, aabb));

		return sheared;
	}

	@Override
	public boolean canHarvestBlock(Block par1Block, ItemStack item)
	{
		for (Item tool : toolsToMimic)
		{
			if (tool.canHarvestBlock(par1Block, new ItemStack(tool)))
				return true;
		}

		return false;
	}

	@Override
	public float func_150893_a(ItemStack stack, Block block)
	{
		for (ItemStack tool : toolStacks)
		{
			if (ForgeHooks.isToolEffective(tool, block, 0))
				return 4.0F;
		}

		return super.func_150893_a(stack, block);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IIconRegister)
	{
		this.icons = new IIcon[2];
		this.itemIcon = this.icons[0] = par1IIconRegister.registerIcon(this.getUnlocalizedName().substring(5));
		this.icons[1] = par1IIconRegister.registerIcon(this.getUnlocalizedName().substring(5) + "1");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int numIcons(ItemStack item)
	{
		return Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.worldObj != null && !this.check(item, Minecraft.getMinecraft().thePlayer.worldObj) ? 1 : 2;
	}

	@Override
	public IIcon getIconForTransparentRender(ItemStack item, int pass)
	{
		return this.icons[pass];
	}

	@Override
	public float getIconTransparency(ItemStack item, int pass)
	{
		return pass == 1 ? 0.5F : 1.0F;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack itemstack, World par2World, Block par3, int par4, int par5, int par6, EntityLivingBase par7EntityLivingBase)
	{
		itemstack.damageItem(1, par7EntityLivingBase);
		NBTTagCompound tag = itemstack.getTagCompound();
		if (tag == null)
			tag = new NBTTagCompound();

		tag.setInteger("dim", par2World.provider.dimensionId);
		tag.setLong("time", par2World.getTotalWorldTime());
		itemstack.setTagCompound(tag);
		return true;
	}

	public boolean check(ItemStack itemstack, World world)
	{
		if (!itemstack.hasTagCompound())
			return true;
		else if (!itemstack.getTagCompound().hasKey("dim") && !itemstack.getTagCompound().hasKey("time"))
			return true;
		else
		{
			long totalWorldTime = world.getTotalWorldTime();
			long time = itemstack.getTagCompound().getLong("time") + getCooldown(itemstack);
			if (itemstack.getTagCompound().getInteger("dim") == world.provider.dimensionId && time >= totalWorldTime)
				return false;
			else
			{
				if (!world.isRemote)
				{
					itemstack.getTagCompound().removeTag("dim");
					itemstack.getTagCompound().removeTag("time");
					if (itemstack.getTagCompound().hasNoTags())
						itemstack.setTagCompound(null);
				}

				return true;
			}
		}
	}

	@Override
	public void onUpdate(ItemStack itemstack, World par2World, Entity par3Entity, int par4, boolean par5)
	{
		this.check(itemstack, par2World);
	}

	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack)
	{
		if (itemstack.hasTagCompound())
		{
			itemstack.getTagCompound().removeTag("dim");
			itemstack.getTagCompound().removeTag("time");
			if (itemstack.getTagCompound().hasNoTags())
				itemstack.setTagCompound(null);
		}

		return null;
	}

	static
	{
		for (int i = 0; i < toolsToMimic.length; ++i)
		{
			toolStacks[i] = new ItemStack(toolsToMimic[i]);
		}

		COOLDOWN = new int[] { 20, 16, 12, 8, 4, 0 };
	}
}
