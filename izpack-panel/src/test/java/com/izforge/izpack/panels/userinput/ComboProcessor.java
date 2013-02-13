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

package com.izforge.izpack.panels.userinput;

import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

/**
 * An {@link Processor} for generating a list of values for a combo field.
 *
 * @author Tim Anderson
 */
public class ComboProcessor implements Processor
{
    /**
     * Processes the contend of an input field.
     *
     * @param client the client object using the services of this processor.
     * @return the values
     */
    @Override
    public String process(ProcessingClient client)
    {
        return "value1:value2:value3:value4:value5";
    }
}
