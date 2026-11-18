/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Paul Wilkinson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.ant;

import com.izforge.izpack.ant.logging.AntHandler;
import com.izforge.izpack.api.data.PackCompression;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.merge.resolve.ResolveUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.PropertySet;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A IzPack Ant task.
 *
 * @author Paul Wilkinson
 */
public class IzPackTask extends Task implements PackagerListener
{
    private static final String MESSAGES = "com/izforge/izpack/ant/langpacks/messages";

    /**
     * The embedded installation configuration
     */
    private ConfigHolder config;

    /**
     * Holds value of property input.
     */
    private String input;

    /**
     * Holds value of property basedir.
     */
    private String basedir;

    /**
     * Holds value of property output.
     */
    private String output;

    /**
     * Whether to automatically create parent directories of the output file
     */
    private boolean mkdirs;

    /**
     * Holds value of property compression.
     */
    private String compression;

    /**
     * Holds value of property compression.
     */
    private int compressionLevel;

    /**
     * Holds value of property installerType.
     */
    private InstallerType installerType;

    /**
     * Holds value of property izPackDir. This should point at the IzPack directory
     */
    private String izPackDir;

    /**
     * Holds properties used to make substitutions in the install file
     */
    private Properties properties;

    /**
     * should we inherit properties from the Ant file?
     */
    private Boolean inheritAll = false;

    /**
     * Creates new IZPackTask
     */
    public IzPackTask()
    {
        basedir = null;
        config = null;
        input = null;
        output = null;
        installerType = null;
        izPackDir = null;
        compression = PackCompression.DEFAULT.toName();
        compressionLevel = -1;
    }

    /**
     * Called by ant to create the object for the config nested element.
     *
     * @return a holder object for the config nested element.
     */
    public ConfigHolder createConfig()
    {
        config = new ConfigHolder(getProject());
        return config;
    }

    /**
     * Logs a message to the Ant log at default priority (MSG_INFO).
     *
     * @param str The message to log.
     */
    public void packagerMsg(String str)
    {
        packagerMsg(str, MSG_INFO);
    }

    /**
     * Logs a message to the Ant log at the specified priority.
     *
     * @param str      The message to log.
     * @param priority The priority of the message.
     */
    public void packagerMsg(String str, int priority)
    {
        final int antPriority;
        switch (priority)
        // No guarantee of a direct conversion. It's an enum
        {
            case MSG_DEBUG:
                antPriority = Project.MSG_DEBUG;
                break;
            case MSG_ERR:
                antPriority = Project.MSG_ERR;
                break;
            case MSG_INFO:
                antPriority = Project.MSG_INFO;
                break;
            case MSG_VERBOSE:
                antPriority = Project.MSG_VERBOSE;
                break;
            case MSG_WARN:
                antPriority = Project.MSG_WARN;
                break;
            default: // rather than die...
                antPriority = Project.MSG_INFO;
        }
        log(str, antPriority);
    }

    /**
     * Called when the packaging starts.
     */
    public void packagerStart()
    {
        log(ResourceBundle.getBundle(MESSAGES).getString("Packager_starting"), Project.MSG_DEBUG);
    }

    /**
     * Called when the packaging stops.
     */
    public void packagerStop()
    {
        log(ResourceBundle.getBundle(MESSAGES).getString("Packager_ended"), Project.MSG_DEBUG);
    }

    /**
     * Packages.
     *
     * @throws BuildException Description of the Exception
     */
    public void execute() throws org.apache.tools.ant.BuildException
    {
        checkInput();

        String kind = (installerType == null ? null : installerType.getValue());

        String configText = null;
        if (config != null)
        {
            configText = config.getText();
            input = null;
        }

        Logger rootLogger = Logger.getLogger("com.izforge.izpack");
        rootLogger.setUseParentHandlers(false);
        rootLogger.setLevel(Level.INFO);
        Handler logHandler = new AntHandler(getProject());

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader urlClassLoader = new URLClassLoader(getUrlsForClassloader(), contextClassLoader))
        {
			Class<?> runableClass = urlClassLoader.loadClass(IzpackAntRunnable.class.getName());
            Constructor<?> constructor = runableClass.getConstructor(String.class, String.class,
            		String.class, String.class, String.class, String.class, Boolean.TYPE, Integer.TYPE, Properties.class,
            		Boolean.class, Map.class, String.class, Handler.class);
            Runnable instance = (Runnable)constructor.newInstance(compression, kind, input, configText, basedir, output, mkdirs,
                    compressionLevel, properties, inheritAll, getProject().getProperties(), izPackDir, logHandler);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            instance.run();
        }
        catch (BuildException e)
        {
            throw e;
        }
        catch (RuntimeException | IOException | ReflectiveOperationException e)
        {
            throw new BuildException(e);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

    }

    private URL[] getUrlsForClassloader() throws IOException
    {
        Collection<URL> result = new HashSet<URL>();
        ClassLoader currentLoader = getClass().getClassLoader();
        Enumeration<URL> urlEnumeration = currentLoader.getResources("");
        result.addAll(Collections.list(urlEnumeration));
        urlEnumeration = currentLoader.getResources("META-INF/");
        while (urlEnumeration.hasMoreElements())
        {
            URL url = urlEnumeration.nextElement();
            result.add(ResolveUtils.processUrlToJarUrl(url));
        }
        URL[] urlArray = new URL[result.size()];
        return result.toArray(urlArray);
    }

    private void checkInput()
    {
        // Either the input attribute or config element must be specified
        if (input == null && config == null)
        {
            throw new BuildException(ResourceBundle.getBundle(MESSAGES).getString("input_must_be_specified"));
        }

        if (output == null)
        {
            throw new BuildException(ResourceBundle.getBundle(MESSAGES).getString("output_must_be_specified"));
        }

        if (basedir == null)
        {
            throw new BuildException(ResourceBundle.getBundle(MESSAGES).getString("basedir_must_be_specified"));
        }
    }

    /**
     * Setter for property input.
     *
     * @param input New value of property input.
     */
    public void setInput(String input)
    {
        this.input = input;
    }

    /**
     * Setter for property basedir.
     *
     * @param basedir New value of property basedir.
     */
    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    /**
     * Setter for property output.
     *
     * @param output New value of property output.
     */
    public void setOutput(String output)
    {
        this.output = output;
    }

    /**
     * Setter for property mkdirs.
     *
     * @param mkdirs New value of property mkdirs.
     */
    public void setMkdirs(boolean mkdirs)
    {
        this.mkdirs = mkdirs;
    }

    /**
     * Setter for property installerType.
     *
     * @param installerType New value of property installerType.
     */
    public void setInstallerType(InstallerType installerType)
    {
        this.installerType = installerType;
    }

    /**
     * Setter for property izPackDir.
     *
     * @param izPackDir New value of property izPackDir.
     */
    public void setIzPackDir(String izPackDir)
    {
        if (!(izPackDir.endsWith("/")))
        {
            izPackDir += "/";
        }
        this.izPackDir = izPackDir;
    }

    /**
     * If true, pass all Ant properties to IzPack. Defaults to false;
     *
     * @param inheritAll true if all Ant properties should be passed to IzPack.
     */
    public void setInheritAll(boolean inheritAll)
    {
        this.inheritAll = inheritAll;
    }

    /**
     * Setter for property compression.
     *
     * @param compression The type compression to set for pack compression.
     */
    public void setCompression(String compression)
    {
        this.compression = compression;
    }

    /**
     * @param compressionLevel The compressionLevel to set.
     */
    public void setCompressionLevel(int compressionLevel)
    {
        this.compressionLevel = compressionLevel;
    }


    /**
     * Ant will call this for each &lt;property&gt; tag to the IzPack task.
     */
    public void addConfiguredProperty(Property property)
    {
        if (properties == null)
        {
            properties = new Properties();
        }

        property.addProperties(properties::setProperty);
    }

    /**
     * A set of properties to pass from the build environment to the install compile
     *
     * @param ps The propertyset collection of properties
     */
    public void addConfiguredPropertyset(PropertySet ps)
    {
        if (properties == null)
        {
            properties = new Properties();
        }

        properties.putAll(ps.getProperties());
    }

    /**
     * Enumerated attribute with the values "asis", "add" and "remove".
     *
     * @author Paul Wilkinson
     */
    public static class InstallerType extends EnumeratedAttribute
    {

        public String[] getValues()
        {
            return new String[]{CompilerData.STANDARD, CompilerData.WEB};
        }
    }
}
