/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.userinput.field.password;

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.FieldReader;


/**
 * Password group field reader.
 *
 * @author Tim Anderson
 */
public class PasswordGroupFieldReader extends FieldReader implements PasswordGroupFieldConfig
{

    /**
     * Constructs a {@code PasswordGroupFieldReader}.
     *
     * @param field the field element
     * @param config the configuration
     */
    public PasswordGroupFieldReader(IXMLElement field, Config config)
    {
        super(field, config);
    }

    /**
     * Returns the password fields.
     *
     * @return the password fields
     */
    public List<PasswordField> getPasswordFields()
    {
        List<PasswordField> result = new ArrayList<PasswordField>();
        Config config = getConfig();
        for (IXMLElement element : getSpec().getChildrenNamed("pwd"))
        {
            int size = config.getInt(element, "size", 1);
            String set = config.getString(element, "set", null);
            String text = getText(element);
            result.add(new PasswordField(text, size, set));
        }
        return result;
    }
}
