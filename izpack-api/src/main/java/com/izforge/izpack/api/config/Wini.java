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
package com.izforge.izpack.api.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import com.izforge.izpack.api.config.spi.WinEscapeTool;

public class Wini extends Ini
{

    private static final long serialVersionUID = -2781377824232440728L;
    public static final char PATH_SEPARATOR = '\\';

    public Wini()
    {
        Config cfg = Config.getGlobal().clone();

        cfg.setEscape(false);
        cfg.setEscapeNewline(false);
        cfg.setGlobalSection(true);
        cfg.setEmptyOption(true);
        cfg.setMultiOption(false);
        cfg.setPathSeparator(PATH_SEPARATOR);
        setConfig(cfg);
    }

    public Wini(File input) throws IOException, InvalidFileFormatException
    {
        this();
        setFile(input);
        load();
    }

    public Wini(URL input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Wini(InputStream input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Wini(Reader input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public String escape(String value)
    {
        return WinEscapeTool.getInstance().escape(value);
    }

    public String unescape(String value)
    {
        return WinEscapeTool.getInstance().unescape(value);
    }
}
