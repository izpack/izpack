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

package com.izforge.izpack.compiler.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;


/**
 * Compiler helper test
 */
public class CompilerHelperTest
{
    private CompilerHelper helper = new CompilerHelper();

    @Test
    public void testResolveJarPath() throws Exception
    {
        assertThat(helper.resolveCustomActionsJarPath("SummaryLogger"), is("bin/customActions/izpack-summary-logger.jar"));
    }

    @Test
    public void testConvertCamelToHyphen() throws Exception
    {
        assertThat(helper.convertNameToDashSeparated("abCdeF").toString(), is("-ab-cde-f"));
        assertThat(helper.convertNameToDashSeparated("aClassName").toString(), is("-a-class-name"));
    }
}
