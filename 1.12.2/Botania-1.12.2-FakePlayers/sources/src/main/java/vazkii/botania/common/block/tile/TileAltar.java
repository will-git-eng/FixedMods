/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Jan 21, 2014, 7:51:36 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import ru.will.git.botania.EventConfig;
import ru.will.git.botania.ModUtils;
import ru.will.git.botania.util.LazyInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.item.IPetalApothecary;
import vazkii.botania.api.recipe.IFlowerComponent;
import vazkii.botania.api.recipe.RecipePetals;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.AltarVariant;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.core.handler.ModSounds;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TileAltar extends TileSimpleInventory implements IPetalApothecary, ITickable
{

	private static final Pattern SEED_PATTERN = Pattern.compile("(?:(?:(?:[A-Z-_.:]|^)seed)|(?:(?:[a-z-_.:]|^)Seed))(?:[sA-Z-_.:]|$)");
	private static final int SET_KEEP_TICKS_EVENT = 0;
	private static final int CRAFT_EFFECT_EVENT = 1;

	public static final String TAG_HAS_WATER = "hasWater";
	public static final String TAG_HAS_LAVA = "hasLava";
	public static final String TAG_IS_MOSSY = "isMossy";

	private static final String ITEM_TAG_APOTHECARY_SPAWNED = "ApothecarySpawned";

	public boolean hasWater = false;
	public boolean hasLava = false;

	public boolean isMossy = false;

	List<ItemStack> lastRecipe = null;
	int recipeKeepTicks = 0;

	public boolean collideEntityItem(EntityItem item)
	{
		ItemStack stack = item.getItem();
		if (this.world.isRemote || stack.isEmpty() || item.isDead)
			return false;

		    
		LazyInitializer<Boolean> cantInteract = new LazyInitializer<>(() -> EventConfig.protectDropPetalApothecary && !ModUtils.canThrowerInteract(item, this.pos));
		    

		if (!this.isMossy && this.world.getBlockState(this.getPos()).getValue(BotaniaStateProps.ALTAR_VARIANT) == AltarVariant.DEFAULT)
		{
			if (stack.getItem() == Item.getItemFromBlock(Blocks.VINE))
			{
				    
				if (cantInteract.get())
					return false;
				    

				this.isMossy = true;
				this.world.updateComparatorOutputLevel(this.pos, this.world.getBlockState(this.pos).getBlock());
				stack.shrink(1);

				return true;
			}
		}

		boolean hasFluidCapability = stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

		if (!this.hasWater() && !this.hasLava())
		{
			if (hasFluidCapability)
			{
				IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

				FluidStack drainWater = fluidHandler.drain(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), false);
				FluidStack drainLava = fluidHandler.drain(new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME), false);

				if (drainWater != null && drainWater.getFluid() == FluidRegistry.WATER && drainWater.amount == Fluid.BUCKET_VOLUME)
				{
					    
					if (cantInteract.get())
						return false;
					    

					this.setWater(true);
					this.world.updateComparatorOutputLevel(this.pos, this.world.getBlockState(this.pos).getBlock());
					fluidHandler.drain(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), true);
					item.setItem(fluidHandler.getContainer());
					return true;
				}
				else if (drainLava != null && drainLava.getFluid() == FluidRegistry.LAVA && drainLava.amount == Fluid.BUCKET_VOLUME)
				{
					    
					if (cantInteract.get())
						return false;
					    

					this.setLava(true);
					this.world.updateComparatorOutputLevel(this.pos, this.world.getBlockState(this.pos).getBlock());
					fluidHandler.drain(new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME), true);
					item.setItem(fluidHandler.getContainer());
					return true;
				}
			}

			return false;
		}

		if (this.hasLava())
		{
			item.setFire(100);
			return true;
		}

		if (SEED_PATTERN.matcher(stack.getTranslationKey()).find())
		{
			for (RecipePetals recipe : BotaniaAPI.petalRecipes)
			{
				if (recipe.matches(this.itemHandler))
				{
					    
					if (cantInteract.get())
						return false;
					    

					this.saveLastRecipe();

					for (int i = 0; i < this.getSizeInventory(); i++)
					{
						this.itemHandler.setStackInSlot(i, ItemStack.EMPTY);
					}

					stack.shrink(1);

					ItemStack output = recipe.getOutput().copy();
					EntityItem outputItem = new EntityItem(this.world, this.pos.getX() + 0.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5, output);
					outputItem.addTag(ITEM_TAG_APOTHECARY_SPAWNED);
					this.world.spawnEntity(outputItem);

					this.setWater(false);
					this.world.updateComparatorOutputLevel(this.pos, this.world.getBlockState(this.pos).getBlock());

					this.world.addBlockEvent(this.getPos(), this.getBlockType(), CRAFT_EFFECT_EVENT, 0);

					return true;
				}
			}
		}
		else if (!hasFluidCapability && !item.getTags().contains(ITEM_TAG_APOTHECARY_SPAWNED))
		{
			if (!this.itemHandler.getStackInSlot(this.getSizeInventory() - 1).isEmpty())
				return false;

			for (int i = 0; i < this.getSizeInventory(); i++)
			{
				if (this.itemHandler.getStackInSlot(i).isEmpty())
				{
					    
					if (cantInteract.get())
						return false;
					    

					this.itemHandler.setStackInSlot(i, stack.splitStack(1));
					this.world.playSound(null, this.pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.1F, 10F);
					return true;
				}
			}
		}

		return false;
	}

	private IFlowerComponent getFlowerComponent(ItemStack stack)
	{
		IFlowerComponent c = null;
		if (stack.getItem() instanceof IFlowerComponent)
			c = (IFlowerComponent) stack.getItem();
		else if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof IFlowerComponent)
			c = (IFlowerComponent) ((ItemBlock) stack.getItem()).getBlock();

		return c;
	}

	public void saveLastRecipe()
	{
		this.lastRecipe = new ArrayList<>();
		for (int i = 0; i < this.getSizeInventory(); i++)
		{
			ItemStack stack = this.itemHandler.getStackInSlot(i);
			if (stack.isEmpty())
				break;
			this.lastRecipe.add(stack.copy());
		}
		this.recipeKeepTicks = 400;
		this.world.addBlockEvent(this.getPos(), ModBlocks.altar, SET_KEEP_TICKS_EVENT, 400);
	}

	public void trySetLastRecipe(EntityPlayer player)
	{
		tryToSetLastRecipe(player, this.itemHandler, this.lastRecipe);
		if (!this.isEmpty())
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this.world, this.pos);
	}

	public static void tryToSetLastRecipe(EntityPlayer player, IItemHandlerModifiable inv, List<ItemStack> lastRecipe)
	{
		if (lastRecipe == null || lastRecipe.isEmpty() || player.world.isRemote)
			return;

		int index = 0;
		boolean didAny = false;
		for (ItemStack stack : lastRecipe)
		{
			if (stack.isEmpty())
				continue;

			for (int i = 0; i < player.inventory.getSizeInventory(); i++)
			{
				ItemStack pstack = player.inventory.getStackInSlot(i);
				if (!pstack.isEmpty() && pstack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, pstack))
				{
					inv.setStackInSlot(index, pstack.splitStack(1));
					didAny = true;
					index++;
					break;
				}
			}
		}

		if (didAny)
		{
			player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.1F, 10F);
			EntityPlayerMP mp = (EntityPlayerMP) player;
			mp.inventoryContainer.detectAndSendChanges();
		}
	}

	public boolean isEmpty()
	{
		for (int i = 0; i < this.getSizeInventory(); i++)
		{
			if (!this.itemHandler.getStackInSlot(i).isEmpty())
				return false;
		}

		return true;
	}

	@Override
	public void update()
	{
		if (!this.world.isRemote)
		{
			List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(this.pos.add(0, 1D / 16D * 20D, 0), this.pos.add(1, 1D / 16D * 32D, 1)));

			boolean didChange = false;
			for (EntityItem item : items)
			{
				didChange = this.collideEntityItem(item) || didChange;
			}

			if (didChange)
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this.world, this.pos);
		}
		else
		{
			for (int i = 0; i < this.getSizeInventory(); i++)
			{
				ItemStack stackAt = this.itemHandler.getStackInSlot(i);
				if (stackAt.isEmpty())
					break;

				if (Math.random() >= 0.97)
				{
					IFlowerComponent comp = this.getFlowerComponent(stackAt);
					Color color = new Color(comp == null ? 0x888888 : comp.getParticleColor(stackAt));
					float red = color.getRed() / 255F;
					float green = color.getGreen() / 255F;
					float blue = color.getBlue() / 255F;
					if (Math.random() >= 0.75F)
						this.world.playSound(null, this.pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.1F, 10F);
					Botania.proxy.sparkleFX(this.pos.getX() + 0.5 + Math.random() * 0.4 - 0.2, this.pos.getY() + 1.2, this.pos.getZ() + 0.5 + Math.random() * 0.4 - 0.2, red, green, blue, (float) Math.random(), 10);
				}
			}

			if (this.hasLava())
			{
				this.isMossy = false;
				this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.pos.getX() + 0.5 + Math.random() * 0.4 - 0.2, this.pos.getY() + 1, this.pos.getZ() + 0.5 + Math.random() * 0.4 - 0.2, 0, 0.05, 0);
				if (Math.random() > 0.9)
					this.world.spawnParticle(EnumParticleTypes.LAVA, this.pos.getX() + 0.5 + Math.random() * 0.4 - 0.2, this.pos.getY() + 1, this.pos.getZ() + 0.5 + Math.random() * 0.4 - 0.2, 0, 0.01, 0);
			}
		}

		if (this.recipeKeepTicks > 0)
			--this.recipeKeepTicks;
		else
			this.lastRecipe = null;
	}

	@Override
	public void writePacketNBT(NBTTagCompound cmp)
	{
		super.writePacketNBT(cmp);

		cmp.setBoolean(TAG_HAS_WATER, this.hasWater());
		cmp.setBoolean(TAG_HAS_LAVA, this.hasLava());
		cmp.setBoolean(TAG_IS_MOSSY, this.isMossy);
	}

	@Override
	public void readPacketNBT(NBTTagCompound cmp)
	{
		super.readPacketNBT(cmp);

		this.hasWater = cmp.getBoolean(TAG_HAS_WATER);
		this.hasLava = cmp.getBoolean(TAG_HAS_LAVA);
		this.isMossy = cmp.getBoolean(TAG_IS_MOSSY);
	}

	@Override
	public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet)
	{
		boolean lastMossy = this.isMossy;
		super.onDataPacket(manager, packet);
		if (this.isMossy != lastMossy)
			this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
	}

	@Override
	public boolean receiveClientEvent(int id, int param)
	{
		switch (id)
		{
			case SET_KEEP_TICKS_EVENT:
				this.recipeKeepTicks = param;
				return true;
			case CRAFT_EFFECT_EVENT:
				if (this.world.isRemote)
				{
					for (int i = 0; i < 25; i++)
					{
						float red = (float) Math.random();
						float green = (float) Math.random();
						float blue = (float) Math.random();
						Botania.proxy.sparkleFX(this.pos.getX() + 0.5 + Math.random() * 0.4 - 0.2, this.pos.getY() + 1, this.pos.getZ() + 0.5 + Math.random() * 0.4 - 0.2, red, green, blue, (float) Math.random(), 10);
					}
					this.world.playSound(this.pos.getX(), this.pos.getY(), this.pos.getZ(), ModSounds.altarCraft, SoundCategory.BLOCKS, 1F, 1F, false);
				}
				return true;
			default:
				return super.receiveClientEvent(id, param);
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 16;
	}

	@Override
	protected SimpleItemStackHandler createItemHandler()
	{
		return new SimpleItemStackHandler(this, false)
		{
			@Override
			protected int getStackLimit(int slot, @Nonnull ItemStack stack)
			{
				return 1;
			}
		};
	}

	@Override
	public void setWater(boolean water)
	{
		this.hasWater = water;
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this.world, this.pos);
	}

	public void setLava(boolean lava)
	{
		this.hasLava = lava;
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this.world, this.pos);
	}

	@Override
	public boolean hasWater()
	{
		return this.hasWater;
	}

	public boolean hasLava()
	{
		return this.hasLava;
	}

	@SideOnly(Side.CLIENT)
	public void renderHUD(Minecraft mc, ScaledResolution res)
	{
		int xc = res.getScaledWidth() / 2;
		int yc = res.getScaledHeight() / 2;

		float angle = -90;
		int radius = 24;
		int amt = 0;
		for (int i = 0; i < this.getSizeInventory(); i++)
		{
			if (this.itemHandler.getStackInSlot(i).isEmpty())
				break;
			amt++;
		}

		if (amt > 0)
		{
			float anglePer = 360F / amt;

			for (RecipePetals recipe : BotaniaAPI.petalRecipes)
			{
				if (recipe.matches(this.itemHandler))
				{
					GlStateManager.color(1F, 1F, 1F, 1F);
					mc.renderEngine.bindTexture(HUDHandler.manaBar);
					RenderHelper.drawTexturedModalRect(xc + radius + 9, yc - 8, 0, 0, 8, 22, 15);

					ItemStack stack = recipe.getOutput();

					net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
					mc.getRenderItem().renderItemIntoGUI(stack, xc + radius + 32, yc - 8);
					mc.getRenderItem().renderItemIntoGUI(new ItemStack(Items.WHEAT_SEEDS), xc + radius + 16, yc + 6);
					net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
					mc.fontRenderer.drawStringWithShadow("+", xc + radius + 14, yc + 10, 0xFFFFFF);
				}
			}

			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			for (int i = 0; i < amt; i++)
			{
				double xPos = xc + Math.cos(angle * Math.PI / 180D) * radius - 8;
				double yPos = yc + Math.sin(angle * Math.PI / 180D) * radius - 8;
				GlStateManager.translate(xPos, yPos, 0);
				mc.getRenderItem().renderItemIntoGUI(this.itemHandler.getStackInSlot(i), 0, 0);
				GlStateManager.translate(-xPos, -yPos, 0);

				angle += anglePer;
			}
			net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		}
		else if (this.recipeKeepTicks > 0 && this.hasWater)
		{
			String s = I18n.format("botaniamisc.altarRefill0");
			mc.fontRenderer.drawStringWithShadow(s, xc - mc.fontRenderer.getStringWidth(s) / 2, yc + 10, 0xFFFFFF);
			s = I18n.format("botaniamisc.altarRefill1");
			mc.fontRenderer.drawStringWithShadow(s, xc - mc.fontRenderer.getStringWidth(s) / 2, yc + 20, 0xFFFFFF);
		}
	}

}
