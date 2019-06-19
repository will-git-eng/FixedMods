package am2.blocks.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import am2.AMCore;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.api.power.PowerTypes;
import am2.api.spell.component.interfaces.ISkillTreeEntry;
import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.component.interfaces.ISpellPart;
import am2.blocks.BlockAMOre;
import am2.blocks.BlocksCommonProxy;
import am2.items.ItemEssence;
import am2.items.ItemRune;
import am2.items.ItemsCommonProxy;
import am2.multiblock.IMultiblockStructureController;
import am2.network.AMDataReader;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleMoveOnHeading;
import am2.power.PowerNodeRegistry;
import am2.spell.SkillManager;
import am2.spell.SpellRecipeManager;
import am2.spell.SpellUtils;
import am2.spell.components.Summon;
import am2.spell.shapes.Binding;
import am2.utility.KeyValuePair;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityCraftingAltar extends TileEntityAMPower implements IMultiblockStructureController
{
	private MultiblockStructureDefinition primary;
	private MultiblockStructureDefinition secondary;
	private boolean isCrafting;
	private final ArrayList<ItemStack> allAddedItems;
	private final ArrayList<ItemStack> currentAddedItems;
	private final ArrayList<KeyValuePair<ISpellPart, byte[]>> spellDef;
	private final ArrayList<ArrayList<KeyValuePair<ISpellPart, byte[]>>> shapeGroups;
	private boolean allShapeGroupsAdded = false;
	private int currentKey = -1;
	private int checkCounter;
	private boolean structureValid;
	private MultiblockStructureDefinition.BlockCoord podiumLocation;
	private MultiblockStructureDefinition.BlockCoord switchLocation;
	private int maxEffects;
	private ItemStack addedPhylactery = null;
	private ItemStack addedBindingCatalyst = null;
	private int[] spellGuide;
	private int[] outputCombo;
	private int[][] shapeGroupGuide;
	private int currentConsumedPower = 0;
	private int ticksExisted = 0;
	private PowerTypes currentMainPowerTypes = PowerTypes.NONE;
	private static final byte CRAFTING_CHANGED = 1;
	private static final byte COMPONENT_ADDED = 2;
	private static final byte FULL_UPDATE = 3;
	private static final int augmatl_mutex = 2;
	private static final int lectern_mutex = 4;
	private MultiblockStructureDefinition.StructureGroup[] lecternGroup_primary;
	private MultiblockStructureDefinition.StructureGroup[] augMatl_primary;
	private MultiblockStructureDefinition.StructureGroup wood_primary;
	private MultiblockStructureDefinition.StructureGroup quartz_primary;
	private MultiblockStructureDefinition.StructureGroup netherbrick_primary;
	private MultiblockStructureDefinition.StructureGroup cobble_primary;
	private MultiblockStructureDefinition.StructureGroup brick_primary;
	private MultiblockStructureDefinition.StructureGroup sandstone_primary;
	private MultiblockStructureDefinition.StructureGroup witchwood_primary;
	private MultiblockStructureDefinition.StructureGroup[] lecternGroup_secondary;
	private MultiblockStructureDefinition.StructureGroup[] augMatl_secondary;
	private MultiblockStructureDefinition.StructureGroup wood_secondary;
	private MultiblockStructureDefinition.StructureGroup quartz_secondary;
	private MultiblockStructureDefinition.StructureGroup netherbrick_secondary;
	private MultiblockStructureDefinition.StructureGroup cobble_secondary;
	private MultiblockStructureDefinition.StructureGroup brick_secondary;
	private MultiblockStructureDefinition.StructureGroup sandstone_secondary;
	private MultiblockStructureDefinition.StructureGroup witchwood_secondary;
	private String currentSpellName = "";

	public TileEntityCraftingAltar()
	{
		super(500);
		this.setupMultiblock();
		this.allAddedItems = new ArrayList();
		this.currentAddedItems = new ArrayList();
		this.isCrafting = false;
		this.structureValid = false;
		this.checkCounter = 0;
		this.setNoPowerRequests();
		this.maxEffects = 2;
		this.spellDef = new ArrayList();
		this.shapeGroups = new ArrayList();

		for (int i = 0; i < 5; ++i)
			this.shapeGroups.add(new ArrayList());

	}

	private void setupMultiblock()
	{
		this.primary = new MultiblockStructureDefinition("craftingAltar_alt");
		Block[] augMatls = new Block[] { Blocks.glass, Blocks.coal_block, Blocks.redstone_block, Blocks.iron_block, Blocks.lapis_block, Blocks.gold_block, Blocks.diamond_block, Blocks.emerald_block, BlocksCommonProxy.AMOres, BlocksCommonProxy.AMOres };
		int[] var10000 = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		BlockAMOre var10003 = BlocksCommonProxy.AMOres;
		var10000[8] = 5;
		var10003 = BlocksCommonProxy.AMOres;
		var10000[9] = 8;
		int[] augMetas = var10000;
		this.lecternGroup_primary = new MultiblockStructureDefinition.StructureGroup[4];

		for (int i = 0; i < this.lecternGroup_primary.length; ++i)
			this.lecternGroup_primary[i] = this.primary.createGroup("lectern" + i, 4);

		int count = 0;

		for (int i = -2; i <= 2; i += 4)
		{
			this.primary.addAllowedBlock(this.lecternGroup_primary[count], i, -3, i, BlocksCommonProxy.blockLectern);
			this.primary.addAllowedBlock(this.lecternGroup_primary[count], i, -2, -i, Blocks.lever, count < 2 ? 2 : 1);
			this.primary.addAllowedBlock(this.lecternGroup_primary[count], i, -2, -i, Blocks.lever, count < 2 ? 10 : 9);
			++count;
			this.primary.addAllowedBlock(this.lecternGroup_primary[count], i, -3, -i, BlocksCommonProxy.blockLectern);
			this.primary.addAllowedBlock(this.lecternGroup_primary[count], i, -2, i, Blocks.lever, count < 2 ? 2 : 1);
			this.primary.addAllowedBlock(this.lecternGroup_primary[count], i, -2, i, Blocks.lever, count < 2 ? 10 : 9);
			++count;
		}

		this.augMatl_primary = new MultiblockStructureDefinition.StructureGroup[augMatls.length];

		for (int i = 0; i < augMatls.length; ++i)
			this.augMatl_primary[i] = this.primary.createGroup("augmatl" + i, 2);

		for (int i = 0; i < augMatls.length; ++i)
			this.primary.addAllowedBlock(this.augMatl_primary[i], -1, 0, -2, augMatls[i], augMetas[i]);

		this.primary.addAllowedBlock(-1, 0, -1, Blocks.stone_brick_stairs, 0);
		this.primary.addAllowedBlock(-1, 0, 0, Blocks.stone_brick_stairs, 0);
		this.primary.addAllowedBlock(-1, 0, 1, Blocks.stone_brick_stairs, 0);

		for (int i = 0; i < augMatls.length; ++i)
			this.primary.addAllowedBlock(this.augMatl_primary[i], -1, 0, 2, augMatls[i], augMetas[i]);

		this.primary.addAllowedBlock(0, 0, -2, Blocks.stone_brick_stairs, 2);
		this.primary.addAllowedBlock(0, 0, -1, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, 0, 0, BlocksCommonProxy.craftingAltar);
		this.primary.addAllowedBlock(0, 0, 1, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, 0, 2, Blocks.stone_brick_stairs, 3);

		for (int i = 0; i < augMatls.length; ++i)
			this.primary.addAllowedBlock(this.augMatl_primary[i], 1, 0, -2, augMatls[i], augMetas[i]);

		this.primary.addAllowedBlock(1, 0, -1, Blocks.stone_brick_stairs, 1);
		this.primary.addAllowedBlock(1, 0, 0, Blocks.stone_brick_stairs, 1);
		this.primary.addAllowedBlock(1, 0, 1, Blocks.stone_brick_stairs, 1);

		for (int i = 0; i < augMatls.length; ++i)
			this.primary.addAllowedBlock(this.augMatl_primary[i], 1, 0, 2, augMatls[i], augMetas[i]);

		this.primary.addAllowedBlock(1, -1, -2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(1, -1, -1, Blocks.stone_brick_stairs, 7);
		this.primary.addAllowedBlock(1, -1, 1, Blocks.stone_brick_stairs, 6);
		this.primary.addAllowedBlock(1, -1, 2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -1, -2, BlocksCommonProxy.magicWall, 0);
		this.primary.addAllowedBlock(0, -1, 2, BlocksCommonProxy.magicWall, 0);
		this.primary.addAllowedBlock(-1, -1, -2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(-1, -1, -1, Blocks.stone_brick_stairs, 7);
		this.primary.addAllowedBlock(-1, -1, 1, Blocks.stone_brick_stairs, 6);
		this.primary.addAllowedBlock(-1, -1, 2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(1, -2, -2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(1, -2, 2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -2, -2, BlocksCommonProxy.magicWall, 0);
		this.primary.addAllowedBlock(0, -2, 2, BlocksCommonProxy.magicWall, 0);
		this.primary.addAllowedBlock(-1, -2, -2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(-1, -2, 2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(1, -3, -2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(1, -3, 2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(0, -3, -2, BlocksCommonProxy.magicWall, 0);
		this.primary.addAllowedBlock(0, -3, 2, BlocksCommonProxy.magicWall, 0);
		this.primary.addAllowedBlock(-1, -3, -2, Blocks.stonebrick, 0);
		this.primary.addAllowedBlock(-1, -3, 2, Blocks.stonebrick, 0);

		for (int i = -2; i <= 2; ++i)
			for (int j = -2; j <= 2; ++j)
				if (i == 0 && j == 0)
					for (int n = 0; n < augMatls.length; ++n)
						this.primary.addAllowedBlock(this.augMatl_primary[n], i, -4, j, augMatls[n], augMetas[n]);
				else
					this.primary.addAllowedBlock(i, -4, j, Blocks.stonebrick, 0);

		this.wood_primary = this.primary.copyGroup("main", "main_wood");
		this.wood_primary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.planks);
		this.wood_primary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.oak_stairs);
		this.quartz_primary = this.primary.copyGroup("main", "main_quartz");
		this.quartz_primary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.quartz_block);
		this.quartz_primary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.quartz_stairs);
		this.netherbrick_primary = this.primary.copyGroup("main", "main_netherbrick");
		this.netherbrick_primary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.nether_brick);
		this.netherbrick_primary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.nether_brick_stairs);
		this.cobble_primary = this.primary.copyGroup("main", "main_cobble");
		this.cobble_primary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.cobblestone);
		this.cobble_primary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.stone_stairs);
		this.brick_primary = this.primary.copyGroup("main", "main_brick");
		this.brick_primary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.brick_block);
		this.brick_primary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.brick_stairs);
		this.sandstone_primary = this.primary.copyGroup("main", "main_sandstone");
		this.sandstone_primary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.sandstone);
		this.sandstone_primary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.sandstone_stairs);
		this.witchwood_primary = this.primary.copyGroup("main", "main_witchwood");
		this.witchwood_primary.replaceAllBlocksOfType(Blocks.stonebrick, BlocksCommonProxy.witchwoodPlanks);
		this.witchwood_primary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, BlocksCommonProxy.witchwoodStairs);
		this.secondary = new MultiblockStructureDefinition("craftingAltar");
		this.lecternGroup_secondary = new MultiblockStructureDefinition.StructureGroup[4];

		for (int i = 0; i < this.lecternGroup_secondary.length; ++i)
			this.lecternGroup_secondary[i] = this.secondary.createGroup("lectern" + i, 4);

		count = 0;

		for (int i = -2; i <= 2; i += 4)
		{
			this.secondary.addAllowedBlock(this.lecternGroup_secondary[count], i, -3, i, BlocksCommonProxy.blockLectern);
			this.secondary.addAllowedBlock(this.lecternGroup_secondary[count], -i, -2, i, Blocks.lever, count < 2 ? 4 : 3);
			this.secondary.addAllowedBlock(this.lecternGroup_secondary[count], -i, -2, i, Blocks.lever, count < 2 ? 12 : 11);
			++count;
			this.secondary.addAllowedBlock(this.lecternGroup_secondary[count], -i, -3, i, BlocksCommonProxy.blockLectern);
			this.secondary.addAllowedBlock(this.lecternGroup_secondary[count], i, -2, i, Blocks.lever, count < 2 ? 4 : 3);
			this.secondary.addAllowedBlock(this.lecternGroup_secondary[count], i, -2, i, Blocks.lever, count < 2 ? 12 : 11);
			++count;
		}

		this.augMatl_secondary = new MultiblockStructureDefinition.StructureGroup[augMatls.length];

		for (int i = 0; i < augMatls.length; ++i)
			this.augMatl_secondary[i] = this.secondary.createGroup("augmatl" + i, 2);

		for (int i = 0; i < augMatls.length; ++i)
			this.secondary.addAllowedBlock(this.augMatl_secondary[i], -2, 0, -1, augMatls[i], augMetas[i]);

		this.secondary.addAllowedBlock(-1, 0, -1, Blocks.stone_brick_stairs, 2);
		this.secondary.addAllowedBlock(0, 0, -1, Blocks.stone_brick_stairs, 2);
		this.secondary.addAllowedBlock(1, 0, -1, Blocks.stone_brick_stairs, 2);

		for (int i = 0; i < augMatls.length; ++i)
			this.secondary.addAllowedBlock(this.augMatl_secondary[i], 2, 0, -1, augMatls[i], augMetas[i]);

		this.secondary.addAllowedBlock(-2, 0, 0, Blocks.stone_brick_stairs, 0);
		this.secondary.addAllowedBlock(-1, 0, 0, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(0, 0, 0, BlocksCommonProxy.craftingAltar);
		this.secondary.addAllowedBlock(1, 0, 0, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(2, 0, 0, Blocks.stone_brick_stairs, 1);

		for (int i = 0; i < augMatls.length; ++i)
			this.secondary.addAllowedBlock(this.augMatl_secondary[i], -2, 0, 1, augMatls[i], augMetas[i]);

		this.secondary.addAllowedBlock(-1, 0, 1, Blocks.stone_brick_stairs, 3);
		this.secondary.addAllowedBlock(0, 0, 1, Blocks.stone_brick_stairs, 3);
		this.secondary.addAllowedBlock(1, 0, 1, Blocks.stone_brick_stairs, 3);

		for (int i = 0; i < augMatls.length; ++i)
			this.secondary.addAllowedBlock(this.augMatl_secondary[i], 2, 0, 1, augMatls[i], augMetas[i]);

		this.secondary.addAllowedBlock(-2, -1, 1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-1, -1, 1, Blocks.stone_brick_stairs, 5);
		this.secondary.addAllowedBlock(1, -1, 1, Blocks.stone_brick_stairs, 4);
		this.secondary.addAllowedBlock(2, -1, 1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-2, -1, 0, BlocksCommonProxy.magicWall, 0);
		this.secondary.addAllowedBlock(2, -1, 0, BlocksCommonProxy.magicWall, 0);
		this.secondary.addAllowedBlock(-2, -1, -1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-1, -1, -1, Blocks.stone_brick_stairs, 5);
		this.secondary.addAllowedBlock(1, -1, -1, Blocks.stone_brick_stairs, 4);
		this.secondary.addAllowedBlock(2, -1, -1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-2, -2, 1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(2, -2, 1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-2, -2, 0, BlocksCommonProxy.magicWall, 0);
		this.secondary.addAllowedBlock(2, -2, 0, BlocksCommonProxy.magicWall, 0);
		this.secondary.addAllowedBlock(-2, -2, -1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(2, -2, -1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-2, -3, 1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(2, -3, 1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(-2, -3, 0, BlocksCommonProxy.magicWall, 0);
		this.secondary.addAllowedBlock(2, -3, 0, BlocksCommonProxy.magicWall, 0);
		this.secondary.addAllowedBlock(-2, -3, -1, Blocks.stonebrick, 0);
		this.secondary.addAllowedBlock(2, -3, -1, Blocks.stonebrick, 0);

		for (int i = -2; i <= 2; ++i)
			for (int j = -2; j <= 2; ++j)
				if (i == 0 && j == 0)
					for (int n = 0; n < augMatls.length; ++n)
						this.secondary.addAllowedBlock(this.augMatl_secondary[n], i, -4, j, augMatls[n], augMetas[n]);
				else
					this.secondary.addAllowedBlock(i, -4, j, Blocks.stonebrick, 0);

		this.wood_secondary = this.secondary.copyGroup("main", "main_wood");
		this.wood_secondary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.planks);
		this.wood_secondary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.oak_stairs);
		this.quartz_secondary = this.secondary.copyGroup("main", "main_quartz");
		this.quartz_secondary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.quartz_block);
		this.quartz_secondary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.quartz_stairs);
		this.netherbrick_secondary = this.secondary.copyGroup("main", "main_netherbrick");
		this.netherbrick_secondary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.nether_brick);
		this.netherbrick_secondary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.nether_brick_stairs);
		this.cobble_secondary = this.secondary.copyGroup("main", "main_cobble");
		this.cobble_secondary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.cobblestone);
		this.cobble_secondary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.stone_stairs);
		this.brick_secondary = this.secondary.copyGroup("main", "main_brick");
		this.brick_secondary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.brick_block);
		this.brick_secondary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.brick_stairs);
		this.sandstone_secondary = this.secondary.copyGroup("main", "main_sandstone");
		this.sandstone_secondary.replaceAllBlocksOfType(Blocks.stonebrick, Blocks.sandstone);
		this.sandstone_secondary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, Blocks.sandstone_stairs);
		this.witchwood_secondary = this.secondary.copyGroup("main", "main_witchwood");
		this.witchwood_secondary.replaceAllBlocksOfType(Blocks.stonebrick, BlocksCommonProxy.witchwoodPlanks);
		this.witchwood_secondary.replaceAllBlocksOfType(Blocks.stone_brick_stairs, BlocksCommonProxy.witchwoodStairs);
	}

	@Override
	public MultiblockStructureDefinition getDefinition()
	{
		return this.secondary;
	}

	public ItemStack getNextPlannedItem()
	{
		if (this.spellGuide != null)
		{
			if (this.allAddedItems.size() * 3 < this.spellGuide.length)
			{
				int guide_id = this.spellGuide[this.allAddedItems.size() * 3];
				int guide_qty = this.spellGuide[this.allAddedItems.size() * 3 + 1];
				int guide_meta = this.spellGuide[this.allAddedItems.size() * 3 + 2];
				ItemStack stack = new ItemStack(Item.getItemById(guide_id), guide_qty, guide_meta);
				return stack;
			}
			else
				return new ItemStack(ItemsCommonProxy.spellParchment);
		}
		else
			return null;
	}

	private int getNumPartsInSpell()
	{
		int parts = 0;
		if (this.outputCombo != null)
			parts = this.outputCombo.length;

		if (this.shapeGroupGuide != null)
			for (int i = 0; i < this.shapeGroupGuide.length; ++i)
				if (this.shapeGroupGuide[i] != null)
					parts += this.shapeGroupGuide[i].length;

		return parts;
	}

	private boolean spellGuideIsWithinStructurePower()
	{
		return this.getNumPartsInSpell() <= this.maxEffects;
	}

	private boolean currentDefinitionIsWithinStructurePower()
	{
		int count = this.spellDef.size();

		for (ArrayList<KeyValuePair<ISpellPart, byte[]>> part : this.shapeGroups)
			count += part.size();

		return count <= this.maxEffects;
	}

	public boolean structureValid()
	{
		return this.structureValid;
	}

	public boolean isCrafting()
	{
		return this.isCrafting;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		++this.ticksExisted;
		this.checkStructure();
		this.checkForStartCondition();
		this.updateLecternInformation();
		if (this.isCrafting)
		{
			this.checkForEndCondition();
			this.updatePowerRequestData();
			if (!super.worldObj.isRemote && !this.currentDefinitionIsWithinStructurePower() && this.ticksExisted > 100)
			{
				super.worldObj.newExplosion((Entity) null, super.xCoord + 0.5D, super.yCoord - 1.5D, super.zCoord + 0.5D, 5.0F, false, true);
				this.setCrafting(false);
				return;
			}

			if (super.worldObj.isRemote && this.checkCounter == 1)
				AMCore.proxy.particleManager.RibbonFromPointToPoint(super.worldObj, super.xCoord + 0.5D, super.yCoord - 2, super.zCoord + 0.5D, super.xCoord + 0.5D, super.yCoord - 3, super.zCoord + 0.5D);

			List<EntityItem> components = this.lookForValidItems();
			ItemStack stack = this.getNextPlannedItem();

			for (EntityItem item : components)
				if (!item.isDead)
				{
					ItemStack entityItemStack = item.getEntityItem();
					if (stack != null && this.compareItemStacks(stack, entityItemStack))
						if (!super.worldObj.isRemote)
						{
							this.updateCurrentRecipe(item);
							item.setDead();
						}
						else
						{
							super.worldObj.playSound(super.xCoord, super.yCoord, super.zCoord, "arsmagica2:misc.craftingaltar.component_added", 1.0F, 0.4F + super.worldObj.rand.nextFloat() * 0.6F, false);

							for (int i = 0; i < 5 * AMCore.config.getGFXLevel(); ++i)
							{
								AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(super.worldObj, "radiant", item.posX, item.posY, item.posZ);
								if (particle != null)
								{
									particle.setMaxAge(40);
									particle.AddParticleController(new ParticleMoveOnHeading(particle, super.worldObj.rand.nextFloat() * 360.0F, super.worldObj.rand.nextFloat() * 360.0F, 0.009999999776482582D, 1, false));
									particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05F).setKillParticleOnFinish(true));
									particle.setParticleScale(0.02F);
									particle.setRGBColorF(super.worldObj.rand.nextFloat(), super.worldObj.rand.nextFloat(), super.worldObj.rand.nextFloat());
								}
							}
						}
				}
		}

	}

	private void updateLecternInformation()
	{
		if (this.podiumLocation != null)
		{
			TileEntity tile = super.worldObj.getTileEntity(super.xCoord + this.podiumLocation.getX(), super.yCoord + this.podiumLocation.getY(), super.zCoord + this.podiumLocation.getZ());

			     TileEntityLectern lectern = (TileEntityLectern) tile;
			TileEntityLectern lectern = tile instanceof TileEntityLectern ? (TileEntityLectern) tile : null;
			    

			if (lectern != null)
				if (lectern.hasStack())
				{
					ItemStack lecternStack = lectern.getStack();
					if (lecternStack.hasTagCompound())
					{
						this.spellGuide = lecternStack.getTagCompound().getIntArray("spell_combo");
						this.outputCombo = lecternStack.getTagCompound().getIntArray("output_combo");
						this.currentSpellName = lecternStack.getDisplayName();
						int numShapeGroups = lecternStack.getTagCompound().getInteger("numShapeGroups");
						this.shapeGroupGuide = new int[numShapeGroups][];

						for (int i = 0; i < numShapeGroups; ++i)
							this.shapeGroupGuide[i] = lecternStack.getTagCompound().getIntArray("shapeGroupCombo_" + i);
					}

					if (this.isCrafting)
					{
						if (this.spellGuide != null)
						{
							lectern.setNeedsBook(false);
							lectern.setTooltipStack(this.getNextPlannedItem());
						}
						else
							lectern.setNeedsBook(true);
					}
					else
						lectern.setTooltipStack((ItemStack) null);

					if (this.spellGuideIsWithinStructurePower())
						lectern.setOverpowered(false);
					else
						lectern.setOverpowered(true);
				}
				else
				{
					if (this.isCrafting)
						lectern.setNeedsBook(true);

					lectern.setTooltipStack((ItemStack) null);
				}

		}
	}

	public MultiblockStructureDefinition.BlockCoord getSwitchLocation()
	{
		return this.switchLocation;
	}

	public boolean switchIsOn()
	{
		if (this.switchLocation == null)
			return false;
		else
		{
			Block block = super.worldObj.getBlock(super.xCoord + this.switchLocation.getX(), super.yCoord + this.switchLocation.getY(), super.zCoord + this.switchLocation.getZ());
			boolean b = false;
			if (block == Blocks.lever)
				for (int i = 0; i < 6; ++i)
				{
					b |= Blocks.lever.isProvidingStrongPower(super.worldObj, super.xCoord + this.switchLocation.getX(), super.yCoord + this.switchLocation.getY(), super.zCoord + this.switchLocation.getZ(), i) > 0;
					if (b)
						break;
				}

			return b;
		}
	}

	public void flipSwitch()
	{
		if (this.switchLocation != null)
		{
			Block block = super.worldObj.getBlock(super.xCoord + this.switchLocation.getX(), super.yCoord + this.switchLocation.getY(), super.zCoord + this.switchLocation.getZ());
			if (block == Blocks.lever)
				Blocks.lever.onBlockActivated(super.worldObj, super.xCoord + this.switchLocation.getX(), super.yCoord + this.switchLocation.getY(), super.zCoord + this.switchLocation.getZ(), (EntityPlayer) null, 0, 0.0F, 0.0F, 0.0F);

		}
	}

	private void updatePowerRequestData()
	{
		ItemStack stack = this.getNextPlannedItem();
		if (stack != null && stack.getItem() instanceof ItemEssence && stack.getItemDamage() > 12)
		{
			if (this.switchIsOn())
			{
				int flags = stack.getItemDamage() - 12;
				this.setPowerRequests();
				this.pickPowerType(stack);
				if (this.currentMainPowerTypes != PowerTypes.NONE && PowerNodeRegistry.For(super.worldObj).checkPower(this, this.currentMainPowerTypes, 100.0F))
					this.currentConsumedPower = (int) (this.currentConsumedPower + PowerNodeRegistry.For(super.worldObj).consumePower(this, this.currentMainPowerTypes, Math.min(100, stack.stackSize - this.currentConsumedPower)));

				if (this.currentConsumedPower >= stack.stackSize)
				{
					PowerNodeRegistry.For(super.worldObj).setPower(this, this.currentMainPowerTypes, 0.0F);
					if (!super.worldObj.isRemote)
						this.addItemToRecipe(new ItemStack(ItemsCommonProxy.essence, stack.stackSize, 12 + flags));

					this.currentConsumedPower = 0;
					this.currentMainPowerTypes = PowerTypes.NONE;
					this.setNoPowerRequests();
					this.flipSwitch();
				}
			}
			else
				this.setNoPowerRequests();
		}
		else
			this.setNoPowerRequests();

	}

	@Override
	protected void setNoPowerRequests()
	{
		this.currentConsumedPower = 0;
		this.currentMainPowerTypes = PowerTypes.NONE;
		super.setNoPowerRequests();
	}

	private void pickPowerType(ItemStack stack)
	{
		if (this.currentMainPowerTypes == PowerTypes.NONE)
		{
			int flags = stack.getItemDamage() - 12;
			PowerTypes highestValid = PowerTypes.NONE;
			float amt = 0.0F;

			for (PowerTypes type : PowerTypes.all())
			{
				float tmpAmt = PowerNodeRegistry.For(super.worldObj).getPower(this, type);
				if (tmpAmt > amt)
					highestValid = type;
			}

			this.currentMainPowerTypes = highestValid;
		}
	}

	private void updateCurrentRecipe(EntityItem item)
	{
		ItemStack stack = item.getEntityItem();
		this.addItemToRecipe(stack);
	}

	private void addItemToRecipe(ItemStack stack)
	{
		this.allAddedItems.add(stack);
		this.currentAddedItems.add(stack);
		if (!super.worldObj.isRemote)
		{
			AMDataWriter writer = new AMDataWriter();
			writer.add(super.xCoord);
			writer.add(super.yCoord);
			writer.add(super.zCoord);
			writer.add((byte) 2);
			writer.add(stack);
			AMNetHandler.INSTANCE.sendPacketToAllClientsNear(super.worldObj.provider.dimensionId, super.xCoord, super.yCoord, super.zCoord, 32.0D, (byte) 4, writer.generate());
		}

		if (this.matchCurrentRecipe())
			this.currentAddedItems.clear();
	}

	private boolean matchCurrentRecipe()
	{
		ISpellPart part = SpellRecipeManager.instance.getPartByRecipe(this.currentAddedItems);
		if (part == null)
			return false;
		else
		{
			ArrayList<KeyValuePair<ISpellPart, byte[]>> currentShapeGroupList = this.getShapeGroupToAddTo();
			if (part instanceof Summon)
				this.handleSummonShape();

			if (part instanceof Binding)
				this.handleBindingShape();

			byte[] metaData = new byte[0];
			if (part instanceof ISpellModifier)
			{
				metaData = ((ISpellModifier) part).getModifierMetadata(this.currentAddedItems.toArray(new ItemStack[this.currentAddedItems.size()]));
				if (metaData == null)
					metaData = new byte[0];
			}

			if (currentShapeGroupList == null)
				this.spellDef.add(new KeyValuePair(part, metaData));
			else
				currentShapeGroupList.add(new KeyValuePair(part, metaData));

			return true;
		}
	}

	private ArrayList<KeyValuePair<ISpellPart, byte[]>> getShapeGroupToAddTo()
	{
		for (int i = 0; i < this.shapeGroupGuide.length; ++i)
		{
			int guideLength = this.shapeGroupGuide[i].length;
			int addedLength = ((ArrayList) this.shapeGroups.get(i)).size();
			if (addedLength < guideLength)
				return this.shapeGroups.get(i);
		}

		return null;
	}

	private void handleSummonShape()
	{
		if (this.currentAddedItems.size() > 2)
			this.addedPhylactery = this.currentAddedItems.get(this.currentAddedItems.size() - 2);

	}

	private void handleBindingShape()
	{
		if (this.currentAddedItems.size() == 7)
			this.addedBindingCatalyst = this.currentAddedItems.get(this.currentAddedItems.size() - 1);

	}

	private List<EntityItem> lookForValidItems()
	{
		if (!this.isCrafting)
			return new ArrayList();
		else
		{
			double radius = super.worldObj.isRemote ? 2.1D : 2.0D;
			List<EntityItem> items = super.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(super.xCoord - radius, super.yCoord - 3, super.zCoord - radius, super.xCoord + radius, super.yCoord, super.zCoord + radius));
			return items;
		}
	}

	private void checkStructure()
	{
		if ((!this.isCrafting || this.checkCounter++ >= 50) && (this.isCrafting || this.checkCounter++ >= 200))
		{
			this.checkCounter = 0;
			boolean primaryvalid = this.primary.checkStructure(super.worldObj, super.xCoord, super.yCoord, super.zCoord);
			boolean secondaryvalid = this.secondary.checkStructure(super.worldObj, super.xCoord, super.yCoord, super.zCoord);
			if (!primaryvalid && !secondaryvalid && this.isCrafting)
				this.setCrafting(false);

			if (!primaryvalid && !secondaryvalid)
			{
				this.podiumLocation = null;
				this.switchLocation = null;
				this.maxEffects = 0;
			}
			else
			{
				this.maxEffects = 0;
				ArrayList<MultiblockStructureDefinition.StructureGroup> lecternGroups = null;
				ArrayList<MultiblockStructureDefinition.StructureGroup> augmatlGroups = null;
				ArrayList<MultiblockStructureDefinition.StructureGroup> mainmatlGroups = null;
				if (primaryvalid)
				{
					lecternGroups = this.primary.getMatchedGroups(4, super.worldObj, super.xCoord, super.yCoord, super.zCoord);
					augmatlGroups = this.primary.getMatchedGroups(2, super.worldObj, super.xCoord, super.yCoord, super.zCoord);
					mainmatlGroups = this.primary.getMatchedGroups(1, super.worldObj, super.xCoord, super.yCoord, super.zCoord);
				}
				else if (secondaryvalid)
				{
					lecternGroups = this.secondary.getMatchedGroups(4, super.worldObj, super.xCoord, super.yCoord, super.zCoord);
					augmatlGroups = this.secondary.getMatchedGroups(2, super.worldObj, super.xCoord, super.yCoord, super.zCoord);
					mainmatlGroups = this.secondary.getMatchedGroups(1, super.worldObj, super.xCoord, super.yCoord, super.zCoord);
				}

				if (lecternGroups != null && lecternGroups.size() > 0)
				{
					MultiblockStructureDefinition.StructureGroup group = lecternGroups.get(0);
					HashMap<MultiblockStructureDefinition.BlockCoord, ArrayList<MultiblockStructureDefinition.BlockDec>> blocks = group.getAllowedBlocks();

					for (MultiblockStructureDefinition.BlockCoord bc : blocks.keySet())
					{
						Block block = super.worldObj.getBlock(super.xCoord + bc.getX(), super.yCoord + bc.getY(), super.zCoord + bc.getZ());
						if (block == BlocksCommonProxy.blockLectern)
							this.podiumLocation = bc;
						else if (block == Blocks.lever)
							this.switchLocation = bc;
					}
				}

				if (augmatlGroups != null && augmatlGroups.size() == 1)
				{
					MultiblockStructureDefinition.StructureGroup group = augmatlGroups.get(0);
					int index = -1;

					for (MultiblockStructureDefinition.StructureGroup augmatlGroup : primaryvalid ? this.augMatl_primary : this.augMatl_secondary)
					{
						++index;
						if (augmatlGroup == group)
							break;
					}

					this.maxEffects = index + 1;
				}

				if (mainmatlGroups != null && mainmatlGroups.size() == 1)
				{
					MultiblockStructureDefinition.StructureGroup group = mainmatlGroups.get(0);
					if (group != this.wood_primary && group != this.wood_secondary)
					{
						if (group != this.cobble_primary && group != this.cobble_secondary && group != this.sandstone_primary && group != this.sandstone_secondary)
						{
							if (group != this.brick_primary && group != this.brick_secondary && group != this.witchwood_primary && group != this.witchwood_secondary)
							{
								if (group != this.netherbrick_primary && group != this.netherbrick_secondary && group != this.quartz_primary && group != this.quartz_secondary)
									this.maxEffects += 2;
								else
									this.maxEffects += 3;
							}
							else
								this.maxEffects += 2;
						}
						else
							++this.maxEffects;
					}
					else
						++this.maxEffects;
				}
			}

			this.setStructureValid(primaryvalid || secondaryvalid);
		}
	}

	private void checkForStartCondition()
	{
		if (!super.worldObj.isRemote && this.structureValid && !this.isCrafting)
		{
			List<Entity> items = super.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(super.xCoord - 2, super.yCoord - 3, super.zCoord - 2, super.xCoord + 2, super.yCoord, super.zCoord + 2));
			if (items.size() == 1)
			{
				EntityItem item = (EntityItem) items.get(0);
				if (item != null && !item.isDead && item.getEntityItem().getItem() == ItemsCommonProxy.rune)
				{
					int var10000 = item.getEntityItem().getItemDamage();
					ItemRune var10001 = ItemsCommonProxy.rune;
					if (var10000 == 1)
					{
						item.setDead();
						this.setCrafting(true);
					}
				}
			}

		}
	}

	private void checkForEndCondition()
	{
		if (this.structureValid && this.isCrafting && super.worldObj != null)
		{
			double radius = super.worldObj.isRemote ? 2.2D : 2.0D;
			List<Entity> items = super.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(super.xCoord - radius, super.yCoord - 3, super.zCoord - radius, super.xCoord + radius, super.yCoord, super.zCoord + radius));
			if (items.size() == 1)
			{
				EntityItem item = (EntityItem) items.get(0);
				if (item != null && !item.isDead && item.getEntityItem() != null && item.getEntityItem().getItem() == ItemsCommonProxy.spellParchment)
					if (!super.worldObj.isRemote)
					{
						item.setDead();
						this.setCrafting(false);
						EntityItem craftedItem = new EntityItem(super.worldObj);
						craftedItem.setPosition(super.xCoord + 0.5D, super.yCoord - 1.5D, super.zCoord + 0.5D);
						ItemStack craftStack = SpellUtils.instance.createSpellStack(this.shapeGroups, this.spellDef);
						if (!craftStack.hasTagCompound())
							craftStack.stackTagCompound = new NBTTagCompound();

						this.AddSpecialMetadata(craftStack);
						craftStack.stackTagCompound.setString("suggestedName", this.currentSpellName != null ? this.currentSpellName : "");
						craftedItem.setEntityItemStack(craftStack);
						super.worldObj.spawnEntityInWorld(craftedItem);
						this.allAddedItems.clear();
						this.currentAddedItems.clear();
					}
					else
						super.worldObj.playSound(super.xCoord, super.yCoord, super.zCoord, "arsmagica2:misc.craftingaltar.create_spell", 1.0F, 1.0F, true);
			}

		}
	}

	private void AddSpecialMetadata(ItemStack craftStack)
	{
		if (this.addedPhylactery != null)
		{
			Summon summon = (Summon) SkillManager.instance.getSkill("Summon");
			summon.setSummonType(craftStack, this.addedPhylactery);
		}

		if (this.addedBindingCatalyst != null)
		{
			Binding binding = (Binding) SkillManager.instance.getSkill("Binding");
			binding.setBindingType(craftStack, this.addedBindingCatalyst);
		}

	}

	private void setCrafting(boolean crafting)
	{
		this.isCrafting = crafting;
		if (!super.worldObj.isRemote)
		{
			AMDataWriter writer = new AMDataWriter();
			writer.add(super.xCoord);
			writer.add(super.yCoord);
			writer.add(super.zCoord);
			writer.add((byte) 1);
			writer.add(crafting);
			AMNetHandler.INSTANCE.sendPacketToAllClientsNear(super.worldObj.provider.dimensionId, super.xCoord, super.yCoord, super.zCoord, 32.0D, (byte) 4, writer.generate());
		}

		if (crafting)
		{
			this.allAddedItems.clear();
			this.currentAddedItems.clear();
			this.spellDef.clear();

			for (ArrayList<KeyValuePair<ISpellPart, byte[]>> groups : this.shapeGroups)
				groups.clear();

			IPowerNode[] nodes = PowerNodeRegistry.For(super.worldObj).getAllNearbyNodes(super.worldObj, new AMVector3(this), PowerTypes.DARK);

			for (IPowerNode node : nodes)
				if (node instanceof TileEntityOtherworldAura)
				{
					((TileEntityOtherworldAura) node).setActive(true, this);
					break;
				}
		}

	}

	private void setStructureValid(boolean valid)
	{
		if (this.structureValid != valid)
		{
			this.structureValid = valid;
			super.worldObj.markBlockForUpdate(super.xCoord, super.yCoord, super.zCoord);
		}
	}

	public void deactivate()
	{
		if (!super.worldObj.isRemote)
		{
			this.setCrafting(false);
			Iterator var1 = this.allAddedItems.iterator();

			while (true)
			{
				ItemStack stack;
				while (true)
				{
					if (!var1.hasNext())
					{
						this.allAddedItems.clear();
						return;
					}

					stack = (ItemStack) var1.next();
					if (stack.getItem() != ItemsCommonProxy.essence)
						break;

					int var10000 = stack.getItemDamage();
					ItemEssence var10001 = ItemsCommonProxy.essence;
					if (var10000 <= 12)
						break;
				}

				EntityItem eItem = new EntityItem(super.worldObj);
				eItem.setPosition(super.xCoord, super.yCoord - 1, super.zCoord);
				eItem.setEntityItemStack(stack);
				super.worldObj.spawnEntityInWorld(eItem);
			}
		}
	}

	@Override
	public boolean canProvidePower(PowerTypes type)
	{
		return false;
	}

	private boolean compareItemStacks(ItemStack target, ItemStack input)
	{
		return target.getItem() == Items.potionitem && input.getItem() == Items.potionitem ? (target.getItemDamage() & 15) == (input.getItemDamage() & 15) : target.getItem() == input.getItem() && (target.getItemDamage() == input.getItemDamage() || target.getItemDamage() == 32767) && target.stackSize == input.stackSize;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		NBTTagCompound altarCompound = new NBTTagCompound();
		altarCompound.setBoolean("isCrafting", this.isCrafting);
		altarCompound.setInteger("currentKey", this.currentKey);
		altarCompound.setString("currentSpellName", this.currentSpellName);
		NBTTagList allAddedItemsList = new NBTTagList();

		for (ItemStack stack : this.allAddedItems)
		{
			NBTTagCompound addedItem = new NBTTagCompound();
			stack.writeToNBT(addedItem);
			allAddedItemsList.appendTag(addedItem);
		}

		altarCompound.setTag("allAddedItems", allAddedItemsList);
		NBTTagList currentAddedItemsList = new NBTTagList();

		for (ItemStack stack : this.currentAddedItems)
		{
			NBTTagCompound addedItem = new NBTTagCompound();
			stack.writeToNBT(addedItem);
			currentAddedItemsList.appendTag(addedItem);
		}

		altarCompound.setTag("currentAddedItems", currentAddedItemsList);
		if (this.addedPhylactery != null)
		{
			NBTTagCompound phylactery = new NBTTagCompound();
			this.addedPhylactery.writeToNBT(phylactery);
			altarCompound.setTag("phylactery", phylactery);
		}

		if (this.addedBindingCatalyst != null)
		{
			NBTTagCompound catalyst = new NBTTagCompound();
			this.addedBindingCatalyst.writeToNBT(catalyst);
			altarCompound.setTag("catalyst", catalyst);
		}

		NBTTagList shapeGroupData = new NBTTagList();

		for (ArrayList<KeyValuePair<ISpellPart, byte[]>> list : this.shapeGroups)
			shapeGroupData.appendTag(this.ISpellPartListToNBT(list));

		altarCompound.setTag("shapeGroups", shapeGroupData);
		NBTTagCompound spellDefSave = this.ISpellPartListToNBT(this.spellDef);
		altarCompound.setTag("spellDef", spellDefSave);
		nbttagcompound.setTag("altarData", altarCompound);
	}

	private NBTTagCompound ISpellPartListToNBT(ArrayList<KeyValuePair<ISpellPart, byte[]>> list)
	{
		NBTTagCompound shapeGroupData = new NBTTagCompound();
		int[] ids = new int[list.size()];
		byte[][] meta = new byte[list.size()][];

		for (int d = 0; d < list.size(); ++d)
		{
			ids[d] = SkillManager.instance.getShiftedPartID((ISkillTreeEntry) ((KeyValuePair) list.get(d)).getKey());
			meta[d] = (byte[]) ((KeyValuePair) list.get(d)).getValue();
		}

		shapeGroupData.setIntArray("group_ids", ids);

		for (int i = 0; i < meta.length; ++i)
			shapeGroupData.setByteArray("meta_" + i, meta[i]);

		return shapeGroupData;
	}

	private ArrayList<KeyValuePair<ISpellPart, byte[]>> NBTToISpellPartList(NBTTagCompound compound)
	{
		int[] ids = compound.getIntArray("group_ids");
		ArrayList<KeyValuePair<ISpellPart, byte[]>> list = new ArrayList();

		for (int i = 0; i < ids.length; ++i)
		{
			int var10000 = ids[i];
			ISkillTreeEntry part = SkillManager.instance.getSkill(i);
			byte[] partMeta = compound.getByteArray("meta_" + i);
			if (part instanceof ISpellPart)
				list.add(new KeyValuePair(part, partMeta));
		}

		return list;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("altarData"))
		{
			NBTTagCompound altarCompound = nbttagcompound.getCompoundTag("altarData");
			NBTTagList allAddedItems = altarCompound.getTagList("allAddedItems", 10);
			NBTTagList currentAddedItems = altarCompound.getTagList("currentAddedItems", 10);
			this.isCrafting = altarCompound.getBoolean("isCrafting");
			this.currentKey = altarCompound.getInteger("currentKey");
			this.currentSpellName = altarCompound.getString("currentSpellName");
			if (altarCompound.hasKey("phylactery"))
			{
				NBTTagCompound phylactery = altarCompound.getCompoundTag("phylactery");
				if (phylactery != null)
					this.addedPhylactery = ItemStack.loadItemStackFromNBT(phylactery);
			}

			if (altarCompound.hasKey("catalyst"))
			{
				NBTTagCompound catalyst = altarCompound.getCompoundTag("catalyst");
				if (catalyst != null)
					this.addedBindingCatalyst = ItemStack.loadItemStackFromNBT(catalyst);
			}

			this.allAddedItems.clear();

			for (int i = 0; i < allAddedItems.tagCount(); ++i)
			{
				NBTTagCompound addedItem = allAddedItems.getCompoundTagAt(i);
				if (addedItem != null)
				{
					ItemStack stack = ItemStack.loadItemStackFromNBT(addedItem);
					if (stack != null)
						this.allAddedItems.add(stack);
				}
			}

			this.currentAddedItems.clear();

			for (int i = 0; i < currentAddedItems.tagCount(); ++i)
			{
				NBTTagCompound addedItem = currentAddedItems.getCompoundTagAt(i);
				if (addedItem != null)
				{
					ItemStack stack = ItemStack.loadItemStackFromNBT(addedItem);
					if (stack != null)
						this.currentAddedItems.add(stack);
				}
			}

			this.spellDef.clear();

			for (ArrayList<KeyValuePair<ISpellPart, byte[]>> groups : this.shapeGroups)
				groups.clear();

			NBTTagCompound currentSpellDef = altarCompound.getCompoundTag("spellDef");
			this.spellDef.addAll(this.NBTToISpellPartList(currentSpellDef));
			NBTTagList currentShapeGroups = altarCompound.getTagList("shapeGroups", 10);

			for (int i = 0; i < currentShapeGroups.tagCount(); ++i)
			{
				NBTTagCompound compound = currentShapeGroups.getCompoundTagAt(i);
				((ArrayList) this.shapeGroups.get(i)).addAll(this.NBTToISpellPartList(compound));
			}

		}
	}

	@Override
	public int getChargeRate()
	{
		return 250;
	}

	@Override
	public boolean canRelayPower(PowerTypes type)
	{
		return false;
	}

	public void HandleUpdatePacket(byte[] remainingBytes)
	{
		AMDataReader rdr = new AMDataReader(remainingBytes, false);
		byte subID = rdr.getByte();
		switch (subID)
		{
			case 1:
				this.setCrafting(rdr.getBoolean());
				break;
			case 2:
				this.allAddedItems.add(rdr.getItemStack());
				break;
			case 3:
				this.isCrafting = rdr.getBoolean();
				this.currentKey = rdr.getInt();
				this.allAddedItems.clear();
				this.currentAddedItems.clear();
				int itemCount = rdr.getInt();

				for (int i = 0; i < itemCount; ++i)
					this.allAddedItems.add(rdr.getItemStack());
		}

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
}
