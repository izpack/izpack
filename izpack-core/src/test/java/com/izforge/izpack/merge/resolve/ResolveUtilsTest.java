/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.merge.resolve;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.net.URL;

import org.junit.Test;

/**
 * Test for resolveUtils
 */
public class ResolveUtilsTest {

    @Test
    public void testConvertPathToPosixPath() throws Exception 
    {
        assertThat(ResolveUtils.convertPathToPosixPath("C:\\Users\\gaou\\.m2") , is("C:/Users/gaou/.m2"));
    }
    
    @Test
    public void testIsFileInJar() throws Exception
    {
        URL container = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URL resource = new URL(container.toString() + "!/jar/izforge/izpack/panels/hello/HelloPanel.class");
        assertThat(ResolveUtils.isFileInJar(resource), is(true));
        
        resource = new URL(container.toString() + "!/jar/izforge/izpack/panels/hello/");
        assertThat(ResolveUtils.isFileInJar(resource), is(false));
    }
}
