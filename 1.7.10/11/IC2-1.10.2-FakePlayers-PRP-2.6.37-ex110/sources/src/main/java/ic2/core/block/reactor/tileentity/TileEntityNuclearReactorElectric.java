package ic2.core.block.reactor.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.Level;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.api.recipe.RecipeOutput;
import ic2.core.ContainerBase;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotReactor;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.block.reactor.gui.GuiNuclearReactor;
import ic2.core.block.type.ResourceBlock;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.item.reactor.ItemReactorHeatStorage;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.WorldSearchUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityNuclearReactorElectric extends TileEntityInventory implements IHasGui, IReactor, IEnergySource, IMetaDelegate, IFluidHandler, IGuiValueProvider
{
	public AudioSource audioSourceMain;
	public AudioSource audioSourceGeiger;
	private float lastOutput = 0.0F;
	public final FluidTank inputTank = new FluidTank(10000);
	public final FluidTank outputTank = new FluidTank(10000);
	private final List<IEnergyTile> subTiles = new ArrayList();
	public final InvSlotReactor reactorSlot = new InvSlotReactor(this, "reactor", 54);
	public final InvSlotOutput coolantoutputSlot;
	public final InvSlotOutput hotcoolantoutputSlot;
	public final InvSlotConsumableLiquidByList coolantinputSlot = new InvSlotConsumableLiquidByList(this, "coolantinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, new Fluid[] { FluidName.coolant.getInstance() });
	public final InvSlotConsumableLiquidByTank hotcoolinputSlot;
	public final Redstone redstone;
	public float output = 0.0F;
	public int updateTicker = IC2.random.nextInt(this.getTickRate());
	public int heat = 0;
	public int maxHeat = 10000;
	public float hem = 1.0F;
	private int EmitHeatbuffer = 0;
	public int EmitHeat = 0;
	private boolean fluidCooled = false;
	public boolean addedToEnergyNet = false;
	private static final float huOutputModifier = 2.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/FluidReactor/outputModifier");

	public TileEntityNuclearReactorElectric()
	{
		this.hotcoolinputSlot = new InvSlotConsumableLiquidByTank(this, "hotcoolinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Fill, this.outputTank);
		this.coolantoutputSlot = new InvSlotOutput(this, "coolantoutputSlot", 1);
		this.hotcoolantoutputSlot = new InvSlotOutput(this, "hotcoolantoutputSlot", 1);
		this.redstone = this.addComponent(new Redstone(this));
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.worldObj.isRemote && !this.isFluidCooled())
		{
			this.refreshChambers();
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			this.addedToEnergyNet = true;
		}

		this.createChamberRedstoneLinks();
		if (this.isFluidCooled())
			this.createCasingRedstoneLinks();

	}

	@Override
	protected void onUnloaded()
	{
		if (IC2.platform.isRendering())
		{
			IC2.audioManager.removeSources(this);
			this.audioSourceMain = null;
			this.audioSourceGeiger = null;
		}

		if (IC2.platform.isSimulating() && this.addedToEnergyNet)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			this.addedToEnergyNet = false;
		}

		super.onUnloaded();
	}

	public int gaugeHeatScaled(int i)
	{
		return i * this.heat / (this.maxHeat / 100 * 85);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.heat = nbt.getInteger("heat");
		this.inputTank.readFromNBT(nbt.getCompoundTag("inputTank"));
		this.outputTank.readFromNBT(nbt.getCompoundTag("outputTank"));
		this.output = nbt.getShort("output");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagCompound inputTankTag = new NBTTagCompound();
		this.inputTank.writeToNBT(inputTankTag);
		nbt.setTag("inputTank", inputTankTag);
		NBTTagCompound outputTankTag = new NBTTagCompound();
		this.outputTank.writeToNBT(outputTankTag);
		nbt.setTag("outputTank", outputTankTag);
		nbt.setInteger("heat", this.heat);
		nbt.setShort("output", (short) (int) this.getReactorEnergyOutput());
		return nbt;
	}

	@Override
	protected void onNeighborChange(Block neighbor)
	{
		super.onNeighborChange(neighbor);
		if (this.addedToEnergyNet)
			this.refreshChambers();

	}

	@Override
	public void drawEnergy(double amount)
	{
	}

	public float sendEnergy(float send)
	{
		return 0.0F;
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction)
	{
		return true;
	}

	@Override
	public double getOfferedEnergy()
	{
		return this.getReactorEnergyOutput() * 5.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/nuclear");
	}

	@Override
	public int getSourceTier()
	{
		return 4;
	}

	@Override
	public double getReactorEUEnergyOutput()
	{
		return this.getOfferedEnergy();
	}

	@Override
	public List<IEnergyTile> getSubTiles()
	{
		return this.subTiles;
	}

	private void processfluidsSlots()
	{
		RecipeOutput outputinputSlot = this.processInputSlot(true);
		if (outputinputSlot != null)
		{
			this.processInputSlot(false);
			List<ItemStack> processResult = outputinputSlot.items;
			this.coolantoutputSlot.add(processResult);
		}

		RecipeOutput outputoutputSlot = this.processOutputSlot(true);
		if (outputoutputSlot != null)
		{
			this.processOutputSlot(false);
			List<ItemStack> processResult = outputoutputSlot.items;
			this.hotcoolantoutputSlot.add(processResult);
		}

	}

	public void refreshChambers()
	{
		List<IEnergyTile> newSubTiles = new ArrayList();
		newSubTiles.add(this);

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			TileEntity te = this.worldObj.getTileEntity(this.pos.offset(dir));
			if (te instanceof TileEntityReactorChamberElectric && !te.isInvalid())
				newSubTiles.add((TileEntityReactorChamberElectric) te);
		}

		if (!newSubTiles.equals(this.subTiles))
		{
			if (this.addedToEnergyNet)
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));

			this.subTiles.clear();
			this.subTiles.addAll(newSubTiles);
			if (this.addedToEnergyNet)
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
		}

	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.updateTicker++ % this.getTickRate() == 0)
		{
			if (!this.worldObj.isAreaLoaded(this.pos, 8))
				this.output = 0.0F;
			else
			{
				boolean toFluidCooled = this.isFluidReactor();
				if (this.fluidCooled != toFluidCooled)
				{
					if (toFluidCooled)
						this.enableFluidMode();
					else
						this.disableFluidMode();

					this.fluidCooled = toFluidCooled;
				}

				this.dropAllUnfittingStuff();
				this.output = 0.0F;
				this.maxHeat = 10000;
				this.hem = 1.0F;
				this.processChambers();
				if (this.fluidCooled)
				{
					this.processfluidsSlots();
					int huOtput = (int) (huOutputModifier * this.EmitHeatbuffer);
					int outputroom = this.outputTank.getCapacity() - this.outputTank.getFluidAmount();
					if (outputroom > 0)
					{
						FluidStack draincoolant;
						if (huOtput < outputroom)
							draincoolant = this.inputTank.drain(huOtput, false);
						else
							draincoolant = this.inputTank.drain(outputroom, false);

						if (draincoolant != null)
						{
							this.EmitHeat = draincoolant.amount;
							huOtput -= this.inputTank.drain(draincoolant.amount, true).amount;
							this.outputTank.fill(new FluidStack(FluidName.hot_coolant.getInstance(), draincoolant.amount), true);
						}
						else
							this.EmitHeat = 0;
					}
					else
						this.EmitHeat = 0;

					this.addHeat(huOtput / 2);
				}

				this.EmitHeatbuffer = 0;
				if (this.calculateHeatEffects())
					return;

				this.setActive(this.heat >= 1000 || this.output > 0.0F);
				this.markDirty();
			}

			IC2.network.get(true).updateTileEntityField(this, "output");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		showHeatEffects(this.worldObj, this.pos, this.heat);
	}

	public static void showHeatEffects(World world, BlockPos pos, int heat)
	{
		Random rnd = world.rand;
		if (rnd.nextInt(8) == 0)
		{
			int puffs = heat / 1000;
			if (puffs > 0)
			{
				puffs = rnd.nextInt(puffs);

				for (int n = 0; n < puffs; ++n)
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + rnd.nextFloat(), pos.getY() + 0.95F, pos.getZ() + rnd.nextFloat(), 0.0D, 0.0D, 0.0D, new int[0]);

				puffs = puffs - (rnd.nextInt(4) + 3);

				for (int n = 0; n < puffs; ++n)
					world.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + rnd.nextFloat(), pos.getY() + 1, pos.getZ() + rnd.nextFloat(), 0.0D, 0.0D, 0.0D, new int[0]);
			}

		}
	}

	public void dropAllUnfittingStuff()
	{
		for (int i = 0; i < this.reactorSlot.size(); ++i)
		{
			ItemStack stack = this.reactorSlot.get(i);
			if (stack != null && !this.isUsefulItem(stack, false))
			{
				this.reactorSlot.put(i, (ItemStack) null);
				this.eject(stack);
			}
		}

		for (int i = this.reactorSlot.size(); i < this.reactorSlot.rawSize(); ++i)
		{
			ItemStack stack = this.reactorSlot.get(i);
			this.reactorSlot.put(i, (ItemStack) null);
			this.eject(stack);
		}

	}

	public boolean isUsefulItem(ItemStack stack, boolean forInsertion)
	{
		Item item = stack.getItem();
		return item == null ? false : forInsertion && this.fluidCooled && item.getClass() == ItemReactorHeatStorage.class && ((ItemReactorHeatStorage) item).getCustomDamage(stack) > 0 ? false : item instanceof IReactorComponent ? true : item == ItemName.tritium_fuel_rod.getInstance() || StackUtil.checkItemEquality(stack, ItemName.nuclear.getItemStack(NuclearResourceType.depleted_uranium)) || StackUtil.checkItemEquality(stack, ItemName.nuclear.getItemStack(NuclearResourceType.depleted_dual_uranium)) || StackUtil.checkItemEquality(stack, ItemName.nuclear.getItemStack(NuclearResourceType.depleted_quad_uranium)) || StackUtil.checkItemEquality(stack, ItemName.nuclear.getItemStack(NuclearResourceType.depleted_mox)) || StackUtil.checkItemEquality(stack, ItemName.nuclear.getItemStack(NuclearResourceType.depleted_dual_mox)) || StackUtil.checkItemEquality(stack, ItemName.nuclear.getItemStack(NuclearResourceType.depleted_quad_mox));
	}

	public void eject(ItemStack drop)
	{
		if (IC2.platform.isSimulating() && drop != null)
			StackUtil.dropAsEntity(this.worldObj, this.pos, drop);
	}

	public boolean calculateHeatEffects()
	{
		if (this.heat >= 4000 && IC2.platform.isSimulating() && ConfigUtil.getFloat(MainConfig.get(), "protection/reactorExplosionPowerLimit") > 0.0F)
		{
			float power = (float) this.heat / (float) this.maxHeat;
			if (power >= 1.0F)
			{
				this.explode();
				return true;
			}
			else
			{
				if (power >= 0.85F && this.worldObj.rand.nextFloat() <= 0.2F * this.hem)
				{
					BlockPos coord = this.getRandCoord(2);
					IBlockState state = this.worldObj.getBlockState(coord);
					Block block = state.getBlock();
					if (block.isAir(state, this.worldObj, coord))
						this.worldObj.setBlockState(coord, Blocks.FIRE.getDefaultState());
					else if (state.getBlockHardness(this.worldObj, coord) >= 0.0F && this.worldObj.getTileEntity(coord) == null)
					{
						Material mat = state.getMaterial();
						if (mat != Material.ROCK && mat != Material.IRON && mat != Material.LAVA && mat != Material.GROUND && mat != Material.CLAY)
							this.worldObj.setBlockState(coord, Blocks.FIRE.getDefaultState());
						else
							this.worldObj.setBlockState(coord, Blocks.FLOWING_LAVA.getDefaultState());
					}
				}

				if (power >= 0.7F)
					for (EntityLivingBase entity : this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.pos.getX() - 3, this.pos.getY() - 3, this.pos.getZ() - 3, this.pos.getX() + 4, this.pos.getY() + 4, this.pos.getZ() + 4)))
						entity.attackEntityFrom(IC2DamageSource.radiation, (int) (this.worldObj.rand.nextInt(4) * this.hem));

				if (power >= 0.5F && this.worldObj.rand.nextFloat() <= this.hem)
				{
					BlockPos coord = this.getRandCoord(2);
					IBlockState state = this.worldObj.getBlockState(coord);
					if (state.getMaterial() == Material.WATER)
						this.worldObj.setBlockToAir(coord);
				}

				if (power >= 0.4F && this.worldObj.rand.nextFloat() <= this.hem)
				{
					BlockPos coord = this.getRandCoord(2);
					if (this.worldObj.getTileEntity(coord) == null)
					{
						IBlockState state = this.worldObj.getBlockState(coord);
						Material mat = state.getMaterial();
						if (mat == Material.WOOD || mat == Material.LEAVES || mat == Material.CLOTH)
							this.worldObj.setBlockState(coord, Blocks.FIRE.getDefaultState());
					}
				}

				return false;
			}
		}
		else
			return false;
	}

	public BlockPos getRandCoord(int radius)
	{
		if (radius <= 0)
			return null;
		else
		{
			BlockPos ret;
			while (true)
			{
				ret = this.pos.add(this.worldObj.rand.nextInt(2 * radius + 1) - radius, this.worldObj.rand.nextInt(2 * radius + 1) - radius, this.worldObj.rand.nextInt(2 * radius + 1) - radius);
				if (!ret.equals(this.pos))
					break;
			}

			return ret;
		}
	}

	public void processChambers()
	{
		int size = this.getReactorSize();

		for (int pass = 0; pass < 2; ++pass)
			for (int y = 0; y < 6; ++y)
				for (int x = 0; x < size; ++x)
				{
					ItemStack stack = this.reactorSlot.get(x, y);
					if (stack != null && stack.getItem() instanceof IReactorComponent)
					{
						IReactorComponent comp = (IReactorComponent) stack.getItem();
						comp.processChamber(stack, this, x, y, pass == 0);
					}
				}

	}

	@Override
	public boolean produceEnergy()
	{
		return this.redstone.hasRedstoneInput() && ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/nuclear") > 0.0F;
	}

	public int getReactorSize()
	{
		if (this.worldObj == null)
			return 9;
		else
		{
			int cols = 3;

			for (EnumFacing dir : EnumFacing.VALUES)
			{
				TileEntity target = this.worldObj.getTileEntity(this.pos.offset(dir));
				if (target instanceof TileEntityReactorChamberElectric)
					++cols;
			}

			return cols;
		}
	}

	private boolean isFullSize()
	{
		return this.getReactorSize() == 9;
	}

	@Override
	public int getTickRate()
	{
		return 20;
	}

	@Override
	protected boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return StackUtil.checkItemEquality(heldItem, BlockName.te.getItemStack(TeBlock.reactor_chamber)) ? false : super.onActivated(player, hand, heldItem, side, hitX, hitY, hitZ);
	}

	@Override
	public ContainerBase<TileEntityNuclearReactorElectric> getGuiContainer(EntityPlayer player)
	{
		return new ContainerNuclearReactor(player, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiNuclearReactor(new ContainerNuclearReactor(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("output"))
		{
			if (this.output > 0.0F)
			{
				if (this.lastOutput <= 0.0F)
				{
					if (this.audioSourceMain == null)
						this.audioSourceMain = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/NuclearReactorLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());

					if (this.audioSourceMain != null)
						this.audioSourceMain.play();
				}

				if (this.output < 40.0F)
				{
					if (this.lastOutput <= 0.0F || this.lastOutput >= 40.0F)
					{
						if (this.audioSourceGeiger != null)
							this.audioSourceGeiger.remove();

						this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerLowEU.ogg", true, false, IC2.audioManager.getDefaultVolume());
						if (this.audioSourceGeiger != null)
							this.audioSourceGeiger.play();
					}
				}
				else if (this.output < 80.0F)
				{
					if (this.lastOutput < 40.0F || this.lastOutput >= 80.0F)
					{
						if (this.audioSourceGeiger != null)
							this.audioSourceGeiger.remove();

						this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerMedEU.ogg", true, false, IC2.audioManager.getDefaultVolume());
						if (this.audioSourceGeiger != null)
							this.audioSourceGeiger.play();
					}
				}
				else if (this.output >= 80.0F && this.lastOutput < 80.0F)
				{
					if (this.audioSourceGeiger != null)
						this.audioSourceGeiger.remove();

					this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerHighEU.ogg", true, false, IC2.audioManager.getDefaultVolume());
					if (this.audioSourceGeiger != null)
						this.audioSourceGeiger.play();
				}
			}
			else if (this.lastOutput > 0.0F)
			{
				if (this.audioSourceMain != null)
					this.audioSourceMain.stop();

				if (this.audioSourceGeiger != null)
					this.audioSourceGeiger.stop();
			}

			this.lastOutput = this.output;
		}

		super.onNetworkUpdate(field);
	}

	@Override
	public TileEntity getCoreTe()
	{
		return this;
	}

	@Override
	public World getReactorWorld()
	{
		return this.worldObj;
	}

	@Override
	public BlockPos getReactorPos()
	{
		return this.pos;
	}

	@Override
	public int getHeat()
	{
		return this.heat;
	}

	@Override
	public void setHeat(int heat)
	{
		this.heat = heat;
	}

	@Override
	public int addHeat(int amount)
	{
		this.heat += amount;
		return this.heat;
	}

	@Override
	public ItemStack getItemAt(int x, int y)
	{
		return x >= 0 && x < this.getReactorSize() && y >= 0 && y < 6 ? this.reactorSlot.get(x, y) : null;
	}

	@Override
	public void setItemAt(int x, int y, ItemStack item)
	{
		if (x >= 0 && x < this.getReactorSize() && y >= 0 && y < 6)
			this.reactorSlot.put(x, y, item);
	}

	@Override
	public void explode()
	{
		float boomPower = 10.0F;
		float boomMod = 1.0F;

		for (int i = 0; i < this.reactorSlot.size(); ++i)
		{
			ItemStack stack = this.reactorSlot.get(i);
			if (stack != null && stack.getItem() instanceof IReactorComponent)
			{
				float f = ((IReactorComponent) stack.getItem()).influenceExplosion(stack, this);
				if (f > 0.0F && f < 1.0F)
					boomMod *= f;
				else
					boomPower += f;
			}

			this.reactorSlot.put(i, (ItemStack) null);
		}

		boomPower = boomPower * this.hem * boomMod;
		IC2.log.log(LogCategory.PlayerActivity, Level.INFO, "Nuclear Reactor at %s melted (raw explosion power %f)", new Object[] { Util.formatPosition(this), Float.valueOf(boomPower) });
		boomPower = Math.min(boomPower, ConfigUtil.getFloat(MainConfig.get(), "protection/reactorExplosionPowerLimit"));

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			TileEntity target = this.worldObj.getTileEntity(this.pos.offset(dir));
			if (target instanceof TileEntityReactorChamberElectric)
				this.worldObj.setBlockToAir(target.getPos());
		}

		this.worldObj.setBlockToAir(this.pos);
    
    

		explosion.doExplosion();
	}

	@Override
	public void addEmitHeat(int heat)
	{
		this.EmitHeatbuffer += heat;
	}

	@Override
	public int getMaxHeat()
	{
		return this.maxHeat;
	}

	@Override
	public void setMaxHeat(int newMaxHeat)
	{
		this.maxHeat = newMaxHeat;
	}

	@Override
	public float getHeatEffectModifier()
	{
		return this.hem;
	}

	@Override
	public void setHeatEffectModifier(float newHEM)
	{
		this.hem = newHEM;
	}

	@Override
	public float getReactorEnergyOutput()
	{
		return this.output;
	}

	@Override
	public float addOutput(float energy)
	{
		return this.output += energy;
	}

	private RecipeOutput processInputSlot(boolean simulate)
	{
		if (!this.coolantinputSlot.isEmpty())
		{
			MutableObject<ItemStack> output = new MutableObject();
			if (this.coolantinputSlot.transferToTank(this.inputTank, output, simulate) && (output.getValue() == null || this.coolantoutputSlot.canAdd(output.getValue())))
			{
				if (output.getValue() == null)
					return new RecipeOutput((NBTTagCompound) null, new ItemStack[0]);

				return new RecipeOutput((NBTTagCompound) null, new ItemStack[] { output.getValue() });
			}
		}

		return null;
	}

	private RecipeOutput processOutputSlot(boolean simulate)
	{
		if (!this.hotcoolinputSlot.isEmpty())
		{
			MutableObject<ItemStack> output = new MutableObject();
			if (this.hotcoolinputSlot.transferFromTank(this.outputTank, output, simulate) && (output.getValue() == null || this.hotcoolantoutputSlot.canAdd(output.getValue())))
			{
				if (output.getValue() == null)
					return new RecipeOutput((NBTTagCompound) null, new ItemStack[0]);

				return new RecipeOutput((NBTTagCompound) null, new ItemStack[] { output.getValue() });
			}
		}

		return null;
	}

	@Override
	public boolean isFluidCooled()
	{
		return this.fluidCooled;
	}

	private void createChamberRedstoneLinks()
	{
		for (EnumFacing facing : EnumFacing.VALUES)
		{
			BlockPos cPos = this.pos.offset(facing);
			TileEntity te = this.worldObj.getTileEntity(cPos);
			if (te instanceof TileEntityReactorChamberElectric)
			{
				TileEntityReactorChamberElectric chamber = (TileEntityReactorChamberElectric) te;
				chamber.redstone.linkTo(this.redstone);
			}
		}

	}

	private void createCasingRedstoneLinks()
	{
		WorldSearchUtil.findTileEntities(this.worldObj, this.pos, 2, new WorldSearchUtil.ITileEntityResultHandler()
		{
			@Override
			public boolean onMatch(TileEntity te)
			{
				if (te instanceof TileEntityReactorRedstonePort)
					((TileEntityReactorRedstonePort) te).redstone.linkTo(TileEntityNuclearReactorElectric.this.redstone);

				return false;
			}
		});
	}

	private void removeCasingRedstoneLinks()
	{
		for (Redstone rs : this.redstone.getLinkedOrigins())
			if (rs.getParent() instanceof TileEntityReactorRedstonePort)
				rs.unlinkOutbound();

	}

	private void enableFluidMode()
	{
		if (this.addedToEnergyNet)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			this.addedToEnergyNet = false;
		}

		this.createCasingRedstoneLinks();
	}

	private void disableFluidMode()
	{
		if (!this.addedToEnergyNet)
		{
			this.refreshChambers();
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			this.addedToEnergyNet = true;
		}

		this.removeCasingRedstoneLinks();
	}

	private boolean isFluidReactor()
	{
		if (!this.isFullSize())
			return false;
		else if (!this.hasFluidChamber())
			return false;
		else
		{
			int range = 2;
			final MutableBoolean foundConflict = new MutableBoolean();
			WorldSearchUtil.findTileEntities(this.worldObj, this.pos, 4, new WorldSearchUtil.ITileEntityResultHandler()
			{
				@Override
				public boolean onMatch(TileEntity te)
				{
					if (!(te instanceof TileEntityNuclearReactorElectric))
						return false;
					else if (te == TileEntityNuclearReactorElectric.this)
						return false;
					else
					{
						TileEntityNuclearReactorElectric reactor = (TileEntityNuclearReactorElectric) te;
						if (reactor.isFullSize() && reactor.hasFluidChamber())
						{
							foundConflict.setTrue();
							return true;
						}
						else
							return false;
					}
				}
			});
			return !foundConflict.getValue().booleanValue();
		}
	}

	private boolean hasFluidChamber()
	{
		int range = 2;
		ChunkCache cache = new ChunkCache(this.worldObj, this.pos.add(-2, -2, -2), this.pos.add(2, 2, 2), 0);
		MutableBlockPos cPos = new MutableBlockPos();

		for (int i = 0; i < 2; ++i)
		{
			int y = this.pos.getY() + 2 * (i * 2 - 1);

			for (int z = this.pos.getZ() - 2; z <= this.pos.getZ() + 2; ++z)
				for (int x = this.pos.getX() - 2; x <= this.pos.getX() + 2; ++x)
				{
					cPos.setPos(x, y, z);
					if (!isFluidChamberBlock(cache, cPos))
						return false;
				}
		}

		for (int i = 0; i < 2; ++i)
		{
			int z = this.pos.getZ() + 2 * (i * 2 - 1);

			for (int y = this.pos.getY() - 2 + 1; y <= this.pos.getY() + 2 - 1; ++y)
				for (int x = this.pos.getX() - 2; x <= this.pos.getX() + 2; ++x)
				{
					cPos.setPos(x, y, z);
					if (!isFluidChamberBlock(cache, cPos))
						return false;
				}
		}

		for (int i = 0; i < 2; ++i)
		{
			int x = this.pos.getX() + 2 * (i * 2 - 1);

			for (int y = this.pos.getY() - 2 + 1; y <= this.pos.getY() + 2 - 1; ++y)
				for (int z = this.pos.getZ() - 2 + 1; z <= this.pos.getZ() + 2 - 1; ++z)
				{
					cPos.setPos(x, y, z);
					if (!isFluidChamberBlock(cache, cPos))
						return false;
				}
		}

		return true;
	}

	private static boolean isFluidChamberBlock(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if (state == BlockName.resource.getBlockState(ResourceBlock.reactor_vessel))
			return true;
		else if (state.getBlock() != BlockName.te.getInstance())
			return false;
		else
		{
			TileEntity te = world.getTileEntity(pos);
			return te == null ? false : te instanceof TileEntityReactorAccessHatch || te instanceof TileEntityReactorFluidPort || te instanceof TileEntityReactorRedstonePort;
		}
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("heat".equals(name))
			return this.maxHeat == 0 ? 0.0D : (double) this.heat / (double) this.maxHeat;
		else
			throw new IllegalArgumentException("Invalid value: " + name);
	}

	public int gaugeLiquidScaled(int i, int tank)
	{
		switch (tank)
		{
			case 0:
				if (this.inputTank.getFluidAmount() <= 0)
					return 0;

				return this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
			case 1:
				if (this.outputTank.getFluidAmount() <= 0)
					return 0;

				return this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
			default:
				return 0;
		}
	}

	public FluidTank getinputtank()
	{
		return this.inputTank;
	}

	public FluidTank getoutputtank()
	{
		return this.outputTank;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return new FluidTankInfo[] { this.inputTank.getInfo(), this.outputTank.getInfo() };
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return !this.fluidCooled ? false : fluid == FluidName.coolant.getInstance();
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		if (!this.fluidCooled)
			return false;
		else
		{
			FluidStack fluidStack = this.outputTank.getFluid();
			return fluidStack == null ? false : fluidStack.isFluidEqual(new FluidStack(fluid, 1));
		}
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		return !this.canFill(from, resource.getFluid()) ? 0 : this.inputTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		return resource != null && resource.isFluidEqual(this.outputTank.getFluid()) ? !this.canDrain(from, resource.getFluid()) ? null : this.outputTank.drain(resource.amount, doDrain) : null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		return this.outputTank.drain(maxDrain, doDrain);
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}
}
