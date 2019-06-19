package mcp.mobius.betterbarrels.common.items.dolly;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import ru.will.git.reflectionmedic.util.EventUtils;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameData;
import mcp.mobius.betterbarrels.BetterBarrels;
import mcp.mobius.betterbarrels.Utils;
import mcp.mobius.betterbarrels.common.JabbaCreativeTab;
import mcp.mobius.betterbarrels.common.LocalizedChat;
import mcp.mobius.betterbarrels.common.blocks.TileEntityBarrel;
import mcp.mobius.betterbarrels.network.BarrelPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemBarrelMover extends Item
{
	protected IIcon text_empty = null;
	protected IIcon text_filled = null;
	protected DollyType type = DollyType.NORMAL;

	protected static ArrayList<Class> classExtensions = new ArrayList<Class>();
	protected static ArrayList<String> classExtensionsNames = new ArrayList<String>();
	protected static HashMap<String, Class> classMap = new HashMap<String, Class>();

	protected Method tagCompoundWrite = Utils.ReflectionHelper.getMethod(NBTTagCompound.class, new String[] { "a", "func_74734_a", "write" }, new Class[] { java.io.DataOutput.class });

	protected enum DollyType
	{
		NORMAL,
		DIAMOND;
	}

	static
	{
		classExtensionsNames.add("cpw.mods.ironchest.TileEntityIronChest");
		classExtensionsNames.add("buildcraft.energy.TileEngine");
    
    
		classExtensionsNames.add("ic2.api.tile.IWrenchable");
		classExtensionsNames.add("mods.railcraft.common.blocks.machine.beta.TileEngine");
		classExtensionsNames.add("forestry.core.gadgets.Engine");
		classExtensionsNames.add("bluedart.tile.TileEntityForceEngine");
    
		classExtensionsNames.add("thermalexpansion.block.machine.TileMachineRoot");
		classExtensionsNames.add("dmillerw.cchests.block.tile.TileChest");

		classExtensionsNames.add("net.mcft.copy.betterstorage.block.tileentity.TileEntityReinforcedChest");
		classExtensionsNames.add("net.mcft.copy.betterstorage.block.tileentity.TileEntityLocker");
		classExtensionsNames.add("net.mcft.copy.betterstorage.block.tileentity.TileEntityCardboardBox");
		classExtensionsNames.add("net.mcft.copy.betterstorage.tile.entity.TileEntityConnectable");
		classExtensionsNames.add("net.mcft.copy.betterstorage.block.tileentity.TileEntityConnectable");
		classExtensionsNames.add("net.mcft.copy.betterstorage.api.lock.ILockable");
		classExtensionsNames.add("net.mcft.copy.betterstorage.api.ILockable");

		classExtensionsNames.add("jds.bibliocraft.tileentities.TileEntityBookcase");
		classExtensionsNames.add("jds.bibliocraft.tileentities.TileEntityPotionShelf");
		classExtensionsNames.add("jds.bibliocraft.tileentities.TileEntityWeaponRack");
		classExtensionsNames.add("jds.bibliocraft.tileentities.TileEntityGenericShelf");
    

		classExtensionsNames.add("com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers");
		classExtensionsNames.add("com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityCompDrawers");

		classExtensionsNames.add("com.bluepowermod.tile.TileBase");

		for (String s : classExtensionsNames)
			try
			{
				classExtensions.add(Class.forName(s));
				classMap.put(s, Class.forName(s));
			}
			catch (ClassNotFoundException e)
			{
				classExtensions.add(null);
			}
	}

	public ItemBarrelMover()
	{
		super();
    
    
		this.setCreativeTab(JabbaCreativeTab.tab);
		this.setNoRepair();
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon(BetterBarrels.modid + ":dolly_" + this.type.name().toLowerCase() + "_empty");
		this.text_empty = this.itemIcon;
		this.text_filled = par1IconRegister.registerIcon(BetterBarrels.modid + ":dolly_" + this.type.name().toLowerCase() + "_filled");
	}

	@Override
	public String getUnlocalizedName()
	{
		return this.getUnlocalizedName(null);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("Container"))
			return "item.dolly." + this.type.name().toLowerCase() + ".full";
		else
			return "item.dolly." + this.type.name().toLowerCase() + ".empty";
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass)
	{
		return this.getIconIndex(stack);
	}

	@Override
	public IIcon getIconIndex(ItemStack stack)
	{
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Container"))
			return this.text_filled;
		else
			return this.text_empty;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return false;

		if (FMLCommonHandler.instance().getMinecraftServerInstance().isBlockProtected(world, x, y, z, player))
			return false;

		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("Container"))
			return this.pickupContainer(stack, player, world, x, y, z);

		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Container"))
			return this.placeContainer(stack, player, world, x, y, z, side);

		return false;
	}

	protected boolean placeContainer(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side)
	{
		NBTTagCompound nbtStack = stack.getTagCompound();
		NBTTagCompound nbtContainerStack = nbtStack.getCompoundTag("Container");

		Block storedBlock;
		if (nbtContainerStack.hasKey("ID"))
			storedBlock = Block.getBlockById(nbtContainerStack.getInteger("ID"));
		else
			storedBlock = Block.getBlockFromName(nbtContainerStack.getString("Block"));
		int blockMeta = nbtContainerStack.getInteger("Meta");
		String TEClassName = nbtContainerStack.getString("TEClass");
		NBTTagCompound nbtContainer = nbtStack.getCompoundTag("Container").getCompoundTag("NBT");

    

		int targX = x;
		int targY = y;
		int targZ = z;

		Block targetBlock = world.getBlock(targX, targY, targZ);

		if (targetBlock == Blocks.snow)
			targSide = ForgeDirection.UP;

    
				&& (targetBlock == null || !targetBlock.isReplaceable(world, targX, targY, targZ)))
		{
			if (targSide.equals(ForgeDirection.NORTH))
				targZ -= 1;
			if (targSide.equals(ForgeDirection.SOUTH))
				targZ += 1;
			if (targSide.equals(ForgeDirection.WEST))
				targX -= 1;
			if (targSide.equals(ForgeDirection.EAST))
				targX += 1;
			if (targSide.equals(ForgeDirection.UP))
				targY += 1;
			if (targSide.equals(ForgeDirection.DOWN))
				targY -= 1;
		}

		if (!world.canPlaceEntityOnSide(storedBlock, targX, targY, targZ, false, side, (Entity) null, stack))
    
		if (EventUtils.cantBreak(player, targX, targY, targZ))
    

		nbtContainer.setInteger("x", targX);
		nbtContainer.setInteger("y", targY);
		nbtContainer.setInteger("z", targZ);

    
		if (TEClassName.contains("net.minecraft.tileentity.TileEntityChest"))
			blockMeta = this.getBarrelOrientationOnPlacement(player).ordinal();

    
		if (TEClassName.contains("buildcraft.energy.TileEngine") && nbtContainer.hasKey("orientation"))
			nbtContainer.setInteger("orientation", 1);

    
		if (TEClassName.contains("mods.railcraft.common.blocks.machine.beta") && nbtContainer.hasKey("direction"))
			nbtContainer.setByte("direction", (byte) 1);

    
		if (TEClassName.contains("forestry.energy.gadgets") && nbtContainer.hasKey("Orientation"))
			nbtContainer.setInteger("Orientation", 1);

    
		if (TEClassName.contains("bluedart.tile.TileEntityForceEngine") && nbtContainer.hasKey("facing"))
			nbtContainer.setByte("facing", (byte) 1);

    
		if (TEClassName.contains("thermalexpansion.block.engine") && nbtContainer.hasKey("side.facing"))
			nbtContainer.setByte("side.facing", (byte) 1);

    
		if (TEClassName.contains("cpw.mods.ironchest") && nbtContainer.hasKey("facing"))
			nbtContainer.setByte("facing", (byte) this.getBarrelOrientationOnPlacement(player).ordinal());

    
		if (TEClassName.contains("ic2.core.block") && nbtContainer.hasKey("facing"))
			nbtContainer.setShort("facing", (short) 6);

    
		if (TEClassName.contains("gregtechmod") && nbtContainer.hasKey("mFacing"))
			nbtContainer.setShort("mFacing", (short) this.getBarrelOrientationOnPlacement(player).ordinal());

    
		if (TEClassName.contains("dmillerw.cchests.block.tile") && nbtContainer.hasKey("orientation"))
			nbtContainer.setByte("orientation", (byte) this.getBarrelOrientationOnPlacement(player).ordinal());

    
		if (TEClassName.contains("net.mcft.copy.betterstorage.block.tileentity") && nbtContainer.hasKey("orientation"))
			nbtContainer.setByte("orientation", (byte) this.getBarrelOrientationOnPlacement(player).ordinal());

    
		if (TEClassName.contains("jds.bibliocraft.tileentities") && nbtContainer.hasKey("bookcaseAngle"))
			nbtContainer.setInteger("bookcaseAngle", this.fromForgeToBiblio(this.getBarrelOrientationOnPlacement(player)));

		if (TEClassName.contains("jds.bibliocraft.tileentities") && nbtContainer.hasKey("potionshelfAngle"))
			nbtContainer.setInteger("potionshelfAngle", this.fromForgeToBiblio(this.getBarrelOrientationOnPlacement(player)));

		if (TEClassName.contains("jds.bibliocraft.tileentities") && nbtContainer.hasKey("rackAngle"))
			nbtContainer.setInteger("rackAngle", this.fromForgeToBiblio(this.getBarrelOrientationOnPlacement(player)));

		if (TEClassName.contains("jds.bibliocraft.tileentities") && nbtContainer.hasKey("genericShelfAngle"))
			nbtContainer.setInteger("genericShelfAngle", this.fromForgeToBiblio(this.getBarrelOrientationOnPlacement(player)));

		if (TEClassName.contains("jds.bibliocraft.tileentities.TileEntityArmorStand"))
    
    

    
    

    
		if (TEClassName.contains("com.jaquadro.minecraft.storagedrawers.block.tile") && nbtContainer.hasKey("Dir"))
			nbtContainer.setInteger("Dir", (short) this.getBarrelOrientationOnPlacement(player).ordinal());

		if (TEClassName.contains("com.bluepowermod.tile") && nbtContainer.hasKey("rotation"))
			try
			{
				Class blockClazz = storedBlock.getClass();
				Method allowVertical = null;
				while (allowVertical == null && !blockClazz.equals(Object.class))
					try
					{
						allowVertical = blockClazz.getDeclaredMethod("canRotateVertical");
					}
					catch (NoSuchMethodException e)
					{
						blockClazz = blockClazz.getSuperclass();
					}

				allowVertical.setAccessible(true);
				boolean vertAllowed = ((Boolean) allowVertical.invoke(storedBlock, (Object[]) null)).booleanValue();
				nbtContainer.setInteger("rotation", (short) Utils.getDirectionFacingEntity(player, vertAllowed).ordinal());
			}
			catch (Exception e)
			{
				BetterBarrels.log.warn("Unable to rotate BluePower machine. place machine will not be rotated to be facing player.");
			}

    
		if (TEClassName.contains("thermalexpansion.block.machine") && nbtContainer.hasKey("side.facing"))
		{
			ForgeDirection side_facing = ForgeDirection.getOrientation(nbtContainer.getByte("side.facing"));
			ForgeDirection new_facing = this.getBarrelOrientationOnPlacement(player);
			byte[] side_array_old = nbtContainer.getByteArray("side.array");
			byte[] side_array_new = side_array_old.clone();

			int rotations = 0;
			while (side_facing != new_facing)
			{
				rotations += 1;
				side_facing = side_facing.getRotation(ForgeDirection.UP);
			}

			for (int i = 2; i < 6; i++)
			{
				ForgeDirection new_direction = ForgeDirection.getOrientation(i);
				for (int j = 0; j < rotations; j++)
					new_direction = new_direction.getRotation(ForgeDirection.DOWN);
				side_array_new[i] = side_array_old[new_direction.ordinal()];
			}

			nbtContainer.setByteArray("side.array", side_array_new);
			nbtContainer.setByte("side.facing", (byte) new_facing.ordinal());
		}

    

		if (nbtContainer.getString("id").equals("TileEntityBarrel"))
		{
			ForgeDirection newBarrelRotation = Utils.getDirectionFacingEntity(player, false);
			ForgeDirection oldBarrelRotation = ForgeDirection.getOrientation(nbtContainer.getInteger("rotation"));
			ForgeDirection newBarrelOrientation = Utils.getDirectionFacingEntity(player, BetterBarrels.allowVerticalPlacement);
			ForgeDirection oldBarrelOrientation = ForgeDirection.getOrientation(nbtContainer.getInteger("orientation"));
			int[] newSideArray = new int[6];
			int[] oldSideArray = nbtContainer.getIntArray("sideUpgrades");
			int[] newSideMetaArray = new int[6];
			int[] oldSideMetaArray = nbtContainer.getIntArray("sideMeta");

    
			if (oldBarrelRotation == ForgeDirection.UNKNOWN)
				oldBarrelRotation = ForgeDirection.SOUTH;
			if (oldBarrelOrientation == ForgeDirection.UNKNOWN)
				oldBarrelOrientation = ForgeDirection.SOUTH;

    
			if (oldBarrelOrientation == ForgeDirection.UP || oldBarrelOrientation == ForgeDirection.DOWN)
			{
				ForgeDirection rot = oldBarrelRotation.getRotation(oldBarrelOrientation);
				for (int i = 0; i < 6; i++)
				{
					int j = ForgeDirection.getOrientation(i).getRotation(rot).ordinal();
					newSideArray[j] = oldSideArray[i];
					newSideMetaArray[j] = oldSideMetaArray[i];
				}
				oldBarrelOrientation = oldBarrelRotation;
				oldSideArray = newSideArray.clone();
				oldSideMetaArray = newSideMetaArray.clone();
			}

			int numberRotationsVAxis = 0;
			while (newBarrelRotation != oldBarrelRotation)
			{
				numberRotationsVAxis += 1;
				oldBarrelRotation = oldBarrelRotation.getRotation(ForgeDirection.UP);
			}

			for (int i = 0; i < 6; i++)
			{
				ForgeDirection idir = ForgeDirection.getOrientation(i);
				for (int rot = 0; rot < numberRotationsVAxis; rot++)
					idir = idir.getRotation(ForgeDirection.UP);
				newSideArray[idir.ordinal()] = oldSideArray[i];
				newSideMetaArray[idir.ordinal()] = oldSideMetaArray[i];
			}

    
			if (newBarrelOrientation == ForgeDirection.UP || newBarrelOrientation == ForgeDirection.DOWN)
			{
				oldSideArray = newSideArray.clone();
				oldSideMetaArray = newSideMetaArray.clone();
				ForgeDirection rot = newBarrelRotation.getRotation(newBarrelOrientation.getOpposite());
				for (int i = 0; i < 6; i++)
				{
					int j = ForgeDirection.getOrientation(i).getRotation(rot).ordinal();
					newSideArray[j] = oldSideArray[i];
					newSideMetaArray[j] = oldSideMetaArray[i];
				}
			}

			nbtContainer.setInteger("orientation", newBarrelOrientation.ordinal());
			nbtContainer.setInteger("rotation", newBarrelRotation.ordinal());
			nbtContainer.setIntArray("sideUpgrades", newSideArray);
			nbtContainer.setIntArray("sideMeta", newSideMetaArray);
		}

		world.setBlock(targX, targY, targZ, storedBlock, blockMeta, 1 + 2);
		world.getTileEntity(targX, targY, targZ).readFromNBT(nbtContainer);

		TileEntity entity = world.getTileEntity(targX, targY, targZ);

    
		if (classMap.get("ic2.api.tile.IWrenchable") != null && classMap.get("ic2.api.tile.IWrenchable").isInstance(entity))
			this.fixIC2Orientation(entity, player, targY);

		if (TEClassName.contains("net.minecraft.tileentity.TileEntityChest"))
			world.setBlockMetadataWithNotify(targX, targY, targZ, blockMeta, 1 + 2);

		stack.getTagCompound().removeTag("Container");

		world.markBlockForUpdate(targX, targY, targZ);

		return true;
	}

	private void fixIC2Orientation(TileEntity entity, EntityPlayer player, int targY)
	{
		try
		{
			Method setFacing = classMap.get("ic2.api.tile.IWrenchable").getMethod("setFacing", new Class[] { short.class });
			Method wrenchCanSetFacing = classMap.get("ic2.api.tile.IWrenchable").getMethod("wrenchCanSetFacing", new Class[] { EntityPlayer.class, int.class });
			if ((Boolean) wrenchCanSetFacing.invoke(entity, player, (short) this.getBarrelOrientationOnPlacement(player, targY, true).ordinal()))
				setFacing.invoke(entity, (short) this.getBarrelOrientationOnPlacement(player, targY, true).ordinal());
			else
				setFacing.invoke(entity, (short) this.getBarrelOrientationOnPlacement(player, targY, false).ordinal());

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean isTEMovable(TileEntity te)
	{
		if (te instanceof TileEntityMobSpawner)
			return this.canPickSpawners();
		if (te instanceof TileEntityBarrel)
			return true;
		if (te instanceof TileEntityChest)
			return true;
		for (Class c : classExtensions)
			if (c != null && c.isInstance(te))
				return true;
		return false;
	}

	private boolean pickupBetterStorageFix(TileEntity container)
	{
		if (classMap.get("net.mcft.copy.betterstorage.api.lock.ILockable") != null && classMap.get("net.mcft.copy.betterstorage.api.lock.ILockable").isInstance(container))
			try
			{
				Method getLock = classMap.get("net.mcft.copy.betterstorage.api.lock.ILockable").getDeclaredMethod("getLock", (Class[]) null);
				Object lock = getLock.invoke(container, (Object[]) null);
				if (lock != null)
					return false;
			}
			catch (Exception e)
			{
				System.out.printf("%s \n", e);
				return false;
			}

		if (classMap.get("net.mcft.copy.betterstorage.api.ILockable") != null && classMap.get("net.mcft.copy.betterstorage.api.ILockable").isInstance(container))
			try
			{
				Method getLock = classMap.get("net.mcft.copy.betterstorage.api.ILockable").getDeclaredMethod("getLock", (Class[]) null);
				Object lock = getLock.invoke(container, (Object[]) null);
				if (lock != null)
					return false;
			}
			catch (Exception e)
			{
				System.out.printf("%s \n", e);
				return false;
			}

		if (classMap.get("net.mcft.copy.betterstorage.tile.entity.TileEntityConnectable") != null && classMap.get("net.mcft.copy.betterstorage.tile.entity.TileEntityConnectable").isInstance(container))
			try
			{
				Method disconnect = classMap.get("net.mcft.copy.betterstorage.tile.entity.TileEntityConnectable").getDeclaredMethod("disconnect", (Class[]) null);
				disconnect.invoke(container, (Object[]) null);
			}
			catch (Exception e)
			{
				System.out.printf("%s \n", e);
				return false;
			}

		if (classMap.get("net.mcft.copy.betterstorage.block.tileentity.TileEntityConnectable") != null && classMap.get("net.mcft.copy.betterstorage.block.tileentity.TileEntityConnectable").isInstance(container))
			try
			{
				Method disconnect = classMap.get("net.mcft.copy.betterstorage.block.tileentity.TileEntityConnectable").getDeclaredMethod("disconnect", (Class[]) null);
				disconnect.invoke(container, (Object[]) null);
			}
			catch (Exception e)
			{
				System.out.printf("%s \n", e);
				return false;
			}

		return true;
	}

	protected boolean canPickSpawners()
	{
		return false;
	}

	protected boolean pickupContainer(ItemStack stack, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity containerTE = world.getTileEntity(x, y, z);
		if (containerTE == null)
    
		if (EventUtils.cantBreak(player, x, y, z))
    

		Block storedBlock = world.getBlock(x, y, z);
		int blockMeta = world.getBlockMetadata(x, y, z);
		NBTTagCompound nbtContainer = new NBTTagCompound();
		NBTTagCompound nbtTarget = new NBTTagCompound();

		if (!this.isTEMovable(containerTE))
			return false;

		if (!this.pickupBetterStorageFix(containerTE))
			return false;

		containerTE.writeToNBT(nbtContainer);

		nbtTarget.setString("Block", GameData.getBlockRegistry().getNameForObject(storedBlock));
		nbtTarget.setInteger("Meta", blockMeta);
		nbtTarget.setString("TEClass", containerTE.getClass().getName());
		nbtTarget.setBoolean("isSpawner", containerTE instanceof TileEntityMobSpawner);
    

		if (this.tagCompoundWrite != null)
		{
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			try
			{
				DataOutputStream outStream = new DataOutputStream(byteStream);
				this.tagCompoundWrite.invoke(nbtTarget, outStream);
				outStream.close();

				if (byteStream.toByteArray().length > 1048576)
    
					BarrelPacketHandler.sendLocalizedChat(player, LocalizedChat.DOLLY_TOO_COMPLEX);
					return false;
				}
			}
			catch (Throwable t)
			{
			}
		}

		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		else if (stack.getTagCompound().hasKey("Container"))
			stack.getTagCompound().removeTag("Container");

    
    
    

		try
		{
			if (containerTE instanceof TileEntityChest)
				((TileEntityChest) containerTE).closeInventory();
			world.removeTileEntity(x, y, z);
			world.setBlock(x, y, z, Blocks.air, 0, 1 + 2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5)
	{
		if (world.isRemote)
			return;

		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Container") && entity instanceof EntityPlayer)
		{

			int amplifier = 1;
			if (stack.getTagCompound().hasKey("amount"))
				amplifier = Math.min(4, stack.getTagCompound().getInteger("amount") / 2048);

			((EntityPlayer) entity).addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 10, amplifier));
			((EntityPlayer) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 10, amplifier));
		}
	}

	private ForgeDirection getBarrelOrientationOnPlacement(EntityPlayer player)
	{
		return this.getBarrelOrientationOnPlacement(player, 0, false);
	}

	private ForgeDirection getBarrelOrientationOnPlacement(EntityPlayer player, int targY, boolean allowVertical)
	{

		ForgeDirection barrelOrientation = ForgeDirection.UNKNOWN;
		Vec3 playerLook = player.getLookVec();
		if (Math.abs(playerLook.xCoord) >= Math.abs(playerLook.zCoord))
		{
			if (playerLook.xCoord > 0)
				barrelOrientation = ForgeDirection.WEST;
			else
				barrelOrientation = ForgeDirection.EAST;
		}
		else if (playerLook.zCoord > 0)
			barrelOrientation = ForgeDirection.NORTH;
		else
			barrelOrientation = ForgeDirection.SOUTH;

		if (allowVertical && player.posY > targY)
			barrelOrientation = ForgeDirection.UP;
		else if (allowVertical && playerLook.yCoord > 0.73)
			barrelOrientation = ForgeDirection.DOWN;

		return barrelOrientation;

	}

	private ArrayList<ForgeDirection> convertOrientationFlagToForge(int flags)
	{
		ArrayList<ForgeDirection> directions = new ArrayList<ForgeDirection>();

		for (int i = 0; i < 4; i++)
			if ((1 << i & flags) != 0)
				directions.add(ForgeDirection.getOrientation(i + 2));

		return directions;
	}

	private int convertForgeToOrientationFlag(ArrayList<ForgeDirection> directions)
	{
		int flags = 0;
		for (ForgeDirection direction : directions)
			flags += 1 << direction.ordinal() - 2;
		return flags;
	}

	private String getBlockName(TileEntity tileEntity)
    
		Block teBlock = tileEntity.getWorldObj().getBlock(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);

		ItemStack pick = null;
		try
		{
			pick = teBlock.getPickBlock(null, tileEntity.getWorldObj(), tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
			if (pick != null)
				return pick.getDisplayName();
		}
		catch (Throwable e)
		{
		}

		return "<Unknown>";
	}

	private ForgeDirection fromMCToForge(short side)
	{
		switch (side)
		{
			case 0:
				return ForgeDirection.DOWN;
			case 1:
				return ForgeDirection.UP;
			case 2:
				return ForgeDirection.EAST;
			case 3:
				return ForgeDirection.WEST;
			case 4:
				return ForgeDirection.NORTH;
			case 5:
				return ForgeDirection.SOUTH;
		}
		return ForgeDirection.UNKNOWN;
	}

	private short fromForgeToMC(ForgeDirection side)
	{
		switch (side)
		{
			case DOWN:
				return (short) 0;
			case UP:
				return (short) 1;
			case EAST:
				return (short) 2;
			case WEST:
				return (short) 3;
			case NORTH:
				return (short) 4;
			case SOUTH:
				return (short) 5;
			case UNKNOWN:
				return (short) -1;
		}
		return -1;
	}

	private short fromForgeToBiblio(ForgeDirection side)
	{
		switch (side)
		{
			case EAST:
				return (short) 2;
			case WEST:
				return (short) 0;
			case NORTH:
				return (short) 1;
			case SOUTH:
				return (short) 3;
			default:
				return (short) -1;
		}
	}
}
