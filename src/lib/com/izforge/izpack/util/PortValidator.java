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

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A validator to check whether a port is available (free) on the localhost.
 * <p/>
 * This validator can be used for rule input fields in the UserInputPanel to make sure that the port
 * the user entered is not in use.
 *
 * @author thorque
 */
public class PortValidator implements Validator
{

    private static final String PARAM_EXCLUDED_PORTS = "excluded";

    public boolean validate(ProcessingClient client, AutomatedInstallData adata)
    {
        InetAddress inet = null;
        String host = "localhost";
        boolean retValue = true;
        int numfields = client.getNumFields();
        List<String> exludedPorts = new ArrayList<String>();
        Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(InstallData.MODIFY_INSTALLATION));

        if (client.hasParams())
        {
            String param = client.getValidatorParams().get(PARAM_EXCLUDED_PORTS);
            
            if (param!=null && !"".equals(param)) 
            {
                VariableSubstitutor vs = new VariableSubstitutor(adata.getVariables());
                param = vs.substitute(param, null);
                exludedPorts.addAll(Arrays.asList( param.split(";")));
            }
        }

        for (int i = 0; i < numfields; i++)
        {
            String value = client.getFieldContents(i);

            if ((value == null) || (value.length() == 0))
            {
                return false;
            }
            else if (modifyinstallation && exludedPorts.contains(value.trim())) continue;

            try
            {
                Socket socket = new Socket("localhost",Integer.parseInt(value));
                socket.close();
                // Someone responding on port - seems not open
                retValue = false;
            }
            catch (Exception ex)
            {
                if (ex.getMessage().contains("refused")) retValue=true;
                else retValue = false;
            }
        }
        return retValue;
    }

}
