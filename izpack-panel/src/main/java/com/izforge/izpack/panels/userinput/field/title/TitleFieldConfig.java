/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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

import com.izforge.izpack.panels.userinput.field.Alignment;
import com.izforge.izpack.panels.userinput.field.FieldConfig;


/**
 * Title field configuration.
 *
 * @author Tim Anderson
 */
public interface TitleFieldConfig extends FieldConfig
{

    /**
     * Returns the icon identifier.
     *
     * @return the icon identifier. May be {@code null}
     */
    String getIconId();

    /**
     * Determines if the title is bold.
     *
     * @return {@code true} if the title is bold
     */
    boolean isBold();

    /**
     * Determines if the title is in italics.
     *
     * @return {@code true} if the title in italics
     */
    boolean isItalic();

    /**
     * Returns the title size.
     * <p/>
     * This is used as a multiplier for the default font size.
     *
     * @return the title size
     */
    float getTitleSize();

    /**
     * Returns the title alignment.
     *
     * @return the title alignment
     */
    Alignment getAlignment();
}
