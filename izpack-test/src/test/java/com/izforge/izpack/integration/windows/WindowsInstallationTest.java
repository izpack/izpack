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

package com.izforge.izpack.integration.windows;

import static com.izforge.izpack.integration.windows.WindowsHelper.checkShortcut;
import static com.izforge.izpack.integration.windows.WindowsHelper.registryDeleteUninstallKey;
import static com.izforge.izpack.integration.windows.WindowsHelper.registryKeyExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.timing.Timeout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.event.RegistryInstallerListener;
import com.izforge.izpack.event.RegistryUninstallerListener;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.integration.AbstractDestroyerTest;
import com.izforge.izpack.integration.HelperTestMethod;
import com.izforge.izpack.integration.UninstallHelper;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;
import com.izforge.izpack.util.os.ShellLink;


/**
 * Test installation on Windows.
 * <p/>
 * Verifies that:
 * <ul>
 * <li>An <em>Uninstall</em> entry is added to the registry by {@link RegistryInstallerListener} during
 * installation</li>
 * <li>A shortcut is created for the uninstaller during installation</li>
 * <li>The <em>Uninstall</em> entry is removed at uninstallation by {@link RegistryUninstallerListener}</li>
 * <li>The shortcut is removed during uninstallation</li>
 * </ul>
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@RunOn(Platform.Name.WINDOWS)
@Container(TestInstallationContainer.class)
public class WindowsInstallationTest extends AbstractDestroyerTest
{

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installer controller.
     */
    private final InstallerController controller;

    /**
     * The librarian.
     */
    private final Librarian librarian;

    /**
     * The registry handler.
     */
    private final RegistryDefaultHandler handler;

    /**
     * The house keeper.
     */
    private final TestHousekeeper housekeeper;

    /**
     * The installer frame fixture.
     */
    private FrameFixture installerFrameFixture;

    /**
     * Registry uninstallation key. Hard-coded so we don't delete too much on cleanup if something unexpected happens.
     */
    private static final String UNINSTALL_KEY = RegistryHandler.UNINSTALL_ROOT + "IzPack Windows Installation Test 1.0";


    /**
     * Constructs a <tt>WindowsRegistryTest</tt>.
     *
     * @param frame       the installer frame
     * @param controller  the installer controller
     * @param installData the installation data
     * @param librarian   the librarian
     * @param handler     the registry handler
     * @param housekeeper the house-keeper
     */
    public WindowsInstallationTest(InstallerFrame frame, InstallerController controller,
                                   AutomatedInstallData installData, Librarian librarian,
                                   RegistryDefaultHandler handler, TestHousekeeper housekeeper)
    {
        super(installData);
        this.frame = frame;
        this.controller = controller;
        this.librarian = librarian;
        this.handler = handler;
        this.housekeeper = housekeeper;
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        destroyRegistryEntries();
    }

    /**
     * Cleans up after the test.
     *
     * @throws Exception for any error
     */
    @After
    public void tearDown() throws Exception
    {
        destroyRegistryEntries();
        if (installerFrameFixture != null)
        {
            installerFrameFixture.cleanUp();
            installerFrameFixture = null;
        }
    }

    /**
     * Tests installation on windows.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/windows/install.xml")
    public void testInstallation() throws Exception
    {
        assertFalse("This test must be run as administrator, or with Windows UAC turned off",
                    new PrivilegedRunner(Platforms.WINDOWS).isElevationNeeded());

        // UNINSTALL_NAME should be null prior to display of CheckedHelloPanel
        assertNull(getInstallData().getVariable("UNINSTALL_NAME"));

        installerFrameFixture = HelperTestMethod.prepareFrameFixture(frame, controller);

        // CheckedHelloPanel
        Thread.sleep(2000);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // UNINSTALL_NAME should now be defined
        assertEquals("IzPack Windows Installation Test 1.0", getInstallData().getVariable("UNINSTALL_NAME"));

        // TargetPanel
        Thread.sleep(1000);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.optionPane(Timeout.timeout(1000)).focus();
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();

        // PacksPanel
        Thread.sleep(2000);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // InstallPanel
        Thread.sleep(2000);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // ShortcutPanel
        Thread.sleep(2000);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // SimpleFinishPanel
        Thread.sleep(1200);
        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();

        // verify the installer has terminated, and an uninstaller has been written
        housekeeper.waitShutdown(2 * 60 * 1000);
        assertTrue(housekeeper.hasShutdown());
        assertEquals(0, housekeeper.getExitCode());
        assertFalse(housekeeper.getReboot());
        File uninstaller = getUninstallerJar();

        // make sure there is an Uninstall entry for the installation
        assertTrue(registryKeyExists(handler, UNINSTALL_KEY));

        // make sure a shortcut to the uninstaller exists
        File shortcut = checkShortcut(ShellLink.PROGRAM_MENU, ShellLink.ALL_USERS, "IzPack Windows Installation Test",
                                      "Uninstaller", uninstaller, "This uninstalls the test", librarian);

        // run the uninstaller
        UninstallHelper.guiUninstall(uninstaller);

        // make sure the Uninstall entry has been removed
        assertFalse(registryKeyExists(handler, UNINSTALL_KEY));

        // verify the shortcut no longer exists
        assertFalse(shortcut.exists());

    }

    /**
     * Destroys registry entries that may not have been cleared out by a previous run.
     *
     * @throws NativeLibException if the entries cannot be removed
     */
    private void destroyRegistryEntries() throws NativeLibException
    {
        registryDeleteUninstallKey(handler, UNINSTALL_KEY);
    }

}
