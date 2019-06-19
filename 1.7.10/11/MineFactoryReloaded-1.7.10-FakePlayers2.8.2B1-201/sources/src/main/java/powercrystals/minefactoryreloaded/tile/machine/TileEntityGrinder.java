package powercrystals.minefactoryreloaded.tile.machine;

import cofh.core.util.fluid.FluidTankAdv;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.IFactoryGrindable;
import powercrystals.minefactoryreloaded.api.MobDrop;
import powercrystals.minefactoryreloaded.core.GrindingDamage;
import powercrystals.minefactoryreloaded.core.ITankContainerBucketable;
import powercrystals.minefactoryreloaded.core.MFRLiquidMover;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryPowered;
import powercrystals.minefactoryreloaded.gui.container.ContainerFactoryPowered;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;
import powercrystals.minefactoryreloaded.world.GrindingWorldServer;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TileEntityGrinder extends TileEntityFactoryPowered implements ITankContainerBucketable
{
	public static final float DAMAGE = 2.6584558E36F;
	protected Random _rand;
	protected GrindingWorldServer _grindingWorld;
	protected GrindingDamage _damageSource;

	protected TileEntityGrinder(Machine var1)
	{
		super(var1);
		createEntityHAM(this);
		this._rand = new Random();
		this.setManageSolids(true);
		this.setCanRotate(true);
		this._tanks[0].setLock(FluidRegistry.getFluid("mobessence"));
	}

	public TileEntityGrinder()
	{
		this(Machine.Grinder);
		this._damageSource = new GrindingDamage();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer var1)
	{
		return new GuiFactoryPowered(this.getContainer(var1), this);
	}

	@Override
	public ContainerFactoryPowered getContainer(InventoryPlayer var1)
	{
		return new ContainerFactoryPowered(this, var1);
	}

	@Override
	public void setWorldObj(World var1)
	{
		super.setWorldObj(var1);
		if (this._grindingWorld != null)
		{
			this._grindingWorld.clearReferences();
			this._grindingWorld.setMachine(null);
		}

		if (this.worldObj instanceof WorldServer)
			this._grindingWorld = new GrindingWorldServer((WorldServer) this.worldObj, this);

	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		if (this._grindingWorld != null)
		{
			this._grindingWorld.clearReferences();
			this._grindingWorld.setMachine(null);
		}

		this._grindingWorld = null;
	}

	public Random getRandom()
	{
		return this._rand;
	}

	@Override
	protected boolean shouldPumpLiquid()
	{
		return true;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}

	@Override
	public int getIdleTicksMax()
	{
		return 200;
	}

	@Override
	public boolean activateMachine()
	{
		this._grindingWorld.cleanReferences();
		List<EntityLivingBase> entities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this._areaManager.getHarvestArea().toAxisAlignedBB());
		Iterator<EntityLivingBase> iterator = entities.iterator();

		EntityLivingBase entity;
		while (true)
		{
			label34:
			while (true)
			{
				if (!iterator.hasNext())
				{
					this.setIdleTicks(this.getIdleTicksMax());
					return false;
				}

				entity = iterator.next();
				if ((!(entity instanceof EntityAgeable) || ((EntityAgeable) entity).getGrowingAge() >= 0) && !entity.isEntityInvulnerable() && entity.getHealth() > 0.0F)
				{
					if (MFRRegistry.getGrindables().containsKey(entity.getClass()))
    
						if (this.fake.cantDamage(entity))
    

						IFactoryGrindable grindable = MFRRegistry.getGrindables().get(entity.getClass());
						List<MobDrop> drop = grindable.grind(entity.worldObj, entity, this.getRandom());
						if (drop != null && drop.size() > 0 && WeightedRandom.getTotalWeight(drop) > 0)
						{
							ItemStack stack = ((MobDrop) WeightedRandom.getRandomItem(this._rand, drop)).getStack();
							this.doDrop(stack);
						}

						if (grindable.processEntity(entity))
						{
							if (entity.getHealth() <= 0.0F)
								continue;
							break;
						}
					}

					for (Class clazz : MFRRegistry.getGrinderBlacklist())
					{
						if (clazz.isInstance(entity))
							continue label34;
					}
					break;
				}
			}

			if (this._grindingWorld.addEntityForGrinding(entity))
				break;
    
		if (this.fake.cantDamage(entity))
		{
			this.setIdleTicks(this.getIdleTicksMax());
			return false;
    

		this.damageEntity(entity);
		if (entity.getHealth() <= 0.0F)
			this.setIdleTicks(20);
		else
			this.setIdleTicks(10);

		return true;
	}

	protected void setRecentlyHit(EntityLivingBase var1, int var2)
	{
		var1.recentlyHit = var2;
	}

	protected void damageEntity(EntityLivingBase var1)
	{
		this.setRecentlyHit(var1, 100);
		var1.attackEntityFrom(this._damageSource, 2.6584558E36F);
	}

	public void acceptXPOrb(EntityXPOrb var1)
	{
		MFRLiquidMover.fillTankWithXP(this._tanks[0], var1);
	}

	@Override
	public int getSizeInventory()
	{
		return 0;
	}

	protected void fillTank(FluidTankAdv var1, String var2, float var3)
	{
		var1.fill(FluidRegistry.getFluidStack(var2, (int) (100.0F * var3)), true);
		this.markDirty();
	}

	@Override
	protected FluidTankAdv[] createTanks()
	{
		return new FluidTankAdv[] { new FluidTankAdv(4000) };
	}

	@Override
	public int fill(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection var1, int var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	public FluidStack drain(ForgeDirection var1, FluidStack var2, boolean var3)
	{
		return this.drain(var2, var3);
	}

	@Override
	public boolean allowBucketDrain(ItemStack var1)
	{
		return true;
	}

	@Override
	public boolean canFill(ForgeDirection var1, Fluid var2)
	{
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection var1, Fluid var2)
	{
		return true;
	}
}
