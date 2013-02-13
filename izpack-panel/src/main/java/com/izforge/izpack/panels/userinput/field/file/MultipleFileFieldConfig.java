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

package com.izforge.izpack.panels.userinput.field.file;

/**
 *
 * Multiple file field configuration.
 *
 * @author Tim Anderson
 */
public interface MultipleFileFieldConfig extends FileFieldConfig
{

    /**
     * Returns the number of visible rows.
     *
     * @return the number of visible rows, or {@code -1} if none is specified
     */
    int getVisibleRows();

    /**
     * Returns the preferred width of the field.
     *
     * @return the preferred width, or {@code -1} if none is specified
     */
    int getPreferredWidth();

    /**
     * Returns the preferred width of the field.
     *
     * @return the preferred height, or {@code -1} if none is specified
     */
    int getPreferredHeight();

    /**
     * Determines if multiple variables should be created to hold the selected files.
     *
     * @return {@code true} if multiple variables should be created; {@code false} if a single variable should be used
     */
    boolean getCreateMultipleVariables();
}
