package thaumcraft.common.lib.potions;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.thaumcraft.EventConfig;
import ru.will.git.thaumcraft.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.common.config.ConfigBlocks;

public class PotionThaumarhia extends Potion
{
	public static PotionThaumarhia instance = null;
	private int statusIconIndex = -1;
	static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/misc/potions.png");

	public PotionThaumarhia(int par1, boolean par2, int par3)
	{
		super(par1, par2, par3);
		this.setIconIndex(0, 0);
	}

	public static void init()
	{
		instance.setPotionName("potion.thaumarhia");
		instance.setIconIndex(7, 2);
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
    
		if (!EventConfig.enableThaumarhia)
		{
			ModUtils.stopPotionEffect(target, this);
			return;
    

		if (!target.worldObj.isRemote && target.worldObj.rand.nextInt(15) == 0)
		{
			int x = MathHelper.floor_double(target.posX);
			int y = MathHelper.floor_double(target.posY);
			int z = MathHelper.floor_double(target.posZ);
			if (target.worldObj.isAirBlock(x, y, z))
    
				if (target instanceof EntityPlayer && EventUtils.cantBreak((EntityPlayer) target, x, y, z))
    

				target.worldObj.setBlock(x, y, z, ConfigBlocks.blockFluxGoo);
			}
		}

	}

	@Override
	public boolean isReady(int par1, int par2)
	{
		return par1 % 20 == 0;
	}
}
