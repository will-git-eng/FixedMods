package thaumcraft.common.items.tools;

import ru.will.git.eventhelper.util.EventUtils;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.IThaumcraftItems;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.utils.EntityUtils;

import java.util.List;

public class ItemElementalSword extends ItemSword implements IThaumcraftItems
{
	public ItemElementalSword(ToolMaterial enumtoolmaterial)
	{
		super(enumtoolmaterial);
		this.setCreativeTab(ConfigItems.TABTC);
		this.setRegistryName("elemental_sword");
		this.setUnlocalizedName("elemental_sword");
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
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		if (tab != ConfigItems.TABTC && tab != CreativeTabs.SEARCH)
			super.getSubItems(tab, items);
		else
		{
			ItemStack w1 = new ItemStack(this);
			EnumInfusionEnchantment.addInfusionEnchantment(w1, EnumInfusionEnchantment.ARCING, 2);
			items.add(w1);
		}

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
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.NONE;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
	{
		playerIn.setActiveHand(hand);
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count)
	{
		super.onUsingTick(stack, player, count);
		int ticks = this.getMaxItemUseDuration(stack) - count;
		if (player.motionY < 0.0D)
		{
			player.motionY /= 1.2000000476837158D;
			player.fallDistance /= 1.2F;
		}

		player.motionY += 0.07999999821186066D;
		if (player.motionY > 0.5D)
			player.motionY = 0.20000000298023224D;

		if (player instanceof EntityPlayerMP)
			EntityUtils.resetFloatCounter((EntityPlayerMP) player);

		List<Entity> targets = player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().grow(2.5D, 2.5D, 2.5D));
		if (targets.size() > 0)
			for (Entity entity : targets)
			{
				if (!(entity instanceof EntityPlayer) && entity instanceof EntityLivingBase && !entity.isDead && (player.getRidingEntity() == null || player.getRidingEntity() != entity))
				{
					
					if (player instanceof EntityPlayer && EventUtils.cantAttack((EntityPlayer) player, entity))
						continue;
					

					Vec3d p = new Vec3d(player.posX, player.posY, player.posZ);
					Vec3d t = new Vec3d(entity.posX, entity.posY, entity.posZ);
					double distance = p.distanceTo(t) + 0.1D;
					Vec3d r = new Vec3d(t.x - p.x, t.y - p.y, t.z - p.z);
					entity.motionX += r.x / 2.5D / distance;
					entity.motionY += r.y / 2.5D / distance;
					entity.motionZ += r.z / 2.5D / distance;
				}
			}

		if (player.world.isRemote)
		{
			int miny = (int) (player.getEntityBoundingBox().minY - 2.0D);
			if (player.onGround)
				miny = MathHelper.floor(player.getEntityBoundingBox().minY);

			for (int a = 0; a < 5; ++a)
			{
				FXDispatcher.INSTANCE.smokeSpiral(player.posX, player.getEntityBoundingBox().minY + (double) (player.height / 2.0F), player.posZ, 1.5F, player.world.rand.nextInt(360), miny, 14540253);
			}

			if (player.onGround)
			{
				float r1 = player.world.rand.nextFloat() * 360.0F;
				float mx = -MathHelper.sin(r1 / 180.0F * 3.1415927F) / 5.0F;
				float mz = MathHelper.cos(r1 / 180.0F * 3.1415927F) / 5.0F;
				player.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, player.posX, player.getEntityBoundingBox().minY + 0.10000000149011612D, player.posZ, (double) mx, 0.0D, (double) mz);
			}
		}
		else if (ticks == 0 || ticks % 20 == 0)
			player.playSound(SoundsTC.wind, 0.5F, 0.9F + player.world.rand.nextFloat() * 0.2F);

		if (ticks % 20 == 0)
			stack.damageItem(1, player);

	}
}
