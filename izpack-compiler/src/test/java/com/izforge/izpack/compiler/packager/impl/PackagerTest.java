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

package com.izforge.izpack.compiler.packager.impl;

import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.PackInfo;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.test.util.TestHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.jar.JarOutputStream;

import static org.mockito.Mockito.mock;

/**
 * Tests the {@link Packager}.
 */
public class PackagerTest extends AbstractPackagerTest
{

    /**
     * Helper to create a packager that writes to the provided jar.
     *
     * @param installerJar the installer jar destination
     * @param mergeManager the merge manager
     * @return a new packager
     */
    @Override
    protected PackagerBase createPackager(File installerJar, MergeManager mergeManager)
    {
        Properties properties = new Properties();
        CompilerPathResolver pathResolver = mock(CompilerPathResolver.class);
        MergeableResolver resolver = mock(MergeableResolver.class);
        CompilerData data = new CompilerData("", "", installerJar.toString(), true);
        RulesEngine rulesEngine = mock(RulesEngine.class);
        Packager packager = new Packager(properties, null, mergeManager, pathResolver, resolver, data,
                rulesEngine);
        packager.setInfo(new Info());
        return packager;
    }

    /*
     * Measures how long (in ms) it takes the packager to create an installer and
     * prints the result to standard output.
     */
    @Test
    public void measureWriteSpeed() throws Exception {

        File installerJar = temporaryFolder.newFile("installer.jar");

        File file1 = TestHelper.createFile(temporaryFolder.getRoot(), "f1.dat", 1024*1024*10);
        File file2 = TestHelper.createFile(temporaryFolder.getRoot(), "f2.dat", 1024*1024*10);

        PackInfo packInfo = createPackInfo("Core", file1, file2);

        IPackager packager = createPackager(installerJar, mock(MergeManager.class));
        packager.addPack(packInfo);

        long startMillis = System.currentTimeMillis();

        packager.createInstaller();

        long timeDiff = System.currentTimeMillis() - startMillis;
        long packSize = packInfo.getPack().getSize();

        System.out.println("Writing pack of " + packSize + " KiB took " + timeDiff + "ms");
    }

    private PackInfo createPackInfo(String name, File... files) throws IOException {

        PackInfo packInfo = new PackInfo(name, null, "", true, false, null, true, calculateTotalSize(files));
        for (File file : files)
        {
            packInfo.addFile(file.getParentFile(), file, "$INSTALL_DIR/" + file.getName(), null,
                    OverrideType.OVERRIDE_TRUE, "", Blockable.BLOCKABLE_NONE, Collections.emptyMap(),
                    "");
        }
        return packInfo;
    }

    private long calculateTotalSize(File... files)
    {
        long totalSize = 0;
        for (File file : files)
        {
            totalSize += file.length();
        }
        return totalSize;
    }
}
