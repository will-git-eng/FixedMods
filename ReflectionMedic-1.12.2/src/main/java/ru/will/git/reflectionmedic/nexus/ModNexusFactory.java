package ru.will.git.reflectionmedic.nexus;

import ru.will.git.reflectionmedic.fake.FakePlayerContainerEntity;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerTileEntity;
import ru.will.git.reflectionmedic.fake.FakePlayerContainerWorld;
import ru.will.git.reflectionmedic.util.FastUtils;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;

public final class ModNexusFactory
{
	private final GameProfile modFakeProfile;

	public ModNexusFactory(@Nonnull GameProfile modFakeProfile)
	{
		Preconditions.checkArgument(modFakeProfile.isComplete(), "modFakeProfile is incomplete");
		this.modFakeProfile = modFakeProfile;
	}

	@Nonnull
	public GameProfile getProfile()
	{
		return this.modFakeProfile;
	}

	@Nonnull
	public FakePlayer getFake(@Nonnull World world)
	{
		return FastUtils.getFake(world, this.modFakeProfile);
	}

	@Nonnull
	public FakePlayerContainerEntity wrapFake(@Nonnull Entity entity)
	{
		return new FakePlayerContainerEntity(this.modFakeProfile, entity);
	}

	@Nonnull
	public FakePlayerContainerTileEntity wrapFake(@Nonnull TileEntity tile)
	{
		return new FakePlayerContainerTileEntity(this.modFakeProfile, tile);
	}

	@Nonnull
	public FakePlayerContainerWorld wrapFake(@Nonnull World world)
	{
		return new FakePlayerContainerWorld(this.modFakeProfile, world);
	}
}
