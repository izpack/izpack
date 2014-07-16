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

// Based on Apache Karaf impl

/**
 * Non-interruptible (via CTRL-C) {@link UnixTerminal}.
 *
 * @since 2.0
 */
public class NoInterruptUnixTerminal
    extends UnixTerminal
{
    public NoInterruptUnixTerminal() throws Exception {
        super();
    }

    @Override
    public void init() throws Exception {
        super.init();
        getSettings().set("intr undef");
    }

    @Override
    public void restore() throws Exception {
        getSettings().set("intr ^C");
        super.restore();
    }
}
