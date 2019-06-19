package thaumcraft.common.entities.projectile;

import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.thaumcraft.EventConfig;
import ru.will.git.thaumcraft.ModUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

import java.util.List;

public class EntityBottleTaint extends EntityThrowable
    
	public final FakePlayerContainer fake = new FakePlayerContainerEntity(ModUtils.profile, this);

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		this.fake.writeToNBT(nbt);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.fake.readFromNBT(nbt);
    

	public EntityBottleTaint(World p_i1788_1_)
	{
		super(p_i1788_1_);
	}

	public EntityBottleTaint(World p_i1790_1_, EntityLivingBase par2EntityLiving)
	{
    
		if (par2EntityLiving instanceof EntityPlayer)
    
	}

	@Override
	protected float getGravityVelocity()
	{
		return 0.05F;
	}

	@Override
	protected float func_70182_d()
	{
		return 0.5F;
	}

	@Override
	protected float func_70183_g()
	{
		return -20.0F;
	}

	@Override
	protected void onImpact(MovingObjectPosition p_70184_1_)
	{
		if (!this.worldObj.isRemote)
		{
			List<EntityLivingBase> ents = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).expand(5.0D, 5.0D, 5.0D));
			if (ents.size() > 0)
				for (EntityLivingBase el : ents)
				{
					if (!(el instanceof ITaintedMob) && !el.isEntityUndead())
    
						if (this.fake.cantDamage(el))
    

						el.addPotionEffect(new PotionEffect(Config.potionTaintPoisonID, 100, 0, false));
					}
    
			if (!EventConfig.enableBottleTaint)
			{
				this.setDead();
				return;
    

			int x = (int) this.posX;
			int y = (int) this.posY;
			int z = (int) this.posZ;

			for (int a = 0; a < 10; ++a)
			{
				int xx = x + (int) ((this.rand.nextFloat() - this.rand.nextFloat()) * 5.0F);
				int zz = z + (int) ((this.rand.nextFloat() - this.rand.nextFloat()) * 5.0F);
				if (this.worldObj.rand.nextBoolean() && this.worldObj.getBiomeGenForCoords(xx, zz) != ThaumcraftWorldGenerator.biomeTaint)
    
					if (this.fake.cantBreak(xx, y, zz))
    

					Utils.setBiomeAt(this.worldObj, xx, zz, ThaumcraftWorldGenerator.biomeTaint);
					if (this.worldObj.isBlockNormalCubeDefault(xx, y - 1, zz, false) && this.worldObj.getBlock(xx, y, zz).isReplaceable(this.worldObj, xx, y, zz))
						this.worldObj.setBlock(xx, y, zz, ConfigBlocks.blockTaintFibres, 0, 3);
				}
			}

			this.setDead();
		}
		else
		{
			for (int a = 0; a < Thaumcraft.proxy.particleCount(100); ++a)
			{
				Thaumcraft.proxy.taintsplosionFX(this);
			}

			Thaumcraft.proxy.bottleTaintBreak(this.worldObj, this.posX, this.posY, this.posZ);
		}

	}
}
