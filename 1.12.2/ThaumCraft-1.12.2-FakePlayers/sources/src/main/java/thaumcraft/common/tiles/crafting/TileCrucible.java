package thaumcraft.common.tiles.crafting;

import ru.will.git.thaumcraft.EventConfig;
import ru.will.git.thaumcraft.ModUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.container.InventoryFake;
import thaumcraft.common.entities.EntitySpecialItem;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.tiles.TileThaumcraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class TileCrucible extends TileThaumcraft implements ITickable, IFluidHandler, IAspectContainer
{
	public short heat = 0;
	public AspectList aspects = new AspectList();
	public final int maxTags = 500;
	int bellows = -1;
	private int delay = 0;
	private long counter = -100L;
	int prevcolor = 0;
	int prevx = 0;
	int prevy = 0;
	public FluidTank tank = new FluidTank(FluidRegistry.WATER, 0, 1000);

	@Override
	public void readSyncNBT(NBTTagCompound nbttagcompound)
	{
		this.heat = nbttagcompound.getShort("Heat");
		this.tank.readFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("Empty"))
			this.tank.setFluid(null);

		this.aspects.readFromNBT(nbttagcompound);
	}

	@Override
	public NBTTagCompound writeSyncNBT(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setShort("Heat", this.heat);
		this.tank.writeToNBT(nbttagcompound);
		this.aspects.writeToNBT(nbttagcompound);
		return nbttagcompound;
	}

	@Override
	public void update()
	{
		++this.counter;
		int prevheat = this.heat;
		if (!this.world.isRemote)
		{
			if (this.tank.getFluidAmount() > 0)
			{
				IBlockState block = this.world.getBlockState(this.getPos().down());
				if (block.getMaterial() != Material.LAVA && block.getMaterial() != Material.FIRE && !BlocksTC.nitor.containsValue(block.getBlock()) && block.getBlock() != Blocks.MAGMA)
				{
					if (this.heat > 0)
					{
						--this.heat;
						if (this.heat == 149)
						{
							this.markDirty();
							this.syncTile(false);
						}
					}
				}
				else if (this.heat < 200)
				{
					++this.heat;
					if (prevheat < 151 && this.heat >= 151)
					{
						this.markDirty();
						this.syncTile(false);
					}
				}
			}
			else if (this.heat > 0)
				--this.heat;

			if (this.aspects.visSize() > 500)
				this.spillRandom();

			if (this.counter >= 100L)
			{
				this.spillRandom();
				this.counter = 0L;
			}
		}
		else if (this.tank.getFluidAmount() > 0)
			this.drawEffects();

		if (this.world.isRemote && prevheat < 151 && this.heat >= 151)
			++this.heat;

	}

	private void drawEffects()
	{
		if (this.heat > 150)
		{
			FXDispatcher.INSTANCE.crucibleFroth((float) this.pos.getX() + 0.2F + this.world.rand.nextFloat() * 0.6F, (float) this.pos.getY() + this.getFluidHeight(), (float) this.pos.getZ() + 0.2F + this.world.rand.nextFloat() * 0.6F);
			if (this.aspects.visSize() > 500)
				for (int a = 0; a < 2; ++a)
				{
					FXDispatcher.INSTANCE.crucibleFrothDown((float) this.pos.getX(), (float) (this.pos.getY() + 1), (float) this.pos.getZ() + this.world.rand.nextFloat());
					FXDispatcher.INSTANCE.crucibleFrothDown((float) (this.pos.getX() + 1), (float) (this.pos.getY() + 1), (float) this.pos.getZ() + this.world.rand.nextFloat());
					FXDispatcher.INSTANCE.crucibleFrothDown((float) this.pos.getX() + this.world.rand.nextFloat(), (float) (this.pos.getY() + 1), (float) this.pos.getZ());
					FXDispatcher.INSTANCE.crucibleFrothDown((float) this.pos.getX() + this.world.rand.nextFloat(), (float) (this.pos.getY() + 1), (float) (this.pos.getZ() + 1));
				}
		}

		if (this.world.rand.nextInt(6) == 0 && this.aspects.size() > 0)
		{
			int color = this.aspects.getAspects()[this.world.rand.nextInt(this.aspects.size())].getColor() + -16777216;
			int x = 5 + this.world.rand.nextInt(22);
			int y = 5 + this.world.rand.nextInt(22);
			this.delay = this.world.rand.nextInt(10);
			this.prevcolor = color;
			this.prevx = x;
			this.prevy = y;
			Color c = new Color(color);
			float r = (float) c.getRed() / 255.0F;
			float g = (float) c.getGreen() / 255.0F;
			float b = (float) c.getBlue() / 255.0F;
			FXDispatcher.INSTANCE.crucibleBubble((float) this.pos.getX() + (float) x / 32.0F + 0.015625F, (float) this.pos.getY() + 0.05F + this.getFluidHeight(), (float) this.pos.getZ() + (float) y / 32.0F + 0.015625F, r, g, b);
		}

	}

	public void ejectItem(ItemStack items)
	{
		boolean first = true;

		while (true)
		{
			ItemStack spitout = items.copy();
			if (spitout.getCount() > spitout.getMaxStackSize())
				spitout.setCount(spitout.getMaxStackSize());

			items.shrink(spitout.getCount());
			EntitySpecialItem entityitem = new EntitySpecialItem(this.world, (double) ((float) this.pos.getX() + 0.5F), (double) ((float) this.pos.getY() + 0.71F), (double) ((float) this.pos.getZ() + 0.5F), spitout);
			entityitem.motionY = 0.07500000298023224D;
			entityitem.motionX = first ? 0.0D : (double) ((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.01F);
			entityitem.motionZ = first ? 0.0D : (double) ((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.01F);
			this.world.spawnEntity(entityitem);
			first = false;
			if (items.getCount() <= 0)
				break;
		}

	}

	public ItemStack attemptSmelt(ItemStack item, String username)
	{
		boolean bubble = false;
		boolean craftDone = false;
		int stacksize = item.getCount();
		EntityPlayer player = this.world.getPlayerEntityByName(username);

		for (int a = 0; a < stacksize; ++a)
		{
			CrucibleRecipe rc = ThaumcraftCraftingManager.findMatchingCrucibleRecipe(player, this.aspects, item);
			if (rc != null && this.tank.getFluidAmount() > 0)
			{
				ItemStack out = rc.getRecipeOutput().copy();
				if (player != null)
					FMLCommonHandler.instance().firePlayerCraftingEvent(player, out, new InventoryFake(item));

				this.aspects = rc.removeMatching(this.aspects);
				this.tank.drain(50, true);
				this.ejectItem(out);
				craftDone = true;
				--stacksize;
				this.counter = -250L;
			}
			else
			{
				AspectList ot = ThaumcraftCraftingManager.getObjectTags(item);
				if (ot != null && ot.size() != 0)
				{
					for (Aspect tag : ot.getAspects())
					{
						this.aspects.add(tag, ot.getAmount(tag));
					}

					bubble = true;
					--stacksize;
					this.counter = -150L;
				}
			}
		}

		if (bubble)
		{
			this.world.playSound(null, this.pos, SoundsTC.bubble, SoundCategory.BLOCKS, 0.2F, 1.0F + this.world.rand.nextFloat() * 0.4F);
			this.syncTile(false);
			this.world.addBlockEvent(this.pos, BlocksTC.crucible, 2, 1);
		}

		if (craftDone)
		{
			this.syncTile(false);
			this.world.addBlockEvent(this.pos, BlocksTC.crucible, 99, 0);
		}

		this.markDirty();
		if (stacksize <= 0)
			return null;
		item.setCount(stacksize);
		return item;
	}

	public void attemptSmelt(EntityItem entity)
	{
		
		if (!entity.isEntityAlive())
			return;
		

		ItemStack item = entity.getItem();

		
		if (item.isEmpty())
			return;
		if (EventConfig.protectCrucible && !ModUtils.canThrowerInteract(entity, this.pos))
			return;
		

		NBTTagCompound itemData = entity.getEntityData();
		String username = itemData.getString("thrower");
		ItemStack res = this.attemptSmelt(item, username);

		if (res != null && res.getCount() > 0)
		{
			item.setCount(res.getCount());
			entity.setItem(item);
		}
		else
			entity.setDead();
	}

	public float getFluidHeight()
	{
		float base = 0.3F + 0.5F * ((float) this.tank.getFluidAmount() / (float) this.tank.getCapacity());
		float out = base + (float) this.aspects.visSize() / 500.0F * (1.0F - base);
		if (out > 1.0F)
			out = 1.001F;

		if (out == 1.0F)
			out = 0.9999F;

		return out;
	}

	public void spillRandom()
	{
		if (this.aspects.size() > 0)
		{
			Aspect tag = this.aspects.getAspects()[this.world.rand.nextInt(this.aspects.getAspects().length)];
			this.aspects.remove(tag, 1);
			AuraHelper.polluteAura(this.world, this.getPos(), tag == Aspect.FLUX ? 1.0F : 0.25F, true);
		}

		this.markDirty();
		this.syncTile(false);
	}

	public void spillRemnants()
	{
		int vs = this.aspects.visSize();
		if (this.tank.getFluidAmount() > 0 || vs > 0)
		{
			this.tank.setFluid(null);
			AuraHelper.polluteAura(this.world, this.getPos(), (float) vs * 0.25F, true);
			int f = this.aspects.getAmount(Aspect.FLUX);
			if (f > 0)
				AuraHelper.polluteAura(this.world, this.getPos(), (float) f * 0.75F, false);

			this.aspects = new AspectList();
			this.world.addBlockEvent(this.pos, BlocksTC.crucible, 2, 5);
			this.markDirty();
			this.syncTile(false);
		}

	}

	@Override
	public boolean receiveClientEvent(int i, int j)
	{
		if (i == 99)
		{
			if (this.world.isRemote)
			{
				FXDispatcher.INSTANCE.drawBamf((double) this.pos.getX() + 0.5D, (double) ((float) this.pos.getY() + 1.25F), (double) this.pos.getZ() + 0.5D, true, true, EnumFacing.UP);
				this.world.playSound((double) ((float) this.pos.getX() + 0.5F), (double) ((float) this.pos.getY() + 0.5F), (double) ((float) this.pos.getZ() + 0.5F), SoundsTC.spill, SoundCategory.BLOCKS, 0.2F, 1.0F, false);
			}

			return true;
		}
		if (i == 1)
		{
			if (this.world.isRemote)
				FXDispatcher.INSTANCE.drawBamf(this.pos.up(), true, true, EnumFacing.UP);

			return true;
		}
		if (i != 2)
			return super.receiveClientEvent(i, j);
		this.world.playSound((double) ((float) this.pos.getX() + 0.5F), (double) ((float) this.pos.getY() + 0.5F), (double) ((float) this.pos.getZ() + 0.5F), SoundsTC.spill, SoundCategory.BLOCKS, 0.2F, 1.0F, false);
		if (this.world.isRemote)
			for (int q = 0; q < 10; ++q)
			{
				FXDispatcher.INSTANCE.crucibleBoil(this.pos, this, j);
			}

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB((double) this.pos.getX(), (double) this.pos.getY(), (double) this.pos.getZ(), (double) (this.pos.getX() + 1), (double) (this.pos.getY() + 1), (double) (this.pos.getZ() + 1));
	}

	@Override
	public AspectList getAspects()
	{
		return this.aspects;
	}

	@Override
	public void setAspects(AspectList aspects)
	{
	}

	@Override
	public int addToContainer(Aspect tag, int amount)
	{
		return 0;
	}

	@Override
	public boolean takeFromContainer(Aspect tag, int amount)
	{
		return false;
	}

	@Override
	public boolean takeFromContainer(AspectList ot)
	{
		return false;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect tag, int amount)
	{
		return false;
	}

	@Override
	public boolean doesContainerContain(AspectList ot)
	{
		return false;
	}

	@Override
	public int containerContains(Aspect tag)
	{
		return 0;
	}

	@Override
	public boolean doesContainerAccept(Aspect tag)
	{
		return true;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		return (T) (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? this.tank : super.getCapability(capability, facing));
	}

	@Override
	public IFluidTankProperties[] getTankProperties()
	{
		return this.tank.getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		this.markDirty();
		this.syncTile(false);
		return this.tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		FluidStack fs = this.tank.drain(resource, doDrain);
		this.markDirty();
		this.syncTile(false);
		return fs;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		FluidStack fs = this.tank.drain(maxDrain, doDrain);
		this.markDirty();
		this.syncTile(false);
		return fs;
	}
}
