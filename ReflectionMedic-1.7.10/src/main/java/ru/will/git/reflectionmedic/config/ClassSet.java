package ru.will.git.reflectionmedic.config;

import ru.will.git.reflectionmedic.reflectionmedic;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public final class ClassSet<T> implements Iterable<Class<? extends T>>
{
	private final Set<Class<? extends T>> classes = new HashSet<>();
	private final Class<T> baseClass;

	public ClassSet(@Nonnull Class<T> baseClass)
	{
		this.baseClass = baseClass;
		Preconditions.checkArgument(baseClass != Class.class, "baseClass must not be java.lang.Class");
	}

	public boolean isEmpty()
	{
		return this.classes.isEmpty();
	}

	public boolean contains(@Nullable T instance)
	{
		return instance != null && this.contains((Class<? extends T>) instance.getClass());
	}

	public boolean contains(@Nonnull Class<? extends T> clazz)
	{
		return this.contains(clazz, true);
	}

	public boolean contains(@Nullable T instance, boolean checkHierarchy)
	{
		return instance != null && this.contains((Class<? extends T>) instance.getClass(), checkHierarchy);
	}

	public boolean contains(@Nonnull Class<? extends T> clazz, boolean checkHierarchy)
	{
		if (this.baseClass.isAssignableFrom(clazz))
		{
			if (this.classes.contains(clazz))
				return true;

			if (checkHierarchy)
				for (Class<? extends T> aClass : this.classes)
				{
					if (aClass.isAssignableFrom(clazz))
						return true;
				}
		}

		return false;
	}

	@Override
	public Iterator<Class<? extends T>> iterator()
	{
		return this.classes.iterator();
	}

	public void clear()
	{
		this.classes.clear();
	}

	public boolean add(@Nonnull Class<? extends T> clazz)
	{
		return this.baseClass.isAssignableFrom(clazz) && this.classes.add(clazz);
	}

	public void addRaw(@Nonnull Collection<String> classNames)
	{
		for (String className : classNames)
		{
			try
			{
				Class<?> clazz = Class.forName(className);
				if (this.baseClass.isAssignableFrom(clazz))
					this.add((Class<? extends T>) clazz);
				else if (reflectionmedic.debug)
					reflectionmedic.LOGGER.warn("Class {} is not assignable from {}", className, this.baseClass.getName());
			}
			catch (ClassNotFoundException e)
			{
				if (reflectionmedic.debug)
					reflectionmedic.LOGGER.warn("Class {} not found", className);
			}
		}
	}

	public Set<String> getRaw()
	{
		return this.classes.stream().map(Class::getName).collect(Collectors.toSet());
	}
}
