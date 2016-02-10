/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Ari Voutilainen
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

package com.izforge.izpack.installer;


import com.izforge.izpack.util.VariableSubstitutor;
import junit.framework.TestCase;


/**
 * This will test some characters as UTF-8.
 *
 * @author Ari Voutilainen, ari.voutilainen (at) iki.fi
 * @note This test will create VariableSubstitutor object with null parameter!
 * There won't be Properties object available!
 */
public class SubstitutorTest extends TestCase
{
    private final String umlautString = "ÄöäÖüÜ";
    private final String cyrillicString = "Закончить";
    private final String japanesString = "ƒCƒ“ƒXƒg[ƒ‹‚Ì’†Ž~";

    protected VariableSubstitutor subst = new VariableSubstitutor(null);

    /**
     * Constructor for the test.
     *
     * @param arg0 Parameter string for the constructor.
     */
    public SubstitutorTest(String arg0)
    {
        super(arg0);
    }

    /**
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }


    /**
     * Tests return value of substitute for string containing umlaut characters.
     */
    public void testUmlautString()
    {
        String returnStr = subst.substitute(umlautString, null);
        assertEquals(umlautString, returnStr);
    }

    /**
     * Tests return value of substitute for Cyrillic string.
     */
    public void testCyrillicString()
    {
        String returnStr = subst.substitute(cyrillicString, null);
        assertEquals(cyrillicString, returnStr);
    }

    /**
     * Tests return value of substitute for Japanese string.
     */
    public void testJapaneseString()
    {
        String returnStr = subst.substitute(japanesString, null);
        assertEquals(japanesString, returnStr);
    }
}
