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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
     * The default messages for the locale.
     */
    private Messages messages;

    /**
     * The code used to select the current locale's default messages.
     */
    private String isoCode;

    /**
     * The supported locales, keyed on 2 and 3 letter language codes and 3 letter country codes.
     */
    private Map<String, Locale> isoLocales = new LinkedHashMap<String, Locale>();

    /**
     * The locales.
     */
    private List<Locale> locales = new ArrayList<Locale>();

    /**
     * The ISO codes as they were specified by <em>langpacks.info</em>.
     */
    private List<String> isoCodes = new ArrayList<String>();

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(DefaultLocales.class.getName());


    /**
     * Constructs a {@code DefaultLocales}.
     * <p/>
     * The locale will default to that of the current host if supported, else it will be set to the first supported
     * locale.
     *
     * @param resources the resources
     * @throws ResourceException if the locales can't be determined
     */
    @SuppressWarnings("unchecked")
    public DefaultLocales(Resources resources)
    {
        this(resources, Locale.getDefault());
    }

    /**
     * Constructs a {@code DefaultLocales}.
     * <p/>
     * The locale will default to that supplied if supported, else it will be set to the first supported locale.
     *
     * @param resources the resources
     * @throws ResourceException if the locales can't be determined
     */
    public DefaultLocales(Resources resources, Locale defaultLocale)
    {
        this.resources = resources;
        List<String> codes = getSupportedLocales();
        if (!codes.isEmpty())
        {
            Map<String, Locale> available = getAvailableLocales(defaultLocale);
            for (String code : codes)
            {
                String key = code.toUpperCase();
                Locale locale = available.get(key);    // check to see if its a country code match
                if (locale == null)
                {
                    key = code.toLowerCase();
                    locale = available.get(key);       // check to see if its a language code match
                }
                if (locale == null)
                {
                    logger.warning("No locale for: " + code);
                }
                else
                {
                    locales.add(locale);
                    addLocale(isoLocales, locale);
                    isoCodes.add(code);
                }
            }
            if (!locales.isEmpty())
            {
                // use the default locale to select the language pack if one is available, otherwise select the first
                // supported locale
                if (!changeLocale(defaultLocale.getCountry()) && !changeLocale(defaultLocale.getLanguage()))
                {
                    for (String code : codes)
                    {
                        if (changeLocale(code))
                        {
                            break;
                        }
                    }
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
     * <p/>
     * This uses a 2 or 3 letter ISO country or language code to locate the corresponding locale.
     *
     * @param code the ISO code
     * @throws ResourceNotFoundException if the locale isn't supported
     */
    @Override
    public void setLocale(String code)
    {
        if (!changeLocale(code))
        {
            throw new ResourceNotFoundException("No locale found for code: " + code);
        }
    }

    /**
     * Returns the current locale's 3 character ISO code.
     * <p/>
     * This is the code that was used to select the locale's messages and may be a country or language code.
     *
     * @return the current locale's ISO code, or {@code null} if there is no current locale
     */
    @Override
    public String getISOCode()
    {
        return isoCode;
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
        Locale locale = findByCountry(code);
        if (locale == null)
        {
            locale = findByLanguage(code);
        }
        return locale;
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
     * Returns the 3 character ISO codes of the supported locales.
     * <p/>
     * For backward compatibility:
     * <ol>
     * <li>these should all be lowercase; and</li>
     * <li>may be a mix of language and country codes</li>
     * </ol>
     *
     * @return the ISO codes
     */
    @Override
    public List<String> getISOCodes()
    {
        return isoCodes;
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
        if (messages == null)
        {
            throw new ResourceNotFoundException("Cannot find messages for locale: " + locale.getLanguage());
        }
        return messages;
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
     * Returns the available locales.
     *
     * @param defaultLocale the default locale
     * @return the available locales, keyed on 2 and 3 character ISO language and country codes
     */
    private Map<String, Locale> getAvailableLocales(Locale defaultLocale)
    {
        Map<String, Locale> available = new HashMap<String, Locale>();
        addLocale(available, defaultLocale);
        for (Locale locale : Locale.getAvailableLocales())
        {
            addLocale(available, locale, defaultLocale);
        }
        return available;
    }

    /**
     * Adds a locale to the supplied locales, keying it on its 2 and 3 character ISO country and language codes.
     *
     * @param locales the locales to add to
     * @param locale  the locale to add
     */
    private void addLocale(Map<String, Locale> locales, Locale locale)
    {
        addLocale(locales, locale, null);
    }

    /**
     * Adds a locale to the supplied locales, keying it on its 2 and 3 character ISO country and language codes.
     * <p/>
     * Where multiple locales have the same language code, the locale not associated with a country will be used,
     * unless it is the default locale.
     *
     * @param locales       the locales to add to
     * @param locale        the locale to add
     * @param defaultLocale the default locale. May be {@code null}
     */
    private void addLocale(Map<String, Locale> locales, Locale locale, Locale defaultLocale)
    {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        Locale existing = locales.get(language);

        // add mapping for language codes, if:
        // * no mapping exists for the 2 character code;  or
        // * a mapping exists that isn't the default locale and has a country, and the new mapping has no country
        if (existing == null || (existing != defaultLocale && !"".equals(existing.getCountry()) && "".equals(country)))
        {
            // use locales not associated with a particular country by preference, unless its the default locale
            if (!"".equals(language))
            {
                locales.put(language, locale);
            }
            String language3 = LocaleHelper.getISO3Language(locale);
            if (language3 != null)
            {
                locales.put(language3, locale);
            }
        }

        // add mapping for 2 character country code
        if (!"".equals(country))
        {
            locales.put(country, locale);
        }

        // add mapping for 3 character country code
        String country3 = LocaleHelper.getISO3Country(locale);
        if (country3 != null)
        {
            locales.put(country3, locale);
        }
    }

    /**
     * Changes the locale.
     *
     * @param code the 2 or 3 character ISO language code
     * @return {@code true} if the locale was changed
     */
    private boolean changeLocale(String code)
    {
        if (code == null || code.equals(""))
        {
            locale = null;
        }
        else
        {
		    		Messages parentMessages = messages;
            messages = null;
            locale = findByCountry(code);
            if (locale == null)
            {
                locale = findByLanguage(code);
            }
            if (locale != null)
            {
                InputStream in = null;
                String country = LocaleHelper.getISO3Country(locale);
                if (country != null)
                {
                    in = getMessagesStream(country.toLowerCase()); // must be lowercase for backwards compatibility
                    isoCode = country;
                }
                if (in == null)
                {
                    String language = LocaleHelper.getISO3Language(locale);
                    if (language != null)
                    {
                        in = getMessagesStream(language);
                        isoCode = language;
                    }
                }
                if (in == null)
                {
                    logger.warning("Cannot find messages for locale: " + code);
                }
                else
                {
                    messages = new LocaleDatabase(in, parentMessages, this);
                }
            }
        }
        return locale != null;
    }

    /**
     * Returns a stream to the messages for the given ISO code.
     *
     * @param code the 2 or 3 character ISO language code
     * @return the stream, or {@code null} if none was found
     */
    private InputStream getMessagesStream(String code)
    {
        InputStream result = null;
        try
        {
            String path = "langpacks/" + code + ".xml";
            result = resources.getInputStream(path);
        }
        catch (ResourceNotFoundException ignore)
        {
            logger.fine("Locale has no langpack for code: " + code);
        }
        return result;
    }

    /**
     * Returns the supported locales.
     *
     * @return the supported locales
     * @throws ResourceException if the supported locales exist but cannot be read
     */
    @SuppressWarnings("unchecked")
    private List<String> getSupportedLocales()
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
     * Returns a locale given its language code.
     *
     * @param code the language code. May be a 2 or 3 character ISO code
     * @return the corresponding locale or {@code null} if none is found
     */
    private Locale findByLanguage(String code)
    {
        return isoLocales.get(code.toLowerCase());
    }

    /**
     * Returns a locale given its country code.
     *
     * @param code the country code. May be a 2 or 3 character ISO code
     * @return the corresponding locale or {@code null} if none is found
     */
    private Locale findByCountry(String code)
    {
        return isoLocales.get(code.toUpperCase());
    }


}
