package thaumcraft.common.lib.potions;

import ru.will.git.eventhelper.util.FastUtils;
import ru.will.git.thaumcraft.EventConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.potions.PotionVisExhaust;

import java.util.List;

public class PotionInfectiousVisExhaust extends Potion
{
	public static Potion instance = null;
	private int statusIconIndex = -1;
	static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/misc/potions.png");

	public PotionInfectiousVisExhaust(boolean par2, int par3)
	{
		super(par2, par3);
		this.setIconIndex(0, 0);
		this.setPotionName("potion.infvisexhaust");
		this.setIconIndex(6, 1);
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
	public void performEffect(EntityLivingBase target, int amplifier)
	{
		
		if (!EventConfig.potionInfectiousVisExhaust)
		{
			FastUtils.stopPotionEffect(target, this);
			return;
		}
		

		List<EntityLivingBase> targets = target.world.getEntitiesWithinAABB(EntityLivingBase.class, target.getEntityBoundingBox().grow(4.0D, 4.0D, 4.0D));
		if (targets.size() > 0)
			for (EntityLivingBase e : targets)
			{
				if (!e.isPotionActive(instance))
					if (amplifier > 0)
						e.addPotionEffect(new PotionEffect(instance, 6000, amplifier - 1, false, true));
						
					else if (EventConfig.potionVisExhaust)
						e.addPotionEffect(new PotionEffect(PotionVisExhaust.instance, 6000, 0, false, true));
			}

	}

	@Override
	public boolean isReady(int par1, int par2)
	{
		return par1 % 40 == 0;
	}
}
