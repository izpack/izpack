/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2005 Klaus Bartz
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.panels.checkedhello;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.hello.HelloPanel;

/**
 * An extended hello panel class which detects whether the product was already installed or not.
 * This class should be only used if the RegistryInstallerListener will be also used. Current the
 * check will be only performed on Windows operating system. This class can be used also as example
 * how to use the registry stuff to get informations from the current system.
 *
 * @author Klaus Bartz
 */
public class CheckedHelloPanel extends HelloPanel
{

    /**
     * Required (serializable)
     */
    private static final long serialVersionUID = 1737042770727953387L;
    /**
     * Flag to break installation or not.
     */
    protected boolean abortInstallation;

    /**
     * The registry helper.
     */
    private transient final RegistryHelper registryHelper;

    /**
     * The logger.
     */
    private static Logger logger = Logger.getLogger(CheckedHelloPanel.class.getName());

    /**
     * The constructor.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent frame
     * @param installData the installation data
     * @param resources   the resources
     * @param handler     the registry handler instance
     * @param log         the log
     * @throws Exception if it cannot be determined if the application is registered
     */
    public CheckedHelloPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                             RegistryDefaultHandler handler, Log log) throws Exception
    {
        super(panel, parent, installData, resources, log);
        registryHelper = new RegistryHelper(handler, installData);
        abortInstallation = isRegistered();
    }

    /**
     * This method should only be called if this product was already installed. It resolves the
     * install path of the first already installed product and asks the user whether to install
     * twice or not.
     *
     * @return whether a multiple Install should be performed or not.
     * @throws NativeLibException for any native library error
     */
    protected boolean multipleInstall() throws NativeLibException
    {
        String path = registryHelper.getInstallationPath();
        if (path == null)
        {
            path = "<not found>";
        }
        String noLuck = getString("CheckedHelloPanel.productAlreadyExist0") + path + " . "
                + getString("CheckedHelloPanel.productAlreadyExist1");
        return (askQuestion(getString("installer.error"), noLuck,
                            AbstractUIHandler.CHOICES_YES_NO) == AbstractUIHandler.ANSWER_YES);
    }

    /**
     * Returns whether the handled application is already registered or not. The validation will be
     * made only on systems which contains a registry (Windows).
     *
     * @return <tt>true</tt> if the application is registered
     * @throws Exception if it cannot be determined if the application is registered
     */
    protected boolean isRegistered() throws Exception
    {
        return registryHelper.isRegistered();
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the internal abort flag is not set, else false
     */
    public boolean isValidated()
    {
        return (!abortInstallation);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.installer.IzPanel#panelActivate()
     */

    public void panelActivate()
    {
        if (abortInstallation)
        {
            parent.lockNextButton();
            try
            {
                if (multipleInstall())
                {
                    setUniqueUninstallKey();
                    abortInstallation = false;
                    parent.unlockNextButton();
                }
                else
                {
                    installData.getInfo().setUninstallerPath(null);
                    installData.getInfo().setUninstallerName(null);
                    installData.getInfo().setUninstallerCondition("uninstaller.nowrite");
                }
            }
            catch (Exception exception)
            {
                logger.log(Level.WARNING, exception.getMessage(), exception);
            }
        }
        installData.setVariable("UNINSTALL_NAME", registryHelper.getUninstallName());
    }

    /**
     * Generates an unique uninstall key, displaying it to the user.
     *
     * @throws NativeLibException for any native library error
     */
    private void setUniqueUninstallKey() throws NativeLibException
    {
        String newUninstallName = registryHelper.updateUninstallName();
        emitNotification(getString("CheckedHelloPanel.infoOverUninstallKey")
                                 + newUninstallName);
    }
}
