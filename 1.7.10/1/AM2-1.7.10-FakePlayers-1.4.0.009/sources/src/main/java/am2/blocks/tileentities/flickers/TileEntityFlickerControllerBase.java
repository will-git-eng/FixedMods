package am2.blocks.tileentities.flickers;

import java.util.HashMap;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;

import am2.api.flickers.IFlickerController;
import am2.api.flickers.IFlickerFunctionality;
import am2.api.power.PowerTypes;
import am2.api.spell.enums.Affinity;
import am2.blocks.tileentities.TileEntityAMPower;
import am2.blocks.tileentities.TileEntityFlickerHabitat;
import am2.items.ItemsCommonProxy;
import am2.power.PowerNodeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityFlickerControllerBase extends TileEntityAMPower implements IFlickerController
{
	private HashMap<Integer, byte[]> sigilMetadata = new HashMap();
	private IFlickerFunctionality operator;
	private int tickCounter;
	Affinity[] nearbyList = new Affinity[6];
	private boolean lastOpWasPowered = false;
	private boolean firstOp = true;

	    
	public final FakePlayerContainer fake = new FakePlayerContainerTileEntity(ModUtils.profile, this);
	    

	public TileEntityFlickerControllerBase()
	{
		super(500);
	}

	protected void setOperator(IFlickerFunctionality operator)
	{
		if (this.operator != null)
			this.operator.RemoveOperator(super.worldObj, this, PowerNodeRegistry.For(super.worldObj).checkPower(this, this.operator.PowerPerOperation()), this.nearbyList);

		this.operator = operator;
		this.tickCounter = 0;
	}

	public void updateOperator(ItemStack stack)
	{
		if (stack != null && stack.getItem() == ItemsCommonProxy.flickerFocus)
			this.operator = FlickerOperatorRegistry.instance.getOperatorForMask(stack.getItemDamage());
	}

	public void scanForNearbyUpgrades()
	{
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity te = super.worldObj.getTileEntity(super.xCoord + direction.offsetX, super.yCoord + direction.offsetY, super.zCoord + direction.offsetZ);
			if (te != null && te instanceof TileEntityFlickerHabitat)
				this.nearbyList[direction.ordinal()] = ((TileEntityFlickerHabitat) te).getSelectedAffinity();
		}

	}

	public void notifyOfNearbyUpgradeChange(TileEntity neighbor)
	{
		if (neighbor instanceof TileEntityFlickerHabitat)
		{
			ForgeDirection direction = this.getNeighboringForgeDirection(neighbor);
			if (direction != ForgeDirection.UNKNOWN)
				this.nearbyList[direction.ordinal()] = ((TileEntityFlickerHabitat) neighbor).getSelectedAffinity();
		}

	}

	private ForgeDirection getNeighboringForgeDirection(TileEntity neighbor)
	{
		return neighbor.xCoord == super.xCoord && neighbor.yCoord == super.yCoord && neighbor.zCoord == super.zCoord + 1 ? ForgeDirection.SOUTH : neighbor.xCoord == super.xCoord && neighbor.yCoord == super.yCoord && neighbor.zCoord == super.zCoord - 1 ? ForgeDirection.NORTH : neighbor.xCoord == super.xCoord + 1 && neighbor.yCoord == super.yCoord && neighbor.zCoord == super.zCoord ? ForgeDirection.EAST : neighbor.xCoord == super.xCoord - 1 && neighbor.yCoord == super.yCoord && neighbor.zCoord == super.zCoord ? ForgeDirection.WEST : neighbor.xCoord == super.xCoord && neighbor.yCoord == super.yCoord + 1 && neighbor.zCoord == super.zCoord ? ForgeDirection.UP : neighbor.xCoord == super.xCoord && neighbor.yCoord == super.yCoord - 1 && neighbor.zCoord == super.zCoord ? ForgeDirection.DOWN : ForgeDirection.UNKNOWN;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (super.worldObj.isBlockIndirectlyGettingPowered(super.xCoord, super.yCoord, super.zCoord))
			++this.tickCounter;
		else if (this.operator != null)
		{
			boolean powered = PowerNodeRegistry.For(super.worldObj).checkPower(this, this.operator.PowerPerOperation());
			Affinity[] unpoweredNeighbors = this.getUnpoweredNeighbors();
			if (this.tickCounter++ >= this.operator.TimeBetweenOperation(powered, unpoweredNeighbors))
			{
				this.tickCounter = 0;
				if ((!powered || !this.operator.RequiresPower()) && this.operator.RequiresPower())
				{
					if (this.lastOpWasPowered && this.operator.RequiresPower() && !powered)
					{
						this.operator.RemoveOperator(super.worldObj, this, powered, unpoweredNeighbors);
						this.lastOpWasPowered = false;
					}
				}
				else
				{
					if (this.firstOp)
					{
						this.scanForNearbyUpgrades();
						this.firstOp = false;
					}

					boolean success = this.operator.DoOperation(super.worldObj, this, powered, unpoweredNeighbors);
					if (success || this.operator.RequiresPower())
						PowerNodeRegistry.For(super.worldObj).consumePower(this, PowerNodeRegistry.For(super.worldObj).getHighestPowerType(this), this.operator.PowerPerOperation());

					this.lastOpWasPowered = true;
				}
			}
		}
	}

	private Affinity[] getUnpoweredNeighbors()
	{
		Affinity[] aff = new Affinity[ForgeDirection.values().length];

		for (int i = 0; i < this.nearbyList.length; ++i)
		{
			ForgeDirection dir = ForgeDirection.values()[i];
			if (this.nearbyList[i] != null && !super.worldObj.isBlockIndirectlyGettingPowered(super.xCoord + dir.offsetX, super.yCoord + dir.offsetY, super.zCoord + dir.offsetZ))
				aff[i] = this.nearbyList[i];
			else
				aff[i] = null;
		}

		return aff;
	}

	private Integer getFlagForOperator(IFlickerFunctionality operator)
	{
		return Integer.valueOf(FlickerOperatorRegistry.instance.getMaskForOperator(operator));
	}

	@Override
	public void setMetadata(IFlickerFunctionality operator, byte[] meta)
	{
		this.sigilMetadata.put(this.getFlagForOperator(operator), meta);
	}

	@Override
	public byte[] getMetadata(IFlickerFunctionality operator)
	{
		byte[] arr = this.sigilMetadata.get(this.getFlagForOperator(operator));
		return arr != null ? arr : new byte[0];
	}

	@Override
	public void removeMetadata(IFlickerFunctionality operator)
	{
		this.sigilMetadata.remove(this.getFlagForOperator(operator));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList sigilMetaStore = new NBTTagList();

		for (Integer i : this.sigilMetadata.keySet())
		{
			NBTTagCompound sigilMetaEntry = new NBTTagCompound();
			sigilMetaEntry.setInteger("sigil_mask", i.intValue());
			sigilMetaEntry.setByteArray("sigil_meta", this.sigilMetadata.get(i));
			sigilMetaStore.appendTag(sigilMetaEntry);
		}

		nbt.setTag("sigil_metadata_collection", sigilMetaStore);

		    
		this.fake.writeToNBT(nbt);
		    
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.sigilMetadata = new HashMap();
		NBTTagList sigilMetaStore = nbt.getTagList("sigil_metadata_collection", 10);

		for (int i = 0; i < sigilMetaStore.tagCount(); ++i)
		{
			NBTTagCompound sigilMetaEntry = sigilMetaStore.getCompoundTagAt(i);
			Integer mask = Integer.valueOf(sigilMetaEntry.getInteger("sigil_mask"));
			byte[] meta = sigilMetaEntry.getByteArray("sigil_meta");
			this.sigilMetadata.put(mask, meta);
		}

		    
		this.fake.readFromNBT(nbt);
		    
	}

	@Override
	public boolean canProvidePower(PowerTypes type)
	{
		return false;
	}

	@Override
	public boolean canRelayPower(PowerTypes type)
	{
		return false;
	}

	@Override
	public boolean canRequestPower()
	{
		return true;
	}

	@Override
	public boolean isSource()
	{
		return false;
	}

	@Override
	public int getChargeRate()
	{
		return 100;
	}

	@Override
	public PowerTypes[] getValidPowerTypes()
	{
		return PowerTypes.all();
	}

	@Override
	public float particleOffset(int axis)
	{
		return 0.5F;
	}

	public Affinity[] getNearbyUpgrades()
	{
		return this.getUnpoweredNeighbors();
	}
}
