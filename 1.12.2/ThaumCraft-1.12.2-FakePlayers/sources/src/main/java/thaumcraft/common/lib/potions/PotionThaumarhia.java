package thaumcraft.common.lib.potions;

import ru.will.git.eventhelper.util.FastUtils;
import ru.will.git.thaumcraft.EventConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.blocks.BlocksTC;

public class PotionThaumarhia extends Potion
{
	public static Potion instance = null;
	private int statusIconIndex = -1;
	static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/misc/potions.png");

	public PotionThaumarhia(boolean par2, int par3)
	{
		super(par2, par3);
		this.setIconIndex(0, 0);
		this.setPotionName("potion.thaumarhia");
		this.setIconIndex(7, 2);
		this.setEffectiveness(0.25D);
	}

	@Override
	public boolean isBadEffect()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getStatusIconIndex()
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(rl);
		return super.getStatusIconIndex();
	}

	@Override
	public void performEffect(EntityLivingBase target, int par2)
	{
		
		if (!EventConfig.potionThaumarhia)
		{
			FastUtils.stopPotionEffect(target, this);
			return;
		}
		

		if (!target.world.isRemote && target.world.rand.nextInt(15) == 0 && target.world.isAirBlock(new BlockPos(target)))
			target.world.setBlockState(new BlockPos(target), BlocksTC.fluxGoo.getDefaultState());
	}

	@Override
	public boolean isReady(int par1, int par2)
	{
		return par1 % 20 == 0;
	}
}
