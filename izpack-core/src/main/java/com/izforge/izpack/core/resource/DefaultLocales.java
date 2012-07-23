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

package com.izforge.izpack.core.resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;


/**
 * Default implementation of {@link Locales}.
 *
 * @author Tim Anderson
 */
public class DefaultLocales implements Locales
{

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The selected locale.
     */
    private Locale locale;


    /**
     * Supported locales.
     */
    private List<Locale> locales = new ArrayList<Locale>();

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(DefaultLocales.class.getName());

    /**
     * Constructs a {@code DefaultLocales}.
     * <p/
     * The locale will default to that of the current host if supported, else it will be set to the first supported
     * locale.
     *
     * @param resources the resources
     * @throws ResourceException if the locales can't be determined
     */
    @SuppressWarnings("unchecked")
    public DefaultLocales(Resources resources)
    {
        this.resources = resources;
        List<String> codes = getSupportedLocales();
        if (!codes.isEmpty())
        {
            Locale defaultLocale = Locale.getDefault();
            Map<String, Locale> iso3 = getLocalesByISO3(defaultLocale);
            for (String code : codes)
            {
                Locale locale = iso3.get(code);
                if (locale == null)
                {
                    logger.warning("No locale for: " + code);
                }
                else
                {
                    locales.add(locale);
                }
            }
            if (!locales.isEmpty())
            {
                if (locales.contains(defaultLocale))
                {
                    locale = defaultLocale;
                }
                else
                {
                    locale = locales.get(0);
                }
            }
        }
    }

    /**
     * Returns the current locale.
     *
     * @return the current locale. May be {@code null}
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * Sets the current locale.
     *
     * @param locale the locale. May be {@code null}
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * Returns the locale corresponding to the supplied ISO2/ISO3 language code.
     *
     * @param code the 2 or 3 character ISO language code
     * @return the corresponding locale, or {@code null} if the locale isn't supported
     */
    @Override
    public Locale getLocale(String code)
    {
        int length = code.length();
        for (Locale locale : locales)
        {
            if (length == 3)
            {
                if (code.equalsIgnoreCase(locale.getISO3Language()))
                {
                    return locale;
                }
            }
            else
            {
                if (code.equalsIgnoreCase(locale.getLanguage()))
                {
                    return locale;
                }
            }
        }
        return null;
    }

    /**
     * Returns the supported locales.
     *
     * @return the supported locales
     */
    @Override
    public List<Locale> getLocales()
    {
        return locales;
    }

    /**
     * Returns messages for the current locale.
     *
     * @return messages for the current locale
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws ResourceException         if no locale is set or the stream is not valid
     */
    @Override
    public Messages getMessages()
    {
        if (locale == null)
        {
            throw new ResourceException("No locale set");
        }
        InputStream in = resources.getInputStream("langpacks/" + locale.getISO3Language() + ".xml");
        return new LocaleDatabase(in, this);
    }

    /**
     * Returns the named messages for the current locale.
     *
     * @param name the message resource name
     * @return messages for the current locale
     * @throws ResourceNotFoundException if the named resource cannot be found
     */
    @Override
    public Messages getMessages(String name)
    {
        InputStream in = resources.getInputStream(name);
        return new LocaleDatabase(in, this);
    }

    /**
     * Returns the supported locales.
     *
     * @return the supported locales
     * @throws ResourceException if the supported locales exist but cannot be read
     */
    @SuppressWarnings("unchecked")
    public List<String> getSupportedLocales()
    {
        List<String> locales = null;
        try
        {
            locales = (List<String>) resources.getObject("langpacks.info");
        }
        catch (ResourceNotFoundException ignore)
        {
            // do nothing
        }
        return (locales != null) ? locales : Collections.<String>emptyList();
    }

    /**
     * Returns the available locales, keyed on their ISO3 language code.
     * <p/>
     * Where multiple locales exist for the same code the locale without a country will be selected.
     * <br/>
     * The exception to this is the {@code defaultLocale}, which will always be returned, unless it has no ISO3 language
     * code.
     *
     * @param defaultLocale the default locale
     * @return the locales
     */
    private Map<String, Locale> getLocalesByISO3(Locale defaultLocale)
    {
        String defaultCode = getISO3Language(defaultLocale);
        Map<String, Locale> iso3 = new HashMap<String, Locale>();
        if (defaultCode != null)
        {
            iso3.put(defaultCode, defaultLocale);
        }

        for (Locale locale : Locale.getAvailableLocales())
        {
            String code = getISO3Language(locale);
            if (!code.equals(defaultCode))
            {
                Locale existing = iso3.get(code);
                if (existing == null || locale.getCountry().isEmpty())
                {
                    iso3.put(code, locale);
                }
            }
        }
        return iso3;
    }

    /**
     * Returns the ISO3 language code for a locale.
     *
     * @param locale the locale
     * @return the locale's ISO3 language code, or {@code null} if it doesn't exist
     */
    private String getISO3Language(Locale locale)
    {
        String result;
        try
        {
            result = locale.getISO3Language();
        }
        catch (MissingResourceException ignore)
        {
            result = null;
        }
        return result;
    }

}
