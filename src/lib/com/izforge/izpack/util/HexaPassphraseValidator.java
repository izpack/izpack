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
package com.izforge.izpack.util;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;


/**
 * This class represents a simple validator for passwords to test equality.  It is
 * based on the example implementation of a password validator that cooperates with the
 * password field in the <code>UserInputPanel</code>. Additional validation may
 * be done by utilizing the params added to the password field.
 *
 * @author Elmar Grom
 * @author Jeff Gordon
 */
public class HexaPassphraseValidator implements Validator
{

    /**
     * PasswordEqualityValidator
     * Validates the contend of multiple password fields. The test
     *
     * @param client the client object using the services of this validator.
     * @return <code>true</code> if the validation passes, otherwise <code>false</code>.
     */
    public boolean validate(ProcessingClient client, AutomatedInstallData adata)
    {
    	boolean returnValue = false;
        try
        {
           /*  MB 1-3-2016 
            *  convert CA and Server passphrase in Hex format   
            */
            String hexstrServerPassphrasetemp = StringTool.asciiToHex(adata.getVariable("syracuse.certificate.serverpassphrase"));
            adata.setVariable("syracuse.certificate.hexserverpassphrase",hexstrServerPassphrasetemp);
            String hexstrCaPassphrasetemp = StringTool.asciiToHex(adata.getVariable("syracuse.certificate.capassphrase"));
            adata.setVariable("syracuse.certificate.hexcapassphrase",hexstrCaPassphrasetemp);
            returnValue = true;
        }
        catch (Exception e)
        {
            System.out.println("Hexa conversion failed: " + e);
        }
        return (returnValue);
    }

    

}
