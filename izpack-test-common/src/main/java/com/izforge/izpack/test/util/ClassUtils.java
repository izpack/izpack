package com.izforge.izpack.test.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * jar Classloading manipulation class
 *
 * @author Anthonin Bonnefoy
 */
public class ClassUtils
{
    @SuppressWarnings("unchecked")
    public static void unloadLastJar()
    {
        try
        {
            URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
            ucpField.setAccessible(true);
            Object ucp = ucpField.get(systemClassLoader);
            Class<? extends Object> ucpClass = ucp.getClass();
            Field pathField = ucpClass.getDeclaredField("path");
            pathField.setAccessible(true);
            ArrayList<URL> path = (ArrayList<URL>) pathField.get(ucp);
            path.remove(path.size() - 1);
            Field loaderField = ucpClass.getDeclaredField("loaders");
            loaderField.setAccessible(true);
            ArrayList<?> loaders = (ArrayList<?>) loaderField.get(ucp);
            loaders.remove(loaders.size() - 1);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static void loadJarInSystemClassLoader(File out)
    {
        try
        {
            URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method declaredMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(systemClassLoader, out.toURI().toURL());
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }
}
