package thaumcraft.common.entities.construct;

import ru.will.git.eventhelper.util.EventUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.items.ItemTCBase;

import java.util.List;

public class ItemTurretPlacer extends ItemTCBase
{
	public ItemTurretPlacer()
	{
		super("turret", "basic", "advanced", "bore");
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if (side == EnumFacing.DOWN)
			return EnumActionResult.PASS;

		boolean replaceable = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
		BlockPos turretPos = replaceable ? pos : pos.offset(side);
		ItemStack heldItem = player.getHeldItem(hand);
		if (!player.canPlayerEdit(turretPos, side, heldItem))
			return EnumActionResult.PASS;

		BlockPos turretPosUp = turretPos.up();
		boolean flag1 = !world.isAirBlock(turretPos) && !world.getBlockState(turretPos).getBlock().isReplaceable(world, turretPos);
		flag1 = flag1 | (!world.isAirBlock(turretPosUp) && !world.getBlockState(turretPosUp).getBlock().isReplaceable(world, turretPosUp));
		if (flag1)
			return EnumActionResult.PASS;

		double d0 = (double) turretPos.getX();
		double d1 = (double) turretPos.getY();
		double d2 = (double) turretPos.getZ();
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));
		if (!list.isEmpty())
			return EnumActionResult.PASS;

		if (!world.isRemote)
		{
			
			if (EventUtils.cantBreak(player, turretPos))
				return EnumActionResult.PASS;
			if (EventUtils.cantBreak(player, turretPosUp))
				return EnumActionResult.PASS;
			

			world.setBlockToAir(turretPos);
			world.setBlockToAir(turretPosUp);

			EntityOwnedConstruct turret = null;
			switch (heldItem.getItemDamage())
			{
				case 0:
					turret = new EntityTurretCrossbow(world, turretPos);
					break;
				case 1:
					turret = new EntityTurretCrossbowAdvanced(world, turretPos);
					break;
				case 2:
					turret = new EntityArcaneBore(world, turretPos, player.getHorizontalFacing());
			}

			if (turret != null)
			{
				world.spawnEntity(turret);
				turret.setOwned(true);
				turret.setValidSpawn();
				turret.setOwnerId(player.getUniqueID());

				
				turret.fake.setProfile(player);
				

				world.playSound(null, turret.posX, turret.posY, turret.posZ, SoundEvents.ENTITY_ARMORSTAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);
			}

			heldItem.shrink(1);
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
