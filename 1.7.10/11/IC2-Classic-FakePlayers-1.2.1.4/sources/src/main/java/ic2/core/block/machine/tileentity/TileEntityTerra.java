package ic2.core.block.machine.tileentity;

import ru.will.git.reflectionmedic.util.EventUtils;
import ru.will.git.ic2.ITerraformingBPFakePlayer;
import ru.will.git.ic2.ModUtils;

import ic2.api.item.ITerraformerBP;
import ic2.api.item.ITerraformingBP;
import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.item.tfbp.TerraformerBluePrint;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityTerra extends TileEntityElecMachine
{
	public int failedAttempts = 0;
	public int lastX = -1;
	public int lastY = -1;
	public int lastZ = -1;
	public AudioSource audioSource = null;
	public int inactiveTicks = 0;

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[0];
	}

	public TileEntityTerra()
	{
		super(1, 0, 100000, 512);
	}

	@Override
	public String getInventoryName()
	{
		return "Terraformer";
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		boolean newActive = false;
		ITerraformerBP tfbp = this.getBluePrint();
		if (tfbp != null && this.energy >= tfbp.getConsume(this.inventory[0]))
		{
			newActive = true;
			int x = this.xCoord;
			int z = this.zCoord;
			int range = 1;
			if (this.lastY > -1)
			{
				range = tfbp.getRange(this.inventory[0]) / 10;
				x = this.lastX - this.worldObj.rand.nextInt(range + 1) + this.worldObj.rand.nextInt(range + 1);
				z = this.lastZ - this.worldObj.rand.nextInt(range + 1) + this.worldObj.rand.nextInt(range + 1);
			}
			else
			{
				if (this.failedAttempts > 4)
					this.failedAttempts = 4;

				range = tfbp.getRange(this.inventory[0]) * (this.failedAttempts + 1) / 5;
				x = x - this.worldObj.rand.nextInt(range + 1) + this.worldObj.rand.nextInt(range + 1);
				z = z - this.worldObj.rand.nextInt(range + 1) + this.worldObj.rand.nextInt(range + 1);
    
    
			{
				this.energy -= tfbp.getConsume(this.inventory[0]);
				this.failedAttempts = 0;
				this.lastX = x;
				this.lastZ = z;
				this.lastY = this.yCoord;
			}
			else
			{
				this.energy -= tfbp.getConsume(this.inventory[0]) / 10;
				++this.failedAttempts;
				this.lastY = -1;
			}
		}

		if (newActive)
		{
			this.inactiveTicks = 0;
			this.setActive(true);
		}
		else if (!newActive && this.getActive() && this.inactiveTicks++ > 30)
			this.setActive(false);

	}

	public ITerraformerBP getBluePrint()
	{
		ItemStack stack = this.inventory[0];
		if (stack == null)
			return null;
		else
		{
			Item item = stack.getItem();
			return item instanceof ITerraformerBP ? (ITerraformerBP) item : item instanceof ITerraformingBP ? new TerraformerBluePrint((ITerraformingBP) item) : null;
		}
	}

	@Override
	public void onUnloaded()
	{
		if (this.isRendering() && this.audioSource != null)
		{
			IC2.audioManager.removeSources(this);
			this.audioSource = null;
		}

		super.onUnloaded();
	}

	@Override
	public double injectEnergy(ForgeDirection directionFrom, double amount, double volt)
	{
		if (amount > 512.0D)
			return 0.0D;
		else if (this.energy + amount > this.maxEnergy)
		{
			int unused = (int) (this.energy + amount - this.maxEnergy);
			this.energy = this.maxEnergy;
			return unused;
		}
		else
		{
			this.energy = (int) (this.energy + amount);
			return 0.0D;
		}
	}

	public boolean ejectBlueprint()
	{
		if (this.inventory[0] == null)
			return false;
		else
		{
			if (this.isSimulating())
			{
				StackUtil.dropAsEntity(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.inventory[0]);
				this.inventory[0] = null;
			}

			return true;
		}
	}

	public void insertBlueprint(ItemStack tfbp)
	{
		this.ejectBlueprint();
		this.inventory[0] = tfbp;
	}

	public static int getFirstSolidBlockFrom(World world, int x, int z, int y)
	{
		while (y > 0)
		{
			if (world.isBlockNormalCubeDefault(x, y, z, false))
				return y;

			--y;
		}

		return -1;
	}

	public static int getFirstBlockFrom(World world, int x, int z, int y)
	{
		while (y > 0)
		{
			if (!world.isAirBlock(x, y, z))
				return y;

			--y;
		}

		return -1;
    
	public static boolean switchGround(World world, Block from, Block to, int x, int y, int z, boolean upwards)
	{
		return switchGround(ModUtils.getModFake(world), world, from, to, x, y, z, upwards);
    
    
	public static boolean switchGround(EntityPlayer player, World world, Block from, Block to, int x, int y, int z, boolean upwards)
	{
		if (upwards)
		{
			++y;
			int saveY = y;

			while (true)
			{
				Block id = world.getBlock(x, y - 1, z);
				if (world.isAirBlock(x, y - 1, z) || id != from)
					if (saveY == y)
						return false;
					else
    
						if (EventUtils.cantBreak(player, x, y, z))
    

						world.setBlock(x, y, z, to);
						return true;
					}

				--y;
			}
		}
		else
			while (true)
			{
				Block id2 = world.getBlock(x, y, z);
				if (world.isAirBlock(x, y, z) || id2 != to)
				{
					id2 = world.getBlock(x, y, z);
					if (!world.isAirBlock(x, y, z) && id2 == from)
    
						if (EventUtils.cantBreak(player, x, y, z))
    

						world.setBlock(x, y, z, to);
						return true;
					}
					else
						return false;
				}

				--y;
			}
	}

	public static BiomeGenBase getBiomeAt(World world, int x, int z)
	{
		return world.getChunkFromBlockCoords(x, z).getBiomeGenForWorldCoords(x & 15, z & 15, world.getWorldChunkManager());
	}

	public static void setBiomeAt(World world, int x, int z, BiomeGenBase biome)
	{
		Chunk chunk = world.getChunkFromBlockCoords(x, z);
		byte[] array = chunk.getBiomeArray();
		array[(z & 15) << 4 | x & 15] = (byte) (biome.biomeID & 255);
		chunk.setBiomeArray(array);
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("active") && this.prevActive != this.getActive())
		{
			if (this.audioSource == null)
				this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Terraformers/TerraformerGenericloop.ogg", true, false, IC2.audioManager.defaultVolume);

			if (this.getActive())
			{
				if (this.audioSource != null)
					this.audioSource.play();
			}
			else if (this.audioSource != null)
				this.audioSource.stop();
		}

		super.onNetworkUpdate(field);
	}

	@Override
	public int getEnergyUsage()
	{
		ITerraformerBP tfbp = this.getBluePrint();
		return tfbp != null ? tfbp.getConsume(this.inventory[0]) : 0;
	}
}
