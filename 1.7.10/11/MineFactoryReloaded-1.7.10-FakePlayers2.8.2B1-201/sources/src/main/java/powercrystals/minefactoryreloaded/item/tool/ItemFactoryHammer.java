package powercrystals.minefactoryreloaded.item.tool;

import cofh.api.block.IDismantleable;
import cofh.api.item.IToolHammer;
import cofh.asm.relauncher.Implementable;
import cofh.lib.util.helpers.BlockHelper;
import ru.will.git.reflectionmedic.util.EventUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import powercrystals.minefactoryreloaded.api.IMFRHammer;
import powercrystals.minefactoryreloaded.item.base.ItemFactoryTool;
import powercrystals.minefactoryreloaded.setup.Machine;

import java.util.Random;

@Implementable({ "buildcraft.api.tools.IToolWrench" })
public class ItemFactoryHammer extends ItemFactoryTool implements IMFRHammer, IToolHammer
{
	public ItemFactoryHammer()
	{
		this.setHarvestLevel("wrench", 1);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float var8, float var9, float var10)
	{
		Block block = world.getBlock(x, y, z);
		if (block != null)
    
			if (EventUtils.cantBreak(player, x, y, z))
    

			PlayerInteractEvent var12 = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, x, y, z, side, world);
			if (MinecraftForge.EVENT_BUS.post(var12) || var12.getResult() == Result.DENY || var12.useBlock == Result.DENY || var12.useItem == Result.DENY)
				return false;

			if (player.isSneaking() && block instanceof IDismantleable && ((IDismantleable) block).canDismantle(player, world, x, y, z))
			{
				if (!world.isRemote)
					((IDismantleable) block).dismantleBlock(player, world, x, y, z, false);

				player.swingItem();
				return !world.isRemote;
			}

			if (BlockHelper.canRotate(block))
			{
				player.swingItem();
				if (player.isSneaking())
				{
					world.setBlockMetadataWithNotify(x, y, z, BlockHelper.rotateVanillaBlockAlt(world, block, x, y, z), 3);
					world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, block.stepSound.getBreakSound(), 1.0F, 0.6F);
				}
				else
				{
					world.setBlockMetadataWithNotify(x, y, z, BlockHelper.rotateVanillaBlock(world, block, x, y, z), 3);
					world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, block.stepSound.getBreakSound(), 1.0F, 0.8F);
				}

				return !world.isRemote;
			}

			if (!player.isSneaking() && block.rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side)))
			{
				player.swingItem();
				world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, block.stepSound.getBreakSound(), 1.0F, 0.8F);
				return !world.isRemote;
			}
		}

		return false;
	}

	@Override
	public boolean isUsable(ItemStack var1, EntityLivingBase var2, int var3, int var4, int var5)
	{
		return true;
	}

	@Override
	public void toolUsed(ItemStack var1, EntityLivingBase var2, int var3, int var4, int var5)
	{
	}

	public boolean canWrench(EntityPlayer var1, int var2, int var3, int var4)
	{
		return true;
	}

	public void wrenchUsed(EntityPlayer var1, int var2, int var3, int var4)
	{
	}

	@Override
	public boolean doesSneakBypassUse(World var1, int var2, int var3, int var4, EntityPlayer var5)
	{
		return true;
	}

	@Override
	public boolean canHarvestBlock(Block var1, ItemStack var2)
	{
		if (var1 == null)
			return false;
		Material var3 = var1.getMaterial();
		return var3 == Material.ice | var3 == Material.cake | var3 == Material.iron | var3 == Material.rock | var3 == Material.wood | var3 == Material.gourd | var3 == Material.anvil | var3 == Material.glass | var3 == Material.piston | var3 == Material.plants | var3 == Machine.MATERIAL | var3 == Material.circuits | var3 == Material.packedIce;
	}

	@Override
	public float func_150893_a(ItemStack var1, Block var2)
	{
		if (var2 == null)
			return 0.0F;
		Material var3 = var2.getMaterial();
		return var3 == Material.ice | var3 == Material.cake | var3 == Material.gourd | var3 == Material.glass ? 15.0F : this.canHarvestBlock(var2, var1) ? 1.35F : 0.15F;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack var1, int var2, int var3, int var4, EntityPlayer var5)
	{
		Block var6 = var5.worldObj.getBlock(var2, var3, var4);
		if (var6.getBlockHardness(var5.worldObj, var2, var3, var4) <= 2.9F)
			return false;
		Random var7 = var5.getRNG();
		var5.playSound("random.break", 0.8F + var7.nextFloat() * 0.4F, 0.4F);
		int var8 = 0;

		for (int var9 = 10 + var7.nextInt(5); var8 < var9; ++var8)
		{
			Vec3 var10 = Vec3.createVectorHelper(((double) var7.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
			var10.rotateAroundX(-var5.rotationPitch * 3.1415927F / 180.0F);
			var10.rotateAroundY(-var5.rotationYaw * 3.1415927F / 180.0F);
			Vec3 var11 = Vec3.createVectorHelper(((double) var7.nextFloat() - 0.5D) * 0.3D, (double) var7.nextFloat(), 0.6D);
			var11.rotateAroundX(-var5.rotationPitch * 3.1415927F / 180.0F);
			var11.rotateAroundY(-var5.rotationYaw * 3.1415927F / 180.0F);
			var11 = var11.addVector(var5.posX, var5.posY + (double) var5.getEyeHeight(), var5.posZ);
			var5.worldObj.spawnParticle("tilecrack_51_0", var11.xCoord, var11.yCoord, var11.zCoord, var10.xCoord, var10.yCoord + 0.05D, var10.zCoord);
		}

		return true;
	}

	@Override
	protected int getWeaponDamage(ItemStack var1)
	{
		return 4;
	}
}
