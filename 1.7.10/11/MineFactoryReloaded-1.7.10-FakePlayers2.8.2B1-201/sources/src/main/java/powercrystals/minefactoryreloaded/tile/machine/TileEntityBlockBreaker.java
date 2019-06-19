package powercrystals.minefactoryreloaded.tile.machine;

import cofh.lib.util.position.BlockPosition;
import ru.will.git.minefactoryreloaded.EventConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryPowered;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryPowered;
import powercrystals.minefactoryreloaded.setup.MFRConfig;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

import java.util.ArrayList;

public class TileEntityBlockBreaker extends TileEntityFactoryPowered
{
	protected BlockPosition bp;

	public TileEntityBlockBreaker()
	{
		super(Machine.BlockBreaker);
		this.setManageSolids(true);
		this.setCanRotate(true);
	}

	@Override
	protected void onRotate()
	{
		this.bp = BlockPosition.fromRotateableTile(this).moveForwards(1);
		super.onRotate();
	}

	@Override
	public void onNeighborBlockChange()
	{
		if (this.bp != null && !this.worldObj.isAirBlock(this.bp.x, this.bp.y, this.bp.z))
			this.setIdleTicks(0);

	}

	@Override
	public boolean activateMachine()
	{
		int x = this.bp.x;
		int y = this.bp.y;
		int z = this.bp.z;
		World world = this.worldObj;
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		if (!block.isAir(world, x, y, z) && !block.getMaterial().isLiquid() && block.getBlockHardness(world, x, y, z) >= 0.0F)
    
			if (EventConfig.inList(EventConfig.blockBreaker, block, meta))
			{
				this.setIdleTicks(this.getIdleTicksMax());
				return false;
			}

			if (this.fake.cantBreak(x, y, z))
			{
				this.setIdleTicks(this.getIdleTicksMax());
				return false;
    

			ArrayList var7 = block.getDrops(world, x, y, z, meta, 0);
			if (world.setBlockToAir(x, y, z))
			{
				this.doDrop(var7);
				if (MFRConfig.playSounds.getBoolean(true))
					world.playAuxSFXAtEntity(null, 2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
			}

			return true;
		}
		else
		{
			this.setIdleTicks(this.getIdleTicksMax());
			return false;
		}
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
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 60;
	}

	@Override
	public int getSizeInventory()
	{
		return 0;
	}
}
