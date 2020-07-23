/*
 * IzPack - Copyright 2001-2020 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2020 Patrick Reinhart
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

package com.izforge.izpack.panels.userinput.field.text;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;

/**
 * Text area field.
 *
 * @author Patrick Reinhart
 */
public class TextArea extends Field
{
    private final int height;

    /**
     * Constructs a {@code TextField}.
     *
     * @param config      the configuration to get field information from
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public TextArea(TextFieldConfig config, InstallData installData)
    {
        super(config, installData);
        height = config.getHeight();
    }

    public int getHeight() {
        if (height < 1) {
            return 5; // default height
        }
        return height;
    }
}
