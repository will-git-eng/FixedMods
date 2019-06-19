package thaumcraft.common.items.tools;

import ru.will.git.eventhelper.util.EventUtils;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.IThaumcraftItems;
import thaumcraft.common.lib.utils.Utils;

public class ItemElementalHoe extends ItemHoe implements IThaumcraftItems
{
	public ItemElementalHoe(ToolMaterial enumtoolmaterial)
	{
		super(enumtoolmaterial);
		this.setCreativeTab(ConfigItems.TABTC);
		this.setRegistryName("elemental_hoe");
		this.setUnlocalizedName("elemental_hoe");
		ConfigItems.ITEM_VARIANT_HOLDERS.add(this);
	}

	@Override
	public Item getItem()
	{
		return this;
	}

	@Override
	public String[] getVariantNames()
	{
		return new String[] { "normal" };
	}

	@Override
	public int[] getVariantMeta()
	{
		return new int[] { 0 };
	}

	@Override
	public ItemMeshDefinition getCustomMesh()
	{
		return null;
	}

	@Override
	public ModelResourceLocation getCustomModelResourceLocation(String variant)
	{
		return new ModelResourceLocation("thaumcraft:" + variant);
	}

	@Override
	public int getItemEnchantability()
	{
		return 5;
	}

	@Override
	public EnumRarity getRarity(ItemStack itemstack)
	{
		return EnumRarity.RARE;
	}

	@Override
	public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
	{
		return stack2.isItemEqual(new ItemStack(ItemsTC.ingots, 1, 0)) || super.getIsRepairable(stack1, stack2);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (player.isSneaking())
			return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);

		boolean did = false;

		for (int xOffset = -1; xOffset <= 1; ++xOffset)
		{
			for (int zOffset = -1; zOffset <= 1; ++zOffset)
			{
				BlockPos targetPos = pos.add(xOffset, 0, zOffset);

				
				if (EventUtils.cantInteract(player, hand, targetPos, facing))
					continue;
				

				if (super.onItemUse(player, world, targetPos, hand, facing, hitX, hitY, hitZ) == EnumActionResult.SUCCESS)
				{
					if (world.isRemote)
						FXDispatcher.INSTANCE.drawBamf(targetPos.getX() + 0.5D, targetPos.getY() + 1.01D, targetPos.getZ() + 0.5D, 0.3F, 0.12F, 0.1F, xOffset == 0 && zOffset == 0, false, EnumFacing.UP);

					if (!did)
						did = true;
				}
			}
		}

		if (!did)
		{
			did = Utils.useBonemealAtLoc(world, player, pos);
			if (did)
			{
				player.getHeldItem(hand).damageItem(3, player);
				if (!world.isRemote)
					world.playBroadcastSound(2005, pos, 0);
				else
					FXDispatcher.INSTANCE.drawBlockMistParticles(pos, 4259648);
			}
		}

		return EnumActionResult.SUCCESS;
	}
}
