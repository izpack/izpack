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

package jline.console.completer;

import static org.junit.Assert.assertArrayEquals;
import jline.console.ConsoleReaderTestSupport;
import jline.console.completer.ArgumentCompleter.ArgumentList;
import jline.console.completer.ArgumentCompleter.WhitespaceArgumentDelimiter;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link WhitespaceArgumentDelimiter}.
 *
 * @author <a href="mailto:mdrob@apache.org">Mike Drob</a>
 */
public class WhitespaceDelimiterTest extends ConsoleReaderTestSupport {

  ArgumentList delimited;
  WhitespaceArgumentDelimiter delimiter;

  @Override
@Before
  public void setUp() {
    delimiter = new WhitespaceArgumentDelimiter();
  }

  @Test
  public void testDelimit() {
    // These all passed before adding quoting and escaping
    delimited = delimiter.delimit("1 2 3", 0);
    assertArrayEquals(new String[] {"1", "2", "3"}, delimited.getArguments());

    delimited = delimiter.delimit("1  2  3", 0);
    assertArrayEquals(new String[] {"1", "2", "3"}, delimited.getArguments());
  }

  @Test
  public void testQuotedDelimit() {
    delimited = delimiter.delimit("\"1 2\" 3", 0);
    assertArrayEquals(new String[] {"1 2", "3"}, delimited.getArguments());

    delimited = delimiter.delimit("'1 2' 3", 0);
    assertArrayEquals(new String[] {"1 2", "3"}, delimited.getArguments());

    delimited = delimiter.delimit("1 '2 3'", 0);
    assertArrayEquals(new String[] {"1", "2 3"}, delimited.getArguments());
  }

  @Test
  public void testMixedQuotes() {
    delimited = delimiter.delimit("\"1' '2\" 3", 0);
    assertArrayEquals(new String[] {"1' '2", "3"}, delimited.getArguments());

    delimited = delimiter.delimit("'1\" 2' 3\"", 0);
    assertArrayEquals(new String[] {"1\" 2", "3"}, delimited.getArguments());
  }

  @Test
  public void testEscapedSpace() {
    delimited = delimiter.delimit("1\\ 2 3", 0);
    assertArrayEquals(new String[] {"1 2", "3"}, delimited.getArguments());
  }

  @Test
  public void testEscapedQuotes() {
    delimited = delimiter.delimit("'1 \\'2' 3", 0);
    assertArrayEquals(new String[] {"1 '2", "3"}, delimited.getArguments());

    delimited = delimiter.delimit("\\'1 '2' 3", 0);
    assertArrayEquals(new String[] {"'1", "2", "3"}, delimited.getArguments());

    delimited = delimiter.delimit("'1 '2\\' 3", 0);
    assertArrayEquals(new String[] {"1 ", "2'", "3"}, delimited.getArguments());
  }
}
