/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.installer.bootstrap;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.container.impl.GUIInstallerContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.language.LanguageDialog;

import javax.swing.*;

/**
 * Gui-dedicated installer bootstrap
 */
public class InstallerGui
{

    public static void run(final String mediaPath) throws Exception
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    InstallerContainer applicationComponent = new GUIInstallerContainer();
                    if (mediaPath != null)
                    {
                        InstallData installData = applicationComponent.getComponent(InstallData.class);
                        installData.setMediaPath(mediaPath);
                    }

                    Container installerContainer = applicationComponent.getComponent(Container.class);

                    InstallerController controller = installerContainer.getComponent(InstallerController.class);

                    installerContainer.getComponent(LanguageDialog.class).initLangPack();
                    controller.buildInstallation().launchInstallation();
                }
                catch (Exception e)
                {
                    throw new IzPackException(e);
                }
            }
        });

    }
}
