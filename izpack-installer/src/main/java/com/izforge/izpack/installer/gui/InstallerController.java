package com.izforge.izpack.installer.gui;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installer frame controller
 *
 * @author Anthonin Bonnefoy
 */
public class InstallerController
{
    private static final Logger logger = Logger.getLogger(InstallerController.class.getName());

    private InstallerFrame installerFrame;

    public InstallerController(InstallDataConfiguratorWithRules installDataRulesEngineManager,
                               InstallerFrame installerFrame)
    {
        this.installerFrame = installerFrame;
        installDataRulesEngineManager.configureInstallData();
    }

    public InstallerController buildInstallation()
    {

        run(() ->
        {
            installerFrame.buildGUI();
            installerFrame.sizeFrame();
        });
        return this;
    }

    public void launchInstallation()
    {
        run(() ->
        {
            installerFrame.setVisible(true);
            installerFrame.navigateNext();
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
            catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                logger.log(Level.INFO, "Action invocation failed", targetException);
                throw new IzPackException(targetException);
            }
            catch (Exception exception)
            {
                logger.log(Level.INFO, "Action invocation failed", exception);
                throw new IzPackException(exception);
            }
        }
    }
}
