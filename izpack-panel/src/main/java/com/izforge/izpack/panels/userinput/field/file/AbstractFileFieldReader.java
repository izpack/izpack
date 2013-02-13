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

package com.izforge.izpack.panels.userinput.field.file;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.FieldReader;


/**
 * File field reader functionality.
 *
 * @author Tim Anderson
 */
public abstract class AbstractFileFieldReader extends FieldReader implements FileFieldConfig
{

    /**
     * Constructs an {@code AbstractFileFieldReader}.
     *
     * @param field  the field element
     * @param config the configuration
     */
    public AbstractFileFieldReader(IXMLElement field, Config config)
    {
        super(field, config);
    }

    /**
     * Returns the file extension.
     *
     * @return the file extension. May be {@code null}
     */
    public String getFileExtension()
    {
        return getConfig().getString(getSpec(), "fileext", null);
    }

    /**
     * Returns the file extension description.
     *
     * @return the file extension description. May be {@code null}
     */
    public String getFileExtensionDescription()
    {
        Config config = getConfig();
        String result = config.getString(getSpec(), "fileext", null);
        return config.getLocalString(result);
    }

    /**
     * Determines if empty input values are allowed.
     *
     * @return {@code true} if empty input values are allowed
     */
    public boolean getAllowEmptyValue()
    {
        return getConfig().getBoolean(getSpec(), "allowEmptyValue", false);
    }
}
