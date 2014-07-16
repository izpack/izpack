/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2014 René Krell
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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

package org.fusesource.jansi;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.AnsiRenderer.render;
import static org.fusesource.jansi.AnsiRenderer.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link AnsiRenderer} class.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class AnsiRendererTest
{
    @Before
    public void setUp() {
        Ansi.setEnabled(true);
    }

    @Test
    public void testTest() throws Exception {
        assertFalse(test("foo"));
        assertTrue(test("@|foo|"));
        assertTrue(test("@|foo"));
    }

    @Test
    public void testRender() {
        String str = render("@|bold foo|@");
        System.out.println(str);
        assertEquals(ansi().a(INTENSITY_BOLD).a("foo").reset().toString(), str);
    }

    @Test
    public void testRender2() {
        String str = render("@|bold,red foo|@");
        System.out.println(str);
        assertEquals(Ansi.ansi().a(INTENSITY_BOLD).fg(RED).a("foo").reset().toString(), str);
    }

    @Test
    public void testRender3() {
        String str = render("@|bold,red foo bar baz|@");
        System.out.println(str);
        assertEquals(ansi().a(INTENSITY_BOLD).fg(RED).a("foo bar baz").reset().toString(), str);
    }

    @Test
    public void testRender4() {
        String str = render("@|bold,red foo bar baz|@ ick @|bold,red foo bar baz|@");
        System.out.println(str);
        assertEquals(ansi()
                .a(INTENSITY_BOLD).fg(RED).a("foo bar baz").reset()
                .a(" ick ")
                .a(INTENSITY_BOLD).fg(RED).a("foo bar baz").reset()
                .toString(), str);
    }

    @Test
    public void testRender5() {
        // Check the ansi() render method.
        String str = ansi().render("@|bold Hello|@").toString();
        System.out.println(str);
        assertEquals(ansi().a(INTENSITY_BOLD).a("Hello").reset().toString(), str);
    }


    @Test
    public void testRenderNothing() {
        assertEquals("foo", render("foo"));
    }

    @Test
    public void testRenderInvalidMissingEnd() {
        String str = render("@|bold foo");
        assertEquals("@|bold foo", str);
    }

    @Test
    public void testRenderInvalidMissingText() {
        String str = render("@|bold|@");
        assertEquals("@|bold|@", str);
    }
}