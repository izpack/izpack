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


package com.izforge.izpack.panels.userinput.validator;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;


/**
 * This class based on a simple validator for passwords to demonstrate
 * the implementation of a password validator that cooperates with the
 * password field in the <code>UserInputPanel</code>. Additional validation may
 * be done by utilizing the params added to the password field.
 *
 * @author Elmar Grom
 * @author Jeff Gordon
 */
public class PasswordKeystoreValidator extends AbstractValidator
{
    private VariableSubstitutor variableSubstitutor;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(PasswordKeystoreValidator.class.getName());

    public PasswordKeystoreValidator(VariableSubstitutor variableSubstitutor)
    {
        this.variableSubstitutor = variableSubstitutor;
    }

    /**
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
     * @param values     the values to validate
     * @param parameters the validator parameters
     * @return {@code true} if the validation passes, otherwise {@code false}
     */
    @Override
    public boolean validate(String[] values, Map<String, String> parameters)
    {
        boolean returnValue = false;
        String keystorePassword;
        String keystoreFile;
        String keystoreType;
        String skipValidation;
        String alias;
        String aliasPassword;
        Map<String, String> params = getParams(parameters);
        try
        {
            // Don't try and open the keystore if skipValidation is true
            skipValidation = params.get("skipValidation");
            logger.fine("skipValidation = " + skipValidation);
            if (skipValidation != null && skipValidation.equalsIgnoreCase("true"))
            {
                logger.fine("Not validating keystore");
                return true;
            }
            // See if keystore password is passed in or is passed through the validator
            keystorePassword = params.get("keystorePassword");
            if (keystorePassword == null)
            {
                keystorePassword = getPassword(values);
                logger.fine("keystorePassword parameter null, using validator password for keystore");
            }
            else if (keystorePassword.equalsIgnoreCase(""))
            {
                keystorePassword = getPassword(values);
                logger.fine("keystorePassword parameter empty, using validator password for keystore");
            }
            // See if alias (key) password is passed in or is passed through the validator
            aliasPassword = params.get("aliasPassword");
            if (aliasPassword == null)
            {
                aliasPassword = getPassword(values);
                logger.fine("aliasPassword parameter null, using validator password for key");
            }
            else if (aliasPassword.equalsIgnoreCase(""))
            {
                aliasPassword = getPassword(values);
                logger.fine("aliasPassword parameter empty, using validator password for key");
            }
            // Get keystore type from parameters or use default
            keystoreType = params.get("keystoreType");
            if (keystoreType == null)
            {
                keystoreType = "JKS";
            }
            logger.fine("keystoreType parameter null, using default of JKS");

            if (keystoreType.equalsIgnoreCase(""))
            {
                keystoreType = "JKS";
                logger.fine("keystoreType parameter empty, using default of JKS");
            }
            // Get keystore location from params
            keystoreFile = params.get("keystoreFile");
            if (keystoreFile != null)
            {
                logger.fine("Attempting to open keystore: " + keystoreFile);
                KeyStore keyStore = getKeyStore(keystoreFile, keystoreType, keystorePassword.toCharArray());
                if (keyStore != null)
                {
                    returnValue = true;
                    logger.fine("keystore password validated");
                    // check alias if provided
                    alias = params.get("keystoreAlias");
                    if (alias != null)
                    {
                        returnValue = keyStore.containsAlias(alias);
                        if (returnValue)
                        {
                            logger.fine("keystore alias '" + alias + "' found, trying to retrieve");
                            try
                            {
                                keyStore.getKey(alias, aliasPassword.toCharArray());
                                logger.fine("keystore alias '" + alias + "' validated");
                            }
                            catch (Exception e)
                            {
                                logger.log(Level.FINE, "keystore alias validation failed: " + e, e);
                                returnValue = false;
                            }
                        }
                        else
                        {
                            logger.fine("keystore alias '" + alias + "' not found");
                        }
                    }
                }
            }
            else
            {
                logger.fine("keystoreFile param not provided");
            }
        }
        catch (Exception e)
        {
            logger.log(Level.FINE, "validate() Failed: " + e, e);
        }

        return returnValue;
    }

    private Map<String, String> getParams(Map<String, String> parameters)
    {
        Map<String, String> result = new HashMap<String, String>();
        for (String key : parameters.keySet())
        {
            // Feed parameter values through vs
            String value = variableSubstitutor.substitute(parameters.get(key));
            result.put(key, value);
        }
        return result;
    }

    private String getPassword(String[] values)
    {
        // ----------------------------------------------------
        // We assume that if there is more than one field an equality validation
        // was already performed.
        // ----------------------------------------------------
        return (values.length > 0) ? values[0] : null;
    }

    private KeyStore getKeyStore(String fileName, String type, char[] password)
    {
        KeyStore keyStore;
        try
        {
            keyStore = KeyStore.getInstance(type);
            keyStore.load(new FileInputStream(fileName), password);
        }
        catch (Exception e)
        {
            System.out.println("getKeyStore() Failed: " + e);
            keyStore = null;
        }
        return keyStore;
    }

}
