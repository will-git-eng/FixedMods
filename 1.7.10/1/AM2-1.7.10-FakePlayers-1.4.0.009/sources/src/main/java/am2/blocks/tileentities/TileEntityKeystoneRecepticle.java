package am2.blocks.tileentities;

import java.util.ArrayList;

import am2.AMChunkLoader;
import am2.AMCore;
import am2.api.blocks.IKeystoneLockable;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.math.AMVector3;
import am2.api.power.PowerTypes;
import am2.blocks.BlocksCommonProxy;
import am2.buffs.BuffList;
import am2.multiblock.IMultiblockStructureController;
import am2.power.PowerNodeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

public class TileEntityKeystoneRecepticle extends TileEntityAMPower implements IInventory, IMultiblockStructureController, IKeystoneLockable
{
	private boolean isActive = false;
	private long key;
	private final int boltType = 2;
	private int surroundingCheckTicks = 20;
	private final MultiblockStructureDefinition primary = new MultiblockStructureDefinition("gateways_alt");
	private final MultiblockStructureDefinition secondary = new MultiblockStructureDefinition("gateways");
	public static int keystoneSlot = 0;
	private ItemStack[] inventory = new ItemStack[this.getSizeInventory()];

	public TileEntityKeystoneRecepticle()
	{
		super(250000);
		this.initMultiblock();
	}

	public void initMultiblock()
	{
		this.primary.addAllowedBlock(0, 0, 0, BlocksCommonProxy.keystoneRecepticle, 0);
		this.primary.addAllowedBlock(0, 0, 0, BlocksCommonProxy.keystoneRecepticle, 2);
		this.primary.addAllowedBlock(0, 0, -1, Blocks.stone_brick_stairs, 2);
		this.primary.addAllowedBlock(0, 0, 1, Blocks.stone_brick_stairs, 3);
		this.primary.addAllowedBlock(0, 0, -1, Blocks.stonebrick);
		this.primary.addAllowedBlock(0, 0, 1, Blocks.stonebrick);
		this.primary.addAllowedBlock(0, -1, -1, Blocks.stone_brick_stairs, 7);
		this.primary.addAllowedBlock(0, -1, -2, Blocks.stone_brick_stairs, 2);
		this.primary.addAllowedBlock(0, -1, 1, Blocks.stone_brick_stairs, 6);
		this.primary.addAllowedBlock(0, -1, 2, Blocks.stone_brick_stairs, 3);
		this.primary.addAllowedBlock(0, -1, 2, Blocks.stonebrick);
		this.primary.addAllowedBlock(0, -1, -2, Blocks.stonebrick);
		this.primary.addAllowedBlock(0, -2, -2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -2, 2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -3, -2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -3, 2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -3, -1, Blocks.stone_brick_stairs, 3);
		this.primary.addAllowedBlock(0, -3, 1, Blocks.stone_brick_stairs, 2);
		this.primary.addAllowedBlock(0, -4, -2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -4, -1, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -4, 0, Blocks.stonebrick, 3);
		this.primary.addAllowedBlock(0, -4, 1, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -4, 2, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(0, 0, 0, BlocksCommonProxy.keystoneRecepticle, 1);
		this.secondary.addAllowedBlock(0, 0, 0, BlocksCommonProxy.keystoneRecepticle, 3);
		this.secondary.addAllowedBlock(-1, 0, 0, Blocks.stone_brick_stairs, 0);
		this.secondary.addAllowedBlock(1, 0, 0, Blocks.stone_brick_stairs, 1);
		this.secondary.addAllowedBlock(-1, 0, 0, Blocks.stonebrick);
		this.secondary.addAllowedBlock(1, 0, 0, Blocks.stonebrick);
		this.secondary.addAllowedBlock(-1, -1, 0, Blocks.stone_brick_stairs, 5);
		this.secondary.addAllowedBlock(-2, -1, 0, Blocks.stone_brick_stairs, 0);
		this.secondary.addAllowedBlock(1, -1, 0, Blocks.stone_brick_stairs, 4);
		this.secondary.addAllowedBlock(2, -1, 0, Blocks.stone_brick_stairs, 1);
		this.secondary.addAllowedBlock(2, -1, 0, Blocks.stonebrick);
		this.secondary.addAllowedBlock(-2, -1, 0, Blocks.stonebrick);
		this.secondary.addAllowedBlock(-2, -2, 0, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(2, -2, 0, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-2, -3, 0, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(2, -3, 0, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-1, -3, 0, Blocks.stone_brick_stairs, 1);
		this.secondary.addAllowedBlock(1, -3, 0, Blocks.stone_brick_stairs, 0);
		this.secondary.addAllowedBlock(-2, -4, 0, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-1, -4, 0, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(0, -4, 0, Blocks.stonebrick, 3);
		this.secondary.addAllowedBlock(1, -4, 0, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(2, -4, 0, Blocks.stonebrick, 0);
	}

	public void onPlaced()
	{
		if (!super.worldObj.isRemote)
			AMChunkLoader.INSTANCE.requestStaticChunkLoad(this.getClass(), super.xCoord, super.yCoord, super.zCoord, super.worldObj);

	}

	@Override
	public void invalidate()
	{
		AMCore var10000 = AMCore.instance;
		AMCore.proxy.blocks.removeKeystonePortal(super.xCoord, super.yCoord, super.zCoord, super.worldObj.provider.dimensionId);
		if (!super.worldObj.isRemote)
			AMChunkLoader.INSTANCE.releaseStaticChunkLoad(this.getClass(), super.xCoord, super.yCoord, super.zCoord, super.worldObj);

		super.invalidate();
	}

	public void setActive(long key)
	{
		this.isActive = true;
		this.key = key;
		int myMeta = super.worldObj.getBlockMetadata(super.xCoord, super.yCoord, super.zCoord);
		if (PowerNodeRegistry.For(super.worldObj).getHighestPowerType(this) == PowerTypes.DARK)
			myMeta = myMeta | 8;
		else if (PowerNodeRegistry.For(super.worldObj).getHighestPowerType(this) == PowerTypes.LIGHT)
			myMeta = myMeta | 4;

		if (!super.worldObj.isRemote)
		{
			for (Object player : super.worldObj.playerEntities)
				if (player instanceof EntityPlayerMP && new AMVector3((EntityPlayerMP) player).distanceSqTo(new AMVector3(this)) <= 4096.0D)
					((EntityPlayerMP) player).playerNetServerHandler.sendPacket(this.getDescriptionPacket());
		}
		else
			super.worldObj.playSound(super.xCoord, super.yCoord, super.zCoord, "arsmagica2:misc.gateway.open", 1.0F, 1.0F, true);

	}

	public boolean isActive()
	{
		return this.isActive;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(super.xCoord + 0.3D, super.yCoord - 3, super.zCoord + 0.3D, super.xCoord + 0.7D, super.yCoord, super.zCoord + 0.7D);
		ArrayList<Entity> entities = (ArrayList) super.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, bb);
		if (this.isActive)
		{
			--this.surroundingCheckTicks;
			if (this.surroundingCheckTicks <= 0)
			{
				this.surroundingCheckTicks = 20;
				this.checkSurroundings();
			}

			if (entities.size() == 1)
				this.doTeleport(entities.get(0));
		}
		else if (entities.size() == 1 && super.worldObj.canBlockSeeTheSky(super.xCoord, super.yCoord, super.zCoord))
		{
			Entity entity = entities.get(0);
			if (entity instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) entity;
				if (player.isPotionActive(BuffList.haste) && player.isPotionActive(Potion.moveSpeed.id) && player.isSprinting())
				{
					this.key = 0L;
					if (!super.worldObj.isRemote)
					{
						EntityLightningBolt elb = new EntityLightningBolt(super.worldObj, super.xCoord, super.yCoord, super.zCoord);
						super.worldObj.spawnEntityInWorld(elb);
					}

					this.doTeleport(player);
				}
			}
		}

	}

	public boolean canActivate()
	{
		boolean allGood = true;
		allGood = allGood & super.worldObj.isAirBlock(super.xCoord, super.yCoord - 1, super.zCoord);
		allGood = allGood & super.worldObj.isAirBlock(super.xCoord, super.yCoord - 2, super.zCoord);
		allGood = allGood & super.worldObj.isAirBlock(super.xCoord, super.yCoord - 3, super.zCoord);
		allGood = allGood & this.checkStructure();
		allGood = allGood & PowerNodeRegistry.For(super.worldObj).checkPower(this);
		allGood = allGood & !this.isActive;
		return allGood;
	}

	private void checkSurroundings()
	{
		if (!this.checkStructure())
			this.deactivate();

	}

	private boolean checkStructure()
	{
		int meta = super.worldObj.getBlockMetadata(super.xCoord, super.yCoord, super.zCoord) & 3;
		boolean remainActive = true;
		switch (meta)
		{
			case 0:
			case 2:
				remainActive = remainActive & this.primary.checkStructure(super.worldObj, super.xCoord, super.yCoord, super.zCoord);
				break;
			case 1:
			default:
				remainActive = remainActive & this.secondary.checkStructure(super.worldObj, super.xCoord, super.yCoord, super.zCoord);
		}

		return remainActive;
	}

	public void deactivate()
	{
		this.isActive = false;
		if (!super.worldObj.isRemote)
			super.worldObj.markBlockForUpdate(super.xCoord, super.yCoord, super.zCoord);

	}

	private void doTeleport(Entity entity)
	{
		this.deactivate();
		AMCore var10000 = AMCore.instance;
		AMVector3 newLocation = AMCore.proxy.blocks.getNextKeystonePortalLocation(super.worldObj, super.xCoord, super.yCoord, super.zCoord, false, this.key);
		AMVector3 myLocation = new AMVector3(super.xCoord, super.yCoord, super.zCoord);
		double distance = myLocation.distanceTo(newLocation);
		float essenceCost = (float) (Math.pow(distance, 2.0D) * 0.0017500000540167093D);
		int meta = super.worldObj.getBlockMetadata((int) newLocation.x, (int) newLocation.y, (int) newLocation.z);
		if (!AMCore.config.getHazardousGateways())
			super.worldObj.playSoundEffect(newLocation.x, newLocation.y, newLocation.z, "mob.endermen.portal", 1.0F, 1.0F);
		else
		{
			float charge = PowerNodeRegistry.For(super.worldObj).getHighestPower(this);
			if (charge < essenceCost)
			{
				essenceCost = charge;
				double distanceWeCanGo = MathHelper.sqrt_double(charge / 0.00175D);
				double deltaZ = newLocation.z - myLocation.z;
				double deltaX = newLocation.x - myLocation.x;
				double angleH = Math.atan2(deltaZ, deltaX);
				double newX = myLocation.x + Math.cos(angleH) * distanceWeCanGo;
				double newZ = myLocation.z + Math.sin(angleH) * distanceWeCanGo;

				double newY;
				for (newY = myLocation.y; super.worldObj.isAirBlock((int) newX, (int) newY, (int) newZ); ++newY)
				{
					    
					double maxHeight = 256;
					if (newY > maxHeight)
					{
						newY = maxHeight;
						break;
					}
					    
				}

				newLocation = new AMVector3(newX, newY, newZ);
			}

			charge = 0.0F;
			switch (meta)
			{
				case 0:
					charge = 270.0F;
					break;
				case 1:
					charge = 180.0F;
					break;
				case 2:
					charge = 90.0F;
					break;
				case 3:
					charge = 0.0F;
			}

			entity.setPositionAndRotation(newLocation.x + 0.5D, newLocation.y - entity.height, newLocation.z + 0.5D, charge, entity.rotationPitch);
			PowerNodeRegistry.For(super.worldObj).consumePower(this, PowerNodeRegistry.For(super.worldObj).getHighestPowerType(this), essenceCost);
			super.worldObj.playSoundEffect(myLocation.x, myLocation.y, myLocation.z, "mob.endermen.portal", 1.0F, 1.0F);
			super.worldObj.playSoundEffect(newLocation.x, newLocation.y, newLocation.z, "mob.endermen.portal", 1.0F, 1.0F);
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 3;
	}

	@Override
	public ItemStack[] getRunesInKey()
	{
		ItemStack[] runes = new ItemStack[] { this.inventory[0], this.inventory[1], this.inventory[2] };
		return runes;
	}

	@Override
	public boolean keystoneMustBeHeld()
	{
		return false;
	}

	@Override
	public boolean keystoneMustBeInActionBar()
	{
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return slot >= this.inventory.length ? null : this.inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.inventory[i] != null)
		{
			if (this.inventory[i].stackSize <= j)
			{
				ItemStack itemstack = this.inventory[i];
				this.inventory[i] = null;
				return itemstack;
			}
			else
			{
				ItemStack itemstack1 = this.inventory[i].splitStack(j);
				if (this.inventory[i].stackSize == 0)
					this.inventory[i] = null;

				return itemstack1;
			}
		}
		else
			return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		if (this.inventory[i] != null)
		{
			ItemStack itemstack = this.inventory[i];
			this.inventory[i] = null;
			return itemstack;
		}
		else
			return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.inventory[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
			itemstack.stackSize = this.getInventoryStackLimit();

	}

	@Override
	public String getInventoryName()
	{
		return "Keystone Recepticle";
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return super.worldObj.getTileEntity(super.xCoord, super.yCoord, super.zCoord) != this ? false : entityplayer.getDistanceSq(super.xCoord + 0.5D, super.yCoord + 0.5D, super.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("KeystoneRecepticleInventory", 10);
		this.inventory = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			String tag = String.format("ArrayIndex", new Object[] { Integer.valueOf(i) });
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < this.inventory.length)
				this.inventory[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
		}

		AMCore var10000 = AMCore.instance;
		AMCore.proxy.blocks.registerKeystonePortal(super.xCoord, super.yCoord, super.zCoord, nbttagcompound.getInteger("keystone_receptacle_dimension_id"));
		this.isActive = nbttagcompound.getBoolean("isActive");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.inventory.length; ++i)
			if (this.inventory[i] != null)
			{
				String tag = String.format("ArrayIndex", new Object[] { Integer.valueOf(i) });
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte(tag, (byte) i);
				this.inventory[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}

		nbttagcompound.setInteger("keystone_receptacle_dimension_id", super.worldObj.provider.dimensionId);
		nbttagcompound.setTag("KeystoneRecepticleInventory", nbttaglist);
		nbttagcompound.setBoolean("isActive", this.isActive);
	}

	@Override
	public boolean canProvidePower(PowerTypes type)
	{
		return false;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		S35PacketUpdateTileEntity packet = new S35PacketUpdateTileEntity(super.xCoord, super.yCoord, super.zCoord, super.worldObj.getBlockMetadata(super.xCoord, super.yCoord, super.zCoord), compound);
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		this.readFromNBT(pkt.func_148857_g());
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return false;
	}

	@Override
	public MultiblockStructureDefinition getDefinition()
	{
		return this.secondary;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(super.xCoord - 3, super.yCoord - 3, super.zCoord - 3, super.xCoord + 3, super.yCoord + 3, super.zCoord + 3);
	}

	@Override
	public int getChargeRate()
	{
		return 5;
	}

	@Override
	public int getRequestInterval()
	{
		return 0;
	}

	@Override
	public boolean canRelayPower(PowerTypes type)
	{
		return false;
	}
}
