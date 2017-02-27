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
package com.izforge.izpack.integration.automation;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.compiler.container.TestAutomatedInstallationContainer;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.integration.AbstractInstallationTest;
import com.izforge.izpack.integration.UninstallHelper;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.net.URL;

import static com.izforge.izpack.test.util.TestHelper.assertFileExists;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link AutomatedInstaller}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestAutomatedInstallationContainer.class)
public class AutomatedInstallerTest extends AbstractInstallationTest
{

    /**
     * The installer.
     */
    private final AutomatedInstaller installer;

    /**
     * Constructs an {@code AutomatedInstaller}.
     *
     * @param installer the installer
     * @param installData the installation data
     */
    public AutomatedInstallerTest(AutomatedInstaller installer, AutomatedInstallData installData)
    {
        super(installData);
        this.installer = installer;
    }

    /**
     * Tests installation and uninstallation.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/basicInstall/basicInstall.xml")
    public void testAutomatedInstaller() throws Exception
    {
        InstallData installData = getInstallData();

        URL url = getClass().getResource("/samples/basicInstall/auto.xml");
        assertNotNull(url);
        String recordfile = FileUtil.convertUrlToFilePath(url);
        String installPath = new File(temporaryFolder.getRoot(), "basicapp").getAbsolutePath();
        replaceInstallPathInAutoInstall(recordfile, installPath);

        installer.init(recordfile, null, new String[0]);
        installer.doInstall();

        File dir = new File(installPath);
        assertFileExists(dir);
        assertFileExists(dir, "Licence.txt");
        assertFileExists(dir, "Readme.txt");
        assertFileExists(dir, "Uninstaller/uninstaller.jar");

        // perform uninstallation
        UninstallHelper.uninstall(installData);

        // verify the install directory no longer exists
        assertFalse(new File(installPath).exists());
    }

    private static void replaceInstallPathInAutoInstall(String recordfile, String installpath) throws Exception
    {
        // Read xml and build a DOM document
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(recordfile));

        XPath xpath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xpath.evaluate("//installpath", doc, XPathConstants.NODE);
        node.setTextContent(installpath);

        // Write the DOM document to the file
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(new DOMSource(doc), new StreamResult(new File(recordfile)));
    }
}
