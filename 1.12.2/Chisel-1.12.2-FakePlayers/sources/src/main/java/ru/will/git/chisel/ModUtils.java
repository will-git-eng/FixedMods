package ru.will.git.chisel;

import ru.will.git.eventhelper.nexus.ModNexus;
import ru.will.git.eventhelper.nexus.ModNexusFactory;
import ru.will.git.eventhelper.nexus.NexusUtils;

@ModNexus(name = "Chisel")
public final class ModUtils
{
	public static final ModNexusFactory NEXUS_FACTORY = NexusUtils.getFactory();
}