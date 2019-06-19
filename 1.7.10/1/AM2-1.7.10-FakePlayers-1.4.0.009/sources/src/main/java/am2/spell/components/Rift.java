package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.RitualShapeHelper;
import am2.api.ArsMagicaApi;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.blocks.BlocksCommonProxy;
import am2.entities.EntityRiftStorage;
import am2.items.ItemEssence;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.spell.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Rift implements ISpellComponent, IRitualInteraction
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		if (world.getBlock(x, y, z) == Blocks.mob_spawner)
		{
			ItemStack[] reagents = RitualShapeHelper.instance.checkForRitual(this, world, x, y, z);
			if (reagents != null)
			{
				if (!world.isRemote)
				{
					    
					EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
					if (EventUtils.cantBreak(player, x, y, z))
						return false;
					    

					world.setBlockToAir(x, y, z);
					RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
					RitualShapeHelper.instance.consumeRitualShape(this, world, x, y, z);
					EntityItem item = new EntityItem(world);
					item.setPosition(x + 0.5D, y + 0.5D, z + 0.5D);
					item.setEntityItemStack(new ItemStack(BlocksCommonProxy.inertSpawner));
					world.spawnEntityInWorld(item);
				}

				return true;
			}
		}

		if (world.isRemote)
			return true;
		else
		{
			    
			EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
			if (EventUtils.cantBreak(player, x, y, z))
				return false;
			    

			EntityRiftStorage storage = new EntityRiftStorage(world);
			int storageLevel = Math.min(1 + SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0), 3);
			storage.setStorageLevel(storageLevel);
			if (face == 1)
				storage.setPosition(x + 0.5D, y + 1.5D, z + 0.5D);
			else if (face == 2)
				storage.setPosition(x + 0.5D, y + 0.5D, z - 1.5D);
			else if (face == 3)
				storage.setPosition(x + 0.5D, y + 0.5D, z + 1.5D);
			else if (face == 4)
				storage.setPosition(x - 1.5D, y + 0.5D, z + 0.5D);
			else if (face == 5)
				storage.setPosition(x + 1.5D, y + 0.5D, z + 0.5D);
			else
				storage.setPosition(x + 0.5D, y - 1.5D, z + 0.5D);

			world.spawnEntityInWorld(storage);
			return true;
		}
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 90.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return ArsMagicaApi.getBurnoutFromMana(this.manaCost(caster));
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
		return EnumSet.of(Affinity.NONE);
	}

	@Override
	public int getID()
	{
		return 48;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[4];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 15);
		var10007 = ItemsCommonProxy.rune;
		var10000[1] = new ItemStack(ItemsCommonProxy.rune, 1, 13);
		var10000[2] = Blocks.chest;
		var10000[3] = Items.ender_eye;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.0F;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape()
	{
		return RitualShapeHelper.instance.corruption;
	}

	@Override
	public ItemStack[] getReagents()
	{
		ItemStack[] var10000 = new ItemStack[] { new ItemStack(ItemsCommonProxy.mobFocus), null };
		ItemEssence var10007 = ItemsCommonProxy.essence;
		var10000[1] = new ItemStack(ItemsCommonProxy.essence, 1, 9);
		return var10000;
	}

	@Override
	public int getReagentSearchRadius()
	{
		return 3;
	}
}
