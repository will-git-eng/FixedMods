package thaumcraft.common.golems.seals;

import ru.will.git.thaumcraft.EventConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.api.golems.EnumGolemTrait;
import thaumcraft.api.golems.GolemHelper;
import thaumcraft.api.golems.IGolemAPI;
import thaumcraft.api.golems.seals.ISealConfigToggles;
import thaumcraft.api.golems.seals.ISealEntity;
import thaumcraft.api.golems.tasks.Task;
import thaumcraft.common.golems.GolemInteractionHelper;
import thaumcraft.common.golems.client.gui.SealBaseContainer;
import thaumcraft.common.golems.client.gui.SealBaseGUI;
import thaumcraft.common.golems.tasks.TaskHandler;
import thaumcraft.common.lib.utils.InventoryUtils;

import java.util.Random;

public class SealUse extends SealFiltered implements ISealConfigToggles
{
	int delay = new Random(System.nanoTime()).nextInt(49);
	int watchedTask = Integer.MIN_VALUE;
	ResourceLocation icon = new ResourceLocation("thaumcraft", "items/seals/seal_use");
	protected ISealConfigToggles.SealToggle[] props = { new SealToggle(true, "pmeta", "golem.prop.meta"), new SealToggle(true, "pnbt", "golem.prop.nbt"), new SealToggle(false, "pore", "golem.prop.ore"), new SealToggle(false, "pmod", "golem.prop.mod"), new SealToggle(false, "pleft", "golem.prop.left"), new SealToggle(false, "pempty", "golem.prop.empty"), new SealToggle(false, "pemptyhand", "golem.prop.emptyhand"), new SealToggle(false, "psneak", "golem.prop.sneak"), new SealToggle(false, "ppro", "golem.prop.provision.wl") };

	@Override
	public String getKey()
	{
		return "thaumcraft:use";
	}

	@Override
	public void tickSeal(World world, ISealEntity seal)
	{
		if (this.delay++ % 5 == 0)
		{
			Task oldTask = TaskHandler.getTask(world.provider.getDimension(), this.watchedTask);
			if (oldTask == null || oldTask.isSuspended() || oldTask.isCompleted())
			{
				if (this.getToggles()[5].value != world.isAirBlock(seal.getSealPos().pos))
					return;

				Task task = new Task(seal.getSealPos(), seal.getSealPos().pos);
				task.setPriority(seal.getPriority());
				TaskHandler.addTask(world.provider.getDimension(), task);
				this.watchedTask = task.getId();
			}

		}
	}

	@Override
	public void onTaskStarted(World world, IGolemAPI golem, Task task)
	{
	}

	public boolean mayPlace(World world, Block blockIn, BlockPos pos, EnumFacing side)
	{
		world.getBlockState(pos);
		AxisAlignedBB axisalignedbb = blockIn.getBoundingBox(blockIn.getDefaultState(), world, pos);
		return axisalignedbb == null || world.checkNoEntityCollision(axisalignedbb, null);
	}

	@Override
	public boolean onTaskCompletion(World world, IGolemAPI golem, Task task)
	{
		if (this.getToggles()[5].value == world.isAirBlock(task.getPos()))
		{
			ItemStack clickStack = golem.getCarrying().get(0);
			if (!this.filter.get(0).isEmpty())

				clickStack = InventoryUtils.findFirstMatchFromFilter(EventConfig.golemCoreUseBlackList, this.filter, this.filterSize, this.blacklist, golem.getCarrying(), new ThaumcraftInvHelper.InvFilter(!this.props[0].value, !this.props[1].value, this.props[2].value, this.props[3].value));

			if (!clickStack.isEmpty() || this.props[6].value)
			{
				ItemStack ss = ItemStack.EMPTY;
				if (!clickStack.isEmpty())
				{
					ss = clickStack.copy();
					golem.dropItem(clickStack.copy());
				}

				BlockPos var10002 = task.getPos();
				ItemStack var10004 = this.props[6].value ? ItemStack.EMPTY : ss;
				GolemInteractionHelper.golemClick(world, golem, var10002, task.getSealPos().face, var10004, this.props[7].value, !this.getToggles()[4].value);
			}
		}

		task.setSuspended(true);
		return true;
	}

	private void dropSomeItems(FakePlayer fp2, IGolemAPI golem)
	{
		for (int i = 0; i < fp2.inventory.mainInventory.size(); ++i)
		{
			if (!fp2.inventory.mainInventory.get(i).isEmpty())
			{
				if (golem.canCarry(fp2.inventory.mainInventory.get(i), true))
					fp2.inventory.mainInventory.set(i, golem.holdItem(fp2.inventory.mainInventory.get(i)));

				if (!fp2.inventory.mainInventory.get(i).isEmpty() && fp2.inventory.mainInventory.get(i).getCount() > 0)
					InventoryUtils.dropItemAtEntity(golem.getGolemWorld(), fp2.inventory.mainInventory.get(i), golem.getGolemEntity());

				fp2.inventory.mainInventory.set(i, ItemStack.EMPTY);
			}
		}

		for (int var4 = 0; var4 < fp2.inventory.armorInventory.size(); ++var4)
		{
			if (!fp2.inventory.armorInventory.get(var4).isEmpty())
			{
				if (golem.canCarry(fp2.inventory.armorInventory.get(var4), true))
					fp2.inventory.armorInventory.set(var4, golem.holdItem(fp2.inventory.armorInventory.get(var4)));

				if (!fp2.inventory.mainInventory.get(var4).isEmpty() && fp2.inventory.armorInventory.get(var4).getCount() > 0)
					InventoryUtils.dropItemAtEntity(golem.getGolemWorld(), fp2.inventory.armorInventory.get(var4), golem.getGolemEntity());

				fp2.inventory.armorInventory.set(var4, ItemStack.EMPTY);
			}
		}

	}

	@Override
	public boolean canGolemPerformTask(IGolemAPI golem, Task task)
	{
		if (!this.props[6].value)
		{
			boolean found = !InventoryUtils.findFirstMatchFromFilter(EventConfig.golemCoreUseBlackList, this.filter, this.filterSize, this.blacklist, golem.getCarrying(), new ThaumcraftInvHelper.InvFilter(!this.props[0].value, !this.props[1].value, this.props[2].value, this.props[3].value)).isEmpty();
			if (!found && this.getToggles()[8].value && !this.blacklist && this.getInv().get(0) != null)
			{
				ISealEntity se = SealHandler.getSealEntity(golem.getGolemWorld().provider.getDimension(), task.getSealPos());
				if (se != null)
				{
					ItemStack stack = this.getInv().get(0).copy();
					if (!this.props[0].value)
						stack.setItemDamage(32767);

					GolemHelper.requestProvisioning(golem.getGolemWorld(), se, stack);
				}
			}

			return found;
		}
		return true;
	}

	@Override
	public void onTaskSuspension(World world, Task task)
	{
	}

	@Override
	public boolean canPlaceAt(World world, BlockPos pos, EnumFacing side)
	{
		return true;
	}

	@Override
	public ResourceLocation getSealIcon()
	{
		return this.icon;
	}

	@Override
	public void onRemoval(World world, BlockPos pos, EnumFacing side)
	{
	}

	@Override
	public Object returnContainer(World world, EntityPlayer player, BlockPos pos, EnumFacing side, ISealEntity seal)
	{
		return new SealBaseContainer(player.inventory, world, seal);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object returnGui(World world, EntityPlayer player, BlockPos pos, EnumFacing side, ISealEntity seal)
	{
		return new SealBaseGUI(player.inventory, world, seal);
	}

	@Override
	public int[] getGuiCategories()
	{
		return new int[] { 1, 3, 0, 4 };
	}

	@Override
	public EnumGolemTrait[] getRequiredTags()
	{
		return new EnumGolemTrait[] { EnumGolemTrait.DEFT, EnumGolemTrait.SMART };
	}

	@Override
	public EnumGolemTrait[] getForbiddenTags()
	{
		return null;
	}

	@Override
	public ISealConfigToggles.SealToggle[] getToggles()
	{
		return this.props;
	}

	@Override
	public void setToggle(int indx, boolean value)
	{
		this.props[indx].setValue(value);
	}
}
