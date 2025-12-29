package com.izforge.izpack.integration;

import static com.izforge.izpack.integration.HelperTestMethod.initLangPack;
import static com.izforge.izpack.integration.HelperTestMethod.prepareFrameFixture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.awt.Image;
import java.io.File;
import java.util.concurrent.TimeUnit;

import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.compiler.container.TestGUIInstallationContainer;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test for an installation
 */
@RunWith(PicoRunner.class)
@Container(TestGUIInstallationContainer.class)
public class InstallationTest
{
    @Rule
    public TestRule globalTimeout = new Timeout(HelperTestMethod.TIMEOUT, TimeUnit.MILLISECONDS);

    private FrameFixture installerFrameFixture;
    private IconsDatabase icons;
    private LanguageDialog languageDialog;
    private InstallerFrame installerFrame;
    private GUIInstallData installData;
    private InstallerController installerController;
    private InstallerContainer installerContainer;

    public InstallationTest(IconsDatabase icons, LanguageDialog languageDialog,
                            InstallerFrame installerFrame, GUIInstallData installData,
                            InstallerController installerController, InstallerContainer installerContainer)
    {
        this.installerController = installerController;
        this.icons = icons;
        this.languageDialog = languageDialog;
        this.installData = installData;
        this.installerFrame = installerFrame;
        this.installerContainer = installerContainer;
    }

    @After
    public void tearBinding() throws NoSuchFieldException, IllegalAccessException
    {
        if (installerFrameFixture != null)
        {
            installerFrameFixture.cleanUp();
            installerFrameFixture = null;
        }
    }

    @Test
    @InstallFile("samples/helloAndFinish.xml")
    public void testHelloAndFinishPanels() throws Exception
    {
        Image image = icons.get("JFrameIcon").getImage();
        assertThat(image, notNullValue());

        initLangPack(languageDialog);
        installerFrameFixture = prepareFrameFixture(installerFrame, installerController);

        // Hello panel
//        installerFrameFixture.requireSize(new Dimension(640, 480));
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.requireVisible();
        // Finish panel
    }

    @Test
    @InstallFile("samples/doublePanel.xml")
    public void testMultiplePanels() throws Exception
    {
        installerController.buildInstallation();

        HelloPanel firstHelloPanel = (HelloPanel) installerContainer.getPanel("42");
        assertThat(firstHelloPanel.getMetadata().getPanelId(), is("42"));

        HelloPanel secondHelloPanel = (HelloPanel) installerContainer.getPanel("34");
        assertThat(secondHelloPanel.getMetadata().getPanelId(), is("34"));
    }

    @Test
    @InstallFile("samples/panelconfiguration.xml")
    public void testPanelConfiguration() throws Exception
    {
        installerController.buildInstallation();

        HelloPanel helloPanel = (HelloPanel) installerContainer.getPanel("hellopanel");
        assertThat(helloPanel.getMetadata().getConfigurationOptionValue("config1", installData.getRules()), is("value1"));
        assertThat(helloPanel.getMetadata().getConfigurationOptionValue("config2", installData.getRules()), is("value2"));
    }

    @Test
    @InstallFile("samples/substanceLaf/substanceLaf.xml")
    public void testSubstanceLaf() throws Exception
    {
        initLangPack(languageDialog);
        installerFrameFixture = prepareFrameFixture(installerFrame, installerController);

        // Hello panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.requireVisible();
        // Finish panel
    }

    @Test
    @InstallFile("samples/silverpeas/silverpeas.xml")
    public void testSilverpeas() throws Exception
    {
        initLangPack(languageDialog);
        installerFrameFixture = prepareFrameFixture(installerFrame, installerController);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.requireVisible();
    }

    @Test
    @Ignore
    @InstallFile("samples/helloAndFinish.xml")
    public void testHelloAndFinishPanelsCompressed() throws Exception
    {
//        System.out.println("Using file " + out.getName());
//        File workingDirectory = getWorkingDirectory("samples");
//        File out = new File("out.jar");
//        File installerFile = new File(workingDirectory, "helloAndFinish.xml");
//        CompilerData data = new CompilerData(installerFile.getAbsolutePath(), workingDirectory.getAbsolutePath(), out.getAbsolutePath());
//        data.setComprFormat("bzip2");
//        data.setComprLevel(2);
//        compileInstallJar(data);
//        applicationContainer.getComponent(LanguageDialog.class).initLangPack();
//        installerFrameFixture = prepareFrameFixture();
//
//        // Hello panel
//        installerFrameFixture.requireSize(new Dimension(640, 480));
//        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
//        installerFrameFixture.requireVisible();
//        // Finish panel
//        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();
    }

    @Test
    @InstallFile("samples/basicInstall/basicInstall.xml")
    public void testBasicInstall() throws Exception
    {
        File installPath = HelperTestMethod.prepareInstallation(installData);
        // Lang picker
        HelperTestMethod.clickDefaultLang(languageDialog);

        installerFrameFixture = prepareFrameFixture(installerFrame, installerController);
        Thread.sleep(600);
        // Hello panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(600);
        // Info Panel
        installerFrameFixture.textBox(GuiId.INFO_PANEL_TEXT_AREA.id).requireText("A readme file ...");
        installerFrameFixture.button(GuiId.BUTTON_PREV.id).requireVisible();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.button(GuiId.BUTTON_PREV.id).requireEnabled();
        Thread.sleep(300);
        // Licence Panel
        installerFrameFixture.textBox(GuiId.LICENCE_TEXT_AREA.id).requireText("(Consider it as a licence file ...)");
        installerFrameFixture.radioButton(GuiId.LICENCE_NO_RADIO.id).requireSelected();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).requireDisabled();
        installerFrameFixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        Thread.sleep(300);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Target Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();
        // Packs Panel
        Thread.sleep(300);
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Install Panel
        HelperTestMethod.waitAndCheckInstallation(installData, installPath);

        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Finish panel
        installerFrameFixture.button(GuiId.FINISH_PANEL_AUTO_BUTTON.id).click();
        Thread.sleep(800);
        installerFrameFixture.fileChooser(GuiId.FINISH_PANEL_FILE_CHOOSER.id).fileNameTextBox().setText("auto.xml");
        Thread.sleep(300);
        installerFrameFixture.fileChooser(GuiId.FINISH_PANEL_FILE_CHOOSER.id).approve();
        assertThat(new File(installPath, "auto.xml").exists(), is(true));
//        installerFrameFixture.button(GuiId.BUTTON_QUIT.id).click();
    }
}
