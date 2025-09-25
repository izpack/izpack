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

package com.izforge.izpack.event;

import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.ProgressNotifiersImpl;
import com.izforge.izpack.test.junit.PicoExtension;
import com.izforge.izpack.util.Platforms;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.izforge.izpack.test.util.TestHelper.assertFileExists;
import static com.izforge.izpack.test.util.TestHelper.assertFileNotExists;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link BSFInstallerListener} class.
 *
 * @author Tim Anderson
 */
@ExtendWith(PicoExtension.class)
public class BSFInstallerListenerTest
{
    /**
     * Temporary folder to perform installations to.
     */
    @TempDir
    public File temporaryFolder;

    /**
     * The variable replacer.
     */
    private VariableSubstitutor replacer;

    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * The installation directory.
     */
    private File installDir;

    /**
     * Sets up the test case.
     *
     * @throws IOException for any I/O error
     */
    @BeforeEach
    public void setUp() throws IOException
    {
        Properties properties = new Properties();
        Variables variables = new DefaultVariables(properties);
        replacer = new VariableSubstitutorImpl(variables);

        installData = new AutomatedInstallData(variables, Platforms.MANDRIVA_LINUX);

        installDir = temporaryFolder;
        installData.setInstallPath(installDir.getPath());
    }

    /**
     * Verifies that Groovy actions are invoked for each listener method.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testGroovyActions() throws IOException
    {
        Resources resources = Mockito.mock(Resources.class);
        InputStream specStream = getClass().getResourceAsStream(
            "/com/izforge/izpack/event/bsf/BSFActionsSpec-groovy.xml");
        assertNotNull(specStream);
        Mockito.when(resources.getInputStream(BSFInstallerListener.SPEC_FILE_NAME)).thenReturn(specStream);

        checkListener(resources, "-groovy.txt");
    }

    /**
     * Verifies that Beanshell actions are invoked for each listener method.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testBeanshellActions() throws IOException
    {
        Resources resources = Mockito.mock(Resources.class);
        InputStream specStream = getClass().getResourceAsStream("/com/izforge/izpack/event/bsf/BSFActionsSpec-bsh.xml");
        assertNotNull(specStream);
        Mockito.when(resources.getInputStream(BSFInstallerListener.SPEC_FILE_NAME)).thenReturn(specStream);

        checkListener(resources, "-bsh.txt");
    }

    /**
     * Tests the {@link BSFInstallerListener}.
     *
     * @param resources the resources
     * @param suffix    the file name suffix
     * @throws IOException for any I/O error
     */
    private void checkListener(Resources resources, String suffix) throws IOException
    {
        Pack pack = new Pack("Base", null, null, null, null, true, true, false, null, true, 0);
        List<Pack> packs = Collections.singletonList(pack);

        ProgressListener progressListener = Mockito.mock(ProgressListener.class);

        UninstallData uninstallData = new UninstallData();
        ProgressNotifiers notifiers = new ProgressNotifiersImpl();
        BSFInstallerListener listener = new BSFInstallerListener(installData, replacer, installData.getVariables(), resources, uninstallData, notifiers);
        listener.initialise();

        assertFileNotExists(installDir, "beforepacks" + suffix);
        listener.beforePacks(packs);
        assertFileExists(installDir, "beforepacks" + suffix);

        assertFileNotExists(installDir, "beforepack" + suffix);
        listener.beforePack(pack);
        assertFileExists(installDir, "beforepack" + suffix);

        File dir = new File(installDir, "dir");
        assertTrue(dir.mkdir());
        assertFileNotExists(installDir, "beforedir" + suffix);
        PackFile dirPackFile = new PackFile(installDir, dir, dir.getName(), null, OverrideType.OVERRIDE_TRUE, null,
                                            Blockable.BLOCKABLE_NONE, null);
        listener.beforeDir(dir, dirPackFile, pack);
        assertFileExists(installDir, "beforedir" + suffix);

        assertFileNotExists(installDir, "afterdir" + suffix);
        listener.afterDir(dir, dirPackFile, pack);
        assertFileExists(installDir, "afterdir" + suffix);

        File file = new File(installDir, "file.txt");
        FileUtils.touch(file);
        assertFileNotExists(installDir, "beforefile" + suffix);
        PackFile packFile = new PackFile(installDir, file, file.getName(), null, OverrideType.OVERRIDE_TRUE, null,
                                         Blockable.BLOCKABLE_NONE, null);
        listener.beforeFile(file, packFile, pack);
        assertFileExists(installDir, "beforefile" + suffix);

        assertFileNotExists(installDir, "afterfile" + suffix);
        listener.afterFile(file, packFile, pack);
        assertFileExists(installDir, "afterfile" + suffix);

        assertFileNotExists(installDir, "afterpack" + suffix);
        listener.afterPack(pack);
        assertFileExists(installDir, "afterpack" + suffix);

        assertFileNotExists(installDir, "afterpacks" + suffix);
        listener.afterPacks(packs, progressListener);
        assertFileExists(installDir, "afterpacks" + suffix);
    }
}
