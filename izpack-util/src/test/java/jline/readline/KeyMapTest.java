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

package jline.readline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jline.console.KeyMap;
import jline.console.Operation;

import org.junit.Test;


public class KeyMapTest {

    @Test
    public void testBound() throws Exception {

        KeyMap map = KeyMap.emacs();

        assertEquals( Operation.COMPLETE, map.getBound("\u001B" + KeyMap.CTRL_OB) );
        assertEquals( Operation.BACKWARD_WORD, map.getBound(KeyMap.ESCAPE + "b") );

        map.bindIfNotBound("\033[0A", Operation.PREVIOUS_HISTORY);
        assertEquals( Operation.PREVIOUS_HISTORY, map.getBound("\033[0A") );


        map.bind( "\033[0AB", Operation.NEXT_HISTORY );
        assertTrue( map.getBound("\033[0A") instanceof KeyMap );
        assertEquals( Operation.NEXT_HISTORY , map.getBound("\033[0AB") );
    }

}
