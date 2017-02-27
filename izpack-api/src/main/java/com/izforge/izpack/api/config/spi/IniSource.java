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
package com.izforge.izpack.api.config.spi;

import com.izforge.izpack.api.config.Config;
import com.izforge.izpack.api.data.InstallData;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class IniSource
{

    public static final char INCLUDE_BEGIN = '<';
    public static final char INCLUDE_END = '>';
    public static final char INCLUDE_OPTIONAL = '?';
    private static final char ESCAPE_CHAR = '\\';
    private URL base;
    private IniSource chain;
    private final String commentChars;
    private final Config config;
    private final HandlerBase handler;
    private final LineNumberReader reader;
    List<String> comments = new ArrayList<String>();

    IniSource(InputStream input, HandlerBase handler, String comments, Config config)
    {
        this(new UnicodeInputStreamReader(input, config.getFileEncoding()), handler, comments, config);
    }

    IniSource(Reader input, HandlerBase handler, String comments, Config config)
    {
        this.reader = new LineNumberReader(input);
        this.handler = handler;
        this.commentChars = comments;
        this.config = config;
    }

    IniSource(URL input, HandlerBase handler, String comments, Config config) throws IOException
    {
        this(new UnicodeInputStreamReader(input.openStream(), config.getFileEncoding()), handler, comments, config);
        this.base = input;
    }

    int getLineNumber()
    {
        int ret;

        if (this.chain == null)
        {
            ret = this.reader.getLineNumber();
        }
        else
        {
            ret = this.chain.getLineNumber();
        }

        return ret;
    }

    String readLine() throws IOException
    {
        String line;

        if (this.chain == null)
        {
            line = readLineLocal();
        }
        else
        {
            line = this.chain.readLine();
            if (line == null)
            {
                this.chain = null;
                line = readLine();
            }
        }

        return line;
    }

    private void close() throws IOException
    {
        this.reader.close();
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
            //buff.deleteCharAt(buff.length() - 1);
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
        if (this.config.isInclude() && (line.length() > 2) && (line.charAt(0) == INCLUDE_BEGIN) && (line.charAt(line.length() - 1) == INCLUDE_END))
        {
            line = line.substring(1, line.length() - 1).trim();
            boolean optional = line.charAt(0) == INCLUDE_OPTIONAL;

            if (optional)
            {
                line = line.substring(1).trim();
            }

            InstallData installData = this.config.getInstallData();
            if (installData != null)
            {
                line = installData.getVariables().replace(line);
            }

            URL loc = (this.base == null) ? new URL(line) : new URL(this.base, line);

            if (optional)
            {
                try
                {
                    this.chain = new IniSource(loc, this.handler, this.commentChars, this.config);
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
                this.chain = new IniSource(loc, this.handler, this.commentChars, this.config);
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

        for (line = this.reader.readLine(); line != null; line = this.reader.readLine())
        {
            line = line.trim();
            if (line.length() == 0)
            {
                if (this.config.isEmptyLines())
                {
                    handleComment(comment);
                    handleEmptyLine();
                }
            }
            else if ((this.commentChars.indexOf(line.charAt(0)) >= 0) && (buff.length() == 0))
            {
                comment.append(line.substring(1));
                comment.append(this.config.getLineSeparator());
            }
            else
            {
                handleComment(comment);
                if (!this.config.isEscapeNewline() || ((countEndingEscapes(line) & 1) == 0))
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

        this.handler.handleComment(comments);
        comments.clear();

        return line;
    }
}
