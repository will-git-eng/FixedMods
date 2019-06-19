package thaumcraft.common.entities.construct;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.enchantment.EnumInfusionEnchantment;
import thaumcraft.common.lib.network.FakeNetHandlerPlayServer;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBoreDig;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.world.aura.AuraHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@EventBusSubscriber
public class EntityArcaneBore extends EntityOwnedConstruct
{
	BlockPos digTarget;
	BlockPos digTargetPrev;
	float digCost;
	int paused;
	int maxPause;
	long soundDelay;
	Object beam1;
	double beamLength;
	private static HashMap<Integer, ArrayList<ItemStack>> drops = new HashMap();
	int breakCounter;
	int digDelay;
	int digDelayMax;
	float radInc;
	public int spiral;
	public float currentRadius;
	private float charge;
	private static final DataParameter<EnumFacing> FACING = EntityDataManager.createKey(EntityArcaneBore.class, DataSerializers.FACING);
	private static final DataParameter<Boolean> ACTIVE = EntityDataManager.createKey(EntityArcaneBore.class, DataSerializers.BOOLEAN);
	public boolean clientDigging;

	public EntityArcaneBore(World worldIn)
	{
		super(worldIn);
		this.digTarget = null;
		this.digTargetPrev = null;
		this.digCost = 0.25F;
		this.paused = 100;
		this.maxPause = 100;
		this.soundDelay = 0L;
		this.beam1 = null;
		this.beamLength = 0.0D;
		this.breakCounter = 0;
		this.digDelay = 0;
		this.digDelayMax = 0;
		this.radInc = 0.0F;
		this.spiral = 0;
		this.currentRadius = 0.0F;
		this.charge = 0.0F;
		this.clientDigging = false;
		this.setSize(0.9F, 0.9F);
	}

	public EntityArcaneBore(World worldIn, BlockPos pos, EnumFacing facing)
	{
		this(worldIn);
		this.setFacing(facing);
		this.setPositionAndRotation((double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D, 0.0F, 0.0F);
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (!this.world.isRemote)
		{
			this.rotationYaw = this.rotationYawHead;
			if (this.ticksExisted % 50 == 0)
				this.heal(1.0F);

			if (this.ticksExisted % 10 == 0 && this.getCharge() < 10.0F)
				this.rechargeVis();

			int k = MathHelper.floor(this.posX);
			int l = MathHelper.floor(this.posY);
			int i1 = MathHelper.floor(this.posZ);
			if (BlockRailBase.isRailBlock(this.world, new BlockPos(k, l - 1, i1)))
				--l;

			BlockPos blockpos = new BlockPos(k, l, i1);
			IBlockState iblockstate = this.world.getBlockState(blockpos);
			if (BlockRailBase.isRailBlock(iblockstate))
			{
				if (iblockstate.getBlock() == BlocksTC.activatorRail)
				{
					boolean ac = iblockstate.getValue(BlockRailPowered.POWERED);
					this.setActive(!ac);
				}
			}
			else if (!this.isRiding())
				this.setActive(this.world.isBlockPowered(new BlockPos(this).down()));

			if (this.validInventory())
				try
				{
					this.getHeldItemMainhand().updateAnimation(this.world, this, 0, true);
				}
				catch (Exception ignored)
				{
				}
		}

		if (!this.isActive())
		{
			this.digTarget = null;
			this.getLookHelper().setLookPosition(this.posX + (double) this.getFacing().getFrontOffsetX(), this.posY, this.posZ + (double) this.getFacing().getFrontOffsetZ(), 10.0F, 33.0F);
		}

		if (this.digTarget != null && this.getCharge() >= this.digCost && !this.world.isRemote)
		{
			this.getLookHelper().setLookPosition((double) this.digTarget.getX() + 0.5D, (double) this.digTarget.getY(), (double) this.digTarget.getZ() + 0.5D, 10.0F, 90.0F);
			if (this.digDelay-- <= 0 && this.dig())
			{
				this.setCharge((float) (byte) (int) (this.getCharge() - this.digCost));
				if (this.soundDelay < System.currentTimeMillis())
				{
					this.soundDelay = System.currentTimeMillis() + 1200L + (long) this.world.rand.nextInt(100);
					this.playSound(SoundsTC.rumble, 0.25F, 0.9F + this.world.rand.nextFloat() * 0.2F);
				}
			}
		}

		if (!this.world.isRemote && this.digTarget == null && this.isActive() && this.validInventory())
		{
			this.findNextBlockToDig();
			if (this.digTarget != null)
			{
				this.world.setEntityState(this, (byte) 16);
				PacketHandler.INSTANCE.sendToAllAround(new PacketFXBoreDig(this.digTarget, this, this.digDelayMax), new TargetPoint(this.world.provider.getDimension(), (double) this.digTarget.getX(), (double) this.digTarget.getY(), (double) this.digTarget.getZ(), 32.0D));
			}
			else
			{
				this.world.setEntityState(this, (byte) 17);
				this.getLookHelper().setLookPosition(this.posX + (double) (this.getFacing().getFrontOffsetX() * 2), this.posY + (double) (this.getFacing().getFrontOffsetY() * 2) + (double) this.getEyeHeight(), this.posZ + (double) (this.getFacing().getFrontOffsetZ() * 2), 10.0F, 33.0F);
			}
		}

	}

	public boolean validInventory()
	{
		ItemStack heldItem = this.getHeldItemMainhand();
		boolean b = heldItem != null && !heldItem.isEmpty() && (heldItem.getItem() instanceof ItemPickaxe || heldItem.getItem().getToolClasses(heldItem).contains("pickaxe"));
		if (b && heldItem.getItemDamage() + 1 >= heldItem.getMaxDamage())
			b = false;

		return b;
	}

	public int getDigRadius()
	{
		int r = 0;
		ItemStack heldItem = this.getHeldItemMainhand();
		if (heldItem != null && !heldItem.isEmpty() && (heldItem.getItem() instanceof ItemPickaxe || heldItem.getItem().getToolClasses(heldItem).contains("pickaxe")))
		{
			r = heldItem.getItem().getItemEnchantability() / 3;
			r = r + EnumInfusionEnchantment.getInfusionEnchantmentLevel(heldItem, EnumInfusionEnchantment.DESTRUCTIVE) * 2;
		}

		return r <= 1 ? 2 : r;
	}

	public int getDigDepth()
	{
		int r = this.getDigRadius() * 8;
		r = r + EnumInfusionEnchantment.getInfusionEnchantmentLevel(this.getHeldItemMainhand(), EnumInfusionEnchantment.BURROWING) * 16;
		return r;
	}

	public int getFortune()
	{
		int r = 0;
		if (this.validInventory())
		{
			ItemStack heldItem = this.getHeldItemMainhand();
			r = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, heldItem);
			int r2 = EnumInfusionEnchantment.getInfusionEnchantmentLevel(heldItem, EnumInfusionEnchantment.SOUNDING);
			r = Math.max(r, r2);
		}

		return r;
	}

	public int getDigSpeed(IBlockState blockState)
	{
		int speed = 0;
		if (this.validInventory())
		{
			ItemStack heldItem = this.getHeldItemMainhand();
			speed = (int) ((float) speed + heldItem.getItem().getDestroySpeed(heldItem, blockState) / 2.0F);
			speed = speed + EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, heldItem);
		}

		return speed;
	}

	public int getRefining()
	{
		int refining = 0;
		ItemStack heldItem = this.getHeldItemMainhand();
		if (heldItem != null && !heldItem.isEmpty())
			refining = EnumInfusionEnchantment.getInfusionEnchantmentLevel(heldItem, EnumInfusionEnchantment.REFINING);

		return refining;
	}

	public boolean hasSilkTouch()
	{
		ItemStack heldItem = this.getHeldItemMainhand();
		return heldItem != null && !heldItem.isEmpty() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, heldItem) > 0;
	}

	private boolean canSilkTouch(BlockPos pos, IBlockState state)
	{
		return this.hasSilkTouch() && state.getBlock().canSilkHarvest(this.world, pos, state, null);
	}

	@SubscribeEvent
	public static void harvestBlockEvent(HarvestDropsEvent event)
	{
		if (event.getHarvester() != null && event.getHarvester().getName().equals("FakeThaumcraftBore"))
		{
			ArrayList<ItemStack> droplist = new ArrayList();
			if (drops.containsKey(event.getHarvester().arrowHitTimer) && drops.get(event.getHarvester().arrowHitTimer) != null)
				droplist = drops.get(event.getHarvester().arrowHitTimer);

			for (ItemStack s : event.getDrops())
			{
				if (event.getHarvester().world.rand.nextFloat() <= event.getDropChance())
					droplist.add(s);
			}

			drops.put(event.getHarvester().arrowHitTimer, droplist);
			event.getDrops().clear();
		}

	}

	private boolean dig()
	{
		boolean b = false;
		if (this.digTarget != null && !this.world.isAirBlock(this.digTarget))
		{
			IBlockState digBs = this.world.getBlockState(this.digTarget);
			if (!digBs.getBlock().isAir(digBs, this.world, this.digTarget))
			{
				
				if (this.fake.cantBreak(this.digTarget))
				{
					this.digDelay = 40;
					return false;
				}
				

				boolean silktouch = false;
				int fortune = this.getFortune();
				if (this.canSilkTouch(this.digTarget, digBs))
				{
					silktouch = true;
					fortune = 0;
				}

				FakePlayer fp = FakePlayerFactory.get((WorldServer) this.world, new GameProfile(null, "FakeThaumcraftBore"));
				fp.connection = new FakeNetHandlerPlayServer(fp.mcServer, new NetworkManager(EnumPacketDirection.CLIENTBOUND), fp);
				fp.arrowHitTimer = this.getEntityId();
				fp.xpCooldown = 1;
				fp.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
				fp.setHeldItem(EnumHand.MAIN_HAND, this.getHeldItemMainhand());
				if (BlockUtils.harvestBlock(this.getEntityWorld(), fp, this.digTarget, false, false, fortune, false))
				{
					ArrayList<ItemStack> items = drops.get(this.getEntityId());
					if (items == null)
						items = new ArrayList();

					List<EntityItem> targets = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB((double) this.digTarget.getX(), (double) this.digTarget.getY(), (double) this.digTarget.getZ(), (double) (this.digTarget.getX() + 1), (double) (this.digTarget.getY() + 1), (double) (this.digTarget.getZ() + 1)).grow(1.5D, 1.5D, 1.5D));
					if (targets.size() > 0)
						for (EntityItem e : targets)
						{
							items.add(e.getItem().copy());
							e.setDead();
						}

					int refining = this.getRefining();
					if (items.size() > 0)
						for (ItemStack is : items)
						{
							ItemStack dropped = is.copy();
							if (!silktouch && refining > 0)
								dropped = Utils.findSpecialMiningResult(is, (float) (refining + 1) * 0.125F, this.world.rand);

							if (dropped != null && !dropped.isEmpty())
							{
								boolean e = false;

								for (EnumFacing f : EnumFacing.VALUES)
								{
									BlockPos p = this.getPosition().offset(f);
									IItemHandler inventory = ThaumcraftInvHelper.getItemHandlerAt(this.getEntityWorld(), p, f);
									if (inventory != null)
									{
										InventoryUtils.ejectStackAt(this.getEntityWorld(), this.getPosition(), f, dropped);
										e = true;
										break;
									}
								}

								if (!e)
									InventoryUtils.ejectStackAt(this.getEntityWorld(), this.getPosition(), this.getFacing().getOpposite(), dropped);
							}
						}

					this.breakCounter += fp.xpCooldown;
					items.clear();
				}
			}

			if (this.getHeldItemMainhand() != null && !this.getHeldItemMainhand().isEmpty())
			{
				if (this.breakCounter >= 50)
				{
					this.breakCounter -= 50;
					this.getHeldItemMainhand().damageItem(1, this);
				}

				if (this.getHeldItemMainhand().getCount() <= 0)
					this.setHeldItem(this.getActiveHand(), ItemStack.EMPTY);
			}
			else
				this.breakCounter = 0;

			b = this.world.setBlockToAir(this.digTarget);
		}

		this.digTarget = null;
		return b;
	}

	private void findNextBlockToDig()
	{
		if (this.digTargetPrev == null || this.getDistanceSqToCenter(this.digTargetPrev) > (double) ((this.getDigRadius() + 1) * (this.getDigRadius() + 1)))
			this.digTargetPrev = new BlockPos(this);

		if (this.radInc == 0.0F)
			this.radInc = 1.0F;

		int digRadius = this.getDigRadius();
		int digDepth = this.getDigDepth();
		int x = this.digTargetPrev.getX();
		int z = this.digTargetPrev.getZ();
		int y = this.digTargetPrev.getY();
		int x2 = x + this.getFacing().getFrontOffsetX() * digDepth;
		int y2 = y + this.getFacing().getFrontOffsetY() * digDepth;
		int z2 = z + this.getFacing().getFrontOffsetZ() * digDepth;
		BlockPos end = new BlockPos(x2, y2, z2);
		RayTraceResult mop = this.world.rayTraceBlocks(new Vec3d(this.digTargetPrev).addVector(0.5D, 0.5D, 0.5D), new Vec3d(end).addVector(0.5D, 0.5D, 0.5D), false, true, false);
		if (mop != null)
		{
			Vec3d digger = new Vec3d(this.posX + (double) this.getFacing().getFrontOffsetX(), this.posY + (double) this.getEyeHeight() + (double) this.getFacing().getFrontOffsetY(), this.posZ + (double) this.getFacing().getFrontOffsetZ());
			mop = this.world.rayTraceBlocks(digger, new Vec3d(mop.getBlockPos()).addVector(0.5D, 0.5D, 0.5D), false, true, false);
			if (mop != null)
			{
				IBlockState blockState = this.world.getBlockState(mop.getBlockPos());
				if (blockState.getBlockHardness(this.world, mop.getBlockPos()) > -1.0F && blockState.getCollisionBoundingBox(this.world, mop.getBlockPos()) != null)
				{
					this.digDelay = Math.max(10 - this.getDigSpeed(blockState), (int) (blockState.getBlockHardness(this.world, mop.getBlockPos()) * 2.0F) - this.getDigSpeed(blockState) * 2);
					if (this.digDelay < 1)
						this.digDelay = 1;

					this.digDelayMax = this.digDelay;
					if (!mop.getBlockPos().equals(this.getPosition()) && !mop.getBlockPos().equals(this.getPosition().down()))
					{
						this.digTarget = mop.getBlockPos();
						return;
					}
				}
			}
		}

		while (x == this.digTargetPrev.getX() && z == this.digTargetPrev.getZ() && y == this.digTargetPrev.getY())
		{
			if (Math.abs(this.currentRadius) > (float) digRadius)
				this.currentRadius = (float) digRadius;

			this.spiral = (int) ((float) this.spiral + 3.0F + Math.max(0.0F, (10.0F - Math.abs(this.currentRadius)) * 2.0F));
			if (this.spiral >= 360)
			{
				this.spiral -= 360;
				this.currentRadius += this.radInc;
				if (this.currentRadius > (float) digRadius || this.currentRadius < (float) -digRadius)
					this.currentRadius = 0.0F;
			}

			Vec3d vsource = new Vec3d((double) (int) this.posX + 0.5D + (double) this.getFacing().getFrontOffsetX(), this.posY + (double) this.getFacing().getFrontOffsetY() + (double) this.getEyeHeight(), (double) (int) this.posZ + 0.5D + (double) this.getFacing().getFrontOffsetZ());
			Vec3d vtar = new Vec3d(0.0D, (double) this.currentRadius, 0.0D);
			vtar = Utils.rotateAroundZ(vtar, (float) this.spiral / 180.0F * 3.1415927F);
			vtar = Utils.rotateAroundY(vtar, 1.5707964F * (float) this.getFacing().getFrontOffsetX());
			vtar = Utils.rotateAroundX(vtar, 1.5707964F * (float) this.getFacing().getFrontOffsetY());
			Vec3d vres = vsource.addVector(vtar.x, vtar.y, vtar.z);
			x = MathHelper.floor(vres.x);
			y = MathHelper.floor(vres.y);
			z = MathHelper.floor(vres.z);
		}

		this.digTargetPrev = new BlockPos(x, y, z);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		try
		{
			if (source.getTrueSource() != null && this.isOwner((EntityLivingBase) source.getTrueSource()))
			{
				EnumFacing f = EnumFacing.getDirectionFromEntityLiving(this.getPosition(), (EntityLivingBase) source.getTrueSource());
				if (f != EnumFacing.DOWN)
					this.setFacing(f);

				return false;
			}
		}
		catch (Exception ignored)
		{
		}

		this.rotationYaw = (float) ((double) this.rotationYaw + this.getRNG().nextGaussian() * 45.0D);
		this.rotationPitch = (float) ((double) this.rotationPitch + this.getRNG().nextGaussian() * 20.0D);
		return super.attackEntityFrom(source, amount);
	}

	protected void rechargeVis()
	{
		this.setCharge(this.getCharge() + AuraHandler.drainVis(this.world, this.getPosition(), 10.0F, false));
	}

	@Override
	public boolean canBePushed()
	{
		return true;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}

	@Override
	public void onDeath(DamageSource cause)
	{
		super.onDeath(cause);
		if (!this.world.isRemote)
			this.dropStuff();

	}

	protected void dropStuff()
	{
		ItemStack heldItem = this.getHeldItemMainhand();
		if (heldItem != null && !heldItem.isEmpty())
			this.entityDropItem(heldItem, 0.5F);

	}

	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand)
	{
		if (player.getHeldItem(hand).getItem() instanceof ItemNameTag)
			return false;
		if (!this.world.isRemote && this.isOwner(player) && !this.isDead)
		{
			if (player.isSneaking())
			{
				this.playSound(SoundsTC.zap, 1.0F, 1.0F);
				this.dropStuff();
				this.entityDropItem(new ItemStack(ItemsTC.turretPlacer, 1, 2), 0.5F);
				this.setDead();
				player.swingArm(hand);
			}
			else
				player.openGui(Thaumcraft.instance, 14, this.world, this.getEntityId(), 0, 0);

			return true;
		}
		return super.processInteract(player, hand);
	}

	@Override
	public void knockBack(Entity p_70653_1_, float p_70653_2_, double p_70653_3_, double p_70653_5_)
	{
		super.knockBack(p_70653_1_, p_70653_2_, p_70653_3_ / 10.0D, p_70653_5_ / 10.0D);
		if (this.motionY > 0.1D)
			this.motionY = 0.1D;

	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.getDataManager().register(FACING, EnumFacing.DOWN);
		this.dataManager.register(ACTIVE, Boolean.FALSE);
	}

	public boolean isActive()
	{
		return this.dataManager.get(ACTIVE);
	}

	public void setActive(boolean attacking)
	{
		this.dataManager.set(ACTIVE, attacking);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.setCharge(nbt.getFloat("charge"));
		this.setFacing(EnumFacing.VALUES[nbt.getByte("faceing")]);
		this.setActive(nbt.getBoolean("active"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		nbt.setFloat("charge", this.getCharge());
		nbt.setByte("faceing", (byte) this.getFacing().getIndex());
		nbt.setBoolean("active", this.isActive());
	}

	public EnumFacing getFacing()
	{
		return this.getDataManager().get(FACING);
	}

	public void setFacing(EnumFacing face)
	{
		this.getDataManager().set(FACING, face);
	}

	public float getCharge()
	{
		return this.charge;
	}

	public void setCharge(float c)
	{
		this.charge = c;
	}

	@Override
	public void move(MoverType mt, double x, double y, double z)
	{
		super.move(mt, x / 5.0D, y, z / 5.0D);
	}

	@Override
	public void onKillCommand()
	{
		this.attackEntityFrom(DamageSource.OUT_OF_WORLD, 400.0F);
	}

	@Override
	protected void dropFewItems(boolean p_70628_1_, int treasure)
	{
		float b = (float) treasure * 0.15F;
		if (this.rand.nextFloat() < 0.2F + b)
			this.entityDropItem(new ItemStack(ItemsTC.mind), 0.5F);

		if (this.rand.nextFloat() < 0.2F + b)
			this.entityDropItem(new ItemStack(ItemsTC.morphicResonator), 0.5F);

		if (this.rand.nextFloat() < 0.2F + b)
			this.entityDropItem(new ItemStack(BlocksTC.crystalAir), 0.5F);

		if (this.rand.nextFloat() < 0.2F + b)
			this.entityDropItem(new ItemStack(BlocksTC.crystalEarth), 0.5F);

		if (this.rand.nextFloat() < 0.5F + b)
			this.entityDropItem(new ItemStack(ItemsTC.mechanismSimple), 0.5F);

		if (this.rand.nextFloat() < 0.5F + b)
			this.entityDropItem(new ItemStack(ItemsTC.plate), 0.5F);

		if (this.rand.nextFloat() < 0.5F + b)
			this.entityDropItem(new ItemStack(BlocksTC.plankGreatwood), 0.5F);

	}

	@Override
	public int getVerticalFaceSpeed()
	{
		return 10;
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
	public float getEyeHeight()
	{
		return 0.8125F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleStatusUpdate(byte par1)
	{
		if (par1 == 16)
			this.clientDigging = true;
		else if (par1 == 17)
			this.clientDigging = false;
		else
			super.handleStatusUpdate(par1);

	}
}
