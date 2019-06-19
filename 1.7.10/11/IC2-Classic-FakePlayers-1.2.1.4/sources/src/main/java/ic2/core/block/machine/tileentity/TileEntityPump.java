package ic2.core.block.machine.tileentity;

import java.util.ArrayList;
import java.util.List;

import ic2.core.ContainerIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Items;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.generator.tileentity.TileEntityGeoGenerator;
import ic2.core.block.machine.container.ContainerPump;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;

public class TileEntityPump extends TileEntityElecMachine implements IHasGui
{
	public int soundTicker = IC2.random.nextInt(64);
	public short pumpCharge = 0;
	public byte delay = 0;
	private AudioSource audioSource;

	public TileEntityPump()
	{
		super(2, 1, 200, 32);
		this.addGuiFields(new String[] { "pumpCharge" });
	}

	@Override
	public String getInventoryName()
	{
		return "Pump";
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		boolean needsInvUpdate = false;
		if (this.energy > 0 && !this.isPumpReady())
		{
			this.useEnergy(1);
			++this.pumpCharge;
			this.getNetwork().updateTileGuiField(this, "pumpCharge");
		}

		if (this.delay > 0)
			--this.delay;

		if (this.energy <= this.maxEnergy)
			needsInvUpdate = this.provideEnergy();

		if (this.delay <= 0 && this.isPumpReady())
		{
			needsInvUpdate = this.pump();
			if (needsInvUpdate)
			{
				this.delay = 120;
				this.getNetwork().updateTileGuiField(this, "pumpCharge");
			}
		}

		if (this.getActive() == this.isPumpReady() && this.energy > 0)
			this.setActive(!this.getActive());

		if (needsInvUpdate)
			this.markDirty();

	}

	@Override
	public void onUnloaded()
	{
		if (this.isRendering() && this.audioSource != null)
		{
			IC2.audioManager.removeSources(this);
			this.audioSource = null;
		}

		super.onUnloaded();
	}

	public boolean pump()
	{
		if (this.canHarvest() && this.isFluidBelow())
    
			if (this.fake.cantBreak(this.xCoord, this.yCoord - 1, this.zCoord))
    

			if (this.isWaterBelow() || this.isLavaBelow())
			{
				Block id = this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord);
				if ((id == Blocks.water || id == Blocks.lava) && this.pumpThis(id))
				{
					this.worldObj.setBlockToAir(this.xCoord, this.yCoord - 1, this.zCoord);
					return true;
				}
			}

			if (this.inventory[0] != null && this.inventory[0].getItem() == Items.bucket)
			{
				FillBucketEvent evt = new FillBucketEvent(FakePlayerFactory.getMinecraft((WorldServer) this.getWorldObj()), new ItemStack(Items.bucket), this.worldObj, new MovingObjectPosition(this.xCoord, this.yCoord - 1, this.zCoord, 1, Vec3.createVectorHelper(this.xCoord, this.yCoord - 1, this.zCoord)));
				MinecraftForge.EVENT_BUS.post(evt);
				if (evt.result != null && evt.result.getItem() != Items.bucket)
				{
					List drops = new ArrayList();
					drops.add(evt.result);
					StackUtil.distributeDrop(this, drops);
					ItemStack itemStack = this.inventory[0];
					--itemStack.stackSize;
					if (this.inventory[0].stackSize <= 0)
						this.inventory[0] = null;

					this.pumpCharge = 0;
					return true;
				}
			}

			if (this.inventory[0] != null && this.inventory[0].getItem() instanceof IFluidContainerItem)
			{
				IFluidContainerItem item = (IFluidContainerItem) this.inventory[0].getItem();
				FluidStack fluid = this.getFluidBelow(false);
				boolean flag = false;
				if (item.fill(this.inventory[0], fluid, false) == fluid.amount)
				{
					item.fill(this.inventory[0], this.getFluidBelow(true), true);
					flag = true;
					this.pumpCharge = 0;
				}

				if (flag && this.isFull(this.inventory[0], item) || !flag)
				{
					List drops2 = new ArrayList();
					drops2.add(this.inventory[0].copy());
					this.inventory[0] = null;
					StackUtil.distributeDrop(this, drops2);
				}

				return flag;
			}
			else
				return false;
		}
		else
			return false;
	}

	public boolean isFluidBelow()
	{
		return this.getFluidBelow(false) != null;
	}

	public FluidStack getFluidBelow(boolean drain)
	{
		Block block = this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord);
		if (this.worldObj.isAirBlock(this.xCoord, this.yCoord - 1, this.zCoord))
			return null;
		else
		{
			Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
			if (fluid == null)
				return null;
			else if (fluid != FluidRegistry.WATER && fluid != FluidRegistry.LAVA)
				return block instanceof IFluidBlock ? ((IFluidBlock) block).drain(this.worldObj, this.xCoord, this.yCoord - 1, this.zCoord, drain) : null;
			else
			{
				if (drain)
					this.worldObj.setBlockToAir(this.xCoord, this.yCoord - 1, this.zCoord);

				return new FluidStack(fluid, 1000);
			}
		}
	}

	public boolean isGeoAvaible()
	{
		return this.isGeoAviable(this.xCoord + 1, this.yCoord, this.zCoord) || this.isGeoAviable(this.xCoord - 1, this.yCoord, this.zCoord) || this.isGeoAviable(this.xCoord, this.yCoord + 1, this.zCoord) || this.isGeoAviable(this.xCoord, this.yCoord - 1, this.zCoord) || this.isGeoAviable(this.xCoord, this.yCoord, this.zCoord + 1) || this.isGeoAviable(this.xCoord, this.yCoord, this.zCoord - 1);
	}

	public boolean isGeoAviable(int x, int y, int z)
	{
		return this.worldObj.getTileEntity(x, y, z) instanceof TileEntityGeoGenerator;
	}

	public boolean isWaterBelow()
	{
		return this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord) == Blocks.water && this.worldObj.getBlockMetadata(this.xCoord, this.yCoord - 1, this.zCoord) == 0;
	}

	public boolean isLavaBelow()
	{
		return this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord) == Blocks.lava && this.worldObj.getBlockMetadata(this.xCoord, this.yCoord - 1, this.zCoord) == 0;
	}

	public boolean pumpThis(Block water)
	{
		if (water == Blocks.lava && this.deliverLavaToGeo())
		{
			this.pumpCharge = 0;
			return true;
		}
		else if (this.inventory[0] != null && this.inventory[0].getItem() == Items.bucket)
		{
			ItemStack drop = null;
			if (water == Blocks.water)
				drop = new ItemStack(Items.water_bucket);

			if (water == Blocks.lava)
				drop = new ItemStack(Items.lava_bucket);

			List drops = new ArrayList();
			drops.add(drop);
			StackUtil.distributeDrop(this, drops);
			--this.inventory[0].stackSize;
			if (this.inventory[0].stackSize <= 0)
				this.inventory[0] = null;

			this.pumpCharge = 0;
			return true;
		}
		else if (this.inventory[0] != null && this.inventory[0].getItem() == Ic2Items.cell.getItem())
		{
			ItemStack drop = null;
			if (water == Blocks.water)
				drop = Ic2Items.waterCell.copy();

			if (water == Blocks.lava)
				drop = Ic2Items.lavaCell.copy();

			ItemStack itemStack = this.inventory[0];
			--itemStack.stackSize;
			if (this.inventory[0].stackSize <= 0)
				this.inventory[0] = null;

			List drops2 = new ArrayList();
			drops2.add(drop);
			StackUtil.distributeDrop(this, drops2);
			this.pumpCharge = 0;
			return true;
		}
		else if (this.inventory[0] != null && this.inventory[0].getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem item = (IFluidContainerItem) this.inventory[0].getItem();
			FluidStack stack = water == Blocks.water ? new FluidStack(FluidRegistry.WATER, 1000) : new FluidStack(FluidRegistry.LAVA, 1000);
			boolean flag = false;
			if (item.fill(this.inventory[0], stack, false) == stack.amount)
			{
				item.fill(this.inventory[0], stack, true);
				this.pumpCharge = 0;
				flag = true;
			}

			if (flag && this.isFull(this.inventory[0], item) || !flag)
			{
				List drops2 = new ArrayList();
				drops2.add(this.inventory[0].copy());
				this.inventory[0] = null;
				StackUtil.distributeDrop(this, drops2);
			}

			return flag;
		}
		else
		{
			this.pumpCharge = 0;
			return this.putInChestBucket(water);
		}
	}

	private boolean isFull(ItemStack par1, IFluidContainerItem par2)
	{
		return par2.getFluid(par1) == null ? false : par2.getFluid(par1).amount >= par2.getCapacity(par1);
	}

	public boolean putInChestBucket(Block water)
	{
		return this.putInChestBucket(this.xCoord, this.yCoord + 1, this.zCoord, water) || this.putInChestBucket(this.xCoord, this.yCoord - 1, this.zCoord, water) || this.putInChestBucket(this.xCoord + 1, this.yCoord, this.zCoord, water) || this.putInChestBucket(this.xCoord - 1, this.yCoord, this.zCoord, water) || this.putInChestBucket(this.xCoord, this.yCoord, this.zCoord + 1, water) || this.putInChestBucket(this.xCoord, this.yCoord, this.zCoord - 1, water);
	}

	public boolean putInChestBucket(int x, int y, int z, Block water)
	{
		if (!(this.worldObj.getTileEntity(x, y, z) instanceof TileEntityChest))
			return false;
		else
		{
			TileEntityChest chest = (TileEntityChest) this.worldObj.getTileEntity(x, y, z);

			for (int i = 0; i < chest.getSizeInventory(); ++i)
				if (chest.getStackInSlot(i) != null && chest.getStackInSlot(i).getItem() == Items.bucket)
				{
					if (water == Blocks.water)
						chest.getStackInSlot(i).func_150996_a(Items.water_bucket);

					if (water == Blocks.lava)
						chest.getStackInSlot(i).func_150996_a(Items.lava_bucket);

					return true;
				}

			return false;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.pumpCharge = nbttagcompound.getShort("pumpCharge");
		this.delay = nbttagcompound.getByte("Delay");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setShort("pumpCharge", this.pumpCharge);
		nbttagcompound.setByte("Delay", this.delay);
	}

	public boolean isPumpReady()
	{
		return this.pumpCharge >= 200;
	}

	public boolean canHarvest()
	{
		return this.isPumpReady() && (this.inventory[0] != null && (this.inventory[0].getItem() == Ic2Items.cell.getItem() || this.inventory[0].getItem() == Items.bucket || this.inventory[0].getItem() instanceof IFluidContainerItem) || this.isBucketInChestAvaible() || this.isGeoAvaible());
	}

	public boolean isBucketInChestAvaible()
	{
		return this.isBucketInChestAvaible(this.xCoord, this.yCoord + 1, this.zCoord) || this.isBucketInChestAvaible(this.xCoord, this.yCoord - 1, this.zCoord) || this.isBucketInChestAvaible(this.xCoord + 1, this.yCoord, this.zCoord) || this.isBucketInChestAvaible(this.xCoord - 1, this.yCoord, this.zCoord) || this.isBucketInChestAvaible(this.xCoord, this.yCoord, this.zCoord + 1) || this.isBucketInChestAvaible(this.xCoord, this.yCoord, this.zCoord - 1);
	}

	public boolean isBucketInChestAvaible(int x, int y, int z)
	{
		if (!(this.worldObj.getTileEntity(x, y, z) instanceof TileEntityChest))
			return false;
		else
		{
			TileEntityChest chest = (TileEntityChest) this.worldObj.getTileEntity(x, y, z);

			for (int i = 0; i < chest.getSizeInventory(); ++i)
				if (chest.getStackInSlot(i) != null && chest.getStackInSlot(i).getItem() == Items.bucket)
					return true;

			return false;
		}
	}

	public boolean deliverLavaToGeo()
	{
		boolean flag = false;
		if (!flag && this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord) instanceof TileEntityGeoGenerator)
		{
			TileEntityGeoGenerator geo = (TileEntityGeoGenerator) this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord);
			if (geo.canTakeBucket())
			{
				flag = true;
				geo.distributeLava(1000);
			}
		}

		if (!flag && this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord) instanceof TileEntityGeoGenerator)
		{
			TileEntityGeoGenerator geo = (TileEntityGeoGenerator) this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);
			if (geo.canTakeBucket())
			{
				flag = true;
				geo.distributeLava(1000);
			}
		}

		if (!flag && this.worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord) instanceof TileEntityGeoGenerator)
		{
			TileEntityGeoGenerator geo = (TileEntityGeoGenerator) this.worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord);
			if (geo.canTakeBucket())
			{
				flag = true;
				geo.distributeLava(1000);
			}
		}

		if (!flag && this.worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord) instanceof TileEntityGeoGenerator)
		{
			TileEntityGeoGenerator geo = (TileEntityGeoGenerator) this.worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord);
			if (geo.canTakeBucket())
			{
				flag = true;
				geo.distributeLava(1000);
			}
		}

		if (!flag && this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1) instanceof TileEntityGeoGenerator)
		{
			TileEntityGeoGenerator geo = (TileEntityGeoGenerator) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1);
			if (geo.canTakeBucket())
			{
				flag = true;
				geo.distributeLava(1000);
			}
		}

		if (!flag && this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1) instanceof TileEntityGeoGenerator)
		{
			TileEntityGeoGenerator geo = (TileEntityGeoGenerator) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1);
			if (geo.canTakeBucket())
			{
				flag = true;
				geo.distributeLava(1000);
			}
		}

		return flag;
	}

	@Override
	public ContainerIC2 getGuiContainer(EntityPlayer entityPlayer)
	{
		return new ContainerPump(entityPlayer, this);
	}

	@Override
	public String getGuiClassName(EntityPlayer entityPlayer)
	{
		return "block.machine.gui.GuiPump";
	}

	@Override
	public void onGuiClosed(EntityPlayer entityPlayer)
	{
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("active") && this.prevActive != this.getActive())
		{
			if (this.audioSource == null)
				this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/PumpOp.ogg", true, false, IC2.audioManager.defaultVolume);

			if (this.getActive())
			{
				if (this.audioSource != null)
					this.audioSource.play();
			}
			else if (this.audioSource != null)
				this.audioSource.stop();
		}

		super.onNetworkUpdate(field);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[] { 0 };
	}

	@Override
	public int getEnergyUsage()
	{
		return 1;
	}
}
