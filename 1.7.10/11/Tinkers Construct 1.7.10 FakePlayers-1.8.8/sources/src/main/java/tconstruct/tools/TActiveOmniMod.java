package tconstruct.tools;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.reflectionmedic.util.FastUtils;
import ru.will.git.tconstruct.ModUtils;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import tconstruct.library.ActiveToolMod;
import tconstruct.library.tools.AbilityHelper;
import tconstruct.library.tools.HarvestTool;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.weaponry.IAmmo;
import tconstruct.util.config.PHConstruct;
import tconstruct.world.TinkerWorld;
import tconstruct.world.entity.BlueSlime;

import java.util.Random;

public class TActiveOmniMod extends ActiveToolMod
{
	Random random = new Random();

    
	@Override
	public void updateTool(ToolCore tool, ItemStack stack, World world, Entity entity)
	{
		if (!world.isRemote && entity instanceof EntityLivingBase && !((EntityLivingBase) entity).isSwingInProgress && stack.getTagCompound() != null)
		{
			if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isUsingItem())
				return;
			NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
			if (tags.hasKey("Moss"))
			{
				int chance = tags.getInteger("Moss");
    
    
				{
					IAmmo ammothing = (IAmmo) tool;
    
						ammothing.addAmmo(1, stack);
    
				else if (this.random.nextInt(check) < chance)
				{
					AbilityHelper.healTool(stack, 1, (EntityLivingBase) entity, true);
				}
			}
		}
	}

    
	@Override
	public boolean beforeBlockBreak(ToolCore tool, ItemStack stack, int x, int y, int z, EntityLivingBase entity)
	{
		NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
		this.baconator(tool, stack, entity, tags);

		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode)
			return false;
		TinkerTools.modLapis.midStreamModify(stack, tool);
		return this.autoSmelt(tool, tags, stack, x, y, z, entity);

	}

	@Override
	public void afterBlockBreak(ToolCore tool, ItemStack stack, Block block, int x, int y, int z, EntityLivingBase entity)
	{
		NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
		this.slimify(tool, tags, block, x, y, z, entity.worldObj);
	}

	private boolean autoSmelt(ToolCore tool, NBTTagCompound tags, ItemStack stack, int x, int y, int z, EntityLivingBase entity)
	{
		World world = entity.worldObj;
		Block block = world.getBlock(x, y, z);
		if (block == null)
			return false;

		int blockMeta = world.getBlockMetadata(x, y, z);

		if (block.getMaterial().isToolNotRequired())
    
			if (tool instanceof HarvestTool)
			{
				if (!((HarvestTool) tool).isEffective(block, blockMeta))
					return false;
			}
			else
				return false;
		}
		else if (!ForgeHooks.canToolHarvestBlock(block, blockMeta, stack))
			return false;

		if (tags.getBoolean("Lava") && block.quantityDropped(blockMeta, 0, this.random) > 0)
		{
			int itemMeta = block.damageDropped(blockMeta);
			int amount = block.quantityDropped(this.random);
    
			if (item == null)
				return false;

			ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(item, amount, itemMeta));
			if (result != null)
    
				if (EventUtils.cantBreak(FastUtils.getLivingPlayer(entity, ModUtils.profile), x, y, z))
    

				world.setBlockToAir(x, y, z);
				if (entity instanceof EntityPlayer && !((EntityPlayer) entity).capabilities.isCreativeMode)
					tool.onBlockDestroyed(stack, world, block, x, y, z, entity);
				if (!world.isRemote)
				{
					ItemStack spawnme = new ItemStack(result.getItem(), amount * result.stackSize, result.getItemDamage());
					if (result.hasTagCompound())
						spawnme.setTagCompound(result.getTagCompound());
					if (!(result.getItem() instanceof ItemBlock) && PHConstruct.lavaFortuneInteraction)
					{
						int loot = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack);
						if (loot > 0)
						{
							spawnme.stackSize *= this.random.nextInt(loot + 1) + 1;
						}
					}
					EntityItem entityitem = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, spawnme);

					entityitem.delayBeforeCanPickup = 10;
					world.spawnEntityInWorld(entityitem);
					world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (blockMeta << 12));

					int i = spawnme.stackSize;
					float f = FurnaceRecipes.smelting().func_151398_b(spawnme);
					int j;

					if (f == 0.0F)
					{
						i = 0;
					}
					else if (f < 1.0F)
					{
						j = MathHelper.floor_float((float) i * f);

						if (j < MathHelper.ceiling_float_int((float) i * f) && (float) Math.random() < (float) i * f - (float) j)
						{
							++j;
						}

						i = j;
					}

					while (i > 0)
					{
						j = EntityXPOrb.getXPSplit(i);
						i -= j;
						entity.worldObj.spawnEntityInWorld(new EntityXPOrb(world, x, y + 0.5, z, j));
					}
				}
				for (int i = 0; i < 5; i++)
				{
					float f = (float) x + this.random.nextFloat();
					float f1 = (float) y + this.random.nextFloat();
					float f2 = (float) z + this.random.nextFloat();
					float f3 = 0.52F;
					float f4 = this.random.nextFloat() * 0.6F - 0.3F;
					world.spawnParticle("smoke", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
					world.spawnParticle("flame", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);

					world.spawnParticle("smoke", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
					world.spawnParticle("flame", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);

					world.spawnParticle("smoke", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
					world.spawnParticle("flame", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);

					world.spawnParticle("smoke", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
					world.spawnParticle("flame", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
				}
				return true;
			}
		}
		return false;
	}

    

	@Override
	public int baseAttackDamage(int earlyModDamage, int damage, ToolCore tool, NBTTagCompound tags, NBTTagCompound toolTags, ItemStack stack, EntityLivingBase player, Entity entity)
	{
		TinkerTools.modLapis.midStreamModify(stack, tool);
		this.baconator(tool, stack, player, tags);
		return 0;
	}

	private void baconator(ToolCore tool, ItemStack stack, EntityLivingBase entity, NBTTagCompound tags)
	{
		final int pigiron = TinkerTools.MaterialID.PigIron;
		int bacon = 0;
		bacon += tags.getInteger("Head") == pigiron ? 1 : 0;
		bacon += tags.getInteger("Handle") == pigiron ? 1 : 0;
		bacon += tags.getInteger("Accessory") == pigiron ? 1 : 0;
		bacon += tags.getInteger("Extra") == pigiron ? 1 : 0;
		int chance = tool.getPartAmount() * 100;
		if (this.random.nextInt(chance) < bacon)
		{
			if (entity instanceof EntityPlayer)
				AbilityHelper.spawnItemAtPlayer((EntityPlayer) entity, new ItemStack(TinkerWorld.strangeFood, 1, 2));
			else
				AbilityHelper.spawnItemAtEntity(entity, new ItemStack(TinkerWorld.strangeFood, 1, 2), 0);
		}
	}

	private void slimify(ToolCore tool, NBTTagCompound tags, Block block, int x, int y, int z, World world)
	{
		if (world.isRemote)
			return;

		int chance = tool.getPartAmount() * 100;
		int count = 0;
    
		if (tags.getInteger("Head") == slimeMat)
			count++;
		if (tags.getInteger("Handle") == slimeMat)
			count++;
		if (tags.getInteger("Accessory") == slimeMat)
			count++;
		if (tags.getInteger("Extra") == slimeMat)
			count++;

		if (this.random.nextInt(chance) < count)
		{
			EntitySlime entity = new EntitySlime(world);
			entity.setPosition(x + 0.5, y, z + 0.5);
    
			world.spawnEntityInWorld(entity);
			entity.playLivingSound();
    
		slimeMat = TinkerTools.MaterialID.BlueSlime;
		count = 0;
		if (tags.getInteger("Head") == slimeMat)
			count++;
		if (tags.getInteger("Handle") == slimeMat)
			count++;
		if (tags.getInteger("Accessory") == slimeMat)
			count++;
		if (tags.getInteger("Extra") == slimeMat)
			count++;

		if (this.random.nextInt(chance) < count)
		{
			BlueSlime entity = new BlueSlime(world);
			entity.setPosition(x + 0.5, y, z + 0.5);
    
			world.spawnEntityInWorld(entity);
			entity.playLivingSound();
		}
	}

	@Override
	public int attackDamage(int modDamage, int currentDamage, ToolCore tool, NBTTagCompound tags, NBTTagCompound toolTags, ItemStack stack, EntityLivingBase player, Entity entity)
	{
		int bonus = modDamage;
		if (entity instanceof EntityLivingBase)
		{
			EnumCreatureAttribute attribute = ((EntityLivingBase) entity).getCreatureAttribute();
			if (attribute == EnumCreatureAttribute.UNDEAD)
			{
				if (tool == TinkerTools.hammer)
				{
					int level = 2;
					bonus += this.random.nextInt(level * 2 + 1) + level * 2;
				}
				if (toolTags.hasKey("ModSmite"))
				{
					int[] array = toolTags.getIntArray("ModSmite");
					int base = array[0] / 18;
					bonus += 1 + base + this.random.nextInt(base + 1);
				}
			}
			if (attribute == EnumCreatureAttribute.ARTHROPOD)
			{
				if (toolTags.hasKey("ModAntiSpider"))
				{
					int[] array = toolTags.getIntArray("ModAntiSpider");
					int base = array[0] / 2;
					bonus += 1 + base + this.random.nextInt(base + 1);
				}
			}
		}
		return bonus;
	}

	@Override
	public float knockback(float modKnockback, float currentKnockback, ToolCore tool, NBTTagCompound tags, NBTTagCompound toolTags, ItemStack stack, EntityLivingBase player, Entity entity)
	{
		float bonus = modKnockback;
		if (toolTags.hasKey("Knockback"))
		{
			float level = toolTags.getFloat("Knockback");
			bonus += level;
		}
		return bonus;
	}

	@Override
	public boolean doesCriticalHit(ToolCore tool, NBTTagCompound tags, NBTTagCompound toolTags, ItemStack stack, EntityLivingBase player, Entity entity)
	{
		return tool == TinkerTools.cutlass && this.random.nextInt(10) == 0;
	}
}
