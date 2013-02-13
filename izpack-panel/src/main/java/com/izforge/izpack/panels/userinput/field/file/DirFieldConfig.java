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
 * Directory field configuration.
 *
 * @author Tim Anderson
 */
public interface DirFieldConfig extends FileFieldConfig
{
    /**
     * Determines if directories must exist.
     *
     * @return {@code true} if the directories must exist
     */
    boolean getMustExist();

    /**
     * Determines if directories can be created if they don't exist.
     *
     * @return {@code true} if directories can be created if they don't exist
     */
    boolean getCreate();

}
