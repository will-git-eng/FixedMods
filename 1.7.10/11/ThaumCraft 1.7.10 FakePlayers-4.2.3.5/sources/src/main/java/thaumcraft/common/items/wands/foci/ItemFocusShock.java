package thaumcraft.common.items.wands.foci;

import ru.will.git.reflectionmedic.util.EventUtils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.fx.bolt.FXLightningBolt;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.projectile.EntityShockOrb;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXZap;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.EntityUtils;

import java.util.ArrayList;

public class ItemFocusShock extends ItemFocusBasic
{
	private static final AspectList costBase = new AspectList().add(Aspect.AIR, 25);
	private static final AspectList costChain = new AspectList().add(Aspect.AIR, 40).add(Aspect.WATER, 10);
	private static final AspectList costGround = new AspectList().add(Aspect.AIR, 75).add(Aspect.EARTH, 25);
	public static FocusUpgradeType chainlightning = new FocusUpgradeType(17, new ResourceLocation("thaumcraft", "textures/foci/chainlightning.png"), "focus.upgrade.chainlightning.name", "focus.upgrade.chainlightning.text", new AspectList().add(Aspect.WEATHER, 1));
	public static FocusUpgradeType earthshock = new FocusUpgradeType(18, new ResourceLocation("thaumcraft", "textures/foci/earthshock.png"), "focus.upgrade.earthshock.name", "focus.upgrade.earthshock.text", new AspectList().add(Aspect.WEATHER, 1));

	public ItemFocusShock()
	{
		this.setCreativeTab(Thaumcraft.tabTC);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir)
	{
		this.icon = ir.registerIcon("thaumcraft:focus_shock");
	}

	@Override
	public String getSortingHelper(ItemStack itemstack)
	{
		return "BL" + super.getSortingHelper(itemstack);
	}

	@Override
	public int getFocusColor(ItemStack itemstack)
	{
		return 10466239;
	}

	@Override
	public AspectList getVisCost(ItemStack itemstack)
	{
		return this.isUpgradedWith(itemstack, chainlightning) ? costChain : this.isUpgradedWith(itemstack, earthshock) ? costGround : costBase;
	}

	@Override
	public int getActivationCooldown(ItemStack focusstack)
	{
		return this.isUpgradedWith(focusstack, chainlightning) ? 500 : this.isUpgradedWith(focusstack, earthshock) ? 1000 : 250;
	}

	@Override
	public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack itemstack)
	{
		return this.isUpgradedWith(itemstack, earthshock) ? ItemFocusBasic.WandFocusAnimation.WAVE : ItemFocusBasic.WandFocusAnimation.CHARGE;
	}

	public static void shootLightning(World world, EntityLivingBase entityplayer, double xx, double yy, double zz, boolean offset)
	{
		double px = entityplayer.posX;
		double py = entityplayer.posY;
		double pz = entityplayer.posZ;
		if (entityplayer.getEntityId() != FMLClientHandler.instance().getClient().thePlayer.getEntityId())
			py = entityplayer.boundingBox.minY + entityplayer.height / 2.0F + 0.25D;

		px = px + -MathHelper.cos(entityplayer.rotationYaw / 180.0F * 3.141593F) * 0.06F;
		py = py + -0.05999999865889549D;
		pz = pz + -MathHelper.sin(entityplayer.rotationYaw / 180.0F * 3.141593F) * 0.06F;
		if (entityplayer.getEntityId() != FMLClientHandler.instance().getClient().thePlayer.getEntityId())
			py = entityplayer.boundingBox.minY + entityplayer.height / 2.0F + 0.25D;

		Vec3 vec3d = entityplayer.getLook(1.0F);
		px = px + vec3d.xCoord * 0.3D;
		py = py + vec3d.yCoord * 0.3D;
		pz = pz + vec3d.zCoord * 0.3D;
		FXLightningBolt bolt = new FXLightningBolt(world, px, py, pz, xx, yy, zz, world.rand.nextLong(), 6, 0.5F, 8);
		bolt.defaultFractal();
		bolt.setType(2);
		bolt.setWidth(0.125F);
		bolt.finalizeBolt();
	}

	@Override
	public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer p, MovingObjectPosition movingobjectposition)
	{
		ItemWandCasting wand = (ItemWandCasting) itemstack.getItem();
		if (this.isUpgradedWith(wand.getFocusItem(itemstack), earthshock))
		{
			if (wand.consumeAllVis(itemstack, p, this.getVisCost(itemstack), !p.worldObj.isRemote, false))
			{
				if (!world.isRemote)
				{
					EntityShockOrb orb = new EntityShockOrb(world, p);
					orb.area += this.getUpgradeLevel(wand.getFocusItem(itemstack), FocusUpgradeType.enlarge) * 2;
					orb.damage = (int) (orb.damage + wand.getFocusPotency(itemstack) * 1.33D);
					world.spawnEntityInWorld(orb);
					world.playSoundAtEntity(orb, "thaumcraft:zap", 1.0F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F);
				}

				p.swingItem();
			}
		}
		else
		{
			p.setItemInUse(itemstack, Integer.MAX_VALUE);
			WandManager.setCooldown(p, -1);
		}

		return itemstack;
	}

	@Override
	public void onUsingFocusTick(ItemStack stack, EntityPlayer p, int count)
	{
		this.doLightningBolt(stack, p, count);
	}

	public void doLightningBolt(ItemStack stack, EntityPlayer p, int count)
	{
		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
		if (!wand.consumeAllVis(stack, p, this.getVisCost(stack), !p.worldObj.isRemote, false))
			p.stopUsingItem();
		else
		{
			int potency = wand.getFocusPotency(stack);
			Entity pointedEntity = EntityUtils.getPointedEntity(p.worldObj, p, 0.0D, 20.0D, 1.1F);
			if (p.worldObj.isRemote)
			{
				MovingObjectPosition mop = BlockUtils.getTargetBlock(p.worldObj, p, false);
				Vec3 v = p.getLook(2.0F);
				double px = p.posX + v.xCoord * 10.0D;
				double py = p.posY + v.yCoord * 10.0D;
				double pz = p.posZ + v.zCoord * 10.0D;
				if (mop != null)
				{
					px = mop.hitVec.xCoord;
					py = mop.hitVec.yCoord;
					pz = mop.hitVec.zCoord;

					for (int a = 0; a < 5; ++a)
					{
						Thaumcraft.proxy.sparkle((float) px + (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.3F, (float) py + (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.3F, (float) pz + (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.3F, 2.0F + p.worldObj.rand.nextFloat(), 2, 0.05F + p.worldObj.rand.nextFloat() * 0.05F);
					}
				}

				if (pointedEntity != null)
				{
					px = pointedEntity.posX;
					py = pointedEntity.boundingBox.minY + pointedEntity.height / 2.0F;
					pz = pointedEntity.posZ;

					for (int a = 0; a < 5; ++a)
					{
						Thaumcraft.proxy.sparkle((float) px + (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.6F, (float) py + (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.6F, (float) pz + (p.worldObj.rand.nextFloat() - p.worldObj.rand.nextFloat()) * 0.6F, 2.0F + p.worldObj.rand.nextFloat(), 2, 0.05F + p.worldObj.rand.nextFloat() * 0.05F);
					}
				}

				shootLightning(p.worldObj, p, px, py, pz, true);
			}
			else
			{
    
				if (pointedEntity instanceof EntityLivingBase && !EventUtils.cantDamage(p, pointedEntity))
				{
					int cl = this.getUpgradeLevel(wand.getFocusItem(stack), chainlightning) * 2;
					pointedEntity.attackEntityFrom(DamageSource.causePlayerDamage(p), (cl > 0 ? 6 : 4) + potency);
					if (cl > 0)
					{
						cl = cl + this.getUpgradeLevel(wand.getFocusItem(stack), FocusUpgradeType.enlarge) * 2;
						EntityLivingBase center = (EntityLivingBase) pointedEntity;
						ArrayList<Integer> targets = new ArrayList();
						targets.add(pointedEntity.getEntityId());

						while (cl > 0)
						{
							--cl;
							ArrayList<Entity> list = EntityUtils.getEntitiesInRange(p.worldObj, center.posX, center.posY, center.posZ, p, EntityLivingBase.class, 8.0D);
							double d = Double.MAX_VALUE;
							Entity closest = null;

							for (Entity e : list)
							{
								if (!targets.contains(e.getEntityId()) && (!(e instanceof EntityPlayer) || MinecraftServer.getServer().isPVPEnabled()))
								{
									double dd = e.getDistanceSqToEntity(center);
									if (dd < d)
									{
										closest = e;
										d = dd;
									}
								}
							}

							if (closest != null)
							{
								PacketHandler.INSTANCE.sendToAllAround(new PacketFXZap(center.getEntityId(), closest.getEntityId()), new TargetPoint(p.worldObj.provider.dimensionId, center.posX, center.posY, center.posZ, 64.0D));
    
    
									closest.attackEntityFrom(DamageSource.causePlayerDamage(p), 4 + potency);

								center = (EntityLivingBase) closest;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean canApplyUpgrade(ItemStack focusstack, EntityPlayer player, FocusUpgradeType type, int rank)
	{
		return !type.equals(FocusUpgradeType.enlarge) || this.isUpgradedWith(focusstack, chainlightning) || this.isUpgradedWith(focusstack, earthshock);
	}

	@Override
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank)
	{
		switch (rank)
		{
			case 1:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal, FocusUpgradeType.potency };
			case 2:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal, FocusUpgradeType.potency };
			case 3:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal, FocusUpgradeType.potency, chainlightning, earthshock };
			case 4:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge };
			case 5:
				return new FocusUpgradeType[] { FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge };
			default:
				return null;
		}
	}
}
