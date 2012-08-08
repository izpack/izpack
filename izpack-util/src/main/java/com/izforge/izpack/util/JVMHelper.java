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
package com.izforge.izpack.util;


import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;


/**
 * Helper to return user-specified JVM arguments.
 *
 * @author Tim Anderson
 */
class JVMHelper
{

    /**
     * Returns arguments supplied to the JVM.
     * <p/>
     * This excludes:
     * <ul>
     * <li>-Xdebug</li>
     * <li>-Xrunjdwp</li>
     * <li>-Dself.mod.*</li>
     * <li>-Dizpack.mode</li>
     * <li>-agentlib</li>
     * <li>-javaagent</li>
     * </ul>
     *
     * @return the JVM arguments
     */
    public List<String> getJVMArguments()
    {
        List<String> result = new ArrayList<String>();
        List<String> inputArguments = getInputArguments();
        inputArguments = join(inputArguments);
        for (String arg : inputArguments)
        {
            if (!arg.startsWith("-Dself.mod.") && !arg.equals("-Xdebug") && !arg.startsWith("-Xrunjdwp")
                    && !arg.startsWith("-Dizpack.mode") && !arg.startsWith("-agentlib")
                    && !arg.startsWith("-javaagent"))
            {
                result.add(arg);
            }
        }
        return result;
    }

    /**
     * Returns the JVM input arguments.
     *
     * @return the input arguments
     */
    protected List<String> getInputArguments()
    {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        return bean.getInputArguments();
    }

    /**
     * Joins any arguments that have been split as a workaround for
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6459832">Bug ID 6459832</a>
     * <p/>
     * This looks for arguments that aren't prefixed with a '-', concatenating them to the previous argument with a
     * space.
     *
     * @param arguments the arguments
     * @return the arguments, with any split arguments joined
     */
    protected List<String> join(List<String> arguments)
    {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < arguments.size(); )
        {
            String arg = arguments.get(i);
            ++i;
            while (i < arguments.size() && !arguments.get(i).startsWith("-"))
            {
                arg = arg + " " + arguments.get(i);
                ++i;
            }
            result.add(arg);
        }
        return result;
    }

}
