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

import com.izforge.izpack.api.config.Config;
import com.izforge.izpack.api.config.InvalidFileFormatException;

public class OptionsParser extends AbstractParser
{
    private static final String COMMENTS = "!#";
    private static final String OPERATORS = ":=";

    public OptionsParser()
    {
        super(OPERATORS, COMMENTS);
    }

    public static OptionsParser newInstance()
    {
        return ServiceFinder.findService(OptionsParser.class);
    }

    public static OptionsParser newInstance(Config config)
    {
        OptionsParser instance = newInstance();

        instance.setConfig(config);

        return instance;
    }

    public void parse(InputStream input, OptionsHandler handler) throws IOException, InvalidFileFormatException
    {
        parse(newIniSource(input, handler), handler);
    }

    public void parse(Reader input, OptionsHandler handler) throws IOException, InvalidFileFormatException
    {
        parse(newIniSource(input, handler), handler);
    }

    public void parse(URL input, OptionsHandler handler) throws IOException, InvalidFileFormatException
    {
        parse(newIniSource(input, handler), handler);
    }

    private void parse(IniSource source, OptionsHandler handler) throws IOException, InvalidFileFormatException
    {
        handler.startOptions();
        for (String line = source.readLine(); line != null; line = source.readLine())
        {
            parseOptionLine(line, source, handler);
        }

        handler.endOptions();
    }
}
