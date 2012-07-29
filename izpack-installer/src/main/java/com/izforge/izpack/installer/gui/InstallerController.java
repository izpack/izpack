package com.izforge.izpack.installer.gui;

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
        installerFrame.buildGUI();
        installerFrame.sizeFrame();
        return this;
    }

    public void launchInstallation()
    {
        installerFrame.setVisible(true);
        installerFrame.navigateNext();
    }

}
