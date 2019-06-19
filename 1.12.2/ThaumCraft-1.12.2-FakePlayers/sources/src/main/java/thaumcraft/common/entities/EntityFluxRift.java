package thaumcraft.common.entities;

import ru.will.git.thaumcraft.EventConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.IPlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.FocusEngine;
import thaumcraft.api.casters.FocusMediumRoot;
import thaumcraft.api.casters.FocusPackage;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.potions.PotionFluxTaint;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.blocks.world.taint.TaintHelper;
import thaumcraft.common.entities.monster.EntityWisp;
import thaumcraft.common.entities.monster.tainted.EntityTaintSeedPrime;
import thaumcraft.common.items.casters.foci.FocusEffectFlux;
import thaumcraft.common.items.casters.foci.FocusMediumCloud;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockBamf;
import thaumcraft.common.lib.potions.PotionInfectiousVisExhaust;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.RandomItemChooser;
import thaumcraft.common.world.aura.AuraHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityFluxRift extends Entity
{
	private static final DataParameter<Integer> SEED = EntityDataManager.createKey(EntityFluxRift.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SIZE = EntityDataManager.createKey(EntityFluxRift.class, DataSerializers.VARINT);
	private static final DataParameter<Float> STABILITY = EntityDataManager.createKey(EntityFluxRift.class, DataSerializers.FLOAT);
	private static final DataParameter<Boolean> COLLAPSE = EntityDataManager.createKey(EntityFluxRift.class, DataSerializers.BOOLEAN);
	int maxSize = 0;
	int lastSize = -1;
	static ArrayList<RandomItemChooser.Item> events = new ArrayList<>();
	public ArrayList<Vec3d> points = new ArrayList<>();
	public ArrayList<Float> pointsWidth = new ArrayList<>();

	public EntityFluxRift(World par1World)
	{
		super(par1World);
		this.setSize(2.0F, 2.0F);
	}

	@Override
	protected void entityInit()
	{
		this.getDataManager().register(SEED, 0);
		this.getDataManager().register(SIZE, 5);
		this.getDataManager().register(STABILITY, 0.0F);
		this.getDataManager().register(COLLAPSE, Boolean.FALSE);
	}

	public boolean getCollapse()
	{
		return this.getDataManager().get(COLLAPSE);
	}

	public void setCollapse(boolean b)
	{
		if (b)
			this.maxSize = this.getRiftSize();

		this.getDataManager().set(COLLAPSE, b);
	}

	public float getRiftStability()
	{
		return this.getDataManager().get(STABILITY);
	}

	public void setRiftStability(float s)
	{
		if (s > 100.0F)
			s = 100.0F;

		if (s < -100.0F)
			s = -100.0F;

		this.getDataManager().set(STABILITY, s);
	}

	public int getRiftSize()
	{
		return this.getDataManager().get(SIZE);
	}

	public void setRiftSize(int s)
	{
		this.getDataManager().set(SIZE, s);
		this.setSize();
	}

	@Override
	public double getYOffset()
	{
		return (double) (-this.height / 2.0F);
	}

	protected void setSize()
	{
		this.calcSteps(this.points, this.pointsWidth, new Random((long) this.getRiftSeed()));
		this.lastSize = this.getRiftSize();
		double x0 = Double.MAX_VALUE;
		double y0 = Double.MAX_VALUE;
		double z0 = Double.MAX_VALUE;
		double x1 = Double.MIN_VALUE;
		double y1 = Double.MIN_VALUE;
		double z1 = Double.MIN_VALUE;

		for (Vec3d v : this.points)
		{
			if (v.x < x0)
				x0 = v.x;

			if (v.x > x1)
				x1 = v.x;

			if (v.y < y0)
				y0 = v.y;

			if (v.y > y1)
				y1 = v.y;

			if (v.z < z0)
				z0 = v.z;

			if (v.z > z1)
				z1 = v.z;
		}

		this.setEntityBoundingBox(new AxisAlignedBB(this.posX + x0, this.posY + y0, this.posZ + z0, this.posX + x1, this.posY + y1, this.posZ + z1));
		this.width = Math.abs((float) Math.max(x1 - x0, z1 - z0));
		this.height = Math.abs((float) (y1 - y0));
	}

	@Override
	public void setPosition(double x, double y, double z)
	{
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		if (this.getDataManager() != null)
			this.setSize();
		else
			super.setPosition(x, y, z);

	}

	public int getRiftSeed()
	{
		return this.getDataManager().get(SEED);
	}

	public void setRiftSeed(int s)
	{
		this.getDataManager().set(SEED, s);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setInteger("MaxSize", this.maxSize);
		nbttagcompound.setInteger("RiftSize", this.getRiftSize());
		nbttagcompound.setInteger("RiftSeed", this.getRiftSeed());
		nbttagcompound.setFloat("Stability", this.getRiftStability());
		nbttagcompound.setBoolean("collapse", this.getCollapse());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		this.maxSize = nbttagcompound.getInteger("MaxSize");
		this.setRiftSize(nbttagcompound.getInteger("RiftSize"));
		this.setRiftSeed(nbttagcompound.getInteger("RiftSeed"));
		this.setRiftStability((float) nbttagcompound.getInteger("Stability"));
		this.setCollapse(nbttagcompound.getBoolean("collapse"));
	}

	@Override
	public void move(MoverType type, double x, double y, double z)
	{
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		
		if (!this.world.isRemote && !EventConfig.enableFluxRift)
		{
			this.setDead();
			return;
		}
		

		if (this.lastSize != this.getRiftSize())
			this.setSize();

		if (!this.world.isRemote)
		{
			if (this.getRiftSeed() == 0)
				this.setRiftSeed(this.rand.nextInt());

			if (!this.points.isEmpty())
			{
				int pi = this.rand.nextInt(this.points.size() - 1);
				Vec3d v1 = this.points.get(pi).addVector(this.posX, this.posY, this.posZ);
				Vec3d v2 = this.points.get(pi + 1).addVector(this.posX, this.posY, this.posZ);
				RayTraceResult rt = this.world.rayTraceBlocks(v1, v2, false);
				if (rt != null && rt.getBlockPos() != null)
				{
					BlockPos p = new BlockPos(rt.getBlockPos());
					IBlockState bs = this.world.getBlockState(p);
					if (!this.world.isAirBlock(p) && bs.getBlockHardness(this.world, p) >= 0.0F && bs.getBlock().canCollideCheck(bs, false))
					{
						this.world.playEvent(null, 2001, p, Block.getStateId(this.world.getBlockState(p)));
						this.world.setBlockToAir(p);
					}
				}

				for (Entity e : EntityUtils.getEntitiesInRange(this.getEntityWorld(), v1.x, v1.y, v1.z, this, Entity.class, 0.5D))
				{
					if (!e.isDead && (!(e instanceof EntityPlayer) || !((EntityPlayer) e).isCreative()))
						try
						{
							e.attackEntityFrom(DamageSource.OUT_OF_WORLD, 2.0F);
							if (e instanceof EntityItem)
								e.setDead();
						}
						catch (Exception ignored)
						{
						}
				}
			}

			if (this.points.size() < 3 && !this.getCollapse())
				this.setCollapse(true);

			if (this.getCollapse())
			{
				this.setRiftSize(this.getRiftSize() - 1);
				if (this.rand.nextBoolean())
					AuraHelper.addVis(this.world, this.getPosition(), 1.0F);
				else
					AuraHelper.polluteAura(this.world, this.getPosition(), 1.0F, false);

				if (this.rand.nextInt(10) == 0)
					this.world.createExplosion(this, this.posX + this.rand.nextGaussian() * 2.0D, this.posY + this.rand.nextGaussian() * 2.0D, this.posZ + this.rand.nextGaussian() * 2.0D, this.rand.nextFloat() / 2.0F, false);

				if (this.getRiftSize() <= 1)
				{
					this.completeCollapse();
					return;
				}
			}

			if (this.ticksExisted % 120 == 0)
				this.setRiftStability(this.getRiftStability() - 0.2F);

			if (this.ticksExisted % 600 == this.getEntityId() % 600)
			{
				float taint = AuraHandler.getFlux(this.world, this.getPosition());
				double size = Math.sqrt((double) (this.getRiftSize() * 2));
				if ((double) taint >= size && this.getRiftSize() < 100 && this.getStability() != EntityFluxRift.EnumStability.VERY_STABLE)
				{
					AuraHandler.drainFlux(this.getEntityWorld(), this.getPosition(), (float) size, false);
					this.setRiftSize(this.getRiftSize() + 1);
				}

				if (this.getRiftStability() < 0.0F && (float) this.rand.nextInt(1000) < Math.abs(this.getRiftStability()) + (float) this.getRiftSize())
					this.executeRiftEvent();
			}

			if (!this.isDead && this.ticksExisted % 300 == 0)
				this.playSound(SoundsTC.evilportal, (float) (0.15000000596046448D + this.rand.nextGaussian() * 0.066D), (float) (0.75D + this.rand.nextGaussian() * 0.1D));
		}
		else
		{
			if (!this.points.isEmpty() && this.points.size() > 2 && !this.getCollapse() && this.getRiftStability() < 0.0F && (float) this.rand.nextInt(150) < Math.abs(this.getRiftStability()))
			{
				int pi = 1 + this.rand.nextInt(this.points.size() - 2);
				Vec3d v1 = this.points.get(pi).addVector(this.posX, this.posY, this.posZ);
				FXDispatcher.INSTANCE.drawCurlyWisp(v1.x, v1.y, v1.z, 0.0D, 0.0D, 0.0D, 0.1F + this.pointsWidth.get(pi) * 3.0F, 1.0F, 1.0F, 1.0F, 0.25F, null, 1, 0, 0);
			}

			if (!this.points.isEmpty() && this.points.size() > 2 && this.getCollapse())
			{
				int pi = 1 + this.rand.nextInt(this.points.size() - 2);
				Vec3d v1 = this.points.get(pi).addVector(this.posX, this.posY, this.posZ);
				FXDispatcher.INSTANCE.drawCurlyWisp(v1.x, v1.y, v1.z, 0.0D, 0.0D, 0.0D, 0.1F + this.pointsWidth.get(pi) * 3.0F, 1.0F, 0.3F + this.rand.nextFloat() * 0.1F, 0.3F + this.rand.nextFloat() * 0.1F, 0.4F, null, 1, 0, 0);
			}
		}

	}

	public static void createRift(World world, BlockPos pos)
	{
		
		if (!EventConfig.enableFluxRift)
			return;
		

		pos = pos.add(world.rand.nextInt(16), 0, world.rand.nextInt(16));
		BlockPos p2 = world.getPrecipitationHeight(pos);
		if (!world.provider.hasSkyLight())
			for (p2 = new BlockPos(p2.getX(), 10, p2.getZ()); !world.isAirBlock(p2); p2 = p2.up(world.rand.nextInt(5) + 1))
			{
				if (p2.getY() > world.getActualHeight() - 5)
					return;
			}

		if (p2.getY() < world.getActualHeight() - 4)
		{
			if (EntityUtils.getEntitiesInRange(world, p2, null, EntityFluxRift.class, 32.0D).size() > 0)
				return;

			EntityFluxRift rift = new EntityFluxRift(world);
			rift.setRiftSeed(world.rand.nextInt());
			rift.setLocationAndAngles((double) p2.getX() + 0.5D, (double) p2.getY() + 0.5D, (double) p2.getZ() + 0.5D, (float) world.rand.nextInt(360), 0.0F);
			float taint = AuraHandler.getFlux(world, p2);
			double size = Math.sqrt((double) (taint * 3.0F));
			if (size > 5.0D && world.spawnEntity(rift))
			{
				rift.setRiftSize((int) size);
				AuraHandler.drainFlux(world, p2, (float) size, false);
				List<EntityPlayer> targets2 = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((double) p2.getX(), (double) p2.getY(), (double) p2.getZ(), (double) (p2.getX() + 1), (double) (p2.getY() + 1), (double) (p2.getZ() + 1)).grow(32.0D, 32.0D, 32.0D));
				if (targets2 != null && targets2.size() > 0)
					for (EntityPlayer target : targets2)
					{
						IPlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(target);
						if (!knowledge.isResearchKnown("f_toomuchflux"))
						{
							target.sendStatusMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.fluxevent.3")), true);
							ThaumcraftApi.internalMethods.completeResearch(target, "f_toomuchflux");
						}
					}
			}
		}
	}

	private void executeRiftEvent()
	{
		RandomItemChooser ric = new RandomItemChooser();
		EntityFluxRift.FluxEventEntry ei = (EntityFluxRift.FluxEventEntry) ric.chooseOnWeight(events);
		if (ei != null)
			if (ei.nearTaintAllowed || !TaintHelper.isNearTaintSeed(this.world, this.getPosition()))
			{
				boolean didit = false;
				switch (ei.event)
				{
					case 0:
						EntityWisp wisp = new EntityWisp(this.world);
						wisp.setLocationAndAngles(this.posX + this.rand.nextGaussian() * 5.0D, this.posY + this.rand.nextGaussian() * 5.0D, this.posZ + this.rand.nextGaussian() * 5.0D, 0.0F, 0.0F);
						if (this.world.rand.nextInt(5) == 0)
							wisp.setType(Aspect.FLUX.getTag());

						if (wisp.getCanSpawnHere() && this.world.spawnEntity(wisp))
							didit = true;
						break;
					case 1:
						
						if (!EventConfig.enableTaintSeed)
							break;
						

						EntityTaintSeedPrime seed = new EntityTaintSeedPrime(this.world);
						seed.setLocationAndAngles((double) (int) (this.posX + this.rand.nextGaussian() * 5.0D) + 0.5D, (double) (int) (this.posY + this.rand.nextGaussian() * 5.0D), (double) (int) (this.posZ + this.rand.nextGaussian() * 5.0D) + 0.5D, (float) this.world.rand.nextInt(360), 0.0F);
						if (seed.getCanSpawnHere() && this.world.spawnEntity(seed))
						{
							didit = true;
							seed.boost = this.getRiftSize();
							AuraHelper.polluteAura(this.getEntityWorld(), this.getPosition(), (float) (this.getRiftSize() / 2), true);
							this.setDead();
						}
						break;
					case 2:
						List<EntityLivingBase> targets2 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(16.0D, 16.0D, 16.0D));
						if (targets2 != null && targets2.size() > 0)
							for (EntityLivingBase target : targets2)
							{
								didit = true;
								if (target instanceof EntityPlayer)
									((EntityPlayer) target).sendStatusMessage(new TextComponentString("§5§o" + I18n.translateToLocal("tc.fluxevent.2")), true);

								
								if (EventConfig.potionInfectiousVisExhaust)
								
								{
									PotionEffect pe = new PotionEffect(PotionInfectiousVisExhaust.instance, 3000, 2);
									pe.getCurativeItems().clear();

									try
									{
										target.addPotionEffect(pe);
									}
									catch (Exception ignored)
									{
									}
								}
							}
						break;
					case 3:
						EntityPlayer target = this.world.getClosestPlayerToEntity(this, 16.0D);
						if (target != null)
						{
							FocusPackage p = new FocusPackage(target);
							FocusMediumRoot root = new FocusMediumRoot();
							root.setupFromCasterToTarget(target, target, 0.5D);
							p.addNode(root);
							FocusMediumCloud fp = new FocusMediumCloud();
							fp.initialize();
							fp.getSetting("radius").setValue(MathHelper.getInt(this.rand, 1, 3));
							fp.getSetting("duration").setValue(MathHelper.getInt(this.rand, Math.min(this.getRiftSize() / 2, 30), Math.min(this.getRiftSize(), 120)));
							p.addNode(fp);
							p.addNode(new FocusEffectFlux());
							FocusEngine.castFocusPackage(target, p, true);
						}
						break;
					case 4:
						this.setCollapse(true);
				}

				if (didit)
					this.setRiftStability(this.getRiftStability() + (float) ei.cost);

			}
	}

	private void calcSteps(ArrayList<Vec3d> pp, ArrayList<Float> ww, Random rr)
	{
		pp.clear();
		ww.clear();
		Vec3d right = new Vec3d(rr.nextGaussian(), rr.nextGaussian(), rr.nextGaussian()).normalize();
		Vec3d left = right.scale(-1.0D);
		Vec3d lr = new Vec3d(0.0D, 0.0D, 0.0D);
		Vec3d ll = new Vec3d(0.0D, 0.0D, 0.0D);
		int steps = MathHelper.ceil((float) this.getRiftSize() / 3.0F);
		float girth = (float) this.getRiftSize() / 300.0F;
		double angle = 0.33D;
		float dec = girth / (float) steps;

		for (int a = 0; a < steps; ++a)
		{
			girth -= dec;
			Vec3d var14 = right.rotatePitch((float) (rr.nextGaussian() * angle));
			right = var14.rotateYaw((float) (rr.nextGaussian() * angle));
			lr = lr.add(right.scale(0.2D));
			pp.add(new Vec3d(lr.x, lr.y, lr.z));
			ww.add(girth);
			Vec3d var15 = left.rotatePitch((float) (rr.nextGaussian() * angle));
			left = var15.rotateYaw((float) (rr.nextGaussian() * angle));
			ll = ll.add(left.scale(0.2D));
			pp.add(0, new Vec3d(ll.x, ll.y, ll.z));
			ww.add(0, girth);
		}

		lr = lr.add(right.scale(0.1D));
		pp.add(new Vec3d(lr.x, lr.y, lr.z));
		ww.add(0.0F);
		ll = ll.add(left.scale(0.1D));
		pp.add(0, new Vec3d(ll.x, ll.y, ll.z));
		ww.add(0, 0.0F);
	}

	public void addStability()
	{
		this.setRiftStability(this.getRiftStability() + 0.125F);
	}

	public EntityFluxRift.EnumStability getStability()
	{
		return this.getRiftStability() > 50.0F ? EntityFluxRift.EnumStability.VERY_STABLE : this.getRiftStability() >= 0.0F ? EnumStability.STABLE : this.getRiftStability() > -25.0F ? EnumStability.UNSTABLE : EnumStability.VERY_UNSTABLE;
	}

	@Override
	public void setFire(int seconds)
	{
	}

	@Override
	public boolean isBurning()
	{
		return false;
	}

	@Override
	public boolean canRenderOnFire()
	{
		return false;
	}

	private void completeCollapse()
	{
		int qq = (int) Math.sqrt((double) this.maxSize);
		if (this.rand.nextInt(100) < qq)
			this.entityDropItem(new ItemStack(ItemsTC.primordialPearl, 1, 4 + this.rand.nextInt(4)), 0.0F);

		for (int a = 0; a < qq; ++a)
		{
			this.entityDropItem(new ItemStack(ItemsTC.voidSeed), 0.0F);
		}

		label79:
		{
			PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockBamf(this.posX, this.posY, this.posZ, 0, true, true, null), new TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 64.0D));
			List<EntityLivingBase> list = EntityUtils.getEntitiesInRange(this.world, this.posX, this.posY, this.posZ, this, EntityLivingBase.class, 32.0D);
			switch (this.getStability())
			{
				case VERY_UNSTABLE:
					
					if (EventConfig.potionFluxTaint)
						
						for (EntityLivingBase p : list)
						{
							int w = (int) ((1.0D - p.getDistanceSq(this) / 32.0D) * 120.0D);
							if (w > 0)
								p.addPotionEffect(new PotionEffect(PotionFluxTaint.instance, w * 20, 0));
						}
				case UNSTABLE:
					for (EntityLivingBase p : list)
					{
						int w = (int) ((1.0D - p.getDistanceSq(this) / 32.0D) * 300.0D);
						if (w > 0)
							p.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, w * 20, 0));
					}
				case STABLE:
					break;
				default:
					break label79;
			}

			for (EntityLivingBase p : list)
			{
				if (p instanceof EntityPlayer)
				{
					int w = (int) ((1.0D - p.getDistanceSq(this) / 32.0D) * 25.0D);
					if (w > 0)
					{
						ThaumcraftApi.internalMethods.addWarpToPlayer((EntityPlayer) p, w, IPlayerWarp.EnumWarpType.NORMAL);
						ThaumcraftApi.internalMethods.addWarpToPlayer((EntityPlayer) p, w, IPlayerWarp.EnumWarpType.TEMPORARY);
					}
				}
			}
		}

		this.setDead();
	}

	static
	{
		events.add(new EntityFluxRift.FluxEventEntry(0, 50, 5, true));
		events.add(new EntityFluxRift.FluxEventEntry(1, 10, 0, false));
		events.add(new EntityFluxRift.FluxEventEntry(2, 20, 10, true));
		events.add(new EntityFluxRift.FluxEventEntry(3, 20, 10, true));
		events.add(new EntityFluxRift.FluxEventEntry(4, 1, 0, true));
	}

	public enum EnumStability
	{
		VERY_STABLE,
		STABLE,
		UNSTABLE,
		VERY_UNSTABLE
	}

	static class FluxEventEntry implements RandomItemChooser.Item
	{
		int weight;
		int event;
		int cost;
		boolean nearTaintAllowed;

		protected FluxEventEntry(int event, int weight, int cost, boolean nearTaintAllowed)
		{
			this.weight = weight;
			this.event = event;
			this.cost = cost;
			this.nearTaintAllowed = nearTaintAllowed;
		}

		@Override
		public double getWeight()
		{
			return (double) this.weight;
		}
	}
}
