package ic2.core.block.generator.tileentity;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;

import ic2.api.Direction;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.api.reactor.IReactorProduct;
import ic2.core.ContainerIC2;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.IHasGui;
import ic2.core.Ic2Items;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.generator.container.ContainerNuclearReactor;
import ic2.core.block.machine.tileentity.TileEntityMachine;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public abstract class TileEntityNuclearReactor extends TileEntityMachine implements IHasGui, IReactor
{
	public static Random randomizer = new Random();
	private static Direction[] directions = Direction.values();
	public int output = 0;
	public int updateTicker;
	public int heat = 0;
	public int maxHeat = 10000;
	public float hem = 1.0F;
	public boolean redstonePowered = false;
	public boolean reactorPower = false;
	public AudioSource audioSourceMain = null;
	public AudioSource audioSourceGeiger = null;
	private int lastOutput = 0;
	private static int[][] slots = new int[10][];
	public int size = 3;
	public long lastCheck = -1L;
	public boolean hasAddedSomething = false;
	public boolean refreshRequest = false;

	public TileEntityNuclearReactor()
	{
		super(54);
		this.updateTicker = randomizer.nextInt(this.getTickRate());
	}

	@Override
	public void onUnloaded()
	{
		if (this.isRendering())
		{
			IC2.audioManager.removeSources(this);
			this.audioSourceMain = null;
			this.audioSourceGeiger = null;
		}

		super.onUnloaded();
	}

	@Override
	public String getInventoryName()
	{
		return "Nuclear Reactor";
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);

		try
		{
			this.output = nbttagcompound.getInteger("output");
		}
		catch (Exception var3)
		{
			this.output = nbttagcompound.getShort("output");
		}

		this.heat = nbttagcompound.getInteger("heat");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("heat", this.heat);
		nbttagcompound.setInteger("output", this.output);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (this.updateTicker++ % this.getTickRate() == 0)
		{
			if (!this.worldObj.doChunksNearChunkExist(this.xCoord, this.yCoord, this.zCoord, 2))
				this.output = 0;
			else
			{
				this.reactorPower = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
				this.dropAllUnfittingStuff();
				this.output = 0;
				this.maxHeat = 10000;
				this.hem = 1.0F;
				this.processChambers();
				if (this.calculateHeatEffects())
					return;

				this.setActive(this.heat >= 1000 || this.output > 0);
				this.getNetwork().updateTileEntityField(this, "heat");
				this.markDirty();
			}

			this.getNetwork().updateTileGuiField(this, "output");
		}
	}

	public void dropAllUnfittingStuff()
	{
		int size = this.getReactorSize();
		if (this.hasAddedSomething)
		{
			this.hasAddedSomething = false;

			for (int x = 0; x < 9; ++x)
				for (int y = 0; y < 6; ++y)
				{
					ItemStack stack = this.getMatrixCoord(x, y);
					if (stack != null)
						if (stack.stackSize <= 0)
							this.setMatrixCoord(x, y, (ItemStack) null);
						else if (x >= size || !this.isUsefulItem(stack))
						{
							this.eject(stack);
							this.setMatrixCoord(x, y, (ItemStack) null);
						}
				}
		}
	}

	public boolean isUsefulItem(ItemStack item)
	{
		if (item == null)
			return false;
		else
		{
			Item id = item.getItem();
			return !(id instanceof IReactorComponent) && (!(id instanceof IReactorProduct) || !((IReactorProduct) id).isProduct(item)) ? id == Ic2Items.reEnrichedUraniumCell.getItem() || id == Ic2Items.nearDepletedUraniumCell.getItem() : true;
		}
	}

	public static boolean isUsefullReactorItem(ItemStack item)
	{
		if (item == null)
			return false;
		else
		{
			Item id = item.getItem();
			return !(id instanceof IReactorComponent) && (!(id instanceof IReactorProduct) || !((IReactorProduct) id).isProduct(item)) ? id == Ic2Items.reEnrichedUraniumCell.getItem() || id == Ic2Items.nearDepletedUraniumCell.getItem() : true;
		}
	}

	public void eject(ItemStack drop)
	{
		if (this.isSimulating() && drop != null)
		{
			float f = 0.7F;
			double d = this.worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double d2 = this.worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double d3 = this.worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			EntityItem entityitem = new EntityItem(this.worldObj, this.xCoord + d, this.yCoord + d2, this.zCoord + d3, drop);
			entityitem.delayBeforeCanPickup = 10;
			this.worldObj.spawnEntityInWorld(entityitem);
		}
	}

	public boolean calculateHeatEffects()
	{
		if (this.heat >= 4000 && this.isSimulating() && IC2.explosionPowerReactorMax > 0.0F)
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
					int[] coord = this.getRandCoord(2);
					if (coord != null)
					{
						Block id = this.worldObj.getBlock(coord[0], coord[1], coord[2]);
						if (id == null)
							this.worldObj.setBlock(coord[0], coord[1], coord[2], Blocks.fire);
						else if (id.getBlockHardness(this.worldObj, coord[0], coord[1], coord[2]) <= -1.0F)
						{
							Material mat = id.getMaterial();
							if (mat != Material.rock && mat != Material.iron && mat != Material.lava && mat != Material.ground && mat != Material.clay)
								this.worldObj.setBlock(coord[0], coord[1], coord[2], Blocks.fire);
							else
								this.worldObj.setBlock(coord[0], coord[1], coord[2], Blocks.flowing_lava, 15, 3);
						}
					}
				}

				if (power >= 0.7F)
				{
					List<EntityLivingBase> list1 = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(this.xCoord - 3, this.yCoord - 3, this.zCoord - 3, this.xCoord + 4, this.yCoord + 4, this.zCoord + 4));

					for (int l = 0; l < list1.size(); ++l)
					{
						EntityLivingBase ent = list1.get(l);
						ent.attackEntityFrom(IC2DamageSource.radiation, (int) (this.worldObj.rand.nextInt(4) * this.hem));
					}
				}

				if (power >= 0.5F && this.worldObj.rand.nextFloat() <= this.hem)
				{
					int[] coord = this.getRandCoord(2);
					if (coord != null)
					{
						Block id = this.worldObj.getBlock(coord[0], coord[1], coord[2]);
						if (id != null && id.getMaterial() == Material.water)
							this.worldObj.setBlockToAir(coord[0], coord[1], coord[2]);
					}
				}

				if (power >= 0.4F && this.worldObj.rand.nextFloat() <= this.hem)
				{
					int[] coord = this.getRandCoord(2);
					if (coord != null)
					{
						Block id = this.worldObj.getBlock(coord[0], coord[1], coord[2]);
						if (id != null)
						{
							Material mat = id.getMaterial();
							if (mat == Material.wood || mat == Material.leaves || mat == Material.cloth)
								this.worldObj.setBlock(coord[0], coord[1], coord[2], Blocks.fire);
						}
					}
				}

				return false;
			}
		}
		else
			return false;
	}

	public int[] getRandCoord(int radius)
	{
		if (radius <= 0)
			return null;
		else
		{
			int[] c = new int[] { this.xCoord + this.worldObj.rand.nextInt(2 * radius + 1) - radius, this.yCoord + this.worldObj.rand.nextInt(2 * radius + 1) - radius, this.zCoord + this.worldObj.rand.nextInt(2 * radius + 1) - radius };
			return c[0] == this.xCoord && c[1] == this.yCoord && c[2] == this.zCoord ? null : c;
		}
	}

	public void processChambers()
	{
		int size = this.getReactorSize();

		for (int pass = 0; pass < 2; ++pass)
			for (int y = 0; y < 6; ++y)
				for (int x = 0; x < size; ++x)
				{
					ItemStack thing = this.getMatrixCoord(x, y);
					if (thing != null && thing.getItem() instanceof IReactorComponent)
					{
						IReactorComponent comp = (IReactorComponent) thing.getItem();
						comp.processChamber(this, thing, x, y, pass == 0);
					}
				}

	}

	@Override
	public boolean produceEnergy()
	{
		return (this.redstonePowered || this.reactorPower) && IC2.energyGeneratorNuclear != 0;
	}

	public ItemStack getMatrixCoord(int x, int y)
	{
		return x >= 0 && x < this.getReactorSize() && y >= 0 && y < 6 ? this.getStackInSlot(x + y * 9) : null;
	}

	public void setMatrixCoord(int x, int y, ItemStack stack)
	{
		if (x >= 0 && x < this.getReactorSize() && y >= 0 && y < 6)
			this.setInventorySlotContents(x + y * 9, stack);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		super.setInventorySlotContents(i, itemstack);
		if (itemstack != null)
			this.hasAddedSomething = true;
	}

	public int getReactorSize()
	{
		if (this.lastCheck < this.getWorld().getTotalWorldTime() || this.refreshRequest)
			this.updateReactorSize();

		return this.size;
	}

	private void updateReactorSize()
	{
		this.refreshRequest = false;
		this.lastCheck = this.getWorld().getTotalWorldTime() + 20L;
		int lastSize = this.size;
		this.size = 3;

		for (Direction direction : directions)
		{
			TileEntity target = direction.applyToTileEntity(this);
			if (target instanceof TileEntityReactorChamber)
				++this.size;
		}
		if (lastSize != this.size)
			this.hasAddedSomething = true;
	}

	public void refreshChambers()
	{
		this.refreshRequest = true;
	}

	@Override
	public int getTickRate()
	{
		return 20;
	}

	@Override
	public ContainerIC2 getGuiContainer(EntityPlayer entityPlayer)
	{
		return new ContainerNuclearReactor(entityPlayer, this);
	}

	@Override
	public String getGuiClassName(EntityPlayer entityPlayer)
	{
		return "block.generator.gui.GuiNuclearReactor";
	}

	@Override
	public void onGuiClosed(EntityPlayer entityPlayer)
	{
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("output"))
		{
			if (this.output > 0)
			{
				if (this.audioSourceMain != null && this.audioSourceMain.isRemoved())
					this.audioSourceMain = null;

				if (this.audioSourceGeiger != null && this.audioSourceGeiger.isRemoved())
					this.audioSourceGeiger = null;

				if (this.lastOutput <= 0)
				{
					if (this.audioSourceMain == null)
						this.audioSourceMain = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/NuclearReactorLoop.ogg", true, false, IC2.audioManager.defaultVolume);

					if (this.audioSourceMain != null)
						this.audioSourceMain.play();
				}

				if (this.output < 40)
				{
					if (this.lastOutput <= 0 || this.lastOutput >= 40)
					{
						if (this.audioSourceGeiger != null)
							this.audioSourceGeiger.remove();

						this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerLowEU.ogg", true, false, IC2.audioManager.defaultVolume);
						if (this.audioSourceGeiger != null)
							this.audioSourceGeiger.play();
					}
				}
				else if (this.output < 80)
				{
					if (this.lastOutput < 40 || this.lastOutput >= 80)
					{
						if (this.audioSourceGeiger != null)
							this.audioSourceGeiger.remove();

						this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerMedEU.ogg", true, false, IC2.audioManager.defaultVolume);
						if (this.audioSourceGeiger != null)
							this.audioSourceGeiger.play();
					}
				}
				else if (this.output >= 80 && this.lastOutput < 80)
				{
					if (this.audioSourceGeiger != null)
						this.audioSourceGeiger.remove();

					this.audioSourceGeiger = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/NuclearReactor/GeigerHighEU.ogg", true, false, IC2.audioManager.defaultVolume);
					if (this.audioSourceGeiger != null)
						this.audioSourceGeiger.play();
				}
			}
			else if (this.lastOutput > 0)
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
	public float getWrenchDropRate()
	{
		return 0.8F;
	}

	@Override
	public ChunkCoordinates getPosition()
	{
		return new ChunkCoordinates(super.xCoord, super.yCoord, super.zCoord);
	}

	@Override
	public World getWorld()
	{
		return this.worldObj;
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
		return this.heat += amount;
	}

	@Override
	public void addEmitHeat(int heat)
	{
	}

	@Override
	public ItemStack getItemAt(int x, int y)
	{
		return this.getMatrixCoord(x, y);
	}

	@Override
	public void setItemAt(int x, int y, ItemStack item)
	{
		this.setMatrixCoord(x, y, item);
	}

	@Override
	public void explode()
	{
		float boomPower = 10.0F;
		float boomMod = 1.0F;

		for (int y = 0; y < 6; ++y)
			for (int x = 0; x < this.getReactorSize(); ++x)
			{
				ItemStack stack = this.getMatrixCoord(x, y);
				if (stack != null && stack.getItem() instanceof IReactorComponent)
				{
					float f = ((IReactorComponent) stack.getItem()).influenceExplosion(this, stack);
					if (f > 0.0F && f < 1.0F)
						boomMod *= f;
					else
						boomPower += f;
				}

				this.setMatrixCoord(x, y, (ItemStack) null);
			}

		boomPower = boomPower * this.hem * boomMod;
		IC2.log.log(Level.INFO, "Nuclear Reactor at " + super.worldObj.provider.dimensionId + ":(" + super.xCoord + "," + super.yCoord + "," + super.zCoord + ") melted (explosion power " + boomPower + ")");
		if (boomPower > IC2.explosionPowerReactorMax)
			boomPower = IC2.explosionPowerReactorMax;

		for (Direction direction : directions)
		{
			TileEntity target = direction.applyToTileEntity(this);
			if (target instanceof TileEntityReactorChamber)
				this.worldObj.setBlockToAir(target.xCoord, target.yCoord, target.zCoord);
		}

		this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
    
    

		explosion.doExplosion();
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
		return this.output += (short) (int) energy;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		int size = this.getReactorSize();
		if (slots[size] == null)
		{
			slots[size] = new int[6 * size];
			int k = 0;

			for (int y = 0; y < 6; ++y)
				for (int x = 0; x < size; ++k)
				{
					slots[size][k] = x + y * 9;
					++x;
				}
		}

		return slots[size];
	}

	@Override
	public double getReactorEUEnergyOutput()
	{
		return this.output;
	}

	@Override
	public void setRedstoneSignal(boolean redstone)
	{
		this.redstonePowered = redstone;
	}

	@Override
	public boolean isFluidCooled()
	{
		return false;
	}
}
