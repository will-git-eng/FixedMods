package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import blusunrize.immersiveengineering.common.items.ItemGraphiteElectrode;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import com.google.common.base.Objects;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class TileEntityArcFurnace extends TileEntityMultiblockPart implements IEnergyReceiver, ISidedInventory
{
	public int facing = 3;
	public EnergyStorage energyStorage = new EnergyStorage(64000);
	public ItemStack[] inventory = new ItemStack[26];
	public int[] process = new int[12];
	public int[] processMax = new int[12];
	public boolean active = false;
	public boolean[] electrodes = new boolean[3];
	@SideOnly(Side.CLIENT)
	public int pouringMetal;
	public boolean computerControlled;
	public boolean computerOn;

	@Override
	public TileEntityArcFurnace master()
	{
		if (this.offset[0] == 0 && this.offset[1] == 0 && this.offset[2] == 0)
			return null;
		TileEntity te = this.worldObj.getTileEntity(this.xCoord - this.offset[0], this.yCoord - this.offset[1], this.zCoord - this.offset[2]);
		return te instanceof TileEntityArcFurnace ? (TileEntityArcFurnace) te : null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if (this.pos < 0)
			return null;
		ItemStack s = MultiblockArcFurnace.instance.getStructureManual()[this.pos / 25][this.pos % 5][this.pos % 25 / 5];
		return s != null ? s.copy() : null;
	}

	@Override
	public void updateEntity()
	{
		if (!this.formed || this.pos != 62)
			return;

		if (this.worldObj.isRemote)
		{
			if (this.pouringMetal > 0)
				this.pouringMetal--;
			if (this.active)
				for (int i = 0; i < 4; i++)
				{
					if (this.worldObj.rand.nextInt(6) == 0)
						ImmersiveEngineering.proxy.spawnSparkFX(this.worldObj, this.xCoord + .5 + (this.facing == 4 ? -.25 : this.facing == 5 ? .25 : 0), this.yCoord + 2, this.zCoord + .5 + (this.facing == 2 ? -.25 : this.facing == 3 ? .25 : 0), this.worldObj.rand.nextDouble() * .05 - .025, .025, this.worldObj.rand.nextDouble() * .05 - .025);
					if (this.worldObj.rand.nextInt(6) == 0)
						ImmersiveEngineering.proxy.spawnSparkFX(this.worldObj, this.xCoord + .5 + (this.facing == 4 ? .25 : this.facing == 5 ? -.25 : .25), this.yCoord + 2, this.zCoord + .5 + (this.facing == 2 ? .25 : this.facing == 3 ? -.25 : -.25), this.worldObj.rand.nextDouble() * .05 - .025, .025, this.worldObj.rand.nextDouble() * .05 - .025);
					if (this.worldObj.rand.nextInt(6) == 0)
						ImmersiveEngineering.proxy.spawnSparkFX(this.worldObj, this.xCoord + .5 + (this.facing == 4 ? .25 : this.facing == 5 ? -.25 : -.25), this.yCoord + 2, this.zCoord + .5 + (this.facing == 2 ? .25 : this.facing == 3 ? -.25 : .25), this.worldObj.rand.nextDouble() * .05 - .025, .025, this.worldObj.rand.nextDouble() * .05 - .025);
				}
			return;
		}
    
		boolean updateClient = true;

		boolean hasElectrodes = true;
		for (int i = 0; i < 3; i++)
		{
			boolean b = this.getStackInSlot(23 + i) != null;
			if (this.electrodes[i] != b)
				update = true;
			this.electrodes[i] = b;
			if (!this.electrodes[i])
			{
				hasElectrodes = false;
				if (this.active)
				{
					this.active = false;
					update = true;
				}
			}
		}
		boolean enabled;
		if (this.computerControlled)
			enabled = this.computerOn;
		else
			enabled = !this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord + (this.facing == 4 ? -2 : this.facing == 5 ? 2 : this.facing == (this.mirrored ? 2 : 3) ? -2 : 2), this.yCoord - 1, this.zCoord + (this.facing == 2 ? -2 : this.facing == 3 ? 2 : this.facing == (this.mirrored ? 5 : 4) ? -2 : 2));
		if (!enabled)
		{
			if (this.active)
			{
				this.active = false;
				update = true;
			}
		}
		else
		{
			if (hasElectrodes)
			{
				ItemStack[] additives = new ItemStack[4];
				for (int i = 0; i < 4; i++)
				{
					additives[i] = this.inventory[12 + i] != null ? this.inventory[12 + i].copy() : null;
				}
				boolean damageElectrodes = false;
				boolean allEmpty = true;
				for (int i = 0; i < 12; i++)
				{
					ArcFurnaceRecipe recipe = ArcFurnaceRecipe.findRecipe(this.getStackInSlot(i), additives);
					if (recipe != null)
					{
						int inputSize = recipe.input instanceof ItemStack ? ((ItemStack) recipe.input).stackSize : 1;
						if (this.getStackInSlot(i).stackSize < inputSize)
							continue;
						int[] outputSlots = null;
						ItemStack[] outUpToNow = new ItemStack[6];
						ItemStack[] outputs = recipe.getOutputs(this.getStackInSlot(i), additives);
						if (outputs != null && outputs.length > 0)
						{
							boolean spaceForOutput = true;
							outputSlots = new int[outputs.length];
							for (int iOut = 0; iOut < outputs.length; iOut++)
							{
								outputSlots[iOut] = -1;
								ItemStack out = outputs[iOut];
								boolean b0 = false;
								for (int j = 16; j < 22; j++)
								{
									int outSize = out.stackSize;
									int maxSize = out.getMaxStackSize();
									if (this.inventory[j] != null)
									{
										outSize += this.inventory[j].stackSize;
										maxSize = Math.min(maxSize, this.inventory[j].getMaxStackSize());
									}
									if (outUpToNow[j - 16] != null)
									{
										outSize += outUpToNow[j - 16].stackSize;
										maxSize = Math.min(maxSize, outUpToNow[j - 16].getMaxStackSize());
									}
									boolean matches = true;
									if (this.inventory[j] != null && !OreDictionary.itemMatches(this.inventory[j], out, false))
										matches = false;
									else if (outUpToNow[j - 16] != null && !OreDictionary.itemMatches(outUpToNow[j - 16], out, false))
										matches = false;
									if (matches && outSize <= maxSize)
									{
										b0 = true;
										outputSlots[iOut] = j;
										if (outUpToNow[j - 16] == null)
											outUpToNow[j - 16] = out.copy();
										else
											outUpToNow[j - 16].stackSize += out.stackSize;
										break;
									}
								}
								if (!b0)
								{
									spaceForOutput = false;
									break;
								}
							}
							if (!spaceForOutput)
								continue;
						}
						else
							continue;
						if (recipe.slag != null && !(this.inventory[22] == null || OreDictionary.itemMatches(this.inventory[22], recipe.slag, false) && this.inventory[22].stackSize + recipe.slag.stackSize <= this.inventory[22].getMaxStackSize()))
							continue;

						if (this.processMax[i] != recipe.time)
						{
							this.processMax[i] = recipe.time;
							this.process[i] = 0;
							update = true;
						}
						else
						{
							int energy = recipe.energyPerTick;
							if (this.energyStorage.extractEnergy(energy, true) >= energy)
							{
								this.energyStorage.extractEnergy(energy, false);
								this.process[i]++;
								damageElectrodes = true;
								if (!this.active)
									this.active = true;
								if (this.process[i] < this.processMax[i])
									allEmpty = false;
							}
							if (this.process[i] >= this.processMax[i])
							{
								this.decrStackSize(i, inputSize);
								for (Object add : recipe.additives)
								{
									int takenAdd = add instanceof ItemStack ? ((ItemStack) add).stackSize : 1;
									for (int j = 12; j < 16; j++)
									{
										if (this.getStackInSlot(j) != null && ApiUtils.stackMatchesObject(this.getStackInSlot(j), add))
										{
											int t = Math.min(takenAdd, this.getStackInSlot(j).stackSize);
											this.decrStackSize(j, t);
											takenAdd -= t;
											if (takenAdd <= 0)
												break;
										}
									}
								}

								this.processMax[i] = 0;
								this.process[i] = 0;
								for (int iOut = 0; iOut < outputs.length; iOut++)
								{
									if (outputs[iOut] != null && outputSlots[iOut] != -1)
									{
										int outputSlot = outputSlots[iOut];
										synchronized (this.inventory)
										{
											if (this.inventory[outputSlot] != null)
												this.inventory[outputSlot].stackSize += outputs[iOut].stackSize;
											else if (this.inventory[outputSlot] == null)
												this.inventory[outputSlot] = outputs[iOut].copy();
										}
										this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 0, 40);
									}
								}
								if (recipe.slag != null)
								{
									synchronized (this.inventory)
									{
										if (this.inventory[22] != null)
											this.inventory[22].stackSize += recipe.slag.stackSize;
										else if (this.inventory[22] == null)
											this.inventory[22] = recipe.slag.copy();
									}
								}
								for (int i2 = 0; i2 < 4; i2++)
								{
									additives[i2] = this.inventory[12 + i2] != null ? this.inventory[12 + i2].copy() : null;
								}
								recipe = ArcFurnaceRecipe.findRecipe(this.inventory[i], additives);
								if (recipe != null)
								{
									allEmpty = false;
									this.processMax[i] = recipe.time;
								}
							}
							update = true;
							updateClient = false;
						}

					}
					else if (this.process[i] > 0)
						this.process[i] = 0;
				}
				if (allEmpty && this.active)
				{
					this.active = false;
					update = true;
					updateClient = true;
				}
				if (damageElectrodes)
				{
					for (int i = 23; i < 26; i++)
					{
						ItemStack electrode = this.getStackInSlot(i);
						if (electrode.getItem() instanceof ItemGraphiteElectrode)
						{
							ItemGraphiteElectrode elec = (ItemGraphiteElectrode) electrode.getItem();
							elec.damage(electrode, 1);
							if (elec.getDurabilityForDisplay(electrode) >= 1)
							{
								this.setInventorySlotContents(i, null);
								updateClient = true;
								update = true;
							}
						}
					}
				}
				else if (this.active)
				{
					this.active = false;
					update = true;
				}
			}

			if (this.worldObj.getTotalWorldTime() % 8 == 0)
			{
				TileEntity inventoryFront = this.worldObj.getTileEntity(this.xCoord + (this.facing == 4 ? -3 : this.facing == 5 ? 3 : 0), this.yCoord - 2, this.zCoord + (this.facing == 2 ? -3 : this.facing == 3 ? 3 : 0));
				for (int j = 16; j < 22; j++)
				{
					if (this.getStackInSlot(j) != null)
					{
						ItemStack stack = Utils.copyStackWithAmount(this.getStackInSlot(j), 1);
						if (inventoryFront instanceof ISidedInventory && ((ISidedInventory) inventoryFront).getAccessibleSlotsFromSide(ForgeDirection.OPPOSITES[this.facing]).length > 0 || inventoryFront instanceof IInventory && ((IInventory) inventoryFront).getSizeInventory() > 0)
							stack = Utils.insertStackIntoInventory((IInventory) inventoryFront, stack, ForgeDirection.OPPOSITES[this.facing]);
						if (stack == null)
						{
							this.decrStackSize(j, 1);
							break;
						}
					}
				}
				TileEntity inventoryBack = this.worldObj.getTileEntity(this.xCoord + (this.facing == 4 ? 3 : this.facing == 5 ? -3 : 0), this.yCoord - 2, this.zCoord + (this.facing == 2 ? 3 : this.facing == 3 ? -3 : 0));
				if (this.getStackInSlot(22) != null)
				{
					ItemStack stack = Utils.copyStackWithAmount(this.getStackInSlot(22), 1);
					if (inventoryBack instanceof ISidedInventory && ((ISidedInventory) inventoryBack).getAccessibleSlotsFromSide(ForgeDirection.OPPOSITES[this.facing]).length > 0 || inventoryBack instanceof IInventory && ((IInventory) inventoryBack).getSizeInventory() > 0)
						stack = Utils.insertStackIntoInventory((IInventory) inventoryBack, stack, this.facing);
					if (stack == null)
						this.decrStackSize(22, 1);
				}
			}
		}

		if (update)
		{
			this.markDirty();
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			if (updateClient)
				this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), -1, -1);
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		this.facing = nbt.getInteger("facing");
		this.energyStorage.readFromNBT(nbt);
		this.process = nbt.getIntArray("process");
		if (this.process == null || this.process.length < 12)
			this.process = new int[12];
		this.processMax = nbt.getIntArray("processMax");
		if (this.processMax == null || this.processMax.length < 12)
			this.processMax = new int[12];
		this.active = nbt.getBoolean("active");
		for (int i = 0; i < this.electrodes.length; i++)
		{
			this.electrodes[i] = nbt.getBoolean("electrodes" + i);
		}

		if (!descPacket)
			this.inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 26);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", this.facing);
		this.energyStorage.writeToNBT(nbt);
		nbt.setIntArray("process", this.process);
		nbt.setIntArray("processMax", this.processMax);
		nbt.setBoolean("active", this.active);
		for (int i = 0; i < this.electrodes.length; i++)
		{
			nbt.setBoolean("electrodes" + i, this.electrodes[i]);
		}

		if (!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(this.inventory));
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if ((id == -1 || id == 255) && this.worldObj.isRemote)
		{
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			return true;
		}
		if (id == 0 && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			if (this.pouringMetal <= 0)
				this.pouringMetal = arg;
		return true;
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if (this.renderAABB == null)
			if (this.pos == 62)
				this.renderAABB = AxisAlignedBB.getBoundingBox(this.xCoord - 2, this.yCoord - 2, this.zCoord - 2, this.xCoord + 3, this.yCoord + 3, this.zCoord + 3);
			else
				this.renderAABB = AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord, this.yCoord, this.zCoord);
		return this.renderAABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared() * Config.getDouble("increasedTileRenderdistance");
	}

	@Override
	public float[] getBlockBounds()
	{
		int fl = this.facing;
		int fw = this.facing;
		if (this.mirrored)
			fw = ForgeDirection.OPPOSITES[fw];

		if (this.pos < 25)
		{
			if (this.pos == 1 || this.pos == 3)
				return new float[] { fl == 4 ? .4375f : 0, 0, fl == 2 ? .4375f : 0, fl == 5 ? .5625f : 1, .5f, fl == 3 ? .5625f : 1 };
			else if (this.pos != 0 && this.pos != 2 && this.pos < 20 && (this.pos <= 9 || this.pos % 5 != 0 && this.pos % 5 != 4))
				return new float[] { 0, 0, 0, 1, .5f, 1 };
		}
		else if (this.pos == 25)
			return new float[] { fl == 5 ? .5f : 0, 0, fl == 3 ? .5f : 0, fl == 4 ? .5f : 1, 1, fl == 2 ? .5f : 1 };
		else if (this.pos == 40 || this.pos == 44)
		{
			if (this.pos == 44)
				fl += fl % 2 == 0 ? 1 : -1;
			return new float[] { fl == 3 ? .125f : fl == 2 ? .625f : 0, .125f, fl == 4 ? .125f : fl == 5 ? .625f : 0, fl == 2 ? .875f : fl == 3 ? .375f : 1, 0.375F, fl == 5 ? .875f : fl == 4 ? .375f : 1 };
		}
		else if (this.pos < 50)
		{
			if (this.pos % 5 == 0)
				return new float[] { fw == 3 ? .125f : 0, 0, fw == 4 ? .125f : 0, fw == 2 ? .875f : 1, this.pos % 25 / 5 == 3 ? .5f : 1, fw == 5 ? .875f : 1 };
			else if (this.pos % 5 == 4)
				return new float[] { fw == 2 ? .125f : 0, 0, fw == 5 ? .125f : 0, fw == 3 ? .875f : 1, this.pos % 25 / 5 == 3 ? .5f : 1, fw == 4 ? .875f : 1 };
			else if (this.pos == 43)
				return new float[] { fl == 4 || fl == 3 ? 0 : .125f, 0.5f, fl == 2 || fl == 4 ? 0 : .125f, fl == 5 || fl == 2 ? 1 : .875f, 1, fl == 3 || fl == 5 ? 1 : .875f };
			else if (this.pos == 41)
				return new float[] { fl == 3 || fl == 4 ? 0 : .125f, 0.5f, fl == 2 || fl == 5 ? 0 : .125f, fl == 2 || fl == 5 ? 1 : .875f, 1, fl == 4 || fl == 3 ? 1 : .875f };
			else if (this.pos > 48 || this.pos < 46)
				return new float[] { 0, .5f, 0, 1, 1, 1 };
		}
		else if (this.pos < 75)
		{
			if (this.pos == 51)
				return new float[] { fw == 3 ? .5f : 0, 0, fw == 4 ? .5f : 0, fw == 2 ? .5f : 1, 1, fw == 5 ? .5f : 1 };
			else if (this.pos == 53)
				return new float[] { fw == 2 ? .5f : 0, 0, fw == 5 ? .5f : 0, fw == 3 ? .5f : 1, 1, fw == 4 ? .5f : 1 };
			else if (this.pos == 60)
				return new float[] { fw == 3 ? .125f : 0, 0, fw == 4 ? .125f : 0, fw == 2 ? .875f : 1, 1, fw == 5 ? .875f : 1 };
			else if (this.pos == 64)
				return new float[] { fw == 2 ? .375f : 0, 0, fw == 5 ? .375f : 0, fw == 3 ? .625f : 1, 1, fw == 4 ? .625f : 1 };
			else if (this.pos == 70)
				return new float[] { fw == 3 ? .5f : 0, 0, fw == 4 ? .5f : 0, fw == 2 ? .5f : 1, 1, fw == 5 ? .5f : 1 };
			else if (this.pos == 74)
				return new float[] { fw == 2 ? .5f : 0, 0, fw == 5 ? .5f : 0, fw == 3 ? .5f : 1, 1, fw == 4 ? .5f : 1 };
			else
				return new float[] { 0, 0, 0, 1, 1, 1 };
		}
		else if (this.pos == 112)
			return new float[] { fl == 4 ? .0625f : 0, 0, fl == 2 ? .0625f : 0, fl == 5 ? .9375f : 1, 1, fl == 3 ? .9375f : 1 };
		else if (this.pos == 117)
			return new float[] { 0, .6875f, 0, 1, 1, 1 };
		else if (this.pos % 25 / 5 == 4)
		{
			if (this.pos % 5 == 1)
				return new float[] { fw == 3 ? .5f : 0, 0, fw == 4 ? .5f : 0, fw == 2 ? .5f : 1, 1, fw == 5 ? .5f : 1 };
			else if (this.pos % 5 == 2)
				return new float[] { fl == 5 ? .25f : 0, 0, fl == 3 ? .25f : 0, fl == 4 ? .75f : 1, 1, fl == 2 ? .75f : 1 };
			else if (this.pos % 5 == 3)
				return new float[] { fw == 2 ? .5f : 0, 0, fw == 5 ? .5f : 0, fw == 3 ? .5f : 1, 1, fw == 4 ? .5f : 1 };
		}
		return new float[] { 0, 0, 0, 1, 1, 1 };
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if (this.formed && !this.worldObj.isRemote)
		{
			int f = this.facing;
			TileEntity master = this.master();
			if (master == null)
				master = this;

			int startX = master.xCoord;
			int startY = master.yCoord;
			int startZ = master.zCoord;

			for (int h = -2; h <= 2; h++)
			{
				for (int l = -2; l <= 2; l++)
				{
					for (int w = -2; w <= 2; w++)
					{
						int xx = f == 4 ? l : f == 5 ? -l : f == 2 ? -w : w;
						int yy = h;
						int zz = f == 2 ? l : f == 3 ? -l : f == 5 ? -w : w;

						ItemStack s = null;
						TileEntity te = this.worldObj.getTileEntity(startX + xx, startY + yy, startZ + zz);
						if (te instanceof TileEntityArcFurnace)
						{
							s = ((TileEntityArcFurnace) te).getOriginalBlock();
							((TileEntityArcFurnace) te).formed = false;
						}
						if (startX + xx == this.xCoord && startY + yy == this.yCoord && startZ + zz == this.zCoord)
							s = this.getOriginalBlock();
						if (s != null && Block.getBlockFromItem(s.getItem()) != null)
						{
							if (startX + xx == this.xCoord && startY + yy == this.yCoord && startZ + zz == this.zCoord)
								this.worldObj.spawnEntityInWorld(new EntityItem(this.worldObj, this.xCoord + .5, this.yCoord + .5, this.zCoord + .5, s));
							else
							{
								if (s.getItem() == Items.cauldron)
									this.worldObj.setBlock(startX + xx, startY + yy, startZ + zz, Blocks.cauldron);
								else
								{
									if (Block.getBlockFromItem(s.getItem()) == IEContent.blockMetalMultiblocks)
										this.worldObj.setBlockToAir(startX + xx, startY + yy, startZ + zz);
									this.worldObj.setBlock(startX + xx, startY + yy, startZ + zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public int getSizeInventory()
	{
		if (!this.formed)
			return 0;
		return this.inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if (!this.formed)
			return null;
		TileEntityArcFurnace master = this.master();
		if (master != null)
			return master.getStackInSlot(slot);
		if (slot < this.inventory.length)
			return this.inventory[slot];
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if (!this.formed)
			return null;
		TileEntityArcFurnace master = this.master();
		if (master != null)
			return master.decrStackSize(slot, amount);
		ItemStack stack = this.getStackInSlot(slot);
		if (stack != null)
			if (stack.stackSize <= amount)
				this.setInventorySlotContents(slot, null);
			else
			{
				stack = stack.splitStack(amount);
				if (stack.stackSize == 0)
					this.setInventorySlotContents(slot, null);
			}
		this.markDirty();
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if (!this.formed)
			return null;
		TileEntityArcFurnace master = this.master();
		if (master != null)
			return master.getStackInSlotOnClosing(slot);
		ItemStack stack = this.getStackInSlot(slot);
		if (stack != null)
			this.setInventorySlotContents(slot, null);
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if (!this.formed)
			return;
		TileEntityArcFurnace master = this.master();
		if (master != null)
		{
			master.setInventorySlotContents(slot, stack);
			return;
		}
		this.inventory[slot] = stack;
		if (stack != null && stack.stackSize > this.getInventoryStackLimit())
			stack.stackSize = this.getInventoryStackLimit();
		this.markDirty();
	}

	@Override
	public String getInventoryName()
	{
		return "IEArcFurnace";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && this.formed && player.getDistanceSq(this.xCoord + .5D, this.yCoord + .5D, this.zCoord + .5D) <= 64;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		if (!this.formed || stack == null)
			return true;
		TileEntityArcFurnace master = this.master();
		if (master != null)
			return master.isItemValidForSlot(slot, stack);
		return slot < 12 && ArcFurnaceRecipe.isValidRecipeInput(stack) || slot >= 12 && slot < 16 && ArcFurnaceRecipe.isValidRecipeAdditive(stack) || slot > 22 && IEContent.itemGraphiteElectrode.equals(stack.getItem());
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if (!this.formed)
			return new int[0];
    
    
    
    

			ArrayList<Integer> slotsMain = new ArrayList<>(12);
			boolean allOccupied = true;
			for (int i = 0; i <= 11; i++)
			{
				if (master.getStackInSlot(i) == null)
				{
					slotsMain.add(i);
					allOccupied = false;
				}
			}
			if (allOccupied)
				slotsMain.addAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
			slotsMain.sort(new Comparator<Integer>()
			{
				@Override
				public int compare(Integer arg0, Integer arg1)
				{
					ItemStack stack0 = master.getStackInSlot(arg0);
					ItemStack stack1 = master.getStackInSlot(arg1);
					return Integer.compare(stack0 != null ? stack0.stackSize : 0, stack1 != null ? stack1.stackSize : 0);
				}
			});

			ArrayList<Integer> slotsAdditives = new ArrayList<>(4);
			allOccupied = true;
			for (int i = 12; i <= 15; i++)
			{
				if (master.getStackInSlot(i) == null)
				{
					slotsAdditives.add(i);
					allOccupied = false;
				}
			}
			if (allOccupied)
				slotsAdditives.addAll(Arrays.asList(12, 13, 14, 15));
			slotsAdditives.sort(new Comparator<Integer>()
			{
				@Override
				public int compare(Integer arg0, Integer arg1)
				{
					ItemStack stack0 = master.getStackInSlot(arg0);
					ItemStack stack1 = master.getStackInSlot(arg1);
					return Integer.compare(stack0 != null ? stack0.stackSize : 0, stack1 != null ? stack1.stackSize : 0);
				}
			});

			int[] ret = new int[slotsMain.size() + slotsAdditives.size()];
			for (int i = 0; i < ret.length; i++)
			{
				int slot = i < slotsMain.size() ? slotsMain.get(i) : slotsAdditives.get(i - slotsMain.size());
				ret[i] = slot;
			}
			return ret;
		}
    
			return new int[] { 16, 17, 18, 19, 20, 21 };
    
			return new int[] { 22 };
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		if (!this.formed)
			return true;
		TileEntityArcFurnace master = this.master();
		if (master != null)
			return master.canInsertItem(slot, stack, side);

		return this.isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		if (!this.formed)
			return true;
		TileEntityArcFurnace master = this.master();
		if (master != null)
			return master.canExtractItem(slot, stack, side);
		return slot >= 16 && slot <= 22;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return this.formed && this.pos >= 46 && this.pos <= 48 && ForgeDirection.OPPOSITES[from.ordinal()] == this.facing;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		TileEntityArcFurnace master = this.master();
		if (this.formed && master != null && this.pos >= 46 && this.pos <= 48 && ForgeDirection.OPPOSITES[from.ordinal()] == this.facing)
		{
			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
			master.markDirty();
			if (rec > 0)
				this.worldObj.markBlockForUpdate(master.xCoord, master.yCoord, master.zCoord);
			return rec;
		}
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		TileEntityArcFurnace master = this.master();
		if (master != null)
			return master.energyStorage.getEnergyStored();
		return this.energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		TileEntityArcFurnace master = this.master();
		if (master != null)
			return master.energyStorage.getMaxEnergyStored();
		return this.energyStorage.getMaxEnergyStored();
	}
}