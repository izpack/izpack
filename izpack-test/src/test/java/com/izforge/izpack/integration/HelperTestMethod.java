package com.izforge.izpack.integration;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.core.Is;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.language.LanguageDialog;

/**
 * Shared methods beetween tests classes
 *
 * @author Anthonin Bonnefoy
 */
public class HelperTestMethod
{
    public static final int TIMEOUT = 60000;

    public static File prepareInstallation(InstallData installData) throws IOException
    {
        File installPath = new File(installData.getDefaultInstallPath());
        FileUtils.deleteDirectory(installPath);
        assertThat(installPath.exists(), Is.is(false));
        return installPath;
    }

    public static void clickDefaultLang(LanguageDialog languageDialog)
    {
        DialogFixture fixture = prepareDialogFixture(languageDialog);
        fixture.button(GuiId.BUTTON_LANG_OK.id).click();
        // Seems necessary to unlock window
        fixture.cleanUp();
    }

    /**
     * Prepare fest fixture for installer frame
     *
     * @param installerFrame
     * @param installerController
     * @throws Exception
     */
    public static FrameFixture prepareFrameFixture(InstallerFrame installerFrame,
                                                   final InstallerController installerController) throws Exception
    {
        FrameFixture installerFrameFixture = new FrameFixture(installerFrame);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                try
                {
                    installerController.buildInstallation();
                    installerController.launchInstallation();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new IzPackException(e);
                }
            }
        });
        return installerFrameFixture;
    }

    /**
     * Prepare fest fixture for lang selection
     *
     * @param languageDialog
     */
    public static DialogFixture prepareDialogFixture(LanguageDialog languageDialog)
    {
        DialogFixture dialogFixture = new DialogFixture(languageDialog);
        dialogFixture.show();
        return dialogFixture;
    }

    public static void waitAndCheckInstallation(InstallData installData)
            throws InterruptedException
    {
        waitAndCheckInstallation(installData, new File(installData.getInstallPath()));
    }

    public static void waitAndCheckInstallation(InstallData installData, File installPath)
            throws InterruptedException
    {
        while (!installData.isCanClose())
        {
            Thread.sleep(500);
        }
        assertThat(installPath.exists(), Is.is(true));
        UninstallData uninstallData = new UninstallData();
        for (String installedFile : uninstallData.getInstalledFilesList())
        {
            File file = new File(installedFile);
            assertThat(file.exists(), Is.is(true));
        }
    }
}
