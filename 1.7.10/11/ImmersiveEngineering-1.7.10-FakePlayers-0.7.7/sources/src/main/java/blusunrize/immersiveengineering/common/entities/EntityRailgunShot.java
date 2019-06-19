package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.RailgunProjectileProperties;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.immersiveengineering.ModUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityRailgunShot extends EntityIEProjectile
{
	private ItemStack ammo;
	static final int dataMarker_ammo = 13;
    
    

	public EntityRailgunShot(World world)
	{
		super(world);
		this.setSize(.5f, .5f);
	}

	public EntityRailgunShot(World world, double x, double y, double z, double ax, double ay, double az, ItemStack ammo)
	{
		super(world, x, y, z, ax, ay, az);
		this.setSize(.5f, .5f);
		this.ammo = ammo;
		this.setAmmoSynced();
	}

	public EntityRailgunShot(World world, EntityLivingBase living, double ax, double ay, double az, ItemStack ammo)
	{
		super(world, living, ax, ay, az);
		this.setSize(.5f, .5f);
		this.ammo = ammo;
    
		if (living instanceof EntityPlayer)
    
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObjectByDataType(dataMarker_ammo, 5);
	}

	public void setAmmoSynced()
	{
		if (this.getAmmo() != null)
			this.dataWatcher.updateObject(dataMarker_ammo, this.getAmmo());
	}

	public ItemStack getAmmoSynced()
	{
		return this.dataWatcher.getWatchableObjectItemStack(dataMarker_ammo);
	}

	public ItemStack getAmmo()
	{
		return this.ammo;
	}

	public RailgunProjectileProperties getAmmoProperties()
	{
		if (this.ammoProperties == null && this.ammo != null)
			this.ammoProperties = RailgunHandler.getProjectileProperties(this.ammo);
		return this.ammoProperties;
	}

	@Override
	public double getGravity()
	{
		return .005 * (this.getAmmoProperties() != null ? this.getAmmoProperties().gravity : 1);
	}

	@Override
	public int getMaxTicksInGround()
	{
		return 500;
	}

	@Override
	public void onEntityUpdate()
    
    
    
    
    
		if (this.getAmmo() == null && this.worldObj.isRemote)
			this.ammo = this.getAmmoSynced();
		super.onEntityUpdate();
	}

	@Override
	public void onImpact(MovingObjectPosition mop)
	{
		if (!this.worldObj.isRemote && this.getAmmo() != null)
			if (mop.entityHit != null)
				if (this.getAmmoProperties() != null)
					if (!this.getAmmoProperties().overrideHitEntity(mop.entityHit, this.getShooter()))
    
						if (this.fake.cantDamage(mop.entityHit))
    

						mop.entityHit.attackEntityFrom(IEDamageSources.causeRailgunDamage(this, this.getShooter()), (float) (this.getAmmoProperties().damage * Config.getDouble("railgun_damage_multiplier")));
					}
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer player)
	{
		if (!this.worldObj.isRemote && this.inGround && this.getAmmo() != null)
			if (player.inventory.addItemStackToInventory(this.getAmmo().copy()))
			{
				this.playSound("random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				this.setDead();
			}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		if (this.ammo != null)
    
    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
    
    
	}
}