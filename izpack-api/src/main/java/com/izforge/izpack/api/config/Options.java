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
package com.izforge.izpack.api.config;

import java.io.*;
import java.net.URL;
import java.util.List;

import com.izforge.izpack.api.config.spi.OptionsBuilder;
import com.izforge.izpack.api.config.spi.OptionsFormatter;
import com.izforge.izpack.api.config.spi.OptionsHandler;
import com.izforge.izpack.api.config.spi.OptionsParser;

public class Options extends BasicOptionMap implements Persistable, Configurable
{

    private static final long serialVersionUID = -1119753444859181822L;
    private List<String> headerComment;
    private List<String> footerComment;
    private Config config;
    private File file;

    public Options()
    {
        this(Config.getGlobal().clone());
    }

    public Options(Config config)
    {
        config.setEmptyOption(true);
        setConfig(config);
    }

    public Options(Reader input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Options(Reader input, Config config) throws IOException, InvalidFileFormatException
    {
        this(config);
        load(input);
    }

    public Options(InputStream input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Options(InputStream input, Config config) throws IOException, InvalidFileFormatException
    {
        this(config);
        load(input);
    }

    public Options(URL input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Options(URL input, Config config) throws IOException, InvalidFileFormatException
    {
        this(config);
        load(input);
    }

    public Options(File input) throws IOException, InvalidFileFormatException
    {
        this();
        this.file = input;
        load();
    }

    public Options(File input, Config config) throws IOException, InvalidFileFormatException
    {
        this(config);
        this.file = input;
        load();
    }

    public List<String> getHeaderComment()
    {
        return this.headerComment;
    }

    public void setHeaderComment(List<String> value)
    {
        this.headerComment = value;
    }

    public List<String> getFooterComment()
    {
        return this.footerComment;
    }

    public void setFooterComment(List<String> value)
    {
        this.footerComment = value;
    }

    @Override
    public Config getConfig()
    {
        return this.config;
    }

    @Override
    public void setConfig(Config value)
    {
        this.config = value;
    }

    @Override
    public File getFile()
    {
        return this.file;
    }

    @Override
    public void setFile(File value)
    {
        this.file = value;
    }

    @Override
    public void load() throws IOException, InvalidFileFormatException
    {
        if (this.file == null)
        {
            throw new FileNotFoundException();
        }

        load(this.file);
    }

    @Override
    public void load(InputStream input) throws IOException, InvalidFileFormatException
    {
        load(new InputStreamReader(input, getConfig().getFileEncoding()));
    }

    @Override
    public void load(Reader input) throws IOException, InvalidFileFormatException
    {
        OptionsParser.newInstance(getConfig()).parse(input, newBuilder());
    }

    @Override
    public void load(URL input) throws IOException, InvalidFileFormatException
    {
        OptionsParser.newInstance(getConfig()).parse(input, newBuilder());
    }

    @Override
    public void load(File input) throws IOException, InvalidFileFormatException
    {
        load(input.toURI().toURL());
    }

    @Override
    public void store() throws IOException
    {
        if (this.file == null)
        {
            throw new FileNotFoundException();
        }

        store(this.file);
    }

    @Override
    public void store(OutputStream output) throws IOException
    {
        store(new OutputStreamWriter(output, getConfig().getFileEncoding()));
    }

    @Override
    public void store(Writer output) throws IOException
    {
        store(OptionsFormatter.newInstance(output, getConfig()));
    }

    @Override
    public void store(File output) throws IOException
    {
        OutputStream stream = new FileOutputStream(output);

        store(stream);
        stream.close();
    }

    protected OptionsHandler newBuilder()
    {
        return OptionsBuilder.newInstance(this);
    }

    protected void store(OptionsHandler formatter) throws IOException
    {
        formatter.startOptions();
        storeComment(formatter, this.headerComment);

        for (String name : keySet())
        {
            int c = getNewLineCount(name);
            if (getConfig().isEmptyLines() && c > 0)
            {
                for (int i = 0; i < c; i++)
                {
                    formatter.handleEmptyLine();
                }
            }

            storeComment(formatter, getComment(name));
            int n = getConfig().isMultiOption() || getConfig().isAutoNumbering() ? length(name) : 1;

            for (int i = 0; i < n; i++)
            {
                String value = get(name, i);

                if (getConfig().isAutoNumbering() && name.matches("(.+\\.\\..+)|(.+\\.)+"))
                {
                    if (value != null)
                    {
                        if (name.matches(".+\\.\\..+"))
                        {
                            formatter.handleOption(name.replaceFirst("\\.\\.", "." + Integer.toString(i) + "."), value);
                        }
                        else if (name.matches("(.+\\.)+"))
                        {
                            formatter.handleOption(name + i, value);
                        }
                    }
                }
                else
                {
                    formatter.handleOption(name, value);
                }
            }
        }
        if (this.footerComment != null)
        {
            formatter.handleEmptyLine();
            storeComment(formatter, this.footerComment);
        }

        formatter.endOptions();
    }

    @Override
    boolean isPropertyFirstUpper()
    {
        return getConfig().isPropertyFirstUpper();
    }

    private void storeComment(OptionsHandler formatter, List<String> comment)
    {
        formatter.handleComment(comment);
    }
}
