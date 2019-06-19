package am2.spell.components;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.blocks.BlocksCommonProxy;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SpellUtils;
import am2.utility.DummyEntityPlayer;
import am2.utility.KeystoneUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class Dig implements ISpellComponent
{
	private static final float hardnessManaFactor = 1.28F;
	private ArrayList<Block> disallowedBlocks = new ArrayList();

	public Dig()
	{
		this.disallowedBlocks = new ArrayList();
		this.disallowedBlocks.add(Blocks.bedrock);
		this.disallowedBlocks.add(Blocks.command_block);
		this.disallowedBlocks.add(BlocksCommonProxy.everstone);

		for (String i : AMCore.config.getDigBlacklist())
			if (i != null && i != "")
				this.disallowedBlocks.add(Block.getBlockFromName(i.replace("tile.", "")));

	}

	public void addDisallowedBlock(String block)
	{
		this.disallowedBlocks.add(Block.getBlockFromName(block));
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		Block block = world.getBlock(x, y, z);
		if (block == Blocks.air)
			return false;
		else
		{
			TileEntity te = world.getTileEntity(x, y, z);
			if (te != null)
			{
				if (!AMCore.config.getDigBreaksTileEntities())
					return false;

				if (te instanceof IKeystoneLockable && !KeystoneUtilities.instance.canPlayerAccess((IKeystoneLockable) te, DummyEntityPlayer.fromEntityLiving(caster), KeystoneAccessType.BREAK))
					return false;
			}

			if (this.disallowedBlocks.contains(block))
				return false;
			else if (block.getBlockHardness(world, x, y, z) == -1.0F)
				return false;
			else
			{
				int meta = world.getBlockMetadata(x, y, z);
				int harvestLevel = block.getHarvestLevel(meta);
				int miningLevel = 2 + SpellUtils.instance.countModifiers(SpellModifiers.MINING_POWER, stack, 0);
				if (harvestLevel > miningLevel)
					return false;
				else
				{
					EntityPlayer casterPlayer = DummyEntityPlayer.fromEntityLiving(caster);

					    
					if (EventUtils.cantBreak(casterPlayer, x, y, z))
						return false;
					    

					if (ForgeEventFactory.doPlayerHarvestCheck(casterPlayer, block, true))
					{
						float xMana = block.getBlockHardness(world, x, y, z) * 1.28F;
						ArsMagicaApi var10000 = ArsMagicaApi.instance;
						float xBurnout = ArsMagicaApi.getBurnoutFromMana(xMana);
						if (!world.isRemote)
						{
							BreakEvent event = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP) casterPlayer).theItemInWorldManager.getGameType(), (EntityPlayerMP) casterPlayer, x, y, z);
							if (event.isCanceled())
								return false;

							block.onBlockHarvested(world, x, y, z, meta, casterPlayer);
							boolean flag = block.removedByPlayer(world, casterPlayer, x, y, z, true);
							if (flag)
							{
								block.onBlockDestroyedByPlayer(world, x, y, z, meta);
								block.harvestBlock(world, casterPlayer, x, y, z, meta);
							}
						}

						ExtendedProperties.For(caster).deductMana(xMana);
						ExtendedProperties.For(caster).addBurnout(xBurnout);
						return true;
					}
					else
						return false;
				}
			}
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
		return 10.0F;
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
	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.EARTH);
	}

	@Override
	public int getID()
	{
		return 8;
	}

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[3];
		ItemRune var10007 = ItemsCommonProxy.rune;
		var10000[0] = new ItemStack(ItemsCommonProxy.rune, 1, 11);
		var10000[1] = Items.iron_shovel;
		var10000[2] = Items.iron_pickaxe;
		return var10000;
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.001F;
	}
}
