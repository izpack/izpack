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

package com.izforge.izpack.panels.userinput.field.title;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.panels.userinput.field.Alignment;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.SimpleFieldReader;

/**
 * Title field reader.
 *
 * @author Tim Anderson
 */
public class TitleFieldReader extends SimpleFieldReader implements TitleFieldConfig
{

    /**
     * Constructs a {@code TitleFieldReader}.
     *
     * @param field  the field element
     * @param config the configuration
     */
    public TitleFieldReader(IXMLElement field, Config config)
    {
        super(field, config);
    }

    /**
     * Returns the title label.
     *
     * @return the title label
     */
    @Override
    public String getLabel()
    {
        return getText(getField());
    }

    /**
     * Returns the icon identifier.
     *
     * @return the icon identifier. May be {@code null}
     */
    public String getIconId()
    {
        return getConfig().getString(getField(), "icon", null);
    }

    /**
     * Determines if the title is bold.
     *
     * @return {@code true} if the title is bold
     */
    public boolean isBold()
    {
        return getConfig().getBoolean(getField(), "bold", false);
    }

    /**
     * Determines if the title is in italics.
     *
     * @return {@code true} if the title in italics
     */
    public boolean isItalic()
    {
        return getConfig().getBoolean(getField(), "italic", false);
    }

    /**
     * Returns the title size.
     * <p/>
     * This is used as a multiplier for the default font size.
     *
     * @return the title size
     */
    public float getTitleSize()
    {
        float result = 2.0f;
        String size = getConfig().getString(getField(), "size", "2.0");
        try
        {
            result = Float.valueOf(size);
        }
        catch (NumberFormatException ignore)
        {
            // do nothing
        }
        return result;
    }

    /**
     * Returns the title alignment.
     *
     * @return the title alignment
     */
    public Alignment getAlignment()
    {
        return getConfig().getAlignment(getField(), "align", Alignment.LEFT);
    }
}
