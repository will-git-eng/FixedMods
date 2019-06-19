package ic2.core.block;

import ru.will.git.eventhelper.util.FastUtils;
import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.item.block.ItemBlockIC2;
import ic2.core.model.ModelUtil;
import ic2.core.ref.BlockName;
import ic2.core.ref.IBlockModelProvider;
import ic2.core.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public abstract class BlockBase extends Block implements IBlockModelProvider
{
	protected BlockBase(BlockName name, Material material)
	{
		this(name, material, ItemBlockIC2.supplier);
	}

	protected BlockBase(BlockName name, Material material, Class<? extends ItemBlock> itemClass)
	{
		this(name, material, createItemBlockSupplier(itemClass));
	}

	protected BlockBase(BlockName name, Material material, Function<Block, Item> itemSupplier)
	{
		super(material);
		this.setCreativeTab(IC2.tabIC2);
		if (name != null)
		{
			this.register(name.name(), IC2.getIdentifier(name.name()), itemSupplier);
			name.setInstance(this);
		}

	}

	protected void register(String name, ResourceLocation identifier, Function<Block, Item> itemSupplier)
	{
		this.setUnlocalizedName(name);
		BlocksItems.registerBlock(this, identifier);
		if (itemSupplier != null)
			BlocksItems.registerItem(itemSupplier.apply(this), identifier);

	}

	protected static Function<Block, Item> createItemBlockSupplier(final Class<? extends ItemBlock> cls)
	{
		if (cls == null)
			throw new NullPointerException("null item class");
		return input -> {
			try
			{
				return (Item) cls.getConstructor(new Class[] { Block.class }).newInstance(new Object[] { input });
			}
			catch (Exception var3)
			{
				throw new RuntimeException(var3);
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(BlockName name)
	{
		registerDefaultItemModel(this);
	}

	@SideOnly(Side.CLIENT)
	public static void registerDefaultItemModel(Block block)
	{
		registerItemModels(block, Collections.singletonList(block.getDefaultState()));
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemModels(Block block, Iterable<IBlockState> states)
	{
		registerItemModels(block, states, null);
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemModels(Block block, Iterable<IBlockState> states, IStateMapper mapper)
	{
		Item item = Item.getItemFromBlock(block);
		if (item != null && item != Items.AIR)
		{
			ResourceLocation loc = Util.getName(item);
			if (loc != null)
			{
				Map<IBlockState, ModelResourceLocation> locations = mapper != null ? mapper.putStateModelLocations(block) : null;

				for (IBlockState state : states)
				{
					int meta = block.getMetaFromState(state);
					ModelResourceLocation location = locations != null ? locations.get(state) : ModelUtil.getModelLocation(loc, state);
					if (location == null)
						throw new RuntimeException("can\'t map state " + state);

					ModelLoader.setCustomModelResourceLocation(item, meta, location);
				}

			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static void registerDefaultVanillaItemModel(Block block, String path)
	{
		Item item = Item.getItemFromBlock(block);
		if (item != null && item != Items.AIR)
		{
			ResourceLocation loc = Util.getName(item);
			if (loc != null)
			{
				if (path != null && !path.isEmpty())
					path = path + '/' + loc.toString();
				else
					path = loc.toString();

				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(path, null));
			}
		}
	}

	@Override
	public String getUnlocalizedName()
	{
		return "ic2." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public String getLocalizedName()
	{
		return Localization.translate(this.getUnlocalizedName());
	}

	@Override
	public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return false;
	}

	public EnumRarity getRarity(ItemStack stack)
	{
		return EnumRarity.COMMON;
	}

	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		FastUtils.setProfile(worldIn, pos, placer, TileEntityBlock.class, tile -> tile.fake);
	}
	
}
