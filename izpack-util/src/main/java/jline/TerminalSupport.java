/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright (c) 2002-2012, the original authors of the JLine project
 * Copyright 2014 René Krell
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

package jline;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import jline.internal.ShutdownHooks;
import jline.internal.ShutdownHooks.Task;

/**
 * Provides support for {@link Terminal} instances.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
public abstract class TerminalSupport
    implements Terminal
{
    private static final Logger logger = Logger.getLogger(TerminalSupport.class.getName());

    public static final int DEFAULT_WIDTH = 80;

    public static final int DEFAULT_HEIGHT = 24;

    private Task shutdownTask;

    private boolean supported;

    private boolean echoEnabled;

    private boolean ansiSupported;

    protected TerminalSupport(final boolean supported) {
        this.supported = supported;
    }

    @Override
    public void init() throws Exception {
        if (shutdownTask != null) {
            ShutdownHooks.remove(shutdownTask);
        }
        // Register a task to restore the terminal on shutdown
        this.shutdownTask = ShutdownHooks.add(new Task()
        {
            @Override
            public void run() throws Exception {
                restore();
            }
        });
    }

    @Override
    public void restore() throws Exception {
        TerminalFactory.resetIf(this);
        if (shutdownTask != null) {
          ShutdownHooks.remove(shutdownTask);
          shutdownTask = null;
        }
    }

    @Override
    public void reset() throws Exception {
        restore();
        init();
    }

    @Override
    public final boolean isSupported() {
        return supported;
    }

    @Override
    public synchronized boolean isAnsiSupported() {
        return ansiSupported;
    }

    protected synchronized void setAnsiSupported(final boolean supported) {
        this.ansiSupported = supported;
        logger.fine("Ansi supported: " + supported);
    }

    /**
     * Subclass to change behavior if needed.
     * @return the passed out
     */
    @Override
    public OutputStream wrapOutIfNeeded(OutputStream out) {
        return out;
    }

    /**
     * Defaults to true which was the behaviour before this method was added.
     */
    @Override
    public boolean hasWeirdWrap() {
        return true;
    }

    @Override
    public int getWidth() {
        return DEFAULT_WIDTH;
    }

    @Override
    public int getHeight() {
        return DEFAULT_HEIGHT;
    }

    @Override
    public synchronized boolean isEchoEnabled() {
        return echoEnabled;
    }

    @Override
    public synchronized void setEchoEnabled(final boolean enabled) {
        this.echoEnabled = enabled;
        logger.fine("Echo enabled: " + enabled);
    }

    @Override
    public InputStream wrapInIfNeeded(InputStream in) throws IOException {
        return in;
    }

    @Override
    public String getOutputEncoding() {
        // null for unknown
        return null;
    }
}
