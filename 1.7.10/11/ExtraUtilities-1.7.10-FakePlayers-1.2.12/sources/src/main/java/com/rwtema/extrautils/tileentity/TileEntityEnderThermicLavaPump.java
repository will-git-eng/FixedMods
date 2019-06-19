package com.rwtema.extrautils.tileentity;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.extrautilities.EventConfig;
import ru.will.git.extrautilities.ModUtils;
import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.ExtraUtilsMod;
import com.rwtema.extrautils.LogHelper;
import com.rwtema.extrautils.helper.XUHelper;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Facing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.TileFluidHandler;

@InterfaceList({ @Interface(iface = "buildcraft.api.mj.IBatteryProvider", modid = "BuildCraftAPI|power") })
public class TileEntityEnderThermicLavaPump extends TileFluidHandler implements IFluidHandler, IEnergyHandler
{
	public EntityPlayer owner = null;
	public boolean finished = false;
	private Ticket chunkTicket;
	private FluidTank tank = new FluidTank(1000);
	private int pump_y = -1;
	private int chunk_x = 0;
	private int chunk_z = 0;
	private int b = 0;
	private boolean find_new_block = false;
	private boolean init = false;
	private int chunk_no = -1;
	private float p = 0.95F;
    
    

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote)
			if (this.finished)
			{
				if (this.chunkTicket != null)
				{
					ForgeChunkManager.releaseTicket(this.chunkTicket);
					this.chunkTicket = null;
				}
			}
			else
			{
				boolean goAgain;
				if (this.chunkTicket == null)
				{
					goAgain = false;
					if (ExtraUtils.validDimensionsForEnderPump != null)
					{
						if (ExtraUtils.allNonVanillaDimensionsValidForEnderPump)
							goAgain = true;

						for (int element : ExtraUtils.validDimensionsForEnderPump)
						{
							if (element == worldObj.provider.dimensionId)
							{
								goAgain = !goAgain;
								break;
							}
						}
					}

					if (!goAgain)
					{
						this.finished = true;
						if (this.owner != null)
						{
							this.owner.addChatComponentMessage(new ChatComponentText("Pump will not function in this dimension"));
							this.owner = null;
						}

						this.markDirty();
						return;
					}

					this.chunkTicket = ForgeChunkManager.requestTicket(ExtraUtilsMod.instance, worldObj, Type.NORMAL);
					if (this.chunkTicket == null)
					{
						this.finished = true;
						if (this.owner != null)
						{
							this.owner.addChatComponentMessage(new ChatComponentText("Unable to assign Chunkloader, this pump will not work"));
							this.owner = null;
						}

						this.markDirty();
						return;
					}

					this.owner = null;
					this.chunkTicket.getModData().setString("id", "pump");
					this.chunkTicket.getModData().setInteger("pumpX", xCoord);
					this.chunkTicket.getModData().setInteger("pumpY", yCoord);
					this.chunkTicket.getModData().setInteger("pumpZ", zCoord);
					ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4));
				}

				goAgain = true;

				for (int i = 0; i < 16 && goAgain; ++i)
				{
					goAgain = false;
					int xOffset = this.b >> 4;
					int zOffset = this.b & 15;
					int x = (this.chunk_x << 4) + xOffset;
    
    
					boolean deny = false;
					deny |= EventConfig.enderPumpOnlyPrivate && !EventUtils.isInPrivate(this.worldObj, x, this.pump_y, z);
					deny |= this.fake.cantBreak(x, this.pump_y, z);
    
					{
						if ((this.tank.getInfo().fluid == null || this.tank.getInfo().fluid.amount <= 0) && this.cofhEnergy.extractEnergy(100, true) == 100 && this.cofhEnergy.extractEnergy(100, false) > 0)
						{
							FluidStack fluid = XUHelper.drainBlock(worldObj, x, this.pump_y, z, true);
							this.tank.fill(fluid, true);
							if (worldObj.isAirBlock(x, this.pump_y, z))
								if (worldObj.rand.nextDouble() < this.p)
									worldObj.setBlock(x, this.pump_y, z, Blocks.stone, 0, 2);
								else
									worldObj.setBlock(x, this.pump_y, z, Blocks.cobblestone, 0, 2);

							--this.pump_y;
							this.markDirty();
						}
					}
					else
					{
						goAgain = true;
						if (!this.init)
							this.b = 256;

						++this.b;
						if (this.b >= 256)
						{
							this.b = 0;
							goAgain = false;
							int dx;
							int dz;
							if (this.init && this.chunk_no > 0)
								for (dx = -2; dx <= 2; ++dx)
								{
									for (dz = -2; dz <= 2; ++dz)
									{
										ForgeChunkManager.unforceChunk(this.chunkTicket, new ChunkCoordIntPair(this.chunk_x + dx, this.chunk_z + dz));
									}
								}

							++this.chunk_no;
							this.setChunk(this.chunk_no);
							dx = -2;

							while (true)
							{
								if (dx > 2)
								{
									ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4));
									break;
								}

								for (dz = -2; dz <= 2; ++dz)
								{
									ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(this.chunk_x + dx, this.chunk_z + dz));
									worldObj.getChunkFromChunkCoords(this.chunk_x + dx, this.chunk_z + dz);
								}

								++dx;
							}
						}

						this.pump_y = yCoord - 1;
						this.init = true;
						this.markDirty();
					}
				}

				FluidStack fluid = this.tank.getInfo().fluid;
				if (fluid != null && fluid.amount > 0)
				{
					int[] sides = XUHelper.rndSeq(6, worldObj.rand);

					for (int i = 0; i < 6; ++i)
					{
						int x = xCoord + Facing.offsetsXForSide[sides[i]];
						int y = yCoord + Facing.offsetsYForSide[sides[i]];
						int z = zCoord + Facing.offsetsZForSide[sides[i]];
						TileEntity tile = worldObj.getTileEntity(x, y, z);
						if (tile instanceof IFluidHandler)
    
							if (EventConfig.enderPumpOnlyPrivate && !EventUtils.isInPrivate(this.worldObj, x, y, z))
								continue;

							if (this.fake.cantBreak(x, y, z))
    

							int amount = ((IFluidHandler) tile).fill(ForgeDirection.values()[sides[i]].getOpposite(), fluid, true);
							this.markDirty();
							this.tank.drain(amount, true);
							fluid = this.tank.getInfo().fluid;
							if (fluid == null || fluid.amount <= 0)
								break;
						}
					}
				}
			}
	}

	@Override
	public void invalidate()
	{
		ForgeChunkManager.releaseTicket(this.chunkTicket);
		super.invalidate();
	}

	@Override
	public void onChunkUnload()
	{
	}

	public void setChunk(int chunk_no)
	{
		int base_chunk_x = xCoord >> 4;
		int base_chunk_z = zCoord >> 4;
		if (chunk_no == 0)
		{
			this.chunk_x = base_chunk_x;
			this.chunk_z = base_chunk_z;
		}
		else
		{
			int j = chunk_no - 1;

			for (int k = 1; k <= 5; ++k)
			{
				if (j < 4 * k)
				{
					if (j < k)
					{
						this.chunk_x = base_chunk_x + j;
						this.chunk_z = base_chunk_z + k - j;
					}
					else if (j < 2 * k)
					{
						j -= k;
						this.chunk_x = base_chunk_x + k - j;
						this.chunk_z = base_chunk_z - j;
					}
					else if (j < 3 * k)
					{
						j -= 2 * k;
						this.chunk_x = base_chunk_x - j;
						this.chunk_z = base_chunk_z - (k - j);
					}
					else
					{
						j -= 3 * k;
						this.chunk_x = base_chunk_x - (k - j);
						this.chunk_z = base_chunk_z + j;
					}

					return;
				}

				j -= 4 * k;
			}

			this.finished = true;
			this.markDirty();
			boolean var6 = true;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		if (par1NBTTagCompound.hasKey("block_no") && par1NBTTagCompound.getTag("block_no") instanceof NBTTagInt)
			this.b = par1NBTTagCompound.getInteger("block_no");
		else
			LogHelper.info("Extra Utilities: Problem loading EnderPump TileEntity Tag (block_no)");

		if (par1NBTTagCompound.hasKey("chunk_no") && par1NBTTagCompound.getTag("chunk_no") instanceof NBTTagByte)
			this.chunk_no = par1NBTTagCompound.getByte("chunk_no");
		else
			LogHelper.info("Extra Utilities: Problem loading EnderPump TileEntity Tag (chunk_no)");

		if (this.chunk_no == -128)
			this.finished = true;
		else
			this.setChunk(this.chunk_no);

		this.tank.readFromNBT(par1NBTTagCompound.getCompoundTag("tank"));
    
    
	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setInteger("block_no", this.b);
		if (this.finished)
			par1NBTTagCompound.setByte("chunk_no", (byte) -128);
		else
			par1NBTTagCompound.setByte("chunk_no", (byte) this.chunk_no);

		NBTTagCompound tank_tags = new NBTTagCompound();
		this.tank.writeToNBT(tank_tags);
		par1NBTTagCompound.setTag("tank", tank_tags);
		NBTTagCompound power_tags = new NBTTagCompound();
    
    
	}

	public void forceChunkLoading(Ticket ticket)
	{
		if (this.chunkTicket == null)
			this.chunkTicket = ticket;

		ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4));

		for (int dx = -2; dx <= 2; ++dx)
		{
			for (int dz = -2; dz <= 2; ++dz)
			{
				ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(this.chunk_x + dx, this.chunk_z + dz));
				worldObj.getChunkFromChunkCoords(this.chunk_x + dx, this.chunk_z + dz);
			}
		}

	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (this.finished)
		{
			NBTTagCompound t = new NBTTagCompound();
			t.setBoolean("finished", true);
		}

		return null;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		return this.cofhEnergy.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return this.cofhEnergy.extractEnergy(maxExtract, simulate);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return this.cofhEnergy.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return this.cofhEnergy.getMaxEnergyStored();
	}
}
