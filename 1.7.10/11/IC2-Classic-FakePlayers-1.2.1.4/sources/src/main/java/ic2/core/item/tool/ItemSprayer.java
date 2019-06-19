package ic2.core.item.tool;

import java.util.ArrayList;
import java.util.Random;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.ic2.ModUtils;

import ic2.api.event.FoamEvent;
import ic2.core.IC2;
import ic2.core.Ic2Items;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.item.ItemGradual;
import ic2.core.item.armor.ItemArmorCFPack;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ItemSprayer extends ItemGradual
{
	public ItemSprayer(int index)
	{
		super(index);
		this.setMaxDamage(1602);
		this.setSpriteID("i1");
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int l, float a, float b, float c)
	{
		if (!IC2.platform.isSimulating())
			return true;
		else
		{
			ItemStack pack = entityplayer.inventory.armorInventory[2];
			boolean pulledFromCFPack = pack != null && pack.getItem() == Ic2Items.cfPack.getItem() && ((ItemArmorCFPack) pack.getItem()).getCFPellet(entityplayer, pack);
			if (!pulledFromCFPack && itemstack.getItemDamage() > 1501)
				return false;
			else if (world.getBlock(i, j, k) == Block.getBlockFromItem(Ic2Items.scaffold.getItem()))
			{
				this.sprayFoam(world, i, j, k, calculateDirectionsFromPlayer(entityplayer), true, entityplayer);
				if (!pulledFromCFPack)
					itemstack.damageItem(100, entityplayer);

				return true;
			}
			else
			{
				if (l == 0)
					--j;

				if (l == 1)
					++j;

				if (l == 2)
					--k;

				if (l == 3)
					++k;

				if (l == 4)
					--i;

				if (l == 5)
    
				if (this.sprayFoam(world, i, j, k, calculateDirectionsFromPlayer(entityplayer), false, entityplayer))
				{
					if (!pulledFromCFPack)
						itemstack.damageItem(100, entityplayer);

					return true;
				}
				else
					return false;
			}
		}
	}

	public static boolean[] calculateDirectionsFromPlayer(EntityPlayer player)
	{
		float yaw = player.rotationYaw % 360.0F;
		float pitch = player.rotationPitch;
		boolean[] r = new boolean[] { true, true, true, true, true, true };
		if (pitch >= -65.0F && pitch <= 65.0F)
		{
			if (yaw >= 300.0F && yaw <= 360.0F || yaw >= 0.0F && yaw <= 60.0F)
				r[2] = false;

			if (yaw >= 30.0F && yaw <= 150.0F)
				r[5] = false;

			if (yaw >= 120.0F && yaw <= 240.0F)
				r[3] = false;

			if (yaw >= 210.0F && yaw <= 330.0F)
				r[4] = false;
		}

		if (pitch <= -40.0F)
			r[0] = false;

		if (pitch >= 40.0F)
			r[1] = false;

		return r;
    
	public boolean sprayFoam(World world, int i, int j, int k, boolean[] directions, boolean scaffold)
	{
		return this.sprayFoam(world, i, j, k, directions, scaffold, ModUtils.getModFake(world));
    
    
	public boolean sprayFoam(World world, int i, int j, int k, boolean[] directions, boolean scaffold, EntityPlayer player)
	{
		Block blockId = world.getBlock(i, j, k);
		FoamEvent.Check eventCheck = new FoamEvent.Check(world, i, j, k);
		MinecraftForge.EVENT_BUS.post(eventCheck);
		if ((scaffold || Block.getBlockFromItem(Ic2Items.constructionFoam.getItem()).canPlaceBlockAt(world, i, j, k) || blockId == Block.getBlockFromItem(Ic2Items.copperCableBlock.getItem()) && world.getBlockMetadata(i, j, k) != 13 || eventCheck.isCanceled()) && (!scaffold || blockId == Block.getBlockFromItem(Ic2Items.scaffold.getItem())))
		{
			ArrayList<ChunkPosition> check = new ArrayList();
			ArrayList<ChunkPosition> place = new ArrayList();
			int foamcount = getSprayMass();
			check.add(new ChunkPosition(i, j, k));

			for (int x = 0; x < check.size() && foamcount > 0; ++x)
			{
				ChunkPosition set = check.get(x);
				Block targetBlockId = world.getBlock(set.chunkPosX, set.chunkPosY, set.chunkPosZ);
				FoamEvent.Check nextCheck = new FoamEvent.Check(world, set.chunkPosX, set.chunkPosY, set.chunkPosZ);
				MinecraftForge.EVENT_BUS.post(nextCheck);
				if (!scaffold && (Block.getBlockFromItem(Ic2Items.constructionFoam.getItem()).canPlaceBlockAt(world, set.chunkPosX, set.chunkPosY, set.chunkPosZ) || targetBlockId == Block.getBlockFromItem(Ic2Items.copperCableBlock.getItem()) && world.getBlockMetadata(set.chunkPosX, set.chunkPosY, set.chunkPosZ) != 13 || nextCheck.isCanceled()) || scaffold && targetBlockId == Block.getBlockFromItem(Ic2Items.scaffold.getItem()))
				{
					this.considerAddingCoord(set, place);
					this.addAdjacentSpacesOnList(set.chunkPosX, set.chunkPosY, set.chunkPosZ, check, directions, scaffold);
					--foamcount;
				}
			}

			for (ChunkPosition pos : place)
    
				if (EventUtils.cantBreak(player, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ))
    

				Block targetBlockId = world.getBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
				if (targetBlockId == Block.getBlockFromItem(Ic2Items.scaffold.getItem()))
				{
					Block.getBlockFromItem(Ic2Items.scaffold.getItem()).dropBlockAsItem(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, world.getBlockMetadata(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ), 0);
					world.setBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, Block.getBlockFromItem(Ic2Items.constructionFoam.getItem()));
				}
				else if (targetBlockId == Block.getBlockFromItem(Ic2Items.copperCableBlock.getItem()))
				{
					TileEntity te = world.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
					if (te instanceof TileEntityCable)
						((TileEntityCable) te).changeFoam((byte) 1);
				}
				else
				{
					FoamEvent.Foam foamPlace = new FoamEvent.Foam(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
					MinecraftForge.EVENT_BUS.post(foamPlace);
					if (!foamPlace.isCanceled())
						world.setBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, Block.getBlockFromItem(Ic2Items.constructionFoam.getItem()));
				}
			}

			return true;
		}
		else
			return false;
	}

	public void addAdjacentSpacesOnList(int x, int y, int z, ArrayList<ChunkPosition> foam, boolean[] directions, boolean ignoreDirections)
	{
		int[] order = this.generateRngSpread(IC2.random);

		for (int i = 0; i < order.length; ++i)
			if (ignoreDirections || directions[order[i]])
				switch (order[i])
				{
					case 0:
						this.considerAddingCoord(new ChunkPosition(x, y - 1, z), foam);
						break;
					case 1:
						this.considerAddingCoord(new ChunkPosition(x, y + 1, z), foam);
						break;
					case 2:
						this.considerAddingCoord(new ChunkPosition(x, y, z - 1), foam);
						break;
					case 3:
						this.considerAddingCoord(new ChunkPosition(x, y, z + 1), foam);
						break;
					case 4:
						this.considerAddingCoord(new ChunkPosition(x - 1, y, z), foam);
						break;
					case 5:
						this.considerAddingCoord(new ChunkPosition(x + 1, y, z), foam);
				}

	}

	public void considerAddingCoord(ChunkPosition coord, ArrayList list)
	{
		for (int i = 0; i < list.size(); ++i)
			if (((ChunkPosition) list.get(i)).chunkPosX == coord.chunkPosX && ((ChunkPosition) list.get(i)).chunkPosY == coord.chunkPosY && ((ChunkPosition) list.get(i)).chunkPosZ == coord.chunkPosZ)
				return;

		list.add(coord);
	}

	public int[] generateRngSpread(Random random)
	{
		int[] re = new int[] { 0, 1, 2, 3, 4, 5 };

		for (int i = 0; i < 16; ++i)
		{
			int first = random.nextInt(6);
			int second = random.nextInt(6);
			int save = re[first];
			re[first] = re[second];
			re[second] = save;
		}

		return re;
	}

	public static int getSprayMass()
	{
		return 13;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player)
	{
		Block block = player.worldObj.getBlock(X, Y, Z);
		if (block == Block.getBlockFromItem(Ic2Items.constructionFoam.getItem()))
		{
			player.worldObj.setBlockToAir(X, Y, Z);
			if (!player.worldObj.isRemote)
				player.worldObj.spawnEntityInWorld(new EntityItem(player.worldObj, X, Y, Z, new ItemStack(Ic2Items.constructionFoam.getItem())));

			return true;
		}
		else
			return super.onBlockStartBreak(itemstack, X, Y, Z, player);
	}
}
