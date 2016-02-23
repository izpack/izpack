/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.container.impl.ConsoleInstallerContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.language.LanguageConsoleDialog;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.util.Housekeeper;

import java.util.logging.Logger;

/**
 * Console installer bootstrap
 */
public class InstallerConsole
{
  private static final Logger logger = Logger.getLogger(InstallerConsole.class.getName());
  
  public static void run(final int type, final int consoleAction, final String path, final String langCode, final String mediaPath, final String[] args)
  {
    final InstallerContainer applicationComponent = new ConsoleInstallerContainer();
    final Container installerContainer = applicationComponent.getComponent(Container.class);
    try
    {
      if (mediaPath != null)
      {
        InstallData installData = applicationComponent.getComponent(InstallData.class);
        installData.setMediaPath(mediaPath);
      }
      ConsoleInstaller consoleInstaller = installerContainer.getComponent(ConsoleInstaller.class);
      if (langCode == null)
      {
        installerContainer.getComponent(LanguageConsoleDialog.class).initLangPack();
      }
      else
      {
        installerContainer.getComponent(LanguageConsoleDialog.class).propagateLocale(langCode);
      }
      if (!installerContainer.getComponent(RequirementsChecker.class).check())
      {
        logger.info("Not all installer requirements are fulfilled.");
        installerContainer.getComponent(Housekeeper.class).shutDown(-1);
      }
      consoleInstaller.run(consoleAction, path, args);
    }
    catch (Exception e)
    {
      throw new IzPackException(e);
    }
  }
}