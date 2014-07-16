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

import java.util.logging.Level;
import java.util.logging.Logger;

import jline.internal.TerminalLineSettings;

/**
 * Terminal that is used for unix platforms. Terminal initialization
 * is handled by issuing the <em>stty</em> command against the
 * <em>/dev/tty</em> file to disable character echoing and enable
 * character input. All known unix systems (including
 * Linux and Macintosh OS X) support the <em>stty</em>), so this
 * implementation should work for an reasonable POSIX system.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:dwkemp@gmail.com">Dale Kemp</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:jbonofre@apache.org">Jean-Baptiste Onofré</a>
 * @since 2.0
 */
public class UnixTerminal
    extends TerminalSupport
{
    private static final Logger logger = Logger.getLogger(UnixTerminal.class.getName());

    private final TerminalLineSettings settings = new TerminalLineSettings();

    public UnixTerminal() throws Exception {
        super(true);
    }

    protected TerminalLineSettings getSettings() {
        return settings;
    }

    /**
     * Remove line-buffered input by invoking "stty -icanon min 1"
     * against the current terminal.
     */
    @Override
    public void init() throws Exception {
        super.init();

        setAnsiSupported(true);

        // Set the console to be character-buffered instead of line-buffered.
        // Make sure we're distinguishing carriage return from newline.
        // Allow ctrl-s keypress to be used (as forward search)
        settings.set("-icanon min 1 -icrnl -inlcr -ixon");
        settings.set("dsusp undef");

        setEchoEnabled(false);
    }

    /**
     * Restore the original terminal configuration, which can be used when
     * shutting down the console reader. The ConsoleReader cannot be
     * used after calling this method.
     */
    @Override
    public void restore() throws Exception {
        settings.restore();
        super.restore();
    }

    /**
     * Returns the value of <tt>stty columns</tt> param.
     */
    @Override
    public int getWidth() {
        int w = settings.getProperty("columns");
        return w < 1 ? DEFAULT_WIDTH : w;
    }

    /**
     * Returns the value of <tt>stty rows>/tt> param.
     */
    @Override
    public int getHeight() {
        int h = settings.getProperty("rows");
        return h < 1 ? DEFAULT_HEIGHT : h;
    }

    @Override
    public synchronized void setEchoEnabled(final boolean enabled) {
        try {
            if (enabled) {
                settings.set("echo");
            }
            else {
                settings.set("-echo");
            }
            super.setEchoEnabled(enabled);
        }
        catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            logger.log(Level.FINE, "Failed to " + (enabled ? "enable" : "disable") + " echo", e);
        }
    }

    public void disableInterruptCharacter()
    {
        try {
            settings.set("intr undef");
        }
        catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            logger.log(Level.FINE, "Failed to disable interrupt character", e);
        }
    }

    public void enableInterruptCharacter()
    {
        try {
            settings.set("intr ^C");
        }
        catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            logger.log(Level.FINE, "Failed to enable interrupt character", e);
        }
    }
}
