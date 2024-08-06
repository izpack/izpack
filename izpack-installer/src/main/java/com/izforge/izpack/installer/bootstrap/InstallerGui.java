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
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.installer.container.impl.GUIInstallerContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.SplashScreen;
import com.izforge.izpack.installer.language.LanguageDialog;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.util.Housekeeper;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gui-dedicated installer bootstrap
 */
public class InstallerGui
{
    private static final Logger logger = Logger.getLogger(InstallerGui.class.getName());
    
    private static SplashScreen splashScreen = null;

    
    public static void run(final String langCode, final String mediaPath, final Overrides defaults) throws Exception
    {
        final InstallerContainer applicationComponent = new GUIInstallerContainer();
        final Container installerContainer = applicationComponent.getComponent(Container.class);

		final Object trigger = new Object();
		// display the splash screen from AWT thread
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    splashScreen = installerContainer.getComponent(SplashScreen.class);
                    splashScreen.displaySplashScreen(trigger);
                }
                catch (Exception e)
                {
                	logger.log(Level.WARNING, "Prepare and display splashScreen failed.", e);
                	
                	// TODO not sure this works because thrown from AWT thread ...
                    throw new IzPackException(e);
                }
            }
        });
        

        try {
        	GUIInstallData installData = applicationComponent.getComponent(GUIInstallData.class);

            if (mediaPath != null)
	        {
	            installData.setMediaPath(mediaPath);
	        }

			if (defaults != null)
			{
				defaults.setInstallData(applicationComponent.getComponent(GUIInstallData.class));
				defaults.load();
				logger.info("Loaded " + defaults.size() + " override(s) from " + defaults.getFile());

				DefaultVariables variables = applicationComponent.getComponent(DefaultVariables.class);
				variables.setOverrides(defaults);
			}

			InstallerController controller = installerContainer.getComponent(InstallerController.class);
	        
	        if (installData.guiPrefs.modifier.containsKey("useSplashScreen")) {
		        int duration = Integer.parseInt(installData.guiPrefs.modifier.get("useSplashScreen"));
		        if (duration > 0) {
		            // wait for creation and signal that the splash screen display duration has elapsed
			        synchronized (trigger) {
			        	trigger.wait(duration);
					}
		        }
	        }
	        
	        if (splashScreen != null) {
	        	// remove the splash screen from AWT thread
	        	SwingUtilities.invokeLater(new Runnable()
	            {
	                public void run()
	                {
	                	try {
	                		splashScreen.removeSplashScreen();
	                	}
	                	catch (Exception e)
	                	{
	                		throw new IzPackException(e);
	                	}
	                }
	            });
	        }
	        
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        if (langCode == null)
                        {
                            try
                            {
                                installerContainer.getComponent(LanguageDialog.class).initLangPack();
                            }
                            catch (Exception ex)
                            {
                                logger.severe("The language pack couldn't be initialized.");
                            }
                        }
                        else
                        {
                          installerContainer.getComponent(LanguageDialog.class).propagateLocale(langCode);
                        }
                        if (!installerContainer.getComponent(RequirementsChecker.class).check())
                        {
                            logger.info("Not all installer requirements are fulfilled.");
                            installerContainer.getComponent(Housekeeper.class).shutDown(-1);
                        }
                    }
                });
	        controller.buildInstallation().launchInstallation();
	    }
	    catch (Exception e)
	    {
	        throw new IzPackException(e);
	    }
        
    }
}
