package ru.will.git.forestry;

import ru.will.git.eventhelper.nexus.ModNexus;
import ru.will.git.eventhelper.nexus.ModNexusFactory;
import ru.will.git.eventhelper.nexus.NexusUtils;

@ModNexus(name = "Forestry")
public final class ModUtils
{
	public static final ModNexusFactory NEXUS_FACTORY = NexusUtils.getFactory();
}
