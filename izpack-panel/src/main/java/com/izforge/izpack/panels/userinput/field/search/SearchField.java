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

package com.izforge.izpack.panels.userinput.field.search;

import java.util.List;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;


/**
 * Search field.
 *
 * @author Tim Anderson
 */
public class SearchField extends Field
{

    /**
     * The filename to search on. May be {@code null}
     */
    private final String filename;


    /**
     * The filename to check the existence of. May be {@code null}
     */
    private final String checkFilename;

    /**
     * The search type.
     */
    private final SearchType type;

    /**
     * The result type.
     */
    private final ResultType resultType;

    /**
     * The choices.
     */
    private final List<String> choices;

    /**
     * The selected choice.
     */
    private final int selected;

    /**
     * Constructs a {@code SearchField}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public SearchField(SearchFieldConfig config, InstallData installData)
    {
        super(config, installData);
        filename = config.getFilename();
        checkFilename = config.getCheckFilename();
        type = config.getSearchType();
        resultType = config.getResultType();
        choices = config.getChoices();
        selected = config.getSelectedIndex();
    }

    /**
     * Returns the name of the file to search for.
     *
     * @return the name of the file to search for. May be {@code null} if searching for directories
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Returns the filename to check the existence of.
     * <p/>
     * This is used when searching for directories; the file name is appended to a directory to determine if
     * the correct directory has been located.
     *
     * @return the filename to to check the existence of. May be {@code null}
     */
    public String getCheckFilename()
    {
        return checkFilename;
    }

    /**
     * Returns the search type.
     *
     * @return the search type
     */
    public SearchType getType()
    {
        return type;
    }

    /**
     * Returns the result type.
     *
     * @return the result type
     */
    public ResultType getResultType()
    {
        return resultType;
    }

    /**
     * Returns the search choices.
     *
     * @return the search choices
     */
    public List<String> getChoices()
    {
        return choices;
    }

    /**
     * Returns the index of the selected choice.
     *
     * @return the selected index or {@code -1} if no choice is selected
     */
    public int getSelectedIndex()
    {
        return selected;
    }
}
