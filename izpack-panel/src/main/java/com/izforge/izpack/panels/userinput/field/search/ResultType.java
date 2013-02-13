/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.userinput.field.search;

/**
 * Result type. Indicates what the {@link SearchField} is to return.
 *
 * @author Tim Anderson
 */
public enum ResultType
{
    /**
     * Indicates the result of search is the whole file name.
     */
    FILE,

    /**
     * Indicates the result of the search is a directory
     */
    DIRECTORY,

    /**
     * Indicates the result of the search is the parent directory
     */
    PARENTDIR
}
