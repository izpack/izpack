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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;


/**
 * Tests the {@link DefaultLocales} class.
 *
 * @author Tim Anderson
 */
public class DefaultLocalesTest
{

    /**
     * Basque ISO code.
     */
    public static final String BASQUE = "eus";

    /**
     * Galacian ISO code.
     */
    public static final String GALACIAN = "glg";

    /**
     * The supported ISO country and language codes. ISO country codes are uppercase, but IzPack expects them as
     * lowercase.
     */
    public static final List<String> ISO_CODES
            = Arrays.asList("bra", "cat", "ces", "chn", "dan", "deu", "ell", "eng", BASQUE, "fin", "fra", GALACIAN,
                            "hun", "idn", "ita", "jpn", "kor", "msa", "nld", "nor", "pol", "prt", "ron", "rus", "slk",
                            "spa", "srp", "swe", "tur", "twn", "ukr");


    /**
     * Verifies that the expected locales are returned.
     */
    @Test
    public void testLocales()
    {
        Resources resources = Mockito.mock(Resources.class);
        Mockito.when(resources.getObject("langpacks.info")).thenReturn(Arrays.asList("eng", "fra"));

        DefaultLocales locales = new DefaultLocales(resources);
        List<Locale> available = locales.getLocales();
        assertEquals(2, available.size());
        assertEquals("eng", available.get(0).getISO3Language());
        assertEquals("fra", available.get(1).getISO3Language());
    }

    /**
     * Verifies that each of the lang packs can be retrieved.
     * Note that lang packs for Galacian (<em>glg</em>) and Basque (<em>eus</em>) aren't supported out of the box
     * by the JVM, so may not be able to be retrieved.
     */
    @Test
    public void testLangPacks()
    {
        ResourceManager resources = createResourcesForMessages();
        Locales locales = new DefaultLocales(resources);
        resources.setLocales(locales);
        for (String code : ISO_CODES)
        {
            Locale locale = locales.getLocale(code);
            if (locale == null && !BASQUE.equals(code) && !GALACIAN.equals(code))
            {
                fail("Failed to retrieve locale for code=" + code);
            }
            else if (locale != null)
            {
                locales.setLocale(code);
                assertNotNull("Failed to retrieve messages for code=" + code, locales.getMessages());
                assertEquals(code.toLowerCase(), locales.getISOCode().toLowerCase());
            }
        }
    }

    /**
     * Verifies that the appropriate locale is selected if the language code is "en" (English).
     */
    @Test
    public void testEnglish()
    {
        // verify that English language countries resolve to "eng"
        checkDefaultLocale("eng", "en", "");    // English
        checkDefaultLocale("eng", "en", "AU");  // Australia
        checkDefaultLocale("eng", "en", "GB");  // Great Britain
        checkDefaultLocale("eng", "en", "US");  // United States
    }

    /**
     * Verifies that the appropriate locale is selected if the language code is "zh" (Chinese).
     */
    @Test
    public void testChinese()
    {
        // check selection of default locale
        checkDefaultLocale("CHN", "zh", "");       // China
        checkDefaultLocale("CHN", "zh", "CN");
        checkDefaultLocale("TWN", "zh", "TW");     // Taiwan

        Locale defaultLocale = new Locale("zh", ""); // language code with no country

        // look up locale by 3 character ISO country code, using the uppercase code as per ISO 3166
        checkLocale("CHN", "CHN", "zh", "CN", defaultLocale);
        checkLocale("TWN", "TWN", "zh", "TW", defaultLocale);

        // same again, but using lowercase version of the code
        checkLocale("chn", "CHN", "zh", "CN", defaultLocale);
        checkLocale("twn", "TWN", "zh", "TW", defaultLocale);

        // look up locale by 2 character ISO country code, using the uppercase code as per ISO 3166
        checkLocale("CN", "CHN", "zh", "CN", defaultLocale);
        checkLocale("TW", "TWN", "zh", "TW", defaultLocale);

        // same again, but using lowercase version of the code.
        checkLocale("cn", "CHN", "zh", "CN", defaultLocale);
        checkLocale("tw", "TWN", "zh", "TW", defaultLocale);

        // now lookup using language code. These resolve to CHN as China is before Taiwan in the ISO code list above.
        checkLocale("zho", "CHN", "zh", "CN", defaultLocale);
        checkLocale("zh", "CHN", "zh", "CN", defaultLocale);
    }

    /**
     * Verifies that Brazilian Portuguese (pt_BR) and Portuguese (pt_PT) can be supported.
     */
    @Test
    public void testPortuguese()
    {
        checkDefaultLocale("PRT", "pt", "");      // Portugal
        checkDefaultLocale("PRT", "pt", "PT");
        checkDefaultLocale("BRA", "pt", "BR");    // Brazil

        Locale defaultLocale = new Locale("pt", "BR"); // Brazilian Portuguese

        // look up locale by 3 character ISO country code, using the uppercase code as per ISO 3166
        checkLocale("BRA", "BRA", "pt", "BR", defaultLocale);
        checkLocale("PRT", "PRT", "pt", "PT", defaultLocale);

        // same again, but using lowercase version of the code
        checkLocale("bra", "BRA", "pt", "BR", defaultLocale);
        checkLocale("prt", "PRT", "pt", "PT", defaultLocale);

        // look up locale by 2 character ISO country code, using the uppercase code as per ISO 3166
        checkLocale("BR", "BRA", "pt", "BR", defaultLocale);
        checkLocale("PT", "PRT", "pt", "PT", defaultLocale);

        // same again, but using lowercase version of the code. Note that the language code for Portuguese is also
        // "pt"
        checkLocale("br", "BRA", "pt", "BR", defaultLocale);
        checkLocale("pt", "PRT", "pt", "PT", defaultLocale);
    }

/*
    @Test
    public void dumpLocales()
    {
        for (Locale locale : Locale.getAvailableLocales())
        {
            System.out.println("language=" + locale.getLanguage() + "," + locale.getISO3Language() + "," +
                                       locale.getDisplayLanguage() + ", country=" + locale.getCountry() +
                                       "," + locale.getISO3Country() + "," + locale.getDisplayCountry() + ", " +
                                       "display name=" + locale.getDisplayName());
        }
    }
*/

    /**
     * Verifies that the expected locale is selected by default when the locales are constructed.
     *
     * @param expectedCode the expected ISO code
     * @param language     the 2 character language code
     * @param country      the 2 character country code, or empty
     */
    private void checkDefaultLocale(String expectedCode, String language, String country)
    {
        Locale locale = new Locale(language, country);
        ResourceManager resources = createResourcesForMessages();
        Locales locales = new DefaultLocales(resources, locale);
        resources.setLocales(locales);
        assertNotNull(locales.getLocale());
        assertEquals(expectedCode, locales.getISOCode());
    }

    /**
     * Checks looking up a locale.
     *
     * @param lookupCode       the ISO code to look up the locale with. May be a 2 or 3 letter ISO code, in any case
     * @param expectedISO      the expected ISO code
     * @param expectedLanguage the expected 2 character language code
     * @param expectedCountry  the expected 2 character country code
     * @param defaultLocale    the default locale
     */
    private void checkLocale(String lookupCode, String expectedISO, String expectedLanguage, String expectedCountry,
                             Locale defaultLocale)
    {
        ResourceManager resources = createResourcesForMessages();
        Locales locales = new DefaultLocales(resources, defaultLocale);
        resources.setLocales(locales);

        locales.setLocale(lookupCode);
        assertNotNull(locales.getLocale());
        assertEquals(expectedISO, locales.getISOCode());
        assertEquals(expectedCountry, locales.getLocale().getCountry());
        assertEquals(expectedLanguage, locales.getLocale().getLanguage());
    }

    /**
     * Helper to create a resource manager that provides access to all supported language packs.
     *
     * @return a new resource manager
     */
    private ResourceManager createResourcesForMessages()
    {
        ResourceManager resources = new ResourceManager()
        {
            @Override
            public Object getObject(String name)
            {
                if (name.equals("langpacks.info"))
                {
                    return ISO_CODES;
                }
                return super.getObject(name);
            }

            @Override
            public InputStream getInputStream(String resource)
            {
                resource = resource.replaceFirst("^langpacks", "installer");
                return super.getInputStream(resource);
            }
        };
        resources.setResourceBasePath("/com/izforge/izpack/bin/langpacks/");
        return resources;
    }


}
