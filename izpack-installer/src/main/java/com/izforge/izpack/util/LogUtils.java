/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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
package com.izforge.izpack.util;

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.core.substitutor.VariableSubstitutorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Properties;
import java.util.logging.*;

/**
 * Utility methods for logging
 */
public class LogUtils
{

    private static final String LOGGING_CONFIGURATION = "/com/izforge/izpack/installer/logging/logging.properties";

    private static final boolean OVERRIDE
            = System.getProperty("java.util.logging.config.class") == null
            && System.getProperty("java.util.logging.config.file") == null;

    public static void loadConfiguration() throws IOException
    {
        if (OVERRIDE)
        {
            loadConfiguration(LOGGING_CONFIGURATION, null, false);
        }
    }

    public static void loadConfiguration(final String resource, Variables variables, final boolean skipFallback) throws IOException
    {
        if (OVERRIDE)
        {
            InputStream is = null;
            try
            {
                InputStream resourceStream = LogUtils.class.getResourceAsStream(resource);
                if (resourceStream == null)
                {
                    if (skipFallback)
                    {
                        return;
                    }
                    resourceStream = LogUtils.class.getResourceAsStream(LOGGING_CONFIGURATION);
                }

                if (resourceStream != null)
                {
                    is = variables != null
                            ? new VariableSubstitutorInputStream(
                                    resourceStream, null,
                                    variables, SubstitutionType.TYPE_JAVA_PROPERTIES, false)
                            : resourceStream;
                    final Properties props = new Properties();
                    props.load(is);
                    loadConfiguration(props);
                }
            }
            catch (IOException e)
            {
                throw new IOException("Cannot apply log configuration from resource '" + resource + "': " + e.getMessage());
            }
            finally
            {
                IOUtils.closeQuietly(is);
            }
        }
    }

    public static void loadConfiguration(final Properties configuration) throws IOException
    {
        if (OVERRIDE)
        {
            boolean mkdirs = false;
            String pattern = null;
            final String cname = FileHandler.class.getName();
            for (String key : configuration.stringPropertyNames())
            {
                if (key.equals(cname + ".pattern"))
                {
                    // Workaround for not normalized file paths, for example ${INSTALL_PATH}/../install_log/name.log
                    // to get them working before creating ${INSTALL_PATH} in the
                    // com.izforge.izpack.installer.unpacker.UnpackerBase.preUnpack phase
                    // otherwise the FileHandler will fail when opening files already in constructor and not recover from that.
                    pattern = FilenameUtils.normalize(configuration.getProperty(key));
                    configuration.setProperty(key, pattern);
                }
                else
                {
                    // This key goes beyond the capabilities of java.util.logging.FileHandler
                    if (key.equals(cname + ".mkdirs"))
                    {
                        mkdirs = Boolean.parseBoolean(configuration.getProperty(key));
                        configuration.remove(key);
                    }
                }
            }
            if (mkdirs && pattern != null)
            {
                FileUtils.forceMkdirParent(new File(pattern));
            }

            LogManager manager = LogManager.getLogManager();
            final PipedOutputStream out = new PipedOutputStream();
            final PipedInputStream in = new PipedInputStream(out);
            try
            {
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            configuration.store(out, null);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        finally
                        {
                            IOUtils.closeQuietly(out);
                        }
                    }
                }
                ).start();

                manager.readConfiguration(in);
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }

            setStandardLevel(Logger.getLogger(""));

            // Default excludes
            for (String prefix : new String[]
            {
                "sun.awt", "java.awt", "javax.swing"
            })
            {
                setStandardLevel(Logger.getLogger(prefix), Level.INFO);
            }
        }
    }

    public static boolean isSamePreviousHandlerType(final Class handlerType)
    {
        LogManager manager = LogManager.getLogManager();

        Handler[] rootHandlers = manager.getLogger("").getHandlers();
        for (Handler prevHandler : rootHandlers)
        {
            //noinspection unchecked
            if (handlerType.isAssignableFrom(prevHandler.getClass()))
            {
                // IzPack maintains just one log file, don't override the existing handler type of it.
                // Special use case: Command line argument -logfile "wins" over the <log-file> tag
                return true;
            }
        }

        return false;
    }

    private static void setStandardLevel(Logger logger)
    {
        setStandardLevel(logger, Debug.isDebug() ? Level.FINE : Level.INFO);
    }

    private static void setStandardLevel(Logger logger, Level defaultLevel)
    {
        Level prev = logger.getLevel();
        logger.setLevel(prev != null && prev != Level.ALL && prev.intValue() < defaultLevel.intValue() ? prev : defaultLevel);
    }
}
