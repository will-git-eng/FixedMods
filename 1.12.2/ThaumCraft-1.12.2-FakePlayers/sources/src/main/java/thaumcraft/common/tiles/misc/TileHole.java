package thaumcraft.common.tiles.misc;

import ru.will.git.eventhelper.fake.FakePlayerContainer;
import ru.will.git.thaumcraft.ModUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.items.casters.foci.FocusEffectRift;

public class TileHole extends TileMemory implements ITickable
{
	public short countdown = 0;
	public short countdownmax = 120;
	public byte count = 0;
	public EnumFacing direction = null;

	
	public final FakePlayerContainer fake = ModUtils.NEXUS_FACTORY.wrapFake(this);
	

	public TileHole()
	{
	}

	public TileHole(IBlockState bi, short max, byte count, EnumFacing direction)
	{
		super(bi);
		this.count = count;
		this.countdownmax = max;
		this.direction = direction;
	}

	public TileHole(byte count)
	{
		this.count = count;
	}

	@Override
	public void update()
	{
		if (this.world.isRemote)
			for (int a = 0; a < 2; ++a)
			{
				this.surroundwithsparkles();
			}
		else
		{
			if (this.countdown == 0 && this.count > 1 && this.direction != null)
			{
				label0:
				switch (this.direction.getAxis())
				{
					case Y:
						int a = 0;

						while (true)
						{
							if (a >= 9)
								break label0;

							if (a / 3 != 1 || a % 3 != 1)
								// TODO gamerforEA add FakePlayerContainer parameter
								FocusEffectRift.createHole(this.fake, this.world, this.getPos().add(-1 + a / 3, 0, -1 + a % 3), null, (byte) 1, this.countdownmax);

							++a;
						}
					case Z:
						a = 0;

						while (true)
						{
							if (a >= 9)
								break label0;

							if (a / 3 != 1 || a % 3 != 1)
								// TODO gamerforEA add FakePlayerContainer parameter
								FocusEffectRift.createHole(this.fake, this.world, this.getPos().add(-1 + a / 3, -1 + a % 3, 0), null, (byte) 1, this.countdownmax);

							++a;
						}
					case X:
						for (a = 0; a < 9; ++a)
						{
							if (a / 3 != 1 || a % 3 != 1)
								// TODO gamerforEA add FakePlayerContainer parameter
								FocusEffectRift.createHole(this.fake, this.world, this.getPos().add(0, -1 + a / 3, -1 + a % 3), null, (byte) 1, this.countdownmax);
						}
				}

				// TODO gamerforEA add FakePlayerContainer parameter
				if (!FocusEffectRift.createHole(this.fake, this.world, this.getPos().offset(this.direction.getOpposite()), this.direction, (byte) (this.count - 1), this.countdownmax))
					this.count = 0;
			}

			++this.countdown;
			if (this.countdown % 20 == 0)
				this.markDirty();

			if (this.countdown >= this.countdownmax)
				this.world.setBlockState(this.getPos(), this.oldblock, 3);
		}

	}

	private void surroundwithsparkles()
	{
		for (EnumFacing d1 : EnumFacing.values())
		{
			IBlockState b1 = this.world.getBlockState(this.getPos().offset(d1));
			if (b1.getBlock() != BlocksTC.hole && !b1.isOpaqueCube())
				for (EnumFacing d2 : EnumFacing.values())
				{
					if (d1.getAxis() != d2.getAxis() && (this.world.getBlockState(this.getPos().offset(d2)).isOpaqueCube() || this.world.getBlockState(this.getPos().offset(d1).offset(d2)).isOpaqueCube()))
					{
						float sx = 0.5F * (float) d1.getFrontOffsetX();
						float sy = 0.5F * (float) d1.getFrontOffsetY();
						float sz = 0.5F * (float) d1.getFrontOffsetZ();
						if (sx == 0.0F)
							sx = 0.5F * (float) d2.getFrontOffsetX();

						if (sy == 0.0F)
							sy = 0.5F * (float) d2.getFrontOffsetY();

						if (sz == 0.0F)
							sz = 0.5F * (float) d2.getFrontOffsetZ();

						if (sx == 0.0F)
							sx = this.world.rand.nextFloat();
						else
							sx = sx + 0.5F;

						if (sy == 0.0F)
							sy = this.world.rand.nextFloat();
						else
							sy = sy + 0.5F;

						if (sz == 0.0F)
							sz = this.world.rand.nextFloat();
						else
							sz = sz + 0.5F;

						FXDispatcher.INSTANCE.sparkle((float) this.getPos().getX() + sx, (float) this.getPos().getY() + sy, (float) this.getPos().getZ() + sz, 0.25F, 0.25F, 1.0F);
					}
				}
		}

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.countdown = nbt.getShort("countdown");
		this.countdownmax = nbt.getShort("countdownmax");
		this.count = nbt.getByte("count");
		byte db = nbt.getByte("direction");
		this.direction = db >= 0 ? EnumFacing.values()[db] : null;

		
		this.fake.readFromNBT(nbt);
		
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setShort("countdown", this.countdown);
		nbt.setShort("countdownmax", this.countdownmax);
		nbt.setByte("count", this.count);
		nbt.setByte("direction", this.direction == null ? -1 : (byte) this.direction.ordinal());

		
		this.fake.writeToNBT(nbt);
		

		return nbt;
	}
}
