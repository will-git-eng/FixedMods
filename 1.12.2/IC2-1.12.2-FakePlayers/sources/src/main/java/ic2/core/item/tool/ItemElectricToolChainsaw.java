package ic2.core.item.tool;

import ru.will.git.eventhelper.util.EventUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;

public class ItemElectricToolChainsaw extends ItemElectricTool implements IHitSoundOverride
{
	public ItemElectricToolChainsaw()
	{
		super(ItemName.chainsaw, 100, ItemElectricTool.HarvestLevel.Iron, EnumSet.of(ItemElectricTool.ToolClass.Axe, ItemElectricTool.ToolClass.Sword, ItemElectricTool.ToolClass.Shears));
		this.maxCharge = 30000;
		this.transferLimit = 100;
		this.tier = 1;
		this.efficiency = 12.0F;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		if (world.isRemote)
			return super.onItemRightClick(world, player, hand);
		if (IC2.keyboard.isModeSwitchKeyDown(player))
		{
			NBTTagCompound compoundTag = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
			if (compoundTag.getBoolean("disableShear"))
			{
				compoundTag.setBoolean("disableShear", false);
				IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.normal");
			}
			else
			{
				compoundTag.setBoolean("disableShear", true);
				IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.noShear");
			}
		}

		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
	{
		if (slot != EntityEquipmentSlot.MAINHAND)
			return super.getAttributeModifiers(slot, stack);
		Multimap<String, AttributeModifier> ret = HashMultimap.create();
		if (ElectricItem.manager.canUse(stack, this.operationEnergyCost))
		{
			ret.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.attackSpeed, 0));
			ret.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, "Tool modifier", 9.0D, 0));
		}

		return ret;
	}

	@Override
	public boolean hitEntity(ItemStack itemstack, EntityLivingBase entityliving, EntityLivingBase attacker)
	{
		ElectricItem.manager.use(itemstack, this.operationEnergyCost, attacker);
		if (attacker instanceof EntityPlayer && entityliving instanceof EntityCreeper && entityliving.getHealth() <= 0.0F)
			IC2.achievements.issueAchievement((EntityPlayer) attacker, "killCreeperChainsaw");

		return true;
	}

	@SubscribeEvent
	public void onEntityInteract(EntityInteract event)
	{
		if (IC2.platform.isSimulating())
		{
			Entity entity = event.getTarget();
			EntityPlayer player = event.getEntityPlayer();
			ItemStack itemstack = player.inventory.getStackInSlot(player.inventory.currentItem);
			if (itemstack != null && itemstack.getItem() == this && entity instanceof IShearable && !StackUtil.getOrCreateNbtData(itemstack).getBoolean("disableShear") && ElectricItem.manager.use(itemstack, this.operationEnergyCost, player))
			{
				IShearable target = (IShearable) entity;
				World world = entity.getEntityWorld();
				BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
				if (target.isShearable(itemstack, world, pos))
				{
					
					if (EventUtils.cantAttack(player, entity))
						return;
					

					for (ItemStack stack : target.onSheared(itemstack, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack)))
					{
						EntityItem ent = entity.entityDropItem(stack, 1.0F);
						ent.motionY += itemRand.nextFloat() * 0.05F;
						ent.motionX += (itemRand.nextFloat() - itemRand.nextFloat()) * 0.1F;
						ent.motionZ += (itemRand.nextFloat() - itemRand.nextFloat()) * 0.1F;
					}
				}
			}
		}
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player)
	{
		if (!IC2.platform.isSimulating())
			return false;
		if (StackUtil.getOrCreateNbtData(itemstack).getBoolean("disableShear"))
			return false;
		World world = player.getEntityWorld();
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof IShearable)
		{
			IShearable target = (IShearable) block;
			if (target.isShearable(itemstack, world, pos) && ElectricItem.manager.use(itemstack, this.operationEnergyCost, player))
			{
				for (ItemStack stack : target.onSheared(itemstack, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack)))
				{
					StackUtil.dropAsEntity(world, pos, stack);
				}

				player.addStat(StatList.getBlockStats(block), 1);
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
				return true;
			}
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getHitSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack)
	{
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getBreakSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack)
	{
		return null;
	}

	@Override
	protected String getIdleSound(EntityLivingBase player, ItemStack stack)
	{
		return "Tools/Chainsaw/ChainsawIdle.ogg";
	}

	@Override
	protected String getStopSound(EntityLivingBase player, ItemStack stack)
	{
		return "Tools/Chainsaw/ChainsawStop.ogg";
	}
}
