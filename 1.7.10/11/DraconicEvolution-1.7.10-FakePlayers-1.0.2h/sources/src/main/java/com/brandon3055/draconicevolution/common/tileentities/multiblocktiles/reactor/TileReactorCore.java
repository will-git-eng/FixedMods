package com.brandon3055.draconicevolution.common.tileentities.multiblocktiles.reactor;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.common.handlers.ProcessHandler;
import com.brandon3055.brandonscore.common.utills.Utills;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.ReactorSound;
import com.brandon3055.draconicevolution.client.gui.GuiHandler;
import com.brandon3055.draconicevolution.client.render.particle.Particles;
import com.brandon3055.draconicevolution.common.blocks.multiblock.IReactorPart;
import com.brandon3055.draconicevolution.common.blocks.multiblock.MultiblockHelper.TileLocation;
import com.brandon3055.draconicevolution.common.handler.ConfigHandler;
import com.brandon3055.draconicevolution.common.lib.References;
import com.brandon3055.draconicevolution.common.tileentities.TileObjectSync;
import ru.will.git.draconicevolution.EventConfig;
import ru.will.git.draconicevolution.ExplosionByPlayer;
import ru.will.git.draconicevolution.ModUtils;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class TileReactorCore extends TileObjectSync
{
	public static final int MAX_SLAVE_RANGE = 10;
	public static final int STATE_OFFLINE = 0;
	public static final int STATE_START = 1;
	public static final int STATE_ONLINE = 2;
	public static final int STATE_STOP = 3;
	public static final int STATE_INVALID = 4;

	public int reactorState = 0;
	public float renderRotation = 0;
	public float renderSpeed = 0;
	public boolean isStructureValid = false;
	public float stabilizerRender = 0F;
	private boolean startupInitialized = false;

	public int reactorFuel = 0;



	public double reactionTemperature = 20;
	public double maxReactTemperature = 10000;

	public double fieldCharge = 0;
	public double maxFieldCharge = 0;

	public int energySaturation = 0;
	public int maxEnergySaturation = 0;

	@SideOnly(Side.CLIENT)





	@Override
	public void updateEntity()
	{
		this.tick++;
		if (this.worldObj.isRemote)
		{
			this.updateSound();

			this.renderSpeed = (float) Math.min((this.reactionTemperature - 20) / 2000D, 1D);
			this.stabilizerRender = (float) Math.min(this.fieldCharge / (this.maxFieldCharge * 0.1D), 1D);

			this.checkPlayerCollision();
			return;


		switch (this.reactorState)
		{
			case STATE_OFFLINE:
				this.offlineTick();
				break;
			case STATE_START:
				this.startingTick();
				break;
			case STATE_ONLINE:
				this.runTick();
				break;
			case STATE_STOP:
				this.runTick();
				break;
		}

		this.detectAndSendChanges();
	}

	@SideOnly(Side.CLIENT)
	private void updateSound()
	{
		if (this.reactorSound == null)
			this.reactorSound = (ReactorSound) DraconicEvolution.proxy.playISound(new ReactorSound(this));
	}

	private void checkPlayerCollision()
	{
		EntityPlayer player = BrandonsCore.proxy.getClientPlayer();
		double distance = Utills.getDistanceAtoB(player.posX, player.posY, player.posZ, this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5);
		if (distance < this.getCoreDiameter() / 2 + 0.5)
		{
			double dMod = 1D - distance / Math.max(0.1, this.getCoreDiameter() / 2 + 0.5);
			double offsetX = player.posX - this.xCoord + 0.5;
			double offsetY = player.posY - this.yCoord + 0.5;
			double offsetZ = player.posZ - this.zCoord + 0.5;
			double m = 1D * dMod;
			player.addVelocity(offsetX * m, offsetY * m, offsetZ * m);
		}
	}

	private void offlineTick()
	{
		if (this.reactionTemperature > 20)
			this.reactionTemperature -= 0.5;
		if (this.fieldCharge > 0)
			this.fieldCharge -= this.maxFieldCharge * 0.0005;
		else if (this.fieldCharge < 0)
			this.fieldCharge = 0;
		if (this.energySaturation > 0)
			this.energySaturation -= this.maxEnergySaturation * 0.000001;
		else if (this.energySaturation < 0)
			this.energySaturation = 0;
	}

	private void startingTick()
	{
		if (!this.startupInitialized)
		{
			int totalFuel = this.reactorFuel + this.convertedFuel;
			this.maxFieldCharge = totalFuel * 96.45061728395062 * 100;
			this.maxEnergySaturation = (int) (totalFuel * 96.45061728395062 * 1000);
			if (this.energySaturation > this.maxEnergySaturation)
				this.energySaturation = this.maxEnergySaturation;
			if (this.fieldCharge > this.maxFieldCharge)
				this.fieldCharge = this.maxFieldCharge;
			this.startupInitialized = true;
		}
	}

	private boolean hasExploded = false;

	public double tempDrainFactor;
	public double generationRate;
	public int fieldDrain;
	public double fieldInputRate;
	public double fuelUseRate;

	private void runTick()

		double saturation = (double) this.energySaturation / (double) this.maxEnergySaturation;
		double saturationI = (1D - (double) this.energySaturation / (double) this.maxEnergySaturation) * 99D;





		double riseAmount = (tempRiseExpo - tempRiseResist * (1D - conversion) + conversion * 1000) / 10000;
		if (this.reactorState == STATE_STOP)
		{
			if (this.reactionTemperature <= 2001)
			{
				this.reactorState = STATE_OFFLINE;
				this.startupInitialized = false;
				return;
			}
			if (this.energySaturation >= this.maxEnergySaturation * 0.99 && this.reactorFuel > 0)
				this.reactionTemperature -= 1D - conversion;
			else
				this.reactionTemperature += riseAmount * 10;
		}
		else

		int baseMaxRFt = (int) (this.maxEnergySaturation / 1000D * ConfigHandler.reactorOutputMultiplier * 1.5D);
		int maxRFt = (int) (baseMaxRFt * (1D + conversion * 2));
		this.generationRate = (1D - saturation) * maxRFt;




		double fieldNegPercent = 1D - this.fieldCharge / this.maxFieldCharge;



		this.conversionUnit += this.fuelUseRate;
		if (this.conversionUnit >= 1 && this.reactorFuel > 0)
		{
			this.conversionUnit--;
			this.reactorFuel--;
			this.convertedFuel++;

		if (this.fieldCharge <= 0 && !this.hasExploded)
		{
			this.hasExploded = true;
			this.goBoom();

	}

	private void goBoom()


		{

				ExplosionByPlayer.createExplosion(this.fake.get(), this.worldObj, null, this.xCoord, this.yCoord, this.zCoord, 5, true);
			else

				float power = 2F + (this.convertedFuel + this.reactorFuel) / 10369F * 18F;



				ProcessHandler.addProcess(explosion);
				this.sendObjectToClient(References.INT_ID, 100, (int) (power * 10F), new NetworkRegistry.TargetPoint(this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 512));
			}
		}

		this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
	}

	public void validateStructure()
	{

		Iterator<TileLocation> i = this.stabilizerLocations.iterator();
		List<TileReactorStabilizer> stabilizers = new ArrayList<TileReactorStabilizer>();
		while (i.hasNext())
		{
			TileLocation location = i.next();
			if (!(location.getTileEntity(this.worldObj) instanceof TileReactorStabilizer) || !((TileReactorStabilizer) location.getTileEntity(this.worldObj)).masterLocation.isThisLocation(this.xCoord, this.yCoord, this.zCoord))
			{
				i.remove();
				updateRequired = true;
			}
			else
				stabilizers.add((TileReactorStabilizer) location.getTileEntity(this.worldObj));

		this.isStructureValid = false;
		List<TileReactorStabilizer> checkList = new ArrayList<TileReactorStabilizer>();
		for (TileReactorStabilizer stabilizer : stabilizers)
		{
			if (checkList.contains(stabilizer))
				continue;

			for (TileReactorStabilizer comp : stabilizers)
			{
				if (!(comp == stabilizer || checkList.contains(comp)) && ForgeDirection.getOrientation(comp.facingDirection) == ForgeDirection.getOrientation(stabilizer.facingDirection).getOpposite())
				{
					checkList.add(comp);
					checkList.add(stabilizer);
					if (checkList.size() == 4)
					{
						this.isStructureValid = true;
						if (this.reactorState == STATE_INVALID)
							this.reactorState = STATE_OFFLINE;
						break;
					}
				}
			}
		}

		for (TileReactorStabilizer stabilizer : stabilizers)
		{
			if (Utills.getDistanceAtoB(stabilizer.xCoord, stabilizer.yCoord, stabilizer.zCoord, this.xCoord, this.yCoord, this.zCoord) < this.getMaxCoreDiameter() / 2 + 1)
			{
				this.isStructureValid = false;
				break;
			}
		}

		if (!this.isStructureValid)
		{
			this.reactorState = STATE_INVALID;
			if (this.reactionTemperature >= 2000)
				this.goBoom();
		}

		if (updateRequired)
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	public void onPlaced()
	{
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			boolean flag = false;
			for (int i = 1; i < MAX_SLAVE_RANGE && !flag; i++)
			{
				TileLocation location = new TileLocation(this.xCoord + direction.offsetX * i, this.yCoord + direction.offsetY * i, this.zCoord + direction.offsetZ * i);
				if (location.getTileEntity(this.worldObj) != null)
				{
					if (location.getTileEntity(this.worldObj) instanceof IReactorPart && !((IReactorPart) location.getTileEntity(this.worldObj)).getMaster().initialized)
						((IReactorPart) location.getTileEntity(this.worldObj)).checkForMaster();
					flag = true;
				}
			}
		}
		this.validateStructure();
	}

	public void onBroken()
	{
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			for (int i = 1; i < MAX_SLAVE_RANGE; i++)
			{
				TileLocation location = new TileLocation(this.xCoord + direction.offsetX * i, this.yCoord + direction.offsetY * i, this.zCoord + direction.offsetZ * i);
				if (location.getTileEntity(this.worldObj) instanceof IReactorPart && ((IReactorPart) location.getTileEntity(this.worldObj)).getMaster().compareTo(new TileLocation(this.xCoord, this.yCoord, this.zCoord)) == 0)
					((IReactorPart) location.getTileEntity(this.worldObj)).shutDown();
			}
		}

		if (this.reactionTemperature >= 2000)
			this.goBoom();

	}

	public boolean onStructureRightClicked(EntityPlayer player)
	{
		if (!this.worldObj.isRemote)
			player.openGui(DraconicEvolution.instance, GuiHandler.GUIID_REACTOR, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		return true;
	}

	public int injectEnergy(int RF)
	{
		int received = 0;
		if (this.reactorState == STATE_START)
		{
			if (!this.startupInitialized)
				return 0;
			if (this.fieldCharge < this.maxFieldCharge / 2)
			{
				received = Math.min(RF, (int) (this.maxFieldCharge / 2) - (int) this.fieldCharge + 1);
				this.fieldCharge += received;
				if (this.fieldCharge > this.maxFieldCharge / 2)
					this.fieldCharge = this.maxFieldCharge / 2;
			}
			else if (this.energySaturation < this.maxEnergySaturation / 2)
			{
				received = Math.min(RF, this.maxEnergySaturation / 2 - this.energySaturation);
				this.energySaturation += received;
			}
			else if (this.reactionTemperature < 2000)
			{
				received = RF;
				this.reactionTemperature += received / (1000D + this.reactorFuel * 10);
				if (this.reactionTemperature > 2000)
					this.reactionTemperature = 2000;
			}
		}
		else if (this.reactorState == STATE_ONLINE || this.reactorState == STATE_STOP)
		{
			this.fieldCharge += RF * (1D - this.fieldCharge / this.maxFieldCharge);
			if (this.fieldCharge > this.maxFieldCharge)
				this.fieldCharge = this.maxFieldCharge;
			return RF;
		}
		return received;
	}

	public boolean canStart()
	{
		this.validateStructure();
		return this.reactionTemperature >= 2000 && this.fieldCharge >= this.maxFieldCharge / 2 && this.energySaturation >= this.maxEnergySaturation / 2 && this.isStructureValid && this.convertedFuel + this.reactorFuel + this.conversionUnit >= 144;
	}

	public boolean canCharge()
	{
		this.validateStructure();
		return this.reactorState != STATE_ONLINE && this.isStructureValid && this.convertedFuel + this.reactorFuel + this.conversionUnit >= 144;
	}

	public boolean canStop()
	{
		this.validateStructure();
		return this.reactorState != STATE_OFFLINE && this.isStructureValid;
	}

	public void processButtonPress(int button)
	{
		if (button == 0 && this.canCharge())
			this.reactorState = STATE_START;
		else if (button == 1 && this.canStart())
			this.reactorState = STATE_ONLINE;
		else if (button == 2 && this.canStop())
			this.reactorState = STATE_STOP;
	}

	public double getCoreDiameter()

		double volume = (this.reactorFuel + this.convertedFuel) / 1296D;
		volume *= 1 + this.reactionTemperature / this.maxReactTemperature * 10D;
		return Math.cbrt(volume / (4 / 3 * Math.PI)) * 2;
	}

	public double getMaxCoreDiameter()

		double volume = (this.reactorFuel + this.convertedFuel) / 1296D;
		volume *= 1 + 1 * 10D;
		return Math.cbrt(volume / (4 / 3 * Math.PI)) * 2;
	}

	private boolean isStructureValidCach = false;
	private boolean startupInitializedCach = false;
	private int reactorStateCach = -1;
	private int reactorFuelCach = -1;
	private int convertedFuelCach = -1;
	private int energySaturationCach = -1;
	private int maxEnergySaturationCach = -1;
	private double reactionTemperatureCach = -1;
	private double maxReactTemperatureCach = -1;
	private double fieldChargeCach = -1;
	private double maxFieldChargeCach = -1;

	private void detectAndSendChanges()
	{
		NetworkRegistry.TargetPoint tp = new NetworkRegistry.TargetPoint(this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 128);
		if (this.reactionTemperatureCach != this.reactionTemperature)
			this.reactionTemperatureCach = (Double) this.sendObjectToClient(References.DOUBLE_ID, 8, this.reactionTemperature, tp);
		if (this.tick % 10 != 0)
			return;
		if (this.isStructureValidCach != this.isStructureValid)

		if (this.startupInitializedCach != this.startupInitialized)
			this.startupInitializedCach = (Boolean) this.sendObjectToClient(References.BOOLEAN_ID, 2, this.startupInitialized, tp);
		if (this.reactorStateCach != this.reactorState)
			this.reactorStateCach = (Integer) this.sendObjectToClient(References.INT_ID, 3, this.reactorState, tp);
		if (this.reactorFuelCach != this.reactorFuel)
			this.reactorFuelCach = (Integer) this.sendObjectToClient(References.INT_ID, 4, this.reactorFuel, tp);
		if (this.convertedFuelCach != this.convertedFuel)
			this.convertedFuelCach = (Integer) this.sendObjectToClient(References.INT_ID, 5, this.convertedFuel, tp);
		if (this.energySaturationCach != this.energySaturation)
			this.energySaturationCach = (Integer) this.sendObjectToClient(References.INT_ID, 6, this.energySaturation, tp);
		if (this.maxEnergySaturationCach != this.maxEnergySaturation)
			this.maxEnergySaturationCach = (Integer) this.sendObjectToClient(References.INT_ID, 7, this.maxEnergySaturation, tp);
		if (this.maxReactTemperatureCach != this.maxReactTemperature)
			this.maxReactTemperatureCach = (Double) this.sendObjectToClient(References.DOUBLE_ID, 9, this.maxReactTemperature, tp);
		if (this.fieldChargeCach != this.fieldCharge)
			this.fieldChargeCach = (Double) this.sendObjectToClient(References.DOUBLE_ID, 10, this.fieldCharge, tp);
		if (this.maxFieldChargeCach != this.maxFieldCharge)
			this.maxFieldChargeCach = (Double) this.sendObjectToClient(References.DOUBLE_ID, 11, this.maxFieldCharge, tp);
	}

	public int getComparatorOutput(int rsMode)
	{
		switch (rsMode)
		{
			case IReactorPart.RMODE_TEMP:
				return this.toRSStrength(this.reactionTemperature, this.maxReactTemperature, rsMode);
			case IReactorPart.RMODE_TEMP_INV:
				return 15 - this.toRSStrength(this.reactionTemperature, this.maxReactTemperature, rsMode);
			case IReactorPart.RMODE_FIELD:
				return this.toRSStrength(this.fieldCharge, this.maxFieldCharge, rsMode);
			case IReactorPart.RMODE_FIELD_INV:
				return 15 - this.toRSStrength(this.fieldCharge, this.maxFieldCharge, rsMode);
			case IReactorPart.RMODE_SAT:
				return this.toRSStrength(this.energySaturation, this.maxEnergySaturation, rsMode);
			case IReactorPart.RMODE_SAT_INV:
				return 15 - this.toRSStrength(this.energySaturation, this.maxEnergySaturation, rsMode);
			case IReactorPart.RMODE_FUEL:
				return this.toRSStrength(this.convertedFuel + this.conversionUnit, this.reactorFuel - this.conversionUnit, rsMode);
			case IReactorPart.RMODE_FUEL_INV:
				return 15 - this.toRSStrength(this.convertedFuel + this.conversionUnit, this.reactorFuel - this.conversionUnit, rsMode);
		}
		return 0;
	}

	private int toRSStrength(double value, double maxValue, int mode)
	{
		if (maxValue == 0)
			return 0;
		double d = value / maxValue;
		int rs = (int) (d * 15D);
		switch (mode)
		{
			case IReactorPart.RMODE_FIELD:
			case IReactorPart.RMODE_FIELD_INV:
				if (d < 0.1)
					rs = 0;
				break;
			case IReactorPart.RMODE_FUEL:
			case IReactorPart.RMODE_FUEL_INV:
				if (d > 0.9)
					rs = 15;
				break;
		}
		return rs;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void receiveObjectFromServer(int index, Object object)
	{
		switch (index)
		{
			case 0:
				this.isStructureValid = (Boolean) object;

			case 2:
				this.startupInitialized = (Boolean) object;
				break;
			case 3:
				this.reactorState = (Integer) object;
				break;
			case 4:
				this.reactorFuel = (Integer) object;
				break;
			case 5:
				this.convertedFuel = (Integer) object;
				break;
			case 6:
				this.energySaturation = (Integer) object;
				break;
			case 7:
				this.maxEnergySaturation = (Integer) object;
				break;
			case 8:
				this.reactionTemperature = (Double) object;
				break;
			case 9:
				this.maxReactTemperature = (Double) object;
				break;
			case 10:
				this.fieldCharge = (Double) object;
				break;
			case 11:
				this.maxFieldCharge = (Double) object;
				break;
			case 100:
				FMLClientHandler.instance().getClient().effectRenderer.addEffect(new Particles.ReactorExplosionParticle(this.worldObj, this.xCoord, this.yCoord, this.zCoord, (Integer) object));
		}
		super.receiveObjectFromServer(index, object);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public double getMaxRenderDistanceSquared()
	{
		return 40960.0D;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		this.readFromNBT(pkt.func_148857_g());
	}

	@Override
	public void writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);

		NBTTagList stabilizerList = new NBTTagList();
		for (TileLocation offset : this.stabilizerLocations)
		{
			NBTTagCompound compound1 = new NBTTagCompound();
			offset.writeToNBT(compound1, "tag");
			stabilizerList.appendTag(compound1);
		}
		if (stabilizerList.tagCount() > 0)
			compound.setTag("Stabilizers", stabilizerList);

		compound.setByte("State", (byte) this.reactorState);
		compound.setBoolean("isStructureValid", this.isStructureValid);

		compound.setInteger("energySaturation", this.energySaturation);
		compound.setInteger("maxEnergySaturation", this.maxEnergySaturation);
		compound.setInteger("reactorFuel", this.reactorFuel);
		compound.setInteger("convertedFuel", this.convertedFuel);
		compound.setDouble("reactionTemperature", this.reactionTemperature);
		compound.setDouble("maxReactTemperature", this.maxReactTemperature);
		compound.setDouble("fieldCharge", this.fieldCharge);


	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);

		this.stabilizerLocations = new ArrayList<TileLocation>();
		if (compound.hasKey("Stabilizers"))
		{
			NBTTagList stabilizerList = compound.getTagList("Stabilizers", 10);
			for (int i = 0; i < stabilizerList.tagCount(); i++)
			{
				TileLocation offset = new TileLocation();
				offset.readFromNBT(stabilizerList.getCompoundTagAt(i), "tag");
				this.stabilizerLocations.add(offset);
			}
		}

		this.reactorState = compound.getByte("State");
		this.isStructureValid = compound.getBoolean("isStructureValid");

		this.energySaturation = compound.getInteger("energySaturation");
		this.maxEnergySaturation = compound.getInteger("maxEnergySaturation");
		this.reactorFuel = compound.getInteger("reactorFuel");
		this.convertedFuel = compound.getInteger("convertedFuel");
		this.reactionTemperature = compound.getDouble("reactionTemperature");
		this.maxReactTemperature = compound.getDouble("maxReactTemperature");
		this.fieldCharge = compound.getDouble("fieldCharge");


	}
}
