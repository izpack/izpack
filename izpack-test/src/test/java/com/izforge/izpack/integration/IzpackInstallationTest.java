package com.izforge.izpack.integration;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.timing.Timeout;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.OsVersion;

/**
 * Test for an installation.
 * <p/>
 * NOTE: this test uses the IzPack install.xml, and will remove any registry entry associated with an existing IzPack
 * installation.
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class IzpackInstallationTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public TestRule globalTimeout = new org.junit.rules.Timeout(HelperTestMethod.TIMEOUT);

    private DialogFixture dialogFrameFixture;
    private FrameFixture installerFrameFixture;
    private LanguageDialog languageDialog;
    private InstallerFrame installerFrame;
    private GUIInstallData installData;
    private InstallerController installerController;
    private RegistryDefaultHandler handler;

    public IzpackInstallationTest(LanguageDialog languageDialog, InstallerFrame installerFrame,
                                  GUIInstallData installData, InstallerController installerController,
                                  RegistryDefaultHandler handler)
    {
        this.installerController = installerController;
        this.languageDialog = languageDialog;
        this.installData = installData;
        this.installerFrame = installerFrame;
        this.handler = handler;
    }

    /**
     * Sets up the test case.
     *
     * @throws NativeLibException for any native library error
     */
    @Before
    public void setUp() throws NativeLibException
    {
        RegistryHandler registry = handler.getInstance();
        if (registry != null)
        {
            // remove any existing uninstall key
            String uninstallName = registry.getUninstallName();
            if (!StringUtils.isEmpty(uninstallName))
            {
                registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
                String key = RegistryHandler.UNINSTALL_ROOT + uninstallName;
                if (registry.keyExist(key))
                {
                    registry.deleteKey(key);
                }
            }
        }
    }

    @After
    public void tearBinding() throws NoSuchFieldException, IllegalAccessException
    {
        try
        {
            if (dialogFrameFixture != null)
            {
                dialogFrameFixture.cleanUp();
                dialogFrameFixture = null;
            }
        }
        finally
        {
            if (installerFrameFixture != null)
            {
                installerFrameFixture.cleanUp();
                installerFrameFixture = null;
            }
        }
    }

    @Test
    @InstallFile("samples/izpack/install.xml")
    public void testIzpackInstallation() throws Exception
    {
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");

        installData.setInstallPath(installPath.getAbsolutePath());
        installData.setDefaultInstallPath(installPath.getAbsolutePath());
        HelperTestMethod.clickDefaultLang(languageDialog);

        installerFrameFixture = HelperTestMethod.prepareFrameFixture(installerFrame, installerController);
        // Hello panel
        Thread.sleep(600);
//        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Check Panel
        Thread.sleep(600);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Licence Panel
        Thread.sleep(600);
        installerFrameFixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Target Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.optionPane(Timeout.timeout(1000)).focus();
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();

        // Packs
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Summary
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Install
        HelperTestMethod.waitAndCheckInstallation(installData, installPath);

        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Shortcut
        // Deselect shortcut creation
        if (!OsVersion.IS_MAC)
        {
            Thread.sleep(1000);
            installerFrameFixture.checkBox(GuiId.SHORTCUT_CREATE_CHECK_BOX.id).click();
            installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        }

        Thread.sleep(1000);

        // Finish
        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();

        Thread.sleep(1000);

        checkIzpackInstallation(installPath);

        // run the uninstaller
        File uninstaller = UninstallHelper.getUninstallerJar(installData);
        UninstallHelper.guiUninstall(uninstaller);
    }

    private void checkIzpackInstallation(File installPath)
    {
        List<String> paths = new ArrayList<String>();
        File[] files = installPath.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                paths.add(file.getName());
            }
        }
        assertThat(paths, IsCollectionContaining.hasItems(
                Is.is("bin"),
                Is.is("legal"),
                Is.is("lib")
        ));
    }
}