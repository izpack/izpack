/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2008 Jeff Gordon
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
import com.izforge.izpack.panels.PasswordGroup;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.util.Map;

/**
 * @author Jeff Gordon
 */
public class PasswordEncryptionValidator implements Validator
{
    private Cipher encryptCipher;

    public boolean validate(ProcessingClient client, AutomatedInstallData adata)
    {
        boolean returnValue = true;
        String encryptedPassword = null;
        String key = null;
        String algorithm = null;
        Map params = getParams(client);
        try
        {
            key = (String) params.get("encryptionKey");
            algorithm = (String) params.get("algorithm");
            if (key != null && algorithm != null)
            {
                initialize(key, algorithm);
                encryptedPassword = encryptString(client.getFieldContents(0));
                if (encryptedPassword != null)
                {
                    PasswordGroup group = (PasswordGroup) client;
                    group.setModifiedPassword(encryptedPassword);
                }
                else
                {
                    returnValue = false;
                }
            }
        }
        catch (Exception e)
        {
            Debug.trace("Password Encryption Failed: " + e);
            returnValue = false;
        }
        return (returnValue);
    }

    private Map getParams(ProcessingClient client)
    {
        PasswordGroup group = null;
        Map params = null;
        try
        {
            group = (PasswordGroup) client;
            if (group.hasParams())
            {
                params = group.getValidatorParams();
            }
        }
        catch (Exception e)
        {
            Debug.trace("getParams() Failed: " + e);
        }
        return (params);
    }

    private void initialize(String key, String algorithm) throws Exception
    {
        try
        {
            //Generate the key bytes
            KeyGenerator keygen = KeyGenerator.getInstance(algorithm);
            keygen.init(new SecureRandom(key.getBytes()));
            byte[] keyBytes = keygen.generateKey().getEncoded();
            SecretKeySpec specKey = new SecretKeySpec(keyBytes, algorithm);
            //Initialize the encryption cipher
            encryptCipher = Cipher.getInstance(algorithm);
            encryptCipher.init(Cipher.ENCRYPT_MODE, specKey);
        }
        catch (Exception e)
        {
            Debug.trace("Error initializing password encryption " + e.getMessage());
            throw e;
        }
    }

    public String encryptString(String string) throws Exception
    {
        String result = null;
        try
        {
            byte[] cryptedbytes = null;
            cryptedbytes = encryptCipher.doFinal(string.getBytes("UTF-8"));
            result = (new BASE64Encoder()).encode(cryptedbytes);
        }
        catch (Exception e)
        {
            Debug.trace("Error encrypting string: " + e.getMessage());
            throw e;
        }
        return result;
    }
}
