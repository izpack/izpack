/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * This file has been adapted from:
 * Copyright 2006-2012 The Scriptella Project Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.ant.logging;

import org.apache.tools.ant.Project;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * java.util.logging Handler adapter for Ant.
 *
 * @author Fyodor Kupolov
 * @version 1.0
 */
public class AntHandler extends Handler
{

    private StringBuilder sb = new StringBuilder();
    private Project project;

    public AntHandler(Project project)
    {
        this.project = project;
    }

    @Override
    public synchronized void publish(LogRecord record)
    {
        String msg = record.getMessage();
        if (msg != null)
        {
            sb.append(msg);
        }
        Throwable thrown = record.getThrown();
        if (thrown != null)
        {
            sb.append("\n").append(thrown.toString()).append('\n');
        }
        final int level = convert(record.getLevel());
        if (level >= 0)
        {
            project.log(sb.toString(), level);
        }
        sb.setLength(0);
    }

    /**
     * Converts JUL level to appropriate Ant message priority
     *
     * @param level JUL level
     * @return Ant message priority
     */
    private int convert(Level level)
    {
        final int lev = level.intValue();
        if (lev >= Level.SEVERE.intValue())
        {
            return Project.MSG_ERR;
        }
        if (lev >= Level.WARNING.intValue())
        {
            return Project.MSG_WARN;
        }
        if (lev >= Level.INFO.intValue())
        {
            return Project.MSG_INFO;
        }
        if (lev >= Level.FINE.intValue())
        {
            return Project.MSG_VERBOSE;
        }
        if (lev > Level.OFF.intValue())
        {
            return Project.MSG_DEBUG;
        }
        return -1;
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close()
    {
    }
}
