package tconstruct.library.tools;

import ru.will.git.tconstruct.EventConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import tconstruct.tools.TinkerTools;
import tconstruct.util.config.PHConstruct;

import java.util.HashSet;
import java.util.Set;

    

public abstract class HarvestTool extends ToolCore
{
	public HarvestTool(int baseDamage)
	{
		super(baseDamage);
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player)
	{
		return super.onBlockStartBreak(stack, x, y, z, player);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass)
    
		if (stack == null || !(stack.getItem() instanceof HarvestTool))
    
		if (toolClass == null || !this.getHarvestType().equals(toolClass))
			return -1;

		if (!stack.hasTagCompound())
			return -1;

    
		if (tags.getBoolean("Broken"))
    
		return tags.getInteger("HarvestLevel");
	}

	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta)
	{
		if (!stack.hasTagCompound())
			return 1.0f;

		NBTTagCompound tags = stack.getTagCompound().getCompoundTag("InfiTool");
		if (tags.getBoolean("Broken"))
			return 0.1f;

		if (this.isEffective(block, meta))
			return this.calculateStrength(tags, block, meta);

		return super.getDigSpeed(stack, block, meta);
	}

	public float calculateStrength(NBTTagCompound tags, Block block, int meta)
	{

		int hlvl = block.getHarvestLevel(meta);
		if (hlvl > tags.getInteger("HarvestLevel"))
			return 0.1f;

		return AbilityHelper.calcToolSpeed(this, tags);
	}

	public float breakSpeedModifier()
	{
		return 1.0f;
	}

	public float stoneboundModifier()
	{
		return 72f;
	}

	@Override
	public boolean func_150897_b(Block block)
	{
		return this.isEffective(block.getMaterial());
	}

	@Override
	public String[] getTraits()
	{
		return new String[] { "harvest" };
	}

	protected abstract Material[] getEffectiveMaterials();

	protected abstract String getHarvestType();

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		Set<String> set = new HashSet<String>();

		if (stack != null && stack.getItem() instanceof HarvestTool)
			set.add(((HarvestTool) stack.getItem()).getHarvestType());

		return set;
	}

	public boolean isEffective(Block block, int meta)
	{
		if (this.getHarvestType().equals(block.getHarvestTool(meta)))
			return true;

		else
			return this.isEffective(block.getMaterial());
	}

	public boolean isEffective(Material material)
	{
		for (Material m : this.getEffectiveMaterials())
		{
			if (m == material)
				return true;
		}

		return false;
    
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float clickX, float clickY, float clickZ)
	{
    

		boolean used = false;
		int hotbarSlot = player.inventory.currentItem;
		int itemSlot = hotbarSlot == 0 ? 8 : hotbarSlot + 1;
		ItemStack nearbyStack = null;

		if (hotbarSlot < 8)
		{
			nearbyStack = player.inventory.getStackInSlot(itemSlot);
			if (nearbyStack != null)
			{
				Item item = nearbyStack.getItem();

				if (item instanceof ItemBlock || item != null && item == TinkerTools.openBlocksDevNull)
				{
					int posX = x;
					int posY = y;
					int posZ = z;

					switch (side)
					{
						case 0:
							--posY;
							break;
						case 1:
							++posY;
							break;
						case 2:
							--posZ;
							break;
						case 3:
							++posZ;
							break;
						case 4:
							--posX;
							break;
						case 5:
							++posX;
							break;
					}

					AxisAlignedBB blockBounds = AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1);
					AxisAlignedBB playerBounds = player.boundingBox;

					if (item instanceof ItemBlock)
					{
						Block blockToPlace = ((ItemBlock) item).field_150939_a;
						if (blockToPlace.getMaterial().blocksMovement())
							if (playerBounds.intersectsWith(blockBounds))
								return false;
					}

					int dmg = nearbyStack.getItemDamage();
    
					if (EventConfig.inList(EventConfig.pickaxeRMBBlackList, nearbyStack))
    

					if (item == TinkerTools.openBlocksDevNull)
    
						player.inventory.currentItem = itemSlot;
						item.onItemUse(nearbyStack, player, world, x, y, z, side, clickX, clickY, clickZ);
						player.inventory.currentItem = hotbarSlot;
						player.swingItem();
					}
					else
    
					if (player.capabilities.isCreativeMode)
    
						nearbyStack.setItemDamage(dmg);
						nearbyStack.stackSize = count;
					}
					if (nearbyStack.stackSize < 1)
					{
						nearbyStack = null;
						player.inventory.setInventorySlotContents(itemSlot, null);
					}
				}
			}
		}

    

		return used;
	}

	protected void breakExtraBlock(World world, int x, int y, int z, int sidehit, EntityPlayer playerEntity, int refX, int refY, int refZ)
    
		if (world.isAirBlock(x, y, z))
    
		if (!(playerEntity instanceof EntityPlayerMP))
			return;
    
    
		Block block = world.getBlock(x, y, z);
    
		if (!this.isEffective(block, meta))
			return;

		Block refBlock = world.getBlock(refX, refY, refZ);
		float refStrength = ForgeHooks.blockStrength(refBlock, player, world, refX, refY, refZ);
    
		if (!ForgeHooks.canHarvestBlock(block, player, meta) || refStrength / strength > 10f)
    
		BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(world, player.theItemInWorldManager.getGameType(), player, x, y, z);
		if (event.isCanceled())
			return;

		if (player.capabilities.isCreativeMode)
		{
			block.onBlockHarvested(world, x, y, z, meta, player);
			if (block.removedByPlayer(world, player, x, y, z, false))
    
			if (!world.isRemote)
				player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
			return;
    
    
		if (!world.isRemote)
    
    
			block.onBlockHarvested(world, x, y, z, meta, player);

    
			{
				block.onBlockDestroyedByPlayer(world, x, y, z, meta);
				block.harvestBlock(world, player, x, y, z, meta);
				block.dropXpOnBlockBreak(world, x, y, z, event.getExpToDrop());
    
			player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
    
		else
    
    
    
    
			world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
			if (block.removedByPlayer(world, player, x, y, z, true))
    
			ItemStack itemstack = player.getCurrentEquippedItem();
			if (itemstack != null)
			{
				itemstack.func_150999_a(world, block, x, y, z, player);

				if (itemstack.stackSize == 0)
					player.destroyCurrentEquippedItem();
    
			if (PHConstruct.extraBlockUpdates)
				Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C07PacketPlayerDigging(2, x, y, z, Minecraft.getMinecraft().objectMouseOver.sideHit));
		}
	}
}
