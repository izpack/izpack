/*
 * IzPack - Copyright 2001-2011 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2011 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.util;

import static com.izforge.izpack.util.Platform.Arch;
import static com.izforge.izpack.util.Platform.Name;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.junit.Test;

import com.izforge.izpack.api.exception.IzPackClassNotFoundException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;

/**
 * Tests the {@link DefaultTargetPlatformFactory} class.
 *
 * @author Tim Anderson
 */
public class DefaultTargetPlatformFactoryTest
{

    /**
     * Verifies that the <tt>TargetPlatformFactory.properties</tt> file has been
     * loaded successfully.
     */
    @Test
    public void testParser()
    {
        Platforms platforms = new Platforms();
        Platform platform = platforms.getCurrentPlatform();
        DefaultTargetPlatformFactory factory = new DefaultTargetPlatformFactory(
                NoDependencyInjectionFactory.instance, platform, platforms)
        {
            @Override
            protected Parser createParser(Platforms platforms, URL url)
            {
                return new Parser(platforms, url)
                {
                    @Override
                    protected void warning(String message)
                    {
                        throw new IllegalStateException("Unexpected parser warning: " + message);
                    }
                };
            }
        };
        DefaultTargetPlatformFactory.Implementations implementations = factory.getImplementations(A.class);
        assertNotNull(implementations);

        assertEquals(DefaultA.class.getName(), implementations.getDefault());

        assertEquals(8, implementations.getPlatforms().size());

        for (Platform p : implementations.getPlatforms())
        {
            System.err.println(p + "=" + implementations.getImplementation(p));
        }
        assertEquals(WinA.class.getName(), implementations.getImplementation(Platforms.WINDOWS));
        assertEquals(WinX86.class.getName(), implementations.getImplementation(new Platform(Name.WINDOWS, Arch.X86)));
        assertEquals(WinX64.class.getName(), implementations.getImplementation(new Platform(Name.WINDOWS, Arch.X64)));
        assertEquals(Win7.class.getName(), implementations.getImplementation(Platforms.WINDOWS_7));
        assertEquals(Win7X64.class.getName(), implementations.getImplementation(
                new Platform(Name.WINDOWS, "WINDOWS_7", OsVersionConstants.WINDOWS_7_VERSION, Arch.X64)));
        assertEquals(DebianA.class.getName(), implementations.getImplementation(Platforms.DEBIAN_LINUX));
        assertEquals(LinuxA.class.getName(), implementations.getImplementation(Platforms.LINUX));
        assertEquals(UnixA.class.getName(), implementations.getImplementation(Platforms.UNIX));
    }

    /**
     * Tests the {@link DefaultTargetPlatformFactory#create(Class, Platform)}
     * method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreate() throws Exception
    {
        Platforms platforms = new Platforms();
        Platform platform = platforms.getCurrentPlatform();
        TargetPlatformFactory factory = new DefaultTargetPlatformFactory(
                NoDependencyInjectionFactory.instance, platform, platforms);

        assertEquals(WinA.class, factory.create(A.class, Platforms.WINDOWS).getClass());

        // all windows versions that don't specify an architecture should use WinA
        assertEquals(WinA.class, factory.create(A.class, Platforms.WINDOWS_2003).getClass());
        assertEquals(WinA.class, factory.create(A.class, Platforms.WINDOWS_XP).getClass());
        assertEquals(WinA.class, factory.create(A.class, Platforms.WINDOWS_VISTA).getClass());

        // check windows platforms that specify an architecture
        Platform windowsX86 = new Platform(Name.WINDOWS, Arch.X86);
        Platform windowsX64 = new Platform(Name.WINDOWS, Arch.X64);
        assertEquals(WinX86.class, factory.create(A.class, windowsX86).getClass());
        assertEquals(WinX64.class, factory.create(A.class, windowsX64).getClass());

        // unix implementations
        assertEquals(UnixA.class, factory.create(A.class, Platforms.UNIX).getClass());
        assertEquals(UnixA.class, factory.create(A.class, Platforms.SUNOS_SPARC).getClass());
        assertEquals(UnixA.class, factory.create(A.class, Platforms.SUNOS_X86).getClass());
        assertEquals(UnixA.class, factory.create(A.class, Platforms.MAC_OSX).getClass());

        // linux implementations
        assertEquals(LinuxA.class, factory.create(A.class, Platforms.LINUX).getClass());
        assertEquals(LinuxA.class, factory.create(A.class, Platforms.UBUNTU_LINUX).getClass());

        // specific linux impl
        assertEquals(DebianA.class, factory.create(A.class, Platforms.DEBIAN_LINUX).getClass());

        // default impl
        assertEquals(DefaultA.class, factory.create(A.class, Platforms.OS_2).getClass());

        // check implementations registered via symbolic name
        assertEquals(Win7.class, factory.create(A.class, Platforms.WINDOWS_7).getClass());

        Platform win7x64 = new Platform(Platforms.WINDOWS_7, Arch.X64);
        assertEquals(Win7X64.class, factory.create(A.class, win7x64).getClass());

        // no specific implementation registered for x86, so should pick up windows_7 impl
        Platform win7x32 = new Platform(Platforms.WINDOWS_7, Arch.X86);
        assertEquals(Win7.class, factory.create(A.class, win7x32).getClass());
    }

    /**
     * Test classes.
     */
    public static interface A
    {
    }

    public static class WinA implements A
    {
    }

    public static class Win7 extends WinA
    {
    }

    public static class WinX86 extends WinA
    {
    }

    public static class WinX64 extends WinA
    {
    }

    public static class Win7X64 extends WinX64
    {
    }

    public static class UnixA implements A
    {
    }

    public static class LinuxA extends UnixA
    {
    }

    public static class DebianA extends LinuxA
    {
    }

    public static class DefaultA implements A
    {
    }

    private static class NoDependencyInjectionFactory implements ObjectFactory
    {

        /**
         * The singleton instance.
         */
        public static ObjectFactory instance = new NoDependencyInjectionFactory();

        /**
         * Creates a new instance of the specified type.
         *
         * @param type the object type
         * @param parameters
         * @return a new instance
         */
        @Override
        public <T> T create(Class<T> type, Object... parameters)
        {
            try
            {
                return type.newInstance();
            }
            catch (Exception exception)
            {
                throw new IzPackException(exception);
            }
        }

        /**
         * Creates a new instance of the specified class name.
         *
         * @param className the class name
         * @param superType the super type
         * @param parameters
         * @return a new instance
         * @throws ClassCastException if <tt>className</tt> does not implement
         * or extend <tt>superType</tt>
         * @throws IzPackClassNotFoundException if the class cannot be found
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> T create(String className, Class<T> superType, Object... parameters)
        {
            Class type;
            try
            {
                type = superType.getClassLoader().loadClass(className);
                if (!superType.isAssignableFrom(type))
                {
                    throw new ClassCastException("Class '" + type.getName() + "' does not implement "
                            + superType.getName());
                }
            }
            catch (ClassNotFoundException exception)
            {
                throw new IzPackClassNotFoundException(className, exception);
            }
            return create((Class<T>) type);
        }
    }

}
