/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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
package com.izforge.izpack.logging;

import com.izforge.izpack.util.Debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Abstract log formatter similar to {@link java.util.logging.SimpleFormatter},
 * not being so messy
 */
public abstract class AbstractFormatter extends Formatter
{

    private final String lineSeparator = System.getProperty("line.separator");

    private final Date dat = new Date();
    private final static String FORMAT = "{0,date,yyyy.MM.dd} {0,time,HH:mm:ss}";
    private MessageFormat formatter;
    private final Object[] args = new Object[1];

    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    @Override
    public synchronized String format(LogRecord record)
    {
        StringBuilder sb = new StringBuilder();

        if (isAddTimeStamp())
        {
            dat.setTime(record.getMillis());
            args[0] = dat;
            StringBuffer text = new StringBuffer();
            if (formatter == null)
            {
                formatter = new MessageFormat(FORMAT);
            }
            formatter.format(args, text, null);
            sb.append(text);
            sb.append(" ");
        }

        Level level = record.getLevel();
        if (isAddMessageLevel() || level.intValue() > Level.INFO.intValue())
        {
            // pad left to the max. length of message level texts
            sb.append(record.getLevel().getLocalizedName());
            sb.append(": ");
        }

        // Append log message
        String message = formatMessage(record);
        sb.append(message);

        // Append stacktrace
        Throwable throwable = record.getThrown();
        if (Debug.isStacktrace() && (throwable != null))
        {
            sb.append(lineSeparator);
            try
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            catch (Exception ignored)
            {
            }
        }

        sb.append(lineSeparator);

        return sb.toString();
    }

    /**
     * Choice for final implementations whether to prepend a time stamp at the
     * beginning of each line
     *
     * @return whether to add time stamps
     */
    abstract boolean isAddTimeStamp();

    /**
     * Choice for final implementations whether to add the message level to each
     * line
     *
     * @return whether to add the message level
     */
    abstract boolean isAddMessageLevel();
}
