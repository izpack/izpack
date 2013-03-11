/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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
package com.izforge.izpack.panels.process;

import java.util.HashMap;
import java.util.Map;


/**
 * Helper for testing {@link ProcessPanel}, {@link ProcessConsolePanel} and {@link ProcessPanelAutomation}.
 *
 * @author Tim Anderson
 */
public class Executable
{
    /**
     * The result to return from execution.
     */
    private static boolean result = false;

    /**
     * Determines if the invocation should throw an exception.
     */
    private static boolean exception = false;

    /**
     * The no. of times {@link #run} was invoked.
     */
    private static int invocations = 0;

    /**
     * The arguments passed to each invocation of {@link #run}.
     */
    private static Map<Integer, String[]> args = new HashMap<Integer, String[]>();

    /**
     * Initialises statics.
     */
    public static void init()
    {
        result = false;
        exception = false;
        invocations = 0;
        args.clear();
    }

    /**
     * Sets the result to return from {@link #run}.
     *
     * @param result the result
     */
    public static void setReturn(boolean result)
    {
        Executable.result = result;
    }

    /**
     * Determines if the next invocation should throw an exception.
     *
     * @param exception if {@code true}, throw an exception on invocation
     */
    public static void setException(boolean exception)
    {
        Executable.exception = exception;
    }

    /**
     * Returns the no. of times {@link #run} has been invoked
     *
     * @return the no. of invocations
     */
    public static int getInvocations()
    {
        return invocations;
    }

    /**
     * Returns the arguments for the specified invocation.
     *
     * @param invocation the invocation (starts at 0)
     * @return the arguments for the invocation, or {@code null} if there was no invocation
     */
    public static String[] getArgs(int invocation)
    {
        return args.get(invocation);
    }

    /**
     * Runs the executable.
     *
     * @param handler the handler
     * @param args    the arguments
     * @return the value of {@link #setReturn(boolean)}.
     */
    public boolean run(AbstractUIProcessHandler handler, String... args)
    {
        Executable.args.put(invocations, args);
        ++invocations;
        if (exception) {
            throw new RuntimeException("Executable exception");
        }
        return result;
    }
}
