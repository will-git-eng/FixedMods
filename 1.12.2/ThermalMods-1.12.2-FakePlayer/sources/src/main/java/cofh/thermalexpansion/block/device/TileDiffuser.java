package cofh.thermalexpansion.block.device;

import cofh.core.block.TileAugmentableSecure;
import cofh.core.fluid.FluidTankCore;
import cofh.core.network.PacketBase;
import cofh.core.util.core.SideConfig;
import cofh.core.util.core.SlotConfig;
import cofh.core.util.helpers.FluidHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.client.device.GuiDiffuser;
import cofh.thermalexpansion.gui.container.device.ContainerDiffuser;
import cofh.thermalexpansion.util.managers.device.DiffuserManager;
import cofh.thermalfoundation.init.TFFluids;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class TileDiffuser extends TileDeviceBase implements ITickable
{
	private static final int TYPE = BlockDevice.Type.DIFFUSER.getMetadata();
	public static int radiusPotion = 4;
	public static int radiusSplash = 6;
	public static int radiusLingering = 8;
	private static final int TIME_CONSTANT = 60;
	private static final int BOOST_TIME = 15;
	private static final int FLUID_AMOUNT = 50;
	private static final int MAX_AMPLIFIER = 4;
	private static final int MAX_DURATION = 7200;
	public static boolean enableParticles = true;
	private int inputTracker;
	private int boostAmp;
	private int boostDur;
	private int boostTime;
	private FluidTankCore tank = new FluidTankCore(4000);
	private FluidStack renderFluid;
	private int offset;
	private boolean forcedCycle;

	public static void initialize()
	{
		TileDeviceBase.SIDE_CONFIGS[TYPE] = new SideConfig();
		TileDeviceBase.SIDE_CONFIGS[TYPE].numConfig = 4;
		TileDeviceBase.SIDE_CONFIGS[TYPE].slotGroups = new int[][] { new int[0], { 0 }, { 0 }, new int[0] };
		TileDeviceBase.SIDE_CONFIGS[TYPE].sideTypes = new int[] { 0, 1, 5, 6 };
		TileDeviceBase.SIDE_CONFIGS[TYPE].defaultSides = new byte[] { (byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1 };
		TileDeviceBase.SLOT_CONFIGS[TYPE] = new SlotConfig();
		TileDeviceBase.SLOT_CONFIGS[TYPE].allowInsertionSlot = new boolean[] { true };
		TileDeviceBase.SLOT_CONFIGS[TYPE].allowExtractionSlot = new boolean[] { false };
		GameRegistry.registerTileEntity(TileDiffuser.class, "thermalexpansion:device_diffuser");
		config();
	}

	public static void config()
	{
		String category = "Device.Diffuser";
		BlockDevice.enable[TYPE] = ThermalExpansion.CONFIG.get(category, "Enable", true);
		category = "Device.Diffuser";
		String comment = "If TRUE, the Decoctive Diffuser will display potion effect particles.";
		enableParticles = ThermalExpansion.CONFIG_CLIENT.get(category, "EnableParticles", enableParticles, comment);
		comment = "Adjust this value to change the area effect radius when Potion fluid is used in a Decoctive Diffuser.";
		radiusPotion = ThermalExpansion.CONFIG.getConfiguration().getInt("PotionRadius", category, radiusPotion, 2, 16, comment);
		comment = "Adjust this value to change the area effect radius when Splash Potion fluid is used in a Decoctive Diffuser.";
		radiusSplash = ThermalExpansion.CONFIG.getConfiguration().getInt("SplashPotionRadius", category, radiusSplash, 2, 16, comment);
		comment = "Adjust this value to change the area effect radius when Lingering Potion fluid is used in a Decoctive Diffuser.";
		radiusLingering = ThermalExpansion.CONFIG.getConfiguration().getInt("LingeringPotionRadius", category, radiusLingering, 2, 16, comment);
	}

	public TileDiffuser()
	{
		this.inventory = new ItemStack[1];
		Arrays.fill(this.inventory, ItemStack.EMPTY);
		this.createAllSlots(this.inventory.length);
		this.offset = MathHelper.RANDOM.nextInt(60);
		this.hasAutoInput = true;
		this.enableAutoInput = true;
	}

	@Override
	public int getType()
	{
		return TYPE;
	}

	@Override
	public boolean hasClientUpdate()
	{
		return true;
	}

	@Override
	public void onRedstoneUpdate()
	{
		boolean curActive = this.isActive;
		this.isActive = this.redstoneControlOrDisable();
		if (this.isActive && !curActive && !this.forcedCycle)
		{
			this.diffuse();
			this.offset = (int) (60L - this.world.getTotalWorldTime() % 60L) - 1;
			this.forcedCycle = true;
		}

		this.updateIfChanged(curActive);
	}

	@Override
	public void update()
	{
		if (this.timeCheckOffset())
		{
			this.forcedCycle = false;
			if (ServerHelper.isClientWorld(this.world))
			{
				if (enableParticles)
					if (this.isActive)
						this.diffuseClient();
			}
			else
			{
				this.transferInput();
				boolean curActive = this.isActive;
				if (this.isActive)
				{
					this.diffuse();
					if (!this.redstoneControlOrDisable())
						this.isActive = false;
				}
				else if (this.redstoneControlOrDisable())
					this.isActive = true;

				this.updateIfChanged(curActive);
			}
		}
	}

	protected void transferInput()
	{
		if (this.getTransferIn())
			for (int i = this.inputTracker + 1; i <= this.inputTracker + 6; ++i)
			{
				int side = i % 6;
				if (SideConfig.isPrimaryInput(this.sideConfig.sideTypes[this.sideCache[side]]) && this.extractItem(0, TileAugmentableSecure.ITEM_TRANSFER[this.level], EnumFacing.VALUES[side]))
				{
					this.inputTracker = side;
					break;
				}
			}
	}

	protected void diffuseClient()
	{
		if (this.renderFluid != null)
		{
			int radius = TFFluids.isSplashPotion(this.renderFluid) ? radiusSplash : TFFluids.isLingeringPotion(this.renderFluid) ? radiusLingering : radiusPotion;
			List<PotionEffect> effects = PotionUtils.getEffectsFromTag(this.renderFluid.tag);
			int color = PotionUtils.getPotionColorFromEffectList(effects);
			int x = this.pos.getX();
			float y = (float) this.pos.getY() + 0.5F;
			int z = this.pos.getZ();

			for (int i = x - radius; i <= x + radius; ++i)
			{
				for (int k = z - radius; k <= z + radius; ++k)
				{
					this.world.spawnAlwaysVisibleParticle(EnumParticleTypes.SPELL_MOB.getParticleID(), (double) ((float) i + this.world.rand.nextFloat()), (double) y, (double) ((float) k + this.world.rand.nextFloat()), (double) ((float) (color >> 16 & 255) / 255.0F), (double) ((float) (color >> 8 & 255) / 255.0F), (double) ((float) (color & 255) / 255.0F));
				}
			}

		}
	}

	protected void diffuse()
	{
		if (this.tank.getFluidAmount() < 50)
		{
			if (this.renderFluid != null)
			{
				this.renderFluid = null;
				this.sendFluidPacket();
			}

		}
		else
		{
			if (this.boostTime <= 0)
			{
				this.boostAmp = 0;
				this.boostDur = 0;
				if (DiffuserManager.isValidReagent(this.inventory[0]))
				{
					this.boostAmp = DiffuserManager.getReagentAmplifier(this.inventory[0]);
					this.boostDur = DiffuserManager.getReagentDuration(this.inventory[0]);
					this.boostTime = 14;
					this.inventory[0].shrink(1);
					if (this.inventory[0].getCount() <= 0)
						this.inventory[0] = ItemStack.EMPTY;
				}
			}
			else
				--this.boostTime;

			FluidStack potionFluid = this.getTankFluid();
			if (!FluidHelper.isFluidEqual(potionFluid, this.renderFluid))
			{
				this.renderFluid = new FluidStack(potionFluid, 0);
				this.sendFluidPacket();
			}

			int radius = TFFluids.isSplashPotion(potionFluid) ? radiusSplash : TFFluids.isLingeringPotion(potionFluid) ? radiusLingering : radiusPotion;
			AxisAlignedBB area = new AxisAlignedBB(this.pos.add(-radius, 1 - radius, -radius), this.pos.add(1 + radius, radius, 1 + radius));
			List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, area);
			this.tank.drain(50, true);
			if (!entities.isEmpty())
			{
				List<PotionEffect> effects = PotionUtils.getEffectsFromTag(potionFluid.tag);

				if (effects.isEmpty())
					return;

				for (EntityLivingBase entity : entities)
				{
					if (entity.canBeHitWithPotion())
					{
						if (this.fake.cantAttack(entity))
							continue;

						for (PotionEffect effect : effects)
						{
							Potion potion = effect.getPotion();
							if (potion.isInstant())
								potion.affectEntity(null, null, entity, effect.getAmplifier() + this.boostAmp, 0.5D);
							else
								entity.addPotionEffect(new PotionEffect(potion, Math.min(effect.getDuration() / 4 * (1 + this.boostDur), 7200), Math.min(effect.getAmplifier() + this.boostAmp, 4), effect.getIsAmbient(), effect.doesShowParticles()));
						}
					}
				}

			}
		}
	}

	protected boolean timeCheckOffset()
	{
		return (this.world.getTotalWorldTime() + (long) this.offset) % 60L == 0L;
	}

	@Override
	public Object getGuiClient(InventoryPlayer inventory)
	{
		return new GuiDiffuser(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory)
	{
		return new ContainerDiffuser(inventory, this);
	}

	@Override
	public int getScaledSpeed(int scale)
	{
		return !this.isActive ? 0 : MathHelper.round((double) (scale * this.boostTime / 15));
	}

	@Override
	public FluidTankCore getTank()
	{
		return this.tank;
	}

	@Override
	public FluidStack getTankFluid()
	{
		return this.tank.getFluid();
	}

	public int getBoostAmp()
	{
		return this.boostAmp;
	}

	public int getBoostDur()
	{
		return this.boostDur;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.inputTracker = nbt.getInteger("TrackIn");
		this.tank.readFromNBT(nbt);
		this.boostAmp = nbt.getInteger("BoostAmp");
		this.boostDur = nbt.getInteger("BoostDur");
		this.boostTime = nbt.getInteger("BoostTime");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("TrackIn", this.inputTracker);
		this.tank.writeToNBT(nbt);
		nbt.setInteger("BoostAmp", this.boostAmp);
		nbt.setInteger("BoostDur", this.boostDur);
		nbt.setInteger("BoostTime", this.boostTime);
		return nbt;
	}

	@Override
	public PacketBase getFluidPacket()
	{
		PacketBase payload = super.getFluidPacket();
		payload.addFluidStack(this.renderFluid);
		return payload;
	}

	@Override
	public PacketBase getGuiPacket()
	{
		PacketBase payload = super.getGuiPacket();
		payload.addInt(this.boostAmp);
		payload.addInt(this.boostDur);
		payload.addInt(this.boostTime);
		payload.addFluidStack(this.tank.getFluid());
		return payload;
	}

	@Override
	public PacketBase getTilePacket()
	{
		PacketBase payload = super.getTilePacket();
		payload.addInt(this.offset);
		if (this.tank.getFluid() == null)
			payload.addFluidStack(this.renderFluid);
		else
			payload.addFluidStack(this.tank.getFluid());

		return payload;
	}

	@Override
	protected void handleFluidPacket(PacketBase payload)
	{
		super.handleFluidPacket(payload);
		this.renderFluid = payload.getFluidStack();
		this.callBlockUpdate();
	}

	@Override
	protected void handleGuiPacket(PacketBase payload)
	{
		super.handleGuiPacket(payload);
		this.boostAmp = payload.getInt();
		this.boostDur = payload.getInt();
		this.boostTime = payload.getInt();
		this.tank.setFluid(payload.getFluidStack());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleTilePacket(PacketBase payload)
	{
		super.handleTilePacket(payload);
		this.offset = payload.getInt();
		this.renderFluid = payload.getFluidStack();
	}

	protected static boolean isValidPotion(FluidStack stack)
	{
		return stack != null && (TFFluids.isPotion(stack) || TFFluids.isSplashPotion(stack) || TFFluids.isLingeringPotion(stack));
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return DiffuserManager.isValidReagent(stack);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing from)
	{
		return super.hasCapability(capability, from) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, final EnumFacing from)
	{
		return (T) (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler()
		{
			@Override
			public IFluidTankProperties[] getTankProperties()
			{
				FluidTankInfo info = TileDiffuser.this.tank.getInfo();
				return new IFluidTankProperties[] { new FluidTankProperties(info.fluid, info.capacity, true, true) };
			}

			@Override
			public int fill(FluidStack resource, boolean doFill)
			{
				return !TileDiffuser.isValidPotion(resource) || from != null && !SideConfig.allowInsertion(TileDiffuser.access$100(TileDiffuser.this).sideTypes[TileDiffuser.super.sideCache[from.ordinal()]]) ? 0 : TileDiffuser.this.tank.fill(resource, doFill);
			}

			@Override
			@Nullable
			public FluidStack drain(FluidStack resource, boolean doDrain)
			{
				return from != null && !SideConfig.allowExtraction(TileDiffuser.access$200(TileDiffuser.this).sideTypes[TileDiffuser.super.sideCache[from.ordinal()]]) ? null : TileDiffuser.this.tank.drain(resource, doDrain);
			}

			@Override
			@Nullable
			public FluidStack drain(int maxDrain, boolean doDrain)
			{
				return from != null && !SideConfig.allowExtraction(TileDiffuser.access$300(TileDiffuser.this).sideTypes[TileDiffuser.super.sideCache[from.ordinal()]]) ? null : TileDiffuser.this.tank.drain(maxDrain, doDrain);
			}
		}) : super.getCapability(capability, from));
	}

	// $FF: synthetic method
	static SideConfig access$100(TileDiffuser x0)
	{
		return x0.sideConfig;
	}

	// $FF: synthetic method
	static SideConfig access$200(TileDiffuser x0)
	{
		return x0.sideConfig;
	}

	// $FF: synthetic method
	static SideConfig access$300(TileDiffuser x0)
	{
		return x0.sideConfig;
	}
}
