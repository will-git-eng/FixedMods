/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * <p>
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * <p>
 * File Created @ [Jan 26, 2014, 12:23:55 AM (GMT)]
 */
package vazkii.botania.common.block.tile.mana;

import ru.will.git.botania.EventConfig;
import ru.will.git.botania.ModUtils;
import ru.will.git.botania.util.LazyInitializer;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.item.IManaDissolvable;
import vazkii.botania.api.mana.*;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.recipe.RecipeManaInfusion;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.PoolVariant;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileMod;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.handler.ManaNetworkHandler;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.ItemManaTablet;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TilePool extends TileMod implements IManaPool, IKeyLocked, ISparkAttachable, IThrottledPacket, ITickable
{

	public static final Color PARTICLE_COLOR = new Color(0x00C6FF);
	public static final int MAX_MANA = 1000000;
	public static final int MAX_MANA_DILLUTED = 10000;

	private static final String TAG_MANA = "mana";
	private static final String TAG_KNOWN_MANA = "knownMana";
	private static final String TAG_OUTPUTTING = "outputting";
	private static final String TAG_COLOR = "color";
	private static final String TAG_MANA_CAP = "manaCap";
	private static final String TAG_CAN_ACCEPT = "canAccept";
	private static final String TAG_CAN_SPARE = "canSpare";
	private static final String TAG_FRAGILE = "fragile";
	private static final String TAG_INPUT_KEY = "inputKey";
	private static final String TAG_OUTPUT_KEY = "outputKey";
	private static final int CRAFT_EFFECT_EVENT = 0;
	private static final int CHARGE_EFFECT_EVENT = 1;

	private boolean outputting = false;

	public EnumDyeColor color = EnumDyeColor.WHITE;
	int mana;
	private int knownMana = -1;

	public int manaCap = -1;
	private int soundTicks = 0;
	private boolean canAccept = true;
	private boolean canSpare = true;
	public boolean fragile = false;
	boolean isDoingTransfer = false;
	int ticksDoingTransfer = 0;

	private String inputKey = "";
	private final String outputKey = "";

	private int ticks = 0;
	private boolean sendPacket = false;

	@Override
	public boolean shouldRefresh(World world, BlockPos pos,
								 @Nonnull IBlockState oldState, @Nonnull IBlockState newState)
	{
		if (oldState.getBlock() != newState.getBlock())
			return true;
		if (oldState.getBlock() != ModBlocks.pool || newState.getBlock() != ModBlocks.pool)
			return true;
		return oldState.getValue(BotaniaStateProps.POOL_VARIANT) != newState.getValue(BotaniaStateProps.POOL_VARIANT);
	}

	@Override
	public boolean isFull()
	{
		Block blockBelow = this.world.getBlockState(this.pos.down()).getBlock();
		return blockBelow != ModBlocks.manaVoid && this.getCurrentMana() >= this.manaCap;
	}

	@Override
	public void recieveMana(int mana)
	{
		int old = this.mana;
		this.mana = Math.max(0, Math.min(this.getCurrentMana() + mana, this.manaCap));
		if (old != this.mana)
		{
			this.world.updateComparatorOutputLevel(this.pos, this.world.getBlockState(this.pos).getBlock());
			this.markDispatchable();
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		ManaNetworkEvent.removePool(this);
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		ManaNetworkEvent.removePool(this);
	}

	public static int calculateComparatorLevel(int mana, int max)
	{
		int val = (int) ((double) mana / (double) max * 15.0);
		if (mana > 0)
			val = Math.max(val, 1);
		return val;
	}

	public static RecipeManaInfusion getMatchingRecipe(@Nonnull ItemStack stack, @Nonnull IBlockState state)
	{
		List<RecipeManaInfusion> matchingNonCatRecipes = new ArrayList<>();
		List<RecipeManaInfusion> matchingCatRecipes = new ArrayList<>();

		for (RecipeManaInfusion recipe : BotaniaAPI.manaInfusionRecipes)
		{
			if (recipe.matches(stack))
			{
				if (recipe.getCatalyst() == null)
					matchingNonCatRecipes.add(recipe);
				else if (recipe.getCatalyst() == state)
					matchingCatRecipes.add(recipe);
			}
		}

		// Recipes with matching catalyst take priority above recipes with no catalyst specified
		return !matchingCatRecipes.isEmpty() ? matchingCatRecipes.get(0) : !matchingNonCatRecipes.isEmpty() ? matchingNonCatRecipes.get(0) : null;
	}

	public boolean collideEntityItem(EntityItem item)
	{
		if (this.world.isRemote || item.isDead || item.getItem().isEmpty())
			return false;

		    
		LazyInitializer<Boolean> cantInteract = new LazyInitializer<>(() -> EventConfig.protectDropManaPool && !ModUtils.canThrowerInteract(item, this.pos));
		    

		ItemStack stack = item.getItem();
		if (stack.getItem() instanceof IManaDissolvable)
		{
			    
			if (cantInteract.get())
				return false;
			    

			((IManaDissolvable) stack.getItem()).onDissolveTick(this, stack, item);
		}

		if (item.age > 100 && item.age < 130)
			return false;

		RecipeManaInfusion recipe = getMatchingRecipe(stack, this.world.getBlockState(this.pos.down()));

		if (recipe != null)
		{
			int mana = recipe.getManaToConsume();
			if (this.getCurrentMana() >= mana)
			{
				    
				if (cantInteract.get())
					return false;
				    

				this.recieveMana(-mana);

				stack.shrink(1);

				ItemStack output = recipe.getOutput().copy();
				EntityItem outputItem = new EntityItem(this.world, this.pos.getX() + 0.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5, output);
				outputItem.age = 105;
				this.world.spawnEntity(outputItem);

				this.craftingFanciness();
				return true;
			}
		}

		return false;
	}

	private void craftingFanciness()
	{
		if (this.soundTicks == 0)
		{
			this.world.playSound(null, this.pos, ModSounds.manaPoolCraft, SoundCategory.BLOCKS, 0.4F, 4F);
			this.soundTicks = 6;
		}

		this.world.addBlockEvent(this.getPos(), this.getBlockType(), CRAFT_EFFECT_EVENT, 0);
	}

	@Override
	public boolean receiveClientEvent(int event, int param)
	{
		switch (event)
		{
			case CRAFT_EFFECT_EVENT:
				if (this.world.isRemote)
				{
					for (int i = 0; i < 25; i++)
					{
						float red = (float) Math.random();
						float green = (float) Math.random();
						float blue = (float) Math.random();
						Botania.proxy.sparkleFX(this.pos.getX() + 0.5 + Math.random() * 0.4 - 0.2, this.pos.getY() + 0.75, this.pos.getZ() + 0.5 + Math.random() * 0.4 - 0.2, red, green, blue, (float) Math.random(), 10);
					}
				}

				return true;
			case CHARGE_EFFECT_EVENT:
				if (this.world.isRemote)
				{
					if (ConfigHandler.chargingAnimationEnabled)
					{
						boolean outputting = param == 1;
						Vector3 itemVec = Vector3.fromBlockPos(this.pos).add(0.5, 0.5 + Math.random() * 0.3, 0.5);
						Vector3 tileVec = Vector3.fromBlockPos(this.pos).add(0.2 + Math.random() * 0.6, 0, 0.2 + Math.random() * 0.6);
						Botania.proxy.lightningFX(outputting ? tileVec : itemVec, outputting ? itemVec : tileVec, 80, this.world.rand.nextLong(), 0x4400799c, 0x4400C6FF);
					}
				}
				return true;
			default:
				return super.receiveClientEvent(event, param);
		}
	}

	@Override
	public void update()
	{
		if (this.manaCap == -1)
			this.manaCap = this.world.getBlockState(this.getPos()).getValue(BotaniaStateProps.POOL_VARIANT) == PoolVariant.DILUTED ? MAX_MANA_DILLUTED : MAX_MANA;

		if (!ManaNetworkHandler.instance.isPoolIn(this) && !this.isInvalid())
			ManaNetworkEvent.addPool(this);

		if (this.world.isRemote)
		{
			double particleChance = 1F - (double) this.getCurrentMana() / (double) this.manaCap * 0.1;
			if (Math.random() > particleChance)
				Botania.proxy.wispFX(this.pos.getX() + 0.3 + Math.random() * 0.5, this.pos.getY() + 0.6 + Math.random() * 0.25, this.pos.getZ() + Math.random(), PARTICLE_COLOR.getRed() / 255F, PARTICLE_COLOR.getGreen() / 255F, PARTICLE_COLOR.getBlue() / 255F, (float) Math.random() / 3F, (float) -Math.random() / 25F, 2F);
			return;
		}

		boolean wasDoingTransfer = this.isDoingTransfer;
		this.isDoingTransfer = false;

		if (this.soundTicks > 0)
		{
			this.soundTicks--;
		}

		if (this.sendPacket && this.ticks % 10 == 0)
		{
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
			this.sendPacket = false;
		}

		List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(this.pos, this.pos.add(1, 1, 1)));
		for (EntityItem item : items)
		{
			if (item.isDead)
				continue;

			ItemStack stack = item.getItem();
			if (!stack.isEmpty() && stack.getItem() instanceof IManaItem)
			{
				IManaItem mana = (IManaItem) stack.getItem();
				if (this.outputting ? mana.canReceiveManaFromPool(stack, this) : mana.canExportManaToPool(stack, this))
				{
					boolean didSomething = false;

					int bellowCount = 0;
					if (this.outputting)
						for (EnumFacing dir : EnumFacing.HORIZONTALS)
						{
							TileEntity tile = this.world.getTileEntity(this.pos.offset(dir));
							if (tile instanceof TileBellows && ((TileBellows) tile).getLinkedTile() == this)
								bellowCount++;
						}
					int transfRate = 1000 * (bellowCount + 1);

					if (this.outputting)
					{
						if (this.canSpare)
						{
							    
							if (EventConfig.protectDropManaPool && !ModUtils.canThrowerInteract(item, this.pos))
								continue;
							    

							if (this.getCurrentMana() > 0 && mana.getMana(stack) < mana.getMaxMana(stack))
								didSomething = true;

							int manaVal = Math.min(transfRate, Math.min(this.getCurrentMana(), mana.getMaxMana(stack) - mana.getMana(stack)));
							mana.addMana(stack, manaVal);
							this.recieveMana(-manaVal);
						}
					}
					else
					{
						if (this.canAccept)
						{
							    
							if (EventConfig.protectDropManaPool && !ModUtils.canThrowerInteract(item, this.pos))
								continue;
							    

							if (mana.getMana(stack) > 0 && !this.isFull())
								didSomething = true;

							int manaVal = Math.min(transfRate, Math.min(this.manaCap - this.getCurrentMana(), mana.getMana(stack)));
							mana.addMana(stack, -manaVal);
							this.recieveMana(manaVal);
						}
					}

					if (didSomething)
					{
						if (ConfigHandler.chargingAnimationEnabled && this.world.rand.nextInt(20) == 0)
						{
							this.world.addBlockEvent(this.getPos(), this.getBlockType(), CHARGE_EFFECT_EVENT, this.outputting ? 1 : 0);
						}
						this.isDoingTransfer = this.outputting;
					}
				}
			}
		}

		if (this.isDoingTransfer)
			this.ticksDoingTransfer++;
		else
		{
			this.ticksDoingTransfer = 0;
			if (wasDoingTransfer)
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		}

		this.ticks++;
	}

	@Override
	public void writePacketNBT(NBTTagCompound cmp)
	{
		cmp.setInteger(TAG_MANA, this.mana);
		cmp.setBoolean(TAG_OUTPUTTING, this.outputting);
		cmp.setInteger(TAG_COLOR, this.color.getMetadata());

		cmp.setInteger(TAG_MANA_CAP, this.manaCap);
		cmp.setBoolean(TAG_CAN_ACCEPT, this.canAccept);
		cmp.setBoolean(TAG_CAN_SPARE, this.canSpare);
		cmp.setBoolean(TAG_FRAGILE, this.fragile);

		cmp.setString(TAG_INPUT_KEY, this.inputKey);
		cmp.setString(TAG_OUTPUT_KEY, this.outputKey);
	}

	@Override
	public void readPacketNBT(NBTTagCompound cmp)
	{
		this.mana = cmp.getInteger(TAG_MANA);
		this.outputting = cmp.getBoolean(TAG_OUTPUTTING);
		this.color = EnumDyeColor.byMetadata(cmp.getInteger(TAG_COLOR));

		if (cmp.hasKey(TAG_MANA_CAP))
			this.manaCap = cmp.getInteger(TAG_MANA_CAP);
		if (cmp.hasKey(TAG_CAN_ACCEPT))
			this.canAccept = cmp.getBoolean(TAG_CAN_ACCEPT);
		if (cmp.hasKey(TAG_CAN_SPARE))
			this.canSpare = cmp.getBoolean(TAG_CAN_SPARE);
		this.fragile = cmp.getBoolean(TAG_FRAGILE);

		if (cmp.hasKey(TAG_INPUT_KEY))
			this.inputKey = cmp.getString(TAG_INPUT_KEY);
		if (cmp.hasKey(TAG_OUTPUT_KEY))
			this.inputKey = cmp.getString(TAG_OUTPUT_KEY);

		if (cmp.hasKey(TAG_KNOWN_MANA))
			this.knownMana = cmp.getInteger(TAG_KNOWN_MANA);
	}

	public void onWanded(EntityPlayer player, ItemStack wand)
	{
		if (player == null)
			return;

		if (player.isSneaking())
		{
			this.outputting = !this.outputting;
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this.world, this.pos);
		}

		if (!this.world.isRemote)
		{
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			this.writePacketNBT(nbttagcompound);
			nbttagcompound.setInteger(TAG_KNOWN_MANA, this.getCurrentMana());
			if (player instanceof EntityPlayerMP)
				((EntityPlayerMP) player).connection.sendPacket(new SPacketUpdateTileEntity(this.pos, -999, nbttagcompound));
		}

		this.world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.ding, SoundCategory.PLAYERS, 0.11F, 1F);
	}

	@SideOnly(Side.CLIENT)
	public void renderHUD(Minecraft mc, ScaledResolution res)
	{
		ItemStack pool = new ItemStack(ModBlocks.pool, 1, this.world.getBlockState(this.getPos()).getValue(BotaniaStateProps.POOL_VARIANT).ordinal());
		String name = I18n.format(pool.getTranslationKey().replaceAll("tile.", "tile." + LibResources.PREFIX_MOD) + ".name");
		int color = 0x4444FF;
		HUDHandler.drawSimpleManaHUD(color, this.knownMana, this.manaCap, name, res);

		int x = res.getScaledWidth() / 2 - 11;
		int y = res.getScaledHeight() / 2 + 30;

		int u = this.outputting ? 22 : 0;
		int v = 38;

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		mc.renderEngine.bindTexture(HUDHandler.manaBar);
		RenderHelper.drawTexturedModalRect(x, y, 0, u, v, 22, 15);
		GlStateManager.color(1F, 1F, 1F, 1F);

		ItemStack tablet = new ItemStack(ModItems.manaTablet);
		ItemManaTablet.setStackCreative(tablet);

		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		mc.getRenderItem().renderItemAndEffectIntoGUI(tablet, x - 20, y);
		mc.getRenderItem().renderItemAndEffectIntoGUI(pool, x + 26, y);
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();

		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
	}

	@Override
	public boolean canRecieveManaFromBursts()
	{
		return true;
	}

	@Override
	public boolean isOutputtingPower()
	{
		return this.outputting;
	}

	@Override
	public int getCurrentMana()
	{
		if (this.world != null)
		{
			IBlockState state = this.world.getBlockState(this.getPos());
			if (state.getProperties().containsKey(BotaniaStateProps.POOL_VARIANT))
				return state.getValue(BotaniaStateProps.POOL_VARIANT) == PoolVariant.CREATIVE ? MAX_MANA : this.mana;
		}

		return 0;
	}

	@Override
	public String getInputKey()
	{
		return this.inputKey;
	}

	@Override
	public String getOutputKey()
	{
		return this.outputKey;
	}

	@Override
	public boolean canAttachSpark(ItemStack stack)
	{
		return true;
	}

	@Override
	public void attachSpark(ISparkEntity entity)
	{
	}

	@Override
	public ISparkEntity getAttachedSpark()
	{
		List sparks = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.pos.up(), this.pos.up().add(1, 1, 1)), Predicates.instanceOf(ISparkEntity.class));
		if (sparks.size() == 1)
		{
			Entity e = (Entity) sparks.get(0);
			return (ISparkEntity) e;
		}

		return null;
	}

	@Override
	public boolean areIncomingTranfersDone()
	{
		return false;
	}

	@Override
	public int getAvailableSpaceForMana()
	{
		int space = Math.max(0, this.manaCap - this.getCurrentMana());
		if (space > 0)
			return space;
		if (this.world.getBlockState(this.pos.down()).getBlock() == ModBlocks.manaVoid)
			return this.manaCap;
		return 0;
	}

	@Override
	public EnumDyeColor getColor()
	{
		return this.color;
	}

	@Override
	public void setColor(EnumDyeColor color)
	{
		this.color = color;
		this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 0b1011);
	}

	@Override
	public void markDispatchable()
	{
		this.sendPacket = true;
	}
}
