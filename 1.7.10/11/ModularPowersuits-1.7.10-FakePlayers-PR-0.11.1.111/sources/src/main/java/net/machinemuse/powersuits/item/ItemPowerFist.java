package net.machinemuse.powersuits.item;

import java.util.List;

import ru.will.git.reflectionmedic.util.EventUtils;

import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.api.tool.ITool;
import forestry.api.arboriculture.IToolGrafter;
import mekanism.api.IMekWrench;
import mods.railcraft.api.core.items.IToolCrowbar;
import net.machinemuse.api.IModularItem;
import net.machinemuse.api.IPowerModule;
import net.machinemuse.api.ModuleManager;
import net.machinemuse.api.moduletrigger.IBlockBreakingModule;
import net.machinemuse.api.moduletrigger.IRightClickModule;
import net.machinemuse.general.gui.MuseIcon;
import net.machinemuse.numina.item.NuminaItemUtils;
import net.machinemuse.numina.network.MusePacketModeChangeRequest;
import net.machinemuse.numina.network.PacketSender;
import net.machinemuse.powersuits.common.Config;
import net.machinemuse.powersuits.powermodule.tool.GrafterModule;
import net.machinemuse.powersuits.powermodule.tool.OmniWrenchModule;
import net.machinemuse.powersuits.powermodule.weapon.MeleeAssistModule;
import net.machinemuse.utils.ElectricItemUtils;
import net.machinemuse.utils.MuseHeatUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.api.IMFRHammer;

    
@Optional.InterfaceList({ @Optional.Interface(iface = "mekanism.api.IMekWrench", modid = "Mekanism",
		striprefs = true), @Optional.Interface(iface = "crazypants.enderio.api.tool.ITool", modid = "EnderIO",
				striprefs = true), @Optional.Interface(iface = "mrtjp.projectred.api.IScrewdriver",
						modid = "ProjRed|Core",
						striprefs = true), @Optional.Interface(iface = "com.bluepowermod.api.misc.IScrewdriver",
								modid = "bluepower", striprefs = true), @Optional.Interface(
										iface = "forestry.api.arboriculture.IToolGrafter", modid = "Forestry",
										striprefs = true), @Optional.Interface(
												iface = "mods.railcraft.api.core.items.IToolCrowbar",
												modid = "Railcraft", striprefs = true), @Optional.Interface(
														iface = "powercrystals.minefactoryreloaded.api.IMFRHammer",
														modid = "MineFactoryReloaded",
														striprefs = true), @Optional.Interface(
																iface = "cofh.api.item.IToolHammer", modid = "CoFHCore",
																striprefs = true), @Optional.Interface(
																		iface = "buildcraft.api.tools.IToolWrench",
																		modid = "BuildCraft|Core",
																		striprefs = true), @Optional.Interface(
																				iface = "appeng.api.implementations.items.IAEWrench",
																				modid = "appliedenergistics2",
																				striprefs = true) })
public class ItemPowerFist extends MPSItemElectricTool implements IModularItem, IToolGrafter, IToolHammer, IMFRHammer, IToolCrowbar, IAEWrench, IToolWrench, com.bluepowermod.api.misc.IScrewdriver, mrtjp.projectred.api.IScrewdriver, ITool, IMekWrench, IModeChangingModularItem
{
	public final String iconpath = MuseIcon.ICON_PREFIX + "handitem";

	public ItemPowerFist()
	{
		super(0, ToolMaterial.EMERALD);
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setCreativeTab(Config.getCreativeTab());
		this.setUnlocalizedName("powerFist");
    
    
	@Override
	public float func_150893_a(ItemStack stack, Block block)
	{
		return 1.0F;
	}

    
	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta)
	{
		return this.func_150893_a(stack, block);
    

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.iconpath);
	}

    
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entityBeingHit, EntityLivingBase entityDoingHitting)
    
		if (EventUtils.cantDamage(entityDoingHitting, entityBeingHit))
    

		if (ModuleManager.itemHasActiveModule(stack, OmniWrenchModule.MODULE_OMNI_WRENCH))
		{
			entityBeingHit.rotationYaw += 90.0f;
			entityBeingHit.rotationYaw %= 360.0f;
		}
		if (entityDoingHitting instanceof EntityPlayer && ModuleManager.itemHasActiveModule(stack, MeleeAssistModule.MODULE_MELEE_ASSIST))
		{
			EntityPlayer player = (EntityPlayer) entityDoingHitting;
			double drain = ModuleManager.computeModularProperty(stack, MeleeAssistModule.PUNCH_ENERGY);
			if (ElectricItemUtils.getPlayerEnergy(player) > drain)
			{
				ElectricItemUtils.drainPlayerEnergy(player, drain);
				double damage = ModuleManager.computeModularProperty(stack, MeleeAssistModule.PUNCH_DAMAGE);
				double knockback = ModuleManager.computeModularProperty(stack, MeleeAssistModule.PUNCH_KNOCKBACK);
				DamageSource damageSource = DamageSource.causePlayerDamage(player);
				if (entityBeingHit.attackEntityFrom(damageSource, (int) damage))
				{
					Vec3 lookVec = player.getLookVec();
					entityBeingHit.addVelocity(lookVec.xCoord * knockback, Math.abs(lookVec.yCoord + 0.2f) * knockback, lookVec.zCoord * knockback);
				}
			}
		}
		return true;
	}

    
	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase entity)
	{
		if (entity instanceof EntityPlayer)
    
			if (EventUtils.cantBreak((EntityPlayer) entity, x, y, z))
    

			for (IBlockBreakingModule module : ModuleManager.getBlockBreakingModules())
				if (ModuleManager.itemHasActiveModule(stack, module.getDataName()))
					if (module.onBlockDestroyed(stack, world, block, x, y, z, (EntityPlayer) entity))
						return true;
		}
		return true;
	}

    
	public float getDamageVsEntity(Entity entity, ItemStack itemStack)
	{
		return (float) ModuleManager.computeModularProperty(itemStack, MeleeAssistModule.PUNCH_DAMAGE);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isFull3D()
	{
		return true;
	}

    
	@Override
	public int getItemEnchantability()
	{
		return 0;
	}

    
	@Override
	public String getToolMaterialName()
	{
		return this.toolMaterial.toString();
	}

    
	@Override
	public boolean getIsRepairable(ItemStack par1stack, ItemStack par2stack)
	{
		return false;
	}

    
	@Override
	public int getMaxItemUseDuration(ItemStack p_77626_1_)
	{
		return 72000;
	}

    
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		for (IRightClickModule module : ModuleManager.getRightClickModules())
			if (module.isValidForItem(itemStack) && ModuleManager.itemHasActiveModule(itemStack, module.getDataName()))
				module.onRightClick(player, world, itemStack);
		return itemStack;
	}

    
	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.bow;
	}

    
	@Override
	public void onPlayerStoppedUsing(ItemStack itemStack, World world, EntityPlayer player, int par4)
	{
		String mode = this.getActiveMode(itemStack, player);
		IPowerModule module = ModuleManager.getModule(mode);
		if (module != null)
			((IRightClickModule) module).onPlayerStoppedUsing(itemStack, world, player, par4);
	}

	public boolean shouldPassSneakingClickToBlock(World world, int x, int y, int z)
	{
		return true;
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		String mode = this.getActiveMode(itemStack, player);
    
		if (module instanceof IRightClickModule && EventUtils.cantBreak(player, x, y, z))
    

		return module instanceof IRightClickModule && ((IRightClickModule) module).onItemUseFirst(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ);
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		String mode = this.getActiveMode(itemStack, player);
		IPowerModule module = ModuleManager.getModule(mode);
		if (module instanceof IRightClickModule)
    
			if (EventUtils.cantBreak(player, x, y, z))
    

			return ((IRightClickModule) module).onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ);
		}
		return false;
	}

	public boolean canHarvestBlock(ItemStack stack, Block block, int meta, EntityPlayer player)
	{
		if (block.getMaterial().isToolNotRequired())
			return true;

		for (IBlockBreakingModule module : ModuleManager.getBlockBreakingModules())
			if (ModuleManager.itemHasActiveModule(stack, module.getDataName()) && module.canHarvestBlock(stack, block, meta, player))
				return true;
		return false;
	}

	@Optional.Method(modid = "Forestry")
	@Override
	public float getSaplingModifier(ItemStack itemStack, World world, EntityPlayer entityPlayer, int x, int y, int z)
	{
		if (ModuleManager.itemHasActiveModule(itemStack, GrafterModule.MODULE_GRAFTER))
		{
			ElectricItemUtils.drainPlayerEnergy(entityPlayer, ModuleManager.computeModularProperty(itemStack, GrafterModule.GRAFTER_ENERGY_CONSUMPTION));
			MuseHeatUtils.heatPlayer(entityPlayer, ModuleManager.computeModularProperty(itemStack, GrafterModule.GRAFTER_HEAT_GENERATION));
			return 100.0f;
		}
		return 0.0f;
	}

    
	@Override
	public boolean isUsable(ItemStack itemStack, EntityLivingBase entityLivingBase, int i, int i1, int i2)
	{
		return entityLivingBase instanceof EntityPlayer && this.getActiveMode(itemStack).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public void toolUsed(ItemStack itemStack, EntityLivingBase entityLivingBase, int i, int i1, int i2)
	{
	}

    
	@Override
	public boolean canWhack(EntityPlayer entityPlayer, ItemStack itemStack, int i, int i1, int i2)
	{
		return this.getActiveMode(itemStack, entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public boolean canLink(EntityPlayer entityPlayer, ItemStack itemStack, EntityMinecart entityMinecart)
	{
		return this.getActiveMode(itemStack, entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public boolean canBoost(EntityPlayer entityPlayer, ItemStack itemStack, EntityMinecart entityMinecart)
	{
		return this.getActiveMode(itemStack, entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public void onLink(EntityPlayer entityPlayer, ItemStack itemStack, EntityMinecart entityMinecart)
	{
	}

    
	@Override
	public void onWhack(EntityPlayer entityPlayer, ItemStack itemStack, int i, int i1, int i2)
	{
	}

    
	@Override
	public void onBoost(EntityPlayer entityPlayer, ItemStack itemStack, EntityMinecart entityMinecart)
	{
	}

    
	@Override
	public boolean canWrench(ItemStack itemStack, EntityPlayer entityPlayer, int i, int i1, int i2)
	{
		return this.getActiveMode(itemStack, entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public void wrenchUsed(EntityPlayer entityPlayer, int i, int i1, int i2)
	{
	}

    
	@Override
	public boolean canWrench(EntityPlayer entityPlayer, int i, int i1, int i2)
	{
		return this.getActiveMode(entityPlayer.getHeldItem(), entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public boolean damage(ItemStack itemStack, int i, EntityPlayer entityPlayer, boolean b)
	{
		return this.getActiveMode(itemStack, entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public void damageScrewdriver(EntityPlayer entityPlayer, ItemStack itemStack)
	{
	}

    
	@Override
	public boolean canUse(EntityPlayer entityPlayer, ItemStack itemStack)
	{
		return this.getActiveMode(itemStack, entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public void used(ItemStack itemStack, EntityPlayer entityPlayer, int i, int i1, int i2)
	{
	}

    
	@Override
	public boolean canUse(ItemStack itemStack, EntityPlayer entityPlayer, int i, int i1, int i2)
	{
		return this.getActiveMode(itemStack, entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public boolean shouldHideFacades(ItemStack itemStack, EntityPlayer entityPlayer)
	{
		return this.getActiveMode(itemStack, entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public boolean canUseWrench(EntityPlayer entityPlayer, int i, int i1, int i2)
	{
		return this.getActiveMode(entityPlayer.getHeldItem(), entityPlayer).equals(OmniWrenchModule.MODULE_OMNI_WRENCH);
	}

    
	@Override
	public void setActiveMode(ItemStack itemStack, String newMode)
	{
		NuminaItemUtils.getTagCompound(itemStack).setString("mode", newMode);
	}

	@Override
	public String getActiveMode(ItemStack itemStack, EntityPlayer player)
	{
		return this.getActiveMode(itemStack);
	}

	@Override
	public void cycleMode(ItemStack itemStack, EntityPlayer player, int dMode)
	{
		List<String> modes = this.getValidModes(itemStack, player);
		if (!modes.isEmpty())
		{
			int newindex = this.clampMode(modes.indexOf(this.getActiveMode(itemStack, player)) + dMode, modes.size());
			String newmode = modes.get(newindex);
			this.setActiveMode(itemStack, newmode);
			PacketSender.sendToServer(new MusePacketModeChangeRequest(player, newmode, player.inventory.currentItem));
		}
	}

	@Override
	public String nextMode(ItemStack itemStack, EntityPlayer player)
	{
		List<String> modes = this.getValidModes(itemStack, player);
		if (!modes.isEmpty())
		{
			int newindex = this.clampMode(modes.indexOf(this.getActiveMode(itemStack, player)) + 1, modes.size());
			return modes.get(newindex);
		}
		else
			return "";
	}

	@Override
	public String prevMode(ItemStack itemStack, EntityPlayer player)
	{
		List<String> modes = this.getValidModes(itemStack, player);
		if (!modes.isEmpty())
		{
			int newindex = this.clampMode(modes.indexOf(this.getActiveMode(itemStack, player)) - 1, modes.size());
			return modes.get(newindex);
		}
		else
			return "";
	}

	@Override
	public List<String> getValidModes(ItemStack itemStack, EntityPlayer player)
	{
		return this.getValidModes(itemStack);
	}

	private int clampMode(int selection, int modesSize)
	{
		if (selection > 0)
			return selection % modesSize;
		else
			return (selection + modesSize * -selection) % modesSize;
	}

    
	@Override
	public void cycleModeForItem(ItemStack itemStack, EntityPlayer player, int dMode)
	{
		if (itemStack != null)
			this.cycleMode(itemStack, player, dMode);
	}

	@Override
	public IIcon getModeIcon(String mode, ItemStack itemStack, EntityPlayer player)
	{
		if (!mode.isEmpty())
			return ModuleManager.getModule(mode).getIcon(itemStack);
		return null;
	}

	@Override
	public List<String> getValidModes(ItemStack itemStack)
	{
		return ModuleManager.getValidModes(itemStack);
	}

	@Override
	public String getActiveMode(ItemStack itemStack)
	{
		String modeFromNBT = NuminaItemUtils.getTagCompound(itemStack).getString("mode");
		if (!modeFromNBT.isEmpty())
			return modeFromNBT;
		else
		{
			List<String> validModes = this.getValidModes(itemStack);
			if (!validModes.isEmpty())
				return validModes.get(0);
			else
				return "";
		}
	}
}
