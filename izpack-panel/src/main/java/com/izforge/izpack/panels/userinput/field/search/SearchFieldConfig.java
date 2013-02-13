/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 *  Copyright 2013 Tim Anderson
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

import java.util.List;

import com.izforge.izpack.panels.userinput.field.FieldConfig;


/**
 * Search field configuration.
 *
 * @author Tim Anderson
 */
public interface SearchFieldConfig extends FieldConfig
{

    /**
     * Returns the filename to search on.
     *
     * @return the filename to search on. May be {@code null}
     */
    String getFilename();

    /**
     * Returns the filename to check the existence of.
     *
     * @return the filename to to check the existence of. May be {@code null}
     */
    String getCheckFilename();

    /**
     * Returns the search type.
     *
     * @return the search type
     */
    SearchType getSearchType();

    /**
     * Returns the result type.
     *
     * @return the result type
     * @throws com.izforge.izpack.api.exception.IzPackException
     *          if the attribute is not present or is invalid
     */
    ResultType getResultType();

    /**
     * Returns the search choices.
     *
     * @return the search choices
     */
    List<String> getChoices();

    /**
     * Returns the index of the selected choice.
     * <p/>
     * A choice is selected if the "set" attribute is 'true'.
     * <p/>
     * This is only valid after {@link #getChoices()} is invoked.
     *
     * @return the selected index or {@code -1} if no choice is selected
     */
    int getSelectedIndex();
}
