/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
import java.util.regex.Pattern;

import com.izforge.izpack.panels.userinput.gui.password.PasswordGroup;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;

/**
 * A validator to enforce non-empty fields.
 * <p/>
 * This validator can be used for rule input fields in the UserInputPanel to make sure that the
 * user's entry matches a specified regular expression.
 *
 * @author Mike Cunneen <mike dot cunneen at screwfix dot com>
 */
public class RegularExpressionValidator implements Validator
{

    public static final String STR_PATTERN_DEFAULT = "[a-zA-Z0-9._-]{3,}@[a-zA-Z0-9._-]+([.][a-zA-Z0-9_-]+)*[.][a-zA-Z0-9._-]{2,4}";

    private static final String PATTERN_PARAM = "pattern";

    public boolean validate(ProcessingClient client)
    {

        String patternString;

        if (client.hasParams())
        {
            Map<String, String> paramMap = client.getValidatorParams();
            patternString = paramMap.get(PATTERN_PARAM);
        }
        else
        {
            patternString = STR_PATTERN_DEFAULT;
        }
        Pattern pattern = Pattern.compile(patternString);
        return pattern.matcher(getString(client)).matches();
    }

    private String getString(ProcessingClient client)
    {
        String returnValue = "";
        if (client instanceof PasswordGroup)
        {
            int numFields = client.getNumFields();
            if (numFields > 0)
            {
                returnValue = client.getFieldContents(0);
            }
            else
            {
                // Should never get here, but might as well try and grab some text
                returnValue = client.getText();
            }
        }
        else
        {
            // Original way to retrieve text for validation
            returnValue = client.getText();
        }
        return returnValue;
    }

}
