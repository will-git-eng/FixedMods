package thaumcraft.common.tiles.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.crafting.ContainerDummy;
import thaumcraft.common.container.InventoryArcaneWorkbench;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class TileArcaneWorkbench extends TileThaumcraft
{
	public InventoryArcaneWorkbench inventoryCraft = new InventoryArcaneWorkbench(this, new ContainerDummy());
	public int auraVisServer = 0;
	public int auraVisClient = 0;

	
	private final Map<EntityPlayer, Container> containers = new WeakHashMap<>();

	public boolean canOpenContainer(EntityPlayer player)
	{
		return !this.containers.containsKey(player);
	}

	public void addContainer(EntityPlayer player, Container container)
	{
		this.containers.put(Objects.requireNonNull(player, "player must nut be null"), Objects.requireNonNull(container, "container must not be null"));
	}

	public boolean removeContainer(EntityPlayer player)
	{
		return this.containers.remove(player) != null;
	}

	public Map<EntityPlayer, Container> getContainers()
	{
		return Collections.unmodifiableMap(this.containers);
	}
	

	@Override
	public void readFromNBT(NBTTagCompound nbtCompound)
	{
		super.readFromNBT(nbtCompound);
		NonNullList<ItemStack> stacks = NonNullList.withSize(this.inventoryCraft.getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(nbtCompound, stacks);

		for (int a = 0; a < stacks.size(); ++a)
		{
			this.inventoryCraft.setInventorySlotContents(a, stacks.get(a));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound)
	{
		super.writeToNBT(nbtCompound);
		NonNullList<ItemStack> stacks = NonNullList.withSize(this.inventoryCraft.getSizeInventory(), ItemStack.EMPTY);

		for (int a = 0; a < stacks.size(); ++a)
		{
			stacks.set(a, this.inventoryCraft.getStackInSlot(a));
		}

		ItemStackHelper.saveAllItems(nbtCompound, stacks);
		return nbtCompound;
	}

	@Override
	public void readSyncNBT(NBTTagCompound nbtCompound)
	{
	}

	@Override
	public NBTTagCompound writeSyncNBT(NBTTagCompound nbtCompound)
	{
		return nbtCompound;
	}

	public void getAura()
	{
		if (!this.getWorld().isRemote)
		{
			int t = 0;
			if (this.world.getBlockState(this.getPos().up()).getBlock() != BlocksTC.arcaneWorkbenchCharger)
				t = (int) AuraHandler.getVis(this.getWorld(), this.getPos());
			else
			{
				int sx = this.pos.getX() >> 4;
				int sz = this.pos.getZ() >> 4;

				for (int xx = -1; xx <= 1; ++xx)
				{
					for (int zz = -1; zz <= 1; ++zz)
					{
						AuraChunk ac = AuraHandler.getAuraChunk(this.world.provider.getDimension(), sx + xx, sz + zz);
						if (ac != null)
							t = (int) ((float) t + ac.getVis());
					}
				}
			}

			this.auraVisServer = t;
		}

	}

	public void spendAura(int vis)
	{
		if (!this.getWorld().isRemote)
			if (this.world.getBlockState(this.getPos().up()).getBlock() == BlocksTC.arcaneWorkbenchCharger)
			{
				int q = vis;
				int z = Math.max(1, vis / 9);
				int attempts = 0;

				while (q > 0)
				{
					++attempts;

					for (int xx = -1; xx <= 1; ++xx)
					{
						for (int zz = -1; zz <= 1; ++zz)
						{
							if (z > q)
								z = q;

							q = (int) ((float) q - AuraHandler.drainVis(this.getWorld(), this.getPos().add(xx * 16, 0, zz * 16), (float) z, false));
							if (q <= 0 || attempts > 1000)
								return;
						}
					}
				}
			}
			else
				AuraHandler.drainVis(this.getWorld(), this.getPos(), (float) vis, false);

	}
}
