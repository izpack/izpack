/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 *  Copyright 2012 Tim Anderson
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.FieldReader;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.PlatformModelMatcher;


/**
 * Search field reader.
 *
 * @author Tim Anderson
 */
public class SearchFieldReader extends FieldReader implements SearchFieldConfig
{

    /**
     * The selected choice, or {@code -1} if no choice is selected.
     */
    private int selected = -1;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(SearchFieldReader.class.getName());

    /**
     * Search type attribute name.
     */
    private static final String SEARCH_TYPE = "type";

    /**
     * Result type attribute name.
     */
    private static final String RESULT_TYPE = "result";

    /**
     * Constructs a {@code SearchFieldReader}.
     *
     * @param field  the field element
     * @param config the configuration
     */
    public SearchFieldReader(IXMLElement field, Config config, PlatformModelMatcher matcher)
    {
        super(field, config);
        this.matcher = matcher;
    }

    /**
     * Returns the filename to search on.
     *
     * @return the filename to search on. May be {@code null}
     */
    public String getFilename()
    {
        return getConfig().getString(getSpec(), "filename", null);
    }

    /**
     * Returns the filename to check the existence of.
     *
     * @return the filename to to check the existence of. May be {@code null}
     */
    public String getCheckFilename()
    {
        return getConfig().getString(getSpec(), "checkfilename", null);
    }

    /**
     * Returns the search type.
     *
     * @return the search type
     */
    public SearchType getSearchType()
    {
        SearchType result = SearchType.FILE;
        Config config = getConfig();
        IXMLElement spec = getSpec();
        String value = config.getString(spec, SEARCH_TYPE, result.toString());
        try
        {
            result = SearchType.valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            logger.log(Level.INFO, "Invalid value for '" + SEARCH_TYPE + "': " + value + " in "
                    + config.getContext(spec));
        }
        return result;
    }

    /**
     * Returns the result type.
     *
     * @return the result type
     * @throws IzPackException if the attribute is not present or is invalid
     */
    public ResultType getResultType()
    {
        ResultType result;
        Config config = getConfig();
        IXMLElement spec = getSpec();
        String value = config.getAttribute(spec, RESULT_TYPE);
        try
        {
            result = ResultType.valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new IzPackException("Invalid value for '" + RESULT_TYPE + "': " + value + " in "
                                              + config.getContext(spec));
        }
        return result;
    }

    /**
     * Returns the search choices.
     *
     * @return the search choices
     */
    public List<String> getChoices()
    {
        selected = -1;
        List<String> result = new ArrayList<String>();
        Config config = getConfig();
        IXMLElement spec = getSpec();
        String set = config.getString(spec, "set", null);
        for (IXMLElement element : spec.getChildrenNamed("choice"))
        {
            List<OsModel> models = OsConstraintHelper.getOsList(element);
            
            if (matcher.matchesCurrentPlatform(models))
            {
                String value = config.getString(element, "value", null);
                if( value.equals(set) )
                {
                    selected = result.size();
                }
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Returns the index of the selected choice.
     * <p/>
     * A choice is selected if the "set" attribute is 'true'.
     * <p/>
     * This is only valid after {@link #getChoices()} is invoked.
     *
     * @return the selected index or {@code -1} if no choice is selected
     */
    public int getSelectedIndex()
    {
        return selected;
    }
}
