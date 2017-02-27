package com.izforge.izpack.test;

import java.lang.annotation.*;

/**
 * Annotation to specify install file
 *
 * @author Anthonin Bonnefoy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(
        {
            ElementType.METHOD, ElementType.TYPE
        })
@Inherited
public @interface InstallFile
{

    /**
     * @return the install file
     */
    public abstract String value();
}
