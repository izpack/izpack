/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Elmar Grom
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

package com.izforge.izpack.panels.userinput.gui.password;

import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;

/*---------------------------------------------------------------------------*/

/**
 * This class can be used to manage multiple related password fields. This is used in the
 * <code>UserInputPanel</code> to manage communication with the validator and processor for
 * password fields.
 *
 * @author Elmar Grom
 * @version 0.0.1 / 2/22/03
 * @see com.izforge.izpack.panels.userinput.UserInputPanel
 */
/*---------------------------------------------------------------------------*/
public class PasswordGroup extends ValuesProcessingClient
{
    /**
     * Creates a password group to manage one or more password fields.
     *
     * @param passwords the passwords
     */
    public PasswordGroup(String[] passwords)
    {
        super(passwords);
    }

}
