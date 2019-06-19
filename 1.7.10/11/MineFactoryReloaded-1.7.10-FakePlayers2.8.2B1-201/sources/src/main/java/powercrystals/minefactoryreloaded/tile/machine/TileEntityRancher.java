package powercrystals.minefactoryreloaded.tile.machine;

import cofh.core.util.fluid.FluidTankAdv;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.IFactoryRanchable;
import powercrystals.minefactoryreloaded.api.RanchedItem;
import powercrystals.minefactoryreloaded.core.ITankContainerBucketable;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryPowered;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryPowered;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

import java.util.Iterator;
import java.util.List;

public class TileEntityRancher extends TileEntityFactoryPowered implements ITankContainerBucketable
{
	public TileEntityRancher()
	{
		super(Machine.Rancher);
		this.setManageSolids(true);
		createEntityHAM(this);
		this.setCanRotate(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiFactoryPowered(this.getContainer(var1), this);
	}

	@Override
	public ContainerFactoryPowered getContainer(InventoryPlayer var1)
	{
		return new ContainerFactoryPowered(this, var1);
	}

	@Override
	protected boolean shouldPumpLiquid()
	{
		return true;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 400;
	}

	@Override
	public boolean activateMachine()
	{
		boolean var1 = false;
		List var2 = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this._areaManager.getHarvestArea().toAxisAlignedBB());
		Iterator var3 = var2.iterator();

		while (true)
		{
			if (!var3.hasNext())
			{
				this.setIdleTicks(this.getIdleTicksMax());
				return false;
			}

			Object var4 = var3.next();
			EntityLivingBase entity = (EntityLivingBase) var4;
			if (MFRRegistry.getRanchables().containsKey(entity.getClass()))
    
				if (this.fake.cantDamage(entity))
    

				IFactoryRanchable var6 = MFRRegistry.getRanchables().get(entity.getClass());
				List<RanchedItem> var7 = var6.ranch(this.worldObj, entity, this);
				if (var7 != null)
				{
					for (RanchedItem var9 : var7)
					{
						if (var9.hasFluid())
						{
							this.fill((FluidStack) var9.getResult(), true);
							var1 = true;
						}
						else
						{
							this.doDrop((ItemStack) var9.getResult());
							var1 = true;
						}
					}

					if (var1)
						break;
				}
			}
		}

		this.setIdleTicks(20);
		return true;
	}

	@Override
	public int getSizeInventory()
	{
		return 9;
	}

	@Override
	public boolean allowBucketDrain(ItemStack var1)
	{
		return true;
	}

	@Override
	public int fill(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection var1, int var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	public FluidStack drain(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	protected FluidTankAdv[] createTanks()
	{
		return new FluidTankAdv[] { new FluidTankAdv(4000) };
	}

	@Override
	public boolean canFill(ForgeDirection var1, Fluid var2)
	{
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection var1, Fluid var2)
	{
		return true;
	}
}
