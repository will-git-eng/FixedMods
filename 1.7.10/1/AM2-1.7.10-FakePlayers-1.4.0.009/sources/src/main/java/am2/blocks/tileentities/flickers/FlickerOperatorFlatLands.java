package am2.blocks.tileentities.flickers;

import ru.will.git.am2.ModUtils;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerWorld;

import am2.api.flickers.IFlickerController;
import am2.api.flickers.IFlickerFunctionality;
import am2.api.spell.enums.Affinity;
import am2.blocks.BlocksCommonProxy;
import am2.items.ItemEssence;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.utility.DummyEntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class FlickerOperatorFlatLands implements IFlickerFunctionality
{
	@Override
	public boolean RequiresPower()
	{
		return false;
	}

	@Override
	public int PowerPerOperation()
	{
		return 10;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController habitat, boolean powered)
	{
		int searchesPerLoop = 12;
		int radius = 6;
		int diameter = radius * 2 + 1;
		if (world.isRemote)
			return false;
		else
		{
			boolean actionPerformed = false;

			TileEntity tile = (TileEntity) habitat;

			    
			FakePlayerContainer fake = new FakePlayerContainerWorld(ModUtils.profile, world);
			if (tile instanceof TileEntityFlickerControllerBase)
				fake.setParent(((TileEntityFlickerControllerBase) tile).fake);
			    

			for (int i = 0; i < searchesPerLoop && !actionPerformed; ++i)
			{
				int x = tile.xCoord - radius + world.rand.nextInt(diameter);
				int z = tile.zCoord - radius + world.rand.nextInt(diameter);
				int y = tile.yCoord + world.rand.nextInt(radius);
				if (x == tile.xCoord && y == tile.yCoord && z == tile.zCoord)
					return false;

				Block block = world.getBlock(x, y, z);
				int meta = world.getBlockMetadata(x, y, z);
				if (block != null && !world.isAirBlock(x, y, z) && block.isOpaqueCube() && block != BlocksCommonProxy.invisibleUtility)
				{
					    
					if (fake.cantBreak(x, y, z))
						continue;
					    

					if (ForgeEventFactory.doPlayerHarvestCheck(new DummyEntityPlayer(world), block, true) && block.removedByPlayer(world, new DummyEntityPlayer(world), x, y, z))
					{
						block.onBlockDestroyedByPlayer(world, x, y, z, meta);
						block.dropBlockAsItem(world, x, y, z, meta, 0);
						if (!world.isRemote)
							world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (world.getBlockMetadata(x, y, z) << 12));

						world.func_147478_e(x, y, z, true);
						actionPerformed = true;
					}
				}
			}

			return actionPerformed;
		}
	}

	@Override
	public boolean DoOperation(World worldObj, IFlickerController habitat, boolean powered, Affinity[] flickers)
	{
		return this.DoOperation(worldObj, habitat, powered);
	}

	@Override
	public void RemoveOperator(World worldObj, IFlickerController habitat, boolean powered)
	{
	}

	@Override
	public int TimeBetweenOperation(boolean powered, Affinity[] flickers)
	{
		return powered ? 1 : 20;
	}

	@Override
	public void RemoveOperator(World worldObj, IFlickerController habitat, boolean powered, Affinity[] flickers)
	{
	}

	@Override
	public Object[] getRecipe()
	{
		Object[] var10000 = new Object[] { "S P", "ENI", " R ", Character.valueOf('S'), Items.iron_shovel, Character.valueOf('P'), Items.iron_pickaxe, Character.valueOf('E'), new ItemStack(ItemsCommonProxy.flickerJar, 1, Affinity.EARTH.ordinal()), Character.valueOf('N'), null, null, null, null, null };
		ItemEssence var10007 = ItemsCommonProxy.essence;
		var10000[10] = new ItemStack(ItemsCommonProxy.essence, 1, 1);
		var10000[11] = Character.valueOf('I');
		var10000[12] = new ItemStack(ItemsCommonProxy.flickerJar, 1, Affinity.ICE.ordinal());
		var10000[13] = Character.valueOf('R');
		ItemRune var1 = ItemsCommonProxy.rune;
		var10000[14] = new ItemStack(ItemsCommonProxy.rune, 1, 0);
		return var10000;
	}
}
