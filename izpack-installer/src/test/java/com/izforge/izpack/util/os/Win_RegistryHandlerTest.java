/*
 * IzPack - Copyright 2021 Hitesh A. Bosamiya, All Rights Reserved.
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
package com.izforge.izpack.util.os;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link Win_RegistryHandler} class.
 *
 * @author Hitesh A. Bosamiya
 */
public class Win_RegistryHandlerTest
{
    private Win_RegistryHandler win_registryHandler;

    @Before
    public void setup()
    {
        win_registryHandler = new Win_RegistryHandler(null);
    }

    @Test
    public void testDuplicateEntriesInPath()
    {
        String key = "SYSTEM\\CurrentControlSet\\Control Session Manager\\Environment";
        String value = "Path";
        String contents = "C:\\MyApplication\\bin;C:\\MyApplication\\bin;C:\\YourApplication\\bin";
        String retContents = win_registryHandler.checkedPathContents(key, value, contents);
        String expected = "C:\\MyApplication\\bin;C:\\YourApplication\\bin";
        assertEquals(expected, retContents);
    }

    @Test
    public void testDuplicateEntriesInPathWithDifferentCase()
    {
        String key = "SYSTEM\\CurrentControlSet\\Control Session Manager\\ENVIRONMENT";
        String value = "Path";
        String contents = "C:\\MyApplication\\bin;C:\\MYAPPLICATION\\BIN;C:\\YourApplication\\bin";
        String retContents = win_registryHandler.checkedPathContents(key, value, contents);
        String expected = "C:\\MyApplication\\bin;C:\\YourApplication\\bin";
        assertEquals(expected, retContents);
    }

    @Test
    public void testNonPathValue()
    {
        String key = "SYSTEM\\CurrentControlSet\\Control Session Manager\\Environment";
        String value = "NonPath";
        String contents = "C:\\MyApplication\\bin;C:\\MyApplication\\bin;C:\\YourApplication\\bin";
        String retContents = win_registryHandler.checkedPathContents(key, value, contents);
        String expected = contents;
        assertEquals(expected, retContents);
    }
}
