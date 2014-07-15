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

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.util.config.base.Config;

class IniSource
{
    public static final char INCLUDE_BEGIN = '<';
    public static final char INCLUDE_END = '>';
    public static final char INCLUDE_OPTIONAL = '?';
    private static final char ESCAPE_CHAR = '\\';
    private URL _base;
    private IniSource _chain;
    private final String _commentChars;
    private final Config _config;
    private final HandlerBase _handler;
    private final LineNumberReader _reader;
    List<String> comments = new ArrayList<String>();

    IniSource(InputStream input, HandlerBase handler, String comments, Config config)
    {
        this(new UnicodeInputStreamReader(input, config.getFileEncoding()), handler, comments, config);
    }

    IniSource(Reader input, HandlerBase handler, String comments, Config config)
    {
        _reader = new LineNumberReader(input);
        _handler = handler;
        _commentChars = comments;
        _config = config;
    }

    IniSource(URL input, HandlerBase handler, String comments, Config config) throws IOException
    {
        this(new UnicodeInputStreamReader(input.openStream(), config.getFileEncoding()), handler, comments, config);
        _base = input;
    }

    int getLineNumber()
    {
        int ret;

        if (_chain == null)
        {
            ret = _reader.getLineNumber();
        }
        else
        {
            ret = _chain.getLineNumber();
        }

        return ret;
    }

    String readLine() throws IOException
    {
        String line;

        if (_chain == null)
        {
            line = readLineLocal();
        }
        else
        {
            line = _chain.readLine();
            if (line == null)
            {
                _chain = null;
                line = readLine();
            }
        }

        return line;
    }

    private void close() throws IOException
    {
        _reader.close();
    }

    private int countEndingEscapes(String line)
    {
        int escapeCount = 0;

        for (int i = line.length() - 1; (i >= 0) && (line.charAt(i) == ESCAPE_CHAR); i--)
        {
            escapeCount++;
        }

        return escapeCount;
    }

    private void handleComment(StringBuilder buff)
    {
        if (buff.length() != 0)
        {
            buff.deleteCharAt(buff.length() - 1);
            comments.add(buff.toString());
            buff.delete(0, buff.length());
        }
    }

    private void handleEmptyLine()
    {
        comments.add("\0");
    }

    private String handleInclude(String input) throws IOException
    {
        String line = input;

        if (_config.isInclude() && (line.length() > 2) && (line.charAt(0) == INCLUDE_BEGIN) && (line.charAt(line.length() - 1) == INCLUDE_END))
        {
            line = line.substring(1, line.length() - 1).trim();
            boolean optional = line.charAt(0) == INCLUDE_OPTIONAL;

            if (optional)
            {
                line = line.substring(1).trim();
            }

            URL loc = (_base == null) ? new URL(line) : new URL(_base, line);

            if (optional)
            {
                try
                {
                    _chain = new IniSource(loc, _handler, _commentChars, _config);
                }
                catch (IOException x)
                {
                    assert true;
                }
                finally
                {
                    line = readLine();
                }
            }
            else
            {
                _chain = new IniSource(loc, _handler, _commentChars, _config);
                line = readLine();
            }
        }

        return line;
    }

    private String readLineLocal() throws IOException
    {
        String line = readLineSkipComments();

        if (line == null)
        {
            close();
        }
        else
        {
            line = handleInclude(line);
        }

        return line;
    }

    private String readLineSkipComments() throws IOException
    {
        String line;
        StringBuilder comment = new StringBuilder();
        StringBuilder buff = new StringBuilder();

        for (line = _reader.readLine(); line != null; line = _reader.readLine())
        {
            line = line.trim();
            if (line.length() == 0)
            {
                if (_config.isEmptyLines())
                {
                    handleComment(comment);
                    handleEmptyLine();
                }
            }
            else if ((_commentChars.indexOf(line.charAt(0)) >= 0) && (buff.length() == 0))
            {
                comment.append(line.substring(1));
                comment.append(_config.getLineSeparator());
            }
            else
            {
                handleComment(comment);
                if (!_config.isEscapeNewline() || ((countEndingEscapes(line) & 1) == 0))
                {
                    buff.append(line);
                    line = buff.toString();

                    break;
                }

                buff.append(line.subSequence(0, line.length() - 1));
            }
        }

        // handle end comments
        if ((line == null) && (comment.length() != 0))
        {
            handleComment(comment);
        }

        _handler.handleComment(comments);
        comments.clear();

        return line;
    }
}
