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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import jline.console.history.MemoryHistory;

import org.junit.Before;
import org.junit.Test;

public class HistorySearchTest {
    private ConsoleReader reader;
    private ByteArrayOutputStream output;

    @Before
    public void setUp() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[]{});
        output = new ByteArrayOutputStream();
        reader = new ConsoleReader("test console reader", in, output, null);
    }

    private MemoryHistory setupHistory() {
        MemoryHistory history = new MemoryHistory();
        history.setMaxSize(10);
        history.add("foo");
        history.add("fiddle");
        history.add("faddle");
        reader.setHistory(history);
        return history;
    }

    @Test
    public void testReverseHistorySearch() throws Exception {
        MemoryHistory history = setupHistory();

        String readLineResult;
        reader.setInput(new ByteArrayInputStream(new byte[]{KeyMap.CTRL_R, 'f', '\n'}));
        readLineResult = reader.readLine();
        assertEquals("faddle", readLineResult);
        assertEquals(3, history.size());

        reader.setInput(new ByteArrayInputStream(new byte[]{
                KeyMap.CTRL_R, 'f', KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_R, '\n'
        }));
        readLineResult = reader.readLine();
        assertEquals("foo", readLineResult);
        assertEquals(4, history.size());

        reader.setInput(new ByteArrayInputStream(new byte[]{KeyMap.CTRL_R, 'f', KeyMap.CTRL_R, KeyMap.CTRL_R, '\n'}));
        readLineResult = reader.readLine();
        assertEquals("fiddle", readLineResult);
        assertEquals(5, history.size());
    }

    @Test
    public void testForwardHistorySearch() throws Exception {
        MemoryHistory history = setupHistory();

        String readLineResult;
        reader.setInput(new ByteArrayInputStream(new byte[]{
                KeyMap.CTRL_R, 'f', KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_S, '\n'
        }));
        readLineResult = reader.readLine();
        assertEquals("fiddle", readLineResult);
        assertEquals(4, history.size());

        reader.setInput(new ByteArrayInputStream(new byte[]{
                KeyMap.CTRL_R, 'f', KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_S, KeyMap.CTRL_S, '\n'
        }));
        readLineResult = reader.readLine();
        assertEquals("faddle", readLineResult);
        assertEquals(5, history.size());

        reader.setInput(new ByteArrayInputStream(new byte[]{
                KeyMap.CTRL_R, 'f', KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_S, '\n'
        }));
        readLineResult = reader.readLine();
        assertEquals("fiddle", readLineResult);
        assertEquals(6, history.size());
    }

    @Test
    public void testSearchHistoryAfterHittingEnd() throws Exception {
        MemoryHistory history = setupHistory();

        String readLineResult;
        reader.setInput(new ByteArrayInputStream(new byte[]{
                KeyMap.CTRL_R, 'f', KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_R, KeyMap.CTRL_S, '\n'
        }));
        readLineResult = reader.readLine();
        assertEquals("fiddle", readLineResult);
        assertEquals(4, history.size());
    }

    @Test
    public void testSearchHistoryWithNoMatches() throws Exception {
        MemoryHistory history = setupHistory();

        String readLineResult;
        reader.setInput(new ByteArrayInputStream(new byte[]{
                'x', KeyMap.CTRL_S, KeyMap.CTRL_S, '\n'
        }));
        readLineResult = reader.readLine();
        assertEquals("", readLineResult);
        assertEquals(3, history.size());
    }

    @Test
    public void testAbortingSearchRetainsCurrentBufferAndPrintsDetails() throws Exception {
        MemoryHistory history = setupHistory();

        String readLineResult;
        reader.setInput(new ByteArrayInputStream(new byte[]{
                'f', KeyMap.CTRL_R, 'f', KeyMap.CTRL_G
        }));
        readLineResult = reader.readLine();
        assertEquals(null, readLineResult);
        assertTrue(output.toString().contains("(reverse-i-search)`ff':"));
        assertEquals("ff", reader.getCursorBuffer().toString());
        assertEquals(3, history.size());
    }

    @Test
    public void testAbortingAfterSearchingPreviousLinesGivesBlank() throws Exception {
        MemoryHistory history = setupHistory();

        String readLineResult;
        reader.setInput(new ByteArrayInputStream(new byte[]{
                'f', KeyMap.CTRL_R, 'f', '\n',
                'f', 'o', 'o', KeyMap.CTRL_G
        }));
        readLineResult = reader.readLine();
        assertEquals("", readLineResult);

        readLineResult = reader.readLine();
        assertEquals(null, readLineResult);
        assertEquals("", reader.getCursorBuffer().toString());
        assertEquals(3, history.size());
    }
}
