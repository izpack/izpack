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

import static com.izforge.izpack.util.StringTool.normalizePath;
import static com.izforge.izpack.util.StringTool.replace;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


public class StringToolTest
{

    /*
     * Class under test for String replace(String, String, String[, boolean])
     */
    @Test
    public void testReplace()
    {
        String ref = "ABC-012-def";

        assertThat(replace(null, null, null)).isEqualTo(null);
        assertThat(replace(ref, null, null)).isEqualTo("ABC-012-def");
        assertThat(replace(ref, "something", null)).isEqualTo("ABC-012-def");
        assertThat(replace(ref, "-", null)).isEqualTo("ABC012def");
        assertThat(replace(ref, "ABC", "abc")).isEqualTo("abc-012-def");
        assertThat(replace(ref, "abc", "abc", false)).isEqualTo("ABC-012-def");
        assertThat(replace(ref, "abc", "abc", true)).isEqualTo("ABC-012-def");
    }

    /*
     * Class under test for String normalizePath(String[, String])
     */
    @Test
    public void testNormalizePath()
    {
        assertThat(normalizePath(
                "C:\\Foo/Bar/is\\so\\boring:plop;plop", "\\")).isEqualTo("C:\\Foo\\Bar\\is\\so\\boring;plop;plop");
        assertThat(normalizePath(
                "/some/where\\that:matters;really", "/")).isEqualTo("/some/where/that:matters:really");
    }

}
