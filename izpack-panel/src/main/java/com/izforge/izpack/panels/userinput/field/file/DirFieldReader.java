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


/**
 * Directory field reader.
 *
 * @author Tim Anderson
 */
public class DirFieldReader extends AbstractFileFieldReader implements DirFieldConfig
{

    /**
     * Constructs a {@code DirFieldReader}.
     *
     * @param field  the field element
     * @param config the configuration
     */
    public DirFieldReader(IXMLElement field, Config config)
    {
        super(field, config);
    }

    /**
     * Determines if directories must exist.
     *
     * @return {@code true} if the directories must exist
     */
    public boolean getMustExist()
    {
        return getConfig().getBoolean(getSpec(), "mustExist", true);
    }

    /**
     * Determines if directories can be created if they don't exist.
     *
     * @return {@code true} if directories can be created if they don't exist
     */
    public boolean getCreate()
    {
        return getConfig().getBoolean(getSpec(), "create", false);
    }

}
