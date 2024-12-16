package com.izforge.izpack.installer.container.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PlatformModelMatcher;

import java.io.IOException;

/**
 * Install data loader
 */
@Singleton
public class AutomatedInstallDataProvider extends AbstractInstallDataProvider<AutomatedInstallData>
{
    private final Resources resources;
    private final Locales locales;
    private final DefaultVariables variables;
    private final Housekeeper housekeeper;
    private final PlatformModelMatcher matcher;

    @Inject
    public AutomatedInstallDataProvider(Resources resources,
                                        Locales locales,
                                        DefaultVariables variables,
                                        Housekeeper housekeeper,
                                        PlatformModelMatcher matcher)
    {
        this.resources = resources;
        this.locales = locales;
        this.variables = variables;
        this.housekeeper = housekeeper;
        this.matcher = matcher;
    }

    @Override
    public AutomatedInstallData loadInstallData()
    {
        try {
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

        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load installation data", e);
        }
    }

}
