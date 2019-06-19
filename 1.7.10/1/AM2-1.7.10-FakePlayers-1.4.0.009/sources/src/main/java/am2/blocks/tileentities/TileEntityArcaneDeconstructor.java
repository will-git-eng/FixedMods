package am2.blocks.tileentities;

import java.util.ArrayList;

import am2.AMCore;
import am2.api.blocks.IKeystoneLockable;
import am2.api.power.PowerTypes;
import am2.api.spell.component.interfaces.ISkillTreeEntry;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.component.interfaces.ISpellPart;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleHoldPosition;
import am2.power.PowerNodeRegistry;
import am2.spell.SkillManager;
import am2.spell.SpellUtils;
import am2.spell.components.Summon;
import am2.spell.shapes.Binding;
import am2.utility.InventoryUtilities;
import am2.utility.RecipeUtilities;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityArcaneDeconstructor extends TileEntityAMPower implements IInventory, ISidedInventory, IKeystoneLockable
{
	private int particleCounter;
	private static final float DECONSTRUCTION_POWER_COST = 1.25F;
	private static final int DECONSTRUCTION_TIME = 200;
	private int current_deconstruction_time = 0;
	private static final PowerTypes[] validPowerTypes = new PowerTypes[] { PowerTypes.DARK };
	@SideOnly(Side.CLIENT)
	AMParticle radiant;
	private ItemStack[] inventory = new ItemStack[this.getSizeInventory()];
	private ItemStack[] deconstructionRecipe;

	public TileEntityArcaneDeconstructor()
	{
		super(500);
	}

	@Override
	public boolean canRelayPower(PowerTypes type)
	{
		return false;
	}

	@Override
	public int getChargeRate()
	{
		return 250;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (super.worldObj.isRemote)
		{
			if (this.particleCounter == 0 || this.particleCounter++ > 1000)
			{
				this.particleCounter = 1;
				this.radiant = (AMParticle) AMCore.proxy.particleManager.spawn(super.worldObj, "radiant", super.xCoord + 0.5F, super.yCoord + 0.5F, super.zCoord + 0.5F);
				if (this.radiant != null)
				{
					this.radiant.setMaxAge(1000);
					this.radiant.setRGBColorF(0.1F, 0.1F, 0.1F);
					this.radiant.setParticleScale(0.1F);
					this.radiant.AddParticleController(new ParticleHoldPosition(this.radiant, 1000, 1, false));
				}
			}
		}
		else if (!this.isActive())
		{
			if (this.inventory[0] != null)
				this.current_deconstruction_time = 1;
		}
		else if (this.inventory[0] == null)
		{
			this.current_deconstruction_time = 0;
			this.deconstructionRecipe = null;
			super.worldObj.markBlockForUpdate(super.xCoord, super.yCoord, super.zCoord);
		}
		else if (PowerNodeRegistry.For(super.worldObj).checkPower(this, PowerTypes.DARK, 1.25F))
		{
			if (this.deconstructionRecipe == null)
			{
				if (!this.getDeconstructionRecipe())
				{
					this.transferOrEjectItem(this.inventory[0]);
					this.setInventorySlotContents(0, (ItemStack) null);
				}
			}
			else
			{
				if (this.current_deconstruction_time++ >= 200)
				{
					if (this.getDeconstructionRecipe())
						for (ItemStack stack : this.deconstructionRecipe)
							this.transferOrEjectItem(stack);

					this.deconstructionRecipe = null;
					this.decrStackSize(0, 1);
					this.current_deconstruction_time = 0;
				}

				if (this.current_deconstruction_time % 10 == 0)
					super.worldObj.markBlockForUpdate(super.xCoord, super.yCoord, super.zCoord);
			}

			PowerNodeRegistry.For(super.worldObj).consumePower(this, PowerTypes.DARK, 1.25F);
		}

	}

	private boolean getDeconstructionRecipe()
	{
		ItemStack checkStack = this.getStackInSlot(0);
		ArrayList<ItemStack> recipeItems = new ArrayList();
		if (checkStack == null)
			return false;
		else if (checkStack.getItem() == ItemsCommonProxy.spell)
		{
			int numStages = SpellUtils.instance.numStages(checkStack);

			for (int i = 0; i < numStages; ++i)
			{
				ISpellShape shape = SpellUtils.instance.getShapeForStage(checkStack, i);
				Object[] componentParts = shape.getRecipeItems();
				if (componentParts != null)
					for (Object o : componentParts)
					{
						ItemStack stack = this.objectToItemStack(o);
						if (stack != null)
						{
							if (stack.getItem() == ItemsCommonProxy.bindingCatalyst)
								stack.setItemDamage(((Binding) SkillManager.instance.getSkill("Binding")).getBindingType(checkStack));

							recipeItems.add(stack.copy());
						}
					}

				ISpellComponent[] components = SpellUtils.instance.getComponentsForStage(checkStack, i);

				for (ISpellComponent component : components)
				{
					componentParts = component.getRecipeItems();
					if (componentParts != null)
						for (Object o : componentParts)
						{
							ItemStack stack = this.objectToItemStack(o);
							if (stack != null)
							{
								if (stack.getItem() == ItemsCommonProxy.crystalPhylactery)
								{
									ItemsCommonProxy.crystalPhylactery.setSpawnClass(stack, ((Summon) SkillManager.instance.getSkill("Summon")).getSummonType(checkStack));
									ItemsCommonProxy.crystalPhylactery.addFill(stack, 100.0F);
								}

								recipeItems.add(stack.copy());
							}
						}
				}

				ISpellModifier[] modifiers = SpellUtils.instance.getModifiersForStage(checkStack, i);

				for (ISpellModifier modifier : modifiers)
				{
					componentParts = modifier.getRecipeItems();
					if (componentParts != null)
						for (Object o : componentParts)
						{
							ItemStack stack = this.objectToItemStack(o);
							if (stack != null)
								recipeItems.add(stack.copy());
						}
				}
			}

			int numShapeGroups = SpellUtils.instance.numShapeGroups(checkStack);

			for (int i = 0; i < numShapeGroups; ++i)
			{
				int[] parts = SpellUtils.instance.getShapeGroupParts(checkStack, i);

				for (int partID : parts)
				{
					ISkillTreeEntry entry = SkillManager.instance.getSkill(partID);
					if (entry != null && entry instanceof ISpellPart)
					{
						Object[] componentParts = ((ISpellPart) entry).getRecipeItems();
						if (componentParts != null)
							for (Object o : componentParts)
							{
								ItemStack stack = this.objectToItemStack(o);
								if (stack != null)
								{
									if (stack.getItem() == ItemsCommonProxy.bindingCatalyst)
										stack.setItemDamage(((Binding) SkillManager.instance.getSkill("Binding")).getBindingType(checkStack));

									recipeItems.add(stack.copy());
								}
							}
					}
				}
			}

			this.deconstructionRecipe = recipeItems.toArray(new ItemStack[recipeItems.size()]);
			return true;
		}
		else
		{
			IRecipe recipe = RecipeUtilities.getRecipeFor(checkStack);
			if (recipe == null)
				return false;
			else
			{
				Object[] recipeParts = RecipeUtilities.getRecipeItems(recipe);
				if (recipeParts != null && checkStack != null && recipe.getRecipeOutput() != null)
				{
					if (recipe.getRecipeOutput().getItem() == checkStack.getItem() && recipe.getRecipeOutput().getItemDamage() == checkStack.getItemDamage() && recipe.getRecipeOutput().stackSize > 1)
						return false;

					for (Object o : recipeParts)
					{
						ItemStack stack = this.objectToItemStack(o);
						if (stack != null && !stack.getItem().hasContainerItem(stack))
						{
							stack.stackSize = 1;
							recipeItems.add(stack.copy());
						}
					}
				}

				this.deconstructionRecipe = recipeItems.toArray(new ItemStack[recipeItems.size()]);
				return true;
			}
		}
	}

	private ItemStack objectToItemStack(Object o)
	{
		ItemStack output = null;
		if (o instanceof ItemStack)
			output = (ItemStack) o;
		else if (o instanceof Item)
			output = new ItemStack((Item) o);
		else if (o instanceof Block)
			output = new ItemStack((Block) o);
		else if (o instanceof ArrayList)
		{
			     output = this.objectToItemStack(((ArrayList) o).get(0));
			ArrayList list = (ArrayList) o;
			if (!list.isEmpty())
				output = this.objectToItemStack(list.get(0));
			    
		}

		if (output != null && output.stackSize == 0)
			output.stackSize = 1;

		return output;
	}

	private void transferOrEjectItem(ItemStack stack)
	{
		if (!super.worldObj.isRemote)
		{
			boolean eject = false;

			for (int i = -1; i <= 1; ++i)
				for (int j = -1; j <= 1; ++j)
					for (int k = -1; k <= 1; ++k)
						if (i != 0 || j != 0 || k != 0)
						{
							TileEntity te = super.worldObj.getTileEntity(super.xCoord + i, super.yCoord + j, super.zCoord + k);
							if (te != null && te instanceof IInventory)
								for (int side = 0; side < 6; ++side)
									if (InventoryUtilities.mergeIntoInventory((IInventory) te, stack, stack.stackSize, side))
										return;
						}

			EntityItem item = new EntityItem(super.worldObj);
			item.setPosition(super.xCoord + 0.5D, super.yCoord + 1.5D, super.zCoord + 0.5D);
			item.setEntityItemStack(stack);
			super.worldObj.spawnEntityInWorld(item);
		}
	}

	public boolean isActive()
	{
		return this.current_deconstruction_time > 0;
	}

	@Override
	public int getSizeInventory()
	{
		return 16;
	}

	@Override
	public ItemStack getStackInSlot(int var1)
	{
		return var1 >= this.inventory.length ? null : this.inventory[var1];
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
		return "ArcaneDeconstructor";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
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
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return i <= 9;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return i == 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i >= 1 && i <= 9;
	}

	@Override
	public ItemStack[] getRunesInKey()
	{
		return new ItemStack[] { this.inventory[13], this.inventory[14], this.inventory[15] };
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
	public PowerTypes[] getValidPowerTypes()
	{
		return validPowerTypes;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("DeconstructorInventory", 10);
		this.inventory = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			String tag = String.format("ArrayIndex", new Object[] { Integer.valueOf(i) });
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < this.inventory.length)
				this.inventory[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
		}

		this.current_deconstruction_time = nbttagcompound.getInteger("DeconstructionTime");
		if (this.current_deconstruction_time > 0)
			this.getDeconstructionRecipe();

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

		nbttagcompound.setTag("DeconstructorInventory", nbttaglist);
		nbttagcompound.setInteger("DeconstructionTime", this.current_deconstruction_time);
	}

	public int getProgressScaled(int i)
	{
		return this.current_deconstruction_time * i / 200;
	}
}
