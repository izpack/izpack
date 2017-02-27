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

/**
 * Static debug mode flags available during installation
 */
public class Debug
{

    /**
     * initial trace flag
     */
    private static boolean trace;

    /**
     * initial stacktrace flag
     */
    private static boolean stacktrace;

    /**
     * initial debug flag
     */
    private static boolean debug;

    static
    {
        stacktrace = Boolean.getBoolean("STACKTRACE");
        trace = Boolean.getBoolean("TRACE");
        debug = Boolean.getBoolean("DEBUG");
    }

    /**
     * Sets whether to trace variables and conditions at runtime interactively.
     *
     * @param trace whether variables and conditions are to be traced at runtime
     * interactively
     */
    public static void setTrace(boolean trace)
    {
        Debug.trace = trace;
    }

    /**
     * Gets whether to trace variables and conditions at runtime interactively.
     *
     * @return whether to trace variables and conditions at runtime
     * interactively
     */
    public static boolean isTrace()
    {
        return trace;
    }

    /**
     * Sets the current trace flag.
     *
     * @param stacktrace desired state
     */
    public static void setStacktrace(boolean stacktrace)
    {
        Debug.stacktrace = stacktrace;
    }

    /**
     * Set whether to show stacktraces at runtime.
     *
     * @return whether to show stacktraces at runtime
     */
    public static boolean isStacktrace()
    {
        return stacktrace;
    }

    /**
     * Sets whether to run in debug mode.
     *
     * @param debug whether the installer is run in debug mode
     */
    public static void setDebug(boolean debug)
    {
        Debug.debug = debug;
    }

    /**
     * Gets whether the installer run in debug mode.
     *
     * @return whether the installer run in debug mode.
     */
    public static boolean isDebug()
    {
        return debug;
    }
}
