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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;
import java.util.jar.JarOutputStream;

import static org.mockito.Mockito.mock;

/**
 * Tests the {@link Packager}.
 */
public class PackagerTest extends AbstractPackagerTest
{
    @TempDir
    Path tempDir;

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
        CompilerPathResolver pathResolver = mock(CompilerPathResolver.class);
        MergeableResolver resolver = mock(MergeableResolver.class);
        CompilerData data = new CompilerData("", "", "", true);
        RulesEngine rulesEngine = mock(RulesEngine.class);
        Packager packager = new Packager(properties, null, jar, mergeManager,
                                         pathResolver, resolver, data, rulesEngine);
        packager.setInfo(new Info());
        return packager;
    }

    /*
     * Measures how long (in ms) it takes the packager to create an installer and
     * prints the result to standard output.
     */
    @Test
    public void measureWriteSpeed() throws Exception {
        File installerJar = new File(tempDir.toFile(), "installer.jar");

        File file1 = TestHelper.createFile(tempDir.toFile(), "f1.dat", 1024*1024*10);
        File file2 = TestHelper.createFile(tempDir.toFile(), "f2.dat", 1024*1024*10);

        PackInfo packInfo = createPackInfo("Core", file1, file2);

        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(installerJar));
        IPackager packager = createPackager(jarOutputStream, mock(MergeManager.class));
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
                    "", null);
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
