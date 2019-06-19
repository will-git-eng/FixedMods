    
package thaumic.tinkerer.common.item;

import ru.will.git.ttinkerer.NoDupeProperties;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import thaumcraft.api.IRepairable;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.ConfigItems;
import thaumic.tinkerer.client.core.helper.IconHelper;
import thaumic.tinkerer.common.core.handler.ModCreativeTab;
import thaumic.tinkerer.common.core.helper.EnumMobAspect;
import thaumic.tinkerer.common.lib.LibItemNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ITTinkererItem;
import thaumic.tinkerer.common.registry.ThaumicTinkererInfusionRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;
import thaumic.tinkerer.common.research.ResearchHelper;
import thaumic.tinkerer.common.research.TTResearchItem;

import java.util.ArrayList;

public class ItemBloodSword extends ItemSword implements IRepairable, ITTinkererItem
{

	private static final int DAMAGE = 10;
	static int handleNext = 0;
	private IIcon activeIcon;

	public ItemBloodSword()
	{
		super(EnumHelper.addToolMaterial("TT_BLOOD", 0, 950, 0, 0, ThaumcraftApi.toolMatThaumium.getEnchantability()));
		MinecraftForge.EVENT_BUS.register(this);
		this.setCreativeTab(ModCreativeTab.INSTANCE);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{

		return super.getItemUseAction(par1ItemStack);
	}

	@Override
	public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
	{
		return stack.stackTagCompound != null && stack.stackTagCompound.getInteger("Activated") == 1 ? this.activeIcon : this.itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.itemIcon = IconHelper.forItem(par1IconRegister, this, 0);
		this.activeIcon = IconHelper.forItem(par1IconRegister, this, 1);
	}

	@Override
	public Multimap getItemAttributeModifiers()
	{
		Multimap multimap = HashMultimap.create();
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", DAMAGE, 0));
		multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", 0.25, 1));
		return multimap;
	}

	public void addDrops(LivingDropsEvent event, ItemStack dropStack)
	{
		EntityItem entityitem = new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, dropStack);
		entityitem.delayBeforeCanPickup = 10;
		event.drops.add(entityitem);
	}

	@SubscribeEvent
	public void onDrops(LivingDropsEvent event)
	{
		if (event.source.damageType.equals("player"))
		{

			EntityPlayer player = (EntityPlayer) event.source.getEntity();
			ItemStack stack = player.getCurrentEquippedItem();
			if (stack != null && stack.getItem() == this && stack.stackTagCompound != null && stack.stackTagCompound.getInteger("Activated") == 1)
			{
    
    
    
				if (aspects != null)
    
					if (event.entityLiving != null && !NoDupeProperties.canDropAspect(event.entityLiving))
    

    
					for (Aspect a : aspects)
					{
						this.addDrops(event, ItemMobAspect.getStackFromAspect(a));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onDamageTaken(LivingAttackEvent event)
	{
		if (event.entity == null)
			return;

		if (event.entity.worldObj.isRemote)
			return;

		boolean handle = handleNext == 0;
		if (!handle)
			handleNext--;

		if (event.entityLiving instanceof EntityPlayer && handle)
		{
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			ItemStack itemInUse = player.itemInUse;
			if (itemInUse != null && itemInUse.getItem() == this)
			{

				event.setCanceled(true);
				handleNext = 3;
				player.attackEntityFrom(DamageSource.magic, 3);
			}
		}

		if (handle)
		{
			Entity source = event.source.getSourceOfDamage();
			if (source != null && source instanceof EntityLivingBase)
			{
				EntityLivingBase attacker = (EntityLivingBase) source;
				ItemStack itemInUse = attacker.getHeldItem();
				if (itemInUse != null && itemInUse.getItem() == this)
					attacker.attackEntityFrom(DamageSource.magic, 2);
			}
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World par2World, EntityPlayer par3EntityPlayer)
	{

		ItemStack cache = super.onItemRightClick(stack, par2World, par3EntityPlayer);
		if (par3EntityPlayer.isSneaking() && !par2World.isRemote)
		{
			if (stack.stackTagCompound == null)
				stack.stackTagCompound = new NBTTagCompound();
			if (stack.stackTagCompound.getInteger("Activated") == 0)
			{
				par3EntityPlayer.addChatMessage(new ChatComponentTranslation("ttmisc.bloodSword.activateEssentiaHarvest"));
				stack.stackTagCompound.setInteger("Activated", 1);
			}
			else
			{
				par3EntityPlayer.addChatMessage(new ChatComponentTranslation("ttmisc.bloodSword.deactivateEssentiaHarvest"));
				stack.stackTagCompound.setInteger("Activated", 0);
			}
		}
		return cache;
	}

	@Override
	public ArrayList<Object> getSpecialParameters()
	{
		return null;
	}

	@Override
	public String getItemName()
	{
		return LibItemNames.BLOOD_SWORD;
	}

	@Override
	public boolean shouldRegister()
	{
		return true;
	}

	@Override
	public boolean shouldDisplayInTab()
	{
		return true;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		return (TTResearchItem) new TTResearchItem(LibResearch.KEY_BLOOD_SWORD, new AspectList().add(Aspect.HUNGER, 2).add(Aspect.WEAPON, 1).add(Aspect.FLESH, 1).add(Aspect.SOUL, 1), -4, 6, 3, new ItemStack(this)).setWarp(1).setParents(LibResearch.KEY_CLEANSING_TALISMAN).setPages(new ResearchPage("0"), ResearchHelper.infusionPage(LibResearch.KEY_BLOOD_SWORD), new ResearchPage("1")).setSecondary();

	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		return new ThaumicTinkererInfusionRecipe(LibResearch.KEY_BLOOD_SWORD, new ItemStack(this), 6, new AspectList().add(Aspect.HUNGER, 20).add(Aspect.DARKNESS, 5).add(Aspect.SOUL, 10).add(Aspect.MAN, 6), new ItemStack(ConfigItems.itemSwordThaumium), new ItemStack(Items.rotten_flesh), new ItemStack(Items.porkchop), new ItemStack(Items.beef), new ItemStack(Items.bone), new ItemStack(Items.diamond), new ItemStack(Items.ghast_tear));

	}
}
