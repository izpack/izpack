package com.izforge.izpack.integration;

import static com.izforge.izpack.integration.HelperTestMethod.clickDefaultLang;
import static com.izforge.izpack.integration.HelperTestMethod.prepareFrameFixture;
import static com.izforge.izpack.integration.HelperTestMethod.waitAndCheckInstallation;
import static com.izforge.izpack.integration.UninstallHelper.getUninstallerJar;
import static com.izforge.izpack.integration.UninstallHelper.guiUninstall;
import static java.lang.Thread.sleep;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.swing.timing.Timeout.timeout;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.compiler.container.TestGUIInstallationContainer;
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
import com.izforge.izpack.test.junit.TestTimeout;
import com.izforge.izpack.util.Platforms;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test for an installation.
 * <p/>
 * NOTE: this test uses the IzPack install.xml, and will remove any registry entry associated with an existing IzPack
 * installation.
 */
@Container(TestGUIInstallationContainer.class)
@TestTimeout(HelperTestMethod.TIMEOUT)
public class IzpackInstallationTest
{
    @TempDir
    public Path temporaryFolder;

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
    @BeforeEach
    public void setUp() throws NativeLibException
    {
        RegistryHandler registry = handler.getInstance();
        if (registry != null)
        {
            // remove any existing uninstall key
            String uninstallName = registry.getUninstallName();
            if (!isEmpty(uninstallName))
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

    @AfterEach
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


        File installPath = temporaryFolder.resolve("izpackTest").toFile();

        installData.setInstallPath(installPath.getAbsolutePath());
        installData.setDefaultInstallPath(installPath.getAbsolutePath());
        clickDefaultLang(languageDialog);

        installerFrameFixture = prepareFrameFixture(installerFrame, installerController);
        // Hello panel
        sleep(600);
        assertThat(panels.getPanel().getClassName()).isEqualTo(CheckedHelloPanel.class.getName());
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();


        // Info Panel
        sleep(600);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Licence Panel
        sleep(1000);
        assertThat(panels.getPanel().getClassName()).isEqualTo(HTMLLicencePanel.class.getName());
        installerFrameFixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Target Panel
        assertThat(panels.getPanel().getClassName()).isEqualTo(TargetPanel.class.getName());
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.optionPane(timeout(1000)).focus();
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();

        // Packs
        sleep(600);
        assertThat(panels.getPanel().getClassName()).isEqualTo(PacksPanel.class.getName());
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Summary
        sleep(600);
        assertThat(panels.getPanel().getClassName()).isEqualTo(SummaryPanel.class.getName());
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Install
        sleep(600);
        assertThat(panels.getPanel().getClassName()).isEqualTo(InstallPanel.class.getName());
        waitAndCheckInstallation(installData, installPath);

        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // Shortcut
        // Deselect shortcut creation
        if (!installData.getPlatform().isA(Platforms.MAC))
        {
            sleep(1000);
            assertThat(panels.getPanel().getClassName()).isEqualTo(ShortcutPanel.class.getName());
            installerFrameFixture.checkBox(GuiId.SHORTCUT_CREATE_CHECK_BOX.id).click();
            installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        }

        sleep(1000);

        // Finish
        assertThat(panels.getPanel().getClassName()).isEqualTo(FinishPanel.class.getName());
        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();

        sleep(1000);

        checkIzpackInstallation(installPath);

        // run the uninstaller
        File uninstaller = getUninstallerJar(installData);
        guiUninstall(uninstaller);
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
        assertThat(paths).contains("bin", "legal", "lib");
    }
}