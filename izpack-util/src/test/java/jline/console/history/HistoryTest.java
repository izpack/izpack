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

package jline.console.history;

import static jline.console.Operation.ACCEPT_LINE;
import static jline.console.Operation.BACKWARD_CHAR;
import static jline.console.Operation.BACKWARD_DELETE_CHAR;
import static jline.console.Operation.BEGINNING_OF_LINE;
import static jline.console.Operation.HISTORY_SEARCH_BACKWARD;
import static jline.console.Operation.HISTORY_SEARCH_FORWARD;
import static jline.console.Operation.NEXT_HISTORY;
import static jline.console.Operation.PREVIOUS_HISTORY;
import jline.console.ConsoleReaderTestSupport;
import jline.console.KeyMap;

import org.junit.Test;

/**
 * Tests command history.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 */
public class HistoryTest
    extends ConsoleReaderTestSupport
{
    @Test
    public void testSingleHistory() throws Exception {
        Buffer b = new Buffer().
            append("test line 1").op(ACCEPT_LINE).
            append("test line 2").op(ACCEPT_LINE).
            append("test line 3").op(ACCEPT_LINE).
            append("test line 4").op(ACCEPT_LINE).
            append("test line 5").op(ACCEPT_LINE).
            append("");

        assertBuffer("", b);

        assertBuffer("test line 5", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 5", b = b.op(BACKWARD_CHAR));
        assertBuffer("test line 4", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 5", b = b.op(NEXT_HISTORY));
        assertBuffer("test line 4", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 3", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 2", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 1", b = b.op(PREVIOUS_HISTORY));

        // beginning of history
        assertBuffer("test line 1", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 1", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 1", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 1", b = b.op(PREVIOUS_HISTORY));

        assertBuffer("test line 2", b = b.op(NEXT_HISTORY));
        assertBuffer("test line 3", b = b.op(NEXT_HISTORY));
        assertBuffer("test line 4", b = b.op(NEXT_HISTORY));
        assertBuffer("test line 5", b = b.op(NEXT_HISTORY));

        // end of history
        assertBuffer("", b = b.op(NEXT_HISTORY));
        assertBuffer("", b = b.op(NEXT_HISTORY));
        assertBuffer("", b = b.op(NEXT_HISTORY));

        assertBuffer("test line 5", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 4", b = b.op(PREVIOUS_HISTORY));
        b = b.op(BEGINNING_OF_LINE).append("XXX").op(ACCEPT_LINE);
        assertBuffer("XXXtest line 4", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 5", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 4", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("test line 5", b = b.op(NEXT_HISTORY));
        assertBuffer("XXXtest line 4", b = b.op(NEXT_HISTORY));
        assertBuffer("", b = b.op(NEXT_HISTORY));

        assertBuffer("XXXtest line 4", b = b.op(PREVIOUS_HISTORY));
        assertBuffer("XXXtest line 4", b = b.op(ACCEPT_LINE).op(PREVIOUS_HISTORY));
        assertBuffer("XXXtest line 4", b = b.op(ACCEPT_LINE).op(PREVIOUS_HISTORY));
        assertBuffer("XXXtest line 4", b = b.op(ACCEPT_LINE).op(PREVIOUS_HISTORY));
        assertBuffer("XXXtest line 4", b = b.op(ACCEPT_LINE).op(PREVIOUS_HISTORY));
    }

    @Test
    public void testHistorySearchBackwardAndForward() throws Exception {
        KeyMap map = console.getKeys();

        // Map in HISTORY_SEARCH_BACKWARD.
        map.bind("\033[0A", HISTORY_SEARCH_BACKWARD);
        map.bind("\033[0B", HISTORY_SEARCH_FORWARD);

        Buffer b = new Buffer().
            append("toes").op(ACCEPT_LINE).
            append("the quick brown").op(ACCEPT_LINE).
            append("fox jumps").op(ACCEPT_LINE).
            append("over the").op(ACCEPT_LINE).
            append("lazy dog").op(ACCEPT_LINE).
            append("");

        assertBuffer("", b);

        // Using history-search-backward behaves like previous-history when
        // no input has been provided.
        assertBuffer("lazy dog", b = b.append("\033[0A"));
        assertBuffer("over the", b = b.append("\033[0A"));
        assertBuffer("fox jumps", b = b.append("\033[0A"));

        // history-search-forward should behave line next-history when no
        // input has been provided.
        assertBuffer("over the", b = b.append("\033[0B"));
        assertBuffer("lazy dog", b = b.append("\033[0B"));
        assertBuffer("", b = b.append("\033[0B"));

        // Make sure we go back correctly.
        assertBuffer("lazy dog", b = b.append("\033[0A"));
        assertBuffer("over the", b = b.append("\033[0A"));
        assertBuffer("fox jumps", b = b.append("\033[0A"));

        // Search forward on 'l'.
        b = b.append("l");
        assertBuffer("lazy dog", b = b.append("\033[0B"));

        // Try moving forward again.  We should be at our original input line,
        // which is just a plain 'l' at this point.
        assertBuffer("l", b = b.append("\033[0B"));

        // Now we should have more context and history-search-backward should
        // take us to "the quick brown" line.
        b = b.op(BACKWARD_DELETE_CHAR).append("t");
        assertBuffer("the quick brown", b = b.append("\033[0A"));

        // Try moving backward again.
        assertBuffer("toes", b = b.append("\033[0A"));

        assertBuffer("the quick brown", b = b.append("\033[0B"));

        b = b.op(BACKWARD_DELETE_CHAR);
        assertBuffer("fox jumps", b = b.append("\033[0B"));

        b = b.append("to");
        assertBuffer("toes", b = b.append("\033[0A"));
    }
}
