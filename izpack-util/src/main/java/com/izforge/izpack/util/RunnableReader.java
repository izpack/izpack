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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A {@code Runnable} that reads from a {@code Reader} a line at a time.
 * <p/>
 * Subclasses determine what to do with the read data.
 *
 * @author Tim Anderson
 */
public abstract class RunnableReader implements Runnable
{

    /**
     * The reader.
     */
    private final BufferedReader reader;

    /**
     * Indicates to stop reading.
     */
    private volatile boolean stop;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(RunnableReader.class.getName());


    /**
     * Constructs a {@link RunnableReader}.
     *
     * @param reader the reader to read from
     */
    public RunnableReader(Reader reader)
    {
        this.reader = new BufferedReader(reader);
    }

    /**
     * Reads from the reader until:
     * <ul>
     * <li>end-of-file</li>
     * <li>an I/O error occurs</li>
     * <li>{@link #stop} is invoked</li>
     * </ul>
     */
    public void run()
    {
        try
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                read(line);
                if (stop)
                {
                    break;
                }
            }
        }
        catch (IOException exception)
        {
            logger.log(Level.FINE, exception.getMessage(), exception);
        }

    }

    /**
     * Indicates to the reader to stop.
     * <p/>
     * Note that this won't interrupt the reader if it is in a blocking read.
     */
    public void stop()
    {
        stop = true;
    }

    /**
     * Invoked after a line has been read.
     *
     * @param line the line
     * @throws IOException for any I/O error
     */
    protected abstract void read(String line) throws IOException;

}
