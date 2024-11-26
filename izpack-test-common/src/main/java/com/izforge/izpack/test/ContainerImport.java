package com.izforge.izpack.test;

import com.izforge.izpack.api.container.Container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify used container
 *
 * @author Anthonin Bonnefoy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface ContainerImport
{
    /**
     * @return the container class
     */
    public abstract Class<? extends Container> value();
}
