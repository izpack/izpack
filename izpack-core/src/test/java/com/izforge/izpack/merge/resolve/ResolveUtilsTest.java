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

import static com.izforge.izpack.merge.resolve.ResolveUtils.convertPathToPosixPath;
import static com.izforge.izpack.merge.resolve.ResolveUtils.isFileInJar;
import static java.lang.ClassLoader.getSystemResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.junit.jupiter.api.Test;

/**
 * Test for resolveUtils
 */
public class ResolveUtilsTest {

    @Test
    public void testConvertPathToPosixPath() throws Exception
    {
        assertThat(convertPathToPosixPath("C:\\Users\\gaou\\.m2")).isEqualTo("C:/Users/gaou/.m2");
    }

    @Test
    public void testIsFileInJar() throws Exception
    {
        URL container = getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URL resource = new URL(container.toString() + "!/jar/izforge/izpack/panels/hello/HelloPanel.class");
        assertThat(isFileInJar(resource)).isTrue();

        resource = new URL(container.toString() + "!/jar/izforge/izpack/panels/hello/");
        assertThat(isFileInJar(resource)).isFalse();
    }
}
