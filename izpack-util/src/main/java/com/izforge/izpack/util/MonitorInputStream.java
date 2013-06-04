/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * This is a grabber for stdout and stderr. It will be launched once at command execution end
 * terminates if the apropriate stream runs out of data.
 *
 * @author Olexij Tkatchenko <ot@parcs.de>
 */
public class MonitorInputStream extends RunnableReader
{

    private BufferedWriter writer;

    /**
     * Construct a new monitor.
     *
     * @param in  The input to read.
     * @param out The writer to write to.
     */
    public MonitorInputStream(Reader in, Writer out)
    {
        super(in);
        this.writer = new BufferedWriter(out);
    }

    /**
     * Invoked after a line has been read.
     * <p/>
     * This echoes the line to the writer.
     *
     * @param line the line
     * @throws IOException for any I/O error
     */
    @Override
    protected void read(String line) throws IOException
    {
        writer.write(line);
        writer.newLine();
        writer.flush();
    }
}