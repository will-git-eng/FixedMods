package chylex.hee.entity.item;

import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.block.BlockDungeonPuzzle;
import chylex.hee.entity.fx.FXType;
import chylex.hee.entity.technical.EntityTechnicalPuzzleChain;
import chylex.hee.init.BlockList;
import chylex.hee.packets.PacketPipeline;
import chylex.hee.packets.client.C20Effect;
import chylex.hee.system.util.BlockPosM;
import chylex.hee.system.util.MathUtil;
import ru.will.git.reflectionmedic.fake.FakePlayerContainer;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.hee.ExplosionByPlayer;
import ru.will.git.hee.ModUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.IdentityHashMap;
import java.util.List;

public class EntityItemIgneousRock extends EntityItem
{
	private static final IdentityHashMap<Block, Block> blockTransformations = new IdentityHashMap();
	private short rockLife = 700;
	private byte thrownDirection;

	    
	public final FakePlayerContainer fake = new FakePlayerContainerEntity(ModUtils.profile, this);

	@Override
	public void func_145799_b(String playerName)
	{
		super.func_145799_b(playerName);
		if (this.worldObj != null)
		{
			EntityPlayer player = this.worldObj.getPlayerEntityByName(playerName);
			if (player != null)
				this.fake.setProfile(player.getGameProfile());
		}
	}
	    

	public EntityItemIgneousRock(World world)
	{
		super(world);
	}

	public EntityItemIgneousRock(World world, double x, double y, double z, ItemStack is)
	{
		super(world, x, y, z, is);
		EntityPlayer thrower = world.getClosestPlayer(x, y - 1.62D, z, 1.0D);
		if (thrower != null)
			this.thrownDirection = (byte) (MathHelper.floor_double(thrower.rotationYaw * 4.0F / 360.0F + 0.5D) & 3);

	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (!this.worldObj.isRemote)
		{
			ItemStack is = this.getEntityItem();
			if ((this.rockLife = (short) (this.rockLife - this.rand.nextInt(this.isInWater() ? 5 : 3))) < 0)
				if (--is.stackSize == 0)
					this.setDead();
				else
				{
					this.rockLife = 700;
					this.setEntityItemStack(is);
				}

			if (this.rand.nextInt(64 - Math.min(32, is.stackSize / 2)) == 0)
				for (int attempt = 0; attempt < 4 + is.stackSize / 8; ++attempt)
				{
					BlockPosM tmpPos = BlockPosM.tmp(this);
					tmpPos.move(MathUtil.floor((this.rand.nextDouble() - 0.5D) * 4.0D), MathUtil.floor((this.rand.nextDouble() - 0.5D) * 4.0D), MathUtil.floor((this.rand.nextDouble() - 0.5D) * 4.0D));

					    
					if (this.fake.cantBreak(tmpPos.x, tmpPos.y, tmpPos.z))
						break;
					    

					Block block = tmpPos.getBlock(this.worldObj);
					Block target = blockTransformations.get(block);
					if (target != null)
						tmpPos.setBlock(this.worldObj, target);
					else if (block.getMaterial() == Material.air)
					{
						if (this.rand.nextInt(5) != 0)
							continue;

						tmpPos.setBlock(this.worldObj, Blocks.fire);
					}
					else if (block == Blocks.tnt)
					{
						tmpPos.setAir(this.worldObj);

						ExplosionByPlayer.createExplosion(this.fake.get(), this.worldObj, null, tmpPos.x, tmpPos.y, tmpPos.z, 3.9F, true);
					}
					else
					{
						if (block != Blocks.tallgrass || tmpPos.getMetadata(this.worldObj) == 0)
							continue;

						tmpPos.setMetadata(this.worldObj, 0, 2);
					}

					if (block.getMaterial() != Material.air)
						PacketPipeline.sendToAllAround(this, 64.0D, new C20Effect(FXType.Basic.IGNEOUS_ROCK_MELT, tmpPos.x + 0.5D, tmpPos.y + 0.5D, tmpPos.z + 0.5D));

					if (this.rand.nextInt(3) == 0)
						break;
				}

			if (this.rand.nextInt(80 - Math.min(32, is.stackSize / 3)) == 0)
			{
				List<EntityLivingBase> nearbyEntities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.boundingBox.expand(3.0D, 3.0D, 3.0D));
				if (!nearbyEntities.isEmpty())
				{
					EntityLivingBase entity = nearbyEntities.get(this.rand.nextInt(nearbyEntities.size()));

					    
					if (!this.fake.cantDamage(entity))
						    
						entity.setFire(1 + this.rand.nextInt(4) + this.getEntityItem().stackSize / 10);
				}
			}
		}

		int ix = MathUtil.floor(this.posX);
		int iy = MathUtil.floor(this.posY);
		int iz = MathUtil.floor(this.posZ);
		if (this.rand.nextInt(6) == 0 && this.worldObj.getBlock(ix, iy, iz).getMaterial() == Material.water)
			HardcoreEnderExpansion.fx.bubble(this.worldObj, this.posX + 0.2F * (this.rand.nextFloat() - 0.5F), this.posY + 0.2F * (this.rand.nextFloat() - 0.5F), this.posZ + 0.2F * (this.rand.nextFloat() - 0.5F), 0.0D, 0.6D, 0.0D);

		if (this.worldObj.getBlock(ix, iy - 1, iz) == BlockList.dungeon_puzzle)
		{
			int meta = this.worldObj.getBlockMetadata(ix, iy - 1, iz);
			if (BlockDungeonPuzzle.canTrigger(meta))
			{
				for (int a = 0; a < 4; ++a)
				{
					HardcoreEnderExpansion.fx.igneousRockBreak(this);
				}

				if (!this.worldObj.isRemote && this.onGround)
				{
					this.worldObj.spawnEntityInWorld(new EntityTechnicalPuzzleChain(this.worldObj, ix, iy - 1, iz, this.thrownDirection));
					this.setDead();
				}
			}
		}

		if (this.rand.nextInt(30) == 0)
			for (int a = 0; a < 2; ++a)
			{
				this.worldObj.spawnParticle("lava", this.posX + 0.2F * (this.rand.nextFloat() - 0.5F), this.posY + 0.2F * (this.rand.nextFloat() - 0.5F), this.posZ + 0.2F * (this.rand.nextFloat() - 0.5F), 0.0D, 0.0D, 0.0D);
			}

	}

	@Override
	public boolean attackEntityFrom(DamageSource damageSource, float amount)
	{
		return !damageSource.isFireDamage() && super.attackEntityFrom(damageSource, amount);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		nbt.setShort("rockLife", this.rockLife);

		    
		this.fake.writeToNBT(nbt);
		    
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.rockLife = nbt.hasKey("rockLife") ? nbt.getShort("rockLife") : this.rockLife;

		    
		this.fake.readFromNBT(nbt);
		    
	}

	static
	{
		blockTransformations.put(Blocks.ice, Blocks.flowing_water);
		blockTransformations.put(Blocks.snow_layer, Blocks.flowing_water);
		blockTransformations.put(Blocks.snow, Blocks.flowing_water);
		blockTransformations.put(Blocks.water, Blocks.cobblestone);
		blockTransformations.put(Blocks.flowing_water, null);
		blockTransformations.put(Blocks.grass, Blocks.dirt);
		blockTransformations.put(Blocks.red_flower, Blocks.tallgrass);
		blockTransformations.put(Blocks.yellow_flower, Blocks.tallgrass);
		blockTransformations.put(Blocks.cobblestone, Blocks.stone);
		blockTransformations.put(Blocks.clay, Blocks.hardened_clay);
		blockTransformations.put(Blocks.sand, Blocks.glass);
	}
}
