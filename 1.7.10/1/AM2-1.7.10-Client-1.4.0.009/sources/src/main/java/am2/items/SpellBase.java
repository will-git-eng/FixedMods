package am2.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Keyboard;

import ru.will.git.am2.EventConfig;

import am2.AMCore;
import am2.api.events.ManaCostEvent;
import am2.api.math.AMVector3;
import am2.api.spell.ItemSpellBase;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.api.spell.enums.Affinity;
import am2.playerextensions.SkillData;
import am2.spell.SkillManager;
import am2.spell.SkillTreeManager;
import am2.spell.SpellHelper;
import am2.spell.SpellTextureHelper;
import am2.spell.SpellUtils;
import am2.spell.shapes.MissingShape;
import am2.texture.ResourceManager;
import am2.utility.MathUtilities;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class SpellBase extends ItemSpellBase
{
	public SpellBase()
	{
		this.setMaxDamage(0);

		    
		if (EventConfig.denyStackSpell)
			this.setMaxStackSize(1);
		    
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.block;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		super.itemIcon = ResourceManager.RegisterTexture("spellFrame", par1IconRegister);
	}

	@Override
	public boolean getShareTag()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1)
	{
		return par1 == -1 ? super.itemIcon : SpellTextureHelper.instance.getIcon(par1);
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass)
	{
		return pass == 0 ? this.getIconFromDamage(stack.getItemDamage()) : super.itemIcon;
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1ItemStack)
	{
		if (par1ItemStack.stackTagCompound == null)
			return "§bMalformed Spell";
		else
		{
			ISpellShape shape = SpellUtils.instance.getShapeForStage(par1ItemStack, 0);
			if (shape instanceof MissingShape)
				return "Unnamed Spell";
			else
			{
				String clsName = shape	.getClass()
										.getName();
				return clsName.substring(clsName.lastIndexOf(46) + 1) + " Spell";
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		if (stack.hasTagCompound())
		{
			ItemStack legacySpell = SpellUtils.instance.constructSpellStack(stack);
			SpellUtils.SpellRequirements reqs = SpellUtils.instance.getSpellRequirements(legacySpell, player);
			float manaCost = reqs.manaCost;
			float burnout = reqs.burnout;
			ArrayList<ItemStack> reagents = reqs.reagents;
			ManaCostEvent mce = new ManaCostEvent(legacySpell, player, manaCost, 0.0F);
			MinecraftForge.EVENT_BUS.post(mce);
			manaCost = mce.manaCost;
			list.add(String.format("§7Mana Cost: %.2f", new Object[] { Float.valueOf(manaCost) }));
			if (Keyboard.isKeyDown(42))
			{
				HashMap<Affinity, Float> affinityData = SpellUtils.instance.AffinityFor(legacySpell);

				for (Affinity aff : affinityData.keySet())
					list.add(String.format("%s (%.2f%%)", new Object[] { aff.toString(), Float.valueOf(affinityData	.get(aff)
																													.floatValue() * 100.0F) }));

				if (stack.stackTagCompound.hasKey("Lore"))
				{
					NBTTagList nbttaglist1 = stack.stackTagCompound.getTagList("Lore", 10);
					if (nbttaglist1.tagCount() > 0)
						for (int j = 0; j < nbttaglist1.tagCount(); ++j)
							list.add(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.ITALIC + nbttaglist1.getStringTagAt(j));
				}
			}
			else
				list.add(StatCollector.translateToLocal("am2.tooltip.shiftForAffinity"));

		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
	{
		return 72000;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer caster)
	{
		if (!stack.hasDisplayName())
		{
			if (!world.isRemote)
				FMLNetworkHandler.openGui(caster, AMCore.instance, 16, world, (int) caster.posX, (int) caster.posY, (int) caster.posZ);
		}
		else
			caster.setItemInUse(stack, this.getMaxItemUseDuration(stack));

		return stack;
	}

	@Override
	public boolean hasEffect(ItemStack par1ItemStack, int pass)
	{
		return false;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int ticksUsed)
	{
		ItemStack classicStack = SpellUtils.instance.constructSpellStack(stack);
		ISpellShape shape = SpellUtils.instance.getShapeForStage(classicStack, 0);
		if (shape != null)
		{
			if (!shape.isChanneled())
				SpellHelper.instance.applyStackStage(stack, player, (EntityLivingBase) null, player.posX, player.posY, player.posZ, 0, world, true, true, 0);

			if (world.isRemote && shape.isChanneled())
				;
		}

	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer caster, int count)
	{
		SpellHelper.instance.applyStackStageOnUsing(stack, caster, caster, caster.posX, caster.posY, caster.posZ, caster.worldObj, true, true, count - 1);
		super.onUsingTick(stack, caster, count);
	}

	@Override
	public MovingObjectPosition getMovingObjectPosition(EntityLivingBase caster, World world, double range, boolean includeEntities, boolean targetWater)
	{
		MovingObjectPosition entityPos = null;
		if (includeEntities)
		{
			Entity pointedEntity = MathUtilities.getPointedEntity(world, caster, range, 1.0D);
			if (pointedEntity != null)
				entityPos = new MovingObjectPosition(pointedEntity);
		}

		float factor = 1.0F;
		float interpPitch = caster.prevRotationPitch + (caster.rotationPitch - caster.prevRotationPitch) * factor;
		float interpYaw = caster.prevRotationYaw + (caster.rotationYaw - caster.prevRotationYaw) * factor;
		double interpPosX = caster.prevPosX + (caster.posX - caster.prevPosX) * factor;
		double interpPosY = caster.prevPosY + (caster.posY - caster.prevPosY) * factor + caster.getEyeHeight();
		double interpPosZ = caster.prevPosZ + (caster.posZ - caster.prevPosZ) * factor;
		Vec3 vec3 = Vec3.createVectorHelper(interpPosX, interpPosY, interpPosZ);
		float offsetYawCos = MathHelper.cos(-interpYaw * 0.017453292F - 3.1415927F);
		float offsetYawSin = MathHelper.sin(-interpYaw * 0.017453292F - 3.1415927F);
		float offsetPitchCos = -MathHelper.cos(-interpPitch * 0.017453292F);
		float offsetPitchSin = MathHelper.sin(-interpPitch * 0.017453292F);
		float finalXOffset = offsetYawSin * offsetPitchCos;
		float finalZOffset = offsetYawCos * offsetPitchCos;
		Vec3 targetVector = vec3.addVector(finalXOffset * range, offsetPitchSin * range, finalZOffset * range);
		MovingObjectPosition mop = world.rayTraceBlocks(vec3, targetVector, targetWater);
		return entityPos != null && mop != null ? new AMVector3(mop.hitVec).distanceSqTo(new AMVector3(caster)) < new AMVector3(entityPos.hitVec).distanceSqTo(new AMVector3(caster)) ? mop : entityPos : entityPos != null ? entityPos : mop;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List list)
	{
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5)
	{
		super.onUpdate(stack, world, entity, par4, par5);
		if (entity instanceof EntityPlayerSP)
		{
			EntityPlayerSP player = (EntityPlayerSP) entity;
			ItemStack usingItem = player.getItemInUse();
			if (usingItem != null && usingItem.getItem() == this && SkillData	.For(player)
																				.isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("SpellMotion"))))
			{
				player.movementInput.moveForward *= 2.5F;
				player.movementInput.moveStrafe *= 2.5F;
			}
		}
	}
}
