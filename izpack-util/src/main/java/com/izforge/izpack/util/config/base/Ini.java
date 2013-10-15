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
package com.izforge.izpack.util.config.base;

import java.io.*;
import java.net.URL;

import com.izforge.izpack.util.config.base.spi.IniBuilder;
import com.izforge.izpack.util.config.base.spi.IniFormatter;
import com.izforge.izpack.util.config.base.spi.IniHandler;
import com.izforge.izpack.util.config.base.spi.IniParser;

public class Ini extends BasicProfile implements Persistable, Configurable
{
    private static final long serialVersionUID = -6029486578113700585L;
    private Config _config;
    private File _file;

    public Ini()
    {
        this(Config.getGlobal().clone());
    }

    public Ini(Config config)
    {
        setConfig(config);
    }

    public Ini(Reader input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Ini(InputStream input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Ini(URL input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Ini(File input) throws IOException, InvalidFileFormatException
    {
        this();
        _file = input;
        load();
    }

    @Override public Config getConfig()
    {
        return _config;
    }

    @Override public void setConfig(Config value)
    {
        _config = value;
    }

    @Override public File getFile()
    {
        return _file;
    }

    @Override public void setFile(File value)
    {
        _file = value;
    }

    @Override public void load() throws IOException, InvalidFileFormatException
    {
        if (_file == null)
        {
            throw new FileNotFoundException();
        }

        load(_file);
    }

    @Override public void load(InputStream input) throws IOException, InvalidFileFormatException
    {
        load(new InputStreamReader(input, getConfig().getFileEncoding()));
    }

    @Override public void load(Reader input) throws IOException, InvalidFileFormatException
    {
        IniParser.newInstance(getConfig()).parse(input, newBuilder());
    }

    @Override public void load(File input) throws IOException, InvalidFileFormatException
    {
        load(input.toURI().toURL());
    }

    @Override public void load(URL input) throws IOException, InvalidFileFormatException
    {
        IniParser.newInstance(getConfig()).parse(input, newBuilder());
    }

    @Override public void store() throws IOException
    {
        if (_file == null)
        {
            throw new FileNotFoundException();
        }

        store(_file);
    }

    @Override public void store(OutputStream output) throws IOException
    {
        store(new OutputStreamWriter(output, getConfig().getFileEncoding()));
    }

    @Override public void store(Writer output) throws IOException
    {
        store(IniFormatter.newInstance(output, getConfig()));
    }

    @Override public void store(File output) throws IOException
    {
        OutputStream stream = new FileOutputStream(output);

        store(stream);
        stream.close();
    }

    protected IniHandler newBuilder()
    {
        return IniBuilder.newInstance(this);
    }

    @Override protected void store(IniHandler formatter, Profile.Section section)
    {
        if (getConfig().isEmptySection() || (section.size() != 0))
        {
            super.store(formatter, section);
        }
    }

    @Override protected void store(IniHandler formatter, Profile.Section section, String option, int index)
    {
        if (getConfig().isMultiOption() || (index == (section.length(option) - 1)))
        {
            super.store(formatter, section, option, index);
        }
    }

    @Override boolean isTreeMode()
    {
        return getConfig().isTree();
    }

    @Override char getPathSeparator()
    {
        return getConfig().getPathSeparator();
    }

    @Override boolean isPropertyFirstUpper()
    {
        return getConfig().isPropertyFirstUpper();
    }
}
