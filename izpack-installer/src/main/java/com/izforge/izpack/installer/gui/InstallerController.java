package com.izforge.izpack.installer.gui;

import javax.swing.SwingUtilities;

import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;

/**
 * Installer frame controller
 *
 * @author Anthonin Bonnefoy
 */
public class InstallerController
{

    private InstallerFrame installerFrame;

    public InstallerController(InstallDataConfiguratorWithRules installDataRulesEngineManager,
                               InstallerFrame installerFrame)
    {

        this.installerFrame = installerFrame;
        installDataRulesEngineManager.configureInstallData();

    }

    public InstallerController buildInstallation()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    installerFrame.buildGUI();
                    installerFrame.sizeFrame();
                }
            });
        }
        catch (Exception exception)
        {
            throw new IllegalStateException(exception);
        }
        return this;
    }

    public void launchInstallation()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    installerFrame.setVisible(true);
                    installerFrame.navigateNext();
                }
            });
        }
        catch (Exception exception)
        {
            throw new IllegalStateException(exception);
        }
    }

}
