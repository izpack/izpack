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

/**
 * Representation of the input terminal for a platform.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
public interface Terminal
{
    void init() throws Exception;

    void restore() throws Exception;

    void reset() throws Exception;

    boolean isSupported();

    int getWidth();

    int getHeight();

    boolean isAnsiSupported();

    /**
     * When ANSI is not natively handled, the output will have to be wrapped.
     */
    OutputStream wrapOutIfNeeded(OutputStream out);

    /**
     * When using native support, return the InputStream to use for reading characters
     * else return the input stream passed as a parameter.
     *
     * @since 2.6
     */
    InputStream wrapInIfNeeded(InputStream in) throws IOException;

    /**
     * For terminals that don't wrap when character is written in last column,
     * only when the next character is written.
     * These are the ones that have 'am' and 'xn' termcap attributes (xterm and
     * rxvt flavors falls under that category)
     */
    boolean hasWeirdWrap();

    boolean isEchoEnabled();

    void setEchoEnabled(boolean enabled);

    String getOutputEncoding();

}
