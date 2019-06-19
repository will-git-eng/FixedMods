package powercrystals.minefactoryreloaded.tile.tank;

import cofh.core.util.fluid.FluidTankAdv;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.lib.util.position.BlockPosition;
import ru.will.git.minefactoryreloaded.EventConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import powercrystals.minefactoryreloaded.core.IDelayedValidate;
import powercrystals.minefactoryreloaded.core.ITankContainerBucketable;
import powercrystals.minefactoryreloaded.core.MFRUtil;
import powercrystals.minefactoryreloaded.setup.MFRThings;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactory;

import java.util.Arrays;
import java.util.List;

public class TileEntityTank extends TileEntityFactory implements ITankContainerBucketable, IDelayedValidate
{
	public static int CAPACITY = 4000;
	TankNetwork grid;
	FluidTankAdv _tank;
	protected byte sides;

	public TileEntityTank()
	{
		super(null);
		this.setManageFluids(true);
		this._tank = new FluidTankAdv(CAPACITY);
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void invalidate()
	{
		if (this.grid != null)
		{
			for (ForgeDirection var4 : ForgeDirection.VALID_DIRECTIONS)
			{
				if ((this.sides & 1 << var4.ordinal()) != 0)
				{
					TileEntityTank var5 = BlockPosition.getAdjacentTileEntity(this, var4, TileEntityTank.class);
					if (var5 != null)
						var5.part(var4.getOpposite());
				}
			}

			if (this.grid != null)
				this.grid.removeNode(this);

			this.grid = null;
		}

		super.invalidate();
	}

	@Override
	public final boolean isNotValid()
	{
		return this.isInvalid();
	}

	@Override
	public void firstTick()
	{
		if (this.inWorld)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir.offsetY == 0 && BlockPosition.blockExists(this, dir))
				{
					TileEntityTank tank = BlockPosition.getAdjacentTileEntity(this, dir, TileEntityTank.class);
					if (tank != null && tank.grid != null && FluidHelper.isFluidEqualOrNull(tank.grid.getStorage().getFluid(), this._tank.getFluid()) && tank.grid != null && (tank.grid == this.grid || tank.grid.addNode(this)))
					{
						tank.join(dir.getOpposite());
						this.join(dir);
					}
				}
			}

			if (this.grid == null)
				this.grid = new TankNetwork(this);

		}
	}

	@Override
	public void cofh_validate()
	{
		super.cofh_validate();
		if (!this.worldObj.isRemote)
			this.firstTick();
	}

	public void join(ForgeDirection var1)
	{
		this.sides = (byte) (this.sides | 1 << var1.ordinal());
		this.markChunkDirty();
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	public void part(ForgeDirection var1)
	{
		this.sides = (byte) (this.sides & ~(1 << var1.ordinal()));
		this.markChunkDirty();
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	public boolean isInterfacing(ForgeDirection var1)
	{
		return 0 != (this.sides & 1 << var1.ordinal());
	}

	int interfaceCount()
	{
		return Integer.bitCount(this.sides);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (this.grid == null)
			return null;
		NBTTagCompound var1 = new NBTTagCompound();
		FluidStack var2 = this.grid.getStorage().drain(1, false);
		if (var2 != null)
			var1.setTag("fluid", var2.writeToNBT(new NBTTagCompound()));

		var1.setByte("sides", this.sides);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, var1);
	}

	@Override
	public void onDataPacket(NetworkManager var1, S35PacketUpdateTileEntity var2)
	{
		super.onDataPacket(var1, var2);
		NBTTagCompound var3 = var2.func_148857_g();
		switch (var2.func_148853_f())
		{
			case 0:
				FluidStack var4 = FluidStack.loadFluidStackFromNBT(var3.getCompoundTag("fluid"));
				this._tank.setFluid(var4);
				this.sides = var3.getByte("sides");
			default:
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				this.worldObj.func_147451_t(this.xCoord, this.yCoord, this.zCoord);
		}
	}

	@Override
	public String getDataType()
	{
		return "tile.mfr.tank.name";
	}

	@Override
	public void writeItemNBT(NBTTagCompound var1)
	{
		super.writeItemNBT(var1);
		if (this._tank.getFluidAmount() != 0)
			var1.setTag("tank", this._tank.writeToNBT(new NBTTagCompound()));

	}

	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		this._tank.readFromNBT(var1.getCompoundTag("tank"));
	}

	public FluidStack getFluid()
	{
		return this.grid == null ? this._tank.getFluid() : this.grid.getStorage().getFluid();
	}

	@Override
	public int fill(ForgeDirection direction, FluidStack resource, boolean doFill)
	{
		return this.grid == null ? 0 : this.grid.getStorage().fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection direction, FluidStack resource, boolean doDrain)
	{
		return this.grid == null ? null : this.grid.getStorage().drain(resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection direction, int amount, boolean doDrain)
	{
		return this.grid == null ? this.worldObj.isRemote ? this._tank.drain(amount, false) : null : this.grid.getStorage().drain(amount, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection direction, Fluid fluid)
	{
		return this.grid != null;
	}

	@Override
	public boolean canDrain(ForgeDirection direction, Fluid fluid)
	{
		return this.grid != null;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection direction)
	{
		return this.grid == null ? FluidHelper.NULL_TANK_INFO : new FluidTankInfo[] { this.grid.getStorage().getInfo() };
	}

	@Override
	public boolean allowBucketFill(ItemStack var1)
    
		return EventConfig.plasticTankManually && var1.getItem() != MFRThings.plasticTankItem;
	}

	@Override
	public boolean allowBucketDrain(ItemStack var1)
    
    
	}

	@Override
	public void getTileInfo(List<IChatComponent> var1, ForgeDirection var2, EntityPlayer var3, boolean var4)
	{
		if (var4)
		{
			var1.add(new ChatComponentText("Grid: " + this.grid));
			if (this.grid != null)
				var1.add(new ChatComponentText(Arrays.toString(this.grid.getStorage().tanks)));
		}

		if (this.grid == null)
		{
			var1.add(new ChatComponentText("Null Grid!!"));
			if (var4)
				var1.add(new ChatComponentText("FluidForGrid: " + StringHelper.getFluidName(this._tank.getFluid(), "") + "@" + this._tank.getFluidAmount()));

		}
		else
		{
			if (this.grid.getStorage().getFluidAmount() == 0)
				var1.add(new ChatComponentText(MFRUtil.empty()));
			else
				var1.add(new ChatComponentText(MFRUtil.getFluidName(this.grid.getStorage().getFluid())));

			var1.add(new ChatComponentText((float) this.grid.getStorage().getFluidAmount() / (float) this.grid.getStorage().getCapacity() * 100.0F + "%"));
			if (var4)
			{
				var1.add(new ChatComponentText("Sides: " + Integer.toBinaryString(this.sides)));
				var1.add(new ChatComponentText(this.grid.getStorage().getFluidAmount() + " / " + this.grid.getStorage().getCapacity()));
				var1.add(new ChatComponentText("Size: " + this.grid.getSize() + " | FluidForGrid: " + StringHelper.getFluidName(this._tank.getFluid(), "") + "@" + this._tank.getFluidAmount()));
				var1.add(new ChatComponentText("Length: " + this.grid.getStorage().length + " | Index: " + this.grid.getStorage().index + " | Reserve: " + this.grid.getStorage().tanks.length));
			}

		}
	}
}
