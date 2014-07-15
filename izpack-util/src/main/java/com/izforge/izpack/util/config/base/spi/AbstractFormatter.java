/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2014 René Krell
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

package com.izforge.izpack.util.config.base.spi;

import java.io.PrintWriter;
import java.util.List;

import com.izforge.izpack.util.config.base.Config;

abstract class AbstractFormatter implements HandlerBase
{
    private static final char COMMENT = '#';
    private Config _config = Config.getGlobal();
    private PrintWriter _output;

    @Override public void handleEmptyLine() {}

    @Override public void handleComment(List<String> comment)
    {
        if (comment != null &&  getConfig().isComment())
        {
            for (String singleComment : comment)
            {
                if (singleComment.startsWith("\0"))
                {
                    // Trick to add intermediate comments separated by new line
                    // before a property and not to loose them
                    getOutput().print(getConfig().getLineSeparator());
                }
                else
                {
                    for (String line : singleComment.split(getConfig().getLineSeparator()))
                    {
                        getOutput().print(COMMENT);
                        getOutput().print(line);
                        getOutput().print(getConfig().getLineSeparator());
                    }
                }
            }
        }
    }

    @Override public void handleOption(String optionName, String optionValue)
    {
        final String operator = getConfig().getOperator();

        if (getConfig().isStrictOperator())
        {
            if (getConfig().isEmptyOption() || (optionValue != null))
            {
                getOutput().print(escapeFilter(optionName));
                getOutput().print(operator);
            }

            if (optionValue != null)
            {
                getOutput().print(escapeFilter(optionValue));
            }

            if (getConfig().isEmptyOption() || (optionValue != null))
            {
                getOutput().print(getConfig().getLineSeparator());
            }
        }
        else
        {
            String value = ((optionValue == null) && getConfig().isEmptyOption()) ? "" : optionValue;

            if (value != null)
            {
                getOutput().print(escapeFilter(optionName));
                getOutput().print(operator);
                getOutput().print(escapeFilter(value));
                getOutput().print(getConfig().getLineSeparator());
            }
        }
    }

    protected Config getConfig()
    {
        return _config;
    }

    protected void setConfig(Config value)
    {
        _config = value;
    }

    protected PrintWriter getOutput()
    {
        return _output;
    }

    protected void setOutput(PrintWriter value)
    {
        _output = value;
    }

    String escapeFilter(String input)
    {
        return getConfig().isEscape() ? EscapeTool.getInstance().escape(input) : input;
    }
}
