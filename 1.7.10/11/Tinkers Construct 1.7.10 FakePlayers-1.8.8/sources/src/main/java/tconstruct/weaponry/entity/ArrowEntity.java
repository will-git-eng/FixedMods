package tconstruct.weaponry.entity;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.tconstruct.ModUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import tconstruct.TConstruct;
import tconstruct.library.entity.ProjectileBase;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.util.PiercingArrowDamage;

public class ArrowEntity extends ProjectileBase
{
	public ArrowEntity(World world)
	{
		super(world);
	}

	public ArrowEntity(World world, double d, double d1, double d2)
	{
		super(world, d, d1, d2);
	}

	public ArrowEntity(World world, EntityPlayer player, float speed, float accuracy, ItemStack stack)
	{
		super(world, player, speed, accuracy, stack);
	}

	@Override
	protected double getStuckDepth()
	{
		return 0.25d;
	}

	@Override
	protected double getSlowdown()
    
    
		return super.getSlowdown();
	}

	@Override
	protected double getGravity()
	{
		if (this.returnStack == null || !this.returnStack.hasTagCompound())
			return super.getGravity();

		float mass = this.returnStack.getTagCompound().getCompoundTag("InfiTool").getFloat("Mass");
    
		return mass;
	}

	@Override
	public void onHitBlock(MovingObjectPosition movingobjectposition)
	{
		super.onHitBlock(movingobjectposition);

		if (this.defused)
			return;

		if (this.returnStack == null || !this.returnStack.hasTagCompound())
    
		float chance = this.returnStack.getTagCompound().getCompoundTag("InfiTool").getFloat("BreakChance");
		if (chance > TConstruct.random.nextFloat())
		{
			this.setDead();
			this.playSound("random.break", 1.0F, 1.5F / (this.rand.nextFloat() * 0.2F + 0.9F));
		}
	}

	@Override
	protected void playHitBlockSound(int x, int y, int z)
	{
		this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	protected void playHitEntitySound()
	{
		this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
    
	@Override
	public boolean dealDamage(float damage, ToolCore ammo, NBTTagCompound tags, Entity entityHit)
	{
    
    
		float shift = (tags.getFloat("Mass") - 0.7f) * this.armorPenetrationModifier();

		if (shift < 0)
			shift = 0;
		if (shift > damage)
			shift = damage;

    
		if (damage > 0)
    
		if (shift > 0)
    
			EntityPlayer owner = this.shootingEntity instanceof EntityPlayer ? (EntityPlayer) this.shootingEntity : ModUtils.getModFake(this.worldObj);
			if (EventUtils.cantDamage(owner, entityHit))
    

			DamageSource damagesource;
			if (this.shootingEntity == null)
				damagesource = new PiercingArrowDamage("arrow", this, this);
			else
    
    
			entityHit.attackEntityFrom(damagesource, shift);
		}

		return dealtDamage;
	}

	protected float armorPenetrationModifier()
	{
		return 1.0f;
	}
}
