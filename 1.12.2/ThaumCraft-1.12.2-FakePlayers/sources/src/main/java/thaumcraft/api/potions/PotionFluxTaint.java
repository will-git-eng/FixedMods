package thaumcraft.api.potions;

import ru.will.git.eventhelper.util.FastUtils;
import ru.will.git.thaumcraft.EventConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.damagesource.DamageSourceThaumcraft;
import thaumcraft.api.entities.ITaintedMob;

public class PotionFluxTaint extends Potion
{
	public static Potion instance = null;
	private int statusIconIndex = -1;
	static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/misc/potions.png");

	public PotionFluxTaint(boolean par2, int par3)
	{
		super(par2, par3);
		this.setIconIndex(3, 1);
		this.setEffectiveness(0.25D);
		this.setPotionName("potion.flux_taint");
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
	public void performEffect(EntityLivingBase target, int strength)
	{
		
		if (!EventConfig.potionFluxTaint)
		{
			FastUtils.stopPotionEffect(target, this);
			return;
		}
		

		IAttributeInstance cai = target.getEntityAttribute(ThaumcraftApiHelper.CHAMPION_MOD);
		if (!(target instanceof ITaintedMob) && (cai == null || (int) cai.getAttributeValue() != 13))
		{
			if (!target.isEntityUndead() && !(target instanceof EntityPlayer))
				target.attackEntityFrom(DamageSourceThaumcraft.taint, 1.0F);
			else if (!target.isEntityUndead() && (target.getMaxHealth() > 1.0F || target instanceof EntityPlayer))
				target.attackEntityFrom(DamageSourceThaumcraft.taint, 1.0F);
		}
		else
			target.heal(1.0F);

	}

	@Override
	public boolean isReady(int par1, int par2)
	{
		int k = 40 >> par2;
		return k <= 0 || par1 % k == 0;
	}
}
