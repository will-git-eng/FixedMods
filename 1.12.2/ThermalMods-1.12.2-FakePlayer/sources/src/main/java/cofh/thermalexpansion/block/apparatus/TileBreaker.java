package cofh.thermalexpansion.block.apparatus;

import cofh.api.tileentity.IInventoryConnection;
import cofh.core.entity.FakePlayerCore;
import cofh.core.gui.container.ContainerTileAugmentable;
import cofh.core.util.core.SideConfig;
import cofh.core.util.core.SlotConfig;
import cofh.core.util.helpers.BlockHelper;
import cofh.core.util.helpers.FluidHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.client.apparatus.GuiBreaker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Arrays;

public class TileBreaker extends TileApparatusBase implements IInventoryConnection, ITickable
{
	private static final int TYPE = BlockApparatus.Type.BREAKER.getMetadata();
	protected boolean augmentFluid;

	public static void initialize()
	{
		TileApparatusBase.SIDE_CONFIGS[TYPE] = new SideConfig();
		TileApparatusBase.SIDE_CONFIGS[TYPE].numConfig = 2;
		TileApparatusBase.SIDE_CONFIGS[TYPE].slotGroups = new int[][] { new int[0], new int[0] };
		TileApparatusBase.SIDE_CONFIGS[TYPE].sideTypes = new int[] { 0, 4 };
		TileApparatusBase.SIDE_CONFIGS[TYPE].defaultSides = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
		TileApparatusBase.SLOT_CONFIGS[TYPE] = new SlotConfig();
		TileApparatusBase.SLOT_CONFIGS[TYPE].allowInsertionSlot = new boolean[0];
		TileApparatusBase.SLOT_CONFIGS[TYPE].allowExtractionSlot = new boolean[0];
		GameRegistry.registerTileEntity(TileBreaker.class, "thermalexpansion:apparatus_breaker");
	}

	public static void config()
	{
		String category = "Apparatus.Breaker";
		BlockApparatus.enable[TYPE] = ThermalExpansion.CONFIG.get(category, "Enable", true);
	}

	public TileBreaker()
	{
		this.inventory = new ItemStack[1];
		Arrays.fill(this.inventory, ItemStack.EMPTY);
		this.radius = 0;
		this.depth = 0;
	}

	@Override
	public int getType()
	{
		return TYPE;
	}

	@Override
	protected void activate()
	{
		this.breakBlocksInArea();
	}

	private void breakBlocksInArea()
	{
		Iterable<BlockPos> area;
		switch (this.facing)
		{
			case 0:
				area = BlockPos.getAllInBox(this.pos.add(-this.radius, -1 - this.depth, -this.radius), this.pos.add(this.radius, -1, this.radius));
				break;
			case 1:
				area = BlockPos.getAllInBox(this.pos.add(-this.radius, 1, -this.radius), this.pos.add(this.radius, 1 + this.depth, this.radius));
				break;
			case 2:
				area = BlockPos.getAllInBox(this.pos.add(-this.radius, -this.radius, -1 - this.depth), this.pos.add(this.radius, this.radius, -1));
				break;
			case 3:
				area = BlockPos.getAllInBox(this.pos.add(-this.radius, -this.radius, 1), this.pos.add(this.radius, this.radius, 1 + this.depth));
				break;
			case 4:
				area = BlockPos.getAllInBox(this.pos.add(-1 - this.depth, -this.radius, -this.radius), this.pos.add(-1, this.radius, this.radius));
				break;
			default:
				area = BlockPos.getAllInBox(this.pos.add(1, -this.radius, -this.radius), this.pos.add(1 + this.depth, this.radius, this.radius));
		}

		for (BlockPos target : area)
		{
			if (this.augmentFluid)
			{
				if (FluidHelper.getFluidFromWorld(this.world, target, false) != null && this.fake.cantBreak(target))
					continue;

				FluidStack stack = this.augmentFluid ? FluidHelper.getFluidFromWorld(this.world, target, true) : null;
				if (stack != null)
				{
					for (int i = 0; i < 6 && stack.amount > 0; ++i)
					{
						if (this.sideCache[i] == 1)
							stack.amount -= FluidHelper.insertFluidIntoAdjacentFluidHandler(this, EnumFacing.VALUES[i], stack, true);
					}

					this.world.setBlockToAir(target);
					continue;
				}
			}

			if (FakePlayerCore.isBlockBreakable(this.fakePlayer, this.world, target))
			{
				if (this.fake.cantBreak(target))
					continue;

				IBlockState state = this.world.getBlockState(target);
				this.stuffedItems.addAll(BlockHelper.breakBlock(this.world, this.fakePlayer, target, state, 0, true, false));
			}
		}

	}

	@Override
	public Object getGuiClient(InventoryPlayer inventory)
	{
		return new GuiBreaker(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory)
	{
		return new ContainerTileAugmentable(inventory, this);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing from)
	{
		return super.hasCapability(capability, from) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		return (T) (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(EmptyFluidHandler.INSTANCE) : super.getCapability(capability, facing));
	}
}
