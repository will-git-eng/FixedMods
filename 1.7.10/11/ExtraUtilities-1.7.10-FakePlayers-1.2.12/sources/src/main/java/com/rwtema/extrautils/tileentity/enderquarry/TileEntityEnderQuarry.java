package com.rwtema.extrautils.tileentity.enderquarry;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.extrautilities.EventConfig;
import ru.will.git.extrautilities.ModUtils;
import com.rwtema.extrautils.EventHandlerEntityItemStealer;
import com.rwtema.extrautils.ExtraUtils;
import com.rwtema.extrautils.ExtraUtilsMod;
import com.rwtema.extrautils.crafting.RecipeEnchantCrafting;
import com.rwtema.extrautils.helper.XUHelper;
import com.rwtema.extrautils.helper.XURandom;
import com.rwtema.extrautils.network.NetworkHandler;
import com.rwtema.extrautils.network.packets.PacketTempChat;
import com.rwtema.extrautils.network.packets.PacketTempChatMultiline;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.IGrowable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Facing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.ArrayList;
import java.util.Random;

public class TileEntityEnderQuarry extends TileEntity implements IEnergyHandler
{
	private static final Random rand = XURandom.getInstance();
	public static boolean disableSelfChunkLoading = false;
	public static int baseDrain = 1800;
	public static float hardnessDrain = 200.0F;
	public ArrayList<ItemStack> items = new ArrayList();
	public int dx = 1;
	public int dy = 0;
	public int dz = 0;
	public EnergyStorage energy = new EnergyStorage(10000000);
	public int inventoryMask = -1;
	public int fluidMask = -1;
	public long progress = 0L;
	public int neededEnergy = -1;
	public boolean started = false;
	public boolean finished = false;
	public FluidStack fluid = null;
	int chunk_x = 0;
	int chunk_z = 0;
	int chunk_y = 0;
	byte t = 0;
	boolean searching = false;
	int fence_x;
	int fence_y;
	int fence_z;
	int fence_dir;
	int fence_elev;
	int min_x;
	int max_x;
	int min_z;
	int max_z;
	private Ticket chunkTicket;
	private EntityPlayer owner;
	private boolean overClock;
	public boolean[] upgrades;
	public static final int UPGRADE_BLANK = 0;
	public static final int UPGRADE_VOID = 1;
	public static final int UPGRADE_SILK = 2;
	public static final int UPGRADE_FORTUNE1 = 3;
	public static final int UPGRADE_FORTUNE2 = 4;
	public static final int UPGRADE_FORTUNE3 = 5;
	public static final int UPGRADE_SPEED1 = 6;
	public static final int UPGRADE_SPEED2 = 7;
	public static final int UPGRADE_SPEED3 = 8;
	public static final int UPGRADE_FLUID = 9;
    
    

	public TileEntityEnderQuarry()
	{
		this.fence_x = this.xCoord;
		this.fence_y = this.yCoord;
		this.fence_z = this.zCoord;
		this.fence_dir = 2;
		this.fence_elev = -1;
		this.min_x = this.xCoord;
		this.max_x = this.xCoord;
		this.min_z = this.zCoord;
		this.max_z = this.zCoord;
		this.overClock = false;
		this.upgrades = new boolean[16];
	}

	@Override
	public boolean shouldRefresh(Block oldID, Block newID, int oldMeta, int newMeta, World world, int x, int y, int z)
	{
		return oldID != newID;
	}

	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		super.readFromNBT(tags);
		this.energy.readFromNBT(tags);
		int n = tags.getInteger("item_no");
		this.items.clear();

		for (int i = 0; i < n; ++i)
		{
			NBTTagCompound t = tags.getCompoundTag("item_" + i);
			this.items.add(ItemStack.loadItemStackFromNBT(t));
		}

		if (tags.hasKey("fluid"))
			this.fluid = FluidStack.loadFluidStackFromNBT(tags.getCompoundTag("fluid"));

		this.finished = tags.getBoolean("finished");
		if (!this.finished)
		{
			this.started = tags.getBoolean("started");
			if (this.started)
			{
				this.min_x = tags.getInteger("min_x");
				this.min_z = tags.getInteger("min_z");
				this.max_x = tags.getInteger("max_x");
				this.max_z = tags.getInteger("max_z");
				this.chunk_x = tags.getInteger("chunk_x");
				this.chunk_y = tags.getInteger("chunk_y");
				this.chunk_z = tags.getInteger("chunk_z");
				this.dx = tags.getInteger("dx");
				this.dy = tags.getInteger("dy");
				this.dz = tags.getInteger("dz");
				this.progress = tags.getLong("progress");
			}
    
    
	}

	@Override
	public void writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);
		this.energy.writeToNBT(tags);

		for (int i = 0; i < this.items.size(); ++i)
		{
			while (i < this.items.size() && this.items.get(i) == null)
			{
				this.items.remove(i);
			}

			if (i < this.items.size())
			{
				NBTTagCompound t = new NBTTagCompound();
				this.items.get(i).writeToNBT(t);
				tags.setTag("item_" + i, t);
			}
		}

		tags.setInteger("item_no", this.items.size());
		if (this.fluid != null)
		{
			NBTTagCompound t = new NBTTagCompound();
			this.fluid.writeToNBT(t);
			tags.setTag("fluid", t);
		}

		if (this.finished)
			tags.setBoolean("finished", true);
		else if (this.started)
		{
			tags.setBoolean("started", true);
			tags.setInteger("min_x", this.min_x);
			tags.setInteger("max_x", this.max_x);
			tags.setInteger("min_z", this.min_z);
			tags.setInteger("max_z", this.max_z);
			tags.setInteger("chunk_x", this.chunk_x);
			tags.setInteger("chunk_y", this.chunk_y);
			tags.setInteger("chunk_z", this.chunk_z);
			tags.setInteger("dx", this.dx);
			tags.setInteger("dy", this.dy);
			tags.setInteger("dz", this.dz);
			tags.setLong("progress", this.progress);
    
    
	}

	public void startDig()
	{
		this.started = true;
		this.chunk_y += 5;
		this.chunk_x = this.min_x + 1 >> 4;
		this.chunk_z = this.min_z + 1 >> 4;
		this.dx = Math.max(0, this.min_x + 1 - (this.chunk_x << 4));
		this.dy = this.chunk_y;
		this.dz = Math.max(0, this.min_z + 1 - (this.chunk_z << 4));
		if (!this.stopHere())
			this.nextBlock();

		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 2);
	}

	public void nextBlock()
	{
		this.nextSubBlock();

		while (!this.stopHere())
		{
			this.nextSubBlock();
		}

	}

	public void nextSubBlock()
	{
		++this.progress;
		--this.dy;
		if (this.dy <= 0)
		{
			++this.dx;
			if (this.dx >= 16 || (this.chunk_x << 4) + this.dx >= this.max_x)
			{
				this.dx = Math.max(0, this.min_x + 1 - (this.chunk_x << 4));
				++this.dz;
				if (this.dz >= 16 || (this.chunk_z << 4) + this.dz >= this.max_z)
				{
					this.nextChunk();
					this.dx = Math.max(0, this.min_x + 1 - (this.chunk_x << 4));
					this.dz = Math.max(0, this.min_z + 1 - (this.chunk_z << 4));
				}
			}

			this.dy = this.chunk_y;
		}

	}

	public void nextChunk()
	{
		this.unloadChunk();
		++this.chunk_x;
		if (this.chunk_x << 4 >= this.max_x)
		{
			this.chunk_x = this.min_x + 1 >> 4;
			++this.chunk_z;
			if (this.chunk_z << 4 >= this.max_z)
			{
				this.finished = true;
				this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 2, 2);
				ForgeChunkManager.releaseTicket(this.chunkTicket);
				return;
			}
		}

		this.dy = this.chunk_y;
		this.loadChunk();
	}

	public boolean stopHere()
	{
		return this.finished || this.isValid((this.chunk_x << 4) + this.dx, (this.chunk_z << 4) + this.dz);
	}

	public boolean isValid(int x, int z)
	{
		return this.min_x < x && x < this.max_x && this.min_z < z && z < this.max_z;
	}

	public void forceChunkLoading(Ticket ticket)
	{
		if (this.chunkTicket == null)
			this.chunkTicket = ticket;

		if (!disableSelfChunkLoading)
			ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4));

		this.loadChunk();
	}

	@Override
	public void updateEntity()
	{
		if (!this.worldObj.isRemote)
		{
			if (this.inventoryMask < 0)
				this.detectInventories();

			++this.t;
			if (this.t >= this.getSpeedNo() || this.overClock)
			{
				this.t = 0;
				if (this.searching)
					this.advFencing();

				if (this.started && !this.finished)
				{
					if (this.chunkTicket == null)
					{
						this.chunkTicket = ForgeChunkManager.requestTicket(ExtraUtilsMod.instance, this.worldObj, Type.NORMAL);
						if (this.chunkTicket == null)
						{
							if (this.owner != null)
								this.owner.addChatComponentMessage(new ChatComponentText("Problem registering chunk-preserving method"));

							this.finished = true;
							return;
						}

						this.chunkTicket.getModData().setString("id", "quarry");
						this.chunkTicket.getModData().setInteger("x", this.xCoord);
						this.chunkTicket.getModData().setInteger("y", this.yCoord);
						this.chunkTicket.getModData().setInteger("z", this.zCoord);
						if (!disableSelfChunkLoading)
							ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(this.xCoord >> 4, this.zCoord >> 4));

						this.loadChunk();
					}

					if (this.neededEnergy > 0 && this.worldObj.getTotalWorldTime() % 100L == 0L)
						this.neededEnergy = -1;

					int n = this.overClock ? 200 : this.getSpeedStack();

					for (int k = 0; k < n; ++k)
					{
						if (this.items.isEmpty() && this.fluid == null)
						{
							if (this.overClock || this.energy.getEnergyStored() >= this.neededEnergy && this.energy.extractEnergy(baseDrain, true) == baseDrain)
							{
								int x = (this.chunk_x << 4) + this.dx;
								int z = (this.chunk_z << 4) + this.dz;
								int y = this.dy;
								if (y >= 0)
								{
									NetworkHandler.sendParticleEvent(this.worldObj, 1, x, y, z);
									if (this.mineBlock(x, y, z, !this.upgrades[1]))
									{
										this.neededEnergy = -1;
										this.nextBlock();
									}
								}
								else
									this.nextBlock();
							}
						}
						else if (!this.overClock)
							this.energy.extractEnergy(baseDrain, false);

						if (!this.items.isEmpty() && this.inventoryMask > 0)
							for (int i = 0; i < 6; ++i)
							{
								if ((this.inventoryMask & 1 << i) > 0)
								{
									TileEntity tile;
									if ((tile = this.worldObj.getTileEntity(this.xCoord + Facing.offsetsXForSide[i], this.yCoord + Facing.offsetsYForSide[i], this.zCoord + Facing.offsetsZForSide[i])) instanceof IInventory)
									{
										IInventory inv = (IInventory) tile;

										for (int j = 0; j < this.items.size(); ++j)
										{
											if (XUHelper.invInsert(inv, this.items.get(j), Facing.oppositeSide[i]) == null)
											{
												this.items.remove(j);
												--j;
											}
										}
									}
									else
										this.detectInventories();
								}
							}

						if (this.fluid != null && this.fluidMask > 0)
							for (int i = 0; this.fluid != null && i < 6; ++i)
							{
								if ((this.fluidMask & 1 << i) > 0)
								{
									TileEntity tile;
									if ((tile = this.worldObj.getTileEntity(this.xCoord + Facing.offsetsXForSide[i], this.yCoord + Facing.offsetsYForSide[i], this.zCoord + Facing.offsetsZForSide[i])) instanceof IFluidHandler)
									{
										IFluidHandler tank = (IFluidHandler) tile;
										this.fluid.amount -= tank.fill(ForgeDirection.getOrientation(i).getOpposite(), this.fluid, true);
										if (this.fluid.amount == 0)
										{
											this.fluid = null;
											break;
										}
									}
									else
										this.detectInventories();
								}
							}
					}

				}
			}
		}
	}

	private int getSpeedNo()
	{
		return this.upgrades[6] ? 1 : this.upgrades[7] ? 1 : this.upgrades[8] ? 1 : 3;
	}

	private int getSpeedStack()
	{
		return this.upgrades[6] ? 1 : this.upgrades[7] ? 3 : this.upgrades[8] ? 9 : 1;
	}

	public TileEntityEnderQuarry.DigType getDigType()
	{
		return this.upgrades[2] ? TileEntityEnderQuarry.DigType.SILK : this.upgrades[3] ? TileEntityEnderQuarry.DigType.FORTUNE : this.upgrades[4] ? TileEntityEnderQuarry.DigType.FORTUNE2 : this.upgrades[5] ? TileEntityEnderQuarry.DigType.FORTUNE3 : TileEntityEnderQuarry.DigType.NORMAL;
	}

	@Override
	public void invalidate()
	{
		ForgeChunkManager.releaseTicket(this.chunkTicket);
		super.invalidate();
	}

	private void loadChunk()
	{
		if (this.xCoord >> 4 != this.chunk_x || this.zCoord >> 4 != this.chunk_z)
			ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(this.chunk_x, this.chunk_z));

	}

	private void unloadChunk()
	{
		if (this.xCoord >> 4 != this.chunk_x || this.zCoord >> 4 != this.chunk_z)
			ForgeChunkManager.unforceChunk(this.chunkTicket, new ChunkCoordIntPair(this.chunk_x, this.chunk_z));

	}

	public boolean mineBlock(int x, int y, int z, boolean replaceWithDirt)
	{
		Block block = this.worldObj.getBlock(x, y, z);
		if (block != Blocks.air && !this.worldObj.isAirBlock(x, y, z))
    
			if (EventConfig.enderQuarryOnlyPrivate && !EventUtils.isInPrivate(this.worldObj, x, y, z))
				return false;

			if (this.fake.cantBreak(x, y, z))
    

			if (BlockBreakingRegistry.blackList(block))
			{
				if (this.upgrades[9] && XUHelper.isFluidBlock(block))
					this.fluid = XUHelper.drainBlock(this.worldObj, x, y, z, true);

				if (!this.overClock)
					this.energy.extractEnergy(baseDrain, false);

				return true;
			}
			else if (!replaceWithDirt || !block.isLeaves(this.worldObj, x, y, z) && !block.isFoliage(this.worldObj, x, y, z) && !block.isWood(this.worldObj, x, y, z) && !(block instanceof IPlantable) && !(block instanceof IGrowable))
			{
				int meta = this.worldObj.getBlockMetadata(x, y, z);
				float hardness = block.getBlockHardness(this.worldObj, x, y, z);
				if (hardness < 0.0F)
				{
					if (!this.overClock)
						this.energy.extractEnergy(baseDrain, false);

					return true;
				}
				else
				{
					int amount = (int) Math.ceil(baseDrain + hardness * hardnessDrain * this.getPowerMultiplier());
					if (amount > this.energy.getMaxEnergyStored())
						amount = this.energy.getMaxEnergyStored();

					if (this.overClock)
						amount = 0;

					if (this.energy.extractEnergy(amount, true) < amount)
					{
						this.neededEnergy = amount;
						return false;
					}
					else
					{
						this.energy.extractEnergy(amount, false);
						if (replaceWithDirt && (block == Blocks.grass || block == Blocks.dirt))
						{
							if (this.worldObj.canBlockSeeTheSky(x, y + 1, z))
								this.worldObj.setBlock(x, y, z, Blocks.grass, 0, 3);

							if (rand.nextInt(16) == 0 && this.worldObj.isAirBlock(x, y + 1, z))
								if (rand.nextInt(5) == 0)
									this.worldObj.getBiomeGenForCoords(x, z).plantFlower(this.worldObj, rand, x, y + 1, z);
								else if (rand.nextInt(2) == 0)
									this.worldObj.setBlock(x, y + 1, z, Blocks.yellow_flower, rand.nextInt(BlockFlower.field_149858_b.length), 3);
								else
									this.worldObj.setBlock(x, y + 1, z, Blocks.red_flower, rand.nextInt(BlockFlower.field_149859_a.length), 3);

							return true;
						}
						else
							return this.harvestBlock(block, x, y, z, meta, replaceWithDirt, this.getDigType());
					}
				}
			}
			else
			{
				if (!this.overClock)
					this.energy.extractEnergy(baseDrain, false);

				return true;
			}
		}
		else
		{
			if (!this.overClock)
				this.energy.extractEnergy(baseDrain, false);

			return true;
		}
	}

	public boolean harvestBlock(Block block, int x, int y, int z, int meta, boolean replaceWithDirt, TileEntityEnderQuarry.DigType digType)
    
		if (EventConfig.enderQuarryOnlyPrivate && !EventUtils.isInPrivate(this.worldObj, x, y, z))
			return false;

		if (this.fake.cantBreak(x, y, z))
    

		boolean isOpaque = block.isOpaqueCube();
		boolean seesSky = replaceWithDirt && isOpaque && this.worldObj.canBlockSeeTheSky(x, y + 1, z);
		FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((WorldServer) this.worldObj);
		fakePlayer.setCurrentItemOrArmor(0, digType.newStack(Items.diamond_pickaxe));

		try
		{
			if (BlockBreakingRegistry.isSpecial(block))
			{
				EventHandlerEntityItemStealer.startCapture(true);
				block.onBlockHarvested(this.worldObj, x, y, z, meta, fakePlayer);
				if (!block.removedByPlayer(this.worldObj, fakePlayer, x, y, z, true))
				{
					this.items.addAll(EventHandlerEntityItemStealer.getCapturedItemStacks());
					boolean var20 = false;
					return var20;
				}

				block.harvestBlock(this.worldObj, fakePlayer, x, y, z, meta);
				block.onBlockDestroyedByPlayer(this.worldObj, x, y, z, meta);
				if (replaceWithDirt && isOpaque)
					this.worldObj.setBlock(x, y, z, seesSky ? Blocks.grass : Blocks.dirt, 0, 3);

				this.items.addAll(EventHandlerEntityItemStealer.getCapturedItemStacks());
			}
			else
			{
				EventHandlerEntityItemStealer.startCapture(true);
				boolean flag = this.worldObj.setBlock(x, y, z, replaceWithDirt && isOpaque ? seesSky ? Blocks.grass : Blocks.dirt : Blocks.air, 0, 3);
				this.items.addAll(EventHandlerEntityItemStealer.getCapturedItemStacks());
				if (!flag)
				{
					boolean var21 = false;
					return var21;
				}

				ArrayList<ItemStack> i = new ArrayList();
				if (digType.isSilkTouch() && block.canSilkHarvest(this.worldObj, fakePlayer, x, y, z, meta))
				{
					int j = 0;
					Item item = Item.getItemFromBlock(block);
					if (item != null)
					{
						if (item.getHasSubtypes())
							j = meta;

						ItemStack itemstack = new ItemStack(item, 1, j);
						i.add(itemstack);
					}
				}
				else
					i.addAll(block.getDrops(this.worldObj, x, y, z, meta, digType.getFortuneModifier()));

				float p = ForgeEventFactory.fireBlockHarvesting(i, this.worldObj, block, x, y, z, meta, digType.getFortuneModifier(), 1.0F, digType.isSilkTouch(), fakePlayer);
				if (p > 0.0F && !i.isEmpty() && (p == 1.0F || rand.nextFloat() < p))
					this.items.addAll(i);
			}

			NetworkHandler.sendParticleEvent(this.worldObj, 0, x, y, z);
			if (seesSky && rand.nextInt(16) == 0 && this.worldObj.isAirBlock(x, y + 1, z))
				if (rand.nextInt(5) == 0)
					this.worldObj.getBiomeGenForCoords(x, z).plantFlower(this.worldObj, rand, x, y + 1, z);
				else if (rand.nextInt(2) == 0)
					this.worldObj.setBlock(x, y + 1, z, Blocks.yellow_flower, rand.nextInt(BlockFlower.field_149858_b.length), 3);
				else
					this.worldObj.setBlock(x, y + 1, z, Blocks.red_flower, rand.nextInt(BlockFlower.field_149859_a.length), 3);

			boolean var19 = true;
			return var19;
		}
		finally
		{
			fakePlayer.setCurrentItemOrArmor(0, null);
		}
	}

	public void debug()
	{
		this.overClock = true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		return this.energy.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return this.energy.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return this.energy.getMaxEnergyStored();
	}

	public static void addUpgradeRecipes()
	{
		ItemStack base = new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 0);
		ItemStack burntQuartz = new ItemStack(ExtraUtils.decorative1, 1, 2);
		ItemStack endersidian = new ItemStack(ExtraUtils.decorative1, 1, 1);
		ExtraUtils.addRecipe(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 0), " E ", "EQE", " E ", Character.valueOf('E'), endersidian, Character.valueOf('Q'), burntQuartz);
		ExtraUtils.addRecipe(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 1), " T ", "RBR", Character.valueOf('B'), base, Character.valueOf('T'), ExtraUtils.trashCan, Character.valueOf('R'), Blocks.quartz_block);
		ExtraUtils.addRecipe(new RecipeEnchantCrafting(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 2), " P ", "RBR", Character.valueOf('B'), base, Character.valueOf('P'), DigType.SILK.newStack(Items.golden_pickaxe), Character.valueOf('R'), Items.redstone));
		ExtraUtils.addRecipe(new RecipeEnchantCrafting(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 3), " P ", "RBR", Character.valueOf('B'), base, Character.valueOf('P'), DigType.FORTUNE.newStack(Items.iron_pickaxe), Character.valueOf('R'), Items.redstone));
		ExtraUtils.addRecipe(new RecipeEnchantCrafting(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 4), " P ", "RBR", Character.valueOf('B'), new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 3), Character.valueOf('P'), DigType.FORTUNE.newStack(Items.golden_pickaxe), Character.valueOf('R'), Items.redstone));
		ExtraUtils.addRecipe(new RecipeEnchantCrafting(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 5), "P P", "RBR", Character.valueOf('B'), new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 4), Character.valueOf('P'), DigType.FORTUNE.newStack(Items.diamond_pickaxe), Character.valueOf('R'), Items.redstone));
		if (ExtraUtils.nodeUpgrade != null)
		{
			ExtraUtils.addRecipe(new RecipeEnchantCrafting(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 6), " R ", "TBT", Character.valueOf('B'), base, Character.valueOf('T'), new ItemStack(ExtraUtils.nodeUpgrade, 1, 0), Character.valueOf('R'), DigType.SPEED.newStack(Items.diamond_pickaxe)));
			ExtraUtils.addRecipe(new RecipeEnchantCrafting(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 7), " R ", "TBT", Character.valueOf('B'), new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 6), Character.valueOf('T'), new ItemStack(ExtraUtils.nodeUpgrade, 1, 0), Character.valueOf('R'), DigType.SPEED2.newStack(Items.diamond_pickaxe)));
			ExtraUtils.addRecipe(new RecipeEnchantCrafting(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 8), "R R", "TBT", Character.valueOf('B'), new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 7), Character.valueOf('T'), new ItemStack(ExtraUtils.nodeUpgrade, 1, 3), Character.valueOf('R'), DigType.SPEED3.newStack(Items.diamond_pickaxe)));
		}

		ExtraUtils.addRecipe(new ItemStack(ExtraUtils.enderQuarryUpgrade, 1, 9), " T ", "RBR", Character.valueOf('B'), base, Character.valueOf('T'), Items.bucket, Character.valueOf('R'), Items.redstone);
	}

	public double getPowerMultiplier()
	{
		double multiplier = 1.0D;

		for (int i = 0; i < 16; ++i)
		{
			if (this.upgrades[i])
				multiplier *= powerMultipliers[i];
		}

		return multiplier;
	}

	public void detectInventories()
	{
		this.inventoryMask = 0;
		this.fluidMask = 0;
		this.upgrades = new boolean[16];

		for (int i = 0; i < 6; ++i)
		{
			int x = this.xCoord + Facing.offsetsXForSide[i];
			int y = this.yCoord + Facing.offsetsYForSide[i];
			int z = this.zCoord + Facing.offsetsZForSide[i];
			TileEntity tile = this.worldObj.getTileEntity(x, y, z);
			if (tile instanceof IInventory)
				this.inventoryMask |= 1 << i;

			if (tile instanceof IFluidHandler)
				this.fluidMask |= 1 << i;

			if (this.worldObj.getBlock(x, y, z) == ExtraUtils.enderQuarryUpgrade)
				this.upgrades[this.worldObj.getBlockMetadata(x, y, z)] = true;
		}

	}

	public void startFencing(EntityPlayer player)
	{
		if (this.finished)
			PacketTempChat.sendChat(player, new ChatComponentText("Quarry has finished"));
		else if (this.started)
		{
			PacketTempChatMultiline.addChatComponentMessage(new ChatComponentText("Mining at: (" + ((this.chunk_x << 4) + this.dx) + "," + this.dy + "," + ((this.chunk_z << 4) + this.dz) + ")"));
			PacketTempChatMultiline.addChatComponentMessage(new ChatComponentText("" + this.progress + " blocks scanned."));
			PacketTempChatMultiline.sendCached(player);
		}
		else if (this.searching)
			PacketTempChat.sendChat(player, new ChatComponentText("Searching fence boundary at: (" + this.fence_x + "," + this.fence_y + "," + this.fence_z + ")"));
		else
		{
			this.owner = player;
			player.addChatComponentMessage(new ChatComponentText("Analyzing Fence boundary"));
			if (!this.checkForMarkers(player))
			{
				this.fence_x = this.xCoord;
				this.fence_y = this.yCoord;
				this.fence_z = this.zCoord;
				this.fence_elev = -1;
				this.fence_dir = -1;
				int j = 0;

				for (int i = 2; i < 6; ++i)
				{
					if (this.isFence(this.fence_x, this.fence_y, this.fence_z, i))
					{
						if (this.fence_dir < 0)
							this.fence_dir = i;

						++j;
						if (j > 2)
						{
							this.stopFencing("Quarry is connected to more than fences on more than 2 sides", false);
							return;
						}
					}
				}

				if (j < 2)
				{
					if (j == 0)
						this.stopFencing("Unable to detect fence boundary", false);

					if (j == 1)
						this.stopFencing("Quarry is only connected to fence boundary on one side", false);

				}
				else
				{
					this.chunk_y = this.yCoord;
					this.fence_x = this.xCoord + Facing.offsetsXForSide[this.fence_dir];
					this.fence_y = this.yCoord + Facing.offsetsYForSide[this.fence_dir];
					this.fence_z = this.zCoord + Facing.offsetsZForSide[this.fence_dir];
					this.min_x = this.xCoord;
					this.max_x = this.xCoord;
					this.min_z = this.zCoord;
					this.max_z = this.zCoord;
					this.searching = true;
				}
			}
		}
	}

	public boolean checkForMarkers(EntityPlayer player)
	{
		for (ForgeDirection d : new ForgeDirection[] { ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.NORTH, ForgeDirection.SOUTH })
		{
			int[] test = { this.getWorldObj().provider.dimensionId, this.xCoord + d.offsetX, this.yCoord, this.zCoord + d.offsetZ };
			int[] test_forward = null;
			int[] test_side = null;
			boolean flag = true;

			for (int[] a : TileEntityEnderMarker.markers)
			{
				if (isIntEqual(a, test))
				{
					flag = false;
					break;
				}
			}

			if (!flag)
			{
				player.addChatComponentMessage(new ChatComponentText("Found attached ender-marker"));

				for (int[] a : TileEntityEnderMarker.markers)
				{
					if (a[0] == test[0] && a[2] == test[2] && (a[1] != test[1] || a[3] != test[3]))
					{
						if (sign(a[1] - test[1]) == d.offsetX && sign(a[3] - test[3]) == d.offsetZ)
							if (test_forward == null)
								test_forward = a;
							else if (!isIntEqual(a, test_forward))
								player.addChatComponentMessage(new ChatComponentText("Quarry marker square is ambiguous - multiple markers found at (" + a[1] + "," + a[3] + ") and (" + test_forward[1] + "," + test_forward[3] + ")"));

						if (d.offsetX == 0 && a[3] == test[3] || d.offsetZ == 0 && a[1] == test[1])
							if (test_side == null)
								test_side = a;
							else if (!isIntEqual(a, test_side))
								player.addChatComponentMessage(new ChatComponentText("Quarry marker square is ambiguous - multiple markers found at (" + a[1] + "," + a[3] + ") and (" + test_side[1] + "," + test_side[3] + ")"));
					}
				}

				if (test_forward == null)
				{
					player.addChatComponentMessage(new ChatComponentText("Quarry marker square is incomplete"));
					return false;
				}

				if (test_side == null)
				{
					player.addChatComponentMessage(new ChatComponentText("Quarry marker square is incomplete"));
					return false;
				}

				int amin_x = Math.min(Math.min(test[1], test_forward[1]), test_side[1]);
				int amax_x = Math.max(Math.max(test[1], test_forward[1]), test_side[1]);
				int amin_z = Math.min(Math.min(test[3], test_forward[3]), test_side[3]);
				int amax_z = Math.max(Math.max(test[3], test_forward[3]), test_side[3]);
				if (amax_x - amin_x > 2 && amax_z - amin_z > 2)
    
					int maxSize = EventConfig.enderQuarryMaxSize;
					if (amax_x - amin_x >= maxSize || amax_z - amin_z >= maxSize)
					{
						this.stopFencing("Region created by ender markers is too large (" + maxSize + "x" + maxSize + " max)", false);
						return false;
    

					this.owner.addChatComponentMessage(new ChatComponentText("Sucessfully established boundary"));
					if (disableSelfChunkLoading)
						this.owner.addChatComponentMessage(new ChatComponentText("Note: Quarry is configured not to self-chunkload."));

					this.chunk_y = this.yCoord;
					this.min_x = amin_x;
					this.max_x = amax_x;
					this.min_z = amin_z;
					this.max_z = amax_z;
					this.searching = false;
					this.startDig();
					return true;
				}

				this.stopFencing("Region created by ender markers is too small", false);
				return false;
			}
		}

		return false;
	}

	public static int sign(int d)
	{
		return d == 0 ? 0 : d > 0 ? 1 : -1;
	}

	public static boolean isIntEqual(int[] a, int[] b)
	{
		if (a == b)
			return true;
		else
		{
			for (int i = 0; i < 4; ++i)
			{
				if (a[i] != b[i])
					return false;
			}

			return true;
		}
	}

	public void stopFencing(String reason, boolean sendLocation)
	{
		this.searching = false;
		if (sendLocation)
			reason = reason + ": (" + this.fence_x + "," + this.fence_y + "," + this.fence_z + ")";

		if (this.owner != null)
			this.owner.addChatComponentMessage(new ChatComponentText(reason));

	}

	private void advFencing()
	{
		Long t = Long.valueOf(System.nanoTime());

		while (this.searching && System.nanoTime() - t.longValue() < 100000L)
		{
			this.advFence();
		}

	}

	public void advFence()
	{
		int new_dir = -1;

		for (int i = 0; i < 6; ++i)
		{
			if (this.fence_elev < 0)
			{
				if (i == Facing.oppositeSide[this.fence_dir])
					continue;
			}
			else if (i == Facing.oppositeSide[this.fence_elev])
				continue;

			if (this.isFence(this.fence_x, this.fence_y, this.fence_z, i))
			{
				if (new_dir != -1)
				{
					this.stopFencing("Fence boundary splits at", true);
					return;
				}

				new_dir = i;
			}
		}

		if (new_dir < 0)
			this.stopFencing("Fence boundary stops at", true);
		else
		{
			if (new_dir <= 1)
			{
				this.fence_elev = new_dir;
				this.fence_y += Facing.offsetsYForSide[new_dir];
				if (new_dir == 1)
					this.chunk_y = Math.max(this.chunk_y, this.fence_y);
			}
			else
			{
				if (this.fence_dir != new_dir)
				{
					if (this.min_z < this.fence_z && this.fence_z < this.max_z || this.min_x < this.fence_x && this.fence_x < this.max_x)
					{
						this.stopFencing("Fence boundary must be square", true);
						return;
					}

					boolean flag = false;
					if (this.fence_z < this.zCoord)
					{
						flag = this.fence_z != this.min_z && this.min_z != this.zCoord;
						this.min_z = this.fence_z;
					}

					if (this.fence_x < this.xCoord && !flag)
					{
						flag = this.fence_x != this.min_x && this.min_x != this.xCoord;
						this.min_x = this.fence_x;
					}

					if (this.fence_z > this.zCoord && !flag)
					{
						flag = this.fence_z != this.max_z && this.max_z != this.zCoord;
						this.max_z = this.fence_z;
					}

					if (this.fence_x > this.xCoord && !flag)
					{
						flag = this.fence_x != this.max_x && this.max_x != this.xCoord;
						this.max_x = this.fence_x;
					}

					if (flag)
					{
						this.stopFencing("Fence boundary must be square", true);
						return;
					}
				}

				this.fence_x += Facing.offsetsXForSide[new_dir];
				this.fence_z += Facing.offsetsZForSide[new_dir];
				this.fence_dir = new_dir;
				this.fence_elev = -1;
			}

			if (this.fence_x == this.xCoord && this.fence_y == this.yCoord && this.fence_z == this.zCoord)
			{
				if (this.max_x - this.min_x <= 2 || this.max_z - this.min_z <= 2)
				{
					this.stopFencing("Region created by fence is too small", false);
					return;
    
				int maxSize = EventConfig.enderQuarryMaxSize;
				if (this.max_x - this.min_x >= maxSize || this.max_z - this.min_z >= maxSize)
				{
					this.stopFencing("Region created by fence is too large (" + maxSize + "x" + maxSize + " max)", false);
					return;
    

				this.owner.addChatComponentMessage(new ChatComponentText("Sucessfully established boundary"));
				if (disableSelfChunkLoading)
					this.owner.addChatComponentMessage(new ChatComponentText("Note: Quarry is configured not to self-chunkload."));

				this.startDig();
				this.searching = false;
			}

		}
	}

	public boolean isFence(int x, int y, int z, int dir)
	{
		return this.isFence(x + Facing.offsetsXForSide[dir], y + Facing.offsetsYForSide[dir], z + Facing.offsetsZForSide[dir]);
	}

	public boolean isFence(int x, int y, int z)
	{
		Block id = this.worldObj.getBlock(x, y, z);
		return BlockBreakingRegistry.isFence(id) || x == this.xCoord && z == this.zCoord && y == this.yCoord;
	}

	public enum DigType
	{
		NORMAL(null, 0),
		SILK(Enchantment.silkTouch, 1),
		FORTUNE(Enchantment.fortune, 1),
		FORTUNE2(Enchantment.fortune, 2),
		FORTUNE3(Enchantment.fortune, 3),
		SPEED(Enchantment.efficiency, 1),
		SPEED2(Enchantment.efficiency, 3),
		SPEED3(Enchantment.efficiency, 5);

		public Enchantment ench;
		public int level;

		DigType(Enchantment ench, int level)
		{
			this.ench = ench;
			this.level = level;
		}

		public int getFortuneModifier()
		{
			return this.ench == Enchantment.fortune ? this.level : 0;
		}

		public ItemStack newStack(Item pick)
		{
			ItemStack stack = new ItemStack(pick);
			if (this.ench != null)
				stack.addEnchantment(this.ench, this.level);

			return stack;
		}

		public boolean isSilkTouch()
		{
			return this.ench == Enchantment.silkTouch;
		}
	}
}
