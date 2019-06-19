package thaumcraft.common.tiles.crafting;

import ru.will.git.thaumcraft.EventConfig;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.IPlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.casters.IInteractWithCaster;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.api.crafting.IInfusionStabiliserExt;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.items.IGogglesDisplayExtended;
import thaumcraft.api.potions.PotionFluxTaint;
import thaumcraft.api.potions.PotionVisExhaust;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.blocks.basic.BlockPillar;
import thaumcraft.common.blocks.devices.BlockPedestal;
import thaumcraft.common.container.InventoryFake;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.events.EssentiaHandler;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockArc;
import thaumcraft.common.lib.network.fx.PacketFXInfusionSource;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.tiles.devices.TileStabilizer;

import java.text.DecimalFormat;
import java.util.*;

public class TileInfusionMatrix extends TileThaumcraft
		implements IInteractWithCaster, IAspectContainer, ITickable, IGogglesDisplayExtended
{
	private ArrayList<BlockPos> pedestals = new ArrayList<>();
	private int dangerCount = 0;
	public boolean active = false;
	public boolean crafting = false;
	public boolean checkSurroundings = true;
	public float costMult = 0.0F;
	private int cycleTime = 20;
	public int stabilityCap = 25;
	public float stability = 0.0F;
	public float stabilityReplenish = 0.0F;
	private AspectList recipeEssentia = new AspectList();
	private ArrayList<ItemStack> recipeIngredients = null;
	private Object recipeOutput = null;
	private String recipePlayer = null;
	private String recipeOutputLabel = null;
	private ItemStack recipeInput = null;
	private int recipeInstability = 0;
	private int recipeXP = 0;
	private int recipeType = 0;
	public HashMap<String, TileInfusionMatrix.SourceFX> sourceFX = new HashMap<>();
	public int count = 0;
	public int craftCount = 0;
	public float startUp;
	private int countDelay;
	ArrayList<ItemStack> ingredients;
	int itemCount;
	private ArrayList<BlockPos> problemBlocks;
	HashMap<Block, Integer> tempBlockCount;
	static DecimalFormat myFormatter = new DecimalFormat("#######.##");

	public TileInfusionMatrix()
	{
		this.countDelay = this.cycleTime / 2;
		this.ingredients = new ArrayList<>();
		this.itemCount = 0;
		this.problemBlocks = new ArrayList<>();
		this.tempBlockCount = new HashMap<>();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB((double) this.getPos().getX() - 0.1D, (double) this.getPos().getY() - 0.1D, (double) this.getPos().getZ() - 0.1D, (double) this.getPos().getX() + 1.1D, (double) this.getPos().getY() + 1.1D, (double) this.getPos().getZ() + 1.1D);
	}

	@Override
	public void readSyncNBT(NBTTagCompound nbtCompound)
	{
		this.active = nbtCompound.getBoolean("active");
		this.crafting = nbtCompound.getBoolean("crafting");
		this.stability = nbtCompound.getFloat("stability");
		this.recipeInstability = nbtCompound.getInteger("recipeinst");
		this.recipeEssentia.readFromNBT(nbtCompound);
	}

	@Override
	public NBTTagCompound writeSyncNBT(NBTTagCompound nbtCompound)
	{
		nbtCompound.setBoolean("active", this.active);
		nbtCompound.setBoolean("crafting", this.crafting);
		nbtCompound.setFloat("stability", this.stability);
		nbtCompound.setInteger("recipeinst", this.recipeInstability);
		this.recipeEssentia.writeToNBT(nbtCompound);
		return nbtCompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtCompound)
	{
		super.readFromNBT(nbtCompound);
		NBTTagList nbttaglist = nbtCompound.getTagList("recipein", 10);
		this.recipeIngredients = new ArrayList<>();

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			this.recipeIngredients.add(new ItemStack(nbttagcompound1));
		}

		String rot = nbtCompound.getString("rotype");
		if (rot != null && rot.equals("@"))
			this.recipeOutput = new ItemStack(nbtCompound.getCompoundTag("recipeout"));
		else if (rot != null)
		{
			this.recipeOutputLabel = rot;
			this.recipeOutput = nbtCompound.getTag("recipeout");
		}

		this.recipeInput = new ItemStack(nbtCompound.getCompoundTag("recipeinput"));
		this.recipeType = nbtCompound.getInteger("recipetype");
		this.recipeXP = nbtCompound.getInteger("recipexp");
		this.recipePlayer = nbtCompound.getString("recipeplayer");
		if (this.recipePlayer.isEmpty())
			this.recipePlayer = null;

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound)
	{
		super.writeToNBT(nbtCompound);
		if (this.recipeIngredients != null && this.recipeIngredients.size() > 0)
		{
			NBTTagList nbttaglist = new NBTTagList();

			for (ItemStack stack : this.recipeIngredients)
			{
				if (!stack.isEmpty())
				{
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("item", (byte) this.count);
					stack.writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
					++this.count;
				}
			}

			nbtCompound.setTag("recipein", nbttaglist);
		}

		if (this.recipeOutput != null && this.recipeOutput instanceof ItemStack)
			nbtCompound.setString("rotype", "@");

		if (this.recipeOutput != null && this.recipeOutput instanceof NBTBase)
			nbtCompound.setString("rotype", this.recipeOutputLabel);

		if (this.recipeOutput != null && this.recipeOutput instanceof ItemStack)
			nbtCompound.setTag("recipeout", ((ItemStack) this.recipeOutput).writeToNBT(new NBTTagCompound()));

		if (this.recipeOutput != null && this.recipeOutput instanceof NBTBase)
			nbtCompound.setTag("recipeout", (NBTBase) this.recipeOutput);

		if (this.recipeInput != null)
			nbtCompound.setTag("recipeinput", this.recipeInput.writeToNBT(new NBTTagCompound()));

		nbtCompound.setInteger("recipetype", this.recipeType);
		nbtCompound.setInteger("recipexp", this.recipeXP);
		if (this.recipePlayer == null)
			nbtCompound.setString("recipeplayer", "");
		else
			nbtCompound.setString("recipeplayer", this.recipePlayer);

		return nbtCompound;
	}

	private TileInfusionMatrix.EnumStability getStability()
	{
		return this.stability > (float) (this.stabilityCap / 2) ? TileInfusionMatrix.EnumStability.VERY_STABLE : this.stability >= 0.0F ? EnumStability.STABLE : this.stability > -25.0F ? EnumStability.UNSTABLE : EnumStability.VERY_UNSTABLE;
	}

	private float getModFromCurrentStability()
	{
		switch (this.getStability())
		{
			case VERY_STABLE:
				return 5.0F;
			case STABLE:
				return 6.0F;
			case UNSTABLE:
				return 7.0F;
			case VERY_UNSTABLE:
				return 8.0F;
			default:
				return 1.0F;
		}
	}

	@Override
	public void update()
	{
		++this.count;
		if (this.checkSurroundings)
		{
			this.checkSurroundings = false;
			this.getSurroundings();
		}

		if (this.world.isRemote)
			this.doEffects();
		else
		{
			if (this.count % (this.crafting ? 20 : 100) == 0 && !this.validLocation())
			{
				this.active = false;
				this.markDirty();
				this.syncTile(false);
				return;
			}

			if (this.active && !this.crafting && this.stability < (float) this.stabilityCap && this.count % Math.max(5, this.countDelay) == 0)
			{
				this.stability += Math.max(0.1F, this.stabilityReplenish);
				if (this.stability > (float) this.stabilityCap)
					this.stability = (float) this.stabilityCap;

				this.markDirty();
				this.syncTile(false);
			}

			if (this.active && this.crafting && this.count % this.countDelay == 0)
			{
				this.craftCycle();
				this.markDirty();
			}
		}

	}

	public boolean validLocation()
	{
		return this.world.getBlockState(this.pos.add(0, -2, 0)).getBlock() instanceof BlockPedestal && this.world.getBlockState(this.pos.add(1, -2, 1)).getBlock() instanceof BlockPillar && this.world.getBlockState(this.pos.add(-1, -2, 1)).getBlock() instanceof BlockPillar && this.world.getBlockState(this.pos.add(1, -2, -1)).getBlock() instanceof BlockPillar && this.world.getBlockState(this.pos.add(-1, -2, -1)).getBlock() instanceof BlockPillar;
	}

	public void craftingStart(EntityPlayer player)
	{
		if (!this.validLocation())
		{
			this.active = false;
			this.markDirty();
			this.syncTile(false);
		}
		else
		{
			this.getSurroundings();
			TileEntity te = null;
			this.recipeInput = ItemStack.EMPTY;
			te = this.world.getTileEntity(this.pos.down(2));
			if (te instanceof TilePedestal)
			{
				TilePedestal ped = (TilePedestal) te;
				if (!ped.getStackInSlot(0).isEmpty())
					this.recipeInput = ped.getStackInSlot(0).copy();
			}

			if (this.recipeInput != null && !this.recipeInput.isEmpty())
			{
				ArrayList<ItemStack> components = new ArrayList<>();

				for (BlockPos cc : this.pedestals)
				{
					te = this.world.getTileEntity(cc);
					if (te instanceof TilePedestal)
					{
						TilePedestal ped = (TilePedestal) te;
						if (!ped.getStackInSlot(0).isEmpty())
							components.add(ped.getStackInSlot(0).copy());
					}
				}

				if (components.size() != 0)
				{
					InfusionRecipe recipe = ThaumcraftCraftingManager.findMatchingInfusionRecipe(components, this.recipeInput, player);
					if ((double) this.costMult < 0.5D)
						this.costMult = 0.5F;

					if (recipe != null)
					{
						this.recipeType = 0;
						this.recipeIngredients = components;

						Object recipeOutput = recipe.getRecipeOutput(player, this.recipeInput, components);

						
						if (!EventConfig.runicMatrixBlackList.isEmpty())
						{
							ItemStack outStack = null;
							if (recipeOutput instanceof ItemStack)
								outStack = (ItemStack) recipeOutput;
							else if (recipeOutput instanceof Object[])
							{
								Object[] obj = (Object[]) recipeOutput;
								if (obj[1] instanceof ItemStack)
									outStack = (ItemStack) obj[1];
							}
							if (EventConfig.runicMatrixBlackList.contains(outStack))
								return;
						}
						

						if (recipeOutput instanceof Object[])
						{
							Object[] obj = (Object[]) recipeOutput;
							this.recipeOutputLabel = (String) obj[0];
							this.recipeOutput = obj[1];
						}
						else
							this.recipeOutput = recipeOutput;

						this.recipeInstability = recipe.getInstability(player, this.recipeInput, components);
						AspectList al = recipe.getAspects(player, this.recipeInput, components);
						AspectList al2 = new AspectList();

						for (Aspect as : al.getAspects())
						{
							if ((int) ((float) al.getAmount(as) * this.costMult) > 0)
								al2.add(as, (int) ((float) al.getAmount(as) * this.costMult));
						}

						this.recipeEssentia = al2;
						this.recipePlayer = player.getName();
						this.crafting = true;
						this.world.playSound(null, this.pos, SoundsTC.craftstart, SoundCategory.BLOCKS, 0.5F, 1.0F);
						this.syncTile(false);
						this.markDirty();
					}
				}
			}
		}
	}

	private float getLossPerCycle()
	{
		return (float) this.recipeInstability / this.getModFromCurrentStability();
	}

	public void craftCycle()
	{
		boolean valid = false;
		float ff = this.world.rand.nextFloat() * this.getLossPerCycle();
		this.stability -= ff;
		this.stability += this.stabilityReplenish;
		if (this.stability < -100.0F)
			this.stability = -100.0F;

		if (this.stability > (float) this.stabilityCap)
			this.stability = (float) this.stabilityCap;

		TileEntity te = this.world.getTileEntity(this.pos.down(2));
		if (te instanceof TilePedestal)
		{
			TilePedestal ped = (TilePedestal) te;
			if (!ped.getStackInSlot(0).isEmpty())
			{
				ItemStack i2 = ped.getStackInSlot(0).copy();
				if (this.recipeInput.getItemDamage() == 32767)
					i2.setItemDamage(32767);

				if (ThaumcraftInvHelper.areItemStacksEqualForCrafting(i2, this.recipeInput))
					valid = true;
			}
		}

		if (!valid || this.stability < 0.0F && (float) this.world.rand.nextInt(1500) <= Math.abs(this.stability))
		{
			switch (this.world.rand.nextInt(24))
			{
				case 0:
				case 1:
				case 2:
				case 3:
					this.inEvEjectItem(0);
					break;
				case 4:
				case 5:
				case 6:
					this.inEvWarp();
					break;
				case 7:
				case 8:
				case 9:
					this.inEvZap(false);
					break;
				case 10:
				case 11:
					this.inEvZap(true);
					break;
				case 12:
				case 13:
					this.inEvEjectItem(1);
					break;
				case 14:
				case 15:
					this.inEvEjectItem(2);
					break;
				case 16:
					this.inEvEjectItem(3);
					break;
				case 17:
					this.inEvEjectItem(4);
					break;
				case 18:
				case 19:
					this.inEvHarm(false);
					break;
				case 20:
				case 21:
					this.inEvEjectItem(5);
					break;
				case 22:
					this.inEvHarm(true);
					break;
				case 23:
					this.world.createExplosion(null, (double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D, 1.5F + this.world.rand.nextFloat(), false);
			}

			this.stability += 5.0F + this.world.rand.nextFloat() * 5.0F;
			this.inResAdd();
			if (valid)
				return;
		}

		if (!valid)
		{
			this.crafting = false;
			this.recipeEssentia = new AspectList();
			this.recipeInstability = 0;
			this.syncTile(false);
			this.world.playSound(null, this.pos, SoundsTC.craftfail, SoundCategory.BLOCKS, 1.0F, 0.6F);
			this.markDirty();
		}
		else if (this.recipeType == 1 && this.recipeXP > 0)
		{
			List<EntityPlayer> targets = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((double) this.getPos().getX(), (double) this.getPos().getY(), (double) this.getPos().getZ(), (double) (this.getPos().getX() + 1), (double) (this.getPos().getY() + 1), (double) (this.getPos().getZ() + 1)).grow(10.0D, 10.0D, 10.0D));
			if (targets != null && targets.size() > 0)
			{
				for (EntityPlayer target : targets)
				{
					if (target.capabilities.isCreativeMode || target.experienceLevel > 0)
					{
						if (!target.capabilities.isCreativeMode)
							target.addExperienceLevel(-1);

						--this.recipeXP;
						target.attackEntityFrom(DamageSource.MAGIC, (float) this.world.rand.nextInt(2));
						PacketHandler.INSTANCE.sendToAllAround(new PacketFXInfusionSource(this.pos, this.pos, target.getEntityId()), new TargetPoint(this.getWorld().provider.getDimension(), (double) this.pos.getX(), (double) this.pos.getY(), (double) this.pos.getZ(), 32.0D));
						target.playSound(SoundEvents.BLOCK_LAVA_EXTINGUISH, 1.0F, 2.0F + this.world.rand.nextFloat() * 0.4F);
						this.countDelay = this.cycleTime;
						return;
					}
				}

				Aspect[] ingEss = this.recipeEssentia.getAspects();
				if (ingEss != null && ingEss.length > 0 && this.world.rand.nextInt(3) == 0)
				{
					Aspect as = ingEss[this.world.rand.nextInt(ingEss.length)];
					this.recipeEssentia.add(as, 1);
					this.stability -= 0.25F;
					this.syncTile(false);
					this.markDirty();
				}
			}

		}
		else
		{
			if (this.recipeType == 1 && this.recipeXP == 0)
				this.countDelay = this.cycleTime / 2;

			if (this.countDelay < 1)
				this.countDelay = 1;

			if (this.recipeEssentia.visSize() > 0)
			{
				for (Aspect aspect : this.recipeEssentia.getAspects())
				{
					int na = this.recipeEssentia.getAmount(aspect);
					if (na > 0)
					{
						if (EssentiaHandler.drainEssentia(this, aspect, null, 12, na > 1 ? this.countDelay : 0))
						{
							this.recipeEssentia.reduce(aspect, 1);
							this.syncTile(false);
							this.markDirty();
							return;
						}

						this.stability -= 0.25F;
						this.syncTile(false);
						this.markDirty();
					}
				}

				this.checkSurroundings = true;
			}
			else if (this.recipeIngredients.size() <= 0)
			{
				this.crafting = false;
				this.craftingFinish(this.recipeOutput, this.recipeOutputLabel);
				this.recipeOutput = null;
				this.syncTile(false);
				this.markDirty();
			}
			else
				for (int a = 0; a < this.recipeIngredients.size(); ++a)
				{
					for (BlockPos cc : this.pedestals)
					{
						te = this.world.getTileEntity(cc);
						if (te instanceof TilePedestal && ((TilePedestal) te).getStackInSlot(0) != null && !((TilePedestal) te).getStackInSlot(0).isEmpty() && ThaumcraftInvHelper.areItemStacksEqualForCrafting(((TilePedestal) te).getStackInSlot(0), this.recipeIngredients.get(a)))
						{
							if (this.itemCount == 0)
							{
								this.itemCount = 5;
								PacketHandler.INSTANCE.sendToAllAround(new PacketFXInfusionSource(this.pos, cc, 0), new TargetPoint(this.getWorld().provider.getDimension(), (double) this.pos.getX(), (double) this.pos.getY(), (double) this.pos.getZ(), 32.0D));
							}
							else if (this.itemCount-- <= 1)
							{
								ItemStack is = ((TilePedestal) te).getStackInSlot(0).getItem().getContainerItem(((TilePedestal) te).getStackInSlot(0));
								((TilePedestal) te).setInventorySlotContents(0, is != null && !is.isEmpty() ? is.copy() : ItemStack.EMPTY);
								te.markDirty();
								((TilePedestal) te).syncTile(false);
								this.recipeIngredients.remove(a);
								this.markDirty();
							}

							return;
						}
					}

					Aspect[] ingEss = this.recipeEssentia.getAspects();
					if (ingEss != null && ingEss.length > 0 && this.world.rand.nextInt(1 + a) == 0)
					{
						Aspect as = ingEss[this.world.rand.nextInt(ingEss.length)];
						this.recipeEssentia.add(as, 1);
						this.stability -= 0.25F;
						this.syncTile(false);
						this.markDirty();
					}
				}
		}
	}

	private void inEvZap(boolean all)
	{
		List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB((double) this.getPos().getX(), (double) this.getPos().getY(), (double) this.getPos().getZ(), (double) (this.getPos().getX() + 1), (double) (this.getPos().getY() + 1), (double) (this.getPos().getZ() + 1)).grow(10.0D, 10.0D, 10.0D));
		if (targets != null && targets.size() > 0)
			for (EntityLivingBase target : targets)
			{
				PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockArc(this.pos, target, 0.3F - this.world.rand.nextFloat() * 0.1F, 0.0F, 0.3F - this.world.rand.nextFloat() * 0.1F), new TargetPoint(this.world.provider.getDimension(), (double) this.pos.getX(), (double) this.pos.getY(), (double) this.pos.getZ(), 32.0D));
				target.attackEntityFrom(DamageSource.MAGIC, (float) (4 + this.world.rand.nextInt(4)));
				if (!all)
					break;
			}

	}

	private void inEvHarm(boolean all)
	{
		List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB((double) this.getPos().getX(), (double) this.getPos().getY(), (double) this.getPos().getZ(), (double) (this.getPos().getX() + 1), (double) (this.getPos().getY() + 1), (double) (this.getPos().getZ() + 1)).grow(10.0D, 10.0D, 10.0D));
		if (targets != null && targets.size() > 0)
			for (EntityLivingBase target : targets)
			{
				
				if (EventConfig.potionFluxTaint && this.world.rand.nextBoolean())
					target.addPotionEffect(new PotionEffect(PotionFluxTaint.instance, 120, 0, false, true));
					
				else if (EventConfig.potionVisExhaust)
				{
					PotionEffect pe = new PotionEffect(PotionVisExhaust.instance, 2400, 0, true, true);
					pe.getCurativeItems().clear();
					target.addPotionEffect(pe);
				}

				if (!all)
					break;
			}

	}

	private void inResAdd()
	{
		List<EntityPlayer> targets = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((double) this.getPos().getX(), (double) this.getPos().getY(), (double) this.getPos().getZ(), (double) (this.getPos().getX() + 1), (double) (this.getPos().getY() + 1), (double) (this.getPos().getZ() + 1)).grow(10.0D));
		if (targets != null && targets.size() > 0)
			for (EntityPlayer player : targets)
			{
				IPlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
				if (!knowledge.isResearchKnown("!INSTABILITY"))
				{
					knowledge.addResearch("!INSTABILITY");
					knowledge.sync((EntityPlayerMP) player);
					player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_PURPLE + I18n.translateToLocal("got.instability")), true);
				}
			}

	}

	private void inEvWarp()
	{
		List<EntityPlayer> targets = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((double) this.getPos().getX(), (double) this.getPos().getY(), (double) this.getPos().getZ(), (double) (this.getPos().getX() + 1), (double) (this.getPos().getY() + 1), (double) (this.getPos().getZ() + 1)).grow(10.0D));
		if (targets != null && targets.size() > 0)
		{
			EntityPlayer target = targets.get(this.world.rand.nextInt(targets.size()));
			if (this.world.rand.nextFloat() < 0.25F)
				ThaumcraftApi.internalMethods.addWarpToPlayer(target, 1, IPlayerWarp.EnumWarpType.NORMAL);
			else
				ThaumcraftApi.internalMethods.addWarpToPlayer(target, 2 + this.world.rand.nextInt(4), IPlayerWarp.EnumWarpType.TEMPORARY);
		}

	}

	private void inEvEjectItem(int type)
	{
		for (int retries = 0; retries < 25 && this.pedestals.size() > 0; ++retries)
		{
			BlockPos cc = this.pedestals.get(this.world.rand.nextInt(this.pedestals.size()));
			TileEntity te = this.world.getTileEntity(cc);
			if (te instanceof TilePedestal && ((TilePedestal) te).getStackInSlot(0) != null && !((TilePedestal) te).getStackInSlot(0).isEmpty())
			{
				BlockPos stabPos = ((TilePedestal) te).findInstabilityMitigator();
				if (stabPos != null)
				{
					TileEntity ste = this.world.getTileEntity(stabPos);
					if (ste instanceof TileStabilizer)
					{
						TileStabilizer tste = (TileStabilizer) ste;
						if (tste.mitigate(MathHelper.getInt(this.world.rand, 5, 10)))
						{
							this.world.addBlockEvent(cc, this.world.getBlockState(cc).getBlock(), 5, 0);
							PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockArc(this.pos, cc.up(), 0.3F - this.world.rand.nextFloat() * 0.1F, 0.0F, 0.3F - this.world.rand.nextFloat() * 0.1F), new TargetPoint(this.world.provider.getDimension(), (double) cc.getX(), (double) cc.getY(), (double) cc.getZ(), 32.0D));
							PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockArc(cc.up(), stabPos, 0.3F - this.world.rand.nextFloat() * 0.1F, 0.0F, 0.3F - this.world.rand.nextFloat() * 0.1F), new TargetPoint(this.world.provider.getDimension(), (double) stabPos.getX(), (double) stabPos.getY(), (double) stabPos.getZ(), 32.0D));
							return;
						}
					}
				}

				if (type > 3 && type != 5)
					((TilePedestal) te).setInventorySlotContents(0, ItemStack.EMPTY);
				else
					InventoryUtils.dropItems(this.world, cc);

				te.markDirty();
				((TilePedestal) te).syncTile(false);
				if (type != 1 && type != 3)
					if (type != 2 && type != 4)
					{
						if (type == 5)
							this.world.createExplosion(null, (double) ((float) cc.getX() + 0.5F), (double) ((float) cc.getY() + 0.5F), (double) ((float) cc.getZ() + 0.5F), 1.0F, false);
					}
					else
					{
						int a = 5 + this.world.rand.nextInt(5);
						AuraHelper.polluteAura(this.world, cc, (float) a, true);
					}
				else
				{
					this.world.setBlockState(cc.up(), BlocksTC.fluxGoo.getDefaultState());
					this.world.playSound(null, cc, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 0.3F, 1.0F);
				}

				this.world.addBlockEvent(cc, this.world.getBlockState(cc).getBlock(), 11, 0);
				PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockArc(this.pos, cc.up(), 0.3F - this.world.rand.nextFloat() * 0.1F, 0.0F, 0.3F - this.world.rand.nextFloat() * 0.1F), new TargetPoint(this.world.provider.getDimension(), (double) cc.getX(), (double) cc.getY(), (double) cc.getZ(), 32.0D));
				return;
			}
		}

	}

	public void craftingFinish(Object out, String label)
	{
		TileEntity te = this.world.getTileEntity(this.pos.down(2));
		if (te instanceof TilePedestal)
		{
			float dmg = 1.0F;
			if (out instanceof ItemStack)
			{
				ItemStack qs = ((ItemStack) out).copy();
				if (((TilePedestal) te).getStackInSlot(0).isItemStackDamageable() && ((TilePedestal) te).getStackInSlot(0).isItemDamaged())
				{
					dmg = (float) ((TilePedestal) te).getStackInSlot(0).getItemDamage() / (float) ((TilePedestal) te).getStackInSlot(0).getMaxDamage();
					if (qs.isItemStackDamageable() && !qs.isItemDamaged())
						qs.setItemDamage((int) ((float) qs.getMaxDamage() * dmg));
				}

				((TilePedestal) te).setInventorySlotContentsFromInfusion(0, qs);
			}
			else if (out instanceof NBTBase)
			{
				ItemStack temp = ((TilePedestal) te).getStackInSlot(0);
				NBTBase tag = (NBTBase) out;
				temp.setTagInfo(label, tag);
				this.syncTile(false);
				te.markDirty();
			}
			else if (out instanceof Enchantment)
			{
				ItemStack temp = ((TilePedestal) te).getStackInSlot(0);
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(temp);
				enchantments.put((Enchantment) out, EnchantmentHelper.getEnchantmentLevel((Enchantment) out, temp) + 1);
				EnchantmentHelper.setEnchantments(enchantments, temp);
				this.syncTile(false);
				te.markDirty();
			}

			if (this.recipePlayer != null)
			{
				EntityPlayer p = this.world.getPlayerEntityByName(this.recipePlayer);
				if (p != null)
					FMLCommonHandler.instance().firePlayerCraftingEvent(p, ((TilePedestal) te).getStackInSlot(0), new InventoryFake(this.recipeIngredients));
			}

			this.recipeEssentia = new AspectList();
			this.recipeInstability = 0;
			this.syncTile(false);
			this.markDirty();
			this.world.addBlockEvent(this.pos.down(2), this.world.getBlockState(this.pos.down(2)).getBlock(), 12, 0);
			this.world.playSound(null, this.pos, SoundsTC.wand, SoundCategory.BLOCKS, 0.5F, 1.0F);
		}

	}

	private void getSurroundings()
	{
		Set<Long> stuff = new HashSet<>();
		this.pedestals.clear();
		this.tempBlockCount.clear();
		this.problemBlocks.clear();
		this.cycleTime = 10;
		this.stabilityReplenish = 0.0F;
		this.costMult = 1.0F;

		try
		{
			for (int xx = -8; xx <= 8; ++xx)
			{
				for (int zz = -8; zz <= 8; ++zz)
				{
					boolean skip = false;

					for (int yy = -3; yy <= 7; ++yy)
					{
						if (xx != 0 || zz != 0)
						{
							int x = this.pos.getX() + xx;
							int y = this.pos.getY() - yy;
							int z = this.pos.getZ() + zz;
							BlockPos bp = new BlockPos(x, y, z);
							Block bi = this.world.getBlockState(bp).getBlock();
							if (bi instanceof BlockPedestal)
								this.pedestals.add(bp);

							try
							{
								if (bi == Blocks.SKULL || bi instanceof IInfusionStabiliser && ((IInfusionStabiliser) bi).canStabaliseInfusion(this.getWorld(), bp))
									stuff.add(bp.toLong());
							}
							catch (Exception ignored)
							{
							}
						}
					}
				}
			}

			long lp;
			for (; !stuff.isEmpty(); stuff.remove(lp))
			{
				Long[] posArray = stuff.toArray(new Long[0]);
				if (posArray == null || posArray[0] == null)
					break;

				lp = posArray[0];

				try
				{
					BlockPos c1 = BlockPos.fromLong(lp);
					int x1 = this.pos.getX() - c1.getX();
					int z1 = this.pos.getZ() - c1.getZ();
					int x2 = this.pos.getX() + x1;
					int z2 = this.pos.getZ() + z1;
					BlockPos c2 = new BlockPos(x2, c1.getY(), z2);
					Block sb1 = this.world.getBlockState(c1).getBlock();
					Block sb2 = this.world.getBlockState(c2).getBlock();
					float amt1 = 0.1F;
					float amt2 = 0.1F;
					if (sb1 instanceof IInfusionStabiliserExt)
						amt1 = ((IInfusionStabiliserExt) sb1).getStabilizationAmount(this.getWorld(), c1);

					if (sb2 instanceof IInfusionStabiliserExt)
						amt2 = ((IInfusionStabiliserExt) sb2).getStabilizationAmount(this.getWorld(), c2);

					if (sb1 == sb2 && amt1 == amt2)
						if (sb1 instanceof IInfusionStabiliserExt && ((IInfusionStabiliserExt) sb1).hasSymmetryPenalty(this.getWorld(), c1, c2))
						{
							this.stabilityReplenish -= ((IInfusionStabiliserExt) sb1).getSymmetryPenalty(this.getWorld(), c1);
							this.problemBlocks.add(c1);
						}
						else
							this.stabilityReplenish += this.calcDeminishingReturns(sb1, amt1);
					else
					{
						this.stabilityReplenish -= Math.max(amt1, amt2);
						this.problemBlocks.add(c1);
					}

					stuff.remove(c2.toLong());
				}
				catch (Exception ignored)
				{
				}
			}

			if (this.world.getBlockState(this.pos.add(-1, -2, -1)).getBlock() instanceof BlockPillar && this.world.getBlockState(this.pos.add(1, -2, -1)).getBlock() instanceof BlockPillar && this.world.getBlockState(this.pos.add(1, -2, 1)).getBlock() instanceof BlockPillar && this.world.getBlockState(this.pos.add(-1, -2, 1)).getBlock() instanceof BlockPillar)
			{
				if (this.world.getBlockState(this.pos.add(-1, -2, -1)).getBlock() == BlocksTC.pillarAncient && this.world.getBlockState(this.pos.add(1, -2, -1)).getBlock() == BlocksTC.pillarAncient && this.world.getBlockState(this.pos.add(1, -2, 1)).getBlock() == BlocksTC.pillarAncient && this.world.getBlockState(this.pos.add(-1, -2, 1)).getBlock() == BlocksTC.pillarAncient)
				{
					--this.cycleTime;
					this.costMult -= 0.1F;
					this.stabilityReplenish -= 0.1F;
				}

				if (this.world.getBlockState(this.pos.add(-1, -2, -1)).getBlock() == BlocksTC.pillarEldritch && this.world.getBlockState(this.pos.add(1, -2, -1)).getBlock() == BlocksTC.pillarEldritch && this.world.getBlockState(this.pos.add(1, -2, 1)).getBlock() == BlocksTC.pillarEldritch && this.world.getBlockState(this.pos.add(-1, -2, 1)).getBlock() == BlocksTC.pillarEldritch)
				{
					this.cycleTime -= 3;
					this.costMult += 0.05F;
					this.stabilityReplenish += 0.2F;
				}
			}

			int[] xm = { -1, 1, 1, -1 };
			int[] zm = { -1, -1, 1, 1 };

			for (int a = 0; a < 4; ++a)
			{
				Block b = this.world.getBlockState(this.pos.add(xm[a], -3, zm[a])).getBlock();
				if (b == BlocksTC.matrixSpeed)
				{
					--this.cycleTime;
					this.costMult += 0.01F;
				}

				if (b == BlocksTC.matrixCost)
				{
					++this.cycleTime;
					this.costMult -= 0.02F;
				}
			}

			this.countDelay = this.cycleTime / 2;
			int apc = 0;

			for (BlockPos cc : this.pedestals)
			{
				boolean items = false;
				int x = this.pos.getX() - cc.getX();
				int z = this.pos.getZ() - cc.getZ();
				Block bb = this.world.getBlockState(cc).getBlock();
				if (bb == BlocksTC.pedestalEldritch)
					this.costMult += 0.0025F;

				if (bb == BlocksTC.pedestalAncient)
					this.costMult -= 0.01F;
			}
		}
		catch (Exception ignored)
		{
		}
	}

	private float calcDeminishingReturns(Block b, float base)
	{
		float bb = base;
		int c = this.tempBlockCount.getOrDefault(b, 0);
		if (c > 0)
			bb = (float) ((double) base * Math.pow(0.75D, (double) c));
		this.tempBlockCount.put(b, c + 1);
		return bb;
	}

	@Override
	public boolean onCasterRightClick(World world, ItemStack wandstack, EntityPlayer player, BlockPos pos, EnumFacing side, EnumHand hand)
	{
		if (world.isRemote && this.active && !this.crafting)
			this.checkSurroundings = true;

		if (!world.isRemote && this.active && !this.crafting)
		{
			this.craftingStart(player);
			return false;
		}
		else if (!world.isRemote && !this.active && this.validLocation())
		{
			world.playSound(null, pos, SoundsTC.craftstart, SoundCategory.BLOCKS, 0.5F, 1.0F);
			this.active = true;
			this.syncTile(false);
			this.markDirty();
			return false;
		}
		else
			return false;
	}

	private void doEffects()
	{
		if (this.crafting)
		{
			if (this.craftCount == 0)
				this.world.playSound((double) this.pos.getX(), (double) this.pos.getY(), (double) this.pos.getZ(), SoundsTC.infuserstart, SoundCategory.BLOCKS, 0.5F, 1.0F, false);
			else if (this.craftCount == 0 || this.craftCount % 65 == 0)
				this.world.playSound((double) this.pos.getX(), (double) this.pos.getY(), (double) this.pos.getZ(), SoundsTC.infuser, SoundCategory.BLOCKS, 0.5F, 1.0F, false);

			++this.craftCount;
			FXDispatcher.INSTANCE.blockRunes((double) this.pos.getX(), (double) (this.pos.getY() - 2), (double) this.pos.getZ(), 0.5F + this.world.rand.nextFloat() * 0.2F, 0.1F, 0.7F + this.world.rand.nextFloat() * 0.3F, 25, -0.03F);
		}
		else if (this.craftCount > 0)
		{
			this.craftCount -= 2;
			if (this.craftCount < 0)
				this.craftCount = 0;

			if (this.craftCount > 50)
				this.craftCount = 50;
		}

		if (this.active && this.startUp != 1.0F)
		{
			if (this.startUp < 1.0F)
				this.startUp += Math.max(this.startUp / 10.0F, 0.001F);

			if ((double) this.startUp > 0.999D)
				this.startUp = 1.0F;
		}

		if (!this.active && this.startUp > 0.0F)
		{
			if (this.startUp > 0.0F)
				this.startUp -= this.startUp / 10.0F;

			if ((double) this.startUp < 0.001D)
				this.startUp = 0.0F;
		}

		for (String fxk : this.sourceFX.keySet().toArray(new String[0]))
		{
			TileInfusionMatrix.SourceFX fx = this.sourceFX.get(fxk);
			if (fx.ticks <= 0)
				this.sourceFX.remove(fxk);
			else
			{
				if (fx.loc.equals(this.pos))
				{
					Entity player = this.world.getEntityByID(fx.color);
					if (player != null)
						for (int a = 0; a < 4; ++a)
						{
							FXDispatcher.INSTANCE.drawInfusionParticles4(player.posX + (double) ((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * player.width), player.getEntityBoundingBox().minY + (double) (this.world.rand.nextFloat() * player.height), player.posZ + (double) ((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * player.width), this.pos.getX(), this.pos.getY(), this.pos.getZ());
						}
				}
				else
				{
					TileEntity tile = this.world.getTileEntity(fx.loc);
					if (tile instanceof TilePedestal)
					{
						ItemStack is = ((TilePedestal) tile).getSyncedStackInSlot(0);
						if (is != null && !is.isEmpty())
							if (this.world.rand.nextInt(3) == 0)
								FXDispatcher.INSTANCE.drawInfusionParticles3((double) ((float) fx.loc.getX() + this.world.rand.nextFloat()), (double) ((float) fx.loc.getY() + this.world.rand.nextFloat() + 1.0F), (double) ((float) fx.loc.getZ() + this.world.rand.nextFloat()), this.pos.getX(), this.pos.getY(), this.pos.getZ());
							else
							{
								Item bi = is.getItem();
								if (bi instanceof ItemBlock)
									for (int a = 0; a < 4; ++a)
									{
										FXDispatcher.INSTANCE.drawInfusionParticles2((double) ((float) fx.loc.getX() + this.world.rand.nextFloat()), (double) ((float) fx.loc.getY() + this.world.rand.nextFloat() + 1.0F), (double) ((float) fx.loc.getZ() + this.world.rand.nextFloat()), this.pos, Block.getBlockFromItem(bi).getDefaultState(), is.getItemDamage());
									}
								else
									for (int a = 0; a < 4; ++a)
									{
										FXDispatcher.INSTANCE.drawInfusionParticles1((double) ((float) fx.loc.getX() + 0.4F + this.world.rand.nextFloat() * 0.2F), (double) ((float) fx.loc.getY() + 1.23F + this.world.rand.nextFloat() * 0.2F), (double) ((float) fx.loc.getZ() + 0.4F + this.world.rand.nextFloat() * 0.2F), this.pos, is);
									}
							}
					}
					else
						fx.ticks = 0;
				}

				--fx.ticks;
				this.sourceFX.put(fxk, fx);
			}
		}

		if (this.crafting && this.stability < 0.0F && (float) this.world.rand.nextInt(250) <= Math.abs(this.stability))
			FXDispatcher.INSTANCE.spark((double) ((float) this.getPos().getX() + this.world.rand.nextFloat()), (double) ((float) this.getPos().getY() + this.world.rand.nextFloat()), (double) ((float) this.getPos().getZ() + this.world.rand.nextFloat()), 3.0F + this.world.rand.nextFloat() * 2.0F, 0.7F + this.world.rand.nextFloat() * 0.1F, 0.1F, 0.65F + this.world.rand.nextFloat() * 0.1F, 0.8F);

		if (this.active && !this.problemBlocks.isEmpty() && this.world.rand.nextInt(25) == 0)
		{
			BlockPos p = this.problemBlocks.get(this.world.rand.nextInt(this.problemBlocks.size()));
			FXDispatcher.INSTANCE.spark((double) ((float) p.getX() + this.world.rand.nextFloat()), (double) ((float) p.getY() + this.world.rand.nextFloat()), (double) ((float) p.getZ() + this.world.rand.nextFloat()), 2.0F + this.world.rand.nextFloat(), 0.7F + this.world.rand.nextFloat() * 0.1F, 0.1F, 0.65F + this.world.rand.nextFloat() * 0.1F, 0.8F);
		}

	}

	@Override
	public AspectList getAspects()
	{
		return this.recipeEssentia;
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
	public boolean canRenderBreaking()
	{
		return true;
	}

	@Override
	public String[] getIGogglesText()
	{
		float lpc = this.getLossPerCycle();
		return lpc != 0.0F ? new String[] { TextFormatting.BOLD + I18n.translateToLocal("stability." + this.getStability().name()), TextFormatting.GOLD + "" + TextFormatting.ITALIC + myFormatter.format((double) this.stabilityReplenish) + " " + I18n.translateToLocal("stability.gain"), TextFormatting.RED + "" + I18n.translateToLocal("stability.range") + TextFormatting.ITALIC + myFormatter.format((double) lpc) + " " + I18n.translateToLocal("stability.loss") } : new String[] { TextFormatting.BOLD + I18n.translateToLocal("stability." + this.getStability().name()), TextFormatting.GOLD + "" + TextFormatting.ITALIC + myFormatter.format((double) this.stabilityReplenish) + " " + I18n.translateToLocal("stability.gain") };
	}

	private enum EnumStability
	{
		VERY_STABLE,
		STABLE,
		UNSTABLE,
		VERY_UNSTABLE
	}

	public class SourceFX
	{
		public BlockPos loc;
		public int ticks;
		public int color;
		public int entity;

		public SourceFX(BlockPos loc, int ticks, int color)
		{
			this.loc = loc;
			this.ticks = ticks;
			this.color = color;
		}
	}
}
