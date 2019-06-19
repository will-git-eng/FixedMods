package am2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

import ru.will.git.am2.EventConfig;

import am2.armor.ArmorHelper;
import am2.enchantments.AMEnchantments;
import am2.network.AMDataWriter;
import am2.network.AMNetHandler;
import am2.playerextensions.AffinityData;
import am2.playerextensions.ExtendedProperties;
import am2.playerextensions.RiftStorage;
import am2.playerextensions.SkillData;
import am2.proxy.tick.ServerTickHandler;
import am2.spell.SkillTreeManager;
import am2.utility.EntityUtilities;
import am2.utility.WebRequestUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PlayerTracker
{
	public static HashMap<UUID, NBTTagCompound> storedExtProps_death;
	public static HashMap<UUID, NBTTagCompound> riftStorage_death;
	public static HashMap<UUID, NBTTagCompound> affinityStorage_death;
	public static HashMap<UUID, NBTTagCompound> spellKnowledgeStorage_death;
	public static HashMap<UUID, NBTTagCompound> storedExtProps_dimension;
	public static HashMap<UUID, NBTTagCompound> riftStorage_dimension;
	public static HashMap<UUID, NBTTagCompound> affinityStorage_dimension;
	public static HashMap<UUID, NBTTagCompound> spellKnowledgeStorage_dimension;
	public static HashMap<UUID, HashMap<Integer, ItemStack>> soulbound_Storage;
	private TreeMap<String, Integer> aals;
	private TreeMap<String, String> clls;
	private TreeMap<String, Integer> cldm;

	public PlayerTracker()
	{
		storedExtProps_death = new HashMap();
		storedExtProps_dimension = new HashMap();
		affinityStorage_death = new HashMap();
		spellKnowledgeStorage_death = new HashMap();
		riftStorage_death = new HashMap();
		riftStorage_dimension = new HashMap();
		affinityStorage_dimension = new HashMap();
		spellKnowledgeStorage_dimension = new HashMap();
		soulbound_Storage = new HashMap();
		this.aals = new TreeMap();
		this.clls = new TreeMap();
		this.cldm = new TreeMap();
	}

	public void postInit()
	{
		this.populateAALList();
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event)
	{
		if (this.hasAA(event.player))
			AMNetHandler.INSTANCE.requestClientAuras((EntityPlayerMP) event.player);

		int[] disabledSkills = SkillTreeManager.instance.getDisabledSkillIDs();
		AMDataWriter writer = new AMDataWriter();
		writer.add(AMCore.config.getSkillTreeSecondaryTierCap()).add(disabledSkills);
		writer.add(AMCore.config.getManaCap());
		byte[] data = writer.generate();
		AMNetHandler.INSTANCE.syncLoginData((EntityPlayerMP) event.player, data);
		if (ServerTickHandler.lastWorldName != null)
			AMNetHandler.INSTANCE.syncWorldName((EntityPlayerMP) event.player, ServerTickHandler.lastWorldName);

	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event)
	{
		if (!event.player.worldObj.isRemote)
			for (Object o : new ArrayList(event.player.worldObj.loadedEntityList))
				if (o instanceof EntityLivingBase && EntityUtilities.isSummon((EntityLivingBase) o) && EntityUtilities.getOwner((EntityLivingBase) o) == event.player.getEntityId())
					((EntityLivingBase) o).setDead();

	}

	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event)
	{
		if (!event.player.worldObj.isRemote)
		{
			storeExtendedPropertiesForDimensionChange(event.player);

			for (Object o : event.player.worldObj.loadedEntityList)
				if (o instanceof EntityLivingBase && EntityUtilities.isSummon((EntityLivingBase) o) && EntityUtilities.getOwner((EntityLivingBase) o) == event.player.getEntityId())
					((EntityLivingBase) o).setDead();

			ExtendedProperties.For(event.player).setDelayedSync(40);
			AffinityData.For(event.player).setDelayedSync(40);
			SkillData.For(event.player).setDelayedSync(40);
		}

	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if (storedExtProps_death.containsKey(event.player.getUniqueID()))
		{
			NBTTagCompound stored = storedExtProps_death.get(event.player.getUniqueID());
			storedExtProps_death.remove(event.player.getUniqueID());
			ExtendedProperties.For(event.player).loadNBTData(stored);
			ExtendedProperties.For(event.player).setDelayedSync(40);
		}
		else if (storedExtProps_dimension.containsKey(event.player.getUniqueID()))
		{
			NBTTagCompound stored = storedExtProps_dimension.get(event.player.getUniqueID());
			storedExtProps_dimension.remove(event.player.getUniqueID());
			ExtendedProperties.For(event.player).loadNBTData(stored);
			ExtendedProperties.For(event.player).setDelayedSync(40);
		}

		if (riftStorage_death.containsKey(event.player.getUniqueID()))
		{
			NBTTagCompound stored = riftStorage_death.get(event.player.getUniqueID());
			riftStorage_death.remove(event.player.getUniqueID());
			RiftStorage.For(event.player).loadNBTData(stored);
		}
		else if (riftStorage_dimension.containsKey(event.player.getUniqueID()))
		{
			NBTTagCompound stored = riftStorage_dimension.get(event.player.getUniqueID());
			riftStorage_dimension.remove(event.player.getUniqueID());
			RiftStorage.For(event.player).loadNBTData(stored);
		}

		if (affinityStorage_death.containsKey(event.player.getUniqueID()))
		{
			NBTTagCompound stored = affinityStorage_death.get(event.player.getUniqueID());
			affinityStorage_death.remove(event.player.getUniqueID());
			AffinityData.For(event.player).loadNBTData(stored);
		}
		else if (affinityStorage_dimension.containsKey(event.player.getUniqueID()))
		{
			NBTTagCompound stored = affinityStorage_dimension.get(event.player.getUniqueID());
			affinityStorage_dimension.remove(event.player.getUniqueID());
			AffinityData.For(event.player).loadNBTData(stored);
		}

		if (spellKnowledgeStorage_death.containsKey(event.player.getUniqueID()))
		{
			NBTTagCompound stored = spellKnowledgeStorage_death.get(event.player.getUniqueID());
			spellKnowledgeStorage_death.remove(event.player.getUniqueID());
			SkillData.For(event.player).loadNBTData(stored);
		}
		else if (spellKnowledgeStorage_dimension.containsKey(event.player.getUniqueID()))
		{
			NBTTagCompound stored = spellKnowledgeStorage_dimension.get(event.player.getUniqueID());
			spellKnowledgeStorage_dimension.remove(event.player.getUniqueID());
			SkillData.For(event.player).loadNBTData(stored);
		}

		if (soulbound_Storage.containsKey(event.player.getUniqueID()))
		{
			HashMap<Integer, ItemStack> soulboundItems = soulbound_Storage.get(event.player.getUniqueID());

			for (Integer i : soulboundItems.keySet())
				if (i.intValue() < event.player.inventory.getSizeInventory())
					event.player.inventory.setInventorySlotContents(i.intValue(), soulboundItems.get(i));
				else
					event.player.entityDropItem(soulboundItems.get(i), 0.0F);
		}

	}

	public void onPlayerDeath(EntityPlayer player)
	{
		storeExtendedPropertiesForRespawn(player);
		storeSoulboundItemsForRespawn(player);
	}

	public static void storeExtendedPropertiesForRespawn(EntityPlayer player)
	{
		if (storedExtProps_death.containsKey(player.getUniqueID()))
			storedExtProps_death.remove(player.getUniqueID());

		NBTTagCompound save = new NBTTagCompound();
		ExtendedProperties.For(player).saveNBTData(save);
		storedExtProps_death.put(player.getUniqueID(), save);
		if (riftStorage_death.containsKey(player.getUniqueID()))
			riftStorage_death.remove(player.getUniqueID());

		NBTTagCompound saveRift = new NBTTagCompound();
		RiftStorage.For(player).saveNBTData(saveRift);
		riftStorage_death.put(player.getUniqueID(), saveRift);
		if (affinityStorage_death.containsKey(player.getUniqueID()))
			affinityStorage_death.remove(player.getUniqueID());

		NBTTagCompound saveAffinity = new NBTTagCompound();
		AffinityData.For(player).saveNBTData(saveAffinity);
		affinityStorage_death.put(player.getUniqueID(), saveAffinity);
		if (spellKnowledgeStorage_death.containsKey(player.getUniqueID()))
			spellKnowledgeStorage_death.remove(player.getUniqueID());

		NBTTagCompound saveSpellKnowledge = new NBTTagCompound();
		SkillData.For(player).saveNBTData(saveSpellKnowledge);
		spellKnowledgeStorage_death.put(player.getUniqueID(), saveSpellKnowledge);
	}

	public static void storeSoulboundItemsForRespawn(EntityPlayer player)
	{
		if (soulbound_Storage.containsKey(player.getUniqueID()))
			soulbound_Storage.remove(player.getUniqueID());

		HashMap<Integer, ItemStack> soulboundItems = new HashMap();
		int slotCount = 0;

		for (ItemStack stack : player.inventory.mainInventory)
		{
			int soulbound_level = EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound.effectId, stack);
			if (soulbound_level > 0)
			{
				    
				if (!EventConfig.enableSoulbound || EventConfig.inList(EventConfig.soulboundBlackList, stack))
				{
					slotCount++;
					continue;
				}
				    

				soulboundItems.put(Integer.valueOf(slotCount), stack.copy());
				player.inventory.setInventorySlotContents(slotCount, (ItemStack) null);
			}

			++slotCount;
		}

		slotCount = 0;

		for (ItemStack stack : player.inventory.armorInventory)
		{
			int soulbound_level = EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound.effectId, stack);
			if (soulbound_level > 0 || ArmorHelper.isInfusionPreset(stack, "soulbnd"))
			{
				    
				if (!EventConfig.enableSoulbound || EventConfig.inList(EventConfig.soulboundBlackList, stack))
				{
					slotCount++;
					continue;
				}
				    

				soulboundItems.put(Integer.valueOf(slotCount + player.inventory.mainInventory.length), stack.copy());
				player.inventory.setInventorySlotContents(slotCount + player.inventory.mainInventory.length, (ItemStack) null);
			}

			++slotCount;
		}

		soulbound_Storage.put(player.getUniqueID(), soulboundItems);
	}

	public static void storeExtendedPropertiesForDimensionChange(EntityPlayer player)
	{
		if (!storedExtProps_death.containsKey(player.getUniqueID()))
		{
			if (storedExtProps_dimension.containsKey(player.getUniqueID()))
				storedExtProps_dimension.remove(player.getUniqueID());

			NBTTagCompound saveExprop = new NBTTagCompound();
			ExtendedProperties.For(player).saveNBTData(saveExprop);
			storedExtProps_dimension.put(player.getUniqueID(), saveExprop);
		}

		if (!riftStorage_death.containsKey(player.getUniqueID()))
		{
			if (riftStorage_dimension.containsKey(player.getUniqueID()))
				riftStorage_dimension.remove(player.getUniqueID());

			NBTTagCompound saveRift = new NBTTagCompound();
			RiftStorage.For(player).saveNBTData(saveRift);
			riftStorage_dimension.put(player.getUniqueID(), saveRift);
		}

		if (!affinityStorage_death.containsKey(player.getUniqueID()))
		{
			if (affinityStorage_dimension.containsKey(player.getUniqueID()))
				affinityStorage_dimension.remove(player.getUniqueID());

			NBTTagCompound saveAffinity = new NBTTagCompound();
			AffinityData.For(player).saveNBTData(saveAffinity);
			affinityStorage_dimension.put(player.getUniqueID(), saveAffinity);
		}

		if (!spellKnowledgeStorage_death.containsKey(player.getUniqueID()))
		{
			if (spellKnowledgeStorage_dimension.containsKey(player.getUniqueID()))
				spellKnowledgeStorage_dimension.remove(player.getUniqueID());

			NBTTagCompound spellKnowledge = new NBTTagCompound();
			SkillData.For(player).saveNBTData(spellKnowledge);
			spellKnowledgeStorage_dimension.put(player.getUniqueID(), spellKnowledge);
		}

	}

	public static void storeSoulboundItemForRespawn(EntityPlayer player, ItemStack stack)
	{
		if (soulbound_Storage.containsKey(player.getUniqueID()))
		{
			HashMap<Integer, ItemStack> soulboundItems = soulbound_Storage.get(player.getUniqueID());
			int slotTest = 0;

			while (soulboundItems.containsKey(Integer.valueOf(slotTest)))
			{
				++slotTest;
				if (slotTest == player.inventory.mainInventory.length)
					slotTest += player.inventory.armorInventory.length;
			}

			soulboundItems.put(Integer.valueOf(slotTest), stack);
		}
	}

	public boolean hasAA(EntityPlayer entity)
	{
		return this.getAAL(entity) > 0;
	}

	public int getAAL(EntityPlayer thePlayer)
	{
		try
		{
			thePlayer.getDisplayName();
		}
		catch (Throwable var3)
		{
			return 0;
		}

		if (this.aals == null || this.clls == null)
			this.populateAALList();

		return this.aals.containsKey(thePlayer.getDisplayName().toLowerCase()) ? this.aals.get(thePlayer.getDisplayName().toLowerCase()).intValue() : 0;
	}

	private void populateAALList()
	{
		this.aals = new TreeMap();
		this.clls = new TreeMap();
		this.cldm = new TreeMap();
		String dls = "http://qorconcept.com/mc/AREW0152.txt";
		char[] dl = dls.toCharArray();

		try
		{
			String s = WebRequestUtils.sendPost(new String(dl), new HashMap());
			String[] lines = s.replace("\r\n", "\n").split("\n");

			for (String line : lines)
			{
				String[] split = line.split(",");

				for (int i = 1; i < split.length; ++i)
					if (split[i].equals(":AL"))
						try
						{
							this.aals.put(split[0].toLowerCase(), Integer.valueOf(Integer.parseInt(split[i + 1])));
						}
						catch (Throwable var13)
						{
							;
						}
					else if (split[i].equals(":CL"))
						try
						{
							this.clls.put(split[0].toLowerCase(), split[i + 1]);
							this.cldm.put(split[0].toLowerCase(), Integer.valueOf(Integer.parseInt(split[i + 2])));
						}
						catch (Throwable var12)
						{
							;
						}
			}
		}
		catch (Throwable var14)
		{
			;
		}

	}

	public String getCLF(String uuid)
	{
		return this.clls.get(uuid.toLowerCase());
	}

	public boolean hasCLS(String uuid)
	{
		return this.clls.containsKey(uuid.toLowerCase());
	}

	public boolean hasCLDM(String uuid)
	{
		return this.cldm.containsKey(uuid.toLowerCase());
	}

	public int getCLDM(String uuid)
	{
		return this.cldm.get(uuid.toLowerCase()).intValue();
	}
}
