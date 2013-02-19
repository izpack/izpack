package com.izforge.izpack.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

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
import com.izforge.izpack.installer.panel.Panels;
import com.izforge.izpack.panels.checkedhello.CheckedHelloPanel;
import com.izforge.izpack.panels.finish.FinishPanel;
import com.izforge.izpack.panels.htmllicence.HTMLLicencePanel;
import com.izforge.izpack.panels.install.InstallPanel;
import com.izforge.izpack.panels.packs.PacksPanel;
import com.izforge.izpack.panels.shortcut.ShortcutPanel;
import com.izforge.izpack.panels.summary.SummaryPanel;
import com.izforge.izpack.panels.target.TargetPanel;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.Platforms;

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
    private Panels panels;

    public IzpackInstallationTest(LanguageDialog languageDialog, InstallerFrame installerFrame,
                                  GUIInstallData installData, InstallerController installerController,
                                  RegistryDefaultHandler handler, Panels panels)
    {
        this.installerController = installerController;
        this.languageDialog = languageDialog;
        this.installData = installData;
        this.installerFrame = installerFrame;
        this.handler = handler;
        this.panels = panels;
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
        // NOTE: the following variable is set for the "warfilesetup" condition defined in
        // izpack-dist/src/main/izpack/conditions.xml. This file may or may not be read by RulesEngineImpl
        // depending on the classpath, and thus cause the test to fail. TODO - fix this $%^#!
        installData.setVariable("izpack.setuptype", "warfile");


        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");

        installData.setInstallPath(installPath.getAbsolutePath());
        installData.setDefaultInstallPath(installPath.getAbsolutePath());
        HelperTestMethod.clickDefaultLang(languageDialog);

        installerFrameFixture = HelperTestMethod.prepareFrameFixture(installerFrame, installerController);
        // Hello panel
        Thread.sleep(600);
        assertEquals(CheckedHelloPanel.class.getName(), panels.getPanel().getClassName());
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();


        // Info Panel
        Thread.sleep(600);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Licence Panel
        Thread.sleep(1000);
        assertEquals(HTMLLicencePanel.class.getName(), panels.getPanel().getClassName());
        installerFrameFixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Target Panel
        assertEquals(TargetPanel.class.getName(), panels.getPanel().getClassName());
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.optionPane(Timeout.timeout(1000)).focus();
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();

        // Packs
        Thread.sleep(600);
        assertEquals(PacksPanel.class.getName(), panels.getPanel().getClassName());
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Summary
        Thread.sleep(600);
        assertEquals(SummaryPanel.class.getName(), panels.getPanel().getClassName());
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Install
        Thread.sleep(600);
        assertEquals(InstallPanel.class.getName(), panels.getPanel().getClassName());
        HelperTestMethod.waitAndCheckInstallation(installData, installPath);

        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Shortcut
        // Deselect shortcut creation
        if (!installData.getPlatform().isA(Platforms.MAC))
        {
            Thread.sleep(1000);
            assertEquals(ShortcutPanel.class.getName(), panels.getPanel().getClassName());
            installerFrameFixture.checkBox(GuiId.SHORTCUT_CREATE_CHECK_BOX.id).click();
            installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        }

        Thread.sleep(1000);

        // Finish
        assertEquals(FinishPanel.class.getName(), panels.getPanel().getClassName());
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