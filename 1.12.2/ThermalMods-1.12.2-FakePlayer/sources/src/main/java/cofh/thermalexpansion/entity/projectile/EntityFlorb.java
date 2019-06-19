package cofh.thermalexpansion.entity.projectile;

import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.item.ItemFlorb;
import ru.will.git.cofh.ModUtils;
import ru.will.git.eventhelper.fake.FakePlayerContainer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class EntityFlorb extends EntityThrowable
{
	private static DataParameter<String> FLUID = EntityDataManager.createKey(EntityFlorb.class, DataSerializers.STRING);
	protected static ItemStack blockCheck = new ItemStack(Blocks.STONE);
	protected float gravity = 0.03F;
	protected Fluid fluid;

	public final FakePlayerContainer fake = ModUtils.NEXUS_FACTORY.wrapFake(this);

	public static void initialize(int id)
	{
		EntityRegistry.registerModEntity(new ResourceLocation("thermalexpansion:florb"), EntityFlorb.class, "florb", id, ThermalExpansion.instance, 64, 10, true);
	}

	public EntityFlorb(World world)
	{
		super(world);
	}

	public EntityFlorb(World world, EntityLivingBase thrower, Fluid fluid)
	{
		super(world, thrower);
		this.fluid = fluid;
		this.setGravity();
		this.setManager();

		this.fake.setRealPlayer(thrower);
	}

	public EntityFlorb(World world, double x, double y, double z, Fluid fluid)
	{
		super(world, x, y, z);
		this.fluid = fluid;
		this.setGravity();
		this.setManager();
	}

	private void setGravity()
	{
		if (this.fluid.getDensity() < 0)
			this.gravity = MathHelper.minF(0.01F, 0.03F + 0.03F * (float) this.fluid.getDensity() / 1000.0F);

	}

	private void setManager()
	{
		this.dataManager.set(FLUID, this.fluid.getName());
	}

	@Override
	public void onEntityUpdate()
	{
		if (this.fluid == null && ServerHelper.isClientWorld(this.world))
			this.fluid = FluidRegistry.getFluid(this.dataManager.get(FLUID));

		super.onEntityUpdate();
	}

	@Override
	protected void onImpact(RayTraceResult traceResult)
	{
		BlockPos pos = traceResult.getBlockPos();
		if (traceResult.entityHit != null)
		{
			pos = traceResult.entityHit.getPosition().add(0, 1, 0);
			if (this.fake.cantAttack(traceResult.entityHit))
			{
				ItemFlorb.dropFlorb(this.getFluid(), this.world, pos);
				this.setDead();
				return;
			}

			traceResult.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
		}

		if (traceResult.sideHit != null && !this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos))
			pos = pos.offset(traceResult.sideHit);

		if (ServerHelper.isServerWorld(this.world))
		{
			if (traceResult.sideHit != null && this.getThrower() instanceof EntityPlayer && !((EntityPlayer) this.getThrower()).canPlayerEdit(pos, traceResult.sideHit, blockCheck))
			{
				ItemFlorb.dropFlorb(this.getFluid(), this.world, pos);
				this.setDead();
				return;
			}

			if (this.fake.cantBreak(pos))
			{
				ItemFlorb.dropFlorb(this.getFluid(), this.world, pos);
				this.setDead();
				return;
			}

			Block block = this.fluid.getBlock();
			IBlockState state = this.world.getBlockState(pos);
			if ("water".equals(this.fluid.getName()))
				block = Blocks.FLOWING_WATER;
			else if ("lava".equals(this.fluid.getName()))
				block = Blocks.FLOWING_LAVA;
			else if (block == null)
				block = Blocks.FLOWING_WATER;

			if (!this.world.isAirBlock(pos) && state.getMaterial() != Material.FIRE && !state.getBlock().isReplaceable(this.world, pos))
				ItemFlorb.dropFlorb(this.getFluid(), this.world, pos);
			else if (!this.fluid.getName().equals("water") || !BiomeDictionary.hasType(this.world.getBiome(pos), Type.NETHER))
			{
				this.world.setBlockState(pos, block.getDefaultState(), 3);
				this.world.notifyBlockUpdate(pos, state, state, 3);
			}

			this.setDead();
		}

	}

	@Override
	protected void entityInit()
	{
		this.dataManager.register(FLUID, "");
	}

	@Override
	protected float getGravityVelocity()
	{
		return this.gravity;
	}

	public Fluid getFluid()
	{
		return this.fluid;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.fluid = FluidRegistry.getFluid(nbt.getString("Fluid"));
		if (this.fluid == null || this.fluid.getBlock() == null)
			this.fluid = FluidRegistry.WATER;

		this.fake.readFromNBT(nbt);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		nbt.setString("Fluid", this.fluid.getName());
		this.fake.writeToNBT(nbt);

	}
}
