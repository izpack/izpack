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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * A logging handler that writes {@link LogRecord} entries to the console.
 * Depending on the log level, the output is directed either to the standard output stream (System.out)
 * or the standard error stream (System.err).
 * <p>
 * This handler extends {@link StreamHandler} and provides a buffered mechanism
 * for handling log records through an internal {@code LogBuffer}.
 * <p>
 * Features:
 * - Logs messages of {@link Level#INFO} or lower to the standard output stream.
 * - Logs messages of higher levels to the standard error stream for better visibility.
 * - Handles exceptions that occur during writing via a custom error handler.
 *
 * @author Patrick Reinhart
 */
public final class ConsoleHandler extends StreamHandler
{
  private final LogBuffer buffer;

  public ConsoleHandler()
  {
    super();
    buffer = new LogBuffer(this::handleWriteError);
    setOutputStream(buffer);
    setFormatter(new ConsoleFormatter());
  }

  private void handleWriteError(Exception writeError)
  {
    getErrorManager().error("Failed to log to stream", writeError, ErrorManager.WRITE_FAILURE);
  }

  @Override
  public void publish(LogRecord logRecord)
  {
    super.publish(logRecord);
    flush();
    if (logRecord.getLevel().intValue() > Level.INFO.intValue())
    {
      buffer.flushToStream(System.err);
    } else {
      buffer.flushToStream(System.out);
    }
  }

  static class LogBuffer extends ByteArrayOutputStream
  {
    private final Consumer<Exception> errorHandler;

    public LogBuffer(Consumer<Exception> errorHandler)
    {
      super();
      this.errorHandler = errorHandler;
    }

    void flushToStream(OutputStream out)
    {
      try
      {
        out.write(buf, 0, count);
      }
      catch (IOException e)
      {
        errorHandler.accept(e);
      }
      finally
      {
        count = 0;
      }
    }
  }
}
