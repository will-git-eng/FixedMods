package powercrystals.minefactoryreloaded.tile.machine;

import cofh.core.util.fluid.FluidTankAdv;
import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.api.IFactoryLaserTarget;
import powercrystals.minefactoryreloaded.core.ITankContainerBucketable;
import powercrystals.minefactoryreloaded.gui.client.GuiChunkLoader;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.container.ContainerChunkLoader;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryPowered;
import powercrystals.minefactoryreloaded.net.ConnectionHandler;
import powercrystals.minefactoryreloaded.setup.MFRConfig;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

import java.lang.reflect.Field;

public class TileEntityChunkLoader extends TileEntityFactoryPowered
		implements ITankContainerBucketable, IFactoryLaserTarget
{
	protected static TObjectIntHashMap<String> fluidConsumptionRate = new TObjectIntHashMap();
	protected short _radius = 0;
	protected boolean activated;
	protected boolean unableToRequestTicket;
	public boolean useAltPower = MFRConfig.enableConfigurableCLEnergy.getBoolean(false);
	protected Ticket _ticket;
	protected int consumptionTicks;
	protected int emptyTicks;
	protected int prevEmpty;
	protected int unactivatedTicks;

	private static void bypassLimit(Ticket var0)
	{
		try
		{
			Field var1 = Ticket.class.getDeclaredField("maxDepth");
			var1.setAccessible(true);
			var1.setInt(var0, 32767);
		}
		catch (Throwable var2)
		{
		}

	}

	public TileEntityChunkLoader()
	{
		super(Machine.ChunkLoader);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiChunkLoader(this.getContainer(var1), this);
	}

	@Override
	public ContainerFactoryPowered getContainer(InventoryPlayer var1)
	{
		if (this.unableToRequestTicket && var1.player.getCommandSenderName().equals(this._owner))
		{
			var1.player.addChatMessage(new ChatComponentTranslation("chat.info.mfr.chunkloader.noticket"));
		}

		return new ContainerChunkLoader(this, var1);
	}

	@Override
	protected FluidTankAdv[] createTanks()
	{
		return new FluidTankAdv[] { new FluidTankAdv(10000) };
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		if (this._ticket != null)
		{
			this.unforceChunks();
			ForgeChunkManager.releaseTicket(this._ticket);
		}

	}

	public void setRadius(short var1)
	{
		int var2 = 38;
		if (this._ticket != null)
		{
			var2 = Math.min((int) Math.sqrt((double) this._ticket.getChunkListDepth() / 3.141592653589793D), var2);
		}

		if (!(var1 < 0 | var1 > var2 | var1 == this._radius))
		{
			this._radius = var1;
			this.markDirty();
			if (this.worldObj != null && !this.worldObj.isRemote)
			{
				this.forceChunks();
			}

		}
	}

	@Override
	protected boolean activateMachine()
	{
		this.activated = true;
		this.unactivatedTicks = 0;
		if (this.consumptionTicks > 0)
		{
			--this.consumptionTicks;
		}
		else
		{
			this.emptyTicks = Math.min('\uffff', this.emptyTicks + 1);
			FluidStack var1 = this._tanks[0].getFluid();
			if (this.drain(this._tanks[0], 1, true) == 1)
			{
				this.consumptionTicks = fluidConsumptionRate.get(this.getFluidName(var1));
				this.emptyTicks = Math.max(-65535, this.emptyTicks - 2);
			}
		}

		return true;
	}

	@Override
	public void updateEntity()
    
		if (this.worldObj == null)
    

		if (!this._owner.isEmpty())
		{
			if (this.unableToRequestTicket)
			{
				this.setIdleTicks(this.getIdleTicksMax());
				super.setIsActive(false);
			}
			else
			{
				this.activated = false;
				if (!this.worldObj.isRemote && MFRConfig.enableChunkLoaderRequiresOwner.getBoolean(false) && !ConnectionHandler.onlinePlayerMap.containsKey(this._owner))
				{
					this.setIdleTicks(this.getIdleTicksMax());
				}

				super.updateEntity();
				if (!this.worldObj.isRemote)
				{
					if (this.getIdleTicks() > 0)
					{
						if (this._ticket != null)
						{
							this.unforceChunks();
						}

					}
					else
					{
						if (!this.activated)
						{
							if (this._ticket != null)
							{
								ImmutableSet<ChunkCoordIntPair> var1 = this._ticket.getChunkList();
								if (var1.size() != 0)
								{
									label146:
									{
										this.unactivatedTicks = Math.min(this._tanks[0].getCapacity() + 10, this.unactivatedTicks + 1);
										if (this.consumptionTicks > 0)
										{
											this.consumptionTicks /= 10;
										}
										else
										{
											this.emptyTicks = Math.min('\uffff', this.emptyTicks + 1);
											FluidStack var2 = this._tanks[0].getFluid();
											if (this.drain(this._tanks[0], Math.min(this.unactivatedTicks, this._tanks[0].getFluidAmount()), true) == this.unactivatedTicks)
											{
												this.consumptionTicks = fluidConsumptionRate.get(this.getFluidName(var2));
												this.consumptionTicks = Math.max(0, this.consumptionTicks - this.unactivatedTicks);
												this.activated = this.emptyTicks == 1 && this.unactivatedTicks < this._tanks[0].getCapacity();
												this.emptyTicks = Math.max(-65535, this.emptyTicks - 2);
												if (this.activated)
												{
													break label146;
												}
											}
										}

										for (ChunkCoordIntPair var3 : var1)
										{
											ForgeChunkManager.unforceChunk(this._ticket, var3);
										}
									}
								}
							}
						}
						else if (this.activated & !this.isActive())
						{
							if (this._ticket == null)
							{
								this._ticket = ForgeChunkManager.requestPlayerTicket(MineFactoryReloadedCore.instance(), this._owner, this.worldObj, Type.NORMAL);
								if (this._ticket == null)
								{
									this.unableToRequestTicket = true;
									return;
								}

								this._ticket.getModData().setInteger("X", this.xCoord);
								this._ticket.getModData().setInteger("Y", this.yCoord);
								this._ticket.getModData().setInteger("Z", this.zCoord);
							}

							this.forceChunks();
						}

						if (this.prevEmpty != this.emptyTicks)
						{
							this.prevEmpty = this.emptyTicks;
							this.onFactoryInventoryChanged();
						}

						super.setIsActive(this.activated);
					}
				}
			}
		}
	}

	protected void unforceChunks()
    
		if (this._ticket.world == null)
    

		ImmutableSet<ChunkCoordIntPair> chunkList = this._ticket.getChunkList();
		if (chunkList.size() != 0)
		{
			for (ChunkCoordIntPair chunk : chunkList)
			{
				ForgeChunkManager.unforceChunk(this._ticket, chunk);
			}
		}
	}

	protected void forceChunks()
	{
		if (this._ticket != null)
		{
			if (MFRConfig.enableChunkLimitBypassing.getBoolean(false))
			{
				bypassLimit(this._ticket);
			}

			ImmutableSet<ChunkCoordIntPair> var1 = this._ticket.getChunkList();
			int var2 = this.xCoord >> 4;
			int var3 = this.zCoord >> 4;
			int var4 = this._radius * this._radius;

			for (ChunkCoordIntPair var6 : var1)
			{
				int var7 = var6.chunkXPos - var2;
				int var8 = var6.chunkZPos - var3;
				if (var7 * var7 + var8 * var8 > var4)
				{
					ForgeChunkManager.unforceChunk(this._ticket, var6);
				}
			}

			for (int var9 = -this._radius; var9 <= this._radius; ++var9)
			{
				int var10 = var9 * var9;

				for (int var11 = -this._radius; var11 <= this._radius; ++var11)
				{
					if (var10 + var11 * var11 <= var4)
					{
						ChunkCoordIntPair var12 = new ChunkCoordIntPair(var2 + var9, var3 + var11);
						if (!var1.contains(var12))
						{
							ForgeChunkManager.forceChunk(this._ticket, var12);
						}
					}
				}
			}

		}
	}

	@Override
	protected void onFactoryInventoryChanged()
	{
		super.onFactoryInventoryChanged();
		if (!this.isInvalid())
		{
			int var1 = this._radius + 1;
			int var3 = this._radius * this._radius;
			int var2;
			if (this._ticket == null)
			{
				var2 = 0;

				for (int var4 = -this._radius; var4 <= this._radius; ++var4)
				{
					int var5 = var4 * var4;

					for (int var6 = -this._radius; var6 <= this._radius; ++var6)
					{
						if (var5 + var6 * var6 <= var3)
						{
							++var2;
						}
					}
				}
			}
			else
			{
				var2 = this._ticket.getChunkList().size();
			}

			int var8;
			if (this.useAltPower)
			{
				int var11 = (var3 + 1) * var2 * 16 * this._machine.getActivationEnergy();
				var11 = var11 & ~var11 >> 31;
				var8 = var11;
				var2 *= 16;
			}
			else
			{
				double var13 = (double) (var1 * var1 * 32 - 17 + var1 * var1 * var1);

				for (int var7 = var1 / 10; var7-- > 0; var13 *= (double) var1 / 6.0D)
				{
				}

				var8 = (int) (var13 * 10.0D);
			}

			var8 = var8 + (int) (StrictMath.cbrt((double) this.emptyTicks) * (double) var2);
			var8 = var8 & ~var8 >> 31;
			if (var8 == 0)
			{
				var8 = 1;
			}

			this.setActivationEnergy(var8);
		}
	}

	public boolean receiveTicket(Ticket ticket)
    
		if (ticket.world == null)
    

		if (MFRConfig.enableChunkLoaderRequiresOwner.getBoolean(false) && !ConnectionHandler.onlinePlayerMap.containsKey(this._owner))
		{
			this._ticket = ticket;
			this.unforceChunks();
			this._ticket = null;
			return false;
		}
		else
		{
			if (this._ticket == null)
			{
    
				if (this.worldObj == null)
				{
					this.unableToRequestTicket = true;
					return true;
    

				this._ticket = ForgeChunkManager.requestPlayerTicket(MineFactoryReloadedCore.instance(), this._owner, this.worldObj, Type.NORMAL);
				if (this._ticket == null)
				{
					this.unableToRequestTicket = true;
					return true;
				}

				this._ticket.getModData().setInteger("X", this.xCoord);
				this._ticket.getModData().setInteger("Y", this.yCoord);
				this._ticket.getModData().setInteger("Z", this.zCoord);
			}

			return true;
		}
	}

	@Override
	public void writePortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		var2.setShort("radius", this._radius);
	}

	@Override
	public void readPortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		this.setRadius(var2.getShort("radius"));
	}

	@Override
	public void writeToNBT(NBTTagCompound var1)
	{
		super.writeToNBT(var1);
		var1.setShort("radius", this._radius);
		var1.setInteger("empty", this.emptyTicks);
		var1.setInteger("inactive", this.unactivatedTicks);
		var1.setInteger("consumed", this.consumptionTicks);
	}

	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		this.setRadius(var1.getShort("radius"));
		this.emptyTicks = var1.getInteger("empty");
		this.unactivatedTicks = var1.getInteger("inactive");
		this.consumptionTicks = var1.getInteger("consumed");
		this.onFactoryInventoryChanged();
	}

	protected boolean isFluidFuel(FluidStack var1)
	{
		String var2 = this.getFluidName(var1);
		return var2 != null && fluidConsumptionRate.containsKey(var2);
	}

	@Override
	public int fill(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		if (!this.unableToRequestTicket & var2 != null && this.isFluidFuel(var2))
		{
			for (FluidTankAdv var7 : this.getTanks())
			{
				if (var7.getFluidAmount() == 0 || var2.isFluidEqual(var7.getFluid()))
				{
					return var7.fill(var2, var3);
				}
			}
		}

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection var1, int var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	public FluidStack drain(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	public boolean allowBucketFill(ItemStack var1)
	{
		return !this.unableToRequestTicket;
	}

	@Override
	public boolean allowBucketDrain(ItemStack var1)
	{
		return true;
	}

	@Override
	public boolean canFill(ForgeDirection var1, Fluid var2)
	{
		return !this.unableToRequestTicket;
	}

	@Override
	public boolean canDrain(ForgeDirection var1, Fluid var2)
	{
		return this.unableToRequestTicket;
	}

	protected String getFluidName(FluidStack var1)
	{
		if (var1 != null && var1.getFluid() != null)
		{
			return var1.getFluid().getName();
		}
		else
		{
			return null;
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 0;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 40;
	}

	public short getRadius()
	{
		return this._radius;
	}

	public boolean getUnableToWork()
	{
		return this.unableToRequestTicket;
	}

	@SideOnly(Side.CLIENT)
	public void setEmpty(int var1)
	{
		this.emptyTicks = var1;
		this.onFactoryInventoryChanged();
	}

	public short getEmpty()
	{
		return (short) this.emptyTicks;
	}

	@Override
	public void setIsActive(boolean var1)
	{
	}

	@Override
	public boolean canFormBeamWith(ForgeDirection var1)
	{
		return true;
	}

	@Override
	public int addEnergy(ForgeDirection var1, int var2, boolean var3)
	{
		return this.storeEnergy(var2, !var3);
	}

	static
	{
		fluidConsumptionRate.put("mobessence", 10);
		fluidConsumptionRate.put("liquidessence", 20);
		fluidConsumptionRate.put("ender", 40);
	}
}
