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

}
