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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.factory.ObjectFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Factory for constructing platform specific implementation implementations of interfaces or classes.
 * <p/>
 * It is configured using one or more <em>TargetPlatformFactory.properties</em> files, located in the class path
 * under <em>com/izforge/izpack/util/</em>. A template is shown below.
 * <pre>
 * #
 * # TargetPlatformFactory template.
 * #
 * # The format of each entry is as follows:
 * #
 * # &lt;interface&gt;[,name[,arch]] = &lt;implementation&gt;
 * #
 * # Where:
 * # . interface      - is the fully qualified interface or class name
 * # . name           - is the platform name corresponding to those defined in com.izforge.izpack.util.Platforms, as
 * #                    lowercase
 * # . arch           - is the platform architecture corresponding to com.izforge.izpack.util.Platform.Arch, as
 * #                    lowercase
 * # . implementation - is the implementation of the interface for the platform
 * #
 * # E.g.:
 * # com.izforge.izpack.util.os.NativeWrapper,windows = com.izforge.izpack.util.os.WinWrapper
 * # com.izforge.izpack.util.os.NativeWrapper,windows,x64 = com.izforge.izpack.util.os.Win64Wrapper
 * # com.izforge.izpack.util.os.NativeWrapper,windows_xp = com.izforge.izpack.util.os.Windows7Wrapper
 * # com.izforge.izpack.util.os.NativeWrapper,windows_7,x86 = com.izforge.izpack.util.os.Windows7x86Wrapper
 * # com.izforge.izpack.util.os.NativeWrapper,windows_7,x64 = com.izforge.izpack.util.os.Windows7x64Wrapper
 * # com.izforge.izpack.util.os.NativeWrapper,unix = com.izforge.izpack.util.os.GenericUnixWrapper
 * # com.izforge.izpack.util.os.NativeWrapper,debian_linux = com.izforge.izpack.util.os.DebianLinuxWrapper
 * # com.izforge.izpack.util.os.NativeWrapper,mac_osx = com.izforge.izpack.util.os.MacOSXWrapper
 * # com.izforge.izpack.util.os.NativeWrapper = com.izforge.izpack.util.os.DefaultWrapper
 * </pre>
 *
 * @author Tim Anderson
 * @see Platforms
 * @see Platform
 */
@ApplicationScoped
public class DefaultTargetPlatformFactory implements TargetPlatformFactory
{

    /**
     * The factory to delegate to.
     */
    private final ObjectFactory factory;

    /**
     * The platform factory.
     */
    private final Platforms platforms;

    /**
     * The current platform.
     */
    private final Platform platform;

    /**
     * Map of interfaces to their corresponding platform implementations.
     */
    private Map<String, Implementations> implementations = new HashMap<>();

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(DefaultTargetPlatformFactory.class.getName());

    /**
     * The TargetPlatformFactory properties.
     */
    private static final String RESOURCE_PATH = "com/izforge/izpack/util/TargetPlatformFactory.properties";


    /**
     * Constructs a <tt>DefaultTargetPlatformFactory</tt>, configured from <em>TargetPlatformFactory.properties</em>
     * resources.
     *
     * @param factory   the factory to delegate to
     * @param platform  the current platform
     * @param platforms the platform factory
     */
    @Inject
    public DefaultTargetPlatformFactory(ObjectFactory factory, Platform platform, Platforms platforms)
    {
        this.factory = factory;
        this.platform = platform;
        this.platforms = platforms;
        try
        {
            Enumeration<URL> urls = getClass().getClassLoader().getResources(RESOURCE_PATH);
            while (urls.hasMoreElements())
            {
                URL url = urls.nextElement();
                try
                {
                    add(url);
                }
                catch (IOException exception)
                {
                    logger.log(Level.WARNING, exception.getMessage(), exception);
                }
            }
        }
        catch (IOException exception)
        {
            logger.log(Level.WARNING, exception.getMessage(), exception);
        }
    }

    /**
     * Creates a platform specific implementation of a class, for the current platform.
     *
     * @param clazz the class to create a platform specific instance of
     * @return the instance for the specified platform
     * @throws Exception for any error
     */
    @Override
    public <T> T create(Class<T> clazz) throws Exception
    {
        return create(clazz, platform);
    }

    /**
     * Creates a platform specific instance of a class, for the specified platform.
     *
     * @param type     the type to create a platform specific instance of
     * @param platform the platform
     * @return the instance for the specified platform
     * @throws Exception for any error
     */
    @Override
    public <T> T create(Class<T> type, Platform platform) throws Exception
    {
        Class<T> impl = getImplementation(type, platform);
        return factory.create(impl);
    }

    /**
     * Returns the implementation of a type for the specified platform.
     *
     * @param type     the type
     * @param platform the platform
     * @return the implementation class of <tt>type</tt> for <tt>platform</tt>
     * @throws ClassNotFoundException if a class is registered but cannot be found
     * @throws IllegalStateException  if no class is registered, or the registered class does not implement nor
     *                                extend <tt>type</tt>
     */
    @SuppressWarnings("unchecked")
    protected <T> Class<T> getImplementation(Class<T> type, Platform platform) throws ClassNotFoundException
    {
        Implementations impls = getImplementations(type);
        if (impls == null)
        {
            throw new IllegalArgumentException("No implementations registered for class=" + type.getName());
        }
        Platform match = null;
        Platform fallback = null;
        for (Platform p : impls.getPlatforms())
        {
            if (p.equals(platform))
            {
                match = p;
                break;
            }
            else if (platform.isA(p))
            {
                if (fallback == null || moreSpecific(platform, fallback, p))
                {
                    fallback = p;
                }
            }
        }
        if (match == null)
        {
            if (fallback != null)
            {
                match = fallback;
            }
        }
        String implName = (match != null) ? impls.getImplementation(match) : impls.getDefault();
        if (implName == null)
        {
            throw new IllegalStateException("No implementation registered for class=" + type.getName()
                                                    + " and platform=" + platform);
        }
        Class<?> impl = Class.forName(implName);
        if (!type.isAssignableFrom(impl))
        {
            throw new IllegalStateException(impl.getName() + " does not extend " + type.getName());
        }
        return (Class<T>)impl;
    }

    /**
     * Adds the configuration from the specified URL.
     *
     * @param properties the configuration
     * @param url        the URL, for error reporting. May be <tt>null</tt>
     */
    protected void add(Properties properties, URL url)
    {
        Parser parser = createParser(platforms, url);
        parser.parse(properties, implementations);
    }

    /**
     * Creates a new parser.
     *
     * @param platforms the platforms
     * @param url       the source URL
     * @return a new parser
     */
    protected Parser createParser(Platforms platforms, URL url)
    {
        return new Parser(platforms, url);
    }

    /**
     * Returns the implementations registered for the specified class.
     *
     * @param clazz the class
     * @return the corresponding implementations, or <tt>null</tt> if none are found
     */
    protected Implementations getImplementations(Class<?> clazz)
    {
        return implementations.get(clazz.getName());
    }

    /**
     * Configures the factory from the specified URL.
     *
     * @param url the URL to load properties from
     * @throws IOException if the properties cannot be read
     */
    private void add(URL url) throws IOException
    {
        Properties properties = new Properties();
        InputStream stream = url.openStream();
        try
        {
            properties.load(stream);
        }
        finally
        {
            stream.close();
        }
        add(properties, url);
    }

    /**
     * Determines if a platform is more specific than the current fallback platform.
     *
     * @param requested the requested platform
     * @param fallback  the current fallback platform
     * @param platform  the platform to check.
     * @return <tt>true</tt> if <tt>platform</tt> is a <tt>fallback</tt> or has the same version as that requested
     *         and the fallback doesn't specify a version
     */
    private boolean moreSpecific(Platform requested, Platform fallback, Platform platform)
    {
        boolean result = platform.isA(fallback);
        if (!result)
        {
            if (requested.getVersion() != null && requested.getVersion().equals(platform.getVersion())
                    && fallback.getVersion() == null)
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Properties parser.
     */
    protected static class Parser
    {

        /**
         * The platforms.
         */
        private final Platforms platforms;

        /**
         * The source URL.
         */
        private final URL url;

        /**
         * Constructs a <tt>Parser</tt>.
         *
         * @param platforms the platforms
         * @param url       the source URL
         */
        public Parser(Platforms platforms, URL url)
        {
            this.platforms = platforms;
            this.url = url;
        }

        /**
         * Parse properties, adding them to the specified implementations.
         *
         * @param properties      the properties to parse
         * @param implementations the implementations to populate
         */
        public void parse(Properties properties, Map<String, Implementations> implementations)
        {
            for (String key : properties.stringPropertyNames())
            {
                String[] interfacePlatform = key.split(",");
                if (interfacePlatform.length >= 1 && interfacePlatform.length <= 3)
                {
                    String iface = trimToNull(interfacePlatform[0]);
                    String name = (interfacePlatform.length >= 2) ? trimToNull(interfacePlatform[1]) : null;
                    String arch = (interfacePlatform.length == 3) ? trimToNull(interfacePlatform[2]) : null;
                    String impl = trimToNull(properties.getProperty(key));
                    if (iface == null)
                    {
                        warning("Ignoring null interface=" + key + " from " + url);
                    }
                    else if (impl == null)
                    {
                        warning("Ignoring null implementation for key=" + key + " from " + url);
                    }
                    else
                    {
                        Implementations impls = implementations.get(iface);
                        if (impls == null)
                        {
                            impls = new Implementations();
                            implementations.put(iface, impls);
                        }
                        if (name == null)
                        {
                            if (impls.getDefault() == null)
                            {
                                impls.setDefault(impl);
                            }
                            else
                            {
                                warning("Ignoring duplicate default implementation=" + impl + " from " + url);
                            }
                        }
                        else
                        {
                            Platform platform = platforms.getPlatform(name, arch);
                            if (platform.getName() == Platform.Name.UNKNOWN)
                            {
                                warning("Ignoring unsupported platform=" + platform + " for key=" + key + " from "
                                                + url);
                            }
                            else if (impls.getImplementation(platform) == null)
                            {
                                impls.addImplementation(platform, impl);
                            }
                            else
                            {
                                warning("Ignoring duplicate implementation=" + impl + " for platform=" + platform
                                                + " from " + url);
                            }
                        }
                    }
                }
                else
                {
                    warning("Ignoring invalid entry=" + key + ", length=" + interfacePlatform.length + " from " + url);
                }
            }
        }

        /**
         * Handles parser warnings.
         * <p/>
         * This implementation logs the message
         *
         * @param message the error message
         */
        protected void warning(String message)
        {
            logger.warning(message);
        }

        /**
         * Trims a string to null if empty.
         *
         * @param str the string to trim. May be <tt>null</tt>
         * @return the trimmed string. May be <tt>null</tt>
         */
        private String trimToNull(String str)
        {
            if (str != null)
            {
                str = str.trim();
            }
            return (str == null || str.length() == 0) ? null : str;
        }

    }

    /**
     * Manages implementations of a class/interface, for multiple platforms.
     */
    protected static class Implementations
    {

        /**
         * Default implementation class name. May be <tt>null</tt>
         */
        private String defaultImplementation;

        /**
         * The implementation class names, keyed on platform.
         */
        private Map<Platform, String> implementations = new HashMap<>();

        /**
         * Sets the default implementation class.
         *
         * @param defaultImplementation the default implementation class name. May be <tt>null</tt>
         */
        public void setDefault(String defaultImplementation)
        {
            this.defaultImplementation = defaultImplementation;
        }

        /**
         * Returns the default implementation class name.
         *
         * @return the default implementation class name. May be <tt>null</tt>
         */
        public String getDefault()
        {
            return defaultImplementation;
        }

        /**
         * Returns the platforms that have an implementation.
         *
         * @return the platforms that have an implementation
         */
        public Set<Platform> getPlatforms()
        {
            return implementations.keySet();
        }

        /**
         * Returns the implementation for the specified platform.
         *
         * @param platform the platform
         * @return the implementation, or <tt>null</tt> if none exists
         */
        public String getImplementation(Platform platform)
        {
            return implementations.get(platform);
        }

        /**
         * Adds an implementation for the specified platform.
         *
         * @param platform       the platform
         * @param implementation the implementation class name
         */
        public void addImplementation(Platform platform, String implementation)
        {
            implementations.put(platform, implementation);
        }

    }

}
