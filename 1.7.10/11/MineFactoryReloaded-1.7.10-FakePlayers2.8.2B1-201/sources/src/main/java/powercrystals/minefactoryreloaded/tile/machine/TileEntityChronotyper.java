package powercrystals.minefactoryreloaded.tile.machine;

import cofh.lib.util.position.BlockPosition;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import powercrystals.minefactoryreloaded.gui.client.GuiChronotyper;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.container.ContainerChronotyper;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

public class TileEntityChronotyper extends TileEntityFactoryPowered
{
	private boolean _moveOld;

	public TileEntityChronotyper()
	{
		super(Machine.Chronotyper);
		createEntityHAM(this);
		this.setCanRotate(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiChronotyper(this.getContainer(var1), this);
	}

	@Override
	public ContainerChronotyper getContainer(InventoryPlayer var1)
	{
		return new ContainerChronotyper(this, var1);
	}

	@Override
	public int getSizeInventory()
	{
		return 0;
	}

	@Override
	protected boolean activateMachine()
	{
		for (Object obj : this.worldObj.getEntitiesWithinAABB(EntityAgeable.class, this._areaManager.getHarvestArea().toAxisAlignedBB()))
		{
			if (obj instanceof EntityAgeable)
			{
				EntityAgeable entity = (EntityAgeable) obj;
				if (entity.getGrowingAge() < 0 && !this._moveOld || entity.getGrowingAge() >= 0 && this._moveOld)
    
					if (this.fake.cantDamage(entity))
    

					BlockPosition pos = BlockPosition.fromRotateableTile(this);
					pos.moveBackwards(1);
					entity.setPosition(pos.x + 0.5D, pos.y + 0.5D, pos.z + 0.5D);
					return true;
				}
			}
		}

		this.setIdleTicks(this.getIdleTicksMax());
		return false;
	}

	public boolean getMoveOld()
	{
		return this._moveOld;
	}

	public void setMoveOld(boolean var1)
	{
		this._moveOld = var1;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 200;
	}

	@Override
	public void writePortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		var2.setBoolean("moveOld", this._moveOld);
	}

	@Override
	public void readPortableData(EntityPlayer var1, NBTTagCompound var2)
	{
		this._moveOld = var2.getBoolean("moveOld");
	}

	@Override
	public void writeItemNBT(NBTTagCompound var1)
	{
		super.writeItemNBT(var1);
		if (this._moveOld)
			var1.setBoolean("moveOld", this._moveOld);

	}

	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		this._moveOld = var1.getBoolean("moveOld");
	}
}
