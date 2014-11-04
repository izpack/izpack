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
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.panels.PasswordGroup;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This class based on a simple validator for passwords to demonstrate
 * the implementation of a password validator that cooperates with the
 * password field in the <code>UserInputPanel</code>. Additional validation may
 * be done by utilizing the params added to the password field.
 *
 * @author Elmar Grom
 * @author Jeff Gordon
 */
public class PasswordKeystoreValidator implements Validator
{

    /**
     * PasswordKeystoreValidator
     * Validates the ability to open a keystore based on the password and
     * parameters provided. Must specify parameter 'keystoreFile', and optionally
     * 'keystoreType' (defaults to JKS), 'keystoreAlias' (to check for existence of a key),
     * and 'aliasPassword' (for trying to retrieve the key).
     * An additional parameter 'skipValidation' can be set to 'true' in a checkbox and
     * allow the validator framework to run, but not actually do the validation.
     * <p/>
     * Optionally checking the key password of multiple keys within a keystore
     * requires the keystore password (if different from the key password) be set
     * in the keystorePassword parameter.
     *
     * @param client the client object using the services of this validator.
     * @return <code>true</code> if the validation passes, otherwise <code>false</code>.
     */
    public boolean validate(ProcessingClient client, AutomatedInstallData adata)
    {
        boolean returnValue = false;
        String keystorePassword = null;
        String keystoreFile = null;
        String keystoreType = "JKS";
        String skipValidation = null;
        String alias = null;
        String aliasPassword = null;
        Map<String, String> params = getParams(client);
        try
        {
            if (params != null)
            {
                // Don't try and open the keystore if skipValidation is true
                skipValidation = params.get("skipValidation");
                System.out.println("skipValidation = " + skipValidation);
                if (skipValidation != null && skipValidation.equalsIgnoreCase("true"))
                {
                    System.out.println("Not validating keystore");
                    return true;
                }
                // See if keystore password is passed in or is passed through the validator
                keystorePassword = params.get("keystorePassword");
                if (keystorePassword == null)
                {
                    keystorePassword = getPassword(client);
                    System.out.println("keystorePassword parameter null, using validator password for keystore");
                }
                else if (keystorePassword.equalsIgnoreCase(""))
                {
                    keystorePassword = getPassword(client);
                    System.out.println("keystorePassword parameter empty, using validator password for keystore");
                }
                // See if alias (key) password is passed in or is passed through the validator
                aliasPassword = params.get("aliasPassword");
                if (aliasPassword == null)
                {
                    aliasPassword = getPassword(client);
                    System.out.println("aliasPassword parameter null, using validator password for key");
                }
                else if (aliasPassword.equalsIgnoreCase(""))
                {
                    aliasPassword = getPassword(client);
                    System.out.println("aliasPassword parameter empty, using validator password for key");
                }
                // Get keystore type from parameters or use default
                keystoreType = params.get("keystoreType");
                if (keystoreFile == null)
                {
                    keystoreType = "JKS";
                    System.out.println("keystoreType parameter null, using default of JKS");
                }
                else if (keystorePassword.equalsIgnoreCase(""))
                {
                    keystoreType = "JKS";
                    System.out.println("keystoreType parameter empty, using default of JKS");
                }
                // Get keystore location from params
                keystoreFile = params.get("keystoreFile");
                if (keystoreFile != null)
                {
                    System.out.println("Attempting to open keystore: " + keystoreFile);
                    KeyStore ks = getKeyStore(keystoreFile, keystoreType, keystorePassword.toCharArray());
                    if (ks != null)
                    {
                        returnValue = true;
                        System.out.println("keystore password validated");
                        // check alias if provided
                        alias = params.get("keystoreAlias");
                        if (alias != null)
                        {
                            returnValue = ks.containsAlias(alias);
                            if (returnValue)
                            {
                                System.out.println("keystore alias '" + alias + "' found, trying to retrieve");
                                try
                                {
                                    ks.getKey(alias, aliasPassword.toCharArray());
                                    System.out.println("keystore alias '" + alias + "' validated");
                                }
                                catch (Exception e)
                                {
                                    System.out.println("keystore alias validation failed: " + e);
                                    returnValue = false;
                                }
                            }
                            else
                            {
                                System.out.println("keystore alias '" + alias + "' not found");
                            }
                        }
                    }
                }
                else
                {
                    System.out.println("keystoreFile param not provided");
                }
            }
            else
            {
                System.out.println("params not provided");
            }
        }
        catch (Exception e)
        {
            System.out.println("validate() Failed: " + e);
        }
        return (returnValue);
    }

    private Map<String, String> getParams(ProcessingClient client)
    {
        Map<String, String> returnValue = null;
        PasswordGroup group = null;
        InstallData idata = getIdata(client);
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        try
        {
            group = (PasswordGroup) client;
            if (group.hasParams())
            {
                Map<String, String> params = group.getValidatorParams();
                returnValue = new HashMap<String, String>();
                Iterator<String> keys = params.keySet().iterator();
                while (keys.hasNext())
                {
                    String key = keys.next();
                    // Feed parameter values through vs
                    String value = vs.substitute(params.get(key), null);
                    // System.out.println("Adding local parameter: "+key+"="+value);
                    returnValue.put(key, value);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("getParams() Failed: " + e);
        }
        return returnValue;
    }

    private InstallData getIdata(ProcessingClient client)
    {
        PasswordGroup group = null;
        InstallData idata = null;
        try
        {
            group = (PasswordGroup) client;
            idata = group.getIdata();
        }
        catch (Exception e)
        {
            System.out.println("getIdata() Failed: " + e);
        }
        return idata;
    }

    private String getPassword(ProcessingClient client)
    {
        // ----------------------------------------------------
        // We assume that if there is more than one field an equality validation
        // was already performed.
        // ----------------------------------------------------
        return client.getFieldContents(0);
    }

    public static KeyStore getKeyStore(String fileName, String type, char[] password)
    {
        KeyStore ks = null;
        try
        {
            ks = KeyStore.getInstance(type);
            ks.load(new FileInputStream(fileName), password);
        }
        catch (Exception e)
        {
            System.out.println("getKeyStore() Failed: " + e);
            ks = null;
        }
        return ks;
    }

}
