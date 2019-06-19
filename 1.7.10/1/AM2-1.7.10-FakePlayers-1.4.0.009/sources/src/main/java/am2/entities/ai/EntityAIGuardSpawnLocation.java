package am2.entities.ai;

import am2.api.math.AMVector3;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIGuardSpawnLocation extends EntityAIBase
{
	private final EntityCreature theGuard;
	World theWorld;
	private final float moveSpeed;
	private final PathNavigate guardPathfinder;
	private int field_48310_h;
	float maxDist;
	float minDist;
	private boolean field_48311_i;
	private final AMVector3 spawnLocation;

	public EntityAIGuardSpawnLocation(EntityCreature par1EntityMob, float moveSpeed, float minDist, float maxDist, AMVector3 spawn)
	{
		this.theGuard = par1EntityMob;
		this.theWorld = par1EntityMob.worldObj;
		this.moveSpeed = moveSpeed;
		this.guardPathfinder = par1EntityMob.getNavigator();
		this.minDist = minDist;
		this.maxDist = maxDist;
		this.spawnLocation = spawn;
		this.setMutexBits(3);
	}

	public double getDistanceSqToSpawnXZ()
	{
		double d = this.theGuard.posX - this.spawnLocation.x;
		double d2 = this.theGuard.posZ - this.spawnLocation.z;
		return d * d + d2 * d2;
	}

	@Override
	public boolean shouldExecute()
	{
		return this.getDistanceSqToSpawnXZ() >= this.minDist * this.minDist;
	}

	@Override
	public boolean continueExecuting()
	{
		return !this.guardPathfinder.noPath() && this.getDistanceSqToSpawnXZ() > this.maxDist * this.maxDist;
	}

	@Override
	public void startExecuting()
	{
		this.field_48310_h = 0;
		this.field_48311_i = this.theGuard.getNavigator().getAvoidsWater();
		this.theGuard.getNavigator().setAvoidsWater(false);
	}

	@Override
	public void resetTask()
	{
		this.guardPathfinder.clearPathEntity();
		this.theGuard.getNavigator().setAvoidsWater(this.field_48311_i);
	}

	@Override
	public void updateTask()
	{
		this.theGuard.getLookHelper().setLookPosition(this.spawnLocation.x, this.spawnLocation.y, this.spawnLocation.z, 10.0F, this.theGuard.getVerticalFaceSpeed());
		if (--this.field_48310_h <= 0)
		{
			this.field_48310_h = 10;
			if (!this.guardPathfinder.tryMoveToXYZ(this.spawnLocation.x, this.spawnLocation.y, this.spawnLocation.z, this.moveSpeed))
				if (this.getDistanceSqToSpawnXZ() >= 144.0D)
				{
					int i = MathHelper.floor_double(this.spawnLocation.x) - 2;
					int j = MathHelper.floor_double(this.spawnLocation.z) - 2;
					int k = MathHelper.floor_double(this.spawnLocation.y);

					for (int l = 0; l <= 4; ++l)
						for (int i1 = 0; i1 <= 4; ++i1)
						{
							Block otherBlock = this.theWorld.getBlock(i + l, k + 1, j + i1);
							if (l < 1 || i1 < 1 || l > 3 || i1 > 3)
							{
								     if (World.doesBlockHaveSolidTopSurface(this.theWorld, i + l, k - 1, j + i1) && !otherBlock.isBlockNormalCube())
								boolean b = otherBlock.getMaterial().blocksMovement() && otherBlock.renderAsNormalBlock();
								if (!b && World.doesBlockHaveSolidTopSurface(this.theWorld, i + l, k - 1, j + i1))
								{
									this.theGuard.setLocationAndAngles(i + l + 0.5F, k, j + i1 + 0.5F, this.theGuard.rotationYaw, this.theGuard.rotationPitch);
									this.guardPathfinder.clearPathEntity();
									return;
								}
							}
						}
				}
		}
	}
}
