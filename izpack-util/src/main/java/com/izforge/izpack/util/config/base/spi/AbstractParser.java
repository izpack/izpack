/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Locale;

import com.izforge.izpack.util.config.base.Config;
import com.izforge.izpack.util.config.base.InvalidFileFormatException;

abstract class AbstractParser
{
    private final String _comments;
    private Config _config = Config.getGlobal();
    private final String _operators;

    protected AbstractParser(String operators, String comments)
    {
        _operators = operators;
        _comments = comments;
    }

    protected Config getConfig()
    {
        return _config;
    }

    protected void setConfig(Config value)
    {
        _config = value;
    }

    protected void parseError(String line, int lineNumber) throws InvalidFileFormatException
    {
        throw new InvalidFileFormatException("parse error (at line: " + lineNumber + "): " + line);
    }

    IniSource newIniSource(InputStream input, HandlerBase handler)
    {
        return new IniSource(input, handler, _comments, getConfig());
    }

    IniSource newIniSource(Reader input, HandlerBase handler)
    {
        return new IniSource(input, handler, _comments, getConfig());
    }

    IniSource newIniSource(URL input, HandlerBase handler) throws IOException
    {
        return new IniSource(input, handler, _comments, getConfig());
    }

    void parseOptionLine(String line, HandlerBase handler, int lineNumber) throws InvalidFileFormatException
    {
        int idx = indexOfOperator(line);
        String name = null;
        String value = null;

        if (idx < 0)
        {
            if (getConfig().isEmptyOption())
            {
                name = line;
            }
            else
            {
                parseError(line, lineNumber);
            }
        }
        else
        {
            name = unescapeFilter(line.substring(0, idx)).trim();
            value = unescapeFilter(line.substring(idx + 1)).trim();
        }

        if (name.length() == 0)
        {
            parseError(line, lineNumber);
        }

        if (getConfig().isLowerCaseOption())
        {
            name = name.toLowerCase(Locale.getDefault());
        }

        handler.handleOption(name, value);
    }

    String unescapeFilter(String line)
    {
        return getConfig().isEscape() ? EscapeTool.getInstance().unescape(line) : line;
    }

    protected int indexOfOperator(String line)
    {
        int idx = -1;
        // skip the operator characters that are enclosed within quotes
        // eg: "http://+:80/Temporary_Listen_Addresses/"=hex:
        int start = 0;
        boolean inQuotes = line.charAt(0) == '"';
        while( inQuotes ) {
        	start = line.indexOf('"', start + 1);
        	if( line.charAt(start - 1) != '\\' || line.charAt(start - 2) == '\\' ) {
        		inQuotes = false;
        		start++;
        	}
        }

        for (char c : _operators.toCharArray())
        {
            int index = line.indexOf(c, start);

            if ((index >= 0) && ((idx == -1) || (index < idx)))
            {
                idx = index;
            }
        }

        return idx;
    }
}
