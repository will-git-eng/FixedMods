package powercrystals.minefactoryreloaded.block;

import ru.will.git.minefactoryreloaded.EventConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockSapling;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.terraingen.TerrainGen;
import powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetNoConnection;
import powercrystals.minefactoryreloaded.gui.MFRCreativeTab;
import powercrystals.minefactoryreloaded.world.MineFactoryReloadedWorldGen;
import powercrystals.minefactoryreloaded.world.WorldGenMassiveTree;
import powercrystals.minefactoryreloaded.world.WorldGenRubberTree;

import java.util.Locale;
import java.util.Random;

public class BlockRubberSapling extends BlockSapling implements IRedNetNoConnection
{
	private static WorldGenRubberTree treeGen = new WorldGenRubberTree(true);

	public BlockRubberSapling()
	{
		this.setHardness(0.0F);
		this.setStepSound(soundTypeGrass);
		this.setBlockName("mfr.rubberwood.sapling");
		this.setCreativeTab(MFRCreativeTab.tab);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister var1)
	{
		this.blockIcon = var1.registerIcon("minefactoryreloaded:" + this.getUnlocalizedName());
	}

	@Override
	public IIcon getIcon(int var1, int var2)
	{
		return this.blockIcon;
	}

	@Override
	public void func_149878_d(World world, int x, int y, int z, Random random)
	{
		if (!world.isRemote && TerrainGen.saplingGrowTree(world, random, x, y, z))
		{
			int meta = this.damageDropped(world.getBlockMetadata(x, y, z));
    
			if (!EventConfig.enableBigSaplings && meta >= 1 && meta <= 3)
    

			switch (meta)
			{
				case 0:
				default:
					BiomeGenBase biomeGenBase = world.getBiomeGenForCoords(x, z);
					if (biomeGenBase != null && biomeGenBase.biomeName.toLowerCase(Locale.US).contains("mega") && random.nextInt(50) == 0 && MineFactoryReloadedWorldGen.generateMegaRubberTree(world, random, x, y, z, true))
						return;

					if (treeGen.growTree(world, random, x, y, z))
						return;
					break;
				case 1:
					if (MineFactoryReloadedWorldGen.generateSacredSpringRubberTree(world, random, x, y, z))
						return;
					break;
				case 2:
					if (MineFactoryReloadedWorldGen.generateMegaRubberTree(world, random, x, y, z, true))
						return;
					break;
				case 3:
					if (new WorldGenMassiveTree().setSloped(true).generate(world, random, x, y, z))
						return;
			}
			world.setBlock(x, y, z, this, meta, 4);
		}
	}

	@Override
	public int damageDropped(int meta)
	{
		return meta & 7;
	}
}
