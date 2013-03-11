/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.installer.console;

import com.izforge.izpack.util.Console;

/**
 * Abstract class implementing basic functions needed by all panel console helpers.
 *
 * @author Mounir El Hajj
 */
abstract public class ConsolePanelHelper extends AbstractConsolePanel
{

    /**
     * Prompts to end the console panel.
     *
     * @return <tt>1</tt> to continue, <tt>2</tt> to quit, <tt>3</tt> to redisplay
     * @see {@link #promptEndPanel(com.izforge.izpack.api.data.InstallData, Console)}
     * @deprecated
     */
    @Deprecated
    public int askEndOfConsolePanel()
    {
        return new Console().prompt("press 1 to continue, 2 to quit, 3 to redisplay", 1, 3, 2);
    }


}
