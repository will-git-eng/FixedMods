package chylex.hee.item;

import chylex.hee.entity.fx.FXType;
import chylex.hee.mechanics.causatum.CausatumMeters;
import chylex.hee.mechanics.causatum.CausatumUtils;
import chylex.hee.mechanics.enhancements.EnhancementHandler;
import chylex.hee.mechanics.enhancements.types.TransferenceGemEnhancements;
import chylex.hee.mechanics.misc.GemSideEffects;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C20Effect;
import chylex.hee.packets.client.C21EffectEntity;
import chylex.hee.system.achievements.AchievementManager;
import chylex.hee.system.util.ItemUtil;
import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.hee.EventConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class ItemTransferenceGem extends ItemAbstractEnergyAcceptor
{
	private static final GemData clientCache = new GemData();

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	@Override
	public int getMaxDamage(ItemStack is)
	{
		return this.calculateMaxDamage(is, TransferenceGemEnhancements.CAPACITY);
	}

	@Override
	public boolean canAcceptEnergy(ItemStack is)
	{
		return is.getItemDamage() > 0;
	}

	@Override
	public void onEnergyAccepted(ItemStack is)
	{
		is.setItemDamage(is.getItemDamage() - 2);
	}

	@Override
	public int getEnergyPerUse(ItemStack is)
	{
		return 3;
	}

	@Override
	protected float getRegenSpeedMultiplier()
	{
		return 0.06F;
	}

	@Override
	public void onUpdate(ItemStack is, World world, Entity entity, int slot, boolean isHeld)
	{
		if (ItemUtil.getTagRoot(is, false).hasKey("cooldown"))
		{
			byte cooldown = is.getTagCompound().getByte("cooldown");

			if (--cooldown <= 0)
				is.getTagCompound().removeTag("cooldown");
			else
				is.getTagCompound().setByte("cooldown", cooldown);
		}

		super.onUpdate(is, world, entity, slot, isHeld);
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (super.onItemUse(is, player, world, x, y, z, side, hitX, hitY, hitZ))
			return true;

		if (!world.isRemote && side == 1 && player.isSneaking())
		{
			    
			if (EventUtils.cantBreak(player, x, y, z))
				return false;
			    

			GemData data = new GemData();
			data.set(player.dimension, x, y, z);
			data.saveToItemStack(is);
			PacketPipeline.sendToAllAround(player.dimension, x, y, z, 64D, new C20Effect(FXType.Basic.GEM_LINK, x, y, z));
			return true;
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack is, World world, EntityPlayer player)
	{
		return world.isRemote || player.isSneaking() ? is : this.tryTeleportEntity(is, player, player);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack is, EntityPlayer player, EntityLivingBase entity)
	{
		if (entity instanceof IBossDisplayData || !EnhancementHandler.hasEnhancement(is, TransferenceGemEnhancements.BEAST) || player.isSneaking())
			return false;
		this.tryTeleportEntity(is, player, entity);
		return true;
	}

	@Override
	public void onCreated(ItemStack is, World world, EntityPlayer player)
	{
		player.addStat(AchievementManager.TRANSFERENCE_GEM, 1);
	}

	public ItemStack tryTeleportEntity(ItemStack is, EntityPlayer player, Entity entity)
	{
		if (entity.isRiding() || entity.riddenByEntity != null || !is.hasTagCompound())
			return is;
		if (ItemUtil.getTagRoot(is, false).hasKey("cooldown"))
			return is;

		    
		if (player != entity && (EventConfig.transferenceGemSelfOnly || EventUtils.cantDamage(player, entity)))
			return is;
		    

		GemData gemData = new GemData();
		gemData.set(is.getTagCompound());

		if (gemData.isLinked() && entity.dimension == gemData.dim)
		{
			int itemDamage = is.getItemDamage();
			if (itemDamage >= is.getMaxDamage())
				return is;

			boolean isLiving = entity instanceof EntityLivingBase;

			PacketPipeline.sendToAllAround(entity, 64D, new C21EffectEntity(FXType.Entity.GEM_TELEPORT_FROM, entity));

			this.damageItem(is, player);

			if (isLiving)
				((EntityLivingBase) entity).setPositionAndUpdate(gemData.x + 0.5D, gemData.y + 1.001D, gemData.z + 0.5D);
			entity.setLocationAndAngles(gemData.x + 0.5D, gemData.y + 1.001D, gemData.z + 0.5D, entity.rotationYaw, entity.rotationPitch);
			entity.fallDistance = 0F;

			float percBroken = itemDamage / (float) is.getMaxDamage();
			if (percBroken > 0.66F && entity.worldObj.rand.nextFloat() * 1.4F < percBroken)
				GemSideEffects.performRandomEffect(entity, percBroken);

			ItemUtil.getTagRoot(is, true).setByte("cooldown", (byte) 50);

			PacketPipeline.sendToAllAround(entity, 64D, new C20Effect(FXType.Basic.GEM_TELEPORT_TO, entity));
			CausatumUtils.increase(player, CausatumMeters.ITEM_USAGE, 1F);
		}

		return is;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack is, int pass)
	{
		return ItemUtil.getTagRoot(is, false).hasKey("cooldown") ? 192 << 16 | 192 << 8 | 192 : super.getColorFromItemStack(is, pass);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack is, EntityPlayer player, List textLines, boolean showAdvancedInfo)
	{
		if (is.hasTagCompound())
		{
			clientCache.set(is.getTagCompound());

			if (clientCache.isLinked())
			{
				textLines.add(EnumChatFormatting.GRAY + I18n.format("item.transferenceGem.info.linked"));
				if (showAdvancedInfo)
					textLines.add(EnumChatFormatting.GRAY.toString() + "DIM " + clientCache.dim + ", X " + clientCache.x + ", Y " + clientCache.y + ", Z " + clientCache.z);
			}
		}

		EnhancementHandler.appendEnhancementNames(is, textLines);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack is)
	{
		return EnumRarity.uncommon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack is, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
	{
		return this.iconArray[getIcon(is)];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconIndex(ItemStack is)
	{
		return this.iconArray[getIcon(is)];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister)
	{
		this.iconArray = new IIcon[3];
		for (int a = 0; a < this.iconArray.length; a++)
		{
			this.iconArray[a] = iconRegister.registerIcon("hardcoreenderexpansion:transference_gem_" + (a + 1));
		}
	}

	private static int getIcon(ItemStack is)
	{
		if (is.getItemDamage() == is.getMaxDamage())
			return 2;
		float percBroken = is.getItemDamage() / (float) is.getMaxDamage();
		return percBroken > 0.56F ? 1 : 0;
	}

	private static class GemData
	{
		private int dim, x, y, z;

		private void set(NBTTagCompound nbt)
		{
			this.set(nbt.hasKey("HED_Gem_Dim") ? nbt.getInteger("HED_Gem_Dim") : -999, nbt.getInteger("HED_Gem_X"), nbt.getInteger("HED_Gem_Y"), nbt.getInteger("HED_Gem_Z"));
		}

		private void set(int dimension, int x, int y, int z)
		{
			this.dim = dimension;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public boolean isLinked()
		{
			return this.dim != -999;
		}

		public void saveToItemStack(ItemStack is)
		{
			NBTTagCompound nbt = ItemUtil.getTagRoot(is, true);
			nbt.setInteger("HED_Gem_Dim", this.dim);
			nbt.setInteger("HED_Gem_X", this.x);
			nbt.setInteger("HED_Gem_Y", this.y);
			nbt.setInteger("HED_Gem_Z", this.z);
		}
	}
}
