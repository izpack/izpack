/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.installer.data.GuiExtension;


/**
 * Helper to map ISO3 codes to their corresponding display names according to the configured "langDisplayType"
 * modifier.
 *
 * @author Tim Anderson
 */
class Languages
{

    enum DisplayType
    {
        ISO3,     //  indicates to display ISO3 language codes
        NATIVE,   // indicates to display the native name for a language.
        DEFAULT   // Indicates to display the default name for a language.
    }

    /**
     * The display type. One of "iso3", "native" or "default".
     */
    private DisplayType displayType;

    /**
     * The language display names, keyed on their ISO3 codes.
     */
    private final Map<String, String> displayNames = new LinkedHashMap<>();

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Languages.class.getName());


    /**
     * Constructs a {@code Languages}.
     *
     * @param locales     the locales
     * @param installData the installation data
     * @param font        the font to verify that language display names can be displayed. May be {@code null}
     */
    public Languages(Locales locales, InstallData installData, Font font)
    {
        displayType = getDisplayType(installData);
        if (displayType == DisplayType.NATIVE && font == null)
        {
            logger.info("Cannot render native language display names - no font supplied for verification");
            displayType = DisplayType.DEFAULT;
        }

        DisplayNameCollector collector;
        if (displayType == DisplayType.NATIVE)
        {
            collector = new NativeDisplayNameCollector(font);
        }
        else if (displayType == DisplayType.DEFAULT)
        {
            collector = new DefaultDisplayNameCollector();
        }
        else
        {
            collector = new ISO3CodeCollector();
        }
        for (String code : locales.getISOCodes())
        {
            collector.addDisplayName(code, locales.getLocale(code), displayNames);
        }
    }
    
    /**
     * Constructs a {@code Languages}.
     *
     * @param locales     the locales
     * @param installData the installation data
     * @param font        the font to verify that language display names can be displayed. May be {@code null}
     */
    public Languages(Locales locales, AutomatedInstallData installData)
    {
        DisplayNameCollector collector = new DefaultDisplayNameCollector();
        for (String code : locales.getISOCodes())
        {
            collector.addDisplayName(code, locales.getLocale(code), displayNames);
        }
    }

    /**
     * Returns the type in which the languages will be displayed.
     *
     * @return the display type
     */
    public DisplayType getDisplayType()
    {
        return displayType;
    }

    /**
     * Returns the language display names, keyed on their ISO3 codes.
     *
     * @return the language display names
     */
    public Map<String, String> getDisplayNames()
    {
        return displayNames;
    }

    /**
     * Returns the type in which the language should be displayed.
     *
     * @return the language display type
     */
    private DisplayType getDisplayType(InstallData installData)
    {
        GuiExtension guiExtension = installData.getExtension(GuiExtension.class)
            .orElseThrow(() -> new IllegalArgumentException("Unsupported install data reference"));
        Map<String, String> modifier = guiExtension.modifiers();
        String langDisplayType = modifier.get("langDisplayType");
        if (langDisplayType != null && !langDisplayType.isEmpty())
        {
            try
            {
                return DisplayType.valueOf(langDisplayType.toUpperCase());
            }
            catch (IllegalArgumentException exception)
            {
                logger.warning("Invalid langDisplayType: " + langDisplayType);
            }
        }
        return DisplayType.DEFAULT;
    }

    /**
     * Collects display names.
     */
    private interface DisplayNameCollector
    {
        void addDisplayName(String code, Locale locale, Map<String, String> displayNames);
    }

    /**
     * Collects ISO3 codes.
     */
    private class ISO3CodeCollector implements DisplayNameCollector
    {

        @Override
        public void addDisplayName(String code, Locale locale, Map<String, String> displayNames)
        {
            displayNames.put(code, code);
        }
    }

    /**
     * Collects display names as they are written in the default locale.
     */
    private class DefaultDisplayNameCollector implements DisplayNameCollector
    {
        @Override
        public void addDisplayName(String code, Locale locale, Map<String, String> displayNames)
        {
            displayNames.put(code, locale.getDisplayLanguage());
        }
    }

    /**
     * Collects display names in the language of the supplied locale. If the language is not displayable by the
     * supplied font, the language name will be returned as written in the default locale.
     */
    private class NativeDisplayNameCollector implements DisplayNameCollector
    {

        private final Font font;

        public NativeDisplayNameCollector(Font font)
        {
            this.font = font;
        }

        @Override
        public void addDisplayName(String code, Locale locale, Map<String, String> displayNames)
        {
            String name = locale.getDisplayLanguage(locale);
            if (font.canDisplayUpTo(name) > -1)
            {
                // Font cannot render it; use language name in the spelling of the default locale.
                name = locale.getDisplayLanguage();
            }
            displayNames.put(code, name);
        }

    }

}
