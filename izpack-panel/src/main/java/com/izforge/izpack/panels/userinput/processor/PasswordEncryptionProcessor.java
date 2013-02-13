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

package com.izforge.izpack.panels.userinput.processor;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.izforge.izpack.util.Base64;

/**
 * @author Jeff Gordon
 */
public class PasswordEncryptionProcessor implements Processor
{
    private Cipher encryptCipher;

    private static final Logger logger = Logger.getLogger(PasswordEncryptionProcessor.class.getName());

    /**
     * Processes the contend of an input field.
     *
     * @param client the client object using the services of this processor.
     * @return The result of the encryption.
     * @throws IzPackException if encryption fails
     */
    @Override
    public String process(ProcessingClient client)
    {
        String result;
        Map<String, String> params = getParams(client);
        String key = params.get("encryptionKey");
        String algorithm = params.get("algorithm");
        if (key != null && algorithm != null)
        {
            initialize(key, algorithm);
            result = encryptString(client.getFieldContents(0));
        }
        else
        {
            throw new IzPackException("PasswordEncryptionProcessor requires encryptionKey and algorithm parameters");
        }
        return result;
    }

    private Map<String, String> getParams(ProcessingClient client)
    {
        Map<String, String> params = Collections.emptyMap();
        try
        {
            if (client.hasParams())
            {
                params = client.getValidatorParams();
            }
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, "Getting validator parameters failed: " + e, e);
        }
        return params;
    }

    private void initialize(String key, String algorithm)
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
        catch (Throwable exception)
        {
            logger.log(Level.WARNING, "Error initializing password encryption: " + exception, exception);
            throw new IzPackException("Failed to initialise password encryption: " + exception.getMessage(), exception);
        }
    }

    private String encryptString(String string)
    {
        String result;
        try
        {
            byte[] cryptedbytes = encryptCipher.doFinal(string.getBytes("UTF-8"));
            result = Base64.encodeBytes(cryptedbytes);
        }
        catch (Throwable exception)
        {
            logger.log(Level.WARNING, "Failed to encrypt password: " + exception, exception);
            throw new IzPackException("Failed to encrypt password: " + exception.getMessage(), exception);
        }

        return result;
    }
}
