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

import com.izforge.izpack.util.config.base.spi.OptionsBuilder;
import com.izforge.izpack.util.config.base.spi.OptionsFormatter;
import com.izforge.izpack.util.config.base.spi.OptionsHandler;
import com.izforge.izpack.util.config.base.spi.OptionsParser;

public class Options extends BasicOptionMap implements Persistable, Configurable
{
    private static final long serialVersionUID = -1119753444859181822L;
    private String _comment;
    private Config _config;
    private File _file;

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

    public Options(InputStream input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Options(URL input) throws IOException, InvalidFileFormatException
    {
        this();
        load(input);
    }

    public Options(File input) throws IOException, InvalidFileFormatException
    {
        this();
        _file = input;
        load();
    }

    public String getComment()
    {
        return _comment;
    }

    public void setComment(String value)
    {
        _comment = value;
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
        OptionsParser.newInstance(getConfig()).parse(input, newBuilder());
    }

    @Override public void load(URL input) throws IOException, InvalidFileFormatException
    {
        OptionsParser.newInstance(getConfig()).parse(input, newBuilder());
    }

    @Override public void load(File input) throws IOException, InvalidFileFormatException
    {
        load(input.toURI().toURL());
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
        store(OptionsFormatter.newInstance(output, getConfig()));
    }

    @Override public void store(File output) throws IOException
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
        storeComment(formatter, _comment);
        for (String name : keySet())
        {
            storeNewLines(formatter, getNewLineCount(name));
            storeComment(formatter, getComment(name));
            int n = getConfig().isMultiOption() || getConfig().isAutoNumbering() ? length(name) : 1;

            for (int i = 0; i < n; i++)
            {
                String value = get(name, i);

                if (getConfig().isAutoNumbering() && name.endsWith("."))
                {
                    if (value != null)
                    {
                        formatter.handleOption(name + i, value);
                    }
                }
                else
                {
                    formatter.handleOption(name, value);
                }
            }
        }

        formatter.endOptions();
    }

    @Override boolean isPropertyFirstUpper()
    {
        return getConfig().isPropertyFirstUpper();
    }

    private void storeNewLines(OptionsHandler formatter, int emptyLines)
    {
        for (int i = 0; i < emptyLines; i++)
        {
            formatter.handleEmptyLine();
        }
    }

    private void storeComment(OptionsHandler formatter, String comment)
    {
        formatter.handleComment(comment);
    }

//    public final static void main(String argv[])
//    {
//        Config.getGlobal().setHeaderComment(false);
//        Config.getGlobal().setEmptyLines(true);
//        Config.getGlobal().setAutoNumbering(true);
//        try
//        {
//            Options options = new Options(new File("/home/rkrell/test/config_patch/test1/sm_server.wrapper.conf.old"));
//            options.store(new File("/home/rkrell/test/config_patch/test1/sm_server.wrapper.conf.old.stored"));
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
}
