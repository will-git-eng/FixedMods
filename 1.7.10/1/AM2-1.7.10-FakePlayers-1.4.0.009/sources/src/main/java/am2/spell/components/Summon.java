package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.api.power.PowerTypes;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.blocks.BlocksCommonProxy;
import am2.entities.EntityBattleChicken;
import am2.entities.EntityHellCow;
import am2.items.ItemCrystalPhylactery;
import am2.items.ItemOre;
import am2.items.ItemsCommonProxy;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import am2.utility.EntityUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class Summon implements ISpellComponent
{
	@Override
	public int getID()
	{
		return 61;
	}

	public EntityLiving summonCreature(ItemStack stack, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z)
	{
		    
		EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
		if (EventUtils.cantBreak(player, x, y, z))
			return null;
		    

		Class clazz = this.getSummonType(stack);
		EntityLiving entity = null;

		try
		{
			entity = (EntityLiving) clazz.getConstructor(new Class[] { World.class }).newInstance(new Object[] { world });
		}
		catch (Throwable var14)
		{
			var14.printStackTrace();
			return null;
		}

		if (entity == null)
			return null;
		else
		{
			if (entity instanceof EntitySkeleton)
			{
				((EntitySkeleton) entity).setSkeletonType(0);
				((EntitySkeleton) entity).setCurrentItemOrArmor(0, new ItemStack(Items.bow));
			}
			else if (entity instanceof EntityHorse && caster instanceof EntityPlayer)
				((EntityHorse) entity).setTamedBy((EntityPlayer) caster);

			entity.setPosition(x, y, z);
			world.spawnEntityInWorld(entity);
			if (caster instanceof EntityPlayer)
				EntityUtilities.makeSummon_PlayerFaction((EntityCreature) entity, (EntityPlayer) caster, false);
			else
				EntityUtilities.makeSummon_MonsterFaction((EntityCreature) entity, false);

			EntityUtilities.setOwner(entity, caster);
			int duration = SpellUtils.instance.getModifiedInt_Mul(4800, stack, caster, target, world, 0, SpellModifiers.DURATION);
			EntityUtilities.setSummonDuration(entity, duration);
			SpellHelper.instance.applyStageToEntity(stack, caster, world, entity, 0, false);
			return entity;
		}
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[7];
		ItemOre var10007 = ItemsCommonProxy.itemOre;
		var10000[0] = new ItemStack(ItemsCommonProxy.itemOre, 1, 4);
		var10007 = ItemsCommonProxy.itemOre;
		var10000[1] = new ItemStack(ItemsCommonProxy.itemOre, 1, 3);
		var10000[2] = BlocksCommonProxy.cerublossom;
		var10000[3] = ItemsCommonProxy.mobFocus;
		ItemCrystalPhylactery var2 = ItemsCommonProxy.crystalPhylactery;
		var10000[4] = new ItemStack(ItemsCommonProxy.crystalPhylactery, 1, 3);
		var10000[5] = String.format("E:%d", new Object[] { Integer.valueOf(PowerTypes.DARK.ID()) });
		var10000[6] = Integer.valueOf(1500);
		return var10000;
	}

	public void setSummonType(ItemStack stack, ItemStack phylacteryStack)
	{
		int var10000 = phylacteryStack.getItemDamage();
		ItemCrystalPhylactery var10001 = ItemsCommonProxy.crystalPhylactery;
		if (var10000 == 3 && phylacteryStack.getItem() instanceof ItemCrystalPhylactery)
		{
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());

			this.setSummonType(stack, ItemsCommonProxy.crystalPhylactery.getSpawnClass(phylacteryStack));
		}

	}

	public Class getSummonType(ItemStack stack)
	{
		String s = SpellUtils.instance.getSpellMetadata(stack, "SummonType");
		if (s == null || s == "")
			s = "Skeleton";

		Class clazz = (Class) EntityList.stringToClassMapping.get(s);
		return clazz;
	}

	public void setSummonType(ItemStack stack, String s)
	{
		Class clazz = (Class) EntityList.stringToClassMapping.get(s);
		this.setSummonType(stack, clazz);
	}

	public void setSummonType(ItemStack stack, Class clazz)
	{
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		clazz = this.checkForSpecialSpawns(stack, clazz);
		String s = (String) EntityList.classToStringMapping.get(clazz);
		if (s == null)
			s = "";

		SpellUtils.instance.setSpellMetadata(stack, "SpawnClassName", s);
		SpellUtils.instance.setSpellMetadata(stack, "SummonType", s);
	}

	private Class checkForSpecialSpawns(ItemStack stack, Class clazz)
	{
		if (clazz == EntityChicken.class)
		{
			if (SpellUtils.instance.modifierIsPresent(SpellModifiers.DAMAGE, stack, 0) && SpellUtils.instance.componentIsPresent(stack, Haste.class, 0))
				return EntityBattleChicken.class;
		}
		else if (clazz == EntityCow.class && SpellUtils.instance.modifierIsPresent(SpellModifiers.DAMAGE, stack, 0) && SpellUtils.instance.componentIsPresent(stack, AstralDistortion.class, 0))
			return EntityHellCow.class;

		return clazz;
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		if (!world.isRemote)
			if (ExtendedProperties.For(caster).getCanHaveMoreSummons())
			{
				if (this.summonCreature(stack, caster, caster, world, impactX, impactY, impactZ) == null)
					return false;
			}
			else if (caster instanceof EntityPlayer)
				((EntityPlayer) caster).addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.noMoreSummons")));

		return true;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (target instanceof EntityLivingBase && EntityUtilities.isSummon((EntityLivingBase) target))
			return false;
		else
		{
			if (!world.isRemote)
				if (ExtendedProperties.For(caster).getCanHaveMoreSummons())
				{
					if (this.summonCreature(stack, caster, caster, world, target.posX, target.posY, target.posZ) == null)
						return false;
				}
				else if (caster instanceof EntityPlayer)
					((EntityPlayer) caster).addChatMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.noMoreSummons")));

			return true;
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 400.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 120.0F;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster)
	{
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier)
	{
	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.ENDER, Affinity.LIFE);
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
