package am2.spell.components;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import ru.will.git.am2.EventConfig;
import ru.will.git.reflectionmedic.util.EventUtils;

import am2.AMCore;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.blocks.BlockAMOre;
import am2.blocks.BlocksCommonProxy;
import am2.items.ItemSpellBook;
import am2.particles.AMParticle;
import am2.particles.ParticleOrbitPoint;
import am2.spell.SpellUtils;
import am2.utility.DummyEntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;

public class Appropriation implements ISpellComponent
{
	private static final String storageKey = "stored_data";
	private static final String storageType = "storage_type";

	@Override
	public Object[] getRecipeItems()
	{
		Object[] var10000 = new Object[] { Items.ender_pearl, null, null };
		BlockAMOre var10007 = BlocksCommonProxy.AMOres;
		var10000[1] = new ItemStack(BlocksCommonProxy.AMOres, 1, 9);
		var10000[2] = Blocks.chest;
		return var10000;
	}

	@Override
	public int getID()
	{
		return 71;
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, int x, int y, int z, int face, double impactX, double impactY, double impactZ, EntityLivingBase caster)
	{
		if (!(caster instanceof EntityPlayer))
			return false;
		else
		{
			ItemStack originalSpellStack = this.getOriginalSpellStack((EntityPlayer) caster);
			if (originalSpellStack == null)
				return false;
			else if (originalSpellStack.stackTagCompound == null)
				return false;
			else
			{
				Block block = world.getBlock(x, y, z);
				if (block == null)
					return false;
				else
				{
					for (String s : AMCore.config.getAppropriationBlockBlacklist())
						if (block.getUnlocalizedName() == s)
							return false;

					if (!world.isRemote)
						if (originalSpellStack.stackTagCompound.hasKey("stored_data"))
						{
							if (world.getBlock(x, y, z) == Blocks.air)
								face = -1;

							if (face != -1)
								switch (face)
								{
									case 0:
										--y;
										break;
									case 1:
										++y;
										break;
									case 2:
										--z;
										break;
									case 3:
										++z;
										break;
									case 4:
										--x;
										break;
									case 5:
										++x;
								}

							if (world.isAirBlock(x, y, z) || !world.getBlock(x, y, z).getMaterial().isSolid())
							{
								NBTTagCompound nbt = null;
								if (stack.getTagCompound() != null)
									nbt = (NBTTagCompound) stack.getTagCompound().copy();

								EntityPlayerMP casterPlayer = (EntityPlayerMP) DummyEntityPlayer.fromEntityLiving(caster);

								    
								if (EventUtils.cantBreak(casterPlayer, x, y, z))
									return false;
								    

								world.captureBlockSnapshots = true;
								this.restore((EntityPlayer) caster, world, originalSpellStack, x, y, z, impactX, impactY, impactZ);
								world.captureBlockSnapshots = false;
								NBTTagCompound newNBT = null;
								if (stack.getTagCompound() != null)
									newNBT = (NBTTagCompound) stack.getTagCompound().copy();

								PlaceEvent placeEvent = null;
								List<BlockSnapshot> blockSnapshots = (List) world.capturedBlockSnapshots.clone();
								world.capturedBlockSnapshots.clear();
								if (nbt != null)
									stack.setTagCompound(nbt);

								if (blockSnapshots.size() > 1)
									placeEvent = ForgeEventFactory.onPlayerMultiBlockPlace(casterPlayer, blockSnapshots, ForgeDirection.UNKNOWN);
								else if (blockSnapshots.size() == 1)
									placeEvent = ForgeEventFactory.onPlayerBlockPlace(casterPlayer, blockSnapshots.get(0), ForgeDirection.UNKNOWN);

								if (placeEvent != null && placeEvent.isCanceled())
								{
									for (BlockSnapshot blocksnapshot : blockSnapshots)
									{
										world.restoringBlockSnapshots = true;
										blocksnapshot.restore(true, false);
										world.restoringBlockSnapshots = false;
									}

									return false;
								}

								if (nbt != null)
									stack.setTagCompound(newNBT);

								for (BlockSnapshot blocksnapshot : blockSnapshots)
								{
									int blockX = blocksnapshot.x;
									int blockY = blocksnapshot.y;
									int blockZ = blocksnapshot.z;
									int metadata = world.getBlockMetadata(blockX, blockY, blockZ);
									int updateFlag = blocksnapshot.flag;
									Block oldBlock = blocksnapshot.replacedBlock;
									Block newBlock = world.getBlock(blockX, blockY, blockZ);
									if (newBlock != null && !newBlock.hasTileEntity(metadata))
										newBlock.onBlockAdded(world, blockX, blockY, blockZ);

									world.markAndNotifyBlock(blockX, blockY, blockZ, (Chunk) null, oldBlock, newBlock, updateFlag);
								}

								world.capturedBlockSnapshots.clear();
							}
						}
						else
						{
							if (block == null || block.getBlockHardness(world, x, y, z) == -1.0F)
								return false;

							EntityPlayerMP casterPlayer = (EntityPlayerMP) DummyEntityPlayer.fromEntityLiving(caster);
							int meta = world.getBlockMetadata(x, y, z);

							    
							if (EventConfig.inList(EventConfig.appropriationBlackList, block, meta))
								return false;
							if (EventUtils.cantBreak(casterPlayer, x, y, z))
								return false;
							    

							NBTTagCompound data = new NBTTagCompound();
							data.setString("storage_type", "block");
							data.setInteger("blockID", Block.getIdFromBlock(block));
							data.setInteger("meta", meta);
							if (!ForgeEventFactory.doPlayerHarvestCheck(casterPlayer, block, true))
								return false;

							BreakEvent event = ForgeHooks.onBlockBreakEvent(world, casterPlayer.theItemInWorldManager.getGameType(), casterPlayer, x, y, z);
							if (event.isCanceled())
								return false;

							TileEntity te = world.getTileEntity(x, y, z);
							if (te != null)
							{
								NBTTagCompound teData = new NBTTagCompound();
								te.writeToNBT(teData);
								data.setTag("tileEntity", teData);

								try
								{
									world.removeTileEntity(x, y, z);
								}
								catch (Throwable var30)
								{
									var30.printStackTrace();
								}
							}

							originalSpellStack.stackTagCompound.setTag("stored_data", data);
							this.setOriginalSpellStackData((EntityPlayer) caster, originalSpellStack);
							world.setBlockToAir(x, y, z);
						}

					return true;
				}
			}
		}
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target)
	{
		if (!(target instanceof EntityPlayer) && !(target instanceof IBossDisplayData))
		{
			if (!(target instanceof EntityLivingBase))
				return false;
			else
			{
				for (Class clazz : AMCore.config.getAppropriationMobBlacklist())
					if (target.getClass() == clazz)
						return false;

				if (!(caster instanceof EntityPlayer))
					return false;
				else
				{
					ItemStack originalSpellStack = this.getOriginalSpellStack((EntityPlayer) caster);
					if (originalSpellStack == null)
						return false;
					else
					{
						if (!world.isRemote)
							if (originalSpellStack.stackTagCompound.hasKey("stored_data"))
							{
								    
								if (EventUtils.cantBreak((EntityPlayer) caster, target.posX, target.posY, target.posZ))
									return false;
								    

								this.restore((EntityPlayer) caster, world, originalSpellStack, (int) target.posX, (int) target.posY, (int) target.posZ, target.posX, target.posY + target.getEyeHeight(), target.posZ);
							}
							else
							{
								    
								if (target.isDead || EventUtils.cantDamage(caster, target))
									return false;
								    

								NBTTagCompound data = new NBTTagCompound();
								data.setString("class", target.getClass().getName());
								data.setString("storage_type", "ent");
								NBTTagCompound targetData = new NBTTagCompound();
								target.writeToNBT(targetData);
								data.setTag("targetNBT", targetData);
								originalSpellStack.stackTagCompound.setTag("stored_data", data);
								this.setOriginalSpellStackData((EntityPlayer) caster, originalSpellStack);
								target.setDead();
							}

						return true;
					}
				}
			}
		}
		else
			return false;
	}

	private void setOriginalSpellStackData(EntityPlayer caster, ItemStack modifiedStack)
	{
		ItemStack originalSpellStack = caster.getCurrentEquippedItem();
		if (originalSpellStack != null)
			if (originalSpellStack.getItem() instanceof ItemSpellBook)
				((ItemSpellBook) originalSpellStack.getItem()).replaceAciveItemStack(originalSpellStack, modifiedStack);
			else
				caster.inventory.setInventorySlotContents(caster.inventory.currentItem, modifiedStack);
	}

	private ItemStack getOriginalSpellStack(EntityPlayer caster)
	{
		ItemStack originalSpellStack = caster.getCurrentEquippedItem();
		if (originalSpellStack == null)
			return null;
		else
		{
			if (originalSpellStack.getItem() instanceof ItemSpellBook)
			{
				originalSpellStack = ((ItemSpellBook) originalSpellStack.getItem()).GetActiveItemStack(originalSpellStack);
				boolean hasAppropriation = false;

				for (int i = 0; i < SpellUtils.instance.numStages(originalSpellStack); ++i)
					if (SpellUtils.instance.componentIsPresent(originalSpellStack, Appropriation.class, i))
					{
						hasAppropriation = true;
						break;
					}

				if (!hasAppropriation)
					return null;
			}

			return originalSpellStack;
		}
	}

	private void restore(EntityPlayer player, World world, ItemStack stack, int x, int y, int z, double hitX, double hitY, double hitZ)
	{
		if (stack.stackTagCompound.hasKey("stored_data"))
		{
			NBTTagCompound storageCompound = stack.stackTagCompound.getCompoundTag("stored_data");
			if (storageCompound != null)
			{
				String type = storageCompound.getString("storage_type");
				if (type.equals("ent"))
				{
					String clazz = storageCompound.getString("class");
					NBTTagCompound entData = storageCompound.getCompoundTag("targetNBT");

					try
					{
						Entity ent = (Entity) Class.forName(clazz).getConstructor(new Class[] { World.class }).newInstance(new Object[] { world });
						ent.readFromNBT(entData);
						ent.setPosition(hitX, hitY, hitZ);
						world.spawnEntityInWorld(ent);
					}
					catch (Throwable var19)
					{
						var19.printStackTrace();
					}
				}
				else if (type.equals("block"))
				{
					int blockID = storageCompound.getInteger("blockID");
					int meta = storageCompound.getInteger("meta");
					Block block = Block.getBlockById(blockID);
					if (block == null)
					{
						if (!player.worldObj.isRemote)
							player.addChatComponentMessage(new ChatComponentText(StatCollector.translateToLocal("am2.tooltip.approError")));

						stack.stackTagCompound.removeTag("stored_data");
						return;
					}

					world.setBlock(x, y, z, block, meta, 2);
					if (storageCompound.hasKey("tileEntity"))
					{
						TileEntity te = world.getTileEntity(x, y, z);
						if (te != null)
						{
							te.readFromNBT(storageCompound.getCompoundTag("tileEntity"));
							te.xCoord = x;
							te.yCoord = y;
							te.zCoord = z;
							te.setWorldObj(world);
						}
					}
				}
			}

			stack.stackTagCompound.removeTag("stored_data");
			this.setOriginalSpellStackData(player, stack);
		}

	}

	@Override
	public float manaCost(EntityLivingBase caster)
	{
		return 415.0F;
	}

	@Override
	public float burnout(EntityLivingBase caster)
	{
		return 100.0F;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster)
	{
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier)
	{
		for (int i = 0; i < 5 + 5 * AMCore.config.getGFXLevel(); ++i)
		{
			AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "water_ball", x, y, z);
			if (particle != null)
			{
				particle.addRandomOffset(1.0D, 1.0D, 1.0D);
				particle.setMaxAge(10);
				particle.setParticleScale(0.1F);
				particle.AddParticleController(new ParticleOrbitPoint(particle, x, y, z, 1, false).SetTargetDistance(world.rand.nextDouble() + 0.10000000149011612D).SetOrbitSpeed(0.20000000298023224D));
			}
		}

	}

	@Override
	public EnumSet<Affinity> getAffinity()
	{
		return EnumSet.of(Affinity.WATER);
	}

	@Override
	public float getAffinityShift(Affinity affinity)
	{
		return 0.0F;
	}
}
