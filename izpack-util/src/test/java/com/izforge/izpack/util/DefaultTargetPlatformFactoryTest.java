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
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.exception.IzPackClassNotFoundException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import java.net.URL;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link DefaultTargetPlatformFactory} class.
 *
 * @author Tim Anderson
 */
public class DefaultTargetPlatformFactoryTest
{

    /**
     * Verifies that the <tt>TargetPlatformFactory.properties</tt> file has been loaded successfully.
     */
    @Test
    public void testParser()
    {
        Platforms platforms = new Platforms();
        Platform platform = platforms.getCurrentPlatform();
        DefaultTargetPlatformFactory factory = new DefaultTargetPlatformFactory(
                NoDependencyInjectionFactory.INSTANCE, platform, platforms)
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
        assertThat(implementations).isNotNull();

        assertThat(implementations.getDefault()).isEqualTo(DefaultA.class.getName());

        assertThat(implementations.getPlatforms().size()).isEqualTo(8);

        for (Platform p : implementations.getPlatforms())
        {
            System.err.println(p + "=" + implementations.getImplementation(p));
        }
        assertThat(implementations.getImplementation(Platforms.WINDOWS)).isEqualTo(WinA.class.getName());
        assertThat(implementations.getImplementation(new Platform(Name.WINDOWS, Arch.X86))).isEqualTo(WinX86.class.getName());
        assertThat(implementations.getImplementation(new Platform(Name.WINDOWS, Arch.X64))).isEqualTo(WinX64.class.getName());
        assertThat(implementations.getImplementation(Platforms.WINDOWS_7)).isEqualTo(Win7.class.getName());
        assertThat(implementations.getImplementation(
                new Platform(Name.WINDOWS, "WINDOWS_7", OsVersionConstants.WINDOWS_7_VERSION, Arch.X64))).isEqualTo(Win7X64.class.getName());
        assertThat(implementations.getImplementation(Platforms.DEBIAN_LINUX)).isEqualTo(DebianA.class.getName());
        assertThat(implementations.getImplementation(Platforms.LINUX)).isEqualTo(LinuxA.class.getName());
        assertThat(implementations.getImplementation(Platforms.UNIX)).isEqualTo(UnixA.class.getName());
    }

    /**
     * Tests the {@link DefaultTargetPlatformFactory#create(Class, Platform)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreate() throws Exception
    {
        Platforms platforms = new Platforms();
        Platform platform = platforms.getCurrentPlatform();
        TargetPlatformFactory factory = new DefaultTargetPlatformFactory(
                NoDependencyInjectionFactory.INSTANCE, platform, platforms);

        assertThat(factory.create(A.class, Platforms.WINDOWS).getClass()).isEqualTo(WinA.class);

        // all windows versions that don't specify an architecture should use WinA
        assertThat(factory.create(A.class, Platforms.WINDOWS_2003).getClass()).isEqualTo(WinA.class);
        assertThat(factory.create(A.class, Platforms.WINDOWS_XP).getClass()).isEqualTo(WinA.class);
        assertThat(factory.create(A.class, Platforms.WINDOWS_VISTA).getClass()).isEqualTo(WinA.class);

        // check windows platforms that specify an architecture
        Platform windowsX86 = new Platform(Name.WINDOWS, Arch.X86);
        Platform windowsX64 = new Platform(Name.WINDOWS, Arch.X64);
        assertThat(factory.create(A.class, windowsX86).getClass()).isEqualTo(WinX86.class);
        assertThat(factory.create(A.class, windowsX64).getClass()).isEqualTo(WinX64.class);

        // unix implementations
        assertThat(factory.create(A.class, Platforms.UNIX).getClass()).isEqualTo(UnixA.class);
        assertThat(factory.create(A.class, Platforms.SUNOS_SPARC).getClass()).isEqualTo(UnixA.class);
        assertThat(factory.create(A.class, Platforms.SUNOS_X86).getClass()).isEqualTo(UnixA.class);
        assertThat(factory.create(A.class, Platforms.MAC_OSX).getClass()).isEqualTo(UnixA.class);

        // linux implementations
        assertThat(factory.create(A.class, Platforms.LINUX).getClass()).isEqualTo(LinuxA.class);
        assertThat(factory.create(A.class, Platforms.UBUNTU_LINUX).getClass()).isEqualTo(LinuxA.class);

        // specific linux impl
        assertThat(factory.create(A.class, Platforms.DEBIAN_LINUX).getClass()).isEqualTo(DebianA.class);

        // default impl
        assertThat(factory.create(A.class, Platforms.OS_2).getClass()).isEqualTo(DefaultA.class);

        // check implementations registered via symbolic name
        assertThat(factory.create(A.class, Platforms.WINDOWS_7).getClass()).isEqualTo(Win7.class);

        Platform win7x64 = new Platform(Platforms.WINDOWS_7, Arch.X64);
        assertThat(factory.create(A.class, win7x64).getClass()).isEqualTo(Win7X64.class);

        // no specific implementation registered for x86, so should pick up windows_7 impl
        Platform win7x32 = new Platform(Platforms.WINDOWS_7, Arch.X86);
        assertThat(factory.create(A.class, win7x32).getClass()).isEqualTo(Win7.class);
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
        public static ObjectFactory INSTANCE = new NoDependencyInjectionFactory();

        /**
         * Creates a new instance of the specified type.
         *
         * @param type       the object type
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
         * @param className  the class name
         * @param superType  the super type
         * @param parameters
         * @return a new instance
         * @throws ClassCastException           if <tt>className</tt> does not implement or extend <tt>superType</tt>
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
