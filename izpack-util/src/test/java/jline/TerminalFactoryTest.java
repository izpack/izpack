/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2014 René Krell
 * Copyright (c) 2002-2012, the original authors of JLine
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

/*
 * Copyright (c) 2002-2012, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */

package jline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link TerminalFactory}.
 */
public class TerminalFactoryTest
{
    @Before
    public void setUp() throws Exception {
        TerminalFactory.reset();
    }

    @After
    public void tearDown() throws Exception {
        TerminalFactory.reset();
    }

    @Test
    public void testConfigureNone() {
        try {
            TerminalFactory.configure(TerminalFactory.NONE);
            Terminal t = TerminalFactory.get();
            assertNotNull(t);
            assertEquals(UnsupportedTerminal.class.getName(), t.getClass().getName());
        } finally {
            TerminalFactory.configure(TerminalFactory.AUTO);
        }
    }

    @Test
    public void testConfigureUnsupportedTerminal() {
        try {
            TerminalFactory.configure(UnsupportedTerminal.class.getName());
            Terminal t = TerminalFactory.get();
            assertNotNull(t);
            assertEquals(UnsupportedTerminal.class.getName(), t.getClass().getName());
        } finally {
            TerminalFactory.configure(TerminalFactory.AUTO);
        }
    }
}