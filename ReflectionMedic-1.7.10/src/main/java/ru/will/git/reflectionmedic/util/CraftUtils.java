package ru.will.git.reflectionmedic.util;

import ru.will.git.reflectionmedic.reflectionmedic;

public final class CraftUtils
{
	public static Class<?> getCraftClass(String name) throws ClassNotFoundException
	{
		return Class.forName((reflectionmedic.craftPackage + '.' + name).replace("//", ".").replace('/', '.'));
	}
}
