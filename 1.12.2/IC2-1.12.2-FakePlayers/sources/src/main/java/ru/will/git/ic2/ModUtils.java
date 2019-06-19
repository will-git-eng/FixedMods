package ru.will.git.ic2;

import ru.will.git.eventhelper.nexus.ModNexus;
import ru.will.git.eventhelper.nexus.ModNexusFactory;
import ru.will.git.eventhelper.nexus.NexusUtils;

@ModNexus(name = "IC2")
public final class ModUtils
{
	public static final ModNexusFactory NEXUS_FACTORY = NexusUtils.getFactory();
}
