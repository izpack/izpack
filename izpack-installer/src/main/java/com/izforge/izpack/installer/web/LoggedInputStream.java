/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Vladimir Ralev
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
package com.izforge.izpack.installer.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import com.izforge.izpack.api.data.Pack;

/**
 * Wraps an InputStream in order to track how much bytes are being read, and
 * then updates the progress dialog. When the stream is opened the progress
 * dialog shows up. When the stream is closed the dialog is disposed. Make sure
 * you are closing the streams.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
public class LoggedInputStream extends InputStream
{

    private long bytesRead = 0;
    private InputStream is;
    private DownloadPanel downloader;
    private volatile boolean cancelled = false;
    private long lastTime = -1;
    private long lastBytes = -1;

    public void setCancelled(boolean cancel)
    {
        cancelled = cancel;
    }

    public LoggedInputStream(InputStream is, WebAccessor webAccessor)
    {
        if (is == null)
        {
            throw new RuntimeException("Unable to connect");
        }
        this.is = is;
        // this.webAccessor = webAccessor;

        String sizeStr;
        if (webAccessor.getContentLength() > 0)
        {
            sizeStr = "(" + Pack.toByteUnitsString(webAccessor.getContentLength()) + ")";
        }
        else
        {
            sizeStr = "";
        }

        downloader = new DownloadPanel(this);
        downloader.setTitle("Downloading");
        downloader.setFileLabel(webAccessor.getUrl() + " " + sizeStr);
        downloader.setLocationRelativeTo(null);
        downloader.setVisible(true);
        if (webAccessor.getContentLength() > 0)
        {
            downloader.setProgressMax(webAccessor.getContentLength());
            downloader.setProgressCurrent(0);
        }
    }

    public int available() throws IOException
    {
        return is.available();
    }

    public void close() throws IOException
    {
        downloader.setVisible(false);
        downloader.dispose();
        is.close();
    }

    public synchronized void mark(int readlimit)
    {
        is.mark(readlimit);
    }

    public boolean markSupported()
    {
        return is.markSupported();
    }

    public synchronized void reset() throws IOException
    {
        is.reset();
    }

    public long skip(long n) throws IOException
    {
        return is.skip(n);
    }

    public int read(byte[] b, int off, int len) throws IOException
    {
        int bytes = is.read(b, off, len);
        if (bytes > 0)
        {
            bytesRead += bytes;
        }
        update();
        return bytes;
    }

    public int read(byte[] b) throws IOException
    {
        int bytes = is.read(b);
        if (bytes > 0)
        {
            bytesRead += bytes;
        }
        update();
        return bytes;
    }

    public long getBytesRead()
    {
        return bytesRead;
    }

    public int read() throws IOException
    {
        int bytes = is.read();
        if (bytes > 0)
        {
            bytesRead += 1;
        }
        update();
        return bytes;
    }

    /**
     * Updates the download progress.
     *
     * @throws InterruptedIOException if the download has been cancelled
     */
    private void update() throws IOException
    {
        if (lastTime > 0)
        {
            long currTime = System.currentTimeMillis();
            long diff = currTime - lastTime;
            if (diff > 800)
            {
                double bps = (double) (bytesRead - lastBytes) / ((double) (diff) / 1000.);
                downloader.setStatusLabel(Pack.toByteUnitsString(Math.round(bps)) + "/s");
                lastTime = currTime;
                lastBytes = bytesRead;
            }
        }
        else
        {
            lastTime = System.currentTimeMillis();
            lastBytes = bytesRead;
        }
        downloader.setProgressCurrent((int) bytesRead);
        if (cancelled)
        {
            throw new InterruptedIOException("Cancelled");
        }
    }
}
