package tconstruct.smeltery.logic;

import ru.will.git.tconstruct.EventConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mantle.blocks.abstracts.InventoryLogic;
import mantle.blocks.abstracts.MultiServantLogic;
import mantle.blocks.iface.IActiveLogic;
import mantle.blocks.iface.IFacingLogic;
import mantle.blocks.iface.IMasterLogic;
import mantle.blocks.iface.IServantLogic;
import mantle.world.CoordTuple;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import tconstruct.TConstruct;
import tconstruct.library.crafting.Smeltery;
import tconstruct.smeltery.SmelteryDamageSource;
import tconstruct.smeltery.TinkerSmeltery;
import tconstruct.smeltery.inventory.SmelteryContainer;
import tconstruct.util.config.PHConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

    

public class SmelteryLogic extends InventoryLogic implements IActiveLogic, IFacingLogic, IFluidTank, IMasterLogic
{
	private static final int MAX_SMELTERY_SIZE = 7;
	public static final int MB_PER_BLOCK_CAPACITY = TConstruct.ingotLiquidValue * 10;

	public boolean validStructure;
	public boolean tempValidStructure;
	protected byte direction;

	public CoordTuple minPos = new CoordTuple(0, 0, 0);
	public CoordTuple maxPos = new CoordTuple(0, 0, 0);
	public int layers;
	public int maxBlockCapacity;

	protected int internalTemp;
	public int useTime;
	public int fuelGague;
	public int fuelAmount;
	protected boolean inUse;

	protected ArrayList<CoordTuple> lavaTanks;
	protected CoordTuple activeLavaTank;

    
    
	private int tick;

	public ArrayList<FluidStack> moltenMetal = new ArrayList<FluidStack>();
	public int maxLiquid;
	public int currentLiquid;

	Random rand = new Random();
    
	public int lastChange = 0;
    

	public SmelteryLogic()
	{
		super(0);
		this.lavaTanks = new ArrayList<CoordTuple>();
		this.activeTemps = new int[0];
		this.meltingTemps = new int[0];
	}

	public int getBlocksPerLayer()
	{
		int xd = this.maxPos.x - this.minPos.x + 1;
		int zd = this.maxPos.z - this.minPos.z + 1;
		return xd * zd;
	}

	public int getCapacityPerLayer()
	{
		return this.getBlocksPerLayer() * MB_PER_BLOCK_CAPACITY;
	}

	public int getBlockCapacity()
	{
		return this.maxBlockCapacity;
	}

	void adjustLayers(int lay, boolean forceAdjust)
	{
		if (lay != this.layers || forceAdjust)
		{
			this.needsUpdate = true;
			this.layers = lay;
			this.maxBlockCapacity = this.getBlocksPerLayer() * this.layers;
			this.maxLiquid = this.maxBlockCapacity * MB_PER_BLOCK_CAPACITY;

			int[] tempActive = this.activeTemps;
			this.activeTemps = new int[this.maxBlockCapacity];
			int activeLength = tempActive.length > this.activeTemps.length ? this.activeTemps.length : tempActive.length;
			System.arraycopy(tempActive, 0, this.activeTemps, 0, activeLength);

			int[] tempMelting = this.meltingTemps;
			this.meltingTemps = new int[this.maxBlockCapacity];
			int meltingLength = tempMelting.length > this.meltingTemps.length ? this.meltingTemps.length : tempMelting.length;
			System.arraycopy(tempMelting, 0, this.meltingTemps, 0, meltingLength);

			ItemStack[] tempInv = this.inventory;
			this.inventory = new ItemStack[this.maxBlockCapacity];
			int invLength = tempInv.length > this.inventory.length ? this.inventory.length : tempInv.length;
			System.arraycopy(tempInv, 0, this.inventory, 0, invLength);

			if (this.activeTemps.length > 0 && this.activeTemps.length > tempActive.length)
				for (int i = tempActive.length; i < this.activeTemps.length; i++)
				{
					this.activeTemps[i] = 200;
					this.meltingTemps[i] = 200;
				}

			if (tempInv.length > this.inventory.length)
				for (int i = this.inventory.length; i < tempInv.length; i++)
				{
					ItemStack stack = tempInv[i];
					if (stack != null)
					{
						float jumpX = this.rand.nextFloat() * 0.8F + 0.1F;
						float jumpY = this.rand.nextFloat() * 0.8F + 0.1F;
						float jumpZ = this.rand.nextFloat() * 0.8F + 0.1F;

						int offsetX = 0;
						int offsetZ = 0;
						switch (this.getRenderDirection())
						{
    
								offsetZ = -1;
								break;
    
								offsetZ = 1;
								break;
    
								offsetX = -1;
								break;
    
								offsetX = 1;
								break;
						}

						while (stack.stackSize > 0)
						{
							int itemSize = this.rand.nextInt(21) + 10;

							if (itemSize > stack.stackSize)
								itemSize = stack.stackSize;

							stack.stackSize -= itemSize;
							EntityItem entityitem = new EntityItem(this.worldObj, this.xCoord + jumpX + offsetX, this.yCoord + jumpY, this.zCoord + jumpZ + offsetZ, new ItemStack(stack.getItem(), itemSize, stack.getItemDamage()));

							if (stack.hasTagCompound())
								entityitem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());

							float offset = 0.05F;
							entityitem.motionX = (float) this.rand.nextGaussian() * offset;
							entityitem.motionY = (float) this.rand.nextGaussian() * offset + 0.2F;
							entityitem.motionZ = (float) this.rand.nextGaussian() * offset;
							this.worldObj.spawnEntityInWorld(entityitem);
						}
					}
    
    
    
		this.updateCurrentLiquid();
	}

    
	@Override
	public String getDefaultName()
	{
		return "crafters.Smeltery";
	}

	@Override
	public Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z)
	{
		return new SmelteryContainer(inventoryplayer, this);
	}

	@Override
	public byte getRenderDirection()
	{
		return this.direction;
	}

	@Override
	public ForgeDirection getForgeDirection()
	{
		return ForgeDirection.VALID_DIRECTIONS[this.direction];
	}

	@Override
	public void setDirection(int side)
	{

	}

	@Override
	public void setDirection(float yaw, float pitch, EntityLivingBase player)
	{
		int facing = MathHelper.floor_double(yaw / 360 + 0.5D) & 3;
		switch (facing)
		{
			case 0:
				this.direction = 2;
				break;

			case 1:
				this.direction = 5;
				break;

			case 2:
				this.direction = 3;
				break;

			case 3:
				this.direction = 4;
				break;
		}
	}

	@Override
	public boolean getActive()
	{
		return this.validStructure;
	}

	@Override
	public void setActive(boolean flag)
	{
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	public int getScaledFuelGague(int scale)
	{
		int ret = this.fuelGague * scale / 52;
		if (ret < 1)
			ret = 1;
		return ret;
	}

	public int getInternalTemperature()
	{
		if (!this.validStructure)
			return 20;

		return this.internalTemp;
	}

	public int getTempForSlot(int slot)
	{
		return this.activeTemps[slot] / 10;
	}

	public int getMeltingPointForSlot(int slot)
	{
		return this.meltingTemps[slot] / 10;
	}

    
	@Override
	public void updateEntity()
	{
		this.tick++;
		if (this.tick == 60)
		{
			this.tick = 0;
			this.detectEntities();
		}

    

		if (this.tick % 4 == 0)
		{
			if (this.useTime > 0)
				this.useTime -= 4;

			if (this.validStructure)
			{
    
				if (this.useTime <= 0 && this.inUse)
					this.updateFuelGague();

				this.heatItems();
			}
		}

		if (this.tick % 20 == 0)
		{
			if (!this.validStructure)
				this.checkValidPlacement();

			if (this.needsUpdate)
			{
				this.needsUpdate = false;
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			}
		}
	}

	void detectEntities()
	{
		if (this.minPos == null || this.maxPos == null)
			return;

		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(this.minPos.x, this.minPos.y, this.minPos.z, this.maxPos.x + 1, this.minPos.y + this.layers, this.maxPos.z + 1);

		List<Entity> list = this.worldObj.getEntitiesWithinAABB(Entity.class, box);
		for (Entity entity : list)
    
			if (entity.isDead)
    

			if (this.moltenMetal.size() >= 1)
			{
    
				Fluid fluid = null;
				int amount = 0;
				float damage = 5;

				if (entity instanceof EntityVillager && PHConstruct.meltableVillagers)
				{
					EntityVillager villager = (EntityVillager) entity;
					fluid = TinkerSmeltery.moltenEmeraldFluid;
					amount = villager.isChild() ? 5 : 40;
				}
				else if (entity instanceof EntityEnderman)
				{
					fluid = TinkerSmeltery.moltenEnderFluid;
					amount = 125;
				}
				else if (entity instanceof EntityIronGolem)
				{
					fluid = TinkerSmeltery.moltenIronFluid;
					amount = 40;
				}
				else if (entity instanceof EntityHorse && PHConstruct.meltableHorses)
				{
					fluid = TinkerSmeltery.glueFluid;
					amount = 108;
				}
				else if (entity instanceof EntityLivingBase)
				{
					EntityLivingBase living = (EntityLivingBase) entity;
					fluid = TinkerSmeltery.bloodFluid;
					amount = living.isChild() || living instanceof EntityPlayer ? 5 : 40;
				}

				if (fluid != null && amount > 0 && damage > 0)
				{
					boolean canFill = false;

					if (EventConfig.smelteryInstantDeath && entity instanceof EntityLivingBase)
					{
						int freeLiquid = Math.max(0, this.maxLiquid - this.currentLiquid - 1);
						if (freeLiquid > 0)
						{
							int freeQuants = MathHelper.floor_float((float) freeLiquid / amount);
							if (freeQuants > 0)
							{
								float health = ((EntityLivingBase) entity).getHealth();
								int quantsAmount = MathHelper.ceiling_float_int(health / damage);
								if (quantsAmount > 0)
								{
									int quantsToBeAdded = Math.min(freeQuants, quantsAmount);
									amount = quantsToBeAdded * amount;
									canFill = entity.attackEntityFrom(DamageSource.outOfWorld, 9999);
								}
							}
						}
					}
					else
						canFill = entity.attackEntityFrom(new SmelteryDamageSource(), damage);

					if (canFill && amount > 0)
						this.fill(new FluidStack(fluid, amount), true);
    
			}
			else if (PHConstruct.throwableSmeltery && entity instanceof EntityItem)
				this.handleItemEntity((EntityItem) entity);
		}
	}

	private void handleItemEntity(EntityItem item)
    
    
		if (this.worldObj.isRemote)
			return;

		item.age = 0;
		ItemStack istack = item.getEntityItem();
    
    
			return;

		int maxSlot = this.getSizeInventory();
		boolean itemDestroyed = false;
		boolean itemAdded = false;

		for (int i = 0; i < maxSlot; i++)
		{
			ItemStack stack = this.getStackInSlot(i);
			if (stack == null && istack.stackSize > 0)
			{
				ItemStack copy = istack.splitStack(1);
				this.setInventorySlotContents(i, copy);
				itemAdded = true;
				if (istack.stackSize <= 0)
				{
					item.setDead();
					itemDestroyed = true;
					break;
				}
			}
		}

		if (!itemDestroyed)
			item.setEntityItemStack(istack);
		if (itemAdded)
		{
    
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
	}

	private void checkHasItems()
	{
		this.inUse = false;
		for (int i = 0; i < this.maxBlockCapacity; i++)
		{
			if (this.isStackInSlot(i) && this.meltingTemps[i] > 200)
			{
				this.inUse = true;
				break;
			}
		}
	}

	private void heatItems()
	{
		if (this.useTime > 0)
		{
			boolean hasUse = false;
			int temperature = this.getInternalTemperature();
			int speed = temperature / 100;
			int refTemp = temperature * 10;
			for (int i = 0; i < this.maxBlockCapacity; i++)
			{
				if (this.meltingTemps[i] > 200 && this.isStackInSlot(i))
				{
					hasUse = true;
					if (this.activeTemps[i] < refTemp && this.activeTemps[i] < this.meltingTemps[i])
    
					else if (this.activeTemps[i] >= this.meltingTemps[i])
						if (!this.worldObj.isRemote)
						{
							FluidStack result = this.getResultFor(this.inventory[i]);
							if (result != null)
								if (this.addMoltenMetal(result, false))
								{
									this.inventory[i] = null;
									this.activeTemps[i] = 200;
									ArrayList alloys = Smeltery.mixMetals(this.moltenMetal);
									for (int al = 0; al < alloys.size(); al++)
									{
										FluidStack liquid = (FluidStack) alloys.get(al);
										this.addMoltenMetal(liquid, true);
									}
									this.markDirty();
								}
						}

				}

				else
					this.activeTemps[i] = 200;
			}
			this.inUse = hasUse;
		}
	}

	boolean addMoltenMetal(FluidStack liquid, boolean first)
	{
		this.needsUpdate = true;
		if (this.moltenMetal.size() == 0)
    
			if (liquid.amount > this.getCapacity())
				return false;

			this.moltenMetal.add(liquid.copy());
			this.updateCurrentLiquid();
			return true;
    
		this.updateCurrentLiquid();

		if (liquid.amount + this.currentLiquid > this.maxLiquid)
			return false;

    
		boolean added = false;
		for (int i = 0; i < this.moltenMetal.size(); i++)
		{
    
    
			if (l.isFluidEqual(liquid))
			{
				l.amount += liquid.amount;
				added = true;
			}
			if (l.amount <= 0)
			{
				this.moltenMetal.remove(l);
				i--;
			}
		}
		if (!added)
			if (first)
				this.moltenMetal.add(0, liquid.copy());
			else
				this.moltenMetal.add(liquid.copy());
		return true;
	}

	private void updateCurrentLiquid()
	{
		this.currentLiquid = 0;
		for (FluidStack liquid : this.moltenMetal)
		{
			this.currentLiquid += liquid.amount;
		}
	}

	private void updateTemperatures()
	{
		for (int i = 0; i < this.maxBlockCapacity && i < this.meltingTemps.length; i++)
		{
    
		}
	}

	public void updateFuelDisplay()
    
		this.verifyFuelTank();
		if (this.activeLavaTank == null)
		{
			this.fuelAmount = 0;
			this.fuelGague = 0;
			return;
    
		IFluidHandler tankContainer = (IFluidHandler) this.worldObj.getTileEntity(this.activeLavaTank.x, this.activeLavaTank.y, this.activeLavaTank.z);
		FluidTankInfo[] info = tankContainer.getTankInfo(ForgeDirection.DOWN);

		int capacity = info[0].capacity;
		this.fuelAmount = info[0].fluid.amount;
		this.fuelGague = (int) (this.fuelAmount * 52f / capacity);
    
	public void updateFuelGague()
    
		if (this.useTime > 0 || !this.inUse)
    
		this.verifyFuelTank();
		if (this.activeLavaTank == null)
    
    
		FluidStack liquid = tankContainer.drain(ForgeDirection.DOWN, 15, false);
    
		{
			do
    
    
				if (liquid == null || liquid.amount == 0)
					break;
				this.useTime += (int) ((float) Smeltery.getFuelDuration(liquid.getFluid()) * Math.round(15f / liquid.amount));
				this.internalTemp = Smeltery.getFuelPower(liquid.getFluid());
			}
    
    
		}
	}

	protected void verifyFuelTank()
    
		if (this.activeLavaTank != null && this.worldObj.blockExists(this.activeLavaTank.x, this.activeLavaTank.y, this.activeLavaTank.z))
		{
			TileEntity tankContainer = this.worldObj.getTileEntity(this.activeLavaTank.x, this.activeLavaTank.y, this.activeLavaTank.z);
			if (tankContainer instanceof IFluidHandler)
			{
    
				if (liquid != null && Smeltery.isSmelteryFuel(liquid.getFluid()))
					return;
			}
    
		this.activeLavaTank = null;
		for (CoordTuple tank : this.lavaTanks)
    
			if (!this.worldObj.blockExists(tank.x, tank.y, tank.z))
    
			TileEntity tankContainer = this.worldObj.getTileEntity(tank.x, tank.y, tank.z);
			if (!(tankContainer instanceof IFluidHandler))
    
			FluidTankInfo[] info = ((IFluidHandler) tankContainer).getTankInfo(ForgeDirection.DOWN);
			if (info.length <= 0 || info[0].fluid == null || info[0].fluid.amount <= 0)
    
			if (!Smeltery.isSmelteryFuel(info[0].fluid.getFluid()))
    
			this.activeLavaTank = tank;
			return;
    
	}

	@SideOnly(Side.CLIENT)
	public FluidStack getFuel()
	{
    
			return new FluidStack(FluidRegistry.LAVA, 0);

		TileEntity tankContainer = this.worldObj.getTileEntity(this.activeLavaTank.x, this.activeLavaTank.y, this.activeLavaTank.z);
		if (tankContainer instanceof IFluidHandler)
			return ((IFluidHandler) tankContainer).getTankInfo(ForgeDirection.DOWN)[0].fluid;

		return new FluidStack(FluidRegistry.LAVA, 0);
	}

	public FluidStack getResultFor(ItemStack stack)
	{
		return Smeltery.getSmelteryResult(stack);
	}

    
    

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public void markDirty()
	{
		this.updateTemperatures();
		this.updateEntity();

		super.markDirty();
    
    
	}

    

    
	@Override
	public void notifyChange(IServantLogic servant, int x, int y, int z)
    
		if (!EventConfig.smelteryLogicUpdateInSameTick)
		{
			long time = this.worldObj.getTotalWorldTime();
			if (this.lastUpdateTime == time)
				return;
			this.lastUpdateTime = time;
    

		this.checkValidPlacement();
	}

	public void checkValidPlacement()
	{
		switch (this.getRenderDirection())
		{
    
				this.alignInitialPlacement(this.xCoord, this.yCoord, this.zCoord + 1);
				break;
    
				this.alignInitialPlacement(this.xCoord, this.yCoord, this.zCoord - 1);
				break;
    
				this.alignInitialPlacement(this.xCoord + 1, this.yCoord, this.zCoord);
				break;
    
				this.alignInitialPlacement(this.xCoord - 1, this.yCoord, this.zCoord);
				break;
		}
    
	public void alignInitialPlacement(int x, int y, int z)
    
    
    
    
    
		{
			if (this.worldObj.getBlock(x - xd1, y, z) == null || this.worldObj.isAirBlock(x - xd1, y, z))
				xd1++;
			else if (this.worldObj.getBlock(x + xd2, y, z) == null || this.worldObj.isAirBlock(x + xd2, y, z))
    
			if (xd1 - xd2 > 1)
    
				xd1--;
				x--;
				xd2++;
    
			if (xd2 - xd1 > 1)
			{
				xd2--;
				x++;
				xd1++;
			}
    
		int zd1 = 1, zd2 = 1;
    
		{
			if (this.worldObj.getBlock(x, y, z - zd1) == null || this.worldObj.isAirBlock(x, y, z - zd1))
				zd1++;
			else if (this.worldObj.getBlock(x, y, z + zd2) == null || this.worldObj.isAirBlock(x, y, z + zd2))
    
			if (zd1 - zd2 > 1)
    
				zd1--;
				z--;
				zd2++;
    
			if (zd2 - zd1 > 1)
			{
				zd2--;
				z++;
				zd1++;
			}
    
		int[] sides = { xd1, xd2, zd1, zd2 };
		this.checkValidStructure(x, y, z, sides);
	}

    
	public void checkValidStructure(int x, int y, int z, int[] sides)
	{
    
    

    
		if (this.checkSameLevel(x, y, z, sides))
		{
			checkLayers++;
			checkLayers += this.recurseStructureUp(x, y + 1, z, sides, 0);
			checkLayers += this.recurseStructureDown(x, y - 1, z, sides, 0);
    

		if (this.tempValidStructure != this.validStructure || checkLayers != this.layers)
			if (this.tempValidStructure)
    
				this.activeLavaTank = null;
				for (CoordTuple tank : this.lavaTanks)
				{
					TileEntity tankContainer = this.worldObj.getTileEntity(tank.x, tank.y, tank.z);
					if (!(tankContainer instanceof IFluidHandler))
						continue;

					FluidStack liquid = ((IFluidHandler) tankContainer).getTankInfo(ForgeDirection.DOWN)[0].fluid;
					if (liquid == null)
						continue;
					if (!Smeltery.isSmelteryFuel(liquid.getFluid()))
						continue;

					this.internalTemp = Smeltery.getFuelPower(liquid.getFluid());
					this.activeLavaTank = tank;
					break;
    
				if (this.activeLavaTank == null)
    
				this.adjustLayers(checkLayers, true);
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				this.validStructure = true;
			}
			else
			{
				this.internalTemp = 20;
				if (this.validStructure)
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				this.validStructure = false;
			}
	}

	public boolean checkBricksOnLevel(int x, int y, int z, int[] sides)
	{
		int numBricks = 0;
		Block block;
		int xMin = x - sides[0];
		int xMax = x + sides[1];
		int zMin = z - sides[2];
    
		for (int xPos = xMin + 1; xPos <= xMax - 1; xPos++)
		{
			for (int zPos = zMin + 1; zPos <= zMax - 1; zPos++)
			{
				block = this.worldObj.getBlock(xPos, y, zPos);
				if (block != null && !this.worldObj.isAirBlock(xPos, y, zPos))
					return false;
			}
    
		for (int xPos = xMin + 1; xPos <= xMax - 1; xPos++)
		{
			numBricks += this.checkBricks(xPos, y, zMin);
			numBricks += this.checkBricks(xPos, y, zMax);
		}

		for (int zPos = zMin + 1; zPos <= zMax - 1; zPos++)
		{
			numBricks += this.checkBricks(xMin, y, zPos);
			numBricks += this.checkBricks(xMax, y, zPos);
		}

    

		return numBricks == neededBricks;
	}

	public boolean checkSameLevel(int x, int y, int z, int[] sides)
	{
		this.lavaTanks.clear();

		boolean check = this.checkBricksOnLevel(x, y, z, sides);

		return check && this.lavaTanks.size() > 0;
	}

	public int recurseStructureUp(int x, int y, int z, int[] sides, int count)
	{
		boolean check = this.checkBricksOnLevel(x, y, z, sides);

		if (!check)
			return count;

		count++;
		return this.recurseStructureUp(x, y + 1, z, sides, count);
	}

	public int recurseStructureDown(int x, int y, int z, int[] sides, int count)
	{
		boolean check = this.checkBricksOnLevel(x, y, z, sides);

		if (!check)
    
			Block block = this.worldObj.getBlock(x, y, z);
			if (block != null && !this.worldObj.isAirBlock(x, y, z))
				if (this.validBlockID(block))
					return this.validateBottom(x, y, z, sides, count);

			return count;
		}

		count++;
		return this.recurseStructureDown(x, y - 1, z, sides, count);
	}

	public int validateBottom(int x, int y, int z, int[] sides, int count)
	{
		int bottomBricks = 0;
		int xMin = x - sides[0] + 1;
		int xMax = x + sides[1] - 1;
		int zMin = z - sides[2] + 1;
    
		for (int xPos = xMin; xPos <= xMax; xPos++)
		{
			for (int zPos = zMin; zPos <= zMax; zPos++)
			{
				if (this.validBlockID(this.worldObj.getBlock(xPos, y, zPos)) && this.worldObj.getBlockMetadata(xPos, y, zPos) >= 2)
				{
					TileEntity te = this.worldObj.getTileEntity(xPos, y, zPos);

					if (te instanceof MultiServantLogic)
					{
						MultiServantLogic servant = (MultiServantLogic) te;
						if (servant.hasValidMaster())
						{
							if (servant.verifyMaster(this, this.worldObj, this.xCoord, this.yCoord, this.zCoord))
								bottomBricks++;
						}
						else
						{
							servant.overrideMaster(this.xCoord, this.yCoord, this.zCoord);
							bottomBricks++;
						}
					}
				}
			}
		}

    

		if (bottomBricks == neededBricks)
		{
			this.tempValidStructure = true;
			this.minPos = new CoordTuple(xMin, y + 1, zMin);
			this.maxPos = new CoordTuple(xMax, y + 1, zMax);
		}
		return count;
	}

    
	int checkBricks(int x, int y, int z)
	{
		int tempBricks = 0;
		Block blockID = this.worldObj.getBlock(x, y, z);
		if (this.validBlockID(blockID) || this.validTankID(blockID))
		{
			TileEntity te = this.worldObj.getTileEntity(x, y, z);
			if (te == this)
				tempBricks++;
			else if (te instanceof MultiServantLogic)
			{
				MultiServantLogic servant = (MultiServantLogic) te;

				if (servant.hasValidMaster())
				{
					if (servant.verifyMaster(this, this.worldObj, this.xCoord, this.yCoord, this.zCoord))
						tempBricks++;
				}
				else
				{
					servant.overrideMaster(this.xCoord, this.yCoord, this.zCoord);
					tempBricks++;
				}

				if (te instanceof LavaTankLogic)
					this.lavaTanks.add(new CoordTuple(x, y, z));
			}
		}
		return tempBricks;
	}

	boolean validBlockID(Block blockID)
	{
		return blockID == TinkerSmeltery.smeltery || blockID == TinkerSmeltery.smelteryNether;
	}

	boolean validTankID(Block blockID)
	{
		return blockID == TinkerSmeltery.lavaTank || blockID == TinkerSmeltery.lavaTankNether;
	}

	@Override
	public int getCapacity()
	{
		return this.maxLiquid;
	}

	public int getTotalLiquid()
	{
		return this.currentLiquid;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
    
		if (!this.validStructure)
			return null;

		if (this.moltenMetal.size() == 0)
			return null;

		FluidStack liquid = this.moltenMetal.get(0);
		if (liquid != null)
		{
			if (liquid.amount - maxDrain <= 0)
			{
				FluidStack liq = liquid.copy();
				if (doDrain)
    
					this.moltenMetal.remove(liquid);
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
					this.needsUpdate = true;
					this.updateCurrentLiquid();
				}
				return liq;
			}
			if (doDrain && maxDrain > 0)
			{
				liquid.amount -= maxDrain;
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				this.currentLiquid -= maxDrain;
				this.needsUpdate = true;
			}
			return new FluidStack(liquid.getFluid(), maxDrain, liquid.tag);
		}
		return new FluidStack(0, 0);
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
    
		if (!this.validStructure)
			return 0;

    
    
    
		{
			if (resource.amount + this.currentLiquid > this.maxLiquid)
				resource.amount = this.maxLiquid - this.currentLiquid;
			int amount = resource.amount;

			if (amount > 0 && doFill)
			{
				if (this.addMoltenMetal(resource, false))
				{
					ArrayList alloys = Smeltery.mixMetals(this.moltenMetal);
					for (int al = 0; al < alloys.size(); al++)
					{
						FluidStack liquid = (FluidStack) alloys.get(al);
						this.addMoltenMetal(liquid, true);
					}
				}
				this.needsUpdate = true;
				this.worldObj.func_147479_m(this.xCoord, this.yCoord, this.zCoord);
			}
			return amount;
		}
		return 0;
	}

	@Override
	public FluidStack getFluid()
	{
		if (this.moltenMetal.size() == 0)
			return null;
		return this.moltenMetal.get(0);
	}

	@Override
	public int getFluidAmount()
	{
		return this.currentLiquid;
	}

	@Override
	public FluidTankInfo getInfo()
	{
		return new FluidTankInfo(this);
	}

	public FluidTankInfo[] getMultiTankInfo()
	{
		FluidTankInfo[] info = new FluidTankInfo[this.moltenMetal.size() + 1];
		for (int i = 0; i < this.moltenMetal.size(); i++)
		{
			FluidStack fluid = this.moltenMetal.get(i);
			info[i] = new FluidTankInfo(fluid.copy(), fluid.amount);
		}
		info[this.moltenMetal.size()] = new FluidTankInfo(null, this.maxLiquid - this.currentLiquid);
		return info;
	}

    

	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		this.layers = tags.getInteger("Layers");
		int[] pos = tags.getIntArray("MinPos");
		if (pos.length > 2)
			this.minPos = new CoordTuple(pos[0], pos[1], pos[2]);
		else
			this.minPos = new CoordTuple(this.xCoord, this.yCoord, this.zCoord);

		pos = tags.getIntArray("MaxPos");
		if (pos.length > 2)
			this.maxPos = new CoordTuple(pos[0], pos[1], pos[2]);
		else
			this.maxPos = new CoordTuple(this.xCoord, this.yCoord, this.zCoord);

		this.maxBlockCapacity = this.getBlocksPerLayer() * this.layers;
		this.inventory = new ItemStack[this.maxBlockCapacity];
		super.readFromNBT(tags);

		this.internalTemp = tags.getInteger("InternalTemp");
		this.inUse = tags.getBoolean("InUse");

		this.direction = tags.getByte("Direction");
		this.useTime = tags.getInteger("UseTime");
		this.currentLiquid = tags.getInteger("CurrentLiquid");
		this.maxLiquid = tags.getInteger("MaxLiquid");
		this.meltingTemps = tags.getIntArray("MeltingTemps");
		this.activeTemps = tags.getIntArray("ActiveTemps");

		NBTTagList liquidTag = tags.getTagList("Liquids", 10);
		this.moltenMetal.clear();

		for (int iter = 0; iter < liquidTag.tagCount(); iter++)
		{
			NBTTagCompound nbt = liquidTag.getCompoundTagAt(iter);
			FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
			if (fluid != null)
				this.moltenMetal.add(fluid);
    
    

		if (!tags.getBoolean("ValidStructure"))
    
    
    
    
	}

	@Override
	public void writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);

		tags.setBoolean("ValidStructure", this.validStructure);
		tags.setInteger("InternalTemp", this.internalTemp);
		tags.setBoolean("InUse", this.inUse);

		int[] pos;
		if (this.minPos == null)
			pos = new int[] { this.xCoord, this.yCoord, this.zCoord };
		else
			pos = new int[] { this.minPos.x, this.minPos.y, this.minPos.z };
		tags.setIntArray("MinPos", pos);

		if (this.maxPos == null)
			pos = new int[] { this.xCoord, this.yCoord, this.zCoord };
		else
			pos = new int[] { this.maxPos.x, this.maxPos.y, this.maxPos.z };
		tags.setIntArray("MaxPos", pos);

		tags.setByte("Direction", this.direction);
		tags.setInteger("UseTime", this.useTime);
		tags.setInteger("CurrentLiquid", this.currentLiquid);
		tags.setInteger("MaxLiquid", this.maxLiquid);
		tags.setInteger("Layers", this.layers);
		tags.setIntArray("MeltingTemps", this.meltingTemps);
		tags.setIntArray("ActiveTemps", this.activeTemps);

		NBTTagList taglist = new NBTTagList();
		for (FluidStack liquid : this.moltenMetal)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			liquid.writeToNBT(nbt);
			taglist.appendTag(nbt);
		}

		tags.setTag("Liquids", taglist);
	}

    
	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		this.readFromNBT(packet.func_148857_g());
		this.markDirty();
		this.worldObj.func_147479_m(this.xCoord, this.yCoord, this.zCoord);
		this.needsUpdate = true;
	}

	@Override
	public String getInventoryName()
	{
		return this.getDefaultName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public void openInventory()
	{
	}
}
