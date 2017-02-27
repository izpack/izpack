package com.izforge.izpack.test;

import java.lang.annotation.*;

/**
 * Annotation to specify the base dir
 *
 * @author Anthonin Bonnefoy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(
        {
            ElementType.TYPE
        })
@Inherited
public @interface BaseDir
{

    /**
     * @return the base dir
     */
    public abstract String value();
}
