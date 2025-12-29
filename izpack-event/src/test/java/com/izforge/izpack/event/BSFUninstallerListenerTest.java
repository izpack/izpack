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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.ProgressNotifiersImpl;
import com.izforge.izpack.util.Platforms;

/**
 * Tests the {@link BSFUninstallerListener} class.
 *
 * @author Tim Anderson
 */
public class BSFUninstallerListenerTest
{

    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The installation directory.
     */
    private File installDir;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp()
    {
        installDir = temporaryFolder.getRoot();
    }

    /**
     * Verifies that Groovy actions are invoked for each listener method.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testGroovy() throws IOException
    {
        List<BSFAction> actions = getActions("/com/izforge/izpack/event/bsf/BSFActionsSpec-groovy.xml", "Base");
        checkListener(actions, "-groovy.txt");
    }

    /**
     * Verifies that Beanshell actions are invoked for each listener method.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testBeanshell() throws IOException
    {
        List<BSFAction> actions = getActions("/com/izforge/izpack/event/bsf/BSFActionsSpec-bsh.xml", "Base");
        checkListener(actions, "-bsh.txt");
    }

    /**
     * Tests the {@link BSFUninstallerListener}.
     *
     * @param actions the uninstallation actions
     * @param suffix  the file name suffix
     * @throws IOException for any I/O error
     */
    public void checkListener(List<BSFAction> actions, String suffix) throws IOException
    {
        UninstallerListener listener = createListener(actions);
        listener.initialise();

        File file1 = new File(installDir, "file1.txt");
        File file2 = new File(installDir, "file2.txt");
        List<File> files = Arrays.asList(file1, file2);
        for (File file : files)
        {
            FileUtils.touch(file);
        }

        System.setProperty("TEST_INSTALL_PATH", installDir.getPath());
        // hack to pass additional parameters to script. TODO

        assertFileNotExists(installDir, "beforedeletion" + suffix);
        listener.beforeDelete(files, Mockito.mock(ProgressListener.class));
        assertFileExists(installDir, "beforedeletion" + suffix);

        assertFileNotExists(installDir, "beforedelete" + suffix);
        listener.beforeDelete(file1);
        assertFileExists(installDir, "beforedelete" + suffix);

        assertFileNotExists(installDir, "afterdelete" + suffix);
        listener.afterDelete(file1);
        assertFileExists(installDir, "afterdelete" + suffix);

        assertFileNotExists(installDir, "afterdeletion" + suffix);
        listener.afterDelete(files, Mockito.mock(ProgressListener.class));
        assertFileExists(installDir, "afterdeletion" + suffix);
    }

    /**
     * Creates a {@link BSFUninstallerListener} that uses the supplied actions.
     *
     * @param actions the actions
     * @return a new {@link BSFUninstallerListener}
     * @throws IOException for any I/O error
     */
    private BSFUninstallerListener createListener(List<BSFAction> actions) throws IOException
    {
        // stream the actions, so the BSFUninstallerListener can read them as a resource
        assertNotNull(actions);
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
        objectOutput.writeObject(actions);
        objectOutput.close();

        Resources resources = Mockito.mock(Resources.class);
        ByteArrayInputStream byteInput = new ByteArrayInputStream(byteOutput.toByteArray());
        Mockito.when(resources.getInputStream("bsfActions")).thenReturn(byteInput);
        return new BSFUninstallerListener(resources);
    }


    /**
     * Loads the specified BSFActionsSpec resource, returning the uninstallation actions for the specified pack.
     * <p/>
     * This uses {@link BSFInstallerListener} to read and produce the uninstallation actions.
     *
     * @param resource the BSFActionsSpec resource
     * @param packName the pack name
     * @return the uninstallation actions for the pack
     */
    @SuppressWarnings("unchecked")
    private List<BSFAction> getActions(String resource, String packName)
    {
        Properties properties = new Properties();
        Variables variables = new DefaultVariables(properties);
        VariableSubstitutor replacer = new VariableSubstitutorImpl(variables);

        InstallData installData = new AutomatedInstallData(variables, Platforms.SUNOS);
        installData.setInstallPath(installDir.getPath());

        Resources resources = Mockito.mock(Resources.class);
        InputStream specStream = getClass().getResourceAsStream(resource);
        assertNotNull(specStream);
        Mockito.when(resources.getInputStream(BSFInstallerListener.SPEC_FILE_NAME)).thenReturn(specStream);

        UninstallData uninstallData = new UninstallData();
        BSFInstallerListener listener = new BSFInstallerListener(installData, replacer, variables, resources,
                                                                 uninstallData, new ProgressNotifiersImpl());
        listener.initialise();
        Pack pack = new Pack(packName, null, null, null, null, true, true, false, null, true, 0);
        List<Pack> packs = Arrays.asList(pack);

        // Verify that when the beforePacks method is invoked, the corresponding BSF action is called.
        listener.beforePacks(packs);
        listener.afterPacks(packs, Mockito.mock(ProgressListener.class));
        return (List<BSFAction>) uninstallData.getAdditionalData().get("bsfActions");
    }

}
