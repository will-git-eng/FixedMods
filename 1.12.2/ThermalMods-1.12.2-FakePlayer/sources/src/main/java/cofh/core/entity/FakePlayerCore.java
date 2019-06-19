package cofh.core.entity;

import cofh.core.util.helpers.ItemHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.UUID;

public class FakePlayerCore extends FakePlayer
{
	private static GameProfile NAME = new GameProfile(UUID.fromString("5ae51d0b-e8bc-5a02-09f4-b5dbb05963da"), "[CoFH]");
	public boolean isSneaking = false;
	public ItemStack previousItem = ItemStack.EMPTY;
	public String myName = "[CoFH]";

	public FakePlayerCore(WorldServer world)
	{
		super(world, NAME);
		this.connection = new NetServerHandlerFake(FMLCommonHandler.instance().getMinecraftServerInstance(), this);
		this.addedToChunk = false;
	}


	public FakePlayerCore(WorldServer world, GameProfile profile)
	{
		super(world, profile != null && profile.isComplete() ? profile : NAME);
		this.connection = new NetServerHandlerFake(FMLCommonHandler.instance().getMinecraftServerInstance(), this);
		this.addedToChunk = false;
	}


	public static boolean isBlockBreakable(FakePlayerCore player, World worldObj, BlockPos pos)
	{
		IBlockState state = worldObj.getBlockState(pos);
		return !state.getBlock().isAir(state, worldObj, pos) && (player == null ? state.getBlockHardness(worldObj, pos) > -1.0F : state.getPlayerRelativeBlockHardness(player, worldObj, pos) > -1.0F);
	}

	public void setItemInHand(ItemStack m_item)
	{
		this.inventory.currentItem = 0;
		this.inventory.setInventorySlotContents(0, m_item);
	}

	public void setItemInHand(int slot)
	{
		this.inventory.currentItem = slot;
	}

	@Override
	public double getDistanceSq(double x, double y, double z)
	{
		return 0.0D;
	}

	@Override
	public double getDistance(double x, double y, double z)
	{
		return 0.0D;
	}

	@Override
	public boolean isSneaking()
	{
		return this.isSneaking;
	}

	@Override
	public void onUpdate()
	{
		ItemStack itemstack = this.previousItem;
		ItemStack itemstack1 = this.getHeldItem(EnumHand.MAIN_HAND);
		if (!ItemStack.areItemStacksEqual(itemstack1, itemstack))
		{
			if (!itemstack.isEmpty())
				this.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));

			if (!itemstack1.isEmpty())
				this.getAttributeMap().applyAttributeModifiers(itemstack1.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));

			this.myName = "[CoFH]" + (!itemstack1.isEmpty() ? " using " + itemstack1.getDisplayName() : "");
		}

		this.previousItem = itemstack1.isEmpty() ? ItemStack.EMPTY : itemstack1.copy();
		this.interactionManager.updateBlockRemoving();
	}

	public void tickItemInUse(ItemStack updateItem)
	{
		if (!updateItem.isEmpty() && ItemHelper.itemsEqualWithMetadata(this.previousItem, this.activeItemStack))
		{
			this.activeItemStackUseCount = ForgeEventFactory.onItemUseTick(this, this.activeItemStack, this.activeItemStackUseCount);
			if (this.activeItemStackUseCount <= 0)
				this.onItemUseFinish();
			else
			{
				this.activeItemStack.getItem().onUsingTick(this.activeItemStack, this, this.activeItemStackUseCount);
				if (this.activeItemStackUseCount <= 25 && this.activeItemStackUseCount % 4 == 0)
					this.updateItemUse(updateItem, 5);

				if (--this.activeItemStackUseCount == 0 && !this.world.isRemote)
					this.onItemUseFinish();
			}
		}
		else
			this.resetActiveHand();
	}

	@Override
	protected void updateItemUse(ItemStack stack, int par2)
	{
		if (stack.getItemUseAction() == EnumAction.DRINK)
			this.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);

		if (stack.getItemUseAction() == EnumAction.EAT)
			this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * (float) this.rand.nextInt(2), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);

	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentString(this.getName());
	}

	@Override
	public float getEyeHeight()
	{
		return this.getDefaultEyeHeight() + this.eyeHeight;
	}

	@Override
	public float getDefaultEyeHeight()
	{
		return 1.1F;
	}
}
