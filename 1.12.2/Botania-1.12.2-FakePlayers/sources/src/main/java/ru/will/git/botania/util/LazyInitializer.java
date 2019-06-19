package ru.will.git.botania.util;

import java.util.Objects;
import java.util.function.Supplier;

public final class LazyInitializer<T>
{
	private final Supplier<T> initializer;
	private T value;

	public LazyInitializer(Supplier<T> initializer)
	{
		this.initializer = initializer;
	}

	public T get()
	{
		if (this.value == null)
			this.value = Objects.requireNonNull(this.initializer.get());
		return this.value;
	}
}
