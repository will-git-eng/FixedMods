package thaumcraft.common.items.equipment;

import ru.will.git.thaumcraft.EventConfig;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.EnumHelper;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IWarpingGear;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.BlockUtils;

import java.util.Set;

public class ItemPrimalCrusher extends ItemTool implements IRepairable, IWarpingGear
{
	public static ToolMaterial material = EnumHelper.addToolMaterial("PRIMALVOID", 5, 500, 8.0F, 4.0F, 20);
	private static final Set isEffective = Sets.newHashSet(Blocks.cobblestone, Blocks.double_stone_slab, Blocks.stone_slab, Blocks.stone, Blocks.sandstone, Blocks.mossy_cobblestone, Blocks.iron_ore, Blocks.iron_block, Blocks.coal_ore, Blocks.gold_block, Blocks.gold_ore, Blocks.diamond_ore, Blocks.diamond_block, Blocks.ice, Blocks.netherrack, Blocks.lapis_ore, Blocks.lapis_block, Blocks.redstone_ore, Blocks.lit_redstone_ore, Blocks.rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.activator_rail, Blocks.grass, Blocks.dirt, Blocks.sand, Blocks.gravel, Blocks.snow_layer, Blocks.snow, Blocks.clay, Blocks.farmland, Blocks.soul_sand, Blocks.mycelium, ConfigBlocks.blockTaint, ConfigBlocks.blockTaintFibres, Blocks.obsidian);
	public IIcon icon;
	int side = 0;

	public ItemPrimalCrusher(ToolMaterial enumtoolmaterial)
	{
		super(3.5F, enumtoolmaterial, isEffective);
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@Override
	public boolean func_150897_b(Block p_150897_1_)
	{
		return p_150897_1_.getMaterial() != Material.wood && p_150897_1_.getMaterial() != Material.leaves && p_150897_1_.getMaterial() != Material.plants;
	}

	@Override
	public float func_150893_a(ItemStack p_150893_1_, Block p_150893_2_)
	{
		return p_150893_2_.getMaterial() != Material.iron && p_150893_2_.getMaterial() != Material.anvil && p_150893_2_.getMaterial() != Material.rock ? super.func_150893_a(p_150893_1_, p_150893_2_) : this.efficiencyOnProperMaterial;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		return ImmutableSet.of("shovel", "pickaxe");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.icon = ir.registerIcon("thaumcraft:primal_crusher");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1)
	{
		return this.icon;
	}

	@Override
	public EnumRarity getRarity(ItemStack itemstack)
	{
		return EnumRarity.epic;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 15)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
	}

	private boolean isEffectiveAgainst(Block block)
	{
		for (Object b : isEffective)
		{
			if (b == block)
				return true;
		}

		return false;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player)
	{
		MovingObjectPosition movingobjectposition = BlockUtils.getTargetBlock(player.worldObj, player, true);
		if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectType.BLOCK)
			this.side = movingobjectposition.sideHit;

		return super.onBlockStartBreak(itemstack, X, Y, Z, player);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, Block bi, int x, int y, int z, EntityLivingBase ent)
    
		if (EventConfig.primalCrusherPlayerOnly && (ent == null || ent.getClass() != EntityPlayerMP.class))
    

		if (ent.isSneaking())
			return super.onBlockDestroyed(stack, world, bi, x, y, z, ent);
		else
		{
			if (!ent.worldObj.isRemote)
			{
				int md = world.getBlockMetadata(x, y, z);
				if (ForgeHooks.isToolEffective(stack, bi, md) || this.isEffectiveAgainst(bi))
					for (int aa = -1; aa <= 1; ++aa)
					{
						for (int bb = -1; bb <= 1; ++bb)
						{
							int xx = 0;
							int yy = 0;
							int zz = 0;
							if (this.side <= 1)
							{
								xx = aa;
								zz = bb;
							}
							else if (this.side <= 3)
							{
								xx = aa;
								yy = bb;
							}
							else
							{
								zz = aa;
								yy = bb;
							}

							if (!(ent instanceof EntityPlayer) || world.canMineBlock((EntityPlayer) ent, x + xx, y + yy, z + zz))
							{
								Block bl = world.getBlock(x + xx, y + yy, z + zz);
								md = world.getBlockMetadata(x + xx, y + yy, z + zz);
								if (bl.getBlockHardness(world, x + xx, y + yy, z + zz) >= 0.0F && (ForgeHooks.isToolEffective(stack, bl, md) || this.isEffectiveAgainst(bl)))
								{
									stack.damageItem(1, ent);
									BlockUtils.harvestBlock(world, (EntityPlayer) ent, x + xx, y + yy, z + zz, true, 2);
								}
							}
						}
					}
			}

			return true;
		}
	}

	@Override
	public int getItemEnchantability()
	{
		return 20;
	}

	@Override
	public int getWarp(ItemStack itemstack, EntityPlayer player)
	{
		return 2;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int p_77663_4_, boolean p_77663_5_)
	{
		super.onUpdate(stack, world, entity, p_77663_4_, p_77663_5_);
		if (stack.isItemDamaged() && entity != null && entity.ticksExisted % 20 == 0 && entity instanceof EntityLivingBase)
			stack.damageItem(-1, (EntityLivingBase) entity);

	}
}
