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

import static jline.internal.Preconditions.checkNotNull;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jline.internal.Configuration;

/**
 * Creates terminal instances.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
public class TerminalFactory
{
    private static final Logger logger = Logger.getLogger(TerminalFactory.class.getName());

    public static final String JLINE_TERMINAL = "jline.terminal";

    public static final String AUTO = "auto";

    public static final String UNIX = "unix";

    public static final String WIN = "win";

    public static final String WINDOWS = "windows";

    public static final String NONE = "none";

    public static final String OFF = "off";

    public static final String FALSE = "false";

    private static Terminal term = null;

    public static synchronized Terminal create() {
        logger.log(Level.FINER, "", new Throwable("CREATE MARKER"));

        String type = Configuration.getString(JLINE_TERMINAL, AUTO);
        if ("dumb".equals(System.getenv("TERM"))) {
            type = "none";
            logger.fine("$TERM=dumb; setting type=" + type);
        }

        logger.fine("Creating terminal; type=" + type);

        Terminal t;
        try {
            String tmp = type.toLowerCase();

            if (tmp.equals(UNIX)) {
                t = getFlavor(Flavor.UNIX);
            }
            else if (tmp.equals(WIN) | tmp.equals(WINDOWS)) {
                t = getFlavor(Flavor.WINDOWS);
            }
            else if (tmp.equals(NONE) || tmp.equals(OFF) || tmp.equals(FALSE)) {
                t = new UnsupportedTerminal();
            }
            else {
                if (tmp.equals(AUTO)) {
                    String os = Configuration.getOsName();
                    Flavor flavor = Flavor.UNIX;
                    if (os.contains(WINDOWS)) {
                        flavor = Flavor.WINDOWS;
                    }
                    t = getFlavor(flavor);
                }
                else {
                    try {
                        t = (Terminal) Thread.currentThread().getContextClassLoader().loadClass(type).newInstance();
                    }
                    catch (Exception e) {
                        throw new IllegalArgumentException(MessageFormat.format("Invalid terminal type: {0}", type), e);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.FINE, "Failed to construct terminal; falling back to unsupported", e);
            t = new UnsupportedTerminal();
        }

        logger.fine("Created Terminal: " + t);

        try {
            t.init();
        }
        catch (Throwable e) {
            logger.log(Level.FINE, "Terminal initialization failed; falling back to unsupported", e);
            return new UnsupportedTerminal();
        }

        return t;
    }

    public static synchronized void reset() {
        term = null;
    }

    public static synchronized void resetIf(final Terminal t) {
        if(t == term) {
            reset();
        }
    }

    public static enum Type
    {
        AUTO,
        WINDOWS,
        UNIX,
        NONE
    }

    public static synchronized void configure(final String type) {
        checkNotNull(type);
        System.setProperty(JLINE_TERMINAL, type);
    }

    public static synchronized void configure(final Type type) {
        checkNotNull(type);
        configure(type.name().toLowerCase());
    }

    //
    // Flavor Support
    //

    public static enum Flavor
    {
        WINDOWS,
        UNIX
    }

    private static final Map<Flavor, Class<? extends Terminal>> FLAVORS = new HashMap<Flavor, Class<? extends Terminal>>();

    static {
        registerFlavor(Flavor.WINDOWS, AnsiWindowsTerminal.class);
        registerFlavor(Flavor.UNIX, UnixTerminal.class);
    }

    public static synchronized Terminal get() {
        if (term == null) {
            term = create();
        }
        return term;
    }

    public static Terminal getFlavor(final Flavor flavor) throws Exception {
        Class<? extends Terminal> type = FLAVORS.get(flavor);
        if (type != null) {
            return type.newInstance();
        }

        throw new InternalError();
    }

    public static void registerFlavor(final Flavor flavor, final Class<? extends Terminal> type) {
        FLAVORS.put(flavor, type);
    }

}
