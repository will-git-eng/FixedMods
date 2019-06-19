package ic2.core.item.armor;

import java.util.List;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class NuclearJetpackLogic implements IReactor
{
	ItemStack[] parts = new ItemStack[25];
	int tick;
	int output;
	int heat;
	int partHeat;
	int maxHeat;
	float hem;
	boolean active;
	EntityPlayer player;

	public NuclearJetpackLogic(ItemStack par1, EntityPlayer par2)
	{
		NBTTagCompound data = StackUtil.getOrCreateNbtData(par1);
		this.readInventory(data.getCompoundTag("Inventory"));
		this.readLogic(data.getCompoundTag("Logic"));
		this.player = par2;
	}

	public void save(ItemStack par1)
	{
		NBTTagCompound data = StackUtil.getOrCreateNbtData(par1);
		NBTTagCompound inv = new NBTTagCompound();
		NBTTagCompound logic = new NBTTagCompound();
		this.writeInventory(inv);
		this.writeLogic(logic);
		data.setTag("Inventory", inv);
		data.setTag("Logic", logic);
	}

	private void readInventory(NBTTagCompound par1)
	{
		NBTTagList nbttaglist = par1.getTagList("Items", 10);
		this.parts = new ItemStack[25];

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			byte b0 = nbttagcompound1.getByte("Slot");
			if (b0 >= 0 && b0 < this.parts.length)
				this.parts[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
		}

	}

	private void writeInventory(NBTTagCompound par1)
	{
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.parts.length; ++i)
			if (this.parts[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.parts[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}

		par1.setTag("Items", nbttaglist);
	}

	private void readLogic(NBTTagCompound par1)
	{
		this.tick = par1.getInteger("CurrentTick");
		this.active = par1.getBoolean("Active");
		this.heat = par1.getInteger("Heat");
		this.output = par1.getInteger("output");
		this.partHeat = par1.getInteger("PartHeat");
		this.maxHeat = par1.getInteger("MaxHeat");
		this.hem = par1.getFloat("Hem");
	}

	private void writeLogic(NBTTagCompound par1)
	{
		par1.setInteger("CurrentTick", this.tick);
		par1.setBoolean("Active", this.active);
		par1.setInteger("Heat", this.heat);
		par1.setInteger("output", this.output);
		par1.setInteger("PartHeat", this.partHeat);
		par1.setInteger("MaxHeat", this.maxHeat);
		par1.setFloat("Hem", this.hem);
	}

	public void onTick(EntityPlayer par1, ItemStack jetpack)
	{
		this.updateState(jetpack);
		if (this.tick++ % 20 == 0)
		{
			if (this.updateReactor())
			{
				par1.inventory.armorInventory[2] = null;
				return;
			}

			this.updatepartHeat();
		}

		ElectricItem.manager.charge(jetpack, this.output, Integer.MAX_VALUE, true, false);
		this.updateState(jetpack);
	}

	public void updatepartHeat()
	{
		int[] partHeat = new int[25];
		int count = 0;

		for (int i = 0; i < this.parts.length; ++i)
		{
			ItemStack part = this.parts[i];
			if (part != null && part.getItem() instanceof IReactorComponent)
			{
				IReactorComponent comp = (IReactorComponent) part.getItem();
				int maxHeat = comp.getMaxHeat(this, part, i % 5, i / 5);
				if (maxHeat > 0)
				{
					partHeat[count] = (int) ((double) part.getItemDamage() / (double) maxHeat * 100.0D);
					++count;
				}
			}
		}

		if (count > 0)
		{
			double cu = 0.0D;

			for (int i = 0; i < count; ++i)
				cu += partHeat[i];

			cu = cu / count;
			this.partHeat = (int) cu;
		}
		else
			this.partHeat = 0;

	}

	public void updateState(ItemStack par1)
	{
		double maxEnergy = 30000.0D;
		if (par1 != null && par1.getItem() instanceof IElectricItem)
			maxEnergy = ((IElectricItem) par1.getItem()).getMaxCharge(par1);

		if (!this.active)
		{
			double produce = ElectricItem.manager.getCharge(par1) / maxEnergy * 100.0D;
			if (produce < 30.0D)
				this.active = true;
		}
		else if (ElectricItem.manager.getCharge(par1) >= maxEnergy)
			this.active = false;

	}

	public boolean updateReactor()
	{
		this.output = 0;
		this.maxHeat = 10000;
		this.hem = 1.0F;
		this.processChamber();
		return this.calculateHeatEffects();
	}

	public boolean calculateHeatEffects()
	{
		if (this.heat >= 4000 && IC2.platform.isSimulating() && IC2.explosionPowerReactorMax > 0.0F)
		{
			float power = (float) this.heat / (float) this.maxHeat;
			if (power >= 1.0F)
			{
				this.explode();
				return true;
			}
			else
			{
				World worldObj = this.getWorld();
				if (power >= 0.85F && worldObj.rand.nextFloat() <= 0.2F * this.hem)
				{
					int[] coord = this.getRandCoord(2);
					if (coord != null)
					{
						Block id = worldObj.getBlock(coord[0], coord[1], coord[2]);
						if (id == null)
							worldObj.setBlock(coord[0], coord[1], coord[2], Blocks.fire);
						else if (id.getBlockHardness(worldObj, coord[0], coord[1], coord[2]) <= -1.0F)
						{
							Material mat = id.getMaterial();
							if (mat != Material.rock && mat != Material.iron && mat != Material.lava && mat != Material.ground && mat != Material.clay)
								worldObj.setBlock(coord[0], coord[1], coord[2], Blocks.fire);
							else
								worldObj.setBlock(coord[0], coord[1], coord[2], Blocks.flowing_lava, 15, 3);
						}
					}
				}

				if (power >= 0.7F)
				{
					ChunkCoordinates coords = this.getPosition();
					List<EntityLivingBase> list1 = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(coords.posX - 3, coords.posY - 3, coords.posZ - 3, coords.posX + 4, coords.posY + 4, coords.posZ + 4));

					for (int l = 0; l < list1.size(); ++l)
					{
						EntityLivingBase ent = list1.get(l);
						ent.attackEntityFrom(IC2DamageSource.radiation, (int) (worldObj.rand.nextInt(4) * this.hem));
					}
				}

				if (power >= 0.5F && worldObj.rand.nextFloat() <= this.hem)
				{
					int[] coord = this.getRandCoord(2);
					if (coord != null)
					{
						Block id = worldObj.getBlock(coord[0], coord[1], coord[2]);
						if (id != null && id.getMaterial() == Material.water)
							worldObj.setBlockToAir(coord[0], coord[1], coord[2]);
					}
				}

				if (power >= 0.4F && worldObj.rand.nextFloat() <= this.hem)
				{
					int[] coord = this.getRandCoord(2);
					if (coord != null)
					{
						Block id = worldObj.getBlock(coord[0], coord[1], coord[2]);
						if (id != null)
						{
							Material mat = id.getMaterial();
							if (mat == Material.wood || mat == Material.leaves || mat == Material.cloth)
								worldObj.setBlock(coord[0], coord[1], coord[2], Blocks.fire);
						}
					}
				}

				return false;
			}
		}
		else
			return false;
	}

	public void processChamber()
	{
		for (int pass = 0; pass < 2; ++pass)
			for (int x = 0; x < 5; ++x)
				for (int y = 0; y < 5; ++y)
				{
					ItemStack stack = this.getMatrix(x, y);
					if (stack != null && stack.getItem() instanceof IReactorComponent)
					{
						IReactorComponent comp = (IReactorComponent) stack.getItem();
						comp.processChamber(this, stack, x, y, pass == 0);
					}
				}

	}

	public int[] getRandCoord(int radius)
	{
		if (radius <= 0)
			return null;
		else
		{
			ChunkCoordinates coords = this.getPosition();
			World worldObj = this.getWorld();
			int[] c = new int[] { coords.posX + worldObj.rand.nextInt(2 * radius + 1) - radius, coords.posY + worldObj.rand.nextInt(2 * radius + 1) - radius, coords.posZ + worldObj.rand.nextInt(2 * radius + 1) - radius };
			return c[0] == coords.posX && c[1] == coords.posY && c[2] == coords.posZ ? null : c;
		}
	}

	@Override
	public ChunkCoordinates getPosition()
	{
		return new ChunkCoordinates((int) this.player.posX, (int) this.player.posY, (int) this.player.posZ);
	}

	@Override
	public World getWorld()
	{
		return this.player.worldObj;
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
	public void addEmitHeat(int heat)
	{
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
		return this.output = (int) (this.output + energy);
	}

	@Override
	public ItemStack getItemAt(int x, int y)
	{
		return this.getMatrix(x, y);
	}

	@Override
	public void setItemAt(int x, int y, ItemStack item)
	{
		this.setMatrix(x, y, item);
	}

	@Override
	public void explode()
	{
		float boomPower = 10.0F;
		float boomMod = 1.0F;

		for (int y = 0; y < 5; ++y)
			for (int x = 0; x < 5; ++x)
			{
				ItemStack stack = this.getMatrix(x, y);
				if (stack != null && stack.getItem() instanceof IReactorComponent)
				{
					float f = ((IReactorComponent) stack.getItem()).influenceExplosion(this, stack);
					if (f > 0.0F && f < 1.0F)
						boomMod *= f;
					else
						boomPower += f;
				}

				this.setMatrix(x, y, (ItemStack) null);
			}

		boomPower = boomPower * this.hem * boomMod;
		IC2.log.info("Nuclear Jetpack at " + this.player.worldObj.provider.dimensionId + ":(" + this.player.posX + "," + this.player.posY + "," + this.player.posZ + ") melted (explosion power " + boomPower + ")");
		if (boomPower > IC2.explosionPowerReactorMax)
			boomPower = IC2.explosionPowerReactorMax;

		this.player.inventory.armorInventory[2] = null;
		this.player.openContainer.detectAndSendChanges();
    
    

		explosion.doExplosion();
	}

	@Override
	public int getTickRate()
	{
		return 20;
	}

	@Override
	public boolean produceEnergy()
	{
		return this.active;
	}

	public ItemStack getStackInSlot(int i)
	{
		return this.parts[i];
	}

	public void setInventoryContent(int i, ItemStack par1)
	{
		this.parts[i] = par1;
	}

	public void setMatrix(int x, int y, ItemStack par1)
	{
		if (x >= 0 && x < 5 && y >= 0 && y < 5)
			this.setInventoryContent(x + y * 5, par1);
	}

	public ItemStack getMatrix(int x, int y)
	{
		return x >= 0 && x < 5 && y >= 0 && y < 5 ? this.getStackInSlot(x + y * 5) : null;
	}

	@Override
	public double getReactorEUEnergyOutput()
	{
		return this.output;
	}

	@Override
	public void setRedstoneSignal(boolean redstone)
	{
	}

	@Override
	public boolean isFluidCooled()
	{
		return false;
	}
}
