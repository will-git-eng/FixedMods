package com.rwtema.extrautils.item;

import ru.will.git.reflectionmedic.util.EventUtils;
import com.rwtema.extrautils.ExtraUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Facing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemGoldenLasso extends Item
{
	public ItemGoldenLasso()
	{
		this.setCreativeTab(ExtraUtils.creativeTabExtraUtils);
		this.maxStackSize = 1;
		this.setHasSubtypes(true);
		this.setUnlocalizedName("extrautils:golden_lasso");
		this.setTextureName("extrautils:golden_lasso");
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, EntityLivingBase par2EntityLiving)
	{
		if (par1ItemStack.hasTagCompound())
		{
			if (par1ItemStack.getItemDamage() != 0)
				return false;

			par1ItemStack.setTagCompound(null);
		}

		if (!(par2EntityLiving instanceof EntityCreature) && !(par2EntityLiving instanceof EntityAmbientCreature))
			return false;
		else if (par2EntityLiving instanceof EntityMob)
			return false;
		else if (((EntityLiving) par2EntityLiving).getAttackTarget() != null)
			return false;
		else
    
			if (EventUtils.cantDamage(par2EntityPlayer, par2EntityLiving))
    

			NBTTagCompound entityTags = new NBTTagCompound();
			entityTags.setBoolean("com.rwtema.extrautils.goldenlasso", true);
			if (!par2EntityLiving.writeMountToNBT(entityTags))
				return false;
			else if (!entityTags.hasKey("com.rwtema.extrautils.goldenlasso") | !entityTags.getBoolean("com.rwtema.extrautils.goldenlasso"))
				return false;
			else
			{
				String name = "";
				if (((EntityLiving) par2EntityLiving).hasCustomNameTag())
					name = ((EntityLiving) par2EntityLiving).getCustomNameTag();

				if (!par2EntityLiving.worldObj.isRemote)
					par2EntityLiving.setDead();

				par1ItemStack.setTagCompound(entityTags);
				if (name.equals(""))
				{
					if (par2EntityLiving instanceof EntityVillager)
						par1ItemStack.setItemDamage(2);
					else
						par1ItemStack.setItemDamage(1);
				}
				else
				{
					par1ItemStack.setStackDisplayName(name);
					par1ItemStack.setItemDamage(2);
				}

				return true;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack, int pass)
	{
		return par1ItemStack.getItemDamage() != 0;
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
	{
		if (par1ItemStack.getItemDamage() == 0 | !par1ItemStack.hasTagCompound())
		{
			par1ItemStack.setItemDamage(0);
			return false;
		}
		else if (!par1ItemStack.getTagCompound().hasKey("id"))
		{
			par1ItemStack.setItemDamage(0);
			return false;
		}
		else if (par3World.isRemote)
			return true;
		else
		{
			Block i1 = par3World.getBlock(par4, par5, par6);
			par4 = par4 + Facing.offsetsXForSide[par7];
			par5 = par5 + Facing.offsetsYForSide[par7];
			par6 = par6 + Facing.offsetsZForSide[par7];
			double d0 = 0.0D;
			if (par7 == 1 && i1 != null && i1.getRenderType() == 11)
				d0 = 0.5D;

			NBTTagCompound tags = par1ItemStack.getTagCompound();
			tags.setTag("Pos", this.newDoubleNBTList(par4 + 0.5D, par5 + d0, par6 + 0.5D));
			tags.setTag("Motion", this.newDoubleNBTList(0.0D, 0.0D, 0.0D));
			tags.setFloat("FallDistance", 0.0F);
			tags.setInteger("Dimension", par3World.provider.dimensionId);
			Entity entity = EntityList.createEntityFromNBT(tags, par3World);
			if (entity != null && entity instanceof EntityLiving && par1ItemStack.hasDisplayName())
				((EntityLiving) entity).setCustomNameTag(par1ItemStack.getDisplayName());

			par3World.spawnEntityInWorld(entity);
			par1ItemStack.setTagCompound(null);
			par1ItemStack.setItemDamage(0);
			if (par2EntityPlayer.capabilities.isCreativeMode)
				par2EntityPlayer.setCurrentItemOrArmor(0, new ItemStack(ExtraUtils.goldenLasso, 1, 0));

			par2EntityPlayer.inventory.markDirty();
			return true;
		}
	}

	protected NBTTagList newDoubleNBTList(double... par1ArrayOfDouble)
	{
		NBTTagList nbttaglist = new NBTTagList();

		for (double d1 : par1ArrayOfDouble)
		{
			nbttaglist.appendTag(new NBTTagDouble(d1));
		}

		return nbttaglist;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		if (par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().hasKey("id"))
		{
			par3List.set(0, ((String) par3List.get(0)).replaceFirst(EnumChatFormatting.ITALIC + par1ItemStack.getDisplayName() + EnumChatFormatting.RESET, this.getItemStackDisplayName(par1ItemStack)));
			String animal_name = StatCollector.translateToLocal("entity." + par1ItemStack.getTagCompound().getString("id") + ".name");
			par3List.add(animal_name);
			if (par1ItemStack.hasDisplayName())
				if (par1ItemStack.getTagCompound().hasKey("spoiler"))
					par3List.add("*this " + animal_name.toLowerCase() + " has chosen a new name*");
				else
					par3List.add(par1ItemStack.getDisplayName());
		}
	}
}
