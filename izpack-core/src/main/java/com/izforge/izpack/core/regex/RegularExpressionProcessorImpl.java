/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.core.regex;

import java.util.Vector;

import com.izforge.izpack.api.regex.RegularExpressionProcessor;
import com.izforge.izpack.util.regex.RegexUtil;
import com.izforge.izpack.util.regex.Regexp;
import com.izforge.izpack.util.regex.RegularExpression;

/**
 * Regular expression utility adapted from and inspired by the PropertyRegEx Ant task
 * (project Ant Contrib)
 *
 * @author René Krell - changes against the original implementation ant-contrib 1.0b3
 * @see <a href='http://ant-contrib.sourceforge.net'>Ant Contrib project</a>
 */
public class RegularExpressionProcessorImpl implements RegularExpressionProcessor
{
    private String input;

    private RegularExpression regexp;
    private String select;
    private String replace;
    private String defaultValue;

    private boolean caseSensitive = true;
    private boolean global = true;

    public void setInput(String input)
    {
        this.input = input;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public void setRegexp(String regex) throws RuntimeException
    {
        this.regexp = new RegularExpression();
        this.regexp.setPattern(regex);
    }


    public void setReplace(String replace)
    {
        if (select != null)
        {
            throw new IllegalArgumentException("You cannot specify both a select and replace expression");
        }
        this.replace = replace;
    }

    public void setSelect(String select)
    {
        if (replace != null)
        {
            throw new IllegalArgumentException("You cannot specify both a select and replace expression");
        }
        this.select = select;
    }

    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }

    public void setGlobal(boolean global)
    {
        this.global = global;
    }

    protected String doReplace() throws RuntimeException
    {
        if (replace == null)
        {
            throw new IllegalArgumentException("No replace expression specified.");
        }

        int options = 0;
        if (!caseSensitive)
        {
            options |= Regexp.MATCH_CASE_INSENSITIVE;
        }
        if (global)
        {
            options |= Regexp.REPLACE_ALL;
        }

        Regexp sregex = regexp.getRegexp();

        String output = null;

        if (sregex.matches(input, options))
        {
            output = sregex.substitute(input,
                                       replace,
                                       options);
        }

        if (output == null)
        {
            if (defaultValue != null)
            {
                return defaultValue;
            }
            else if (replace != null)
            {
                return input;
            }
        }

        return output;
    }

    protected String doSelect() throws RuntimeException
    {
        if (select == null)
        {
            throw new IllegalArgumentException("No select expression specified.");
        }

        int options = 0;
        if (!caseSensitive)
        {
            options |= Regexp.MATCH_CASE_INSENSITIVE;
        }

        Regexp sregex = regexp.getRegexp();

        String output = select;
        Vector<String> groups = sregex.getGroups(input, options);

        if (groups != null && groups.size() > 0)
        {
            output = RegexUtil.select(select, groups);
        }
        else
        {
            output = null;
        }

        if (output == null)
        {
            output = defaultValue;
        }

        return output;
    }


    protected void validate()
    {
        if (regexp == null)
        {
            throw new IllegalArgumentException("No match expression specified.");
        }
        if (replace == null && select == null)
        {
            throw new IllegalArgumentException("You must specify either a replace or select expression");
        }
    }

    public String execute()
    {
        validate();

        if (replace != null)
        {
            return doReplace();
        }
        else
        {
            return doSelect();
        }
    }

}
