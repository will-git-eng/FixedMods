package thaumcraft.common.lib.potions;

import ru.will.git.eventhelper.util.FastUtils;
import ru.will.git.thaumcraft.EventConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionUnnaturalHunger extends Potion
{
	public static Potion instance = null;
	private int statusIconIndex = -1;
	static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/misc/potions.png");

	public PotionUnnaturalHunger(boolean par2, int par3)
	{
		super(par2, par3);
		this.setIconIndex(0, 0);
		this.setPotionName("potion.unhunger");
		this.setIconIndex(7, 1);
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
		
		if (!EventConfig.potionUnhunger)
		{
			FastUtils.stopPotionEffect(target, this);
			return;
		}
		

		if (!target.world.isRemote && target instanceof EntityPlayer)
			((EntityPlayer) target).addExhaustion(0.025F * (float) (par2 + 1));
	}

	@Override
	public boolean isReady(int par1, int par2)
	{
		return true;
	}
}
