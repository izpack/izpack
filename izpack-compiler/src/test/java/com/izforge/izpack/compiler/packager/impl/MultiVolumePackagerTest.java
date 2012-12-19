/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.compiler.packager.impl;

import java.util.Properties;

import org.mockito.Mockito;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;

/**
 * Tests the {@link MultiVolumePackager}.
 *
 * @author Tim Anderson
 */
public class MultiVolumePackagerTest extends AbstractPackagerTest
{

    /**
     * Helper to create a packager that writes to the provided jar.
     *
     * @param jar          the jar stream
     * @param mergeManager the merge manager
     * @return a new packager
     */
    @Override
    protected PackagerBase createPackager(JarOutputStream jar, MergeManager mergeManager)
    {
        Properties properties = new Properties();
        PackagerListener listener = null;
        PackCompressor compressor = Mockito.mock(PackCompressor.class);
        CompilerPathResolver pathResolver = Mockito.mock(CompilerPathResolver.class);
        MergeableResolver resolver = Mockito.mock(MergeableResolver.class);
        String baseDir = getBaseDir().getPath();
        CompilerData data = new CompilerData(
                "",
                baseDir,
                baseDir + "/target/test.jar",
                true);
        MultiVolumePackager packager = new MultiVolumePackager(properties, listener, jar, mergeManager,
                                                               pathResolver, resolver, compressor, data);
        packager.setInfo(new Info());
        return packager;
    }
}
