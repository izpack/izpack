package com.izforge.izpack.integration;

import static java.lang.Thread.sleep;
import static javax.swing.SwingUtilities.invokeAndWait;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.language.LanguageDialog;
import java.io.File;
import java.io.IOException;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;

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
        deleteDirectory(installPath);
        assertThat(installPath).doesNotExist();
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
     * Invokes {@link LanguageDialog#initLangPack()} within the EDT.
     *
     * @param dialog The language dialog.
     * @throws Exception If an error occurs.
     */
    public static void initLangPack(final LanguageDialog dialog) throws Exception
    {
        invokeAndWait(new Runnable() {
            @Override
            public void run() {

                try
                {
                    dialog.initLangPack();
                }
                catch (Exception e)
                {
                    throw new IzPackException(e);
                }
            }
        });
    }

    /**
     * Prepare fest fixture for installer frame
     *
     * @param installerFrame
     * @param installerController
     */
    public static FrameFixture prepareFrameFixture(InstallerFrame installerFrame,
                                                   final InstallerController installerController)
    {
        FrameFixture installerFrameFixture = new FrameFixture(installerFrame);

        installerController.buildInstallation();
        installerController.launchInstallation();
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
            sleep(500);
        }
        assertThat(installPath).exists();
        UninstallData uninstallData = new UninstallData();
        for (String installedFile : uninstallData.getInstalledFilesList())
        {
            File file = new File(installedFile);
            assertThat(file).exists();
        }
    }
}
