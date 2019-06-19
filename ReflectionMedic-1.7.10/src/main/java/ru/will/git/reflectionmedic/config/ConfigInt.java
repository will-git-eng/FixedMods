package ru.will.git.reflectionmedic.config;

import net.minecraftforge.common.config.Configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigInt
{
	String name() default "";

	String category() default Configuration.CATEGORY_GENERAL;

	String comment() default "";

	int min() default Integer.MIN_VALUE;

	int max() default Integer.MAX_VALUE;

	String oldName() default "";

	String oldCategory() default "";
}
