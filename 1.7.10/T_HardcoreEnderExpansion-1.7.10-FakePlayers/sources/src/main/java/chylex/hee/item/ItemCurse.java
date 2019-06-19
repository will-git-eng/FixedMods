package chylex.hee.item;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.entity.projectile.EntityProjectileCurse;
import chylex.hee.entity.technical.EntityTechnicalCurseBlock;
import chylex.hee.entity.technical.EntityTechnicalCurseEntity;
import chylex.hee.mechanics.curse.CurseType;
import chylex.hee.system.achievements.AchievementManager;
import chylex.hee.system.util.BlockPosM;
import ru.will.git.reflectionmedic.util.EventUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemCurse extends Item
{
	@SideOnly(Side.CLIENT)
	private IIcon icon1, icon2;

	public ItemCurse()
	{
		this.setHasSubtypes(true);
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		BlockPosM tmpPos = BlockPosM.tmp(x, y, z);
		Block block = tmpPos.getBlock(world);

		if (block == Blocks.snow_layer && (tmpPos.getMetadata(world) & 7) < 1)
			side = 1;
		else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z))
			tmpPos.move(side);

		if (is.stackSize == 0)
			return false;
		else
		{
			CurseType type = CurseType.getFromDamage(is.getItemDamage());
			if (type == null)
				return false;

			if (!world.isRemote)
			{
				    
				if (EventUtils.cantBreak(player, tmpPos.x, tmpPos.y, tmpPos.z))
					return false;
				    

				world.spawnEntityInWorld(new EntityTechnicalCurseBlock(world, tmpPos.x, tmpPos.y, tmpPos.z, player.getUniqueID(), type, CurseType.isEternal(is.getItemDamage())));
				--is.stackSize;
			}
			else
				world.playSound(tmpPos.x + 0.5D, tmpPos.y, tmpPos.z + 0.5D, "hardcoreenderexpansion:mob.random.curse", 0.8F, 0.9F + itemRand.nextFloat() * 0.2F, false);

			return true;
		}
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack is, EntityPlayer player, EntityLivingBase entity)
	{
		CurseType type = CurseType.getFromDamage(is.getItemDamage());
		if (type == null)
			return false;

		if (!player.worldObj.isRemote)
		{
			    
			if (EventUtils.cantBreak(player, entity.posX, entity.posY, entity.posZ))
				return false;
			    

			player.worldObj.spawnEntityInWorld(new EntityTechnicalCurseEntity(player.worldObj, entity, type, CurseType.isEternal(is.getItemDamage())));
		}
		else
		{
			player.worldObj.playSound(entity.posX, entity.posY, entity.posZ, "hardcoreenderexpansion:mob.random.curse", 0.8F, 0.9F + itemRand.nextFloat() * 0.2F, false);
			for (int a = 0; a < 40; a++)
			{
				HardcoreEnderExpansion.fx.curse(player.worldObj, entity.posX + (itemRand.nextDouble() - 0.5D) * 1.5D, entity.posY + (itemRand.nextDouble() - 0.5D) * 1.5D, entity.posZ + (itemRand.nextDouble() - 0.5D) * 1.5D, type);
			}
		}

		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack is, World world, EntityPlayer player)
	{
		CurseType type = CurseType.getFromDamage(is.getItemDamage());
		if (type == null)
			return is;

		if (!world.isRemote)
			world.spawnEntityInWorld(new EntityProjectileCurse(world, player, type, CurseType.isEternal(is.getItemDamage())));
		if (!player.capabilities.isCreativeMode)
			--is.stackSize;
		return is;
	}

	@Override
	public void onCreated(ItemStack is, World world, EntityPlayer player)
	{
		player.addStat(AchievementManager.CURSE, 1);
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		CurseType type = CurseType.getFromDamage(is.getItemDamage());
		return "item.curse." + (type == null ? "invalid" : type.name().toLowerCase().replaceAll("_", ""));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack is, EntityPlayer player, List textLines, boolean showAdvancedInfo)
	{
		if (CurseType.isEternal(is.getItemDamage()))
			textLines.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("item.curse.eternal"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tabs, List list)
	{
		for (CurseType type : CurseType.values())
		{
			list.add(new ItemStack(item, 1, type.damage));
			list.add(new ItemStack(item, 1, type.damage | 256));
		}
	}

	@Override
	public int getColorFromItemStack(ItemStack is, int pass)
	{
		CurseType type = CurseType.getFromDamage(is.getItemDamage());
		return type == null ? 16777215 : type.getColor(pass);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int damage, int pass)
	{
		return pass == 0 ? this.icon1 : this.icon2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	public int getRenderPasses(int metadata)
	{
		return 2;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.icon1 = iconRegister.registerIcon(this.iconString + "_1");
		this.icon2 = iconRegister.registerIcon(this.iconString + "_2");
	}
}
