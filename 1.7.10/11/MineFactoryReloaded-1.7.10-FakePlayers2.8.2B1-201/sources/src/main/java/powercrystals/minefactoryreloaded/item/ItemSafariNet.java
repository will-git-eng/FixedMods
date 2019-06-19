package powercrystals.minefactoryreloaded.item;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.minefactoryreloaded.EventConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.IMobEggHandler;
import powercrystals.minefactoryreloaded.api.IRandomMobProvider;
import powercrystals.minefactoryreloaded.api.ISafariNetHandler;
import powercrystals.minefactoryreloaded.api.RandomMob;
import powercrystals.minefactoryreloaded.core.MFRUtil;
import powercrystals.minefactoryreloaded.core.UtilInventory;
import powercrystals.minefactoryreloaded.item.base.ItemFactory;
import powercrystals.minefactoryreloaded.setup.MFRThings;
import powercrystals.minefactoryreloaded.setup.village.VillageTradeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemSafariNet extends ItemFactory
{
	private IIcon _iconEmpty;
	private IIcon _iconBack;
	private IIcon _iconMid;
	private IIcon _iconFront;
	private final boolean multiuse;
	private final int type;
	private Random colorRand;

	public ItemSafariNet(int var1)
	{
		this(var1, false);
	}

	public ItemSafariNet(int var1, boolean var2)
	{
		this.colorRand = new Random();
		this.multiuse = var2;
		this.type = var1;
		this.setMaxStackSize(var2 ? 12 : 1);
	}

	@Override
	public int getItemStackLimit(ItemStack var1)
	{
		return isSingleUse(var1) && isEmpty(var1) ? this.maxStackSize : 1;
	}

	@Override
	public void addInfo(ItemStack var1, EntityPlayer var2, List<String> var3, boolean var4)
	{
		super.addInfo(var1, var2, var3, var4);
		int var5 = ((ItemSafariNet) var1.getItem()).type;
		if (1 == (var5 & 1))
			var3.add(StatCollector.translateToLocal("tip.info.mfr.safarinet.persistent"));

		if (2 == (var5 & 2))
			var3.add(StatCollector.translateToLocal("tip.info.mfr.safarinet.nametag"));

		if (var1.getTagCompound() != null)
			if (var1.getTagCompound().getBoolean("hide"))
				var3.add(StatCollector.translateToLocal("tip.info.mfr.safarinet.mystery"));
			else
			{
				var3.add(MFRUtil.localize("entity.", var1.getTagCompound().getString("id")));
				Class var6 = (Class) EntityList.stringToClassMapping.get(var1.getTagCompound().getString("id"));
				if (var6 == null)
					return;

				for (ISafariNetHandler var8 : MFRRegistry.getSafariNetHandlers())
				{
					if (var8.validFor().isAssignableFrom(var6))
						var8.addInformation(var1, var2, var3, var4);
				}
			}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack var1, int var2)
	{
		return isEmpty(var1) ? this._iconEmpty : var2 == 0 ? this._iconBack : var2 == 1 ? this._iconMid : var2 == 2 ? this._iconFront : null;
	}

	@Override
	public void registerIcons(IIconRegister var1)
	{
		this._iconEmpty = var1.registerIcon("minefactoryreloaded:" + this.getUnlocalizedName() + ".empty");
		this._iconBack = var1.registerIcon("minefactoryreloaded:" + this.getUnlocalizedName() + ".back");
		this._iconMid = var1.registerIcon("minefactoryreloaded:" + this.getUnlocalizedName() + ".mid");
		this._iconFront = var1.registerIcon("minefactoryreloaded:" + this.getUnlocalizedName() + ".front");
		this.itemIcon = this._iconEmpty;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	public int getRenderPasses(int var1)
	{
		return 3;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack var1, int var2)
	{
		if (var1.getItemDamage() == 0 && var1.getTagCompound() == null)
			return 16777215;
		else if (var1.getTagCompound() != null && var1.getTagCompound().getBoolean("hide"))
		{
			WorldClient var4 = Minecraft.getMinecraft().theWorld;
			this.colorRand.setSeed(var4.getSeed() ^ var4.getTotalWorldTime() / 140L * var2);
			return var2 == 2 ? this.colorRand.nextInt() : var2 == 1 ? this.colorRand.nextInt() : 16777215;
		}
		else
		{
			EntityEggInfo var3 = this.getEgg(var1);
			return var3 == null ? 16777215 : var2 == 2 ? var3.primaryColor : var2 == 1 ? var3.secondaryColor : 16777215;
		}
	}

	private EntityEggInfo getEgg(ItemStack var1)
	{
		if (var1.getTagCompound() == null)
			return null;
		else
		{
			for (IMobEggHandler var3 : MFRRegistry.getModMobEggHandlers())
			{
				EntityEggInfo var4 = var3.getEgg(var1);
				if (var4 != null)
					return var4;
			}

			return null;
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float var8, float var9, float var10)
	{
		if (world.isRemote)
			return true;
		else if (isEmpty(stack))
			return true;
		else
    
			if (EventConfig.safariNet && EventUtils.cantBreak(player, x, y, z))
    

			if (player != null && player.capabilities.isCreativeMode)
				stack = stack.copy();

			return releaseEntity(stack, world, x, y, z, side) != null;
		}
	}

	public static Entity releaseEntity(ItemStack var0, World var1, int var2, int var3, int var4, int var5)
	{
		if (var1.isRemote)
			return null;
		else
		{
			Block var7 = var1.getBlock(var2, var3, var4);
			var2 = var2 + Facing.offsetsXForSide[var5];
			var3 = var3 + Facing.offsetsYForSide[var5];
			var4 = var4 + Facing.offsetsZForSide[var5];
			double var8 = 0.0D;
			if (var5 == 1 && var7.getRenderType() == 11)
				var8 = 0.5D;

			Entity entity;
			if (var0.getItemDamage() != 0)
				entity = spawnCreature(var1, var0.getItemDamage(), var2 + 0.5D, var3 + var8, var4 + 0.5D);
			else
				entity = spawnCreature(var1, var0.getTagCompound(), var2 + 0.5D, var3 + var8, var4 + 0.5D, var5);

			if (entity != null)
			{
				if (entity instanceof EntityLiving)
				{
					int var10 = ((ItemSafariNet) var0.getItem()).type;
					if (1 == (var10 & 1))
						((EntityLiving) entity).func_110163_bv();

					if (var0.hasDisplayName())
					{
						((EntityLiving) entity).setCustomNameTag(var0.getDisplayName());
						if (2 == (var10 & 2))
							((EntityLiving) entity).setAlwaysRenderNameTag(true);
					}
				}

				if (isSingleUse(var0))
					--var0.stackSize;
				else if (var0.getItemDamage() != 0)
					var0.setItemDamage(0);

				var0.setTagCompound(null);
			}

			return entity;
		}
	}

	private static Entity spawnCreature(World var0, NBTTagCompound var1, double var2, double var4, double var6, int var8)
	{
		Entity var9;
		if (var1.getBoolean("hide"))
		{
			ArrayList var10 = new ArrayList();

			for (IRandomMobProvider var12 : MFRRegistry.getRandomMobProviders())
			{
				var10.addAll(var12.getRandomMobs(var0));
			}

			RandomMob var17 = (RandomMob) WeightedRandom.getRandomItem(var0.rand, var10);
			var9 = var17.getMob();
			if (var9 instanceof EntityLiving && var17.shouldInit)
				((EntityLiving) var9).onSpawnWithEgg(null);
		}
		else
		{
			NBTTagList var15 = var1.getTagList("Pos", 6);
			var15.func_150304_a(0, new NBTTagDouble(var2));
			var15.func_150304_a(1, new NBTTagDouble(var4));
			var15.func_150304_a(2, new NBTTagDouble(var6));
			var9 = EntityList.createEntityFromNBT(var1, var0);
			if (var9 != null)
				var9.readFromNBT(var1);
		}

		if (var9 != null)
		{
			int var16 = Facing.offsetsXForSide[var8];
			int var18 = var8 == 0 ? -1 : 0;
			int var19 = Facing.offsetsZForSide[var8];
			AxisAlignedBB var13 = var9.boundingBox;
			var9.setLocationAndAngles(var2 + (var13.maxX - var13.minX) * 0.5D * var16, var4 + (var13.maxY - var13.minY) * 0.5D * var18, var6 + (var13.maxZ - var13.minZ) * 0.5D * var19, var0.rand.nextFloat() * 360.0F, 0.0F);
			var0.spawnEntityInWorld(var9);
			if (var9 instanceof EntityLiving)
				((EntityLiving) var9).playLivingSound();

			for (Entity var14 = var9.riddenByEntity; var14 != null; var14 = var14.riddenByEntity)
			{
				var14.setLocationAndAngles(var2, var4, var6, var0.rand.nextFloat() * 360.0F, 0.0F);
				var0.spawnEntityInWorld(var14);
				if (var14 instanceof EntityLiving)
					((EntityLiving) var14).playLivingSound();
			}
		}

		return var9;
	}

	private static Entity spawnCreature(World var0, int var1, double var2, double var4, double var6)
	{
		if (!EntityList.entityEggs.containsKey(Integer.valueOf(var1)))
			return null;
		else
		{
			Entity var8 = EntityList.createEntityByID(var1, var0);
			if (var8 != null)
			{
				var8.setLocationAndAngles(var2, var4, var6, var0.rand.nextFloat() * 360.0F, 0.0F);
				if (var8 instanceof EntityLiving)
					((EntityLiving) var8).onSpawnWithEgg(null);

				var0.spawnEntityInWorld(var8);
				if (var8 instanceof EntityLiving)
					((EntityLiving) var8).playLivingSound();
			}

			return var8;
		}
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity)
	{
		return captureEntity(stack, entity, player);
	}

	public static boolean captureEntity(ItemStack var0, EntityLivingBase var1)
	{
		return captureEntity(var0, var1, null);
	}

	public static boolean captureEntity(ItemStack stack, EntityLivingBase entity, EntityPlayer player)
	{
		if (entity.worldObj.isRemote)
			return false;
		else if (!isEmpty(stack))
			return false;
		else if (MFRRegistry.getSafariNetBlacklist().contains(entity.getClass()))
			return false;
		else if (entity instanceof EntityPlayer)
			return true;
		else
    
			if (EventConfig.safariNet && EventUtils.cantDamage(player, entity))
    

			boolean var3 = player != null && player.capabilities.isCreativeMode;
			NBTTagCompound var4 = new NBTTagCompound();
			synchronized (entity)
			{
				entity.writeToNBT(var4);
				var4.setString("id", EntityList.getEntityString(entity));
				if (entity.isDead)
					return false;
				else
				{
					if (!var3)
						entity.setDead();

					if (var3 | entity.isDead)
					{
						var3 = false;
						if (--stack.stackSize > 0)
						{
							var3 = true;
							stack = stack.copy();
						}

						stack.stackSize = 1;
						stack.setTagCompound(var4);
						if (!var3 || player != null && player.inventory.addItemStackToInventory(stack))
						{
							if (var3)
							{
								player.openContainer.detectAndSendChanges();
								((EntityPlayerMP) player).sendContainerAndContentsToPlayer(player.openContainer, player.openContainer.getInventory());
							}
						}
						else
							UtilInventory.dropStackInAir(entity.worldObj, entity, stack);

						return true;
					}
					else
						return false;
				}
			}
		}
	}

	public static boolean isEmpty(ItemStack var0)
	{
		return !isSafariNet(var0) || var0.getItemDamage() == 0 && (var0.getTagCompound() == null || !var0.getTagCompound().hasKey("id") && !var0.getTagCompound().getBoolean("hide"));
	}

	public static boolean isSingleUse(ItemStack var0)
	{
		return isSafariNet(var0) && !((ItemSafariNet) var0.getItem()).multiuse;
	}

	public static boolean isSafariNet(ItemStack var0)
	{
		return var0 != null && var0.getItem() instanceof ItemSafariNet;
	}

	public static ItemStack makeMysteryNet(ItemStack var0)
	{
		if (isSafariNet(var0))
		{
			NBTTagCompound var1 = new NBTTagCompound();
			var1.setBoolean("hide", true);
			var0.setTagCompound(var1);
		}

		return var0;
	}

	public static Class<?> getEntityClass(ItemStack var0)
	{
		if (isSafariNet(var0) && !isEmpty(var0))
		{
			if (var0.getItemDamage() != 0)
			{
				int var2 = var0.getItemDamage();
				return !EntityList.entityEggs.containsKey(Integer.valueOf(var2)) ? null : (Class) EntityList.IDtoClassMapping.get(Integer.valueOf(var2));
			}
			else
			{
				String var1 = var0.getTagCompound().getString("id");
				return !EntityList.stringToClassMapping.containsKey(var1) ? null : (Class) EntityList.stringToClassMapping.get(var1);
			}
		}
		else
			return null;
	}

	@Override
	public void getSubItems(Item var1, List<ItemStack> var2)
	{
		super.getSubItems(var1, var2);
		if (var1.equals(MFRThings.safariNetSingleItem))
			var2.add(VillageTradeHandler.getHiddenNetStack());

	}
}
