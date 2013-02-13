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
 * Multiple file field reader.
 *
 * @author Tim Anderson
 */
public class MultipleFileFieldReader extends AbstractFileFieldReader implements MultipleFileFieldConfig
{

    /**
     * Constructs a {@code MultipleFileFieldReader}.
     *
     * @param field  the field element
     * @param config the configuration
     */
    public MultipleFileFieldReader(IXMLElement field, Config config)
    {
        super(field, config);
    }

    /**
     * Returns the number of visible rows.
     *
     * @return the number of visible rows, or {@code -1} if none is specified
     */
    public int getVisibleRows()
    {
        return getConfig().getInt(getSpec(), "visibleRows", -1);
    }

    /**
     * Returns the preferred width of the field.
     *
     * @return the preferred width, or {@code -1} if none is specified
     */
    public int getPreferredWidth()
    {
        return getConfig().getInt(getSpec(), "prefX", -1);
    }

    /**
     * Returns the preferred width of the field.
     *
     * @return the preferred height, or {@code -1} if none is specified
     */
    public int getPreferredHeight()
    {
        return getConfig().getInt(getSpec(), "prefY", -1);
    }

    /**
     * Determines if multiple variables should be created to hold the selected files.
     *
     * @return {@code true} if multiple variables should be created; {@code false} if a single variable should be used
     */
    public boolean getCreateMultipleVariables()
    {
        return getConfig().getBoolean(getSpec(), "multipleVariables", false);
    }
}
