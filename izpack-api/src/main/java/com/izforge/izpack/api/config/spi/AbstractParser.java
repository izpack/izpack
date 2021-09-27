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

package com.izforge.izpack.api.config.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.config.Config;
import com.izforge.izpack.api.config.InvalidFileFormatException;

abstract class AbstractParser
{
	private static Logger logger = Logger.getLogger(AbstractParser.class.getName());
	
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

    void parseOptionLine(String line, IniSource source, HandlerBase handler) throws InvalidFileFormatException
    {
        int idx = indexOfOperator(line);
        String name = null;
        String value = null;

        if (idx < 0)
        {
            if (getConfig().isEmptyOption())
            {
                name = line;
                value = line;
            }
            else
            {
                parseError(line, source.getLineNumber());
            }
        }
        else
        {
        	// If the value is a string we have to check if the terminating '"' is the last character of the line.
        	// If this is not the case then get the next line.
        	int lastCharacterIndex = line.length() -1;
        	if ((idx < lastCharacterIndex) && (line.charAt(idx + 1) == '"') && (line.charAt(lastCharacterIndex) != '"')) {
        		try {
        			String nextLine = source.readLine();
        			line += nextLine;
    			} 
        		catch (IOException e) 
        		{
    				logger.log(Level.WARNING, "Read next line from ini source to parse options failed.", e);
    	            parseError(line, source.getLineNumber() - 1);
    			}
        	}
            name = unescapeFilter(line.substring(0, idx)).trim();
            value = unescapeFilter(line.substring(idx + 1)).trim();
        }

        if (name.length() == 0)
        {
            parseError(line, source.getLineNumber());
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

    // the 'operator' is the first = that is not in quotes
    protected int indexOfOperator(String line)
    {
        int idx = -1;
        // skip the operator characters that are enclosed within quotes
        // eg: "http://+:80/Temporary_Listen_Addresses/"=hex:
        int start = 0;
        boolean inQuotes = line.charAt(0) == '"';
        while( inQuotes ) {
        	start = line.indexOf('"', start + 1);
    		if (start > 1) {
	        	if( line.charAt(start - 1) != '\\' || line.charAt(start - 2) == '\\' ) {
	        		inQuotes = false;
	        		start++;
	        	}
    		}
    		else if (start < 0) {
        		inQuotes = false;
    		}
        }

        if (start > -1) 
        {
	        for (char c : _operators.toCharArray())
	        {
	            int index = line.indexOf(c, start);
	
	            if ((index >= 0) && ((idx == -1) || (index < idx)))
	            {
	                idx = index;
	            }
	        }
        }
        
        return idx;
    }
}
