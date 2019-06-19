    
package thaumic.tinkerer.common.item.kami.tool;

import ru.will.git.ttinkerer.EventConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.ConfigItems;
import thaumic.tinkerer.client.core.handler.kami.ToolModeHUDHandler;
import thaumic.tinkerer.client.core.helper.IconHelper;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.block.kami.BlockBedrockPortal;
import thaumic.tinkerer.common.core.handler.ConfigHandler;
import thaumic.tinkerer.common.dim.WorldProviderBedrock;
import thaumic.tinkerer.common.item.kami.ItemKamiResource;
import thaumic.tinkerer.common.lib.LibItemNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ThaumicTinkererInfusionRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;
import thaumic.tinkerer.common.research.KamiResearchItem;
import thaumic.tinkerer.common.research.ResearchHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemIchorPickAdv extends ItemIchorPick implements IAdvancedTool
    
	private static final Material[] ALL_BLOCK_MATERIALS;

	static
	{
		List<Material> list = new ArrayList<Material>();
		list.add(Material.grass);
		list.add(Material.ground);
		list.add(Material.wood);
		list.add(Material.rock);
		list.add(Material.iron);
		list.add(Material.anvil);
		list.add(Material.sponge);
		list.add(Material.cloth);
		list.add(Material.sand);
		list.add(Material.circuits);
		list.add(Material.carpet);
		list.add(Material.glass);
		list.add(Material.tnt);
		list.add(Material.redstoneLight);
		list.add(Material.coral);
		list.add(Material.ice);
		list.add(Material.packedIce);
		list.add(Material.snow);
		list.add(Material.craftedSnow);
		list.add(Material.cactus);
		list.add(Material.clay);
		list.add(Material.web);
		ALL_BLOCK_MATERIALS = list.toArray(new Material[list.size()]);
    

	IIcon[] specialIcons = new IIcon[3];

	public ItemIchorPickAdv()
	{
		this.setHasSubtypes(true);
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{
		super.registerIcons(par1IconRegister);
		for (int i = 0; i < this.specialIcons.length; i++)
		{
			this.specialIcons[i] = IconHelper.forItem(par1IconRegister, this, i);
		}
	}

	@Override
	public IIcon getIconFromDamage(int par1)
	{
		return par1 >= this.specialIcons.length ? super.getIconFromDamage(par1) : this.specialIcons[par1];
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		if (par3EntityPlayer.isSneaking())
		{
			ToolHandler.changeMode(par1ItemStack);
			ToolModeHUDHandler.setTooltip(ToolHandler.getToolModeStr(this, par1ItemStack));
		}

		return par1ItemStack;
	}

	@Override
	public float func_150893_a(ItemStack p_150893_1_, Block p_150893_2_)
	{
		return p_150893_2_ == Blocks.bedrock ? Float.MAX_VALUE : super.func_150893_a(p_150893_1_, p_150893_2_);
	}

	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta)
	{
		return block == Blocks.bedrock ? Float.MAX_VALUE : super.getDigSpeed(stack, block, meta);
	}

	@Override
	public boolean func_150897_b(Block p_150897_1_)
	{
		return p_150897_1_ == Blocks.bedrock || super.func_150897_b(p_150897_1_);
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player)
	{
		World world = player.worldObj;
		Block blk = world.getBlock(x, y, z);
    
    
		Material[] materials = EventConfig.enableIchorPickAdvAllBreaking ? ALL_BLOCK_MATERIALS : ToolHandler.materialsPick;
    
		{
			return false;
		}

		MovingObjectPosition block = ToolHandler.raytraceFromEntity(world, player, true, 4.5);
		if (block == null)
    
		if (!EventConfig.enableIchorPickAdvBedrockBreaking && blk.getBlockHardness(world, x, y, z) < 0)
    

		ForgeDirection direction = ForgeDirection.getOrientation(block.sideHit);
		int fortune = EnchantmentHelper.getFortuneModifier(player);
		boolean silk = EnchantmentHelper.getSilkTouchModifier(player);
		if (ConfigHandler.bedrockDimensionID != 0 && blk == Blocks.bedrock && (world.provider.isSurfaceWorld() && y < 5 || y > 253 && world.provider instanceof WorldProviderBedrock))
			world.setBlock(x, y, z, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockBedrockPortal.class));
		if (ConfigHandler.bedrockDimensionID != 0 && blk == Blocks.bedrock && y <= 253 && world.provider instanceof WorldProviderBedrock)
			world.setBlock(x, y, z, Blocks.air);
		switch (ToolHandler.getMode(stack))
		{
			case 0:
				break;
			case 1:
				boolean doX = direction.offsetX == 0;
				boolean doY = direction.offsetY == 0;
    
    
    

				break;
			case 2:
				int xo = -direction.offsetX;
				int yo = -direction.offsetY;
    
    
    

				break;
		}
		return false;
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		par3List.add(ToolHandler.getToolModeStr(this, par1ItemStack));
	}

	@Override
	public String getType()
	{
		return "pick";
	}

	@Override
	public String getItemName()
	{
		return LibItemNames.ICHOR_PICK_GEM;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		if (!ConfigHandler.enableKami)
			return null;
		return (IRegisterableResearch) new KamiResearchItem(LibResearch.KEY_ICHOR_PICK_GEM, new AspectList().add(Aspect.FIRE, 2).add(Aspect.TOOL, 1).add(Aspect.MINE, 1).add(Aspect.EARTH, 1), 13, 15, 5, new ItemStack(this)).setParents(LibResearch.KEY_ICHOR_TOOLS).setPages(new ResearchPage("0"), ResearchHelper.infusionPage(LibResearch.KEY_ICHOR_PICK_GEM), new ResearchPage("1"));

	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		return new ThaumicTinkererInfusionRecipe(LibResearch.KEY_ICHOR_PICK_GEM, new ItemStack(this), 15, new AspectList().add(Aspect.FIRE, 50).add(Aspect.MINE, 64).add(Aspect.METAL, 32).add(Aspect.EARTH, 32).add(Aspect.HARVEST, 32).add(Aspect.GREED, 16).add(Aspect.SENSES, 16), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemIchorPick.class)), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemKamiResource.class), 1, 2), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemKamiResource.class)), new ItemStack(ConfigItems.itemPickElemental), new ItemStack(ConfigItems.itemFocusExcavation), new ItemStack(Blocks.tnt), new ItemStack(ConfigItems.itemNugget, 1, 21), new ItemStack(ConfigItems.itemNugget, 1, 16), new ItemStack(ConfigItems.itemNugget, 1, 31), new ItemStack(Items.diamond), new ItemStack(ConfigItems.itemFocusExcavation), new ItemStack(ConfigItems.itemPickElemental), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemKamiResource.class), 1, 1));

	}
}
