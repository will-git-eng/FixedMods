package am2.spell.components;

import java.util.EnumSet;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;

import am2.RitualShapeHelper;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.items.ItemsCommonProxy;
import am2.spell.SpellUtils;
import am2.utility.InventoryUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class PlaceBlock implements ISpellComponent
{
	private static final String KEY_BLOCKID = "PlaceBlockID";
	private static final String KEY_META = "PlaceMeta";

	@Override
	public Object[] getRecipeItems()
	{
		return new Object[] { Items.stone_axe, Items.stone_pickaxe, Items.stone_shovel, Blocks.chest };
	}

	@Override
	public int getID()
	{
		return 75;
	}

	private MultiblockStructureDefinition.BlockDec getPlaceBlock(ItemStack stack)
	{
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("PlaceBlockID"))
		{
			MultiblockStructureDefinition var10002 = RitualShapeHelper.instance.hourglass;
			RitualShapeHelper.instance.hourglass.getClass();
			return var10002.new BlockDec(Block.getBlockById(stack.stackTagCompound.getInteger("PlaceBlockID")), stack.stackTagCompound.getInteger("PlaceMeta"));
		}
		else
			return null;
	}

	private void setPlaceBlock(ItemStack stack, Block block, int meta)
	{
		if (!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		stack.stackTagCompound.setInteger("PlaceBlockID", Block.getIdFromBlock(block));
		stack.stackTagCompound.setInteger("PlaceMeta", meta);
		if (!stack.stackTagCompound.hasKey("Lore"))
			stack.stackTagCompound.setTag("Lore", new NBTTagList());

		ItemStack blockStack = new ItemStack(block, 1, meta);
		NBTTagList tagList = stack.stackTagCompound.getTagList("Lore", 10);

		for (int i = 0; i < tagList.tagCount(); ++i)
		{
			String str = tagList.getStringTagAt(i);
			if (str.startsWith(String.format(StatCollector.translateToLocal("am2.tooltip.placeBlockSpell"), new Object[] { "" })))
				tagList.removeTag(i);
		}

		tagList.appendTag(new NBTTagString(String.format(StatCollector.translateToLocal("am2.tooltip.placeBlockSpell"), new Object[] { blockStack.getDisplayName() })));
		stack.stackTagCompound.setTag("Lore", tagList);
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		if (!(caster instanceof EntityPlayer))
			return false;
		else
		{
			EntityPlayer player = (EntityPlayer) caster;
			ItemStack spellStack = player.getCurrentEquippedItem();
			if (spellStack != null && spellStack.getItem() == ItemsCommonProxy.spell && SpellUtils.instance.componentIsPresent(spellStack, PlaceBlock.class, SpellUtils.instance.numStages(spellStack) - 1))
			{
				MultiblockStructureDefinition.BlockDec bd = this.getPlaceBlock(spellStack);
				if (bd != null && !caster.isSneaking())
				{
					if (world.isAirBlock(x, y, z) || !world.getBlock(x, y, z).getMaterial().isSolid())
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

					if (world.isAirBlock(x, y, z) || !world.getBlock(x, y, z).getMaterial().isSolid())
					{
						ItemStack searchStack = new ItemStack(bd.getBlock(), 1, bd.getMeta());
						if (!world.isRemote && (player.capabilities.isCreativeMode || InventoryUtilities.inventoryHasItem(player.inventory, searchStack, 1)))
						{
							    
							if (EventUtils.cantBreak(player, x, y, z))
								return false;
							    

							world.setBlock(x, y, z, bd.getBlock(), bd.getMeta(), 3);
							if (!player.capabilities.isCreativeMode)
								InventoryUtilities.deductFromInventory(player.inventory, searchStack, 1);
						}

						return true;
					}
				}
				else if (caster.isSneaking())
				{
					    
					if (EventUtils.cantBreak(player, x, y, z))
						return false;
					    

					if (!world.isRemote && !world.isAirBlock(x, y, z))
						this.setPlaceBlock(spellStack, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));

					return true;
				}

				return false;
			}
			else
				return false;
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
		return 5.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 1.0F;
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
		return EnumSet.of(Affinity.EARTH, Affinity.ENDER);
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.05F;
	}
}
