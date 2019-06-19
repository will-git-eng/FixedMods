package thaumcraft.common.items.wands;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.thaumcraft.EventConfig;
import ru.will.git.thaumcraft.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.BlockCoordinates;
import thaumcraft.api.IArchitect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.*;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileOwned;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ItemWandCasting extends Item implements IArchitect
{
	private IIcon icon;
	DecimalFormat myFormatter = new DecimalFormat("#######.##");
	public ItemFocusBasic.WandFocusAnimation animation = null;

	public ItemWandCasting()
	{
		this.maxStackSize = 1;
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@Override
	public boolean isDamageable()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.icon = par1IconRegister.registerIcon("thaumcraft:blank");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass)
	{
		return this.icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}

	public int getMaxVis(ItemStack stack)
	{
		return this.getRod(stack).getCapacity() * (this.isSceptre(stack) ? 150 : 100);
	}

	@Override
	public EnumRarity getRarity(ItemStack itemstack)
	{
		return EnumRarity.uncommon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		ItemStack w1 = new ItemStack(this, 1, 0);
		ItemStack w2 = new ItemStack(this, 1, 9);
		ItemStack w3 = new ItemStack(this, 1, 54);
		((ItemWandCasting) w2.getItem()).setCap(w2, ConfigItems.WAND_CAP_GOLD);
		((ItemWandCasting) w3.getItem()).setCap(w3, ConfigItems.WAND_CAP_THAUMIUM);
		((ItemWandCasting) w2.getItem()).setRod(w2, ConfigItems.WAND_ROD_GREATWOOD);
		((ItemWandCasting) w3.getItem()).setRod(w3, ConfigItems.WAND_ROD_SILVERWOOD);
		ItemStack sceptre = new ItemStack(ConfigItems.itemWandCasting, 1, 128);
		((ItemWandCasting) sceptre.getItem()).setCap(sceptre, ConfigItems.WAND_CAP_THAUMIUM);
		((ItemWandCasting) sceptre.getItem()).setRod(sceptre, ConfigItems.WAND_ROD_SILVERWOOD);
		sceptre.setTagInfo("sceptre", new NBTTagByte((byte) 1));

		for (Aspect aspect : Aspect.getPrimalAspects())
		{
			((ItemWandCasting) w1.getItem()).addVis(w1, aspect, ((ItemWandCasting) w1.getItem()).getMaxVis(w1), true);
			((ItemWandCasting) w2.getItem()).addVis(w2, aspect, ((ItemWandCasting) w2.getItem()).getMaxVis(w2), true);
			((ItemWandCasting) w3.getItem()).addVis(w3, aspect, ((ItemWandCasting) w3.getItem()).getMaxVis(w3), true);
			((ItemWandCasting) sceptre.getItem()).addVis(sceptre, aspect, ((ItemWandCasting) sceptre.getItem()).getMaxVis(sceptre), true);
		}

		par3List.add(w1);
		par3List.add(w2);
		par3List.add(w3);
		par3List.add(sceptre);
	}

	@Override
	public String getItemStackDisplayName(ItemStack is)
	{
		String name = StatCollector.translateToLocal("item.Wand.name");
		name = name.replace("%CAP", StatCollector.translateToLocal("item.Wand." + this.getCap(is).getTag() + ".cap"));
		String rod = this.getRod(is).getTag();
		if (rod.contains("_staff"))
			rod = rod.substring(0, this.getRod(is).getTag().indexOf("_staff"));

		name = name.replace("%ROD", StatCollector.translateToLocal("item.Wand." + rod + ".rod"));
		name = name.replace("%OBJ", this.isStaff(is) ? StatCollector.translateToLocal("item.Wand.staff.obj") : this.isSceptre(is) ? StatCollector.translateToLocal("item.Wand.sceptre.obj") : StatCollector.translateToLocal("item.Wand.wand.obj"));
		return name;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		int pos = list.size();
		String tt2 = "";
		if (stack.hasTagCompound())
		{
			StringBuilder tt = new StringBuilder();
			int tot = 0;
			int num = 0;

			for (Aspect aspect : Aspect.getPrimalAspects())
			{
				if (stack.stackTagCompound.hasKey(aspect.getTag()))
				{
					String amount = this.myFormatter.format(stack.stackTagCompound.getInteger(aspect.getTag()) / 100.0F);
					float mod = this.getConsumptionModifier(stack, player, aspect, false);
					String consumption = this.myFormatter.format(mod * 100.0F);
					++num;
					tot = (int) (tot + mod * 100.0F);
					String text = "";
					ItemStack focus = this.getFocusItem(stack);
					if (focus != null)
					{
						int amt = ((ItemFocusBasic) focus.getItem()).getVisCost(focus).getAmount(aspect);
						if (amt > 0)
							text = "§r, " + this.myFormatter.format(amt * mod / 100.0F) + " " + StatCollector.translateToLocal(((ItemFocusBasic) focus.getItem()).isVisCostPerTick(focus) ? "item.Focus.cost2" : "item.Focus.cost1");
					}

					if (Thaumcraft.proxy.isShiftKeyDown())
						list.add(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + amount + ", §o(" + consumption + "% " + StatCollector.translateToLocal("tc.vis.cost") + ")" + text);
					else
					{
						if (tt.length() > 0)
							tt.append(" | ");

						tt.append('§').append(aspect.getChatcolor()).append(amount).append("§r");
					}
				}
			}

			if (!Thaumcraft.proxy.isShiftKeyDown() && num > 0)
			{
				list.add(tt.toString());
				tot /= num;
				tt2 = " (" + tot + "% " + StatCollector.translateToLocal("tc.vis.costavg") + ")";
			}
		}

		list.add(pos, EnumChatFormatting.GOLD + StatCollector.translateToLocal("item.capacity.text") + " " + this.getMaxVis(stack) / 100 + "§r" + tt2);
		if (this.getFocus(stack) != null)
		{
			list.add(EnumChatFormatting.BOLD.toString() + EnumChatFormatting.ITALIC + EnumChatFormatting.GREEN + this.getFocus(stack).getItemStackDisplayName(this.getFocusItem(stack)));
			if (Thaumcraft.proxy.isShiftKeyDown())
				this.getFocus(stack).addFocusInformation(this.getFocusItem(stack), player, list, par4);
		}

	}

	public AspectList getAllVis(ItemStack is)
	{
		AspectList out = new AspectList();

		for (Aspect aspect : Aspect.getPrimalAspects())
		{
			if (is.hasTagCompound() && is.stackTagCompound.hasKey(aspect.getTag()))
				out.merge(aspect, is.stackTagCompound.getInteger(aspect.getTag()));
			else
				out.merge(aspect, 0);
		}

		return out;
	}

	public AspectList getAspectsWithRoom(ItemStack wandstack)
	{
		AspectList out = new AspectList();
		AspectList cur = this.getAllVis(wandstack);
		Aspect[] arr$ = cur.getAspects();
		int len$ = arr$.length;

		for (Aspect aspect : arr$)
		{
			if (cur.getAmount(aspect) < this.getMaxVis(wandstack))
				out.add(aspect, 1);
		}

		return out;
	}

	public void storeAllVis(ItemStack is, AspectList in)
	{
		Aspect[] arr$ = in.getAspects();
		int len$ = arr$.length;

		for (Aspect aspect : arr$)
		{
			is.setTagInfo(aspect.getTag(), new NBTTagInt(in.getAmount(aspect)));
		}

	}

	public int getVis(ItemStack is, Aspect aspect)
	{
		int out = 0;
		if (is != null && aspect != null && is.hasTagCompound() && is.stackTagCompound.hasKey(aspect.getTag()))
			out = is.stackTagCompound.getInteger(aspect.getTag());

		return out;
	}

	public void storeVis(ItemStack is, Aspect aspect, int amount)
	{
		is.setTagInfo(aspect.getTag(), new NBTTagInt(amount));
	}

	public float getConsumptionModifier(ItemStack is, EntityPlayer player, Aspect aspect, boolean crafting)
	{
		float consumptionModifier = 1.0F;
		if (this.getCap(is).getSpecialCostModifierAspects() != null && this.getCap(is).getSpecialCostModifierAspects().contains(aspect))
			consumptionModifier = this.getCap(is).getSpecialCostModifier();
		else
			consumptionModifier = this.getCap(is).getBaseCostModifier();

		if (player != null)
			consumptionModifier -= WandManager.getTotalVisDiscount(player, aspect);

		if (this.getFocus(is) != null && !crafting)
			consumptionModifier -= this.getFocusFrugal(is) / 10.0F;

		if (this.isSceptre(is))
			consumptionModifier -= 0.1F;

		return Math.max(consumptionModifier, 0.1F);
	}

	public int getFocusPotency(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.potency) + (this.hasRunes(itemstack) ? 1 : 0);
	}

	public int getFocusTreasure(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.treasure);
	}

	public int getFocusFrugal(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.frugal);
	}

	public int getFocusEnlarge(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.enlarge);
	}

	public int getFocusExtend(ItemStack itemstack)
	{
		return this.getFocus(itemstack) == null ? 0 : this.getFocus(itemstack).getUpgradeLevel(this.getFocusItem(itemstack), FocusUpgradeType.extend);
	}

	public boolean consumeVis(ItemStack is, EntityPlayer player, Aspect aspect, int amount, boolean crafting)
	{
		amount = (int) (amount * this.getConsumptionModifier(is, player, aspect, crafting));
		if (this.getVis(is, aspect) >= amount)
		{
			this.storeVis(is, aspect, this.getVis(is, aspect) - amount);
			return true;
		}
		return false;
	}

	public boolean consumeAllVisCrafting(ItemStack is, EntityPlayer player, AspectList aspects, boolean doit)
	{
		if (aspects != null && aspects.size() != 0)
		{
			AspectList nl = new AspectList();
			Aspect[] arr$ = aspects.getAspects();
			int len$ = arr$.length;

			for (Aspect aspect : arr$)
			{
				int cost = aspects.getAmount(aspect) * 100;
				nl.add(aspect, cost);
			}

			return this.consumeAllVis(is, player, nl, doit, true);
		}
		return false;
	}

	public boolean consumeAllVis(ItemStack is, EntityPlayer player, AspectList aspects, boolean doit, boolean crafting)
	{
		if (aspects != null && aspects.size() != 0)
		{
			AspectList nl = new AspectList();
			Aspect[] arr$ = aspects.getAspects();
			int len$ = arr$.length;

			int i$;
			Aspect aspect;
			for (i$ = 0; i$ < len$; ++i$)
			{
				aspect = arr$[i$];
				int cost = aspects.getAmount(aspect);
				cost = (int) (cost * this.getConsumptionModifier(is, player, aspect, crafting));
				nl.add(aspect, cost);
			}

			arr$ = nl.getAspects();
			len$ = arr$.length;

			for (i$ = 0; i$ < len$; ++i$)
			{
				aspect = arr$[i$];
				if (this.getVis(is, aspect) < nl.getAmount(aspect))
					return false;
			}

			if (doit && !player.worldObj.isRemote)
			{
				arr$ = nl.getAspects();
				len$ = arr$.length;

				for (i$ = 0; i$ < len$; ++i$)
				{
					aspect = arr$[i$];
					this.storeVis(is, aspect, this.getVis(is, aspect) - nl.getAmount(aspect));
				}
			}

			return true;
		}
		return false;
	}

	public int addVis(ItemStack is, Aspect aspect, int amount, boolean doit)
	{
		if (!aspect.isPrimal())
			return 0;
		int storeAmount = this.getVis(is, aspect) + amount * 100;
		int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
		if (doit)
			this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));

		return leftover / 100;
	}

	public int addRealVis(ItemStack is, Aspect aspect, int amount, boolean doit)
	{
		if (!aspect.isPrimal())
			return 0;
		int storeAmount = this.getVis(is, aspect) + amount;
		int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
		if (doit)
			this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));

		return leftover;
	}

	@Override
	public void onUpdate(ItemStack is, World w, Entity e, int slot, boolean currentItem)
	{
		if (!w.isRemote)
		{
			EntityPlayer player = (EntityPlayer) e;
			if (this.getRod(is).getOnUpdate() != null)
				this.getRod(is).getOnUpdate().onUpdate(is, player);
		}
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    
		if (EventUtils.cantBreak(player, x, y, z))
    

		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		boolean result = false;
		ForgeDirection direction = ForgeDirection.getOrientation(side);
		if (block instanceof IWandable)
		{
			int tile = ((IWandable) block).onWandRightClick(world, stack, player, x, y, z, side, meta);
			if (tile >= 0)
				return tile == 1;
		}

		TileEntity tile1 = world.getTileEntity(x, y, z);
		if (tile1 instanceof IWandable)
		{
			int ret = ((IWandable) tile1).onWandRightClick(world, stack, player, x, y, z, side, meta);
			if (ret >= 0)
				return ret == 1;
		}

		if (WandTriggerRegistry.hasTrigger(block, meta))
			return WandTriggerRegistry.performTrigger(world, stack, player, x, y, z, side, block, meta);
		if ((block == ConfigBlocks.blockWoodenDevice && meta == 2 || block == ConfigBlocks.blockCosmeticOpaque && meta == 2) && (!Config.wardedStone || tile1 instanceof TileOwned && player.getCommandSenderName().equals(((TileOwned) tile1).owner)))
			if (!world.isRemote)
			{
				((TileOwned) tile1).safeToRemove = true;
				world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, new ItemStack(block, 1, meta)));
				world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
				world.setBlockToAir(x, y, z);
			}
			else
				player.swingItem();

		if (block == ConfigBlocks.blockArcaneDoor && (!Config.wardedStone || tile1 instanceof TileOwned && player.getCommandSenderName().equals(((TileOwned) tile1).owner)))
			if (!world.isRemote)
			{
				((TileOwned) tile1).safeToRemove = true;
				if ((meta & 8) == 0)
					tile1 = world.getTileEntity(x, y + 1, z);
				else
					tile1 = world.getTileEntity(x, y - 1, z);

				if (tile1 instanceof TileOwned)
					((TileOwned) tile1).safeToRemove = true;

				if (Config.wardedStone || !Config.wardedStone && (meta & 8) == 0)
					world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, new ItemStack(ConfigItems.itemArcaneDoor)));

				world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
				world.setBlockToAir(x, y, z);
			}
			else
				player.swingItem();

		return result;
	}

	public ItemFocusBasic getFocus(ItemStack stack)
	{
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("focus"))
		{
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("focus");
    
			if (focusStack == null || !(focusStack.getItem() instanceof ItemFocusBasic))
			{
				stack.stackTagCompound.removeTag("focus");
				return null;
    

			return (ItemFocusBasic) focusStack.getItem();
		}
		return null;
	}

	public ItemStack getFocusItem(ItemStack stack)
	{
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("focus"))
		{
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("focus");
    
			if (focusStack == null || !(focusStack.getItem() instanceof ItemFocusBasic))
			{
				stack.stackTagCompound.removeTag("focus");
				return null;
    

			return focusStack;
		}
		return null;
	}

	public void setFocus(ItemStack stack, ItemStack focus)
	{
		if (focus == null)
			stack.stackTagCompound.removeTag("focus");
		else
			stack.setTagInfo("focus", focus.writeToNBT(new NBTTagCompound()));
	}

	public WandRod getRod(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.stackTagCompound.hasKey("rod") ? WandRod.rods.get(stack.stackTagCompound.getString("rod")) : ConfigItems.WAND_ROD_WOOD;
	}

	public boolean isStaff(ItemStack stack)
	{
		WandRod rod = this.getRod(stack);
		return rod instanceof StaffRod;
	}

	public boolean isSceptre(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.stackTagCompound.hasKey("sceptre");
	}

	public boolean hasRunes(ItemStack stack)
	{
		WandRod rod = this.getRod(stack);
		return rod instanceof StaffRod && ((StaffRod) rod).hasRunes();
	}

	public void setRod(ItemStack stack, WandRod rod)
	{
		stack.setTagInfo("rod", new NBTTagString(rod.getTag()));
		if (rod instanceof StaffRod)
		{
			NBTTagList tags = new NBTTagList();
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("AttributeName", SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
			AttributeModifier am = new AttributeModifier(field_111210_e, "Weapon modifier", 6.0D, 0);
			tag.setString("Name", am.getName());
			tag.setDouble("Amount", am.getAmount());
			tag.setInteger("Operation", am.getOperation());
			tag.setLong("UUIDMost", am.getID().getMostSignificantBits());
			tag.setLong("UUIDLeast", am.getID().getLeastSignificantBits());
			tags.appendTag(tag);
			stack.stackTagCompound.setTag("AttributeModifiers", tags);
		}

	}

	public WandCap getCap(ItemStack stack)
	{
		return stack.hasTagCompound() && stack.stackTagCompound.hasKey("cap") ? WandCap.caps.get(stack.stackTagCompound.getString("cap")) : ConfigItems.WAND_CAP_IRON;
	}

	public void setCap(ItemStack stack, WandCap cap)
	{
		stack.setTagInfo("cap", new NBTTagString(cap.getTag()));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
	{
		MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);
		if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK)
		{
			int x = mop.blockX;
			int y = mop.blockY;
    
			if (EventUtils.cantBreak(player, x, y, z))
    

			Block block = world.getBlock(x, y, z);

			if (block instanceof IWandable)
			{
				ItemStack stack = ((IWandable) block).onWandRightClick(world, itemstack, player);
				if (stack != null)
					return stack;
			}

			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof IWandable)
			{
				ItemStack stack = ((IWandable) tile).onWandRightClick(world, itemstack, player);
				if (stack != null)
					return stack;
			}
		}

		ItemFocusBasic focus = this.getFocus(itemstack);
		if (focus != null && !WandManager.isOnCooldown(player))
		{
    
    

			ItemStack stack = focus.onFocusRightClick(itemstack, world, player, mop);
			if (stack != null)
    
				if (isHeldItem && stack.stackSize > 0 && !ModUtils.isValidStack(player.getHeldItem()))
    

				return stack;
			}
		}

		return super.onItemRightClick(itemstack, world, player);
	}

	public void setObjectInUse(ItemStack stack, int x, int y, int z)
	{
		if (stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();

		stack.stackTagCompound.setInteger("IIUX", x);
		stack.stackTagCompound.setInteger("IIUY", y);
		stack.stackTagCompound.setInteger("IIUZ", z);
	}

	public void clearObjectInUse(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();

		stack.stackTagCompound.removeTag("IIUX");
		stack.stackTagCompound.removeTag("IIUY");
		stack.stackTagCompound.removeTag("IIUZ");
	}

	public IWandable getObjectInUse(ItemStack stack, World world)
	{
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("IIUX"))
		{
			TileEntity te = world.getTileEntity(stack.stackTagCompound.getInteger("IIUX"), stack.stackTagCompound.getInteger("IIUY"), stack.stackTagCompound.getInteger("IIUZ"));
			if (te instanceof IWandable)
				return (IWandable) te;
		}

		return null;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count)
	{
		IWandable tv = this.getObjectInUse(stack, player.worldObj);
		if (tv != null)
		{
			this.animation = ItemFocusBasic.WandFocusAnimation.WAVE;
			tv.onUsingWandTick(stack, player, count);
		}
		else
		{
			ItemFocusBasic focus = this.getFocus(stack);
			if (focus != null && !WandManager.isOnCooldown(player))
			{
				WandManager.setCooldown(player, focus.getActivationCooldown(this.getFocusItem(stack)));
				focus.onUsingFocusTick(stack, player, count);
			}
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int count)
	{
		IWandable tv = this.getObjectInUse(stack, player.worldObj);
		if (tv != null)
		{
			tv.onWandStoppedUsing(stack, world, player, count);
			this.animation = null;
		}
		else
		{
			ItemFocusBasic focus = this.getFocus(stack);
			if (focus != null)
				focus.onPlayerStoppedUsingFocus(stack, world, player, count);
		}

		this.clearObjectInUse(stack);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.bow;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemstack)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack)
	{
		ItemStack focus = this.getFocusItem(stack);
		if (focus != null && !WandManager.isOnCooldown(entityLiving))
		{
			WandManager.setCooldown(entityLiving, this.getFocus(stack).getActivationCooldown(focus));
			return focus.getItem().onEntitySwing(entityLiving, stack);
		}
		return super.onEntitySwing(entityLiving, stack);
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer player)
	{
		ItemFocusBasic focus = this.getFocus(itemstack);
		if (focus != null && !WandManager.isOnCooldown(player))
		{
			WandManager.setCooldown(player, focus.getActivationCooldown(this.getFocusItem(itemstack)));
			return focus.onFocusBlockStartBreak(itemstack, x, y, z, player);
		}
		return false;
	}

	@Override
	public boolean canHarvestBlock(Block par1Block, ItemStack itemstack)
	{
		ItemFocusBasic focus = this.getFocus(itemstack);
		return focus != null && this.getFocusItem(itemstack).getItem().canHarvestBlock(par1Block, itemstack);
	}

	@Override
	public float func_150893_a(ItemStack itemstack, Block block)
	{
		ItemFocusBasic focus = this.getFocus(itemstack);
		return focus != null ? this.getFocusItem(itemstack).getItem().func_150893_a(itemstack, null) : super.func_150893_a(itemstack, block);
	}

	@Override
	public ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, int side, EntityPlayer player)
	{
		ItemFocusBasic focus = this.getFocus(stack);
		return focus instanceof IArchitect && focus.isUpgradedWith(this.getFocusItem(stack), FocusUpgradeType.architect) ? ((IArchitect) focus).getArchitectBlocks(stack, world, x, y, z, side, player) : null;
	}

	@Override
	public boolean showAxis(ItemStack stack, World world, EntityPlayer player, int side, IArchitect.EnumAxis axis)
	{
		ItemFocusBasic focus = this.getFocus(stack);
		return focus instanceof IArchitect && focus.isUpgradedWith(this.getFocusItem(stack), FocusUpgradeType.architect) && ((IArchitect) focus).showAxis(stack, world, player, side, axis);
	}
}
