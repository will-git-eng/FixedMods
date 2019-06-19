package tconstruct.armor.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import tconstruct.api.IPlayerExtendedInventoryWrapper;

import java.lang.ref.WeakReference;

public class TPlayerStats implements IExtendedEntityProperties,
    
{
	public static final String PROP_NAME = "TConstruct";

	public WeakReference<EntityPlayer> player;

	public int level;
	public int bonusHealth;
	public int damage;
	public int hunger;
	public int previousDimension;

	public boolean climbWalls;
	public boolean activeGoggles = false;

	public boolean beginnerManual;
	public boolean materialManual;
	public boolean smelteryManual;
	public boolean weaponryManual;
    
	public int derpLevel;

	public ArmorExtended armor;
	public KnapsackInventory knapsack;

	public TPlayerStats()
	{
		this.armor = new ArmorExtended();
		this.knapsack = new KnapsackInventory();
	}

	public TPlayerStats(EntityPlayer entityplayer)
	{
		this.player = new WeakReference<EntityPlayer>(entityplayer);
		this.armor = new ArmorExtended();
		this.armor.init(entityplayer);

		this.knapsack = new KnapsackInventory();
		this.knapsack.init(entityplayer);

		this.derpLevel = 1;
	}

	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		NBTTagCompound tTag = new NBTTagCompound();
		this.armor.saveToNBT(tTag);
		this.knapsack.saveToNBT(tTag);
		tTag.setBoolean("beginnerManual", this.beginnerManual);
		tTag.setBoolean("materialManual", this.materialManual);
		tTag.setBoolean("smelteryManual", this.smelteryManual);
		tTag.setBoolean("weaponryManual", this.weaponryManual);
		tTag.setBoolean("battlesignBonus", this.battlesignBonus);
		tTag.setInteger("derpLevel", this.derpLevel);
		compound.setTag(PROP_NAME, tTag);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
    
		if (properties == null)
    

		this.armor.readFromNBT(properties);
		this.knapsack.readFromNBT(properties);
		this.beginnerManual = properties.getBoolean("beginnerManual");
		this.materialManual = properties.getBoolean("materialManual");
		this.smelteryManual = properties.getBoolean("smelteryManual");
		this.weaponryManual = properties.getBoolean("weaponryManual");
		this.battlesignBonus = properties.getBoolean("battlesignBonus");
		this.derpLevel = properties.getInteger("derpLevel");
	}

	@Override
	public void init(Entity entity, World world)
	{
		this.player = new WeakReference<EntityPlayer>((EntityPlayer) entity);
		this.armor.init((EntityPlayer) entity);
		this.knapsack.init((EntityPlayer) entity);
	}

	public void copyFrom(TPlayerStats stats, boolean copyCalc)
	{
		this.armor = stats.armor;
		this.knapsack = stats.knapsack;
		this.beginnerManual = stats.beginnerManual;
		this.materialManual = stats.materialManual;
		this.smelteryManual = stats.smelteryManual;
		this.weaponryManual = stats.weaponryManual;
		this.battlesignBonus = stats.battlesignBonus;

		this.derpLevel = stats.derpLevel;

		if (copyCalc)
		{
			this.bonusHealth = stats.bonusHealth;
			this.hunger = stats.hunger;
			this.level = stats.level;
		}
	}

	public static final void register(EntityPlayer player)
	{
		player.registerExtendedProperties(TPlayerStats.PROP_NAME, new TPlayerStats(player));
	}

	public static final TPlayerStats get(EntityPlayer player)
	{
		return (TPlayerStats) player.getExtendedProperties(PROP_NAME);
	}

	@Override
	public IInventory getKnapsackInventory(EntityPlayer player)
	{
		return this.knapsack;
	}

	@Override
	public IInventory getAccessoryInventory(EntityPlayer player)
	{
		return this.armor;
	}

}
