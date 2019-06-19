package powercrystals.minefactoryreloaded.tile.machine;

import cofh.lib.util.position.BlockPosition;
import ru.will.git.minefactoryreloaded.EventConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryPowered;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryPowered;
import powercrystals.minefactoryreloaded.setup.MFRConfig;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

public class TileEntityBlockPlacer extends TileEntityFactoryPowered
{
	public TileEntityBlockPlacer()
	{
		super(Machine.BlockPlacer);
		this.setManageSolids(true);
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
	public int getSizeInventory()
	{
		return 9;
	}

	@Override
	protected boolean activateMachine()
	{
		for (int slot = 0; slot < this.getSizeInventory(); ++slot)
		{
			ItemStack stack = this._inventory[slot];
			if (stack != null && stack.getItem() instanceof ItemBlock)
			{
				ItemBlock itemBlock = (ItemBlock) stack.getItem();
				Block block = itemBlock.field_150939_a;
				BlockPosition pos = BlockPosition.fromRotateableTile(this);
				pos.moveForwards(1);
				if (this.worldObj.isAirBlock(pos.x, pos.y, pos.z) && block.canPlaceBlockOnSide(this.worldObj, pos.x, pos.y, pos.z, 0))
				{
    
					if (EventConfig.inList(EventConfig.blockPlacer, block, itemMeta))
					{
						this.setIdleTicks(this.getIdleTicksMax());
						return false;
					}

					if (this.fake.cantBreak(pos.x, pos.y, pos.z))
					{
						this.setIdleTicks(this.getIdleTicksMax());
						return false;
    

					int meta = block.onBlockPlaced(this.worldObj, pos.x, pos.y, pos.z, 0, pos.x, pos.y, pos.z, itemMeta);
					if (this.worldObj.setBlock(pos.x, pos.y, pos.z, block, meta, 3))
					{
						block.onBlockPlacedBy(this.worldObj, pos.x, pos.y, pos.z, FakePlayerFactory.getMinecraft((WorldServer) this.worldObj), stack);
						block.onPostBlockPlaced(this.worldObj, pos.x, pos.y, pos.z, meta);
						if (MFRConfig.playSounds.getBoolean(true))
							this.worldObj.playSoundEffect(pos.x + 0.5D, pos.y + 0.5D, pos.z + 0.5D, block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);

						this.decrStackSize(slot, 1);
						return true;
					}
				}
			}
		}

		this.setIdleTicks(this.getIdleTicksMax());
		return false;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 20;
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3)
	{
		return var2 != null && var2.getItem() instanceof ItemBlock;
	}
}
