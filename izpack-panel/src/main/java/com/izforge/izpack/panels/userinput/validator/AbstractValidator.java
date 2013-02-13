/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;


/**
 * Abstract implementation of the {@link Validator} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractValidator implements Validator
{

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AbstractValidator.class.getName());

    /**
     * Validates the content of a field.
     *
     * @param client the client object using the services of this validator.
     * @return {@code true} if the validation passes, otherwise {@code false}
     */
    @Override
    public boolean validate(ProcessingClient client)
    {
        boolean result = false;
        try
        {
            String[] values = getValues(client);
            Map<String, String> parameters = getParameters(client);
            result = validate(values, parameters);
        }
        catch (Throwable exception)
        {
            logger.log(Level.WARNING, "Validation failed: " + exception.getMessage(), exception);
        }
        return result;
    }

    /**
     * Validates values.
     *
     * @param values     the values to validate
     * @param parameters the validator parameters
     * @return {@code true} if the validation passes, otherwise {@code false}
     */
    public abstract boolean validate(String[] values, Map<String, String> parameters);

    /**
     * Returns the validator parameters.
     *
     * @param client the client object using the services of this validator.
     * @return the validator parameters
     */
    protected Map<String, String> getParameters(ProcessingClient client)
    {
        return (client.hasParams()) ? client.getValidatorParams() : Collections.<String, String>emptyMap();
    }

    /**
     * Returns the field values.
     *
     * @param client the processing client
     * @return the field values
     */
    protected String[] getValues(ProcessingClient client)
    {
        String[] values = new String[client.getNumFields()];
        for (int i = 0; i < values.length; ++i)
        {
            values[i] = client.getFieldContents(i);
        }
        return values;
    }
}
