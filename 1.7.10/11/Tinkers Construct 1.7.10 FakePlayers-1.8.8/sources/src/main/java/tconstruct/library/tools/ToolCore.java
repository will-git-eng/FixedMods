package tconstruct.library.tools;

import cofh.api.energy.IEnergyContainerItem;
import cofh.core.item.IEqualityOverrideItem;
import ru.will.git.tconstruct.EventConfig;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import tconstruct.library.ActiveToolMod;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.modifier.IModifyable;
import tconstruct.library.modifier.ItemModifier;
import tconstruct.library.util.TextureHelper;
import tconstruct.tools.TinkerTools;
import tconstruct.tools.entity.FancyEntityItem;
import tconstruct.util.config.PHConstruct;
import tconstruct.weaponry.TinkerWeaponry;

import java.util.*;

    

@Optional.InterfaceList({ @Optional.Interface(modid = "CoFHAPI|energy",
											  iface = "cofh.api.energy.IEnergyContainerItem"), @Optional.Interface(modid = "CoFHCore",
																												   iface = "cofh.core.item.IEqualityOverrideItem") })
public abstract class ToolCore extends Item implements IEnergyContainerItem, IEqualityOverrideItem, IModifyable
{
	protected Random random = new Random();
	protected int damageVsEntity;
	public static IIcon blankSprite;
	public static IIcon emptyIcon;

	public ToolCore(int baseDamage)
	{
		this.maxStackSize = 1;
		this.setMaxDamage(100);
		this.setUnlocalizedName("InfiTool");
		this.setCreativeTab(TConstructRegistry.toolTab);
		this.damageVsEntity = baseDamage;
		TConstructRegistry.addToolMapping(this);
		this.setNoRepair();
		this.canRepair = false;
	}

	@Override
	public String getBaseTagName()
	{
		return "InfiTool";
	}

	@Override
	public String getModifyType()
	{
		return "Tool";
	}

    

	public int durabilityTypeHandle()
	{
		return 1;
	}

	public int durabilityTypeAccessory()
	{
		return 0;
	}

	public int durabilityTypeExtra()
	{
		return 0;
	}

	public int getModifierAmount()
	{
		return 3;
	}

	public String getToolName()
	{
		return this.getClass().getSimpleName();
	}

	public String getLocalizedToolName()
	{
		return StatCollector.translateToLocal("tool." + this.getToolName().toLowerCase());
	}

    

	public HashMap<Integer, IIcon> headIcons = new HashMap<Integer, IIcon>();
	public HashMap<Integer, IIcon> brokenIcons = new HashMap<Integer, IIcon>();
	public HashMap<Integer, IIcon> handleIcons = new HashMap<Integer, IIcon>();
	public HashMap<Integer, IIcon> accessoryIcons = new HashMap<Integer, IIcon>();
	public HashMap<Integer, IIcon> effectIcons = new HashMap<Integer, IIcon>();
    
	public HashMap<Integer, String> headStrings = new HashMap<Integer, String>();
	public HashMap<Integer, String> brokenPartStrings = new HashMap<Integer, String>();
	public HashMap<Integer, String> handleStrings = new HashMap<Integer, String>();
	public HashMap<Integer, String> accessoryStrings = new HashMap<Integer, String>();
	public HashMap<Integer, String> effectStrings = new HashMap<Integer, String>();
	public HashMap<Integer, String> extraStrings = new HashMap<Integer, String>();

	@SideOnly(Side.CLIENT)
	@Override
	public boolean requiresMultipleRenderPasses()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public final int getRenderPasses(int metadata)
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack)
	{
		return false;
    
	public int getPartAmount()
	{
		return 3;
	}

	public abstract String getIconSuffix(int partType);

	public abstract String getEffectSuffix();

	public abstract String getDefaultFolder();

    
	public String getDefaultTexturePath()
	{
		return "tinker:" + this.getDefaultFolder();
	}

	public void registerPartPaths(int index, String[] location)
	{
		this.headStrings.put(index, location[0]);
		this.brokenPartStrings.put(index, location[1]);
		this.handleStrings.put(index, location[2]);
		if (location.length > 3)
			this.accessoryStrings.put(index, location[3]);
		if (location.length > 4)
			this.extraStrings.put(index, location[4]);
	}

	public void registerAlternatePartPaths(int index, String[] location)
	{

	}

	public void registerEffectPath(int index, String location)
	{
		this.effectStrings.put(index, location);
	}

	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		boolean minimalTextures = PHConstruct.minimalTextures;
		this.addIcons(this.headStrings, this.headIcons, iconRegister, this.getIconSuffix(0), minimalTextures);
		this.addIcons(this.brokenPartStrings, this.brokenIcons, iconRegister, this.getIconSuffix(1), minimalTextures);
		this.addIcons(this.handleStrings, this.handleIcons, iconRegister, this.getIconSuffix(2), minimalTextures);
		this.addIcons(this.accessoryStrings, this.accessoryIcons, iconRegister, this.getIconSuffix(3), minimalTextures);
		this.addIcons(this.extraStrings, this.extraIcons, iconRegister, this.getIconSuffix(4), minimalTextures);

		this.addIcons(this.effectStrings, this.effectIcons, iconRegister, null, false);

		emptyIcon = iconRegister.registerIcon("tinker:blankface");
	}

	protected void addIcons(HashMap<Integer, String> textures, HashMap<Integer, IIcon> icons, IIconRegister iconRegister, String standard, boolean defaultOnly)
	{
		icons.clear();

    
			for (Map.Entry<Integer, String> entry : textures.entrySet())
			{
				if (TextureHelper.itemTextureExists(entry.getValue()))
					icons.put(entry.getKey(), iconRegister.registerIcon(entry.getValue()));
			}

		if (standard != null && !standard.isEmpty())
		{
			standard = this.getDefaultTexturePath() + "/" + standard;
			icons.put(-1, iconRegister.registerIcon(standard));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta)
	{
		return blankSprite;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int renderPass)
	{
		NBTTagCompound tags = stack.getTagCompound();

		if (tags != null)
		{
			tags = stack.getTagCompound().getCompoundTag("InfiTool");
			if (renderPass < this.getPartAmount())
    
				if (renderPass == 0)
    
				else if (renderPass == 1)
				{
					if (tags.getBoolean("Broken"))
						return this.getCorrectIcon(this.brokenIcons, tags.getInteger("RenderHead"));
					else
						return this.getCorrectIcon(this.headIcons, tags.getInteger("RenderHead"));
    
				else if (renderPass == 2)
    
				else if (renderPass == 3)
					return this.getCorrectIcon(this.extraIcons, tags.getInteger("RenderExtra"));
    
			else if (renderPass <= 10)
			{
				String effect = "Effect" + (1 + renderPass - this.getPartAmount());
				if (tags.hasKey(effect))
					return this.effectIcons.get(tags.getInteger(effect));
			}
			return blankSprite;
		}
		return emptyIcon;
	}

	protected IIcon getCorrectIcon(Map<Integer, IIcon> icons, int id)
	{
		if (icons.containsKey(id))
    
		return icons.get(-1);
	}

    
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		if (!stack.hasTagCompound())
			return;

		NBTTagCompound tags = stack.getTagCompound();
		if (tags.hasKey("Energy"))
		{
			String color = "";
			int RF = tags.getInteger("Energy");

			if (RF != 0)
				if (RF <= this.getMaxEnergyStored(stack) / 3)
					color = "\u00a74";
				else if (RF > this.getMaxEnergyStored(stack) * 2 / 3)
					color = "\u00a72";
				else
					color = "\u00a76";

			String energy = color + tags.getInteger("Energy") + "/" + this.getMaxEnergyStored(stack) + " RF";
			list.add(energy);
		}
		if (tags.hasKey("InfiTool"))
		{
			boolean broken = tags.getCompoundTag("InfiTool").getBoolean("Broken");
			if (broken)
				list.add("\u00A7o" + StatCollector.translateToLocal("tool.core.broken"));
			else
			{
				int head = tags.getCompoundTag("InfiTool").getInteger("Head");
				int handle = tags.getCompoundTag("InfiTool").getInteger("Handle");
				int binding = tags.getCompoundTag("InfiTool").getInteger("Accessory");
				int extra = tags.getCompoundTag("InfiTool").getInteger("Extra");

				String headName = this.getAbilityNameForType(head, 0);
				if (!headName.equals(""))
					list.add(getStyleForType(head) + headName);

				String handleName = this.getAbilityNameForType(handle, 1);
				if (!handleName.equals("") && handle != head)
					list.add(getStyleForType(handle) + handleName);

				if (this.getPartAmount() >= 3)
				{
					String bindingName = this.getAbilityNameForType(binding, 2);
					if (!bindingName.equals("") && binding != head && binding != handle)
						list.add(getStyleForType(binding) + bindingName);
				}

				if (this.getPartAmount() >= 4)
				{
					String extraName = this.getAbilityNameForType(extra, 3);
					if (!extraName.equals("") && extra != head && extra != handle && extra != binding)
						list.add(getStyleForType(extra) + extraName);
				}

				int unbreaking = tags.getCompoundTag("InfiTool").getInteger("Unbreaking");
				String reinforced = this.getReinforcedName(head, handle, binding, extra, unbreaking);
				if (!reinforced.equals(""))
					list.add(reinforced);

				boolean displayToolTips = true;
				int tipNum = 0;
				while (displayToolTips)
				{
					tipNum++;
					String tooltip = "Tooltip" + tipNum;
					if (tags.getCompoundTag("InfiTool").hasKey(tooltip))
					{
						String tipName = tags.getCompoundTag("InfiTool").getString(tooltip);
						if (!tipName.equals(""))
    
    
							String locString = "modifier.tooltip." + EnumChatFormatting.getTextWithoutFormattingCodes(tipName);
							locString = locString.replace(" ", "");
							if (StatCollector.canTranslate(locString))
								tipName = tipName.replace(EnumChatFormatting.getTextWithoutFormattingCodes(tipName), StatCollector.translateToLocal(locString));

							list.add(tipName);
						}
					}
					else
						displayToolTips = false;
				}
			}
		}
		list.add("");
		int attack = (int) (tags.getCompoundTag("InfiTool").getInteger("Attack") * this.getDamageModifier());
		list.add("\u00A79+" + attack + " " + StatCollector.translateToLocalFormatted("attribute.name.generic.attackDamage"));

	}

	public static String getStyleForType(int type)
	{
		return TConstructRegistry.getMaterial(type).style();
	}

    
	public String getAbilityNameForType(int type, int part)
	{
		return TConstructRegistry.getMaterial(type).ability();
	}

	public String getReinforcedName(int head, int handle, int accessory, int extra, int unbreaking)
	{
		tconstruct.library.tools.ToolMaterial headMat = TConstructRegistry.getMaterial(head);
		tconstruct.library.tools.ToolMaterial handleMat = TConstructRegistry.getMaterial(handle);
		tconstruct.library.tools.ToolMaterial accessoryMat = TConstructRegistry.getMaterial(accessory);
		tconstruct.library.tools.ToolMaterial extraMat = TConstructRegistry.getMaterial(extra);

		int reinforced = 0;
		String style = "";
		int current = headMat.reinforced();
		if (current > 0)
		{
			style = headMat.style();
			reinforced = current;
		}
		current = handleMat.reinforced();
		if (current > 0 && current > reinforced)
		{
			style = handleMat.style();
			reinforced = current;
		}
		if (this.getPartAmount() >= 3)
		{
			current = accessoryMat.reinforced();
			if (current > 0 && current > reinforced)
			{
				style = accessoryMat.style();
				reinforced = current;
			}
		}
		if (this.getPartAmount() >= 4)
		{
			current = extraMat.reinforced();
			if (current > 0 && current > reinforced)
			{
				style = extraMat.style();
				reinforced = current;
			}
		}

		reinforced += unbreaking - reinforced;

		if (reinforced > 0)
			return style + this.getReinforcedString(reinforced);
		return "";
	}

	String getReinforcedString(int reinforced)
	{
		if (reinforced > 9)
			return StatCollector.translateToLocal("tool.unbreakable");
		String ret = StatCollector.translateToLocal("tool.reinforced") + " ";
		switch (reinforced)
		{
			case 1:
				ret += "I";
				break;
			case 2:
				ret += "II";
				break;
			case 3:
				ret += "III";
				break;
			case 4:
				ret += "IV";
				break;
			case 5:
				ret += "V";
				break;
			case 6:
				ret += "VI";
				break;
			case 7:
				ret += "VII";
				break;
			case 8:
				ret += "VIII";
				break;
			case 9:
				ret += "IX";
				break;
			default:
				ret += "X";
				break;
		}
		return ret;
    
	public void onEntityDamaged(World world, EntityLivingBase player, Entity entity)
	{

	}

    

	@Override
	public void getSubItems(Item id, CreativeTabs tab, List list)
	{
		Iterator iter = TConstructRegistry.toolMaterials.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry pairs = (Map.Entry) iter.next();
			tconstruct.library.tools.ToolMaterial material = (tconstruct.library.tools.ToolMaterial) pairs.getValue();
			this.buildTool((Integer) pairs.getKey(), ToolBuilder.defaultToolName(material, this), list);
		}
	}

	public void buildTool(int id, String name, List list)
	{
		Item accessory = this.getAccessoryItem();
		ItemStack accessoryStack = accessory != null ? new ItemStack(this.getAccessoryItem(), 1, id) : null;
		Item extra = this.getExtraItem();
		ItemStack extraStack = extra != null ? new ItemStack(extra, 1, id) : null;
		ItemStack tool = ToolBuilder.instance.buildTool(new ItemStack(this.getHeadItem(), 1, id), new ItemStack(this.getHandleItem(), 1, id), accessoryStack, extraStack, name);
		if (tool != null)
		{
			tool.getTagCompound().getCompoundTag("InfiTool").setBoolean("Built", true);
			list.add(tool);
		}
	}

	public abstract Item getHeadItem();

	public abstract Item getAccessoryItem();

	public Item getExtraItem()
	{
		return null;
	}

	public Item getHandleItem()
	{
		return TinkerTools.toolRod;
	}

    

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5)
	{
		for (ActiveToolMod mod : TConstructRegistry.activeModifiers)
		{
			mod.updateTool(this, stack, world, entity);
		}
    
	@Override
    
	@Override
	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player)
	{
		if (!stack.hasTagCompound())
			return false;

		boolean cancelHarvest = false;
		for (ActiveToolMod mod : TConstructRegistry.activeModifiers)
		{
			if (mod.beforeBlockBreak(this, stack, x, y, z, player))
				cancelHarvest = true;
		}

		return cancelHarvest;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack itemstack, World world, Block block, int x, int y, int z, EntityLivingBase player)
	{
		if (!itemstack.hasTagCompound())
    
		for (ActiveToolMod mod : TConstructRegistry.activeModifiers)
		{
			mod.afterBlockBreak(this, itemstack, block, x, y, z, player);
		}

		if (block != null && block.getBlockHardness(world, x, y, z) != 0.0D)
			return AbilityHelper.onBlockChanged(itemstack, world, block, x, y, z, player, this.random);
		return true;
	}

	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta)
	{
		if (!stack.hasTagCompound())
			return 0f;

		NBTTagCompound tags = stack.getTagCompound();
		if (tags.getCompoundTag("InfiTool").getBoolean("Broken"))
			return 0.1f;
		return 1f;
    
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
	{
		return AbilityHelper.onLeftClickEntity(stack, player, entity, this, 0);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase mob, EntityLivingBase player)
	{
		return true;
	}

	public boolean pierceArmor()
	{
		return false;
	}

	public float chargeAttack()
	{
		return 1f;
	}

	public int getDamageVsEntity(Entity par1Entity)
	{
		return this.damageVsEntity;
    
	public float getDurabilityModifier()
	{
		return 1f;
	}

	public float getRepairCost()
	{
		return this.getDurabilityModifier();
	}

	public float getDamageModifier()
	{
		return 1.0f;
	}

	@Override
	public int getColorFromItemStack(ItemStack stack, int renderPass)
	{
		NBTTagCompound tags = stack.getTagCompound();

		if (tags != null)
		{
			tags = stack.getTagCompound().getCompoundTag("InfiTool");
			if (renderPass < this.getPartAmount())
				switch (renderPass)
				{
					case 0:
						return this.getCorrectColor(stack, renderPass, tags, "Handle", this.handleIcons);
					case 1:
						return tags.getBoolean("Broken") ? this.getCorrectColor(stack, renderPass, tags, "Head", this.brokenIcons) : this.getCorrectColor(stack, renderPass, tags, "Head", this.headIcons);
					case 2:
						return this.getCorrectColor(stack, renderPass, tags, "Accessory", this.accessoryIcons);
					case 3:
						return this.getCorrectColor(stack, renderPass, tags, "Extra", this.extraIcons);
				}
		}
		return super.getColorFromItemStack(stack, renderPass);
	}

	protected int getCorrectColor(ItemStack stack, int renderPass, NBTTagCompound tags, String key, Map<Integer, IIcon> map)
    
		if (tags.hasKey(key + "Color"))
    
		Integer matId = tags.getInteger("Render" + key);
		if (map.containsKey(matId))
    
		return this.getDefaultColor(renderPass, matId);
	}

	protected int getDefaultColor(int renderPass, int materialID)
	{
		if (TConstructRegistry.getMaterial(materialID) != null)
			return TConstructRegistry.getMaterial(materialID).primaryColor();

		return 0xffffffff;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		boolean used = false;
		int hotbarSlot = player.inventory.currentItem;
		int itemSlot = hotbarSlot == 0 ? 8 : hotbarSlot + 1;
		ItemStack nearbyStack = null;

		if (hotbarSlot < 8)
		{
			nearbyStack = player.inventory.getStackInSlot(itemSlot);
			if (nearbyStack != null)
			{
				Item item = nearbyStack.getItem();
				if (item instanceof ItemPotion && ItemPotion.isSplash(nearbyStack.getItemDamage()))
    
					if (EventConfig.inList(EventConfig.pickaxeRMBBlackList, nearbyStack))
    

					nearbyStack = item.onItemRightClick(nearbyStack, world, player);
					if (nearbyStack.stackSize < 1)
					{
						nearbyStack = null;
						player.inventory.setInventorySlotContents(itemSlot, null);
					}
    
				if (item != null && item == TinkerWeaponry.shuriken)
    
					if (EventConfig.inList(EventConfig.pickaxeRMBBlackList, nearbyStack))
    

					item.onItemRightClick(nearbyStack, world, player);
				}
			}
		}
		return stack;
	}

    
	@Override
	public boolean isItemTool(ItemStack par1ItemStack)
	{
		return false;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return false;
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	@Override
	public int getItemEnchantability()
	{
		return 0;
	}

	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack, int pass)
	{
		return false;
	}

    
	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		if (!stack.hasTagCompound())
			return false;

		NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
		return !tags.getBoolean("Broken") && this.getDamage(stack) > 0;
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return 100;
	}

	@Override
	public int getDamage(ItemStack stack)
	{
		NBTTagCompound tags = stack.getTagCompound();
		if (tags == null)
			return 0;
		if (tags.hasKey("Energy"))
		{
			int energy = tags.getInteger("Energy");
			int max = this.getMaxEnergyStored(stack);
			if (energy > 0)
			{
				int damage = (max - energy) * 100 / max;
				if (damage == 0 && max - energy > 0)
					damage = 1;
				super.setDamage(stack, damage);
				return damage;
			}
		}
		int dur = tags.getCompoundTag("InfiTool").getInteger("Damage");
		int max = tags.getCompoundTag("InfiTool").getInteger("TotalDurability");
		int damage = 0;
		if (max > 0)
    
		if (damage == 0 && dur > 0)
    
		super.setDamage(stack, damage);
		return damage;
	}

	@Override
	public int getDisplayDamage(ItemStack stack)
	{
		return this.getDamage(stack);
	}

	@Override
	public void setDamage(ItemStack stack, int damage)
	{
		int change = damage - stack.getItemDamage();
		if (change == 0)
			return;

		AbilityHelper.damageTool(stack, change, null, false);
    
	}

    
	@Override
	public boolean hasCustomEntity(ItemStack stack)
	{
		return true;
	}

	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack)
	{
		return new FancyEntityItem(world, location, itemstack);
    
    
	protected int capacity = 400000;
	protected int maxReceive = 400000;
	protected int maxExtract = 80;

    
	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate)
	{
		NBTTagCompound tags = container.getTagCompound();
		if (tags == null || !tags.hasKey("Energy"))
			return 0;
		int energy = tags.getInteger("Energy");
    
    
    
		energyReceived = Math.min(maxEnergy - energy, Math.min(energyReceived, maxReceive));
		if (!simulate)
		{
			energy += energyReceived;
    
		}
		return energyReceived;
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate)
	{
		NBTTagCompound tags = container.getTagCompound();
		if (tags == null || !tags.hasKey("Energy"))
			return 0;
		int energy = tags.getInteger("Energy");
    
    
		energyExtracted = Math.min(energy, Math.min(energyExtracted, maxExtract));
		if (!simulate)
		{
			energy -= energyExtracted;
    
		}
		return energyExtracted;
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int getEnergyStored(ItemStack container)
	{
		NBTTagCompound tags = container.getTagCompound();
		if (tags == null || !tags.hasKey("Energy"))
			return 0;
		return tags.getInteger("Energy");
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int getMaxEnergyStored(ItemStack container)
	{
		NBTTagCompound tags = container.getTagCompound();
		if (tags == null || !tags.hasKey("Energy"))
			return 0;

		if (tags.hasKey("EnergyMax"))
    
		return this.capacity;
	}

	@Override
	@Optional.Method(modid = "CoFHCore")
	public boolean isLastHeldItemEqual(ItemStack current, ItemStack previous)
	{
		if (!current.hasTagCompound() || !previous.hasTagCompound())
			return false;

		NBTTagCompound curTags = current.getTagCompound();
		NBTTagCompound prevTags = previous.getTagCompound();
		if (curTags == prevTags)
			return true;
		if (!curTags.hasKey("InfiTool") || !prevTags.hasKey("InfiTool"))
    
		curTags = (NBTTagCompound) curTags.copy();
		prevTags = (NBTTagCompound) prevTags.copy();

		curTags.removeTag("Energy");
		prevTags.removeTag("Energy");
		curTags.getCompoundTag("InfiTool").removeTag("Damage");
		prevTags.getCompoundTag("InfiTool").removeTag("Damage");

		return curTags.equals(prevTags);
    
}
