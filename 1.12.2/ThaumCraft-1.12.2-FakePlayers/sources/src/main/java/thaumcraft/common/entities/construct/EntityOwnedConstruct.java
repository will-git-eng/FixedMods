package thaumcraft.common.entities.construct;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import ru.will.git.thaumcraft.ModUtils;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemNameTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.common.lib.SoundsTC;

import java.util.UUID;

public class EntityOwnedConstruct extends EntityCreature implements IEntityOwnable
{
	protected static final DataParameter<Byte> TAMED = EntityDataManager.createKey(EntityOwnedConstruct.class, DataSerializers.BYTE);
	protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityOwnedConstruct.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	boolean validSpawn = false;

	
	public final FakePlayerContainer fake = ModUtils.NEXUS_FACTORY.wrapFake(this);
	

	public EntityOwnedConstruct(World worldIn)
	{
		super(worldIn);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.getDataManager().register(TAMED, (byte) 0);
		this.getDataManager().register(OWNER_UNIQUE_ID, Optional.absent());
	}

	public boolean isOwned()
	{
		return (this.getDataManager().get(TAMED) & 4) != 0;
	}

	public void setOwned(boolean tamed)
	{
		byte b0 = this.getDataManager().get(TAMED);
		if (tamed)
			this.getDataManager().set(TAMED, (byte) (b0 | 4));
		else
			this.getDataManager().set(TAMED, (byte) (b0 & -5));

	}

	@Override
	public UUID getOwnerId()
	{
		return (UUID) ((Optional) this.getDataManager().get(OWNER_UNIQUE_ID)).orNull();
	}

	public void setOwnerId(UUID p_184754_1_)
	{
		this.getDataManager().set(OWNER_UNIQUE_ID, Optional.fromNullable(p_184754_1_));
	}

	@Override
	protected int decreaseAirSupply(int air)
	{
		return air;
	}

	@Override
	public boolean canBreatheUnderwater()
	{
		return true;
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundsTC.clack;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundsTC.clack;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundsTC.tool;
	}

	@Override
	public int getTalkInterval()
	{
		return 240;
	}

	@Override
	protected boolean canDespawn()
	{
		return false;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (this.getAttackTarget() != null && this.isOnSameTeam(this.getAttackTarget()))
			this.setAttackTarget(null);

		if (!this.world.isRemote && !this.validSpawn)
			this.setDead();

	}

	public void setValidSpawn()
	{
		this.validSpawn = true;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tagCompound)
	{
		super.writeEntityToNBT(tagCompound);
		tagCompound.setBoolean("v", this.validSpawn);
		if (this.getOwnerId() == null)
			tagCompound.setString("OwnerUUID", "");
		else
			tagCompound.setString("OwnerUUID", this.getOwnerId().toString());

		
		this.fake.writeToNBT(tagCompound);
		
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tagCompound)
	{
		super.readEntityFromNBT(tagCompound);
		this.validSpawn = tagCompound.getBoolean("v");
		String s = "";
		if (tagCompound.hasKey("OwnerUUID", 8))
			s = tagCompound.getString("OwnerUUID");
		else
		{
			String s1 = tagCompound.getString("Owner");
			s = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), s1);
		}

		if (!s.isEmpty())
			try
			{
				this.setOwnerId(UUID.fromString(s));
				this.setOwned(true);
			}
			catch (Throwable var4)
			{
				this.setOwned(false);
			}

		
		this.fake.readFromNBT(tagCompound);
		
	}

	public EntityLivingBase getOwnerEntity()
	{
		try
		{
			UUID uuid = this.getOwnerId();
			return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
		}
		catch (IllegalArgumentException var2)
		{
			return null;
		}
	}

	public boolean isOwner(EntityLivingBase entityIn)
	{
		return entityIn == this.getOwnerEntity();
	}

	@Override
	public Team getTeam()
	{
		if (this.isOwned())
		{
			EntityLivingBase entitylivingbase = this.getOwnerEntity();
			if (entitylivingbase != null)
				return entitylivingbase.getTeam();
		}

		return super.getTeam();
	}

	@Override
	public boolean isOnSameTeam(Entity otherEntity)
	{
		if (this.isOwned())
		{
			EntityLivingBase entitylivingbase1 = this.getOwnerEntity();
			if (otherEntity == entitylivingbase1)
				return true;

			if (entitylivingbase1 != null)
				return entitylivingbase1.isOnSameTeam(otherEntity);
		}

		return super.isOnSameTeam(otherEntity);
	}

	@Override
	public void onDeath(DamageSource cause)
	{
		if (!this.world.isRemote && this.world.getGameRules().getBoolean("showDeathMessages") && this.hasCustomName() && this.getOwnerEntity() instanceof EntityPlayerMP)
			this.getOwnerEntity().sendMessage(this.getCombatTracker().getDeathMessage());

		super.onDeath(cause);
	}

	@Override
	public Entity getOwner()
	{
		return this.getOwnerEntity();
	}

	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand)
	{
		if (this.isDead)
			return false;
		if (!player.isSneaking() && (player.getHeldItemMainhand() == null || !(player.getHeldItemMainhand().getItem() instanceof ItemNameTag)))
		{
			if (!this.world.isRemote && !this.isOwner(player))
			{
				player.sendStatusMessage(new TextComponentTranslation("§5§o" + I18n.translateToLocal("tc.notowned")), true);
				return true;
			}
			return super.processInteract(player, hand);
		}
		return false;
	}
}
