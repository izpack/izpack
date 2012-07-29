/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.panels.userinput;

import com.izforge.izpack.util.StringTool;

import junit.framework.Assert;
import junit.framework.TestCase;

public class StringToolTest extends TestCase
{

    /*
     * Class under test for String replace(String, String, String[, boolean])
     */
    public void testReplace()
    {
        String ref = "ABC-012-def";

        Assert.assertEquals(null, StringTool.replace(null, null, null));
        assertEquals("ABC-012-def", StringTool.replace(ref, null, null));
        assertEquals("ABC-012-def", StringTool.replace(ref, "something", null));
        assertEquals("ABC012def", StringTool.replace(ref, "-", null));
        assertEquals("abc-012-def", StringTool.replace(ref, "ABC", "abc"));
        assertEquals("ABC-012-def", StringTool.replace(ref, "abc", "abc", false));
        assertEquals("ABC-012-def", StringTool.replace(ref, "abc", "abc", true));
    }

    /*
     * Class under test for String normalizePath(String[, String])
     */
    public void testNormalizePath()
    {
        assertEquals("C:\\Foo\\Bar\\is\\so\\boring;plop;plop", StringTool.normalizePath(
                "C:\\Foo/Bar/is\\so\\boring:plop;plop", "\\"));
        assertEquals("/some/where/that:matters:really", StringTool.normalizePath(
                "/some/where\\that:matters;really", "/"));
    }

}
