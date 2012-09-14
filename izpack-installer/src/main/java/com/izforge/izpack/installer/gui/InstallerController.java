package com.izforge.izpack.installer.gui;

import javax.swing.SwingUtilities;

import com.izforge.izpack.api.exception.IzPackException;
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

        run(new Runnable()
        {
            @Override
            public void run()
            {
                installerFrame.buildGUI();
                installerFrame.sizeFrame();
            }
        });
        return this;
    }

    public void launchInstallation()
    {
        run(new Runnable()
        {
            @Override
            public void run()
            {
                installerFrame.setVisible(true);
                installerFrame.navigateNext();
            }
        });
    }

    /**
     * Runs a {@code Runnable} inside the event dispatch thread.
     *
     * @param action the action to run
     */
    private void run(Runnable action)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            action.run();
        }
        else
        {
            try
            {
                SwingUtilities.invokeAndWait(action);
            }
            catch (Exception exception)
            {
                throw new IzPackException(exception);
            }
        }
    }
}
