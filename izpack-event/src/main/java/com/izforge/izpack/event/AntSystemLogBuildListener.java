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
package com.izforge.izpack.event;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.DateUtils;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

class AntSystemLogBuildListener implements BuildListener
{

    private static final Logger LOGGER = Logger.getLogger(AntSystemLogBuildListener.class.getName());

    public AntSystemLogBuildListener(int level)
    {
        this.level = level;
    }

    /**
     * Lowest level of message to write out
     */
    private final int level;

    /**
     * Time of the start of the build
     */
    private long startTime;

    @Override
    public void buildStarted(BuildEvent buildEvent)
    {
        startTime = System.currentTimeMillis();
    }

    private static String throwableMessage(Throwable error, boolean verbose)
    {
        StringBuilder m = new StringBuilder();
        while (error instanceof BuildException)
        {
            Throwable cause = error.getCause();
            if (cause == null)
            {
                break;
            }
            String msg1 = error.toString();
            String msg2 = cause.toString();
            if (msg1.endsWith(msg2))
            {
                m.append(msg1.substring(0, msg1.length() - msg2.length()));
                error = cause;
            }
            else
            {
                break;
            }
        }
        if (verbose || !(error instanceof BuildException))
        {
            m.append(StringUtils.getStackTrace(error));
        }
        else
        {
            m.append(error);
        }
        return m.toString();
    }

    @Override
    public void buildFinished(BuildEvent event)
    {
        Throwable error = event.getException();
        if (error == null)
        {
            LOGGER.info("BUILD SUCCESSFUL");
        }
        else
        {
            LOGGER.severe("BUILD FAILED");
            LOGGER.severe(throwableMessage(error, Project.MSG_VERBOSE <= level));
        }
        LOGGER.info("Total time: " + formatTime(System.currentTimeMillis() - startTime));
    }

    @Override
    public void targetStarted(BuildEvent event)
    {
        String targetName = event.getTarget().getName().trim();
        if (Project.MSG_INFO <= level && !targetName.equals(""))
        {
            LOGGER.info(targetName + ":");
        }
    }

    @Override
    public void targetFinished(BuildEvent event)
    {

    }

    @Override
    public void taskStarted(BuildEvent event)
    {

    }

    @Override
    public void taskFinished(BuildEvent event)
    {

    }

    @Override
    public void messageLogged(BuildEvent event)
    {
        int priority = event.getPriority();
        // Filter out messages based on priority
        if (priority <= level)
        {
            if (event.getTask() != null)
            {
                // Print out the name of the task if we're in one
                String label = "  [" + event.getTask().getTaskName() + "] ";
                BufferedReader r = null;
                try
                {
                    r = new BufferedReader(new StringReader(event.getMessage()));
                    String line;
                    while ((line = r.readLine()) != null)
                    {
                        if (priority != Project.MSG_ERR)
                        {
                            LOGGER.info(label + line);
                        }
                        else
                        {
                            LOGGER.severe(label + line);
                        }
                    }
                }
                catch (IOException e)
                {
                    // shouldn't be possible
                    LOGGER.severe(event.getMessage());
                }
                finally
                {
                    if (r != null)
                    {
                        FileUtils.close(r);
                    }
                }
            }

            Throwable ex = event.getException();
            if (Project.MSG_DEBUG <= level && ex != null)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Convenience method to format a specified length of time.
     *
     * @param millis Length of time to format, in milliseconds.
     * @return the time as a formatted string.
     * @see DateUtils#formatElapsedTime(long)
     */
    private static String formatTime(final long millis)
    {
        return DateUtils.formatElapsedTime(millis);
    }
}
