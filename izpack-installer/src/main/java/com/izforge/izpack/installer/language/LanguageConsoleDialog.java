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
package com.izforge.izpack.installer.language;

import com.izforge.izpack.api.exception.UserInterruptException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.installer.container.provider.AbstractInstallDataProvider;
import com.izforge.izpack.installer.data.ConsoleInstallData;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.Housekeeper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LanguageConsoleDialog {
  private final ConsoleInstallData installData;
  private final Console console;
  private final Housekeeper housekeeper;
  private final Locales locales;
  private Map<String, String> displayNames = new LinkedHashMap<String, String>();
  private static final Logger logger = Logger.getLogger(LanguageConsoleDialog.class.getName());
  
  public LanguageConsoleDialog(Locales locales, ConsoleInstallData installData, Console console, Housekeeper housekeeper)
  {
    this.installData = installData;
    this.console = console;
    this.housekeeper = housekeeper;
    this.locales = locales;
  }
  
  /**
   * Displays the dialog.
   */
  public void initLangPack()
  {
    Languages languages = new Languages(locales, installData);
    displayNames = languages.getDisplayNames();
    switch (locales.getLocales().size()) {
      case 0:
        break;
      case 1:
        String codeOfUniqueLanguage = displayNames.keySet().iterator().next();
        propagateLocale(codeOfUniqueLanguage);
        break;
      default:
        Iterator<String> iterator = displayNames.keySet().iterator();
        console.println("Select your language");

        String defaultIso3Lang = installData.getVariable("DEFAULT_ISO3_LANG");
        if (defaultIso3Lang == null || !displayNames.containsKey(defaultIso3Lang.toLowerCase()))
        {
          defaultIso3Lang = Locale.getDefault().getISO3Language();
        }

        int i = 0;
        int defaultCodeIndex = -1;
        while (iterator.hasNext())
        {
          String langCode = iterator.next();
          if (langCode.equalsIgnoreCase(defaultIso3Lang))
          {
              defaultCodeIndex = i;
          }
          console.println(i + "  [" + (i == defaultCodeIndex? "x" : " ") + "] " + langCode);
          i++;
        }
        try {
            int numberOfUniqueLanguage = console.prompt(installData.getMessages().get("ConsoleInstaller.inputSelection"), 0, displayNames.keySet().size() - 1, defaultCodeIndex, 0);
            String[] keys = displayNames.keySet().toArray(new String[0]);
            propagateLocale(keys[numberOfUniqueLanguage]);
        }
        catch (UserInterruptException uie)
        {
            console.println(uie.getMessage());
            // At this time the locale may not be set so translated message is not used
            console.println("[ Console installation ABORTED by the user! ]");
            housekeeper.shutDown(1);
        } 
    }
  }
  
  /**
   * Sets the selected locale on the installation data.
   *
   * @param code the locale ISO code
   */
  public void propagateLocale(String code)
  {
    try
    {
      locales.setLocale(code);
      Locale newLocale = locales.getLocale();
      Locale.setDefault(newLocale);
      installData.setLocale(locales.getLocale(), locales.getISOCode());
      installData.setMessages(locales.getMessages());
      AbstractInstallDataProvider.addCustomLangpack(installData, locales);
      AbstractInstallDataProvider.addUserInputLangpack(installData, locales);
    }
    catch (Exception exception)
    {
      logger.log(Level.SEVERE, exception.getMessage(), exception);
    }
  }
}