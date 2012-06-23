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

package com.izforge.izpack.integration.packaging;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.packager.impl.Packager;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.unpacker.Unpacker;
import com.izforge.izpack.integration.AbstractInstallationTest;
import com.izforge.izpack.integration.HelperTestMethod;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;


/**
 * Tests the {@link Packager} in conjunction with the {@link Unpacker}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class PackagingTest extends AbstractInstallationTest
{
    /**
     * The installation jar.
     */
    private final JarFile installer;

    /**
     * The compilation data.
     */
    private final CompilerData compilerData;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installer controller.
     */
    private final InstallerController controller;

    /**
     * Frame fixture.
     */
    private FrameFixture frameFixture;

    /**
     * Constructs a <tt>PackagingTest</tt>.
     *
     * @param installer    the installation jar
     * @param compilerData the compilation data
     * @param installData  the installation data
     * @param frame        the installer frame
     * @param controller   the installer controller
     */
    public PackagingTest(JarFile installer, CompilerData compilerData, AutomatedInstallData installData,
                         InstallerFrame frame, InstallerController controller)
    {
        super(installData);
        this.installer = installer;
        this.compilerData = compilerData;
        this.frame = frame;
        this.controller = controller;
    }

    /**
     * Tears down the test case.
     */
    @After
    public void tearDown()
    {
        if (frameFixture != null)
        {
            frameFixture.cleanUp();
        }
    }

    /**
     * Tests pack200 compression of packaged jars.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/packaging/pack200.xml")
    public void testPack200() throws Exception
    {
        // make sure the source jar exists
        File source = new File(compilerData.getBasedir(), "izpack-test-listener.jar");
        assertTrue(source.exists());

        // verify that the izpack-test-listener.jar has been written out as a pack200 resource
        assertThat(installer, ZipMatcher.isZipContainingFiles("resources/packs/pack200-0"));

        // now run installation
        frameFixture = HelperTestMethod.prepareFrameFixture(frame, controller);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        frameFixture.requireVisible();

        HelperTestMethod.waitAndCheckInstallation(getInstallData());

        // verify the test jar has been installed, and contains the same entries as the source
        File target = new File(getInstallPath(), "izpack-test-listener.jar");
        assertTrue(target.exists());

        JarFile sourceJar = new JarFile(source);
        JarFile targetJar = new JarFile(target);

        Set<String> sourceEntries = getFiles(sourceJar);
        Set<String> targetEntries = getFiles(targetJar);
        assertEquals(sourceEntries, targetEntries);
    }

    /**
     * Returns the file names from a jar file, excluding directory entries which aren't returned in a pack200 jar.
     *
     * @param file the jar file
     * @return the file names
     * @throws IOException for any I/O error
     */
    private Set<String> getFiles(JarFile file) throws IOException
    {
        Set<String> result = new HashSet<String>();
        Enumeration<? extends ZipEntry> entries = file.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory())
            {
                result.add(entry.getName());
            }
        }
        return result;
    }

}
