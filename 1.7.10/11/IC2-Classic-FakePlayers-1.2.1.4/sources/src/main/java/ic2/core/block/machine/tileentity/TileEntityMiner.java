package ic2.core.block.machine.tileentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IMiningDrill;
import ic2.api.item.IScannerItem;
import ic2.core.ContainerIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Items;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class TileEntityMiner extends TileEntityElecMachine implements IHasGui
{
	public static ForgeDirection[] validDirs = new ForgeDirection[] { ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST };
	public int targetX = 0;
	public int targetY = -1;
	public int targetZ = 0;
	public int currentX = 0;
	public int currentY = 0;
	public int currentZ = 0;
	public short miningTicker = 0;
	public String stuckOn = null;
	private AudioSource audioSource;

	public TileEntityMiner()
	{
		super(4, 0, 1000, 32, IC2.enableMinerLapotron ? 3 : 1);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		boolean wasOperating = this.isOperating();
		boolean needsInvUpdate = false;
		if (this.isOperating())
		{
			this.useEnergy(1);
			if (this.inventory[1] != null && this.inventory[1].getItem() instanceof IElectricItem)
				this.energy = (int) (this.energy - ElectricItem.manager.charge(this.inventory[1], this.energy, 2, false, false));

			if (this.inventory[3] != null && this.inventory[3].getItem() instanceof IElectricItem)
				this.energy = (int) (this.energy - ElectricItem.manager.charge(this.inventory[3], this.energy, 2, false, false));
		}

		if (this.energy <= this.maxEnergy)
			needsInvUpdate = this.provideEnergy();

		if (wasOperating)
			needsInvUpdate = this.mine();
		else if (this.inventory[3] == null)
			if (this.energy >= 2 && this.canWithdraw())
			{
				this.targetY = -1;
				++this.miningTicker;
				this.useEnergy(2);
				if (this.miningTicker >= 20)
				{
					this.miningTicker = 0;
					needsInvUpdate = this.withdrawPipe();
				}
			}
			else if (this.isStuck())
				this.miningTicker = 0;

		this.setActive(this.isOperating());
		if (wasOperating != this.isOperating())
			needsInvUpdate = true;

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

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.targetX = nbttagcompound.getInteger("targetX");
		this.targetY = nbttagcompound.getInteger("targetY");
		this.targetZ = nbttagcompound.getInteger("targetZ");
		this.miningTicker = nbttagcompound.getShort("miningTicker");
		this.currentX = nbttagcompound.getInteger("currentX");
		this.currentY = nbttagcompound.getInteger("currentY");
		this.currentZ = nbttagcompound.getInteger("currentZ");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("targetX", this.targetX);
		nbttagcompound.setInteger("targetY", this.targetY);
		nbttagcompound.setInteger("targetZ", this.targetZ);
		nbttagcompound.setShort("miningTicker", this.miningTicker);
		nbttagcompound.setInteger("currentX", this.currentX);
		nbttagcompound.setInteger("currentY", this.currentY);
		nbttagcompound.setInteger("currentZ", this.currentZ);
	}

	public boolean mine()
	{
		if (this.targetY < 0)
		{
			this.aquireTarget();
			return false;
		}
		else
		{
			boolean forcedTickSkip;
			for (forcedTickSkip = false; !this.canReachTarget(this.currentX, this.currentY, this.currentZ, true); forcedTickSkip = true)
			{
				int x = this.currentX - this.xCoord;
				int z = this.currentZ - this.zCoord;
				if (Math.abs(x) > Math.abs(z))
				{
					if (x > 0)
						--this.currentX;
					else
						++this.currentX;
				}
				else if (z > 0)
					--this.currentZ;
				else
					++this.currentZ;
			}

			if (forcedTickSkip)
				return false;
			else if (this.canMine(this.currentX, this.currentY, this.currentZ))
			{
				this.stuckOn = null;
				++this.miningTicker;
				--this.energy;
				IMiningDrill drill = (IMiningDrill) this.inventory[3].getItem();
				if (!drill.isBasicDrill(this.inventory[3]))
				{
					this.miningTicker = (short) (this.miningTicker + drill.getExtraSpeed(this.inventory[3]));
					this.energy -= drill.getExtraEnergyCost(this.inventory[3]);
				}

				if (this.miningTicker >= 200)
				{
					this.miningTicker = 0;
					this.mineBlock();
					return true;
				}
				else
					return false;
			}
			else
			{
				Block id = this.worldObj.getBlock(this.currentX, this.currentY, this.currentZ);
				if ((id == Blocks.flowing_water || id == Blocks.water || id == Blocks.flowing_lava || id == Blocks.lava) && this.isAnyPumpConnected())
					return false;
				else
				{
					this.miningTicker = -1;
					this.stuckOn = id.getUnlocalizedName();
					return false;
				}
			}
		}
	}

	public boolean isValueableOre(int x, int y, int z)
	{
		ItemStack stack = this.inventory[1];
		if (stack == null)
			return false;
		else
		{
			IScannerItem item = (IScannerItem) stack.getItem();
			return item.isValuableOre(stack, this.worldObj.getBlock(x, y, z), this.worldObj.getBlockMetadata(x, y, z));
		}
	}

	public void mineBlock()
	{
		if (this.inventory[3].getItem() instanceof IMiningDrill)
    
		if (this.fake.cantBreak(this.targetX, this.targetY, this.targetZ))
    

		Block id = this.worldObj.getBlock(this.currentX, this.currentY, this.currentZ);
		int meta = this.worldObj.getBlockMetadata(this.currentX, this.currentY, this.currentZ);
		boolean liquid = false;
		if (id == Blocks.flowing_water || id == Blocks.water || id == Blocks.flowing_lava || id == Blocks.lava)
		{
			liquid = true;
			if (meta != 0)
				id = null;
		}

		if (!this.worldObj.isAirBlock(this.currentX, this.currentY, this.currentZ))
		{
			if (!liquid)
			{
				if (this.hasEnchantment(this.inventory[3], Enchantment.silkTouch) && id.canSilkHarvest(this.getWorldObj(), FakePlayerFactory.getMinecraft((WorldServer) this.getWorldObj()), this.currentX, this.currentY, this.currentZ, meta))
				{
					ArrayList<ItemStack> drops = new ArrayList();
					ItemStack stack = this.createStackedBlock(id, meta);
					if (stack != null)
						drops.add(stack);

					ForgeEventFactory.fireBlockHarvesting(drops, this.getWorldObj(), id, this.currentX, this.currentY, this.currentZ, meta, 0, 1.0F, true, FakePlayerFactory.getMinecraft((WorldServer) this.getWorldObj()));
					StackUtil.distributeDrop(this, drops);
				}
				else
				{
					int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, this.inventory[3]);
					ArrayList<ItemStack> drops = id.getDrops(this.worldObj, this.currentX, this.currentY, this.currentZ, meta, fortune);
					ForgeEventFactory.fireBlockHarvesting(drops, this.getWorldObj(), id, this.currentX, this.currentY, this.currentZ, meta, fortune, 1.0F, false, FakePlayerFactory.getMinecraft((WorldServer) this.getWorldObj()));
					StackUtil.distributeDrop(this, drops);
				}
			}
			else
			{
				if (id == Blocks.flowing_water || id == Blocks.water)
					this.usePump(Blocks.water);

				if (id == Blocks.flowing_lava || id == Blocks.lava)
					this.usePump(Blocks.lava);
			}

			this.worldObj.setBlockToAir(this.currentX, this.currentY, this.currentZ);
			this.energy -= 2 * (this.yCoord - this.currentY);
		}

		if (this.currentX == this.xCoord && this.currentZ == this.zCoord)
		{
			this.worldObj.setBlock(this.currentX, this.currentY, this.currentZ, Block.getBlockFromItem(Ic2Items.miningPipe.getItem()));
			if (this.inventory[2].stackSize == 0)
				this.inventory[2] = null;

			this.energy -= 10;
		}

		this.updateMineTip(this.currentY);
		if ((this.currentX != this.targetX || this.currentY != this.targetY || this.currentZ != this.targetZ) && !this.worldObj.isAirBlock(this.targetX, this.targetY, this.targetZ) && this.isValueableOre(this.targetX, this.targetY, this.targetZ))
		{
			this.currentX = this.targetX;
			this.currentY = this.targetY;
			this.currentZ = this.targetZ;
		}
		else
			this.targetY = -1;

	}

	private ItemStack createStackedBlock(Block ore, int meta)
	{
		int resultMeta = 0;
		if (ore != null)
		{
			Item item = Item.getItemFromBlock(ore);
			if (item != null && item.getHasSubtypes())
				resultMeta = meta;
		}

		return new ItemStack(ore, 1, resultMeta);
	}

	private boolean hasEnchantment(ItemStack itemStack, Enchantment ench)
	{
		NBTTagList list = itemStack.getEnchantmentTagList();
		boolean flag = false;
		if (list != null)
			for (int i = 0; i < list.tagCount(); ++i)
			{
				NBTTagCompound data = list.getCompoundTagAt(i);
				int id = data.getShort("id");
				if (id == ench.effectId)
				{
					flag = true;
					break;
				}
			}

		return flag;
	}

	public boolean withdrawPipe()
	{
    
		if (this.fake.cantBreak(this.xCoord, y, this.zCoord))
    

		Block block = this.worldObj.getBlock(this.xCoord, y, this.zCoord);
		if (!this.worldObj.isAirBlock(this.xCoord, y, this.zCoord))
			;

		if (this.yCoord - y > 1)
			StackUtil.distributeDrop(this, block.getDrops(this.worldObj, this.xCoord, y, this.zCoord, this.worldObj.getBlockMetadata(this.xCoord, y, this.zCoord), 0));

		this.worldObj.setBlockToAir(this.xCoord, y, this.zCoord);
		if (this.inventory[2] != null && this.inventory[2].getItem() != Ic2Items.miningPipe.getItem() && Block.getBlockFromItem(this.inventory[2].getItem()) != Blocks.air)
		{
			this.worldObj.setBlock(this.xCoord, y, this.zCoord, Block.getBlockFromItem(this.inventory[2].getItem()), this.inventory[2].getItemDamage(), 3);
			ItemStack itemStack = this.inventory[2];
			--itemStack.stackSize;
			if (this.inventory[2].stackSize == 0)
				this.inventory[2] = null;

			this.updateMineTip(y + 1);
			return true;
		}
		else
		{
			this.updateMineTip(y + 1);
			return false;
		}
	}

	public void updateMineTip(int low)
	{
		if (low != this.yCoord)
		{
			int x = this.xCoord;
			int y = this.yCoord - 1;

			int z;
			for (z = this.zCoord; y > low; --y)
			{
				Block id = this.worldObj.getBlock(x, y, z);
				if (id != Block.getBlockFromItem(Ic2Items.miningPipe.getItem()) && this.inventory[2] != null && this.inventory[2].stackSize > 0)
    
					if (this.fake.cantBreak(x, y, z))
    

					this.worldObj.setBlock(x, y, z, Block.getBlockFromItem(Ic2Items.miningPipe.getItem()));
					ItemStack itemStack = this.inventory[2];
					--itemStack.stackSize;
					if (this.inventory[2].stackSize <= 0)
						this.inventory[2] = null;
				}
    
			if (this.fake.cantBreak(x, low, z))
    

			this.worldObj.setBlock(x, low, z, Block.getBlockFromItem(Ic2Items.miningPipeTip.getItem()));
		}
	}

	public boolean canReachTarget(int x, int y, int z, boolean ignore)
	{
		if (this.xCoord == x && this.zCoord == z)
			return true;
		else if (!ignore && !this.canPass(this.worldObj.getBlock(x, y, z)) && !this.worldObj.isAirBlock(x, y, z))
			return false;
		else
		{
			int xdif = x - this.xCoord;
			int zdif = z - this.zCoord;
			if (Math.abs(xdif) > Math.abs(zdif))
			{
				if (xdif > 0)
					--x;
				else
					++x;
			}
			else if (zdif > 0)
				--z;
			else
				++z;

			return this.canReachTarget(x, y, z, false);
		}
	}

	public void aquireTarget()
	{
		int y = this.getPipeTip();
		if (y < this.yCoord && this.inventory[1] != null && this.inventory[1].getItem() instanceof IScannerItem)
		{
			IScannerItem item = (IScannerItem) this.inventory[1].getItem();
			int scanrange = item.startLayerScan(this.inventory[1]);
			if (scanrange > 0)
			{
				int minX = this.xCoord - scanrange;
				int maxX = this.xCoord + scanrange;
				int minZ = this.zCoord - scanrange;
				int maxZ = this.zCoord + scanrange;
				Set<ChunkCoordinates> visited = new HashSet();
				List<ChunkCoordinates> toCheck = new ArrayList();
				visited.add(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord));
				toCheck.addAll(this.getAroundCoords(new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord)));
				Collections.shuffle(toCheck);

				while (!((List) toCheck).isEmpty())
				{
					ChunkCoordinates coords = toCheck.remove(0);
					int x = coords.posX;
					int z = coords.posZ;
					if (!visited.contains(coords) && x >= minX && x <= maxX && z >= minZ && z <= maxZ)
					{
						visited.add(coords);
						if (this.canMine(x, y, z))
						{
							Block n = this.worldObj.getBlock(x, y, z);
							int m = this.worldObj.getBlockMetadata(x, y, z);
							if (this.isAnyPumpConnected() && super.worldObj.getBlockMetadata(x, y, z) == 0 && (n == Blocks.flowing_lava || n == Blocks.lava))
							{
								this.setTarget(x, y, z);
								return;
							}

							if (item.isValuableOre(this.inventory[1], n, m))
							{
								this.setTarget(x, y, z);
								return;
							}

							toCheck.addAll(this.getAroundCoords(coords));
						}
					}
				}
			}

			this.setTarget(this.xCoord, y - 1, this.zCoord);
		}
		else
			this.setTarget(this.xCoord, y - 1, this.zCoord);
	}

	public List<ChunkCoordinates> getAroundCoords(ChunkCoordinates par1)
	{
		List<ChunkCoordinates> result = new ArrayList();

		for (ForgeDirection dir : validDirs)
			result.add(new ChunkCoordinates(par1.posX + dir.offsetX, par1.posY + dir.offsetY, par1.posZ + dir.offsetZ));

		return result;
	}

	public void setTarget(int x, int y, int z)
	{
		this.targetX = x;
		this.targetY = y;
		this.targetZ = z;
		this.currentX = x;
		this.currentY = y;
		this.currentZ = z;
	}

	public int getPipeTip()
	{
		int y;
		for (y = this.yCoord; this.worldObj.getBlock(this.xCoord, y - 1, this.zCoord) == Block.getBlockFromItem(Ic2Items.miningPipe.getItem()) || this.worldObj.getBlock(this.xCoord, y - 1, this.zCoord) == Block.getBlockFromItem(Ic2Items.miningPipeTip.getItem()); --y)
			;

		return y;
	}

	public boolean canPass(Block block)
	{
		return block == Blocks.air || block == Blocks.flowing_water || block == Blocks.water || block == Blocks.flowing_lava || block == Blocks.lava || block == Block.getBlockFromItem(Ic2Items.miner.getItem()) || block == Block.getBlockFromItem(Ic2Items.miningPipe.getItem()) || block == Block.getBlockFromItem(Ic2Items.miningPipeTip.getItem());
	}

	public boolean isOperating()
	{
		return this.energy > 100 && this.canOperate();
	}

	public boolean canOperate()
	{
		return this.inventory[2] != null && this.inventory[3] != null && this.inventory[2].getItem() == Ic2Items.miningPipe.getItem() && this.canMiningDrillBeUsed() && !this.isStuck();
	}

	public boolean canMiningDrillBeUsed()
	{
		if (!(this.inventory[3].getItem() instanceof IMiningDrill))
			return false;
		else
		{
			IMiningDrill drill = (IMiningDrill) this.inventory[3].getItem();
			if (!drill.canMine(this.inventory[3]))
			{
				if (!(drill instanceof IElectricItem))
					return false;

				IElectricItem item = (IElectricItem) drill;
				if (item.getMaxCharge(this.inventory[3]) == 0.0D || item.getTransferLimit(this.inventory[3]) == 0.0D)
					return false;
			}

			return true;
		}
	}

	public boolean isStuck()
	{
		return this.miningTicker < 0;
	}

	public String getStuckOn()
	{
		return this.stuckOn;
	}

	public boolean canMine(int x, int y, int z)
	{
		Block id = this.worldObj.getBlock(x, y, z);
		int meta = this.worldObj.getBlockMetadata(x, y, z);
		return this.worldObj.isAirBlock(x, y, z) ? this.canMineEvent(x, y, z, id, meta) : id != Block.getBlockFromItem(Ic2Items.miningPipe.getItem()) && id != Block.getBlockFromItem(Ic2Items.miningPipeTip.getItem()) && id != Blocks.chest ? (id == Blocks.flowing_water || id == Blocks.water || id == Blocks.flowing_lava || id == Blocks.lava) && this.isPumpConnected() ? this.canMineEvent(x, y, z, id, meta) : id.getBlockHardness(this.worldObj, x, y, z) < 0.0F ? false : id.canCollideCheck(meta, false) && id.getMaterial().isToolNotRequired() ? this.canMineEvent(x, y, z, id, meta) : id == Blocks.web ? this.canMineEvent(x, y, z, id, meta) : this.inventory[3] != null ? !ForgeHooks.canToolHarvestBlock(id, meta, this.inventory[3]) && !this.inventory[3].func_150998_b(id) ? false : this.canMineEvent(x, y, z, id, meta) : false : false;
	}

	public boolean canMineEvent(int x, int y, int z, Block block, int meta)
	{
		BreakEvent evt = new BreakEvent(x, y, z, this.worldObj, block, meta, FakePlayerFactory.getMinecraft((WorldServer) this.worldObj));
		evt.setExpToDrop(0);
		MinecraftForge.EVENT_BUS.post(evt);
		return !evt.isCanceled();
	}

	public boolean canWithdraw()
	{
		return this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord) == Block.getBlockFromItem(Ic2Items.miningPipe.getItem()) || this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord) == Block.getBlockFromItem(Ic2Items.miningPipeTip.getItem());
	}

	public boolean isPumpConnected()
	{
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord)).canHarvest() || this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord)).canHarvest() || this.worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord)).canHarvest() || this.worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord)).canHarvest() || this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1)).canHarvest() || this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1)).canHarvest();
	}

	public boolean isAnyPumpConnected()
	{
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord) instanceof TileEntityPump || this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord) instanceof TileEntityPump || this.worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord) instanceof TileEntityPump || this.worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord) instanceof TileEntityPump || this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1) instanceof TileEntityPump || this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1) instanceof TileEntityPump;
	}

	public void usePump(Block water)
	{
		if (this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord)).canHarvest())
			((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord + 1, this.zCoord)).pumpThis(water);
		else if (this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord)).canHarvest())
			((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord - 1, this.zCoord)).pumpThis(water);
		else if (this.worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord)).canHarvest())
			((TileEntityPump) this.worldObj.getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord)).pumpThis(water);
		else if (this.worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord)).canHarvest())
			((TileEntityPump) this.worldObj.getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord)).pumpThis(water);
		else if (this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1)).canHarvest())
			((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1)).pumpThis(water);
		else if (this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1) instanceof TileEntityPump && ((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1)).canHarvest())
			((TileEntityPump) this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1)).pumpThis(water);
	}

	@Override
	public String getInventoryName()
	{
		return "Miner";
	}

	public int gaugeEnergyScaled(int i)
	{
		if (this.energy <= 0)
			return 0;
		else
		{
			int r = this.energy * i / 1000;
			if (r > i)
				r = i;

			return r;
		}
	}

	@Override
	public ContainerIC2 getGuiContainer(EntityPlayer entityPlayer)
	{
		return new ContainerMiner(entityPlayer, this);
	}

	@Override
	public String getGuiClassName(EntityPlayer entityPlayer)
	{
		return "block.machine.gui.GuiMiner";
	}

	@Override
	public void onGuiClosed(EntityPlayer entityPlayer)
	{
		if (this.stuckOn != null)
			entityPlayer.addChatComponentMessage(new ChatComponentText("Is Stuck on: " + this.stuckOn + " Coords: x" + this.currentX + " y:" + this.currentY + " z:" + this.currentZ));

	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("active") && this.prevActive != this.getActive())
		{
			if (this.audioSource != null && this.audioSource.isRemoved())
				this.audioSource = null;

			if (this.audioSource == null)
				this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/MinerOp.ogg", true, false, IC2.audioManager.defaultVolume);

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
	public int[] getAccessibleSlotsFromSide(int side)
	{
		ForgeDirection leftSide = null;
		ForgeDirection rightSide = null;
		ForgeDirection frontSide = null;
		ForgeDirection backSide = null;
		switch (this.getFacing())
		{
			case 2:
				leftSide = ForgeDirection.WEST;
				rightSide = ForgeDirection.EAST;
				frontSide = ForgeDirection.SOUTH;
				backSide = ForgeDirection.NORTH;
				break;
			case 3:
				leftSide = ForgeDirection.EAST;
				rightSide = ForgeDirection.WEST;
				frontSide = ForgeDirection.NORTH;
				backSide = ForgeDirection.SOUTH;
				break;
			case 4:
				leftSide = ForgeDirection.SOUTH;
				rightSide = ForgeDirection.NORTH;
				frontSide = ForgeDirection.EAST;
				backSide = ForgeDirection.WEST;
				break;
			default:
				leftSide = ForgeDirection.NORTH;
				rightSide = ForgeDirection.SOUTH;
				frontSide = ForgeDirection.WEST;
				backSide = ForgeDirection.EAST;
		}

		return side != leftSide.ordinal() && side != frontSide.ordinal() ? side != rightSide.ordinal() && side != backSide.ordinal() ? side == 0 ? new int[] { 0 } : new int[] { 2 } : new int[] { 1 } : new int[] { 3 };
	}

	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}

	@Override
	public int getEnergyUsage()
	{
		return 1 + (this.inventory[3] != null && this.inventory[3].getItem() instanceof IMiningDrill ? ((IMiningDrill) this.inventory[3].getItem()).getExtraEnergyCost(this.inventory[3]) : 0);
	}
}
