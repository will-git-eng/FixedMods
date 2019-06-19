package am2.armor.infusions;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import ru.will.git.reflectionmedic.util.EventUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import am2.api.items.armor.IArmorImbuement;
import am2.api.items.armor.ImbuementApplicationTypes;
import am2.api.items.armor.ImbuementTiers;
import am2.blocks.BlocksCommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Lightstep implements IArmorImbuement
{
	    
	private final Cache<EntityPlayer, Object> cantBreakCache;

	public Lightstep()
	{
		this.cantBreakCache = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(1, TimeUnit.SECONDS).build();
	}
	    

	@Override
	public String getID()
	{
		return "lightstep";
	}

	@Override
	public int getIconIndex()
	{
		return 30;
	}

	@Override
	public ImbuementTiers getTier()
	{
		return ImbuementTiers.FOURTH;
	}

	@Override
	public EnumSet<ImbuementApplicationTypes> getApplicationTypes()
	{
		return EnumSet.of(ImbuementApplicationTypes.ON_TICK);
	}

	@Override
	public boolean applyEffect(EntityPlayer player, World world, ItemStack stack, ImbuementApplicationTypes matchedType, Object... params)
	{
		if (world.isRemote)
			return false;
		else if (player.isSneaking())
			return false;
		else
		{
			int x = (int) Math.floor(player.posX);
			int y = (int) Math.floor(player.posY) + 1;
			int z = (int) Math.floor(player.posZ);
			int ll = world.getBlockLightValue(x, y, z);
			if (ll < 7 && world.isAirBlock(x, y, z))
			{
				    
				if (this.cantBreakCache.getIfPresent(player) != null)
					return false;

				if (EventUtils.cantBreak(player, x, y, z))
				{
					this.cantBreakCache.put(player, new Object());
					return false;
				}
				    

				world.setBlock(x, y, z, BlocksCommonProxy.blockMageTorch, 15, 2);
				return true;
			}
			else
				return false;
		}
	}

	@Override
	public int[] getValidSlots()
	{
		return new int[] { 3 };
	}

	@Override
	public boolean canApplyOnCooldown()
	{
		return true;
	}

	@Override
	public int getCooldown()
	{
		return 0;
	}

	@Override
	public int getArmorDamage()
	{
		return 1;
	}
}
