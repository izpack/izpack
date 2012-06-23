/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.compiler.listener;

/**
 * Used to handle the packager messages in the command-line mode.
 *
 * @author julien created October 26, 2002
 */
public class CmdlinePackagerListener implements PackagerListener
{

    /**
     * Print a message to the console at default priority (MSG_INFO).
     *
     * @param info The information.
     */
    public void packagerMsg(String info)
    {
        packagerMsg(info, MSG_INFO);
    }

    /**
     * Print a message to the console at the specified priority.
     *
     * @param info     The information.
     * @param priority priority to be used for the message prefix
     */
    public void packagerMsg(String info, int priority)
    {
        final String prefix;
        switch (priority)
        {
            case MSG_DEBUG:
                prefix = "[ DEBUG ] ";
                break;
            case MSG_ERR:
                prefix = "[ ERROR ] ";
                break;
            case MSG_WARN:
                prefix = "[ WARNING ] ";
                break;
            case MSG_INFO:
            case MSG_VERBOSE:
            default: // don't die, but don't prepend anything
                prefix = "";
        }

        System.out.println(prefix + info);
    }

    /**
     * Called when the packager starts.
     */
    public void packagerStart()
    {
        System.out.println("[ Begin ]");
        System.out.println();
    }

    /**
     * Called when the packager stops.
     */
    public void packagerStop()
    {
        System.out.println();
        System.out.println("[ End ]");
    }
}
