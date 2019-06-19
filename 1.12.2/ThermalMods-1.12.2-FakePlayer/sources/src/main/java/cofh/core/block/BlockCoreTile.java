package cofh.core.block;

import cofh.api.block.IBlockInfo;
import cofh.api.block.IDismantleable;
import cofh.api.core.IAugmentable;
import cofh.api.core.ISecurable;
import cofh.api.item.IPlacementUtilItem;
import cofh.api.tileentity.IInventoryRetainer;
import cofh.api.tileentity.IReconfigurableFacing;
import cofh.api.tileentity.IRedstoneControl;
import cofh.api.tileentity.ITileInfo;
import cofh.core.util.CoreUtils;
import cofh.core.util.core.IInitializer;
import cofh.core.util.helpers.*;
import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyReceiver;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class BlockCoreTile extends BlockCore implements IInitializer, IBlockInfo, IDismantleable
{
	public BlockCoreTile(Material material, String modName)
	{
		super(material, modName);
		this.setSoundType(SoundType.STONE);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileCore ? ((TileCore) tile).getExtendedState(state, world, pos) : state;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileCore)
			((TileCore) tile).blockBroken();

		if ((!(tile instanceof IInventoryRetainer) || !((IInventoryRetainer) tile).retainInventory()) && tile instanceof IInventory)
		{
			IInventory inv = (IInventory) tile;

			for (int i = 0; i < inv.getSizeInventory(); ++i)
			{
				CoreUtils.dropItemStackIntoWorldWithVelocity(inv.getStackInSlot(i), world, pos);
			}
		}

		if (tile != null)
			world.removeTileEntity(pos);

	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		return willHarvest || super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack)
	{
		super.harvestBlock(world, player, pos, state, te, stack);
		world.setBlockToAir(pos);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntity tile = world.getTileEntity(pos);
		if (ServerHelper.isServerWorld(world) && tile instanceof ISecurable && SecurityHelper.isSecure(stack))
		{
			GameProfile stackOwner = SecurityHelper.getOwner(stack);
			if (!((ISecurable) tile).setOwner(stackOwner) && placer instanceof ICommandSender)
				((ISecurable) tile).setOwnerName(placer.getName());

			((ISecurable) tile).setAccess(SecurityHelper.getAccess(stack));
		}

		if (tile instanceof IRedstoneControl && RedstoneControlHelper.hasRSControl(stack))
			((IRedstoneControl) tile).setControl(RedstoneControlHelper.getControl(stack));

		if (tile instanceof IReconfigurableFacing)
		{
			IReconfigurableFacing reconfig = (IReconfigurableFacing) tile;
			EnumFacing facing = reconfig.allowYAxisFacing() ? EnumFacing.getDirectionFromEntityLiving(pos, placer) : placer.getHorizontalFacing().getOpposite();
			reconfig.setFacing(facing.ordinal(), placer.isSneaking());
		}

		if (tile instanceof TileCore)
		{
			((TileCore) tile).fake.setProfile(placer);
			

			((TileCore) tile).blockPlaced();
		}

		if (ServerHelper.isServerWorld(world))
		{
			ItemStack offhand = placer.getHeldItemOffhand();
			if (!offhand.isEmpty() && offhand.getItem() instanceof IPlacementUtilItem)
				((IPlacementUtilItem) offhand.getItem()).onBlockPlacement(offhand, world, pos, state, (EntityPlayer) placer);
		}

	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileCore)
			((TileCore) tile).onNeighborBlockChange();

	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
	{
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileCore)
			((TileCore) tile).onNeighborTileChange(neighbor);

	}

	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof ISecurable && !((ISecurable) tile).canPlayerAccess(player) ? -1.0F : super.getPlayerRelativeBlockHardness(state, player, world, pos);
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileCore && tile.hasWorld() ? ((TileCore) tile).getComparatorInputOverride() : 0;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileCore && tile.hasWorld() ? ((TileCore) tile).getLightValue() : super.getLightValue(state, world, pos);
	}

	@Override
	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type)
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param)
	{
		TileEntity tile = world.getTileEntity(pos);
		return tile != null && tile.receiveClientEvent(id, param);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		drops.addAll(this.dropDelegate(this.getItemStackTag(world, pos), world, pos, fortune));
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		Item item = Item.getItemFromBlock(this);
		if (item == Items.AIR)
			return ItemStack.EMPTY;
		int bMeta = state.getBlock().getMetaFromState(state);
		ItemStack retStack = new ItemStack(item, 1, bMeta);
		retStack.setTagCompound(this.getItemStackTag(world, pos));
		return retStack;
	}

	public NBTTagCompound getItemStackTag(IBlockAccess world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		NBTTagCompound retTag = new NBTTagCompound();
		if (tile instanceof TileNameable && !((TileNameable) tile).customName.isEmpty())
			retTag = ItemHelper.setItemStackTagName(retTag, ((TileNameable) tile).customName);

		if (tile instanceof TileAugmentableSecure)
		{
			retTag.setBoolean("Creative", ((TileAugmentableSecure) tile).isCreative);
			retTag.setByte("Level", (byte) ((TileAugmentableSecure) tile).getLevel());
			if (((TileAugmentableSecure) tile).isSecured())
				retTag = SecurityHelper.setItemStackTagSecure(retTag, (ISecurable) tile);
		}

		if (tile instanceof IAugmentable)
			retTag = AugmentHelper.setItemStackTagAugments(retTag, (IAugmentable) tile);

		if (tile instanceof IRedstoneControl)
			retTag = RedstoneControlHelper.setItemStackTagRS(retTag, (IRedstoneControl) tile);

		if (tile instanceof TileReconfigurable)
			retTag = ReconfigurableHelper.setItemStackTagReconfig(retTag, (TileReconfigurable) tile);

		if (tile instanceof IEnergyHandler)
			retTag.setInteger("Energy", ((IEnergyHandler) tile).getEnergyStored(null));

		return retTag;
	}

	public ArrayList<ItemStack> dropDelegate(NBTTagCompound nbt, IBlockAccess world, BlockPos pos, int fortune)
	{
		IBlockState state = world.getBlockState(pos);
		int meta = state.getBlock().getMetaFromState(state);
		ItemStack dropBlock = new ItemStack(this, 1, meta);
		if (nbt != null)
			dropBlock.setTagCompound(nbt);

		ArrayList<ItemStack> ret = new ArrayList<>();
		ret.add(dropBlock);
		return ret;
	}

	public ArrayList<ItemStack> dismantleDelegate(NBTTagCompound nbt, World world, BlockPos pos, EntityPlayer player, boolean returnDrops, boolean simulate)
	{
		TileEntity tile = world.getTileEntity(pos);
		IBlockState state = world.getBlockState(pos);
		int meta = state.getBlock().getMetaFromState(state);
		ArrayList<ItemStack> ret = new ArrayList<>();
		if (state.getBlock() != this)
			return ret;
		ItemStack dropBlock = new ItemStack(this, 1, meta);
		if (nbt != null)
			dropBlock.setTagCompound(nbt);

		if (!simulate)
		{
			if (tile instanceof TileCore)
				((TileCore) tile).blockDismantled();

			world.setBlockToAir(pos);
			if (!returnDrops)
			{
				float f = 0.3F;
				double x2 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
				double y2 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
				double z2 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
				EntityItem dropEntity = new EntityItem(world, (double) pos.getX() + x2, (double) pos.getY() + y2, (double) pos.getZ() + z2, dropBlock);
				dropEntity.setPickupDelay(10);
				if (tile instanceof ISecurable && !((ISecurable) tile).getAccess().isPublic())
					dropEntity.setOwner(player.getName());

				world.spawnEntity(dropEntity);
				if (player != null)
					CoreUtils.dismantleLog(player.getName(), state.getBlock(), meta, pos);
			}
		}

		ret.add(dropBlock);
		return ret;
	}

	@Override
	public void getBlockInfo(List<ITextComponent> info, IBlockAccess world, BlockPos pos, EnumFacing side, EntityPlayer player, boolean debug)
	{
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof ITileInfo)
			((ITileInfo) tile).getTileInfo(info, side, player, debug);
		else if (tile instanceof IEnergyReceiver)
		{
			IEnergyReceiver rec = (IEnergyReceiver) tile;
			if (rec.getMaxEnergyStored(side) <= 0)
				return;

			info.add(new TextComponentTranslation("info.cofh.energy").appendText(": " + StringHelper.formatNumber((long) rec.getEnergyStored(side)) + "/" + StringHelper.formatNumber((long) rec.getMaxEnergyStored(side)) + " RF"));
		}

	}

	@Override
	public ArrayList<ItemStack> dismantleBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player, boolean returnDrops)
	{
		return this.dismantleDelegate(this.getItemStackTag(world, pos), world, pos, player, returnDrops, false);
	}

	@Override
	public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof ISecurable ? ((ISecurable) tile).canPlayerAccess(player) : !(tile instanceof TileCore) || ((TileCore) tile).canPlayerDismantle(player);
	}

	@Override
	public boolean preInit()
	{
		return false;
	}

	@Override
	public boolean initialize()
	{
		return false;
	}
}
