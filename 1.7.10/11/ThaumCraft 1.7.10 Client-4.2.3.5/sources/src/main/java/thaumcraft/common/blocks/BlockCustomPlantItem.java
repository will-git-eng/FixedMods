package thaumcraft.common.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.config.ConfigBlocks;

public class BlockCustomPlantItem extends ItemBlock
{
	public IIcon[] icon = new IIcon[6];

	public BlockCustomPlantItem(Block par1)
	{
		super(par1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.icon[0] = ir.registerIcon("thaumcraft:greatwoodsapling");
		this.icon[1] = ir.registerIcon("thaumcraft:silverwoodsapling");
		this.icon[2] = ir.registerIcon("thaumcraft:shimmerleaf");
		this.icon[3] = ir.registerIcon("thaumcraft:cinderpearl");
		this.icon[4] = ir.registerIcon("thaumcraft:purifier_seed");
		this.icon[5] = ir.registerIcon("thaumcraft:manashroom");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta)
    
    

		return this.icon[meta];
	}

	@Override
	public int getMetadata(int par1)
	{
		return par1;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack)
	{
		return this.getUnlocalizedName() + "." + par1ItemStack.getItemDamage();
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float p_77648_8_, float p_77648_9_, float p_77648_10_)
	{
		if (side != 1)
			return false;
		if (player.canPlayerEdit(x, y, z, side, stack) && player.canPlayerEdit(x, y + 1, z, side, stack))
		{
			if (world.getBlock(x, y, z).canSustainPlant(world, x, y, z, ForgeDirection.UP, new CustomPlantTypes(stack.getItemDamage())) && world.isAirBlock(x, y + 1, z))
			{
				world.setBlock(x, y + 1, z, ConfigBlocks.blockCustomPlant, stack.getItemDamage(), 3);
				world.playSoundEffect((double) ((float) x + 0.5F), (double) ((float) y + 1.5F), (double) ((float) z + 0.5F), ConfigBlocks.blockCustomPlant.stepSound.getStepResourcePath(), (ConfigBlocks.blockCustomPlant.stepSound.getVolume() + 1.0F) / 2.0F, ConfigBlocks.blockCustomPlant.stepSound.getPitch() * 0.8F);
				--stack.stackSize;
				return true;
			}
			return false;
		}
		return false;
	}

	private class CustomPlantTypes implements IPlantable
	{
		int md = 0;

		public CustomPlantTypes(int md)
		{
			this.md = md;
		}

		@Override
		public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z)
		{
			return this.md == 3 ? EnumPlantType.Desert : this.md != 4 && this.md != 5 ? EnumPlantType.Plains : EnumPlantType.Cave;
		}

		@Override
		public Block getPlant(IBlockAccess world, int x, int y, int z)
		{
			return ConfigBlocks.blockCustomPlant;
		}

		@Override
		public int getPlantMetadata(IBlockAccess world, int x, int y, int z)
		{
			return this.md;
		}
	}
}
