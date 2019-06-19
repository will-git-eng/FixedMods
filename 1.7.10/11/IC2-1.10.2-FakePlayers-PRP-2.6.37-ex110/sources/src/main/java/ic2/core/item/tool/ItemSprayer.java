package ic2.core.item.tool;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ru.will.git.ic2.ModUtils;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.BlockFoam;
import ic2.core.block.BlockIC2Fence;
import ic2.core.block.BlockScaffold;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.item.ItemIC2FluidContainer;
import ic2.core.item.armor.ItemArmorCFPack;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSprayer extends ItemIC2FluidContainer implements IBoxable
{
	public ItemSprayer()
	{
		super(ItemName.foam_sprayer, 8000);
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
	{
		subItems.add(new ItemStack(this));
		ItemStack filled = new ItemStack(this);
		this.fill(filled, new FluidStack(FluidName.construction_foam.getInstance(), Integer.MAX_VALUE), true);
		subItems.add(filled);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
	{
		if (IC2.platform.isSimulating() && IC2.keyboard.isModeSwitchKeyDown(player))
		{
			NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
			int mode = nbtData.getInteger("mode");
			mode = mode == 0 ? 1 : 0;
			nbtData.setInteger("mode", mode);
			String sMode = mode == 0 ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.single";
			IC2.platform.messagePlayer(player, "ic2.tooltip.mode", new Object[] { sMode });
		}

		return super.onItemRightClick(stack, world, player, hand);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset)
	{
		if (IC2.keyboard.isModeSwitchKeyDown(player))
			return EnumActionResult.PASS;
		else if (!IC2.platform.isSimulating())
			return EnumActionResult.SUCCESS;
		else
		{
			RayTraceResult rtResult = this.rayTrace(world, player, true);
			if (rtResult == null)
				return EnumActionResult.PASS;
			else
			{
				if (rtResult.typeOfHit == Type.BLOCK && !pos.equals(rtResult.getBlockPos()))
				{
					BlockPos fluidPos = rtResult.getBlockPos();
					if (LiquidUtil.drainBlockToContainer(world, fluidPos, stack, player))
						return EnumActionResult.SUCCESS;
				}

				int maxFoamBlocks = 0;
				FluidStack fluid = this.getFluid(stack);
				if (fluid != null && fluid.amount > 0)
					maxFoamBlocks += fluid.amount / this.getFluidPerFoam();

				ItemStack pack = player.inventory.armorInventory[2];
				if (pack != null && pack.getItem() == ItemName.cf_pack.getInstance())
				{
					fluid = ((ItemArmorCFPack) pack.getItem()).getFluid(pack);
					if (fluid != null && fluid.amount > 0)
						maxFoamBlocks += fluid.amount / this.getFluidPerFoam();
					else
						pack = null;
				}
				else
					pack = null;

				if (maxFoamBlocks == 0)
					return EnumActionResult.FAIL;
				else
				{
					maxFoamBlocks = Math.min(maxFoamBlocks, this.getMaxFoamBlocks(stack));
					ItemSprayer.Target target;
					if (canPlaceFoam(world, pos, ItemSprayer.Target.Scaffold))
						target = ItemSprayer.Target.Scaffold;
					else if (canPlaceFoam(world, pos, ItemSprayer.Target.Cable))
						target = ItemSprayer.Target.Cable;
					else
					{
						pos = pos.offset(side);
						target = ItemSprayer.Target.Any;
					}

					Vec3d viewVec = player.getLookVec();
    
					int amount = this.sprayFoam(player, world, pos, playerViewFacing.getOpposite(), target, maxFoamBlocks);

					amount = amount * this.getFluidPerFoam();
					if (amount > 0)
					{
						if (pack != null)
						{
							fluid = ((ItemArmorCFPack) pack.getItem()).drainfromCFpack(player, pack, amount);
							amount -= fluid.amount;
						}

						if (amount > 0)
							this.drain(stack, amount, true);

						return EnumActionResult.SUCCESS;
					}
					else
						return EnumActionResult.PASS;
				}
			}
		}
    
	public int sprayFoam(World world, BlockPos pos, EnumFacing excludedDir, ItemSprayer.Target target, int maxFoamBlocks)
	{
		return this.sprayFoam(ModUtils.getModFake(world), world, pos, excludedDir, target, maxFoamBlocks);
    
    
	public int sprayFoam(EntityPlayer player, World world, BlockPos pos, EnumFacing excludedDir, ItemSprayer.Target target, int maxFoamBlocks)
	{
		if (!canPlaceFoam(world, pos, target))
			return 0;
		else
		{
			Queue<BlockPos> toCheck = new ArrayDeque();
			Set<BlockPos> positions = new HashSet();
			toCheck.add(pos);

			BlockPos cPos;
			while ((cPos = (BlockPos) ((Queue) toCheck).poll()) != null && ((Set) positions).size() < maxFoamBlocks)
				if (canPlaceFoam(world, cPos, target) && positions.add(cPos))
					for (EnumFacing dir : EnumFacing.VALUES)
						if (dir != excludedDir)
							toCheck.add(cPos.offset(dir));

			toCheck.clear();
			int failedPlacements = 0;

			for (BlockPos targetPos : positions)
    
				if (ModUtils.cantBreak(player, world, targetPos))
				{
					++failedPlacements;
					continue;
    

				IBlockState state = world.getBlockState(targetPos);
				Block targetBlock = state.getBlock();
				if (targetBlock == BlockName.scaffold.getInstance())
				{
					BlockScaffold scaffold = (BlockScaffold) targetBlock;
					switch (state.getValue(scaffold.getTypeProperty()))
					{
						case wood:
						case reinforced_wood:
							scaffold.dropBlockAsItem(world, targetPos, state, 0);
							world.setBlockState(targetPos, BlockName.foam.getBlockState(BlockFoam.FoamType.normal));
							break;
						case reinforced_iron:
							StackUtil.dropAsEntity(world, targetPos, BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron));
						case iron:
							world.setBlockState(targetPos, BlockName.foam.getBlockState(BlockFoam.FoamType.reinforced));
					}
				}
				else if (targetBlock == BlockName.te.getInstance())
				{
					TileEntity te = world.getTileEntity(targetPos);
					if (te instanceof TileEntityCable && !((TileEntityCable) te).foam())
						++failedPlacements;
				}
				else if (!world.setBlockState(targetPos, BlockName.foam.getBlockState(BlockFoam.FoamType.normal)))
					++failedPlacements;
			}

			return positions.size() - failedPlacements;
		}
	}

	protected int getMaxFoamBlocks(ItemStack stack)
	{
		NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
		return nbtData.getInteger("mode") == 0 ? 10 : 1;
	}

	protected int getFluidPerFoam()
	{
		return 100;
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	@Override
	public boolean canfill(Fluid fluid)
	{
		return fluid == FluidName.construction_foam.getInstance();
	}

	private static boolean canPlaceFoam(World world, BlockPos pos, ItemSprayer.Target target)
	{
		switch (target)
		{
			case Any:
				return BlockName.foam.getInstance().canReplace(world, pos, EnumFacing.DOWN, BlockName.foam.getItemStack(BlockFoam.FoamType.normal));
			case Scaffold:
				return world.getBlockState(pos).getBlock() == BlockName.scaffold.getInstance();
			case Cable:
				if (world.getBlockState(pos).getBlock() != BlockName.te.getInstance())
					return false;

				TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileEntityCable)
					return !((TileEntityCable) te).isFoamed();
				break;
			default:
				assert false;
		}

		return false;
	}

	private static enum Target
	{
		Any,
		Scaffold,
		Cable;
	}
}
