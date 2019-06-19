package am2.spell.components;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Random;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.utility.DummyEntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class Plant implements ISpellComponent
{
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int аace, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		Block soil = world.getBlock(x, y, z);
		IInventory inventory = DummyEntityPlayer.fromEntityLiving(caster).inventory;
		HashMap<Integer, ItemStack> seeds = this.GetAllSeedsInInventory(inventory);
		int currentSlot = 0;
		if (soil != Blocks.air && seeds.size() > 0)
		{
			currentSlot = seeds.keySet().iterator().next().intValue();
			ItemStack seedStack = seeds.get(Integer.valueOf(currentSlot));
			IPlantable seed = (IPlantable) seedStack.getItem();
			if (soil != null && soil.canSustainPlant(world, x, y, z, ForgeDirection.UP, seed) && world.isAirBlock(x, y + 1, z))
			{
				    
				EntityPlayer player = caster instanceof EntityPlayer ? (EntityPlayer) caster : ModUtils.getModFake(world);
				if (EventUtils.cantBreak(player, x, y, z))
					return false;
				    

				world.setBlock(x, y + 1, z, seed.getPlant(world, x, y, z));
				--seedStack.stackSize;
				if (seedStack.stackSize <= 0)
				{
					inventory.setInventorySlotContents(currentSlot, (ItemStack) null);
					seeds.remove(Integer.valueOf(currentSlot));
					if (seeds.size() == 0)
						return true;
				}
			}

			return true;
		}
		else
			return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 80.0F;
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
		for (int i = 0; i < 15; ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "plant", x, y + 1.0D, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 1.0D, 1.0D);
				particle.addVelocity(rand.nextDouble() * 0.2D - 0.1D, 0.20000000298023224D, rand.nextDouble() * 0.2D - 0.1D);
				particle.setDontRequireControllers();
				particle.setAffectedByGravity();
				particle.setMaxAge(20);
				particle.setParticleScale(0.1F);
				if (colorModifier > -1)
					particle.setRGBColorF((colorModifier >> 16 & 255) / 255.0F, (colorModifier >> 8 & 255) / 255.0F, (colorModifier & 255) / 255.0F);
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.NATURE);
	}

	private HashMap<Integer, ItemStack> GetAllSeedsInInventory(IInventory inventory)
	{
		HashMap<Integer, ItemStack> seeds = new HashMap();

		for (int i = 0; i < inventory.getSizeInventory(); ++i)
		{
			ItemStack slotStack = inventory.getStackInSlot(i);
			if (slotStack != null)
			{
				Item item = slotStack.getItem();
				if (item instanceof IPlantable)
					seeds.put(Integer.valueOf(i), slotStack);
			}
		}

		return seeds;
	}

	@Override
	public int getID()
	{
		return 41;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[4];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 6);
		var10000[1] = Items.wheat_seeds;
		var10000[2] = new ItemStack(Blocks.sapling, 1, 32767);
		var10000[3] = Items.wheat_seeds;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.01F;
	}
}
