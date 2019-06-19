package chylex.hee.block;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.block.material.MaterialEnderGoo;
import chylex.hee.entity.GlobalMobData;
import chylex.hee.init.ItemList;
import chylex.hee.system.util.BlockPosM;
import ru.will.git.reflectionmedic.util.EventUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import java.util.Random;

public class BlockEnderGoo extends BlockFluidClassic
{
	public static boolean shouldBattleWater = true;
	public static final Material enderGoo = new MaterialEnderGoo();
	public static final Fluid fluid = new Fluid("enderGoo").setDensity(1500).setTemperature(220).setViscosity(1500);
	private final byte[] xOff = { (byte) -1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
	private final byte[] yOff = { (byte) 0, (byte) 0, (byte) -1, (byte) 1, (byte) 0, (byte) 0 };
	private final byte[] zOff = { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) -1, (byte) 1 };
	private static final PotionEffect weakness;
	private static final PotionEffect miningFatigue;
	private static final PotionEffect poison;

	public BlockEnderGoo()
	{
		super(fluid, enderGoo);
		this.disableStats();
		this.setQuantaPerBlock(5);
		this.setTickRate(18);
		this.setTickRandomly(true);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		if (world.blockExists(x - 1, y, z - 1) && world.blockExists(x + 1, y, z + 1))
		{
			super.updateTick(world, x, y, z, rand);
			if (shouldBattleWater)
			{
				BlockPosM tmpPos = BlockPosM.tmp(x, y, z);
				int meta = tmpPos.getMetadata(world);

				for (int i = 0; i < 6; ++i)
				{
					if (tmpPos.set(x + this.xOff[i], y + this.yOff[i], z + this.zOff[i]).getMaterial(world) == Material.water)
						if (rand.nextInt(Math.max(1, 10 - meta - (world.provider.dimensionId == 1 ? 7 : 0) + (i != 2 && i != 3 ? 0 : 2))) == 0)
						{
							tmpPos.setBlock(world, this, Math.max(2, tmpPos.getMetadata(world)));
							if (rand.nextInt(6 - meta) == 0)
								tmpPos.setAir(world);
						}
						else if (world.provider.dimensionId != 1 && rand.nextInt(4) != 0)
						{
							tmpPos.set(x, y, z).setBlock(world, Blocks.flowing_water, 2);

							for (int b = 0; b < 2 + rand.nextInt(5); ++b)
							{
								int index = rand.nextInt(6);
								if (tmpPos.set(x + this.xOff[index], y + this.yOff[index], z + this.zOff[index]).getBlock(world) == this)
									tmpPos.setBlock(world, Blocks.flowing_water, 2);
							}

							return;
						}
				}
			}

		}
		else
			world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
	}

	@Override
	public void velocityToAddToEntity(World world, int x, int y, int z, Entity entity, Vec3 vec)
	{
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if (!world.isRemote && entity instanceof EntityLivingBase && !GlobalMobData.isEnderGooTolerant((EntityLivingBase) entity))
		{
			EntityLivingBase e = (EntityLivingBase) entity;
			e.addPotionEffect(weakness);
			e.addPotionEffect(miningFatigue);
			PotionEffect eff = e.getActivePotionEffect(Potion.poison);
			if (eff == null)
			{
				e.addPotionEffect(poison);
				if ((eff = e.getActivePotionEffect(Potion.poison)) == null)
					return;
			}

			if (eff.getDuration() < 102)
				eff.combine(new PotionEffect(Potion.poison.id, eff.getDuration() + 17, eff.getAmplifier(), eff.getIsAmbient()));

			Vec3 vec = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
			super.velocityToAddToEntity(world, x, y, z, entity, vec);
			vec.normalize();
			entity.addVelocity(vec.xCoord * 0.0075D, vec.yCoord * 0.005D, vec.zCoord * 0.0075D);
			entity.motionX *= 0.25D;
			entity.motionY *= 0.45D;
			entity.motionZ *= 0.25D;
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand)
	{
		HardcoreEnderExpansion.fx.enderGoo(world, x, y, z);
	}

	@SubscribeEvent
	public void onBucketFill(FillBucketEvent event)
	{
		int x = event.target.blockX;
		int y = event.target.blockY;
		int z = event.target.blockZ;
		World world = event.world;
		if (BlockPosM.tmp(x, y, z).getBlock(world) == this)
		{
			    
			if (EventUtils.cantBreak(event.entityPlayer, x, y, z))
				return;
			    

			BlockPosM.tmp(x, y, z).setAir(world);
			event.result = new ItemStack(ItemList.bucket_ender_goo);
			event.setResult(Result.ALLOW);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		super.registerBlockIcons(iconRegister);
		fluid.setIcons(this.blockIcon);
	}

	static
	{
		weakness = new PotionEffect(Potion.weakness.id, 5, 1, false);
		miningFatigue = new PotionEffect(Potion.digSlowdown.id, 5, 1, false);
		poison = new PotionEffect(Potion.poison.id, 100, 2, false);
	}
}
