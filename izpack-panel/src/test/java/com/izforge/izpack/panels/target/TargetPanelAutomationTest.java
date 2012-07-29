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
package com.izforge.izpack.panels.target;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;


/**
 * Tests the {@link TargetPanelAutomation} class.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class TargetPanelAutomationTest
{

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * Constructs a {@code TargetPanelAutomationTest}.
     *
     * @param installData the installation data
     */
    public TargetPanelAutomationTest(InstallData installData)
    {
        this.installData = installData;
    }

    /**
     * Verifies that a directory containing an unrecognised .installationinformation file may not be selected to
     * install to, from {@link TargetPanelAutomation#runAutomated}
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testIncompatibleInstallation() throws IOException
    {
        File root = temporaryFolder.getRoot();
        File badDir = new File(root, "badDir");
        assertTrue(badDir.mkdirs());
        File goodDir = new File(root, "goodDir");   // don't bother creating it

        // get the expected error message for the locale
        String expectedMessage = TargetPanelTestHelper.getIncompatibleInstallationMessage(installData);

        // try and select an incompatible install dir. Should fail with an InstallerException
        TargetPanelAutomation panel = new TargetPanelAutomation();
        IXMLElement badPath = createElement(badDir);
        try
        {
            TargetPanelTestHelper.createBadInstallationInfo(badDir);
            panel.runAutomated(installData, badPath);
            fail("Expected runAutomated() to fail");
        }
        catch (InstallerException expected)
        {
            assertEquals(expectedMessage, expected.getMessage());
        }

        // now run again, with a dir containing no incompatible install info, and verify it succeeds
        IXMLElement goodPath = createElement(goodDir);
        panel.runAutomated(installData, goodPath);
        assertEquals(goodDir.getAbsolutePath(), installData.getInstallPath());
    }

    /**
     * Helper to create an element containing an "installpath" element.
     *
     * @param dir the directory to select as the target
     * @return a new element
     */
    private IXMLElement createElement(File dir)
    {
        IXMLElement element = new XMLElementImpl("foo");
        XMLElementImpl path = new XMLElementImpl("installpath", element);
        path.setContent(dir.getAbsolutePath());
        element.addChild(path);
        return element;
    }
}
