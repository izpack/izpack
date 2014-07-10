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

import com.izforge.izpack.panels.userinput.field.FieldConfig;


/**
 * File field configuration.
 *
 * @author Tim Anderson
 */
public interface FileFieldConfig extends FieldConfig
{

    /**
     * Returns the file extension.
     *
     * @return the file extension. May be {@code null}
     */
    String getFileExtension();

    /**
     * Returns the file extension description.
     *
     * @return the file extension description. May be {@code null}
     */
    String getFileExtensionDescription();

    /**
     * Determines if empty input values are allowed.
     *
     * @return {@code true} if empty input values are allowed
     */
    boolean getAllowEmptyValue();

    /**
     * Determine if the file must exist.
     *
     * @return  @return {@code true} if file must exist
     */
    boolean mustExist();
}
