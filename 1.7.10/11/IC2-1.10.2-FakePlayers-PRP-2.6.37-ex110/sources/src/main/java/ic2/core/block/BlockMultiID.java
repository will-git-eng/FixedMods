package ic2.core.block;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import ic2.core.block.state.EnumProperty;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.type.IExtBlockType;
import ic2.core.item.block.ItemBlockMulti;
import ic2.core.ref.BlockName;
import ic2.core.ref.IMultiBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMultiID<T extends Enum<T> & IIdProvider> extends BlockBase implements IMultiBlock<T>
{
	private static final ThreadLocal<EnumProperty<? extends Enum<?>>> currentTypeProperty = new ThreadLocal<EnumProperty<? extends Enum<?>>>()
	{
		@Override
		protected EnumProperty<? extends Enum<?>> initialValue()
		{
			throw new UnsupportedOperationException();
		}
	};
	protected final EnumProperty<T> typeProperty;

	public static <T extends Enum<T> & IIdProvider> BlockMultiID<T> create(BlockName name, Material material, Class<T> typeClass)
	{
		EnumProperty<T> typeProperty = createTypeProperty(typeClass);
		currentTypeProperty.set(typeProperty);
		BlockMultiID<T> ret = new BlockMultiID(name, material);
		currentTypeProperty.remove();
		return ret;
	}

	private static <T extends Enum<T> & IIdProvider> EnumProperty<T> createTypeProperty(Class<T> typeClass)
	{
		EnumProperty<T> ret = new EnumProperty("type", typeClass);
		if (ret.getAllowedValues().size() > 16)
			throw new IllegalArgumentException("Too many values to fit in 16 meta values for " + typeClass);
		else
			return ret;
	}

	protected static <T extends Enum<T> & IIdProvider, U extends BlockMultiID<T>> U create(Class<U> blockClass, Class<T> typeClass, Object... ctorArgs)
	{
		EnumProperty<T> typeProperty = createTypeProperty(typeClass);
		Constructor<U> ctor = null;

		label29: for (Constructor<?> cCtor : blockClass.getDeclaredConstructors())
		{
			Class<?>[] parameterTypes = cCtor.getParameterTypes();
			if (parameterTypes.length == ctorArgs.length)
			{
				for (int i = 0; i < parameterTypes.length; ++i)
				{
					Class<?> type = parameterTypes[i];
					Object arg = ctorArgs[i];
					if (arg == null && type.isPrimitive() || arg != null && !parameterTypes[i].isInstance(arg))
						continue label29;
				}

				if (ctor != null)
					throw new IllegalArgumentException("ambiguous constructor");

				ctor = (Constructor<U>) cCtor;
			}
		}

		if (ctor == null)
			throw new IllegalArgumentException("no matching constructor");
		else
		{
			currentTypeProperty.set(typeProperty);

			U ret;
			try
			{
				ctor.setAccessible(true);
				ret = ctor.newInstance(ctorArgs);
			}
			catch (Exception var16)
			{
				throw new RuntimeException(var16);
			}
			finally
			{
				currentTypeProperty.remove();
			}

			return ret;
		}
	}

	protected BlockMultiID(BlockName name, Material material)
	{
		this(name, material, ItemBlockMulti.class);
	}

	protected BlockMultiID(BlockName name, Material material, Class<? extends ItemBlock> itemClass)
	{
		super(name, material, itemClass);
		this.typeProperty = this.getTypeProperty();
		this.setDefaultState(this.blockState.getBaseState().withProperty(this.typeProperty, this.typeProperty.getDefault()));
    
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity tile;
		if (placer instanceof EntityPlayer && (tile = worldIn.getTileEntity(pos)) instanceof TileEntityBlock)
			((TileEntityBlock) tile).fake.setProfile(((EntityPlayer) placer).getGameProfile());
    

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels(BlockName name)
	{
		registerItemModels(this, this.getTypeStates());
	}

	protected final List<IBlockState> getTypeStates()
	{
		List<IBlockState> ret = new ArrayList(this.typeProperty.getAllowedValues().size());

		for (T type : this.typeProperty.getAllowedValues())
			ret.add(this.getDefaultState().withProperty(this.typeProperty, type));

		return ret;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { this.getTypeProperty() });
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		EnumProperty<T> typeProperty = this.getTypeProperty();
		return this.getDefaultState().withProperty(typeProperty, typeProperty.getValueOrDefault(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return ((IIdProvider) (Enum) state.getValue(this.getTypeProperty())).getId();
	}

	protected T getType(IBlockAccess world, BlockPos pos)
	{
		return this.getType(world.getBlockState(pos));
	}

	protected final T getType(IBlockState state)
	{
		return (T) (state.getBlock() != this ? null : (Enum) state.getValue(this.typeProperty));
	}

	@Override
	public IBlockState getState(T type)
	{
		if (type == null)
			throw new IllegalArgumentException("invalid type: " + type);
		else
			return this.getDefaultState().withProperty(this.typeProperty, type);
	}

	@Override
	public ItemStack getItemStack(T type)
	{
		return this.getItemStack(this.getState(type));
	}

	@Override
	public ItemStack getItemStack(String variant)
	{
		if (variant == null)
			throw new IllegalArgumentException("invalid type: " + variant);
		else
		{
			T type = this.typeProperty.getValue(variant);
			if (type == null)
				throw new IllegalArgumentException("invalid variant " + variant + " for " + this);
			else
				return this.getItemStack(type);
		}
	}

	@Override
	public String getVariant(ItemStack stack)
	{
		if (stack == null)
			throw new NullPointerException("null stack");
		else
		{
			Item item = Item.getItemFromBlock(this);
			if (stack.getItem() != item)
				throw new IllegalArgumentException("The stack " + stack + " doesn\'t match " + item + " (" + this + ")");
			else
			{
				IBlockState state = this.getStateFromMeta(stack.getMetadata());
				T type = this.getType(state);
				return ((IIdProvider) type).getName();
			}
		}
	}

	public ItemStack getItemStack(IBlockState state)
	{
		if (state.getBlock() != this)
			return null;
		else
		{
			Item item = Item.getItemFromBlock(this);
			if (item == null)
				throw new RuntimeException("no matching item for " + this);
			else
			{
				int meta = this.getMetaFromState(state);
				return new ItemStack(item, 1, meta);
			}
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		ItemStack stack = this.getItemStack(state);
		if (stack == null)
			return new ArrayList();
		else
		{
			List<ItemStack> ret = new ArrayList();
			ret.add(stack);
			return ret;
		}
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tabs, List<ItemStack> itemList)
	{
		for (T type : this.typeProperty.getAllowedValues())
			itemList.add(this.getItemStack(type));

	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state)
	{
		return this.getItemStack(state);
	}

	public final EnumProperty<T> getTypeProperty()
	{
		EnumProperty<T> ret;
		if (this.typeProperty != null)
			ret = this.typeProperty;
		else
		{
			ret = (EnumProperty) currentTypeProperty.get();
			if (ret == null)
				throw new IllegalStateException("The type property can\'t be obtained.");
		}

		return ret;
	}

	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos)
	{
		if (IExtBlockType.class.isAssignableFrom(this.typeProperty.getValueClass()))
		{
			T type = this.getType(state);
			if (type != null)
				return ((IExtBlockType) type).getHardness();
		}

		return super.getBlockHardness(state, world, pos);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
	{
		if (IExtBlockType.class.isAssignableFrom(this.typeProperty.getValueClass()))
		{
			T type = this.getType(world, pos);
			if (type != null)
				return ((IExtBlockType) type).getExplosionResistance();
		}

		return super.getExplosionResistance(world, pos, exploder, explosion);
	}
}
