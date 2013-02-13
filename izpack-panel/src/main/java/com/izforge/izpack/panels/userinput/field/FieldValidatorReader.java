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

package com.izforge.izpack.panels.userinput.field;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.izforge.izpack.api.adaptator.IXMLElement;


/**
 * Reads an {@link FieldValidator} from an {@link Config}.
 *
 * @author Tim Anderson
 */
public class FieldValidatorReader
{

    /**
     * The validator element.
     */
    private final IXMLElement validator;

    /**
     * The configuration.
     */
    private final Config config;


    /**
     * Constructs a {@code} FieldValidatorReader}.
     *
     * @param validator the validator element
     * @param config    the configuration
     */
    public FieldValidatorReader(IXMLElement validator, Config config)
    {
        this.validator = validator;
        this.config = config;
    }

    /**
     * Returns the validator class name.
     *
     * @return the validator class name
     */
    public String getClassName()
    {
        return config.getAttribute(validator, "class");
    }

    /**
     * Returns the validation error message.
     *
     * @return the validation error message. May be {@code null}
     */
    public String getMessage()
    {
        return config.getText(validator);

    }

    /**
     * Returns the validation parameters.
     *
     * @return the validation parameters, in the order they were defined
     */
    public Map<String, String> getParameters()
    {
        Map<String, String> result = Collections.emptyMap();
        List<IXMLElement> params = validator.getChildrenNamed("param");
        if (!params.isEmpty())
        {
            result = new LinkedHashMap<String, String>();

            for (IXMLElement parameter : params)
            {
                String name = config.getAttribute(parameter, "name");
                String value = parameter.getAttribute("value");
                result.put(name, value);
            }
        }
        return result;
    }
}
