/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Jan 24, 2015, 4:46:39 PM (GMT)]
 */
package vazkii.botania.common.item.lens;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import vazkii.botania.api.internal.IManaBurst;

public class LensPiston extends Lens
{
	@Override
	public boolean collideBurst(IManaBurst burst, EntityThrowable entity, RayTraceResult pos, boolean isManaBlock, boolean dead, ItemStack stack)
	{
		BlockPos coords = burst.getBurstSourceBlockPos();
		if (!entity.world.isRemote)
		{
			BlockPos collidePos = pos.getBlockPos();
			if (collidePos != null && !coords.equals(collidePos) && !burst.isFake() && !isManaBlock)
			{
				BlockPos pos_ = collidePos.offset(pos.sideHit.getOpposite());

				if (entity.world.isAirBlock(pos_) || entity.world.getBlockState(pos_).getBlock().isReplaceable(entity.world, pos_))
				{
					IBlockState state = entity.world.getBlockState(collidePos);
					TileEntity tile = entity.world.getTileEntity(collidePos);

					if (state.getPushReaction() == EnumPushReaction.NORMAL && state.getBlock() != Blocks.OBSIDIAN && state.getBlockHardness(entity.world, pos_) >= 0 && tile == null)
					{
						    
						FakePlayerContainer fake = burst.getFakePlayerContainer();
						if (fake != null && (fake.cantBreak(collidePos) || fake.cantPlace(pos_, state)))
							return false;
						    

						entity.world.destroyBlock(collidePos, false);
						entity.world.setBlockState(pos_, state, 1 | 2);
					}
				}
			}
		}

		return dead;
	}

}
