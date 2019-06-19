package exnihilo.items;

import java.util.Set;

import ru.will.git.reflectionmedic.util.EventUtils;
import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import exnihilo.data.ModData;
import exnihilo.proxies.Proxy;
import exnihilo.utils.CrookUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemCrook extends ItemTool
{
	public static final double pullingForce = 1.5D;
	public static final double pushingForce = 1.5D;
	public static final Set blocksEffectiveAgainst = Sets.newHashSet(new Block[0]);

	public ItemCrook()
	{
		super(0.0F, ToolMaterial.WOOD, blocksEffectiveAgainst);
		this.setMaxDamage(this.getMaxDamage() * 2);
	}

	public ItemCrook(ToolMaterial mat)
	{
		super(0.0F, mat, blocksEffectiveAgainst);
	}

	@Override
	public boolean func_150897_b(Block block)
	{
		return block.isLeaves(Proxy.getProxy().getWorld(), 0, 0, 0);
	}

	@Override
	public float func_150893_a(ItemStack item, Block block)
	{
		return block.isLeaves(Proxy.getProxy().getWorld(), 0, 0, 0) ? this.efficiencyOnProperMaterial + 1.0F : 1.0F;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack item, int X, int Y, int Z, EntityPlayer player)
	{
		CrookUtils.doCrooking(item, X, Y, Z, player);
		return false;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack item, EntityPlayer player, Entity entity)
	{
		if (!player.worldObj.isRemote)
    
			int x = MathHelper.floor_double(entity.posX);
			int y = MathHelper.floor_double(entity.posY);
			int z = MathHelper.floor_double(entity.posZ);
			if (EventUtils.cantBreak(player, x, y, z))
    

			double distance = Math.sqrt(Math.pow(player.posX - entity.posX, 2.0D) + Math.pow(player.posZ - entity.posZ, 2.0D));
			double scalarX = (player.posX - entity.posX) / distance;
			double scalarZ = (player.posZ - entity.posZ) / distance;
			double velX = 0.0D - scalarX * 1.5D;
			double velZ = 0.0D - scalarZ * 1.5D;
			double velY = 0.0D;
			entity.addVelocity(velX, velY, velZ);
		}

		item.damageItem(1, player);
		return true;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack item, EntityPlayer player, EntityLivingBase entity)
    
		int x = MathHelper.floor_double(entity.posX);
		int y = MathHelper.floor_double(entity.posY);
		int z = MathHelper.floor_double(entity.posZ);
		if (EventUtils.cantBreak(player, x, y, z))
    

		double distance = Math.sqrt(Math.pow(player.posX - entity.posX, 2.0D) + Math.pow(player.posZ - entity.posZ, 2.0D));
		double scalarX = (player.posX - entity.posX) / distance;
		double scalarZ = (player.posZ - entity.posZ) / distance;
		double velX = scalarX * 1.5D;
		double velZ = scalarZ * 1.5D;
		double velY = 0.0D;
		entity.addVelocity(velX, velY, velZ);
		item.damageItem(1, player);
		return true;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote && world.getBlock(x, y, z) == Blocks.dirt && world.getBlock(x, y + 1, z) == Blocks.water && ForgeDirection.getOrientation(side) == ForgeDirection.UP && ModData.LILYPAD_CHANCE > 0)
		{
			if (world.rand.nextInt(ModData.LILYPAD_CHANCE) == 0)
			{
				EntityItem item = new EntityItem(world, x, y + 1, z, new ItemStack(Blocks.waterlily));
				world.spawnEntityInWorld(item);
			}

			stack.damageItem(1, player);
			return true;
		}
		else
			return false;
	}

	@Override
	public String getUnlocalizedName()
	{
		return "exnihilo.crook";
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "exnihilo.crook";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register)
	{
		this.itemIcon = register.registerIcon("exnihilo:Crook");
	}
}
