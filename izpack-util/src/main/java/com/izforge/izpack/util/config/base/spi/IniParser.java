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

public class IniParser extends AbstractParser
{
    private static final String COMMENTS = ";#";
    private static final String OPERATORS = ":=";
    static final char SECTION_BEGIN = '[';
    static final char SECTION_END = ']';

    public IniParser()
    {
        super(OPERATORS, COMMENTS);
    }
    
    protected IniParser(String operators, String comments)
    {
    	super(operators, comments);
    }

    public static IniParser newInstance()
    {
        return ServiceFinder.findService(IniParser.class);
    }

    public static IniParser newInstance(Config config)
    {
        IniParser instance = newInstance();

        instance.setConfig(config);

        return instance;
    }

    public void parse(InputStream input, IniHandler handler) throws IOException, InvalidFileFormatException
    {
        parse(newIniSource(input, handler), handler);
    }

    public void parse(Reader input, IniHandler handler) throws IOException, InvalidFileFormatException
    {
        parse(newIniSource(input, handler), handler);
    }

    public void parse(URL input, IniHandler handler) throws IOException, InvalidFileFormatException
    {
        parse(newIniSource(input, handler), handler);
    }

    private void parse(IniSource source, IniHandler handler) throws IOException, InvalidFileFormatException
    {
        handler.startIni();
        String sectionName = null;

        for (String line = source.readLine(); line != null; line = source.readLine())
        {
            if (line.charAt(0) == SECTION_BEGIN)
            {
                if (sectionName != null)
                {
                    handler.endSection();
                }

                sectionName = parseSectionLine(line, source, handler);
            }
            else
            {
                if (sectionName == null)
                {
                    if (getConfig().isGlobalSection())
                    {
                        sectionName = getConfig().getGlobalSectionName();
                        handler.startSection(sectionName);
                    }
                    else
                    {
                        parseError(line, source.getLineNumber());
                    }
                }

                parseOptionLine(line, handler, source.getLineNumber());
            }
        }

        if (sectionName != null)
        {
            handler.endSection();
        }

        handler.endIni();
    }

    private String parseSectionLine(String line, IniSource source, IniHandler handler) throws InvalidFileFormatException
    {
        String sectionName;

        if (line.charAt(line.length() - 1) != SECTION_END)
        {
            parseError(line, source.getLineNumber());
        }

        sectionName = unescapeFilter(line.substring(1, line.length() - 1).trim());
        if ((sectionName.length() == 0) && !getConfig().isUnnamedSection())
        {
            parseError(line, source.getLineNumber());
        }

        if (getConfig().isLowerCaseSection())
        {
            sectionName = sectionName.toLowerCase(Locale.getDefault());
        }

        handler.startSection(sectionName);

        return sectionName;
    }
}
