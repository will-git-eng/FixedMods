/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture;

import com.google.common.base.Preconditions;
import forestry.api.apiculture.*;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorState;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IIndividual;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.apiculture.network.packets.PacketBeeLogicActiveEntity;
import forestry.core.config.Constants;
import forestry.core.errors.EnumErrorCode;
import forestry.core.utils.Log;
import forestry.core.utils.NetworkUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class BeekeepingLogic implements IBeekeepingLogic
{

	private static final int totalBreedingTime = Constants.APIARY_BREEDING_TIME;

	private final IBeeHousing housing;
	private final IBeeModifier beeModifier;
	private final IBeeListener beeListener;

	private int beeProgress;
	private int beeProgressMax;

	private int queenWorkCycleThrottle;
	private IEffectData[] effectData = new IEffectData[2];

	private final Stack<ItemStack> spawn = new Stack<>();

	private final HasFlowersCache hasFlowersCache = new HasFlowersCache();
	private final QueenCanWorkCache queenCanWorkCache = new QueenCanWorkCache();
	private final PollenHandler pollenHandler = new PollenHandler();

	// Client
	private boolean active;
	@Nullable
	private IBee queen;
	private ItemStack queenStack = ItemStack.EMPTY; // used to detect server changes and sync clientQueen

	
	private static final int MAX_BUFFER_SIZE = 32;
	

	public BeekeepingLogic(IBeeHousing housing)
	{
		this.housing = housing;
		this.beeModifier = BeeManager.beeRoot.createBeeHousingModifier(housing);
		this.beeListener = BeeManager.beeRoot.createBeeHousingListener(housing);
	}

	// / SAVING & LOADING
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		this.beeProgress = nbttagcompound.getInteger("BreedingTime");
		this.queenWorkCycleThrottle = nbttagcompound.getInteger("Throttle");

		if (nbttagcompound.hasKey("queen"))
		{
			NBTTagCompound queenNBT = nbttagcompound.getCompoundTag("queen");
			this.queenStack = new ItemStack(queenNBT);
			this.queen = BeeManager.beeRoot.getMember(this.queenStack);
		}

		this.setActive(nbttagcompound.getBoolean("Active"));

		this.hasFlowersCache.readFromNBT(nbttagcompound);

		NBTTagList nbttaglist = nbttagcompound.getTagList("Offspring", 10);
		int spawnToAdd = nbttaglist.tagCount();

		
		spawnToAdd = MathHelper.clamp(spawnToAdd, 0, Math.min(MAX_BUFFER_SIZE, this.spawn.size()));
		

		for (int i = 0; i < spawnToAdd; i++)
		{
			this.spawn.add(new ItemStack(nbttaglist.getCompoundTagAt(i)));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setInteger("BreedingTime", this.beeProgress);
		nbttagcompound.setInteger("Throttle", this.queenWorkCycleThrottle);

		if (!this.queenStack.isEmpty())
		{
			NBTTagCompound queenNBT = new NBTTagCompound();
			this.queenStack.writeToNBT(queenNBT);
			nbttagcompound.setTag("queen", queenNBT);
		}

		nbttagcompound.setBoolean("Active", this.active);

		this.hasFlowersCache.writeToNBT(nbttagcompound);

		Stack<ItemStack> spawnCopy = new Stack<>();
		spawnCopy.addAll(this.spawn);
		NBTTagList nbttaglist = new NBTTagList();
		while (!spawnCopy.isEmpty())
		{
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
			spawnCopy.pop().writeToNBT(nbttagcompound1);
			nbttaglist.appendTag(nbttagcompound1);
		}
		nbttagcompound.setTag("Offspring", nbttaglist);
		return nbttagcompound;
	}

	@Override
	public void writeData(PacketBuffer data)
	{
		data.writeBoolean(this.active);
		if (this.active)
		{
			data.writeItemStack(this.queenStack);
			this.hasFlowersCache.writeData(data);
		}
	}

	@Override
	public void readData(PacketBuffer data) throws IOException
	{
		boolean active = data.readBoolean();
		this.setActive(active);
		if (active)
		{
			this.queenStack = data.readItemStack();
			this.queen = BeeManager.beeRoot.getMember(this.queenStack);
			this.hasFlowersCache.readData(data);
		}
	}

	/* Activatable */
	private void setActive(boolean active)
	{
		if (this.active == active)
			return;
		this.active = active;

		this.syncToClient();
	}

	/* UPDATING */

	@Override
	public boolean canWork()
	{

		IErrorLogic errorLogic = this.housing.getErrorLogic();
		errorLogic.clearErrors();

		IBeeHousingInventory beeInventory = this.housing.getBeeInventory();

		boolean hasSpace = addPendingProducts(beeInventory, this.spawn);
		errorLogic.setCondition(!hasSpace, EnumErrorCode.NO_SPACE_INVENTORY);

		ItemStack queenStack = beeInventory.getQueen();
		EnumBeeType beeType = BeeManager.beeRoot.getType(queenStack);
		// check if we're breeding
		if (beeType == EnumBeeType.PRINCESS)
		{
			boolean hasDrone = BeeManager.beeRoot.isDrone(beeInventory.getDrone());
			errorLogic.setCondition(!hasDrone, EnumErrorCode.NO_DRONE);

			this.setActive(false); // not active (no bee FX) when we are breeding
			return !errorLogic.hasErrors();
		}

		if (beeType == EnumBeeType.QUEEN)
		{
			if (!isQueenAlive(queenStack))
			{
				IBee dyingQueen = BeeManager.beeRoot.getMember(queenStack);
				Collection<ItemStack> spawned = killQueen(dyingQueen, this.housing, this.beeListener);
				this.spawn.addAll(spawned);

				
				int size = this.spawn.size();
				if (size > MAX_BUFFER_SIZE)
				{
					List<ItemStack> list = new ArrayList<>(this.spawn.subList(size - MAX_BUFFER_SIZE, size));
					this.spawn.clear();
					this.spawn.addAll(list);
				}
				

				queenStack = ItemStack.EMPTY;
			}
		}
		else
			queenStack = ItemStack.EMPTY;

		if (this.queenStack != queenStack)
		{
			if (!queenStack.isEmpty())
			{
				this.queen = BeeManager.beeRoot.getMember(queenStack);
				if (this.queen != null)
					this.hasFlowersCache.onNewQueen(this.queen, this.housing);
			}
			else
				this.queen = null;
			this.queenStack = queenStack;
			this.queenCanWorkCache.clear();
		}

		if (errorLogic.setCondition(this.queen == null, EnumErrorCode.NO_QUEEN))
		{
			this.setActive(false);
			this.beeProgress = 0;
			return false;
		}

		Set<IErrorState> queenErrors = this.queenCanWorkCache.queenCanWork(this.queen, this.housing);
		for (IErrorState errorState : queenErrors)
		{
			errorLogic.setCondition(true, errorState);
		}

		this.hasFlowersCache.update(this.queen, this.housing);

		boolean hasFlowers = this.hasFlowersCache.hasFlowers();
		boolean flowerCacheNeedsSync = this.hasFlowersCache.needsSync();
		errorLogic.setCondition(!hasFlowers, EnumErrorCode.NO_FLOWER);

		boolean canWork = !errorLogic.hasErrors();
		if (this.active != canWork)
			this.setActive(canWork);
		else if (flowerCacheNeedsSync)
			this.syncToClient();
		return canWork;
	}

	@Override
	public void doWork()
	{
		IBeeHousingInventory beeInventory = this.housing.getBeeInventory();
		ItemStack queenStack = beeInventory.getQueen();
		EnumBeeType beeType = BeeManager.beeRoot.getType(queenStack);
		if (beeType == EnumBeeType.PRINCESS)
			this.tickBreed();
		else if (beeType == EnumBeeType.QUEEN)
			this.queenWorkTick(this.queen, queenStack);
	}

	@Override
	public void clearCachedValues()
	{
		if (!this.housing.getWorldObj().isRemote)
		{
			this.queenCanWorkCache.clear();
			this.canWork();
			if (this.queen != null)
				this.hasFlowersCache.forceLookForFlowers(this.queen, this.housing);
		}
	}

	private void queenWorkTick(@Nullable IBee queen, ItemStack queenStack)
	{
		if (queen == null)
		{
			this.beeProgress = 0;
			this.beeProgressMax = 0;
			return;
		}

		// Effects only fire when queen can work.
		this.effectData = queen.doEffect(this.effectData, this.housing);

		// Work cycles are throttled, rather than occurring every game tick.
		this.queenWorkCycleThrottle++;
		if (this.queenWorkCycleThrottle >= ModuleApiculture.ticksPerBeeWorkCycle)
		{
			this.queenWorkCycleThrottle = 0;

			doProduction(queen, this.housing, this.beeListener);
			World world = this.housing.getWorldObj();
			List<IBlockState> flowers = this.hasFlowersCache.getFlowers(world);
			if (flowers.size() < ModuleApiculture.maxFlowersSpawnedPerHive)
			{
				BlockPos blockPos = queen.plantFlowerRandom(this.housing, flowers);
				if (blockPos != null)
					this.hasFlowersCache.addFlowerPos(blockPos);
			}
			this.pollenHandler.doPollination(queen, this.housing, this.beeListener);

			// Age the queen
			IBeeGenome mate = queen.getMate();
			Preconditions.checkNotNull(mate);
			float lifespanModifier = this.beeModifier.getLifespanModifier(queen.getGenome(), mate, 1.0f);
			queen.age(world, lifespanModifier);

			// Write the changed queen back into the item stack.
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			queen.writeToNBT(nbttagcompound);
			queenStack.setTagCompound(nbttagcompound);
			this.housing.getBeeInventory().setQueen(queenStack);
		}

		this.beeProgress = queen.getHealth();
		this.beeProgressMax = queen.getMaxHealth();
	}

	private static void doProduction(IBee queen, IBeeHousing beeHousing, IBeeListener beeListener)
	{
		// Produce and add stacks
		List<ItemStack> products = queen.produceStacks(beeHousing);
		beeListener.wearOutEquipment(1);

		IBeeHousingInventory beeInventory = beeHousing.getBeeInventory();

		for (ItemStack stack : products)
		{
			beeInventory.addProduct(stack, false);
		}
	}

	private static boolean addPendingProducts(IBeeHousingInventory beeInventory, Stack<ItemStack> spawn)
	{
		boolean housingHasSpace = true;

		while (!spawn.isEmpty())
		{
			ItemStack next = spawn.peek();
			if (beeInventory.addProduct(next, true))
				spawn.pop();
			else
			{
				housingHasSpace = false;
				break;
			}
		}

		return housingHasSpace;
	}

	/**
	 * Checks if a queen is alive. Much faster than reading the whole bee nbt
	 */
	private static boolean isQueenAlive(ItemStack queenStack)
	{
		if (queenStack.isEmpty())
			return false;
		NBTTagCompound nbtTagCompound = queenStack.getTagCompound();
		if (nbtTagCompound == null)
			return false;
		int health = nbtTagCompound.getInteger("Health");
		return health > 0;
	}

	// / BREEDING
	private void tickBreed()
	{
		this.beeProgressMax = totalBreedingTime;

		IBeeHousingInventory beeInventory = this.housing.getBeeInventory();

		ItemStack droneStack = beeInventory.getDrone();
		ItemStack princessStack = beeInventory.getQueen();

		EnumBeeType droneType = BeeManager.beeRoot.getType(droneStack);
		EnumBeeType princessType = BeeManager.beeRoot.getType(princessStack);
		if (droneType != EnumBeeType.DRONE || princessType != EnumBeeType.PRINCESS)
		{
			this.beeProgress = 0;
			return;
		}

		if (this.beeProgress < totalBreedingTime)
			this.beeProgress++;
		if (this.beeProgress < totalBreedingTime)
			return;

		// Mate and replace princess with queen
		IBee princess = BeeManager.beeRoot.getMember(princessStack);
		IBee drone = BeeManager.beeRoot.getMember(droneStack);
		princess.mate(drone);

		NBTTagCompound nbttagcompound = new NBTTagCompound();
		princess.writeToNBT(nbttagcompound);
		this.queenStack = new ItemStack(ModuleApiculture.getItems().beeQueenGE);
		this.queenStack.setTagCompound(nbttagcompound);

		beeInventory.setQueen(this.queenStack);

		// Register the new queen with the breeding tracker
		BeeManager.beeRoot.getBreedingTracker(this.housing.getWorldObj(), this.housing.getOwner()).registerQueen(princess);

		// Remove drone
		beeInventory.getDrone().shrink(1);

		// Reset breeding time
		this.queen = princess;
		this.beeProgress = princess.getHealth();
		this.beeProgressMax = princess.getMaxHealth();
	}

	private static Collection<ItemStack> killQueen(IBee queen, IBeeHousing beeHousing, IBeeListener beeListener)
	{
		IBeeHousingInventory beeInventory = beeHousing.getBeeInventory();

		Collection<ItemStack> spawn;

		if (queen.canSpawn())
		{
			spawn = spawnOffspring(queen, beeHousing);
			beeListener.onQueenDeath();
			beeInventory.getQueen().setCount(0);
			beeInventory.setQueen(ItemStack.EMPTY);
		}
		else
		{
			Log.warning("Tried to spawn offspring off an unmated queen. Devolving her to a princess.");

			ItemStack convert = new ItemStack(ModuleApiculture.getItems().beePrincessGE);
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			queen.writeToNBT(nbttagcompound);
			convert.setTagCompound(nbttagcompound);

			spawn = Collections.singleton(convert);
			beeInventory.setQueen(ItemStack.EMPTY);
		}

		return spawn;
	}

	/**
	 * Creates the succeeding princess and between one and three drones.
	 */
	private static Collection<ItemStack> spawnOffspring(IBee queen, IBeeHousing beeHousing)
	{

		World world = beeHousing.getWorldObj();

		Stack<ItemStack> offspring = new Stack<>();
		IApiaristTracker breedingTracker = BeeManager.beeRoot.getBreedingTracker(world, beeHousing.getOwner());

		// Princess
		boolean secondPrincess = world.rand.nextInt(10000) < ModuleApiculture.getSecondPrincessChance() * 100;
		int count = secondPrincess ? 2 : 1;
		while (count > 0)
		{
			count--;
			IBee heiress = queen.spawnPrincess(beeHousing);
			if (heiress != null)
			{
				ItemStack princess = BeeManager.beeRoot.getMemberStack(heiress, EnumBeeType.PRINCESS);
				breedingTracker.registerPrincess(heiress);
				offspring.push(princess);
			}
		}

		// Drones
		List<IBee> drones = queen.spawnDrones(beeHousing);
		for (IBee drone : drones)
		{
			ItemStack droneStack = BeeManager.beeRoot.getMemberStack(drone, EnumBeeType.DRONE);
			breedingTracker.registerDrone(drone);
			offspring.push(droneStack);
		}

		IBeeHousingInventory beeInventory = beeHousing.getBeeInventory();

		Collection<ItemStack> spawn = new ArrayList<>();

		while (!offspring.isEmpty())
		{
			ItemStack spawned = offspring.pop();
			if (!beeInventory.addProduct(spawned, true))
				spawn.add(spawned);
		}

		return spawn;
	}

	/* CLIENT */

	@Override
	public void syncToClient()
	{
		World world = this.housing.getWorldObj();
		if (world != null && !world.isRemote)
			if (this.housing instanceof Entity)
			{
				Entity housingEntity = (Entity) this.housing;
				NetworkUtil.sendNetworkPacket(new PacketBeeLogicActiveEntity(this.housing, housingEntity), housingEntity.getPosition(), world);
			}
			else
				NetworkUtil.sendNetworkPacket(new PacketBeeLogicActive(this.housing), this.housing.getCoordinates(), world);
	}

	@Override
	public void syncToClient(EntityPlayerMP player)
	{
		World world = this.housing.getWorldObj();
		if (world != null && !world.isRemote)
			if (this.housing instanceof TileEntity)
				NetworkUtil.sendToPlayer(new PacketBeeLogicActive(this.housing), player);
			else if (this.housing instanceof Entity)
				NetworkUtil.sendToPlayer(new PacketBeeLogicActiveEntity(this.housing, (Entity) this.housing), player);
	}

	@Override
	public int getBeeProgressPercent()
	{
		if (this.beeProgressMax == 0)
			return 0;

		return Math.round(this.beeProgress * 100f / this.beeProgressMax);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canDoBeeFX()
	{
		return !Minecraft.getMinecraft().isGamePaused() && this.active;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void doBeeFX()
	{
		if (this.queen != null)
			this.queen.doFX(this.effectData, this.housing);
	}

	@Override
	public List<BlockPos> getFlowerPositions()
	{
		return this.hasFlowersCache.getFlowerCoords();
	}

	private static class QueenCanWorkCache
	{
		private static final int ticksPerCheckQueenCanWork = 10;

		private Set<IErrorState> queenCanWorkCached = Collections.emptySet();
		private int queenCanWorkCooldown = 0;

		public Set<IErrorState> queenCanWork(IBee queen, IBeeHousing beeHousing)
		{
			if (this.queenCanWorkCooldown <= 0)
			{
				this.queenCanWorkCached = queen.getCanWork(beeHousing);
				this.queenCanWorkCooldown = ticksPerCheckQueenCanWork;
			}
			else
				this.queenCanWorkCooldown--;

			return this.queenCanWorkCached;
		}

		public void clear()
		{
			this.queenCanWorkCached.clear();
			this.queenCanWorkCooldown = 0;
		}
	}

	private static class PollenHandler
	{
		private static final int MAX_POLLINATION_ATTEMPTS = 20;

		@Nullable
		private IIndividual pollen;
		private int attemptedPollinations = 0;

		public void doPollination(IBee queen, IBeeHousing beeHousing, IBeeListener beeListener)
		{
			// Get pollen if none available yet
			if (this.pollen == null)
			{
				this.attemptedPollinations = 0;
				this.pollen = queen.retrievePollen(beeHousing);
				if (this.pollen != null)
					if (beeListener.onPollenRetrieved(this.pollen))
						this.pollen = null;
			}

			if (this.pollen != null)
			{
				this.attemptedPollinations++;
				if (queen.pollinateRandom(beeHousing, this.pollen) || this.attemptedPollinations >= MAX_POLLINATION_ATTEMPTS)
					this.pollen = null;
			}
		}
	}

}
