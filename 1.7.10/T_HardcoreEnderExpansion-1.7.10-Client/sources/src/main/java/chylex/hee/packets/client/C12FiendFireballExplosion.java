package chylex.hee.packets.client;

import chylex.hee.entity.projectile.EntityProjectileFiendFireball;
import chylex.hee.packets.AbstractClientPacket;
import chylex.hee.proxy.ModCommonProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.world.Explosion;

public class C12FiendFireballExplosion extends AbstractClientPacket
{
	private double x;
	private double y;
	private double z;
	private boolean isOP;

	public C12FiendFireballExplosion()
	{
	}

	public C12FiendFireballExplosion(EntityProjectileFiendFireball fireball)
	{
		this.x = fireball.posX;
		this.y = fireball.posY;
		this.z = fireball.posZ;
		this.isOP = ModCommonProxy.opMobs;
	}

	@Override
	public void write(ByteBuf buffer)
	{
		buffer.writeDouble(this.x).writeDouble(this.y).writeDouble(this.z).writeBoolean(this.isOP);
	}

	@Override
	public void read(ByteBuf buffer)
	{
		this.x = buffer.readDouble();
		this.y = buffer.readDouble();
		this.z = buffer.readDouble();
		this.isOP = buffer.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void handle(EntityClientPlayerMP player)
	{
		Explosion explosion = new Explosion(player.worldObj, null, this.x, this.y, this.z, this.isOP ? 3.4F : 2.8F);
		explosion.doExplosionA();

		    
		if (player.worldObj.isRemote)
			explosion.affectedBlockPositions.clear();
		    

		explosion.doExplosionB(true);
	}
}
