/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Thorsten Kamann
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

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * A validator to check wheter a host:port is available (free).
 * <p/>
 * This validator can be used for rule input fields in the UserInputPanel to make sure that the port
 * the user entered is not in use.
 *
 * @author thorque
 */
public class HostAddressValidator implements Validator
{

    public boolean validate(ProcessingClient client)
    {
        InetAddress inet = null;
        String host = "";
        boolean retValue = true;

        try
        {
            host = client.getFieldContents(0);
        }
        catch (Exception e)
        {
            return false;
        }

        try
        {
            inet = InetAddress.getByName(host);
        }
        catch (Exception ex)
        {
            retValue = false;
        }
        return retValue;
    }

}
