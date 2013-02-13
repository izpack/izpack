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

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.panels.userinput.field.Alignment;
import com.izforge.izpack.panels.userinput.field.Field;


/**
 * Title field.
 *
 * @author Tim Anderson
 */
public class TitleField extends Field
{

    /**
     * The icon identifier. May be {@code null}.
     */
    private final String iconId;

    /**
     * Determines if the title is bold.
     */
    private final boolean bold;

    /**
     * Determines if the title is in italics.
     */
    private final boolean italic;

    /**
     * The title size.
     */
    private final float titleSize;

    /**
     * The title alignment.
     */
    private final Alignment alignment;


    /**
     * Constructs a {@code TitleField}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public TitleField(TitleFieldConfig config, InstallData installData)
    {
        super(config, installData);
        this.iconId = config.getIconId();
        this.bold = config.isBold();
        this.italic = config.isItalic();
        this.titleSize = config.getTitleSize();
        this.alignment = config.getAlignment();
    }

    /**
     * Returns the icon identifier.
     *
     * @return the icon identifier. May be {@code null}
     */
    public String getIconId()
    {
        return iconId;
    }

    /**
     * Returns the icon name.
     * <p/>
     * This tries to locate a localised name for the icon from the supplied {@code messages}, using the
     * {@link #getIconId() icon identifier} as the key.
     *
     * @param messages the messages
     * @return the icon name. May be {@code null}
     */
    public String getIconName(Messages messages)
    {
        String id = getIconId();
        return (id != null) ? messages.get(id) : null;
    }

    /**
     * Determines if the title is bold.
     *
     * @return {@code true} if the title is bold
     */
    public boolean isBold()
    {
        return bold;
    }

    /**
     * Determines if the title is in italics.
     *
     * @return {@code true} if the title in italics
     */
    public boolean isItalic()
    {
        return italic;
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
        return titleSize;
    }

    /**
     * Returns the title alignment.
     *
     * @return the title alignment
     */
    public Alignment getAlignment()
    {
        return alignment;
    }
}
