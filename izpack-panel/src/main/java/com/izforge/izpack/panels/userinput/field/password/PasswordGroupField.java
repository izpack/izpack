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

package com.izforge.izpack.panels.userinput.field.password;

import java.util.List;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;


/**
 * Password group field.
 *
 * @author Tim Anderson
 */
public class PasswordGroupField extends Field
{

    /**
     * The password fields.
     */
    private final List<PasswordField> fields;


    /**
     * Constructs a {@code PasswordGroupField}.
     *
     * @param config      the configuration to get field information from
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public PasswordGroupField(PasswordGroupFieldConfig config, InstallData installData)
    {
        super(config, installData);
        this.fields = config.getPasswordFields();
    }

    /**
     * Returns the password fields.
     *
     * @return the password fields
     */
    public List<PasswordField> getPasswordFields()
    {
        return fields;
    }
}
