package tconstruct.library.entity;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.tconstruct.ModUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.*;
import net.minecraft.world.World;
import tconstruct.library.ActiveToolMod;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.AbilityHelper;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.weaponry.AmmoItem;
import tconstruct.util.Reference;
import tconstruct.weaponry.entity.ArrowEntity;

    
public abstract class ProjectileBase extends EntityArrow implements IEntityAdditionalSpawnData
{
	public static final String woodSound = Reference.resource("woodHit");
	public static final String stoneSound = Reference.resource("stoneHit");

	public ItemStack returnStack;

	public boolean bounceOnNoDamage = true;
    

	public ProjectileBase(World world)
	{
		super(world);
	}

	public ProjectileBase(World world, double d, double d1, double d2)
	{
		this(world);
		this.setPosition(d, d1, d2);
	}

	public ProjectileBase(World world, EntityPlayer player, float speed, float accuracy, ItemStack stack)
	{
		this(world);

		this.shootingEntity = player;

    
		this.setLocationAndAngles(player.posX, player.posY + player.getEyeHeight(), player.posZ, player.rotationYaw, player.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = +MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = -MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI);
    
    
	}

	public ItemStack getEntityItem()
	{
		return this.returnStack;
	}

	protected void playHitBlockSound(int x, int y, int z)
	{
		Block block = this.worldObj.getBlock(x, y, z);
		if (block != null && block.getMaterial() == Material.wood)
			this.worldObj.playSoundAtEntity(this, woodSound, 1.0f, 1.0f);
		else
			this.worldObj.playSoundAtEntity(this, stoneSound, 1.0f, 1.0f);

		if (block != null)
			this.worldObj.playSoundAtEntity(this, block.stepSound.getBreakSound(), 0.7f, 1.0f);
	}

	protected void playHitEntitySound()
	{

	}

    
	protected double getStuckDepth()
	{
		return 0.5f;
	}

	protected void doLivingHit(EntityLivingBase entityHit)
	{
		float knockback = this.returnStack.getTagCompound().getCompoundTag("InfiTool").getFloat("Knockback");
		if (this.shootingEntity instanceof EntityLivingBase)
			knockback += EnchantmentHelper.getKnockbackModifier((EntityLivingBase) this.shootingEntity, entityHit);

		if (!this.worldObj.isRemote)
			entityHit.setArrowCountInEntity(entityHit.getArrowCountInEntity() + 1);

		if (knockback > 0)
		{
			double horizontalSpeed = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);

			if (horizontalSpeed > 0.0F)
				entityHit.addVelocity(this.motionX * knockback * 0.6000000238418579D / horizontalSpeed, 0.1D, this.motionZ * knockback * 0.6000000238418579D / horizontalSpeed);
		}

		if (this.shootingEntity != null && this.shootingEntity instanceof EntityLivingBase)
		{
			EnchantmentHelper.func_151384_a(entityHit, this.shootingEntity);
			EnchantmentHelper.func_151385_b((EntityLivingBase) this.shootingEntity, entityHit);
		}

		if (this.shootingEntity != null && entityHit != this.shootingEntity && entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP)
			((EntityPlayerMP) this.shootingEntity).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0.0F));
	}

	public void onHitBlock(MovingObjectPosition movingobjectposition)
	{
		this.field_145791_d = movingobjectposition.blockX;
		this.field_145792_e = movingobjectposition.blockY;
		this.field_145789_f = movingobjectposition.blockZ;
		this.field_145790_g = this.worldObj.getBlock(this.field_145791_d, this.field_145792_e, this.field_145789_f);
		this.inData = this.worldObj.getBlockMetadata(this.field_145791_d, this.field_145792_e, this.field_145789_f);
		this.motionX = movingobjectposition.hitVec.xCoord - this.posX;
		this.motionY = movingobjectposition.hitVec.yCoord - this.posY;
		this.motionZ = movingobjectposition.hitVec.zCoord - this.posZ;
		double speed = this.getStuckDepth() * MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		this.posX -= this.motionX / speed * 0.05000000074505806D;
		this.posY -= this.motionY / speed * 0.05000000074505806D;
		this.posZ -= this.motionZ / speed * 0.05000000074505806D;

		this.playHitBlockSound(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ);

		this.inGround = true;
		this.arrowShake = 7;
		this.setIsCritical(false);
    

		if (this.field_145790_g.getMaterial() != Material.air)
			this.field_145790_g.onEntityCollidedWithBlock(this.worldObj, this.field_145791_d, this.field_145792_e, this.field_145789_f, this);
	}

	public void onHitEntity(MovingObjectPosition movingobjectposition)
	{
		NBTTagCompound tags = this.returnStack.getTagCompound().getCompoundTag("InfiTool");
    
    
		float dist2 = 0;
		dist2 += MathHelper.abs((float) movingobjectposition.entityHit.lastTickPosX - (float) this.lastTickPosX);
		dist2 += MathHelper.abs((float) movingobjectposition.entityHit.lastTickPosY - (float) this.lastTickPosY);
		dist2 += MathHelper.abs((float) movingobjectposition.entityHit.lastTickPosZ - (float) this.lastTickPosZ);
		dist2 = MathHelper.sqrt_double(dist2);

		distance += dist2;

		if (!tags.hasKey("BaseAttack"))
    
    
    
			int atk = tags.getInteger("Attack");
			if (tags.hasKey("ModAttack"))
			{
				int bonusDmg = tags.getIntArray("ModAttack")[0] / 24 + 1;
				atk -= bonusDmg;
			}
			tags.setInteger("BaseAttack", atk);
		}

		float baseAttack = tags.getInteger("BaseAttack");
		float totalAttack = tags.getInteger("Attack");
    
    
    
		if (this instanceof ArrowEntity)
			damage = Math.max(0, damage - totalAttack / 2f);

    
    
    
		int baseDamage = 0;
    
			for (ActiveToolMod toolmod : TConstructRegistry.activeModifiers)
			{
				int dmg = toolmod.baseAttackDamage(baseDamage, (int) damage, ammo, this.returnStack.getTagCompound(), tags, this.returnStack, (EntityPlayer) this.shootingEntity, movingobjectposition.entityHit);
				if (dmg > baseDamage)
					baseDamage = dmg;
			}
    
    
    
    
		if (this.shootingEntity != null && movingobjectposition.entityHit instanceof EntityLivingBase)
			bonusDamage += EnchantmentHelper.getEnchantmentModifierLiving((EntityLivingBase) this.shootingEntity, (EntityLivingBase) movingobjectposition.entityHit);
    
		if (damage < 1)
    
		int modDamage = 0;
		if (shotByPlayer)
			for (ActiveToolMod mod : TConstructRegistry.activeModifiers)
			{
				modDamage += mod.attackDamage(modDamage, (int) damage, ammo, this.returnStack.getTagCompound(), tags, this.returnStack, (EntityPlayer) this.shootingEntity, movingobjectposition.entityHit);
			}
    
		if (this.getIsCritical())
    
		if (!this.dealDamage(damage, ammo, tags, movingobjectposition.entityHit))
		{
			if (!this.bounceOnNoDamage)
    
			this.motionX *= -0.10000000149011612D;
			this.motionY *= -0.10000000149011612D;
			this.motionZ *= -0.10000000149011612D;
			this.rotationYaw += 180.0F;
			this.prevRotationYaw += 180.0F;
			this.ticksInAir = 0;
			return;
    
		else
			AbilityHelper.processFiery(this.shootingEntity, movingobjectposition.entityHit, tags);

		if (movingobjectposition.entityHit instanceof EntityLivingBase)
			this.doLivingHit((EntityLivingBase) movingobjectposition.entityHit);

		this.playHitEntitySound();

    
			if (this.rand.nextInt(10) + 1 > tags.getInteger("Reinforced"))
				this.setDead();
			else
			{
				this.motionX = Math.max(-0.1, Math.min(0.1, -this.motionX));
				this.motionY = 0.2;
				this.motionZ = Math.max(-0.1, Math.min(0.1, -this.motionZ));
				this.ticksInAir = 0;
				this.posX = movingobjectposition.entityHit.posX;
				this.posY = movingobjectposition.entityHit.posY + movingobjectposition.entityHit.height / 2d;
				this.posZ = movingobjectposition.entityHit.posZ;
    
			}
    
	public boolean dealDamage(float damage, ToolCore ammo, NBTTagCompound tags, Entity entityHit)
    
		EntityPlayer owner = this.shootingEntity instanceof EntityPlayer ? (EntityPlayer) this.shootingEntity : ModUtils.getModFake(this.worldObj);
		if (EventUtils.cantDamage(owner, entityHit))
    

    
		DamageSource damagesource;
		if (this.shootingEntity == null)
			damagesource = DamageSource.causeArrowDamage(this, this);
		else
			damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity);
		dealtDamage = entityHit.attackEntityFrom(damagesource, damage);

		return dealtDamage;
	}

	@Override
	public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_)
    
    
    
	}

    
	public void onUpdate()
    
    
    
		if (this.arrowShake > 0)
    
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, f) * 180.0D / Math.PI);
    
		Block block = this.worldObj.getBlock(this.field_145791_d, this.field_145792_e, this.field_145789_f);
		if (block.getMaterial() != Material.air)
		{
			block.setBlockBoundsBasedOnState(this.worldObj, this.field_145791_d, this.field_145792_e, this.field_145789_f);
    
			if (axisalignedbb != null && axisalignedbb.isVecInside(Vec3.createVectorHelper(this.posX, this.posY, this.posZ)))
				this.inGround = true;
		}

		if (this.inGround)
			this.updateInGround();
		else
			this.updateInAir();
    
	protected void updateInGround()
	{
		Block block = this.worldObj.getBlock(this.field_145791_d, this.field_145792_e, this.field_145789_f);
    
		if (block == this.field_145790_g && j == this.inData)
		{
			++this.ticksInGround;

			if (this.ticksInGround == 1200)
				this.setDead();
		}
		else
		{
			this.inGround = false;
			this.motionX *= this.rand.nextFloat() * 0.2F;
			this.motionY *= this.rand.nextFloat() * 0.2F;
			this.motionZ *= this.rand.nextFloat() * 0.2F;
			this.ticksInGround = 0;
			this.ticksInAir = 0;
		}
    
	protected void updateInAir()
    
    
		Vec3 curPos = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
		Vec3 newPos = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
    
    
		if (movingobjectposition != null)
			newPos = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
		else
			newPos = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		if (!this.defused)
    
			Entity entity = null;
			List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double distance = 0.0D;
			float f1;

			for (Entity ent : list)
			{
				if (!ent.canBeCollidedWith())
    
				if (ent == this.shootingEntity && this.ticksInAir < 5)
					continue;

				f1 = 0.3F;
				AxisAlignedBB axisalignedbb1 = ent.boundingBox.expand(f1, f1, f1);
    
				if (movingobjectposition1 != null)
    
					double otherDistance = curPos.distanceTo(movingobjectposition1.hitVec);

					if (otherDistance < distance || distance == 0.0D)
					{
						entity = ent;
						distance = otherDistance;
					}
				}
    
			if (entity != null)
    
			if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer)
			{
    
				if (entityplayer.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer) this.shootingEntity).canAttackPlayer(entityplayer))
    
			}
    
		if (movingobjectposition != null)
			if (movingobjectposition.entityHit != null)
				this.onHitEntity(movingobjectposition);
			else
    
		if (this.getIsCritical())
    
		this.doMoveUpdate();
    
		if (this.isInWater())
		{
			for (int l = 0; l < 4; ++l)
			{
				float f4 = 0.25F;
				this.worldObj.spawnParticle("bubble", this.posX - this.motionX * f4, this.posY - this.motionY * f4, this.posZ - this.motionZ * f4, this.motionX, this.motionY, this.motionZ);
    
			slowdown = 1d - 20d * this.getSlowdown();
    
		if (this.isWet())
    
		this.motionX *= slowdown;
		this.motionY *= slowdown;
    
		this.motionY -= this.getGravity();
    
		this.func_145775_I();
	}

	public void drawCritParticles()
	{
		for (int i = 0; i < 4; ++i)
		{
			this.worldObj.spawnParticle("crit", this.posX + this.motionX * i / 4.0D, this.posY + this.motionY * i / 4.0D, this.posZ + this.motionZ * i / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ);
		}
	}

	protected void doMoveUpdate()
	{
		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		double f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
		this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
    
		while (this.rotationPitch - this.prevRotationPitch < -180.0F)
		{
			this.prevRotationPitch -= 360.0F;
		}

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
		{
			this.prevRotationPitch += 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw < -180.0F)
		{
			this.prevRotationYaw -= 360.0F;
		}

		while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
		{
			this.prevRotationYaw += 360.0F;
		}

		this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
		this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
	}

    
	protected double getSlowdown()
	{
		return 0.01;
	}

    
	protected double getGravity()
	{
		return 0.05;
	}

    
	@Override
	public void onCollideWithPlayer(EntityPlayer player)
	{
		if (!this.worldObj.isRemote && this.inGround && this.arrowShake <= 0)
		{
			boolean flag = this.canBePickedUp == 1 || this.canBePickedUp == 2 && player.capabilities.isCreativeMode;

    
				if (this.returnStack != null && this.returnStack.getItem() instanceof AmmoItem)
				{
					if (!((AmmoItem) this.returnStack.getItem()).pickupAmmo(this.returnStack, null, player))
						flag = false;
    
    
				else if (this.returnStack != null && !player.inventory.addItemStackToInventory(this.returnStack))
					flag = false;

			if (flag)
			{
				this.playSound("random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				player.onItemPickup(this, 1);
				this.setDead();
			}
		}
	}

    

	@Override
	public void writeEntityToNBT(NBTTagCompound tags)
	{
		super.writeEntityToNBT(tags);

		tags.setTag("Throwable", this.returnStack.writeToNBT(new NBTTagCompound()));
    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tags)
	{
		super.readEntityFromNBT(tags);

		this.returnStack = ItemStack.loadItemStackFromNBT(tags.getCompoundTag("Throwable"));
    
    
		if (this.returnStack == null || !(this.returnStack.getItem() instanceof ToolCore))
			this.setDead();
	}

	@Override
	public void writeSpawnData(ByteBuf data)
	{
		ByteBufUtils.writeItemStack(data, this.returnStack);
    
		int id = this.shootingEntity == null ? this.getEntityId() : this.shootingEntity.getEntityId();
    
		data.writeDouble(this.motionX);
		data.writeDouble(this.motionY);
		data.writeDouble(this.motionZ);
	}

	@Override
	public void readSpawnData(ByteBuf data)
	{
		this.returnStack = ByteBufUtils.readItemStack(data);
		this.rotationYaw = data.readFloat();
		this.shootingEntity = this.worldObj.getEntityByID(data.readInt());

		this.motionX = data.readDouble();
		this.motionY = data.readDouble();
    
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.posY -= 0.10000000149011612D;
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
	}
}
