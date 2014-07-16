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

package jline.console;

import static jline.console.Operation.BACKWARD_KILL_WORD;
import static jline.console.Operation.BACKWARD_WORD;
import static jline.console.Operation.KILL_WORD;
import static jline.console.Operation.UNIX_WORD_RUBOUT;
import static jline.console.Operation.YANK;
import static jline.console.Operation.YANK_POP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for the {@link KillRing}.
 */
public class KillRingTest extends ConsoleReaderTestSupport {

    @Test
    public void testEmptyKillRing() {
        KillRing killRing = new KillRing();
        assertNull(killRing.yank());
    }

    @Test
    public void testOneElementKillRing() {
        KillRing killRing = new KillRing();
        killRing.add("foo");
        String yanked = killRing.yank();
        assertNotNull(yanked);
        assertEquals(yanked, "foo");
    }

    @Test
    public void testKillKill() {
        // A kill followed by another kill will be saved in the same
        // slot.
        KillRing killRing = new KillRing();
        killRing.add("foo");
        killRing.add(" bar");
        String yanked = killRing.yank();
        assertNotNull(yanked);
        assertEquals(yanked, "foo bar");
    }

    @Test
    public void testYankTwice() {
        // A yank followed by another yank should yield the same
        // string.
        KillRing killRing = new KillRing();
        killRing.add("foo");
        killRing.resetLastKill();
        killRing.add("bar");

        String yanked = killRing.yank();
        assertNotNull(yanked);
        assertEquals(yanked, "bar");

        yanked = killRing.yank();
        assertNotNull(yanked);
        assertEquals(yanked, "bar");
    }


    @Test
    public void testYankPopNoPreviousYank() {
        // A yank-pop without a previous yank should return null.
        KillRing killRing = new KillRing();
        killRing.add("foo");
        String yanked = killRing.yankPop();
        assertNull(yanked);
    }

    @Test
    public void testYankPopWithOneSlot() {
        // Verifies that the ring works fine with one element.
        KillRing killRing = new KillRing();
        killRing.add("foo");

        String yanked = killRing.yank();
        assertNotNull(yanked);
        assertEquals(yanked, "foo");
        //
        yanked = killRing.yankPop();
        assertNotNull(yanked);
        assertEquals(yanked, "foo");
        //
        yanked = killRing.yankPop();
        assertNotNull(yanked);
        assertEquals(yanked, "foo");
    }

    @Test
    public void testYankPop() {
        // Verifies that the ring actually works like that, ie, a
        // series of yank-pop commands should eventually start
        // repeating.
        KillRing killRing = new KillRing();
        killRing.add("foo");
        killRing.resetLastKill();
        killRing.add("bar");
        killRing.resetLastKill();
        killRing.add("baz");

        String yanked = killRing.yank();
        assertNotNull(yanked);
        assertEquals(yanked, "baz");
        //
        yanked = killRing.yankPop();
        assertNotNull(yanked);
        assertEquals(yanked, "bar");
        //
        yanked = killRing.yankPop();
        assertNotNull(yanked);
        assertEquals(yanked, "foo");
        // Back to the beginning.
        yanked = killRing.yankPop();
        assertNotNull(yanked);
        assertEquals(yanked, "baz");
    }

    // Those tests are run using a buffer.

    @Test
    public void testBufferEmptyRing() throws Exception {
        Buffer b = new Buffer("This is a test");
        assertBuffer("This is a test", b = b.op(BACKWARD_WORD));
        assertBuffer("This is a test", b = b.op(YANK));
    }

    @Test
    public void testBufferWordRuboutOnce() throws Exception {
        Buffer b = new Buffer("This is a test");
        assertBuffer("This is a ", b = b.op(UNIX_WORD_RUBOUT));
        assertBuffer("This is a test", b = b.op(YANK));
    }

    @Test
    public void testBufferWordRuboutTwice() throws Exception {
        Buffer b = new Buffer("This is a test");
        assertBuffer("This is a ", b = b.op(UNIX_WORD_RUBOUT));
        assertBuffer("This is ", b = b.op(UNIX_WORD_RUBOUT));
        assertBuffer("This is a test", b = b.op(YANK));
    }

    @Test
    public void testBufferYankPop() throws Exception {
        Buffer b = new Buffer("This is a test");
        b = b.op(BACKWARD_WORD);
        b = b.op(BACKWARD_WORD);
        assertBuffer("This a test", b = b.op(UNIX_WORD_RUBOUT));
        assertBuffer("This a test", b = b.op(BACKWARD_WORD));
        assertBuffer(" a test", b = b.op(KILL_WORD));
        assertBuffer("This a test", b = b.op(YANK));
        assertBuffer("is  a test", b = b.op(YANK_POP));
    }

    @Test
    public void testBufferMixedKillsAndYank() throws Exception {
        Buffer b = new Buffer("This is a test");
        b = b.op(BACKWARD_WORD);
        b = b.op(BACKWARD_WORD);
        assertBuffer("This is  test", b = b.op(KILL_WORD));
        assertBuffer("This  test", b = b.op(BACKWARD_KILL_WORD));
        assertBuffer("This ", b = b.op(KILL_WORD));
        assertBuffer("", b = b.op(BACKWARD_KILL_WORD));
        assertBuffer("This is a test", b = b.op(YANK));
    }
}
