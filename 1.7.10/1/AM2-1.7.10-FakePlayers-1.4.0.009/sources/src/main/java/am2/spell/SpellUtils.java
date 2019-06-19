package am2.spell;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.events.AffinityChangingEvent;
import am2.api.events.ModifierCalculatedEvent;
import am2.api.spell.ISpellUtils;
import am2.api.spell.component.interfaces.ISkillTreeEntry;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.component.interfaces.ISpellModifier;
import am2.api.spell.component.interfaces.ISpellPart;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.armor.ArmorHelper;
import am2.armor.ArsMagicaArmorMaterial;
import am2.enchantments.AMEnchantmentHelper;
import am2.items.ItemsCommonProxy;
import am2.playerextensions.AffinityData;
import am2.playerextensions.ExtendedProperties;
import am2.playerextensions.SkillData;
import am2.spell.components.Summon;
import am2.spell.shapes.MissingShape;
import am2.utility.KeyValuePair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class SpellUtils implements ISpellUtils
{
	private static final String CurShapeGroup_Identifier = "CurrentShapeGroup";
	private static final String NumShapeGroups_Identifier = "NumShapeGroups";
	private static final String ShapeGroup_Identifier = "ShapeGroup_";
	private static final String ShapeGroupMeta_Identifier = "ShapeGroupMeta_";
	private static final String Stages_Identifier = "NumStages";
	private static final String Shape_Prefix = "ShapeOrdinal_";
	private static final String Component_Prefix = "SpellComponentIDs_";
	private static final String Modifier_Prefix = "SpellModifierIDs_";
	private static final String Shape_Meta_Prefix = "ShapeMeta_";
	private static final String Component_Meta_Prefix = "SpellComponentMeta_";
	private static final String Modifier_Meta_Prefix = "SpellModifierMeta_";
	private static final String Global_Spell_Meta = "spellMetadata";
	private static final String BaseManaCostIdentifier = "BMC_";
	private static final String BaseBurnoutIdentifier = "BB_";
	private static final String BaseReagentsIdentifier = "BRR";
	private static final String ForcedAffinity = "ForcedAffinity";
	public static SpellUtils instance = new SpellUtils();

	@Override
	public double getModifiedDouble_Mul(double defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers check)
	{
		return this.getModifiedDouble_Mul(defaultValue, stack, caster, target, world, 0, check);
	}

	@Override
	public int getModifiedInt_Mul(int defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers check)
	{
		return this.getModifiedInt_Mul(defaultValue, stack, caster, target, world, 0, check);
	}

	@Override
	public double getModifiedDouble_Mul(SpellModifiers check, ItemStack stack, EntityLivingBase caster, Entity target, World world)
	{
		return this.getModifiedDouble_Mul(check, stack, caster, target, world, 0);
	}

	@Override
	public int getModifiedInt_Mul(SpellModifiers check, ItemStack stack, EntityLivingBase caster, Entity target, World world)
	{
		return this.getModifiedInt_Mul(check, stack, caster, target, world, 0);
	}

	@Override
	public double getModifiedDouble_Add(double defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers check)
	{
		return this.getModifiedDouble_Add(defaultValue, stack, caster, target, world, 0, check);
	}

	@Override
	public int getModifiedInt_Add(int defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, SpellModifiers check)
	{
		return this.getModifiedInt_Add(defaultValue, stack, caster, target, world, 0, check);
	}

	@Override
	public double getModifiedDouble_Add(SpellModifiers check, ItemStack stack, EntityLivingBase caster, Entity target, World world)
	{
		return this.getModifiedDouble_Add(check, stack, caster, target, world, 0);
	}

	@Override
	public int getModifiedInt_Add(SpellModifiers check, ItemStack stack, EntityLivingBase caster, Entity target, World world)
	{
		return this.getModifiedInt_Add(check, stack, caster, target, world, 0);
	}

	@Override
	public boolean modifierIsPresent(SpellModifiers check, ItemStack stack)
	{
		return this.modifierIsPresent(check, stack, 0);
	}

	@Override
	public int countModifiers(SpellModifiers check, ItemStack stack)
	{
		return this.countModifiers(check, stack, 0);
	}

	public double getModifiedDouble_Mul(double defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage, SpellModifiers check)
	{
		int ordinalCount = 0;
		double modifiedValue = defaultValue;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
			{
				byte[] meta = this.getModifierMetadataFromStack(stack, modifier, stage, ordinalCount++);
				modifiedValue *= modifier.getModifier(check, caster, target, world, meta);
			}

		if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer) caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("AugmentedCasting"))))
			modifiedValue *= 1.100000023841858D;

		ModifierCalculatedEvent event = new ModifierCalculatedEvent(stack, caster, check, defaultValue, modifiedValue, ModifierCalculatedEvent.OperationType.MULTIPLY);
		MinecraftForge.EVENT_BUS.post(event);
		return event.modifiedValue;
	}

	public int getModifiedInt_Mul(int defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage, SpellModifiers check)
	{
		int ordinalCount = 0;
		int modifiedValue = defaultValue;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
			{
				byte[] meta = this.getModifierMetadataFromStack(stack, modifier, stage, ordinalCount++);
				modifiedValue = (int) (modifiedValue * modifier.getModifier(check, caster, target, world, meta));
			}

		if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer) caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("AugmentedCasting"))))
			modifiedValue = (int) (modifiedValue * 1.1F);

		ModifierCalculatedEvent event = new ModifierCalculatedEvent(stack, caster, check, defaultValue, modifiedValue, ModifierCalculatedEvent.OperationType.MULTIPLY);
		MinecraftForge.EVENT_BUS.post(event);
		return (int) event.modifiedValue;
	}

	public double getModifiedDouble_Mul(SpellModifiers check, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage)
	{
		int ordinalCount = 0;
		double modifiedValue = check.defaultValue;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
			{
				byte[] meta = this.getModifierMetadataFromStack(stack, modifier, stage, ordinalCount++);
				modifiedValue *= modifier.getModifier(check, caster, target, world, meta);
			}

		if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer) caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("AugmentedCasting"))))
			modifiedValue *= 1.100000023841858D;

		ModifierCalculatedEvent event = new ModifierCalculatedEvent(stack, caster, check, check.defaultValue, modifiedValue, ModifierCalculatedEvent.OperationType.MULTIPLY);
		MinecraftForge.EVENT_BUS.post(event);
		return event.modifiedValue;
	}

	public int getModifiedInt_Mul(SpellModifiers check, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage)
	{
		int ordinalCount = 0;
		int modifiedValue = check.defaultValueInt;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
			{
				byte[] meta = this.getModifierMetadataFromStack(stack, modifier, stage, ordinalCount++);
				modifiedValue = (int) (modifiedValue * modifier.getModifier(check, caster, target, world, meta));
			}

		if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer) caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("AugmentedCasting"))))
			modifiedValue = (int) (modifiedValue * 1.1F);

		ModifierCalculatedEvent event = new ModifierCalculatedEvent(stack, caster, check, check.defaultValue, modifiedValue, ModifierCalculatedEvent.OperationType.MULTIPLY);
		MinecraftForge.EVENT_BUS.post(event);
		return (int) event.modifiedValue;
	}

	public double getModifiedDouble_Add(double defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage, SpellModifiers check)
	{
		int ordinalCount = 0;
		double modifiedValue = defaultValue;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
			{
				byte[] meta = this.getModifierMetadataFromStack(stack, modifier, stage, ordinalCount++);
				modifiedValue += modifier.getModifier(check, caster, target, world, meta);
			}

		if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer) caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("AugmentedCasting"))))
			modifiedValue *= 1.100000023841858D;

		ModifierCalculatedEvent event = new ModifierCalculatedEvent(stack, caster, check, defaultValue, modifiedValue, ModifierCalculatedEvent.OperationType.ADD);
		MinecraftForge.EVENT_BUS.post(event);
		return event.modifiedValue;
	}

	public int getModifiedInt_Add(int defaultValue, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage, SpellModifiers check)
	{
		int ordinalCount = 0;
		double modifiedValue = defaultValue;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
			{
				byte[] meta = this.getModifierMetadataFromStack(stack, modifier, stage, ordinalCount++);
				modifiedValue += modifier.getModifier(check, caster, target, world, meta);
			}

		if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer) caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("AugmentedCasting"))))
			modifiedValue *= 1.100000023841858D;

		ModifierCalculatedEvent event = new ModifierCalculatedEvent(stack, caster, check, defaultValue, modifiedValue, ModifierCalculatedEvent.OperationType.ADD);
		MinecraftForge.EVENT_BUS.post(event);
		return (int) Math.ceil(event.modifiedValue);
	}

	public double getModifiedDouble_Add(SpellModifiers check, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage)
	{
		int ordinalCount = 0;
		double modifiedValue = check.defaultValue;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
			{
				byte[] meta = this.getModifierMetadataFromStack(stack, modifier, stage, ordinalCount++);
				modifiedValue += modifier.getModifier(check, caster, target, world, meta);
			}

		if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer) caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("AugmentedCasting"))))
			modifiedValue *= 1.100000023841858D;

		ModifierCalculatedEvent event = new ModifierCalculatedEvent(stack, caster, check, check.defaultValue, modifiedValue, ModifierCalculatedEvent.OperationType.ADD);
		MinecraftForge.EVENT_BUS.post(event);
		return event.modifiedValue;
	}

	public int getModifiedInt_Add(SpellModifiers check, ItemStack stack, EntityLivingBase caster, Entity target, World world, int stage)
	{
		int ordinalCount = 0;
		int modifiedValue = check.defaultValueInt;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
			{
				byte[] meta = this.getModifierMetadataFromStack(stack, modifier, stage, ordinalCount++);
				modifiedValue = (int) (modifiedValue + modifier.getModifier(check, caster, target, world, meta));
			}

		if (caster instanceof EntityPlayer && SkillData.For((EntityPlayer) caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("AugmentedCasting"))))
			modifiedValue = (int) (modifiedValue * 1.1F);

		ModifierCalculatedEvent event = new ModifierCalculatedEvent(stack, caster, check, check.defaultValue, modifiedValue, ModifierCalculatedEvent.OperationType.ADD);
		MinecraftForge.EVENT_BUS.post(event);
		return (int) event.modifiedValue;
	}

	public boolean modifierIsPresent(SpellModifiers check, ItemStack stack, int stage)
	{
		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
				return true;

		return false;
	}

	public int countModifiers(SpellModifiers check, ItemStack stack, int stage)
	{
		int count = 0;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(check))
				++count;

		return count;
	}

	public int modifyDurationBasedOnArmor(EntityLivingBase caster, int baseDuration)
	{
		if (!(caster instanceof EntityPlayer))
			return baseDuration;
		else
		{
			int armorSet = ArmorHelper.getFullArsMagicaArmorSet((EntityPlayer) caster);
			if (armorSet == ArsMagicaArmorMaterial.MAGE.getMaterialID())
				baseDuration = (int) (baseDuration * 1.25F);
			else if (armorSet == ArsMagicaArmorMaterial.BATTLEMAGE.getMaterialID())
				baseDuration = (int) (baseDuration * 1.1F);
			else if (armorSet == ArsMagicaArmorMaterial.ARCHMAGE.getMaterialID())
				baseDuration = (int) (baseDuration * 2.0F);

			return baseDuration;
		}
	}

	public SpellUtils.SpellRequirements getSpellRequirements(ItemStack stack, EntityLivingBase caster)
	{
		if (!this.spellRequirementsPresent(stack, caster))
			this.writeSpellRequirements(stack, caster, this.calculateSpellRequirements(stack, caster));

		return this.modifySpellRequirementsByAffinity(stack, caster, this.parseSpellRequirements(stack, caster));
	}

	public SpellUtils.SpellRequirements modifySpellRequirementsByAffinity(ItemStack stack, EntityLivingBase caster, SpellUtils.SpellRequirements reqs)
	{
		HashMap<Affinity, Float> affinities = this.AffinityFor(stack);
		AffinityData affData = AffinityData.For(caster);
		if (affData == null)
			return reqs;
		else
		{
			float manaCost = reqs.manaCost;

			for (Affinity aff : affinities.keySet())
			{
				float depth = affData.getAffinityDepth(aff);
				float effectiveness = affinities.get(aff).floatValue();
				float multiplier = 0.5F * depth;
				float manaMod = manaCost * effectiveness;
				manaCost -= manaMod * multiplier;
			}

			return new SpellUtils.SpellRequirements(manaCost, reqs.burnout, reqs.reagents);
		}
	}

	private SpellUtils.SpellRequirements calculateSpellRequirements(ItemStack stack, EntityLivingBase caster)
	{
		float manaCost = 0.0F;
		float burnout = 0.0F;
		ArrayList<ItemStack> reagents = new ArrayList();
		int stages = this.numStages(stack);

		for (int i = stages - 1; i >= 0; --i)
		{
			float stageManaCost = 0.0F;
			float stageBurnout = 0.0F;
			ISpellShape shape = this.getShapeForStage(stack, i);
			ISpellComponent[] components = this.getComponentsForStage(stack, i);
			ISpellModifier[] modifiers = this.getModifiersForStage(stack, i);

			for (ISpellComponent component : components)
			{
				ItemStack[] componentReagents = component.reagents(caster);
				if (componentReagents != null)
					for (ItemStack reagentStack : componentReagents)
						reagents.add(reagentStack);

				stageManaCost += component.manaCost(caster);
				stageBurnout += component.burnout(caster);
			}

			HashMap<ISpellModifier, Integer> modifierWithQuantity = new HashMap();

			for (ISpellModifier modifier : modifiers)
				if (modifierWithQuantity.containsKey(modifier))
				{
					Integer qty = modifierWithQuantity.get(modifier);
					if (qty == null)
						qty = Integer.valueOf(1);

					qty = Integer.valueOf(qty.intValue() + 1);
					modifierWithQuantity.put(modifier, qty);
				}
				else
					modifierWithQuantity.put(modifier, Integer.valueOf(1));

			for (ISpellModifier modifier : modifierWithQuantity.keySet())
				stageManaCost *= modifier.getManaCostMultiplier(stack, i, modifierWithQuantity.get(modifier).intValue());

			manaCost += stageManaCost * shape.manaCostMultiplier(stack);
			burnout += stageBurnout;
		}

		return new SpellUtils.SpellRequirements(manaCost, burnout, reagents);
	}

	private SpellUtils.SpellRequirements parseSpellRequirements(ItemStack stack, EntityLivingBase caster)
	{
		float burnoutPct = ExtendedProperties.For(caster).getCurrentFatigue() / ExtendedProperties.For(caster).getMaxFatigue() + 1.0F;
		float manaCost = stack.stackTagCompound.getFloat("BMC_") * burnoutPct;
		float burnout = stack.stackTagCompound.getFloat("BB_");
		int[] reagentList = stack.stackTagCompound.getIntArray("BRR");
		ArrayList<ItemStack> reagents = new ArrayList();

		for (int i = 0; i < reagentList.length; i += 3)
			reagents.add(new ItemStack(Item.getItemById(reagentList[i]), reagentList[i + 1], reagentList[i + 2]));

		return new SpellUtils.SpellRequirements(manaCost, burnout, reagents);
	}

	private void writeSpellRequirements(ItemStack stack, EntityLivingBase caster, SpellUtils.SpellRequirements requirements)
	{
		if (stack.hasTagCompound())
		{
			stack.stackTagCompound.setFloat("BMC_", requirements.manaCost);
			stack.stackTagCompound.setFloat("BB_", requirements.burnout);
			int[] reagentList = new int[requirements.reagents.size() * 3];
			int count = 0;

			for (ItemStack reagentStack : requirements.reagents)
			{
				reagentList[count++] = Item.getIdFromItem(reagentStack.getItem());
				reagentList[count++] = reagentStack.stackSize;
				reagentList[count++] = reagentStack.getItemDamage();
			}

			stack.stackTagCompound.setIntArray("BRR", reagentList);
			this.writeModVersionToStack(stack);
		}
	}

	private boolean spellRequirementsPresent(ItemStack stack, EntityLivingBase caster)
	{
		return !stack.hasTagCompound() ? false : this.isOldVersionSpell(stack) ? false : stack.stackTagCompound.hasKey("BMC_") && stack.stackTagCompound.hasKey("BB_") && stack.stackTagCompound.hasKey("BRR");
	}

	public int[] getShapeGroupParts(ItemStack stack)
	{
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("CurrentShapeGroup"))
		{
			int currentShapeGroup = stack.stackTagCompound.getInteger("CurrentShapeGroup");
			return stack.stackTagCompound.getIntArray(String.format("%s%d", new Object[] { "ShapeGroup_", Integer.valueOf(currentShapeGroup) }));
		}
		else
			return new int[0];
	}

	public int[] getShapeGroupParts(ItemStack stack, int index)
	{
		return !stack.hasTagCompound() ? new int[0] : stack.stackTagCompound.getIntArray(String.format("%s%d", new Object[] { "ShapeGroup_", Integer.valueOf(index) }));
	}

	public void setShapeGroup(ItemStack stack, int shapeGroup)
	{
		if (stack.hasTagCompound())
		{
			if (shapeGroup < 0 || shapeGroup >= stack.stackTagCompound.getInteger("NumShapeGroups"))
				shapeGroup = 0;

			stack.stackTagCompound.setInteger("CurrentShapeGroup", shapeGroup);
			this.changeEnchantmentsForShapeGroup(stack);
		}
	}

	public int cycleShapeGroup(ItemStack stack)
	{
		if (!stack.hasTagCompound())
			return 0;
		else
		{
			int current = stack.stackTagCompound.getInteger("CurrentShapeGroup");
			int max = stack.stackTagCompound.getInteger("NumShapeGroups");
			return max == 0 ? 0 : (current + 1) % max;
		}
	}

	public void changeEnchantmentsForShapeGroup(ItemStack stack)
	{
		    
		if (stack == null)
			return;
		    

		ItemStack constructed = this.constructSpellStack(stack);
		int looting = 0;
		int silkTouch = 0;

		for (int i = 0; i < instance.numStages(constructed); ++i)
		{
			looting += instance.countModifiers(SpellModifiers.FORTUNE_LEVEL, constructed, i);
			silkTouch += instance.countModifiers(SpellModifiers.SILKTOUCH_LEVEL, constructed, i);
		}

		AMEnchantmentHelper.fortuneStack(stack, looting);
		AMEnchantmentHelper.lootingStack(stack, looting);
		AMEnchantmentHelper.silkTouchStack(stack, silkTouch);
	}

	public void addShapeGroup(int[] shapeGroupParts, byte[][] metaDatas, ItemStack stack)
	{
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		int numShapeGroups = stack.stackTagCompound.getInteger("NumShapeGroups") + 1;
		stack.stackTagCompound.setInteger("NumShapeGroups", numShapeGroups);
		stack.stackTagCompound.setInteger("CurrentShapeGroup", 0);
		stack.stackTagCompound.setIntArray(String.format("%s%d", new Object[] { "ShapeGroup_", Integer.valueOf(numShapeGroups - 1) }), shapeGroupParts);

		for (int i = 0; i < metaDatas.length; ++i)
			stack.stackTagCompound.setByteArray(String.format("%s%d%d", new Object[] { "ShapeGroupMeta_", Integer.valueOf(numShapeGroups - 1), Integer.valueOf(i) }), metaDatas[i]);

	}

	public ItemStack constructSpellStack(ItemStack stack)
	{
		    
		if (stack == null)
			return null;
		    

		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("CurrentShapeGroup"))
		{
			ItemStack classicStack = new ItemStack(ItemsCommonProxy.spell);
			classicStack.setTagCompound(new NBTTagCompound());
			if (stack.stackTagCompound.hasKey("spellMetadata"))
				classicStack.stackTagCompound.setTag("spellMetadata", stack.stackTagCompound.getTag("spellMetadata"));

			int[] shapeGroup = this.getShapeGroupParts(stack);
			ArrayList<SpellStageDefinition> newStages = new ArrayList();
			int currentShapeGroup = stack.stackTagCompound.getInteger("CurrentShapeGroup");
			int shapeGroupElementCount = 0;

			for (int i : shapeGroup)
			{
				ISkillTreeEntry entry = SkillManager.instance.getSkill(i);
				if (entry instanceof ISpellShape)
				{
					newStages.add(new SpellStageDefinition());
					newStages.get(newStages.size() - 1).shape = entry.getID();
				}
				else if (entry instanceof ISpellModifier)
				{
					byte[] meta = stack.stackTagCompound.getByteArray(String.format("%s%d%d", new Object[] { "ShapeGroupMeta_", Integer.valueOf(currentShapeGroup), Integer.valueOf(shapeGroupElementCount) }));
					if (meta == null)
						meta = new byte[0];

					newStages.get(newStages.size() - 1).definition.addModifier(SkillManager.instance.getShiftedPartID(entry), meta);
				}

				++shapeGroupElementCount;
			}

			if (this.numStages(stack) > 0 && newStages.size() > 0)
			{
				SpellStageDefinition last = newStages.get(newStages.size() - 1);
				int firstShape = stack.stackTagCompound.getInteger("ShapeOrdinal_0");
				if (firstShape == SkillManager.instance.missingShape.getID())
				{
					int[] components = stack.stackTagCompound.getIntArray("SpellComponentIDs_0");
					ISpellModifier[] modifiers = this.getModifiersForStage(stack, 0);

					for (int i : components)
						last.definition.addComponent(i);

					HashMap<Integer, Integer> ordinals = new HashMap();

					for (ISpellModifier modifier : modifiers)
					{
						int ordinal = 0;
						if (ordinals.containsKey(Integer.valueOf(modifier.getID())))
							ordinal = ordinals.get(Integer.valueOf(modifier.getID())).intValue();

						last.definition.addModifier(modifier.getID() + 5000, this.getModifierMetadataFromStack(stack, modifier, 0, ordinal));
					}
				}
			}

			for (SpellStageDefinition stage : newStages)
			{
				this.addSpellStageToScroll(classicStack, stage.shape, stage.definition.getComponents(), stage.definition.getModifiers());
				ISkillTreeEntry var24 = SkillManager.instance.getSkill(stage.shape);
			}

			for (int i = 0; i < this.numStages(stack); ++i)
			{
				SpellStageDefinition def = new SpellStageDefinition();
				def.shape = stack.stackTagCompound.getInteger("ShapeOrdinal_" + i);
				if (def.shape != SkillManager.instance.missingShape.getID())
				{
					int[] components = stack.stackTagCompound.getIntArray("SpellComponentIDs_" + i);

					for (int c : components)
						def.definition.addComponent(c);

					ISpellModifier[] modifiers = this.getModifiersForStage(stack, i);
					HashMap<Integer, Integer> ordinals = new HashMap();

					for (ISpellModifier modifier : modifiers)
					{
						int ordinal = 0;
						if (ordinals.containsKey(Integer.valueOf(modifier.getID())))
							ordinal = ordinals.get(Integer.valueOf(modifier.getID())).intValue();

						def.definition.addModifier(SkillManager.instance.getShiftedPartID(modifier), this.getModifierMetadataFromStack(stack, modifier, i, ordinal));
						ordinals.put(Integer.valueOf(modifier.getID()), Integer.valueOf(ordinal++));
					}

					this.addSpellStageToScroll(classicStack, def.shape, def.definition.getComponents(), def.definition.getModifiers());
				}
			}

			return classicStack;
		}
		else
			return stack.copy();
	}

	public ItemStack popStackStage(ItemStack stack)
	{
		if (!stack.hasTagCompound())
			return stack;
		else
		{
			ItemStack workingStack = stack.copy();
			int stages = this.numStages(workingStack);
			if (stages == 0)
				return workingStack;
			else
			{
				for (int i = 1; i < stages; ++i)
				{
					workingStack.stackTagCompound.setIntArray("SpellComponentIDs_" + (i - 1), workingStack.stackTagCompound.getIntArray("SpellComponentIDs_" + i));
					workingStack.stackTagCompound.setIntArray("SpellModifierIDs_" + (i - 1), workingStack.stackTagCompound.getIntArray("SpellModifierIDs_" + i));
					workingStack.stackTagCompound.setInteger("ShapeOrdinal_" + (i - 1), workingStack.stackTagCompound.getInteger("ShapeOrdinal_" + i));
				}

				workingStack.stackTagCompound.setInteger("NumStages", stages - 1);
				workingStack.stackTagCompound.removeTag("SpellComponentIDs_" + (stages - 1));
				workingStack.stackTagCompound.removeTag("SpellModifierIDs_" + (stages - 1));
				workingStack.stackTagCompound.removeTag("ShapeOrdinal_" + (stages - 1));
				return workingStack;
			}
		}
	}

	public int numStages(ItemStack stack)
	{
		if (stack != null && stack.hasTagCompound())
		{
			int numStages = stack.stackTagCompound.hasKey("NumStages") ? stack.stackTagCompound.getInteger("NumStages") : stack.stackTagCompound.getInteger("ShapeOrdinal_");
			return numStages;
		}
		else
			return 0;
	}

	public int numShapeGroups(ItemStack stack)
	{
		return !stack.hasTagCompound() ? 0 : stack.stackTagCompound.getInteger("NumShapeGroups");
	}

	public ISpellComponent[] getComponentsForStage(ItemStack stack, int stage)
	{
		if (stack != null && stack.hasTagCompound())
		{
			int[] componentIDs = stack.stackTagCompound.getIntArray("SpellComponentIDs_" + stage);
			ISpellComponent[] components = new ISpellComponent[componentIDs.length];
			int count = 0;

			for (int i : componentIDs)
			{
				ISkillTreeEntry component = SkillManager.instance.getSkill(i);
				if (SkillTreeManager.instance.isSkillDisabled(component))
					components[count++] = SkillManager.instance.missingComponent;
				else
					components[count++] = component != null && component instanceof ISpellComponent ? (ISpellComponent) component : SkillManager.instance.missingComponent;
			}

			return components;
		}
		else
			return new ISpellComponent[0];
	}

	public ISpellModifier[] getModifiersForStage(ItemStack stack, int stage)
	{
		if (stack != null && stack.hasTagCompound())
		{
			int[] modifierIDs = stack.stackTagCompound.getIntArray("SpellModifierIDs_" + stage);
			ISpellModifier[] modifiers = new ISpellModifier[modifierIDs.length];
			int count = 0;

			for (int i : modifierIDs)
			{
				ISkillTreeEntry modifier = SkillManager.instance.getSkill(i);
				if (SkillTreeManager.instance.isSkillDisabled(modifier))
					modifiers[count++] = SkillManager.instance.missingModifier;
				else
					modifiers[count++] = modifier != null && modifier instanceof ISpellModifier ? (ISpellModifier) modifier : SkillManager.instance.missingModifier;
			}

			return modifiers;
		}
		else
			return new ISpellModifier[0];
	}

	public ISpellShape getShapeForStage(ItemStack stack, int stage)
	{
		if (stack != null && stack.hasTagCompound())
		{
			int shapeIndex = stack.stackTagCompound.getInteger("ShapeOrdinal_" + stage);
			ISkillTreeEntry shape = SkillManager.instance.getSkill(shapeIndex);
			return SkillTreeManager.instance.isSkillDisabled(shape) ? SkillManager.instance.missingShape : shape != null && shape instanceof ISpellShape ? (ISpellShape) shape : SkillManager.instance.missingShape;
		}
		else
			return SkillManager.instance.missingShape;
	}

	public boolean casterHasAllReagents(EntityLivingBase caster, ArrayList<ItemStack> reagents)
	{
		return caster instanceof EntityPlayer && ((EntityPlayer) caster).capabilities.isCreativeMode ? true : true;
	}

	public boolean casterHasMana(EntityLivingBase caster, float mana)
	{
		return caster instanceof EntityPlayer && ((EntityPlayer) caster).capabilities.isCreativeMode ? true : ExtendedProperties.For(caster).getCurrentMana() + ExtendedProperties.For(caster).getBonusCurrentMana() >= mana;
	}

	public void addSpellStageToScroll(ItemStack scrollStack, int shape, int[] components, ListMultimap<Integer, byte[]> modifiers)
	{
		if (scrollStack.stackTagCompound == null)
			scrollStack.stackTagCompound = new NBTTagCompound();

		int nextStage = this.numStages(scrollStack);
		scrollStack.stackTagCompound.setInteger("NumStages", nextStage + 1);
		scrollStack.stackTagCompound.setInteger("ShapeOrdinal_" + nextStage, shape);
		scrollStack.stackTagCompound.setIntArray("SpellComponentIDs_" + nextStage, components);
		int[] modifierarray = new int[modifiers.values().size()];
		int index = 0;

		for (Integer modifierID : modifiers.keySet())
		{
			int ordinalCount = 0;

			for (byte[] meta : modifiers.get(modifierID))
			{
				ISpellModifier modifier = SkillManager.instance.getModifier(modifierID.intValue());
				if (modifier != SkillManager.instance.missingModifier)
				{
					modifierarray[index++] = modifierID.intValue();
					if (meta == null)
						meta = new byte[0];

					this.writeModifierMetadataToStack(scrollStack, modifier, nextStage, ordinalCount++, meta);
				}
			}
		}

		scrollStack.stackTagCompound.setIntArray("SpellModifierIDs_" + nextStage, modifierarray);
	}

	public Affinity mainAffinityFor(ItemStack stack)
	{
		if (!stack.hasTagCompound())
			return Affinity.NONE;
		else if (stack.stackTagCompound.hasKey("ForcedAffinity"))
		{
			int aff = stack.stackTagCompound.getInteger("ForcedAffinity");
			return Affinity.values()[aff];
		}
		else
		{
			HashMap<Integer, Integer> affinityFrequency = new HashMap();

			for (int i = 0; i < this.numStages(stack); ++i)
				for (ISpellComponent comp : this.getComponentsForStage(stack, i))
					for (Affinity affinity : comp.getAffinity())
						if (!affinityFrequency.containsKey(Integer.valueOf(affinity.ordinal())))
							affinityFrequency.put(Integer.valueOf(affinity.ordinal()), Integer.valueOf(1));
						else
						{
							int old = affinityFrequency.get(Integer.valueOf(affinity.ordinal())).intValue();
							affinityFrequency.put(Integer.valueOf(affinity.ordinal()), Integer.valueOf(old + 1));
						}

			int highestCount = 0;
			int highestID = 0;

			for (Integer key : affinityFrequency.keySet())
			{
				int count = affinityFrequency.get(key).intValue();
				if (count > highestCount)
				{
					highestID = key.intValue();
					highestCount = count;
				}
			}

			return Affinity.values()[highestID];
		}
	}

	public void doAffinityShift(EntityLivingBase caster, ISpellComponent component, ISpellShape governingShape)
	{
		if (caster instanceof EntityPlayer)
		{
			AffinityData aff = AffinityData.For(caster);

			for (Affinity affinity : component.getAffinity())
			{
				float shift = component.getAffinityShift(affinity) * aff.getDiminishingReturnsFactor() * 5.0F;
				float xp = 0.05F * aff.getDiminishingReturnsFactor();
				if (governingShape.isChanneled())
				{
					shift /= 4.0F;
					xp /= 4.0F;
				}

				if (caster instanceof EntityPlayer)
				{
					if (SkillData.For((EntityPlayer) caster).isEntryKnown(SkillTreeManager.instance.getSkillTreeEntry(SkillManager.instance.getSkill("AffinityGains"))))
					{
						shift *= 1.1F;
						xp *= 0.9F;
					}

					ItemStack chestArmor = ((EntityPlayer) caster).getCurrentArmor(2);
					if (chestArmor != null && ArmorHelper.isInfusionPreset(chestArmor, "mg_xp"))
						xp *= 1.25F;
				}

				if (shift > 0.0F)
				{
					AffinityChangingEvent event = new AffinityChangingEvent((EntityPlayer) caster, affinity, shift);
					MinecraftForge.EVENT_BUS.post(event);
					if (!event.isCanceled())
						aff.incrementAffinity(affinity, event.amount);
				}

				if (xp > 0.0F)
				{
					xp = (float) (xp * caster.getAttributeMap().getAttributeInstance(ArsMagicaApi.xpGainModifier).getAttributeValue());
					ExtendedProperties.For(caster).addMagicXP(xp);
				}
			}

			aff.addDiminishingReturns(governingShape.isChanneled());
		}
	}

	public HashMap<Affinity, Float> AffinityFor(ItemStack stack)
	{
		HashMap<Affinity, Integer> affinityFrequency = new HashMap();
		HashMap<Affinity, Float> affinities = new HashMap();
		float totalAffinityEntries = 0.0F;
		if (stack.stackTagCompound.hasKey("ForcedAffinity"))
		{
			int aff = stack.stackTagCompound.getInteger("ForcedAffinity");
			affinities.put(Affinity.values()[aff], Float.valueOf(100.0F));
			return affinities;
		}
		else
		{
			for (int i = 0; i < this.numStages(stack); ++i)
				for (ISpellComponent comp : this.getComponentsForStage(stack, i))
					if (comp != SkillManager.instance.missingComponent)
						for (Affinity affinity : comp.getAffinity())
						{
							++totalAffinityEntries;
							if (!affinityFrequency.containsKey(affinity))
								affinityFrequency.put(affinity, Integer.valueOf(1));
							else
							{
								int old = affinityFrequency.get(affinity).intValue();
								affinityFrequency.put(affinity, Integer.valueOf(old + 1));
							}
						}

			for (Affinity key : affinityFrequency.keySet())
			{
				int count = affinityFrequency.get(key).intValue();
				float percent = totalAffinityEntries > 0.0F ? count / totalAffinityEntries : 0.0F;
				affinities.put(key, Float.valueOf(percent));
			}

			return affinities;
		}
	}

	public void addSpellStageToScroll(ItemStack scrollStack, String shape, String[] components, String[] modifiers)
	{
		int spell_shape = SkillManager.instance.getShiftedPartID(SkillManager.instance.getSkill(shape));
		int[] spell_components = new int[components.length];
		ListMultimap<Integer, byte[]> spell_modifiers = ArrayListMultimap.create();

		for (int i = 0; i < spell_components.length; ++i)
			if (!components[i].equals(""))
				spell_components[i] = SkillManager.instance.getShiftedPartID(SkillManager.instance.getSkill(components[i]));

		for (int i = 0; i < modifiers.length; ++i)
			if (!modifiers[i].equals(""))
			{
				int modifierID = SkillManager.instance.getShiftedPartID(SkillManager.instance.getSkill(modifiers[i]));
				byte[] meta = new byte[0];
				spell_modifiers.put(Integer.valueOf(modifierID), meta);
			}

		this.addSpellStageToScroll(scrollStack, spell_shape, spell_components, spell_modifiers);
	}

	public float modifyDamage(EntityLivingBase caster, float damage)
	{
		float factor = (float) (ExtendedProperties.For(caster).getMagicLevel() < 20 ? 0.5D + 0.5D * ExtendedProperties.For(caster).getMagicLevel() / 19.0D : 1.0D + 1.0D * (ExtendedProperties.For(caster).getMagicLevel() - 20) / 79.0D);
		return damage * factor;
	}

	public void writeModVersionToStack(ItemStack stack)
	{
		if (stack.hasTagCompound())
			stack.stackTagCompound.setString("spell_mod_version", AMCore.instance.getVersion());
	}

	public void writeModifierMetadataToStack(ItemStack stack, ISpellModifier modifier, int stage, int ordinal, byte[] meta)
	{
		if (stack.hasTagCompound())
		{
			String identifier = String.format("%s%d_%d_%d", new Object[] { "SpellModifierMeta_", Integer.valueOf(modifier.getID()), Integer.valueOf(stage), Integer.valueOf(ordinal) });
			stack.stackTagCompound.setByteArray(identifier, meta);
		}
	}

	public byte[] getModifierMetadataFromStack(ItemStack stack, ISpellModifier modifier, int stage, int ordinal)
	{
		if (!stack.hasTagCompound())
			return new byte[0];
		else
		{
			String identifier = String.format("%s%d_%d_%d", new Object[] { "SpellModifierMeta_", Integer.valueOf(modifier.getID()), Integer.valueOf(stage), Integer.valueOf(ordinal) });
			return stack.stackTagCompound.getByteArray(identifier);
		}
	}

	public int getNextOrdinalForModifier(ItemStack stack, int stage, EnumSet<SpellModifiers> enumSet)
	{
		int ordinalCount = 0;

		for (ISpellModifier modifier : this.getModifiersForStage(stack, stage))
			if (modifier.getAspectsModified().contains(enumSet))
				++ordinalCount;

		return ordinalCount;
	}

	public boolean isOldVersionSpell(ItemStack stack)
	{
		if (!stack.hasTagCompound())
			return false;
		else
		{
			String version = stack.stackTagCompound.getString("spell_mod_version");
			return version != AMCore.instance.getVersion();
		}
	}

	public boolean componentIsPresent(ItemStack stack, Class clazz, int stage)
	{
		if (!stack.hasTagCompound())
			return false;
		else
		{
			ISpellComponent[] components = this.getComponentsForStage(stack, stage);

			for (ISpellComponent comp : components)
				if (comp.getClass() == clazz)
					return true;

			return false;
		}
	}

	public boolean spellIsChanneled(ItemStack stack)
	{
		ISpellShape shape = instance.getShapeForStage(stack, 0);
		if (this.numShapeGroups(stack) != 0 && shape instanceof MissingShape)
		{
			int[] parts = this.getShapeGroupParts(stack);
			ISpellShape finalShape = null;

			for (int i : parts)
			{
				ISkillTreeEntry entry = SkillManager.instance.getSkill(i);
				if (entry instanceof ISpellShape)
				{
					finalShape = (ISpellShape) entry;
					break;
				}
			}

			return finalShape != null ? finalShape.isChanneled() : false;
		}
		else
			return shape.isChanneled();
	}

	public ItemStack createSpellStack(ArrayList<ArrayList<KeyValuePair<ISpellPart, byte[]>>> shapeGroups, ArrayList<KeyValuePair<ISpellPart, byte[]>> spell)
	{
		ArrayList<KeyValuePair<ISpellPart, byte[]>> recipeCopy = (ArrayList) spell.clone();
		if (recipeCopy.size() > 0 && !(((KeyValuePair) recipeCopy.get(0)).getKey() instanceof ISpellShape))
			recipeCopy.add(0, new KeyValuePair(SkillManager.instance.missingShape, new byte[0]));

		ItemStack stack = new ItemStack(ItemsCommonProxy.spell);
		boolean hasSummon = false;

		while (recipeCopy.size() > 0)
		{
			ArrayList<Integer> components = new ArrayList();
			ArrayListMultimap<Integer, byte[]> modifiers = ArrayListMultimap.create();
			KeyValuePair<ISpellPart, byte[]> part = recipeCopy.get(0);
			recipeCopy.remove(0);
			if (part.getKey() instanceof ISpellShape)
			{
				ISpellShape shape = (ISpellShape) part.getKey();

				for (part = recipeCopy.size() > 0 ? (KeyValuePair) recipeCopy.get(0) : null; part != null && !(part.getKey() instanceof ISpellShape); part = recipeCopy.size() > 0 ? (KeyValuePair) recipeCopy.get(0) : null)
				{
					recipeCopy.remove(0);
					if (part.getKey() instanceof ISpellComponent)
					{
						components.add(Integer.valueOf(SkillManager.instance.getShiftedPartID(part.getKey())));
						if (part.getKey() instanceof Summon)
							hasSummon = true;
					}
					else if (part.getKey() instanceof ISpellModifier)
						modifiers.put(Integer.valueOf(SkillManager.instance.getShiftedPartID(part.getKey())), part.getValue());
				}

				if (hasSummon)
					((Summon) SkillManager.instance.getSkill("Summon")).setSummonType(stack, EntitySkeleton.class);

				instance.addSpellStageToScroll(stack, shape.getID(), this.ArrayListToIntArray(components), modifiers);
			}
		}

		for (int i = 0; i < shapeGroups.size(); ++i)
		{
			ArrayList<KeyValuePair<ISpellPart, byte[]>> shapeGroup = shapeGroups.get(i);
			if (shapeGroup.size() != 0)
			{
				int[] sgp = new int[shapeGroup.size()];
				byte[][] sgp_m = new byte[shapeGroup.size()][];

				for (int n = 0; n < shapeGroup.size(); ++n)
				{
					sgp[n] = SkillManager.instance.getShiftedPartID((ISkillTreeEntry) ((KeyValuePair) shapeGroup.get(n)).getKey());
					sgp_m[n] = (byte[]) ((KeyValuePair) shapeGroup.get(n)).getValue();
				}

				instance.addShapeGroup(sgp, sgp_m, stack);
			}
		}

		instance.writeModVersionToStack(stack);
		ItemStack checkStack = this.constructSpellStack(stack);
		int silkTouchLevel = 0;
		int fortuneLevel = 0;

		for (int i = 0; i < this.numStages(checkStack); ++i)
		{
			int st = this.countModifiers(SpellModifiers.SILKTOUCH_LEVEL, checkStack, 0);
			int fn = this.countModifiers(SpellModifiers.FORTUNE_LEVEL, checkStack, 0);
			if (st > silkTouchLevel)
				silkTouchLevel = st;

			if (fn > fortuneLevel)
				fortuneLevel = fn;
		}

		if (fortuneLevel > 0)
		{
			AMEnchantmentHelper.fortuneStack(stack, fortuneLevel);
			AMEnchantmentHelper.lootingStack(stack, fortuneLevel);
		}

		if (silkTouchLevel > 0)
			AMEnchantmentHelper.silkTouchStack(stack, silkTouchLevel);

		return stack;
	}

	private int[] ArrayListToIntArray(ArrayList<Integer> list)
	{
		int[] arr = new int[list.size()];

		for (int i = 0; i < arr.length; ++i)
			arr[i] = list.get(i).intValue();

		return arr;
	}

	public void setForcedAffinity(ItemStack stack, Affinity aff)
	{
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.stackTagCompound.setInteger("ForcedAffinity", aff.ordinal());
	}

	public String getSpellMetadata(ItemStack stack, String key)
	{
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("spellMetadata"))
		{
			NBTTagCompound metaComp = stack.stackTagCompound.getCompoundTag("spellMetadata");
			return metaComp.getString(key);
		}
		else
			return "";
	}

	public void setSpellMetadata(ItemStack stack, String string, String s)
	{
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		NBTTagCompound meta = stack.stackTagCompound.getCompoundTag("spellMetadata");
		meta.setString(string, s);
		stack.stackTagCompound.setTag("spellMetadata", meta);
	}

	public class SpellRequirements
	{
		public final float manaCost;
		public final float burnout;
		public final ArrayList<ItemStack> reagents;

		public SpellRequirements(float mana, float burnout, ArrayList<ItemStack> reagents)
		{
			this.manaCost = mana;
			this.burnout = burnout;
			this.reagents = reagents;
		}
	}
}
