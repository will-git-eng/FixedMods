package com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.reactor;

import com.brandon3055.brandonscore.common.handlers.IProcess;
import com.brandon3055.brandonscore.common.handlers.ProcessHandler;
import com.brandon3055.brandonscore.common.utills.Utills;
import ru.will.git.draconicevolution.ModUtils;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerWorld;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by brandon3055 on 12/8/2015.
 */
public class ReactorExplosion implements IProcess
{

	public static DamageSource fusionExplosion = new DamageSource("damage.de.fusionExplode").setExplosion().setDamageBypassesArmor().setDamageIsAbsolute().setDamageAllowedInCreativeMode();

	private World worldObj;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private float power;
	private Random random = new Random();

	private double expansion = 0;

	    
	public final FakePlayerContainer fake;
	    

	public ReactorExplosion(World world, int x, int y, int z, float power)
	{
		this.worldObj = world;
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
		this.power = power;
		this.isDead = world.isRemote;

		    
		this.fake = new FakePlayerContainerWorld(ModUtils.profile, world);
		    
	}

	@Override
	public void updateProcess()
	{

		int OD = (int) this.expansion;
		int ID = OD - 1;
		int size = (int) this.expansion;

		for (int x = this.xCoord - size; x < this.xCoord + size; x++)
		{
			for (int z = this.zCoord - size; z < this.zCoord + size; z++)
			{
				double dist = Utills.getDistanceAtoB(x, z, this.xCoord, this.zCoord);
				if (dist < OD && dist >= ID)
				{
					float tracePower = this.power - (float) (this.expansion / 10D);
					tracePower *= 1F + (this.random.nextFloat() - 0.5F) * 0.2;
					ReactorExplosionTrace explosionTrace = new ReactorExplosionTrace(this.worldObj, x, this.yCoord, z, tracePower, this.random);

					    
					explosionTrace.fake.setParent(this.fake);
					    

					ProcessHandler.addProcess(explosionTrace);
				}
			}
		}

		this.isDead = this.expansion >= this.power * 10;
		this.expansion += 1;
	}

	private boolean isDead = false;

	@Override
	public boolean isDead()
	{
		return this.isDead;
	}
}
