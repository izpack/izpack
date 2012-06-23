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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.ProgressNotifiersImpl;
import com.izforge.izpack.util.IoHelper;

/**
 * Tests the {@link AntActionInstallerListener} class.
 *
 * @author Tim Anderson
 */
public class AntActionInstallerListenerTest
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
     * The resources.
     */
    private Resources resources;


    /**
     * Sets up the test case.
     *
     * @throws IOException for any I/O error
     */
    @Before
    public void setUp() throws IOException
    {
        Properties properties = new Properties();
        Variables variables = new DefaultVariables(properties);
        replacer = new VariableSubstitutorImpl(variables);

        installData = new AutomatedInstallData(variables);

        installDir = temporaryFolder.getRoot();
        installData.setInstallPath(installDir.getPath());

        // copy build.xml to the install dir, so that the listener can find it
        IoHelper.copyStream(getClass().getResourceAsStream("/com/izforge/izpack/event/ant/build.xml"),
                            new FileOutputStream(new File(installDir, "build.xml")));

        resources = Mockito.mock(Resources.class);
        InputStream specStream = getClass().getResourceAsStream("/com/izforge/izpack/event/ant/AntActionsSpec.xml");
        assertNotNull(specStream);
        Mockito.when(resources.getInputStream(AntActionInstallerListener.SPEC_FILE_NAME)).thenReturn(specStream);
    }

    /**
     * Verifies that Ant targets are invoked for each listener method.
     */
    @Test
    public void testAntTargets()
    {
        Pack pack = new Pack("Base", null, null, null, null, true, true, false, null, true);
        List<Pack> packs = Arrays.asList(pack);

        ProgressListener progressListener = Mockito.mock(ProgressListener.class);

        UninstallData uninstallData = new UninstallData();
        ProgressNotifiers notifiers = new ProgressNotifiersImpl();
        AntActionInstallerListener listener = new AntActionInstallerListener(replacer, resources, installData,
                                                                             uninstallData, notifiers);
        listener.initialise();

        // Verify that when the beforePacks method is invoked, the corresponding Ant target is called.
        // This touches a file "beforepacks.txt"
        assertFileNotExists(installDir, "beforepacks.txt");
        listener.beforePacks(packs);
        assertFileExists(installDir, "beforepacks.txt");

        // Verify that when the beforePack method is invoked, the corresponding Ant target is called.
        // This touches a file "beforepack.txt"
        assertFileNotExists(installDir, "beforepack.txt");
        listener.beforePack(pack, 0);
        assertFileExists(installDir, "beforepack.txt");

        // Verify that when the afterPack method is invoked, the corresponding Ant target is called.
        // This touches a file "afterpack.txt"
        assertFileNotExists(installDir, "afterpack.txt");
        listener.afterPack(pack, 0);
        assertFileExists(installDir, "afterpack.txt");

        // Verify that when the afterPacks method is invoked, the corresponding Ant target is called.
        // This touches a file "afterpacks.txt"
        assertFileNotExists(installDir, "afterpacks.txt");
        listener.afterPacks(packs, progressListener);
        assertFileExists(installDir, "afterpacks.txt");
    }

}