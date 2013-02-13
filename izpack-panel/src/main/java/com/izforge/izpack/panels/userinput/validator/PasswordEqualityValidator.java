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

import java.util.Map;
import java.util.logging.Logger;


/**
 * This class represents a simple validator for passwords to test equality.  It is
 * based on the example implementation of a password validator that cooperates with the
 * password field in the <code>UserInputPanel</code>. Additional validation may
 * be done by utilizing the params added to the password field.
 *
 * @author Elmar Grom
 * @author Jeff Gordon
 */
public class PasswordEqualityValidator extends AbstractValidator
{

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(PasswordEqualityValidator.class.getName());

    /**
     * Validates values.
     *
     * @param values     the values to validate
     * @param parameters the validator parameters
     * @return {@code true} if the validation passes, otherwise {@code false}
     */
    @Override
    public boolean validate(String[] values, Map<String, String> parameters)
    {
        if (!parameters.isEmpty())
        {
            logger.warning(getClass().getName() + " does not accept parameters");
        }
        boolean result = true;
        if (values.length > 1)
        {
            String expected = values[0];
            for (int i = 1; i < values.length; ++i)
            {
                String value = values[i];
                if (expected != null && !expected.equals(value) || expected == null && value != null)
                {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

}
