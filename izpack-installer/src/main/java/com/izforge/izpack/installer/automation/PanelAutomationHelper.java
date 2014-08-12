/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Tino Schwarze
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

package com.izforge.izpack.installer.automation;

import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.util.Housekeeper;

/**
 * Abstract class implementing basic functions needed by all panel automation helpers.
 *
 * @author tisc
 */
abstract public class PanelAutomationHelper implements AbstractUIHandler
{

    /*
     * @see com.izforge.izpack.api.handler.AbstractUIHandler#emitNotification(java.lang.String)
     */
    @Override
    public void emitNotification(String message)
    {
        System.out.println(message);
    }

    /*
     * @see com.izforge.izpack.api.handler.AbstractUIHandler#emitWarning(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean emitWarning(String title, String message)
    {
        System.err.println("[ WARNING: " + message + " ]");
        // default: continue
        return true;
    }

    /*
     * @see com.izforge.izpack.api.handler.AbstractUIHandler#emitError(java.lang.String, java.lang.String)
     */
    @Override
    public void emitError(String title, String message)
    {
        System.err.println("[ ERROR: " + message + " ]");
    }

    /*
    * @see com.izforge.izpack.api.handler.AbstractUIHandler#emitErrorAndBlockNext(java.lang.String,
    * java.lang.String)
    */
    @Override
    public void emitErrorAndBlockNext(String title, String message)
    {
        emitError(title, message);
    }

    /*
     * @see com.izforge.izpack.api.handler.AbstractUIHandler#askQuestion(java.lang.String,
     * java.lang.String, int)
     */
    @Override
    public int askQuestion(String title, String question, int choices)
    {
        // don't know what to answer
        return AbstractUIHandler.ANSWER_CANCEL;
    }

    /*
     * @see com.izforge.izpack.api.handler.AbstractUIHandler#askQuestion(java.lang.String,
     * java.lang.String, int, int)
     */
    @Override
    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        return default_choice;
    }

    @Override
    public int askWarningQuestion(String title, String question, int choices, int default_choice)
    {
        return askQuestion(title, question,  choices, default_choice);
    }
}
