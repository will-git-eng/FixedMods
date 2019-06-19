package chylex.hee.tileentity;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.block.BlockEnergyCluster;
import chylex.hee.mechanics.energy.EnergyChunkData;
import chylex.hee.mechanics.energy.EnergyClusterData;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C10ParticleEnergyTransfer;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.ColorUtil;
import chylex.hee.system.util.MathUtil;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import ru.will.git.hee.ModUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class TileEntityEnergyCluster extends TileEntityAbstractSynchronized
{
	public final EnergyClusterData data;
	public boolean shouldNotExplode = false;
	private boolean shouldBeDestroyedSilently = false;
	private byte[] colRgb;
	private BlockPosM cachedCoords = new BlockPosM(0, -1, 0);

	    
	public final FakePlayerContainer fake = new FakePlayerContainerTileEntity(ModUtils.profile, this);
	    

	public TileEntityEnergyCluster()
	{
		this.data = new EnergyClusterData();
	}

	public TileEntityEnergyCluster(World world)
	{
		this();
		float[] rgb = ColorUtil.hsvToRgb(world.rand.nextFloat(), 0.5F, 0.65F);
		this.colRgb = new byte[] { (byte) (Math.floor(rgb[0] * 255F) - 128), (byte) (Math.floor(rgb[1] * 255F) - 128), (byte) (Math.floor(rgb[2] * 255F) - 128) };
	}

	@Override
	public void updateEntity()
	{
		if (this.shouldBeDestroyedSilently)
		{
			this.shouldNotExplode = true;
			BlockPosM.tmp(this.xCoord, this.yCoord, this.zCoord).setAir(this.worldObj);
			return;
		}

		if (!this.worldObj.isRemote)
		{
			if (this.cachedCoords.y == -1)
			{
				this.data.generate(this.worldObj, this.xCoord, this.zCoord);
				this.cachedCoords = new BlockPosM(this.xCoord, this.yCoord, this.zCoord);
				this.synchronize();
			}
			else if (this.cachedCoords.x != this.xCoord || this.cachedCoords.y != this.yCoord || this.cachedCoords.z != this.zCoord)
			{
				BlockEnergyCluster.destroyCluster(this);
				return;
			}

			this.data.update(this);
		}
		else if (this.worldObj.rand.nextInt(5) == 0)
			HardcoreEnderExpansion.fx.energyCluster(this);

		this.shouldNotExplode = false;
	}

	public float addEnergy(float amount, TileEntityAbstractEnergyInventory tile)
	{
		if (this.data.getEnergyLevel() < this.data.getMaxEnergyLevel())
			PacketPipeline.sendToAllAround(this, 64D, new C10ParticleEnergyTransfer(tile, this));

		float left = this.data.addEnergy(amount);
		if (!MathUtil.floatEquals(left, amount))
			this.synchronize();
		return left;
	}

	public float drainEnergy(float amount, TileEntityAbstractEnergyInventory tile)
	{
		if (this.data.getEnergyLevel() >= EnergyChunkData.minSignificantEnergy)
			PacketPipeline.sendToAllAround(this, 64D, new C10ParticleEnergyTransfer(tile, this));

		float left = this.data.drainEnergy(amount);
		if (!MathUtil.floatEquals(left, amount))
			this.synchronize();
		return left;
	}

	public float getColor(int index)
	{
		return (this.colRgb[index] + 128F) / 255F;
	}

	public byte getColorRaw(int index)
	{
		return this.colRgb[index];
	}

	@Override
	public NBTTagCompound writeTileToNBT(NBTTagCompound nbt)
	{
		nbt.setByteArray("col", this.colRgb);
		nbt.setLong("loc", this.cachedCoords.toLong());
		this.data.writeToNBT(nbt);

		    
		this.fake.writeToNBT(nbt);
		    

		return nbt;
	}

	@Override
	public void readTileFromNBT(NBTTagCompound nbt)
	{
		this.colRgb = nbt.getByteArray("col");
		this.cachedCoords = BlockPosM.fromNBT(nbt, "loc");
		this.data.readFromNBT(nbt);

		if (this.colRgb.length == 0)
			this.shouldBeDestroyedSilently = true;

		    
		this.fake.readFromNBT(nbt);
		    
	}
}
