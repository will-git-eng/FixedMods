package chylex.hee.entity.technical;

import chylex.hee.world.structure.island.biome.data.AbstractBiomeInteraction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.*;

public class EntityTechnicalBiomeInteraction extends EntityTechnicalBase
{
	    
	private static final Map<Class<? extends AbstractBiomeInteraction>, List<WeakReference<EntityTechnicalBiomeInteraction>>> ENTITIES = new HashMap<Class<? extends AbstractBiomeInteraction>, List<WeakReference<EntityTechnicalBiomeInteraction>>>();

	private static void removeEntity(EntityTechnicalBiomeInteraction entity)
	{
		if (entity.addedToList && entity.interaction != null)
		{
			List<WeakReference<EntityTechnicalBiomeInteraction>> list = ENTITIES.get(entity.getInteractionType());
			if (list != null)
				for (Iterator<WeakReference<EntityTechnicalBiomeInteraction>> iterator = list.iterator(); iterator.hasNext(); )
				{
					WeakReference<EntityTechnicalBiomeInteraction> ref = iterator.next();
					EntityTechnicalBiomeInteraction ent = ref.get();
					if (ent == null)
					{
						iterator.remove();
						continue;
					}
					if (ent == entity)
					{
						iterator.remove();
						return;
					}
				}
		}
	}

	private static void addEntity(EntityTechnicalBiomeInteraction entity)
	{
		if (!entity.addedToList && entity.interaction != null)
		{
			entity.addedToList = true;
			List<WeakReference<EntityTechnicalBiomeInteraction>> list = ENTITIES.get(entity.getInteractionType());
			if (list == null)
				ENTITIES.put(entity.getInteractionType(), list = new ArrayList<WeakReference<EntityTechnicalBiomeInteraction>>());
			list.add(new WeakReference<EntityTechnicalBiomeInteraction>(entity));
		}
	}

	public static List<EntityTechnicalBiomeInteraction> getEntities(World world, AxisAlignedBB aabb, Class<? extends AbstractBiomeInteraction> clazz, int ticksExisted)
	{
		List<EntityTechnicalBiomeInteraction> resultList = new ArrayList<EntityTechnicalBiomeInteraction>();

		List<WeakReference<EntityTechnicalBiomeInteraction>> list = ENTITIES.get(clazz);
		if (list != null)
			for (Iterator<WeakReference<EntityTechnicalBiomeInteraction>> iterator = list.iterator(); iterator.hasNext(); )
			{
				WeakReference<EntityTechnicalBiomeInteraction> entityRef = iterator.next();
				EntityTechnicalBiomeInteraction entity = entityRef.get();
				if (entity == null || entity.isDead)
					iterator.remove();
				else if (entity.ticksExisted >= ticksExisted && entity.boundingBox.intersectsWith(aabb))
					resultList.add(entity);
			}

		return resultList;
	}

	public static EntityTechnicalBiomeInteraction getEntity(World world, AxisAlignedBB aabb, Class<? extends AbstractBiomeInteraction> clazz, int ticksExisted)
	{
		List<WeakReference<EntityTechnicalBiomeInteraction>> list = ENTITIES.get(clazz);
		if (list != null)
			for (Iterator<WeakReference<EntityTechnicalBiomeInteraction>> iterator = list.iterator(); iterator.hasNext(); )
			{
				WeakReference<EntityTechnicalBiomeInteraction> entityRef = iterator.next();
				EntityTechnicalBiomeInteraction entity = entityRef.get();
				if (entity == null || entity.isDead)
					iterator.remove();
				else if (entity.ticksExisted >= ticksExisted && entity.boundingBox.intersectsWith(aabb))
					return entity;
			}

		return null;
	}

	private boolean addedToList;

	@Override
	public void setDead()
	{
		removeEntity(this);
		super.setDead();
	}
	    

	private AbstractBiomeInteraction interaction;

	public EntityTechnicalBiomeInteraction(World world)
	{
		super(world);
	}

	public EntityTechnicalBiomeInteraction(World world, double x, double y, double z, AbstractBiomeInteraction interaction)
	{
		super(world);
		this.setPosition(x, y, z);
		this.interaction = interaction;
		this.interaction.init(this);
		this.interaction.init();
	}

	@Override
	protected void entityInit()
	{
	}

	@Override
	public void onUpdate()
	{
		if (!this.worldObj.isRemote)
		{
			    
			addEntity(this);
			    

			this.interaction.update();
		}
	}

	public AbstractBiomeInteraction getInteraction()
	{
		return this.interaction;
	}

	public Class<? extends AbstractBiomeInteraction> getInteractionType()
	{
		return this.interaction != null ? this.interaction.getClass() : null;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt)
	{
		if (!this.worldObj.isRemote)
		{
			NBTTagCompound tag = new NBTTagCompound();
			this.interaction.saveToNBT(tag);
			nbt.setTag("interactionData", tag);
			nbt.setString("interactionId", this.interaction.getIdentifier());
		}

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt)
	{
		if (!this.worldObj.isRemote)
		{
			this.interaction = AbstractBiomeInteraction.BiomeInteraction.createByIdentifier(nbt.getString("interactionId"));
			if (this.interaction != null)
			{
				this.interaction.init(this);
				this.interaction.loadFromNBT(nbt.getCompoundTag("interactionData"));
			}
			else
			{
				this.setDead();
			}
		}
	}
}
