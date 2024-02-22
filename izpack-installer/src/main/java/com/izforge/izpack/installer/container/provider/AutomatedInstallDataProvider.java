package com.izforge.izpack.installer.container.provider;

import java.io.IOException;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * Install data loader
 */
public class AutomatedInstallDataProvider extends AbstractInstallDataProvider
{

    public AutomatedInstallData provide(Resources resources, Locales locales, DefaultVariables variables,
                                        Housekeeper housekeeper, PlatformModelMatcher matcher)
            throws IOException, ClassNotFoundException, InstallerException
    {
        AutomatedInstallData automatedInstallData = new InstallData(variables, matcher.getCurrentPlatform());
        automatedInstallData.setVariable(com.izforge.izpack.api.data.InstallData.INSTALLER_MODE, com.izforge.izpack.api.data.InstallData.INSTALLER_MODE_AUTO);
        // Loads the installation data
        loadInstallData(automatedInstallData, resources, matcher, housekeeper);
        loadInstallerRequirements(automatedInstallData, resources);
        loadDynamicVariables(variables, automatedInstallData, resources);
        loadDynamicConditions(automatedInstallData, resources);
        loadDefaultLocale(automatedInstallData, locales);
        // Load custom langpack if exist.
        AbstractInstallDataProvider.addCustomLangpack(automatedInstallData, locales);
        // Load user input langpack if exist.
        AbstractInstallDataProvider.addUserInputLangpack(automatedInstallData, locales);
        return automatedInstallData;
    }

}
