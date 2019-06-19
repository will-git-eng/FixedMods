package ru.will.git.ic2;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.user.UserStorageService;

import com.flowpowered.math.vector.Vector3i;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public final class ModUtils
{
	public static final GameProfile profile = new GameProfile(UUID.fromString("6c788982-d6ca-11e4-b9d6-1681e6b88ec1"), "[IC2]");
	private static FakePlayer player = null;

	public static final FakePlayer getModFake(World world)
	{
		if (player == null)
			player = FastUtils.getFake(world, profile);
		else
			player.worldObj = world;

		return player;
	}

	public static final boolean cantBreak(EntityPlayer player, World world, BlockPos pos)
	{
		if (player == null)
			player = getModFake(world);

    
		Object source = cause.root();
		if (source instanceof org.spongepowered.api.entity.Entity)
		{
			System.out.println("Break start");
			org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) source;
			System.out.println("Entity: " + spongeEntity.toString());
			EntityType type = spongeEntity.getType();
			System.out.println("Type: " + type);
			System.out.println("Type id: " + (type == null ? null : type.getId()));
			System.out.println("Break end");
    

		org.spongepowered.api.world.World worldSponge = (org.spongepowered.api.world.World) world;

		BlockSnapshot original = worldSponge.createSnapshot(pos.getX(), pos.getY(), pos.getZ());
		Transaction<BlockSnapshot> transaction = new Transaction<BlockSnapshot>(original, BlockSnapshot.NONE);
		transaction.setCustom(getAirSnapshot(worldSponge, pos));
		List<Transaction<BlockSnapshot>> transactions = Collections.singletonList(transaction);

		ChangeBlockEvent.Break event = SpongeEventFactory.createChangeBlockEventBreak(cause, worldSponge, transactions);
		return Sponge.getEventManager().post(event);
	}

	public static final boolean cantDamage(EntityPlayer player, Entity target)
	{
		if (target == null)
			return false;
		if (player == null)
			player = getModFake(target.worldObj);

    
		Object source = cause.root();
		if (source instanceof org.spongepowered.api.entity.Entity)
		{
			System.out.println("Damage start");
			org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) source;
			System.out.println("Entity: " + spongeEntity.toString());
			EntityType type = spongeEntity.getType();
			System.out.println("Type: " + type);
			System.out.println("Type id: " + (type == null ? null : type.getId()));
			System.out.println("Damage end");
    

		org.spongepowered.api.entity.Entity targetEntity = (org.spongepowered.api.entity.Entity) target;

		AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(cause, Collections.EMPTY_LIST, targetEntity, 0, 0);
		return Sponge.getEventManager().post(event);
	}

	public static final Cause getCause(EntityPlayer player)
	{
		if (player instanceof Player)
			return Cause.of(NamedCause.simulated((Player) player));

		Optional<ProviderRegistration<UserStorageService>> provider = Sponge.getServiceManager().getRegistration(UserStorageService.class);
		if (provider.isPresent())
		{
			UserStorageService service = provider.get().getProvider();
			Optional<User> user = service.get(player.getGameProfile().getId());
			if (user.isPresent())
				return Cause.of(NamedCause.simulated(user.get()));
		}

		return Cause.of(NamedCause.of("EntityPlayer", player));
	}

	public static final BlockState getState(BlockType type)
	{
		BlockState.Builder builder = Sponge.getRegistry().createBuilder(BlockState.Builder.class);
		builder.blockType(type);
		return builder.build();
	}

	private static final BlockSnapshot getAirSnapshot(org.spongepowered.api.world.World world, BlockPos pos)
	{
		BlockSnapshot.Builder builder = Sponge.getRegistry().createBuilder(BlockSnapshot.Builder.class);
		builder.world(world.getProperties());
		builder.blockState(getState(BlockTypes.AIR));
		builder.position(new Vector3i(pos.getX(), pos.getY(), pos.getZ()));
		return builder.build();
	}
}