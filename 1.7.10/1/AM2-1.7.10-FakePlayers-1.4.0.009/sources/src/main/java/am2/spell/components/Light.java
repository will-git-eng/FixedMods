package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.RitualShapeHelper;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.power.IPowerNode;
import am2.api.spell.component.interfaces.IRitualInteraction;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.blocks.BlocksCommonProxy;
import am2.buffs.BuffEffectIllumination;
import am2.items.ItemOre;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.power.PowerNodeRegistry;
import am2.spell.SpellUtils;
import am2.spell.modifiers.Colour;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Light implements ISpellComponent, IRitualInteraction
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		if (world.getBlock(x, y, z) == BlocksCommonProxy.obelisk)
		{
			ItemStack[] reagents = RitualShapeHelper.instance.checkForRitual(this, world, x, y, z);
			if (reagents != null)
			{
				    
				EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
				if (EventUtils.cantBreak(player, x, y, z))
					return false;
				    

				if (!world.isRemote)
				{
					RitualShapeHelper.instance.consumeRitualReagents(this, world, x, y, z);
					RitualShapeHelper.instance.consumeRitualShape(this, world, x, y, z);
					world.setBlock(x, y, z, BlocksCommonProxy.celestialPrism);
					PowerNodeRegistry.For(world).registerPowerNode((IPowerNode) world.getTileEntity(x, y, z));
				}

				return true;
			}
		}

		if (world.getBlock(x, y, z) == Blocks.air)
			face = -1;

		if (face != -1)
			switch (face)
			{
				case 0:
					--y;
					break;
				case 1:
					++y;
					break;
				case 2:
					--z;
					break;
				case 3:
					++z;
					break;
				case 4:
					--x;
					break;
				case 5:
					++x;
			}

		if (world.getBlock(x, y, z) != Blocks.air)
			return false;
		else
		{
			    
			EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
			if (EventUtils.cantBreak(player, x, y, z))
				return false;
			    

			if (!world.isRemote)
				world.setBlock(x, y, z, BlocksCommonProxy.blockMageTorch, this.getColorMeta(stack), 2);

			return true;
		}
	}

	private int getColorMeta(ItemStack spell)
	{
		int meta = 15;
		int color = 16777215;
		if (SpellUtils.instance.modifierIsPresent(SpellModifiers.COLOR, spell, 0))
		{
			ISpellModifier[] mods = SpellUtils.instance.getModifiersForStage(spell, 0);
			int ordinalCount = 0;

			for (ISpellModifier mod : mods)
				if (mod instanceof Colour)
				{
					byte[] data = SpellUtils.instance.getModifierMetadataFromStack(spell, mod, 0, ordinalCount++);
					color = (int) mod.getModifier(SpellModifiers.COLOR, (EntityLivingBase) null, (Entity) null, (World) null, data);
				}
		}

		for (int i = 0; i < 16; ++i)
		{
			ItemDye var10000 = (ItemDye) Items.dye;
			if (ItemDye.field_150922_c[i] == color)
			{
				meta = i;
				break;
			}
		}

		return meta;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (target instanceof EntityLivingBase)
		{
			    
			if (target.isDead || EventUtils.cantDamage(caster, target))
				return false;
			    

			int duration = SpellUtils.instance.getModifiedInt_Mul(600, stack, caster, target, world, 0, SpellModifiers.DURATION);
			duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);
			if (!world.isRemote)
				((EntityLivingBase) target).addPotionEffect(new BuffEffectIllumination(duration, SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0)));

			return true;
		}
		else
			return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 50.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 10.0F;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster)
	{
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier)
	{
		for (int i = 0; i < 5; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 0.5D, 1.0D);
				particle.addVelocity(rand.nextDouble() * 0.2D - 0.1D, rand.nextDouble() * 0.2D, rand.nextDouble() * 0.2D - 0.1D);
				particle.setAffectedByGravity();
				particle.setDontRequireControllers();
				particle.setMaxAge(5);
				particle.setParticleScale(0.1F);
				particle.setRGBColorF(0.6F, 0.2F, 0.8F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.NONE);
	}

	@Override
	public int getID()
	{
		return 33;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[4];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 15);
		var10000[1] = BlocksCommonProxy.cerublossom;
		var10000[2] = Blocks.torch;
		var10000[3] = BlocksCommonProxy.vinteumTorch;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape()
	{
		return RitualShapeHelper.instance.purification;
	}

	@Override
	public ItemStack[] getReagents()
	{
		ItemStack[] var10000 = new ItemStack[2];
		ItemOre var10007 = ItemsCommonProxy.itemOre;
		var10000[0] = new ItemStack(ItemsCommonProxy.itemOre, 1, 7);
		var10000[1] = new ItemStack(ItemsCommonProxy.manaFocus);
		return var10000;
	}

	@Override
	public int getReagentSearchRadius()
	{
		return 3;
	}
}
