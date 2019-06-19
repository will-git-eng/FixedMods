package thaumcraft.common.lib.potions;

import ru.will.git.thaumcraft.EventConfig;
import ru.will.git.thaumcraft.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class PotionUnnaturalHunger extends Potion
{
	public static PotionUnnaturalHunger instance = null;
	private int statusIconIndex = -1;
	static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/misc/potions.png");

	public PotionUnnaturalHunger(int par1, boolean par2, int par3)
	{
		super(par1, par2, par3);
		this.setIconIndex(0, 0);
	}

	public static void init()
	{
		instance.setPotionName("potion.unhunger");
		instance.setIconIndex(7, 1);
		instance.setEffectiveness(0.25D);
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
    
		if (!EventConfig.enableUnhunger)
		{
			ModUtils.stopPotionEffect(target, this);
			return;
    

		if (!target.worldObj.isRemote && target instanceof EntityPlayer)
			((EntityPlayer) target).addExhaustion(0.025F * (float) (par2 + 1));
	}

	@Override
	public boolean isReady(int par1, int par2)
	{
		return true;
	}
}
