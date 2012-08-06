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


import static com.izforge.izpack.test.util.TestHelper.assertFileExists;
import static com.izforge.izpack.test.util.TestHelper.assertFileNotExists;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.ProgressNotifiersImpl;
import com.izforge.izpack.util.Platforms;

/**
 * Tests the {@link BSFInstallerListener} class.
 *
 * @author Tim Anderson
 */
public class BSFInstallerListenerTest
{

    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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
     * @throws java.io.IOException for any I/O error
     */
    @Before
    public void setUp() throws IOException
    {
        Properties properties = new Properties();
        Variables variables = new DefaultVariables(properties);
        replacer = new VariableSubstitutorImpl(variables);

        installData = new AutomatedInstallData(variables, Platforms.MANDRIVA_LINUX);

        installDir = temporaryFolder.getRoot();
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
        List<Pack> packs = Arrays.asList(pack);

        ProgressListener progressListener = Mockito.mock(ProgressListener.class);

        UninstallData uninstallData = new UninstallData();
        ProgressNotifiers notifiers = new ProgressNotifiersImpl();
        BSFInstallerListener listener = new BSFInstallerListener(installData, replacer, resources, uninstallData,
                                                                 notifiers);
        listener.initialise();

        // Verify that when the beforePacks method is invoked, the corresponding BSF action is called.
        assertFileNotExists(installDir, "beforepacks" + suffix);
        listener.beforePacks(packs);
        assertFileExists(installDir, "beforepacks" + suffix);

        // Verify that when the beforePack method is invoked, the corresponding BSF action is called.
        assertFileNotExists(installDir, "beforepack" + suffix);
        listener.beforePack(pack, 0);
        assertFileExists(installDir, "beforepack" + suffix);

        // Verify that when the beforeDir method is invoked, the corresponding BSF action is called.
        File dir = new File(installDir, "dir");
        assertTrue(dir.mkdir());
        assertFileNotExists(installDir, "beforedir" + suffix);
        PackFile dirPackFile = new PackFile(installDir, dir, dir.getName(), null, OverrideType.OVERRIDE_TRUE, null,
                                            Blockable.BLOCKABLE_NONE);
        listener.beforeDir(dir, dirPackFile, pack);
        assertFileExists(installDir, "beforedir" + suffix);

        // Verify that when the afterDir method is invoked, the corresponding BSF action is called.
        assertFileNotExists(installDir, "afterdir" + suffix);
        listener.afterDir(dir, dirPackFile, pack);
        assertFileExists(installDir, "afterdir" + suffix);

        // Verify that when the beforeFile method is invoked, the corresponding BSF action is called.
        File file = new File(installDir, "file.txt");
        FileUtils.touch(file);
        assertFileNotExists(installDir, "beforefile" + suffix);
        PackFile packFile = new PackFile(installDir, file, file.getName(), null, OverrideType.OVERRIDE_TRUE, null,
                                         Blockable.BLOCKABLE_NONE);
        listener.beforeFile(file, packFile, pack);
        assertFileExists(installDir, "beforefile" + suffix);

        // Verify that when the afterFile method is invoked, the corresponding BSF action is called.
        assertFileNotExists(installDir, "afterfile" + suffix);
        listener.afterFile(file, packFile, pack);
        assertFileExists(installDir, "afterfile" + suffix);

        // Verify that when the afterPack method is invoked, the corresponding BSF action is called.
        assertFileNotExists(installDir, "afterpack" + suffix);
        listener.afterPack(pack, 0);
        assertFileExists(installDir, "afterpack" + suffix);

        // Verify that when the afterPacks method is invoked, the corresponding BSF action is called.
        // This touches a file "afterpacks.txt"
        assertFileNotExists(installDir, "afterpacks" + suffix);
        listener.afterPacks(packs, progressListener);
        assertFileExists(installDir, "afterpacks" + suffix);
    }

}